package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.HennaUnequipList;

public class RequestHennaUnequipList extends L2GameClientPacket {
    private int _symbolId;

    /**
     * format: d
     */
    @Override
    protected void readImpl() {
        _symbolId = readD(); //?
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;

        HennaUnequipList he = new HennaUnequipList(activeChar);
        activeChar.sendPacket(he);
    }
}