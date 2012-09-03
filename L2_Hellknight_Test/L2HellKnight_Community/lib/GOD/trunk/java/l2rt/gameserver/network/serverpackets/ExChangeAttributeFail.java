package l2rt.gameserver.network.serverpackets;

public class ExChangeAttributeFail extends L2GameServerPacket 
{

    @Override
    protected void writeImpl() 
	{
    	writeC(0xFE);
    	writeH(0x11A);
    }
}