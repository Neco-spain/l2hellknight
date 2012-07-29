package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.commission.CommissionShopManager;
import l2p.gameserver.model.Player;

/**
 * @author : Ragnarok
 * @date : 09.02.12  12:33
 *
 * Приходит при нажатия вкладки "Регистрация", запращивает список вещей, которые можно положить в коммиссионный магазин
 * Отправляет {@link l2p.gameserver.serverpackets.ExResponseCommissionItemList}
 */
public class RequestCommissionRegistrableItemList extends L2GameClientPacket {
    @Override
    protected void readImpl() throws Exception {
        // Do nothing
    }

    @Override
    protected void runImpl() throws Exception {
        Player player = getClient().getActiveChar();
        if(player == null) {
            return;
        }
        CommissionShopManager.getInstance().showRegistrableItems(player);
    }
}
