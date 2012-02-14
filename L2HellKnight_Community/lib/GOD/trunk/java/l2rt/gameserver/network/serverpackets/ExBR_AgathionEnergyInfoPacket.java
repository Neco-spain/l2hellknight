package l2rt.gameserver.network.serverpackets;

public class ExBR_AgathionEnergyInfoPacket extends L2GameServerPacket
{

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xC3);
		int count = 0;
		writeD(count);
		for(int i = 0; i < count; i++)
		{
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
		}
	}
}