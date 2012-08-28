package l2m.loginserver.clientpackets;

import java.io.PrintStream;
import javax.crypto.Cipher;
import l2m.commons.util.TypeSystem;
import l2m.loginserver.Config;
import l2m.loginserver.GameServerManager;
import l2m.loginserver.IpBanManager;
import l2m.loginserver.L2LoginClient;
import l2m.loginserver.L2LoginClient.LoginClientState;
import l2m.loginserver.accounts.Account;
import l2m.loginserver.accounts.SessionManager;
import l2m.loginserver.accounts.SessionManager.Session;
import l2m.loginserver.crypt.PasswordHash;
import l2m.loginserver.gameservercon.GameServer;
import l2m.loginserver.gameservercon.lspackets.GetAccountInfo;
import l2m.loginserver.serverpackets.LoginFail.LoginFailReason;
import l2m.loginserver.serverpackets.LoginOk;
import l2m.loginserver.utils.Log;

public class RequestAuthLogin extends L2LoginClientPacket
{
  private byte[] _raw = new byte['\u0080'];

  protected void readImpl()
  {
    readB(_raw);
    readD();
    readD();
    readD();
    readD();
    readD();
    readD();
    readH();
    readC();
  }

  protected void runImpl() throws Exception
  {
    L2LoginClient client = (L2LoginClient)getClient();
    byte[] decrypted;
    try
    {
      Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
      rsaCipher.init(2, client.getRSAPrivateKey());
      decrypted = rsaCipher.doFinal(_raw, 0, 128);
    }
    catch (Exception e)
    {
      client.closeNow(true);
      return;
    }

    String user = new String(decrypted, 94, 14).trim();
    user = user.toLowerCase();
    String password = new String(decrypted, 108, 16).trim();
    int ncotp = decrypted[124];
    ncotp |= decrypted[125] << 8;
    ncotp |= decrypted[126] << 16;
    ncotp |= decrypted[127] << 24;

    int currentTime = (int)(System.currentTimeMillis() / 1000L);

    Account account = new Account(user);
    account.restore();

    String passwordHash = Config.DEFAULT_CRYPT.encrypt(password);

    if (account.getPasswordHash() == null) {
      if ((Config.AUTO_CREATE_ACCOUNTS) && (user.matches(Config.ANAME_TEMPLATE)) && (password.matches(Config.APASSWD_TEMPLATE)))
      {
        account.setAllowedIP("");
        account.setPasswordHash(passwordHash);
        account.save();
      }
      else
      {
        client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
        return;
      }
    }
    boolean passwordCorrect = account.getPasswordHash().equals(passwordHash);

    if (!passwordCorrect)
    {
      for (PasswordHash c : Config.LEGACY_CRYPT) {
        if (!c.compare(password, account.getPasswordHash()))
          continue;
        passwordCorrect = true;
        account.setPasswordHash(passwordHash);
        break;
      }
    }

    if (!IpBanManager.getInstance().tryLogin(client.getIpAddress(), passwordCorrect))
    {
      client.closeNow(false);
      return;
    }

    if (!passwordCorrect)
    {
      client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
      return;
    }

    if (account.getAccessLevel() < 0)
    {
      client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
      return;
    }

    if (account.getBanExpire() > currentTime)
    {
      client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
      return;
    }

    if (!account.isAllowedIP(client.getIpAddress()))
    {
      client.close(LoginFail.LoginFailReason.REASON_ATTEMPTED_RESTRICTED_IP);
      return;
    }

    for (GameServer gs : GameServerManager.getInstance().getGameServers()) {
      if ((gs.getProtocol() >= 2) && (gs.isAuthed()))
        gs.sendPacket(new GetAccountInfo(user));
    }
    account.setLastAccess(currentTime);
    account.setLastIP(client.getIpAddress());
    protection(Config.ENABLE_DDOS_PROTECTION, client.getIpAddress());

    Log.LogAccount(account);

    SessionManager.Session session = SessionManager.getInstance().openSession(account);

    client.setAuthed(true);
    client.setLogin(user);
    client.setAccount(account);
    client.setSessionKey(session.getSessionKey());
    client.setState(L2LoginClient.LoginClientState.AUTHED);

    client.sendPacket(new LoginOk(client.getSessionKey()));
  }

  private void protection(boolean status, String host)
  {
    if (!status) {
      return;
    }
    String command = Config.RULE_DDOS_PROTECTION.replace("$IP", host);
    try
    {
      if (TypeSystem.isUnix())
        Runtime.getRuntime().exec(command);
    }
    catch (Exception e)
    {
      System.out.println("RequestAuthLogin: " + command);
    }
  }
}