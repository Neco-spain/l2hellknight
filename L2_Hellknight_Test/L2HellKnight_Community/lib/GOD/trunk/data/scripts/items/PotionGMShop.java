package items;

import l2rt.Config;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;

public class PotionGMShop implements IItemHandler, ScriptFile
{
	
	private static final int[] _itemIds = { Config.ITEM_MOLL_ID_1, Config.ITEM_MOLL_ID_2, Config.ITEM_MOLL_ID_3 };

	public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return;
		L2Player player = (L2Player) playable;
		int itemId = item.getItemId();
		int reduce = (player.getNetConnection().getPoint());
		if (itemId == Config.ITEM_MOLL_ID_1)
		{
			player.getNetConnection().setPoint(reduce+Config.ITEM_MOLL_KOL_1);
			player.sendMessage("You have successfully received " + (Config.ITEM_MOLL_KOL_1) + " points.");
			Functions.removeItem(player, itemId, 1);
		}
		if (itemId == Config.ITEM_MOLL_ID_2)
		{
			player.getNetConnection().setPoint(reduce+Config.ITEM_MOLL_KOL_2);
			player.sendMessage("You have successfully received " + (Config.ITEM_MOLL_KOL_2) + " points.");
			Functions.removeItem(player, itemId, 1);
		}
		if (itemId == Config.ITEM_MOLL_ID_3)
		{
			player.getNetConnection().setPoint(reduce+Config.ITEM_MOLL_KOL_3);
			player.sendMessage("You have successfully received " + (Config.ITEM_MOLL_KOL_3) + " points.");
			Functions.removeItem(player, itemId, 1);
		}
	}

	public final int[] getItemIds()
	{
		return _itemIds;
	}

	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}