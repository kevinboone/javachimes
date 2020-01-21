package me.kevinboone.utils.javapopt;
import java.util.*;
import java.io.*;

/**
Java command-line argument parser V1.0

See `overview' for information on how to use this
interface

(c)2001 Kevin Boone/Web-Tomorrow, all rights reserved
*/
public interface PoptParseHandler
{
/**
This method is called by the Popt parser every time it matches an
argument. 
*/
public boolean handleArg(Popt popt, String identifier, Object value);
}


/**
Package-access class TestParseHandler is used on by the Popt test client
*/
class TestParseHandler implements PoptParseHandler
{
public boolean handleArg(Popt popt, String identifier, Object value)
  {
  System.out.println ("Got arg, identifier=" + identifier + ", value = " + value);
  // We can indicate that parsing failed by doing this:
  //  popt.setErrorMessage("why I failed")
  //  return false
  return true;
  }
}

