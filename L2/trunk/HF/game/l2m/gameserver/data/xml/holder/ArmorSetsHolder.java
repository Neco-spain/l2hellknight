package l2m.gameserver.data.xml.holder;

import java.util.ArrayList;
import java.util.List;
import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.model.ArmorSet;

public final class ArmorSetsHolder extends AbstractHolder
{
  private static final ArmorSetsHolder _instance = new ArmorSetsHolder();

  private List<ArmorSet> _armorSets = new ArrayList();

  public static ArmorSetsHolder getInstance()
  {
    return _instance;
  }

  public void addArmorSet(ArmorSet armorset)
  {
    _armorSets.add(armorset);
  }

  public ArmorSet getArmorSet(int chestItemId)
  {
    for (ArmorSet as : _armorSets)
      if (as.getChestItemIds().contains(Integer.valueOf(chestItemId)))
        return as;
    return null;
  }

  public int size()
  {
    return _armorSets.size();
  }

  public void clear()
  {
    _armorSets.clear();
  }
}