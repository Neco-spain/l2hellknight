package l2rt.gameserver.model;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.InstancedZoneManager.InstancedZone;
import l2rt.gameserver.instancemanager.InstancedZoneManager.SpawnInfo;
import l2rt.gameserver.model.instances.L2DoorInstance;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2ReflectionBossInstance;
import l2rt.gameserver.network.serverpackets.ExSendUIEvent;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.ReflectionTable;
import l2rt.gameserver.tables.TerritoryTable;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Rnd;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

public class Reflection extends L2Object
{
	protected long _id = Integer.MAX_VALUE;
	private InstancedZone _instance = null;
	private int _instancedZoneId = 0;
	private Location _coreLoc; // место, к которому кидает при использовании SoE/unstuck, иначе выбрасывает в основной мир
	private Location _returnLoc; // если не прописано core, но прописан return, то телепортит туда, одновременно перемещая в основной мир
	private Location _teleportLoc; // точка входа
	private GArray<L2Spawn> _spawns = new GArray<L2Spawn>();
	private GArray<L2DoorInstance> _doors = new GArray<L2DoorInstance>();
	protected GArray<Long> _objects = new GArray<Long>();
	protected final ReentrantLock _objects_lock = new ReentrantLock(), _CollapseTimer_lock = new ReentrantLock();
	protected int _playerCount = 0;
	private Timer _collapseTimer;
	private Timer _collapse1minTimer;
	private TimerTask _collapseTimerTask;
	private TimerTask _collapse1minTimerTask;
	private L2Party _party;
	private L2CommandChannel _commandChannel;
	private boolean _isCollapseStarted;
	private int _geoIndex = 0;
	private String _name = "";
	private boolean _notCollapseWithoutPlayers = false;

	public int getGeoIndex()
	{
		return _geoIndex;
	}

	public void setGeoIndex(int id)
	{
		_geoIndex = id;
	}

	public int getInstancedZoneId()
	{
		return _instancedZoneId;
	}

	public void setInstancedZoneId(int id)
	{
		_instancedZoneId = id;
	}

	/**
	 * Использовать только для статичных отражений с id <= 0.
	 */
	public Reflection(long id)
	{
		super(IdFactory.getInstance().getNextId());
		_id = id;
		ReflectionTable.getInstance().addReflection(this);
	}

	/**
	 * Создает отражение и регистрирует его в индексе. Вызывать только из конструктора отражения.
	 * @param name
	 */
	public Reflection(String name)
	{
		super(IdFactory.getInstance().getNextId());
		_name = name;
		ReflectionTable.getInstance().addReflection(this);
	}

	/**
	 * Создает отражение и регистрирует его в индексе. Сохраняет информацию о инстансе.
	 */
	public Reflection(InstancedZone iz)
	{
		super(IdFactory.getInstance().getNextId());
		_instance = iz;
		_name = iz.getName();
		ReflectionTable.getInstance().addReflection(this);
	}

	public void addSpawn(L2Spawn spawn)
	{
		if(spawn != null)
			_spawns.add(spawn);
	}

	public void addDoor(L2DoorInstance door)
	{
		_doors.add(door);
	}

	public void setParty(L2Party party)
	{
		_party = party;
	}

	public L2Party getParty()
	{
		return _party;
	}

	public void setCommandChannel(L2CommandChannel commandChannel)
	{
		_commandChannel = commandChannel;
	}

	public void setNotCollapseWithoutPlayers(boolean value)
	{
		_notCollapseWithoutPlayers = value;
	}

	/**
	 * Время в мс
	 * @param time
	 */
	public void startCollapseTimer(long time)
	{
		if(_id <= 0)
		{
			new Exception("Basic reflection " + _id + " could not be collapsed!").printStackTrace();
			return;
		}

		_CollapseTimer_lock.lock();
		if(_collapseTimerTask != null)
		{
			_collapseTimerTask.cancel();
			_collapseTimerTask = null;
		}

		if(_collapse1minTimerTask != null)
		{
			_collapse1minTimerTask.cancel();
			_collapse1minTimerTask = null;
		}

		if(_collapseTimer != null)
		{
			_collapseTimer.cancel();
			_collapseTimer = null;
		}

		if(_collapse1minTimer != null)
		{
			_collapse1minTimer.cancel();
			_collapse1minTimer = null;
		}

		_collapseTimer = new Timer();
		_collapseTimerTask = new TimerTask(){
			@Override
			public void run()
			{
				collapse();
			}
		};

		_collapse1minTimer = new Timer();
		_collapse1minTimerTask = new TimerTask(){
			@Override
			public void run()
			{
				minuteBeforeCollapse();
			}
		};

		if(time > 60 * 1000L)
			_collapse1minTimer.schedule(_collapse1minTimerTask, time - 60 * 1000L);
		_collapseTimer.schedule(_collapseTimerTask, time);
		_CollapseTimer_lock.unlock();
	}

