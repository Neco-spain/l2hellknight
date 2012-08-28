package l2p.gameserver.loginservercon.gspackets;

import l2p.gameserver.loginservercon.SendablePacket;

public class PingResponse extends SendablePacket
{
  protected void writeImpl()
  {
    writeC(255);
    writeQ(System.currentTimeMillis());
  }
}