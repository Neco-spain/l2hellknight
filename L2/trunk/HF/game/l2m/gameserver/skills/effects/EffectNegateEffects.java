package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.skills.Env;

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