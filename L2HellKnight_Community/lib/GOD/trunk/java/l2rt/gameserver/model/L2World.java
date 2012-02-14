package l2rt.gameserver.model;

import l2rt.Config;
import l2rt.gameserver.instancemanager.DayNightSpawnManager;
import l2rt.gameserver.instancemanager.RaidBossSpawnManager;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.GArray;
import l2rt.util.Location;

import java.util.logging.Logger;

/**
 * @author Diamond
 * @Date: 15/5/2007
 * @Time: 10:06:34
 */
public class L2World
{
	private static final Logger _log = Logger.getLogger(L2World.class.getName());

	/** Map dimensions */
	public static final int MAP_MIN_X = Config.GEO_X_FIRST - 20 << 15;
	public static final int MAP_MAX_X = (Config.GEO_X_LAST - 19 << 15) - 1;
	public static final int MAP_MIN_Y = Config.GEO_Y_FIRST - 18 << 15;
	public static final int MAP_MAX_Y = (Config.GEO_Y_LAST - 17 << 15) - 1;
	public static final int MAP_MIN_Z = -32768;
	public static final int MAP_MAX_Z = 32767;

	public static final int WORLD_SIZE_X = Config.GEO_X_LAST - Config.GEO_X_FIRST + 1;
	public static final int WORLD_SIZE_Y = Config.GEO_Y_LAST - Config.GEO_Y_FIRST + 1;

	public static final int DIV_BY = Config.DIV_BY;
	public static final int DIV_BY_FOR_Z = Config.DIV_BY_FOR_Z;
	private static final int SMALL_DIV_BY = Config.DIV_BY / 2;

