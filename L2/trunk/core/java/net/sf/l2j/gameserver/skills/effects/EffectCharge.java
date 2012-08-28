//L2DDT
package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;


public class EffectCharge extends L2Effect
{
	public int numCharges;

	public EffectCharge(Env env, EffectTemplate template)
	{
		super(env, template);
		numCharges = 1;
		if (env.target instanceof L2PcInstance)
		{
			env.target.sendPacket(new EtcStatusUpdate((L2PcInstance)env.target));
			SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
			sm.addNumber(numCharges);
			getEffected().sendPacket(sm);
		}
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.CHARGE;
	}

	@Override
	public boolean onActionTime()
    {
    	return false;
    }

	@Override
	public int getLevel() { return numCharges; }

	public void addNumCharges(int i) { numCharges = numCharges + i; }
}
