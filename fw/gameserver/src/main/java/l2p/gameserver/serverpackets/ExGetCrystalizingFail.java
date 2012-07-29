package l2p.gameserver.serverpackets;

public class ExGetCrystalizingFail extends L2GameServerPacket {
    private int reason;

    public ExGetCrystalizingFail(int id) {
        this.reason = id;
    }

    protected final void writeImpl() {
        writeEx(0xe1);
        writeD(reason);
    }
}