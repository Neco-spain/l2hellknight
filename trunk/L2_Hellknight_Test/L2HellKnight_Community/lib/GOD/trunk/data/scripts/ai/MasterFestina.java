package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Location;
import l2rt.util.Rnd;

/**
 * Через 10 сек после смерти активирует зону перехода на следующий этаж Tylly's Workshop.
 * Контролирует спаун охраны.
 * @author SYS
 */
public class MasterFestina extends Fighter
{
	private static Location TullyFloor4LocationPoint = new Location(-14238, 273002, -10496);
	private static L2Zone _zone;
	private static Location[] _mysticSpawnPoints;
	private static Location[] _spiritGuardSpawnPoints;
	private final static int FOUNDRY_MYSTIC_ID = 22387;
	private final static int FOUNDRY_SPIRIT_GUARD_ID = 22389;
	private long _lastFactionNotifyTime = 0;

	public MasterFestina(L2Character actor)
	{
		super(actor);

		_zone = ZoneManager.getInstance().getZoneById(ZoneType.dummy, 704010, true);

		_mysticSpawnPoints = new Location[] { new Location(-11480, 273992, -11768), new Location(-11128, 273992, -11864),
				new Location(-10696, 273992, -11936), new Location(-12552, 274920, -11752),
				new Location(-12568, 275320, -11864), new Location(-12568, 275784, -11936),
				new Location(-13480, 273880, -11752), new Location(-13880, 273880, -11864),
				new Location(-14328, 273880, -11936), new Location(-12456, 272968, -11752),
				new Location(-12456, 272552, -11864), new Location(-12456, 272120, -11936) };

		_spiritGuardSpawnPoints = new Location[] { new Location(-12552, 272168, -11936),
				new Location(-12552, 272520, -11872), new Location(-12552, 272984, -11744),
				new Location(-13432, 273960, -11736), new Location(-13864, 273960, -11856),
				new Location(-14296, 273976, -11936), new Location(-12504, 275736, -11936),
				new Location(-12472, 275288, -11856), new Location(-12472, 274888, -11744),
				new Location(-11544, 273912, -11752), new Location(-11160, 273912, -11856),
				new Location(-10728, 273896, -11936) };
	}

	@Override
	protected void onEvtSpawn()
	{
		// Спауним охрану
		for(Location loc : _mysticSpawnPoints)
		{
			L2MonsterInstance mob = new L2MonsterInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(FOUNDRY_MYSTIC_ID));
			mob.setSpawnedLoc(loc);
			mob.onSpawn();
			mob.spawnMe(loc);
		}
		for(Location loc : _spiritGuardSpawnPoints)
		{
			L2MonsterInstance mob = new L2MonsterInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(FOUNDRY_SPIRIT_GUARD_ID));
			mob.setSpawnedLoc(loc);
			mob.onSpawn();
			mob.spawnMe(loc);
		}
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		if(System.currentTimeMillis() - _lastFactionNotifyTime > actor.minFactionNotifyInterval)
		{
			for(L2NpcInstance npc : actor.getAroundNpc(3000, 500))
				if(npc.getNpcId() == FOUNDRY_MYSTIC_ID || npc.getNpcId() == FOUNDRY_SPIRIT_GUARD_ID)
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, new Object[] { attacker, Rnd.get(1, 100) });

			_lastFactionNotifyTime = System.currentTimeMillis();
		}

		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		_lastFactionNotifyTime = 0;
		super.onEvtDead(killer);
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		// Удаляем охрану
		for(L2NpcInstance npc : actor.getAroundNpc(3000, 500))
			if(npc.getNpcId() == FOUNDRY_MYSTIC_ID || npc.getNpcId() == FOUNDRY_SPIRIT_GUARD_ID)
				npc.deleteMe();

		ThreadPoolManager.getInstance().scheduleAi(new TeleportTask(), 10000, true);
	}

	public class TeleportTask implements Runnable
	{
		public void run()
		{
			for(L2Player p : _zone.getInsidePlayersIncludeZ())
				if(p != null)
					p.teleToLocation(TullyFloor4LocationPoint);
		}
	}
}