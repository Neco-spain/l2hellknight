package l2rt.gameserver.network.serverpackets;

public class ExShowStatPage extends L2GameServerPacket{
    private int ids;

    //статистика музея
    public ExShowStatPage(int id) {
    	ids = id;
    }

    @Override
    protected void writeImpl() {
		writeC(0xfe);
        writeH(0x10e);
        writeD(ids); //xz
        writeS("aa"); //xz
    }
}
