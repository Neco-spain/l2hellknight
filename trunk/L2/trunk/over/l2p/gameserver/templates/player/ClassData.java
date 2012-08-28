package l2p.gameserver.templates.player;

import gnu.trove.TIntObjectHashMap;

public class ClassData
{
  private final int _classId;
  private final TIntObjectHashMap<LvlUpData> _lvlsUpData = new TIntObjectHashMap();

  public ClassData(int classId)
  {
    _classId = classId;
  }

  public void addLvlUpData(int lvl, double hp, double mp, double cp)
  {
    _lvlsUpData.put(lvl, new LvlUpData(hp, mp, cp));
  }

  public LvlUpData getLvlUpData(int lvl)
  {
    return (LvlUpData)_lvlsUpData.get(lvl);
  }

  public int getClassId()
  {
    return _classId;
  }
}