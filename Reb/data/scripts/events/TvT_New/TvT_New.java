package events.TvT_New;

import java.util.HashMap;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2r.commons.util.Rnd;
import l2r.gameserver.Announcements;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.data.xml.holder.InstantZoneHolder;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.events.GameEvent;
import l2r.gameserver.model.entity.events.GameEventManager;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.instances.DoorInstance;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ChangeWaitType;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.Revive;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.skills.effects.EffectTemplate;
import l2r.gameserver.stats.Env;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.InstantZone;
import l2r.gameserver.utils.GArray;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Strings;

public class TvT_New extends GameEvent implements ScriptFile
{
	private int _state = 0;
	private static TvT_New _instance;
	private Logger _log = Logger.getLogger(TvT_New.class.getName());

	private FastMap<Player, Integer> _participants = new FastMap<Player, Integer>();

	private HashMap<Player, Integer> live_list = new HashMap<Player, Integer>();
	private int[] _score;
	private int curr_round = 1;

	public long startBattle = 0;
	private Reflection _ref;
	private InstantZone _instantZone;

	public TvT_New()
	{
		_instance = this;
	}

	public static TvT_New getInstance()
	{
		if (_instance == null)
			_instance = new TvT_New();
		return _instance;
	}

	public int getState()
	{
		return _state;
	}

	public String getName()
	{
		return "TvT_New";
	}

	public long getNextTime()
	{
		long next_time = getConfigs().START_TIME;

		while (next_time <= System.currentTimeMillis() / 1000)
		{
			getConfigs().START_TIME += 86400;
			setNextEvent();
			next_time = getConfigs().START_TIME;
		}

		return next_time;
	}

	public void setNextEvent()
	{
		if (TvTConfig._configs != null && TvTConfig._configs.size() > 1)
		{
			TvTConfig._configs.sort();
		}
	}

	public Configs getConfigs()
	{
		return TvTConfig._configs == null ? null : TvTConfig._configs.get(0);
	}

	@Override
	public boolean canUseItem(Player actor, ItemInstance item)
	{
		return _state == 0 || getConfigs().RESTRICT_ITEMS == null || getConfigs().RESTRICT_ITEMS.contains(item.getItemId());
	}

	@Override
	public void onLoad()
	{
		int instId = Rnd.get(147,150);
		TvTConfig.load();
		GameEventManager.getInstance().registerEvent(getInstance());
		_log.fine("Loaded Event: TvT_New");
		_ref = new Reflection();
		_instantZone = InstantZoneHolder.getInstance().getInstantZone(instId);
		_ref.init(_instantZone);
		_state = 0;
	}

	@Override
	public void onReload()
	{
		if (_ref != null)
			_ref.clearReflection(1, false);
		if (TvTConfig._configs.size() > 0)
			TvTConfig._configs.clear();
		if (_state != 0)
			finish();
		onLoad();
	}

	@Override
	public void onShutdown()
	{
		_state = 0;
	}

	@Override
	public boolean register(Player player)
	{
		if (!canRegister(player, true))
			return false;

		player.setPvPTeam(TeamWithMinPlayers());
		_participants.put(player, 0);
		player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.YouRegistred", player, new Object[0]));
		player._event = this;
		return true;
	}

	public void addPlayer()
	{
		registerPlayer();
	}

	public void registerPlayer()
	{
		Player player = getSelf();
		GameEvent event = GameEventManager.getInstance().findEvent("TvT_New");
		event.register(player);
	}

