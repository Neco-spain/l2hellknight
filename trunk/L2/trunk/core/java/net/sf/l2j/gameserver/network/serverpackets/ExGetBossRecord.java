package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Map;

/**
 * Format: ch ddd [ddd]
 * 
 * @author KenM
 */
public class ExGetBossRecord extends L2GameServerPacket
{
	private static final String _S__FE_33_EXGETBOSSRECORD = "[S] FE:33 ExGetBossRecord";
	private final Map<Integer, Integer>	_bossRecordInfo;
	private final int						_ranking;
	private final int						_totalPoints;

	public ExGetBossRecord(int ranking, int totalScore, Map<Integer, Integer> list)
	{
		_ranking = ranking;
		_totalPoints = totalScore;
		_bossRecordInfo = list;
	}
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x33);
		writeD(_ranking);
		writeD(_totalPoints);
		if(_bossRecordInfo == null)
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
		else
		{
			writeD(_bossRecordInfo.size());
			for (int bossId : _bossRecordInfo.keySet())
			{
				writeD(bossId);
				writeD(_bossRecordInfo.get(bossId));
				writeD(0x00); //??
			}
		}
	}
	@Override
	public String getType()
	{
		return _S__FE_33_EXGETBOSSRECORD;
	}

}
