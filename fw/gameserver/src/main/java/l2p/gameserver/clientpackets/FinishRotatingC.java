package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.FinishRotating;

/**
 * format:		cdd
 */
public class FinishRotatingC extends L2GameClientPacket {
    private int _degree;
    @SuppressWarnings("unused")
    private int _unknown;

    @Override
    protected void readImpl() {
        _degree = readD();
        _unknown = readD();
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;
        activeChar.broadcastPacket(new FinishRotating(activeChar, _degree, 0));
    }
}