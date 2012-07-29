package l2p.gameserver.serverpackets;

/**
 * @author : Ragnarok
 * @date : 28.03.12  16:23
 */
public class ExCallToChangeClass extends L2GameServerPacket {
    private int classId;
    private boolean showMsg;

    public ExCallToChangeClass(int classId, boolean showMsg) {
        this.classId = classId;
        this.showMsg = showMsg;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xFD);
        writeD(classId); // New Class Id
        writeD(showMsg); // Show Message
    }
}
