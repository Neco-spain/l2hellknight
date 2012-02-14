package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.util.GArray;

public class PetInventoryUpdate extends L2GameServerPacket
{
	private GArray<L2ItemInstance> _items;

	public PetInventoryUpdate()
	{
		_items = new GArray<L2ItemInstance>();
	}

	public PetInventoryUpdate(GArray<L2ItemInstance> items)
	{
		_items = items;
	}

	public PetInventoryUpdate addNewItem(L2ItemInstance item)
	{
		item.setLastChange(L2ItemInstance.ADDED);
		_items.add(item);
		return this;
	}

	public PetInventoryUpdate addModifiedItem(L2ItemInstance item)
	{
		item.setLastChange(L2ItemInstance.MODIFIED);
		_items.add(item);
		return this;
	}

	public PetInventoryUpdate addRemovedItem(L2ItemInstance item)
	{
		item.setLastChange(L2ItemInstance.REMOVED);
		_items.add(item);
		return this;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xb4);
		writeH(_items.size());
		for(L2ItemInstance temp : _items)
		{
			writeH(temp.getLastChange());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
            writeD(temp.getEquipSlot());
            writeQ(temp.getCount());
            writeH(temp.getItem().getType2ForPackets());
            writeH(temp.getCustomType1());
            writeH(temp.isEquipped() ? 1 : 0);
            writeD(temp.getBodyPart());
            writeH(temp.getEnchantLevel());
            writeH(temp.getCustomType2());
            writeH(temp.getAugmentationId());
			writeH(0x00);
            writeD(temp.isShadowItem() ? temp.getLifeTimeRemaining() : -1);
            writeD(temp.isTemporalItem() ? temp.getLifeTimeRemaining() : 0x00);
			writeItemElements(temp);
			writeEnchantEffect(temp);
			writeD(0x00);//Visible itemID
		}
	}
}