package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager.WaitingRoom;

public class PartyMatchDetail extends L2GameServerPacket
{
  private PartyWaitingRoomManager.WaitingRoom _room;

  public PartyMatchDetail(PartyWaitingRoomManager.WaitingRoom room)
  {
    _room = room;
  }

  protected final void writeImpl()
  {
    writeC(151);
    writeD(_room.id);
    writeD(_room.maxPlayers);
    writeD(_room.minLvl);
    writeD(_room.maxLvl);
    writeD(_room.loot);
    writeD(_room.location);
    writeS(_room.title);
  }
}