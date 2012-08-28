package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.Rnd;

public final class ChanceCondition
{
  public static final int EVT_HIT = 1;
  public static final int EVT_CRIT = 2;
  public static final int EVT_CAST = 4;
  public static final int EVT_PHYSICAL = 8;
  public static final int EVT_MAGIC = 16;
  public static final int EVT_MAGIC_GOOD = 32;
  public static final int EVT_MAGIC_OFFENSIVE = 64;
  public static final int EVT_ATTACKED = 128;
  public static final int EVT_ATTACKED_HIT = 256;
  public static final int EVT_ATTACKED_CRIT = 512;
  public static final int EVT_HIT_BY_SKILL = 1024;
  public static final int EVT_HIT_BY_OFFENSIVE_SKILL = 2048;
  public static final int EVT_HIT_BY_GOOD_MAGIC = 4096;
  private TriggerType _triggerType;
  private int _chance;

  private ChanceCondition(TriggerType trigger, int chance)
  {
    _triggerType = trigger;
    _chance = chance;
  }

  public static ChanceCondition parse(StatsSet set)
  {
    try
    {
      TriggerType trigger = (TriggerType)set.getEnum("chanceType", TriggerType.class, null);
      int chance = set.getInteger("activationChance", 0);
      if ((trigger != null) && (chance > 0))
        return new ChanceCondition(trigger, chance);
    }
    catch (Exception e) {
    }
    return null;
  }

  public boolean trigger(int event)
  {
    return (_triggerType.check(event)) && (Rnd.get(100) < _chance);
  }

  public String toString()
  {
    return "Trigger[" + _chance + ";" + _triggerType.toString() + "]";
  }

  public static enum TriggerType
  {
    ON_HIT(1), 

    ON_CRIT(2), 

    ON_CAST(4), 

    ON_PHYSICAL(8), 

    ON_MAGIC(16), 

    ON_MAGIC_GOOD(32), 

    ON_MAGIC_OFFENSIVE(64), 

    ON_ATTACKED(128), 

    ON_ATTACKED_HIT(256), 

    ON_ATTACKED_CRIT(512), 

    ON_HIT_BY_SKILL(1024), 

    ON_HIT_BY_OFFENSIVE_SKILL(2048), 

    ON_HIT_BY_GOOD_MAGIC(4096);

    private int _mask;

    private TriggerType(int mask) {
      _mask = mask;
    }

    public boolean check(int event)
    {
      return (_mask & event) != 0;
    }
  }
}