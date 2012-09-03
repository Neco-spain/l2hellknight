package l2rt.gameserver.network.serverpackets;

import java.util.TreeSet;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.Warehouse.WarehouseType;
import l2rt.gameserver.templates.L2Item;

public class WareHouseDepositList extends L2GameServerPacket
{
	private int _whtype;
	private long char_adena;
	private TreeSet<L2ItemInstance> _itemslist = new TreeSet<L2ItemInstance>(Inventory.OrderComparator);

	public WareHouseDepositList(L2Player cha, WarehouseType whtype)
	{
		cha.setUsingWarehouseType(whtype);
		_whtype = whtype.getPacketValue();
		char_adena = cha.getAdena();
		for(L2ItemInstance item : cha.getInventory().getItems())
		{
			if(item != null && item.canBeStored(cha, _whtype == 1))
			{
				_itemslist.add(item);
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x41);
		writeH(_whtype);
		writeQ(char_adena);
		writeH(0x00);//Deposited items count
		writeD(0x00); // 0 = 100 количество слотов
		writeH(_itemslist.size());
		for(L2ItemInstance temp : _itemslist)
		{
			L2Item item = temp.getItem();
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getEquipSlot());
			writeQ(temp.getCount());
			writeH(item.getType2ForPackets());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeD(temp.getAugmentationId());
			writeD(temp.isShadowItem() ? temp.getLifeTimeRemaining() : -1);
			writeD(temp.isTemporalItem() ? temp.getLifeTimeRemaining() : 0);
			writeH(1); // при 0 итем красный
			writeItemElements(temp);
			writeEnchantEffect();
			writeD(0x00);//Visible itemID
			writeD(temp.getObjectId());
		}
	}
}