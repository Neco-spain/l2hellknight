package net.sf.l2j.gameserver.network.serverpackets;

public class ExUseSharedGroupItem extends L2GameServerPacket
{
  private int _unk1;
  private int _unk2;
  private int _unk3;
  private int _unk4;

  public ExUseSharedGroupItem(int unk1, int unk2, int unk3, int unk4)
  {
    _unk1 = unk1;
    _unk2 = unk2;
    _unk3 = unk3;
    _unk4 = unk4;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(73);

    writeD(_unk1);
    writeD(_unk2);
    writeD(_unk3);
    writeD(_unk4);
  }
}