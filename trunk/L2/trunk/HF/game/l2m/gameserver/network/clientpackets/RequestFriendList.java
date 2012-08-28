package l2m.gameserver.network.clientpackets;

import java.util.Map;
import java.util.Map.Entry;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.actor.instances.player.Friend;
import l2m.gameserver.model.actor.instances.player.FriendList;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.SystemMessage;

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