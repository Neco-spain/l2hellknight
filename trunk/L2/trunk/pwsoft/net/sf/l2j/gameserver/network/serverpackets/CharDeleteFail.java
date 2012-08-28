package net.sf.l2j.gameserver.network.serverpackets;

public class CharDeleteFail extends L2GameServerPacket
{
  public static final int REASON_DELETION_FAILED = 1;
  public static final int REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER = 2;
  public static final int REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED = 3;
  private int _error;

  public CharDeleteFail(int errorCode)
  {
    _error = errorCode;
  }

  protected final void writeImpl()
  {
    writeC(36);
    writeD(_error);
  }
}