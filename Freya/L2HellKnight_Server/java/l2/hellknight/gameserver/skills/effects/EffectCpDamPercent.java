package l2.hellknight.gameserver.skills.effects;

import l2.hellknight.gameserver.model.L2Effect;
import l2.hellknight.gameserver.network.serverpackets.StatusUpdate;
import l2.hellknight.gameserver.skills.Env;
import l2.hellknight.gameserver.templates.EffectTemplate;
import l2.hellknight.gameserver.templates.L2EffectType;

/**
 * @author Zoey76
 */
public class EffectCpDamPercent extends L2Effect
{
	public EffectCpDamPercent(Env env, EffectTemplate template)
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