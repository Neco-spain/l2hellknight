package l2m.gameserver.model;

import l2m.gameserver.aConfig;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.model.base.Experience;

public class SubClass
{
  public static final int CERTIFICATION_65 = 1;
  public static final int CERTIFICATION_70 = 2;
  public static final int CERTIFICATION_75 = 4;
  public static final int CERTIFICATION_80 = 8;
  private int _class = 0;
  private long _exp = Experience.LEVEL[aConfig.get("AltLevelNewSubClass", 40)]; private long minExp = Experience.LEVEL[aConfig.get("AltLevelNewSubClass", 40)]; private long maxExp = Experience.LEVEL[(Experience.LEVEL.length - 1)];
  private int _sp = 0;
  private int _level = aConfig.get("AltLevelNewSubClass", 40);
  private int _certification;
  private double _Hp = 1.0D; private double _Mp = 1.0D; private double _Cp = 1.0D;
  private boolean _active = false; private boolean _isBase = false;
  private DeathPenalty _dp;

  public int getClassId()
  {
    return _class;
  }

  public long getExp()
  {
    return _exp;
  }

  public long getMaxExp()
  {
    return maxExp;
  }

  public void addExp(long val)
  {
    setExp(_exp + val);
  }

  public long getSp()
  {
    return Math.min(_sp, 2147483647);
  }

  public void addSp(long val)
  {
    setSp(_sp + val);
  }

  public int getLevel()
  {
    return _level;
  }

  public void setClassId(int classId)
  {
    _class = classId;
  }

  public void setExp(long val)
  {
    val = Math.max(val, minExp);
    val = Math.min(val, maxExp);

    _exp = val;
    _level = Experience.getLevel(_exp);
  }

  public void setSp(long spValue)
  {
    spValue = Math.max(spValue, 0L);
    spValue = Math.min(spValue, 2147483647L);

    _sp = (int)spValue;
  }

  public void setHp(double hpValue)
  {
    _Hp = hpValue;
  }

  public double getHp()
  {
    return _Hp;
  }

  public void setMp(double mpValue)
  {
    _Mp = mpValue;
  }

  public double getMp()
  {
    return _Mp;
  }

  public void setCp(double cpValue)
  {
    _Cp = cpValue;
  }

  public double getCp()
  {
    return _Cp;
  }

  public void setActive(boolean active)
  {
    _active = active;
  }

  public boolean isActive()
  {
    return _active;
  }

  public void setBase(boolean base)
  {
    _isBase = base;
    minExp = Experience.LEVEL[aConfig.get("AltLevelNewSubClass", 40)];
    maxExp = (Experience.LEVEL[(Experience.getMaxSubLevel() + 1)] - 1L);
  }

  public boolean isBase()
  {
    return _isBase;
  }

  public DeathPenalty getDeathPenalty(Player player)
  {
    if (_dp == null)
      _dp = new DeathPenalty(player, 0);
    return _dp;
  }

  public void setDeathPenalty(DeathPenalty dp)
  {
    _dp = dp;
  }

  public int getCertification()
  {
    return _certification;
  }

  public void setCertification(int certification)
  {
    _certification = certification;
  }

  public void addCertification(int c)
  {
    _certification |= c;
  }

  public boolean isCertificationGet(int v)
  {
    return (_certification & v) == v;
  }

  public String toString()
  {
    return ClassId.VALUES[_class].toString() + " " + _level;
  }
}