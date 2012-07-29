package l2p.gameserver.serverpackets;

public class ExEventMatchObserver extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0x0E);
        // TODO dccSS
    }
}