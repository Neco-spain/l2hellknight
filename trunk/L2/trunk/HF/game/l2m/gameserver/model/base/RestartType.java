package l2m.gameserver.model.base;

public enum RestartType
{
  TO_VILLAGE, 
  TO_CLANHALL, 
  TO_CASTLE, 
  TO_FORTRESS, 
  TO_FLAG, 
  FIXED, 
  TO_VILLAGE1, 
  AGATHION;

  public static final RestartType[] VALUES;

  static { VALUES = values();
  }
}