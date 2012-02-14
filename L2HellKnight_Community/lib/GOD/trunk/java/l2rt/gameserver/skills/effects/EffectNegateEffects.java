package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.skills.Env;

public class EffectNegateEffects extends L2Effect
{
	public EffectNegateEffects(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onExit()
	{
		super.onExit();
	}

	@Override
	public boolean onActionTime()
	{
		for(L2Effect e : _effected.getEffectList().getAllEffects())
			if(!e.getStackType().equals(EffectTemplate.NO_STACK) && (e.getStackType().equals(getStackType()) || e.getStackType().equals(getStackType2())) || !e.getStackType2().equals(EffectTemplate.NO_STACK) && (e.getStackType2().equals(getStackType()) || e.getStackType2().equals(getStackType2())))
				if(e.getStackOrder() <= getStackOrder())
					e.exit();
		return false;
	}
}