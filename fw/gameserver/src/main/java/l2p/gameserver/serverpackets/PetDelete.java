package l2p.gameserver.serverpackets;

public class PetDelete extends L2GameServerPacket {
    private int _summonType;
    private int _objectId;

    public PetDelete(int summonType, int objectId) {
        _summonType = summonType;
        _objectId = objectId;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xb7);
        writeD(_summonType);// Summon Type
        writeD(_objectId);// Pet ObjectID
    }
}