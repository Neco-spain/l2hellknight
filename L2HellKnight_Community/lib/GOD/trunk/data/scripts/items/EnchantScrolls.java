package items;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ChooseInventoryItem;

public class EnchantScrolls implements IItemHandler, ScriptFile
{
	private static final int[] _itemIds = {
		729, 730, 731, 732, 947, 948, 949, 950, 951, 952, 953, 954, 955, 956, 957,
		958, 959, 960, 961, 962, 6569, 6570, 6571, 6572, 6573, 6574, 6575, 6576, 6577, 6578, 13540, 20519, 20520, 20521,
		20522, 22006, 22007, 22008, 22009, 22010, 22011, 22012, 22013, 22014, 22015, 22016, 22017, 22018, 22019, 22020,
		22021, 19447, 19448, 17526, 17527
		};

	public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return;
		L2Player player = (L2Player) playable;

		if(player.getEnchantScroll() != null)
			return;

		player.setEnchantScroll(item);
		player.sendPacket(Msg.SELECT_ITEM_TO_ENCHANT, new ChooseInventoryItem(item.getItemId()));
		return;
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