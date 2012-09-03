package events.RabbitsToRiches;

import java.io.File;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Multisell;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.ItemList;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.util.Files;
import l2rt.util.GArray;
import l2rt.util.Rnd;
import l2rt.util.Util;

public class RabbitsToRiches extends Functions implements ScriptFile
{
	private static int EVENT_MANAGER = 32365; // Snow

	private static int TREASURE_SACK_PIECE = 10272;
	private static final int[] PLACE_TREASURE_SACK = { 10254, 10255, 10256, 10257, 10258, 10259 };

	private static GArray<L2Spawn> _spawns = new GArray<L2Spawn>();
	private static boolean _active = false;
	private static boolean MultiSellLoaded = false;

	private static File[] multiSellFiles = { new File(Config.DATAPACK_ROOT, "data/scripts/events/RabbitsToRiches/32365.xml") };

	/**
	 * Спавнит эвент менеджера
	 */
	private void spawnEventManagers()
	{
		final int EVENT_MANAGERS1[][] = { { 82264, 147528, -3495, 16384 }, // Giran
				{ 148552, 27352, -2231, 32768 }, // Aden
				{ 82808, 56152, -1551, 49152 }, // Oren
				{ 17800, 145992, -3117, 49152 }, // Dion
				{ 111704, 219208, -3569, 49152 }, // Heina
				{ -14200, 124232, -3143, 32768 }, // Gludio
				{ 147352, -56536, -2806, 11548 }, // Goddart
				{ 87720, -142152, -1366, 40960 }, // Shudward
				{ -82472, 151624, -3155, 49152 }, // Gludin
				{ 115976, 76552, -2745, 53988 }, // Hunter Village
				{ 43272, -50568, -823, 57344 }, // Rune
		};

		SpawnNPCs(EVENT_MANAGER, EVENT_MANAGERS1, _spawns);
	}

	/**
	 * Удаляет спавн эвент менеджера
	 */
	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}

	/**
	 * Читает статус эвента из базы.
	 * 
	 * @return
	 */
	private static boolean isActive()
	{
		return IsActive("RabbitsToRiches");
	}

	/**
	 * Start Event
	 */
	public void startEvent()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive("RabbitsToRiches", true))
		{
			loadMultiSell();
			spawnEventManagers();
			System.out.println("Event: 'L2 Rabbits To Riches Event' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.RabbitsToRiches.RabbitsToRiches.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event: 'L2 Rabbits To Riches Event' already started.");

		_active = true;
		show(Files.read("data/html/admin/events.htm", player), player);
	}

	/**
	 * Stop Event
	 */
	public void stopEvent()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive("RabbitsToRiches", false))
		{
			unSpawnEventManagers();
			System.out.println("Event: 'Rabbits To Riches Event' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.RabbitsToRiches.RabbitsToRiches.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event: 'L2 Rabbits To Riches Event' not started.");

		_active = false;
		show(Files.read("data/html/admin/events.htm", player), player);
	}

	/**
	 * Продает 1 сундук игроку
	 */
	public void sack(String[] var)
	{
		L2Player player = (L2Player) getSelf();

		if(!player.isQuestContinuationPossible(true))
			return;

		if(player.isActionsDisabled() || player.isSitting() || player.getLastNpc() == null || player.getLastNpc().getDistance(player) > 300)
			return;

		@SuppressWarnings("unused")
		int sack_count = 1;
		try
		{
			sack_count = Integer.valueOf(var[0]);
		}
		catch(Exception E)
		{}

		if(player.getInventory().getItemByItemId(TREASURE_SACK_PIECE) == null || player.getInventory().getItemByItemId(TREASURE_SACK_PIECE).getCount() < 50)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return;
		}

		removeItem(player, TREASURE_SACK_PIECE, 50);
		int i = Rnd.get(PLACE_TREASURE_SACK.length);
		addItem(player, PLACE_TREASURE_SACK[i], 1);
		player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1).addItemName(i));
		player.sendPacket(new ItemList(player, true));
	}

	/**
	 * Добавляет в диалоги эвент менеджеров строчку с байпасом для покупки сундука
	 */
	private static int[] sack_counts = { 1 };

	public String DialogAppend_32365(Integer val)
	{
		if(val != 0)
			return "";

		String price;
		String append = "";
		for(int cnt : sack_counts)
		{
			price = Util.formatAdena(50 * cnt);
			append += "<a action=\"bypass -h scripts_events.RabbitsToRiches.RabbitsToRiches:sack " + cnt + "\">";
			if(cnt == 1)
				append += new CustomMessage("scripts.events.RabbitsToRiches.RabbitsToRiches.sack", getSelf()).addString(price);
			else
				append += "</a><br>";
		}

		return append;
	}

	private static void loadMultiSell()
	{
		if(MultiSellLoaded)
			return;
		for(File f : multiSellFiles)
			L2Multisell.getInstance().parseFile(f);
		MultiSellLoaded = true;
	}

	public static void OnReloadMultiSell()
	{
		MultiSellLoaded = false;
		if(_active)
			loadMultiSell();
	}

	public void onLoad()
	{
		if(isActive())
		{
			_active = true;
			loadMultiSell();
			spawnEventManagers();
			System.out.println("Loaded Event: L2 Rabbits To Riches [state: activated]");
			if(Config.EVENT_TREASURE_SACK_CHANCE > 0.8)
				System.out.println("Event L2 Rabbits To Riches: << W A R N I N G >> RATES IS TO HIGH!!!");
		}
		else
			System.out.println("Loaded Event: L2 Rabbits To Riches Event [state: deactivated]");
	}

	public void onReload()
	{
		unSpawnEventManagers();
		if(MultiSellLoaded)
		{
			for(File f : multiSellFiles)
				L2Multisell.getInstance().remove(f);
			MultiSellLoaded = false;
		}
	}

	public void onShutdown()
	{
		unSpawnEventManagers();
		if(MultiSellLoaded)
		{
			for(File f : multiSellFiles)
				L2Multisell.getInstance().remove(f);
			MultiSellLoaded = false;
		}
	}

	/**
	 * Обработчик смерти мобов, управляющий эвентовым дропом
	 */
	public static void OnDie(L2Character cha, L2Character killer)
	{
		if(_active && SimpleCheckDrop(cha, killer) && Rnd.get(1000) <= Config.EVENT_TREASURE_SACK_CHANCE * killer.getPlayer().getRateItems() * Config.RATE_DROP_ITEMS * ((L2NpcInstance) cha).getTemplate().rateHp)
			((L2NpcInstance) cha).dropItem(killer.getPlayer(), TREASURE_SACK_PIECE, 1);
	}

	public static void OnPlayerEnter(L2Player player)
	{
		if(_active)
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.RabbitsToRiches.RabbitsToRiches.AnnounceEventStarted", null);
	}
}