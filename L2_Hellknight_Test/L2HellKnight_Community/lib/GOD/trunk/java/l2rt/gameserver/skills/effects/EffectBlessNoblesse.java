package l2rt.gameserver.skills.effects;

import l2rt.config.ConfigSystem;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.skills.Env;

public final class EffectBlessNoblesse extends L2Effect
{
	public EffectBlessNoblesse(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().setIsBlessedByNoblesse(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if (ConfigSystem.getBoolean("DeleteNoblesseBlessing"))
			getEffected().setIsBlessedByNoblesse(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}