package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Request;
import l2m.gameserver.model.Request.L2RequestType;
import l2m.gameserver.model.actor.instances.player.FriendList;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.L2Friend;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;

public class RequestFriendAddReply extends L2GameClientPacket
{
  private int _response;

  protected void readImpl()
  {
    _response = (_buf.hasRemaining() ? readD() : 0);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Request request = activeChar.getRequest();
    if ((request == null) || (!request.isTypeOf(Request.L2RequestType.FRIEND))) {
      return;
    }
    if (activeChar.isOutOfControl())
    {
      request.cancel();
      activeChar.sendActionFailed();
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
      activeChar.sendPacket(Msg.THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME);
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
      requestor.sendPacket(Msg.YOU_HAVE_FAILED_TO_INVITE_A_FRIEND);
      activeChar.sendActionFailed();
      return;
    }

    requestor.getFriendList().addFriend(activeChar);
    activeChar.getFriendList().addFriend(requestor);

    requestor.sendPacket(new IStaticPacket[] { Msg.YOU_HAVE_SUCCEEDED_IN_INVITING_A_FRIEND, new SystemMessage(132).addString(activeChar.getName()), new L2Friend(activeChar, true) });
    activeChar.sendPacket(new IStaticPacket[] { new SystemMessage(479).addString(requestor.getName()), new L2Friend(requestor, true) });
  }
}