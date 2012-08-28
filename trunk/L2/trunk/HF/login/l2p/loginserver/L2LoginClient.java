package l2m.loginserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import l2m.commons.net.nio.impl.MMOClient;
import l2m.commons.net.nio.impl.MMOConnection;
import l2m.loginserver.accounts.Account;
import l2m.loginserver.crypt.LoginCrypt;
import l2m.loginserver.crypt.ScrambledKeyPair;
import l2m.loginserver.serverpackets.AccountKicked;
import l2m.loginserver.serverpackets.AccountKicked.AccountKickedReason;
import l2m.loginserver.serverpackets.L2LoginServerPacket;
import l2m.loginserver.serverpackets.LoginFail;
import l2m.loginserver.serverpackets.LoginFail.LoginFailReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class L2LoginClient extends MMOClient<MMOConnection<L2LoginClient>>
{
  private static final Logger _log = LoggerFactory.getLogger(L2LoginClient.class);
  private LoginClientState _state;
  private LoginCrypt _loginCrypt;
  private ScrambledKeyPair _scrambledPair;
  private byte[] _blowfishKey;
  private String _login;
  private SessionKey _skey;
  private Account _account;
  private String _ipAddr;
  private int _sessionId;

  public L2LoginClient(MMOConnection<L2LoginClient> con)
  {
    super(con);
    _state = LoginClientState.CONNECTED;
    _scrambledPair = Config.getScrambledRSAKeyPair();
    _blowfishKey = Config.getBlowfishKey();
    _loginCrypt = new LoginCrypt();
    _loginCrypt.setKey(_blowfishKey);
    _sessionId = con.hashCode();
    _ipAddr = getConnection().getSocket().getInetAddress().getHostAddress();
  }

  public boolean decrypt(ByteBuffer buf, int size)
  {
    boolean ret;
    try
    {
      ret = _loginCrypt.decrypt(buf.array(), buf.position(), size);
    }
    catch (IOException e)
    {
      _log.error("", e);
      closeNow(true);
      return false;
    }

    if (!ret) {
      closeNow(true);
    }
    return ret;
  }

  public boolean encrypt(ByteBuffer buf, int size)
  {
    int offset = buf.position();
    try
    {
      size = _loginCrypt.encrypt(buf.array(), offset, size);
    }
    catch (IOException e)
    {
      _log.error("", e);
      return false;
    }

    buf.position(offset + size);
    return true;
  }

  public LoginClientState getState()
  {
    return _state;
  }

  public void setState(LoginClientState state)
  {
    _state = state;
  }

  public byte[] getBlowfishKey()
  {
    return _blowfishKey;
  }

  public byte[] getScrambledModulus()
  {
    return _scrambledPair.getScrambledModulus();
  }

  public RSAPrivateKey getRSAPrivateKey()
  {
    return (RSAPrivateKey)_scrambledPair.getKeyPair().getPrivate();
  }

  public String getLogin()
  {
    return _login;
  }

  public void setLogin(String login)
  {
    _login = login;
  }

  public Account getAccount()
  {
    return _account;
  }

  public void setAccount(Account account)
  {
    _account = account;
  }

  public SessionKey getSessionKey()
  {
    return _skey;
  }

  public void setSessionKey(SessionKey skey)
  {
    _skey = skey;
  }

  public void setSessionId(int val)
  {
    _sessionId = val;
  }

  public int getSessionId()
  {
    return _sessionId;
  }

  public void sendPacket(L2LoginServerPacket lsp)
  {
    if (isConnected())
      getConnection().sendPacket(lsp);
  }

  public void close(LoginFail.LoginFailReason reason)
  {
    if (isConnected())
      getConnection().close(new LoginFail(reason));
  }

  public void close(AccountKicked.AccountKickedReason reason)
  {
    if (isConnected())
      getConnection().close(new AccountKicked(reason));
  }

  public void close(L2LoginServerPacket lsp)
  {
    if (isConnected())
      getConnection().close(lsp);
  }

  public void onDisconnection()
  {
    _state = LoginClientState.DISCONNECTED;
    _skey = null;
    _loginCrypt = null;
    _scrambledPair = null;
    _blowfishKey = null;
  }

  public String toString()
  {
    switch (1.$SwitchMap$l2m$loginserver$L2LoginClient$LoginClientState[_state.ordinal()])
    {
    case 1:
      return "[ Account : " + getLogin() + " IP: " + getIpAddress() + "]";
    }
    return "[ State : " + getState() + " IP: " + getIpAddress() + "]";
  }

  public String getIpAddress()
  {
    return _ipAddr;
  }

  protected void onForcedDisconnection()
  {
  }

  public static enum LoginClientState
  {
    CONNECTED, 
    AUTHED_GG, 
    AUTHED, 
    DISCONNECTED;
  }
}