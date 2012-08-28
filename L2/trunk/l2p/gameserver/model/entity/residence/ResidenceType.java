package l2p.gameserver.model.entity.residence;

public enum ResidenceType
{
  Castle, 
  ClanHall, 
  Fortress, 
  Dominion;

  public static final ResidenceType[] VALUES;

  static { VALUES = values();
  }
}