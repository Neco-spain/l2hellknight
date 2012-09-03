package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.skills.AbnormalEffect;
import l2rt.gameserver.skills.Env;

public final class EffectGrow extends L2Effect
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
			L2NpcInstance npc = (L2NpcInstance) _effected;
			npc.setCollisionHeight(npc.getCollisionHeight() * 1.24);
			npc.setCollisionRadius(npc.getCollisionRadius() * 1.19);

			npc.startAbnormalEffect(AbnormalEffect.GROW);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected.isNpc())
		{
			L2NpcInstance npc = (L2NpcInstance) _effected;
			npc.setCollisionHeight(npc.getTemplate().collisionHeight);
			npc.setCollisionRadius(npc.getTemplate().collisionRadius);

			npc.stopAbnormalEffect(AbnormalEffect.GROW);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}