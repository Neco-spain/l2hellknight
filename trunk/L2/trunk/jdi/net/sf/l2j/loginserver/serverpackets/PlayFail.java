package net.sf.l2j.loginserver.serverpackets;

public final class PlayFail extends L2LoginServerPacket
{
  private final PlayFailReason _reason;

  public PlayFail(PlayFailReason reason)
  {
    _reason = reason;
  }

  protected void write()
  {
    writeC(6);
    writeC(_reason.getCode());
  }

  public static enum PlayFailReason
  {
    REASON_SYSTEM_ERROR(1), 
    REASON_USER_OR_PASS_WRONG(2), 
    REASON3(3), 
    REASON4(4), 
    REASON_TOO_MANY_PLAYERS(15);

    private final int _code;

    private PlayFailReason(int code) {
      _code = code;
    }

    public final int getCode()
    {
      return _code;
    }
  }
}