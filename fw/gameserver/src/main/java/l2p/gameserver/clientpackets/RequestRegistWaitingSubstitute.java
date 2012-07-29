package l2p.gameserver.clientpackets;

import l2p.gameserver.model.FindPartyManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.ExWaitWaitingSubStituteInfo;

public final class RequestRegistWaitingSubstitute extends L2GameClientPacket {
    private int _key;

    @Override
    protected void readImpl() {
        _key = readD();
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;

        if (_key == 1)
            FindPartyManager.searchPartyOn(activeChar);
        else
            FindPartyManager.searchPartyOff(activeChar);
    }
}
