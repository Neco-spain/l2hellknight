package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

/**
 * Target is immune to death.
 *
 * @author Yorie
 */
public final class EffectDeathImmunity extends Effect {
    public EffectDeathImmunity(Env env, EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean checkCondition() {
        return super.checkCondition();
    }

    @Override
    public void onStart() {
        super.onStart();
        getEffected().addDeathImmunity();
    }

    @Override
    public void onExit() {
        getEffected().removeDeathImmunity();
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}