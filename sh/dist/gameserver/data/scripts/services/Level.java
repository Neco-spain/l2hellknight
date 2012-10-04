package services;

import l2p.gameserver.Config;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.model.base.Experience;

public class Level extends Functions
{
	public void list()
	{
		Player player = getSelf();
		if(!Config.SERVICES_LVL_ENABLED)
		{
			show(HtmCache.getInstance().getNotNull("npcdefault.htm", player), player);
			return;
		}
		String html = null;
		
		html = HtmCache.getInstance().getNotNull("scripts/services/level.htm", player);
		String add = "";
		if(player.getLevel() < Config.SERVICES_LVL_UP_MAX)
			if(player.isLangRus())
				add += "<button value=\"Поднять уровень на 1 (Цена:" + Config.SERVICES_LVL_UP_PRICE + " " + ItemHolder.getInstance().getTemplate(Config.SERVICES_LVL_UP_ITEM).getName() + ") \" action=\"bypass -h scripts_services.Level:up" + "\" width=250 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">" + "</a><br>";
			else
				add += "<button value=\"Raise the level at1 (Price:" + Config.SERVICES_LVL_UP_PRICE + " " + ItemHolder.getInstance().getTemplate(Config.SERVICES_LVL_UP_ITEM).getName() + ") \" action=\"bypass -h scripts_services.Level:up" + "\" width=250 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">" + "</a><br>";
		if(player.getLevel() > Config.SERVICES_LVL_DOWN_MAX)
			if(player.isLangRus())
				add += "<button value=\"Понизить уровень на 1 (Цена:" + Config.SERVICES_LVL_DOWN_PRICE + " " + ItemHolder.getInstance().getTemplate(Config.SERVICES_LVL_DOWN_ITEM).getName() + ") \" action=\"bypass -h scripts_services.Level:down" + "\" width=250 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">" + "</a><br>";
			else
				add += "<button value=\"Lower level at the 1 (Price:" + Config.SERVICES_LVL_DOWN_PRICE + " " + ItemHolder.getInstance().getTemplate(Config.SERVICES_LVL_DOWN_ITEM).getName() + ") \" action=\"bypass -h scripts_services.Level:down" + "\" width=250 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">" + "</a><br>";
		html = html.replaceFirst("%toreplace%", add);
			
		show(html, player);
	}
	
	public void up()
	{
		Player player = getSelf();
		if(!Config.SERVICES_LVL_ENABLED)
		{
			show(HtmCache.getInstance().getNotNull("npcdefault.htm", player), player);
			return;
		}
		int level = player.getLevel()+1;
		if ((Functions.getItemCount(player, Config.SERVICES_LVL_UP_ITEM) > Config.SERVICES_LVL_UP_PRICE))
		{
			Functions.removeItem(player, Config.SERVICES_LVL_UP_ITEM, Config.SERVICES_LVL_UP_PRICE);
                        setLevel(player, level);
		}
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
	}
	
	public void down()
	{
		Player player = getSelf();
		if(!Config.SERVICES_LVL_ENABLED)
		{
			show(HtmCache.getInstance().getNotNull("npcdefault.htm", player), player);
			return;
		}
		int level = player.getLevel()-1;
		if ((Functions.getItemCount(player, Config.SERVICES_LVL_DOWN_ITEM) > Config.SERVICES_LVL_DOWN_PRICE))
		{
			Functions.removeItem(player, Config.SERVICES_LVL_DOWN_ITEM, Config.SERVICES_LVL_DOWN_PRICE);
                        setLevel(player, level);
		}
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
	}
        
	private void setLevel(Player player, int level)
	{
		Long exp_add = Experience.LEVEL[level] - player.getExp();
		player.addExpAndSp(exp_add, 0, 0, 0, false, false);
	}
}