	public void stopCollapseTimer()
	{
		_CollapseTimer_lock.lock();
		if(_collapseTimerTask != null)
			_collapseTimerTask.cancel();
		_collapseTimerTask = null;
		if(_collapse1minTimerTask != null)
			_collapse1minTimerTask.cancel();
		_collapse1minTimerTask = null;

		if(_collapseTimer != null)
			_collapseTimer.cancel();
		if(_collapse1minTimer != null)
			_collapse1minTimer.cancel();
		_collapseTimer = null;
		_collapse1minTimer = null;
		_CollapseTimer_lock.unlock();
	}

	public void minuteBeforeCollapse()
	{
		if(_isCollapseStarted)
			return;
		_objects_lock.lock();
		for(Long storedId : _objects)
		{
			L2Object o = L2ObjectsStorage.get(storedId);
			if(o != null && o.isPlayer())
				((L2Player) o).sendPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(1));
		}
		_objects_lock.unlock();
	}

	public void collapse()
	{
		if(_id <= 0)
		{
			new Exception("Basic reflection " + _id + " could not be collapsed!").printStackTrace();
			return;
		}

		_CollapseTimer_lock.lock();
		if(_isCollapseStarted)
		{
			_CollapseTimer_lock.unlock();
			return;
		}
		_isCollapseStarted = true;
		_CollapseTimer_lock.unlock();

		try
		{
			stopCollapseTimer();

			for(L2Spawn s : _spawns)
				if(s != null)
					s.despawnAll();

			for(L2DoorInstance d : _doors)
				d.deleteMe();

			GArray<L2Player> teleport_list = new GArray<L2Player>();
			GArray<L2Object> delete_list = new GArray<L2Object>();

			_objects_lock.lock();
			for(Long storedId : _objects)
			{
				L2Object o = L2ObjectsStorage.get(storedId);
				if(o != null)
					if(o.isPlayer())
						teleport_list.add((L2Player) o);
					else if(!o.isSummon() && !o.isPet())
						delete_list.add(o);
			}
			_objects_lock.unlock();

			for(L2Player player : teleport_list)
			{
				if(player.getParty() != null)
				{
					if(equals(player.getParty().getReflection()))
						player.getParty().setReflection(null);
					if(player.getParty().getCommandChannel() != null && equals(player.getParty().getCommandChannel().getReflection()))
						player.getParty().getCommandChannel().setReflection(null);
				}
				if(equals(player.getReflection()))
					if(getReturnLoc() != null)
						player.teleToLocation(getReturnLoc(), 0);
					else
						player.setReflection(0);
				player.sendPacket(new ExSendUIEvent(player, true, true, 0, 10, _name)); // остановка счётчика (для инстанта закена).
			}

			if(_commandChannel != null)
				_commandChannel.setReflection(null);

			if(getParty() != null)
				getParty().setReflection(null);

			setParty(null);

			for(L2Object o : delete_list)
				o.deleteMe();

			_doors = null;
			_objects = null;
			_commandChannel = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2ObjectsStorage.remove(getId());
			IdFactory.getInstance().releaseId(getObjectId());
		}
	}

	public long getId()
	{
		return _id;
	}

	public void setId(long newId)
	{
		_id = newId;
	}

	public void addObject(L2Object o)
	{
		if(_isCollapseStarted)
			return;
		_objects_lock.lock();
		_objects.add(o.getStoredId());
		if(o.isPlayer())
			_playerCount++;
		_objects_lock.unlock();
	}

	public void removeObject(L2Object o)
	{
		if(_isCollapseStarted)
			return;
		_objects_lock.lock();
		_objects.remove(o.getStoredId());
		_objects_lock.unlock();
		if(o.isPlayer())
			_playerCount--;
		if(_playerCount <= 0 && _id > 0 && !_notCollapseWithoutPlayers)
			collapse();
	}

	public void setCoreLoc(Location l)
	{
		_coreLoc = l;
	}

	public Location getCoreLoc()
	{
		return _coreLoc;
	}

	public void setReturnLoc(Location l)
	{
		_returnLoc = l;
	}

	public Location getReturnLoc()
	{
		return _returnLoc;
	}

	public void setTeleportLoc(Location l)
	{
		_teleportLoc = l;
	}

	public Location getTeleportLoc()
	{
		return _teleportLoc;
	}

	public GArray<L2Spawn> getSpawns()
	{
		return _spawns;
	}

	public GArray<L2Player> getPlayers()
	{
		GArray<L2Player> result = new GArray<L2Player>();
		_objects_lock.lock();
		if(_objects != null)
			for(Long storedId : _objects)
			{
				L2Object o = L2ObjectsStorage.get(storedId);
				if(o != null && o.isPlayer())
					result.add((L2Player) o);
			}
		_objects_lock.unlock();
		return result;
	}

	public GArray<L2DoorInstance> getDoors()
	{
		return _doors;
	}

	@Override
	protected void finalize()
	{
		collapse();
	}

	public boolean canChampions()
	{
		return _id <= 0;
	}

	public boolean isAutolootForced()
	{
		return false;
	}

	public boolean isCollapseStarted()
	{
		return _isCollapseStarted;
	}

	public int getPlayerCount()
	{
		return _playerCount;
	}

	@Override
	public String getName()
	{
		return _name;
	}

	public InstancedZone getInstancedZone()
	{
		return _instance;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false; // XXX: для совместимости с суперклассом
	}

	public void FillSpawns(GArray<SpawnInfo> si)
	{
		if(si == null)
			return;
		for(SpawnInfo s : si)
		{
			L2Spawn c;
			GArray<int[]> points = TerritoryTable.getInstance().getLocation(s.getLocationId()).getCoords();
			switch(s.getType())
			{
				case 0: // точечный спаун, в каждой указанной точке
					for(int[] point : points)
					{
						c = s.getSpawn().clone();
						addSpawn(c);
						c.setReflection(getId());
						c.setRespawnDelay(s.getSpawn().getRespawnDelay(), s.getSpawn().getRespawnDelayRandom());
						c.setLocation(0);
						c.setLoc(new Location(point));
						if(!NpcTable.getTemplate(c.getNpcId()).isInstanceOf(L2ReflectionBossInstance.class))
							c.startRespawn();
						c.doSpawn(true);
						if(s.getSpawn().getNativeRespawnDelay() == 0)
							c.stopRespawn();
					}
					break;
				case 1: // один точечный спаун в рандомной точке
					c = s.getSpawn().clone();
					addSpawn(c);
					c.setReflection(getId());
					c.setRespawnDelay(s.getSpawn().getRespawnDelay(), s.getSpawn().getRespawnDelayRandom());
					c.setLocation(0);
					c.setLoc(new Location(points.get(Rnd.get(points.size()))));
					if(!NpcTable.getTemplate(c.getNpcId()).isInstanceOf(L2ReflectionBossInstance.class))
						c.startRespawn();
					c.doSpawn(true);
					if(s.getSpawn().getNativeRespawnDelay() == 0)
						c.stopRespawn();
					break;
				case 2: // локационный спаун
					c = s.getSpawn().clone();
					addSpawn(c);
					c.setReflection(getId());
					c.setRespawnDelay(s.getSpawn().getRespawnDelay(), s.getSpawn().getRespawnDelayRandom());
					if(!NpcTable.getTemplate(c.getNpcId()).isInstanceOf(L2ReflectionBossInstance.class))
						c.startRespawn();
					for(int j = 0; j < c.getAmount(); j++)
						c.doSpawn(true);
					if(s.getSpawn().getNativeRespawnDelay() == 0)
						c.stopRespawn();
			}
		}
	}

	public void FillDoors(GArray<L2DoorInstance> doors)
	{
		if(doors == null)
			return;
		for(L2DoorInstance d : doors)
		{
			L2DoorInstance door = d.clone();
			door.setReflection(this);
			addDoor(door);
			door.spawnMe();
			if(d.isOpen())
				door.openMe();
			door.setIsInvul(d.isInvul());
		}
	}

	/**
	 * Открывает дверь в отражении
	 */
	public void openDoor(int doorId)
	{
		for(L2DoorInstance door : getDoors())
			if(door.getDoorId() == doorId)
				door.openMe();
	}

	/**
	 * Закрывает дверь в отражении
	 */
	public void closeDoor(int doorId)
	{
		for(L2DoorInstance door : getDoors())
			if(door.getDoorId() == doorId)
				door.closeMe();
	}

	/**
	 * Удаляет все спауны из рефлекшена и запускает коллапс-таймер. Время указывается в минутах.
	 */
	public void clearReflection(int collapseTime, boolean message)
	{
		if(getId() <= 0)
			return;

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(){
			@Override
			public void run()
			{
				for(L2Spawn s : getSpawns())
				{
					s.despawnAll();
					s.stopRespawn();
				}
			}
		}, 1000);

		startCollapseTimer(collapseTime * 60 * 1000L);

		if(message)
			for(L2Player pl : getPlayers())
				if(pl != null)
					pl.sendPacket(new SystemMessage(SystemMessage.THIS_INSTANCE_ZONE_WILL_BE_TERMINATED_IN_S1_MINUTES_YOU_WILL_BE_FORCED_OUT_OF_THE_DANGEON_THEN_TIME_EXPIRES).addNumber(collapseTime));
	}
	
	public L2CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}

	public GArray<L2MonsterInstance> getMonsters()
	{
		GArray<L2MonsterInstance> result = new GArray<L2MonsterInstance>();
		_objects_lock.lock();
		if(_objects != null)
		{
			for(Long storedId : _objects)
			{
				L2Object o = L2ObjectsStorage.get(storedId);
				if((o != null) && (o.isMonster()))
				{
					result.add((L2MonsterInstance) o);
				}
			}
		}
		_objects_lock.unlock();
		return result;
	}
}