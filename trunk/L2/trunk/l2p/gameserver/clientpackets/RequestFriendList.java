package l2p.gameserver.clientpackets;

import java.util.Map;
import java.util.Map.Entry;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.actor.instances.player.Friend;
import l2p.gameserver.model.actor.instances.player.FriendList;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestFriendList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    activeChar.sendPacket(Msg._FRIENDS_LIST_);
    Map _list = activeChar.getFriendList().getList();
    for (Map.Entry entry : _list.entrySet())
    {
      Player friend = World.getPlayer(((Integer)entry.getKey()).intValue());
      if (friend != null)
        activeChar.sendPacket(new SystemMessage(488).addName(friend));
      else
        activeChar.sendPacket(new SystemMessage(489).addString(((Friend)entry.getValue()).getName()));
    }
    activeChar.sendPacket(Msg.__EQUALS__);
  }
}