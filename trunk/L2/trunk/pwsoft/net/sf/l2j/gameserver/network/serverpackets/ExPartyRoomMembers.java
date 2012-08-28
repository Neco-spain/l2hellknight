package net.sf.l2j.gameserver.network.serverpackets;

import java.util.concurrent.ConcurrentLinkedQueue;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager.WaitingRoom;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class ExPartyRoomMembers extends L2GameServerPacket
{
  private PartyWaitingRoomManager.WaitingRoom _room;
  private L2PcInstance _player;
  private ConcurrentLinkedQueue<L2PcInstance> _players;

  public ExPartyRoomMembers(L2PcInstance player, PartyWaitingRoomManager.WaitingRoom room)
  {
    _room = room;
    _players = room.players;
  }

  protected final void writeImpl()
  {
    if (_players == null)
      return;
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    writeC(254);
    writeH(14);

    writeD(_room.owner.equals(activeChar) ? 1 : 0);

    writeD(_room.players.size());
    for (L2PcInstance player : _players)
    {
      if (player == null) {
        continue;
      }
      writeD(player.getObjectId());
      writeS(player.getName());
      writeD(player.getActiveClass());
      writeD(player.getLevel());
      writeD(TownManager.getInstance().getClosestLocation(player));
      if (_room.owner.equals(player)) {
        writeD(1);
      }
      else if ((_room.owner.getParty() != null) && (_room.owner.getParty().getPartyMembers().contains(player)))
        writeD(2);
      else
        writeD(0);
    }
  }
}