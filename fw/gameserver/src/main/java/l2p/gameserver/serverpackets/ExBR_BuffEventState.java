package l2p.gameserver.serverpackets;

public class ExBR_BuffEventState extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0xDB);
        // TODO dddd
    }
}