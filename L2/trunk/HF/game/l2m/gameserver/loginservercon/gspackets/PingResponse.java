package l2m.gameserver.loginservercon.gspackets;

import l2m.gameserver.loginservercon.SendablePacket;

public class PingResponse extends SendablePacket
{
  protected void writeImpl()
  {
    writeC(255);
    writeQ(System.currentTimeMillis());
  }
}