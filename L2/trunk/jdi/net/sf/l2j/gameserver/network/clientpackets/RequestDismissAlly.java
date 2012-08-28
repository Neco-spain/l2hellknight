package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestDismissAlly extends L2GameClientPacket
{
  private static final String _C__86_REQUESTDISMISSALLY = "[C] 86 RequestDismissAlly";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
    {
      return;
    }
    if (!activeChar.isClanLeader())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
      return;
    }
    activeChar.getClan().dissolveAlly(activeChar);
  }

  public String getType()
  {
    return "[C] 86 RequestDismissAlly";
  }
}