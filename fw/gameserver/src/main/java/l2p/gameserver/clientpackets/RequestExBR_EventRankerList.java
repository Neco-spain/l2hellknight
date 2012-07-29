package l2p.gameserver.clientpackets;

public class RequestExBR_EventRankerList extends L2GameClientPacket {
    private int unk, unk2, unk3;

    /**
     * format: ddd
     */
    @Override
    protected void readImpl() {
        unk = readD();
        unk2 = readD();
        unk3 = readD();
    }

    @Override
    protected void runImpl() {
        //TODO not implemented
    }

}