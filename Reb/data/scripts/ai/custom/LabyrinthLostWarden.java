package ai.custom;

import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.model.instances.ReflectionBossInstance;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.stats.funcs.FuncSet;

public class LabyrinthLostWarden extends Fighter
{

	public LabyrinthLostWarden(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		Reflection r = actor.getReflection();
		if(!r.isDefault())
			if(checkMates(actor.getNpcId()))
				if(findLostCaptain() != null)
					findLostCaptain().addStatFunc(new FuncSet(Stats.POWER_ATTACK, 0x30, this, findLostCaptain().getTemplate().basePAtk * 0.66));
		super.onEvtDead(killer);
	}

	private boolean checkMates(int id)
	{
		for(NpcInstance n : getActor().getReflection().getNpcs())
			if(n.getNpcId() == id && !n.isDead())
				return false;
		return true;
	}

	private NpcInstance findLostCaptain()
	{
		for(NpcInstance n : getActor().getReflection().getNpcs())
			if(n instanceof ReflectionBossInstance)
				return n;
		return null;
	}
}