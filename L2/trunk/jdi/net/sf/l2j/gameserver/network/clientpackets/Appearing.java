package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public final class Appearing extends L2GameClientPacket
{
  private static final String _C__30_APPEARING = "[C] 30 Appearing";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;
    if (activeChar.isTeleporting()) activeChar.onTeleported();

    sendPacket(new UserInfo(activeChar));
  }

  public String getType()
  {
    return "[C] 30 Appearing";
  }
}