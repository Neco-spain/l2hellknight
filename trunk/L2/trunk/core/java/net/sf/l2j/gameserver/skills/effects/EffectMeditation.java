package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

final class EffectMeditation extends L2Effect {

	public EffectMeditation(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.MEDITATION;
	}

	@Override
	public boolean onStart() {
		getEffected().startMeditation();
		return true;
	}

	@Override
	public void onExit() {
		getEffected().stopMeditation(this);
	}

    @Override
	public boolean onActionTime()
    {
    	getEffected().stopMeditation(this);
    	return false;
    }
}

