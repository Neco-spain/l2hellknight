package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Request;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.JoinPledge;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestAnswerJoinPledge extends L2GameClientPacket
{
  private static final String _C__25_REQUESTANSWERJOINPLEDGE = "[C] 25 RequestAnswerJoinPledge";
  private int _answer;

  protected void readImpl()
  {
    _answer = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
    {
      return;
    }

    L2PcInstance requestor = activeChar.getRequest().getPartner();
    if (requestor == null)
    {
      return;
    }

    if (_answer == 0)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION);
      sm.addString(requestor.getName());
      activeChar.sendPacket(sm);
      sm = null;
      sm = new SystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_TO_CLAN_INVITATION);
      sm.addString(activeChar.getName());
      requestor.sendPacket(sm);
      sm = null;
    }
    else
    {
      if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge))
      {
        return;
      }

      RequestJoinPledge requestPacket = (RequestJoinPledge)requestor.getRequest().getRequestPacket();
      L2Clan clan = requestor.getClan();

      if (clan.checkClanJoinCondition(requestor, activeChar, requestPacket.getPledgeType()))
      {
        JoinPledge jp = new JoinPledge(requestor.getClanId());
        activeChar.sendPacket(jp);

        activeChar.setPledgeType(requestPacket.getPledgeType());
        if (requestPacket.getPledgeType() == -1)
        {
          activeChar.setPowerGrade(9);
          activeChar.setLvlJoinedAcademy(activeChar.getLevel());
        }
        else
        {
          activeChar.setPowerGrade(5);
        }
        clan.addClanMember(activeChar);
        activeChar.setClanPrivileges(activeChar.getClan().getRankPrivs(activeChar.getPowerGrade()));

        activeChar.sendPacket(new SystemMessage(SystemMessageId.ENTERED_THE_CLAN));

        SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN);
        sm.addString(activeChar.getName());
        clan.broadcastToOnlineMembers(sm);
        sm = null;

        clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(activeChar), activeChar);
        clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));

        activeChar.sendPacket(new PledgeShowMemberListAll(clan, activeChar));
        activeChar.setClanJoinExpiryTime(0L);
        activeChar.broadcastUserInfo();
      }
    }

    activeChar.getRequest().onRequestResponse();
  }

  public String getType()
  {
    return "[C] 25 RequestAnswerJoinPledge";
  }
}