package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.ExLoadStatWorldRank;

public final class RequestWorldStatistics extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;

        activeChar.sendPacket(new ExLoadStatWorldRank());
        //activeChar.sendPacket(new ExResponseCommissionItemList(activeChar));
    }
}
