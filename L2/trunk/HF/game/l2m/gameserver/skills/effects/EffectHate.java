package l2m.gameserver.skills.effects;

import l2m.gameserver.ai.CharacterAI;
import l2m.gameserver.ai.CtrlEvent;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;

public class EffectHate extends Effect
{
  public EffectHate(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    if ((_effected.isNpc()) && (_effected.isMonster()))
      _effected.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _effector, Double.valueOf(_template._value));
  }

  public boolean isHidden()
  {
    return true;
  }

  public boolean onActionTime()
  {
    return false;
  }
}