package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.ClanWarehousePool;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.Warehouse;
import l2rt.gameserver.model.items.Warehouse.WarehouseType;

import java.util.logging.Logger;

public class SendWareHouseWithDrawList extends L2GameClientPacket
{
	//Format: cdb, b - array of (dd)
	private static Logger _log = Logger.getLogger(SendWareHouseWithDrawList.class.getName());

	private int _count;
	private long[] _items;
	private long[] counts;

	@Override
	public void readImpl()
	{
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new long[_count * 2];
		counts = new long[_count];
		for(int i = 0; i < _count; i++)
		{
			_items[i * 2 + 0] = readD(); // item object id
			_items[i * 2 + 1] = readQ(); // count
			if(_items[i * 2 + 0] < 1 || _items[i * 2 + 1] < 0)
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

		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2NpcInstance whkeeper = activeChar.getLastNpc();
		if(whkeeper == null || !activeChar.isInRange(whkeeper.getLoc(), L2Character.INTERACTION_DISTANCE))
		{
			activeChar.sendPacket(Msg.WAREHOUSE_IS_TOO_FAR);
			return;
		}

		boolean canWithdrawCWH = false;
		int clanId = 0;
		if(activeChar.getClan() != null)
		{
			clanId = activeChar.getClan().getClanId();
			if(((activeChar.getClanPrivileges() & L2Clan.CP_CL_WAREHOUSE_SEARCH) == L2Clan.CP_CL_WAREHOUSE_SEARCH) && (Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE || activeChar.getClan().getLeaderId() == activeChar.getObjectId() || activeChar.getVarB("canWhWithdraw")))
				canWithdrawCWH = true;
		}

		if(activeChar.getUsingWarehouseType() == WarehouseType.CLAN && !canWithdrawCWH)
			return;

		int weight = 0;
		int finalCount = activeChar.getInventory().getSize();
		int[] olditems = new int[_count];

		for(int i = 0; i < _count; i++)
		{
			long itemObjId = _items[i * 2 + 0];
			long count = _items[i * 2 + 1];
			L2ItemInstance oldinst = L2ItemInstance.restoreFromDb(itemObjId, false);

			if(count < 0)
			{
				activeChar.sendPacket(Msg.INCORRECT_ITEM_COUNT);
				return;
			}

			if(oldinst == null)
			{
				activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.SendWareHouseWithDrawList.Changed", activeChar));
				return;
			}

			if(oldinst.getOwnerId() != activeChar.getObjectId()) // с чужих складов можно брать если это фрейт или квх при наличии прав
				if(oldinst.getOwnerId() == clanId)
				{
					if(!canWithdrawCWH)
						continue;
				}
				else if(!activeChar.getAccountChars().containsKey(oldinst.getOwnerId()))
					continue;

			if(oldinst.getCount() < count)
				count = oldinst.getCount();

			counts[i] = count;
			olditems[i] = oldinst.getObjectId();
			weight += oldinst.getItem().getWeight() * count;
			finalCount++;

			if(oldinst.getItem().isStackable() && activeChar.getInventory().getItemByItemId(oldinst.getItemId()) != null)
				finalCount--;
		}

		if(!activeChar.getInventory().validateCapacity(finalCount))
		{
			activeChar.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return;
		}

		if(!activeChar.getInventory().validateWeight(weight))
		{
			activeChar.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return;
		}

		Warehouse warehouse = null;
		if(activeChar.getUsingWarehouseType() == WarehouseType.PRIVATE)
			warehouse = activeChar.getWarehouse();
		else if(activeChar.getUsingWarehouseType() == WarehouseType.CLAN)
		{
			ClanWarehousePool.getInstance().AddWork(activeChar, olditems, counts);
			return;
		}
		else if(activeChar.getUsingWarehouseType() == WarehouseType.FREIGHT)
			warehouse = activeChar.getFreight();
		else
		{
			// Something went wrong!
			_log.warning("Error retrieving a warehouse object for char " + activeChar.getName() + " - using warehouse type: " + activeChar.getUsingWarehouseType());
			return;
		}

		for(int i = 0; i < olditems.length; i++)
		{
			L2ItemInstance TransferItem = warehouse.takeItemByObj(olditems[i], counts[i]);
			if(TransferItem == null)
				_log.warning("Error getItem from warhouse player: " + activeChar.getName());
			activeChar.getInventory().addItem(TransferItem);
		}

		activeChar.sendChanges();
	}
}