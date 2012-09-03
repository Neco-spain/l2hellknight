package l2rt.gameserver.network.serverpackets;

/**
 * @author : Ragnarok
 * @date : 19.12.10    13:19
 */
public class ExAskModifyPartyLooting extends L2GameServerPacket {
    String name;
    int mode;

    public ExAskModifyPartyLooting(String name, int mode) {
        this.name = name;
        this.mode = mode;
    }

    @Override
    protected void writeImpl() {
		writeC(EXTENDED_PACKET);
        writeH(0xBE);
        writeS(name);
        writeD(mode);
    }
}
