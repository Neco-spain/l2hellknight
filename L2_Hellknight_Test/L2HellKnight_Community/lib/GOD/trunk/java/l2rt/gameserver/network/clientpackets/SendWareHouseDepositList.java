package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.L2ItemInstance.ItemClass;
import l2rt.gameserver.model.items.PcInventory;
import l2rt.gameserver.model.items.Warehouse;
import l2rt.gameserver.model.items.Warehouse.WarehouseType;
import l2rt.util.GArray;

import java.util.HashMap;

/**
 * Format: cdb, b - array of (dd)
 */
public class SendWareHouseDepositList extends L2GameClientPacket
{
	private static final int _WAREHOUSE_FEE = 30;
	private HashMap<Integer, Long> _items;

	@Override
	public void readImpl()
	{
		int itemsCount = readD();
		if(itemsCount * 12 > _buf.remaining() || itemsCount > Short.MAX_VALUE || itemsCount < 0)
		{
			_items = null;
			return;
		}
		_items = new HashMap<Integer, Long>(itemsCount + 1, 0.999f);
		for(int i = 0; i < itemsCount; i++)
		{
			int obj_id = readD();
			long itemQuantity = readQ();
			if(obj_id < 1 || itemQuantity < 0)
			{
				_items = null;
				return;
			}
			_items.put(obj_id, itemQuantity);
		}
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _items == null)
			return;

		// Проверяем наличие npc и расстояние до него
		L2NpcInstance whkeeper = activeChar.getLastNpc();
		if(whkeeper == null || !activeChar.isInRange(whkeeper.getLoc(), L2Character.INTERACTION_DISTANCE))
		{
			activeChar.sendPacket(Msg.WAREHOUSE_IS_TOO_FAR);
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		Warehouse warehouse;
		PcInventory inventory = activeChar.getInventory();
		boolean privatewh = activeChar.getUsingWarehouseType() != WarehouseType.CLAN;
		int slotsleft = 0;
		long adenaDeposit = 0;

		// Список предметов, уже находящихся на складе
		L2ItemInstance[] itemsOnWarehouse;
		if(privatewh)
		{
			warehouse = activeChar.getWarehouse();
			itemsOnWarehouse = warehouse.listItems(ItemClass.ALL);
			slotsleft = activeChar.getWarehouseLimit() - itemsOnWarehouse.length;
		}
		else
		{
			warehouse = activeChar.getClan().getWarehouse();
			itemsOnWarehouse = warehouse.listItems(ItemClass.ALL);
			slotsleft = activeChar.getClan().getWhBonus() + Config.WAREHOUSE_SLOTS_CLAN - itemsOnWarehouse.length;
		}

		// Список стекуемых предметов, уже находящихся на складе
		GArray<Integer> stackableList = new GArray<Integer>();
		for(L2ItemInstance i : itemsOnWarehouse)
			if(i.isStackable())
				stackableList.add(i.getItemId());

		// Создаем новый список передаваемых предметов, на основе полученных данных
		GArray<L2ItemInstance> itemsToStoreList = new GArray<L2ItemInstance>(_items.size() + 1);
		for(Integer itemObjectId : _items.keySet())
		{
			L2ItemInstance item = inventory.getItemByObjectId(itemObjectId);
			if(item == null || !item.canBeStored(activeChar, privatewh)) // а его вообще положить можно?
				continue;
			if(!item.isStackable() || !stackableList.contains(item.getItemId())) // вещь требует слота
			{
				if(slotsleft <= 0) // если слоты кончились нестекуемые вещи и отсутствующие стекуемые пропускаем
					continue;
				slotsleft--; // если слот есть то его уже нет
			}
			if(item.getItemId() == 57)
				adenaDeposit = _items.get(itemObjectId);
			itemsToStoreList.add(item);
		}

		// Проверяем, хватит ли у нас денег на уплату налога
		int fee = itemsToStoreList.size() * _WAREHOUSE_FEE;
		if(fee + adenaDeposit > activeChar.getAdena())
		{
			activeChar.sendPacket(Msg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
			return;
		}

		// Сообщаем о том, что слоты кончились
		if(slotsleft <= 0)
			activeChar.sendPacket(Msg.YOUR_WAREHOUSE_IS_FULL);

		// Перекидываем
		for(L2ItemInstance itemToStore : itemsToStoreList)
			warehouse.addItem(inventory.dropItem(itemToStore, _items.get(itemToStore.getObjectId()), false), activeChar.getName());

		// Платим налог
		activeChar.reduceAdena(fee, true);

		// Обновляем параметры персонажа
		activeChar.updateStats();
	}
}