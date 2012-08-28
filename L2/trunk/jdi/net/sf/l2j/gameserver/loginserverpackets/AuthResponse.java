package net.sf.l2j.gameserver.loginserverpackets;

public class AuthResponse extends LoginServerBasePacket
{
  private int _serverId;
  private String _serverName;

  public AuthResponse(byte[] decrypt)
  {
    super(decrypt);
    _serverId = readC();
    _serverName = readS();
  }

  public int getServerId()
  {
    return _serverId;
  }

  public String getServerName()
  {
    return _serverName;
  }
}