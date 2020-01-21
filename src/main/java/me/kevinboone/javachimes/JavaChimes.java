package me.kevinboone.javachimes;
import javax.sound.midi.*;
import java.util.*;

/**
*/
public class JavaChimes extends Thread 
{
/*
Define the scales available, and constants to allow
them to be selected. Note that the first note is always
`0', that is, the same as the base note. The values in the
arrays are semitones above the base note
*/
public static int[] majorScale = {0, 2, 4, 5, 7, 9, 11};
public static int[] minorScale = {0, 2, 3, 5, 7, 8, 10};
public static int[] harmonicScale = {0, 2, 3, 5, 7, 11};
public static int[] melodicScale = {0, 2, 3, 5, 7, 9, 11};
public static int[] enigmaticScale = {0, 1, 4, 6, 8, 10, 11};
public static int[] chromaticScale = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
public static int[] arpeggioScale = {0, 4, 7};
public static int[] domseventhScale = {0, 4, 7, 11};
public static int[] pentatonicScale = {0, 2, 5, 7, 9};
public static int[] greekpentatonicScale = {0, 1, 5, 7, 8};
public static int[] gregorianScale = {0, 2, 3, 5, 7, 9, 10};
public static int[] hijaziScale = {0, 1, 4, 5, 6, 8, 11};


public static final int SCALETYPE_MAJOR = 0;
public static final int SCALETYPE_MINOR = 1;
public static final int SCALETYPE_HARMONIC = 2;
public static final int SCALETYPE_MELODIC = 3;
public static final int SCALETYPE_ENIGMATIC = 4;
public static final int SCALETYPE_CHROMATIC = 5;
public static final int SCALETYPE_ARPEGGIO = 6;
public static final int SCALETYPE_DOMSEVENTH = 7;
public static final int SCALETYPE_PENTATONIC = 8;
public static final int SCALETYPE_GREEKPENTATONIC = 9;
public static final int SCALETYPE_GREGORIAN = 10;
public static final int SCALETYPE_HIJAZI = 11;
public static final int SCALETYPE_USER = -1;

/**
A reference to a MIDI synth object
*/
protected Synthesizer synthesizer;

protected int program = 15 /* tub. bell */ - 1;
protected boolean quit = false;
/**
MIDI channel assigned during the `open()' call
*/
protected MidiChannel channel = null;
protected int noteOnDurationMillis = 2000;
protected int windSpeed = 20;
protected int gustyness = 30;
protected int baseNote = 85; 
protected int numChimes = 5;
protected int scaleType = SCALETYPE_PENTATONIC;
protected int device = -1;
protected int channelNumber = 0;
protected Random random = new Random();
protected int[] scale;

/**
Constructor readies the synth device, etc
*/
public JavaChimes() throws JavaChimesException
  {
  // Make a default scale, in case the client calls start()
  //  without calling any of the property-setting methods
  makeScale();
  }

/**
Closes the MIDI system and frees resources
*/
protected void close() 
  {
  if (synthesizer != null) 
    synthesizer.close();
  synthesizer = null;
  channel = null;
  }

protected void checkRange (final int x, final int min, 
    final int max, final String method)
  throws JavaChimesIllegalArgumentException
  {
  if (x < min || x > max) 
    throw new JavaChimesIllegalArgumentException 
      ("Argument out of range (" + min + "-" + max + ") in " + method + "()"); 
  }

/**
Sets the (one-based) MIDI program number for output
*/
public void setProgram (final int _program)
  {
  checkRange (_program, 1, 128, "setProgram");
  program = _program;
  if (channel != null)
    channel.programChange(_program - 1);
  }

public int getProgram ()
  {
  return program + 1;
  }

/**
Sets the (one-based) MIDI channel number for output
*/
public void setChannel (final int _channelNumber)
  {
  checkRange (_channelNumber, 1, 16, "setChannel");
  channelNumber = _channelNumber  - 1;
  }

public int getChannel ()
  {
  return channelNumber + 1;
  }

/**
Sets the base note for the scale, 0-127 
*/
public void setBaseNote (final int _baseNote)
  {
  checkRange (_baseNote, 0, 127, "setBaseNote");
  baseNote = _baseNote;
  makeScale();
  }

public int getBaseNote ()
  {
  return baseNote;
  }

/**
Sets the scale type to the specified constant 
*/
public void setScaleType (final int _scaleType)
  {
  checkRange (_scaleType, 0, 11, "setScaleType");
  scaleType = _scaleType;
  makeScale();
  }

public int getScaleType ()
  {
  return scaleType;
  }

/**
Sets the scale to the specified note intervals 
*/
public void setScale (final int[] _scale)
  {
  scale = _scale;
  scaleType = SCALETYPE_USER;
  }

public int[] getScale ()
  {
  return scale;
  }

/**
Sets the device number; -1 = system default 
*/
public void setDevice (final int _device)
  {
  device = _device;
  }

public int getDevice ()
  {
  return device;
  }

/**
Sets the number of chimes. Theoretically this depends on the
scale in use. 
*/
public void setNumChimes (final int _numChimes)
  {
  checkRange (_numChimes, 1, 128, "setNumChimes");
  numChimes = _numChimes;
  makeScale();
  }

public int getNumChimes ()
  {
  return numChimes;
  }

/**
Sets the note-on duration to the specified number of seconds.  
*/
public void setNoteOnDuration (final int _noteOnDuration)
  {
  checkRange (_noteOnDuration, 1, 0x7FFFFFFF, "setNoteOnDuration");
  noteOnDurationMillis = _noteOnDuration * 1000;
  }

public int getNoteOnDuration ()
  {
  return noteOnDurationMillis / 1000;
  }

/**
Sets the windspeed
*/
public void setWindSpeed (final int _windSpeed)
  {
  checkRange (_windSpeed, 1, 100, "setWindSpeed");
  windSpeed = _windSpeed;
  }

public int getWindSpeed ()
  {
  return windSpeed;
  }

/**
Sets the gustyness 
*/
public void setGustyness (final int _gustyness)
  {
  checkRange (_gustyness, 0, 100, "setGustyness");
  gustyness = _gustyness;
  }

public int getGustyness ()
  {
  return gustyness;
  }


/**
Causes the sound generation to stop after the next note has been played,
and the MIDI system to be closed.
*/
public void signal()
  {
  quit = true;
  }

/**
Causes the sound generation to stop after the next note has been played,
and the MIDI system to be closed. This method does not return to the 
caller until these operations have been effected
*/
public void signalAndWait()
  {
  quit = true;
  while (synthesizer != null){};
  }


/**
Returns an array of names of names of available MIDI devices.
*/
public static String[] getDeviceNames () throws JavaChimesException
  {
  MidiDevice.Info info[] = MidiSystem.getMidiDeviceInfo();
  Vector v = new Vector();
  for (int i = 0; i < info.length; i++)
    v.add(info[i].getName());
  String[] t = new String[v.size()];;
  for (int i = 0; i < info.length; i++)
    t[i] = (String)v.elementAt(i);
  return t;
  }

/**
Opens the MIDI system and retrieves a MidiChannel instance for subsequent
output operations
*/
public void open () throws JavaChimesException
  {
  try
    {
    if (device == -1)
      {
      synthesizer = MidiSystem.getSynthesizer();
      }
    else
      {
      MidiDevice.Info info[] = MidiSystem.getMidiDeviceInfo();
      if ((device < 0) || (device >= info.length))
        throw new JavaChimesException ("Device number out of range");
      synthesizer = (Synthesizer)MidiSystem.getMidiDevice(info[device]);
      }
    synthesizer.open();
            //sequencer = MidiSystem.getSequencer();
            //sequence = new Sequence(Sequence.PPQ, 10);
    }
  catch (MidiUnavailableException e)
    {
    throw new JavaChimesException ("Can't get access to a synth device");
    }
  catch (ClassCastException e)
    {
    throw new JavaChimesException ("Selected MIDI device is not a synthesizer");
    }

  Soundbank sb = synthesizer.getDefaultSoundbank();
  if (sb != null) 
    {
    Instrument[] instruments = synthesizer.getDefaultSoundbank().getInstruments();
    synthesizer.loadInstrument(instruments[0]);
    }

  MidiChannel midiChannels[] = synthesizer.getChannels();
  channel = midiChannels[channelNumber];
  }

/**
Recalculates the notes in the scale array. Client classes can call
this, but it is called automatically from setBaseNote(), etc.
*/
public void makeScale()
  {
  if (scaleType == SCALETYPE_USER) return;
  int currentBaseNote = baseNote;
  int[] selectedScale;
  switch (scaleType)
    {
    case SCALETYPE_MAJOR:
      selectedScale = majorScale;
      break;
    case SCALETYPE_MINOR:
      selectedScale = minorScale;
      break;
    case SCALETYPE_HARMONIC:
      selectedScale = harmonicScale;
      break;
    case SCALETYPE_MELODIC:
      selectedScale = melodicScale;
      break;
    case SCALETYPE_ENIGMATIC:
      selectedScale = enigmaticScale;
      break;
    case SCALETYPE_CHROMATIC:
      selectedScale = chromaticScale;
      break;
    case SCALETYPE_DOMSEVENTH:
      selectedScale = domseventhScale;
      break;
    case SCALETYPE_ARPEGGIO:
      selectedScale = arpeggioScale;
      break;
    case SCALETYPE_GREEKPENTATONIC:
      selectedScale = greekpentatonicScale;
      break;
    case SCALETYPE_GREGORIAN:
      selectedScale = gregorianScale;
      break;
    case SCALETYPE_HIJAZI:
      selectedScale = hijaziScale;
      break;
    default:
      selectedScale = pentatonicScale;
    }
  scale = new int[numChimes];
  int ssIndex = 0;
  for (int i = 0; i < numChimes; i++)
    {
    scale[i] = currentBaseNote + selectedScale[ssIndex++];
    if (ssIndex >= selectedScale.length)
      {
      ssIndex = 0;
      int lastNote = selectedScale[selectedScale.length - 1];
      if (lastNote > 36)
        currentBaseNote += 48;
      else if (lastNote > 24)
        currentBaseNote += 36; 
      else if (lastNote > 12)
        currentBaseNote +=24; 
      else
        currentBaseNote +=12; 
      } 
    
    // Shouldn't happen, but protect against generating
    //  notes > 127
    if (currentBaseNote > 127) currentBaseNote -= 12;
    }
  }

/**
Do everything, until something calls stop()
*/
public void run() throws JavaChimesException 
  {
  Vector noteList = new Vector();
  quit = false;
  open();
  channel.programChange(program);
  int notesInScale = scale.length;

  while (!quit)
    {
    double eventsPerSec = random.nextGaussian() * gustyness/10.0 
      + windSpeed/10.0;
    if (eventsPerSec <= 0.0) eventsPerSec = 1;
    double maxEventsPerSec = windSpeed/10.0 + 3.0 * gustyness/10.0;
    int delayMillis = (int)((1.0 / eventsPerSec) * 1000.0);

    try 
      { 
      Thread.sleep(delayMillis); 
      } catch (Exception e){}
    long timeNow = System.currentTimeMillis();

    int velocity = (int)(eventsPerSec / maxEventsPerSec * 127.0);
    if (velocity < 0) velocity = 0;
    if (velocity > 127) velocity = 127;

    // On each pass, generate a new note, and check which existing notes
    //  are sounding and need to be cleared
    int kNum = scale[random.nextInt(notesInScale)]; 
    channel.noteOn(kNum, velocity); 
    NoteInfo ni = new NoteInfo (kNum, timeNow);
    noteList.add (ni);
    
    Iterator it = noteList.iterator();
    while (it.hasNext())
      {
      ni = (NoteInfo)it.next();
      if (timeNow - ni.startTime > noteOnDurationMillis) // FRIG 
        {
        channel.noteOff(ni.kNum);
        noteList.remove(ni);
        it = noteList.iterator();
        }
      }
    }
  close();
  }

}




