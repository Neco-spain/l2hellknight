package events.glitmedal;

import java.io.File;

import l2rt.Config;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Multisell;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.util.Files;
import l2rt.util.GArray;
import l2rt.util.Rnd;
import l2rt.util.Util;

/**
 * User: darkevil
 * Date: 26.02.2008
 * Time: 1:17:42
 */
public class glitmedal extends Functions implements ScriptFile
{
	private static int EVENT_MANAGER_ID1 = 31228; // Roy
	private static int EVENT_MANAGER_ID2 = 31229; // Winnie

	// Для временного статуса который выдается в игре рандомно либо 0 либо 1
	private int isTalker;

	// Медали
	private static int EVENT_MEDAL = 6392;
	private static int EVENT_GLITTMEDAL = 6393;

	private static int Badge_of_Rabbit = 6399;
	private static int Badge_of_Hyena = 6400;
	private static int Badge_of_Fox = 6401;
	private static int Badge_of_Wolf = 6402;

	private static GArray<L2Spawn> _spawns = new GArray<L2Spawn>();
	private static boolean _active = false;
	private static boolean MultiSellLoaded = false;

	private static File[] multiSellFiles = { new File(Config.DATAPACK_ROOT, "data/scripts/events/glitmedal/502.xml"),
			new File(Config.DATAPACK_ROOT, "data/scripts/events/glitmedal/503.xml"),
			new File(Config.DATAPACK_ROOT, "data/scripts/events/glitmedal/504.xml"),
			new File(Config.DATAPACK_ROOT, "data/scripts/events/glitmedal/505.xml"),
			new File(Config.DATAPACK_ROOT, "data/scripts/events/glitmedal/506.xml"), };

	public void onLoad()
	{
		if(isActive())
		{
			_active = true;
			loadMultiSell();
			spawnEventManagers();
			System.out.println("Loaded Event: L2 Medal Collection Event [state: activated]");
			if(Config.EVENT_GLITTMEDAL_NORMAL_CHANCE > 80 || Config.EVENT_GLITTMEDAL_GLIT_CHANCE > 0.8)
				System.out.println("Event L2 Medal Collection: << W A R N I N G >> RATES IS TO HIGH!!!");
		}
		else
			System.out.println("Loaded Event: L2 Medal Collection Event [state: deactivated]");
	}

	/**
	 * Читает статус эвента из базы.
	 *
	 * @return
	 */
	private static boolean isActive()
	{
		return IsActive("glitter");
	}

