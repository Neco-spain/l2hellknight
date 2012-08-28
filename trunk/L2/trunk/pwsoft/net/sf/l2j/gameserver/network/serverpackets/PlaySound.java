package net.sf.l2j.gameserver.network.serverpackets;

public class PlaySound extends L2GameServerPacket
{
  private int _unk1;
  private String _soundFile;
  private int _unk3;
  private int _unk4;
  private int _unk5;
  private int _unk6;
  private int _unk7;

  public PlaySound(String soundFile)
  {
    _unk1 = 0;
    _soundFile = soundFile;
    _unk3 = 0;
    _unk4 = 0;
    _unk5 = 0;
    _unk6 = 0;
    _unk7 = 0;
  }

  public PlaySound(int unk1, String soundFile, int unk3, int unk4, int unk5, int unk6, int unk7)
  {
    _unk1 = unk1;
    _soundFile = soundFile;
    _unk3 = unk3;
    _unk4 = unk4;
    _unk5 = unk5;
    _unk6 = unk6;
    _unk7 = unk7;
  }

  protected final void writeImpl()
  {
    writeC(152);
    writeD(_unk1);
    writeS(_soundFile);
    writeD(_unk3);
    writeD(_unk4);
    writeD(_unk5);
    writeD(_unk6);
    writeD(_unk7);
  }
}