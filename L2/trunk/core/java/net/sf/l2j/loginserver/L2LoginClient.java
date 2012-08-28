package net.sf.l2j.loginserver;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.crypt.LoginCrypt;
import net.sf.l2j.loginserver.crypt.ScrambledKeyPair;
import net.sf.l2j.loginserver.serverpackets.AccountKicked;
import net.sf.l2j.loginserver.serverpackets.L2LoginServerPacket;
import net.sf.l2j.loginserver.serverpackets.LoginFail;
import net.sf.l2j.loginserver.serverpackets.PlayFail;
import net.sf.l2j.loginserver.serverpackets.LoginFail.LoginFailReason;
import net.sf.l2j.loginserver.serverpackets.PlayFail.PlayFailReason;
import net.sf.l2j.util.Rnd;

import org.mmocore.network.MMOClient;
import org.mmocore.network.MMOConnection;

public final class L2LoginClient extends MMOClient<MMOConnection<L2LoginClient>>
{
	private static Logger _log = Logger.getLogger(L2LoginClient.class.getName());

	public static enum LoginClientState { CONNECTED, AUTHED_GG, AUTHED_LOGIN};

	private LoginClientState _state;

	private LoginCrypt _loginCrypt;
	private ScrambledKeyPair _scrambledPair;
	private byte[] _blowfishKey;

	private String _account;
	private int _accessLevel;
	private int _lastServer;
	private boolean _usesInternalIP;
	private SessionKey _sessionKey;
	private int _sessionId;
	private boolean _joinedGS;

	private long _connectionStartTime;

	public L2LoginClient(MMOConnection<L2LoginClient> con)
	{
		super(con);
		_state = LoginClientState.CONNECTED;
		String ip = getConnection().getSocket().getInetAddress().getHostAddress();

		if (ip.startsWith("192.168") || ip.startsWith("10.0") || ip.equals("127.0.0.1"))
		{
			_usesInternalIP = true;
		}

		_scrambledPair = LoginController.getInstance().getScrambledRSAKeyPair();
		_blowfishKey = LoginController.getInstance().getBlowfishKey();
		_sessionId = Rnd.nextInt();
		_connectionStartTime = System.currentTimeMillis();
		_loginCrypt = new LoginCrypt();
		_loginCrypt.setKey(_blowfishKey);
		
		if (!BruteProtector.canLogin(ip))
	    {
	      LoginController.getInstance().addBanForAddress(getConnection().getSocket().getInetAddress(), Config.BRUT_BAN_IP_TIME * 1000);
	      close(new AccountKicked(AccountKicked.AccountKickedReason.REASON_PERMANENTLY_BANNED));
	      _log.warning(new StringBuilder().append("Drop connection from IP ").append(ip).append(" because of BruteForce.").toString());
	    }
		
	}

	public boolean usesInternalIP()
	{
		return _usesInternalIP;
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		boolean ret = false;
		try
		{
			ret = _loginCrypt.decrypt(buf.array(), buf.position(), size);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			closeNow();
			return false;
		}

		if (!ret)
		{
			byte[] dump = new byte[size];
			System.arraycopy(buf.array(), buf.position(), dump, 0, size);
			_log.warning("Wrong checksum from client: "+toString());
			closeNow();
		}

		return ret;
	}

	@Override
	public boolean encrypt(ByteBuffer buf, int size)
	{
		final int offset = buf.position();
		try
		{
			size = _loginCrypt.encrypt(buf.array(), offset, size);
		}
		catch (IOException e)
		{
			e.printStackTrace();
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
		return _scrambledPair._scrambledModulus;
	}

	public RSAPrivateKey getRSAPrivateKey()
	{
		return (RSAPrivateKey) _scrambledPair._pair.getPrivate();
	}

	public String getAccount()
	{
		return _account;
	}

	public void setAccount(String account)
	{
		_account = account;
	}

	public void setAccessLevel(int accessLevel)
	{
		_accessLevel = accessLevel;
	}

	public int getAccessLevel()
	{
		return _accessLevel;
	}

	public void setLastServer(int lastServer)
	{
		_lastServer = lastServer;
	}

	public int getLastServer()
	{
		return _lastServer;
	}

	public int getSessionId()
	{
		return _sessionId;
	}

	public boolean hasJoinedGS()
	{
		return _joinedGS;
	}

	public void setJoinedGS(boolean val)
	{
		_joinedGS = val;
	}

	public void setSessionKey(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}

	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}

	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}

	public void sendPacket(L2LoginServerPacket lsp)
	{
		getConnection().sendPacket(lsp);
	}

	public void close(LoginFailReason reason)
	{
		getConnection().close(new LoginFail(reason));
	}

	public void close(PlayFailReason reason)
	{
		getConnection().close(new PlayFail(reason));
	}

	public void close(L2LoginServerPacket lsp)
	{
		getConnection().close(lsp);
	}

	public void onDisconnection()
	{
		if (Config.DEBUG)
		{
			_log.info("DISCONNECTED: "+toString());
		}

		if (getState() != LoginClientState.AUTHED_LOGIN)
		{
			LoginController.getInstance().removeLoginClient(this);
		}
		else if (!hasJoinedGS())
		{
			LoginController.getInstance().removeAuthedLoginClient(getAccount());
		}
	}

	@Override
	public String toString()
	{
		InetAddress address = getConnection().getSocket().getInetAddress();
		if (getState() == LoginClientState.AUTHED_LOGIN)
		{
			return "["+getAccount()+" ("+(address == null ? "disconnected" : address.getHostAddress())+")]";
		}
		else
		{
			return "["+(address == null ? "disconnected" : address.getHostAddress())+"]";
		}
	}
}
