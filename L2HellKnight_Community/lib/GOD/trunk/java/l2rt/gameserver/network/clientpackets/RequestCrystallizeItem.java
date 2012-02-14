package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.UsablePacketItem;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.util.Log;
import l2rt.util.Rnd;

public class RequestCrystallizeItem extends L2GameClientPacket
{
	//Format: cdd
	private int _objectId;
	private long count;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		count = readQ(); //FIXME: count??
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM, Msg.ActionFail);
			return;
		}
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if(item == null || !item.canBeCrystallized(activeChar, true))
		{
			activeChar.sendActionFailed();
			return;
		}
		if (this.count != 1L)
		{
			count = 1L;
		}
		
		activeChar.getInventory().destroyItem(item, 1L, true);
		Log.LogItem(activeChar, Integer.valueOf(933), item);
		for (UsablePacketItem reward : activeChar.CrystallizationProducts)
		if (Rnd.get(100) < reward.prob)
		{
			L2ItemInstance itemx = activeChar.getInventory().addItem(reward.itemId, reward.count);
			activeChar.sendPacket(new L2GameServerPacket[] { Msg.THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED, new SystemMessage(29).addItemName(Integer.valueOf(reward.itemId)).addNumber(Long.valueOf(reward.count)) });
			Log.LogItem(activeChar, Integer.valueOf(959), itemx);
		}
	}


}