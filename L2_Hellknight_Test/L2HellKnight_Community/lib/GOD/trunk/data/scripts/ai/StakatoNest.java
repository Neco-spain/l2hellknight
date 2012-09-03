package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.instances.L2MinionInstance;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.MinionList;
import l2rt.util.Rnd;


public class StakatoNest extends Fighter
{
	// Cannibalistic Stakato Leader
	private static final int _stakato_leader = 22625;

	public StakatoNest(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance npc = getActor();
		L2MonsterInstance _mob = (L2MonsterInstance) npc;
		if ((_mob.getNpcId() == _stakato_leader) && (Rnd.get(1000) < 100) && (_mob.getCurrentHp() < (_mob.getMaxHp() * 0.3)))
		{
			MinionList ml = ((L2MonsterInstance) npc).getMinionList();
			if (ml != null)
			{
				for(L2MinionInstance m : ml.getSpawnedMinions())
				{
					double _hp = m.getCurrentHp();
					
					if (_hp > (m.getMaxHp() * 0.3))
					{
						_mob.abortAttack(true, false);
						_mob.abortCast(true);
						//_mob.setHeading(Util.calculateHeadingFrom(_mob, m));
						_mob.doCast(SkillTable.getInstance().getInfo(4484, 1), _mob, true);
						_mob.setCurrentHp(_mob.getCurrentHp() + _hp, false);
						m.doDie(m);
						m.deleteMe();
					}
				}
			}
		}
		super.onEvtAttacked(attacker, damage);
	}	
}