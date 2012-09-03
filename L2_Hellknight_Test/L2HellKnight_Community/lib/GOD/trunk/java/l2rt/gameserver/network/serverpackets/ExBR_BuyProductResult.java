package l2rt.gameserver.network.serverpackets;

public class ExBR_BuyProductResult extends L2GameServerPacket
{
    private int code;
    public ExBR_BuyProductResult(int code) {
        this.code = code;
    }

    @Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xCC);
		writeD(code);
	}
}