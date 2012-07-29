package l2p.gameserver.serverpackets;

public class ExBrBuffEventState extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0xBF);
        // TODO dddd
    }
}