package net.sf.l2j.gameserver.network.serverpackets;

public class AuthLoginFail extends L2GameServerPacket
{
  public static final int NO_TEXT = 0;
  public static final int SYSTEM_ERROR_LOGIN_LATER = 1;
  public static final int PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT = 2;
  public static final int PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT2 = 3;
  public static final int ACCESS_FAILED_TRY_LATER = 4;
  public static final int INCORRECT_ACCOUNT_INFO_CONTACT_CUSTOMER_SUPPORT = 5;
  public static final int ACCESS_FAILED_TRY_LATER2 = 6;
  public static final int ACOUNT_ALREADY_IN_USE = 7;
  public static final int ACCESS_FAILED_TRY_LATER3 = 8;
  public static final int ACCESS_FAILED_TRY_LATER4 = 9;
  public static final int ACCESS_FAILED_TRY_LATER5 = 10;
  private int _reason;

  public AuthLoginFail(int reason)
  {
    _reason = reason;
  }

  protected final void writeImpl()
  {
    writeC(20);
    writeD(_reason);
  }
}