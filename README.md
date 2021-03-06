# JavaChimes 1.0


## What is this?

JavaChimes is a `wind-chime simulator' written entirely in Java. It simulates
the sound generated by a mechanical wind-chime using MIDI notes generated
using the MIDI classes defined in the Java sound API. In operation
JavaChimes can produce a fairly realistic imitation of a `real' wind-chime,
with appropriate tweaking. There is no graphical user interface (yet); the
program is designed to be run from the command line. Because it is written
entirely in Java, JavaChimes should work on any system that supports the
Java sound API and has appropriate hardware (e.g., a soundboard). 

JavaChimes allows the notes, wind properties, and MIDI characteristics
to be adjusted using command-line switches.

JavaChimes is distributed as a Java JAR archive, which includes both the
compiled classes and the Java source code.

JavaChimes is very old program -- I wrote it in the days of JDK 1.3,
long before Maven. I updated it in 2020 to build with Maven -- no
other changes were necessary. Isn't Java wonderful ? ;)


## Pre-requisites

To use JavaChimes you will need the following.

- Java JDK version 1.2 or later, or equivalent; I recommend version 1.3 or later, as support for the sound API is built in. Although this code is 18 years old now, it works fine with JDK 1.8 

- Java sound API classes, appropriate for your platform, if this is not
included in your Java run-time (it usually is)

- A sound generating device supported by the sound API, or a supported MIDI
output device and a separate MIDI instrument


## Building JavaChimes

Install Maven, and then run

    $ mvn package

This generates the file `target/javachimes-[version]-with-dependencies.jar`. 
This JAR is self-contained, but it may be useful to rename it to something
less ungainly (e.g., javachimes.jar) and copy it to a convenient directory.

## Running JavaChimes

JavaChimes is designed to be run from the command line (prompt), and has no
graphical user interface (at least so far). 
which includes full source code. To run the program directly from the JAR file,
simply do this at the prompt:

    java -jar /path/to/javachimes.jar

This will run the program with defaults for the various tweakable parameters.
All parameters are specified after the basic command line.

Note that the example above assumed that the `java' program (or `java.exe' on
Windows systems) is in the system's search path; if not you may have to
enter the full path of the java executable).

The command-line parameters are listed in the next section.


## Command-line parameters

-a, --channel channel\_number     set MIDI channel

Sets the MIDI output channel, in the range 1-16. This is not particularly
relevant for soundboard-based synthesizers, as they typically respond on all
channels. However, it may well be important for external sound generators. In
all cases, bear in mind the General MIDI specifications call for channel 10 to
be reserved for percussion effects.

-b, --basenote note\_number       MIDI base note (1-128)

The base (root) note of the scale, when using one of the built-in scales.
This is specified as a MIDI note number, where middle C is `60' and adding one
increases the pitch by a semitone (so the C two octaves above middle C is 84
-- 60 + 12 + 12). This parameter is ignored when the --scale switch is in use.

-c, --scale interval_list        scale

Specifies the exact set of notes to use, as MIDI note numbers separated
by commas.

-d, --onduration on_duration    note-on duration (sec)

Set the note-on duration. See the `notes' section below.

-e, --device device\_number       select device
Lists the know MIDI devices with their numbers. The number can then be used
as a parameter to the --device switch to select from a range of installed
devices or drivers.

-g, --gustyness gustyness        `gustyness' (0-100)

Sets the `gustyness' of the wind; this affects the variability in the
time delays between chimes. See `algorithm' below.

-l, --listdevs                   

Lists the know MIDI devices with their numbers. The number can then be used
as a parameter to the --device switch to select from a range of installed
devices or drivers.

-n, --numchimes num_chimes       number of chimes (1-10)

Select the number of chimes to use. `1' is legal, but not very interesting.

-p, --program prog\_number        MIDI program number (1-128)

Selects the program (voice, instrument) to use. See notes below.

-s, --seconds play\_duration      duration of play (seconds)

Specifies the number of seconds for which to play. The default is to
play forever, until the program is forcibly stopped by control-C or 
whatever.

-t, --scaletype scale\_type       scale type (0-8)

Sets which of the pre-defined scales to use. The default is `8', a 
pentatonic scale. This switch is over-ridden by `--scale'. 

-w, --windspeed wind\_speed       wind speed (1-100)

Sets the wind-speed, which affects the rate of sound events.

-v, --version                    show version

-h, --help                       displays help message

-?                               same as --help

Defaults are used for all command-line parameters that are not specified.
Example command-line usage is given later in this document.


