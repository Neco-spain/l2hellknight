package l2p.gameserver.serverpackets;

public class AutoAttackStop extends L2GameServerPacket {
    // dh
    private int _targetId;

    public AutoAttackStop(int targetId) {
        _targetId = targetId;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x26);
        writeD(_targetId);
    }
}