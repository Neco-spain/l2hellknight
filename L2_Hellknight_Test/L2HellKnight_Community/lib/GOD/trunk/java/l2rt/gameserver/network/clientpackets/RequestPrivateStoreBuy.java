package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2TradeList;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Список покупаемого в приватном магазине продажи
 * см. также l2rt.gameserver.network.clientpackets.RequestPrivateStoreBuySellList
 */
public class RequestPrivateStoreBuy extends L2GameClientPacket
{
	// format: cddb, b - array of (ddd)
	private int _sellerID;
	private int _count;
	private long[] _items; // count * 3

	@Override
	public void readImpl()
	{
		_sellerID = readD();
		_count = readD();
		if(_count * 20 > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new long[_count * 3];
		for(int i = 0; i < _count; i++)
		{
			_items[i * 3 + 0] = readD(); // object id
			_items[i * 3 + 1] = readQ(); // count
			_items[i * 3 + 2] = readQ(); // price

			if(_items[i * 3 + 0] < 1 || _items[i * 3 + 1] < 1 || _items[i * 3 + 2] < 1)
			{
				_items = null;
				break;
			}
		}
	}

	@Override
	public void runImpl()
	{
		if(_items == null)
			return;

		L2Player buyer = getClient().getActiveChar();
		if(buyer == null)
			return;

		if(!buyer.getPlayerAccess().UseTrade)
		{
			buyer.sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
			return;
		}

		ConcurrentLinkedQueue<TradeItem> buyerlist = new ConcurrentLinkedQueue<TradeItem>();

		L2Player seller = (L2Player) buyer.getVisibleObject(_sellerID);
		if(seller == null || seller.getPrivateStoreType() != L2Player.STORE_PRIVATE_SELL && seller.getPrivateStoreType() != L2Player.STORE_PRIVATE_SELL_PACKAGE || seller.getDistance3D(buyer) > L2Character.INTERACTION_DISTANCE)
		{
			buyer.sendActionFailed();
			return;
		}

		if(seller.getTradeList() == null)
		{
			L2TradeList.cancelStore(seller);
			return;
		}

		if(!L2TradeList.validateList(seller))
		{
			buyer.sendPacket(new SystemMessage(SystemMessage.CANNOT_PURCHASE));
			return;
		}

		ConcurrentLinkedQueue<TradeItem> sellerlist = seller.getSellList();
		double cost = 0;

		if(seller.getPrivateStoreType() == L2Player.STORE_PRIVATE_SELL_PACKAGE)
		{
			buyerlist = new ConcurrentLinkedQueue<TradeItem>();
			buyerlist.addAll(sellerlist);
			for(TradeItem ti : buyerlist)
				cost += 1d * ti.getOwnersPrice() * ti.getCount();
		}
		else
			for(int i = 0; i < _count; i++)
			{
				int objectId = (int) _items[i * 3 + 0];
				long count = _items[i * 3 + 1];
				long price = _items[i * 3 + 2];

				for(TradeItem si : sellerlist)
					if(si.getObjectId() == objectId)
					{
						if(count > si.getCount() || price != si.getOwnersPrice())
						{
							buyer.sendActionFailed();
							return;
						}

						L2ItemInstance sellerItem = seller.getInventory().getItemByObjectId(objectId);
						if(sellerItem == null || sellerItem.getCount() < count)
						{
							buyer.sendActionFailed();
							return;
						}

						TradeItem temp = new TradeItem();
						temp.setObjectId(si.getObjectId());
						temp.setItemId(sellerItem.getItemId());
						temp.setCount(count);
						temp.setOwnersPrice(si.getOwnersPrice());
						temp.setAttackElement(sellerItem.getAttackElementAndValue());
						temp.setDefenceFire(sellerItem.getDefenceFire());
						temp.setDefenceWater(sellerItem.getDefenceWater());
						temp.setDefenceWind(sellerItem.getDefenceWind());
						temp.setDefenceEarth(sellerItem.getDefenceEarth());
						temp.setDefenceHoly(sellerItem.getDefenceHoly());
						temp.setDefenceUnholy(sellerItem.getDefenceUnholy());

						cost += 1d * temp.getOwnersPrice() * temp.getCount();
						buyerlist.add(temp);
					}
			}

		if(buyer.getAdena() < cost || cost > Long.MAX_VALUE || cost < 0)
		{
			buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			buyer.sendActionFailed();
			return;
		}

		if(!L2TradeList.validateTrade(seller, buyerlist))
		{
			buyer.sendPacket(Msg.INCORRECT_ITEM_COUNT);
			buyer.sendActionFailed();
			L2TradeList.validateList(seller);
			return;
		}

		seller.getTradeList().buySellItems(buyer, buyerlist, seller, sellerlist);
		buyer.sendChanges();

		seller.saveTradeList();

		// на всякий случай немедленно сохраняем все изменения
		for(L2ItemInstance i : buyer.getInventory().getItemsList())
			i.updateDatabase(true, true);

		for(L2ItemInstance i : seller.getInventory().getItemsList())
			i.updateDatabase(true, true);

		if(seller.getSellList().isEmpty())
			L2TradeList.cancelStore(seller);

		seller.sendChanges();
		buyer.sendActionFailed();
	}
}