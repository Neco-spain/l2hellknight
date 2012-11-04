package events.FightClub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.listener.actor.player.OnPlayerExitListener;
import l2r.gameserver.listener.actor.player.OnTeleportListener;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.actor.listener.CharListenerList;
import l2r.gameserver.model.base.InvisibleType;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2r.gameserver.network.serverpackets.Revive;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Location;
import npc.model.events.FightClubManagerInstance.Rate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FightClubManager extends Functions implements ScriptFile, OnPlayerExitListener, OnTeleportListener
{

	private static Logger _log = LoggerFactory.getLogger(FightClubManager.class);

	private static Map<Long, Rate> _ratesMap;
	private static List<FightClubArena> _fights;
	private static ReflectionManager _reflectionManager;
	protected static List<Long> _inBattle;
	private static Map<Long, Location> _restoreCoord;
	private static List<Long> _inList;
	private static StringBuilder _itemsList;
	private static Map<String, Integer> _allowedItems;
	private static Location _player1loc;
	private static Location _player2loc;

	@Override
	public void onLoad()
	{
		if(!Config.FIGHT_CLUB_ENABLED)
			return;

		CharListenerList.addGlobal(this);

		_ratesMap = new HashMap<Long, Rate>();
		_fights = new ArrayList<FightClubArena>();
		_restoreCoord = new HashMap<Long, Location>();
		_inBattle = new ArrayList<Long>();
		_inList = new ArrayList<Long>();
		_reflectionManager = ReflectionManager.getInstance();
		_itemsList = new StringBuilder();
		_allowedItems = new HashMap<String, Integer>();
		_player1loc = new Location(-80696, -44296, -11496);
		_player2loc = new Location(-82536, -47032, -11504);

		for(int i = 0; i < Config.ALLOWED_RATE_ITEMS.length; i++)
		{
			String itemName = ItemFunctions.createItem(Integer.parseInt(Config.ALLOWED_RATE_ITEMS[i])).getTemplate().getName();
			_itemsList.append(itemName).append(";");
			_allowedItems.put(itemName, Integer.parseInt(Config.ALLOWED_RATE_ITEMS[i]));
		}

		_log.info("Loaded Event: Fight Club");

	}

	@Override
	public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
	{
		if(player.getTeam() != TeamType.NONE && _inBattle.contains(player.getStoredId()))
			removePlayer(player);
	}

	/**
	 * Удаляет всю информацию об игроке
	 * @param player - ссылка на удаляемого игрока
	 */
	private static void removePlayer(Player player)
	{
		if(player != null)
		{
			player.setTeam(TeamType.NONE);
			if(_inBattle.contains(player.getStoredId()))
				_inBattle.remove(player.getStoredId());
			if(_inList.contains(player.getStoredId()))
			{
				_ratesMap.remove(player.getStoredId());
				_inList.remove(player.getStoredId());
			}
			if(_restoreCoord.containsKey(player.getStoredId()))
				;
			_restoreCoord.remove(player.getStoredId());
		}
	}

	public static Location getRestoreLocation(Player player)
	{
		return _restoreCoord.get(player.getStoredId());
	}

	public static Player getPlayer(long playerStoredI)
	{
		return GameObjectsStorage.getAsPlayer(playerStoredI);
	}

	@Override
	public void onPlayerExit(Player player)
	{
		removePlayer(player);
	}

	@Override
	public void onReload()
	{
		_fights.clear();
		_ratesMap.clear();
		_inBattle.clear();
		_inList.clear();
		onLoad();
	}

	@Override
	public void onShutdown()
	{
		if(!Config.FIGHT_CLUB_ENABLED)
			return;
		
		_fights.clear();
		_ratesMap.clear();
		_inBattle.clear();
		_inList.clear();
	}

	public static String addApplication(Player player, String item, int count)
	{
		if(!checkPlayer(player, true))
			return null;
		if(isRegistered(player))
			return "reg";
		if(Functions.getItemCount(player, _allowedItems.get(item)) < count)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledItems", player), player);
			return "NoItems";
		}
		final Rate rate = new Rate(player, _allowedItems.get(item), count);
		_ratesMap.put(player.getStoredId(), rate);
		_inList.add(0, player.getStoredId());
		if(Config.FIGHT_CLUB_ANNOUNCE_RATE)
		{
			final String[] args = { player.getName(), String.valueOf(player.getLevel()), String.valueOf(rate.getItemCount()), item };
			Announcements.getInstance().announceByCustomMessage("scripts.events.fightclub.Announce", args, ChatType.MPCC_ROOM);
		}

		return "OK";
	}

	/**
	 * Отправляет ConfirmDlg
	 * @param requested - игрок, выставивший заявку. <b>Ему</b> отправляется запрос
	 * @param requester - игрок, выбравший из списка соперника. <b>От него</b> отправляется запрос
	 */
	public static boolean requestConfirmation(Player requested, Player requester)
	{
		if(!checkPlayer(requester, true))
			return false;

		if((requested.getLevel() - requester.getLevel()) > Config.MAXIMUM_LEVEL_DIFFERENCE || (requester.getLevel() - requested.getLevel()) > Config.MAXIMUM_LEVEL_DIFFERENCE)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledLevel", requester, Config.MINIMUM_LEVEL_TO_PARRICIPATION, Config.MAXIMUM_LEVEL_TO_PARRICIPATION, Config.MAXIMUM_LEVEL_DIFFERENCE), requester);
			return false;
		}
		Object[] duelists = { requested, requester };
		requested.scriptRequest(new CustomMessage("scripts.events.fightclub.AskPlayer", requested, requester.getName(), requester.getLevel()).toString(), "events.FightClub.FightClubManager:doStart", duelists);
		return true;
	}

	/**
	 * Проверка игроков для последующего создания арены для них
	 * @param requested - игрок, выставивший заявку. <b>Ему</b> отправляется запрос
	 * @param requester - игрок, выбравший из списка соперника. <b>От него</b> отправляется запрос
	 */
	public static void doStart(Player requested, Player requester)
	{
		final int itemId = _ratesMap.get(requested.getStoredId()).getItemId();
		final int itemCount = _ratesMap.get(requested.getStoredId()).getItemCount();
		if(!checkPrepare(requested, requester, itemId, itemCount))
			return;

		if(!checkPlayer(requested, false))
			return;

		if(!checkPlayer(requester, true))
			return;

		_inList.remove(requested.getStoredId());
		_ratesMap.remove(requested.getStoredId());
		_restoreCoord.put(requested.getStoredId(), new Location(requested.getX(), requested.getY(), requested.getZ()));
		_restoreCoord.put(requester.getStoredId(), new Location(requester.getX(), requester.getY(), requester.getZ()));
		Functions.removeItem(requested, itemId, itemCount);
		Functions.removeItem(requester, itemId, itemCount);
		createBattle(requested, requester, itemId, itemCount);
	}

	private static boolean checkPrepare(Player requested, Player requester, int itemId, int itemCount)
	{

		if(Functions.getItemCount(requested, itemId) < itemCount)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledItems", requested), requested);
			show(new CustomMessage("scripts.events.fightclub.CancelledOpponent", requester), requester);
			return false;
		}

		if(Functions.getItemCount(requester, itemId) < itemCount)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledItems", requester), requester);
			return false;
		}

		if(_inBattle.contains(requested.getStoredId()))
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledOpponent", requester), requested);
			return false;
		}

		return true;
	}

	private static void createBattle(Player player1, Player player2, int itemId, int itemCount)
	{
		_inBattle.add(player1.getStoredId());
		_inBattle.add(player2.getStoredId());
		final Reflection _reflection = new Reflection();
		_reflectionManager.add(_reflection);
		final FightClubArena _arena = new FightClubArena(player1, player2, itemId, itemCount, _reflection);
		_fights.add(_arena);
	}

	public static void deleteArena(FightClubArena arena)
	{
		removePlayer(arena.getPlayer1());
		removePlayer(arena.getPlayer2());
		arena.getReflection().collapse();
		_fights.remove(arena);
	}

	public static boolean checkPlayer(Player player, boolean first)
	{

		if(first && player.isDead())
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledDead", player), player);
			return false;
		}

		if(first && player.getTeam() != TeamType.NONE)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.getLevel() < Config.MINIMUM_LEVEL_TO_PARRICIPATION || player.getLevel() > Config.MAXIMUM_LEVEL_TO_PARRICIPATION)
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledLevel", player), player);
			return false;
		}

		if(player.isMounted())
		{
			show(new CustomMessage("scripts.events.fightclub.Cancelled", player), player);
			return false;
		}

		if(player.isCursedWeaponEquipped())
		{
			show(new CustomMessage("scripts.events.fightclub.Cancelled", player), player);
			return false;
		}

		if(player.isInDuel())
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledDuel", player), player);
			return false;
		}

		if(player.getOlympiadGame() != null || first && Olympiad.isRegistered(player))
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledOlympiad", player), player);
			return false;
		}

		if(player.isInParty() && player.getParty().isInDimensionalRift())
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.isInObserverMode())
		{
			show(new CustomMessage("scripts.event.fightclub.CancelledObserver", player), player);
			return false;
		}

		if(player.isTeleporting())
		{
			show(new CustomMessage("scripts.events.fightclub.CancelledTeleport", player), player);
			return false;
		}
		return true;
	}

	/**
	 * Приватный метод. Возващает true, если игрок зарегистрировал ставку
	 * @param player - ссылка на проверяемого игрока
	 * @return - true, если зарегистрирован
	 */
	private static boolean isRegistered(Player player)
	{
		if(_inList.contains(player.getStoredId()))
			return true;
		return false;
	}

	/**
	 * Возвращает объект класса {@link Rate}, содержащий параметры "заявки"
	 * <b> Метод для использования в FightClubInstanceManager! </b>
	 * @param index
	 * @return объект, содержащий заявку
	 */
	public static Rate getRateByIndex(int index)
	{
		return _ratesMap.get(_inList.get(index));
	}

	/**
	 * Возвращает объект класса {@link Rate}, содержащий параметры "заявки"
	 * <b> Метод для использования в FightClubInstanceManager! </b>
	 * @param index
	 * @return объект, содержащий заявку
	 */
	public static Rate getRateByStoredId(long storedId)
	{
		return _ratesMap.get(storedId);
	}

	/**
	 * Возвращает через ; имена предметов,
	 * разрешенных в качестве ставки.
	 * <b> Метод для использования в FightClubInstanceManager! </b>
	 * @return список предметов через ";"
	 */
	public static String getItemsList()
	{
		return _itemsList.toString();
	}

	/**
	 * <b> Метод для использования в FightClubInstanceManager! </b>
	 * @param playerObject - ссылка на игрока
	 * @return true, если игрок зарегистрировал ставку
	 */
	public static boolean isRegistered(Object playerObject)
	{
		if(_ratesMap.containsKey(((Player) playerObject).getStoredId()))
			return true;
		return false;
	}

	/**
	 * Удаляет регистрацию игрока в списке через метод {@link #removePlayer(Player)}
	 * <b> Метод для использования в FightClubInstanceManager! </b>
	 * @param playerObject ссылка на игрока
	 */
	public static void deleteRegistration(Player player)
	{
		removePlayer(player);
	}

	/**
	 * Возвращает количеств игроков, сделавших свои ставки
	 * <b> Метод для использования в FightClubInstanceManager! </b>
	 * @return - количество игроков, сделавших ставки
	 */
	public static int getRatesCount()
	{
		return _inList.size();
	}

	/**
	 * Ставит в root игрока
	 * @param player
	 */
	private static void rootPlayer(Player player)
	{
		player.startRooted();
		player.startAbnormalEffect(AbnormalEffect.ROOT);
		if(player.getPet() != null)
		{
			player.getPet().startRooted();
			player.getPet().startAbnormalEffect(AbnormalEffect.ROOT);
		}
	}

	/**
	 * Снимает root с игрока
	 * @param player
	 */
	private static void unrootPlayers(Player player)
	{
		player.stopRooted();
		player.stopAbnormalEffect(AbnormalEffect.ROOT);
		if(player.getPet() != null)
		{
			player.getPet().stopRooted();
			player.getPet().stopAbnormalEffect(AbnormalEffect.ROOT);
		}
	}

	/**
	 * Телепортирует игроков на сохраненные координаты
	 * @param player1
	 * @param player2
	 */
	@SuppressWarnings("static-access")
	public static void teleportPlayersBack(Player player1, Player player2, Object args)
	{

		if(Config.REMOVE_CLAN_SKILLS && player1.getClan() != null)
		{
			for(final Skill skill : player1.getClan().getAllSkills())
				player1.addSkill(skill);
		}

		if(Config.REMOVE_CLAN_SKILLS && player2.getClan() != null)
		{
			for(final Skill skill : player1.getClan().getAllSkills())
				player1.addSkill(skill);
		}

		if(Config.REMOVE_HERO_SKILLS && player1.isHero())
			Hero.getInstance().addSkills(player1);

		if(Config.REMOVE_HERO_SKILLS && player2.isHero())
			Hero.getInstance().addSkills(player1);
		player1.block();
		player1.teleToLocation(_restoreCoord.get(player1.getStoredId()), ReflectionManager.DEFAULT);
		player1.unblock();
		player2.block();
		player2.teleToLocation(_restoreCoord.get(player2.getStoredId()), ReflectionManager.DEFAULT);
		player2.unblock();
	}

	/**
	 * Выводит текст по центру экрана. Выводит нескольким игрокам.
	 * Положение - TOP_CENTER
	 * @param address - адрес текста
	 * @param arg - параметр замены (один)
	 * @param bigFont - большой шрифт
	 * @param players - список игроков
	 */
	protected static void sayToPlayers(String address, Object arg, boolean bigFont, Player... players)
	{
		for(Player player : players)
		{
			final CustomMessage sm = new CustomMessage(address, player, arg);
			player.sendPacket(new ExShowScreenMessage(sm.toString(), 3000, ScreenMessageAlign.TOP_CENTER, bigFont));
		}
	}

	/**
	 * Выводит текст по центру экрана. Выводит нескольким игрокам.
	 * Положение - TOP_CENTER
	 * @param address - адрес текста
	 * @param bigFont - большой шрифт
	 * @param players - список игроков
	 */
	protected static void sayToPlayers(String address, boolean bigFont, Player... players)
	{
		for(Player player : players)
		{
			final CustomMessage sm = new CustomMessage(address, player);
			player.sendPacket(new ExShowScreenMessage(sm.toString(), 3000, ScreenMessageAlign.TOP_CENTER, bigFont));
		}
	}

	/**
	 * Выводит текст по центру экрана. Положение - TOP_CENTER
	 * @param player - целевой игрок
	 * @param address - адрес текста
	 * @param bigFont - большой шрифт
	 * @param args - параметры замены текста
	 */
	protected static void sayToPlayer(Player player, String address, boolean bigFont, Object... args)
	{
		player.sendPacket(new ExShowScreenMessage(new CustomMessage(address, player, args).toString(), 3000, ScreenMessageAlign.TOP_CENTER, bigFont));
	}

	/**
	 * Возрождает мёртвых игроков
	 */
	public static void resurrectPlayers(Player player1, Player player2, Object obj)
	{
		if(player1.isDead())
		{
			player1.restoreExp();
			player1.setCurrentCp(player1.getMaxCp());
			player1.setCurrentHp(player1.getMaxHp(), true);
			player1.setCurrentMp(player1.getMaxMp());
			player1.broadcastPacket(new Revive(player1));
		}
		if(player2.isDead())
		{
			player2.restoreExp();
			player2.setCurrentCp(player2.getMaxCp());
			player2.setCurrentHp(player2.getMaxHp(), true);
			player2.setCurrentMp(player2.getMaxMp());
			player2.broadcastPacket(new Revive(player2));
		}
	}

	/**
	 * Восстанавливает HP/MP/CP участникам
	 */
	public void healPlayers(Player player1, Player player2, Object obj)
	{
		player1.setCurrentCp(player1.getMaxCp());
		player1.setCurrentHpMp(player1.getMaxHp(), player1.getMaxMp());
		player2.setCurrentCp(player2.getMaxCp());
		player2.setCurrentHpMp(player2.getMaxHp(), player2.getMaxMp());
	}

	/**
	 * Запускает битву между игроками.
	 * @param player1
	 * @param player2
	 */
	protected static void startBattle(Player player1, Player player2)
	{
		unrootPlayers(player1);
		player1.setTeam(TeamType.BLUE);
		unrootPlayers(player2);
		player2.setTeam(TeamType.RED);
		sayToPlayers("scripts.events.fightclub.Start", true, player1, player2);
	}

	/**
	 * Телепортирует игроков в коллизей в заданное отражение
	 * @param player1 - первый игрок
	 * @param player2 - втрой игрок
	 * @param reflection - отражение
	 */
	@SuppressWarnings("static-access")
	public static void teleportPlayersToColliseum(Player player1, Player player2, Reflection reflection)
	{
		player1.block();
		unRide(player1);

		if(Config.UNSUMMON_PETS)
			unSummonPet(player1, true);

		if(Config.UNSUMMON_SUMMONS)
			unSummonPet(player1, false);

		if(player1.isInvisible())
			player1.setInvisibleType(InvisibleType.NONE);

		if(Config.REMOVE_CLAN_SKILLS && player1.getClan() != null)
		{
			for(final Skill skill : player1.getClan().getAllSkills())
				player1.removeSkill(skill);
		}

		if(Config.REMOVE_HERO_SKILLS && player1.isHero())
			Hero.getInstance().removeSkills(player1);
		if(Config.CANCEL_BUFF_BEFORE_FIGHT)
		{
			player1.getEffectList().stopAllEffects();
			if(player1.getPet() != null)
				player1.getPet().getEffectList().stopAllEffects();
		}

		player1.teleToLocation(_player1loc, reflection);
		player1.unblock();
		rootPlayer(player1);

		player2.block();
		unRide(player2);

		if(Config.UNSUMMON_PETS)
			unSummonPet(player2, true);

		if(Config.UNSUMMON_SUMMONS)
			unSummonPet(player2, false);

		if(player2.isInvisible())
			player2.setInvisibleType(InvisibleType.NONE);

		if(Config.REMOVE_CLAN_SKILLS && player2.getClan() != null)
		{
			for(final Skill skill : player2.getClan().getAllSkills())
				player2.removeSkill(skill);
		}

		if(Config.REMOVE_HERO_SKILLS && player2.isHero())
			Hero.getInstance().removeSkills(player2);

		if(Config.CANCEL_BUFF_BEFORE_FIGHT)
		{
			player2.getEffectList().stopAllEffects();
			if(player2.getPet() != null)
				player2.getPet().getEffectList().stopAllEffects();
		}

		player2.teleToLocation(_player2loc, reflection);
		player2.unblock();
		rootPlayer(player2);
	}

	protected static class TeleportTask extends RunnableImpl
	{

		private Player player;
		private Location location;

		public TeleportTask(Player player, Location location)
		{
			this.player = player;
			this.location = location;
			player.block();
		}

		@Override
		public void runImpl() throws Exception
		{
			player.teleToLocation(location);
			player.unblock();
		}

	}
}