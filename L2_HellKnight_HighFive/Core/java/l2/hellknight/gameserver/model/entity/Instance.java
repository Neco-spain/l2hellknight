package l2.hellknight.gameserver.model.entity;

import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.hash.TIntHashSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2.hellknight.Config;
import l2.hellknight.gameserver.Announcements;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.datatables.DoorTable;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.idfactory.IdFactory;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.MapRegionManager;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.L2WorldRegion;
import l2.hellknight.gameserver.model.StatsSet;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2DoorInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.templates.L2DoorTemplate;
import l2.hellknight.gameserver.model.actor.templates.L2NpcTemplate;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.CreatureSay;
import l2.hellknight.gameserver.network.serverpackets.L2GameServerPacket;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

/**
 * @author evill33t, GodKratos
 */
public class Instance
{
	private static final Logger _log = Logger.getLogger(Instance.class.getName());
	
	private int _id;
	private String _name;
	
	private TIntHashSet _players = new TIntHashSet();
	private final EjectPlayerProcedure _ejectProc;
	
	private FastList<L2Npc> _npcs = new FastList<L2Npc>().shared();
	private ArrayList<L2DoorInstance> _doors = null;
	private int[] _spawnLoc = new int[3];
	private boolean _allowSummon = true;
	private long _emptyDestroyTime = -1;
	private long _lastLeft = -1;
	private long _instanceStartTime = -1;
	private long _instanceEndTime = -1;
	private boolean _isPvPInstance = false;
	private boolean _showTimer = false;
	private boolean _isTimerIncrease = true;
	private String _timerText = "";
	
	protected ScheduledFuture<?> _CheckTimeUpTask = null;
	
	public Instance(int id)
	{
		_id = id;
		_ejectProc = new EjectPlayerProcedure();
		_instanceStartTime = System.currentTimeMillis();
	}
	
	/**
	 * @return the ID of this instance.
	 */
	public int getId()
	{
		return _id;
	}
	
	/**
	 * @return the name of this instance
	 */
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	/**
	 * @return whether summon friend type skills are allowed for this instance
	 */
	public boolean isSummonAllowed()
	{
		return _allowSummon;
	}
	
	/**
	 * Sets the status for the instance for summon friend type skills
	 * @param b 
	 */
	public void setAllowSummon(boolean b)
	{
		_allowSummon = b;
	}
	
	/*
	 * Returns true if entire instance is PvP zone
	 */
	public boolean isPvPInstance()
	{
		return _isPvPInstance;
	}
	
	/*
	 * Sets PvP zone status of the instance
	 */
	public void setPvPInstance(boolean b)
	{
		_isPvPInstance = b;
	}
	
