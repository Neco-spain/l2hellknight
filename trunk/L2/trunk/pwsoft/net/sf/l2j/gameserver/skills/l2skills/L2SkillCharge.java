package net.sf.l2j.gameserver.skills.l2skills;

import javolution.util.FastList;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillCharge extends L2Skill
{
  public L2SkillCharge(StatsSet set)
  {
    super(set);
  }

  public void useSkill(L2Character caster, FastList<L2Object> targets)
  {
    if (caster.isAlikeDead()) {
      return;
    }

    if (caster.getCharges() < getLevel())
      caster.increaseCharges();
    else
      caster.sendPacket(Static.FORCE_MAXLEVEL_REACHED);
  }
}