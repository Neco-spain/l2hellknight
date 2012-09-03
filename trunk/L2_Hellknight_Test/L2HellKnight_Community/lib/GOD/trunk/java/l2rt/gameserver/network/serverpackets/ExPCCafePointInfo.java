package l2rt.gameserver.network.serverpackets;
/**
 * Format: ch ddcdc
 */
public class ExPCCafePointInfo extends L2GameServerPacket
{
	private int mAddPoint, mPeriodType, pointType, pcBangPoints;
	private int remainTime; // Оставшееся время в часах
	public ExPCCafePointInfo()
	{
		pcBangPoints = 0;
		mAddPoint = 0;
		remainTime = 0;
		mPeriodType = 0;
		pointType = 0;
	}
	public ExPCCafePointInfo(final int points, final int modify_points, final boolean mod, final boolean _double, final int hours_left)
	{
		//pcBangPoints = player.getPcBangPoints();
		pcBangPoints = points;
		mAddPoint = modify_points;
		remainTime = hours_left;
		if(mod && _double)
		{
			mPeriodType = 1;
			pointType = 0;
		}
		else if(mod)
		{
			mPeriodType = 1;
			pointType = 1;
		}
		else
		{
			mPeriodType = 2;
			pointType = 2;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x32);
		writeD(pcBangPoints); // num points
		writeD(mAddPoint); // points inc display
		writeC(mPeriodType); // period(0=don't show window,1=acquisition,2=use points)
		writeD(remainTime); // period hours left
		writeC(pointType); // points inc display color(0=yellow,1=cyan-blue,2=red,all other black)
	}
}