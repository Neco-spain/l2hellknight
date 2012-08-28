package net.sf.l2j.gameserver.network.loginserverpackets;

public class AuthResponse extends LoginServerBasePacket
{
  private int _serverId;
  private String _serverName;

  public AuthResponse(byte[] decrypt)
  {
    super(decrypt);
    int salt = readC();
    salt = readC();
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