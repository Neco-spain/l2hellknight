package l2rt.gameserver.network.serverpackets;

import javolution.util.FastList;
import l2rt.gameserver.model.base.UsablePacketItem;

public class ExGetCrystalizingEstimation extends L2GameServerPacket
{
    private FastList<UsablePacketItem> products;

    public ExGetCrystalizingEstimation(FastList<UsablePacketItem> products)
	{
        this.products = products;
    }

    @Override
    protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
        writeH(0xe0);
        writeD(products.size());
        for (UsablePacketItem item : products)
		{
            writeD(item.itemId);
            writeQ(item.count);
            writeF(item.prob);
        }
    }
}