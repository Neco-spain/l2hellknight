package l2rt.gameserver.instancemanager;

import javolution.util.FastList;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.NaiaSpore;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Location;

public class TowerOfNaiaManager
{
	public static final int SPORE_FIRE_ID = 25605;
	public static final int SPORE_WATER_ID = 25606;
	public static final int SPORE_WIND_ID = 25607;
	public static final int SPORE_EARTH_ID = 25608;
	public static final int EPIDOS_FIRE_ID = 25609;
	public static final int EPIDOS_WATER_ID = 25610;
	public static final int EPIDOS_WIND_ID = 256011;
	public static final int EPIDOS_EARTH_ID = 25612;
	public static final Location CENTRAL_COLUMN = new Location(-45474, 247450, -13994);
	public static final Location TELEPORT_LOCATION = new Location(-46344, 247784, -14207);

	private static Attribute _currentAttribute = Attribute.FIRE;
	private static int _currentEpidosIndex = 0;
	private static boolean _isEpidosSpawned = false;

	private static L2MonsterInstance _roofLock = null;

	private static FastList<L2MonsterInstance> _spores = new FastList<L2MonsterInstance>();

	public static synchronized void handleEpidosIndex(L2MonsterInstance naiaSpore)
	{
		if(naiaSpore == null)
			return;

		if(_isEpidosSpawned)
			return;

		int sporeId = naiaSpore.getNpcId();
		switch(sporeId)
		{
			case 25605:
				if(_currentAttribute == Attribute.FIRE)
					_currentEpidosIndex += 1;
				else if(_currentAttribute == Attribute.WATER)
					_currentEpidosIndex += -2;
				else
					_currentEpidosIndex += -1;
				break;
			case 25606:
				if(_currentAttribute == Attribute.WATER)
					_currentEpidosIndex += 1;
				else if(_currentAttribute == Attribute.FIRE)
					_currentEpidosIndex += -2;
				else
					_currentEpidosIndex += -1;
				break;
			case 25607:
				if(_currentAttribute == Attribute.WIND)
					_currentEpidosIndex += 1;
				else if(_currentAttribute == Attribute.EARTH)
					_currentEpidosIndex += -2;
				else
					_currentEpidosIndex += -1;
				break;
			case 25608:
				if(_currentAttribute == Attribute.EARTH)
					_currentEpidosIndex += 1;
				else if(_currentAttribute == Attribute.WIND)
					_currentEpidosIndex += -2;
				else
					_currentEpidosIndex += -1;
		}

		if(_currentEpidosIndex >= 50)
		{
			for(L2MonsterInstance spore : _spores)
			{
				if(spore != null)
				{
					NaiaSpore ai = (NaiaSpore) spore.getAI();
					if(ai != null)
					{
						ai.notifyEpidosIndexReached();
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnEpidosTask(_currentAttribute), 3000);
						_isEpidosSpawned = true;
					}
				}
			}
		}
		else
		{
			if(_currentEpidosIndex > 0)
				return;
			switch(sporeId)
			{
				case 25605:
					_currentAttribute = Attribute.FIRE;
					break;
				case 25606:
					_currentAttribute = Attribute.WATER;
					break;
				case 25607:
					_currentAttribute = Attribute.WIND;
					break;
				case 25608:
					_currentAttribute = Attribute.EARTH;
			}
		}
	}

	public static synchronized void addSpore(L2MonsterInstance naiaSpore)
	{
		int i = _spores.indexOf(naiaSpore);
		if(i != -1)
			_spores.remove(i);
		_spores.add(naiaSpore);
	}

	public static void registerRoofLock(L2MonsterInstance roofLock)
	{
		_roofLock = roofLock;
	}

	public static L2MonsterInstance getRoofLock()
	{
		return _roofLock;
	}

	public static boolean isEpidosSpawned()
	{
		return _isEpidosSpawned;
	}

	private static class TeleportEpidosTask implements Runnable
	{
		L2MonsterInstance _mob = null;

		public TeleportEpidosTask(L2MonsterInstance mob)
		{
			_mob = mob;
		}

		public void run()
		{
			if(_mob != null)
				_mob.teleToLocation(TELEPORT_LOCATION);
		}
	}

	private static class SpawnEpidosTask implements Runnable
	{
		private Attribute _attribute;

		public SpawnEpidosTask(Attribute attribute)
		{
			_attribute = attribute;
		}

		public void run()
		{
			int epidosId = 0;
			switch(_attribute)
			{
				case FIRE:
					epidosId = 25610;
					break;
				case WATER:
					epidosId = 25609;
					break;
				case WIND:
					epidosId = 25612;
					break;
				case EARTH:
					epidosId = 25611;
			}

			L2MonsterInstance epidos = new L2MonsterInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(epidosId));
			epidos.setSpawnedLoc(CENTRAL_COLUMN);
			epidos.onSpawn();
			epidos.spawnMe(CENTRAL_COLUMN);

			ThreadPoolManager.getInstance().scheduleGeneral(new TeleportEpidosTask(epidos), 3000);
		}
	}

	public static enum Attribute
	{
		FIRE,
		WATER,
		WIND,
		EARTH;
	}
}