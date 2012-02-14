package events.arena;

import java.util.HashMap;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.listeners.L2ZoneEnterLeaveListener;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.network.clientpackets.Say2C;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.Say2;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;
import l2rt.util.GCSArray;
import l2rt.util.Location;
import l2rt.util.Util;

public abstract class ArenaTemplate extends Functions implements ScriptFile
{
	// Эти переменные выставляются автоматически при вызове скрипта
	protected int _managerId;
	protected String _className;

	protected Long _creatorId;
	protected L2NpcInstance _manager;
	protected int _status = 0;
	protected int _battleType = 1;
	protected int _team1exp = 0;
	protected int _team2exp = 0;
	protected int _price = 10000;
	protected int _team1count = 1;
	protected int _team2count = 1;
	protected int _team1min = 1;
	protected int _team1max = 85;
	protected int _team2min = 1;
	protected int _team2max = 85;
	protected int _timeToStart = 10;
	protected boolean _timeOutTask;

	protected GArray<Location> _team1points;
	protected GArray<Location> _team2points;

	protected GCSArray<Long> _team1list = new GCSArray<Long>();
	protected GCSArray<Long> _team2list = new GCSArray<Long>();
	protected GCSArray<Long> _team1live = new GCSArray<Long>();
	protected GCSArray<Long> _team2live = new GCSArray<Long>();

	protected HashMap<Integer, Integer> _expToReturn = new HashMap<Integer, Integer>();
	protected HashMap<Integer, Integer> _classToReturn = new HashMap<Integer, Integer>();

	protected L2Zone _zone;
	protected ZoneListener _zoneListener;

	public void template_stop()
	{
		say("Бой прерван по техническим причинам, ставки возвращены");
		if(_battleType == 1)
			returnAdenaToTeams();
		else if(_battleType == 2)
			returnExpToTeams();
		unParalyzeTeams();
		clearTeams();
		_status = 0;
		_timeOutTask = false;
	}

	public void template_create1(L2Player player)
	{
		if(_status > 0)
			show("Дождитесь окончания боя", player);
		else
			show("data/scripts/events/arena/" + _managerId + "-1.html", player);
	}

	public void template_create2(L2Player player)
	{
		if(_status > 0)
			show("Дождитесь окончания боя", player);
		else
			show("data/scripts/events/arena/" + _managerId + "-2.html", player);
	}

	public void template_register(L2Player player)
	{
		if(_status > 1)
			show("Дождитесь окончания боя", player);
		else
			show("data/scripts/events/arena/" + _managerId + "-3.html", player);
	}

	public void template_check1(L2Player player, L2NpcInstance manager, String[] var)
	{
		if(player.isDead())
			return;

		if(var.length != 8)
		{
			show("Некорректные данные", player);
			return;
		}

		if(_status > 0)
		{
			show("Дождитесь окончания боя", player);
			return;
		}
		if(manager == null || !manager.isNpc())
		{
			show("Hacker? :) " + manager, player);
			return;
		}
		_manager = manager;
		try
		{
			_price = Integer.valueOf(var[0]);
			_team1count = Integer.valueOf(var[1]);
			_team2count = Integer.valueOf(var[2]);
			_team1min = Integer.valueOf(var[3]);
			_team1max = Integer.valueOf(var[4]);
			_team2min = Integer.valueOf(var[5]);
			_team2max = Integer.valueOf(var[6]);
			_timeToStart = Integer.valueOf(var[7]);
		}
		catch(Exception e)
		{
			show("Некорректные данные", player);
			return;
		}
		if(_price < 10000 || _price > 100000000)
		{
			show("Неправильная ставка", player);
			return;
		}
		if(_team1count < 1 || _team1count > 5 || _team2count < 1 || _team2count > 5)
		{
			show("Неправильный размер команды", player);
			return;
		}
		if(_team1min < 1 || _team1min > 85 || _team2min < 1 || _team2min > 85 || _team1max < 1 || _team1max > 85 || _team2max < 1 || _team2max > 85 || _team1min > _team1max || _team2min > _team2max)
		{
			show("Неправильный уровень", player);
			return;
		}
		if(player.getLevel() < _team1min || player.getLevel() > _team1max)
		{
			show("Неправильный уровень", player);
			return;
		}
		if(_timeToStart < 1 || _timeToStart > 10)
		{
			show("Неправильное время", player);
			return;
		}
		if(player.getAdena() < _price)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		_battleType = 1;
		_creatorId = player.getStoredId();
		player.reduceAdena(_price, true);
		_status = 1;
		_team1list.clear();
		_team2list.clear();
		_team1live.clear();
		_team2live.clear();
		_team1list.add(player.getStoredId());
		say(player.getName() + " создал бой " + _team1count + "х" + _team2count + ", " + _team1min + "-" + _team1max + "lv vs " + _team2min + "-" + _team2max + "lv, ставка " + _price + "а, начало через " + _timeToStart + " мин");
		executeTask("events.arena." + _className, "announce", new Object[0], 60000);
	}

