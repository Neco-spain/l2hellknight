package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.skills.Env;

final class EffectPhoenixBless extends L2Effect
{
    public EffectPhoenixBless(Env env, EffectTemplate template)
    {
        super(env, template);
    }

    @Override
    public EffectType getEffectType()
    {
        return EffectType.PHOENIX_BLESSING;
    }

    @Override
    public boolean onStart()
    {
        if (getEffected() instanceof L2PlayableInstance)
		{
            ((L2PlayableInstance)getEffected()).startPhoenixBlessing();
			return true;
		}
		return false;
    }

    @Override
    public void onExit()
    {
        if (getEffected() instanceof L2PlayableInstance)
            ((L2PlayableInstance)getEffected()).stopPhoenixBlessing(this);
    }

    @Override
    public boolean onActionTime()
    {
        return false;
    }
}