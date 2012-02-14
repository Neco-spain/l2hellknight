package l2rt.gameserver.model.instances;

import l2rt.Config;
import l2rt.gameserver.TradeController;
import l2rt.gameserver.TradeController.NpcTradeList;
import l2rt.gameserver.instancemanager.TownManager;
import l2rt.gameserver.model.L2Multisell;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.network.serverpackets.BuyList;
import l2rt.gameserver.network.serverpackets.ExBuySellList;
import l2rt.gameserver.network.serverpackets.ShopPreviewList;
import l2rt.gameserver.templates.L2NpcTemplate;

import java.io.File;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class L2MerchantInstance extends L2NpcInstance
{
	private static Logger _log = Logger.getLogger(L2MerchantInstance.class.getName());

	public L2MerchantInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		String temp = "data/html/merchant/" + pom + ".htm";
		File mainText = new File(temp);
		if(mainText.exists())
			return temp;

		temp = "data/html/teleporter/" + pom + ".htm";
		mainText = new File(temp);
		if(mainText.exists())
			return temp;

		temp = "data/html/petmanager/" + pom + ".htm";
		mainText = new File(temp);
		if(mainText.exists())
			return temp;

		temp = "data/html/default/" + pom + ".htm";
		mainText = new File(temp);
		if(mainText.exists())
			return temp;

		return "data/html/teleporter/" + pom + ".htm";
	}

	private void showWearWindow(L2Player player, int val)
	{
		if(!player.getPlayerAccess().UseShop)
			return;

		player.tempInventoryDisable();
		NpcTradeList list = TradeController.getInstance().getBuyList(val);

		if(list != null)
		{
			ShopPreviewList bl = new ShopPreviewList(list, player.getAdena(), player.expertiseIndex);
			player.sendPacket(bl);
		}
		else
		{
			_log.warning("no buylist with id:" + val);
			player.sendActionFailed();
		}
	}

	protected void showShopWindow(L2Player player, int listId, boolean tax)
	{
		if(!player.getPlayerAccess().UseShop)
			return;

		double taxRate = 0;

		if(tax)
		{
			Castle castle = getCastle(player);
			if(castle != null)
				taxRate = castle.getTaxRate();
		}

		player.tempInventoryDisable();
		NpcTradeList list = TradeController.getInstance().getBuyList(listId);
		if(list == null || list.getNpcId() == getNpcId())
		{
			player.sendPacket(new BuyList(list, player, taxRate));
			player.sendPacket(new ExBuySellList(list, player, taxRate));
		}
		else
		{
			_log.warning("[L2MerchantInstance] possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			_log.warning("buylist id:" + listId + " / list_npc = " + list.getNpcId() + " / npc = " + getNpcId());
		}
	}

	protected void showShopWindow(L2Player player)
	{
		showShopWindow(player, 0, false);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		if(actualCommand.equalsIgnoreCase("Buy") || actualCommand.equalsIgnoreCase("Sell"))
		{
			int val = 0;
			if(st.countTokens() > 0)
				val = Integer.parseInt(st.nextToken());
			showShopWindow(player, val, true);
		}
		else if(actualCommand.equalsIgnoreCase("Wear") && Config.WEAR_TEST_ENABLED)
		{
			if(st.countTokens() < 1)
				return;
			int val = Integer.parseInt(st.nextToken());
			showWearWindow(player, val);
		}
		else if(actualCommand.equalsIgnoreCase("Multisell"))
		{
			if(st.countTokens() < 1)
				return;
			int val = Integer.parseInt(st.nextToken());
			Castle castle = getCastle(player);
			L2Multisell.getInstance().SeparateAndSend(val, player, castle != null ? castle.getTaxRate() : 0);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public Castle getCastle(L2Player player)
	{
		if(getReflection().getId() < 0)
		{
			if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX)
				return null;
			String var = player.getVar("backCoords");
			if(var != null && !var.isEmpty())
			{
				String[] loc = var.split(",");
				return TownManager.getInstance().getClosestTown(Integer.parseInt(loc[0]), Integer.parseInt(loc[1])).getCastle();
			}
			return TownManager.getInstance().getClosestTown(this).getCastle();
		}
		return super.getCastle(player);
	}
}