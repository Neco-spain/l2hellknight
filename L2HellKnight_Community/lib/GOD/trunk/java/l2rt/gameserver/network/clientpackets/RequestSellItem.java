package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.TradeController;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.instances.*;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.L2ItemInstance.ItemLocation;
import l2rt.gameserver.network.serverpackets.ExBuySellList;
import l2rt.util.Log;
import l2rt.util.SafeMath;
import l2rt.util.Util;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * packet type id 0x37
 * format:		cddb, b - array if (ddd)
 */
public class RequestSellItem extends L2GameClientPacket
{
	private int _listId;
	private int _count;
	private long[] _items; // count*3

	@Override
	public void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(_count * 16 > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new long[_count * 3];
		for(int i = 0; i < _count; i++)
		{
			_items[i * 3 + 0] = readD();
			_items[i * 3 + 1] = readD();
			_items[i * 3 + 2] = readQ();
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
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(_items == null || _count <= 0)
			return;

		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() < 0 && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}

		L2NpcInstance npc = activeChar.getLastNpc();

		if("sell".equalsIgnoreCase(activeChar.getLastBbsOperaion()))
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

		for(int i = 0; i < _count; i++)
		{
			int objectId = (int) _items[i * 3 + 0];
			int itemId = (int) _items[i * 3 + 1];
			long cnt = _items[i * 3 + 2];

			if(cnt < 0)
			{
				Util.handleIllegalPlayerAction(activeChar, "Integer overflow", "RequestSellItem[100]", 0);
				continue;
			}
			else if(cnt == 0)
				continue;

			L2ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
			if(item == null || !item.canBeTraded(activeChar) || !item.getItem().isSellable())
			{
				activeChar.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
				return;
			}

			if(item.getItemId() != itemId)
			{
				Util.handleIllegalPlayerAction(activeChar, "Fake packet", "RequestSellItem[115]", 0);
				continue;
			}

			if(item.getCount() < cnt)
			{
				Util.handleIllegalPlayerAction(activeChar, "Incorrect item count", "RequestSellItem[121]", 0);
				continue;
			}

			long price = item.getReferencePrice() * cnt / 2;

			if (!Config.SELL_FREE_ADENA)
				activeChar.addAdena(price);
			else 
				activeChar.addAdena(1);
				
			Log.LogItem(activeChar, Log.SellItem, item);

			// If player sells the enchant scroll he is using, deactivate it
			if(activeChar.getEnchantScroll() != null && item.getObjectId() == activeChar.getEnchantScroll().getObjectId())
				activeChar.setEnchantScroll(null);

			L2ItemInstance refund = activeChar.getInventory().dropItem(item, cnt, true);

			refund.setLocation(ItemLocation.VOID);
			ConcurrentLinkedQueue<L2ItemInstance> refundlist = activeChar.getInventory().getRefundItemsList();
			if(refund.isStackable())
			{
				boolean found = false;
				for(L2ItemInstance ahri : refundlist)
					if(ahri.getItemId() == refund.getItemId())
					{
						ahri.setCount(SafeMath.safeAddLongOrMax(ahri.getCount(), refund.getCount()));
						found = true;
						break;
					}
				if(!found)
					refundlist.add(refund);
			}
			else
				refundlist.add(refund);

			if(refundlist.size() > 12)
				refundlist.poll();
		}

		double taxRate = 0;
		Castle castle = null;
		if(npc != null)
		{
			castle = npc.getCastle(activeChar);
			if(castle != null)
				taxRate = castle.getTaxRate();
		}

		activeChar.sendPacket(new ExBuySellList(TradeController.getInstance().getBuyList(_listId), activeChar, taxRate).done());
		activeChar.updateStats();
	}
}