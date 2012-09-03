package services;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.SetupGauge;
import l2rt.gameserver.tables.PetDataTable;

public class RideHire extends Functions implements ScriptFile
{
	public String DialogAppend_30827(Integer val)
	{
		if(val == 0)
		{
			L2Player player = (L2Player) getSelf();
			return player.isLangRus() ? "<br>[scripts_services.RideHire:ride_prices|Взять на прокат ездовое животное.]" : "<br>[scripts_services.RideHire:ride_prices|Ride hire mountable pet.]";
		}
		return "";
	}

	public void ride_prices()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		show("data/scripts/services/ride-prices.htm", player, npc);
	}

	public void ride(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		boolean ru = player.isLangRus();
		if(args.length != 3)
		{
			show(ru ? "Некорректные данные" : "Incorrect input", player, npc);
			return;
		}

		if(!L2NpcInstance.canBypassCheck(player, npc))
			return;

		if(player.isTerritoryFlagEquipped() || player.isCombatFlagEquipped())
		{
			player.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
			return;
		}

		if(player.getTransformation() != 0)
		{
			show(ru ? "Вы не можете взять пета в прокат, пока находитесь в режиме трансформации." : "Can't ride while in transformation mode.", player, npc);
			return;
		}

		if(player.getPet() != null || player.isMounted())
		{
			player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
			return;
		}

		int npc_id;

		switch(Integer.parseInt(args[0]))
		{
			case 1:
				npc_id = PetDataTable.WYVERN_ID;
				break;
			case 2:
				npc_id = PetDataTable.STRIDER_WIND_ID;
				break;
			case 3:
				npc_id = PetDataTable.WGREAT_WOLF_ID;
				break;
			case 4:
				npc_id = PetDataTable.WFENRIR_WOLF_ID;
				break;
			default:
				show(ru ? "У меня нет таких питомцев!" : "Unknown pet.", player, npc);
				return;
		}

		if((npc_id == PetDataTable.WYVERN_ID || npc_id == PetDataTable.STRIDER_WIND_ID) && !SiegeManager.getCanRide())
		{
			show(ru ? "Прокат виверн/страйдеров не работает во время осады." : "Can't ride wyvern/strider while Siege in progress.", player, npc);
			return;
		}

		Integer time = Integer.parseInt(args[1]);
		Long price = Long.parseLong(args[2]);

		if(time > 1800)
		{
			show(ru ? "Слишком большое время." : "Too long time to ride.", player, npc);
			return;
		}

		if(player.getAdena() < price)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		player.reduceAdena(price, true);

		doLimitedRide(player, npc_id, time);
	}

	public void doLimitedRide(L2Player player, Integer npc_id, Integer time)
	{
		if(!ride(player, npc_id))
			return;
		player.sendPacket(new SetupGauge(3, time * 1000));
		executeTask(player, "services.RideHire", "rideOver", new Object[0], time * 1000);
	}

	public void rideOver()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		unRide(player);
		show(player.isLangRus() ? "Время проката закончилось. Приходите еще!" : "Ride time is over.<br><br>Welcome back again!", player);
	}

	public void onLoad()
	{
		System.out.println("Loaded Service: Ride Hire");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}