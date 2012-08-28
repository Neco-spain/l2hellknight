package l2p.gameserver.model.reward;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import l2p.gameserver.model.Player;

public class RewardList extends ArrayList<RewardGroup>
{
  public static final int MAX_CHANCE = 1000000;
  private final RewardType _type;
  private final boolean _autoLoot;

  public RewardList(RewardType rewardType, boolean a)
  {
    super(5);
    _type = rewardType;
    _autoLoot = a;
  }

  public List<RewardItem> roll(Player player)
  {
    return roll(player, 1.0D, false, false);
  }

  public List<RewardItem> roll(Player player, double mod)
  {
    return roll(player, mod, false, false);
  }

  public List<RewardItem> roll(Player player, double mod, boolean isRaid)
  {
    return roll(player, mod, isRaid, false);
  }

  public List<RewardItem> roll(Player player, double mod, boolean isRaid, boolean isSiegeGuard)
  {
    List temp = new ArrayList(size());
    for (RewardGroup g : this)
    {
      List tdl = g.roll(_type, player, mod, isRaid, isSiegeGuard);
      if (!tdl.isEmpty())
        for (RewardItem itd : tdl)
          temp.add(itd);
    }
    return temp;
  }

  public boolean validate()
  {
    for (Iterator i$ = iterator(); i$.hasNext(); ) { g = (RewardGroup)i$.next();

      int chanceSum = 0;
      for (RewardData d : g.getItems())
        chanceSum = (int)(chanceSum + d.getChance());
      if (chanceSum <= 1000000)
        return true;
      mod = 1000000 / chanceSum;
      for (RewardData d : g.getItems())
      {
        double chance = d.getChance() * mod;
        d.setChance(chance);
        g.setChance(1000000.0D);
      }
    }
    RewardGroup g;
    double mod;
    return false;
  }

  public boolean isAutoLoot()
  {
    return _autoLoot;
  }

  public RewardType getType()
  {
    return _type;
  }
}