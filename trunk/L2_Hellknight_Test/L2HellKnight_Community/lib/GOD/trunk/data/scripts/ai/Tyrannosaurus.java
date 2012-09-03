package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2NpcInstance;

/**
 * АИ для мобов Tyrannosaurus
 *
 * @author SYS
 */
public class Tyrannosaurus extends Fighter
{
	public Tyrannosaurus(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
		{
			return;
		}
	}

	@Override
	protected void onEvtSeeSpell(L2Skill skill, L2Character caster)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || caster == null)
		{
			return;
		}
		int trexMaxHp = actor.getMaxHp();
		int skillId = skill.getId();
		int minhp = 0;
		int maxhp = 0;
		double trexCurrentHp = actor.getCurrentHp();
		switch(skillId)
		{
			case 3626:
				minhp = (60 * trexMaxHp) / 100;
				maxhp = (100 * trexMaxHp) / 100;
				break;
			case 3267:
				minhp = (25 * trexMaxHp) / 100;
				maxhp = (65 * trexMaxHp) / 100;
				break;
			case 3268:
				minhp = (0 * trexMaxHp) / 100;
				maxhp = (25 * trexMaxHp) / 100;
				break;
		}
		if(trexCurrentHp < minhp || trexCurrentHp > maxhp)
		{
			actor.getEffectList().stopEffect(skill);
			caster.sendMessage("The conditions are not right to use this skill now."); // TODO: retail msg
		}
	}
}