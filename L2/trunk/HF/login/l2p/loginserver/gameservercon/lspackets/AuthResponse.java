package l2m.loginserver.gameservercon.lspackets;

import l2m.loginserver.gameservercon.GameServer;
import l2m.loginserver.gameservercon.SendablePacket;

public class AuthResponse extends SendablePacket
{
  private int serverId;
  private String name;

  public AuthResponse(GameServer gs)
  {
    serverId = gs.getId();
    name = gs.getName();
  }

  protected void writeImpl()
  {
    writeC(0);
    writeC(serverId);
    writeS(name);
  }
}