package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Skill.SkillType;
import l2m.gameserver.skills.Env;

public final class EffectInvulnerable extends Effect
{
  public EffectInvulnerable(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean checkCondition()
  {
    if (_effected.isInvul())
      return false;
    Skill skill = _effected.getCastingSkill();
    if ((skill != null) && ((skill.getSkillType() == Skill.SkillType.TAKECASTLE) || (skill.getSkillType() == Skill.SkillType.TAKEFORTRESS) || (skill.getSkillType() == Skill.SkillType.TAKEFLAG)))
      return false;
    return super.checkCondition();
  }

  public void onStart()
  {
    super.onStart();
    _effected.startHealBlocked();
    _effected.setIsInvul(true);
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopHealBlocked();
    _effected.setIsInvul(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}