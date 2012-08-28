package l2m.gameserver.loginservercon.gspackets;

import l2m.gameserver.loginservercon.SendablePacket;

public class GetAccountPoint extends SendablePacket
{
  private String _account;

  public GetAccountPoint(String account)
  {
    _account = account;
  }

  protected void writeImpl()
  {
    writeC(12);
    writeS(_account);
  }
}