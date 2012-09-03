package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.L2Item;
import l2rt.util.GArray;

/**
 * Format: dQd[hddQhhdhhhddddddddd]
 */
public class PackageSendableList extends L2GameServerPacket
{
	private int player_obj_id;
	private long char_adena;
	private GArray<L2ItemInstance> _itemslist = new GArray<L2ItemInstance>();

	public PackageSendableList(L2Player cha, int playerObjId)
	{
		player_obj_id = playerObjId;
		char_adena = cha.getAdena();
		for(L2ItemInstance item : cha.getInventory().getItems())
			if(item != null && item.canBeStored(cha, false))
				_itemslist.add(item);
	}

	@Override
	protected final void writeImpl()
	{
		if(player_obj_id == 0)
			return;

		writeC(0xD2);
		writeD(player_obj_id);

		writeQ(char_adena);
		writeD(_itemslist.size());
		for(L2ItemInstance temp : _itemslist)
		{
			L2Item item = temp.getItem();
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeQ(temp.getCount());
			writeH(item.getType2ForPackets());
			writeH(temp.getCustomType1());
			writeD(temp.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeH(0x00); // ?
			writeD(temp.getObjectId()); // some item identifier later used by client to answer (see RequestPackageSend) not item id nor object id maybe some freight system id??
			writeItemElements(temp);
			writeEnchantEffect(temp);
		}
	}
}