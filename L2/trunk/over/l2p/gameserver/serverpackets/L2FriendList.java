package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.actor.instances.player.Friend;
import l2p.gameserver.model.actor.instances.player.FriendList;

public class L2FriendList extends L2GameServerPacket
{
  private List<FriendInfo> _list = Collections.emptyList();

  public L2FriendList(Player player)
  {
    Map list = player.getFriendList().getList();
    _list = new ArrayList(list.size());
    for (Map.Entry entry : list.entrySet())
    {
      FriendInfo f = new FriendInfo(null);
      FriendInfo.access$102(f, ((Integer)entry.getKey()).intValue());
      FriendInfo.access$202(f, ((Friend)entry.getValue()).getName());
      FriendInfo.access$302(f, ((Friend)entry.getValue()).isOnline());
      _list.add(f);
    }
  }

  protected final void writeImpl()
  {
    writeC(117);
    writeD(_list.size());
    for (FriendInfo friendInfo : _list)
    {
      writeD(0);
      writeS(friendInfo._name);
      writeD(friendInfo._online ? 1 : 0);
      writeD(friendInfo._objectId);
    }
  }

  private static class FriendInfo
  {
    private int _objectId;
    private String _name;
    private boolean _online;
  }
}