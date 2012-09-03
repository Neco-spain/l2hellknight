package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.network.clientpackets.RequestExPostItemList;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.L2Item;

import java.util.TreeSet;

/**
 * Ответ на запрос создания нового письма.
 * Отсылается при получении {@link RequestExPostItemList}
 * Содержит список вещей, которые можно приложить к письму.
 */
public class ExReplyPostItemList extends L2GameServerPacket
{
	private TreeSet<L2ItemInstance> _itemslist = new TreeSet<L2ItemInstance>(Inventory.OrderComparator);

	public ExReplyPostItemList(L2Player cha)
	{
		if(!cha.getPlayerAccess().UseTrade) // если не разрешен трейд передавать предметы нельзя
			return;

		String tradeBan = cha.getVar("tradeBan"); // если трейд забанен тоже
		if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
			return;

		for(L2ItemInstance item : cha.getInventory().getItems())
			if(item != null && item.canBeTraded(cha))
				_itemslist.add(item);
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xB2);

		writeD(_itemslist.size());
		for(L2ItemInstance temp : _itemslist)
		{
			L2Item item = temp.getItem();
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
            writeD(temp.getEquipSlot());
			writeQ(temp.getCount());
			writeH(item.getType2ForPackets());
			writeH(temp.getCustomType1());
            writeH(0x00);//
			writeD(temp.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
            writeH(temp.getAugmentationId());
			writeH(0x00);
            writeD(temp.isShadowItem() ? temp.getLifeTimeRemaining() : -1);
            writeD(temp.isTemporalItem() ? temp.getLifeTimeRemaining() : 0x00);
			writeH(1); // при 0 итем красный(заблокирован) 
			writeItemElements(temp);
			writeEnchantEffect(temp);
			writeD(0x00);//Visible itemID
		}
	}
}