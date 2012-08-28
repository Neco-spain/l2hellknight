package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectPsychicalMute extends L2Effect {


    public EffectPsychicalMute(Env env, EffectTemplate template) {
        super(env, template);
    }


    @Override
	public EffectType getEffectType() {
        return L2Effect.EffectType.PSYCHICAL_MUTE;
    }

    @Override
	public boolean onStart() 
	{
        getEffected().startPsychicalMuted();
		return true;
    }

    @Override
	public boolean onActionTime() {
        // Simply stop the effect
        getEffected().stopPsychicalMuted(this);
        return false;
    }

    @Override
	public void onExit() {
        getEffected().stopPsychicalMuted(this);
    }
}
