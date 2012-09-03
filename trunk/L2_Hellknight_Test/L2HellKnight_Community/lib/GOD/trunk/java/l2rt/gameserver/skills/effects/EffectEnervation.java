package l2rt.gameserver.skills.effects;

import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.skills.Env;

public class EffectEnervation extends L2Effect
{
	public EffectEnervation(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.hasAI() && _effected.getAI() instanceof DefaultAI)
			((DefaultAI) _effected.getAI()).set("DebuffIntention", 0.5);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected.hasAI() && _effected.getAI() instanceof DefaultAI)
			((DefaultAI) _effected.getAI()).set("DebuffIntention", 1.);
	}
}