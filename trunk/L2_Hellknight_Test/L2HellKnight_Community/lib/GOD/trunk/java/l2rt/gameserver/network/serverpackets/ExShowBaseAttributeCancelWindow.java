package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;

import java.util.Vector;

/**
 * @author SYS
 */
public class ExShowBaseAttributeCancelWindow extends L2GameServerPacket
{
	private final Vector<L2ItemInstance> _items = new Vector<L2ItemInstance>();
	private long _price;

	public ExShowBaseAttributeCancelWindow(L2Player activeChar, long price)
	{
		for(L2ItemInstance i : activeChar.getInventory().getItemsList())
		{
			if(i.hasAttribute())
                _items.add(i);
		}
		_price = price; //FIXME по идее должно быть отдельно для каждой _items
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x74);
		writeD(_items.size());
		for(L2ItemInstance i : _items)
		{
			writeD(i.getObjectId());
			writeQ(_price);
		}
	}
}