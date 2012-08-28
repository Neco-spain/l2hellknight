package net.sf.l2j.gameserver.model.base;

import net.sf.l2j.Config;

public final class SubClass
{
  private PlayerClass _class;
  private long _exp = Experience.LEVEL[Config.SUB_START_LVL];
  private int _sp = 0;
  private byte _level = Config.SUB_START_LVL;
  private int _classIndex = 1;

  public SubClass(int classId, long exp, int sp, byte level, int classIndex)
  {
    _class = PlayerClass.values()[classId];
    _exp = exp;
    _sp = sp;
    _level = level;
    _classIndex = classIndex;
  }

  public SubClass(int classId, int classIndex)
  {
    _class = PlayerClass.values()[classId];
    _classIndex = classIndex;
  }

  public SubClass()
  {
  }

  public PlayerClass getClassDefinition()
  {
    return _class;
  }

  public int getClassId()
  {
    return _class.ordinal();
  }

  public long getExp()
  {
    return _exp;
  }

  public int getSp()
  {
    return _sp;
  }

  public byte getLevel()
  {
    return _level;
  }

  public int getClassIndex()
  {
    return _classIndex;
  }

  public void setClassId(int classId)
  {
    _class = PlayerClass.values()[classId];
  }

  public void setExp(long expValue)
  {
    if (expValue > Experience.LEVEL[81]) {
      expValue = Experience.LEVEL[81];
    }
    _exp = expValue;
  }

  public void setSp(int spValue)
  {
    _sp = spValue;
  }

  public void setClassIndex(int classIndex)
  {
    _classIndex = classIndex;
  }

  public void setLevel(byte levelValue)
  {
    if (levelValue > 80)
      levelValue = 80;
    else if (levelValue < Config.SUB_START_LVL) {
      levelValue = Config.SUB_START_LVL;
    }
    _level = levelValue;
  }

  public void incLevel()
  {
    if (getLevel() == 80) {
      return;
    }
    _level = (byte)(_level + 1);
    setExp(Experience.LEVEL[getLevel()]);
  }

  public void decLevel()
  {
    if (getLevel() == Config.SUB_START_LVL) {
      return;
    }
    _level = (byte)(_level - 1);
    setExp(Experience.LEVEL[getLevel()]);
  }
}