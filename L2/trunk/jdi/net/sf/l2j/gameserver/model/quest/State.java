package net.sf.l2j.gameserver.model.quest;

public class State
{
  public static final byte CREATED = 0;
  public static final byte STARTED = 1;
  public static final byte COMPLETED = 2;

  public static String getStateName(byte state)
  {
    switch (state)
    {
    case 1:
      return "Started";
    case 2:
      return "Completed";
    }
    return "Start";
  }

  public static byte getStateId(String statename)
  {
    if (statename.equals("Started"))
      return 1;
    if (statename.equals("Completed"))
      return 2;
    return 0;
  }
}