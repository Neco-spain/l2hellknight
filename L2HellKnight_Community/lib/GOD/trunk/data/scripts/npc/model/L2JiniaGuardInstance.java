package npc.model;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2JiniaGuardInstance extends L2MonsterInstance
{
	
	public L2JiniaGuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect)
	{
		if(attacker.isPlayer()) //Игнорирует атаку игрока...
			return;
			
		super.reduceCurrentHp(i, attacker, skill, awake, standUp, directHp, canReflect);
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}