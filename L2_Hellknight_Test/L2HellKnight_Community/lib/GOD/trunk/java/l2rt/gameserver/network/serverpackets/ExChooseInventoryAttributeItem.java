package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.items.L2ItemInstance;

public class ExChooseInventoryAttributeItem extends L2GameServerPacket 
{
    private L2ItemInstance item;
    private byte element;
    private byte level;

    public ExChooseInventoryAttributeItem(L2ItemInstance item) 
	{
        this.item = item;
        element = item.getEnchantAttributeStoneElement(false);
        level = item.getAttributeElementLevel() > 0? item.getAttributeElementLevel() : 0;
    }

    @Override
    protected void writeImpl() 
	{
		writeC(EXTENDED_PACKET);
        writeH(0x62);
        writeD(item.getItemId());
        for(byte i=0; i < 6; i++)
            writeD(i == element ? 1 : 0);
        writeD(level);
    }
}