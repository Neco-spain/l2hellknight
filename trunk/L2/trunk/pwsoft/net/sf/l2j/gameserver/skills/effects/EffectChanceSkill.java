package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Env;

final class EffectChanceSkill extends L2Effect
{
  public EffectChanceSkill(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.CHANCE_SKILL;
  }

  public void onStart()
  {
    L2Skill skill = SkillTable.getInstance().getInfo(getSkill().getChanceTriggeredId(), getSkill().getChanceTriggeredLevel());
    if (skill != null)
      getEffected().addChanceSkill(skill);
  }

  public void onExit()
  {
    if (getEffected().getChanceSkills() != null)
    {
      L2Skill skill = SkillTable.getInstance().getInfo(getSkill().getChanceTriggeredId(), getSkill().getChanceTriggeredLevel());
      getEffected().removeChanceSkill(skill);
    }
  }

  public boolean onActionTime()
  {
    return false;
  }
}