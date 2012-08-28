package l2p.gameserver.model.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2p.commons.math.SafeMath;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.templates.item.ItemTemplate;

public class RewardGroup
  implements Cloneable
{
  private double _chance;
  private boolean _isAdena = false;
  private boolean _notRate = false;
  private List<RewardData> _items = new ArrayList();
  private double _chanceSum;

  public RewardGroup(double chance)
  {
    setChance(chance);
  }

  public boolean notRate()
  {
    return _notRate;
  }

  public void setNotRate(boolean notRate)
  {
    _notRate = notRate;
  }

  public double getChance()
  {
    return _chance;
  }

  public void setChance(double chance)
  {
    _chance = chance;
  }

  public boolean isAdena()
  {
    return _isAdena;
  }

  public void setIsAdena(boolean isAdena)
  {
    _isAdena = isAdena;
  }

  public void addData(RewardData item)
  {
    if (item.getItem().isAdena())
      _isAdena = true;
    _chanceSum += item.getChance();
    item.setChanceInGroup(_chanceSum);
    _items.add(item);
  }

  public List<RewardData> getItems()
  {
    return _items;
  }

  public RewardGroup clone()
  {
    RewardGroup ret = new RewardGroup(_chance);
    for (RewardData i : _items)
      ret.addData(i.clone());
    return ret;
  }

  public List<RewardItem> roll(RewardType type, Player player, double mod, boolean isRaid, boolean isSiegeGuard)
  {
    switch (1.$SwitchMap$l2p$gameserver$model$reward$RewardType[type.ordinal()])
    {
    case 1:
    case 2:
      return rollItems(mod, 1.0D, 1.0D);
    case 3:
      return rollItems(mod, Config.RATE_DROP_SPOIL, player.getRateSpoil());
    case 4:
      if (_isAdena) {
        return rollAdena(mod, Config.RATE_DROP_ADENA, player.getRateAdena());
      }
      if (isRaid) {
        return rollItems(mod, Config.RATE_DROP_RAIDBOSS, 1.0D);
      }
      if (isSiegeGuard) {
        return rollItems(mod, Config.RATE_DROP_SIEGE_GUARD, 1.0D);
      }
      return rollItems(mod, Config.RATE_DROP_ITEMS, player.getRateItems());
    }
    return Collections.emptyList();
  }

  public List<RewardItem> rollItems(double mod, double baseRate, double playerRate)
  {
    if (mod <= 0.0D)
      return Collections.emptyList();
    double rate;
    double rate;
    if (_notRate)
      rate = Math.min(mod, 1.0D);
    else {
      rate = baseRate * playerRate * mod;
    }
    double mult = Math.ceil(rate);

    List ret = new ArrayList((int)(mult * _items.size()));
    for (long n = 0L; n < mult; n += 1L)
      if (Rnd.get(1, 1000000) <= _chance * Math.min(rate - n, 1.0D))
        rollFinal(_items, ret, 1.0D, Math.max(_chanceSum, 1000000.0D));
    return ret;
  }

  private List<RewardItem> rollAdena(double mod, double baseRate, double playerRate)
  {
    double chance = _chance;
    if (mod > 10.0D)
    {
      mod *= _chance / 1000000.0D;
      chance = 1000000.0D;
    }

    if (mod <= 0.0D) {
      return Collections.emptyList();
    }
    if (Rnd.get(1, 1000000) > chance) {
      return Collections.emptyList();
    }
    double rate = baseRate * playerRate * mod;

    List ret = new ArrayList(_items.size());
    rollFinal(_items, ret, rate, Math.max(_chanceSum, 1000000.0D));
    for (RewardItem i : ret) {
      i.isAdena = true;
    }
    return ret;
  }

  private void rollFinal(List<RewardData> items, List<RewardItem> ret, double mult, double chanceSum)
  {
    int chance = Rnd.get(0, (int)chanceSum);

    for (RewardData i : items)
    {
      if ((chance < i.getChanceInGroup()) && (chance > i.getChanceInGroup() - i.getChance()))
      {
        double imult = i.notRate() ? 1.0D : mult;
        long count;
        long count;
        if (i.getMinDrop() >= i.getMaxDrop())
          count = Math.round(i.getMinDrop() * imult);
        else {
          count = Rnd.get(Math.round(i.getMinDrop() * imult), Math.round(i.getMaxDrop() * imult));
        }
        RewardItem t = null;

        for (RewardItem r : ret) {
          if (i.getItemId() == r.itemId)
          {
            t = r;
            break;
          }
        }
        if (t == null)
        {
          ret.add(t = new RewardItem(i.getItemId()));
          t.count = count; break;
        }
        if (i.notRate())
          break;
        t.count = SafeMath.addAndLimit(t.count, count); break;
      }
    }
  }
}