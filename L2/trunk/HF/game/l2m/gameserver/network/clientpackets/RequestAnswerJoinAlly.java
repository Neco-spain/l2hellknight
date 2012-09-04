package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Request;
import l2m.gameserver.model.Request.L2RequestType;
import l2m.gameserver.model.pledge.Alliance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.GameClient;

public class RequestAnswerJoinAlly extends L2GameClientPacket
{
  private int _response;

  protected void readImpl()
  {
    _response = (_buf.remaining() >= 4 ? readD() : 0);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Request request = activeChar.getRequest();
    if ((request == null) || (!request.isTypeOf(Request.L2RequestType.ALLY))) {
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
      activeChar.sendPacket(Msg.THAT_PLAYER_IS_NOT_ONLINE);
      activeChar.sendActionFailed();
      return;
    }

    if (requestor.getRequest() != request)
    {
      request.cancel();
      activeChar.sendActionFailed();
      return;
    }

    if (requestor.getAlliance() == null)
    {
      request.cancel();
      activeChar.sendActionFailed();
      return;
    }

    if (_response == 0)
    {
      request.cancel();
      requestor.sendPacket(Msg.YOU_HAVE_FAILED_TO_INVITE_A_CLAN_INTO_THE_ALLIANCE);
      return;
    }

    try
    {
      Alliance ally = requestor.getAlliance();
      activeChar.sendPacket(Msg.YOU_HAVE_ACCEPTED_THE_ALLIANCE);
      activeChar.getClan().setAllyId(requestor.getAllyId());
      activeChar.getClan().updateClanInDB();
      ally.addAllyMember(activeChar.getClan(), true);
      ally.broadcastAllyStatus();
    }
    finally
    {
      request.done();
    }
  }
}