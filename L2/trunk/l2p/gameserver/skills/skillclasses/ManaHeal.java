package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.Config;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.templates.StatsSet;

public class ManaHeal extends Skill
{
  private final boolean _ignoreMpEff;

  public ManaHeal(StatsSet set)
  {
    super(set);
    _ignoreMpEff = set.getBool("ignoreMpEff", false);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    double mp = _power;

    int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
    if ((sps > 0) && (Config.MANAHEAL_SPS_BONUS)) {
      mp *= (sps == 2 ? 1.5D : 1.3D);
    }
    for (Creature target : targets)
    {
      if (target.isHealBlocked()) {
        continue;
      }
      double newMp = activeChar == target ? mp : Math.min(mp * 1.7D, mp * (!_ignoreMpEff ? target.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0D, activeChar, this) : 100.0D) / 100.0D);

      if ((getMagicLevel() > 0) && (activeChar != target))
      {
        int diff = target.getLevel() - getMagicLevel();
        if (diff > 5) {
          if (diff < 20)
            newMp = newMp / 100.0D * (100 - diff * 5);
          else
            newMp = 0.0D;
        }
      }
      if (newMp == 0.0D)
      {
        activeChar.sendPacket(new SystemMessage(1597).addSkillName(_id, getDisplayLevel()));
        getEffects(activeChar, target, getActivateRate() > 0, false);
        continue;
      }

      double addToMp = Math.max(0.0D, Math.min(newMp, target.calcStat(Stats.MP_LIMIT, null, null) * target.getMaxMp() / 100.0D - target.getCurrentMp()));

      if (addToMp > 0.0D)
        target.setCurrentMp(addToMp + target.getCurrentMp());
      if (target.isPlayer())
        if (activeChar != target)
          target.sendPacket(new SystemMessage(1069).addString(activeChar.getName()).addNumber(Math.round(addToMp)));
        else
          activeChar.sendPacket(new SystemMessage(1068).addNumber(Math.round(addToMp)));
      getEffects(activeChar, target, getActivateRate() > 0, false);
    }

    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}