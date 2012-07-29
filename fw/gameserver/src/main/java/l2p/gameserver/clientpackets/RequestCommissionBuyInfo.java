package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.commission.CommissionShopManager;
import l2p.gameserver.model.Player;

/**
 * @author : Ragnarok
 * @date : 22.04.12  11:52
 */
public class RequestCommissionBuyInfo extends L2GameClientPacket {
    private long auctionId;
    private int exItemType;

    @Override
    protected void readImpl() throws Exception {
        auctionId = readQ();
        exItemType = readD();
    }

    @Override
    protected void runImpl() throws Exception {
        Player player = getClient().getActiveChar();
        if (player == null)
            return;
        CommissionShopManager.getInstance().showCommissionBuyInfo(player, auctionId, exItemType);
    }
}
