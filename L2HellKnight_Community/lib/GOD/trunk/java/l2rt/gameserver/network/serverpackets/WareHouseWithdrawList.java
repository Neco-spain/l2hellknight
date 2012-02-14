package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.L2ItemInstance.ItemClass;
import l2rt.gameserver.model.items.Warehouse.WarehouseType;
import l2rt.gameserver.templates.L2Item;

import java.util.NoSuchElementException;

public class WareHouseWithdrawList extends L2GameServerPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2; // final - 4?
	public static final int CASTLE = 3;
	public static final int FREIGHT = 4; // final - 1?

	private long _money;
	private L2ItemInstance[] _items;
	private int _type;
	private boolean can_writeImpl = false;

	public WareHouseWithdrawList(L2Player cha, WarehouseType type, ItemClass clss)
	{
		if(cha == null)
			return;

		_money = cha.getAdena();
		_type = type.getPacketValue();
		cha.setUsingWarehouseType(type);
		switch(type)
		{
			case PRIVATE:
				_items = cha.getWarehouse().listItems(clss);
				break;
			case CLAN:
			case CASTLE:
				_items = cha.getClan().getWarehouse().listItems(clss);
				break;
			/*
			 case CASTLE:
			 items = _cha.getClan().getCastleWarehouse().listItems();
			 break;
			 */
			case FREIGHT:
				_items = cha.getFreight().listItems(clss);
				break;
			default:
				throw new NoSuchElementException("Invalid value of 'type' argument");
		}

		if(_items.length == 0)
		{
			cha.sendPacket(Msg.YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE);
			return;
		}

		can_writeImpl = true;
	}

	@Override
	protected final void writeImpl()
	{
		if(!can_writeImpl)
			return;
		writeC(0x42);
		//PUSH Engine.205A535C                      ASCII "hQhd"
		writeH(_type);
		writeQ(_money);
		writeH(_items.length);
		writeH(1); //Неизвестно. На оффе было 1.
		writeD(0); //Неизвестно.
		writeD(0); //Количество занятых ячеек в инвентаре.
		for(L2ItemInstance temp : _items)
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