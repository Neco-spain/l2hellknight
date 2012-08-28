package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Skill.AddedSkill;
import l2p.gameserver.stats.Env;

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