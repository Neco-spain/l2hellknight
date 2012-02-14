package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2TradeList;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PrivateStoreManageList extends L2GameServerPacket
{
	private int seller_id;
	private long seller_adena;
	private boolean _package = false;
	private ConcurrentLinkedQueue<TradeItem> _sellList;
	private ConcurrentLinkedQueue<TradeItem> _haveList;

	/**
	 * Окно управления личным магазином покупки
	 * @param seller
	 */
	public PrivateStoreManageList(L2Player seller, boolean pkg)
	{
		seller_id = seller.getObjectId();
		seller_adena = seller.getAdena();
		_package = pkg;

		// Проверяем список вещей в инвентаре, если вещь остутствует - убираем из списка продажи
		_sellList = new ConcurrentLinkedQueue<TradeItem>();
		for(TradeItem i : seller.getSellList())
		{
			L2ItemInstance inst = seller.getInventory().getItemByObjectId(i.getObjectId());
			if(i.getCount() <= 0 || inst == null || !inst.canBeTraded(seller))
				continue;
			if(inst.getCount() < i.getCount())
				i.setCount(inst.getCount());
			_sellList.add(i);
		}

		L2TradeList _list = new L2TradeList(0);
		// Строим список вещей, годных для продажи имеющихся в инвентаре
		for(L2ItemInstance item : seller.getInventory().getItemsList())
			if(item != null && item.canBeTraded(seller) && item.getItemId() != L2Item.ITEM_ID_ADENA)
				_list.addItem(item);

		_haveList = new ConcurrentLinkedQueue<TradeItem>();

		// Делаем список для собственно передачи с учетом количества
		for(L2ItemInstance item : _list.getItems())
		{
			TradeItem ti = new TradeItem();
			ti.setObjectId(item.getObjectId());
			ti.setItemId(item.getItemId());
			ti.setCount(item.getCount());
			ti.setEnchantLevel(item.getEnchantLevel());
			ti.setAttackElement(item.getAttackElementAndValue());
			ti.setDefenceFire(item.getDefenceFire());
			ti.setDefenceWater(item.getDefenceWater());
			ti.setDefenceWind(item.getDefenceWind());
			ti.setDefenceEarth(item.getDefenceEarth());
			ti.setDefenceHoly(item.getDefenceHoly());
			ti.setDefenceUnholy(item.getDefenceUnholy());
			_haveList.add(ti);
		}

		//Убираем совпадения между списками, в сумме оба списка должны совпадать с содержимым инвентаря
		if(_sellList.size() > 0)
			for(TradeItem itemOnSell : _sellList)
			{
				_haveList.remove(itemOnSell);
				boolean added = false;
				for(TradeItem itemInInv : _haveList)
					if(itemInInv.getObjectId() == itemOnSell.getObjectId())
					{
						added = true;
						itemOnSell.setCount(Math.min(itemOnSell.getCount(), itemInInv.getCount()));
						if(itemOnSell.getCount() == itemInInv.getCount())
							_haveList.remove(itemInInv);
						else if(itemOnSell.getCount() > 0)
							itemInInv.setCount(itemInInv.getCount() - itemOnSell.getCount());
						else
							_sellList.remove(itemOnSell);
						break;
					}
				if(!added)
					_sellList.remove(itemOnSell);
			}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xA0);
		//section 1
		writeD(seller_id);
		writeD(_package ? 1 : 0);
		writeQ(seller_adena);

		//Список имеющихся вещей
		writeD(_haveList.size());
		for(TradeItem temp : _haveList)
		{
			L2Item tempItem = ItemTemplates.getInstance().getTemplate(temp.getItemId());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
            writeD(temp.getEquipSlot());
			writeQ(temp.getCount());
			writeH(tempItem.getType2ForPackets());
            writeH(temp.getCustomType1());
            writeH(0x00);
            writeD(tempItem.getBodyPart());
            writeH(temp.getEnchantLevel());//enchant lvl
            writeH(temp.getCustomType2());
            writeD(0x00);// Augmentation Id
            writeD(0x00);// Mana
            writeD(0x00);// Temp Life time
			writeItemElements(temp);
			writeEnchantEffect(temp);
            writeQ(tempItem.getReferencePrice());
		}

		//Список вещей уже поставленых на продажу
		writeD(_sellList.size());
		for(TradeItem temp2 : _sellList)
		{
			L2Item tempItem = ItemTemplates.getInstance().getTemplate(temp2.getItemId());
			writeD(temp2.getObjectId());
			writeD(temp2.getItemId());
            writeD(temp2.getEquipSlot());
			writeQ(temp2.getCount());
            writeH(tempItem.getType2ForPackets());
            writeH(temp2.getCustomType1());
            writeH(0x00);
            writeD(tempItem.getBodyPart());
            writeH(temp2.getEnchantLevel());//enchant lvl
            writeH(temp2.getCustomType2());
            writeD(0x00);// Augmentation Id
            writeD(-1);// Mana
            writeD(0x00);// Temp Life time
			writeItemElements(temp2);
			writeEnchantEffect(temp2);
			writeD(0x00);//Visible itemID
            writeQ(temp2.getOwnersPrice());
			writeQ(temp2.getStorePrice());
		}
	}
}