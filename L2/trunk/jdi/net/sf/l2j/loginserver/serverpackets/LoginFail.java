package net.sf.l2j.loginserver.serverpackets;

public final class LoginFail extends L2LoginServerPacket
{
  private LoginFailReason _reason;

  public LoginFail(LoginFailReason reason)
  {
    _reason = reason;
  }

  protected void write()
  {
    writeC(1);
    writeD(_reason.getCode());
  }

  public static enum LoginFailReason
  {
    REASON_SYSTEM_ERROR(1), 
    REASON_PASS_WRONG(2), 
    REASON_USER_OR_PASS_WRONG(3), 
    REASON_ACCESS_FAILED(4), 
    REASON_ACCOUNT_IN_USE(7), 
    REASON_SERVER_OVERLOADED(15), 
    REASON_SERVER_MAINTENANCE(16), 
    REASON_TEMP_PASS_EXPIRED(17), 
    REASON_DUAL_BOX(35);

    private final int _code;

    private LoginFailReason(int code) {
      _code = code;
    }

    public final int getCode()
    {
      return _code;
    }
  }
}