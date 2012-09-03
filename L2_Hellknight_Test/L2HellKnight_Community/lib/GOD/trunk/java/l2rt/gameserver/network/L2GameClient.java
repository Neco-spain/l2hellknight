package l2rt.gameserver.network;

import drin.nativeLib.Deflater;
import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.extensions.network.MMOClient;
import l2rt.extensions.network.MMOConnection;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.security.SecondaryPasswordAuth;
import l2rt.gameserver.instancemanager.PlayerManager;
import l2rt.gameserver.loginservercon.LSConnection;
import l2rt.gameserver.loginservercon.SessionKey;
import l2rt.gameserver.loginservercon.gspackets.PlayerLogout;
import l2rt.gameserver.loginservercon.gspackets.PointConnection;
import l2rt.gameserver.model.CharSelectInfoPackage;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;
import l2rt.util.HWID.HardwareID;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import ccpGuard.crypt.GameCrypt;

/**
 * Represents a client connected on Game Server
 */
public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>>
{
	protected static Logger _log = Logger.getLogger(L2GameClient.class.getName());

	public GameCrypt _crypt = null;
	private float _bonus = 1;
	private long _bonus_expire = 0;
	public GameClientState _state;
	private int _upTryes = 0, _upTryesTotal = 0;
	private long _upTryesRefresh = 0;
    private int point = 0;

	public ccpGuard.ProtectInfo _prot_info = null;


    public void setPoint(int point) 
	{
        this.point = point;
		LSConnection.getInstance().sendPacket(new PointConnection(getLoginName(), getPoint()));
    }

    public int getPoint() 
	{
        return point;
    }

    public static enum GameClientState
	{
		CONNECTED,
		AUTHED,
		IN_GAME
	}

	private String _loginName;
	private L2Player _activeChar;
	private SessionKey _sessionId = null;
	private MMOConnection<L2GameClient> _connection = null;
	private SecondaryPasswordAuth _secondaryAuth;
	//private byte[] _filter;

	private int revision = 0;
	private boolean _gameGuardOk = false;

	public boolean protect_used = false;
	public byte client_lang = -1;
	public HardwareID HWID = null;

	private GArray<Integer> _charSlotMapping = new GArray<Integer>();
	private PacketLogger pktLogger = null;
	private boolean pktLoggerMatch = false;
	public StatsSet account_fields = null;
	public Deflater deflater = null;

	public L2GameClient(MMOConnection<L2GameClient> con, boolean offline)
	{
		super(con);
		if(!offline)
		{
			_state = GameClientState.CONNECTED;
			_connection = con;
			_sessionId = new SessionKey(-1, -1, -1, -1);
			_crypt = new GameCrypt();
			if(Config.PROTECT_ENABLE)
				protect_used = !Config.PROTECT_UNPROTECTED_IPS.isIpInNets(getIpAddr());

			_prot_info = new ccpGuard.ProtectInfo(this, getIpAddr(), offline);
			
			if(Config.LOG_CLIENT_PACKETS || Config.LOG_SERVER_PACKETS)
			{
				pktLogger = new PacketLogger(this, Config.PACKETLOGGER_FLUSH_SIZE);
				if(Config.PACKETLOGGER_IPS != null)
					if(Config.PACKETLOGGER_IPS.isIpInNets(getIpAddr()))
						pktLoggerMatch = true;
			}
		}
		else
			_state = GameClientState.IN_GAME;
	}

	public L2GameClient(MMOConnection<L2GameClient> con)
	{
		this(con, false);
	}

	public void OnOfflineTrade()
	{
		deflater = null;
		_charSlotMapping = null;
	}

	public void disconnectOffline()
	{
		onDisconnection();
	}

	@Override
	protected void onDisconnection()
	{
		if(pktLogger != null)
		{
			if(!pktLogger.assigned() && pktLoggerMatch)
				pktLogger.assign();
			pktLogger.close();
			pktLogger = null;
		}

		if(getLoginName() == null || getLoginName().equals("") || _state != GameClientState.IN_GAME && _state != GameClientState.AUTHED)
			return;

		ccpGuard.Protection.doDisconection(this);
		try
		{
			if(_activeChar != null && _activeChar.isInOfflineMode())
				//LSConnection.getInstance().sendPacket(new PlayerLogout(getLoginName()));
				return;

			LSConnection.getInstance().removeAccount(this);
			L2Player player = _activeChar;
			_activeChar = null;

			if(player != null && !player.isLogoutStarted()) // this should only happen on connection loss
			{
				player.scheduleDelete(Config.PLAYER_DISCONNECT_INGAME_TIME);
				if(player.getNetConnection() != null)
				{
					if(!player.isInOfflineMode())
						player.getNetConnection().closeNow(false);
					player.setNetConnection(null);
				}
				player.setConnected(false);
				if(Config.PLAYER_DISCONNECT_INGAME_TIME > 0)
					player.broadcastUserInfo(false);
				_activeChar = null;
			}

			setConnection(null);
		}
		catch(Exception e1)
		{
			_log.log(Level.WARNING, "error while disconnecting client", e1);
		}
		finally
		{
			LSConnection.getInstance().sendPacket(new PlayerLogout(getLoginName()));
		}
		super.onDisconnection();
	}

	public void markRestoredChar(int charslot) throws Exception
	{
		int objid = getObjectIdForSlot(charslot);
		if(objid < 0)
			return;

		if(_activeChar != null && _activeChar.getObjectId() == objid)
			_activeChar.setDeleteTimer(0);

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "data error on restore char:", e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void markToDeleteChar(int charslot) throws Exception
	{
		int objid = getObjectIdForSlot(charslot);
		if(objid < 0)
			return;

		if(_activeChar != null && _activeChar.getObjectId() == objid)
			_activeChar.setDeleteTimer((int) (System.currentTimeMillis() / 1000));

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?");
			statement.setLong(1, (int) (System.currentTimeMillis() / 1000));
			statement.setInt(2, objid);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "data error on update deletime char:", e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void deleteChar(int charslot) throws Exception
	{
		//have to make sure active character must be nulled
		if(_activeChar != null)
		{
			_activeChar.logout(false, false, true, true);
			_activeChar = null;
		}

		int objid = getObjectIdForSlot(charslot);
		if(objid == -1)
			return;

		PlayerManager.deleteCharByObjId(objid);
	}

	public boolean hasHWID()
	{
		return HWID != null;
	}

	public L2Player loadCharFromDisk(int charslot)
	{
		Integer objectId = getObjectIdForSlot(charslot);
		if(objectId == -1)
			return null;

		L2Player character = null;
		L2Player old_player = L2ObjectsStorage.getPlayer(objectId);

		if(old_player != null)
			if(old_player.isInOfflineMode() || old_player.isLogoutStarted())
				// оффтрейдового чара проще выбить чем восстанавливать
				old_player.logout(false, false, true, true);
			else
			{
				old_player.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
				LSConnection.getInstance().sendPacket(new PlayerLogout(getLoginName()));

				if(old_player.getNetConnection() != null)
				{
					old_player.getNetConnection().setActiveChar(null);
					old_player.getNetConnection().closeNow(false);
				}
				old_player.setLogoutStarted(false);
				old_player.setNetConnection(this);
				character = old_player;
			}

		if(character == null)
			character = L2Player.restore(objectId);

		if(character != null)
		{
			// preinit some values for each login
			character.setRunning(); // running is default
			character.standUp(); // standing is default

			character.updateStats();
			character.setOnlineStatus(true);
			setActiveChar(character);
			character.restoreBonus();
			character.bookmarks.restore();
			if(Config.USE_CLIENT_LANG)
				switch(client_lang)
				{
					case 0:
						character.setVar("lang@", "en");
						break;
					case 1:
						character.setVar("lang@", "ru");
						break;
				}
			if(protect_used && Config.PROTECT_GS_STORE_HWID && hasHWID())
				character.storeHWID(HWID.Full);

			if(pktLogger != null)
				if(!pktLogger.assigned())
				{
					if(!pktLoggerMatch)
						if(Config.PACKETLOGGER_CHARACTERS != null)
						{
							String char_name = character.getName();
							for(int i = 0; i < Config.PACKETLOGGER_CHARACTERS.size(); i++)
							{
								String s_mask = Config.PACKETLOGGER_CHARACTERS.get(i);
								if(char_name.matches(s_mask))
								{
									pktLoggerMatch = true;
									break;
								}
							}
						}
					if(pktLoggerMatch)
						pktLogger.assign();
					else
						pktLogger = null;
				}
		}
		else
			_log.warning("could not restore obj_id: " + objectId + " in slot:" + charslot);

		return character;
	}

	public int getObjectIdForSlot(int charslot)
	{
		if(charslot < 0 || charslot >= _charSlotMapping.size())
		{
			_log.warning(getLoginName() + " tried to modify Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		return _charSlotMapping.get(charslot);
	}

	@Override
	public MMOConnection<L2GameClient> getConnection()
	{
		return _connection;
	}

	public L2Player getActiveChar()
	{
		return _activeChar;
	}

	/**
	 * @return Returns the sessionId.
	 */
	public SessionKey getSessionId()
	{
		return _sessionId;
	}

	public String getLoginName()
	{
		return _loginName;
	}

	private void logHWID()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Config.PROTECT_GS_LOG_HWID_QUERY);
			statement.setString(1, _loginName);
			statement.setString(2, getIpAddr());
			statement.setString(3, HWID.Full);
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warning("could not log HWID:" + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void setLoginName(String loginName)
	{
		_loginName = loginName;
		if(protect_used && Config.PROTECT_GS_LOG_HWID && getIpAddr() != "Disconnected")
			logHWID();

		if (Config.SECOND_AUTH_ENABLED)
		{
			_secondaryAuth = new SecondaryPasswordAuth(this);
		}
		
		if(pktLogger != null && !pktLoggerMatch && Config.PACKETLOGGER_ACCOUNTS != null)
			for(int i = 0; i < Config.PACKETLOGGER_ACCOUNTS.size(); i++)
			{
				String s_mask = Config.PACKETLOGGER_ACCOUNTS.get(i);
				if(loginName.matches(s_mask))
				{
					pktLoggerMatch = true;
					break;
				}
			}
	}

	public void setActiveChar(L2Player cha)
	{
		_activeChar = cha;
		if(cha != null)
			// we store the connection in the player object so that external
			// events can directly send events to the players client
			// might be changed later to use a central event management and distribution system
			_activeChar.setNetConnection(this);
	}

	public void setSessionId(SessionKey sessionKey)
	{
		_sessionId = sessionKey;
	}

	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping.clear();

		for(CharSelectInfoPackage element : chars)
		{
			int objectId = element.getObjectId();
			_charSlotMapping.add(objectId);
		}
	}

	public void setCharSelection(int c)
	{
		_charSlotMapping.clear();
		_charSlotMapping.add(c);
	}

	/**
	 * @return Returns the revision.
	 */
	public int getRevision()
	{
		return revision;
	}

	/**
	 * @param revision The revision to set.
	 */
	public void setRevision(int revision)
	{
		this.revision = revision;
	}

	public void setGameGuardOk(boolean gameGuardOk)
	{
		_gameGuardOk = gameGuardOk;
	}

	public boolean isGameGuardOk()
	{
		return _gameGuardOk;
	}

	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		if(pktLogger != null && Config.LOG_SERVER_PACKETS)
			pktLogger.log_packet((byte) 1, buf, size);
		_crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}

    @Override
    public boolean decrypt(ByteBuffer buf, int size)
	{
        _crypt.decrypt(buf.array(), buf.position(), size);
        if (pktLogger != null && Config.LOG_CLIENT_PACKETS)
            pktLogger.log_packet((byte) 0, buf, size);
        return true;
    }

	public void sendPacket(L2GameServerPacket... gsp)
	{
		if(getConnection() == null)
			return;
		getConnection().sendPacket(gsp);
	}

	@SuppressWarnings("unchecked")
	public void sendPackets(Collection<L2GameServerPacket> gsp)
	{
		if(getConnection() == null)
			return;
		getConnection().sendPackets((Collection) gsp);
	}

	public void close(L2GameServerPacket gsp)
	{
		getConnection().close(gsp);
	}

	public String getIpAddr()
	{
		try
		{
			return _connection.getSocket().getInetAddress().getHostAddress();
		}
		catch(NullPointerException e)
		{
			return "Disconnected";
		}
	}

	public byte[] enableCrypt()
	{
		//initCompression();
		byte[] key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		return key;
	}

	//public void initCompression()
	//{
	//	if(protect_used && Config.PROTECT_COMPRESSION > 0 && Config.PROTECT_COMPRESSION < 10)
	//		deflater = new Deflater(Config.PROTECT_COMPRESSION, true);
	//}

	public float getBonus()
	{
		return _bonus;
	}

	public void setBonus(float bonus)
	{
		_bonus = bonus;
	}

	/**
	 * @return время окончания бонуса в unixtime
	 */
	public long getBonusExpire()
	{
		return _bonus_expire;
	}

	public void setBonusExpire(long time)
	{
		if(time < 0)
			return;
		if(time < System.currentTimeMillis() / 1000)
		{
			_bonus = 1;
			return;
		}
		_bonus_expire = time;
	}

	public GameClientState getState()
	{
		return _state;
	}

	public void setState(GameClientState state)
	{
		_state = state;
	}

	/**
	 * @return произведено ли отключение игрока
	 */
	public boolean onClientPacketFail()
	{
		if(isPacketsFailed())
			return true;

		if(_upTryesRefresh == 0)
			_upTryesRefresh = System.currentTimeMillis() + 5000;
		else if(_upTryesRefresh < System.currentTimeMillis())
		{
			_upTryesRefresh = System.currentTimeMillis() + 5000;
			_upTryes = 0;
		}

		_upTryes++;
		_upTryesTotal++;

		if(_upTryes > 4 || _upTryesTotal > 10)
		{
			_log.warning("Too many client packet fails, connection closed. IP: " + getIpAddr() + ", account:" + getLoginName());
			L2Player activeChar = getActiveChar();
			if(activeChar != null)
				activeChar.logout(false, false, true, true);
			else
				closeNow(true);
			_upTryesTotal = Integer.MAX_VALUE;
			return true;
		}

		return false;
	}

	public boolean isPacketsFailed()
	{
		return _upTryesTotal == Integer.MAX_VALUE;
	}
	
	public SecondaryPasswordAuth getSecondaryAuth()
	{
		return _secondaryAuth;
	}
	

	@Override
	public Deflater getDeflater()
	{
		return deflater;
	}

	@Override
	public String toString()
	{
		return "L2GameClient: " + (_activeChar == null ? _loginName : _activeChar) + "@" + getIpAddr();
	}
}