package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExVariationCancelResult;
import l2rt.gameserver.network.serverpackets.InventoryUpdate;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.templates.L2Item;

public final class RequestRefineCancel extends L2GameClientPacket
{
	//format: (ch)d
	private int _targetItemObjId;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);

		// cannot remove augmentation from a not augmented item
		if(targetItem == null || !targetItem.isAugmented())
		{
			activeChar.sendPacket(new ExVariationCancelResult(0), Msg.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
			return;
		}

		// get the price
		int price = getRemovalPrice(targetItem.getItem());

		if(price < 0)
			activeChar.sendPacket(new ExVariationCancelResult(0));

		// try to reduce the players adena
		if(activeChar.getAdena() < price)
		{
			activeChar.sendPacket(new ExVariationCancelResult(0), Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		activeChar.reduceAdena(price, true);

		// cancel boni
		targetItem.getAugmentation().removeBoni(activeChar);

		// remove the augmentation
		targetItem.removeAugmentation();

		// send inventory update
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);

		// send system message
		SystemMessage sm = new SystemMessage(SystemMessage.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1);
		sm.addItemName(targetItem.getItemId());
		activeChar.sendPacket(new ExVariationCancelResult(1), iu, sm);

		activeChar.broadcastUserInfo(true);
	}

	public static int getRemovalPrice(L2Item item)
	{
		switch(item.getItemGrade().cry)
		{
			case L2Item.CRYSTAL_C:
				if(item.getCrystalCount() < 1720)
					return 95000;
				else if(item.getCrystalCount() < 2452)
					return 150000;
				else
					return 210000;
			case L2Item.CRYSTAL_B:
				if(item.getCrystalCount() < 1746)
					return 240000;
				else
					return 270000;
			case L2Item.CRYSTAL_A:
				if(item.getCrystalCount() < 2160)
					return 330000;
				else if(item.getCrystalCount() < 2824)
					return 390000;
				else
					return 420000;
			case L2Item.CRYSTAL_S:
				if(item.getCrystalCount() == 10394) // Icarus
					return 920000;
				else if(item.getCrystalCount() == 7050) // Dynasty
					return 720000;
				else if(item.getName().contains("Vesper")) // Vesper
					return 920000;
				else
					return 480000;
				// any other item type is not augmentable
			default:
				return -1;
		}
	}
}