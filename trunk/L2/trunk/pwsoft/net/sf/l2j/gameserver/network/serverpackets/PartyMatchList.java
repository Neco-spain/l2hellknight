package net.sf.l2j.gameserver.network.serverpackets;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager.WaitingRoom;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PartyMatchList extends L2GameServerPacket
{
  private L2PcInstance _player;
  private ConcurrentLinkedQueue<PartyWaitingRoomManager.WaitingRoom> _rooms;
  private int _size;

  public PartyMatchList(L2PcInstance player, int unk1, int territoryId, int levelType, int unk4, String unk5)
  {
    _player = player;
    if (levelType == 0)
      levelType = player.getLevel();
    else
      levelType = -1;
    _rooms = PartyWaitingRoomManager.getInstance().getRooms(levelType, territoryId, new ConcurrentLinkedQueue());
    _size = _rooms.size();
  }

  protected final void writeImpl()
  {
    writeC(150);
    writeD(_size > 0 ? 1 : 0);
    writeD(_size);

    for (PartyWaitingRoomManager.WaitingRoom room : _rooms)
    {
      if (room == null) {
        continue;
      }
      writeD(room.id);
      writeS(room.title);
      writeD(room.location);
      writeD(room.minLvl);
      writeD(room.maxLvl);
      writeD(room.players.size());
      writeD(room.maxPlayers);
      writeS(room.leaderName);
    }
  }
}