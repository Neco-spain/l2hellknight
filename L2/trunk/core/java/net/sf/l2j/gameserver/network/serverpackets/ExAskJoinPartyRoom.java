package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExAskJoinPartyRoom extends L2GameServerPacket
{
  private static final String _S__FE_34_EXASKJOINPARTYROOM = "[S] FE:34 ExAskJoinPartyRoom";
  @SuppressWarnings("unused")
private String _charName;
  L2PcInstance _player;
  private int _party_status;

  public ExAskJoinPartyRoom(L2PcInstance player, int kol_vo, int party_member, int party_status)
  {
    _player = player;
    _party_status = party_status;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(14);
    writeD(1);
    writeD(1);
    writeD(0);
    writeS(_player.getName());
    writeD(_player.getClassIndex());
    writeD(_player.getLevel());
    //writeD(_player.getTownZone(_player));
    writeD(_party_status);
  }

  public String getType()
  {
    return _S__FE_34_EXASKJOINPARTYROOM;
  }
}