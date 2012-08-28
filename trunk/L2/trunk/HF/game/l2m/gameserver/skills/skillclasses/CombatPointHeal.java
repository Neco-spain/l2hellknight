package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.templates.StatsSet;

public class CombatPointHeal extends Skill
{
  private final boolean _ignoreCpEff;

  public CombatPointHeal(StatsSet set)
  {
    super(set);
    _ignoreCpEff = set.getBool("ignoreCpEff", false);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets)
      if (target != null)
      {
        if ((target.isDead()) || (target.isHealBlocked()))
          continue;
        double maxNewCp = _power * (!_ignoreCpEff ? target.calcStat(Stats.CPHEAL_EFFECTIVNESS, 100.0D, activeChar, this) : 100.0D) / 100.0D;
        double addToCp = Math.max(0.0D, Math.min(maxNewCp, target.calcStat(Stats.CP_LIMIT, null, null) * target.getMaxCp() / 100.0D - target.getCurrentCp()));
        if (addToCp > 0.0D)
          target.setCurrentCp(addToCp + target.getCurrentCp());
        target.sendPacket(new SystemMessage(1405).addNumber(()addToCp));
        getEffects(activeChar, target, getActivateRate() > 0, false);
      }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}