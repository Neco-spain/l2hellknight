package l2p.gameserver.clientpackets;

import l2p.gameserver.serverpackets.SendStatus;

public final class RequestStatus extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        getClient().close(new SendStatus());
    }
}