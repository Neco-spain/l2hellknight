package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.ExListPartyMatchingWaitingRoom;

/**
 * @author VISTALL
 */
public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket {
    private int _minLevel, _maxLevel, _page, _classes[];

    @Override
    protected void readImpl() {
        _page = readD();
        _minLevel = readD();
        _maxLevel = readD();
        int size = readD();
        if (size > Byte.MAX_VALUE || size < 0)
            size = 0;
        _classes = new int[size];
        for (int i = 0; i < size; i++)
            _classes[i] = readD();
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;

        activeChar.sendPacket(new ExListPartyMatchingWaitingRoom(activeChar, _minLevel, _maxLevel, _page, _classes));
    }
}