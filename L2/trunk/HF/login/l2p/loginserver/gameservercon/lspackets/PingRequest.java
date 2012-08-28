package l2m.loginserver.gameservercon.lspackets;

import l2m.loginserver.gameservercon.SendablePacket;

public class PingRequest extends SendablePacket
{
  protected void writeImpl()
  {
    writeC(255);
  }
}