package npc.model;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class QueenAntLarvaInstance extends L2MonsterInstance
{
	public QueenAntLarvaInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setImobilised(true);
	}

	@Override
	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect)
	{
		double damage = getCurrentHp() - i > 1 ? i : getCurrentHp() - 1;
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect);
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}