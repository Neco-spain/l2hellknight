package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2TradeList;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.PrivateStoreManageList;
import l2rt.gameserver.network.serverpackets.PrivateStoreMsgSell;
import l2rt.gameserver.templates.L2Item;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Это список вещей которые игрок хочет продать в создаваемом им приватном магазине
 * Старое название SetPrivateStoreListSell
 * Format: cddb, b = array of (ddd)
 */
public class SetPrivateStoreList extends L2GameClientPacket
{
	private int _count;
	private boolean _package;
	private long[] _items; // count * 3

	@Override
	public void readImpl()
	{
		_package = readD() == 1;
		_count = readD();
		// Иначе нехватит памяти при создании массива.
		if(_count * 20 > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new long[_count * 3];
		for(int i = 0; i < _count; i++)
		{
			_items[i * 3 + 0] = readD(); // objectId
			_items[i * 3 + 1] = readQ(); // count
			_items[i * 3 + 2] = readQ(); // price
			if(_items[i * 3 + 0] < 1 || _items[i * 3 + 1] < 1 || _items[i * 3 + 2] < 0)
			{
				_items = null;
				break;
			}
		}
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_items == null || _count <= 0 || !activeChar.checksForShop(false))
		{
			L2TradeList.cancelStore(activeChar);
			return;
		}

		TradeItem temp;
		ConcurrentLinkedQueue<TradeItem> listsell = new ConcurrentLinkedQueue<TradeItem>();

		int maxSlots = activeChar.getTradeLimit();

		if(_count > maxSlots)
		{
			activeChar.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			L2TradeList.cancelStore(activeChar);
			activeChar.sendPacket(new PrivateStoreManageList(activeChar, _package));
			return;
		}

		int count = _count;
		for(int x = 0; x < _count; x++)
		{
			int objectId = (int) _items[x * 3 + 0];
			long cnt = _items[x * 3 + 1];
			long price = _items[x * 3 + 2];

			L2ItemInstance itemToSell = activeChar.getInventory().getItemByObjectId(objectId);

			if(cnt < 1 || itemToSell == null || !itemToSell.canBeTraded(activeChar) || itemToSell.getItemId() == L2Item.ITEM_ID_ADENA)
			{
				count--;
				continue;
			}

			// If player sells the enchant scroll he is using, deactivate it
			if(activeChar.getEnchantScroll() != null && itemToSell.getObjectId() == activeChar.getEnchantScroll().getObjectId())
				activeChar.setEnchantScroll(null);

			if(cnt > itemToSell.getCount())
				cnt = itemToSell.getCount();

			temp = new TradeItem();
			temp.setObjectId(objectId);
			temp.setCount(cnt);
			temp.setOwnersPrice(price);
			temp.setItemId(itemToSell.getItemId());
			temp.setEnchantLevel(itemToSell.getEnchantLevel());
			temp.setAttackElement(itemToSell.getAttackElementAndValue());
			temp.setDefenceFire(itemToSell.getDefenceFire());
			temp.setDefenceWater(itemToSell.getDefenceWater());
			temp.setDefenceWind(itemToSell.getDefenceWind());
			temp.setDefenceEarth(itemToSell.getDefenceEarth());
			temp.setDefenceHoly(itemToSell.getDefenceHoly());
			temp.setDefenceUnholy(itemToSell.getDefenceUnholy());

			listsell.add(temp);
		}

		if(count != 0)
		{
			activeChar.setSellList(listsell);
			activeChar.setPrivateStoreType(_package ? L2Player.STORE_PRIVATE_SELL_PACKAGE : L2Player.STORE_PRIVATE_SELL);
			activeChar.broadcastUserInfo(true);
			activeChar.broadcastPacket(new PrivateStoreMsgSell(activeChar));
			activeChar.sitDown();
		}
		else
			L2TradeList.cancelStore(activeChar);
	}
}