package l2p.gameserver.serverpackets;

public class ExEventMatchLockResult extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0x0B);
        // TODO пока не реализован даже в клиенте
    }
}