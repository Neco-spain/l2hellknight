package services;

import java.util.Calendar;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.PlaySound;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.taskmanager.DecayTaskManager;
import l2rt.util.Files;
import l2rt.util.Location;
import l2rt.util.Util;

/**
 * Используется для выдачи призов к дню рождения чара
 *
 * @Author: SYS
 * @Date: 04/08/2009
 */
public class Birthday extends Functions implements ScriptFile
{
	private static final int HAT = 10250; // Adventurer Hat (Event)
	private static final int NPC_ALEGRIA = 32600; // Alegria
	private static final int BIRTHDAY_CAKE = 5950;
	private static final String msgNotToday = "data/scripts/services/Birthday-no.htm";
	private static final String msgAlreadyRecived = "data/scripts/services/Birthday-already.htm";
	private static final String msgSpawned = "data/scripts/services/Birthday-spawned.htm";

	public void onLoad()
	{
		System.out.println("Loaded Service: Birthday");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	/**
	 * Вызывается у гейткиперов
	 */
	public void reciveGift()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();

		if(player == null || npc == null || !L2NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;

		if(!isBirthdayToday(player))
		{
			show(Files.read(msgNotToday, player), player, npc);
			return;
		}

		if(isGiftRecivedToday(player))
		{
			show(Files.read(msgAlreadyRecived, player), player, npc);
			return;
		}

		for(L2NpcInstance n : L2World.getAroundNpc(npc))
			if(n.getNpcId() == NPC_ALEGRIA)
			{
				show(Files.read(msgSpawned, player), player, npc);
				return;
			}

		player.sendPacket(new PlaySound(1, "HB01", 0, 0, new Location()));

		try
		{
			Location loc = GeoEngine.findPointToStay(npc.getX(), npc.getY(), npc.getZ(), 40, 60, npc.getReflection().getGeoIndex());
			loc.setH(Util.getHeadingTo(loc, player.getLoc()));
			L2Spawn spawn = new L2Spawn(NpcTable.getTemplate(NPC_ALEGRIA));
			spawn.setLoc(loc);
			spawn.doSpawn(true);

			ThreadPoolManager.getInstance().scheduleAi(new DeSpawnScheduleTimerTask(spawn), 180000, false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Вызывается у NPC Alegria
	 */
	public void reciveGiftAlegria()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();

		if(player == null || npc == null || !L2NpcInstance.canBypassCheck(player, player.getLastNpc()) || npc.isBusy())
			return;

		if(!isBirthdayToday(player))
		{
			show(Files.read(msgNotToday, player), player, npc);
			return;
		}

		if(isGiftRecivedToday(player))
		{
			show(Files.read(msgAlreadyRecived, player), player, npc);
			return;
		}

		npc.altUseSkill(SkillTable.getInstance().getInfo(BIRTHDAY_CAKE, 1), player);
		addItem(player, HAT, 1);
		show(Files.read("data/html/default/32600-2.htm ", player), player, npc);

		long now = System.currentTimeMillis() / 1000;
		player.setVar("Birthday", String.valueOf(now));

		DecayTaskManager.getInstance().addDecayTask(npc);
		npc.setBusy(true);
	}

	/**
	 * Вернет true если у чара сегодня день рождения
	 */
	private boolean isBirthdayToday(L2Player player)
	{
		if(player.getCreateTime() == 0)
			return false;

		Calendar create = Calendar.getInstance();
		create.setTimeInMillis(player.getCreateTime());
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());

		return create.get(Calendar.MONTH) == now.get(Calendar.MONTH) && create.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH) && create.get(Calendar.YEAR) != now.get(Calendar.YEAR);
	}

	/**
	 * Возвращает true если чар уже получал сегодня подарок на свой день рождения
	 */
	private boolean isGiftRecivedToday(L2Player player)
	{
		int lastBirthday = 0;
		try
		{
			String var = player.getVar("Birthday");
			if(var != null && !var.equals("null"))
				lastBirthday = Integer.parseInt(var);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return true;
		}

		Calendar birthday = Calendar.getInstance();
		birthday.setTimeInMillis(lastBirthday * 1000L);
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());

		return birthday.get(Calendar.YEAR) == now.get(Calendar.YEAR) && birthday.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);
	}

	public class DeSpawnScheduleTimerTask implements Runnable
	{
		L2Spawn spawned = null;

		public DeSpawnScheduleTimerTask(L2Spawn spawn)
		{
			spawned = spawn;
		}

		public void run()
		{
			try
			{
				spawned.getLastSpawn().decayMe();
				spawned.getLastSpawn().deleteMe();
			}
			catch(Throwable t)
			{}
		}
	}
}