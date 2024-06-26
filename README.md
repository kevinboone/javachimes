# JavaChimes 1.0b

v0.1b, June 2024

## What is this?

JavaChimes is a 'wind-chime simulator' written entirely in Java. 
Or, more pompously, it's a aleatory music generator.
It simulates
the sound generated by a mechanical wind-chime using MIDI notes generated using
the MIDI classes, defined in the Java sound API. In operation JavaChimes can
produce a fairly realistic imitation of a 'real' wind-chime, with appropriate
tweaking. There is no graphical user interface (yet); the program is designed
to be run from the command line. Because it is written entirely in Java,
JavaChimes should work on any system with a Java JVM that supports the Java
sound API, and which has a soundcard. By default, JavaChimes will use the
sampled sound generator built into the Java virtual machine, but it can be
configured to use an external MIDI device if you have one. Java JVMs
after about JDK 1.6 allow the use of 3rd-party soundbank files, and
these can hugely improve the sound quality. 

JavaChimes allows the tones, number of chimes, wind properties, and MIDI
characteristics to be adjusted using command-line switches.

JavaChimes is distributed as source code and a Java JAR archive, 
which can be run directly using Java. There is no need to build from
source, unless you want to.

JavaChimes is very old program -- I wrote it in the days of JDK 1.3, long
before Maven. Some parts date back to 2001. 
I updated it in 2020 to build with Maven -- no other changes were
necessary. Isn't Java wonderful ? ;) As of 2024, it definitely works with
OpenJDK Java 8, 11, and 17.  My impression is that the quality of Java's
internal sound generation has declined over the years, but perhaps I've just
gotten fussier.

Although it's old, and there are better programs available, JavaChimes has a
special place in my affections, because it's my longest-lived Java project. 

Although I haven't run it on anything but Linux for at least ten years,
Java being what it is I expect JavaChimes to work on other platforms. 

## Pre-requisites

To use JavaChimes you will need the following.

- Java JDK version 1.3 or later :) Although the code is more than 20 years
old, it works fine with Java 8-17.

- Java sound API classes, appropriate for your platform, if not
included in your Java run-time (they always are these days, I think).

- A sound generating device supported by the sound API, or a supported MIDI
output device and a separate MIDI instrument. All Java JVMs since 1.3 have
a built in sound generator, of variable quality.


## Building JavaChimes

Install Maven, and then run

    $ mvn package

This generates the file `target/javachimes-[version]-with-dependencies.jar`. 
This JAR is self-contained, but it may be useful to rename it to something
less ungainly (e.g., `javachimes.jar`) and copy it to a convenient directory.

## Installing JavaChimes

JavaChimes doesn't require any particular installation, but you may like
to create a script or batch file to run it more easily at the prompt.
For Linux, there's a script `samples/install_linux.sh` to do that. The
script creates an executable called `javachimes`.


## Running JavaChimes

JavaChimes is designed to be run from the command line (prompt), and has no
graphical user interface (at least so far). 
which includes full source code. To run the program directly from the JAR file,
simply do this at the prompt:

    java -jar /path/to/javachimes.jar

This will run the program with defaults for the various tweakable parameters.
All parameters are specified after the basic command line.

