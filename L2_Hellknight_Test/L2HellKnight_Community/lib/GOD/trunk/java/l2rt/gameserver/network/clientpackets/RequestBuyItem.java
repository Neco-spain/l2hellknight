package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.TradeController;
import l2rt.gameserver.TradeController.NpcTradeList;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.instances.*;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.BuyList;
import l2rt.gameserver.network.serverpackets.ExBuySellList;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.GArray;
import l2rt.util.Log;
import l2rt.util.SafeMath;
import l2rt.util.Util;

import java.util.logging.Logger;

/**
 * format:		cddb, b - array of (dd)
 */
public class RequestBuyItem extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestBuyItem.class.getName());

	private int _listId;
	private int _count;
	private long[] _items; // count*2

	@Override
	public void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new long[_count * 2];
		for(int i = 0; i < _count; i++)
		{
			_items[i * 2 + 0] = readD();
			_items[i * 2 + 1] = readQ();
			if(_items[i * 2 + 0] < 1 || _items[i * 2 + 1] < 1)
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

		if(_items == null || _count == 0)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() < 0 && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}

		// Проверяем, не подменили ли id
		if(activeChar.getBuyListId() != _listId)
		{
			Util.handleIllegalPlayerAction(activeChar, "RequestBuyItem[100]", "Tried to buy from buylist: " + _listId, 1);
			return;
		}

		L2NpcInstance npc = activeChar.getLastNpc();

		if("buy".equalsIgnoreCase(activeChar.getLastBbsOperaion()))
			activeChar.setLastBbsOperaion(null);
		else
		{
			boolean isValidMerchant = npc instanceof L2ClanHallManagerInstance || npc instanceof L2MerchantInstance || npc instanceof L2MercManagerInstance || npc instanceof L2CastleChamberlainInstance || npc instanceof L2NpcFriendInstance;
			if(!activeChar.isGM() && (npc == null || !isValidMerchant || !activeChar.isInRange(npc.getLoc(), L2Character.INTERACTION_DISTANCE)))
			{
				activeChar.sendActionFailed();
				return;
			}
		}

		L2NpcInstance merchant = null;
		if(npc != null && (npc instanceof L2MerchantInstance || npc instanceof L2ClanHallManagerInstance)) //TODO расширить список?
			merchant = npc;

		GArray<L2ItemInstance> items = new GArray<L2ItemInstance>(_count);
		for(int i = 0; i < _count; i++)
		{
			int itemId = (int) _items[i * 2 + 0];
			long cnt = _items[i * 2 + 1];
			if(cnt <= 0)
			{
				activeChar.sendActionFailed();
				return;
			}

			L2ItemInstance inst = ItemTemplates.getInstance().createItem(itemId);
			if(inst == null)
			{
				activeChar.sendActionFailed();
				return;
			}

			if(!inst.isStackable() && cnt != 1)
			{
				activeChar.sendActionFailed();
				return;
			}

			inst.setCount(cnt);
			items.add(inst);
		}

		long finalLoad = 0;
		int finalCount = activeChar.getInventory().getSize();
		int needsSpace = 2;
		int weight = 0;
		long currentMoney = activeChar.getAdena();

		int itemId;
		long cnt, price, tax = 0, totalCost = 0, subTotal = 0;
		double taxRate = 0;

		Castle castle = null;
		if(merchant != null)
		{
			castle = merchant.getCastle(activeChar);
			if(castle != null)
				taxRate = castle.getTaxRate();
		}

		for(int i = 0; i < items.size(); i++)
		{
			itemId = items.get(i).getItemId();
			cnt = items.get(i).getCount();
			needsSpace = 2;
			if(items.get(i).getItem().isStackable())
			{
				needsSpace = 1;
				if(activeChar.getInventory().getItemByItemId(itemId) != null)
					needsSpace = 0;
			}
			NpcTradeList list = TradeController.getInstance().getBuyList(_listId);
			if(list == null)
			{
				Log.add("tried to buy from non-exist list " + _listId, "errors", activeChar);
				activeChar.sendActionFailed();
				return;
			}
			TradeItem ti = getItemByItemId(itemId, list);
			price = ti == null ? 0 : ti.getOwnersPrice();
			if(itemId >= 3960 && itemId <= 4921)
				price *= Config.RATE_SIEGE_GUARDS_PRICE;

			if(price == 0 && !activeChar.getPlayerAccess().UseGMShop)
			{
				Util.handleIllegalPlayerAction(activeChar, "RequestBuyItem[191]", "Tried to buy zero price item, list " + _listId + " item " + itemId, 0);

				for(L2ItemInstance item : items)
					item.deleteMe();

				activeChar.sendMessage("Error: zero-price item! Please notify GM.");

				activeChar.sendActionFailed();
				return;
			}

			
			
			weight = items.get(i).getItem().getWeight();
			
			if(price < 0)
			{
				_log.warning("ERROR, no price found. Wrong buylist?");

				for(L2ItemInstance item : items)
					item.deleteMe();

				activeChar.sendActionFailed();
				return;
			}

			try
			{
				if(cnt < 0)
					throw new ArithmeticException("cnt < 0");
				subTotal = SafeMath.safeAddLong(subTotal, SafeMath.safeMulLong(cnt, price)); // Before tax

				tax = SafeMath.safeMulLong(subTotal, taxRate);
				totalCost = SafeMath.safeAddLong(subTotal, tax);
				if(totalCost < 0)
					throw new ArithmeticException("213: Tried to purchase negative " + totalCost + " adena worth of goods.");

				finalLoad = SafeMath.safeAddLong(finalLoad, SafeMath.safeMulLong(cnt, weight));
				if(finalLoad < 0)
					throw new ArithmeticException("254: Tried to purchase negative " + finalLoad + " adena worth of goods.");
			}
			catch(ArithmeticException e)
			{
				Util.handleIllegalPlayerAction(activeChar, "RequestBuyItem[157]", "merchant: " + merchant + ": " + e.getMessage(), 1);

				for(L2ItemInstance item : items)
					item.deleteMe();

				sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED, Msg.ActionFail);
				return;
			}

			if(needsSpace == 2)
				finalCount += cnt;
			else if(needsSpace == 1)
				finalCount += 1;
		}

		if(totalCost > currentMoney || subTotal < 0 || (currentMoney <= 0 && !activeChar.getPlayerAccess().UseGMShop))
		{
			for(L2ItemInstance item : items)
				item.deleteMe();

			sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, Msg.ActionFail);
			return;
		}

		if(!activeChar.getInventory().validateWeight(finalLoad))
		{
			for(L2ItemInstance item : items)
				item.deleteMe();

			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT, Msg.ActionFail);
			return;
		}

		if(!activeChar.getInventory().validateCapacity(finalCount))
		{
			for(L2ItemInstance item : items)
				item.deleteMe();

			sendPacket(Msg.YOUR_INVENTORY_IS_FULL, Msg.ActionFail);
			return;
		}

		if (totalCost > currentMoney && activeChar.getPlayerAccess().UseGMShop)
		{
			for(L2ItemInstance item : items)
			{
				Log.LogItem(activeChar, merchant, Log.BuyItem, item);
				activeChar.getInventory().addItem(item);
			}
		}
		
		// Для магазинов с ограниченным количеством товара число продаваемых предметов уменьшаем после всех проверок
		NpcTradeList list = TradeController.getInstance().getBuyList(_listId);
		for(int i = 0; i < items.size(); i++)
		{
			itemId = items.get(i).getItemId();
			cnt = items.get(i).getCount();
			TradeItem ic = getItemByItemId(itemId, list);

			if(ic != null && ic.isCountLimited())
				if(cnt > ic.getCurrentValue())
				{
					int t = (int) (System.currentTimeMillis() / 60000);
					if(ic.getLastRechargeTime() + ic.getRechargeTime() <= t)
					{
						ic.setLastRechargeTime(t);
						ic.setCurrentValue(ic.getCount());
					}
					else
						continue;
				}
				else
					ic.setCurrentValue(ic.getCurrentValue() - cnt);
		}

		if (activeChar.getPlayerAccess().UseGMShop && activeChar.isGM())
		{
			
		}
		else
		{
			activeChar.reduceAdena(totalCost, true);
		}
		
		/*activeChar.reduceAdena(totalCost, true);*/
		for(L2ItemInstance item : items)
		{
			Log.LogItem(activeChar, merchant, Log.BuyItem, item);
			activeChar.getInventory().addItem(item);
		}

		// Add tax to castle treasury if not owned by npc clan
		if(castle != null && castle.getOwnerId() > 0 && activeChar.getReflection().getId() == 0)
		{
			castle.addToTreasury(tax, true, false);
			Log.add(castle.getName() + "|" + tax + "|BuyItem", "treasury");
		}
		sendPacket(new BuyList(list, activeChar, taxRate));
		sendPacket(new ExBuySellList(list, activeChar, taxRate).done());
		activeChar.updateStats();
	}

	private static final TradeItem getItemByItemId(int itemId, NpcTradeList list)
	{
		for(TradeItem ti : list.getItems())
			if(ti.getItemId() == itemId)
				return ti;
		return null;
	}
}