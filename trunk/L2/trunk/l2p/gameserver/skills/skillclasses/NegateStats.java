package l2p.gameserver.skills.skillclasses;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import l2p.commons.util.Rnd;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Skill;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.skills.effects.EffectTemplate;
import l2p.gameserver.stats.Formulas;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.stats.funcs.FuncTemplate;
import l2p.gameserver.templates.StatsSet;

public class NegateStats extends Skill
{
  private final List<Stats> _negateStats;
  private final boolean _negateOffensive;
  private final int _negateCount;

  public NegateStats(StatsSet set)
  {
    super(set);

    String[] negateStats = set.getString("negateStats", "").split(" ");
    _negateStats = new ArrayList(negateStats.length);
    for (String stat : negateStats) {
      if (!stat.isEmpty())
        _negateStats.add(Stats.valueOfXml(stat));
    }
    _negateOffensive = set.getBool("negateDebuffs", false);
    _negateCount = set.getInteger("negateCount", 0);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets) {
      if (target != null)
      {
        if ((!_negateOffensive) && (!Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate())))
        {
          activeChar.sendPacket(new SystemMessage(139).addString(target.getName()).addSkillName(getId(), getLevel()));
          continue;
        }

        int count = 0;
        List effects = target.getEffectList().getAllEffects();
        for (Iterator i$ = _negateStats.iterator(); i$.hasNext(); ) { stat = (Stats)i$.next();
          for (Effect e : effects)
          {
            Skill skill = e.getSkill();

            if ((!skill.isOffensive()) && (skill.getMagicLevel() > getMagicLevel()) && (Rnd.chance(skill.getMagicLevel() - getMagicLevel())))
            {
              count++;
              continue;
            }
            if ((skill.isOffensive() == _negateOffensive) && (containsStat(e, stat)) && (skill.isCancelable()))
            {
              target.sendPacket(new SystemMessage(749).addSkillName(e.getSkill().getId(), e.getSkill().getDisplayLevel()));
              e.exit();
              count++;
            }
            if ((_negateCount > 0) && (count >= _negateCount))
              break;
          }
        }
        Stats stat;
        getEffects(activeChar, target, getActivateRate() > 0, false);
      }
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }

  private boolean containsStat(Effect e, Stats stat)
  {
    for (FuncTemplate ft : e.getTemplate().getAttachedFuncs())
      if (ft._stat == stat)
        return true;
    return false;
  }

  public boolean isOffensive()
  {
    return !_negateOffensive;
  }

  public List<Stats> getNegateStats()
  {
    return _negateStats;
  }
}