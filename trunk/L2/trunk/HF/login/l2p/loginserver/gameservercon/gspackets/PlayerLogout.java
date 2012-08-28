package l2m.loginserver.gameservercon.gspackets;

import l2m.loginserver.gameservercon.GameServer;
import l2m.loginserver.gameservercon.ReceivablePacket;

public class PlayerLogout extends ReceivablePacket
{
  private String account;

  protected void readImpl()
  {
    account = readS();
  }

  protected void runImpl()
  {
    GameServer gs = getGameServer();
    if (gs.isAuthed())
      gs.removeAccount(account);
  }
}