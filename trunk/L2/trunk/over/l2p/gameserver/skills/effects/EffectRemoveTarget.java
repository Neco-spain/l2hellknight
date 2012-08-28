package l2p.gameserver.skills.effects;

import l2p.commons.util.Rnd;
import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;
import l2p.gameserver.templates.StatsSet;

public final class EffectRemoveTarget extends Effect
{
  private boolean _doStopTarget;

  public EffectRemoveTarget(Env env, EffectTemplate template)
  {
    super(env, template);
    _doStopTarget = template.getParam().getBool("doStopTarget", false);
  }

  public boolean checkCondition()
  {
    return Rnd.chance(_template.chance(100));
  }

  public void onStart()
  {
    if ((getEffected().getAI() instanceof DefaultAI)) {
      ((DefaultAI)getEffected().getAI()).setGlobalAggro(System.currentTimeMillis() + 3000L);
    }
    getEffected().setTarget(null);
    if (_doStopTarget)
      getEffected().stopMove();
    getEffected().abortAttack(true, true);
    getEffected().abortCast(true, true);
    getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, getEffector());
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