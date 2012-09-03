package ai;

import javolution.util.FastMap;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2MinionInstance;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Location;
import l2rt.util.MinionList;

/**
 * AI боса Ranku для Tower of Infinitum:
 * - при смерти спаунит портал.
 * - В боевом состоянии АИ снижает ХП у первичных миньонов
 * - При неактивности портает игроков на этаж ниже
 *
 * @author SYS
 */
public class Ranku extends Fighter
{
	private static final long TELEPORT_BACK_BY_INACTIVITY_INTERVAL = 5 * 60 * 1000; // 5 мин
	private static final int TELEPORTATION_CUBIC_ID = 32375;
	private static final Location CUBIC_POSITION = new Location(-19016, 278312, -15040, 0);
	private long _lastAttackTime = 0;
	private static FastMap<Integer, L2Zone> _floorZones = null;
	private static final int ZONE_OFFSET = 705000;
	private static final int SCAPEGOAT_ID = 32305;

	public Ranku(L2Character actor)
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
		_lastAttackTime = System.currentTimeMillis();
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
			// Портаем всех игроков с 10 на 9 этаж
			for(L2Character c : actor.getAroundCharacters(3500, 200))
				if(c != null && c.isPlayer() && _floorZones.get(10).checkIfInZone(c))
					c.teleToLocation(_floorZones.get(9).getSpawn());

			_lastAttackTime = 0;
			return true;
		}

		return super.thinkActive();
	}

	@Override
	protected void thinkAttack()
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return;

		// Уменьшаем ХП у миньонов-носильщиков во время боя
		MinionList ml = ((L2MonsterInstance) actor).getMinionList();
		if(ml != null && ml.hasMinions())
			for(L2MinionInstance m : ml.getSpawnedMinions())
				if(m.getNpcId() == SCAPEGOAT_ID && !m.isDead())
					m.reduceCurrentHp(m.getMaxHp() / 30, actor, null, false, true, true, false);

		super.thinkAttack();
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		_lastAttackTime = 0;

		L2NpcInstance cubic = new L2NpcInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(TELEPORTATION_CUBIC_ID));
		cubic.setSpawnedLoc(CUBIC_POSITION);
		cubic.onSpawn();
		cubic.spawnMe(CUBIC_POSITION);

		super.onEvtDead(killer);
	}
}