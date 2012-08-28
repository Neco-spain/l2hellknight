package net.sf.l2j.gameserver.templates;

public enum L2ArmorType
{
  NONE(1, "None"), 
  LIGHT(2, "Light"), 
  HEAVY(3, "Heavy"), 
  MAGIC(4, "Magic"), 
  PET(5, "Pet");

  final int _id;
  final String _name;

  private L2ArmorType(int id, String name)
  {
    _id = id;
    _name = name;
  }

  public int mask()
  {
    return 1 << _id + 16;
  }

  public String toString()
  {
    return _name;
  }
}