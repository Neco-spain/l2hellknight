package ai;

import java.util.HashMap;

import l2rt.gameserver.ai.Priest;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;
import bosses.ValakasManager;

public class Valakas extends Priest
{
	final L2Skill valakas_lava_skin;

	public Valakas(L2Character actor)
	{
		super(actor);
		HashMap<Integer, L2Skill> skills = getActor().getTemplate().getSkills();
		valakas_lava_skin = skills.get(4680);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		ValakasManager.setLastAttackTime();
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected boolean createNewTask()
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return false;

		// Если стоим на лаве, то используем защитный скилл
		if(Rnd.chance(30) && actor.isInZone(ZoneType.poison))
		{
			clearTasks();
			return chooseTaskAndTargets(valakas_lava_skin, actor, 0);
		}

		return super.createNewTask();
	}

	@Override
	protected boolean maybeMoveToHome()
	{
		L2NpcInstance actor = getActor();
		if(actor != null && !ValakasManager.getZone().checkIfInZone(actor.getX(), actor.getY()))
			teleportHome(true);
		return false;
	}

	@Override
	public int getRatePHYS()
	{
		return 0;
	}
}