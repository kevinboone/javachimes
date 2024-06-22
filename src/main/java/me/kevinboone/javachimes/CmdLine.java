package me.kevinboone.javachimes; 
import me.kevinboone.utils.javapopt.*; 
import java.util.*;

/**
*/
public class CmdLine 
{
protected static int[] makeScale (String scaleString)
  {
  StringTokenizer st = new StringTokenizer (scaleString, ",");
  int[] t = new int[st.countTokens()];
  for (int i = 0; i < t.length; i++)
    {
    String token = st.nextToken();
    t[i] = Integer.parseInt (token);
    }
  return t;
  }

public static void main(String[] args) throws Exception
    {
    // MIDI program code (zero-based) of default program (voice)
    final int DEFAULT_PROGRAM = 14;

    // Default length of time (seconds) a note is allowed to sound before being killed off. 
    // Of course, the synth system may have timed it out before this, if it decays quickly.
    final int DEFAULT_NOTE_ON_DURATION = 2;
    final int DEFAULT_WINDSPEED = 20;
    final int DEFAULT_GUSTYNESS = 30;
    final int DEFAULT_BASENOTE = 85;
    final int DEFAULT_NUMCHIMES = 5;
    final int DEFAULT_DEVICE = -1;
    final int DEFAULT_CHANNEL = 1;
    final int DEFAULT_SCALETYPE = JavaChimes.SCALETYPE_PENTATONIC;
    final String soundfileFile = null;

    Popt popt = new Popt (args); 
    
    // Tell Popt what arguments we accept 
    popt.addSwitchSpec ("channel", 'a', "channel", false, 
      "set MIDI channel (1)", "[1-16]", Integer.class, true);
    popt.addSwitchSpec ("basenote", 'b', "basenote", false, 
       "MIDI base note (85)", "[1-128]", Integer.class, true);
    popt.addSwitchSpec ("scale", 'c', "scale", false, 
       "scale", "[n1,n2,n3...]", String.class, true);
    popt.addSwitchSpec ("noteonduration", 'd', "duration", false, 
        "note-on duration (2)", "seconds", Integer.class, true);
    popt.addSwitchSpec ("device", 'e', "device", false, 
        "select device (default)", "(dev)", Integer.class, true);
    popt.addSwitchSpec ("soundfont", 'f', "soundfont", false, 
        "soundfont file (none)", "(file)", String.class, true);
    popt.addSwitchSpec ("gustyness", 'g', "gustyness", false, 
        "'gustyness' (30)", "(0-100)", Integer.class, true);
    popt.addSwitchSpec ("listdevs", 'l', 
        "listdevs", false, "list devices", true);
    popt.addSwitchSpec ("numchimes", 'n', 
        "numchimes", false, "number of chimes (5)", "(> 1)", 
         Integer.class, true);
    popt.addSwitchSpec ("program", 'p', 
        "program", false, "MIDI program number (14)", 
        "(1-128)", Integer.class, true);
    popt.addSwitchSpec ("seconds", 's', "seconds", false, 
        "duration of play (unlimited)", "(> 0)", 
        Integer.class, true);
    popt.addSwitchSpec ("scaletype", 't', 
         "scaletype", false, "scale type (8)", "(0-11)", 
         Integer.class, true);
    popt.addSwitchSpec ("windspeed", 'w', "windspeed", false, 
         "wind speed (20)", "(1-100)", Integer.class, true);
    popt.addSwitchSpec ("version", 'v', "version", false, 
         "show version", false);

    // Add help arguments for --help, -h, and -?
    popt.addHelp (true, true, true);

    // Parse the command line and check for `help' and `verion' arguments
    if (popt.parse() == false)
      {
      System.err.println (popt.getErrorMessage());
      System.err.println ("Usage: java -jar javachimes.jar " + 
         popt.getShortUsage());
      System.err.println (popt.getUsage());
      System.exit(-1);
      }

    if (popt.supplied("help"))
      {
      System.out.println ("Usage: java -jar javachimes.jar " + "[options]");
      System.out.println (popt.getUsage());
      System.exit(0);
      }
    else if (popt.supplied("version"))
       {
       System.out.println 
         ("JavaChimes version 1.0b, Copyright (c)2003-2024 Kevin Boone");
       System.out.println 
         ("Distributed under the terms of the GNU Public Licence v3.0");
      System.exit(0);
       }
    else if (popt.supplied("listdevs"))
      {
      String[] devs = JavaChimes.getDeviceNames();
      for (int i = 0; i < devs.length; i++)
	{
	System.out.println ("" + i + ": " + devs[i]); 
	}
      System.exit(0);
      }
    else
      {
      JavaChimes javaChimes = new JavaChimes();

      if (popt.supplied ("soundfont"))
        javaChimes.setSoundbankFile (popt.getString ("soundfont"));
      javaChimes.setProgram (popt.getIntOrDefault 
        ("program", DEFAULT_PROGRAM));

      javaChimes.setProgram (popt.getIntOrDefault 
        ("program", DEFAULT_PROGRAM));
      javaChimes.setNoteOnDuration (popt.getIntOrDefault
        ("noteonduration", DEFAULT_NOTE_ON_DURATION));
      javaChimes.setWindSpeed (popt.getIntOrDefault 
        ("windspeed", DEFAULT_WINDSPEED)); 
      javaChimes.setGustyness (popt.getIntOrDefault 
        ("gustyness", DEFAULT_GUSTYNESS)); 
      javaChimes.setBaseNote (popt.getIntOrDefault 
        ("basenote", DEFAULT_BASENOTE)); 
      javaChimes.setDevice (popt.getIntOrDefault 
        ("device", DEFAULT_DEVICE)); 
      javaChimes.setChannel (popt.getIntOrDefault 
        ("channel", DEFAULT_CHANNEL)); 
      if (popt.supplied ("scaletype"))
	javaChimes.setScaleType (popt.getInt ("scaletype")); 
      else if (popt.supplied ("scale"))
	javaChimes.setScale (makeScale(popt.getString("scale"))); 
      else 
	javaChimes.setScaleType (DEFAULT_SCALETYPE);

      javaChimes.setNumChimes (popt.getIntOrDefault 
        ("numchimes", DEFAULT_NUMCHIMES)); 

      javaChimes.start();

      int count = 0; 
      int seconds = popt.getIntOrDefault ("seconds", -1);
      while (count++ < seconds || seconds == -1 && javaChimes.isAlive()) 
	{ 
	try 
	  { 
	  Thread.sleep (1000); 
	  } 
	catch (Exception e){} 
	}
      if (javaChimes.isAlive())
        javaChimes.signal();
      System.exit(0);
      }
    }
}




