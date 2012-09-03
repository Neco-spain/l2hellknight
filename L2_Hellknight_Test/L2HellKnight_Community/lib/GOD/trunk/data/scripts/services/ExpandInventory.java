package services;

import l2rt.Config;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;

public class ExpandInventory extends Functions implements ScriptFile
{
	public void get()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(!Config.SERVICES_EXPAND_INVENTORY_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}

		if(player.getInventoryLimit() >= Config.SERVICES_EXPAND_INVENTORY_MAX)
		{
			player.sendMessage("Already max count.");
			return;
		}

		L2Item item = ItemTemplates.getInstance().getTemplate(Config.SERVICES_EXPAND_INVENTORY_ITEM);
		L2ItemInstance pay = player.getInventory().getItemByItemId(item.getItemId());
		if(pay != null && pay.getCount() >= Config.SERVICES_EXPAND_INVENTORY_PRICE)
		{
			player.getInventory().destroyItem(pay, Config.SERVICES_EXPAND_INVENTORY_PRICE, true);
			player.setExpandInventory(player.getExpandInventory() + 1);
			player.setVar("ExpandInventory", String.valueOf(player.getExpandInventory()));
			player.sendMessage("Inventory capacity is now " + player.getInventoryLimit());
		}
		else if(Config.SERVICES_EXPAND_INVENTORY_ITEM == 57)
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

		if(!Config.SERVICES_EXPAND_INVENTORY_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}

		L2Item item = ItemTemplates.getInstance().getTemplate(Config.SERVICES_EXPAND_INVENTORY_ITEM);

		String out = "";

		out += "<html><body>Расширение инвентаря";
		out += "<br><br><table>";
		out += "<tr><td>Текущий размер:</td><td>" + player.getInventoryLimit() + "</td></tr>";
		out += "<tr><td>Максимальный размер:</td><td>" + Config.SERVICES_EXPAND_INVENTORY_MAX + "</td></tr>";
		out += "<tr><td>Стоимость слота:</td><td>" + Config.SERVICES_EXPAND_INVENTORY_PRICE + " " + item.getName() + "</td></tr>";
		out += "</table><br><br>";
		out += "<button width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h scripts_services.ExpandInventory:get\" value=\"Расширить\">";
		out += "</body></html>";

		show(out, player);
	}

	public void onLoad()
	{
		System.out.println("Loaded Service: Expand Inventory");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}