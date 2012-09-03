package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2WorldRegion;
import l2rt.gameserver.skills.Env;

public final class EffectInvisible extends L2Effect
{
	public EffectInvisible(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(!_effected.isPlayer())
			return false;
		L2Player player = (L2Player) _effected;
		if(player.isInvisible())
			return false;
		if(player.isCombatFlagEquipped() || player.isTerritoryFlagEquipped())
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		L2Player player = (L2Player) _effected;
		player.setInvisible(true);
		player.sendUserInfo(true);
		if(player.getCurrentRegion() != null)
			for(L2WorldRegion neighbor : player.getCurrentRegion().getNeighbors())
				neighbor.removePlayerFromOtherPlayers(player);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		L2Player player = (L2Player) _effected;
		if(!player.isInvisible())
			return;
		player.setInvisible(false);
		player.broadcastUserInfo(true);
		if(player.getPet() != null)
			player.getPet().broadcastPetInfo();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}