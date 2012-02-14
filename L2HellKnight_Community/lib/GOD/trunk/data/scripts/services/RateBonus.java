package services;

import java.sql.SQLException;
import java.util.Date;

import l2rt.Config;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.mysql;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Files;
import l2rt.util.Log;

public class RateBonus extends Functions implements ScriptFile
{
	public void list()
	{
		L2Player player = (L2Player) getSelf();
		String html;
		if(player.getNetConnection().getBonus() == 1)
		{
			html = Files.read("data/scripts/services/RateBonus.htm", player);

			String add = new String();
			for(int i = 0; i < Config.SERVICES_RATE_BONUS_DAYS.length; i++)
				add += "<a action=\"bypass -h scripts_services.RateBonus:get " + i + "\">" //
						+ (int) (Config.SERVICES_RATE_BONUS_VALUE[i] * 100 - 100) + //
						"% for " + Config.SERVICES_RATE_BONUS_DAYS[i] + //
						" days - " + Config.SERVICES_RATE_BONUS_PRICE[i] + //
						" " + ItemTemplates.getInstance().getTemplate(Config.SERVICES_RATE_BONUS_ITEM[i]).getName() + "</a><br>";

			html = html.replaceFirst("%toreplace%", add);
		}
		else if(player.getNetConnection().getBonus() > 1)
		{
			long endtime = player.getNetConnection().getBonusExpire();
			if(endtime >= 0)
				html = Files.read("data/scripts/services/RateBonusAlready.htm", player).replaceFirst("endtime", new Date(endtime * 1000L).toString());
			else
				html = Files.read("data/scripts/services/RateBonusInfinite.htm", player);
		}
		else
			html = Files.read("data/scripts/services/RateBonusNo.htm", player);
		show(html, player);
	}

	public void get(String[] param)
	{
		L2Player player = (L2Player) getSelf();

		int i = Integer.parseInt(param[0]);

		L2Item item = ItemTemplates.getInstance().getTemplate(Config.SERVICES_RATE_BONUS_ITEM[i]);
		L2ItemInstance pay = player.getInventory().getItemByItemId(item.getItemId());
		if(pay != null && pay.getCount() >= Config.SERVICES_RATE_BONUS_PRICE[i])
		{
			player.getInventory().destroyItem(pay, Config.SERVICES_RATE_BONUS_PRICE[i], true);
			Log.add(player.getName() + "|" + player.getObjectId() + "|rate bonus|" + Config.SERVICES_RATE_BONUS_VALUE[i] + "|" + Config.SERVICES_RATE_BONUS_DAYS[i] + "|", "services");
			try
			{
				mysql.setEx(L2DatabaseFactory.getInstanceLogin(), "UPDATE `accounts` SET `bonus`=?,`bonus_expire`=UNIX_TIMESTAMP()+" + Config.SERVICES_RATE_BONUS_DAYS[i] + "*24*60*60 WHERE `login`=?", Config.SERVICES_RATE_BONUS_VALUE[i], player.getAccountName());
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
			player.getNetConnection().setBonus(Config.SERVICES_RATE_BONUS_VALUE[i]);
			player.getNetConnection().setBonusExpire(System.currentTimeMillis() / 1000 + Config.SERVICES_RATE_BONUS_DAYS[i] * 24 * 60 * 60);
			player.restoreBonus();
			if(player.getParty() != null)
				player.getParty().recalculatePartyData();
			show(Files.read("data/scripts/services/RateBonusGet.htm", player), player);
		}
		else if(Config.SERVICES_RATE_BONUS_ITEM[i] == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
	}

	public void howtogetcol()
	{
		show("data/scripts/services/howtogetcol.htm", (L2Player) getSelf());
	}

	public void onLoad()
	{
		System.out.println("Loaded Service: Rate bonus");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}