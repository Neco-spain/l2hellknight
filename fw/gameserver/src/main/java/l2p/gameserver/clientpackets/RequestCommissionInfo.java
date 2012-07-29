package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.commission.CommissionShopManager;
import l2p.gameserver.model.Player;

/**
 * @author : Ragnarok
 * @date : 10.02.12  0:39
 * 
 * Приходит при нажатии на итем в окне регистрации вещей на продажу.
 * Отправляет {@link l2p.gameserver.serverpackets.ExResponseCommissionInfo}
 */
public class RequestCommissionInfo extends L2GameClientPacket {
    private int itemObjId;

    @Override
    protected void readImpl() throws Exception {
        itemObjId = readD(); // id выбранного итема
    }

    @Override
    protected void runImpl() throws Exception {
        Player player = getClient().getActiveChar();
        if(player == null) {
            return;
        }
        CommissionShopManager.getInstance().showCommissionInfo(player, itemObjId);
    }
}
