package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.templates.StatsSet;

public class ManaHealPercent extends Skill
{
  private final boolean _ignoreMpEff;

  public ManaHealPercent(StatsSet set)
  {
    super(set);
    _ignoreMpEff = set.getBool("ignoreMpEff", true);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets) {
      if (target != null)
      {
        if ((target.isDead()) || (target.isHealBlocked())) {
          continue;
        }
        getEffects(activeChar, target, getActivateRate() > 0, false);

        double mp = _power * target.getMaxMp() / 100.0D;
        double newMp = mp * (!_ignoreMpEff ? target.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0D, activeChar, this) : 100.0D) / 100.0D;
        double addToMp = Math.max(0.0D, Math.min(newMp, target.calcStat(Stats.MP_LIMIT, null, null) * target.getMaxMp() / 100.0D - target.getCurrentMp()));

        if (addToMp > 0.0D)
          target.setCurrentMp(target.getCurrentMp() + addToMp);
        if (target.isPlayer())
          if (activeChar != target)
            target.sendPacket(new SystemMessage(1069).addString(activeChar.getName()).addNumber(Math.round(addToMp)));
          else
            activeChar.sendPacket(new SystemMessage(1068).addNumber(Math.round(addToMp)));
      }
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}