package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.ExBR_ProductList;

public class RequestExBR_ProductList extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();

        if (activeChar == null)
            return;

        activeChar.sendPacket(new ExBR_ProductList());
    }
}