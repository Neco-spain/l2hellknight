package l2m.gameserver.model.reward;

import java.util.ArrayList;
import java.util.List;
import l2p.commons.math.SafeMath;
import l2p.commons.util.Rnd;
import l2m.gameserver.Config;
import l2m.gameserver.data.xml.holder.ItemHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.templates.item.ItemTemplate;
import org.apache.commons.lang3.ArrayUtils;

public class RewardData
  implements Cloneable
{
  private ItemTemplate _item;
  private boolean _notRate = false;
  private long _mindrop;
  private long _maxdrop;
  private double _chance;
  private double _chanceInGroup;

  public RewardData(int itemId)
  {
    _item = ItemHolder.getInstance().getTemplate(itemId);
    if ((_item.isArrow()) || ((Config.NO_RATE_EQUIPMENT) && (_item.isEquipment())) || ((Config.NO_RATE_KEY_MATERIAL) && (_item.isKeyMatherial())) || ((Config.NO_RATE_RECIPES) && (_item.isRecipe())) || (ArrayUtils.contains(Config.NO_RATE_ITEMS, itemId)))
    {
      _notRate = true;
    }
  }

  public RewardData(int itemId, long min, long max, double chance) {
    this(itemId);
    _mindrop = min;
    _maxdrop = max;
    _chance = chance;
  }

  public boolean notRate()
  {
    return _notRate;
  }

  public void setNotRate(boolean notRate)
  {
    _notRate = notRate;
  }

  public int getItemId()
  {
    return _item.getItemId();
  }

  public ItemTemplate getItem()
  {
    return _item;
  }

  public long getMinDrop()
  {
    return _mindrop;
  }

  public long getMaxDrop()
  {
    return _maxdrop;
  }

  public double getChance()
  {
    return _chance;
  }

  public void setMinDrop(long mindrop)
  {
    _mindrop = mindrop;
  }

  public void setMaxDrop(long maxdrop)
  {
    _maxdrop = maxdrop;
  }

  public void setChance(double chance)
  {
    _chance = chance;
  }

  public void setChanceInGroup(double chance)
  {
    _chanceInGroup = chance;
  }

  public double getChanceInGroup()
  {
    return _chanceInGroup;
  }

  public String toString()
  {
    return "ItemID: " + getItem() + " Min: " + getMinDrop() + " Max: " + getMaxDrop() + " Chance: " + getChance() / 10000.0D + "%";
  }

  public RewardData clone()
  {
    return new RewardData(getItemId(), getMinDrop(), getMaxDrop(), getChance());
  }

  public boolean equals(Object o)
  {
    if ((o instanceof RewardData))
    {
      RewardData drop = (RewardData)o;
      return drop.getItemId() == getItemId();
    }
    return false;
  }

  public List<RewardItem> roll(Player player, double mod)
  {
    double rate = 1.0D;
    if (_item.isAdena())
      rate = Config.RATE_DROP_ADENA * player.getRateAdena();
    else {
      rate = Config.RATE_DROP_ITEMS * (player != null ? player.getRateItems() : 1.0D);
    }
    return roll(rate * mod);
  }

  public List<RewardItem> roll(double rate)
  {
    double mult = Math.ceil(rate);

    List ret = new ArrayList(1);
    RewardItem t = null;

    for (int n = 0; n < mult; n++)
    {
      if (Rnd.get(1000000) > _chance * Math.min(rate - n, 1.0D))
        continue;
      long count;
      long count;
      if (getMinDrop() >= getMaxDrop())
        count = getMinDrop();
      else {
        count = Rnd.get(getMinDrop(), getMaxDrop());
      }
      if (t == null)
      {
        ret.add(t = new RewardItem(_item.getItemId()));
        t.count = count;
      }
      else {
        t.count = SafeMath.addAndLimit(t.count, count);
      }
    }

    return ret;
  }
}