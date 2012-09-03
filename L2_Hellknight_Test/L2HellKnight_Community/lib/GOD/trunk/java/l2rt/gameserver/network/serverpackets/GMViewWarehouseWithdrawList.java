package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.L2ItemInstance.ItemClass;

public class GMViewWarehouseWithdrawList extends L2GameServerPacket
{
	private final L2ItemInstance[] _items;
	private String _charName;
	private long _charAdena;

	public GMViewWarehouseWithdrawList(L2Player cha)
	{
		_charName = cha.getName();
		_charAdena = cha.getAdena();
		_items = cha.getWarehouse().listItems(ItemClass.ALL);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x9b);
		writeS(_charName);
		writeQ(_charAdena);
		writeH(_items.length);
		for(L2ItemInstance temp : _items)
		{
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
            writeD(temp.getEquipSlot());
			writeQ(temp.getCount());
			writeH(temp.getItem().getType2ForPackets());
			writeH(temp.getCustomType1());
            writeD(temp.getBodyPart());
            writeH(temp.getEnchantLevel());
            writeH(temp.getCustomType2());
            writeH(temp.getAugmentationId());
			writeH(0x00);
            writeD(temp.isShadowItem() ? temp.getLifeTimeRemaining() : -1);
            writeD(temp.isTemporalItem() ? temp.getLifeTimeRemaining() : 0x00); // limited time item life remaining
            writeItemElements(temp);
            writeEnchantEffect(temp);
            writeD(0x00);
			writeD(0x00);
		}
	}
}