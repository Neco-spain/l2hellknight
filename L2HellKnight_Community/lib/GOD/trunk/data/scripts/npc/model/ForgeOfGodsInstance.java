package npc.model;

import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Rnd;

public class ForgeOfGodsInstance extends L2MonsterInstance
{
	private boolean canSpawn = false;
	private static final int MOBS[] = { 18799, 18800, 18801, 18802, 18803 };
	
	public ForgeOfGodsInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	
	@Override
	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect)
	{
		if(Rnd.chance(50)) // Шанс появления.
			canSpawn = true; // Если шанс успешный, разрешаем спавн.
		super.reduceCurrentHp(i, attacker, skill, awake, standUp, directHp, canReflect);
		if (canSpawn && isDead()) // Если моб умер и спавн разрешен, спавним.
		{
			L2MonsterInstance npc = new L2MonsterInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(MOBS[Rnd.get(MOBS.length)]));
			npc.setSpawnedLoc(GeoEngine.findPointToStay(getX(), getY(), getZ(), 100, 120, getReflection().getGeoIndex()));
			npc.setReflection(getReflection().getId());
			npc.onSpawn();
			npc.spawnMe(npc.getSpawnedLoc());
			npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(1, 100));
			canSpawn = false; // Закрываем спавн. Если этого не сделать, то тригер останется открытым для всех остальных мобов.
		}
	}
}