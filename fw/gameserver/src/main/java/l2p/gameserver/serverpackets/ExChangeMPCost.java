package l2p.gameserver.serverpackets;

/**
 * @author : Ragnarok
 * @date : 11.04.12  16:03
 */
public class ExChangeMPCost extends L2GameServerPacket {
    private int unk1;
    private double unk2;

    public ExChangeMPCost(int unk1, double unk2) {
        this.unk1 = unk1;
        this.unk2 = unk2;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xEA);
        writeD(unk1);// TODO unknown
        writeF(unk2);// TODO unknown
    }
}
