package me.kevinboone.utils.javapopt;
import java.util.*;
import java.io.*;

/**
Java command-line argument parser V1.0

See `overview' for information on how to use this
class. 

(c)2001 Kevin Boone/Web-Tomorrow, all rights reserved
*/
public class Popt 
{
/**
args is initialized to the input arguments in the constructor
*/
protected String args[];

/**
longFormShort is initialized in the constructor
*/
protected boolean longFormShort;

/**
ignoreCase is initialized in the constructor
*/
protected boolean ignoreCase;

/**
Store switch specifications as supplied by the client
*/
protected Vector switchSpecList;

/**
Store non-switch specifications as supplied by the client
*/
protected Vector nonSwitchSpecList;

/**
Args not matched by the parser, set in parse() and available
through getLeftoverArgs()
*/
protected String[] leftoverArgs = new String[0];

/**
strict is set in the constructor
*/
boolean strict;

/**
numericSwitches is set in the constructor
*/
boolean numericSwitches;

/**
errorMessage is set by any of the parsing methods, if they
can't parse the input. Can be retrieved by client by calling
getErrorMessage()
*/
protected String errorMessage;

/**
Table of matched args that were parsed
*/
protected Hashtable matchTable;

/**
Application defined parse handler
*/
protected PoptParseHandler poptParseHandler;

/**
The Popt constructor takes an array of Strings, typically as supplied on the
command-line to a program, and a set of flags controlling the strictness
of the parsing process. The String[] input can legitimately be empty;
this will cause no problems, but it must not be null
<pre>
args          The arguments to parse

longFormShort true if the long name can be supplied with
              only a single dash. This option is helpful 
              only if there are no long-form arguments
              that happen to have only one letter. The parser
              does not check for this, as it is unusual in practice              

strict        if true, certain non-critical errors are
              considered critical. For example, in strict
              mode switches that are not in the client's
              supplied list are considered an error; otherwise
              they are silently ignored. Note, however, that if
              a switch is not listed, we don't know if it should
              take arguments, so we can't skip the arguments. This
              may mean that parsing falls over further on instead.

ignoreCase    match case-insensitively on short names. Long names are
              always match this way. If you set this flag, ensure
              that there are no ambiguous short names. 

numericSwiches true if switches are allowed to begin with digits.
              Only set this to true if you're sure you will require
              numeric switches, because it prevents negative
              numbers being used in arguments.
</pre>
*/
public Popt (String[] _args, 
    boolean _longFormShort, boolean _strict, boolean _ignoreCase, 
    boolean _numericSwitches)
  {
  args = _args;
  longFormShort = _longFormShort;
  strict = _strict;
  ignoreCase = _ignoreCase;
  numericSwitches = _numericSwitches;
  switchSpecList = new Vector();
  nonSwitchSpecList = new Vector();
  matchTable = new Hashtable();
  }

/**
The Popt constructor takes an array of Strings, typically as supplied on the
command-line to a program, and uses default settings for the strictness
parameters.
*/
public Popt (String[] _args)
  {
  this(_args, true, true, true, false);
  }

/**
addNonSwitchSpec() is called by clients to add non-switch args to the list
recognized by the parser. It should be called once for each argument.
<pre>
identifier    name used to refer to this item after parsing

argName	      name of the argument, if there is one

compulsory    true if it would be an error to omit this argument 

description   short text description of the argument, for usage message

argType       type of the argument. Currently we recognise
              String, Integer, and Double. Note that args are
              always classes, never primitives      
</pre>
Note that non-swtich args are recognized by their order of appearance
of the command line. Some or all may be optional, but it is erroneous
to specify a compulsory argument _after_ an optional one (how would the
parser know which arguments to insist on?)
*/
public void addNonSwitchSpec (String _identifier, String _argName, boolean _compulsory, 
    String _description, Class _argType)
  {
  PoptNonSwitchSpec pss = new PoptNonSwitchSpec (_identifier, _argName, _compulsory, 
     _description, _argType);
  nonSwitchSpecList.add(pss);
  }

/**
addSwitchSpec() is called by clients to add switches to the list
recognized by the parser. It should be called once for each switch.
<pre>
identifier    name used to refer to this item after parsing

shortName     short name of the switch, _without_ the
              leading `-' 

longName      long name of the switch, _without_ the
              leading `--' 

compulsory    true if it would be an error to omit this switch

description   short text description of the switch, for usage message

hasArg        true if this switch requires an argument

argName	      name of the argument, if there is one

argType       type of the argument. Currently we recognise
              String, Integer, and Double. Note that args are
              always classes, never primitives      
</pre>
*/
public void addSwitchSpec (String identifier, char shortName, String longName, 
     boolean compulsory, String description, String argName, Class argType, boolean allowDuplicates)
  {
  PoptSwitchSpec pss = new PoptSwitchSpec (identifier, shortName, longName, compulsory, 
     description, true, argName, argType, allowDuplicates);
  switchSpecList.add(pss);
  }

/**
Convenience version of addSwitchSpec() for switches that don't take
arguments (and therefore don't need to have them specified). 
<pre>
identifier    name used to refer to this item after parsing

shortName     short name of the switch, _without_ the
              leading `-' 

longName      long name of the switch, _without_ the
              leading `--' 

compulsory    true if it would be an error to omit this switch

description   short text description of the switch, for usage message
</pre>
*/
public void addSwitchSpec (String identifier, char shortName, String longName, 
     boolean compulsory, String description, boolean allowDuplicates)
  {
  PoptSwitchSpec pss = new PoptSwitchSpec (identifier, shortName, longName, compulsory, 
     description, false, "", null, allowDuplicates);
  switchSpecList.add(pss);
  }

/**
Adds the `help' switches to the argument list. This method can add between
zero and three entries: <code>longHelp</code> recognizes `--help',
<code>shortHelp</code> recognizes `-h' and <code>query</code> recognizes
`-?'. Don't use <code>shortHelp</code> if you have other switches denoted
by `-h'.
*/
public void addHelp (boolean longHelp, boolean shortHelp, boolean query)
  {
  if (longHelp && shortHelp)
     addSwitchSpec ("help", 'h', "help", false, "displays help message", true);
  if (longHelp && !shortHelp)
     addSwitchSpec ("help", (char)0, "help", false, "displays help message", true);
  if (!longHelp && shortHelp)
     addSwitchSpec ("help", 'h', null, false, "displays help message", true);
  if (query)
     addSwitchSpec ("help", '?', null, false, "same as --help", true);
  }

/**
parse() processes the command-line arguments
*/
public boolean parse()
  {
  if (parseSwitchArgs() == false) return false;
  return parseNonSwitchArgs();
  }

/**
setPoptParseHandler sets that supplementary parser handler.
The `handleArg' method in this class is called whenever an
argument is matched, in addition to anything that might
happen in the parser itself.
*/
public void setPoptParseHandler (PoptParseHandler _poptParseHandler)
  {
  poptParseHandler = _poptParseHandler;
  }

/**
parse() processes the command-line arguments that are _not_ switches. This
method takes as its input the arguments left over from parsing the switches.
Therefore, if a client does use this method (and it probably shouldn't), it
should call parseSwitchArgs() first. Otherwise this method will do nothing
*/
public boolean parseNonSwitchArgs()
  {
  for (int i = 0; i < nonSwitchSpecList.size(); i++)
    {
    PoptNonSwitchSpec as = (PoptNonSwitchSpec) nonSwitchSpecList.elementAt(i);
    if (i >= leftoverArgs.length)
       {
       if (as.compulsory)
         { 
         // run out of args, and this one compulsory: error
         errorMessage = getMissingCompulsoryArgumentMessage() + getPreferredSwitchText(as);
         return false;
         }
       else
         {
         // run out of args, but no matter as this one is not
         //  compulsory
         }
       }
    else
       {
       // got an arg: try to assign it to this specification
       String switchArg = leftoverArgs[i];
       Object value;
       if (as.argType == null || as.argType == String.class)
         value = switchArg;
       else if (as.argType == Integer.class)
         {
         try
           {
           value = new Integer (Integer.parseInt (switchArg));
           }
         catch (NumberFormatException e)
           {
           errorMessage = getBadIntegerMessage () + switchArg;
           return false;
           }
         }
       else if (as.argType == Double.class)
         {
         try
           {
           value = new Double (Double.parseDouble (switchArg));
           }
         catch (NumberFormatException e)
           {
           errorMessage = getBadDoubleMessage () + switchArg;
           return false;
           }
         }
       else 
         {
         // The client has supplied an argument type that is not
         //  recognized TODO check this in addSwitchSpec etc 
         throw new RuntimeException ("Internal error: unrecognized argument type in Java Popt");
         }

       addMatch (as.identifier, value); 

       if (poptParseHandler != null)
         poptParseHandler.handleArg (this, as.identifier, value);
       }
     }
  
  // Remove any matched arguments from leftoverArgs, in case the client
  //  wants them for something else 

  int numLeftover = leftoverArgs.length - nonSwitchSpecList.size();
  // numLeftover may be zero or negative
  if (numLeftover <= 0)
    {
    leftoverArgs = new String[0];
    return true;
    }
  String[] newLeftoverArgs = new String[numLeftover];
  for (int i = 0; i < numLeftover; i++)
    {
    newLeftoverArgs[i] = leftoverArgs[i + nonSwitchSpecList.size()];
    } 
  leftoverArgs = newLeftoverArgs;
  return true;
  }

/**
parse() processes the switch command-line arguments,
writing anything left over into leftoverArgs
*/
public boolean parseSwitchArgs()
  {
  Vector leftoverArgsVector = new Vector();
  for (int i = 0; i < args.length; i++)
    {
    String arg = args[i];
    // Note that zero-length command-line args are possible,
    //  in theory
    if (arg.length() > 0 && arg.charAt(0) == '-')
      {
      if (!numericSwitches && arg.length() > 1 && Character.isDigit (arg.charAt(1))) 
        {
        leftoverArgsVector.add (arg);
        continue;
        }
      int ePos = arg.indexOf ("=");
      String searchArg;
      if (ePos > 0)
        searchArg = arg.substring (0, ePos);
      else
	searchArg = arg;

      PoptSwitchSpec as = findSwitchSpec(searchArg);
      if (as != null)
        {
	// We've found one with a matching name; now see if it has arguments.
	// If it does, we need to check the arguments as well
        if (as.hasArg)
          {
          // If the arg has an equals sign in it, then get the switch arg from
          //  the bit after the equals; don't advance `i'.
          // Otherwise advance `i', as we will need to get the next command-line arg.
          //  if this takes 'i' off the end of the array, then this is a
          //  syntax error and we will abort
          String switchArg;
          if (ePos > 0)
            {
            switchArg = arg.substring (ePos + 1);
            arg = arg.substring (0, ePos);
            }
          else
            { 
            i++;
            if (i >= args.length)
              {
              // Oops...
              errorMessage = getMissingArgumentMessage () + arg;
              return false;
              }
            switchArg = args[i];
            }
          if (switchArg.length() > 0 && switchArg.charAt(0) == '-')
            {
            if (numericSwitches)
              {
              // Oops...
              errorMessage = getMissingArgumentMessage () + arg;
              return false;
              }
            }
          // There is a valid String argument, 
          // but does it match the client's type spec? 

          Object value;
          if (as.argType == null || as.argType == String.class)
            value = switchArg;
          else if (as.argType == Integer.class)
            {
            try
              {
              value = new Integer (Integer.parseInt (switchArg));
              }
            catch (NumberFormatException e)
              {
              errorMessage = getBadIntegerMessage () + switchArg;
              return false;
              }
            }
          else if (as.argType == Double.class)
            {
            try
              {
              value = new Double (Double.parseDouble (switchArg));
              }
            catch (NumberFormatException e)
              {
              errorMessage = getBadDoubleMessage () + switchArg;
              return false;
              }
            }
          else 
            {
            // The client has supplied an argument type that is not
            //  recognized TODO check this in addSwitchSpec etc 
            throw new RuntimeException ("Internal error: unrecognized argument type in Java Popt");
            }

          addMatch (as.identifier, value); 
          if (poptParseHandler != null)
             poptParseHandler.handleArg (this, as.identifier, value);
          }
        else
          {
          // No args required; add as a boolean
          if (addMatch (as.identifier, new Boolean(true)) == false && !as.allowDuplicates)
            {
            errorMessage = getDuplicateSwitchMessage () + arg;
            return false;
            }
          if (poptParseHandler != null)
             poptParseHandler.handleArg (this, as.identifier, new Boolean(true));
          }
        }
      else
        {
        if (strict)
          {
          errorMessage = getUnmatchedSwitchMessage() + arg;
          return false;
          }
        else
	  {
          // Do nothing: tacitly ignore non-matched switch 
          }
        }
      }
    else
      leftoverArgsVector.add (arg);
    }

  leftoverArgs = new String[leftoverArgsVector.size()];
  for (int i = 0; i < leftoverArgsVector.size(); i++)
    {
    leftoverArgs[i] = (String)leftoverArgsVector.elementAt(i);
    } 

  // TODO: check all compulsories have been supplied

  for (int i = 0; i < switchSpecList.size(); i++)
    {
    PoptSwitchSpec as = (PoptSwitchSpec) switchSpecList.elementAt(i);
    if (as.compulsory)
      {
      if (!supplied(as.identifier))
        {
        errorMessage = getMissingCompulsoryArgumentMessage() + getPreferredSwitchText(as);
        return false;
        }
      }
    }
    
  return true;
  }

/**
supplied() returns true if the command-line contained
the specified argument. If the argument was a switch,
and the switch did not take an argument, then this is
probably the best way for the application to determine
whether the argument was present or not
*/
public boolean supplied (String identifier)
  {
  return matchTable.get (identifier) != null;
  }

/**
getArg() gets an argument from the match table, if
it is there, or null otherwise. If the argument was
a switch, and the switch did not itself take an
argument, then this method will return a 
Boolean object set to `true'. If the argument was a switch
that took a value, then the value of the switch argument
is returned.  If the argument was a
non-switch, the argument itself is returned.  
That is, in all cases the return value is something
that is supplied on the command line, although in the
`Boolean' case it is supplied implicitly
*/
public Object getArg (String identifier)
  {
  return matchTable.get (identifier);
  }

/**
getInt() gets the specified argument as an int primitive.
This method can be handy if you know that the argument exists
and is an integer (because it is compulsory). Otherwise don't
use this without first checking that the argument exists,
unless you are prepared to handle NullPointerException and
ClassCastException...
*/
public int getInt (String identifier)
  {
  return ((Integer)matchTable.get(identifier)).intValue();
  }

/**
getDoubleOrDefault() gets the specified argument as a double primitive,
or returns the supplied default if the argument was not
specified. It only makes sense to use this on non-compulsory
arguments, as compulsory arguments will never take the default
value.
Note that the respone of this method is undefined if
it is asked to return an attribute for which a value has been
entered, but is of the wrong type. This can only be a coding error,
because the user's input will already have been validated to be
of the correct type for the argument specification supplied.
*/
public double getDoubleOrDefault (String identifier, double def)
  {
  Object o = matchTable.get (identifier);
  if (o == null) return def;
  return ((Double)o).doubleValue();
  }

/**
getIntOrDefault() gets the specified argument as an int primitive,
or returns the supplied default if the argument was not
specified. It only makes sense to use this on non-compulsory
arguments, as compulsory arguments will never take the default
value.
Note that the respone of this method is undefined if
it is asked to return an attribute for which a value has been
entered, but is of the wrong type. This can only be a coding error,
because the user's input will already have been validated to be
of the correct type for the argument specification supplied.
*/
public int getIntOrDefault (String identifier, int def)
  {
  Object o = matchTable.get (identifier);
  if (o == null) return def;
  return ((Integer)o).intValue();
  }

/**
getStringOrDefault() gets the specified argument as a String
or returns the supplied default if the argument was not
specified. It only makes sense to use this on non-compulsory
arguments, as compulsory arguments will never take the default
value.
Because anything can be converted into a String, this method
works on any argument, regardless of type
*/
public String getStringOrDefault (String identifier, String def)
  {
  Object o = matchTable.get (identifier);
  if (o == null) return def;
  return o.toString(); 
  }

/**
getDouble() gets the specified argument as an double primitive.
This method can be handy if you know that the argument exists
and is a double (because it is compulsory). Otherwise don't
use this without first checking that the argument exists,
unless you are prepared to handle NullPointerException and
ClassCastException...
*/
public double getDouble (String identifier)
  {
  return ((Double)matchTable.get(identifier)).doubleValue();
  }

/**
getString() gets the specified argument as a String.
This method can be handy if you know that the argument exists
and is a String (because it is compulsory). Otherwise don't
use this without first checking that the argument exists,
unless you are prepared to handle ClassCastException
*/
public String getString (String identifier)
  {
  return (String)(matchTable.get(identifier));
  }

/**
addMatch() adds an argument to the match list, keyed by its
identifier. Returns true if the entry was added. If it
returns false, this indicates that the entry over-wrote
an existing entry with the same name. This _may_ be an
error (that's for the application to decide). So the
caller of this methid should probably check the return
value and take appropriate action. When called by
parseSwitchArgs(), the parser will abort if this method
returns false and `strict' is set. 
*/
protected boolean addMatch (String identifier, Object o)
  {
  boolean ret = true;
  if (getArg (identifier) != null)
    ret = false;
  matchTable.put (identifier, o);
  return ret;
  }

/**
Finds the switch arg specification, if it exists, based
on the supplied arg. arg should include the dashes,
because the algorithm needs to know whether to match
the long form of the name or the short form.
Returns null if the arg does not match anything.
This method is called by parseSwitchArgs().
*/
protected PoptSwitchSpec findSwitchSpec(String arg)
  {
  boolean matchLong;
  boolean matchShort;
  boolean dodgyLong;
  String realArg;
  char realShortArg;
  if (arg.indexOf ("--") == 0)
     {
     realArg = arg.substring(2);
     realShortArg = (char)0;
     matchLong = true;
     matchShort = false;
     dodgyLong = false;
     }
  else if (arg.indexOf("-") == 0)
    {
    realArg = arg.substring(1);
    if (realArg.length() == 1)
      {
      realShortArg = realArg.charAt(0);
      matchShort = true;
      matchLong = false;
      dodgyLong = false;
      }
    else
      {
      // Dodgy case: long name given with single '-'
      realShortArg = (char)0;
      matchShort = false;
      matchLong = true;
      dodgyLong = true;
      }
    }
  else
    return null; // Should never happen: method only
                 //   called on switches
  for (int i = 0; i < switchSpecList.size(); i++)
    {
    PoptSwitchSpec as = (PoptSwitchSpec) switchSpecList.elementAt(i);
    if (matchShort)
      {
      if (realShortArg == as.shortName)
        return as;
      if ((Character.toLowerCase(realShortArg) == Character.toLowerCase(as.shortName)) && ignoreCase)
        return as;
      }
    if (matchLong)
      {
      if (realArg.equalsIgnoreCase(as.longName))
        {
        if (!dodgyLong || longFormShort)
          return as;
        }
      }
    }
  return null;
  }

/**
Returns an array of command-line arguments that were not matched
as switches. These having meanings that are known only to the
client. The array will be empty unless parse() has been called.
*/
public String[] getLeftoverArgs()
  {
  return leftoverArgs;
  }

/**
Sets the array of command-line arguments that were not matched as switches. A
client application should probably not need to call this method directly,
unless it wants to merge command-line arguments from different sources, for
example. If you do call it, the only useful thing that can be done afterwards
is parseNonSwitchArgs() 
*/
public void setLeftoverArgs(String[] _leftoverArgs)
  {
  leftoverArgs = _leftoverArgs;
  }

/**
Gets the preferred text to identify the argument in the 
usage messages. An attribute is selected from the
argument specification in this order of priority:
longName, shortName, argName. Note that non-switch
arguments only have argName, the other two attributes
are null by definition.  Used only be getUsage()
*/
private String getPreferredSwitchText (PoptArgSpec as)
  {
  if (as.shortName != (char)0)
    return "-" + as.shortName;
  if (as.longName != null)
    return "--" + as.longName;
  return as.argName; 
  }

/**
Returns the first part of the usage message for each argument.
Used only by `getUsage()'.
*/
private String getUsageArgNameSection (PoptSwitchSpec as)
  {
  String argNameSection = "";
  if (as.shortName != (char)0)
    argNameSection = "-" + as.shortName;
  if (as.longName != null)
    {
    if (as.shortName != (char)0)
      argNameSection += ", ";
    argNameSection += "--" + as.longName; 
    }
  if (as.hasArg)
    argNameSection += " " + as.argName;
  return argNameSection;
  }

/**
getShortUsage() returns a String that can be output to the console. This String
contains a summary of the switches, without details. For a full usage
message, use getUsage(). Note that in practice a client will need to use
getShortUsage() and getUsage() together. getShortUsage() only displays switches;
it does not display the text `usage:', or the program name, or the non-switch
arguments. These must be prepended and appended by the client. 
*/
public String getShortUsage ()
  {
  String s = "";
  for (int i = 0; i < switchSpecList.size(); i++)
    {
    PoptSwitchSpec as = (PoptSwitchSpec) switchSpecList.elementAt(i);
    if (!as.compulsory) s += "[";
    s += getPreferredSwitchText(as);
    if (as.hasArg) s += " " + as.argName;
    if (!as.compulsory) s += "]";
    s += " ";
    }
  for (int i = 0; i < nonSwitchSpecList.size(); i++)
    {
    PoptNonSwitchSpec as = (PoptNonSwitchSpec) nonSwitchSpecList.elementAt(i);
    if (!as.compulsory) s += "[";
    s += getPreferredSwitchText(as);
    if (!as.compulsory) s += "]";
    s += " ";
    }
  return s;
  }

/**
getUsage() returns a String that can be output to the console. This String
contains a summary of the usage of the program, derived from the command-line
arguments supplied by the client. Output will span multiple lines. For a
one-line summary, use getShortUsage().
*/

public String getUsage ()
  {
  String s = "";
  
  // Determine the width of the name section, for text alignment 
  int maxArgNameSectionLength = 0;
  for (int i = 0; i < switchSpecList.size(); i++)
    {
    PoptSwitchSpec as = (PoptSwitchSpec) switchSpecList.elementAt(i);
    String argNameSection = getUsageArgNameSection(as);
    if (argNameSection.length() > maxArgNameSectionLength)
      maxArgNameSectionLength = argNameSection.length(); 
    }

  // Now generate the message for each switch 
  for (int i = 0; i < switchSpecList.size(); i++)
    {
    PoptSwitchSpec as = (PoptSwitchSpec) switchSpecList.elementAt(i);
    String argNameSection = getUsageArgNameSection(as);
    s += argNameSection;
    for (int j = 0; j < maxArgNameSectionLength - argNameSection.length() + 4; j++)
      s += " ";
    s += as.description;
    s += "\n";
    }

  // Now generate the uage message for each non-switch 
  for (int i = 0; i < nonSwitchSpecList.size(); i++)
    {
    PoptNonSwitchSpec as = (PoptNonSwitchSpec) nonSwitchSpecList.elementAt(i);
    String name = getPreferredSwitchText(as);
    s += name;
    for (int j = 0; j < maxArgNameSectionLength - name.length() + 4; j++)
      s += " ";
    s += as.description;
    s += "\n";
    }
  return s;
  }

/**
Called to get an error message if a switch name is not matched. Override if you
want a different message or a different language.
*/
protected String getUnmatchedSwitchMessage()
  {
  return "Unrecognized switch: ";
  }

/**
Called to get an error message if a switch is supplied more than once. This may
not be an error, depending on the application 
*/
protected String getDuplicateSwitchMessage()
  {
  return "Duplicate switch: ";
  }

/**
Called to get an error message if a switch that requires an argument does not
have an argument. 
*/
protected String getMissingArgumentMessage()
  {
  return "Missing argument for switch: ";
  }

/**
Called to get an error message if an argument defined to be
an integer could not be converted to an integer 
*/
protected String getBadIntegerMessage()
  {
  return "Argument was not an integer: ";
  }

/**
Called to get an error message if an argument defined to be
compulsory was not supplied on the command line
*/
protected String getMissingCompulsoryArgumentMessage()
  {
  return "Compulsory argument was not supplied: ";
  }

/**
Called to get an error message if an argument defined to be
a double could not be converted to a double 
*/
protected String getBadDoubleMessage()
  {
  return "Argument was not a double: ";
  }

/**
Retrieves the last error message, if any. If there has been no error, 
returns null. This method can only usefully be called after one of the
parsing methods; other methods don't set the error message
*/
public String getErrorMessage()
  {
  return errorMessage;
  }

/*
Inner utility class to store argument details; this is the
base class for PoptSwitchSpec and PoptNonSwitchSpec,
A non-switch arg is modelled as a switch arg with no
switch names
*/
class PoptArgSpec
  {
  char shortName;
  String longName;
  boolean hasArg;
  Class argType;
  boolean compulsory;
  String argName;
  String description;
  String identifier;
  boolean allowDuplicates;
  PoptArgSpec (String _identifier, char _shortName, String _longName, boolean _compulsory,
          String _description, boolean _hasArg, String _argName, Class _argType, boolean _allowDuplicates)
    {
    identifier = _identifier;
    shortName = _shortName;
    longName = _longName;
    hasArg = _hasArg; 
    argType = _argType;
    compulsory = _compulsory;
    argName = _argName;
    description = _description;
    allowDuplicates = _allowDuplicates;
    }

  }
// End of inner class `PoptArgSpec'

/*
Inner utility class to store switch arg details
*/
class PoptSwitchSpec extends PoptArgSpec
  {
  PoptSwitchSpec (String _identifier, char _shortName, String _longName, boolean _compulsory,
          String _description, boolean _hasArg, String _argName, Class _argType, boolean _allowDuplicates)
    {
    super (_identifier, _shortName, _longName, _compulsory, 
      _description, _hasArg, _argName, _argType, _allowDuplicates);
    }

  }
// End of inner class `PoptSwitchSpec'

/*
Inner utility class to store non-switch arg details
*/
class PoptNonSwitchSpec extends PoptArgSpec
  {
  PoptNonSwitchSpec (String _identifier, String _argName, boolean _compulsory,
          String _description,  Class _argType)
    {
    super (_identifier, (char)0, null, _compulsory, 
      _description, true, _argName, _argType, true);
    }

  }
// End of inner class `PoptNonSwitchSpec'

/**
The main() method tests the JavaPopt parser. It accepts various command-line
arguments, and displays the values entered. Use the --help switch to
see which arguments are accepted
*/
public static void main(String[] args)
  {
  Popt popt = new Popt (args); 
  popt.setPoptParseHandler(new TestParseHandler());
  
  // Tell Popt what arguments we accept 
  popt.addSwitchSpec ("version", 'v', "version", false, "show version", false);
  popt.addSwitchSpec ("double", 'd', "double", false, "a real number", "double", Double.class, true);
  popt.addSwitchSpec ("string", 's', "string", false, "a piece of text", "string", String.class, true);
  popt.addSwitchSpec ("integer", 'i', "integer", false, "an integer", "string", Integer.class, true);
  popt.addSwitchSpec ("longflag", (char)0, "longflag", false, "a flag", true);
  popt.addNonSwitchSpec ("message", "message", false, "a piece of text", String.class);

  // Add help arguments for --help, -h, and -?
  popt.addHelp (true, true, true);

  // Parse the command line and check for `help' and `verion' arguments
  if (popt.parse() == false)
    {
    System.err.println (popt.getErrorMessage());
    System.err.println ("Usage: Popt " + popt.getShortUsage());
    System.err.println (popt.getUsage());
    }
  else if (popt.supplied("help"))
    {
    System.out.println ("Usage: Popt " + popt.getShortUsage());
    System.out.println (popt.getUsage());
    }
  else if (popt.supplied("version"))
     {
     System.out.println ("JavaPopt version 1.0 (c)2000 Kevin Boone");
     }
  else
    {
    if (popt.supplied("double"))
      System.out.println ("double=" + popt.getDouble("double"));
    else
      System.out.println ("double argument not supplied");
    if (popt.supplied("string"))
      System.out.println ("string=" + popt.getString("string"));
    else
      System.out.println ("string argument not supplied");
    if (popt.supplied("integer"))
      System.out.println ("integer=" + popt.getInt("integer"));
    else
      System.out.println ("integer argument not supplied");
    if (popt.supplied("longflag"))
      System.out.println ("longflag set");
    else
      System.out.println ("longflag not set");
    System.out.println (popt.getStringOrDefault ("message", "message argument not supplied"));
    }
  }

}


