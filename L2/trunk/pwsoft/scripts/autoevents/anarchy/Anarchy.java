package scripts.autoevents.anarchy;

import java.io.PrintStream;

public class Anarchy
{
  private static Anarchy _event = new Anarchy();

  public static void init() {
    System.out.println("EventManager [ERROR]: Anarchy, only for advanced.");
  }

  public static Anarchy getEvent()
  {
    return _event;
  }

  public boolean isInBattle()
  {
    return false;
  }

  public boolean isInBattle(int townId)
  {
    return false;
  }
}