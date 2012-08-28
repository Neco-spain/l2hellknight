//L2DDT
package net.sf.l2j.gameserver.skills.effects;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.util.Rnd;

final class EffectConfusion extends L2Effect {

	public EffectConfusion(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.CONFUSION;
	}

	@Override
	public boolean onStart() {
		getEffected().startConfused();
		onActionTime();
		return true;
	}

	@Override
	public void onExit() {
		getEffected().stopConfused(this);
	}

    @Override
	public boolean onActionTime()
    {
    	if (Config.DEBUG)
    		System.out.println(getEffected());
		List<L2Character> targetList = new FastList<L2Character>();

        for (L2Object obj : getEffected().getKnownList().getKnownObjects().values())
        {
            if (obj == null)
                continue;

            if ((obj instanceof L2Character) && (obj != getEffected()))
                targetList.add((L2Character)obj);
        }
		if (targetList.size()==0){
			return true;
		}
		int nextTargetIdx = Rnd.nextInt(targetList.size());
		L2Object target = targetList.get(nextTargetIdx);

		getEffected().setTarget(target);
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK,target);


    	return true;
    }
}

