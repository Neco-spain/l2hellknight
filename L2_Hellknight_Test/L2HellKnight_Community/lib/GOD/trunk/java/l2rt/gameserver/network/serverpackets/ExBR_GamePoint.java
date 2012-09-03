package l2rt.gameserver.network.serverpackets;

public class ExBR_GamePoint extends L2GameServerPacket
{
    private int objectId;
    private int points;
	
    public ExBR_GamePoint(int objectId, int points) {
        this.objectId = objectId;
        this.points = points;
    }

    @Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xC9);
        writeD(objectId);
        writeQ(points);
        writeD(0x00);
	}
}