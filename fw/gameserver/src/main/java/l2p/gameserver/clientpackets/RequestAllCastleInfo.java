package l2p.gameserver.clientpackets;

import l2p.gameserver.serverpackets.ExShowCastleInfo;

public class RequestAllCastleInfo extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        getClient().getActiveChar().sendPacket(new ExShowCastleInfo());
    }
}