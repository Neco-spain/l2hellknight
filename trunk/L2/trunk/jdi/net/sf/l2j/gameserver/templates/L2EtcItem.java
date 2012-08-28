package net.sf.l2j.gameserver.templates;

public final class L2EtcItem extends L2Item
{
  public L2EtcItem(L2EtcItemType type, StatsSet set)
  {
    super(type, set);
  }

  public L2EtcItemType getItemType()
  {
    return (L2EtcItemType)_type;
  }

  public final boolean isConsumable()
  {
    return (getItemType() == L2EtcItemType.SHOT) || (getItemType() == L2EtcItemType.POTION);
  }

  public int getItemMask()
  {
    return getItemType().mask();
  }
}