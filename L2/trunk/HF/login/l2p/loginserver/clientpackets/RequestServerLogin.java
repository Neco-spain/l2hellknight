package l2m.loginserver.clientpackets;

import l2m.loginserver.GameServerManager;
import l2m.loginserver.L2LoginClient;
import l2m.loginserver.SessionKey;
import l2m.loginserver.accounts.Account;
import l2m.loginserver.gameservercon.GameServer;
import l2m.loginserver.serverpackets.LoginFail.LoginFailReason;
import l2m.loginserver.serverpackets.PlayOk;

public class RequestServerLogin extends L2LoginClientPacket
{
  private int _loginOkID1;
  private int _loginOkID2;
  private int _serverId;

  protected void readImpl()
  {
    _loginOkID1 = readD();
    _loginOkID2 = readD();
    _serverId = readC();
  }

  protected void runImpl()
  {
    L2LoginClient client = (L2LoginClient)getClient();
    SessionKey skey = client.getSessionKey();
    if ((skey == null) || (!skey.checkLoginPair(_loginOkID1, _loginOkID2)))
    {
      client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
      return;
    }

    Account account = client.getAccount();
    GameServer gs = GameServerManager.getInstance().getGameServerById(_serverId);
    if ((gs == null) || (!gs.isAuthed()) || ((gs.isGmOnly()) && (account.getAccessLevel() < 100)) || ((gs.getOnline() >= gs.getMaxPlayers()) && (account.getAccessLevel() < 50)))
    {
      client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
      return;
    }

    account.setLastServer(_serverId);
    account.update();

    client.close(new PlayOk(skey));
  }
}