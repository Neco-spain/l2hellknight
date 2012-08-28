package l2p.gameserver.model.items;

import java.io.Serializable;
import l2p.gameserver.model.base.Element;

public class ItemAttributes
  implements Serializable
{
  private static final long serialVersionUID = 401594188363005415L;
  private int fire;
  private int water;
  private int wind;
  private int earth;
  private int holy;
  private int unholy;

  public ItemAttributes()
  {
    this(0, 0, 0, 0, 0, 0);
  }

  public ItemAttributes(int fire, int water, int wind, int earth, int holy, int unholy)
  {
    this.fire = fire;
    this.water = water;
    this.wind = wind;
    this.earth = earth;
    this.holy = holy;
    this.unholy = unholy;
  }

  public int getFire()
  {
    return fire;
  }

  public void setFire(int fire)
  {
    this.fire = fire;
  }

  public int getWater()
  {
    return water;
  }

  public void setWater(int water)
  {
    this.water = water;
  }

  public int getWind()
  {
    return wind;
  }

  public void setWind(int wind)
  {
    this.wind = wind;
  }

  public int getEarth()
  {
    return earth;
  }

  public void setEarth(int earth)
  {
    this.earth = earth;
  }

  public int getHoly()
  {
    return holy;
  }

  public void setHoly(int holy)
  {
    this.holy = holy;
  }

  public int getUnholy()
  {
    return unholy;
  }

  public void setUnholy(int unholy)
  {
    this.unholy = unholy;
  }

  public Element getElement()
  {
    if (fire > 0)
      return Element.FIRE;
    if (water > 0)
      return Element.WATER;
    if (wind > 0)
      return Element.WIND;
    if (earth > 0)
      return Element.EARTH;
    if (holy > 0)
      return Element.HOLY;
    if (unholy > 0) {
      return Element.UNHOLY;
    }
    return Element.NONE;
  }

  public int getValue()
  {
    if (fire > 0)
      return fire;
    if (water > 0)
      return water;
    if (wind > 0)
      return wind;
    if (earth > 0)
      return earth;
    if (holy > 0)
      return holy;
    if (unholy > 0) {
      return unholy;
    }
    return 0;
  }

  public void setValue(Element element, int value)
  {
    switch (1.$SwitchMap$l2p$gameserver$model$base$Element[element.ordinal()])
    {
    case 1:
      fire = value;
      break;
    case 2:
      water = value;
      break;
    case 3:
      wind = value;
      break;
    case 4:
      earth = value;
      break;
    case 5:
      holy = value;
      break;
    case 6:
      unholy = value;
    }
  }

  public int getValue(Element element)
  {
    switch (1.$SwitchMap$l2p$gameserver$model$base$Element[element.ordinal()])
    {
    case 1:
      return fire;
    case 2:
      return water;
    case 3:
      return wind;
    case 4:
      return earth;
    case 5:
      return holy;
    case 6:
      return unholy;
    }
    return 0;
  }

  public ItemAttributes clone()
  {
    return new ItemAttributes(fire, water, wind, earth, holy, unholy);
  }
}