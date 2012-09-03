package ai;

import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Rnd;

public class UnstableSeed extends DefaultAI
{
	private int count = 4000;

	public UnstableSeed(L2Character actor)
	{
		super(actor);
		AI_TASK_DELAY = 60000;
		AI_TASK_ACTIVE_DELAY = 60000;
	}

	@Override
	protected void onEvtThink()
	{
		L2NpcInstance npc = getActor();
		if(npc == null || npc.isDead())
			return;
		if(Rnd.chance(30))
			ItemTemplates.getInstance().createItem(13797).dropToTheGround(npc, npc.getLoc().rnd(100, 150, false));
		super.onEvtThink();
	}

	@Override
	protected void onEvtSeeSpell(L2Skill skill, L2Character caster)
	{
		L2NpcInstance npc = getActor();
		if(npc == null || skill == null || caster == null || caster.isPlayable() || npc.isDead())
			return;
		if(skill.getId() == 5909 || skill.getId() == 5910)
		{
			count--;
			if(count <= 0)
			{
				npc.broadcastPacket(new MagicSkillUse(npc, npc, 6037, 1, 500, 0));
				npc.doDie(caster);
				npc.deleteMe();
			}
		}
		super.onEvtSeeSpell(skill, caster);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
}