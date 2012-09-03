package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.ReflectionTable;
import l2rt.util.Location;
import l2rt.util.Rnd;

/**
 * AI GreatPowerfulDevice (ID: 18777) в Seed of Destruction:
 * - при смерти всех в этом измерении шедюлит открытие двери через 5 мин открывает двери
 * - при смерти всех в этом измерении спаунит толпу мобов вокруг обелиска
 *
 * @author SYS
 */
public class GreatPowerfulDevice extends DefaultAI
{
	private static final int DOOR = 12240027;
	private static final int OPEN_DOOR_DELAY = 3 * 60 * 1000; // 3 мин
	private static final int[] MOBS = { 22540, // White Dragon Leader
			22546, // Warrior of Light
			22542, // Dragon Steed Troop Magic Leader
			22547, // Dragon Steed Troop Healer
			22538 // Dragon Steed Troop Commander
	};
	private static final Location OBELISK_LOC = new Location(-245825, 217075, -12208);

	public GreatPowerfulDevice(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		long refId = actor.getReflection().getId();

		if(checkAllDestroyed(actor.getNpcId(), refId))
		{
			// Спаун мобов вокруг обелиска
			for(int i = 0; i < 6; i++)
				for(int mobId : MOBS)
				{
					L2MonsterInstance npc = new L2MonsterInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(mobId));
					npc.setSpawnedLoc(Rnd.coordsRandomize(OBELISK_LOC.clone().setZ(-12224), 500, 900));
					npc.setReflection(actor.getReflection());
					npc.onSpawn();
					npc.spawnMe(npc.getSpawnedLoc());
				}

			// Таск открытия двери
			// Возможно на оффе дверь открывается по какому-то другому тригеру
			ThreadPoolManager.getInstance().scheduleGeneral(new OpenDoorTask(DOOR, refId), OPEN_DOOR_DELAY);
		}
		super.onEvtDead(killer);
	}

	/**
	 * Проверяет, уничтожены ли все GreatPowerfulDevice в текущем измерении
	 * @return true если все уничтожены
	 */
	private static boolean checkAllDestroyed(int mobId, long refId)
	{
		for(L2NpcInstance npc : L2ObjectsStorage.getAllByNpcId(mobId, true))
			if(npc.getReflection().getId() == refId)
				return false;
		return true;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	private class OpenDoorTask implements Runnable
	{
		private long _refId;
		private int _doorId;

		public OpenDoorTask(int doorId, long refId)
		{
			_doorId = doorId;
			_refId = refId;
		}

		public void run()
		{
			ReflectionTable.getInstance().get(_refId).openDoor(_doorId);
		}
	}
}