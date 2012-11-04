package services;


import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.templates.item.ItemTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Buemo
 * @editor RuleZzz
 * @update Buemo
 * @date 30.12.11
 */
public class SellPcService extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(Player.class);


	public void dialog()
	{
		Player player = getSelf();
		if(player == null)
			return;

		show("scripts/services/SellPcService.htm", player);
	}


	public void pay(String[] param)
	{
		Player player = getSelf();
		if(player == null)
			return;

		int points = Integer.parseInt(param[0]);    //поинты (очки)
		int itemId = Integer.parseInt(param[1]);    //ид предмета, который взымается
		int itemCount = Integer.parseInt(param[2]); //количество предмета, который взымается

		ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId); //id итема

		if (item == null)
			return;

		ItemInstance pay = player.getInventory().getItemByItemId(item.getItemId());
		if(pay != null && pay.getCount() >= itemCount) //кол-во денег
		{
			player.addPcBangPoints(points, false);
			player.getInventory().destroyItem(pay, itemCount);
			player.sendMessage(player.isLangRus() ? "Вы приобрели " + points + " PC-Очков" : "You have purchased " + points + " PC-Points");
		} else
			player.sendMessage(player.isLangRus() ? "У вас недостаточно " + item.getName() : "You are not " + item.getName());
	}

	public void onLoad()
	{
		_log.info("Loaded Service: SellPcService");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}
