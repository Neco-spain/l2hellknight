package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2TradeList;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.items.L2ItemInstance;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * format: cddb, b - array of (ddhhdd)
 * Список продаваемого в приватный магазин покупки
 * см. также l2rt.gameserver.network.clientpackets.RequestPrivateStoreBuy
 */
public class RequestPrivateStoreBuySellList extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestPrivateStoreBuySellList.class.getName());
	private int _buyerID, _count;
	private L2Player _buyer, _seller;
	private ConcurrentLinkedQueue<TradeItem> _sellerlist = new ConcurrentLinkedQueue<TradeItem>(), _buyerlist = null;

	private int _fail = 0; // 1 — некритичный сбой, просто прерывать обмен, 2 — снимать продавца с трейда
	private boolean seller_fail = false;

	@Override
	public void readImpl()
	{
		_seller = getClient().getActiveChar();

		_buyerID = readD();
		_buyer = (L2Player) _seller.getVisibleObject(_buyerID);
		_count = readD();

		if(_count * 28 > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
		{
			seller_fail = true;
			return;
		}

		if(_seller == null || _buyer == null || _seller.getDistance3D(_buyer) > L2Character.INTERACTION_DISTANCE)
		{
			_fail = 1;
			return;
		}

		if(_buyer.getTradeList() == null)
		{
			_fail = 2;
			return;
		}

		if(!_seller.getPlayerAccess().UseTrade)
		{
			_seller.sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
			_fail = 1;
			return;
		}

		_buyerlist = _buyer.getBuyList();

		TradeItem temp;
		long sum = 0;
		for(int i = 0; i < _count; i++)
		{
			temp = new TradeItem();

			readD(); // ObjectId, не работает, поскольку используется id вещи-образца скупщика
			temp.setItemId(readD());
			readH();
			readH();
			temp.setCount(readQ());
			temp.setOwnersPrice(readQ());

			if(temp.getItemId() < 1 || temp.getCount() < 1 || temp.getOwnersPrice() < 1)
			{
				_seller.sendPacket(Msg.ActionFail, Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
				_fail = 1;
				return;
			}

			sum += temp.getCount() * temp.getOwnersPrice();

			L2ItemInstance SIItem = _seller.getInventory().getItemByItemId(temp.getItemId());
			if(SIItem == null)
			{
				_seller.sendActionFailed();
				_log.warning("Player " + _seller.getName() + " tries to sell to PSB:" + _buyer.getName() + " item not in inventory");
				return;
			}

			if(SIItem.isEquipped())
			{
				_seller.sendPacket(Msg.ActionFail, Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
				_fail = 1;
				return;
			}

			temp.setObjectId(SIItem.getObjectId());

			if(temp.getCount() > SIItem.getCount())
				temp.setCount(SIItem.getCount());

			temp.setEnchantLevel(SIItem.getEnchantLevel());
			temp.setAttackElement(SIItem.getAttackElementAndValue());
			temp.setDefenceFire(SIItem.getDefenceFire());
			temp.setDefenceWater(SIItem.getDefenceWater());
			temp.setDefenceWind(SIItem.getDefenceWind());
			temp.setDefenceEarth(SIItem.getDefenceEarth());
			temp.setDefenceHoly(SIItem.getDefenceHoly());
			temp.setDefenceUnholy(SIItem.getDefenceUnholy());

			_sellerlist.add(temp);
		}

		if(sum > _buyer.getAdena()) // если у продавца не хватает денег - снимать с трейда, ибо нефиг
		{
			_seller.sendPacket(Msg.ActionFail, Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
			_fail = 2;
			return;
		}

		_fail = 0;
	}

	@Override
	public void runImpl()
	{
		if(seller_fail || _buyer == null)
		{
			if(_seller != null)
				_seller.sendActionFailed();
			return;
		}

		if(_fail == 2)
		{
			L2TradeList.cancelStore(_buyer);
			return;
		}

		_buyer.getTradeList();
		if(_fail == 1 || _buyer.getPrivateStoreType() != L2Player.STORE_PRIVATE_BUY || !_buyer.getTradeList().buySellItems(_buyer, _buyerlist, _seller, _sellerlist))
		{
			_seller.sendActionFailed();
			return;
		}

		_buyer.saveTradeList();

		// на всякий случай немедленно сохраняем все изменения
		for(L2ItemInstance i : _buyer.getInventory().getItemsList())
			i.updateDatabase(true, true);

		for(L2ItemInstance i : _seller.getInventory().getItemsList())
			i.updateDatabase(true, true);

		if(_buyer.getBuyList().isEmpty())
			L2TradeList.cancelStore(_buyer);

		_buyer.updateStats();
		_seller.sendActionFailed();
	}
}