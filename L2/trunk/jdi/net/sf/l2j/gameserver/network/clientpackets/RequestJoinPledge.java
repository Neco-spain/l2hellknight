package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Request;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AskJoinPledge;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinPledge extends L2GameClientPacket
{
  private static final String _C__24_REQUESTJOINPLEDGE = "[C] 24 RequestJoinPledge";
  private int _target;
  private int _pledgeType;

  protected void readImpl()
  {
    _target = readD();
    _pledgeType = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
    {
      return;
    }
    if (!(L2World.getInstance().findObject(_target) instanceof L2PcInstance))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
      return;
    }

    L2PcInstance target = (L2PcInstance)L2World.getInstance().findObject(_target);
    L2Clan clan = activeChar.getClan();
    if (!clan.checkClanJoinCondition(activeChar, target, _pledgeType))
    {
      return;
    }
    if (!activeChar.getRequest().setRequest(target, this))
    {
      return;
    }

    SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_INVITED_YOU_TO_JOIN_THE_CLAN_S2);
    sm.addString(activeChar.getName());
    sm.addString(activeChar.getClan().getName());
    target.sendPacket(sm);
    sm = null;
    AskJoinPledge ap = new AskJoinPledge(activeChar.getObjectId(), activeChar.getClan().getName());
    target.sendPacket(ap);
  }

  public int getPledgeType()
  {
    return _pledgeType;
  }

  public String getType()
  {
    return "[C] 24 RequestJoinPledge";
  }
}