package services;

import l2rt.Config;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.network.clientpackets.EnterWorld;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.HWID;

public class Window extends Functions implements ScriptFile
{
	public void get()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(!Config.SERVICES_WINDOW_ENABLED || !Config.PROTECT_ENABLE || Config.PROTECT_GS_MAX_SAME_HWIDs == 0 || player.getNetConnection() == null || !player.getNetConnection().protect_used)
		{
			show("Сервис отключен.", player);
			return;
		}

		int size = HWID.getBonus(player, "window", EnterWorld.WindowsBonusComparator);

		if(Config.PROTECT_GS_MAX_SAME_HWIDs + size >= Config.SERVICES_WINDOW_MAX)
		{
			player.sendMessage("Already max count.");
			return;
		}

		L2Item item = ItemTemplates.getInstance().getTemplate(Config.SERVICES_WINDOW_ITEM);
		L2ItemInstance pay = player.getInventory().getItemByItemId(item.getItemId());
		if(pay != null && pay.getCount() >= Config.SERVICES_WINDOW_PRICE)
		{
			player.getInventory().destroyItem(pay, Config.SERVICES_WINDOW_PRICE, true);
			HWID.setBonus(player.getHWID(), "window", size + 1);
			player.sendMessage("Max window count is now " + (Config.PROTECT_GS_MAX_SAME_HWIDs + size + 1));
		}
		else if(Config.SERVICES_WINDOW_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);

		show();
	}

	public void show()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(!Config.SERVICES_WINDOW_ENABLED || !Config.PROTECT_ENABLE || Config.PROTECT_GS_MAX_SAME_HWIDs == 0 || player.getNetConnection() == null || !player.getNetConnection().protect_used)
		{
			show("Сервис отключен.", player);
			return;
		}

		L2Item item = ItemTemplates.getInstance().getTemplate(Config.SERVICES_WINDOW_ITEM);

		String out = "";

		out += "<html><body>Дополнительные окна:";
		out += "<br><br><table>";
		out += "<tr><td>Текущее число:</td><td>" + (Config.PROTECT_GS_MAX_SAME_HWIDs + HWID.getBonus(player, "window")) + " окон</td></tr>";
		out += "<tr><td>Максимальное число:</td><td>" + Config.SERVICES_WINDOW_MAX + " окон</td></tr>";
		out += "<tr><td>Стоимость расширения:</td><td>" + Config.SERVICES_WINDOW_PRICE + " " + item.getName() + "</td></tr>";
		out += "<tr><td>Время действия:</td><td>" + Config.SERVICES_WINDOW_DAYS + " дней</td></tr>";
		out += "</table><br><br>";
		out += "<button width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.Window:get\" value=\"Расширить\">";
		out += "</body></html>";

		show(out, player);
	}

	public void onLoad()
	{
		System.out.println("Loaded Service: Window");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}