package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.Skill;
import l2m.gameserver.skills.Env;

public class EffectNegateMusic extends Effect
{
  public EffectNegateMusic(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
  }

  public void onExit()
  {
    super.onExit();
  }

  public boolean onActionTime()
  {
    for (Effect e : _effected.getEffectList().getAllEffects())
      if (e.getSkill().isMusic())
        e.exit();
    return false;
  }
}