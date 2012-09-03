package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ExBR_GamePoint;

public class RequestExBR_GamePoint extends L2GameClientPacket
{
    @Override
    public void readImpl() {
    }

    @Override
    public void runImpl() {
        L2Player activeChar = getClient().getActiveChar();

        if (activeChar == null)
            return;

        activeChar.sendPacket(new ExBR_GamePoint(activeChar.getObjectId(), activeChar.getNetConnection().getPoint()));
    }
}