package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.templates.StatsSet;

public class HealPercent extends Skill
{
  private final boolean _ignoreHpEff;

  public HealPercent(StatsSet set)
  {
    super(set);
    _ignoreHpEff = set.getBool("ignoreHpEff", true);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if ((activeChar.isPlayable()) && (target.isMonster()))
      return false;
    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets) {
      if (target != null)
      {
        if (target.isHealBlocked()) {
          continue;
        }
        getEffects(activeChar, target, getActivateRate() > 0, false);

        double hp = _power * target.getMaxHp() / 100.0D;
        double newHp = hp * (!_ignoreHpEff ? target.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0D, activeChar, this) : 100.0D) / 100.0D;
        double addToHp = Math.max(0.0D, Math.min(newHp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100.0D - target.getCurrentHp()));

        if (addToHp > 0.0D)
          target.setCurrentHp(addToHp + target.getCurrentHp(), false);
        if (target.isPlayer())
          if (activeChar != target)
            target.sendPacket(new SystemMessage(1067).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
          else
            activeChar.sendPacket(new SystemMessage(1066).addNumber(Math.round(addToHp)));
      }
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}