	/** calculated offset used so top left region is 0,0 */
	public static final int OFFSET_X = Math.abs(MAP_MIN_X / DIV_BY);
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y / DIV_BY);
	public static final int OFFSET_Z = Math.abs(MAP_MIN_Z / DIV_BY_FOR_Z);

	private static final int SMALL_OFFSET_X = Math.abs(MAP_MIN_X / DIV_BY);
	private static final int SMALL_OFFSET_Y = Math.abs(MAP_MIN_Y / DIV_BY);

	/** Размерность массива регионов */
	private static final int REGIONS_X = MAP_MAX_X / DIV_BY + OFFSET_X;
	private static final int REGIONS_Y = MAP_MAX_Y / DIV_BY + OFFSET_Y;
	private static final int REGIONS_Z = MAP_MAX_Z / DIV_BY_FOR_Z + OFFSET_Z;

	/** Размерность уменьшенной версии массива регионов */
	private static final int SMALL_REGIONS_X = MAP_MAX_X / SMALL_DIV_BY + SMALL_OFFSET_X;
	private static final int SMALL_REGIONS_Y = MAP_MAX_Y / SMALL_DIV_BY + SMALL_OFFSET_Y;

	// TODO убрать +1 и сделать везде сдвиг на 1
	private static L2WorldRegion[][][] _worldRegions = new L2WorldRegion[REGIONS_X + 1][REGIONS_Y + 1][];
	private static L2WorldRegion[][] _smallWorldRegions = new L2WorldRegion[SMALL_REGIONS_X + 1][SMALL_REGIONS_Y + 1];

	static
	{
		String[] split_regions = Config.VERTICAL_SPLIT_REGIONS.split(";");

		for(int x = 1; x <= REGIONS_X; x++)
			for(int y = 1; y <= REGIONS_Y; y++)
			{
				int wx = (x - OFFSET_X) * DIV_BY + DIV_BY / 2;
				int wy = (y - OFFSET_Y) * DIV_BY + DIV_BY / 2;
				if(split_regions.equals(wx + "_" + wy))
					_worldRegions[x][y] = new L2WorldRegion[REGIONS_Z + 1];
				else
					_worldRegions[x][y] = new L2WorldRegion[1];
			}
	}

	/**
	 * Выдает массив регионов, примыкающих к текущему, плюс текущий регион (квадрат 3х3 либо куб 3х3х3, если локация многоэтажная)
	 * На входе - координаты региона (уже преобразованные)
	 */
	public static GArray<L2WorldRegion> getNeighbors(int x, int y, int z, boolean small)
	{
		GArray<L2WorldRegion> neighbors = new GArray<L2WorldRegion>();
		if(small)
		{
			for(int a = -Config.VIEW_OFFSET; a <= Config.VIEW_OFFSET; a++)
				for(int b = -Config.VIEW_OFFSET; b <= Config.VIEW_OFFSET; b++)
					if(validRegion(x + a, y + b, 0, true) && _smallWorldRegions[x + a][y + b] != null)
						neighbors.add(_smallWorldRegions[x + a][y + b]);
		}
		else
			for(int a = -Config.VIEW_OFFSET; a <= Config.VIEW_OFFSET; a++)
				for(int b = -Config.VIEW_OFFSET; b <= Config.VIEW_OFFSET; b++)
					if(validRegion(x + a, y + b, 0, false))
						if(_worldRegions[x + a][y + b].length > 1)
						{
							for(int c = -Config.VIEW_OFFSET; c <= Config.VIEW_OFFSET; c++)
								if(validRegion(x + a, y + b, z + c, false) && _worldRegions[x + a][y + b][z + c] != null)
									neighbors.add(_worldRegions[x + a][y + b][z + c]);
						}
						else if(_worldRegions[x + a][y + b][0] != null)
							neighbors.add(_worldRegions[x + a][y + b][0]);
		return neighbors;
	}

	/**
	 * Выдает список соседей, с заданным разбросом по Z (параллилепипед 3х3хN)
	 * На входе - координаты обьекта (не региона)
	 */
	public static GArray<L2WorldRegion> getNeighborsZ(int x, int y, int z1, int z2)
	{
		GArray<L2WorldRegion> neighbors = new GArray<L2WorldRegion>();

		int _x = x / DIV_BY + OFFSET_X;
		int _y = y / DIV_BY + OFFSET_Y;
		int _z1 = z1 / DIV_BY_FOR_Z + OFFSET_Z;
		int _z2 = z2 / DIV_BY_FOR_Z + OFFSET_Z;

		for(int a = -1; a <= 1; a++)
			for(int b = -1; b <= 1; b++)
				if(validRegion(_x + a, _y + b, 0, false))
					if(_worldRegions[_x + a][_y + b].length > 1)
					{
						for(int c = _z1; c <= _z2; c++)
							if(validRegion(_x + a, _y + b, c, false) && _worldRegions[_x + a][_y + b][c] != null)
								neighbors.add(_worldRegions[_x + a][_y + b][c]);
					}
					else if(_worldRegions[_x + a][_y + b][0] != null)
						neighbors.add(_worldRegions[_x + a][_y + b][0]);

		return neighbors;
	}

	/**
	 * @param x координата
	 * @param y координата
	 * @param z координата
	 * @return Правильные ли координаты для региона
	 */
	public static boolean validRegion(int x, int y, int z, boolean small)
	{
		if(small)
			return x >= 1 && x <= SMALL_REGIONS_X && y >= 1 && y <= SMALL_REGIONS_Y;
		return x >= 1 && x <= REGIONS_X && y >= 1 && y <= REGIONS_Y && z >= 0 && z < _worldRegions[x][y].length;
	}

	/**
	 * @param loc локация для поиска региона
	 * @return Регион, соответствующий локации (не путать с L2Object.getCurrentRegion())
	 */
	public static L2WorldRegion getRegion(Location loc, long refId)
	{
		return getRegion(loc.x, loc.y, loc.z, refId == -2);
	}

	/**
	 * @param obj обьект для поиска региона
	 * @return Регион, соответствующий координатам обьекта (не путать с L2Object.getCurrentRegion())
	 */
	public static L2WorldRegion getRegion(L2Object obj)
	{
		return getRegion(obj.getX(), obj.getY(), obj.getZ(), obj.getReflection().getId() == -2);
	}

	/**
	 * @param x координата
	 * @param y координата
	 * @param z координата
	 * @param small true, если регион маленький (торговая площадь в параллельном мире)
	 * @return Регион, соответствующий координатам
	 */
	public static L2WorldRegion getRegion(int x, int y, int z, boolean small)
	{
		if(small)
		{
			int _x = x / SMALL_DIV_BY + SMALL_OFFSET_X;
			int _y = y / SMALL_DIV_BY + SMALL_OFFSET_Y;
			if(validRegion(_x, _y, 0, true))
			{
				if(_smallWorldRegions[_x][_y] == null)
					_smallWorldRegions[_x][_y] = new L2WorldRegion(_x, _y, 0, true);
				return _smallWorldRegions[_x][_y];
			}
		}
		else
		{
			int _x = x / DIV_BY + OFFSET_X;
			int _y = y / DIV_BY + OFFSET_Y;

			if(validRegion(_x, _y, 0, false))
			{
				int _z = 0;

				if(_worldRegions[_x][_y].length > 1)
				{
					_z = z / DIV_BY_FOR_Z + OFFSET_Z;
					if(!validRegion(_x, _y, _z, false))
						return null;
				}

				if(_worldRegions[_x][_y][_z] == null)
					_worldRegions[_x][_y][_z] = new L2WorldRegion(_x, _y, _z, false);

				return _worldRegions[_x][_y][_z];
			}
		}
		return null;
	}

	/**
	 * Удаление обьекта из всех территорий, также очистка территорий у удаленного обьекта
	 */
	public static void removeObject(L2Object object)
	{
		if(object != null)
			object.clearTerritories();
	}

	/**
	* Находит игрока по имени
	* Регистр символов любой.
	* @param name имя
	* @return найденый игрок или null если игрока нет
	*/
	public static L2Player getPlayer(String name)
	{
		return L2ObjectsStorage.getPlayer(name);
	}

	/**
	 * Проверяет, сменился ли регион в котором находится обьект
	 * Если сменился - удаляет обьект из старого региона и добавляет в новый.
	 * @param object обьект для проверки
	 * @param dropper - если это L2ItemInstance, то будет анимация дропа с перса
	 */
	public static void addVisibleObject(L2Object object, L2Character dropper)
	{
		if(object == null || !object.isVisible() || object.inObserverMode())
			return;

		if(object.isPet() || object.isSummon())
		{
			L2Player owner = object.getPlayer();
			if(owner != null && object.getReflection() != owner.getReflection())
				object.setReflection(owner.getReflection());
		}

		L2WorldRegion region = getRegion(object);
		L2WorldRegion currentRegion = object.getCurrentRegion();

		if(region == null || currentRegion != null && currentRegion.equals(region))
			return;
		
		if(object.isNpc())
			((L2NpcInstance) object).setShowSpawnAnimation(((L2NpcInstance) object).getTemplate().getAIParams().getBool("spawnAnimationDisabled", false) ? 0 : 2);

		if(currentRegion == null) // Новый обьект (пример - игрок вошел в мир, заспаунился моб, дропнули вещь)
		{
			// Показываем обьект в текущем и соседних регионах
			// Если обьект игрок, показываем ему все обьекты в текущем и соседних регионах
			for(L2WorldRegion neighbor : region.getNeighbors())
				neighbor.addToPlayers(object, dropper);

			// Добавляем обьект в список видимых
			region.addObject(object);
			object.setCurrentRegion(region);
		}
		else
		// Обьект уже существует, перешел из одного региона в другой
		{
			currentRegion.removeObject(object, true); // Удаляем обьект из старого региона
			region.addObject(object); // Добавляем обьект в список видимых
			object.setCurrentRegion(region);

			GArray<L2WorldRegion> oldNeighbors = currentRegion.getNeighbors();
			GArray<L2WorldRegion> newNeighbors = region.getNeighbors();

			// Убираем обьект из старых соседей.
			for(L2WorldRegion neighbor : oldNeighbors)
			{
				boolean flag = true;
				for(L2WorldRegion newneighbor : newNeighbors)
					if(newneighbor != null && newneighbor.equals(neighbor))
					{
						flag = false;
						break;
					}
				if(flag)
					neighbor.removeFromPlayers(object, true);
			}

			// Показываем обьект, но в отличие от первого случая - только для новых соседей.
			for(L2WorldRegion neighbor : newNeighbors)
			{
				boolean flag = true;
				for(L2WorldRegion oldneighbor : oldNeighbors)
					if(oldneighbor != null && oldneighbor.equals(neighbor))
					{
						flag = false;
						break;
					}
				if(flag)
					neighbor.addToPlayers(object, dropper);
			}
		}
		
		if(object.isNpc())
			((L2NpcInstance) object).setShowSpawnAnimation(0);
	}

	/**
	 * Удаляет обьект из текущего региона
	 * @param object обьект для удаления
	 */
	public static void removeVisibleObject(L2Object object)
	{
		if(object == null || object.isVisible() || object.inObserverMode())
			return;
		if(object.getCurrentRegion() != null)
		{
			object.getCurrentRegion().removeObject(object, false);
			if(object.getCurrentRegion() != null)
				for(L2WorldRegion neighbor : object.getCurrentRegion().getNeighbors())
					neighbor.removeFromPlayers(object, false);
			object.setCurrentRegion(null);
		}
	}

	/**
	 * Проверяет координаты на корректность
	 * @param x координата x
	 * @param y координата y
	 * @return Корректные ли координаты
	 */
	public static boolean validCoords(int x, int y)
	{
		return x > MAP_MIN_X && x < MAP_MAX_X && y > MAP_MIN_Y && y < MAP_MAX_Y;
	}

	/**
	 * Удаляет весь спаун
	 */
	public static synchronized void deleteVisibleNpcSpawns()
	{
		RaidBossSpawnManager.getInstance().cleanUp();
		DayNightSpawnManager.getInstance().cleanUp();

		_log.info("Deleting all visible NPC's...");

		for(int i = 1; i <= REGIONS_X; i++)
			for(int j = 1; j <= REGIONS_Y; j++)
				if(_worldRegions[i][j].length > 1)
				{
					for(int k = 0; k < REGIONS_Z; k++)
						if(_worldRegions[i][j][k] != null)
							_worldRegions[i][j][k].deleteVisibleNpcSpawns();
				}
				else if(_worldRegions[i][j][0] != null)
					_worldRegions[i][j][0].deleteVisibleNpcSpawns();

		for(int i = 1; i <= SMALL_REGIONS_X; i++)
			for(int j = 1; j <= SMALL_REGIONS_Y; j++)
				if(_smallWorldRegions[i][j] != null)
					_smallWorldRegions[i][j].deleteVisibleNpcSpawns();

		_log.info("All visible NPC's deleted.");
	}

	public static L2Object getAroundObjectById(L2Object object, Integer id)
	{
		L2WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return null;
		GArray<L2WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(L2WorldRegion region : neighbors)
			size += region.getObjectsSize();
		for(L2WorldRegion region : neighbors)
			for(L2Object o : region.getObjectsList(new GArray<L2Object>(size), object.getObjectId(), object.getReflection()))
				if(o != null && o.getObjectId() == id)
					return o;
		return null;
	}

	public static GArray<L2Object> getAroundObjects(L2Object object)
	{
		int oid = object.getObjectId();
		L2WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return new GArray<L2Object>(0);
		GArray<L2WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(L2WorldRegion region : neighbors)
			size += region.getObjectsSize();
		GArray<L2Object> result = new GArray<L2Object>(size);
		for(L2WorldRegion region : neighbors)
			region.getObjectsList(result, oid, object.getReflection());
		return result;
	}

	public static GArray<L2Object> getAroundObjects(L2Object object, int radius, int height)
	{
		int oid = object.getObjectId();
		L2WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return new GArray<L2Object>(0);
		GArray<L2WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(L2WorldRegion region : neighbors)
			size += region.getObjectsSize();
		GArray<L2Object> result = new GArray<L2Object>(size);
		for(L2WorldRegion region : neighbors)
			region.getObjectsList(result, oid, object.getReflection(), object.getX(), object.getY(), object.getZ(), radius * radius, height);
		return result;
	}

	public static GArray<L2Character> getAroundCharacters(L2Object object)
	{
		int oid = object.getObjectId();
		L2WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return new GArray<L2Character>(0);
		GArray<L2WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(L2WorldRegion region : neighbors)
			size += region.getObjectsSize();
		GArray<L2Character> result = new GArray<L2Character>(size);
		for(L2WorldRegion region : neighbors)
			region.getCharactersList(result, oid, object.getReflection().getId());
		return result;
	}

	public static GArray<L2Character> getAroundCharacters(L2Object object, int radius, int height)
	{
		int oid = object.getObjectId();
		L2WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return new GArray<L2Character>(0);
		GArray<L2WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(L2WorldRegion region : neighbors)
			size += region.getObjectsSize();
		GArray<L2Character> result = new GArray<L2Character>(size);
		for(L2WorldRegion region : neighbors)
			region.getCharactersList(result, oid, object.getReflection().getId(), object.getX(), object.getY(), object.getZ(), radius * radius, height);
		return result;
	}

	public static GArray<L2NpcInstance> getAroundNpc(L2Object object)
	{
		int oid = object.getObjectId();
		L2WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return new GArray<L2NpcInstance>(0);
		GArray<L2WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(L2WorldRegion region : neighbors)
			size += region.getObjectsSize();
		GArray<L2NpcInstance> result = new GArray<L2NpcInstance>(size);
		for(L2WorldRegion region : neighbors)
			region.getNpcsList(result, oid, object.getReflection().getId());
		return result;
	}

	public static GArray<L2NpcInstance> getAroundNpc(L2Object object, int radius, int height)
	{
		int oid = object.getObjectId();
		L2WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return new GArray<L2NpcInstance>(0);
		GArray<L2WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(L2WorldRegion region : neighbors)
			size += region.getObjectsSize();
		GArray<L2NpcInstance> result = new GArray<L2NpcInstance>(size);
		for(L2WorldRegion region : neighbors)
			region.getNpcsList(result, oid, object.getReflection().getId(), object.getX(), object.getY(), object.getZ(), radius * radius, height);
		return result;
	}

	public static GArray<L2Playable> getAroundPlayables(L2Object object)
	{
		int oid = object.getObjectId();
		L2WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return new GArray<L2Playable>(0);
		GArray<L2WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(L2WorldRegion region : neighbors)
			size += region.getPlayersSize();
		GArray<L2Playable> result = new GArray<L2Playable>(size * 2); // Считаем что петов почти столько же, сколько игроков
		for(L2WorldRegion region : neighbors)
			region.getPlayablesList(result, oid, object.getReflection());
		return result;
	}

	public static GArray<L2Playable> getAroundPlayables(L2Object object, int radius, int height)
	{
		int oid = object.getObjectId();
		L2WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return new GArray<L2Playable>(0);
		GArray<L2WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(L2WorldRegion region : neighbors)
			size += region.getPlayersSize();
		GArray<L2Playable> result = new GArray<L2Playable>(size * 2); // Считаем что петов почти столько же, сколько игроков
		for(L2WorldRegion region : neighbors)
			region.getPlayablesList(result, oid, object.getReflection(), object.getX(), object.getY(), object.getZ(), radius * radius, height);
		return result;
	}

	public static GArray<L2Player> getAroundPlayers(L2Object object)
	{
		int oid = object.getObjectId();
		L2WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return new GArray<L2Player>(0);
		GArray<L2WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 10; // С небольшим запасом
		for(L2WorldRegion region : neighbors)
			size += region.getPlayersSize();
		GArray<L2Player> result = new GArray<L2Player>(size);
		for(L2WorldRegion region : neighbors)
			region.getPlayersList(result, oid, object.getReflection());
		return result;
	}

	public static GArray<L2Player> getAroundPlayers(L2Object object, int radius, int height)
	{
		int oid = object.getObjectId();
		L2WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return new GArray<L2Player>(0);
		GArray<L2WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 10; // С запасом
		for(L2WorldRegion region : neighbors)
			size += region.getPlayersSize();
		GArray<L2Player> result = new GArray<L2Player>(size);
		for(L2WorldRegion region : neighbors)
			region.getPlayersList(result, oid, object.getReflection(), object.getX(), object.getY(), object.getZ(), radius * radius, height);
		return result;
	}

	public static boolean isAroundPlayers(L2Object object)
	{
		return object.getCurrentRegion() != null && !object.getCurrentRegion().areNeighborsEmpty();
	}

	public static void addTerritory(L2Territory territory)
	{
		int xmin = territory.getXmin() / DIV_BY + OFFSET_X;
		int ymin = territory.getYmin() / DIV_BY + OFFSET_Y;
		int zmin = territory.getZmin() / DIV_BY_FOR_Z + OFFSET_Z;

		int xmax = territory.getXmax() / DIV_BY + OFFSET_X;
		int ymax = territory.getYmax() / DIV_BY + OFFSET_Y;
		int zmax = territory.getZmax() / DIV_BY_FOR_Z + OFFSET_Z;

		for(int x = xmin; x <= xmax; x++)
			for(int y = ymin; y <= ymax; y++)
				if(validRegion(x, y, 0, false))
					if(_worldRegions[x][y].length > 1)
					{
						for(int z = zmin; z <= zmax; z++)
							if(validRegion(x, y, z, false))
							{
								if(_worldRegions[x][y][z] == null)
									_worldRegions[x][y][z] = new L2WorldRegion(x, y, z, false);
								_worldRegions[x][y][z].addTerritory(territory);
							}
					}
					else
					{
						if(_worldRegions[x][y][0] == null)
							_worldRegions[x][y][0] = new L2WorldRegion(x, y, 0, false);
						_worldRegions[x][y][0].addTerritory(territory);
					}
	}

	public static void removeTerritory(L2Territory territory)
	{
		int xmin = territory.getXmin() / DIV_BY + OFFSET_X;
		int ymin = territory.getYmin() / DIV_BY + OFFSET_Y;
		int zmin = territory.getZmin() / DIV_BY_FOR_Z + OFFSET_Z;

		int xmax = territory.getXmax() / DIV_BY + OFFSET_X;
		int ymax = territory.getYmax() / DIV_BY + OFFSET_Y;
		int zmax = territory.getZmax() / DIV_BY_FOR_Z + OFFSET_Z;

		for(int x = xmin; x <= xmax; x++)
			for(int y = ymin; y <= ymax; y++)
				if(validRegion(x, y, 0, false))
					if(_worldRegions[x][y].length > 1)
					{
						for(int z = zmin; z <= zmax; z++)
							if(validRegion(x, y, z, false) && _worldRegions[x][y][z] != null)
								_worldRegions[x][y][z].removeTerritory(territory);
					}
					else if(_worldRegions[x][y][0] != null)
						_worldRegions[x][y][0].removeTerritory(territory);
	}

	/**
	 * Создает и возвращает список территорий для точек x, y, z
	 * @param x точка x
	 * @param y точка y
	 * @return список территорий в зависимости от параметра onlyActive
	 */
	public static GArray<L2Territory> getTerritories(int x, int y, int z)
	{
		L2WorldRegion region = L2World.getRegion(x, y, z, false); // Здесь важно именно false
		return region == null ? null : region.getTerritories(x, y, z);
	}

	public static L2Territory getWater(Location loc)
	{
		GArray<L2Territory> territories = getTerritories(loc.x, loc.y, loc.z);
		if(territories != null)
			for(L2Territory terr : territories)
				if(terr != null && terr.getZone() != null && terr.getZone().getType() == ZoneType.water)
					return terr;
		return null;
	}

	public static boolean isWater(Location loc)
	{
		return getWater(loc) != null;
	}

	public static L2WorldRegion[][][] getRegions()
	{
		return _worldRegions;
	}
		
	public static int getRegionsCount(Boolean active)
	{
		int ret = 0;
		for(L2WorldRegion[][] wr0 : _worldRegions)
		{
			if(wr0 == null)
				continue;
			for(L2WorldRegion[] wr1 : wr0)
			{
				if(wr1 == null)
					continue;
				for(L2WorldRegion wr2 : wr1)
				{
					if(wr2 == null)
						continue;
					if(active != null && wr2.areNeighborsEmpty() == active)
						continue;
					ret++;
				}
			}
		}

		return ret;
	}
}