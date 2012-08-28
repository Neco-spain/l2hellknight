package net.sf.l2j.gameserver.templates;

public class L2HelperBuff
{
  private int _lowerLevel;
  private int _upperLevel;
  private int _skillID;
  private int _skillLevel;
  private boolean _isMagicClass;

  public L2HelperBuff(StatsSet set)
  {
    _lowerLevel = set.getInteger("lowerLevel");
    _upperLevel = set.getInteger("upperLevel");
    _skillID = set.getInteger("skillID");
    _skillLevel = set.getInteger("skillLevel");

    if ("false".equals(set.getString("isMagicClass")))
      _isMagicClass = false;
    else
      _isMagicClass = true;
  }

  public int getLowerLevel()
  {
    return _lowerLevel;
  }

  public void setLowerLevel(int lowerLevel)
  {
    _lowerLevel = lowerLevel;
  }

  public int getUpperLevel()
  {
    return _upperLevel;
  }

  public void setUpperLevel(int upperLevel)
  {
    _upperLevel = upperLevel;
  }

  public int getSkillID()
  {
    return _skillID;
  }

  public void setSkillID(int skillID)
  {
    _skillID = skillID;
  }

  public int getSkillLevel()
  {
    return _skillLevel;
  }

  public void setSkillLevel(int skillLevel)
  {
    _skillLevel = skillLevel;
  }

  public boolean isMagicClassBuff()
  {
    return _isMagicClass;
  }

  public void setIsMagicClass(boolean isMagicClass)
  {
    _isMagicClass = isMagicClass;
  }
}