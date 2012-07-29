package l2p.gameserver.serverpackets;

public class Ex2ndPasswordAck extends L2GameServerPacket {
    public static final int SUCCESS = 0;
    public static final int WRONG_PATTERN = 1;

    int _response;

    public Ex2ndPasswordAck(int response) {
        _response = response;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x10b);
        writeC(0);
        writeD(_response == WRONG_PATTERN ? 1 : 0);
        writeD(0);
    }
}
