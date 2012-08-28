package net.sf.l2j.loginserver.clientpackets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import net.sf.l2j.Config;
import net.sf.l2j.loginserver.GameServerTable.GameServerInfo;
import net.sf.l2j.loginserver.GameServerThread;
import net.sf.l2j.loginserver.HackingException;
import net.sf.l2j.loginserver.L2LoginClient;
import net.sf.l2j.loginserver.L2LoginClient.LoginClientState;
import net.sf.l2j.loginserver.LoginController;
import net.sf.l2j.loginserver.LoginController.AuthLoginResult;
import net.sf.l2j.loginserver.serverpackets.AccountKicked;
import net.sf.l2j.loginserver.serverpackets.AccountKicked.AccountKickedReason;
import net.sf.l2j.loginserver.serverpackets.LoginFail.LoginFailReason;
import net.sf.l2j.loginserver.serverpackets.LoginOk;
import net.sf.l2j.loginserver.serverpackets.ServerList;
import org.mmocore.network.ISocket;
import org.mmocore.network.MMOConnection;

public class RequestAuthLogin extends L2LoginClientPacket
{
  private static Logger _log = Logger.getLogger(RequestAuthLogin.class.getName());

  private byte[] _raw = new byte['\u0080'];
  private String _user;
  private String _password;
  private int _ncotp;

  public String getPassword()
  {
    return _password;
  }

  public String getUser()
  {
    return _user;
  }

  public int getOneTimePassword()
  {
    return _ncotp;
  }

  public boolean readImpl()
  {
    if (getAvaliableBytes() >= 128)
    {
      readB(_raw);
      return true;
    }

    return false;
  }

  public void run()
  {
    byte[] decrypted = null;
    try
    {
      Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
      rsaCipher.init(2, ((L2LoginClient)getClient()).getRSAPrivateKey());
      decrypted = rsaCipher.doFinal(_raw, 0, 128);
    }
    catch (GeneralSecurityException e)
    {
      e.printStackTrace();
      return;
    }

    _user = new String(decrypted, 94, 14).trim();
    _user = _user.toLowerCase();
    _password = new String(decrypted, 108, 16).trim();
    _ncotp = decrypted[124];
    _ncotp |= decrypted[125] << 8;
    _ncotp |= decrypted[126] << 16;
    _ncotp |= decrypted[127] << 24;

    LoginController lc = LoginController.getInstance();
    L2LoginClient client = (L2LoginClient)getClient();
    InetAddress address = ((L2LoginClient)getClient()).getConnection().getSocket().getInetAddress();
    String addhost = address.getHostAddress();
    try
    {
      LoginController.AuthLoginResult result = lc.tryAuthLogin(_user, _password, (L2LoginClient)getClient());

      switch (1.$SwitchMap$net$sf$l2j$loginserver$LoginController$AuthLoginResult[result.ordinal()])
      {
      case 1:
        client.setAccount(_user);
        client.setState(L2LoginClient.LoginClientState.AUTHED_LOGIN);
        client.setSessionKey(lc.assignSessionKeyToClient(_user, client));
        if (Config.SHOW_LICENCE)
        {
          client.sendPacket(new LoginOk(((L2LoginClient)getClient()).getSessionKey()));
        }
        else
        {
          ((L2LoginClient)getClient()).sendPacket(new ServerList((L2LoginClient)getClient()));
        }
        if (!Config.ENABLE_DDOS_PROTECTION_SYSTEM)
        {
          break;
        }

        try
        {
          Runtime rt = Runtime.getRuntime();
          String[] cmd = { "/bin/sh", "-c", "/sbin/iptables -L -n | grep 7777 | grep " + addhost };
          Process proc = rt.exec(cmd);
          BufferedReader is = new BufferedReader(new InputStreamReader(proc.getInputStream()));

          boolean check = false;
          String line;
          while ((line = is.readLine()) != null)
          {
            check = true;
          }

          is.close();
          if (!check)
          {
            String addipaccess = "/sbin/iptables -I INPUT -s " + addhost + " -p tcp -m tcp --dport 7777 -j ACCEPT";
            Runtime.getRuntime().exec(addipaccess);
          }

          if (Config.ENABLE_DEBUG_DDOS_PROTECTION_SYSTEM)
            _log.info("Accepted access ip: " + addhost);
        }
        catch (Exception e)
        {
          _log.info("Accept by ip " + addhost + " not allowed");
        }

      case 2:
        client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
        break;
      case 3:
        client.close(new AccountKicked(AccountKicked.AccountKickedReason.REASON_PERMANENTLY_BANNED));
        break;
      case 4:
        L2LoginClient oldClient;
        if ((oldClient = lc.getAuthedClient(_user)) == null) {
          break;
        }
        oldClient.close(LoginFail.LoginFailReason.REASON_ACCOUNT_IN_USE);
        lc.removeAuthedLoginClient(_user); break;
      case 5:
        GameServerTable.GameServerInfo gsi;
        if ((gsi = lc.getAccountOnGameServer(_user)) == null) {
          break;
        }
        client.close(LoginFail.LoginFailReason.REASON_ACCOUNT_IN_USE);

        if (!gsi.isAuthed())
          break;
        gsi.getGameServerThread().kickPlayer(_user);
      }

    }
    catch (HackingException e)
    {
      lc.addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
      _log.info("Banned (" + address + ") for " + Config.LOGIN_BLOCK_AFTER_BAN + " seconds, due to " + e.getConnects() + " incorrect login attempts.");
    }
  }
}