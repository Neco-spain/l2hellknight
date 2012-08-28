package l2p.gameserver.loginservercon.gspackets;

import l2p.gameserver.loginservercon.SendablePacket;

public class ReduceAccountPoints extends SendablePacket
{
  private String account;
  private int count;

  public ReduceAccountPoints(String account, int count)
  {
    this.account = account;
    this.count = count;
  }

  protected void writeImpl()
  {
    writeC(18);
    writeS(account);
    writeD(count);
  }
}