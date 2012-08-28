package l2p.gameserver.serverpackets;

import java.util.List;

public class ExGetBossRecord extends L2GameServerPacket
{
  private List<BossRecordInfo> _bossRecordInfo;
  private int _ranking;
  private int _totalPoints;

  public ExGetBossRecord(int ranking, int totalScore, List<BossRecordInfo> bossRecordInfo)
  {
    _ranking = ranking;
    _totalPoints = totalScore;
    _bossRecordInfo = bossRecordInfo;
  }

  protected final void writeImpl()
  {
    writeEx(52);

    writeD(_ranking);
    writeD(_totalPoints);

    writeD(_bossRecordInfo.size());
    for (BossRecordInfo w : _bossRecordInfo)
    {
      writeD(w._bossId);
      writeD(w._points);
      writeD(w._unk1);
    }
  }

  public static class BossRecordInfo {
    public int _bossId;
    public int _points;
    public int _unk1;

    public BossRecordInfo(int bossId, int points, int unk1) {
      _bossId = bossId;
      _points = points;
      _unk1 = unk1;
    }
  }
}