	@Override
	public void unreg(Player player)
	{
		if (player == null) return;

		if (_state == 2 || !isParticipant(player))
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.YouCancelRegistration", player, new Object[0]));
			return;
		}
		_participants.remove(player);
		player._event = null;
		player.setPvPTeam(0);
		player.allowPvPTeam();
		player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.YouRegistrationCanceled", player, new Object[0]));
	}

	@Override
	public void remove(Player player)
	{
		if (player == null)
			return;

		if (_participants.containsKey(player))
			_participants.remove(player);
		if (live_list.containsKey(player))
			live_list.remove(player);
		player._event = null;
		player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.YouDisqualified", player, new Object[0]));
	}

	public boolean canRegister(Player player, boolean first)
	{
		if (getConfigs().ALLOW_TAKE_ITEM)
		{
			long take_item_count = Functions.getItemCount(player, getConfigs().TAKE_ITEM_ID);
			String name_take_items = ItemHolder.getInstance().getTemplate(getConfigs().TAKE_ITEM_ID).getName();
			if(take_item_count > 0)
			{
				if((int)take_item_count < getConfigs().TAKE_COUNT)
				{
					player.sendMessage("Недостаточно" + name_take_items + "для участия.");
					return false;
				}
			}
			else
			{
				player.sendMessage("У Вас нет " + name_take_items + ", требуется для участия.");
				return false;
			}
		}
		if (first && _state != 1)
		{
			player.sendMessage("Процесс регистрации не активен.");
			return false;
		}
		if (first && isParticipant(player))
		{
			player.sendMessage("Вы уже являетесь участником этого эвента.");
			return false;
		}
		if (player.isMounted())
		{
			player.sendMessage("Отзовите питомца.");
			return false;
		}
		if (player.isInDuel())
		{
			player.sendMessage("Вы должны завершить дуель.");
			return false;
		}
		if ((player.getLevel() < getConfigs().MIN_LEVEL) || (player.getLevel() > getConfigs().MAX_LEVEL))
		{
			player.sendMessage("Вы не подходите для участия в эвенте с таким уровнем.");
			return false;
		}
		if ((first) && (player.getPvPTeam() != 0))
		{
			player.sendMessage("Вы уже зарегестрированы на другом эвенте.");
			return false;
		}
		if (player.isInOlympiadMode() || ((first) && Olympiad.isRegistered(player)))
		{
			player.sendMessage("Вы уже зарегестрированы на Олимпиаде.");
			return false;
		}
		if ((player.isInParty()) && (player.getParty().isInDimensionalRift()))
		{
			player.sendMessage("Вы уже зарегестрированы на другом эвенте.");
			return false;
		}
		if (player.isTeleporting())
		{
			player.sendMessage("Вы находитесь в процессе телепортации.");
			return false;
		}
		if ((first) && (this._participants.size() >= getConfigs().MAX_PARTICIPANTS))
		{
			player.sendMessage("Достигнуто максимальное кол-во участников.");
			return false;
		}
		if (player.isCursedWeaponEquipped())
		{
			player.sendMessage("С проклятым оружием на эвент нельзя.");
			return false;
		}
		if (player.getKarma() > 0)
		{
			player.sendMessage("PK не может учавствовать в эвенте.");
			return false;
		}
		return true;
	}

	public void question()
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if (player != null && player.getLevel() >= getConfigs().MIN_LEVEL && player.getLevel() <= getConfigs().MAX_LEVEL && player.getReflection().getId() <= 0 && !player.isInOlympiadMode())
			{
				player.scriptRequest(new CustomMessage("scripts.events.TvT_New.TvT_New.AskPlayer", player).toString(), "events.TvT_New.TvT_New:registerPlayer", new Object[0]);
			}
		}
	}

	public int getCountPlayers()
	{
		return _participants.size();
	}

	public void canRegisters()
	{
		if (_participants != null)
			for (Player player : _participants.keySet())
				if (!canRegister(player, false))
					player.sendMessage("Если все условия не будут соблюдены - вы будите дисквалифицированы");
	}

	@Override
	public boolean isParticipant(Player player)
	{
		return _participants.containsKey(player);
	}

	public int TeamWithMinPlayers()
	{
		int[] count = new int[getConfigs().TEAM_COUNTS + 1];

		for (Player player : _participants.keySet())
		{
			count[player.getPvPTeam()] += 1;
		}
		int min = count[1];

		for (int i = 1; i < count.length; i++)
			min = Math.min(min, count[i]);
		for (int i = 1; i < count.length; i++)
		{
			if (count[i] != min) 
				continue;
			min = i;
		}
		return min;
	}

	public void sayToAll(String adress, String[] replacements, boolean all)
	{
		if (all)
			Announcements.getInstance().announceByCustomMessage(adress, replacements, ChatType.CRITICAL_ANNOUNCE);
		else
			for (Player player : _participants.keySet())
				Announcements.getInstance().announceToPlayerByCustomMessage(player, adress, replacements, ChatType.CRITICAL_ANNOUNCE);
	}

	public void startRegistration()
	{
		_state = 1;
		sayToAll("scripts.events.TvT_New.TvT_New.AnnounceRegistrationStarted", new String[] { getName() }, true);
		question();
		
		_score = new int[getConfigs().TEAM_COUNTS + 1];

		if (getConfigs().TIME_TO_START_BATTLE >= 30)
			ThreadPoolManager.getInstance().schedule(new StartMessages("scripts.events.TvT_New.TvT_New.EventStartOver", new String[] { "30" }), (getConfigs().TIME_TO_START_BATTLE - 30) * 1000);
		if (getConfigs().TIME_TO_START_BATTLE >= 10)
			ThreadPoolManager.getInstance().schedule(new StartMessages("scripts.events.TvT_New.TvT_New.EventStartOver", new String[] { "10" }), (getConfigs().TIME_TO_START_BATTLE - 10) * 1000);
		for (int i = 5; i >= 1; i--)
		{
			if (getConfigs().TIME_TO_START_BATTLE - i >= i)
				ThreadPoolManager.getInstance().schedule(new StartMessages("scripts.events.TvT_New.TvT_New.EventStartOver", new String[] { Integer.toString(i) }), (getConfigs().TIME_TO_START_BATTLE - i) * 1000);
		}
		ThreadPoolManager.getInstance().schedule(new TaskVoid("canRegisters", null), (getConfigs().TIME_TO_START_BATTLE - 10) * 1000);
		ThreadPoolManager.getInstance().schedule(new TaskVoid("start", null), getConfigs().TIME_TO_START_BATTLE * 1000);
	}

	@Override
	public void start()
	{
		if (_state == 0)
		{
			startRegistration();
		}
		else if (_state == 1)
		{
			if (getCountPlayers() >= getConfigs().MIN_PARTICIPANTS)
			{
				teleportPlayersToColiseum();
				ThreadPoolManager.getInstance().schedule(new go(), getConfigs().PAUSE_TIME * 1000);
				sayToAll("scripts.events.TvT_New.TvT_New.AnnounceTeleportToColiseum", null, true);
				_state = 2;
			}
			else
			{
				sayToAll("scripts.events.TvT_New.TvT_New.AnnounceEventCanceled", new String[] { getName() }, true);
				_participants.clear();
				_state = 0;
				abort();
			}
		}
		else
		{
			sayToAll("scripts.events.TvT_New.TvT_New.AnnounceStartError", null, true);
		}
	}

	@Override
	public void finish()
	{
		sayToAll("scripts.events.TvT_New.TvT_New.AnnounceEventEnd", null, false);

		if (_state == 2)
		{
			int WinTeam = -1;
			int count = 0;
			int max = 0;

			for (int i = 0; i < _score.length - 1; i++)
			{
				max = Math.max(_score[i], _score[(i + 1)]);
			}
			for (int i = 0; i < _score.length; i++)
			{
				if (_score[i] != max)
					continue;
				WinTeam = i;
				count++;
			}
			if (count != 1 || WinTeam == -1 || _score[WinTeam] == 0)
			{
				sayToAll("scripts.events.TvT_New.TvT_New.EventDraw", null, false);
			}
			else
			{
				rewardToWinTeam(WinTeam);
				ThreadPoolManager.getInstance().schedule(new TaskVoid("restorePlayers", null), 1000L);
				ThreadPoolManager.getInstance().schedule(new TaskVoid("teleportPlayersToSavedCoords", null), 2000L);
			}

			ThreadPoolManager.getInstance().schedule(new TaskVoid("clearAll", null), 3500L);
			setNextEvent();
			GameEventManager.getInstance().nextEvent();
			_state = 0;
		}
	}

	@Override
	public void abort()
	{
		finish();
		if (_state > 0)
			sayToAll("scripts.events.TvT_New.TvT_New.EventCompletedManually", new String[] { getName() }, true);
	}

	@Override
	public void onLogout(Player player)
	{
		if ((player == null) || (player.getPvPTeam() < 1))
		{
			return;
		}

		if ((_state == 1) && (_participants.containsKey(player)))
		{
			unreg(player);
			return;
		}

		if ((_state == 2) && (_participants.containsKey(player)))
		{
			remove(player);
			try
			{
				player.teleToLocation(player._stablePoint, ReflectionManager.DEFAULT);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void teleportPlayersToSavedCoords()
	{
		for (Player player : this._participants.keySet())
		{
			teleportPlayerToSavedCoords(player);
		}
	}

	public void teleportPlayerToSavedCoords(Player player)
	{
		try
		{
			if (player._stablePoint == null) // игрока не портнуло на стадион
				return;
			player.getPlayer().getEffectList().stopAllEffects();
			player.teleToLocation(player._stablePoint, ReflectionManager.DEFAULT);
			player._stablePoint = null;
			player.setPvPTeam(0);
			player.allowPvPTeam();
			player._event = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void doDie(Creature killer, Creature self)
	{
		if (self == null || killer == null)
			return;
		if ((self instanceof Player) && (killer instanceof Player))
		{
			if(_state > 1 && self.isPlayer() && self.getTeam() != TeamType.NONE && (live_list.containsKey(self.getStoredId())))
			{
				Player player = (Player)self;
				Player kill = (Player)killer;
				if (_participants.containsKey(player))
				{
					_participants.put(player, _participants.get(player) + 1);
				}
				if(getConfigs().ALLOW_KILL_BONUS)
				{
					Functions.addItem(kill, getConfigs().KILL_BONUS_ID, getConfigs().KILL_BONUS_COUNT);
				}
				if (_state == 2 && player.getPvPTeam() > 0 && kill.getPvPTeam() > 0 && _participants.containsKey(player) && _participants.containsKey(kill))
				{
					if (getConfigs().RESURRECTION_TIME == 0)
					{
						if (player != null)
						{
							player.setFakeDeath(true);
							player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
							player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_START_FAKEDEATH));
							player.broadcastCharInfo();
							player.abortCast(true, false);
							player.abortAttack(true, false);
							Functions.show(new CustomMessage("scripts.events.TvT_New.TvT_New.YouDead", player), player);
							live_list.remove(player);
						}
					}
				}
				else
				{
					if (killer != null)
					{
						_score[kill.getPvPTeam()] += 1;
					}
					if (player != null)
					{
						player.setFakeDeath(true);
						player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
						player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_START_FAKEDEATH));
						player.broadcastCharInfo();
						player.abortCast(true, false);
						player.abortAttack(true, false);
						player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.YouWillBeRessuction", player, new Object[0]).add(new Object[] { getConfigs().RESURRECTION_TIME }));
						ThreadPoolManager.getInstance().schedule(new TaskVoid("ResurrectionPlayer", player), getConfigs().RESURRECTION_TIME * 1000);
					}
				}
			}
		}
	}

	public boolean checkRound(boolean finish)
	{
		if (!finish)
		{
			int liveTeamCount = 0;
			int team = 0;
			for (int i = 1; i <= getConfigs().TEAM_COUNTS; i++)
			{
				if (!live_list.containsValue(Integer.valueOf(i)))
					continue;
				liveTeamCount++;
				team = i;
				if (liveTeamCount > 1)
					return false;
			}

			_score[team] += 1;
		}

		if (curr_round >= getConfigs().NUMBER_OF_ROUNDS)
			finish();
		else
			nextRound();

		return true;
	}

	public void nextRound()
	{
		for (Player player : _participants.keySet())
		{
			restorePlayer(player);
			Reflection ref = _ref;
			InstantZone instantZone = ref.getInstancedZone();
			Location tele = Location.findPointToStay(instantZone.getTeleportCoords().get(player.getPvPTeam() - 1), 50, 50, ref.getGeoIndex());
			player.teleToLocation(tele, ref);
			if(getConfigs().NUMBER_OF_ROUNDS > 0)
				live_list.put(player, player.getPvPTeam());
			player.sendPacket(new ExShowScreenMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.StartBattle", player, new Object[0]).toString(), getConfigs().PAUSE_TIME * 700, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, true));
		}
		curr_round += 1;
		paralyzePlayers();
		ThreadPoolManager.getInstance().schedule(new go(), getConfigs().PAUSE_TIME * 1000);
	}

	public void teleportPlayersToColiseum()
	{
		for (Player player : _participants.keySet())
		{
			if (!canRegister(player, false))
			{
				remove(player);
				player.sendMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.YouDisqualified", player, new Object[0]));
				return;
			}
			if(getConfigs().ALLOW_TAKE_ITEM)
				Functions.removeItem(player, getConfigs().TAKE_ITEM_ID, (long)getConfigs().TAKE_COUNT);
			unRide(player);
			unSummonPet(player, true);
			if (getConfigs().STOP_ALL_EFFECTS) 
				player.getEffectList().stopAllEffects();
			playersBuff();
			player.allowPvPTeam();
			player._stablePoint = player._stablePoint == null ? player.getReflection().getReturnLoc() == null ? player.getLoc() : player.getReflection().getReturnLoc() : player._stablePoint;
			Reflection ref = _ref;
			InstantZone instantZone = ref.getInstancedZone();
			Location tele = Location.findPointToStay(instantZone.getTeleportCoords().get(player.getPvPTeam() - 1), 50, 50, ref.getGeoIndex());
			player.teleToLocation(tele, ref);
			restorePlayer(player);
			if (getConfigs().NUMBER_OF_ROUNDS > 0) 
				live_list.put(player, Integer.valueOf(player.getPvPTeam()));
			player.sendPacket(new ExShowScreenMessage(new CustomMessage("scripts.events.TvT_New.TvT_New.StartBattle", player, new Object[0]).toString(), getConfigs().PAUSE_TIME * 700, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, true));
		}

		paralyzePlayers();
	}

	public void paralyzePlayers()
	{
		Skill revengeSkill = SkillTable.getInstance().getInfo(4515, 1);
		for (Player player : _participants.keySet())
		{
			player.getEffectList().stopEffect(1411);
			revengeSkill.getEffects(player, player, false, false);
			if (player.getPet() != null)
			{
				revengeSkill.getEffects(player, player.getPet(), false, false);
			}
		}
	}

	public void unParalyzePlayers()
	{
		for (Player player : _participants.keySet())
		{
			player.getEffectList().stopEffect(4515);
			if (player.getPet() != null)
				player.getPet().getEffectList().stopEffect(4515);
			if(player.isInParty())
				player.leaveParty();
		}
	}

	public void restorePlayer(Player player)
	{
		ClassId nclassId = ClassId.VALUES[player.getClassId().getId()];
		if(player.isFakeDeath())
		{
			player.setFakeDeath(false);
			player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_STOP_FAKEDEATH));
			player.broadcastPacket(new Revive(player));
			player.broadcastCharInfo();
		}
		if(nclassId.isMage())
			playerBuff(player, getConfigs().LIST_MAGE_MAG_SUPPORT);
		else
			playerBuff(player, getConfigs().LIST_MAGE_FAITER_SUPPORT);
		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
	}

	public void restorePlayers()
	{
		for (Player player : this._participants.keySet())
		{
			if(player.isFakeDeath())
			{
				player.setFakeDeath(false);
				player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_STOP_FAKEDEATH));
				player.broadcastPacket(new Revive(player));
				player.broadcastCharInfo();
			}
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}

	public void ResurrectionPlayer(Player player)
	{
		if ((player._event == null) || (_state != 2) || (!_participants.containsKey(player)))
			return;
		restorePlayer(player);
		Reflection ref = _ref;
		InstantZone instantZone = ref.getInstancedZone();
		Location tele = Location.findPointToStay(instantZone.getTeleportCoords().get(player.getPvPTeam() - 1), 50, 50, ref.getGeoIndex());
		player.teleToLocation(tele, ref);
		restorePlayer(player);
	}

	private void clearAll()
	{
		_participants.clear();
		live_list.clear();
	}

	public void rewardToWinTeam(int WinTeam)
	{
		int count = 0;
		for (Player player : _participants.keySet())
		{
			if (player != null && player.getPvPTeam() == WinTeam)
				count++;
		}
		if (count < 1)
			return;

		for (Player player : _participants.keySet())
		{
			if (player != null && player.getPvPTeam() == WinTeam)// && _participants.get(player) >= getConfigs().REWARD_FOR_KILL)
			{
				for(int i = 0; i < getConfigs().getRewardId().length; i++)
					Functions.addItem(player, getConfigs().getRewardId()[i], getConfigs().getRewardCount()[i]);
			}
		}
		sayToAll("scripts.events.TvT_New.TvT_New.EventWin", new String[] { getConfigs().TEAM_NAME.get(WinTeam - 1), getConfigs().NUMBER_OF_ROUNDS > 0 ? "Очков" : "Убийств", Integer.toString(_score[WinTeam]) }, false);
	}

	public StringBuffer getInformation(Player player)
	{
		int rounds = getConfigs().NUMBER_OF_ROUNDS;
		long min = (getConfigs().START_TIME - System.currentTimeMillis() / 1000L) / 60L;
		String time = min + " минут";
		String reward = "";//getConfigs().REWARD_COUNT + " " + ItemHolder.getInstance().getTemplate(getConfigs().REWARD_ITEM_ID).getName();

		StringBuffer content = new StringBuffer();
		content.append("<table width=425 cellspacing=0>");
		content.append("<tr><td align=center>Эвент: <font color=LEVEL>").append(getName()).append("</font></td></tr>");
		content.append("<tr><td align=center>Тип: <font color=LEVEL>").append(rounds > 0 ? "По раундам" : "По времени").append("</font></td></tr>");
		content.append("<tr><td align=center>Приз: <font color=LEVEL>").append(reward).append("</font></td></tr>");
		content.append("<tr><td align=center>Кол-во команд: <font color=LEVEL>").append(getConfigs().TEAM_COUNTS).append("</font></td></tr>");
		content.append("<tr><td align=center>Мин/Макс участников: <font color=LEVEL>").append(getConfigs().MIN_PARTICIPANTS).append("/").append(getConfigs().MAX_PARTICIPANTS).append("</font></td></tr>");
		if (_state == 1)
		{
			content.append("<tr><td align=center>");
			if (_participants == null || !_participants.containsKey(player))
				content.append(Strings.htmlButton("Зарегестрироваться", "bypass -h _bbseventreg;" + getName(), 120, 25));
			else 
				content.append(Strings.htmlButton("Отмена", "bypass -h _bbseventunreg;", 120, 25));
				content.append("</td></tr>");
		}
		else
		{
			content.append("<tr><td align=center>Начало через: <font color=LEVEL>").append(time).append("</font></td></tr>");
		}
		content.append("</table>");
		return content;
	}

	class TaskVoid implements Runnable
	{
		String _name;
		Player _player;

		TaskVoid(String name, Player player)
		{
			_name = name;
			_player = player;
		}

		public void run()
		{
			if (_name.equals("canRegisters"))
				canRegisters();
			else if (_name.equals("start"))
				start();
			else if (_name.equals("restorePlayers"))
				restorePlayers();
			else if (_name.equals("teleportPlayersToSavedCoords"))
				teleportPlayersToSavedCoords();
			else if (_name.equals("clearAll"))
				clearAll();
			else if (_name.equals("ResurrectionPlayer"))
				ResurrectionPlayer(_player);
		}
	}

	class StartMessages implements Runnable
	{
		String _adress;
		String[] _replacing;

		StartMessages(String adress, String[] replacing)
		{
			_adress = adress;
			_replacing = replacing;
		}

		public void run()
		{
			if (_state == 1)
				sayToAll(_adress, _replacing, true);
		}
	}

	public class go implements Runnable
	{
		public go()
		{
		}

		public void run()
		{
			openDoors();
			unParalyzePlayers();
			int time = getConfigs().TIME_TO_END_BATTLE;

			sayToAll("scripts.events.TvT_New.TvT_New.RoundStarted", null, false);

			while (time >= 0 && _state == 2)
			{
				int sec = time - time / 60 * 60;

				for (Player player : _participants.keySet())
				{
					String message;
					if (TvT_New.this.getConfigs().NUMBER_OF_ROUNDS > 0)
					{
						message = "Раунд: " + curr_round + " из " + getConfigs().NUMBER_OF_ROUNDS;
						message = message + "\nКоманда: " + getConfigs().TEAM_NAME.get(player.getPvPTeam() - 1);
					}
					else
					{
						message = "\nКоманда: " + getConfigs().TEAM_NAME.get(player.getPvPTeam() - 1);
					}

					if (sec < 10)
						message = message + "\nОсталось: " + time / 60 + ":0" + sec;
					else
					{
						message = message + "\nОсталось: " + time / 60 + ":" + sec;
					}
					player.sendPacket(new ExShowScreenMessage(message, 2000, ExShowScreenMessage.ScreenMessageAlign.BOTTOM_RIGHT, false));
				}
				if (getConfigs().NUMBER_OF_ROUNDS > 0 && checkRound(false))
					return;
				try
				{
					Thread.sleep(1000);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				time--;
			}

			if (getConfigs().NUMBER_OF_ROUNDS > 0)
				checkRound(true);
			else
				finish();
		}
	}

	public void openDoors()
	{
		for(DoorInstance door : _ref.getDoors())
			door.openMe();
	}

	private void playersBuff()
	{
		for(Player player : _participants.keySet())
		{
			ClassId nclassId = ClassId.VALUES[player.getClassId().getId()];
			if(nclassId.isMage())
				playerBuff(player, getConfigs().LIST_MAGE_MAG_SUPPORT);
			else
				playerBuff(player, getConfigs().LIST_MAGE_FAITER_SUPPORT);
		}
	}

	private void playerBuff(Player player, GArray<Integer> list)
	{
		int time = getConfigs().TIME_MAGE_SUPPORT;
		Summon pet = player.getPet();
		Skill skill = null;

		for(int i : list)
		{
			int lvl = SkillTable.getInstance().getBaseLevel(i);
			
			skill = SkillTable.getInstance().getInfo(i, lvl);
			if(pet != null)
				for(EffectTemplate et : skill.getEffectTemplates())
				{	
					Env env = new Env(pet, pet, skill);
					Effect effect = et.getEffect(env);
					effect.setPeriod(time * 60000);
					pet.getEffectList().addEffect(effect);
					pet.updateEffectIcons();
				}
			else
				for(EffectTemplate et : skill.getEffectTemplates())
				{	
					Env env = new Env(player, player, skill);
					Effect effect = et.getEffect(env);
					effect.setPeriod(time * 60000);
					player.getEffectList().addEffect(effect);
					player.sendChanges();
					player.updateEffectIcons();
				}
		}
	}

	@Override
	public boolean canAttack(Creature attacker, Creature target)
	{
		if(attacker.getTeam() == target.getTeam())
			return false;
		return true;
	}

	@Override
	public boolean canUseSkill(Creature caster, Creature target, Skill skill)
	{
		if(caster.getTeam() == target.getTeam() && skill.isOffensive())
			return false;
		return true;
	}
}