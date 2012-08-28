package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestExitPartyMatchingWaitingRoom extends L2GameClientPacket
{
  private static final String _C__D0_17_REQUESTEXITPARTYMATCHINGWAITINGROOM = "[C] D0:17 RequestExitPartyMatchingWaitingRoom";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    activeChar.broadcastUserInfo();
  }

  public String getType()
  {
    return "[C] D0:17 RequestExitPartyMatchingWaitingRoom";
  }
}