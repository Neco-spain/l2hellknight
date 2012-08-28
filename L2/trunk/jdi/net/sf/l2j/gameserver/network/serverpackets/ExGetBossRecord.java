package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ExGetBossRecord extends L2GameServerPacket
{
  private static final String _S__FE_33_EXGETBOSSRECORD = "[S] FE:33 ExGetBossRecord";
  private final Map<Integer, Integer> _bossRecordInfo;
  private final int _ranking;
  private final int _totalPoints;

  public ExGetBossRecord(int ranking, int totalScore, Map<Integer, Integer> list)
  {
    _ranking = ranking;
    _totalPoints = totalScore;
    _bossRecordInfo = list;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(51);
    writeD(_ranking);
    writeD(_totalPoints);
    Iterator i$;
    if (_bossRecordInfo == null)
    {
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
    }
    else
    {
      writeD(_bossRecordInfo.size());
      for (i$ = _bossRecordInfo.keySet().iterator(); i$.hasNext(); ) { int bossId = ((Integer)i$.next()).intValue();

        writeD(bossId);
        writeD(((Integer)_bossRecordInfo.get(Integer.valueOf(bossId))).intValue());
        writeD(0);
      }
    }
  }

  public String getType()
  {
    return "[S] FE:33 ExGetBossRecord";
  }
}