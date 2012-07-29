package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;

public class RequestPrivateStoreQuitBuy extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;

        if (!activeChar.isInStoreMode() || activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_BUY) {
            activeChar.sendActionFailed();
            return;
        }

        activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
        activeChar.standUp();
        activeChar.broadcastCharInfo();
    }
}