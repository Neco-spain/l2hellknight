package l2m.loginserver.gameservercon.lspackets;

import l2m.loginserver.gameservercon.SendablePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetAccountPoint extends SendablePacket
{
  public static final Logger _log = LoggerFactory.getLogger(SetAccountPoint.class);
  private String _account;
  private int _accpoints;

  public SetAccountPoint(String account, int points)
  {
    _account = account;
    _accpoints = points;
  }

  protected void writeImpl()
  {
    writeC(12);
    writeS(_account);
    writeD(_accpoints);
  }
}