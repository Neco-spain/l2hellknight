package l2m.gameserver.model.actor.instances.player;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import l2m.gameserver.data.dao.CharacterFriendDAO;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.network.serverpackets.L2Friend;
import l2m.gameserver.network.serverpackets.L2FriendStatus;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import org.apache.commons.lang3.StringUtils;

public class FriendList
{
  private Map<Integer, Friend> _friendList = Collections.emptyMap();
  private final Player _owner;

  public FriendList(Player owner)
  {
    _owner = owner;
  }

  public void restore()
  {
    _friendList = CharacterFriendDAO.getInstance().select(_owner);
  }

  public void removeFriend(String name)
  {
    if (StringUtils.isEmpty(name))
      return;
    int objectId = removeFriend0(name);
    if (objectId > 0)
    {
      Player friendChar = World.getPlayer(objectId);

      _owner.sendPacket(new IStaticPacket[] { new SystemMessage(133).addString(name), new L2Friend(name, false, friendChar != null, objectId) });

      if (friendChar != null)
        friendChar.sendPacket(new IStaticPacket[] { new SystemMessage(481).addString(_owner.getName()), new L2Friend(_owner, false) });
    }
    else {
      _owner.sendPacket(new SystemMessage(171).addString(name));
    }
  }

  public void notifyFriends(boolean login) {
    for (Friend friend : _friendList.values())
    {
      Player friendPlayer = GameObjectsStorage.getPlayer(friend.getObjectId());
      if (friendPlayer != null)
      {
        Friend thisFriend = (Friend)friendPlayer.getFriendList().getList().get(Integer.valueOf(_owner.getObjectId()));
        if (thisFriend == null) {
          continue;
        }
        thisFriend.update(_owner, login);

        if (login) {
          friendPlayer.sendPacket(new SystemMessage(503).addString(_owner.getName()));
        }
        friendPlayer.sendPacket(new L2FriendStatus(_owner, login));

        friend.update(friendPlayer, login);
      }
    }
  }

  public void addFriend(Player friendPlayer)
  {
    _friendList.put(Integer.valueOf(friendPlayer.getObjectId()), new Friend(friendPlayer));

    CharacterFriendDAO.getInstance().insert(_owner, friendPlayer);
  }

  private int removeFriend0(String name)
  {
    if (name == null) {
      return 0;
    }
    Integer objectId = Integer.valueOf(0);
    for (Map.Entry entry : _friendList.entrySet())
    {
      if (name.equalsIgnoreCase(((Friend)entry.getValue()).getName()))
      {
        objectId = (Integer)entry.getKey();
        break;
      }
    }

    if (objectId.intValue() > 0)
    {
      _friendList.remove(objectId);
      CharacterFriendDAO.getInstance().delete(_owner, objectId.intValue());
      return objectId.intValue();
    }
    return 0;
  }

  public Map<Integer, Friend> getList()
  {
    return _friendList;
  }

  public String toString()
  {
    return "FriendList[owner=" + _owner.getName() + "]";
  }
}