package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ExListPartyMatchingWaitingRoom;

public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket
{
  private int _unk1;
  private int _minLvl;
  private int _maxLevel;
  private int _unk4;

  protected void readImpl()
  {
    _unk1 = readD();
    _minLvl = readD();
    _maxLevel = readD();
    _unk4 = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    player.sendPacket(new ExListPartyMatchingWaitingRoom(player, _unk1, _minLvl, _maxLevel));
  }
}