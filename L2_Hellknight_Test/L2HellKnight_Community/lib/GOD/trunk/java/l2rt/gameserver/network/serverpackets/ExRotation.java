package l2rt.gameserver.network.serverpackets;

/**
 * @author : Ragnarok
 * @date : 19.12.10    13:15
 */
public class ExRotation extends L2GameServerPacket{
    private int objId, heading;

    public ExRotation(int objId, int heading) {
        this.objId = objId;
        this.heading = heading;
    }

    @Override
    protected void writeImpl() {
		writeC(EXTENDED_PACKET);
        writeH(0xC0);
        writeD(objId);
        writeD(heading);
    }
}
