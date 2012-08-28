package l2p.gameserver.utils;

public enum Language
{
  ENGLISH("en"), 
  RUSSIAN("ru");

  public static final Language[] VALUES;
  private String _shortName;

  private Language(String shortName) {
    _shortName = shortName;
  }

  public String getShortName()
  {
    return _shortName;
  }

  static
  {
    VALUES = values();
  }
}