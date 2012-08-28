package l2m.gameserver.loginservercon.gspackets;

import l2m.gameserver.loginservercon.SendablePacket;

public class PlayerLogout extends SendablePacket
{
  private String account;

  public PlayerLogout(String account)
  {
    this.account = account;
  }

  protected void writeImpl()
  {
    writeC(4);
    writeS(account);
  }
}