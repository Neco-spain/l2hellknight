package l2m.gameserver.loginservercon.gspackets;

import l2m.gameserver.loginservercon.SendablePacket;

public class RemoveAccountPoint extends SendablePacket
{
  private String _account;
  private int _point;

  public RemoveAccountPoint(String account, int point)
  {
    _account = account;
    _point = point;
  }

  protected void writeImpl()
  {
    writeC(13);
    writeS(_account);
    writeD(_point);
  }
}