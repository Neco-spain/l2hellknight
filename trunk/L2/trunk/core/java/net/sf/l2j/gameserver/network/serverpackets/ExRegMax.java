//L2DDT
package net.sf.l2j.gameserver.network.serverpackets;

public class ExRegMax extends L2GameServerPacket
{

	private static final String _S__FE_01_EXREGMAX = "[S] FE:01 ExRegenMax";
	private double _max;
	private int _count;
	private int _time;

	public ExRegMax(double max, int count, int time)
	{
		_max = max;
		_count = count;
		_time = time;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x01);
		writeD(1);
		writeD(_count);
		writeD(_time);
		writeF(_max);
	}

	@Override
	public String getType()
	{
		return _S__FE_01_EXREGMAX;
	}
	
}
