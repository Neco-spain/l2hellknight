package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.EnchantResult;

public class RequestExCancelEnchantItem extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar != null) {
            activeChar.setEnchantScroll(null);
            activeChar.sendPacket(EnchantResult.CANCEL);
        }
    }
}