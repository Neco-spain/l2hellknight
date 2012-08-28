package l2m.gameserver.model;

import l2m.gameserver.data.xml.holder.ItemHolder;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.utils.Location;

public class TeleportLocation extends Location
{
  public static final long serialVersionUID = 1L;
  private final long _price;
  private final ItemTemplate _item;
  private final int _name;
  private final int _castleId;

  public TeleportLocation(int item, long price, int name, int castleId)
  {
    _price = price;
    _name = name;
    _item = ItemHolder.getInstance().getTemplate(item);
    _castleId = castleId;
  }

  public long getPrice()
  {
    return _price;
  }

  public ItemTemplate getItem()
  {
    return _item;
  }

  public int getName()
  {
    return _name;
  }

  public int getCastleId()
  {
    return _castleId;
  }
}