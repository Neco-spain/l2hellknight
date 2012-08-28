package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public final class EffectBuff extends Effect
{
  public EffectBuff(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean onActionTime()
  {
    return false;
  }
}