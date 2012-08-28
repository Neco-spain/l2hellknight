package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchList;

public class RequestPartyMatchList extends L2GameClientPacket
{
  private int _unk1;
  private int _unk4;
  private String _unk5;
  private int _territoryId;
  private int _levelType;

  protected void readImpl()
  {
    _unk1 = readD();
    _territoryId = readD();
    _levelType = readD();
    if (_buf.remaining() > 0)
    {
      _unk4 = readD();
      _unk5 = readS();
    }
    else
    {
      _unk4 = 0;
      _unk5 = "none?";
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    player.sendPacket(new PartyMatchList(player, _unk1, _territoryId, _levelType, _unk4, _unk5));
    PartyWaitingRoomManager.getInstance().registerPlayer(player);
    player.setLFP(true);
    player.broadcastUserInfo();
  }
}