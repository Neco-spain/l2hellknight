package net.sf.l2j.gameserver.network.serverpackets;

public class ExDuelStart extends L2GameServerPacket
{
  private int _unk1;

  public ExDuelStart(int unk1)
  {
    _unk1 = unk1;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(77);

    writeD(_unk1);
  }
}