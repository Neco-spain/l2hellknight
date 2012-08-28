package l2m.loginserver.gameservercon.gspackets;

import l2m.loginserver.gameservercon.GameServer;
import l2m.loginserver.gameservercon.GameServerConnection;
import l2m.loginserver.gameservercon.ReceivablePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingResponse extends ReceivablePacket
{
  private static final Logger _log = LoggerFactory.getLogger(PingResponse.class);
  private long _serverTime;

  protected void readImpl()
  {
    _serverTime = readQ();
  }

  protected void runImpl()
  {
    GameServer gameServer = getGameServer();
    if (!gameServer.isAuthed()) {
      return;
    }
    gameServer.getConnection().onPingResponse();

    long diff = System.currentTimeMillis() - _serverTime;

    if (Math.abs(diff) > 999L)
      _log.warn("Gameserver " + gameServer.getId() + " [" + gameServer.getName() + "] : time offset " + diff + " ms.");
  }
}