package l2m.gameserver.serverpackets;

import l2m.gameserver.model.matching.MatchingRoom;

public class PartyRoomInfo extends L2GameServerPacket
{
  private int _id;
  private int _minLevel;
  private int _maxLevel;
  private int _lootDist;
  private int _maxMembers;
  private int _location;
  private String _title;

  public PartyRoomInfo(MatchingRoom room)
  {
    _id = room.getId();
    _minLevel = room.getMinLevel();
    _maxLevel = room.getMaxLevel();
    _lootDist = room.getLootType();
    _maxMembers = room.getMaxMembersSize();
    _location = room.getLocationId();
    _title = room.getTopic();
  }

  protected final void writeImpl()
  {
    writeC(157);
    writeD(_id);
    writeD(_maxMembers);
    writeD(_minLevel);
    writeD(_maxLevel);
    writeD(_lootDist);
    writeD(_location);
    writeS(_title);
  }
}