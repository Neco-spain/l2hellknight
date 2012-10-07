package handler.items;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.SSQStatus;
import l2p.gameserver.serverpackets.ShowXMasSeal;

public class Books extends SimpleItemHandler
{
	private static final int[] ITEM_IDS = new int[] { 5555, 5707 };

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();

		switch(itemId)
		{
			case 5555:
				player.sendPacket(new ShowXMasSeal(5555));
				break;
			case 5707:
				player.sendPacket(new SSQStatus(player, 1));
				break;
			default:
				return false;
		}

		return true;
	}
}
