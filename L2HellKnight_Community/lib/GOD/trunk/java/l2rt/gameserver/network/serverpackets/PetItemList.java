package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.instances.L2PetInstance;
import l2rt.gameserver.model.items.L2ItemInstance;

public class PetItemList extends L2GameServerPacket
{
	private L2ItemInstance[] items;

	public PetItemList(L2PetInstance cha)
	{
		items = cha.getInventory().getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xb3);
		writeH(items.length);

		for(L2ItemInstance temp : items)
		{
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
            writeD(temp.getEquipSlot());
			writeQ(temp.getCount());
            writeH(temp.getItem().getType2ForPackets()); // item type2
            writeH(temp.getCustomType1());
            writeH(temp.isEquipped() ? 1 : 0);
            writeD(temp.getBodyPart()); // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
            writeH(temp.getEnchantLevel()); // enchant level
            writeH(temp.getCustomType2());
            writeD(temp.getAugmentationId());
            writeD(temp.isShadowItem() ? temp.getLifeTimeRemaining() : -1);
            writeD(temp.isTemporalItem() ? temp.getLifeTimeRemaining() : 0x00);
			writeItemElements(temp);
			writeEnchantEffect(temp);
			writeD(0x00);//Visible itemID
		}
	}
}