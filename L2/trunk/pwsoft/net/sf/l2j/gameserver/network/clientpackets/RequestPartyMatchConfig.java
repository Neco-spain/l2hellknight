package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager.WaitingRoom;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestPartyMatchConfig extends L2GameClientPacket
{
  private int _unk1;
  private int _maxPlayers;
  private int _minLvl;
  private int _maxLvl;
  private int _unk5;
  private String title;
  private boolean error = false;

  protected void readImpl()
  {
    try
    {
      _unk1 = readD();
      _maxPlayers = readD();
      _minLvl = readD();
      _maxLvl = readD();
      _unk5 = readD();
      title = readS();
    } catch (BufferUnderflowException e) {
      error = true;
    }
  }

  protected void runImpl()
  {
    if (error) {
      return;
    }

    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    PartyWaitingRoomManager.WaitingRoom room = player.getPartyRoom();
    if (room == null) {
      PartyWaitingRoomManager.getInstance().registerRoom(player, title, _maxPlayers, _minLvl, _maxLvl, TownManager.getInstance().getClosestLocation(player));
    } else {
      room.maxPlayers = _maxPlayers;
      room.minLvl = _minLvl;
      room.maxLvl = _maxLvl;
      room.title = title;
      room.location = TownManager.getInstance().getClosestLocation(player);
      PartyWaitingRoomManager.getInstance().refreshRoom(room);
    }

    if (player.isLFP()) {
      player.setLFP(false);
      player.broadcastUserInfo();
    }
  }
}