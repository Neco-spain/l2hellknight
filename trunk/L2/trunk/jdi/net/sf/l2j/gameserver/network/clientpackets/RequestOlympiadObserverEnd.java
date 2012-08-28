package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestOlympiadObserverEnd extends L2GameClientPacket
{
  private static final String _C__D0_12_REQUESTOLYMPIADOBSERVEREND = "[C] D0:12 RequestOlympiadObserverEnd";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    if (activeChar.inObserverMode()) activeChar.leaveOlympiadObserverMode();
  }

  public String getType()
  {
    return "[C] D0:12 RequestOlympiadObserverEnd";
  }
}