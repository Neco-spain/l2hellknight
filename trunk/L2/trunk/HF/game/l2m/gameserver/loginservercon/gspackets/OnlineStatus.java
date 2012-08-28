package l2m.gameserver.loginservercon.gspackets;

import l2m.gameserver.loginservercon.SendablePacket;

public class OnlineStatus extends SendablePacket
{
  private boolean _online;

  public OnlineStatus(boolean online)
  {
    _online = online;
  }

  protected void writeImpl()
  {
    writeC(1);
    writeC(_online ? 1 : 0);
  }
}