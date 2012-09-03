package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.TradeController.NpcTradeList;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.TradeItem;
import l2rt.util.GArray;

public final class BuyList extends L2GameServerPacket
{
	private int _listId;
	private final GArray<TradeItem> _Buylist;
	private long _money;
	private double _TaxRate = 0;

	public BuyList(NpcTradeList Buylist, L2Player activeChar)
	{
		_listId = Buylist.getListId();
		_money = activeChar.getAdena();
		activeChar.setBuyListId(_listId);
		_Buylist = cloneAndFilter(Buylist.getItems());
	}

	public BuyList(NpcTradeList Buylist, L2Player activeChar, double taxRate)
	{
		_listId = Buylist.getListId();
		_money = activeChar.getAdena();
		_TaxRate = taxRate;
		activeChar.setBuyListId(_listId);
		_Buylist = cloneAndFilter(Buylist.getItems());
	}

	protected static GArray<TradeItem> cloneAndFilter(GArray<TradeItem> list)
	{
		if(list == null)
		{
			return null;
		}
		GArray<TradeItem> ret = new GArray<TradeItem>(list.size());
		for(TradeItem item : list)
		{
			// А не пора ли обновить количество лимитированных предметов в трейд листе?
			if(item.getCurrentValue() < item.getCount() && item.getLastRechargeTime() + item.getRechargeTime() <= System.currentTimeMillis() / 60000)
			{
				item.setLastRechargeTime(item.getLastRechargeTime() + item.getRechargeTime());
				item.setCurrentValue(item.getCount());
			}
			if(item.getCurrentValue() == 0 && item.getCount() != 0)
			{
				continue;
			}
			ret.add(item);
		}
		return ret;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
        writeH(0xB7);
        writeD(0x00);
        writeQ(_money);        // current money
        writeD(_listId);
        writeD(0x00); // 0 = 250 количество слотов
		if(_Buylist == null)
		{
			writeH(0);
		}
		else
		{
			writeH(_Buylist.size());
			for(TradeItem item : _Buylist)
			{
				writeD(item.getItemId());
                writeD(item.getItemId());
                writeD(0);
                writeQ(item.getCurrentValue() < 0 ? 0 : item.getCurrentValue());
                writeH(item.getItem().getType2ForPackets());
                writeH(item.getItem().getType1());    // Custom Type 1
                writeH(0x00);    // isEquipped
                writeD(item.getItem().getBodyPart());    // Body Part
                writeH(0x00);    // Enchant
                writeH(0x00);    // Custom Type
                writeD(0x00);    // Augment
                writeD(-1);        // Mana
                writeD(-9999);    // Time
                writeH(1);         // при 0 итем красный
                writeItemElements(item);
                writeEnchantEffect(item);
				writeD(0x00);//Visible itemID
                
                /*if (item.getItemId() >= 3960 && item.getItemId() <= 4026)// Config.RATE_SIEGE_GUARDS_PRICE-//'
                    writeQ((long) (item.getOwnersPrice() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _TaxRate)));
                else*/
                    writeQ((long) (item.getOwnersPrice() * (1 + _TaxRate)));
			}
		}
	}
}