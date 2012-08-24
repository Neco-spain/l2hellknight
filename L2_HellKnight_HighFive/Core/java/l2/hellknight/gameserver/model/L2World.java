/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2.hellknight.gameserver.model;

import gnu.trove.procedure.TObjectProcedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2.hellknight.Config;
import l2.hellknight.gameserver.datatables.AdminTable;
import l2.hellknight.gameserver.datatables.CharNameTable;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PetInstance;
import l2.hellknight.gameserver.util.L2TIntObjectHashMap;
import l2.hellknight.gameserver.util.Point3D;
import l2.hellknight.util.StringUtil;

/**
 * This class ...
 *
 * @version $Revision: 1.21.2.5.2.7 $ $Date: 2005/03/27 15:29:32 $
 */
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
	
	/*
	 * biteshift, defines number of regions
	 * note, shifting by 15 will result in regions corresponding to map tiles
	 * shifting by 12 divides one tile to 8x8 regions
	 */
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
	private final L2TIntObjectHashMap<L2PcInstance> _allPlayers;
	
	/** L2ObjectHashMap(L2Object) containing all visible objects */
	private final L2TIntObjectHashMap<L2Object> _allObjects;
	private final L2TIntObjectHashMap<String> _allObjectsDebug;
	
	/** List with the pets instances and their owner id */
	private final L2TIntObjectHashMap<L2PetInstance> _petsInstance;
	
	private L2WorldRegion[][] _worldRegions;
	
	/**
	 * Constructor of L2World.<BR><BR>
	 */
	protected L2World()
	{
		_allPlayers = new L2TIntObjectHashMap<>();
		_allObjects = new L2TIntObjectHashMap<>();
		_allObjectsDebug = new L2TIntObjectHashMap<>();
		_petsInstance = new L2TIntObjectHashMap<>();
		
		initRegions();
	}
	
	/**
	 * @return the current instance of L2World.
	 */
	public static L2World getInstance()
	{
		return SingletonHolder._instance;
	}
	
	/**
	 * Add L2Object object in _allObjects.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Withdraw an item from the warehouse, create an item</li>
	 * <li> Spawn a L2Character (PC, NPC, Pet)</li><BR>
	 * @param object 
	 */
	public void storeObject(L2Object object)
	{
		if (_allObjects.containsKey(object.getObjectId()))
		{
			_log.log(Level.WARNING, "--------[L2World] object: " + object + " already exist in OID map!--------");
			_log.log(Level.WARNING, "New object: " + StringUtil.getTraceString(Thread.currentThread().getStackTrace()));
			_log.log(Level.WARNING, "----------------- Previous Put -----------------");
			_log.log(Level.WARNING, "Previous: " + _allObjectsDebug.get(object.getObjectId()));
			_log.log(Level.WARNING, "---------------------- End ---------------------");
			return;
		}
		
		_allObjects.put(object.getObjectId(), object);
		_allObjectsDebug.put(object.getObjectId(), StringUtil.getTraceString(Thread.currentThread().getStackTrace()));
	}
	
	/**
	 * Remove L2Object object from _allObjects of L2World.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Delete item from inventory, tranfer Item from inventory to warehouse</li>
	 * <li> Crystallize item</li>
	 * <li> Remove NPC/PC/Pet from the world</li><BR>
	 *
	 * @param object L2Object to remove from _allObjects of L2World
	 *
	 */
	public void removeObject(L2Object object)
	{
		_allObjects.remove(object.getObjectId()); // suggestion by whatev
		_allObjectsDebug.remove(object.getObjectId());
	}
	
	public void removeObjects(List<L2Object> list)
	{
		for (L2Object o : list)
		{
			if (o != null)
			{
				_allObjects.remove(o.getObjectId()); // suggestion by whatev
				_allObjectsDebug.remove(o.getObjectId());
			}
		}
	}
	
	public void removeObjects(L2Object[] objects)
	{
		for (L2Object o : objects)
		{
			_allObjects.remove(o.getObjectId()); // suggestion by whatev
			_allObjectsDebug.remove(o.getObjectId());
		}
	}
	
	/**
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packets : Action, AttackRequest, RequestJoinParty, RequestJoinPledge...</li><BR>
	 *
	 * @param oID Identifier of the L2Object
	 * @return the L2Object object that belongs to an ID or null if no object found.
	 */
	public L2Object findObject(int oID)
	{
		return _allObjects.get(oID);
	}
	
	public final L2Object[] getAllVisibleObjectsArray()
	{
		return _allObjects.values(new L2Object[0]);
	}
	
	public final boolean forEachObject(final TObjectProcedure<L2Object> proc)
	{
		return _allObjects.forEachValue(proc);
	}
	
	/**
	 * Get the count of all visible objects in world.<br><br>
	 * 
	 * @return count off all L2World objects
	 */
	public final int getAllVisibleObjectsCount()
	{
		return _allObjects.size();
	}
	
	/**
	 * @return a table containing all GMs.
	 */
	public List<L2PcInstance> getAllGMs()
	{
		return AdminTable.getInstance().getAllGms(true);
	}
	
	public L2TIntObjectHashMap<L2PcInstance> getAllPlayers()
	{
		return _allPlayers;
	}
	
	public final L2PcInstance[] getAllPlayersArray()
	{
		return _allPlayers.values(new L2PcInstance[0]);
	}
	
	public final boolean forEachPlayer(final TObjectProcedure<L2PcInstance> proc)
	{
		return _allPlayers.forEachValue(proc);
	}
	
	/**
	 * Return how many players are online.<BR><BR>
	 *
	 * @return number of online players.
	 */
	public int getAllPlayersCount()
	{
		return _allPlayers.size();
	}
	
	/**
	 * <B>If you have access to player objectId use {@link #getPlayer(int playerObjId)}</B>
	 * @param name Name of the player to get Instance
	 * @return the player instance corresponding to the given name.
	 */
	public L2PcInstance getPlayer(String name)
	{
		return getPlayer(CharNameTable.getInstance().getIdByName(name));
	}
	
	/**
	 * @param playerObjId Object ID of the player to get Instance
	 * @return the player instance corresponding to the given object ID.
	 */
	public L2PcInstance getPlayer(int playerObjId)
	{
		return _allPlayers.get(playerObjId);
	}
	
	/**
	 * @param ownerId ID of the owner
	 * @return the pet instance from the given ownerId.
	 */
	public L2PetInstance getPet(int ownerId)
	{
		return _petsInstance.get(ownerId);
	}
	
	/**
	 * Add the given pet instance from the given ownerId.<BR><BR>
	 *
	 * @param ownerId ID of the owner
	 * @param pet L2PetInstance of the pet
	 * @return 
	 */
	public L2PetInstance addPet(int ownerId, L2PetInstance pet)
	{
		return _petsInstance.put(ownerId, pet);
	}
	
	/**
	 * Remove the given pet instance.<BR><BR>
	 *
	 * @param ownerId ID of the owner
	 */
	public void removePet(int ownerId)
	{
		_petsInstance.remove(ownerId);
	}
	
	/**
	 * Remove the given pet instance.<BR><BR>
	 *
	 * @param pet the pet to remove
	 */
	public void removePet(L2PetInstance pet)
	{
		_petsInstance.remove(pet.getOwner().getObjectId());
	}
	
	/**
	 * Add a L2Object in the world.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * L2Object (including L2PcInstance) are identified in <B>_visibleObjects</B> of his current L2WorldRegion and in <B>_knownObjects</B> of other surrounding L2Characters <BR>
	 * L2PcInstance are identified in <B>_allPlayers</B> of L2World, in <B>_allPlayers</B> of his current L2WorldRegion and in <B>_knownPlayer</B> of other surrounding L2Characters <BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the L2Object object in _allPlayers* of L2World </li>
	 * <li>Add the L2Object object in _gmList** of GmListTable </li>
	 * <li>Add object in _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters </li><BR>
	 *
	 * <li>If object is a L2Character, add all surrounding L2Object in its _knownObjects and all surrounding L2PcInstance in its _knownPlayer </li><BR>
	 *
	 * <I>*  only if object is a L2PcInstance</I><BR>
	 * <I>** only if object is a GM L2PcInstance</I><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object in _visibleObjects and _allPlayers* of L2WorldRegion (need synchronisation)</B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects and _allPlayers* of L2World (need synchronisation)</B></FONT><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Drop an Item </li>
	 * <li> Spawn a L2Character</li>
	 * <li> Apply Death Penalty of a L2PcInstance </li><BR><BR>
	 *
	 * @param object L2object to add in the world
	 * @param newRegion L2WorldRegion in wich the object will be add (not used)
	 */
	public void addVisibleObject(L2Object object, L2WorldRegion newRegion)
	{
		// If selected L2Object is a L2PcIntance, add it in L2ObjectHashSet(L2PcInstance) _allPlayers of L2World
		// XXX TODO: this code should be obsoleted by protection in putObject func...
		if (object instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) object;
			
			if (!player.isTeleporting())
			{
				L2PcInstance tmp = getPlayer(player.getObjectId());
				if (tmp != null)
				{
					_log.warning("Duplicate character!? Closing both characters (" + player.getName() + ")");
					player.logout();
					tmp.logout();
					return;
				}
				addToAllPlayers(player);
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
	
	/**
	 * Add the L2PcInstance to _allPlayers of L2World.<BR><BR>
	 * @param cha 
	 */
	public void addToAllPlayers(L2PcInstance cha)
	{
		_allPlayers.put(cha.getObjectId(), cha);
	}
	
	/**
	 * Remove the L2PcInstance from _allPlayers of L2World.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Remove a player from the visible objects </li><BR>
	 * @param cha 
	 */
	public void removeFromAllPlayers(L2PcInstance cha)
	{
		_allPlayers.remove(cha.getObjectId());
	}
	
	/**
	 * Remove a L2Object from the world.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * L2Object (including L2PcInstance) are identified in <B>_visibleObjects</B> of his current L2WorldRegion and in <B>_knownObjects</B> of other surrounding L2Characters <BR>
	 * L2PcInstance are identified in <B>_allPlayers</B> of L2World, in <B>_allPlayers</B> of his current L2WorldRegion and in <B>_knownPlayer</B> of other surrounding L2Characters <BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2Object object from _allPlayers* of L2World </li>
	 * <li>Remove the L2Object object from _visibleObjects and _allPlayers* of L2WorldRegion </li>
	 * <li>Remove the L2Object object from _gmList** of GmListTable </li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters </li><BR>
	 *
	 * <li>If object is a L2Character, remove all L2Object from its _knownObjects and all L2PcInstance from its _knownPlayer </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World</B></FONT><BR><BR>
	 *
	 * <I>*  only if object is a L2PcInstance</I><BR>
	 * <I>** only if object is a GM L2PcInstance</I><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Pickup an Item </li>
	 * <li> Decay a L2Character</li><BR><BR>
	 *
	 * @param object L2object to remove from the world
	 * @param oldRegion L2WorldRegion in which the object was before removing
	 *
	 */
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
				for (L2Object obj : vObj)
				{
					if (obj != null)
					{
						obj.getKnownList().removeKnownObject(object);
						object.getKnownList().removeKnownObject(obj);
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
	
	/**
	 * Return all visible objects of the L2WorldRegion object's and of its surrounding L2WorldRegion.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All visible object are identified in <B>_visibleObjects</B> of their current L2WorldRegion <BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Find Close Objects for L2Character </li><BR>
	 *
	 * @param object L2object that determine the current L2WorldRegion
	 * @return 
	 */
	public List<L2Object> getVisibleObjects(L2Object object)
	{
		L2WorldRegion reg = object.getWorldRegion();
		
		if (reg == null)
			return null;
		
		// Create an FastList in order to contain all visible L2Object
		List<L2Object> result = new ArrayList<>();
		
		// Go through the FastList of region
		for (L2WorldRegion regi : reg.getSurroundingRegions())
		{
			// Go through visible objects of the selected region
			Collection<L2Object> vObj = regi.getVisibleObjects().values();
			for (L2Object _object : vObj)
			{
				if (_object == null || _object.equals(object))
					continue; // skip our own character
				else if (!_object.isVisible())
					continue; // skip dying objects
				result.add(_object);
			}
		}
		
		return result;
	}
	
	/**
	 * Return all visible objects of the L2WorldRegions in the circular area (radius) centered on the object.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All visible object are identified in <B>_visibleObjects</B> of their current L2WorldRegion <BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Define the aggrolist of monster </li>
	 * <li> Define visible objects of a L2Object </li>
	 * <li> Skill : Confusion... </li><BR>
	 *
	 * @param object L2object that determine the center of the circular area
	 * @param radius Radius of the circular area
	 * @return 
	 */
	public List<L2Object> getVisibleObjects(L2Object object, int radius)
	{
		if (object == null || !object.isVisible())
			return new ArrayList<>();
		
		int x = object.getX();
		int y = object.getY();
		int sqRadius = radius * radius;
		
		// Create an FastList in order to contain all visible L2Object
		List<L2Object> result = new ArrayList<>();
		
		// Go through the FastList of region
		for (L2WorldRegion regi : object.getWorldRegion().getSurroundingRegions())
		{
			// Go through visible objects of the selected region
			Collection<L2Object> vObj = regi.getVisibleObjects().values();
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
		
		return result;
	}
	
	/**
	 * Return all visible objects of the L2WorldRegions in the spheric area (radius) centered on the object.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All visible object are identified in <B>_visibleObjects</B> of their current L2WorldRegion <BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Define the target list of a skill </li>
	 * <li> Define the target list of a polearme attack </li><BR><BR>
	 *
	 * @param object L2object that determine the center of the circular area
	 * @param radius Radius of the spheric area
	 * @return 
	 */
	public List<L2Object> getVisibleObjects3D(L2Object object, int radius)
	{
		if (object == null || !object.isVisible())
			return new ArrayList<>();
		
		int x = object.getX();
		int y = object.getY();
		int z = object.getZ();
		int sqRadius = radius * radius;
		
		// Create an FastList in order to contain all visible L2Object
		List<L2Object> result = new ArrayList<>();
		
		// Go through visible object of the selected region
		for (L2WorldRegion regi : object.getWorldRegion().getSurroundingRegions())
		{
			Collection<L2Object> vObj = regi.getVisibleObjects().values();
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
		
		return result;
	}
	
	/**
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All visible object are identified in <B>_visibleObjects</B> of their current L2WorldRegion <BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Find Close Objects for L2Character </li><BR>
	 *
	 * @param object L2object that determine the current L2WorldRegion
	 * @return all visible players of the L2WorldRegion object's and of its surrounding L2WorldRegion.
	 */
	public List<L2Playable> getVisiblePlayable(L2Object object)
	{
		L2WorldRegion reg = object.getWorldRegion();
		
		if (reg == null)
			return null;
		
		// Create an FastList in order to contain all visible L2Object
		List<L2Playable> result = new ArrayList<>();
		
		// Go through the FastList of region
		for (L2WorldRegion regi : reg.getSurroundingRegions())
		{
			// Create an Iterator to go through the visible L2Object of the L2WorldRegion
			Map<Integer, L2Playable> _allpls = regi.getVisiblePlayable();
			Collection<L2Playable> _playables = _allpls.values();
			// Go through visible object of the selected region
			for (L2Playable _object : _playables)
			{
				if (_object == null || _object.equals(object))
					continue; // skip our own character
					
				if (!_object.isVisible()) // GM invisible is different than this...
					continue; // skip dying objects
					
				result.add(_object);
			}
		}
		
		return result;
	}
	
	/**
	 * Calculate the current L2WorldRegions of the object according to its position (x,y).<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Set position of a new L2Object (drop, spawn...) </li>
	 * <li> Update position of a L2Object after a movement </li><BR>
	 *
	 * @param point position of the object
	 * @return 
	 */
	public L2WorldRegion getRegion(Point3D point)
	{
		return _worldRegions[(point.getX() >> SHIFT_BY) + OFFSET_X][(point.getY() >> SHIFT_BY) + OFFSET_Y];
	}
	
	public L2WorldRegion getRegion(int x, int y)
	{
		return _worldRegions[(x >> SHIFT_BY) + OFFSET_X][(y >> SHIFT_BY) + OFFSET_Y];
	}
	
	/**
	 * Returns the whole 2d array containing the world regions
	 * used by ZoneData.java to setup zones inside the world regions
	 * @return
	 */
	public L2WorldRegion[][] getAllWorldRegions()
	{
		return _worldRegions;
	}
	
	/**
	 * Check if the current L2WorldRegions of the object is valid according to its position (x,y).<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Init L2WorldRegions </li><BR>
	 *
	 * @param x X position of the object
	 * @param y Y position of the object
	 *
	 * @return True if the L2WorldRegion is valid
	 */
	private boolean validRegion(int x, int y)
	{
		return (x >= 0 && x <= REGIONS_X && y >= 0 && y <= REGIONS_Y);
	}
	
	/**
	 * Init each L2WorldRegion and their surrounding table.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All surrounding L2WorldRegion are identified in <B>_surroundingRegions</B> of the selected L2WorldRegion in order to scan a large area around a L2Object<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Constructor of L2World </li><BR>
	 *
	 */
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
	
	/**
	 * Deleted all spawns in the world.
	 */
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
	
	private static class SingletonHolder
	{
		protected static final L2World _instance = new L2World();
	}
}
