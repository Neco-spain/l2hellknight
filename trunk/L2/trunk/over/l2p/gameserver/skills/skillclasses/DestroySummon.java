package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Summon;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.stats.Formulas;
import l2p.gameserver.templates.StatsSet;

public class DestroySummon extends Skill
{
  public DestroySummon(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets) {
      if (target != null)
      {
        if ((getActivateRate() > 0) && (!Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate())))
        {
          activeChar.sendPacket(new SystemMessage(139).addString(target.getName()).addSkillName(getId(), getLevel()));
          continue;
        }

        if (target.isSummon())
        {
          ((Summon)target).saveEffects();
          ((Summon)target).unSummon();
          getEffects(activeChar, target, getActivateRate() > 0, false);
        }
      }
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}