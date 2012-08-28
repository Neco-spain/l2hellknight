package l2m.loginserver.gameservercon.lspackets;

import l2m.loginserver.gameservercon.SendablePacket;

public class KickPlayer extends SendablePacket
{
  private String account;

  public KickPlayer(String login)
  {
    account = login;
  }

  protected void writeImpl()
  {
    writeC(3);
    writeS(account);
  }
}