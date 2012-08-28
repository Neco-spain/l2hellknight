package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Request;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestAnswerJoinAlly extends L2GameClientPacket
{
  private static final String _C__83_REQUESTANSWERJOINALLY = "[C] 83 RequestAnswerJoinAlly";
  private int _response;

  protected void readImpl()
  {
    _response = readD();
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

    if (_response == 0)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_ALLY_INVITATION));
      requestor.sendPacket(new SystemMessage(SystemMessageId.NO_RESPONSE_TO_ALLY_INVITATION));
    }
    else
    {
      if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinAlly))
      {
        return;
      }

      L2Clan clan = requestor.getClan();

      if (clan.checkAllyJoinCondition(requestor, activeChar))
      {
        requestor.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND));

        activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ACCEPTED_ALLIANCE));

        activeChar.getClan().setAllyId(clan.getAllyId());
        activeChar.getClan().setAllyName(clan.getAllyName());
        activeChar.getClan().setAllyPenaltyExpiryTime(0L, 0);
        activeChar.getClan().updateClanInDB();
      }
    }

    activeChar.getRequest().onRequestResponse();
  }

  public String getType()
  {
    return "[C] 83 RequestAnswerJoinAlly";
  }
}