package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.TradeController.NpcTradeList;
import l2rt.gameserver.model.TradeItem;
import l2rt.util.GArray;

/**
 * Format: c ddh[hdddhhd]
 * c - id (0xE8)
 *
 * d - money
 * d - manor id
 * h - size
 * [
 * h - item type 1
 * d - object id
 * d - item id
 * d - count
 * h - item type 2
 * h
 * d - price
 * ]
 */
public final class BuyListSeed extends L2GameServerPacket
{
	private int _manorId;
	private GArray<TradeItem> _list = new GArray<TradeItem>();
	private long _money;

	public BuyListSeed(NpcTradeList list, int manorId, long currentMoney)
	{
		_money = currentMoney;
		_manorId = manorId;
		_list = list.getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xe9);

		writeQ(_money); // current money
		writeD(_manorId); // manor id

		writeH(_list.size()); // list length

		for(TradeItem item : _list)
		{
            writeD(0x00);// вероятно, objId
            writeD(item.getItemId());
            writeD(0x00);// Location Slot
            writeQ(item.getCount());
            writeH(item.getItem().getType2());
            writeH(item.getCustomType1());
            writeH(0x00);// item.isEquiped()?
            writeD(item.getBodyPart());
            writeH(item.getEnchantLevel());
            writeH(item.getCustomType2());
			writeH(0x00);
			writeH(0x00);
            writeD(-1);// Shadow Life Time
            writeD(-9999);// Temporal Life time
			writeH(1); 
            writeItemElements();
            writeEnchantEffect();
			writeD(0x00);//Visible itemID
			writeQ(item.getOwnersPrice());
		}
	}
}