	public void template_check2(L2Player player, L2NpcInstance manager, String[] var)
	{
		if(!Config.ALT_ARENA_EXP)
		{
			show("Эта опция недоступна", player);
			return;
		}

		if(player.isDead())
			return;

		if(var.length != 7)
		{
			show("Некорректные данные", player);
			return;
		}

		if(_status > 0)
		{
			show("Дождитесь окончания боя", player);
			return;
		}
		if(manager == null || !manager.isNpc())
		{
			show("Hacker? :) " + manager, player);
			return;
		}
		_manager = manager;
		try
		{
			_team1count = Integer.valueOf(var[0]);
			_team2count = Integer.valueOf(var[1]);
			_team1min = Integer.valueOf(var[2]);
			_team1max = Integer.valueOf(var[3]);
			_team2min = Integer.valueOf(var[4]);
			_team2max = Integer.valueOf(var[5]);
			_timeToStart = Integer.valueOf(var[6]);
		}
		catch(Exception e)
		{
			show("Некорректные данные", player);
			return;
		}
		if(_team1count < 1 || _team1count > 5 || _team2count < 1 || _team2count > 5)
		{
			show("Неправильный размер команды", player);
			return;
		}
		if(_team1min < 1 || _team1min > 82 || _team2min < 1 || _team2min > 82 || _team1max < 1 || _team1max > 82 || _team2max < 1 || _team2max > 82 || _team1min > _team1max || _team2min > _team2max)
		{
			show("Неправильный уровень", player);
			return;
		}
		if(player.getLevel() - _team1min > 10 || _team1max - player.getLevel() > 10 || player.getLevel() - _team2min > 10 || _team2max - player.getLevel() > 10)
		{
			show("Разница в уровнях не может быть более 10", player);
			return;
		}
		if(player.getLevel() < _team1min || player.getLevel() > _team1max)
		{
			show("Неправильный уровень", player);
			return;
		}
		if(_timeToStart < 1 || _timeToStart > 10)
		{
			show("Неправильное время", player);
			return;
		}

		_battleType = 2;
		_creatorId = player.getStoredId();
		_team1exp = 0;
		_team2exp = 0;
		_expToReturn.clear();
		_classToReturn.clear();

		removeExp(player, 1);

		_status = 1;
		_team1list.clear();
		_team2list.clear();
		_team1live.clear();
		_team2live.clear();
		_team1list.add(player.getStoredId());
		say(player.getName() + " создал бой " + _team1count + "х" + _team2count + ", " + _team1min + "-" + _team1max + "lv vs " + _team2min + "-" + _team2max + "lv, ставка " + "опыт, начало через " + _timeToStart + " мин");
		executeTask("events.arena." + _className, "announce", new Object[0], 60000);
	}

