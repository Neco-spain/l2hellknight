package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;

public class GMViewItemList extends L2GameServerPacket
{
	private L2ItemInstance[] _items;
	private L2Player _player;

	public GMViewItemList(L2Player cha)
	{
		_items = cha.getInventory().getItems();
		_player = cha;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x9a);
		writeS(_player.getName());
		writeD(_player.getInventoryLimit()); //c4?
		writeH(1); // show window ??
		writeH(_items.length);
		for(L2ItemInstance temp : _items)
		{
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getEquipSlot());
			writeQ(temp.getCount());
			writeH(temp.getItem().getType2ForPackets()); // item type2
			writeH(temp.getCustomType1()); // ?
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart()); // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
			writeH(temp.getEnchantLevel()); // enchant level
			writeH(temp.getCustomType2()); // ?
			writeH(temp.getAugmentationId());
			writeH(0x00);
			writeD(temp.isShadowItem() ? temp.getLifeTimeRemaining() : -1); //interlude
			writeD(temp.isTemporalItem() ? temp.getLifeTimeRemaining() : 0x00); // limited time item life remaining
			writeH(1);
			writeItemElements(temp);
			writeEnchantEffect();
			writeD(0x00);//Visible itemID
		}
		
		short some_count = 0; // кол-во хз чего
		writeH(some_count);
		if(some_count > 0)
		{
			writeC(0); //?
			for(int i = 0; i < some_count; i++)
			{
				writeD(0);
			}
		}
	}
}