package l2m.loginserver.serverpackets;

public final class AccountKicked extends L2LoginServerPacket
{
  private int reason;

  public AccountKicked(AccountKickedReason reason)
  {
    this.reason = reason.getCode();
  }

  protected void writeImpl()
  {
    writeC(2);
    writeD(reason);
  }

  public static enum AccountKickedReason
  {
    REASON_FALSE_DATA_STEALER_REPORT(0), 
    REASON_DATA_STEALER(1), 
    REASON_SOUSPICION_DATA_STEALER(3), 
    REASON_NON_PAYEMENT_CELL_PHONE(4), 
    REASON_30_DAYS_SUSPENDED_CASH(8), 
    REASON_PERMANENTLY_SUSPENDED_CASH(16), 
    REASON_PERMANENTLY_BANNED(32), 
    REASON_ACCOUNT_MUST_BE_VERIFIED(64);

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