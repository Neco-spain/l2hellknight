package l2p.gameserver.skills.effects;

import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

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