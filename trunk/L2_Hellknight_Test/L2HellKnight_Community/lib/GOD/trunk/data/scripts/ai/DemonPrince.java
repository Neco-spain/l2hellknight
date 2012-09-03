package ai;

import javolution.util.FastMap;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.Location;

/**
 * AI боса Demon Prince для Tower of Infinitum:
 * - при смерти спаунит портал.
 * - на 10% ХП использует скилл NPC Ultimate Defense(5044.3)
 * @author SYS
 */
public class DemonPrince extends Fighter
{
	private static final int ULTIMATE_DEFENSE_SKILL_ID = 5044;
	private static final L2Skill ULTIMATE_DEFENSE_SKILL = SkillTable.getInstance().getInfo(ULTIMATE_DEFENSE_SKILL_ID, 3);
	private static final long TELEPORT_BACK_BY_INACTIVITY_INTERVAL = 5 * 60 * 1000; // 5 мин
	private static final int TELEPORTATION_CUBIC_ID = 32374;
	private static final Location CUBIC_POSITION = new Location(-22200, 278328, -8256, 0);
	private boolean _notUsedUltimateDefense = true;
	private long _lastAttackTime = 0;
	private static FastMap<Integer, L2Zone> _floorZones = null;
	private static final int ZONE_OFFSET = 705000;

	public DemonPrince(L2Character actor)
	{
		super(actor);
		if(_floorZones == null)
		{
			_floorZones = new FastMap<Integer, L2Zone>(10);
			for(int i = 1; i < 11; i++)
				_floorZones.put(i, ZoneManager.getInstance().getZoneById(ZoneType.dummy, ZONE_OFFSET + i, true));
		}
	}

	@Override
	protected void onEvtSpawn()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		// При спауне удаляем Teleportation Cubic если есть
		for(L2NpcInstance npc : actor.getAroundNpc(1000, 200))
			if(npc.getNpcId() == TELEPORTATION_CUBIC_ID)
			{
				npc.deleteMe();
				return;
			}
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		_lastAttackTime = System.currentTimeMillis();

		if(_notUsedUltimateDefense && actor.getCurrentHpPercents() < 10)
		{
			_notUsedUltimateDefense = false;

			// FIXME Скилл использует, но эффект скила не накладывается.
			clearTasks();
			addTaskBuff(actor, ULTIMATE_DEFENSE_SKILL);
		}

		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return true;

		// Портает на этаж вниз, если боса долго не били
		if(_lastAttackTime != 0 && _lastAttackTime + TELEPORT_BACK_BY_INACTIVITY_INTERVAL < System.currentTimeMillis())
		{
			// Портаем всех игроков с 5 на 4 этаж
			for(L2Character c : actor.getAroundCharacters(3500, 200))
				if(c != null && c.isPlayer() && _floorZones.get(5).checkIfInZone(c))
					c.teleToLocation(_floorZones.get(4).getSpawn());

			_lastAttackTime = 0;
			return true;
		}

		return super.thinkActive();
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		_notUsedUltimateDefense = true;
		_lastAttackTime = 0;

		L2NpcInstance cubic = new L2NpcInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(TELEPORTATION_CUBIC_ID));
		cubic.setSpawnedLoc(CUBIC_POSITION);
		cubic.onSpawn();
		cubic.spawnMe(CUBIC_POSITION);

		super.onEvtDead(killer);
	}
}