package l2p.gameserver.serverpackets;

/**
 * @author : Ragnarok
 * @date : 17.04.12  0:14
 * <p/>
 * Отображает окно для смены класса на перерожденный
 */
public class ExChangeToAwakenedClass extends L2GameServerPacket {
    private int classId;

    public ExChangeToAwakenedClass(int classId) {
        this.classId = classId;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xFE);
        writeD(classId);
    }
}
