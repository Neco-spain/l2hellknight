package l2p.gameserver.serverpackets;

public class ExWaitWaitingSubStituteInfo extends L2GameServerPacket {
    private boolean _open;

    public ExWaitWaitingSubStituteInfo(boolean open) {
        _open = open;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x103);
        writeD(_open);
    }
}