	public void template_register_check(L2Player player, String[] var)
	{
		if(player.isDead())
			return;

		if(_status > 1)
		{
			show("Дождитесь окончания боя", player);
			return;
		}

		if(var.length != 1)
		{
			show("Некорректные данные", player);
			return;
		}

		int _regTeam;
		try
		{
			_regTeam = Integer.valueOf(var[0]);
		}
		catch(Exception e)
		{
			show("Некорректные данные", player);
			return;
		}

		if(_regTeam != 1 && _regTeam != 2)
		{
			show("Неправильный номер команды, введите 1 или 2", player);
			return;
		}
		if(_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId()))
		{
			show("Вы уже зарегистрированы", player);
			return;
		}
		if(_battleType == 1 && player.getAdena() < _price)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		if(_regTeam == 1)
		{
			if(player.getLevel() < _team1min || player.getLevel() > _team1max)
			{
				show("Вы не подходите по уровню", player);
				return;
			}
			if(_team1list.size() >= _team1count)
			{
				show("Команда 1 переполнена", player);
				return;
			}

			if(_battleType == 1)
				player.reduceAdena(_price, true);
			else if(_battleType == 2)
				removeExp(player, 1);

			_team1list.add(player.getStoredId());
			say(player.getName() + " зарегистрировался за 1 команду");
			if(_team1list.size() >= _team1count && _team2list.size() >= _team2count)
			{
				say("Команды готовы, старт через 1 минуту.");
				_timeToStart = 1;
			}
		}
		else
		{
			if(player.getLevel() < _team2min || player.getLevel() > _team2max)
			{
				show("Вы не подходите по уровню", player);
				return;
			}
			if(_team2list.size() >= _team2count)
			{
				show("Команда 2 переполнена", player);
				return;
			}

			if(_battleType == 1)
				player.reduceAdena(_price, true);
			else if(_battleType == 2)
				removeExp(player, 2);

			_team2list.add(player.getStoredId());
			say(player.getName() + " зарегистрировался за 2 команду");
			if(_team1list.size() >= _team1count && _team2list.size() >= _team2count)
			{
				say("Команды готовы, старт через 1 минуту.");
				_timeToStart = 1;
			}
		}
	}

	public void template_announce()
	{
		L2Player creator = L2ObjectsStorage.getAsPlayer(_creatorId);

		if(_status != 1 || creator == null)
			return;

		if(_timeToStart > 1)
		{
			_timeToStart--;
			say(creator.getName() + " создал бой " + _team1count + "х" + _team2count + ", " + _team1min + "-" + _team1max + "lv vs " + _team2min + "-" + _team2max + "lv, ставка " + (_battleType == 1 ? _price + "а" : "опыт") + ", начало через " + _timeToStart + " мин");
			executeTask("events.arena." + _className, "announce", new Object[0], 60000);
		}
		else if(_team2list.size() > 0)
		{
			say("Подготовка к бою");
			executeTask("events.arena." + _className, "prepare", new Object[0], 5000);
		}
		else
		{
			say("Бой не состоялся, нет противников");
			_status = 0;
			if(_battleType == 1)
				returnAdenaToTeams();
			else if(_battleType == 2)
				returnExpToTeams();
			clearTeams();
		}
	}

	public void template_prepare()
	{
		if(_status != 1)
			return;

		_status = 2;
		for(L2Player player : getPlayers(_team1list))
			if(!player.isDead())
				_team1live.add(player.getStoredId());
		for(L2Player player : getPlayers(_team2list))
			if(!player.isDead())
				_team2live.add(player.getStoredId());
		if(!checkTeams())
			return;
		clearArena();
		paralyzeTeams();
		teleportTeamsToArena();
		say("Бой начнется через 15 секунд");
		executeTask("events.arena." + _className, "start", new Object[0], 15000);
	}

	public void template_start()
	{
		if(_status != 2)
			return;

		if(!checkTeams())
			return;
		say("Go!!!");
		unParalyzeTeams();
		_status = 3;
		executeTask("events.arena." + _className, "timeOut", new Object[0], 180000);
		_timeOutTask = true;
	}

	public void clearArena()
	{
		for(L2Player player : L2World.getAroundPlayers(_manager, 2000, 200))
			if(player.isInZone(ZoneType.battle_zone))
				player.teleToLocation(_zone.getSpawn());
	}

	public boolean checkTeams()
	{
		if(_team1live.isEmpty())
		{
			teamHasLost(1);
			return false;
		}
		else if(_team2live.isEmpty())
		{
			teamHasLost(2);
			return false;
		}
		return true;
	}

	public void paralyzeTeams()
	{
		L2Skill revengeSkill = SkillTable.getInstance().getInfo(L2Skill.SKILL_RAID_CURSE, 1);
		for(L2Player player : getPlayers(_team1live))
		{
			player.getEffectList().stopEffect(L2Skill.SKILL_MYSTIC_IMMUNITY);
			revengeSkill.getEffects(player, player, false, false);
			if(player.getPet() != null)
				revengeSkill.getEffects(player, player.getPet(), false, false);
		}
		for(L2Player player : getPlayers(_team2live))
		{
			player.getEffectList().stopEffect(L2Skill.SKILL_MYSTIC_IMMUNITY);
			revengeSkill.getEffects(player, player, false, false);
			if(player.getPet() != null)
				revengeSkill.getEffects(player, player.getPet(), false, false);
		}
	}

	public void unParalyzeTeams()
	{
		for(L2Player player : getPlayers(_team1list))
		{
			player.getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);
			if(player.getPet() != null)
				player.getPet().getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);
		}
		for(L2Player player : getPlayers(_team2list))
		{
			player.getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);
			if(player.getPet() != null)
				player.getPet().getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);
		}
	}

	public void teleportTeamsToArena()
	{
		Integer n = 0;
		for(L2Player player : getPlayers(_team1live))
		{
			player.teleToLocation(_team1points.get(n));
			if(player.getPet() != null)
				player.getPet().teleToLocation(_team1points.get(n));
			player.setTeam(1, true);
			n++;
		}
		n = 0;
		for(L2Player player : getPlayers(_team2live))
		{
			player.teleToLocation(_team2points.get(n));
			if(player.getPet() != null)
				player.getPet().teleToLocation(_team2points.get(n));
			player.setTeam(2, true);
			n++;
		}
	}

	public boolean playerHasLost(L2Player player)
	{
		_team1live.remove(player.getStoredId());
		_team2live.remove(player.getStoredId());
		L2Skill revengeSkill = SkillTable.getInstance().getInfo(L2Skill.SKILL_RAID_CURSE, 1);
		player.getEffectList().stopEffect(L2Skill.SKILL_MYSTIC_IMMUNITY);
		revengeSkill.getEffects(player, player, false, false);
		return !checkTeams();
	}

	public void teamHasLost(Integer team_id)
	{
		if(team_id == 1)
		{
			say("Команда 2 победила");
			if(_battleType == 1)
				payAdenaToTeam(2);
			else if(_battleType == 2)
				payExpToTeam(2);
		}
		else
		{
			say("Команда 1 победила");
			if(_battleType == 1)
				payAdenaToTeam(1);
			else if(_battleType == 2)
				payExpToTeam(1);
		}
		unParalyzeTeams();
		clearTeams();
		_status = 0;
		_timeOutTask = false;
	}

	public void template_timeOut()
	{
		if(_timeOutTask && _status == 3)
		{
			say("Время истекло, ничья!");
			if(_battleType == 1)
				returnAdenaToTeams();
			else if(_battleType == 2)
				returnExpToTeams();
			unParalyzeTeams();
			clearTeams();
			_status = 0;
			_timeOutTask = false;
		}
	}

	public void payAdenaToTeam(Integer team_id)
	{
		if(team_id == 1)
			for(L2Player player : getPlayers(_team1list))
				player.addAdena(_price + _team2list.size() * _price / _team1list.size());
		else
			for(L2Player player : getPlayers(_team2list))
				player.addAdena(_price + _team1list.size() * _price / _team2list.size());
	}

	public void payExpToTeam(Integer team_id)
	{
		if(team_id == 1)
			for(L2Player player : getPlayers(_team1list))
			{
				returnExp(player);
				addExp(player, _team2exp / _team1list.size() / 2);
			}
		else
			for(L2Player player : getPlayers(_team2list))
			{
				returnExp(player);
				addExp(player, _team1exp / _team2list.size() / 2);
			}
	}

	public void returnAdenaToTeams()
	{
		for(L2Player player : getPlayers(_team1list))
			player.addAdena(_price);
		for(L2Player player : getPlayers(_team2list))
			player.addAdena(_price);
	}

	public void returnExpToTeams()
	{
		for(L2Player player : getPlayers(_team1list))
			returnExp(player);
		for(L2Player player : getPlayers(_team2list))
			returnExp(player);
	}

	public void clearTeams()
	{
		for(L2Player player : getPlayers(_team1list))
			player.setTeam(0, true);
		for(L2Player player : getPlayers(_team2list))
			player.setTeam(0, true);
		_team1list.clear();
		_team2list.clear();
		_team1live.clear();
		_team2live.clear();
	}

	public void removeExp(L2Player player, int team)
	{
		int lostExp = Math.round((Experience.LEVEL[player.getLevel() + 1] - Experience.LEVEL[player.getLevel()]) * 4 / 100);
		player.addExpAndSp(-1 * lostExp, 0, false, false);
		_expToReturn.put(player.getObjectId(), lostExp);
		_classToReturn.put(player.getObjectId(), player.getActiveClassId());

		if(team == 1)
			_team1exp += lostExp;
		else if(team == 2)
			_team2exp += lostExp;
	}

	public void returnExp(L2Player player)
	{
		int addExp = _expToReturn.get(player.getObjectId());
		int classId = _classToReturn.get(player.getObjectId());
		if(addExp > 0 && player.getActiveClassId() == classId)
			player.addExpAndSp(addExp, 0, false, false);
	}

	public void addExp(L2Player player, int exp)
	{
		int classId = _classToReturn.get(player.getObjectId());
		if(player.getActiveClassId() == classId)
			player.addExpAndSp(exp, 0, false, false);
	}

	/**
	 * Это не хендлер, хендлеры находятся в наследниках 
	 */
	public void onDie(L2Object self, L2Character killer)
	{
		if(_status >= 2 && self != null && self.isPlayer() && (_team1list.contains(self.getStoredId()) || _team2list.contains(self.getStoredId())))
		{
			L2Player player = self.getPlayer();
			L2Player kplayer = killer.getPlayer();
			if(kplayer != null)
			{
				say(kplayer.getName() + " убил " + player.getName());
				if(player.getTeam() == kplayer.getTeam() || !_team1list.contains(kplayer.getStoredId()) && !_team2list.contains(kplayer.getStoredId()))
				{
					say("Нарушение правил, игрок " + kplayer.getName() + " оштрафован на " + _price);
					kplayer.reduceAdena(_price, true);
				}
				playerHasLost(player);
			}
			else
			{
				say(player.getName() + " убит");
				playerHasLost(player);
			}
		}
	}

	/**
	 * Это не хендлер, хендлеры находятся в наследниках 
	 */
	public void onPlayerExit(L2Player player)
	{
		if(player != null && _status > 0 && (_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId())))
			switch(_status)
			{
				case 1:
					removePlayer(player);
					say(player.getName() + " дисквалифицирован");
					if(player.getStoredId() == _creatorId)
					{
						say("Бой прерван, ставки возвращены");
						if(_battleType == 1)
							returnAdenaToTeams();
						else if(_battleType == 2)
							returnExpToTeams();
						unParalyzeTeams();
						clearTeams();
						_status = 0;
						_timeOutTask = false;
					}
					break;
				case 2:
					removePlayer(player);
					say(player.getName() + " дисквалифицирован");
					checkTeams();
					break;
				case 3:
					removePlayer(player);
					say(player.getName() + " дисквалифицирован");
					checkTeams();
					break;
			}
	}

	/**
	 * Это не хендлер, хендлеры находятся в наследниках 
	 */
	public Location onEscape(L2Player player)
	{
		if(player != null && _status > 1 && player.isInZone(_zone))
			onPlayerExit(player);
		return null;
	}

	public class ZoneListener extends L2ZoneEnterLeaveListener
	{
		@Override
		public void objectEntered(L2Zone zone, L2Object object)
		{
			L2Player player = object.getPlayer();
			if(_status >= 2 && player != null && !(_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId())))
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask((L2Character) object, _zone.getSpawn()), 3000);
		}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{
			L2Player player = object.getPlayer();
			if(_status >= 2 && player != null && (_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId())))
			{
				double angle = Util.convertHeadingToDegree(object.getHeading()); // угол в градусах
				double radian = Math.toRadians(angle - 90); // угол в радианах
				int x = (int) (object.getX() + 50 * Math.sin(radian));
				int y = (int) (object.getY() - 50 * Math.cos(radian));
				int z = object.getZ();
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask((L2Character) object, new Location(x, y, z)), 3000);
			}
		}
	}

	public class TeleportTask implements Runnable
	{
		Location loc;
		L2Character target;

		public TeleportTask(L2Character target, Location loc)
		{
			this.target = target;
			this.loc = loc;
			target.startStunning();
		}

		public void run()
		{
			target.stopStunning();
			target.teleToLocation(loc);
		}
	}

	private void removePlayer(L2Player player)
	{
		if(player != null)
		{
			_team1list.remove(player.getStoredId());
			_team2list.remove(player.getStoredId());
			_team1live.remove(player.getStoredId());
			_team2live.remove(player.getStoredId());
			player.setTeam(0, true);
		}
	}

	private GArray<L2Player> getPlayers(GCSArray<Long> list)
	{
		GArray<L2Player> result = new GArray<L2Player>();
		for(Long storeId : list)
		{
			L2Player player = L2ObjectsStorage.getAsPlayer(storeId);
			if(player != null)
				result.add(player);
		}
		return result;
	}

	public void say(String text)
	{
		if(_manager == null)
			return;
		Say2 cs = new Say2(0, Say2C.SHOUT, _manager.getName(), text);
		for(L2Player player : L2World.getAroundPlayers(_manager, 4000, 200))
			if(player != null && !player.isBlockAll())
				player.sendPacket(cs);
	}
}