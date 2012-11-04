package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.stats.Env;

public final class EffectGrow extends Effect
{
	public EffectGrow(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isNpc())
		{
			NpcInstance npc = (NpcInstance) _effected;
			npc.setCollisionHeight(npc.getCollisionHeight() * 1.24);
			npc.setCollisionRadius(npc.getCollisionRadius() * 1.19);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected.isNpc())
		{
			NpcInstance npc = (NpcInstance) _effected;
			npc.setCollisionHeight(npc.getTemplate().collisionHeight);
			npc.setCollisionRadius(npc.getTemplate().collisionRadius);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}