package l2rt.gameserver.model.items.listeners;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;

public final class ItemAugmentationListener implements PaperdollListener
{
	private Inventory _inv;

	public ItemAugmentationListener(Inventory inv)
	{
		_inv = inv;
	}

	public void notifyUnequipped(int slot, L2ItemInstance item)
	{
		if(_inv.getOwner() == null || !_inv.getOwner().isPlayer() || !item.isEquipable())
			return;

		if(item.isAugmented())
		{
			L2Player player = _inv.getOwner().getPlayer();
			item.getAugmentation().removeBoni(player);
			player.updateStats();
		}
	}

	public void notifyEquipped(int slot, L2ItemInstance item)
	{
		if(_inv.getOwner() == null || !_inv.getOwner().isPlayer() || !item.isEquipable())
			return;

		if(item.isAugmented())
		{
			L2Player player = _inv.getOwner().getPlayer();
			item.getAugmentation().applyBoni(player);
			player.updateStats();
		}
	}
}