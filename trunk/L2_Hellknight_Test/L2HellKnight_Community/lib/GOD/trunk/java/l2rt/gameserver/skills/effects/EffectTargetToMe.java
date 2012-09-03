package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.network.serverpackets.FlyToLocation;
import l2rt.gameserver.skills.Env;
import l2rt.util.Location;

public class EffectTargetToMe extends L2Effect
{
	public EffectTargetToMe(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Location flyLoc = _effected.getFlyLocation(getEffector(),getSkill());		
		_effected.setLoc(flyLoc);
		_effected.broadcastPacket(new FlyToLocation(_effected, flyLoc, getSkill().getFlyType()));
	}

	@Override
	public void onExit()
	{
		super.onExit();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}