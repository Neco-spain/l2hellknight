package services;


import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.utils.Util;

public class Delevel extends Functions implements ScriptFile
{
	public void delevel_page()
	{
		Player player = (Player) getSelf();
		if(player == null)
		{
			return;
		}
		String append = "ДеЛвл";
		append += "<br>";
		append += "<font color=\"LEVEL\">" + new CustomMessage("scripts.services.Delevel.DelevelFor", getSelf()).addString(Util.formatAdena(Config.SERVICES_DELEVEL_COUNT)).addItemName(Config.SERVICES_DELEVEL_ITEM) + "</font>";
		append += "<table>";
		append += "<tr><td></td></tr>";
		append += "<tr><td><button value=\"Делевел\" action=\"bypass -h scripts_services.Delevel:delevel\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
		append += "</table>";
		show(append, player);
	}

	public void delevel()
	{
		Player player = (Player) getSelf();
		if(!Config.SERVICES_DELEVEL_ENABLED)
		{
			player.sendMessage("Данный сервис не доступен.");
			return;
		}
		if(player.getLevel() <= Config.SERVICES_DELEVEL_MIN_LEVEL)
		{
			player.sendMessage("Данный сервис доступен персонажам с " + Config.SERVICES_DELEVEL_MIN_LEVEL + " уровня.");
			return;
		}
		else if(getItemCount(player, Config.SERVICES_DELEVEL_ITEM) < Config.SERVICES_DELEVEL_COUNT)
		{
			player.sendMessage("У вас недостаточно предметов.");
			return;
		}
		else
		{
			long pXp = player.getExp();
			long tXp = Experience.LEVEL[(player.getLevel() - 1)];
			if(pXp <= tXp)
			{
				return;
			}
			removeItem(player, Config.SERVICES_DELEVEL_ITEM, Config.SERVICES_DELEVEL_COUNT);
			player.addExpAndSp(-(pXp - tXp), 0, 0, 0, false, false);
		}
	}

	public void onLoad()
	{
	}

	public void onReload()
	{
	}

	public void onShutdown()
	{
	}
}