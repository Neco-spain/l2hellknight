package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.TradeController;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.instances.*;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExBuySellList;
import l2rt.util.GArray;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestExRefundItem extends L2GameClientPacket
{
	private int _listId;
	private int[] _ids;

	/**
	 * format: d dx[d]
	 */
	@Override
	public void readImpl()
	{
		_listId = readD();
		_ids = new int[readD()];
		for(int i = 0; i < _ids.length; i++)
			_ids[i] = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		ConcurrentLinkedQueue<L2ItemInstance> list = activeChar.getInventory().getRefundItemsList();

		if(list == null || list.isEmpty())
		{
			activeChar.sendActionFailed();
			return;
		}

		L2NpcInstance npc = activeChar.getLastNpc();

		boolean isValidMerchant = npc instanceof L2ClanHallManagerInstance || npc instanceof L2MerchantInstance || npc instanceof L2MercManagerInstance || npc instanceof L2CastleChamberlainInstance || npc instanceof L2NpcFriendInstance;
		if(!activeChar.isGM() && (npc == null || !isValidMerchant || !activeChar.isInRange(npc.getLoc(), L2Character.INTERACTION_DISTANCE)))
		{
			activeChar.sendActionFailed();
			return;
		}

		GArray<L2ItemInstance> toreturn = new GArray<L2ItemInstance>(_ids.length);
		long price = 0, weight = 0;

		for(int itemId : _ids)
			for(L2ItemInstance item : list)
				if(item.getObjectId() == itemId)
				{
					price += item.getCount() * item.getReferencePrice() / 2;
					weight += item.getCount() * item.getItem().getWeight();
					toreturn.add(item);
				}

		if(toreturn.isEmpty())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getAdena() < price)
		{
			activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			activeChar.sendActionFailed();
			return;
		}

		if(!activeChar.getInventory().validateWeight(weight))
		{
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			activeChar.sendActionFailed();
			return;
		}

		if(!activeChar.getInventory().validateCapacity(toreturn))
		{
			sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			activeChar.sendActionFailed();
			return;
		}

		if(!activeChar.getInventory().validateWeight(weight))
		{
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			activeChar.sendActionFailed();
			return;
		}

		activeChar.reduceAdena(price, true);

		for(L2ItemInstance itm : toreturn)
		{
			list.remove(itm);
			activeChar.getInventory().addItem(itm);
		}

		double taxRate = 0;
		Castle castle = null;
		if(npc != null)
		{
			castle = npc.getCastle(activeChar);
			if(castle != null)
				taxRate = castle.getTaxRate();
		}

		activeChar.sendPacket(/*new ExRefundList(activeChar), */new ExBuySellList(TradeController.getInstance().getBuyList(_listId), activeChar, taxRate).done());
		activeChar.updateStats();
	}
}