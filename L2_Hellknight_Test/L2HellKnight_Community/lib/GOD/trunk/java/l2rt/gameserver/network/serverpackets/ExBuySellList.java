package l2rt.gameserver.network.serverpackets;

import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import l2rt.gameserver.TradeController.NpcTradeList;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.util.GArray;

public class ExBuySellList extends L2GameServerPacket
{
	private int _listId, _done;
	private final TreeSet<L2ItemInstance> _SellList;
	private final ConcurrentLinkedQueue<L2ItemInstance> _RefundList;
	private long _money;
	private double _TaxRate = 0;

	public ExBuySellList(NpcTradeList Buylist, L2Player activeChar, double taxRate)
	{
		_money = activeChar.getAdena();
		_TaxRate = taxRate;
		_RefundList = activeChar.getInventory().getRefundItemsList();
		_SellList = new TreeSet<L2ItemInstance>(Inventory.OrderComparator);
		for(L2ItemInstance item : activeChar.getInventory().getItemsList())
		{
			if(item.getItem().isSellable() && item.canBeTraded(activeChar) && item.getReferencePrice() > 0)
			{
				_SellList.add(item);
			}
		}
	}

	public ExBuySellList done()
	{
		_done = 1;
		return this;
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
		writeD(0x01);
		writeD(0x00); // 0 = 250 количество слотов 
		if(_SellList == null)
		{
			writeH(0);
		}
		else
		{
			writeH(_SellList.size());
			for(L2ItemInstance item : _SellList)
			{
				
				writeD(item.getObjectId());
				writeD(item.getItemId());
				writeD(item.getEquipSlot());
				writeQ(item.getCount());
				writeH(item.getItem().getType2ForPackets());
				writeH(item.getCustomType1());
				writeH(0x00);
				writeD(item.getBodyPart());
				writeH(item.getEnchantLevel());
				writeH(item.getCustomType2());	// Augment, Mana, Time - hardcode for now
				writeD(0x00);
				writeD(-1);
				writeD(-9999);
				writeH(1); // при 0 итем красный 
				writeItemElements(item);
				writeEnchantEffect();
				//writeH(0x00); // unknown
				writeQ(item.getItem().getReferencePrice() / 2);
			}
		}
		if(_RefundList == null)
		{
			writeH(0);
		}
		else
		{
			writeH(_RefundList.size());
			//hx[ddQhhhhQhhhhhhhh h]
			int idx = 0;
			for(L2ItemInstance item : _RefundList)
			{				
				writeD(item.getObjectId());
				writeD(item.getItemId());
				writeD(0x00);
				writeQ(item.getCount());
				writeH(item.getItem().getType2ForPackets());
				writeH(item.getCustomType1());
				writeH(0x00);
				writeD(item.getItem().getBodyPart());
				writeH(item.getEnchantLevel());
				writeH(item.getCustomType2());
				// Augment, Mana, Time - hardcode for now
				writeH(0x00);
				writeH(0x00);
				writeD(-1);
				writeD(-9999);
				writeH(1); 
				writeItemElements(item);
				writeEnchantEffect();
				writeD(0x00);//Visible itemID
				writeH(0x00);
				writeD(idx++);
				writeQ(item.getItem().getReferencePrice() / 2 * item.getCount());
			}
		}
		writeC(_done);
	}

}