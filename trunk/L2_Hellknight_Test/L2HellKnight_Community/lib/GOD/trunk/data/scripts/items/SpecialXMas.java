package items;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ShowXMasSeal;

public class SpecialXMas implements IItemHandler, ScriptFile
{
	private static int[] _itemIds = { 5555 };

	public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
	{
		if(!playable.isPlayer())
			return;
		L2Player activeChar = (L2Player) playable;
		int itemId = item.getItemId();

		if(itemId == 5555) // Token of Love
		{
			ShowXMasSeal SXS = new ShowXMasSeal(5555);
			activeChar.broadcastPacket(SXS);
		}
	}

	public int[] getItemIds()
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