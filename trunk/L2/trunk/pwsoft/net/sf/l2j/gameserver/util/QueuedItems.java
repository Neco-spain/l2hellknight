package net.sf.l2j.gameserver.util;

public class QueuedItems
{
  private static QueuedItems _instance;

  public static QueuedItems getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new QueuedItems();
  }
}