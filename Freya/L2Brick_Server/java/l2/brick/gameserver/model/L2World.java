package l2.brick.gameserver.model;

import gnu.trove.TObjectProcedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;

import l2.brick.Config;
import l2.brick.gameserver.GmListTable;
import l2.brick.gameserver.datatables.CharNameTable;
import l2.brick.gameserver.model.actor.L2Playable;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.actor.instance.L2PetInstance;
import l2.brick.gameserver.network.L2GameClient;
import l2.brick.gameserver.util.L2TIntObjectHashMap;
import l2.brick.util.Point3D;
import l2.brick.util.StringUtil;

public final class L2World
{
	private static Logger _log = Logger.getLogger(L2World.class.getName());

	/**
	 * Gracia border
	 * Flying objects not allowed to the east of it.
	 */
	public static final int GRACIA_MAX_X = -166168;
	public static final int GRACIA_MAX_Z = 6105;
	public static final int GRACIA_MIN_Z = -895;

	public static final int SHIFT_BY = 12;
	
	private static final int TILE_SIZE = 32768;

	/** Map dimensions */
	public static final int MAP_MIN_X = (Config.WORLD_X_MIN - 20) * TILE_SIZE;
	public static final int MAP_MAX_X = (Config.WORLD_X_MAX - 19) * TILE_SIZE;
	public static final int MAP_MIN_Y = (Config.WORLD_Y_MIN - 18) * TILE_SIZE;
	public static final int MAP_MAX_Y = (Config.WORLD_Y_MAX - 17) * TILE_SIZE;

	/** calculated offset used so top left region is 0,0 */
	public static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
	
	/** number of regions */
	private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
	private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
	
	//private FastMap<String, L2PcInstance> _allGms;
	
	/** HashMap(Integer Player id, L2PcInstance) containing all the players in game */
	private L2TIntObjectHashMap<L2PcInstance> _allPlayers;
	
	/** L2ObjectHashMap(L2Object) containing all visible objects */
	private Map<Integer, L2Object> _allObjects;
	
	/** List with the pets instances and their owner id */
	private Map<Integer, L2PetInstance> _petsInstance;
	
	private L2WorldRegion[][] _worldRegions;
	
	/**
	 * Constructor of L2World.<BR><BR>
	 */
	private L2World()
	{
		//_allGms	 = new FastMap<String, L2PcInstance>();
		_allPlayers = new L2TIntObjectHashMap<L2PcInstance>();
		_petsInstance = new FastMap<Integer, L2PetInstance>().shared();
		_allObjects = new FastMap<Integer, L2Object>().shared();
		
		initRegions();
	}
	
	public static L2World getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void storeObject(L2Object object)
	{
		assert !_allObjects.containsKey(object.getObjectId());
		
		if (_allObjects.containsKey(object.getObjectId()))
		{
			_log.warning("[L2World] object: " + object + " already exist in OID map!");
			_log.info(StringUtil.getTraceString(Thread.currentThread().getStackTrace()));
			return;
		}
		
		_allObjects.put(object.getObjectId(), object);
	}
	
	public long timeStoreObject(L2Object object)
	{
		long time = System.nanoTime();
		_allObjects.put(object.getObjectId(), object);
		return System.nanoTime() - time;
	}
	
	public void removeObject(L2Object object)
	{
		_allObjects.remove(Integer.valueOf(object.getObjectId())); // suggestion by whatev
		//IdFactory.getInstance().releaseId(object.getObjectId());
	}
	
	public void removeObjects(List<L2Object> list)
	{
		for (L2Object o : list)
		{
			if (o != null)
				_allObjects.remove(Integer.valueOf(o.getObjectId())); // suggestion by whatev
		}
		//IdFactory.getInstance().releaseId(object.getObjectId());
	}
	
	public void removeObjects(L2Object[] objects)
	{
		for (L2Object o : objects)
			_allObjects.remove(Integer.valueOf(o.getObjectId())); // suggestion by whatev
		//IdFactory.getInstance().releaseId(object.getObjectId());
	}
	
