package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.L2Item;
import l2rt.util.GArray;

public class ExChangeAttributeItemList extends L2GameServerPacket 
{
	private GArray<L2ItemInstance> _itemslist;
    public ExChangeAttributeItemList(L2Player player) 
	{
		_itemslist = new GArray<L2ItemInstance>();
		for(L2ItemInstance item : player.getInventory().getItems())
			if(item != null 
			&& item.canBeTraded(player)
			&& item.getItem().getType2() == L2Item.TYPE2_WEAPON
			&& (item.getItem().getCrystalType() == L2Item.Grade.S || item.getItem().getCrystalType() == L2Item.Grade.S80) 
			&& item.getWeaponElementLevel() >0 )
				_itemslist.add(item);
    }

    @Override
    protected void writeImpl() 
	{
    	writeC(0xFE);
    	writeH(0x117);
    	writeD(33502);
    	writeD(_itemslist.size());
    	for(L2ItemInstance item : _itemslist)
    	{
	    	writeD(item.getObjectId());
	    	writeD(item.getItemId());
	    	writeD(item.getEquipSlot());
	    	writeQ(item.getCount());
	    	writeH(item.getItem().getType2ForPackets());
	    	writeH(item.getCustomType1());
	    	writeH(item.isEquipped() ? 1 : 0);
	    	writeD(item.getItem().getBodyPart());
	    	writeH(item.getEnchantLevel());
	    	writeH(item.getCustomType2());
	    	writeH(item.getAugmentationId());
	    	writeH(0x00); //??
	    	writeD(item.getLifeTimeRemaining());
	    	writeD(item.getLifeTimeRemaining());
	    	writeH(0x01); // блокировать ли вещь( 01 нет, 00 да)
	    	writeH(item.getAttackAttributeElement());
	    	writeH(item.getAttackElementValue());
	    	writeH(item.getDefenceFire());
	    	writeH(item.getDefenceWater());
	    	writeH(item.getDefenceWind());
	    	writeH(item.getDefenceEarth());
	    	writeH(item.getDefenceHoly());
	    	writeH(item.getDefenceUnholy());
	    	writeH(0);
	    	writeH(0);
	    	writeH(0);
			writeD(0x00);//Visible itemID
    	}
    }
}