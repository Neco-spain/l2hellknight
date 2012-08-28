package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TransactionType;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AskJoinFriend;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

public final class RequestFriendInvite extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestFriendInvite.class.getName());
  private String _name;

  protected void readImpl()
  {
    _name = readS();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }
    if (player.isTransactionInProgress())
    {
      player.sendPacket(Static.WAITING_FOR_ANOTHER_REPLY);
      return;
    }

    if (System.currentTimeMillis() - player.gCPBB() < 100L)
      return;
    player.sCPBB();

    L2PcInstance friend = L2World.getInstance().getPlayer(_name);

    if (friend == null)
    {
      if (player.getTargetId() != -1) {
        friend = L2World.getInstance().getPlayer(player.getTargetId());
      }
      if (friend == null)
      {
        player.sendPacket(Static.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
        return;
      }
    }
    _name = Util.capitalizeFirst(friend.getName());

    if (friend == player)
    {
      player.sendPacket(Static.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
      return;
    }

    if ((friend.isAlone()) || ((friend.isGM()) && (friend.getMessageRefusal())))
    {
      player.sendMessage("\u0418\u0433\u0440\u043E\u043A \u043F\u0440\u043E\u0441\u0438\u043B \u0435\u0433\u043E \u043D\u0435 \u0431\u0435\u0441\u043F\u043E\u043A\u043E\u0438\u0442\u044C");
      player.sendActionFailed();
      return;
    }

    if (player.haveFriend(friend.getObjectId()))
    {
      player.sendPacket(SystemMessage.id(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST).addString(_name));
      return;
    }
    SystemMessage sm;
    if (!friend.isTransactionInProgress())
    {
      friend.setTransactionRequester(player, System.currentTimeMillis() + 10000L);
      friend.setTransactionType(L2PcInstance.TransactionType.FRIEND);
      player.setTransactionRequester(friend, System.currentTimeMillis() + 10000L);
      player.setTransactionType(L2PcInstance.TransactionType.FRIEND);

      friend.sendPacket(new AskJoinFriend(player.getName()));
      sm = SystemMessage.id(SystemMessageId.S1_REQUESTED_TO_BECOME_FRIENDS).addString(player.getName());
    }
    else {
      sm = SystemMessage.id(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(friend.getName());
    }
    friend.sendPacket(sm);
    SystemMessage sm = null;
  }
}