package l2p.gameserver.serverpackets;

/**
 * Format (ch)dd
 * d: window type
 * d: ban user (1)
 */
public class Ex2ndPasswordCheck extends L2GameServerPacket {
    public static final int PASSWORD_NEW = 0x00;
    public static final int PASSWORD_PROMPT = 0x01;
    public static final int PASSWORD_OK = 0x02;

    int _windowType;
    int _banTime;

    public Ex2ndPasswordCheck(int windowType, int banTime) {
        _windowType = windowType;
        _banTime = banTime;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x109);
        writeD(_windowType);
        writeD(_banTime);
    }
}
