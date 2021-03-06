package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Request;
import l2p.gameserver.model.Request.L2RequestType;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.JoinParty;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class RequestAnswerJoinParty extends L2GameClientPacket
{
  private int _response;

  protected void readImpl()
  {
    if (_buf.hasRemaining())
      _response = readD();
    else
      _response = 0;
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Request request = activeChar.getRequest();
    if ((request == null) || (!request.isTypeOf(Request.L2RequestType.PARTY))) {
      return;
    }
    if (!request.isInProgress())
    {
      request.cancel();
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isOutOfControl())
    {
      request.cancel();
      activeChar.sendActionFailed();
      return;
    }

    Player requestor = request.getRequestor();
    if (requestor == null)
    {
      request.cancel();
      activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
      activeChar.sendActionFailed();
      return;
    }

    if (requestor.getRequest() != request)
    {
      request.cancel();
      activeChar.sendActionFailed();
      return;
    }

    if (_response <= 0)
    {
      request.cancel();
      requestor.sendPacket(JoinParty.FAIL);
      return;
    }

    if (activeChar.isInOlympiadMode())
    {
      request.cancel();
      activeChar.sendPacket(SystemMsg.A_PARTY_CANNOT_BE_FORMED_IN_THIS_AREA);
      requestor.sendPacket(JoinParty.FAIL);
      return;
    }

    if (requestor.isInOlympiadMode())
    {
      request.cancel();
      requestor.sendPacket(JoinParty.FAIL);
      return;
    }

    Party party = requestor.getParty();

    if ((party != null) && (party.getMemberCount() >= 9))
    {
      request.cancel();
      activeChar.sendPacket(SystemMsg.THE_PARTY_IS_FULL);
      requestor.sendPacket(SystemMsg.THE_PARTY_IS_FULL);
      requestor.sendPacket(JoinParty.FAIL);
      return;
    }

    IStaticPacket problem = activeChar.canJoinParty(requestor);
    if (problem != null)
    {
      request.cancel();
      activeChar.sendPacket(new IStaticPacket[] { problem, ActionFail.STATIC });
      requestor.sendPacket(JoinParty.FAIL);
      return;
    }

    if (party == null)
    {
      int itemDistribution = request.getInteger("itemDistribution");
      requestor.setParty(party = new Party(requestor, itemDistribution));
    }

    try
    {
      activeChar.joinParty(party);
      requestor.sendPacket(JoinParty.SUCCESS);
    }
    finally
    {
      request.done();
    }
  }
}