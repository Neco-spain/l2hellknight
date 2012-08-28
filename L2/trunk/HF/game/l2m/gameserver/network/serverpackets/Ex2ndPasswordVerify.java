package l2m.gameserver.network.serverpackets;

public class Ex2ndPasswordVerify extends L2GameServerPacket
{
  public static final int PASSWORD_OK = 0;
  public static final int PASSWORD_WRONG = 1;
  public static final int PASSWORD_BAN = 2;
  int _wrongTentatives;
  int _mode;

  public Ex2ndPasswordVerify(int mode, int wrongTentatives)
  {
    _mode = mode;
    _wrongTentatives = wrongTentatives;
  }

  protected void writeImpl()
  {
    writeEx(230);
    writeD(_mode);
    writeD(_wrongTentatives);
  }
}