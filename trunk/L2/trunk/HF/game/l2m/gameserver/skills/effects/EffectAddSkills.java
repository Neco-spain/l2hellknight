package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Skill.AddedSkill;
import l2m.gameserver.skills.Env;

public class EffectAddSkills extends Effect
{
  public EffectAddSkills(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    for (Skill.AddedSkill as : getSkill().getAddedSkills())
      getEffected().addSkill(as.getSkill());
  }

  public void onExit()
  {
    super.onExit();
    for (Skill.AddedSkill as : getSkill().getAddedSkills())
      getEffected().removeSkill(as.getSkill());
  }

  public boolean onActionTime()
  {
    return false;
  }
}