Note that the example above assumed that the `java' program (or `java.exe` on
Windows systems) is in the system's search path; if not you may have to
enter the full path of the java executable).

## Command-line parameters

_-a, --channel (1-16)_ set MIDI channel

Sets the MIDI output channel, in the range 1-16. This is not particularly
relevant for soundboard-based synthesizers, as they typically respond on all
channels. However, it may well be important for external sound generators. In
all cases, bear in mind the General MIDI specifications call for channel 10 to
be reserved for percussion effects. The default value is 1.

_-b, --basenote (1-128)_ MIDI base note 

The base (root) note of the scale, when using one of the built-in scales.
This is specified as a MIDI note number, where middle C is '60' and adding one
increases the pitch by a semitone (so the C two octaves above middle C is 84
-- 60 + 12 + 12). This parameter is ignored when the --scale switch is in use.
The default value is 85.

_-c, --scale (n1,n2,n3...)_ scale

Specifies the exact set of notes to use, as MIDI note numbers separated
by commas.

_-d, --onduration (seconds)_ note-on duration 

Set the note-on duration. See the 'notes' section below. The default
is two seconds.

_-e, --device (number)_ MIDI device

Specify the MIDI device. The default is to use the first one known to the
JVM, which is usual the internal sound generator.

_-f, --soundfont (file)_ Select a soundfont file

Java usually supports soundbanks in SoundFont 2 format; files usually have
names ending in `.sf2`. Some JVMs may support other formats. See 'resources'
section below for information about configuring memory, if necessary.
If this option is not given, JavaChimes will use default instrument sounds.

_-g, --gustyness (0-100)_ 'gustyness' 

Sets the 'gustyness' of the wind; this affects the variability in the
time delays between chimes. See 'algorithm' below. The default value is
30.

-l, --listdevs                   

Lists the know MIDI devices with their numbers. The number can then be used
as a parameter to the `--device` switch to select from a range of installed
devices or drivers.

_-n, --numchimes (1-10)_ number of chimes

Select the number of chimes to use. '1' is allowed, but not very interesting.
The default is 5. 

_-p, --program (1-128)_ MIDI program number 

Selects the program (voice, instrument) to use. See notes below. Default
is 14: usually xylophone or bell.

_-s, --seconds (number)_  duration of play

Specifies the number of seconds for which to play. The default is to
play forever, until the program is forcibly stopped by control-C or 
whatever.

_-t, --scaletype (0-11)_ scale type 

Sets which of the pre-defined scales to use. The default is '8', a 
pentatonic scale. This switch is over-ridden by '--scale'. See
"Scales" below.

_-w, --windspeed (1-100)_ wind speed 

Sets the wind-speed, which affects the rate production of sound events.
Default is 20.

_-v, --version_ show version

_-h, --help, -?_ displays help message

Defaults are used for all command-line parameters that are not specified.
Example command-line usage is given later in this document.


## Algorithm 

The rate and loudness of tones is determines by two parameters which I have
called 'windspeed' (w) and (lacking a better term) 'gustyness' (g). OK, so
there's no such word as 'gustyness'; pedants please don't write in.  The
average rate of generation is proportional to the windspeed parameter, while
'gustyness' controls the variability.  If g=0, then the rate of generation of
tones will be approximately constant.  Technically, the rate of generation (in
seconds) is randomly selected from a normal distribution whose mean is w/10,
and whose standard deviation is g/10. Dividing by 10 allows generation rates
of less than one chime per second while still using only integer parameters.

Of course, some sensible values of g and w will allow occasional negative
values of rate of generation. The algorithm silently converts these to a rate
of one chime per second.

A low windspeed (e.g., < 10), coupled with a high gustyness (e.g., > 20) gives
a pattern of intense bursts of activity separated by long pauses, which is
quite typical or real wind-chimes.
However, the long periods of silence
might make it difficult to tell whether the program is working.


## Scales

JavaChimes supports a number of pre-defined scales, or an exact scale can be
specified on the command line. The default is to use a pentatonic scale.

### 1. Using pre-defined scales

JavaChimes recognizes a number of pre-defined scales; the choice is made using
the `-t` or `--scale` command-line switch, like this:

    -t [scale_number]

or

    --scale [scale_number]

scale\_number can be any of the following values:

0: major (7 notes) 
1: natural minor (7 notes) 
2: harmonic minor (7 notes)  
3: melodic minor (7 notes)  
4: enigmatic (7 notes)  
5: chromatic (11 notes)  
6: arpeggio (3 notes)  
7: dominant ('barbershop') seventh (4 notes)  
8: pentatonic (5 notes) (this is the default)  
9: Greek pentatonic (5 notes)  
10: Gregorian (7 notes)  
11: Hijazi (7 notes)  

As well as the scale, you can specify a base note, and a number of chimes. The
program selects actual note values randomly using these parameters.  Best
results are often achieved by using a number of chimes one greater than the
number of notes in the scale, so that the base note appears twice, separated by
an octave. In any event, there will always be as many chimes as specified, even
if the scale selected does not have enough notes; the program simply selects
notes that span multiple octaves. The relevant command line switches are:

-n: number of chimes 

-b: base note (0-127) 

-t: scale type (0-6 from list above)

### 2. Specify the scale exactly. With this method the program must be supplied
with a list of actual MIDI note codes. The base note and number of chimes are
ignored even if specified on the command line.

For example, here is an example that spans two octaves:

javachimes -c 70,75,77,82,87,89


## Handling multiple devices

Some systems will have more than one available MIDI device; for example,
it may be possible to have the same MIDI events handled by a built-in
tone generator, or handled by some kind of wavetable synthesizer. Most
systems use multiple device drivers to handle these different sound
generation techniques.

In general, the names or numbers of the available MIDI devices in the
Java sound API bear little relationship to the drivers and devices as 
defined at the system level. JavaChimes allows the supported
devices to be listed using the `--listdevs` switch. This produces a
numbered list of devices; the appropriate number can then be used as
an argument to the `--device` switch. If this is not done, then JavaChimes
uses the default synthesizer device, whatever that happens to be. On
the Linux systems I have tried, this uses software-based wavetable
synthesis, even on systems with a hardware wavetable device. Although
somewhat inefficient, sound quality is reasonable.


## MIDI channels

Basic MIDI devices support 16 channels, usually labelled '1' to '16'.
JavaChimes allows the output channel to be set in this range; if not set
channel 1 is used.. Bizarre results may be expected if a channel is selected
that is not supported by the hardware. In general, the channel is irrelevant
for internal sound generation (i.e., not using an external MIDI instrument),
except that many systems use channel 10 for percussion effects.
 

## Voice selection

The `-p` or `--program` switches set the instrument (voice) for output, in 
the range 1-128. The sounds associated with these voices tend to depend on
the whim of the hardware/driver vendors, but most devices attempt to follow
the 'General MIDI' (GM) specification. The default voice is number 14, which is
officially 'xylophone'. This sounds better on some systems than others.
The full list of GM voices can be obtained from
[Wikipedia](https://en.wikipedia.org/wiki/General_MIDI),
but the best approach is probably to try all the numbers until you find
which ones sound best in this application. The choice of program number
will depend on the pitch and scale you use, and on the specific
Java JVM you have, if you're using the internal sound generator.


## Soundbank support

Version 0.1b can supply the JVM's built-in sound generator with a soundbank
file. I believe all JVMs from JDK 1.6 onwards support SoundFont 2 (SF2)
soundbank files; some JVMs might support more. 

To use a soundbank file, just specify it using the `--soundfont` 
switch.

A good, free SF2 soundbank is Frank Wren's _Fluid R3_. This is 
available, at the time of writing, from the
[keymusician](https://keymusician01.s3.amazonaws.com/FluidR3_GM.zip)
site. Programs 8-15 are particularly effect, but some of the
plucked string and percussion sounds are also useful.

Be aware that using a large soundbank like this will radically increase
the amount of memory the program uses.

## Examples

    java -jar javachimes.jar

Play indefinitely using defaults for all parameters

    java -jar javachimes.jar -w 50 -g 50 -t 10 -n 8

Play indefinitely using wind-speed and gustyness of 50 (out of 100), using
  scale 10 (gregorian) and 8 chimes.

    java -jar javachimes.jar -l 

Get a list of MIDI devices and their numbers

    java -jar javachimes.jar -e 3 -s 10

Play using default properties on MIDI device 3, for 10 seconds 

    play -jar javachimes.jar -c 70,75,77,82,87,89

Play indefinitely using chimes set to the specified MIDI note numbers


## Notes

### Note-on duration

A 'real' wind chime has no note-on duration, that is, when a chime is struck
it sounds with decreasing amplitude until too faint to hear. A MIDI synth
device, however, expects a note that is struck to be released at some later
stage. While some devices will silently release notes that have become too
quiet to hear, not all will, and even if they do there is no compelling reason
to continue to allocate CPU time to processing inaudible notes. On the other
hand, if a note-off event is sent before the sound has decayed naturally, the
sound will be peculiar.


### Strike velocity profile

Because JavaChimes aims to be a realistic simulation, a greater windspeed
leads to louder chimes; this simulates the hammer striking the chimes harder.
This effect is achieved by determining the MIDI 'note on velocity' from the
instantaneous wind speed. If you MIDI device/emulator is capable of responding
to velocity commands with a change of timbre as well as volume, then the
effect should be even more realistic. However, the velocity is 'normalized'
such that the maximum permissible velocity is assigned to 'peak' wind-speed
values for the specified mean wind-speed. This may not be strictly accurate,
but the alternative is to require a volume modification whenever changing the
wind-speed setting, which I expect most people would find inconvenient.

### Resources 

JavaChimes uses more memory and CPU if the rate of sound generation is higher,
or if the note on duration is longer. However, the most significant
determinant of memory usage is the soundbank. With the default
instruments, JavaChimes can be made to run with as little as 8Mb of
JVM heap (`-Xmx8m`). With a substantial SoundFont file, however, it
might need as much as 256Mb. CPU usage varies between about 2% and
about 20% -- the latter when sounds are almost continuous.

You can use the environment variable `JAVA_OPTS` to pass memory
(and other) settings to the JVM.

    JAVA_OPTS=-Xmx8mb javachimes

### Limitations

- The Java sound API considers 'sequencers' and 'synthesizers' to be MIDI
  devices, and the `--listdevs` option lists both as they can't easily be
  distinguished. However, JavaChimes can't reliably output via a sequencer
  device, only a synthesizer.  Unpredictable results may be expected if an
  inappropriate output device is used. 

- An unfortunate limitation of the algorithm is that tones will never be
  struck simultaneously, and that's something that can sometimes happen
  with real chimes. 

- There's no graphical user interface.

## Author and copyright

JavaChimes was written by Kevin Boone and is Copyright (c)2003-2024 Kevin
Boone. It is distributed under the terms of the GNU Public License v3.0, a copy
of which should be included with the distribution. In summary, you may do
anything you like with this software so long as the original author is
acknowledged. There is no warranty of any kind. 

## Revision history

0.1, 2003  
First working version, for JDK 1.3

0.1a, 2020  
Added Maven build, rather than shell scripts

0.1b, June 2024  
Added soundfont support. Added Linux installer. Added man page.
Updated documentation.


