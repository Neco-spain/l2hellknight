package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager.WaitingRoom;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchList;

public final class RequestOustFromPartyRoom extends L2GameClientPacket
{
  private int _charId;

  protected void readImpl()
  {
    _charId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }
    L2PcInstance target = L2World.getInstance().getPlayer(_charId);
    if ((target == null) || (target.equals(player)))
    {
      player.sendPacket(Static.TARGET_IS_INCORRECT);
      return;
    }

    PartyWaitingRoomManager.WaitingRoom rRoom = player.getPartyRoom();
    if (rRoom == null) {
      return;
    }
    if (!rRoom.owner.equals(player)) {
      return;
    }
    PartyWaitingRoomManager.WaitingRoom tRoom = target.getPartyRoom();
    if (tRoom == null) {
      return;
    }
    if (tRoom.id != rRoom.id) {
      return;
    }
    PartyWaitingRoomManager.getInstance().exitRoom(target, rRoom);
    target.sendPacket(new PartyMatchList(player, 0, -1, 0, 0, ""));
    PartyWaitingRoomManager.getInstance().registerPlayer(target);
    target.setLFP(true);
    target.broadcastUserInfo();
  }
}