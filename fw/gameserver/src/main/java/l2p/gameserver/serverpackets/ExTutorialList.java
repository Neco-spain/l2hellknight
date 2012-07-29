package l2p.gameserver.serverpackets;

public class ExTutorialList extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0x6B);
        writeB(new byte[128]);
    }
}