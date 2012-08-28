package net.sf.l2j.gameserver.skills.effects;

import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.util.Rnd;

final class EffectConfuseMob extends L2Effect
{
  public EffectConfuseMob(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.CONFUSE_MOB_ONLY;
  }

  public void onStart()
  {
    getEffected().startConfused();
    onActionTime();
  }

  public void onExit()
  {
    getEffected().stopConfused(this);
  }

  public boolean onActionTime()
  {
    List targetList = new FastList();

    for (L2Object obj : getEffected().getKnownList().getKnownObjects().values())
    {
      if (obj == null) {
        continue;
      }
      if ((obj.isL2Attackable()) && (obj != getEffected())) {
        targetList.add((L2Character)obj);
      }
    }
    if (targetList.size() == 0) {
      return true;
    }

    int nextTargetIdx = Rnd.nextInt(targetList.size());
    L2Object target = (L2Object)targetList.get(nextTargetIdx);

    getEffected().setTarget(target);
    getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
    targetList.clear();
    targetList = null;

    return true;
  }
}