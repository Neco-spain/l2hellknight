package l2rt.gameserver.network.serverpackets;

/**
 * @author : Ragnarok
 * @date : 19.12.10    12:49
 */
public class ExAskCoupleAction extends L2GameServerPacket {
    private int requesterId;
    private int coupleId;

    public ExAskCoupleAction(int objId, int id) {
        requesterId = objId;
        coupleId = id;
    }

    @Override
    protected void writeImpl() {
		writeC(EXTENDED_PACKET);
        writeH(0xBB);
        writeD(coupleId);
        writeD(requesterId);
    }
}
