package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.stats.Env;

public class EffectNegateEffects extends Effect
{
  public EffectNegateEffects(Env env, EffectTemplate template)
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
      if (((!e.getStackType().equals(EffectTemplate.NO_STACK)) && ((e.getStackType().equals(getStackType())) || (e.getStackType().equals(getStackType2())))) || ((!e.getStackType2().equals(EffectTemplate.NO_STACK)) && ((e.getStackType2().equals(getStackType())) || (e.getStackType2().equals(getStackType2()))) && 
        (e.getStackOrder() <= getStackOrder())))
        e.exit();
    return false;
  }
}