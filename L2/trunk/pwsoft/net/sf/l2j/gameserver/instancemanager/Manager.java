package net.sf.l2j.gameserver.instancemanager;

public class Manager
{
  public static void reloadAll()
  {
    AuctionManager.getInstance().reload();
  }
}