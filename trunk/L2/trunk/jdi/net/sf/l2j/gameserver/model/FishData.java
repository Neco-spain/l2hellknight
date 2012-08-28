package net.sf.l2j.gameserver.model;

public class FishData
{
  private int _id;
  private int _level;
  private String _name;
  private int _hp;
  private int _hpRegen;
  private int _type;
  private int _group;
  private int _fishGuts;
  private int _gutsCheckTime;
  private int _waitTime;
  private int _combatTime;

  public FishData(int id, int lvl, String name, int HP, int HpRegen, int type, int group, int fish_guts, int guts_check_time, int wait_time, int combat_time)
  {
    _id = id;
    _level = lvl;
    _name = name.intern();
    _hp = HP;
    _hpRegen = HpRegen;
    _type = type;
    _group = group;
    _fishGuts = fish_guts;
    _gutsCheckTime = guts_check_time;
    _waitTime = wait_time;
    _combatTime = combat_time;
  }

  public FishData(FishData copyOf)
  {
    _id = copyOf.getId();
    _level = copyOf.getLevel();
    _name = copyOf.getName();
    _hp = copyOf.getHP();
    _hpRegen = copyOf.getHpRegen();
    _type = copyOf.getType();
    _group = copyOf.getGroup();
    _fishGuts = copyOf.getFishGuts();
    _gutsCheckTime = copyOf.getGutsCheckTime();
    _waitTime = copyOf.getWaitTime();
    _combatTime = copyOf.getCombatTime();
  }

  public int getId()
  {
    return _id;
  }

  public int getLevel()
  {
    return _level;
  }

  public String getName()
  {
    return _name;
  }

  public int getHP()
  {
    return _hp;
  }

  public int getHpRegen() {
    return _hpRegen;
  }

  public int getType() {
    return _type;
  }

  public int getGroup() {
    return _group;
  }

  public int getFishGuts() {
    return _fishGuts;
  }

  public int getGutsCheckTime() {
    return _gutsCheckTime;
  }

  public int getWaitTime() {
    return _waitTime;
  }

  public int getCombatTime() {
    return _combatTime;
  }

  public void setType(int type) {
    _type = type;
  }
}