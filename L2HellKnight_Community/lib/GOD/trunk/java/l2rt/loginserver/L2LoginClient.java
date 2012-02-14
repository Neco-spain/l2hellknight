package l2rt.loginserver;

import l2rt.Config;
import l2rt.extensions.network.MMOClient;
import l2rt.extensions.network.MMOConnection;
import l2rt.gameserver.templates.StatsSet;
import l2rt.loginserver.crypt.LoginCrypt;
import l2rt.loginserver.crypt.ScrambledKeyPair;
import l2rt.loginserver.serverpackets.AccountKicked;
import l2rt.loginserver.serverpackets.AccountKicked.AccountKickedReason;
import l2rt.loginserver.serverpackets.L2LoginServerPacket;
import l2rt.loginserver.serverpackets.LoginFail;
import l2rt.loginserver.serverpackets.LoginFail.LoginFailReason;
import l2rt.util.HWID.HardwareID;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;
import java.sql.SQLException;
import java.util.logging.Logger;

public final class L2LoginClient extends MMOClient<MMOConnection<L2LoginClient>>
{
	private static Logger _log = Logger.getLogger(L2LoginClient.class.getName());
    private int point;
	private int _points;

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public static enum LoginClientState
	{
		CONNECTED,
		AUTHED_GG,
		AUTHED_LOGIN,
		FAKE_LOGIN
	}

	private LoginClientState _state;
	public boolean PlayOK = false;

	// Crypt
	private LoginCrypt _loginCrypt;
	private ScrambledKeyPair _scrambledPair;
	private byte[] _blowfishKey;

	private String _account;
	private int _accessLevel;
	private int _lastServer;
	private SessionKey _sessionKey;
	private int _sessionId;
	private float _bonus = 1;
	private int _bonusExpire = 0;
	//private boolean _proxy = false;

	private long _connectionStartTime;
	private boolean protect_used = false;
	private HardwareID HWID = null;

	public StatsSet account_fields = null;

	public L2LoginClient(MMOConnection<L2LoginClient> con)
	{
		super(con);
		_state = LoginClientState.CONNECTED;
		String ip = getIpAddress();
		protect_used = Config.PROTECT_ENABLE;
		if(protect_used)
			protect_used = !Config.PROTECT_UNPROTECTED_IPS.isIpInNets(ip);
		_scrambledPair = LoginController.getInstance().getScrambledRSAKeyPair();
		_blowfishKey = LoginController.getInstance().getBlowfishKey();
		_connectionStartTime = System.currentTimeMillis();
		_loginCrypt = new LoginCrypt();
		_loginCrypt.setKey(_blowfishKey, protect_used);
		_sessionId = con.hashCode();
		if(IpManager.getInstance().CheckIp(ip))
		{
			close(new AccountKicked(AccountKickedReason.REASON_PERMANENTLY_BANNED));
			_log.warning("Drop connection from banned IP: " + ip);
		}
	}

	public boolean isProtectUsed()
	{
		return protect_used;
	}

	public HardwareID getHWID()
	{
		return HWID;
	}

	public void setHWID(String val)
	{
		HWID = new HardwareID(val);
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		boolean ret;
		try
		{
			ret = _loginCrypt.decrypt(buf.array(), buf.position(), size);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			closeNow(true);
			return false;
		}

		if(!ret)
		{
			_log.warning("Wrong checksum from client: " + toString());
			closeNow(true);
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
		catch(IOException e)
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
		if(_scrambledPair == null || _scrambledPair._scrambledModulus == null)
		{
			closeNow(true);
			return null;
		}

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

	public void setSessionId(int val)
	{
		_sessionId = val;
	}

	public int getSessionId()
	{
		return _sessionId;
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

	public void close(L2LoginServerPacket lsp)
	{
		getConnection().close(lsp);
	}

	@Override
	public void onDisconnection()
	{
		if(Config.LOGIN_DEBUG)
			_log.info("DISCONNECTED: " + toString());

		LoginController lc = LoginController.getInstance();
		if(getState() != LoginClientState.AUTHED_LOGIN)
			lc.removeLoginClient(this);
		else if(!PlayOK && _account != null && lc.isAccountInLoginServer(_account))
			lc.removeAuthedLoginClient(_account);

		_loginCrypt = null;
		_scrambledPair = null;
		_blowfishKey = null;

		super.onDisconnection();
	}

	@Override
	public String toString()
	{
		InetAddress address = getConnection().getSocket().getInetAddress();
		if(getState() == LoginClientState.AUTHED_LOGIN)
			return "[" + getAccount() + " (" + (address == null ? "disconnected" : address.getHostAddress()) + ")]";
		return "[" + (address == null ? "disconnected" : address.getHostAddress()) + "]";
	}

	public void addBannedIP(String ip, int incorrectCount)
	{
		int bantime = incorrectCount * incorrectCount;
		IpManager.getInstance().BanIp(ip, "Loginserver", bantime, "LoginTryBeforeBan=" + Config.LOGIN_TRY_BEFORE_BAN + " Count=" + incorrectCount);
	}

	public void setBonus(float value, int expire)
	{
		_bonus = value;
		_bonusExpire = expire;
	}

	public float getBonus()
	{
		return _bonus;
	}

	public int getBonusExpire()
	{
		return _bonusExpire;
	}

	//public void setProxy(boolean value)
	//{
	//	_proxy = value;
	//}

	//public boolean getProxy()
	//{
	//	return _proxy;
	//}

	public String getIpAddress()
	{
		try
		{
			return getConnection().getSocket().getInetAddress().getHostAddress();
		}
		catch(Exception e)
		{
			return "Null IP";
		}
	}
	
	public void setPointL(int value) throws SQLException
    {
        _points = value;
        LoginController lc = LoginController.getInstance();
        lc.rePoint(getAccount(), this);
    }

	public int getPointL()
    {
        return _points;
    }
}