package net.sf.l2j.gameserver.network.serverpackets;

public class ExDuelReady extends L2GameServerPacket
{
  private int _unk1;

  public ExDuelReady(int unk1)
  {
    _unk1 = unk1;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(76);

    writeD(_unk1);
  }
}