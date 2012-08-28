package l2p.gameserver.serverpackets;

import l2p.gameserver.model.matching.MatchingRoom;

public class ExMpccRoomInfo extends L2GameServerPacket
{
  private int _index;
  private int _memberSize;
  private int _minLevel;
  private int _maxLevel;
  private int _lootType;
  private int _locationId;
  private String _topic;

  public ExMpccRoomInfo(MatchingRoom matching)
  {
    _index = matching.getId();
    _locationId = matching.getLocationId();
    _topic = matching.getTopic();
    _minLevel = matching.getMinLevel();
    _maxLevel = matching.getMaxLevel();
    _memberSize = matching.getMaxMembersSize();
    _lootType = matching.getLootType();
  }

  public void writeImpl()
  {
    writeEx(155);

    writeD(_index);
    writeD(_memberSize);
    writeD(_minLevel);
    writeD(_maxLevel);
    writeD(_lootType);
    writeD(_locationId);
    writeS(_topic);
  }
}