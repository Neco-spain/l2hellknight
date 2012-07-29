package l2p.gameserver.serverpackets;

public class ExLoadStatWorldRank extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0x100);
        // writeD(0x00); // ?
        // writeD(0x00); // ?
    }
}
