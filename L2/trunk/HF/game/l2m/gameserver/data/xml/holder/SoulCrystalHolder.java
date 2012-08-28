package l2m.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.templates.SoulCrystal;

public final class SoulCrystalHolder extends AbstractHolder
{
  private static final SoulCrystalHolder _instance = new SoulCrystalHolder();

  private final TIntObjectHashMap<SoulCrystal> _crystals = new TIntObjectHashMap();

  public static SoulCrystalHolder getInstance()
  {
    return _instance;
  }

  public void addCrystal(SoulCrystal crystal)
  {
    _crystals.put(crystal.getItemId(), crystal);
  }

  public SoulCrystal getCrystal(int item)
  {
    return (SoulCrystal)_crystals.get(item);
  }

  public SoulCrystal[] getCrystals()
  {
    return (SoulCrystal[])_crystals.getValues(new SoulCrystal[_crystals.size()]);
  }

  public int size()
  {
    return _crystals.size();
  }

  public void clear()
  {
    _crystals.clear();
  }
}