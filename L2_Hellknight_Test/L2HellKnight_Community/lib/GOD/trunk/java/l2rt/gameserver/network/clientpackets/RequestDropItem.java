package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.util.Location;
import l2rt.util.Log;

import java.util.logging.Logger;

/**
 * format:		cdd ddd
 * 						cdQ ddd - gracia final
 */
public class RequestDropItem extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestDropItem.class.getName());

	private int _objectId;
	private long _count;
	private Location _loc;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_count = readQ();
		_loc = new Location(readD(), readD(), readD());
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.isDead())
			return;

		if(_count < 1 || _loc.isNull())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!Config.ALLOW_DISCARDITEM)
		{
			activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.RequestDropItem.Disallowed", activeChar));
			return;
		}

		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isInTransaction())
		{
			sendPacket(Msg.NOTHING_HAPPENED);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(activeChar.isActionsDisabled() || activeChar.isSitting() || activeChar.isDropDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!activeChar.isInRangeSq(_loc, 22500) || Math.abs(_loc.z - activeChar.getZ()) > 50)
		{
			activeChar.sendPacket(Msg.THAT_IS_TOO_FAR_FROM_YOU_TO_DISCARD);
			return;
		}

		L2ItemInstance oldItem = activeChar.getInventory().getItemByObjectId(_objectId);
		if(oldItem == null)
		{
			_log.warning(activeChar.getName() + ":tried to drop an item that is not in the inventory ?!?:" + _objectId);
			return;
		}

		if(!oldItem.canBeDropped(activeChar, false))
		{
			activeChar.sendPacket(Msg.THAT_ITEM_CANNOT_BE_DISCARDED);
			return;
		}

		long oldCount = oldItem.getCount();
		if(oldCount < _count)
		{
			activeChar.sendActionFailed();
			return;
		}

		L2ItemInstance dropedItem = activeChar.getInventory().dropItem(_objectId, _count, false);

		if(dropedItem == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		dropedItem.dropToTheGround(activeChar, _loc);
		activeChar.disableDrop(1000);
		Log.LogItem(activeChar, Log.Drop, dropedItem);
		activeChar.updateStats();
	}
}