package handlers.effecthandlers;

import l2.hellknight.gameserver.model.effects.EffectTemplate;
import l2.hellknight.gameserver.model.effects.L2Effect;
import l2.hellknight.gameserver.model.effects.L2EffectType;
import l2.hellknight.gameserver.model.stats.Env;
import l2.hellknight.gameserver.network.serverpackets.StatusUpdate;

/**
 * @author Zoey76
 */
public class CpDamPercent extends L2Effect
{
	public CpDamPercent(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CPDAMPERCENT;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;
		
		double cp = getEffected().getCurrentCp() * (100 - getEffectPower()) / 100;
		getEffected().setCurrentCp(cp);
		
		StatusUpdate sucp = new StatusUpdate(getEffected());
		sucp.addAttribute(StatusUpdate.CUR_CP, (int) cp);
		getEffected().sendPacket(sucp);
		return false;
	}
}