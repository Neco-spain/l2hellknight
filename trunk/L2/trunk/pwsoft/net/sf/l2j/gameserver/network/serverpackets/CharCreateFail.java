package net.sf.l2j.gameserver.network.serverpackets;

public class CharCreateFail extends L2GameServerPacket
{
  public static final int REASON_CREATION_FAILED = 0;
  public static final int REASON_TOO_MANY_CHARACTERS = 1;
  public static final int REASON_NAME_ALREADY_EXISTS = 2;
  public static final int REASON_16_ENG_CHARS = 3;
  private int _error;

  public CharCreateFail(int errorCode)
  {
    _error = errorCode;
  }

  protected final void writeImpl()
  {
    writeC(26);
    writeD(_error);
  }
}