## Defaults

These are the defaults that JavaChimes uses for tweakable parameters that
are not specified on the command line.

MIDI output channel (1-16): 1
Wind-speed (1-100): 20
Wind gustyness (0-100): 30
Base note (0-127): 85
Note-on duration: 2 sec
Number of chimes (1-10): 5
Scale (0-11): 8 (pentatonic; see list of scales below) 


## Algorithm 

The rate and loudness of tones is determines by two parameters which I have
called `windspeed' (w) and (lacking a better term) `gustyness' (g). OK, so
there's no such word as `gustyness'; pedants please don't write in.  The
average rate of generation is proportional to the windspeed parameter, while
`gustyness' controls the variability.  If g=0, then the rate of generation of
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


## Selecting a scale for the chimes

JavaChimes supports a number of pre-defined scales, or an exact scale can be
specified on the command line. The default is to use a pentatonic scale.

### 1. Using pre-defined scales

JavaChimes recognizes a number of pre-defined scales; the choice is made using
the `-t' or '--scale' command-line switch, like this:

-t [scale_number]

or

--scale [scale_number]

scale_number can be any of the following values:

0: major (7 notes) 
1: natural minor (7 notes)  
2: harmonic minor (7 notes)  
3: melodic minor (7 notes)  
4: enigmatic (7 notes)  
5: chromatic (11 notes)  
6: arpeggio (3 notes)  
7: dominant (`barbershop') seventh (4 notes)  
8: pentatonic (5 notes) (this is the default)  
9: Greek pentatonic (5 notes)  
10: Gregorian (7 notes)  
11: Hijazi (7 notes)  

As well as the scale, you can specify a base note, and a number of chimes. The
program determines actual note values from these parameters. Best results are
often achieved by using a number of chimes one greater than the number of
notes in the scale, so that the base note appears twice, separated by an
octave. In any event, there will always be as many chimes as specified, even
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
devices to be listed using the `--listdevs' switch. This produces a
numbered list of devices; the appropriate number can then be used as
an argument to the `--device' switch. If this is not done, then JavaChimes
uses the default synthesizer device, whatever that happens to be. On
the Linux systems I have tried, this uses software-based wavetable
synthesis, even on systems with a hardware wavetable device. Although
somewhat inefficient, sound quality is reasonable.


## MIDI channels

Basic MIDI devices support 16 channels, usually labelled `1' to `16'.
JavaChimes allows the output channel to be set in this range; if not set
channel 1 is used.. Bizarre results may be expected if a channel is selected
that is not supported by the hardware. In general, the channel is irrelevant
for internal sound generation (i.e., not using an external MIDI instrument),
except that many systems use channel 10 for percussion effects.
 

## Voice selection

The `-p' or `--program' switches set the instrument (voice) for output, in 
the range 1-128. The sounds associated with these voices tend to depend on
the whim of the hardware/driver vendors, but most devices attempt to follow
the `General MIDI' (GM) specification. The default voice is number 15, which is
officially `tubular bells'. This sounds better on some systems than others.
The full list of GM voices can be obtained from

http://www.midi.org/about-midi/specinfo.htm#MIDISPEC

but the best approach is probably to try all the numbers until you find
which ones sound best in this application.


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

A `real' wind chime has no note-on duration, that is, when a chime is struck
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
This effect is achieved by determining the MIDI `note on velocity' from the
instantaneous wind speed. If you MIDI device/emulator is capable of responding
to velocity commands with a change of timbre as well as volume, then the
effect should be even more realistic. However, the velocity is `normalized'
such that the maximum permissible velocity is assigned to `peak' wind-speed
values for the specified mean wind-speed. This may not be strictly accurate,
but the alternative is to require a volume modification whenever changing the
wind-speed setting, which I expect most people would find inconvenient.


### Limitations

- The Java sound API considers `sequencers' and `synthesizers' to be MIDI
devices, and the `--listdevs' option lists both as they can't easily be
distinguished. However, JavaChimes can't reliably output via a sequencer
device, only a synthesizer.  Unpredictable results may be expected if an
inappropriate output device is used. 

- JavaChimes does not support soundbanks or related technologies. It always
selects the default voice set for the output device.


## Author and copyright
JavaChimes was written by Kevin Boone and is Copyright (c)2001-2002 Kevin Boone. It is
distributed under the terms of the GNU Public License v3.0, a copy of 
which should
be included with the distribution. In summary, you may do anything you like
with this software so long as the original author is acknowledged. There is
no warranty of any kind. 





