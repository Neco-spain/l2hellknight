package net.sf.l2j.loginserver.serverpackets;

public final class AccountKicked extends L2LoginServerPacket
{
  private AccountKickedReason _reason;

  public AccountKicked(AccountKickedReason reason)
  {
    _reason = reason;
  }

  protected void write()
  {
    writeC(2);
    writeD(_reason.getCode());
  }

  public static enum AccountKickedReason
  {
    REASON_DATA_STEALER(1), 
    REASON_GENERIC_VIOLATION(8), 
    REASON_7_DAYS_SUSPENDED(16), 
    REASON_PERMANENTLY_BANNED(32);

    private final int _code;

    private AccountKickedReason(int code) {
      _code = code;
    }

    public final int getCode()
    {
      return _code;
    }
  }
}