package l2p.gameserver.skills.effects;

import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.SummonAI;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Summon;
import l2p.gameserver.stats.Env;

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