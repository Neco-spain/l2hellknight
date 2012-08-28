package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Summon;
import l2p.gameserver.stats.Env;

public final class EffectDestroySummon extends Effect
{
  public EffectDestroySummon(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean checkCondition()
  {
    if (!_effected.isSummon())
      return false;
    return super.checkCondition();
  }

  public void onStart()
  {
    super.onStart();
    ((Summon)_effected).unSummon();
  }

  public boolean onActionTime()
  {
    return false;
  }
}