package l2p.gameserver.templates.item;

import l2p.gameserver.templates.StatsSet;

public final class EtcItemTemplate extends ItemTemplate
{
  public EtcItemTemplate(StatsSet set)
  {
    super(set);
    type = ((ItemType)set.getEnum("type", EtcItemType.class));
    _type1 = 4;
    switch (1.$SwitchMap$l2p$gameserver$templates$item$EtcItemTemplate$EtcItemType[getItemType().ordinal()])
    {
    case 1:
      _type2 = 3;
      break;
    case 2:
      _type2 = 4;
      break;
    default:
      _type2 = 5;
    }
  }

  public EtcItemType getItemType()
  {
    return (EtcItemType)type;
  }

  public long getItemMask()
  {
    return getItemType().mask();
  }

  public final boolean isShadowItem()
  {
    return false;
  }

  public final boolean canBeEnchanted(boolean gradeCheck)
  {
    return false;
  }

  public static enum EtcItemType
    implements ItemType
  {
    ARROW(1, "Arrow"), 
    MATERIAL(2, "Material"), 
    PET_COLLAR(3, "PetCollar"), 
    POTION(4, "Potion"), 
    RECIPE(5, "Recipe"), 
    SCROLL(6, "Scroll"), 
    QUEST(7, "Quest"), 
    MONEY(8, "Money"), 
    OTHER(9, "Other"), 
    SPELLBOOK(10, "Spellbook"), 
    SEED(11, "Seed"), 
    BAIT(12, "Bait"), 
    SHOT(13, "Shot"), 
    BOLT(14, "Bolt"), 
    RUNE(15, "Rune"), 
    HERB(16, "Herb"), 
    MERCENARY_TICKET(17, "Mercenary Ticket");

    private final long _mask;
    private final String _name;

    private EtcItemType(int id, String name) { _mask = (1L << id + WeaponTemplate.WeaponType.VALUES.length + ArmorTemplate.ArmorType.VALUES.length);
      _name = name;
    }

    public long mask()
    {
      return _mask;
    }

    public String toString()
    {
      return _name;
    }
  }
}