package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.FriendRecvMsg;

public final class RequestSendFriendMsg extends L2GameClientPacket
{
  private String _message;
  private String _reciever;

  protected void readImpl()
  {
    _message = readS();
    _reciever = readS();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    L2PcInstance targetPlayer = L2World.getInstance().getPlayer(_reciever);
    if (targetPlayer == null)
    {
      player.sendPacket(Static.TARGET_IS_NOT_FOUND_IN_THE_GAME);
      return;
    }

    if (!player.haveFriend(targetPlayer.getObjectId()))
    {
      player.sendPacket(Static.FRIEND_NOT_FOUND);
      return;
    }

    targetPlayer.sendPacket(new FriendRecvMsg(player.getName(), _reciever, _message));
  }
}