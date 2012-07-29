package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.ExGetBookMarkInfo;

public class RequestBookMarkSlotInfo extends L2GameClientPacket {
    @Override
    protected void readImpl() {
        //just trigger
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        activeChar.sendPacket(new ExGetBookMarkInfo(activeChar));
    }
}