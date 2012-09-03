package services;

import l2rt.Config;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;

public class NickColor extends Functions implements ScriptFile
{
	public void list()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		StringBuilder append = new StringBuilder();
		append.append("You can change nick color for small price ").append(Config.SERVICES_CHANGE_NICK_COLOR_PRICE).append(" ").append(ItemTemplates.getInstance().getTemplate(Config.SERVICES_CHANGE_NICK_COLOR_ITEM).getName()).append(".");
		append.append("<br>Possible colors:<br>");
		for(String color : Config.SERVICES_CHANGE_NICK_COLOR_LIST)
			append.append("<br><a action=\"bypass -h scripts_services.NickColor:change ").append(color).append("\"><font color=\"").append(color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2)).append("\">").append(color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2)).append("</font></a>");
		append.append("<br><a action=\"bypass -h scripts_services.NickColor:change FFFFFF\"><font color=\"FFFFFF\">Revert to default (free)</font></a>");
		show(append.toString(), player, null);
	}

	public void change(String[] param)
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		if(param[0].equalsIgnoreCase("FFFFFF"))
		{
			player.setNameColor(Integer.decode("0xFFFFFF"));
			player.broadcastUserInfo(true);
			return;
		}

		L2Item item = ItemTemplates.getInstance().getTemplate(Config.SERVICES_CHANGE_NICK_COLOR_ITEM);
		L2ItemInstance pay = player.getInventory().getItemByItemId(item.getItemId());
		if(pay != null && pay.getCount() >= Config.SERVICES_CHANGE_NICK_COLOR_PRICE)
		{
			player.setNameColor(Integer.decode("0x" + param[0]));
			player.getInventory().destroyItem(pay, Config.SERVICES_CHANGE_NICK_COLOR_PRICE, true);
			player.broadcastUserInfo(true);
		}
		else if(Config.SERVICES_CHANGE_NICK_COLOR_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
	}

	public void onLoad()
	{
		System.out.println("Loaded Service: Nick color change");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}