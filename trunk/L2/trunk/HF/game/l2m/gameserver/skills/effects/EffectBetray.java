package l2m.gameserver.skills.effects;

import l2m.gameserver.ai.CtrlIntention;
import l2m.gameserver.ai.SummonAI;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Summon;
import l2m.gameserver.skills.Env;

public class EffectBetray extends Effect
{
  public EffectBetray(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    if ((_effected != null) && (_effected.isSummon()))
    {
      Summon summon = (Summon)_effected;
      summon.setDepressed(true);
      summon.getAI().Attack(summon.getPlayer(), true, false);
    }
  }

  public void onExit()
  {
    super.onExit();
    if ((_effected != null) && (_effected.isSummon()))
    {
      Summon summon = (Summon)_effected;
      summon.setDepressed(false);
      summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }
  }

  public boolean onActionTime()
  {
    return false;
  }
}