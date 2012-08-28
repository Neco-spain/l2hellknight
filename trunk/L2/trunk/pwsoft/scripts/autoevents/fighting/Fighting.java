package scripts.autoevents.fighting;

import java.io.PrintStream;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class Fighting
{
  private static Fighting _event = new Fighting();

  public static void init() {
    System.out.println("EventManager [ERROR]: Fighting, only for advanced.");
  }

  public static Fighting getEvent()
  {
    return _event;
  }

  public void announceWinner(L2PcInstance player)
  {
  }

  private boolean foundIp(String ip)
  {
    return false;
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