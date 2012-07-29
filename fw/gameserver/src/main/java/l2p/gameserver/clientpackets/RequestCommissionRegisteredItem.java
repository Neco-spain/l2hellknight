package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.commission.CommissionShopManager;
import l2p.gameserver.model.Player;

/**
 * @author : Ragnarok
 * @date : 09.02.12  12:34
 *
 * Приходит при нажатии вкладки "Регистрация", запрашивает список вещей, находящихся в коммиссионном магазине
 * Отправляет {@link l2p.gameserver.serverpackets.ExResponseCommissionList}
 */
public class RequestCommissionRegisteredItem extends L2GameClientPacket {
    @Override
    protected void readImpl() throws Exception {
        // Trigger
    }

    @Override
    protected void runImpl() throws Exception {
        Player player = getClient().getActiveChar();
        if(player == null) {
            return;
        }
        CommissionShopManager.getInstance().showPlayerRegisteredItems(player);
    }
}
