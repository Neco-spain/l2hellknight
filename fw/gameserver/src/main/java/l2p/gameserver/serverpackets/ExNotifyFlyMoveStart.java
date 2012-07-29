package l2p.gameserver.serverpackets;

public final class ExNotifyFlyMoveStart extends L2GameServerPacket {
    public static ExNotifyFlyMoveStart EX_NOTIFY_FLY_MOVE_START = new ExNotifyFlyMoveStart();

    @Override
    protected void writeImpl() {
        writeEx(0x114);
    }
}