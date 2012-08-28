package l2p.gameserver.model.entity.events.objects;

public class CastleDamageZoneObject extends ZoneObject
{
  public static final long serialVersionUID = 1L;
  private final long _price;

  public CastleDamageZoneObject(String name, long price)
  {
    super(name);
    _price = price;
  }

  public long getPrice()
  {
    return _price;
  }
}