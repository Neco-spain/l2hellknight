package l2m.loginserver.gameservercon.gspackets;

import l2m.loginserver.gameservercon.GameServer;
import l2m.loginserver.gameservercon.ReceivablePacket;

public class OnlineStatus extends ReceivablePacket
{
  private boolean _online;

  protected void readImpl()
  {
    _online = (readC() == 1);
  }

  protected void runImpl()
  {
    GameServer gameServer = getGameServer();
    if (!gameServer.isAuthed()) {
      return;
    }
    gameServer.setOnline(_online);
  }
}