	public long timeRemoveObject(L2Object object)
	{
		long time = System.nanoTime();
		_allObjects.remove(Integer.valueOf(object.getObjectId()));
		return System.nanoTime() - time;
	}
	
	public L2Object findObject(int oID)
	{
		return _allObjects.get(Integer.valueOf(oID));
	}
	
	public long timeFindObject(int objectID)
	{
		long time = System.nanoTime();
		_allObjects.get(Integer.valueOf(objectID));
		return System.nanoTime() - time;
	}

	@Deprecated
	public final Map<Integer, L2Object> getAllVisibleObjects()
	{
		return _allObjects;
	}
	
	public final int getAllVisibleObjectsCount()
	{
		return _allObjects.size();
	}
	
	public FastList<L2PcInstance> getAllGMs()
	{
		return GmListTable.getInstance().getAllGms(true);
	}
	
	public int getAllPlayersCount()
	{
		return _allPlayers.size();
	}
	
	public L2PcInstance getPlayer(String name)
	{
		return getPlayer(CharNameTable.getInstance().getIdByName(name));
	}
	
	public L2PcInstance getPlayer(int playerObjId)
	{
		return _allPlayers.get(Integer.valueOf(playerObjId));
	}

	public L2PetInstance getPet(int ownerId)
	{
		return _petsInstance.get(Integer.valueOf(ownerId));
	}
	
	public L2PetInstance addPet(int ownerId, L2PetInstance pet)
	{
		return _petsInstance.put(ownerId, pet);
	}
	
	public void removePet(int ownerId)
	{
		_petsInstance.remove(Integer.valueOf(ownerId));
	}
	
	public void removePet(L2PetInstance pet)
	{
		_petsInstance.remove(Integer.valueOf(pet.getOwner().getObjectId()));
	}
	
	public void addVisibleObject(L2Object object, L2WorldRegion newRegion)
	{
		// If selected L2Object is a L2PcIntance, add it in L2ObjectHashSet(L2PcInstance) _allPlayers of L2World
		// XXX TODO: this code should be obsoleted by protection in putObject func...
		if (object instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) object;
			if (!player.isTeleporting())
			{
				L2PcInstance tmp = _allPlayers.get(Integer.valueOf(player.getObjectId()));
				if (tmp != null)
				{
					_log.warning("Duplicate character!? Closing both characters (" + player.getName() + ")");
					player.logout();
					tmp.logout();
					return;
				}
				_allPlayers.put(player.getObjectId(), player);
			}
		}
		
		if (!newRegion.isActive())
			return;
		
		// Get all visible objects contained in the _visibleObjects of L2WorldRegions
		// in a circular area of 2000 units
		List<L2Object> visibles = getVisibleObjects(object, 2000);
		if (Config.DEBUG)
			_log.finest("objects in range:" + visibles.size());
		
