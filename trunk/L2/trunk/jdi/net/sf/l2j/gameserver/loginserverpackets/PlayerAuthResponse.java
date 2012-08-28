package net.sf.l2j.gameserver.loginserverpackets;

public class PlayerAuthResponse extends LoginServerBasePacket
{
  private String _account;
  private boolean _authed;

  public PlayerAuthResponse(byte[] decrypt)
  {
    super(decrypt);

    _account = readS();
    _authed = (readC() != 0);
  }

  public String getAccount()
  {
    return _account;
  }

  public boolean isAuthed()
  {
    return _authed;
  }
}