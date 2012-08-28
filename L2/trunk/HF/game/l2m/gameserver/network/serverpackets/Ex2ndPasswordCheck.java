package l2m.gameserver.network.serverpackets;

public class Ex2ndPasswordCheck extends L2GameServerPacket
{
  public static final int PASSWORD_NEW = 0;
  public static final int PASSWORD_PROMPT = 1;
  public static final int PASSWORD_OK = 2;
  int _windowType;

  public Ex2ndPasswordCheck(int windowType)
  {
    _windowType = windowType;
  }

  protected void writeImpl()
  {
    writeEx(229);
    writeD(_windowType);
    writeD(0);
  }
}