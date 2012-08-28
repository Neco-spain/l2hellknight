package net.sf.l2j.gameserver.network.loginserverpackets;

public class PlayerAuthResponse extends LoginServerBasePacket
{
  private String _account;
  private boolean _authed;
  private String _hwid;
  private boolean _email;

  public PlayerAuthResponse(byte[] decrypt)
  {
    super(decrypt);

    _account = readS();
    _authed = (readC() != 0);
    _hwid = readS();
    _email = (readC() != 0);
  }

  public String getAccount()
  {
    return _account;
  }

  public boolean isAuthed()
  {
    return _authed;
  }

  public String getHWID() {
    return _hwid;
  }

  public boolean hasEmail() {
    return _email;
  }
}