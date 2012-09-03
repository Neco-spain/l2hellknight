package l2rt.gameserver.network.serverpackets;

import java.util.TreeSet;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;

public class TradeStart extends L2GameServerPacket
{
	private TreeSet<L2ItemInstance> _tradelist = new TreeSet<L2ItemInstance>(Inventory.OrderComparator);
	private boolean can_writeImpl = false;
	private int requester_obj_id;

	public TradeStart(L2Player me, L2Player other)
	{
		if (me == null)
		{
			return;
		}
		requester_obj_id = other.getObjectId();
		L2ItemInstance[] inventory = me.getInventory().getItems();
		for(L2ItemInstance item : inventory)
		{
			if ((item == null) || (!item.canBeTraded(me)))
				continue;
			_tradelist.add(item);
		}

		can_writeImpl = true;
	}

	protected final void writeImpl()
	{
		if (!can_writeImpl)
		{
			return;
		}
		writeC(0x14);
		writeD(requester_obj_id);
		int count = _tradelist.size();
		writeH(count);
		for (L2ItemInstance temp : _tradelist)
		{
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getEquipSlot());
			writeQ(temp.getCount());
			writeH(temp.getItem().getType2ForPackets());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeD(temp.getAugmentationId());
			writeD(temp.isShadowItem() ? temp.getLifeTimeRemaining() : -1);
			writeD(temp.isTemporalItem() ? temp.getLifeTimeRemaining() : 0);
			writeH(1);
			writeItemElements(temp);
			writeEnchantEffect();
			writeD(0x00);//Visible itemID
		}
	}

}