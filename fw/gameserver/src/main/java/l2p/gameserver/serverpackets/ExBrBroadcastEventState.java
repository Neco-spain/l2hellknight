package l2p.gameserver.serverpackets;

public class ExBrBroadcastEventState extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0xBD);
        // TODO dddddddSS
    }
}