package l2p.gameserver.serverpackets;

public class ExChangeAttributeFail extends L2GameServerPacket {

    @Override
    protected void writeImpl() {
        writeC(0xFE);
        writeH(0x11A);
    }
}