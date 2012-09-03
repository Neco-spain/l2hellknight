package l2rt.gameserver.instancemanager;

import java.util.Calendar;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.InstancedZoneManager.InstancedZone;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.model.instances.L2CubeNpcInstance;
import l2rt.gameserver.model.instances.L2DoorInstance;
import l2rt.gameserver.model.instances.L2WatcherInstance;
import l2rt.gameserver.network.serverpackets.ExCubeTimerStop;
import l2rt.gameserver.network.serverpackets.ExShowCubeInfo;
import l2rt.gameserver.network.serverpackets.ExShowCubeTimer;
import l2rt.gameserver.network.serverpackets.ExShowScreenMessage;
import l2rt.gameserver.network.serverpackets.ItemList;
import l2rt.gameserver.network.serverpackets.NpcSay;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.ReflectionTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.ExclusiveTask;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class KrateisCubeManager
{
	private static KrateisCubeManager _instance;

	private L2CubeNpcInstance _npcRegistrator = null;
	private int _registratorCoord[] = { -70567, -71071, -1420};
	public int _managerCoord[] = { -87027, -82026};
	private int _roomX[] = { -85905, -83910, -81905, -79909, -77908};
	private int _roomY[] = { -85802, -83807, -81802, -79805, -77807};
	private int _skillsId[] = {1086, 1204, 1059, 1085, 1078, 1068, 1240, 1077, 1242, 1062};
	private int _skillsLvl[] = {2, 2, 3, 3, 6, 3, 3, 3, 3, 2};

	private boolean _eventActive = false;
	public boolean _isRegPeriod = false;

	private FastList<EventPlayer> _regPlayers70 = new FastList<EventPlayer>();
	private FastList<EventPlayer> _regPlayers76 = new FastList<EventPlayer>();
	private FastList<EventPlayer> _regPlayers80 = new FastList<EventPlayer>();
	private FastList<EventPlayer> _ingamePlayers70 = new FastList<EventPlayer>();
	private FastList<EventPlayer> _ingamePlayers76 = new FastList<EventPlayer>();
	private FastList<EventPlayer> _ingamePlayers80 = new FastList<EventPlayer>();
	private FastList<L2WatcherInstance> _watchers = new FastList<L2WatcherInstance>();
	private FastList<Rooms> _rooms70 = new FastList<Rooms>();
	private FastList<Rooms> _rooms76 = new FastList<Rooms>();
	private FastList<Rooms> _rooms80 = new FastList<Rooms>();

	private int _instance70Id = 0;
	private int _instance76Id = 0;
	private int _instance80Id = 0;

	private long _anonceTimer = 0;
	private long _anonceStep = 0;
	private int _secToEvent = 0;
	private boolean _half = true;

	public static final KrateisCubeManager getInstance()
	{
		if(_instance == null)
			_instance = new KrateisCubeManager();
		return _instance;
	}

	public KrateisCubeManager()
	{
		_instance70Id = createInstance(1001);
		if(_instance70Id > 0)
			doorControl(_instance70Id, 70);
		_instance76Id = createInstance(1002);
		if(_instance76Id > 0)
			doorControl(_instance76Id, 76);
		_instance80Id = createInstance(1003);
		if(_instance76Id > 0)
			doorControl(_instance80Id, 80);
		if((_instance70Id > 0) && (_instance76Id > 0) && (_instance80Id > 0))
		{
			_anonceTask.schedule(1000);
			_doorControlTask.schedule(10000);
		}
		spawnWatchers();
	}

	public int createInstance(int id)
	{
		FastMap<Integer, InstancedZone> listInstance = InstancedZoneManager.getInstance().getById(id);
		InstancedZone loc = listInstance.get(0);

		Reflection ref = new Reflection(IdFactory.getInstance().getNextId());

		for(L2DoorInstance door : loc.getDoors())
		{
			ref.addDoor(door.clone());
		}

		return (int) ref.getId();
	}

	private void startEvent()
	{
		if(_ingamePlayers70.size() > 0)
		{
			distributeRooms(_ingamePlayers70);
			teleportInCube(_ingamePlayers70);
		}
		if(_ingamePlayers76.size() > 0)
		{
			distributeRooms(_ingamePlayers76);
			teleportInCube(_ingamePlayers76);
		}
		if(_ingamePlayers80.size() > 0)
		{
			distributeRooms(_ingamePlayers80);
			teleportInCube(_ingamePlayers80);
		}
		spawnWatchers();
	}

	public void changeWather(L2WatcherInstance npc)
	{
		if(_watchers.contains(npc))
			_watchers.remove(npc);
		L2NpcTemplate template;
		if(npc.getTemplate().npcId == 18602)
		{
			NpcTable.getInstance();
			template = NpcTable.getTemplate(18601);
		}
		else
		{
			NpcTable.getInstance();
			template = NpcTable.getTemplate(18602);
		}
		L2WatcherInstance newWatcher = new L2WatcherInstance(IdFactory.getInstance().getNextId(), template);
		newWatcher.setCurrentHpMp(newWatcher.getMaxHp(), newWatcher.getMaxMp());
		newWatcher.spawnMe(new Location(npc.getX(), npc.getY(), -8357));
		newWatcher.setReflection(npc.getReflection());
		_watchers.add(newWatcher);
		npc.deleteMe();
	}

	private void distributeRooms(FastList<EventPlayer> par)
	{
		int row[] = {5, 5, 5, 5, 5};
		for(EventPlayer ep : par)
		{
			int randCol = Rnd.get(0, 4);
			boolean ok = false;
			while( !ok)
			{
				if(row[randCol] > 0)
				{
					ep._room = randCol * 5 + row[randCol];
					ep._roomX = _roomX[randCol];
					ep._roomY = _roomY[row[randCol] - 1];
					row[randCol]--;
					ok = true;
				}
				else
				{
					if(randCol == 4)
						randCol = 0;
					else
						randCol++;
				}
			}
		}
	}

	private void finishEvent()
	{
		if(_instance70Id > 0)
		{
			calcReward(_ingamePlayers70);
			ejectPlayers(_ingamePlayers70);
			_ingamePlayers70.clear();
		}
		if(_instance76Id > 0)
		{
			calcReward(_ingamePlayers76);
			ejectPlayers(_ingamePlayers76);
			_ingamePlayers76.clear();
		}
		if(_instance80Id > 0)
		{
			calcReward(_ingamePlayers80);
			ejectPlayers(_ingamePlayers80);
			_ingamePlayers80.clear();
		}
	}

	private void calcReward(FastList<EventPlayer> fl)
	{
		double dif = 0.05;
		int pos = 0;
		for(EventPlayer ep : fl)
		{
			L2Player pl = L2World.getPlayer(ep._player);
			if(pl != null)
			{
				pos++;
				int col = (int) ((ep._points * dif) * (1 + (fl.size() / pos * 0.04)));
				dif -= 0.0016;
				if(col > 0)
				{
					SystemMessage smsg = new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S2_S1S);
					smsg.addItemName(13067);
					smsg.addNumber(col);
					pl.sendPacket(smsg);
					pl.sendPacket(new ItemList(pl, false));
				}
			}
		}
	}

	private void sortPlayers(FastList<EventPlayer> fl)
	{
		FastList<EventPlayer> sort = new FastList<EventPlayer>();
		int max = -1;
		EventPlayer maxVal = null;
		for(int x = 1; x <= fl.size(); x++)
		{
			for(EventPlayer ep : fl)
			{
				if(ep._points > max)
				{
					max = ep._points;
					maxVal = ep;
				}
			}
			fl.remove(maxVal);
			sort.add(maxVal);
			max = -1;
			maxVal = null;
		}
		for(EventPlayer ep : sort)
			fl.add(ep);
	}

	public void exitFromEvent(L2Player pl)
	{
		if(pl == null)
			return;
		boolean ok = false;
		for(EventPlayer ep : _ingamePlayers70)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()))
			{
				ok = true;
				_ingamePlayers70.remove(ep);
			}
		}
		if( !ok)
			for(EventPlayer ep : _ingamePlayers76)
			{
				if(ep._player.equalsIgnoreCase(pl.getName()))
				{
					ok = true;
					_ingamePlayers76.remove(ep);
				}
			}
		if( !ok)
			for(EventPlayer ep : _ingamePlayers80)
			{
				if(ep._player.equalsIgnoreCase(pl.getName()))
				{
					ok = true;
					_ingamePlayers80.remove(ep);
				}
			}
	}

	private void skillsControl(L2Player pl)
	{
		if(pl == null)
			return;
		pl.getEffectList().stopAllEffects();
		L2Skill skill;
		for(int x = 0; x < _skillsId.length; x++)
		{
			skill = SkillTable.getInstance().getInfo(_skillsId[x], _skillsLvl[x]);
			if(skill != null)
				skill.getEffects(pl, pl, false, false);
		}
	}

	public int getCubeInstance(int par)
	{
		if(par == 70)
			return _instance70Id;
		if(par == 76)
			return _instance76Id;
		if(par == 80)
			return _instance80Id;
		return 0;
	}

	public void addRankListener(L2Player pl)
	{
		for(EventPlayer ep : _ingamePlayers70)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()) && ep._room > 0)
			{
				ep._rankListen = true;
				refreshRankListener(_ingamePlayers70);
			}
		}
		for(EventPlayer ep : _ingamePlayers76)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()) && ep._room > 0)
			{
				ep._rankListen = true;
				refreshRankListener(_ingamePlayers76);
			}
		}
		for(EventPlayer ep : _ingamePlayers80)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()) && ep._room > 0)
			{
				ep._rankListen = true;
				refreshRankListener(_ingamePlayers80);
			}
		}
	}

	public void removeRankListener(L2Player pl)
	{
		for(EventPlayer ep : _ingamePlayers70)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()) && ep._room > 0)
				ep._rankListen = false;
		}
		for(EventPlayer ep : _ingamePlayers76)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()) && ep._room > 0)
				ep._rankListen = false;
		}
		for(EventPlayer ep : _ingamePlayers80)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()) && ep._room > 0)
				ep._rankListen = false;
		}
	}

	public boolean returnToCube(L2Player pl)
	{
		if(pl == null || !_isRegPeriod)
			return false;
		boolean ok = false;
		for(EventPlayer ep : _ingamePlayers70)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()) && ep._room > 0)
			{
				ok = true;
				int xDif = Rnd.get( -300, 300);
				int yDif = Rnd.get( -300, 300);
				pl.teleToLocation(ep._roomX + xDif, ep._roomY + yDif, -8357);
				skillsControl(pl);
			}
		}
		if( !ok)
			for(EventPlayer ep : _ingamePlayers76)
			{
				if(ep._player.equalsIgnoreCase(pl.getName()) && ep._room > 0)
				{
					ok = true;
					int xDif = Rnd.get( -300, 300);
					int yDif = Rnd.get( -300, 300);
					pl.teleToLocation(ep._roomX + xDif, ep._roomY + yDif, -8357);
					skillsControl(pl);
				}
			}
		if( !ok)
			for(EventPlayer ep : _ingamePlayers80)
			{
				if(ep._player.equalsIgnoreCase(pl.getName()) && ep._room > 0)
				{
					ok = true;
					int xDif = Rnd.get( -300, 300);
					int yDif = Rnd.get( -300, 300);
					pl.teleToLocation(ep._roomX + xDif, ep._roomY + yDif, -8357);
					skillsControl(pl);
				}
			}
		return ok;
	}

	private void firstStep()
	{
		for(EventPlayer ep : _regPlayers70)
		{
			L2Player pl = L2World.getPlayer(ep._player);
			if(pl != null)
				if(pl.getDistance(_npcRegistrator) < 800 && (pl.getLevel() >= 70 && pl.getLevel() <= 75))
					if(_ingamePlayers70.size() < 25)
						_ingamePlayers70.add(ep);
		}
		for(EventPlayer ep : _regPlayers76)
		{
			L2Player pl = L2World.getPlayer(ep._player);
			if(pl != null)
				if(pl.getDistance(_npcRegistrator) < 800 && (pl.getLevel() >= 76 && pl.getLevel() <= 79))
					if(_ingamePlayers76.size() < 25)
						_ingamePlayers76.add(ep);
		}
		for(EventPlayer ep : _regPlayers80)
		{
			L2Player pl = L2World.getPlayer(ep._player);
			if(pl != null)
				if(pl.getDistance(_npcRegistrator) < 800 && pl.getLevel() >= 80)
					if(_ingamePlayers80.size() < 25)
						_ingamePlayers80.add(ep);
		}
		_regPlayers70 = new FastList<EventPlayer>();
		_regPlayers76 = new FastList<EventPlayer>();
		_regPlayers80 = new FastList<EventPlayer>();

		if(_ingamePlayers70.size() > 0)
			teleportPlayers(_instance70Id, _ingamePlayers70);
		if(_ingamePlayers76.size() > 0)
			teleportPlayers(_instance76Id, _ingamePlayers76);
		if(_ingamePlayers80.size() > 0)
			teleportPlayers(_instance80Id, _ingamePlayers80);
	}

	private void doorControl(int refId, int lvl)
	{
		Reflection inst = ReflectionTable.getInstance().get(refId);
		if(inst != null)
		{
			boolean newRoom = true;
			int cnt = 0;
			Rooms curRoom = null;
			for(L2DoorInstance di : inst.getDoors())
			{
				if(newRoom)
				{
					curRoom = new Rooms();
					if(lvl == 70)
						_rooms70.add(curRoom);
					else if(lvl == 76)
						_rooms76.add(curRoom);
					else if(lvl == 80)
						_rooms80.add(curRoom);
					newRoom = false;
					cnt = 0;
				}
				else if(cnt == 3)
					newRoom = true;
				curRoom._doors.add(di);
				cnt++;
			}
		}
	}

	public void spawnWatchers()
	{
		for(L2WatcherInstance wi : _watchers)
			wi.deleteMe();
		if(_instance70Id > 0)
		{
			for(int x : _roomX)
				for(int y : _roomY)
				{
					L2NpcTemplate template;
					if(Rnd.nextBoolean())
					{
						NpcTable.getInstance();
						template = NpcTable.getTemplate(18601);
					}
					else
					{
						NpcTable.getInstance();
						template = NpcTable.getTemplate(18602);
					}
					L2WatcherInstance newWatcher = new L2WatcherInstance(IdFactory.getInstance().getNextId(), template);
					newWatcher.setCurrentHpMp(newWatcher.getMaxHp(), newWatcher.getMaxMp());
					newWatcher.spawnMe(new Location(x, y, -8357));
					newWatcher.setReflection(_instance70Id);
					_watchers.add(newWatcher);
				}
		}
		if(_instance76Id > 0)
		{
			for(int x : _roomX)
				for(int y : _roomY)
				{
					L2NpcTemplate template;
					if(Rnd.nextBoolean())
					{
						NpcTable.getInstance();
						template = NpcTable.getTemplate(18601);
					}
					else
					{
						NpcTable.getInstance();
						template = NpcTable.getTemplate(18602);
					}
					L2WatcherInstance newWatcher = new L2WatcherInstance(IdFactory.getInstance().getNextId(), template);
					newWatcher.setCurrentHpMp(newWatcher.getMaxHp(), newWatcher.getMaxMp());
					newWatcher.spawnMe(new Location(x, y, -8357));
					newWatcher.setReflection(_instance76Id);
					_watchers.add(newWatcher);
				}
		}
		if(_instance80Id > 0)
		{
			for(int x : _roomX)
				for(int y : _roomY)
				{
					L2NpcTemplate template;
					if(Rnd.nextBoolean())
					{
						NpcTable.getInstance();
						template = NpcTable.getTemplate(18601);
					}
					else
					{
						NpcTable.getInstance();
						template = NpcTable.getTemplate(18602);
					}
					L2WatcherInstance newWatcher = new L2WatcherInstance(IdFactory.getInstance().getNextId(), template);
					newWatcher.setCurrentHpMp(newWatcher.getMaxHp(), newWatcher.getMaxMp());
					newWatcher.spawnMe(new Location(x, y, -8357));
					newWatcher.setReflection(_instance80Id);
					_watchers.add(newWatcher);
				}
		}
	}

	private void teleportPlayers(int instanceId, FastList<EventPlayer> fl)
	{
		for(EventPlayer ep : fl)
		{
			L2Player pl = L2World.getPlayer(ep._player);
			if(pl != null)
			{
				int xDif = Rnd.get( -300, 300);
				int yDif = Rnd.get(0, 600);
				pl.setReflection(instanceId);
				pl.setInKrateisCube(true);
				pl.teleToLocation(_managerCoord[0] + xDif, _managerCoord[1] + yDif, -8357);
			}
		}
	}

	private void teleportInCube(FastList<EventPlayer> fl)
	{
		for(EventPlayer ep : fl)
		{
			L2Player pl = L2World.getPlayer(ep._player);
			if(pl != null)
			{
				int xDif = Rnd.get( -300, 300);
				int yDif = Rnd.get( -300, 300);
				pl.sendPacket(new ExShowCubeTimer(0));
				pl.teleToLocation(ep._roomX + xDif, ep._roomY + yDif, -8357);
				skillsControl(pl);
			}
		}
	}

	private void ejectPlayers(FastList<EventPlayer> fl)
	{
		for(EventPlayer ep : fl)
		{
			L2Player pl = L2World.getPlayer(ep._player);
			if(pl != null)
			{
				pl.sendPacket(new ExCubeTimerStop());
				pl.teleToLocation( -70293, -71029, -1416);
				pl.setReflection(0);
			}
		}
	}

	public void ejectPlayer(L2Player pl)
	{
		pl.sendPacket(new ExCubeTimerStop());
		pl.teleToLocation( -70293, -71029, -1416);
		pl.setReflection(0);
		exitFromEvent(pl);
	}

	public synchronized void addPoint(L2Player pl, boolean isMob)
	{
		int cubLvl = 80;
		if(pl.getLevel() < 80)
			cubLvl = 76;
		if(pl.getLevel() < 76)
			cubLvl = 70;
		FastList<EventPlayer> ingamePlayers = new FastList<EventPlayer>();
		if(cubLvl == 70)
			ingamePlayers = _ingamePlayers70;
		if(cubLvl == 76)
			ingamePlayers = _ingamePlayers76;
		if(cubLvl == 80)
			ingamePlayers = _ingamePlayers80;
		for(EventPlayer ep : ingamePlayers)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()))
			{
				if(isMob)
				{
					ep._points += 3;
					pl.sendPacket(new ExShowCubeTimer(ep._points));
				}
				else
				{
					ep._points += 5;
					pl.sendPacket(new ExShowCubeTimer(ep._points));
				}
				sortPlayers(ingamePlayers);
				refreshRankListener(ingamePlayers);
				return;
			}
		}
	}

	private void refreshRankListener(FastList<EventPlayer> ingamePlayers)
	{
		for(EventPlayer ep : ingamePlayers)
		{
			if(ep._rankListen)
			{
				L2Player pl = L2World.getPlayer(ep._player);
				if(pl != null)
					pl.sendPacket(new ExShowCubeInfo(ingamePlayers));
			}
		}
	}

	public synchronized boolean registerOnEvent(L2Player pl, int par)
	{
		if(pl == null || !_isRegPeriod)
			return false;
		if(par == 70 && (pl.getLevel() >= 70 && pl.getLevel() <= 75))
		{
			if( !isRegister(pl))
			{
				EventPlayer ep = new EventPlayer();
				ep._player = pl.getName();
				_regPlayers70.add(ep);
				return true;
			}
		}
		if(par == 76 && (pl.getLevel() >= 76 && pl.getLevel() <= 79))
		{
			if( !isRegister(pl))
			{
				EventPlayer ep = new EventPlayer();
				ep._player = pl.getName();
				_regPlayers76.add(ep);
				return true;
			}
		}
		if(par == 80 && pl.getLevel() >= 80)
		{
			if( !isRegister(pl))
			{
				EventPlayer ep = new EventPlayer();
				ep._player = pl.getName();
				_regPlayers80.add(ep);
				return true;
			}
		}
		return false;
	}

	public synchronized void unRegisterOnEvent(L2Player pl)
	{
		if(pl == null || !_isRegPeriod)
			return;
		for(EventPlayer ep : _regPlayers70)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()))
				_regPlayers70.remove(ep);
		}
		for(EventPlayer ep : _regPlayers76)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()))
				_regPlayers76.remove(ep);
		}
		for(EventPlayer ep : _regPlayers80)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()))
				_regPlayers80.remove(ep);
		}
	}

	public boolean isRegister(L2Player pl)
	{
		boolean ch = false;
		for(EventPlayer ep : _regPlayers70)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()))
				ch = true;
		}
		for(EventPlayer ep : _regPlayers76)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()))
				ch = true;
		}
		for(EventPlayer ep : _regPlayers80)
		{
			if(ep._player.equalsIgnoreCase(pl.getName()))
				ch = true;
		}
		return ch;
	}

	private void addTime(int par)
	{
		Calendar tmpDate = Calendar.getInstance();
		tmpDate.setTimeInMillis(_anonceTimer);
		tmpDate.add(Calendar.MINUTE, par);
		_anonceTimer = tmpDate.getTimeInMillis();
	}

	private void anonce(String str)
	{
		_npcRegistrator.broadcastPacket(new NpcSay(_npcRegistrator, 0, "Hello:)"));
	}

	private void prepareEvent()
	{
		_eventActive = true;
		NpcTable.getInstance();
		L2NpcTemplate template = NpcTable.getTemplate(32503);
		_npcRegistrator = new L2CubeNpcInstance(IdFactory.getInstance().getNextId(), template);
		_npcRegistrator.setCurrentHpMp(_npcRegistrator.getMaxHp(), _npcRegistrator.getMaxMp());
		_npcRegistrator.spawnMe(new Location(_registratorCoord[0], _registratorCoord[1], _registratorCoord[2]));
	}

	private final ExclusiveTask _anonceTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if(_anonceTimer == 0)
			{
				Calendar tmpDate = Calendar.getInstance();
				tmpDate.setTimeInMillis(System.currentTimeMillis());
				tmpDate.add(Calendar.HOUR, 1);
				tmpDate.set(Calendar.MINUTE, 0);
				tmpDate.set(Calendar.SECOND, 0);
				_anonceTimer = tmpDate.getTimeInMillis();
				schedule(_anonceTimer - System.currentTimeMillis());
				return;
			}
			if( !_eventActive)
				prepareEvent();
			if(_anonceStep == 0)
				startEvent();
			if(_anonceStep < 4)
			{
				_isRegPeriod = true;
				if(_half)
					anonce("Регистрация доступна до 27 мин.");
				else
					anonce("Регистрация доступна до 57 мин.");
				addTime(5);
				_anonceStep++;
			}
			else if(_anonceStep == 4)
			{
				anonce("Соревнования начнутся через 10 мин.");
				addTime(2);
				_anonceStep++;
				finishEvent();
			}
			else if(_anonceStep == 5)
			{
				anonce("Регистрация на соревнования Куба Кратеи закончится через 5 мин.");
				addTime(2);
				_anonceStep++;
			}
			else if(_anonceStep == 6)
			{
				anonce("Регистрация на соревнования Куба Кратеи закончится через 3 мин.");
				addTime(1);
				_anonceStep++;
			}
			else if(_anonceStep == 7)
			{
				anonce("Соревнования начнутся через 5 мин.");
				addTime(1);
				_anonceStep++;
			}
			else if(_anonceStep == 8)
			{
				anonce("Регистрация на соревнования Куба Кратеи закончится через 1 мин.");
				addTime(1);
				_anonceStep++;
			}
			else if(_anonceStep == 9)
			{
				_isRegPeriod = false;
				anonce("Сейчас начнутся соревнования.");
				addTime(3);
				if(_half)
					_half = false;
				else
					_half = true;
				_anonceStep = 0;
				firstStep();
				_secToEvent = 30;
				_countTask.schedule(150000);
			}
			schedule(_anonceTimer - System.currentTimeMillis());
		}
	};

	private final ExclusiveTask _countTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			for(EventPlayer ep : _ingamePlayers70)
			{
				L2Player pl = L2World.getPlayer(ep._player);
				if(pl != null)
					pl.sendPacket(new ExShowScreenMessage(" " + _secToEvent + " сек. до начала матча!", 800));
			}
			for(EventPlayer ep : _ingamePlayers76)
			{
				L2Player pl = L2World.getPlayer(ep._player);
				if(pl != null)
					pl.sendPacket(new ExShowScreenMessage(" " + _secToEvent + " сек. до начала матча!", 800));
			}
			for(EventPlayer ep : _ingamePlayers80)
			{
				L2Player pl = L2World.getPlayer(ep._player);
				if(pl != null)
					pl.sendPacket(new ExShowScreenMessage(" " + _secToEvent + " сек. до начала матча!", 800));
			}

			_secToEvent--;
			if(_secToEvent == 0)
			{
				cancel();
				return;
			}
			schedule(1000);
		}
	};

	private final ExclusiveTask _doorControlTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			for(Rooms room : _rooms70)
				openDoor(room);
			for(Rooms room : _rooms76)
				openDoor(room);
			for(Rooms room : _rooms80)
				openDoor(room);
			schedule(10000);
		}
	};

	private void openDoor(Rooms room)
	{
		int rndPair = Rnd.get(0, 1);
		rndPair *= 2;
		if(room._doors.size() == 4)
		{
			if( !room._doors.get(rndPair).isOpen())
			{
				room._doors.get(rndPair).openMe();
				room._doors.get(rndPair + 1).openMe();
				if(rndPair == 0)
				{
					room._doors.get(2).closeMe();
					room._doors.get(3).closeMe();
				}
				else
				{
					room._doors.get(0).closeMe();
					room._doors.get(1).closeMe();
				}
			}
		}
	}

	public class EventPlayer
	{
		public String _player;
		public int _points = 0;
		public boolean _rankListen = false;
		int _room = 0;
		int _roomX;
		int _roomY;
	}

	private class Rooms
	{
		FastList<L2DoorInstance> _doors = new FastList<L2DoorInstance>();
	}
}