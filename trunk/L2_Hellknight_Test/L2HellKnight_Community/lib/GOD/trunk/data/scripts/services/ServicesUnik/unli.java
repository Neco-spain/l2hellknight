package services.ServicesUnik;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Files;
import l2rt.util.Util;

public class unli extends Functions implements ScriptFile
{
	

	public String DialogAppend_50009(Integer val)
	{
		return Files.read("data/scripts/services/ServicesUnik/index.htm");
	}
	
	public void rename()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		String append = "!Rename";
		append += "<br>";
		append += "<font color=\"LEVEL\">" + new CustomMessage("scripts.services.Rename.RenameFor", getSelf()).addString(Util.formatAdena(Config.SERVICES_CHANGE_NICK_PRICE)).addItemName(Config.SERVICES_CHANGE_NICK_ITEM) + "</font>";
		append += "<table>";
		append += "<tr><td>" + new CustomMessage("scripts.services.Rename.NewName", getSelf()) + " <edit var=\"new_name\" width=80></td></tr>";
		append += "<tr><td></td></tr>";
		append += "<tr><td><button value=\"" + new CustomMessage("scripts.services.Rename.RenameButton", getSelf()) + "\" action=\"bypass -h scripts_services.Rename:rename $new_name\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>";
		append += "</table>";
		show(append, player);
	}
	
	public void colorNick()
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
	
	public void colorTitle()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		StringBuilder append = new StringBuilder();
		append.append("You can change nick color for small price ").append(Config.SERVICES_CHANGE_NICK_COLOR_PRICE).append(" ").append(ItemTemplates.getInstance().getTemplate(Config.SERVICES_CHANGE_NICK_COLOR_ITEM).getName()).append(".");
		append.append("<br>Possible colors:<br>");
		for(String color : Config.SERVICES_CHANGE_NICK_COLOR_LIST)
			append.append("<br><a action=\"bypass -h scripts_services.ServicesUnik.unli:change ").append(color).append("\"><font color=\"").append(color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2)).append("\">").append(color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2)).append("</font></a>");
		append.append("<br><a action=\"bypass -h scripts_services.ServicesUnik.unli:change FFFFFF\"><font color=\"FFFFFF\">Revert to default (free)</font></a>");
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
			player.setTitleColor(Integer.decode("0x" + param[0]));
			player.getInventory().destroyItem(pay, Config.SERVICES_CHANGE_NICK_COLOR_PRICE, true);
			player.broadcastUserInfo(true);
		}
		else if(Config.SERVICES_CHANGE_NICK_COLOR_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
	}
	
	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void onReload() {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void onShutdown() {
		// TODO Auto-generated method stub
		
	}
}