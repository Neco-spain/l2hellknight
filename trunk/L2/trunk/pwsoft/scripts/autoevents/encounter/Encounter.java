package scripts.autoevents.encounter;

import java.io.PrintStream;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class Encounter
{
  private static Encounter _event = new Encounter();

  public static void init() {
    System.out.println("EventManager [ERROR]: Encounter, only for advanced.");
  }

  public static Encounter getEvent()
  {
    return _event;
  }

  public void announceWinner(L2PcInstance player, int max)
  {
  }

  public void regPlayer(L2PcInstance player)
  {
  }

  public void delPlayer(L2PcInstance player)
  {
  }

  public void notifyFail(L2PcInstance player)
  {
  }

  public void notifyDeath(L2PcInstance player)
  {
  }

  public boolean isRegged(L2PcInstance player)
  {
    return false;
  }

  public boolean isInBattle()
  {
    return false;
  }
}