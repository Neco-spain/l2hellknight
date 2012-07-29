package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Creature;
import l2p.gameserver.serverpackets.ExCursedWeaponList;

public class RequestCursedWeaponList extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        Creature activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;

        activeChar.sendPacket(new ExCursedWeaponList());
    }
}