package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.skills.Env;

public class EffectEnervation extends Effect
{
  public EffectEnervation(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    if (_effected.isNpc())
      ((NpcInstance)_effected).setParameter("DebuffIntention", Double.valueOf(0.5D));
  }

  public boolean onActionTime()
  {
    return false;
  }

  public void onExit()
  {
    super.onExit();
    if (_effected.isNpc())
      ((NpcInstance)_effected).setParameter("DebuffIntention", Double.valueOf(1.0D));
  }
}