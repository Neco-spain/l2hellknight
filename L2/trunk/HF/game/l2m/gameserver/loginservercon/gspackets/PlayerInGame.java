package l2m.gameserver.loginservercon.gspackets;

import l2m.gameserver.loginservercon.SendablePacket;

public class PlayerInGame extends SendablePacket
{
  private String account;

  public PlayerInGame(String account)
  {
    this.account = account;
  }

  protected void writeImpl()
  {
    writeC(3);
    writeS(account);
  }
}