package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.itemmall.ItemMall;
import l2rt.gameserver.model.L2Player;

public class RequestExBR_ProductInfo extends L2GameClientPacket
{
    private int iProductID;

    @Override
    public void readImpl() {
        iProductID = readD();
    }

    @Override
    public void runImpl() {
        L2Player player = getClient().getActiveChar();

        if (player == null)
            return;
        else
            ItemMall.getInstance().showItemInfo(player, iProductID);
    }
}