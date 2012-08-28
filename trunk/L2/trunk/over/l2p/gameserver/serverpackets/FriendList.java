package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.actor.instances.player.Friend;

public class FriendList extends L2GameServerPacket
{
  private List<FriendInfo> _friends = Collections.emptyList();

  public FriendList(Player player)
  {
    Map friends = player.getFriendList().getList();
    _friends = new ArrayList(friends.size());
    for (Map.Entry entry : friends.entrySet())
    {
      Friend friend = (Friend)entry.getValue();
      FriendInfo f = new FriendInfo(null);
      FriendInfo.access$102(f, friend.getName());
      FriendInfo.access$202(f, friend.getClassId());
      FriendInfo.access$302(f, ((Integer)entry.getKey()).intValue());
      FriendInfo.access$402(f, friend.getLevel());
      FriendInfo.access$502(f, friend.isOnline());
      _friends.add(f);
    }
  }

  protected void writeImpl()
  {
    writeC(88);
    writeD(_friends.size());
    for (FriendInfo f : _friends)
    {
      writeD(f.objectId);
      writeS(f.name);
      writeD(f.online);
      writeD(f.online ? f.objectId : 0);
      writeD(f.classId);
      writeD(f.level);
    }
  }

  private class FriendInfo
  {
    private String name;
    private int objectId;
    private boolean online;
    private int level;
    private int classId;

    private FriendInfo()
    {
    }
  }
}