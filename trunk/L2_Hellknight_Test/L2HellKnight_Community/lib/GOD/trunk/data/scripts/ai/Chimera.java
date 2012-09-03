package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

public class Chimera extends Fighter
{
	public Chimera(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSeeSpell(L2Skill skill, L2Character caster)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.getCurrentHpPercents() > 10) // 10% ХП для использования бутылки
			return;
		if(skill.getId() == 2359)
		{
			// 10% шанс получения Life Force
			L2Player killer = null;
			L2Character MostHated = actor.getMostHated();
			if(MostHated != null && MostHated instanceof L2Playable)
				killer = MostHated.getPlayer();
			actor.dropItem(killer, Rnd.chance(15) ? 9681 : actor.getNpcId() == 22353 ? 9682 : 9680, 1);
			actor.decayMe();
			actor.doDie(actor);
		}
	}
}