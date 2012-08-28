package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.skillclasses.NegateStats;
import l2m.gameserver.skills.Env;

public class EffectBlockStat extends Effect
{
  public EffectBlockStat(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    _effected.addBlockStats(((NegateStats)_skill).getNegateStats());
  }

  public void onExit()
  {
    super.onExit();
    _effected.removeBlockStats(((NegateStats)_skill).getNegateStats());
  }

  public boolean onActionTime()
  {
    return false;
  }
}