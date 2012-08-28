package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.skills.Env;

public class EffectUnAggro extends Effect
{
  public EffectUnAggro(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    if (_effected.isNpc())
      ((NpcInstance)_effected).setUnAggred(true);
  }

  public void onExit()
  {
    super.onExit();
    if (_effected.isNpc())
      ((NpcInstance)_effected).setUnAggred(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}