		// tell the player about the surroundings
		// Go through the visible objects contained in the circular area
		for (L2Object visible : visibles)
		{
			if (visible == null)
				continue;

			// Add the object in L2ObjectHashSet(L2Object) _knownObjects of the visible L2Character according to conditions :
			//   - L2Character is visible
			//   - object is not already known
			//   - object is in the watch distance
			// If L2Object is a L2PcInstance, add L2Object in L2ObjectHashSet(L2PcInstance) _knownPlayer of the visible L2Character
			visible.getKnownList().addKnownObject(object);
			
			// Add the visible L2Object in L2ObjectHashSet(L2Object) _knownObjects of the object according to conditions
			// If visible L2Object is a L2PcInstance, add visible L2Object in L2ObjectHashSet(L2PcInstance) _knownPlayer of the object
			object.getKnownList().addKnownObject(visible);
		}
	}
	
	public void addToAllPlayers(L2PcInstance cha)
	{
		_allPlayers.put(cha.getObjectId(), cha);
	}
	
	public void removeFromAllPlayers(L2PcInstance cha)
	{
		_allPlayers.remove(Integer.valueOf(cha.getObjectId()));
	}
	
	public void removeVisibleObject(L2Object object, L2WorldRegion oldRegion)
	{
		if (object == null)
			return;
		
		//removeObject(object);
		
		if (oldRegion != null)
		{
			// Remove the object from the L2ObjectHashSet(L2Object) _visibleObjects of L2WorldRegion
			// If object is a L2PcInstance, remove it from the L2ObjectHashSet(L2PcInstance) _allPlayers of this L2WorldRegion
			oldRegion.removeVisibleObject(object);
			
			// Go through all surrounding L2WorldRegion L2Characters
			for (L2WorldRegion reg : oldRegion.getSurroundingRegions())
			{
				Collection<L2Object> vObj = reg.getVisibleObjects().values();
				{
					for (L2Object obj : vObj)
					{
						if (obj != null)
						{
							obj.getKnownList().removeKnownObject(object);
							object.getKnownList().removeKnownObject(obj);
						}
					}
				}
			}
			
			// If object is a L2Character :
			// Remove all L2Object from L2ObjectHashSet(L2Object) containing all L2Object detected by the L2Character
			// Remove all L2PcInstance from L2ObjectHashSet(L2PcInstance) containing all player ingame detected by the L2Character
			object.getKnownList().removeAllKnownObjects();
			
			// If selected L2Object is a L2PcIntance, remove it from L2ObjectHashSet(L2PcInstance) _allPlayers of L2World
			if (object instanceof L2PcInstance)
			{
				if (!((L2PcInstance) object).isTeleporting())
					removeFromAllPlayers((L2PcInstance) object);
				
				// If selected L2Object is a GM L2PcInstance, remove it from Set(L2PcInstance) _gmList of GmListTable
				//if (((L2PcInstance)object).isGM())
				//GmListTable.getInstance().deleteGm((L2PcInstance)object);
			}
			
		}
	}
	
	public List<L2Object> getVisibleObjects(L2Object object)
	{
		L2WorldRegion reg = object.getWorldRegion();
		if (reg == null)
			return null;
		
		// Create an FastList in order to contain all visible L2Object
		List<L2Object> result = new ArrayList<L2Object>();
		
		// Go through the FastList of region
		for (L2WorldRegion regi : reg.getSurroundingRegions())
		{
			// Go through visible objects of the selected region
			Collection<L2Object> vObj = regi.getVisibleObjects().values();
			{
				for (L2Object _object : vObj)
				{
					if (_object == null || _object.equals(object))
						continue; // skip our own character
					
					if (!_object.isVisible())
						continue; // skip dying objects
					
					result.add(_object);
				}
			}
		}
		
		return result;
	}
	
	public List<L2Object> getVisibleObjects(L2Object object, int radius)
	{
		if (object == null || !object.isVisible())
			return new ArrayList<L2Object>();
		
		int x = object.getX();
		int y = object.getY();
		int sqRadius = radius * radius;
		
		// Create an FastList in order to contain all visible L2Object
		List<L2Object> result = new ArrayList<L2Object>();
		
		// Go through the FastList of region
		for (L2WorldRegion regi : object.getWorldRegion().getSurroundingRegions())
		{
			// Go through visible objects of the selected region
			Collection<L2Object> vObj = regi.getVisibleObjects().values();
			{
				for (L2Object _object : vObj)
				{
					if (_object == null || _object.equals(object))
						continue; // skip our own character
						
					int x1 = _object.getX();
					int y1 = _object.getY();
					
					double dx = x1 - x;
					double dy = y1 - y;
					
					if (dx * dx + dy * dy < sqRadius)
						result.add(_object);
				}
			}
		}
		
		return result;
	}
	
	public List<L2Object> getVisibleObjects3D(L2Object object, int radius)
	{
		if (object == null || !object.isVisible())
			return new ArrayList<L2Object>();
		
		int x = object.getX();
		int y = object.getY();
		int z = object.getZ();
		int sqRadius = radius * radius;
		
		// Create an FastList in order to contain all visible L2Object
		List<L2Object> result = new ArrayList<L2Object>();
		
		// Go through visible object of the selected region
		for (L2WorldRegion regi : object.getWorldRegion().getSurroundingRegions())
		{
			Collection<L2Object> vObj = regi.getVisibleObjects().values();
			{
				for (L2Object _object : vObj)
				{
					if (_object == null || _object.equals(object))
						continue; // skip our own character
						
					int x1 = _object.getX();
					int y1 = _object.getY();
					int z1 = _object.getZ();
					
					long dx = x1 - x;
					long dy = y1 - y;
					long dz = z1 - z;
					
					if (dx * dx + dy * dy + dz * dz < sqRadius)
						result.add(_object);
				}
			}
		}
		
		return result;
	}
	
	public List<L2Playable> getVisiblePlayable(L2Object object)
	{
		L2WorldRegion reg = object.getWorldRegion();
		if (reg == null)
			return null;
		
		// Create an FastList in order to contain all visible L2Object
		List<L2Playable> result = new ArrayList<L2Playable>();
		
		// Go through the FastList of region
		for (L2WorldRegion regi : reg.getSurroundingRegions())
		{
			// Create an Iterator to go through the visible L2Object of the L2WorldRegion
			Collection<L2Playable> _playables = regi.getVisiblePlayable().values();
			{
				for (L2Playable _object : _playables)
				{
					if (_object == null || _object.equals(object))
						continue; // skip our own character
						
					if (!_object.isVisible()) // GM invisible is different than this...
						continue; // skip dying objects
						
					result.add(_object);
				}
			}
		}
		
		return result;
	}
	
	public L2WorldRegion getRegion(Point3D point)
	{
		return _worldRegions[(point.getX() >> SHIFT_BY) + OFFSET_X][(point.getY() >> SHIFT_BY) + OFFSET_Y];
	}
	
	public L2WorldRegion getRegion(int x, int y)
	{
		return _worldRegions[(x >> SHIFT_BY) + OFFSET_X][(y >> SHIFT_BY) + OFFSET_Y];
	}
	
	public L2WorldRegion[][] getAllWorldRegions()
	{
		return _worldRegions;
	}
	
	private boolean validRegion(int x, int y)
	{
		return (x >= 0 && x <= REGIONS_X && y >= 0 && y <= REGIONS_Y);
	}
	
	private void initRegions()
	{
		_worldRegions = new L2WorldRegion[REGIONS_X + 1][REGIONS_Y + 1];
		
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				_worldRegions[i][j] = new L2WorldRegion(i, j);
			}
		}
		
		for (int x = 0; x <= REGIONS_X; x++)
		{
			for (int y = 0; y <= REGIONS_Y; y++)
			{
				for (int a = -1; a <= 1; a++)
				{
					for (int b = -1; b <= 1; b++)
					{
						if (validRegion(x + a, y + b))
						{
							_worldRegions[x + a][y + b].addSurroundingRegion(_worldRegions[x][y]);
						}
					}
				}
			}
		}
		
		_log.info("L2World: (" + REGIONS_X + " by " + REGIONS_Y + ") World Region Grid set up.");
		
	}
	
	public void deleteVisibleNpcSpawns()
	{
		_log.info("Deleting all visible NPC's.");
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				_worldRegions[i][j].deleteVisibleNpcSpawns();
			}
		}
		_log.info("All visible NPC's deleted.");
	}
	
	public int getDetachedCount()
	{
		int count = 0;
		
		for (final L2PcInstance player : getAllPlayersArray())
		{
			final L2GameClient client = player.getClient();			
			if (client != null && client.isDetached())
				count++;
		}
		
		return count;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final L2World _instance = new L2World();
	}
	
	//Kratei Cube	
	public L2PcInstance findPlayer(int objectId)
	{
		L2Object obj = _allObjects.get(objectId);
		
		if (obj instanceof L2PcInstance)
			return (L2PcInstance) obj;
		
		return null;
	}
	
	public L2TIntObjectHashMap<L2PcInstance> getAllPlayers()
	{
		return _allPlayers;
	}
	
	public final L2PcInstance[] getAllPlayersArray()
	{
		return _allPlayers.getValues(new L2PcInstance[_allPlayers.size()]);
	}
	
	public final boolean forEachPlayer(final TObjectProcedure<L2PcInstance> proc)
	{
		return _allPlayers.forEachValue(proc);
	}
}
