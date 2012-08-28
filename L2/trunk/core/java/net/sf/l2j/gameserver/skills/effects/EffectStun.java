//L2DDT
package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

final class EffectStun extends L2Effect {

	public EffectStun(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.STUN;
	}

	@Override
	public boolean onStart() {
		getEffected().startStunning();
		return true;
	}

	@Override
	public void onExit() {
		getEffected().stopStunning(this);
	}

    @Override
	public boolean onActionTime()
    {
    	return false;
    }
}

