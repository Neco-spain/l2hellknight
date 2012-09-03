package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PrivateStoreListSell extends L2GameServerPacket
{
	private int seller_id;
	private long buyer_adena;
	private final boolean _package;
	private ConcurrentLinkedQueue<TradeItem> _sellList;

	/**
	 * Список вещей в личном магазине продажи, показываемый покупателю
	 * @param buyer
	 * @param seller
	 */
	public PrivateStoreListSell(L2Player buyer, L2Player seller)
	{
		seller_id = seller.getObjectId();
		buyer_adena = buyer.getAdena();
		_package = seller.getPrivateStoreType() == L2Player.STORE_PRIVATE_SELL_PACKAGE;
		_sellList = seller.getSellList();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xA1);
		writeD(seller_id);
		writeD(_package ? 1 : 0);
		writeQ(buyer_adena);

		writeD(_sellList.size());
		for(TradeItem ti : _sellList)
		{
			L2Item tempItem = ItemTemplates.getInstance().getTemplate(ti.getItemId());
			writeD(ti.getObjectId());
			writeD(ti.getItemId());
            writeD(ti.getEquipSlot());
			writeQ(ti.getCount());
			writeH(tempItem.getType2ForPackets());
            writeH(ti.getCustomType1());
            writeH(0x00);
            writeD(tempItem.getBodyPart());
            writeH(ti.getEnchantLevel());
            writeH(ti.getCustomType2());
            writeD(0x00);// Augmentation Id
            writeD(-1);// Mana
            writeD(-9999);// Temp Life time
			writeH(1); // при 0 итем красный(заблокирован) 
            writeItemElements(ti);
			writeEnchantEffect(ti);
			writeD(0x00);//Visible itemID
            writeQ(ti.getOwnersPrice());
			writeQ(tempItem.getReferencePrice() * 2);
		}
	}
}