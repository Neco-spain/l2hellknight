package l2m.gameserver.loginservercon.gspackets;

import l2m.gameserver.Config;
import l2m.gameserver.loginservercon.SendablePacket;

public class AuthRequest extends SendablePacket
{
  protected void writeImpl()
  {
    writeC(0);
    writeD(2);
    writeC(Config.REQUEST_ID);
    writeC(Config.ACCEPT_ALTERNATE_ID ? 1 : 0);
    writeD(Config.LOGIN_SERVER_SERVER_TYPE);
    writeD(Config.LOGIN_SERVER_AGE_LIMIT);
    writeC(Config.LOGIN_SERVER_GM_ONLY ? 1 : 0);
    writeC(Config.LOGIN_SERVER_BRACKETS ? 1 : 0);
    writeC(Config.LOGIN_SERVER_IS_PVP ? 1 : 0);
    writeS(Config.EXTERNAL_HOSTNAME);
    writeS(Config.INTERNAL_HOSTNAME);
    writeH(Config.PORTS_GAME.length);
    for (int PORT_GAME : Config.PORTS_GAME)
      writeH(PORT_GAME);
    writeD(Config.MAXIMUM_ONLINE_USERS);
  }
}