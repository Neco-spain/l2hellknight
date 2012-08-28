package net.sf.l2j.gameserver.network.serverpackets;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
  private final ConcurrentLinkedQueue<L2PcInstance> _finders;
  private int _size;

  public ExListPartyMatchingWaitingRoom(L2PcInstance player, int page, int minLvl, int maxLvl)
  {
    _finders = PartyWaitingRoomManager.getInstance().getFinders(page, minLvl, maxLvl, new ConcurrentLinkedQueue());
    _size = _finders.size();
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(53);

    writeD(_size);
    if (_size == 0)
    {
      writeD(0);
      return;
    }
    writeD(_size);

    for (L2PcInstance player : _finders)
    {
      if (player == null) {
        continue;
      }
      writeS(player.getName());
      writeD(player.getActiveClass());
      writeD(player.getLevel());
    }
  }
}