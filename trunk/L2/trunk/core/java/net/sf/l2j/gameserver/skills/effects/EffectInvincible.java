package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectInvincible extends L2Effect
{
	public EffectInvincible(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return L2Effect.EffectType.INVINCIBLE;
	}

	@Override
	public boolean onStart() {
		getEffected().setIsInvul(true);
		return true;
	}

	@Override
	public boolean onActionTime()
	{
		getEffected().setIsInvul(false);
		return false;
	}


	@Override
	public void onExit()
	{
		getEffected().setIsInvul(false);
	}
}
