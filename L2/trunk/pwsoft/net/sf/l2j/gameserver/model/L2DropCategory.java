package net.sf.l2j.gameserver.model;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.util.Rnd;

public class L2DropCategory
{
  private FastList<L2DropData> _drops;
  private int _categoryChance;
  private int _categoryBalancedChance;
  private int _categoryType;

  public L2DropCategory(int categoryType)
  {
    _categoryType = categoryType;
    _drops = new FastList(0);
    _categoryChance = 0;
    _categoryBalancedChance = 0;
  }

  public void addDropData(L2DropData drop, String type)
  {
    if (!drop.isQuestDrop())
    {
      _drops.add(drop);
      _categoryChance += drop.getChance();

      float rate = Config.RATE_DROP_ITEMS;
      if (type.equalsIgnoreCase("L2RaidBoss"))
        rate = Config.RATE_DROP_ITEMS_BY_RAID;
      else if (type.equalsIgnoreCase("L2GrandBoss")) {
        rate = Config.RATE_DROP_ITEMS_BY_GRANDRAID;
      }
      _categoryBalancedChance = (int)(_categoryBalancedChance + Math.min(drop.getChance() * rate, 1000000.0F));
    }
  }

  public FastList<L2DropData> getAllDrops()
  {
    return _drops;
  }

  public void clearAllDrops()
  {
    _drops.clear();
  }

  public boolean isSweep()
  {
    return getCategoryType() == -1;
  }

  public int getCategoryChance()
  {
    if (getCategoryType() >= 0) {
      return _categoryChance;
    }
    return 1000000;
  }

  public int getCategoryBalancedChance()
  {
    if (getCategoryType() >= 0) {
      return _categoryBalancedChance;
    }
    return 1000000;
  }

  public int getCategoryType()
  {
    return _categoryType;
  }

  public synchronized L2DropData dropSeedAllowedDropsOnly()
  {
    FastList drops = new FastList();
    int subCatChance = 0;
    for (L2DropData drop : getAllDrops())
    {
      if ((drop.getItemId() == 57) || (drop.getItemId() == 6360) || (drop.getItemId() == 6361) || (drop.getItemId() == 6362))
      {
        drops.add(drop);
        subCatChance += drop.getChance();
      }

    }

    int randomIndex = Rnd.get(subCatChance);
    int sum = 0;
    for (L2DropData drop : drops)
    {
      sum += drop.getChance();

      if (sum > randomIndex)
      {
        drops.clear();
        drops = null;
        return drop;
      }
    }

    return null;
  }

  public synchronized L2DropData dropOne(boolean raid)
  {
    int randomIndex = Rnd.get(getCategoryBalancedChance());
    int sum = 0;
    for (L2DropData drop : getAllDrops())
    {
      sum = (int)(sum + Math.min(drop.getChance() * (raid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS), 1000000.0F));

      if (sum >= randomIndex)
        return drop;
    }
    return null;
  }
}