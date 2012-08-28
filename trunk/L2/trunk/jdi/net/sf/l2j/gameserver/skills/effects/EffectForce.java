package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Env;

public final class EffectForce extends L2Effect
{
  public int forces;

  public EffectForce(Env env, EffectTemplate template)
  {
    super(env, template);
    forces = getSkill().getLevel();
  }

  public boolean onActionTime()
  {
    return true;
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.BUFF;
  }

  public void increaseForce()
  {
    if (forces < 3)
    {
      forces += 1;
      updateBuff();
    }
  }

  public void decreaseForce()
  {
    forces -= 1;
    if (forces < 1)
      exit();
    else
      updateBuff();
  }

  private void updateBuff()
  {
    exit();
    SkillTable.getInstance().getInfo(getSkill().getId(), forces).getEffects(getEffector(), getEffected());
  }
}