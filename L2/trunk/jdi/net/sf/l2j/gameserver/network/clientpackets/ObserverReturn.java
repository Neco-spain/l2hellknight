package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class ObserverReturn extends L2GameClientPacket
{
  private static final String OBSRETURN__C__04 = "[C] b8 ObserverReturn";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    if (activeChar.inObserverMode()) activeChar.leaveObserverMode();
  }

  public String getType()
  {
    return "[C] b8 ObserverReturn";
  }
}