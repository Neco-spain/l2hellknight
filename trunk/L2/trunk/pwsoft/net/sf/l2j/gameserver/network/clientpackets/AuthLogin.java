package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.util.WebStat;

public final class AuthLogin extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(AuthLogin.class.getName());
  private String _loginName;
  private int _playKey1;
  private int _playKey2;
  private int _loginKey1;
  private int _loginKey2;

  protected void readImpl()
  {
    _loginName = readS().toLowerCase();
    _playKey2 = readD();
    _playKey1 = readD();
    _loginKey1 = readD();
    _loginKey2 = readD();
  }

  protected void runImpl()
  {
    LoginServerThread.SessionKey key = new LoginServerThread.SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);

    L2GameClient client = (L2GameClient)getClient();

    if (client.getAccountName() == null)
    {
      client.setAccountName(_loginName);
      LoginServerThread.getInstance().addGameServerLogin(_loginName, client);
      LoginServerThread.getInstance().addWaitingClientAndSendRequest(_loginName, client, key);
    }

    if (Config.WEBSTAT_ENABLE)
      WebStat.getInstance().addGame(client.getIpAddr());
  }
}