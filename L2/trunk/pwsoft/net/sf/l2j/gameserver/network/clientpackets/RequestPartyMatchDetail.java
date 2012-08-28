package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestPartyMatchDetail extends L2GameClientPacket
{
  private int _roomId;
  private int _unk1;
  private int _unk2;
  private int _unk3;

  protected void readImpl()
  {
    _roomId = readD();
    _unk1 = readD();
    _unk2 = readD();
    _unk3 = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    PartyWaitingRoomManager.getInstance().joinRoom(player, _roomId);
  }
}