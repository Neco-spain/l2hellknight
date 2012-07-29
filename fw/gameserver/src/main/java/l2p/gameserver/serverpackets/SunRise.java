package l2p.gameserver.serverpackets;

public class SunRise extends L2GameServerPacket {
    @Override
    protected final void writeImpl() {
        writeC(0x12);
    }
}