package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2TradeList;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.L2Item;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PrivateStoreManageListBuy extends L2GameServerPacket
{
	private ConcurrentLinkedQueue<TradeItem> buyList = new ConcurrentLinkedQueue<TradeItem>();
	private int buyer_id;
	private long buyer_adena;
	private L2TradeList _list;

	/**
	 * Окно управления личным магазином продажи
	 * @param buyer
	 */
	public PrivateStoreManageListBuy(L2Player buyer)
	{
		buyer_id = buyer.getObjectId();
		buyer_adena = buyer.getAdena();

		int _id, body_part, type2;
		long count, store_price, owner_price;
		L2Item tempItem;
        buyList = buyer.getBuyList();

		_list = new L2TradeList(0);
		for(L2ItemInstance item : buyer.getInventory().getItems())
			if(item != null && item.canBeTraded(buyer) && item.getItemId() != L2Item.ITEM_ID_ADENA)
			{
				for(TradeItem ti : buyer.getBuyList())
					if(ti.getItemId() == item.getItemId())
						continue;
				_list.addItem(item);
			}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xBD);
		//section 1
		writeD(buyer_id);
		writeQ(buyer_adena);

		//section2
		writeD(_list.getItems().size());//for potential sells
		for(L2ItemInstance temp : _list.getItems())
		{
            writeD(temp.getObjectId());
            writeD(temp.getItemId());
            writeD(temp.getEquipSlot());
            writeQ(temp.getCount());
            writeH(temp.getItem().getType2());
            writeH(temp.getCustomType1());
            writeH(temp.isEquipped() ? 1 : 0);
            writeD(temp.getBodyPart());
            writeH(temp.getEnchantLevel());
            writeH(temp.getCustomType2());
            writeD(temp.getAugmentationId());
            writeD(temp.isShadowItem() ? temp.getLifeTimeRemaining() : -1);
            writeD(temp.isTemporalItem() ? temp.getLifeTimeRemaining() : 0x00);
			writeItemElements(temp);
			writeEnchantEffect(temp);
            writeQ(temp.getPriceToSell());
		}

		//section 3
		writeD(buyList.size());//count for any items already added for sell
        for (TradeItem item : buyList) {
            writeD(item.getObjectId());//objId
            writeD(item.getItemId());
            writeD(item.getEquipSlot());
            writeQ(item.getCount());
            writeH(item.getItem().getType2());
            writeH(item.getCustomType1());
            writeH(0x00);
            writeD(item.getItem().getBodyPart());
            writeH(item.getEnchantLevel());
            writeH(item.getCustomType2());
            writeD(0x00);// Augmentation ID
            writeD(-1);// Mana
            writeD(0x00);// Life Time Remaining
            writeH(item.getAttackElement()[0]); // attack element (-2 - none)
            writeH(item.getAttackElement()[1]); // attack element value
            writeH(item.getDefenceFire()); // водная стихия (fire pdef)
            writeH(item.getDefenceWater()); // огненная стихия (water pdef)
            writeH(item.getDefenceWind()); // земляная стихия (wind pdef)
            writeH(item.getDefenceEarth()); // воздушная стихия (earth pdef)
            writeH(item.getDefenceHoly()); // темная стихия (holy pdef)
            writeH(item.getDefenceUnholy()); // светлая стихия (dark pdef)
            writeEnchantEffect();
			writeD(0x00);//Visible itemID
            writeQ(item.getOwnersPrice());
            writeQ(item.getStorePrice());
            writeQ(0x00);//unknown
        }
	}

    @Deprecated
	static class BuyItemInfo
	{
		public int _id, body_part, type2;
		public long count, store_price, owner_price;

		public BuyItemInfo(int __id, long count2, long store_price2, int _body_part, int _type2, long owner_price2)
		{
			_id = __id;
			count = count2;
			store_price = store_price2;
			body_part = _body_part;
			type2 = _type2;
			owner_price = owner_price2;
		}
	}
}