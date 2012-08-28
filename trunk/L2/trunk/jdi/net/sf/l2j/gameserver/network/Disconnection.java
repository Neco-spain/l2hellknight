package net.sf.l2j.gameserver.network;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class Disconnection
  implements Runnable
{
  private L2PcInstance _activeChar;

  public Disconnection(L2PcInstance activeChar)
  {
    _activeChar = activeChar;
  }

  public void run()
  {
    _activeChar.closeNetConnection(true);
  }
}