	/**
	 * Set the instance duration task
	 * @param duration in milliseconds
	 */
	public void setDuration(int duration)
	{
		if (_CheckTimeUpTask != null)
			_CheckTimeUpTask.cancel(true);
		
		_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(duration), 500);
		_instanceEndTime = System.currentTimeMillis() + duration + 500;
	}
	
	/**
	 * Set time before empty instance will be removed
	 * @param time in milliseconds
	 */
	public void setEmptyDestroyTime(long time)
	{
		_emptyDestroyTime = time;
	}
	
	/**
	 * Checks if the player exists within this instance
	 * @param objectId
	 * @return true if player exists in instance
	 */
	public boolean containsPlayer(int objectId)
	{
		return _players.contains(objectId);
	}
	
	/**
	 * Adds the specified player to the instance
	 * @param objectId Players object ID
	 */
	public void addPlayer(int objectId)
	{
		synchronized(_players)
		{
			_players.add(objectId);
		}
	}
	
	/**
	 * Removes the specified player from the instance list
	 * @param objectId Players object ID
	 */
	public void removePlayer(int objectId)
	{
		synchronized(_players)
		{
			_players.remove(objectId);
		}
		
		if (_players.isEmpty() && _emptyDestroyTime >= 0)
		{
			_lastLeft = System.currentTimeMillis();
			setDuration((int) (_instanceEndTime - System.currentTimeMillis() - 500));
		}
	}
	
	/**
	 * Removes the player from the instance by setting InstanceId to 0 and teleporting to nearest town.
	 * @param objectId
	 */
	public void ejectPlayer(int objectId)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(objectId);
		if (player != null && player.getInstanceId() == this.getId())
		{
			player.setInstanceId(0);
			player.sendMessage("You were removed from the instance");
			if (getSpawnLoc()[0] != 0 && getSpawnLoc()[1] != 0 && getSpawnLoc()[2] != 0)
				player.teleToLocation(getSpawnLoc()[0], getSpawnLoc()[1], getSpawnLoc()[2]);
			else
				player.teleToLocation(MapRegionManager.TeleportWhereType.Town);
		}
	}
	
	public void addNpc(L2Npc npc)
	{
		_npcs.add(npc);
	}
	
	public void removeNpc(L2Npc npc)
	{
		if (npc.getSpawn() != null)
			npc.getSpawn().stopRespawn();
		//npc.deleteMe();
		_npcs.remove(npc);
	}
	
	/**
	 * Adds a door into the instance
	 * @param doorId - from doorData.xml
	 * @param set - statset for initializing door
	 */
	private void addDoor(int doorId, StatsSet set)
	{
		if (_doors == null)
			_doors = new ArrayList<>(2);
		
		for (L2DoorInstance door: _doors)
		{
			if (door.getDoorId() == doorId)
			{
				_log.warning("Door ID " + doorId + " already exists in instance " + this.getId());
				return;
			}
		}
		
		L2DoorTemplate temp = DoorTable.getInstance().getDoorTemplate(doorId);
		L2DoorInstance newdoor = new L2DoorInstance(IdFactory.getInstance().getNextId(), temp, set);
		newdoor.setInstanceId(getId());
		newdoor.setCurrentHp(newdoor.getMaxHp());
		newdoor.spawnMe(temp.posX, temp.posY, temp.posZ);
		_doors.add(newdoor);
	}
	
	public TIntHashSet getPlayers()
	{
		return _players;
	}
	
	public FastList<L2Npc> getNpcs()
	{
		return _npcs;
	}

	/**
	 * @param npcId ID of NPC to return
	 * @return first found L2Npc object in this instance with given template ID
	 */
	public L2Npc getNpc(int npcId)
	{
		for (L2Npc npc : getNpcs())
		{
			if (npc.getNpcId() == npcId)
				return npc;
		}
		
		return null;
	}
	
	public ArrayList<L2DoorInstance> getDoors()
	{
		return _doors;
	}
	
	public L2DoorInstance getDoor(int id)
	{
		for (L2DoorInstance temp: getDoors())
		{
			if (temp.getDoorId() == id)
				return temp;
		}
		return null;
	}
	
	public long getInstanceEndTime()
	{
		return _instanceEndTime;
	}
	
	public long getInstanceStartTime()
	{
		return _instanceStartTime;
	}
	
	public boolean isShowTimer()
	{
		return _showTimer;
	}
	
	public boolean isTimerIncrease()
	{
		return _isTimerIncrease;
	}
	
	public String getTimerText()
	{
		return _timerText;
	}
	
	/**
	 * Returns the spawn location for this instance to be used when leaving the instance
	 * @return int[3]
	 */
	public int[] getSpawnLoc()
	{
		return _spawnLoc;
	}
	
	/**
	 * Sets the spawn location for this instance to be used when leaving the instance
	 * @param loc 
	 */
	public void setSpawnLoc(int[] loc)
	{
		if (loc == null || loc.length < 3)
			return;
		System.arraycopy(loc, 0, _spawnLoc, 0, 3);
	}
	
	public void removePlayers()
	{
		_players.forEach(_ejectProc);
		
		synchronized (_players)
		{
			_players.clear();
		}
	}
	
	public void removeNpcs()
	{
		for (L2Npc mob : _npcs)
		{
			if (mob != null)
			{
				if (mob.getSpawn() != null)
					mob.getSpawn().stopRespawn();
				mob.deleteMe();
			}
		}
		_npcs.clear();
	}
	
	public void removeDoors()
	{
		if (_doors == null)
			return;
		
		for (L2DoorInstance door: _doors)
		{
			if (door != null)
			{
				L2WorldRegion region = door.getWorldRegion();
				door.decayMe();
				
				if (region != null)
					region.removeVisibleObject(door);
				
				door.getKnownList().removeAllKnownObjects();
				L2World.getInstance().removeObject(door);
			}
		}
		_doors.clear();
		_doors = null;
	}
	
	public void loadInstanceTemplate(String filename)
	{
		Document doc = null;
		File xml = new File(Config.DATAPACK_ROOT, "data/instances/" + filename);
		
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(xml);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("instance".equalsIgnoreCase(n.getNodeName()))
				{
					parseInstance(n);
				}
			}
		}
		catch (IOException e)
		{
			_log.log(Level.WARNING, "Instance: can not find " + xml.getAbsolutePath() + " ! " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Instance: error while loading " + xml.getAbsolutePath() + " ! " + e.getMessage(), e);
		}
	}
	
	private void parseInstance(Node n) throws Exception
	{
		L2Spawn spawnDat;
		L2NpcTemplate npcTemplate;
		String name = null;
		name = n.getAttributes().getNamedItem("name").getNodeValue();
		setName(name);
		
		Node a;
		Node first = n.getFirstChild();
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("activityTime".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if (a != null)
				{
					_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(Integer.parseInt(a.getNodeValue()) * 60000), 15000);
					_instanceEndTime = System.currentTimeMillis() + Long.parseLong(a.getNodeValue()) * 60000 + 15000;
				}
			}
			/*			else if ("timeDelay".equalsIgnoreCase(n.getNodeName()))
						{
							a = n.getAttributes().getNamedItem("val");
							if (a != null)
								instance.setTimeDelay(Integer.parseInt(a.getNodeValue()));
						}*/
			else if ("allowSummon".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if (a != null)
					setAllowSummon(Boolean.parseBoolean(a.getNodeValue()));
			}
			else if ("emptyDestroyTime".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if (a != null)
					_emptyDestroyTime = Long.parseLong(a.getNodeValue()) * 1000;
			}
			else if ("showTimer".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if (a != null)
					_showTimer = Boolean.parseBoolean(a.getNodeValue());
				a = n.getAttributes().getNamedItem("increase");
				if (a != null)
					_isTimerIncrease = Boolean.parseBoolean(a.getNodeValue());
				a = n.getAttributes().getNamedItem("text");
				if (a != null)
					_timerText = a.getNodeValue();
			}
			else if ("PvPInstance".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if (a != null)
					setPvPInstance(Boolean.parseBoolean(a.getNodeValue()));
			}
			else if ("doorlist".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					int doorId = 0;
					if ("door".equalsIgnoreCase(d.getNodeName()))
					{
						doorId = Integer.parseInt(d.getAttributes().getNamedItem("doorId").getNodeValue());
						StatsSet set = new StatsSet();
						for (Node bean = d.getFirstChild(); bean != null; bean = bean.getNextSibling())
						{
							if ("set".equalsIgnoreCase(bean.getNodeName()))
							{
								NamedNodeMap attrs = bean.getAttributes();
								String setname = attrs.getNamedItem("name").getNodeValue();
								String value = attrs.getNamedItem("val").getNodeValue();
								set.set(setname, value);
							}
						}
						addDoor(doorId, set);
					}
				}
			}
			else if ("spawnlist".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					int npcId = 0, x = 0, y = 0, z = 0, respawn = 0, heading = 0, delay = -1;
					
					if ("spawn".equalsIgnoreCase(d.getNodeName()))
					{
						
						npcId = Integer.parseInt(d.getAttributes().getNamedItem("npcId").getNodeValue());
						x = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
						y = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
						z = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());
						heading = Integer.parseInt(d.getAttributes().getNamedItem("heading").getNodeValue());
						respawn = Integer.parseInt(d.getAttributes().getNamedItem("respawn").getNodeValue());
						if (d.getAttributes().getNamedItem("onKillDelay") != null)
							delay = Integer.parseInt(d.getAttributes().getNamedItem("onKillDelay").getNodeValue());
						
						npcTemplate = NpcTable.getInstance().getTemplate(npcId);
						if (npcTemplate != null)
						{
							spawnDat = new L2Spawn(npcTemplate);
							spawnDat.setLocx(x);
							spawnDat.setLocy(y);
							spawnDat.setLocz(z);
							spawnDat.setAmount(1);
							spawnDat.setHeading(heading);
							spawnDat.setRespawnDelay(respawn);
							if (respawn == 0)
								spawnDat.stopRespawn();
							else
								spawnDat.startRespawn();
							spawnDat.setInstanceId(getId());
							L2Npc spawned = spawnDat.doSpawn();
							if (delay >= 0 && spawned instanceof L2Attackable)
								((L2Attackable)spawned).setOnKillDelay(delay);
						}
						else
						{
							_log.warning("Instance: Data missing in NPC table for ID: " + npcId + " in Instance " + getId());
						}
					}
				}
			}
			else if ("spawnpoint".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					_spawnLoc[0] = Integer.parseInt(n.getAttributes().getNamedItem("spawnX").getNodeValue());
					_spawnLoc[1] = Integer.parseInt(n.getAttributes().getNamedItem("spawnY").getNodeValue());
					_spawnLoc[2] = Integer.parseInt(n.getAttributes().getNamedItem("spawnZ").getNodeValue());
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "Error parsing instance xml: " + e.getMessage(), e);
					_spawnLoc = new int[3];
				}
			}
		}
		if (Config.DEBUG)
			_log.info(name + " Instance Template for Instance " + getId() + " loaded");
	}
	
	protected void doCheckTimeUp(int remaining)
	{
		CreatureSay cs = null;
		int timeLeft;
		int interval;
		
		if (_players.isEmpty() && _emptyDestroyTime == 0)
		{
			remaining = 0;
			interval = 500;
		}
		else if (_players.isEmpty() && _emptyDestroyTime > 0)
		{
			
			Long emptyTimeLeft = _lastLeft + _emptyDestroyTime - System.currentTimeMillis();
			if (emptyTimeLeft <= 0)
			{
				interval = 0;
				remaining = 0;
			}
			else if (remaining > 300000 && emptyTimeLeft > 300000)
			{
				interval = 300000;
				remaining = remaining - 300000;
			}
			else if (remaining > 60000 && emptyTimeLeft > 60000)
			{
				interval = 60000;
				remaining = remaining - 60000;
			}
			else if (remaining > 30000 && emptyTimeLeft > 30000)
			{
				interval = 30000;
				remaining = remaining - 30000;
			}
			else
			{
				interval = 10000;
				remaining = remaining - 10000;
			}
		}
		else if (remaining > 300000)
		{
			timeLeft = remaining / 60000;
			interval = 300000;
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
			sm.addString(Integer.toString(timeLeft));
			Announcements.getInstance().announceToInstance(sm, getId());
			remaining = remaining - 300000;
		}
		else if (remaining > 60000)
		{
			timeLeft = remaining / 60000;
			interval = 60000;
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
			sm.addString(Integer.toString(timeLeft));
			Announcements.getInstance().announceToInstance(sm, getId());
			remaining = remaining - 60000;
		}
		else if (remaining > 30000)
		{
			timeLeft = remaining / 1000;
			interval = 30000;
			cs = new CreatureSay(0, Say2.ALLIANCE, "Notice", timeLeft + " seconds left.");
			remaining = remaining - 30000;
		}
		else
		{
			timeLeft = remaining / 1000;
			interval = 10000;
			cs = new CreatureSay(0, Say2.ALLIANCE, "Notice", timeLeft + " seconds left.");
			remaining = remaining - 10000;
		}
		if (cs != null)
			_players.forEach(new SendPacketToPlayerProcedure(cs));
		
		cancelTimer();
		if (remaining >= 10000)
			_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(remaining), interval);
		else
			_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), interval);
	}
	
	public void cancelTimer()
	{
		if (_CheckTimeUpTask != null)
			_CheckTimeUpTask.cancel(true);
	}
	
	public class CheckTimeUp implements Runnable
	{
		private int	_remaining;
		
		public CheckTimeUp(int remaining)
		{
			_remaining = remaining;
		}
		
		@Override
		public void run()
		{
			doCheckTimeUp(_remaining);
		}
	}
	
	public class TimeUp implements Runnable
	{
		@Override
		public void run()
		{
			InstanceManager.getInstance().destroyInstance(getId());
		}
	}
	
	
	private final class EjectPlayerProcedure implements TIntProcedure
	{
		EjectPlayerProcedure()
		{
			
		}
		
		@Override
		public final boolean execute(final int objId)
		{
			ejectPlayer(objId);
			return true;
		}
	}
	
	private final class SendPacketToPlayerProcedure implements TIntProcedure
	{
		private final L2GameServerPacket _packet;
		
		SendPacketToPlayerProcedure(final L2GameServerPacket packet)
		{
			_packet = packet;
		}
		
		@Override
		public final boolean execute(final int objId)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			
			if (player.getInstanceId() == getId())
			{
				player.sendPacket(_packet);
			}
			return true;
		}
	}
}