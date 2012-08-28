package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Skill;
import l2p.gameserver.stats.Env;

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