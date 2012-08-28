package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class FriendManage extends L2GameServerPacket
{
  private static final String _S__FB_FRIENDMANAGE = "[S] FB FriendManage";
  boolean _add;
  String _name;
  boolean _online;
  int _object_id;

  public FriendManage(L2PcInstance player, boolean add)
  {
    _add = add;
    _name = player.getName();
    _object_id = player.getObjectId();
    _online = true;
  }

  public FriendManage(String name, boolean add, boolean online, int object_id)
  {
    _name = name;
    _add = add;
    _object_id = object_id;
    _online = online;
  }

  protected final void writeImpl()
  {
    writeC(251);
    writeD(_add ? 1 : 3);
    writeD(0);
    writeS(_name);
    writeD(_online ? 1 : 0);
    writeD(_object_id);
  }

  public String getType()
  {
    return _S__FB_FRIENDMANAGE;
  }
}