package net.sf.l2j.gameserver.network;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.protection.nProtect;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.CharSelectInfoPackage;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.util.EventData;

import org.mmocore.network.MMOClient;
import org.mmocore.network.MMOConnection;

/**
 * Represents a client connected on Game Server
 * @author  KenM
 */
public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>>
{
	protected static final Logger _log = Logger.getLogger(L2GameClient.class.getName());
	public static enum GameClientState { CONNECTED, AUTHED, IN_GAME };

	public GameClientState state;

	// Info
	public String accountName;
	public SessionKey sessionId;
	public L2PcInstance activeChar;
	private ReentrantLock _activeCharLock = new ReentrantLock();

	//@SuppressWarnings("unused")
	private long _connectionStartTime;
	private List<Integer> _charSlotMapping = new FastList<Integer>();
	// Task
	@SuppressWarnings("rawtypes")
	protected /*final*/ ScheduledFuture _autoSaveInDB;
	protected ScheduledFuture<?> _cleanupTask = null;
	private ScheduledFuture<?> _guardCheckTask = null;
	// Crypt
	public GameCrypt crypt;
	private boolean _isAuthedGG;
	// Flood protection
	public byte packetsSentInSec = 0;
	public int packetsSentStartTick = 0;
	
	private boolean _isDetached = false;

	public L2GameClient(MMOConnection<L2GameClient> con)
	{
		super(con);
		state = GameClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();
		crypt = new GameCrypt();
		_guardCheckTask = nProtect.getInstance().startTask(this);
		_autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), 300000L, 900000L);
	}

	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		crypt.setKey(key);
		return key;
	}

	public GameClientState getState()
	{
		return state;
	}

	public void setState(GameClientState pState)
	{
		state = pState;
	}

	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		crypt.decrypt(buf.array(), buf.position(), size);
		return true;
	}

	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}

	public L2PcInstance getActiveChar()
	{
		return activeChar;
	}

	public void setActiveChar(L2PcInstance pActiveChar)
	{
		activeChar = pActiveChar;
		if (activeChar != null)
		{
			L2World.getInstance().storeObject(getActiveChar());
		}
	}

	public ReentrantLock getActiveCharLock()
	{
		return _activeCharLock;
	}
	
	public boolean isAuthedGG()
	{
		return _isAuthedGG;
	}

	public void setGameGuardOk(boolean val)
	{
		_isAuthedGG = val;
	}

	public void setAccountName(String pAccountName)
	{
		accountName = pAccountName;
	}

	public String getAccountName()
	{
		return accountName;
	}

	public void setSessionId(SessionKey sk)
	{
		sessionId = sk;
	}

	public SessionKey getSessionId()
	{
		return sessionId;
	}

	public void sendPacket(L2GameServerPacket gsp)
	{
		if (_isDetached) return;

		getConnection().sendPacket(gsp);
		gsp.runImpl();
	}

	public boolean isDetached()
	{
		return _isDetached;
	}
	
	public void isDetached(boolean b)
	{
		_isDetached = b;
	}
	
	public L2PcInstance markToDeleteChar(int charslot) throws Exception
	{
		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
		    return null;

		L2PcInstance character = L2PcInstance.load(objid);
		if (character.getClanId() != 0)
			return character;

		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?");
			statement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS*86400000L); // 24*60*60*1000 = 86400000
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Data error on update delete time of char: " + e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	    return null;
	}

	public L2PcInstance deleteChar(int charslot) throws Exception
	{
		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
    	    return null;

		L2PcInstance character = L2PcInstance.load(objid);
		if (character.getClanId() != 0)
			return character;

		deleteCharByObjId(objid);
		return null;
	}

	/**
	 * Save the L2PcInstance to the database.
	 */
	public static void saveCharToDisk(L2PcInstance cha)
	{
        try
        {
            cha.store();
        }
        catch(Exception e)
        {
            _log.severe("Error saving player character: "+e);
        }
	}

	public void markRestoredChar(int charslot) throws Exception
	{
		int objid = getObjectIdForSlot(charslot);
    		if (objid < 0)
    		    return;
		java.sql.Connection con = null;
		try
		{
		con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
		statement.setInt(1, objid);
		statement.execute();
		statement.close();
		}
		catch (Exception e)
		{
			_log.severe("Data error on restoring char: " + e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}




	public static void deleteCharByObjId(int objid)
	{
	    if (objid < 0)
	        return;

	    java.sql.Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement ;

        	statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();

            statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();

			statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

            statement = con.prepareStatement("DELETE FROM heroes WHERE char_id=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();

            statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();

            statement = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();

        	statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();


			statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Data error on deleting char: " + e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}

	public L2PcInstance loadCharFromDisk(int charslot)
	{
		L2PcInstance character = L2PcInstance.load(getObjectIdForSlot(charslot));

        if (character != null)
		{
			character.setRunning();
			character.standUp();

			character.refreshOverloaded();
			character.refreshExpertisePenalty();
			character.sendPacket(new UserInfo(character));
			character.broadcastKarma();
            character.setOnlineStatus(true);
		}
		else
		{
			_log.severe("could not restore in slot: "+ charslot);
		}

		//setCharacter(character);
		return character;
	}

	/**
     * @param chars
     */
    public void setCharSelection(CharSelectInfoPackage[] chars)
    {
        _charSlotMapping.clear();

        for (int i = 0; i < chars.length; i++)
        {
            int objectId = chars[i].getObjectId();
            _charSlotMapping.add(new Integer(objectId));
        }
    }

    public void close(L2GameServerPacket gsp)
    {
    	getConnection().close(gsp);
    }

    /**
     * @param charslot
     * @return
     */
    private int getObjectIdForSlot(int charslot)
    {
        if (charslot < 0 || charslot >= _charSlotMapping.size())
        {
            _log.warning(toString()+" tried to delete Character in slot "+charslot+" but no characters exits at that slot.");
            return -1;
        }
        Integer objectId = _charSlotMapping.get(charslot);
        return objectId.intValue();
    }

    @Override
    protected void onForcedDisconnection()
    {
    	_log.info("Client "+toString()+" disconnected abnormally.");
		L2PcInstance player = null;
		if((player = getActiveChar()) !=null)
		{

		_log.log(Level.WARNING, "Character disconnected at Loc X:"+getActiveChar().getX()+" Y:"+getActiveChar().getY()+" Z:"+getActiveChar().getZ());

		_log.log(Level.WARNING, "Character disconnected in (closest) zone: "+MapRegionTable.getInstance().getClosestTownName(getActiveChar()));
		
		if(player.isInParty())
			{
				player.getParty().removePartyMember(player);
			}
		
		else if(Olympiad.getInstance().isRegistered(player))
			{
				Olympiad.getInstance().unRegisterNoble(player);
			}
			
		player.deleteMe();
		
			try
			{
				//the force operation will allow to not save client position to prevent again criticals and stuck
				player.store();
			}
			catch(Exception e)
			{

			}
			L2World.getInstance().removeFromAllPlayers(player);
			setActiveChar(null);
			LoginServerThread.getInstance().sendLogout(getAccountName());
		}
		stopGuardTask();
		nProtect.getInstance().closeSession(this);
    }
    
	public void stopGuardTask()
	{
		if(_guardCheckTask != null)
		{
			_guardCheckTask.cancel(true);
			_guardCheckTask = null;
		}
	}

	@Override
    protected void onDisconnection()
	{
    	try
    	{
    		ThreadPoolManager.getInstance().executeTask(new DisconnectTask());
    	}
    	catch (RejectedExecutionException e)
    	{
    	}
    }

    @Override
	public String toString()
	{
		try
		{
			InetAddress address = getConnection().getSocket().getInetAddress();
			switch (getState())
			{
				case CONNECTED:
					return "[IP: "+(address == null ? "disconnected" : address.getHostAddress())+"]";
				case AUTHED:
					return "[Account: "+getAccountName()+" - IP: "+(address == null ? "disconnected" : address.getHostAddress())+"]";
				case IN_GAME:
					return "[Character: "+(getActiveChar() == null ? "disconnected" : getActiveChar().getName())+" - Account: "+getAccountName()+" - IP: "+(address == null ? "disconnected" : address.getHostAddress())+"]";
				default:
					throw new IllegalStateException("Missing state on switch");
			}
		}
		catch (NullPointerException e)
		{
			return "[Character read failed due to disconnect]";
		}
	}

	class DisconnectTask implements Runnable
	{
		public void run()
		{
			boolean fast = true;

			try
			{
				isDetached(true);
				L2PcInstance player = L2GameClient.this.getActiveChar();
				if (player != null)
				{
					if (!player.isInOlympiadMode()
							&& !player.isFestivalParticipant()
							&& !TvTEvent.isPlayerParticipant(player.getObjectId()) 
							&& !player.isInJail()
							&& !player.isInFunEvent())
					if (player.isInCombat())
					{
						fast = false;
					}
					
					if(!Olympiad.getInstance().isRegistered(player) 
							&& !player.isInOlympiadMode() 
							&& !player.isInFunEvent() && 
							player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE || 
							player.isInCraftMode() && Config.OFFLINE_CRAFT_ENABLE)
					{
						player.setOffline(true);
						player.leaveParty();
						player.store();
						player.startAbnormalEffect(L2Character.ABNORMAL_EFFECT_SLEEP);
						/*if(Config.OFFLINE_SET_NAME_COLOR)
						{
							player._originalNameColorOffline=player.getAppearance().getNameColor();
							player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
							player.broadcastUserInfo();
						}*/
						
						if (player.getOfflineStartTime() == 0)
							player.setOfflineStartTime(System.currentTimeMillis());
						
						return;
					}
					
					if(Olympiad.getInstance().isRegistered(player)){
						Olympiad.getInstance().unRegisterNoble(player);
					}
					
					// Remove player from world
					player.deleteMe();
					
					//Save data
					try
					{
						player.store();
					}
					catch(Exception e2)
					{
						
							e2.printStackTrace();
					}
					
				}
				cleanMe(fast);
			}
			catch (Exception e1)
			{
				_log.log(Level.WARNING, "Error while disconnecting client.", e1);
			}
		}
	}

	public void cleanMe(boolean fast)
	{
		try
		{
			synchronized(this)
			{
	            if (_cleanupTask == null)
				{
	            	_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), fast ? 5 : 15000L);
				}
			}
		}
		catch (Exception e1)
		{
			_log.log(Level.WARNING, "Error during cleanup.", e1);			
		}
	}

	class CleanupTask implements Runnable
	{
		public void run()
		{
			try
			{
				try
				{
					RegionBBSManager.getInstance().changeCommunityBoard();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				if (_autoSaveInDB != null)
                {
				    _autoSaveInDB.cancel(true);
                }

	            L2PcInstance player = L2GameClient.this.getActiveChar();
				if (player != null)
				{
	                if (player.atEvent)
	                {
	                	EventData data = new EventData(player.eventX, player.eventY, player.eventZ, player.eventkarma, player.eventpvpkills, player.eventpkkills, player.eventTitle, player.kills, player.eventSitForced);
	                    L2Event.connectionLossData.put(player.getName(), data);
	                }
	                if (player.isFlying())
	                {
	                	player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
	                }
					isDetached(false);
					player.deleteMe();
					try
	                {
						saveCharToDisk(player);
					}
	                catch (Exception e2) {}
				}
				L2GameClient.this.setActiveChar(null);
			}
			catch (Exception e1)
			{
				_log.log(Level.WARNING, "Error while cleanup client.", e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(L2GameClient.this.getAccountName());
			}
		}
	}
	
	class AutoSaveTask implements Runnable
	{
		public void run()
		{
			try
			{
				L2PcInstance player = L2GameClient.this.getActiveChar();
				if (player != null)
				{
					saveCharToDisk(player);
				}
			}
			catch (Throwable e)
			{
				_log.severe(e.toString());
			}
		}
	}

	public boolean isSendingTooManyUnknownPackets()
	{
		if (getActiveChar() == null)
			return false;

		if (!FloodProtector.getInstance().tryPerformAction(getActiveChar().getObjectId(), FloodProtector.PROTECTED_UNKNOWNPACKET))
		{
			_packetCount++;

			if (_packetCount >= Config.MAX_UNKNOWN_PACKETS)
				return true;
			else
				return false;
		}
		else
		{
			_packetCount = 0;
			return false;
		}
	}
	private int	_packetCount	= 0;
}