	/**
	 * Запускает эвент
	 */
	public void startEvent()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive("glitter", true))
		{
			loadMultiSell();
			spawnEventManagers();
			System.out.println("Event 'L2 Medal Collection Event' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.glitmedal.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'L2 Medal Collection Event' already started.");

		_active = true;

		show(Files.read("data/html/admin/events.htm", player), player);
	}

	/**
	 * Останавливает эвент
	 */
	public void stopEvent()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;
		if(SetActive("glitter", false))
		{
			unSpawnEventManagers();
			System.out.println("Event 'L2 Medal Collection Event' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.glitmedal.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'L2 Medal Collection Event' not started.");

		_active = false;

		show(Files.read("data/html/admin/events.htm", player), player);
	}

	public static void OnPlayerEnter(L2Player player)
	{
		if(_active)
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.glitmedal.AnnounceEventStarted", null);
	}

	/**
	 * Спавнит эвент менеджеров
	 */
	private void spawnEventManagers()
	{
		// 1й эвент кот
		final int EVENT_MANAGERS1[][] = { { 147893, -56622, -2776, 0 }, { -81070, 149960, -3040, 0 },
				{ 82882, 149332, -3464, 49000 }, { 44176, -48732, -800, 33000 }, { 147920, 25664, -2000, 16384 },
				{ 117498, 76630, -2695, 38000 }, { 111776, 221104, -3543, 16384 }, { -84516, 242971, -3730, 34000 },
				{ -13073, 122801, -3117, 0 }, { -44337, -113669, -224, 0 }, { 11281, 15652, -4584, 25000 },
				{ 44122, 50784, -3059, 57344 }, { 80986, 54504, -1525, 32768 }, { 114733, -178691, -821, 0 },
				{ 18178, 145149, -3054, 7400 }, };

		// 2й эвент кот
		final int EVENT_MANAGERS2[][] = { { 147960, -56584, -2776, 0 }, { -81070, 149860, -3040, 0 },
				{ 82798, 149332, -3464, 49000 }, { 44176, -48688, -800, 33000 }, { 147985, 25664, -2000, 16384 },
				{ 117459, 76664, -2695, 38000 }, { 111724, 221111, -3543, 16384 }, { -84516, 243015, -3730, 34000 },
				{ -13073, 122841, -3117, 0 }, { -44342, -113726, -240, 0 }, { 11327, 15682, -4584, 25000 },
				{ 44157, 50827, -3059, 57344 }, { 80986, 54452, -1525, 32768 }, { 114719, -178742, -821, 0 },
				{ 18154, 145192, -3054, 7400 }, };

		SpawnNPCs(EVENT_MANAGER_ID1, EVENT_MANAGERS1, _spawns);
		SpawnNPCs(EVENT_MANAGER_ID2, EVENT_MANAGERS2, _spawns);
	}

	/**
	 * Удаляет спавн эвент менеджеров
	 */
	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}

	private static void loadMultiSell()
	{
		if(MultiSellLoaded)
			return;
		for(File f : multiSellFiles)
			L2Multisell.getInstance().parseFile(f);
		MultiSellLoaded = true;
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
		if(_active && SimpleCheckDrop(cha, killer))
		{
			long count = Util.rollDrop(1, 1, Config.EVENT_GLITTMEDAL_NORMAL_CHANCE * killer.getPlayer().getRateItems() * ((L2MonsterInstance) cha).getTemplate().rateHp * 10000L, true);
			if(count > 0)
				addItem(killer.getPlayer(), EVENT_MEDAL, count);
			if(killer.getPlayer().getInventory().getCountOf(Badge_of_Wolf) == 0 && Rnd.chance(Config.EVENT_GLITTMEDAL_GLIT_CHANCE * Config.RATE_DROP_ITEMS * killer.getPlayer().getRateItems() * ((L2MonsterInstance) cha).getTemplate().rateHp))
				addItem(killer.getPlayer(), EVENT_GLITTMEDAL, 1);
		}
	}

	public void glitchang()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.isQuestContinuationPossible(true))
			return;

		if(getItemCount(player, EVENT_MEDAL) >= 1000)
		{
			removeItem(player, EVENT_MEDAL, 1000);
			addItem(player, EVENT_GLITTMEDAL, 10);
			return;
		}
		player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
	}

	public void medal()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.isQuestContinuationPossible(true))
			return;

		if(getItemCount(player, Badge_of_Wolf) >= 1)
		{
			show(Files.read("data/scripts/events/glitmedal/event_col_agent1_q0996_05.htm", player), player);
			return;
		}
		else if(getItemCount(player, Badge_of_Fox) >= 1)
		{
			show(Files.read("data/scripts/events/glitmedal/event_col_agent1_q0996_04.htm", player), player);
			return;
		}
		else if(getItemCount(player, Badge_of_Hyena) >= 1)
		{
			show(Files.read("data/scripts/events/glitmedal/event_col_agent1_q0996_03.htm", player), player);
			return;
		}
		else if(getItemCount(player, Badge_of_Rabbit) >= 1)
		{
			show(Files.read("data/scripts/events/glitmedal/event_col_agent1_q0996_02.htm", player), player);
			return;
		}

		show(Files.read("data/scripts/events/glitmedal/event_col_agent1_q0996_01.htm", player), player);
		return;
	}

	public void medalb()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.isQuestContinuationPossible(true))
			return;

		if(getItemCount(player, Badge_of_Wolf) >= 1)
		{
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_05.htm", player), player);
			return;
		}
		else if(getItemCount(player, Badge_of_Fox) >= 1)
		{
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_04.htm", player), player);
			return;
		}
		else if(getItemCount(player, Badge_of_Hyena) >= 1)
		{
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_03.htm", player), player);
			return;
		}
		else if(getItemCount(player, Badge_of_Rabbit) >= 1)
		{
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_02.htm", player), player);
			return;
		}

		show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_01.htm", player), player);
		return;
	}

	public void game()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.isQuestContinuationPossible(true))
			return;

		if(getItemCount(player, Badge_of_Fox) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 40)
			{
				show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_11.htm", player), player);
				return;
			}
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_12.htm", player), player);
			return;
		}
		else if(getItemCount(player, Badge_of_Hyena) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 20)
			{
				show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_11.htm", player), player);
				return;
			}
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_12.htm", player), player);
			return;
		}
		else if(getItemCount(player, Badge_of_Rabbit) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 10)
			{
				show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_11.htm", player), player);
				return;
			}
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_12.htm", player), player);
			return;
		}

		else if(getItemCount(player, EVENT_GLITTMEDAL) >= 5)
		{
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_11.htm", player), player);
			return;
		}

		show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_12.htm", player), player);
		return;
	}

	public void gamea()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.isQuestContinuationPossible(true))
			return;
		isTalker = Rnd.get(2);

		if(getItemCount(player, Badge_of_Fox) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 40)
				if(isTalker == 1)
				{
					removeItem(player, Badge_of_Fox, 1);
					removeItem(player, EVENT_GLITTMEDAL, getItemCount(player, EVENT_GLITTMEDAL));
					addItem(player, Badge_of_Wolf, 1);
					show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_24.htm", player), player);
					return;
				}
				else if(isTalker == 0)
				{
					removeItem(player, EVENT_GLITTMEDAL, 40);
					show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_25.htm", player), player);
					return;
				}
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player), player);
			return;
		}

		else if(getItemCount(player, Badge_of_Hyena) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 20)
				if(isTalker == 1)
				{
					removeItem(player, Badge_of_Hyena, 1);
					removeItem(player, EVENT_GLITTMEDAL, 20);
					addItem(player, Badge_of_Fox, 1);
					show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_23.htm", player), player);
					return;
				}
				else if(isTalker == 0)
				{
					removeItem(player, EVENT_GLITTMEDAL, 20);
					show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_25.htm", player), player);
					return;
				}
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player), player);
			return;
		}

		else if(getItemCount(player, Badge_of_Rabbit) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 10)
				if(isTalker == 1)
				{
					removeItem(player, Badge_of_Rabbit, 1);
					removeItem(player, EVENT_GLITTMEDAL, 10);
					addItem(player, Badge_of_Hyena, 1);
					show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_22.htm", player), player);
					return;
				}
				else if(isTalker == 0)
				{
					removeItem(player, EVENT_GLITTMEDAL, 10);
					show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_25.htm", player), player);
					return;
				}
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player), player);
			return;
		}

		if(getItemCount(player, EVENT_GLITTMEDAL) >= 5)
			if(isTalker == 1)
			{
				removeItem(player, EVENT_GLITTMEDAL, 5);
				addItem(player, Badge_of_Rabbit, 1);
				show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_21.htm", player), player);
				return;
			}
			else if(isTalker == 0)
			{
				removeItem(player, EVENT_GLITTMEDAL, 5);
				show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_25.htm", player), player);
				return;
			}
		show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player), player);
		return;
	}

	// FIXME: нафига две идентичные функции?
	public void gameb()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.isQuestContinuationPossible(true))
			return;
		isTalker = Rnd.get(2);

		if(getItemCount(player, Badge_of_Fox) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 40)
				if(isTalker == 1)
				{
					removeItem(player, Badge_of_Fox, 1);
					removeItem(player, EVENT_GLITTMEDAL, 40);
					addItem(player, Badge_of_Wolf, 1);
					show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_34.htm", player), player);
					return;
				}
				else if(isTalker == 0)
				{
					removeItem(player, EVENT_GLITTMEDAL, 40);
					show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_35.htm", player), player);
					return;
				}
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player), player);
			return;
		}

		else if(getItemCount(player, Badge_of_Hyena) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 20)
				if(isTalker == 1)
				{
					removeItem(player, Badge_of_Hyena, 1);
					removeItem(player, EVENT_GLITTMEDAL, 20);
					addItem(player, Badge_of_Fox, 1);
					show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_33.htm", player), player);
					return;
				}
				else if(isTalker == 0)
				{
					removeItem(player, EVENT_GLITTMEDAL, 20);
					show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_35.htm", player), player);
					return;
				}
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player), player);
			return;
		}

		else if(getItemCount(player, Badge_of_Rabbit) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 10)
				if(isTalker == 1)
				{
					removeItem(player, Badge_of_Rabbit, 1);
					removeItem(player, EVENT_GLITTMEDAL, 10);
					addItem(player, Badge_of_Hyena, 1);
					show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_32.htm", player), player);
					return;
				}
				else if(isTalker == 0)
				{
					removeItem(player, EVENT_GLITTMEDAL, 10);
					show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_35.htm", player), player);
					return;
				}
			show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player), player);
			return;
		}

		if(getItemCount(player, EVENT_GLITTMEDAL) >= 5)
			if(isTalker == 1)
			{
				removeItem(player, EVENT_GLITTMEDAL, 5);
				addItem(player, Badge_of_Rabbit, 1);
				show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_31.htm", player), player);
				return;
			}
			else if(isTalker == 0)
			{
				removeItem(player, EVENT_GLITTMEDAL, 5);
				show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_35.htm", player), player);
				return;
			}
		show(Files.read("data/scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player), player);
		return;
	}

	public static void OnReloadMultiSell()
	{
		MultiSellLoaded = false;
		if(_active)
			loadMultiSell();
	}
}