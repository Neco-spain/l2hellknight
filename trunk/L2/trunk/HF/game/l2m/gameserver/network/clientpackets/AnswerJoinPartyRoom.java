package l2m.gameserver.clientpackets;

import java.nio.ByteBuffer;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Request;
import l2m.gameserver.model.Request.L2RequestType;
import l2m.gameserver.model.matching.MatchingRoom;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class AnswerJoinPartyRoom extends L2GameClientPacket
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
    if ((request == null) || (!request.isTypeOf(Request.L2RequestType.PARTY_ROOM))) {
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

    if (_response == 0)
    {
      request.cancel();
      requestor.sendPacket(SystemMsg.THE_PLAYER_DECLINED_TO_JOIN_YOUR_PARTY);
      return;
    }

    if (activeChar.getMatchingRoom() != null)
    {
      request.cancel();
      activeChar.sendActionFailed();
      return;
    }

    try
    {
      MatchingRoom room = requestor.getMatchingRoom();
      if ((room == null) || (room.getType() != MatchingRoom.PARTY_MATCHING))
        return;
      room.addMember(activeChar);
    }
    finally
    {
      request.done();
    }
  }
}