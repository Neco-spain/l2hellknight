package l2p.gameserver.serverpackets;

import l2p.gameserver.model.jump.JumpLocation;

import java.util.List;

public class ExFlyMove extends L2GameServerPacket {
    public static final int FLY_TYPE_CHOOSE = 1;// (also 0) Выбор одного из нескольких вариантов
    public static final int FLY_TYPE_JUMP = 2;// Прыжок

    private int flyType;
    private int objId;
    private List<JumpLocation> locations;

    public ExFlyMove(int flyType, int objId, List<JumpLocation> locations) {
        this.flyType = flyType;
        this.objId = objId;
        this.locations = locations;
    }


    @Override
    protected final void writeImpl() {
        writeEx(0xE7);

        writeD(objId);
        writeD(flyType);
        writeD(0); // unknown
        writeD(0); // unknown
        writeD(locations.size());
        for (JumpLocation jumpLocation : locations) {
            writeD(jumpLocation.getId());
            writeD(0);
            writeD(jumpLocation.getX());
            writeD(jumpLocation.getY());
            writeD(jumpLocation.getZ());
        }
    }
}
