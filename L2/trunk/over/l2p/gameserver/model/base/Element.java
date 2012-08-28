package l2p.gameserver.model.base;

import java.util.Arrays;
import l2p.gameserver.stats.Stats;

public enum Element
{
  FIRE(0, Stats.ATTACK_FIRE, Stats.DEFENCE_FIRE), 
  WATER(1, Stats.ATTACK_WATER, Stats.DEFENCE_WATER), 
  WIND(2, Stats.ATTACK_WIND, Stats.DEFENCE_WIND), 
  EARTH(3, Stats.ATTACK_EARTH, Stats.DEFENCE_EARTH), 
  HOLY(4, Stats.ATTACK_HOLY, Stats.DEFENCE_HOLY), 
  UNHOLY(5, Stats.ATTACK_UNHOLY, Stats.DEFENCE_UNHOLY), 
  NONE(-2, null, null);

  public static final Element[] VALUES;
  private final int id;
  private final Stats attack;
  private final Stats defence;

  private Element(int id, Stats attack, Stats defence) { this.id = id;
    this.attack = attack;
    this.defence = defence;
  }

  public int getId()
  {
    return id;
  }

  public Stats getAttack()
  {
    return attack;
  }

  public Stats getDefence()
  {
    return defence;
  }

  public static Element getElementById(int id)
  {
    for (Element e : VALUES)
      if (e.getId() == id)
        return e;
    return NONE;
  }

  public static Element getReverseElement(Element element)
  {
    switch (1.$SwitchMap$l2p$gameserver$model$base$Element[element.ordinal()])
    {
    case 1:
      return FIRE;
    case 2:
      return WATER;
    case 3:
      return EARTH;
    case 4:
      return WIND;
    case 5:
      return UNHOLY;
    case 6:
      return HOLY;
    }

    return NONE;
  }

  public static Element getElementByName(String name)
  {
    for (Element e : VALUES)
      if (e.name().equalsIgnoreCase(name))
        return e;
    return NONE;
  }

  static
  {
    VALUES = (Element[])Arrays.copyOf(values(), 6);
  }
}