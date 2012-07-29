package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;

public class RequestMagicSkillList extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;
        activeChar.sendSkillList();
    }
}