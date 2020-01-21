package me.kevinboone.javachimes;
import javax.sound.midi.*;

/**
Helper class used by JavaChimes to store information about which
notes are currently sounding, and how long they have been sounding
*/
class NoteInfo 
{
int kNum; // MIDI key number
long startTime; // Time struck
NoteInfo (final int _kNum, final long _startTime)
  {
  kNum = _kNum;
  startTime = _startTime;
  }
}
