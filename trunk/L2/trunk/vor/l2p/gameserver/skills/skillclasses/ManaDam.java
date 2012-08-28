package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.commons.util.Rnd;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.stats.Formulas;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.templates.StatsSet;

public class ManaDam extends Skill
{
  public ManaDam(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    int sps = 0;
    if (isSSPossible()) {
      sps = activeChar.getChargedSpiritShot();
    }
    for (Creature target : targets) {
      if (target != null)
      {
        if (target.isDead()) {
          continue;
        }
        int magicLevel = getMagicLevel() == 0 ? activeChar.getLevel() : getMagicLevel();
        int landRate = Rnd.get(30, 100);
        landRate *= target.getLevel();
        landRate /= magicLevel;

        if (Rnd.chance(landRate))
        {
          double mAtk = activeChar.getMAtk(target, this);
          if (sps == 2)
            mAtk *= 4.0D;
          else if (sps == 1) {
            mAtk *= 2.0D;
          }
          double mDef = target.getMDef(activeChar, this);
          if (mDef < 1.0D) {
            mDef = 1.0D;
          }
          double damage = Math.sqrt(mAtk) * getPower() * (target.getMaxMp() / 97) / mDef;

          boolean crit = Formulas.calcMCrit(activeChar.getMagicCriticalRate(target, this));
          if (crit)
          {
            activeChar.sendPacket(Msg.MAGIC_CRITICAL_HIT);
            damage *= activeChar.calcStat(Stats.MCRITICAL_DAMAGE, (activeChar.isPlayable()) && (target.isPlayable()) ? 2.5D : 3.0D, target, this);
          }
          target.reduceCurrentMp(damage, activeChar);
        }
        else
        {
          SystemMessage msg = new SystemMessage(2269).addName(target).addName(activeChar);
          activeChar.sendPacket(msg);
          target.sendPacket(msg);
          target.reduceCurrentHp(1.0D, activeChar, this, true, true, false, true, false, false, true);
        }

        getEffects(activeChar, target, getActivateRate() > 0, false);
      }
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}