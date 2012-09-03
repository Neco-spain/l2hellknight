package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.itemmall.ItemMall;

public class ExBR_ProductInfo extends L2GameServerPacket
{
    private ItemMall.ItemMallItem item;

    public ExBR_ProductInfo(ItemMall.ItemMallItem item) {
        this.item = item;
    }

    @Override
    protected void writeImpl() {
		writeC(EXTENDED_PACKET);
        writeH(0xCB);

        writeD(item.template.brId);
        writeD(item.price);
        writeD(1);// по идее тут начало повторяющегося блока for()
        writeD(item.template.itemId);
        writeD(item.count);  //quantity // количество
        writeD(item.iWeight); //weight
        writeD(item.iDropable ? 1 : 0); //0 - dont drop/trade
    }
}