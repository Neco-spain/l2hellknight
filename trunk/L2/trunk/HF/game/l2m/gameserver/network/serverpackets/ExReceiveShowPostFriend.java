package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;
import org.napile.primitive.maps.IntObjectMap;

public class ExReceiveShowPostFriend extends L2GameServerPacket
{
  private IntObjectMap<String> _list;

  public ExReceiveShowPostFriend(Player player)
  {
    _list = player.getPostFriends();
  }

  public void writeImpl()
  {
    writeEx(211);
    writeD(_list.size());
    for (String t : _list.values())
      writeS(t);
  }
}