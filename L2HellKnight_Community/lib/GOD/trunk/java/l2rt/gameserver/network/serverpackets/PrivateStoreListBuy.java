package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PrivateStoreListBuy extends L2GameServerPacket
{
	private int buyer_id;
	private long seller_adena;
	private ConcurrentLinkedQueue<TradeItem> _buyerslist;

	/**
	 * Список вещей в личном магазине покупки, показываемый продающему
	 * @param seller
	 * @param storePlayer
	 */
	public PrivateStoreListBuy(L2Player seller, L2Player storePlayer)
	{
		seller_adena = seller.getAdena();
		buyer_id = storePlayer.getObjectId();

		ConcurrentLinkedQueue<L2ItemInstance> sellerItems = seller.getInventory().getItemsList();
		_buyerslist = new ConcurrentLinkedQueue<TradeItem>();
		_buyerslist.addAll(storePlayer.getBuyList());

		for(TradeItem buyListItem : _buyerslist)
			buyListItem.setCurrentValue(0);

		for(L2ItemInstance sellerItem : sellerItems)
			for(TradeItem buyListItem : _buyerslist)
				if(sellerItem.getItemId() == buyListItem.getItemId() && sellerItem.canBeTraded(seller))
				{
					buyListItem.setCurrentValue(Math.min(buyListItem.getCount(), sellerItem.getCount()));
					continue;
				}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xBE);

		writeD(buyer_id);
		writeQ(seller_adena);
		writeD(_buyerslist.size());
		for(TradeItem buyersitem : _buyerslist)
		{
			L2Item tmp = ItemTemplates.getInstance().getTemplate(buyersitem.getItemId());
			writeD(buyersitem.getObjectId());
			writeD(buyersitem.getItemId());
            writeD(buyersitem.getEquipSlot());
            writeQ(buyersitem.getCurrentValue()); //give max possible sell amount
            writeH(tmp.getType2());
            writeH(buyersitem.getCustomType1());
            writeH(0x00);
            writeD(tmp.getBodyPart());
			writeH(buyersitem.getEnchantLevel());
            writeH(tmp.getType2ForPackets());
            writeD(0x00);// Augmentation Id
            writeD(-1);// Mana
            writeD(-9999);// Temp Life time
			writeH(1); // при 0 итем красный(заблокирован) 
            writeItemElements(buyersitem);
            writeEnchantEffect(buyersitem);
			writeD(0x00);//Visible itemID
            writeD(0x00);// unk
            writeQ(buyersitem.getOwnersPrice());
			writeQ(tmp.getReferencePrice());
			writeQ(buyersitem.getCount()); // maximum possible tradecount


		}
	}
}