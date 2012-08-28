package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;

final class EffectInvisible extends L2Effect {

    public EffectInvisible(Env env, EffectTemplate template)
    {
        super(env, template);
    }

	@Override
	public EffectType getEffectType()
    {
        return EffectType.INVISIBLE;
    }

	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			getEffected().startMuted();
			getEffected().startPsychicalMuted();
			((L2PcInstance)getEffected()).getAppearance().setInvisible();
			((L2PcInstance)getEffected()).setIsAttackDisable(true);
			return true;
		}
		return false;
	}

	@Override
	public void onExit()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			getEffected().stopMuted(this);
			getEffected().stopPsychicalMuted(this);
			((L2PcInstance)getEffected()).getAppearance().setVisible();
			((L2PcInstance)getEffected()).setIsAttackDisable(false);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
