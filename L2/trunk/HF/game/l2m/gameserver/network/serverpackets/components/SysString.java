package l2m.gameserver.serverpackets.components;

public enum SysString
{
  PASSENGER_BOAT_INFO(801), 

  PREVIOUS(1037), 

  NEXT(1038);

  private static final SysString[] VALUES;
  private final int _id;

  private SysString(int i) {
    _id = i;
  }

  public int getId()
  {
    return _id;
  }

  public static SysString valueOf2(String id)
  {
    for (SysString m : VALUES) {
      if (m.name().equals(id))
        return m;
    }
    return null;
  }

  public static SysString valueOf(int id)
  {
    for (SysString m : VALUES) {
      if (m.getId() == id)
        return m;
    }
    return null;
  }

  static
  {
    VALUES = values();
  }
}