package l2m.gameserver.templates.mapregion;

import l2m.gameserver.model.Territory;

public class DomainArea
  implements RegionData
{
  private final int _id;
  private final Territory _territory;

  public DomainArea(int id, Territory territory)
  {
    _id = id;
    _territory = territory;
  }

  public int getId()
  {
    return _id;
  }

  public Territory getTerritory()
  {
    return _territory;
  }
}