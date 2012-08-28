package l2p.gameserver.skills.skillclasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.stats.conditions.ConditionTargetRelation;
import l2p.gameserver.stats.conditions.ConditionTargetRelation.Relation;
import l2p.gameserver.templates.StatsSet;

public class ChainHeal extends Skill
{
  private final int[] _healPercents;
  private final int _healRadius;
  private final int _maxTargets;

  public ChainHeal(StatsSet set)
  {
    super(set);
    _healRadius = set.getInteger("healRadius", 350);
    String[] params = set.getString("healPercents", "").split(";");
    _maxTargets = params.length;
    _healPercents = new int[params.length];
    for (int i = 0; i < params.length; i++)
      _healPercents[i] = Integer.parseInt(params[i]);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    int curTarget = 0;
    for (Creature target : targets)
    {
      if (target == null) {
        continue;
      }
      getEffects(activeChar, target, getActivateRate() > 0, false);

      double hp = _healPercents[curTarget] * target.getMaxHp() / 100.0D;
      double addToHp = Math.max(0.0D, Math.min(hp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100.0D - target.getCurrentHp()));

      if (addToHp > 0.0D) {
        target.setCurrentHp(addToHp + target.getCurrentHp(), false);
      }
      if (target.isPlayer()) {
        if (activeChar != target)
          target.sendPacket(new SystemMessage(1067).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
        else
          activeChar.sendPacket(new SystemMessage(1066).addNumber(Math.round(addToHp)));
      }
      curTarget++;
    }

    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }

  public List<Creature> getTargets(Creature activeChar, Creature aimingTarget, boolean forceUse)
  {
    List result = new ArrayList();
    List targets = aimingTarget.getAroundCharacters(_healRadius, 128);
    if ((targets == null) || (targets.isEmpty())) {
      return result;
    }
    List healTargets = new ArrayList();
    healTargets.add(new HealTarget(-100.0D, aimingTarget));
    for (Creature target : targets)
    {
      if ((target == null) || (target.isHealBlocked()) || (target.isCursedWeaponEquipped()) || (target.isCurrentHpFull()) || (ConditionTargetRelation.getRelation(activeChar, target) != ConditionTargetRelation.Relation.Friend) || 
        (target.isAutoAttackable(activeChar))) {
        continue;
      }
      double hpPercent = target.getCurrentHp() / target.getMaxHp();
      healTargets.add(new HealTarget(hpPercent, target));
    }

    HealTarget[] healTargetsArr = new HealTarget[healTargets.size()];
    healTargets.toArray(healTargetsArr);
    Arrays.sort(healTargetsArr, new Comparator()
    {
      public int compare(ChainHeal.HealTarget o1, ChainHeal.HealTarget o2)
      {
        if ((o1 == null) || (o2 == null))
          return 0;
        if (o1.getHpPercent() < o2.getHpPercent())
          return -1;
        if (o1.getHpPercent() > o2.getHpPercent())
          return 1;
        return 0;
      }
    });
    int targetsCount = 0;
    for (HealTarget ht : healTargetsArr)
    {
      result.add(ht.getTarget());
      targetsCount++;
      if (targetsCount >= _maxTargets)
        break;
    }
    return result;
  }

  private static class HealTarget {
    private final double hpPercent;
    private final Creature target;

    public HealTarget(double hpPercent, Creature target) {
      this.hpPercent = hpPercent;
      this.target = target;
    }

    public double getHpPercent()
    {
      return hpPercent;
    }

    public Creature getTarget()
    {
      return target;
    }
  }
}