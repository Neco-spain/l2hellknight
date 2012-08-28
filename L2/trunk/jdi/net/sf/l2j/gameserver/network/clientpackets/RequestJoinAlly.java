package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Request;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AskJoinAlly;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinAlly extends L2GameClientPacket
{
  private static final String _C__82_REQUESTJOINALLY = "[C] 82 RequestJoinAlly";
  private int _id;

  protected void readImpl()
  {
    _id = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
    {
      return;
    }
    if (!(L2World.getInstance().findObject(_id) instanceof L2PcInstance))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
      return;
    }
    if (activeChar.getClan() == null)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER));
      return;
    }
    L2PcInstance target = (L2PcInstance)L2World.getInstance().findObject(_id);
    L2Clan clan = activeChar.getClan();
    if (!clan.checkAllyJoinCondition(activeChar, target))
    {
      return;
    }
    if (!activeChar.getRequest().setRequest(target, this))
    {
      return;
    }

    SystemMessage sm = new SystemMessage(SystemMessageId.S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE);
    sm.addString(activeChar.getClan().getAllyName());
    sm.addString(activeChar.getName());
    target.sendPacket(sm);
    sm = null;
    AskJoinAlly aja = new AskJoinAlly(activeChar.getObjectId(), activeChar.getClan().getAllyName());
    target.sendPacket(aja);
  }

  public String getType()
  {
    return "[C] 82 RequestJoinAlly";
  }
}