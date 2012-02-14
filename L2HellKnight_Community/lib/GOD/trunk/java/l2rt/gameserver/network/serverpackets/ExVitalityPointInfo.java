package l2rt.gameserver.network.serverpackets;

public class ExVitalityPointInfo extends L2GameServerPacket
{
	private final int _VitalityPoint;

	public ExVitalityPointInfo(int VitalityPoint)
	{
		_VitalityPoint = VitalityPoint;
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xA0);
		writeD(_VitalityPoint);
	}
}