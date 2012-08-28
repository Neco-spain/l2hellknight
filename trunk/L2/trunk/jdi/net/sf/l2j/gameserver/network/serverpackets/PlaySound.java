package net.sf.l2j.gameserver.network.serverpackets;

public class PlaySound extends L2GameServerPacket
{
  private static final String _S__98_PlaySound = "[S] 98 PlaySound";
  private int _unknown1;
  private String _soundFile;
  private int _unknown3;
  private int _unknown4;
  private int _unknown5;
  private int _unknown6;
  private int _unknown7;

  public PlaySound(String soundFile)
  {
    _unknown1 = 0;
    _soundFile = soundFile;
    _unknown3 = 0;
    _unknown4 = 0;
    _unknown5 = 0;
    _unknown6 = 0;
    _unknown7 = 0;
  }

  public PlaySound(int unknown1, String soundFile, int unknown3, int unknown4, int unknown5, int unknown6, int unknown7)
  {
    _unknown1 = unknown1;
    _soundFile = soundFile;
    _unknown3 = unknown3;
    _unknown4 = unknown4;
    _unknown5 = unknown5;
    _unknown6 = unknown6;
    _unknown7 = unknown7;
  }

  protected final void writeImpl()
  {
    writeC(152);
    writeD(_unknown1);
    writeS(_soundFile);
    writeD(_unknown3);
    writeD(_unknown4);
    writeD(_unknown5);
    writeD(_unknown6);
    writeD(_unknown7);
  }

  public String getType()
  {
    return "[S] 98 PlaySound";
  }
}