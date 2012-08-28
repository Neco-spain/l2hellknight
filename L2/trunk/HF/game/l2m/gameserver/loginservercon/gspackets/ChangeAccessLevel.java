package l2m.gameserver.loginservercon.gspackets;

import l2m.gameserver.loginservercon.SendablePacket;

public class ChangeAccessLevel extends SendablePacket
{
  private String account;
  private int level;
  private int banExpire;

  public ChangeAccessLevel(String account, int level, int banExpire)
  {
    this.account = account;
    this.level = level;
    this.banExpire = banExpire;
  }

  protected void writeImpl()
  {
    writeC(17);
    writeS(account);
    writeD(level);
    writeD(banExpire);
  }
}