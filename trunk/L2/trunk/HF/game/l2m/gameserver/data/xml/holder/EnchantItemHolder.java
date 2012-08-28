package l2m.gameserver.data.xml.holder;

import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.templates.item.support.EnchantScroll;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.napile.primitive.sets.IntSet;

public class EnchantItemHolder extends AbstractHolder
{
  private static EnchantItemHolder _instance = new EnchantItemHolder();

  private IntObjectMap<EnchantScroll> _enchantScrolls = new HashIntObjectMap();

  public static EnchantItemHolder getInstance()
  {
    return _instance;
  }

  public void addEnchantScroll(EnchantScroll enchantScroll)
  {
    _enchantScrolls.put(enchantScroll.getItemId(), enchantScroll);
  }

  public EnchantScroll getEnchantScroll(int id)
  {
    return (EnchantScroll)_enchantScrolls.get(id);
  }

  public int[] getEnchantScrolls()
  {
    return _enchantScrolls.keySet().toArray();
  }

  public void log()
  {
    info("load " + _enchantScrolls.size() + " enchant scroll(s).");
  }

  public int size()
  {
    return 0;
  }

  public void clear()
  {
  }
}