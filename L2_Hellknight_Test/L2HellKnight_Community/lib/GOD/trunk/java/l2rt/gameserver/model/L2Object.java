package l2rt.gameserver.model;

import l2rt.extensions.listeners.MethodInvokeListener;
import l2rt.extensions.listeners.PropertyChangeListener;
import l2rt.extensions.listeners.engine.DefaultListenerEngine;
import l2rt.extensions.listeners.engine.ListenerEngine;
import l2rt.extensions.listeners.events.L2Object.TerritoryChangeEvent;
import l2rt.extensions.listeners.events.MethodEvent;
import l2rt.extensions.listeners.events.PropertyEvent;
import l2rt.extensions.scripts.Events;
import l2rt.extensions.scripts.Script;
import l2rt.extensions.scripts.ScriptObject;
import l2rt.extensions.scripts.Scripts;
import l2rt.gameserver.ai.L2CharacterAI;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.instancemanager.FortressSiegeManager;
import l2rt.gameserver.instancemanager.MercTicketManager;
import l2rt.gameserver.instancemanager.QuestManager;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.entity.vehicle.L2AirShip;
import l2rt.gameserver.model.entity.vehicle.L2Ship;
import l2rt.gameserver.model.entity.vehicle.L2Vehicle;
import l2rt.gameserver.model.instances.*;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.tables.ReflectionTable;
import l2rt.gameserver.tables.TerritoryTable;
import l2rt.util.GArray;
import l2rt.util.GCSArray;
import l2rt.util.Location;
import l2rt.util.Util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public abstract class L2Object
{
	private static final Logger _log = Logger.getLogger(L2Object.class.getName());

	public static final int POLY_NONE = 0;
	public static final int POLY_NPC = 1;
	public static final int POLY_ITEM = 2;

	protected long _reflection = Long.MIN_VALUE;

	/** Object identifier */
	protected int _objectId;
	protected Long _storedId;

	/** Object location : Used for items/chars that are seen in the world */
	private int _x;
	private int _y;
	private int _z;

	private int _poly_id;

	private GArray<L2Territory> _territories = null;
	private final ReentrantLock territoriesLock = new ReentrantLock();

	private GCSArray<L2Zone> _zones = null;

	/** Object visibility */
	protected boolean _hidden;

	/**
	 * Constructor<?> of L2Object.<BR><BR>
	 * @param objectId этого объекта
	 */
	public L2Object(Integer objectId, boolean putInStorage)
	{
		_objectId = objectId;
		_storedId = putInStorage ? L2ObjectsStorage.put(this) : L2ObjectsStorage.putDummy(this);
	}

	public L2Object(Integer objectId)
	{
		this(objectId, objectId > 0);
	}

	public static Object callScriptsNoOwner(Script scriptClass, Method method)
	{
		return callScriptsNoOwner(scriptClass, method, null, null);
	}

	public static Object callScriptsNoOwner(Script scriptClass, Method method, Object[] args)
	{
		return callScriptsNoOwner(scriptClass, method, args, null);
	}

	public static Object callScriptsNoOwner(Script scriptClass, Method method, Object[] args, HashMap<String, Object> variables)
	{
		if(l2rt.extensions.scripts.Scripts.loading)
			return null;

		ScriptObject o;
		try
		{
			o = scriptClass.newInstance();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

		if(variables != null && variables.size() > 0)
			for(Map.Entry<String, Object> obj : variables.entrySet())
				try
				{
					o.setProperty(obj.getKey(), obj.getValue());
				}
				catch(Exception e)
				{}

		try
		{
			o.setProperty("self", null);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return args == null ? o.invokeMethod(method) : o.invokeMethod(method, args);
	}

	public Object callScripts(Script scriptClass, Method method)
	{
		return callScripts(scriptClass, method, null, null);
	}

	public Object callScripts(Script scriptClass, Method method, Object[] args)
	{
		return callScripts(scriptClass, method, args, null);
	}

	public Object callScripts(Script scriptClass, Method method, Object[] args, HashMap<String, Object> variables)
	{
		if(l2rt.extensions.scripts.Scripts.loading)
			return null;

		ScriptObject o;
		try
		{
			o = scriptClass.newInstance();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

		if(variables != null && variables.size() > 0)
			for(Map.Entry<String, Object> obj : variables.entrySet())
				try
				{
					o.setProperty(obj.getKey(), obj.getValue());
				}
				catch(Exception e)
				{}

		try
		{
			if(o.isFunctions())
				o.setProperty("self", getStoredId());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		Object ret = args == null ? o.invokeMethod(method) : o.invokeMethod(method, args);

		try
		{
			if(o.isFunctions())
				o.setProperty("self", null);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return ret;
	}

	public Object callScripts(String _class, String method)
	{
		return callScripts(_class, method, null, null);
	}

	public Object callScripts(String _class, String method, Object[] args)
	{
		return callScripts(_class, method, args, null);
	}

	public Object callScripts(String _class, String method, Object[] args, HashMap<String, Object> variables)
	{
		if(l2rt.extensions.scripts.Scripts.loading)
			return null;

		ScriptObject o;

		Script scriptClass = Scripts.getInstance().getClasses().get(_class);

		if(scriptClass == null)
		{
			_log.info("Script class " + _class + " not found");
			return null;
		}

		try
		{
			o = scriptClass.newInstance();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

		if(variables != null && variables.size() > 0)
			for(Map.Entry<String, Object> obj : variables.entrySet())
				try
				{
					o.setProperty(obj.getKey(), obj.getValue());
				}
				catch(Exception e)
				{}

		try
		{
			o.setProperty("self", getStoredId());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		Object ret = args == null ? o.invokeMethod(method) : o.invokeMethod(method, args);

		try
		{
			o.setProperty("self", null);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return ret;
	}

	public Reflection getReflection()
	{
		Reflection result = ReflectionTable.getInstance().get(_reflection);
		if(result == null)
		{
			result = ReflectionTable.getInstance().getDefault();
			_reflection = result.getId();
		}
		return result;
	}

	public long getReflectionId()
	{
		return _reflection;
	}

	public void setReflection(long i)
	{
		if(_reflection == i)
			return;

		boolean blink = false;
		if(!_hidden)
		{
			decayMe();
			blink = true;
		}

		if(_reflection > 0)
			getReflection().removeObject(this);
		_reflection = i;
		if(_reflection > 0)
			getReflection().addObject(this);

		if(blink)
			spawnMe();
	}

	public final void setReflection(Reflection i)
	{
		setReflection(i.getId());
	}

	/**
	 * Return the identifier of the L2Object.<BR><BR>
	 *
	 * @ - deprecated?
	 */
	@Override
	public final int hashCode()
	{
		return _objectId;
	}

	public final int getObjectId()
	{
		return _objectId;
	}

	public final Long getStoredId()
	{
		return _storedId;
	}

	public int getX()
	{
		return _x;
	}

	public int getY()
	{
		return _y;
	}

	public int getZ()
	{
		return _z;
	}

	/**
	 * Возвращает позицию (x, y, z, heading)
	 * @return Location
	 */
	public Location getLoc()
	{
		return new Location(_x, _y, _z, getHeading());
	}

	public int getGeoZ(Location loc)
	{
		if(isFlying() || isInWater() || isInVehicle() || isVehicle() || isDoor())
			return loc.z;
		if(isNpc())
		{
			L2Spawn spawn = ((L2NpcInstance) this).getSpawn();
			if(spawn != null && spawn.getLocx() == 0 && spawn.getLocy() == 0)
				return GeoEngine.getHeight(loc, getReflection().getGeoIndex());
			return loc.z;
		}
		return GeoEngine.getHeight(loc, getReflection().getGeoIndex());
	}

	public void setPolyInfo(int polytype, int polyid)
	{
		_poly_id = (polytype << 24) + polyid;
		if(isPlayer())
		{
			L2Player cha = (L2Player) this;
			cha.teleToLocation(getLoc());
			cha.broadcastUserInfo(true);
		}
		else
		{
			decayMe();
			spawnMe(getLoc());
		}
	}

	public boolean isPolymorphed()
	{
		return _poly_id != 0;
	}

	public int getPolytype()
	{
		return _poly_id >> 24;
	}

	public int getPolyid()
	{
		return _poly_id & 0xFFFFFF;
	}

	/**
	 * Устанавливает позицию (x, y, z) L2Object
	 * @param loc Location
	 */
	public void setLoc(Location loc, boolean MoveTask)
	{
		setXYZ(loc.x, loc.y, loc.z, MoveTask);
	}

	/**
	 * Устанавливает позицию (x, y, z) L2Object
	 * @param loc Location
	 */
	public void setLoc(Location loc)
	{
		setXYZ(loc.x, loc.y, loc.z);
	}

	public void setXYZ(int x, int y, int z)
	{
		setXYZ(x, y, z, false);
	}

	/**
	 * Set the x,y,z position of the L2Object and if necessary modify its _worldRegion.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Update position during and after movement, or after teleport </li><BR>
	 *
	 * @param x new x coord
	 * @param y new y coord
	 * @param z new z coord
	 */
	public void setXYZ(int x, int y, int z, boolean MoveTask)
	{
		if(!L2World.validCoords(x, y))
			if(isPlayer())
			{
				_log.warning("Player " + this + " (" + _objectId + ") at bad coords: (" + getX() + ", " + getY() + ").");
				L2Player player = (L2Player) this;
				player.abortAttack(true, true);
				player.abortCast(true);
				player.sendActionFailed();
				player.teleToClosestTown();
				return;
			}
			else if(isNpc())
			{
				L2Spawn spawn = ((L2NpcInstance) this).getSpawn();
				if(spawn == null)
					return;
				if(spawn.getLocx() != 0)
				{
					x = spawn.getLocx();
					y = spawn.getLocy();
					z = spawn.getLocz();
				}
				else
				{
					int p[] = TerritoryTable.getInstance().getRandomPoint(spawn.getLocation());
					x = p[0];
					y = p[1];
					z = p[2];
				}
			}
			else if(isCharacter())
			{
				decayMe();
				return;
			}

		_x = x;
		_y = y;
		_z = z;

		L2World.addVisibleObject(this, null);

		if(isCharacter())
			updateTerritories();
	}

	public void updateTerritories()
	{
		GArray<L2Territory> current_territories = L2World.getTerritories(getX(), getY(), getZ());
		GArray<L2Territory> new_territories = null;
		GArray<L2Territory> old_territories = null;

		territoriesLock.lock();
		try
		{
			if(_territories == null)
				new_territories = current_territories;
			else
			{
				if(current_territories != null)
					for(L2Territory terr : current_territories)
						if(!_territories.contains(terr))
						{
							if(new_territories == null)
								new_territories = new GArray<L2Territory>();
							new_territories.add(terr);
						}

				if(_territories.size() > 0)
					for(L2Territory terr : _territories)
						if(current_territories == null || !current_territories.contains(terr))
						{
							if(old_territories == null)
								old_territories = new GArray<L2Territory>();
							old_territories.add(terr);
						}
			}

			if(current_territories != null && current_territories.size() > 0)
				_territories = current_territories;
			else
				_territories = null;
		}
		finally
		{
			territoriesLock.unlock();
		}

		if(old_territories != null)
			for(L2Territory terr : old_territories)
				if(terr != null)
					terr.doLeave(this, true);

		if(new_territories != null)
			for(L2Territory terr : new_territories)
				if(terr != null)
					terr.doEnter(this);

		firePropertyChanged(new TerritoryChangeEvent(old_territories, new_territories, this));
	}

	/**
	 * Set the x,y,z position of the L2Object and make it invisible.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Object is invisble if <B>_hidden</B> = true<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Create a Door</li>
	 * <li> Restore L2Player</li><BR>
	 *
	 * @param x new x coord
	 * @param y new y coord
	 * @param z new z coord
	 */
	public void setXYZInvisible(int x, int y, int z)
	{
		if(x > L2World.MAP_MAX_X)
			x = L2World.MAP_MAX_X - 5000;
		if(x < L2World.MAP_MIN_X)
			x = L2World.MAP_MIN_X + 5000;
		if(y > L2World.MAP_MAX_Y)
			y = L2World.MAP_MAX_Y - 5000;
		if(y < L2World.MAP_MIN_Y)
			y = L2World.MAP_MIN_Y + 5000;

		if(z < -32768 || z > 32768)
			z = 0;

		_x = x;
		_y = y;
		_z = z;

		_hidden = true;
	}

	public void setXYZInvisible(Location loc)
	{
		setXYZInvisible(loc.x, loc.y, loc.z);
	}

	/**
	 * Return the visibility state of the L2Object. <BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Object is invisible if <B>_hidden</B>=true or <B>_worldregion</B>==null <BR><BR>
	 *
	 * @return true if visible
	 */
	public final boolean isVisible()
	{
		return !_hidden;
	}

	public boolean isInvisible()
	{
		return false;
	}

	/**
	 * Init a dropped L2ItemInstance and add it in the world as a visible object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion </li>
	 * <li>Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion</li>
	 * <li>Add the L2ItemInstance dropped in the world as a <B>visible</B> object</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects of L2World </B></FONT><BR><BR>
	 *
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> this instanceof L2ItemInstance</li>
	 * <li> _worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Drop item</li>
	 * <li> Call Pet</li><BR>
	 *
	 * @param dropper Char that dropped item
	 * @param loc drop coordinates
	 */
	public void dropMe(L2Character dropper, Location loc)
	{
		if(dropper != null)
			setReflection(dropper.getReflection());

		// Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion
		_hidden = false;

		_x = loc.x;
		_y = loc.y;
		_z = getGeoZ(loc);

		L2World.addVisibleObject(this, dropper);
	}

	public final void spawnMe(Location loc)
	{
		if(loc.x > L2World.MAP_MAX_X)
			loc.x = L2World.MAP_MAX_X - 5000;
		if(loc.x < L2World.MAP_MIN_X)
			loc.x = L2World.MAP_MIN_X + 5000;
		if(loc.y > L2World.MAP_MAX_Y)
			loc.y = L2World.MAP_MAX_Y - 5000;
		if(loc.y < L2World.MAP_MIN_Y)
			loc.y = L2World.MAP_MIN_Y + 5000;

		_x = loc.x;
		_y = loc.y;
		_z = getGeoZ(loc);
		if(loc.h > 0)
			setHeading(loc.h);

		spawnMe();
	}

	/**
	 * Добавляет обьект в мир, добавляет в текущий регион. Делает обьект видимым.
	 */
	public void spawnMe()
	{
		// Set the x,y,z position of the L2Object spawn and update its _worldregion
		_hidden = false;

		L2World.addVisibleObject(this, null);

		if(isCharacter())
			updateTerritories();
	}

	public void toggleVisible()
	{
		if(isVisible())
			decayMe();
		else
			spawnMe();
	}

	/**
	 * Do Nothing.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2Summon :  Reset isShowSpawnAnimation flag</li>
	 * <li> L2NpcInstance    :  Reset some flags</li><BR><BR>
	 *
	 */
	public void onSpawn()
	{}

	public final boolean pickupMe(L2Character target)
	{
		// Create a server->client GetItem packet to pick up the L2ItemInstance
		//player.broadcastPacket(new GetItem((L2ItemInstance) this, player.getObjectId()));

		// if this item is a mercenary ticket, remove the spawns!
		if(isItem())
		{
			L2ItemInstance item = (L2ItemInstance) this;
			int itemId = item.getItemId();
			/*if(itemId >= 3960 && itemId <= 3972 // Gludio
			 || itemId >= 3973 && itemId <= 3985 // Dion
			 || itemId >= 3986 && itemId <= 3998 // Giran
			 || itemId >= 3999 && itemId <= 4011 // Oren
			 || itemId >= 4012 && itemId <= 4026 // Aden
			 || itemId >= 5205 && itemId <= 5215 // Innadril
			 || itemId >= 6779 && itemId <= 6833 // Goddard
			 || itemId >= 7973 && itemId <= 8029 // Rune
			 || itemId >= 7918 && itemId <= 7972 // Schuttgart
			 )*/
			if(itemId >= 3960 && itemId <= 4026 || itemId >= 5205 && itemId <= 5214 || itemId >= 6038 && itemId <= 6306 || itemId >= 6779 && itemId <= 6833 || itemId >= 7918 && itemId <= 8029)
				MercTicketManager.getInstance().removeTicket(item);

			if(target != null && target.isPlayer())
			{
				L2Player player = (L2Player) target;

				if(item.getItem().isCombatFlag() && !FortressSiegeManager.checkIfCanPickup(player))
					return false;
				if(item.getItem().isTerritoryFlag())
					return false;

				if(itemId == 57 || itemId == 6353)
				{
					Quest q = QuestManager.getQuest(255);
					if(q != null)
						player.processQuestEvent(q.getName(), "CE" + itemId, null);
				}
			}
			else if(FortressSiegeManager.isCombatFlag(itemId))
				return false;
		}

		// Remove the L2ItemInstance from the world
		_hidden = true;
		L2World.removeVisibleObject(this);

		return true;
	}

	/**
	 * Удаляет обьект из текущего региона, делая его невидимым.
	 * Не путать с deleteMe. Объект после decayMe подлежит реюзу через spawnMe.
	 * Если перепутать будет утечка памяти.
	 */
	public void decayMe()
	{
		_hidden = true;
		L2World.removeVisibleObject(this);
	}

	/**
	 * Удаляет объект из мира вообще.
	 * Не путать с decayMe. Обьект после deleteMe подлежит сборке мусора.
	 * Если попытаться использовать объект после deleteMe будет большая фигня!!!
	 */
	public void deleteMe()
	{
		decayMe();
		L2World.removeObject(this);
		L2ObjectsStorage.remove(_storedId);
	}

	public void onAction(L2Player player, boolean shift)
	{
		if(Events.onAction(player, this, shift))
			return;

		player.sendActionFailed();
	}

	public void onForcedAttack(L2Player player, boolean shift)
	{
		player.sendActionFailed();
	}

	public boolean isAttackable(L2Character attacker)
	{
		return false;
	}

	public abstract boolean isAutoAttackable(L2Character attacker);

	public boolean isMarker()
	{
		return false;
	}

	public String getL2ClassShortName()
	{
		return getClass().getName().replaceAll("^.*\\.(.*?)$", "$1");
	}

	public final long getXYDeltaSq(int x, int y)
	{
		long dx = x - getX();
		long dy = y - getY();
		return dx * dx + dy * dy;
	}

	public final long getXYDeltaSq(Location loc)
	{
		return getXYDeltaSq(loc.x, loc.y);
	}

	public final long getZDeltaSq(int z)
	{
		long dz = z - getZ();
		return dz * dz;
	}

	public final long getZDeltaSq(Location loc)
	{
		return getZDeltaSq(loc.z);
	}

	public final long getXYZDeltaSq(int x, int y, int z)
	{
		return getXYDeltaSq(x, y) + getZDeltaSq(z);
	}

	public final long getXYZDeltaSq(Location loc)
	{
		return getXYDeltaSq(loc.x, loc.y) + getZDeltaSq(loc.z);
	}

	public final double getDistance(int x, int y)
	{
		return Math.sqrt(getXYDeltaSq(x, y));
	}

	public final double getDistance(int x, int y, int z)
	{
		return Math.sqrt(getXYZDeltaSq(x, y, z));
	}

	public final double getDistance(Location loc)
	{
		return getDistance(loc.x, loc.y, loc.z);
	}

	/**
	 * Проверяет в досягаемости расстояния ли объект
	 * @param obj проверяемый объект
	 * @param range расстояние
	 * @return true, если объект досягаем
	 */
	public final boolean isInRange(L2Object obj, long range)
	{
		if(obj == null)
			return false;
		long dx = Math.abs(obj.getX() - getX());
		if(dx > range)
			return false;
		long dy = Math.abs(obj.getY() - getY());
		if(dy > range)
			return false;
		long dz = Math.abs(obj.getZ() - getZ());
		return dz <= 1500 && dx * dx + dy * dy <= range * range;
	}

	public final boolean isInRangeZ(L2Object obj, long range)
	{
		if(obj == null)
			return false;
		long dx = Math.abs(obj.getX() - getX());
		if(dx > range)
			return false;
		long dy = Math.abs(obj.getY() - getY());
		if(dy > range)
			return false;
		long dz = Math.abs(obj.getZ() - getZ());
		return dz <= range && dx * dx + dy * dy + dz * dz <= range * range;
	}

	public final boolean isInRange(Location loc, long range)
	{
		return isInRangeSq(loc, range * range);
	}

	public final boolean isInRangeSq(Location loc, long range)
	{
		return getXYDeltaSq(loc) <= range;
	}

	public final boolean isInRangeZ(Location loc, long range)
	{
		return isInRangeZSq(loc, range * range);
	}

	public final boolean isInRangeZSq(Location loc, long range)
	{
		return getXYZDeltaSq(loc) <= range;
	}

	public final double getDistance(L2Object obj)
	{
		if(obj == null)
			return 0;
		return Math.sqrt(getXYDeltaSq(obj.getX(), obj.getY()));
	}

	public final double getDistance3D(L2Object obj)
	{
		if(obj == null)
			return 0;
		return Math.sqrt(getXYZDeltaSq(obj.getX(), obj.getY(), obj.getZ()));
	}

	public final double getRealDistance(L2Object obj)
	{
		return getRealDistance3D(obj, true);
	}

	public final double getRealDistance3D(L2Object obj)
	{
		return getRealDistance3D(obj, false);
	}

	public final double getRealDistance3D(L2Object obj, boolean ignoreZ)
	{
		double distance = ignoreZ ? getDistance(obj) : getDistance3D(obj);
		if(isCharacter())
			distance -= ((L2Character) this).getTemplate().collisionRadius;
		if(obj.isCharacter())
			distance -= ((L2Character) obj).getTemplate().collisionRadius;
		return distance > 0 ? distance : 0;
	}

	public final long getSqDistance(int x, int y)
	{
		return getXYDeltaSq(x, y);
	}

	public final long getSqDistance(L2Object obj)
	{
		if(obj == null)
			return 0;
		return getXYDeltaSq(obj.getLoc());
	}

	/**
	 * Возвращает L2Player управляющий даным обьектом.<BR>
	 * <li>Для L2Player это сам игрок.</li>
	 * <li>Для L2Summon это его хозяин.</li><BR><BR>
	 * @return L2Player управляющий даным обьектом.
	 */
	public L2Player getPlayer()
	{
		return null;
	}

	public int getHeading()
	{
		return 0;
	}

	public float getMoveSpeed()
	{
		return 0;
	}

	public boolean isInZonePeace()
	{
		return isInZone(ZoneType.peace_zone) && !isInZoneBattle();
	}

	public boolean isInZoneBattle()
	{
		return isInZone(ZoneType.battle_zone) || isInZone(ZoneType.OlympiadStadia);
	}

	public boolean isInZoneOlympiad()
	{
		return isInZone(ZoneType.OlympiadStadia);
	}

	public boolean isInZoneWater()
	{
		return isInZone(ZoneType.water) && !isInZone(ZoneType.no_water) && !isInVehicle();
	}

	public boolean isInWater()
	{
		return isPlayer() && ((L2Player) this).getWaterTask() != null;
	}

	public boolean isSwimming()
	{
		return getWaterZ() != Integer.MIN_VALUE;
	}

	public void addZone(L2Zone zone)
	{
		if(_zones == null)
			_zones = new GCSArray<L2Zone>(3);
		_zones.add(zone);
	}

	public void removeZone(L2Zone zone)
	{
		if(_zones == null)
			return;
		_zones.remove(zone);
	}

	public boolean isInZone(ZoneType type)
	{
		if(_zones == null)
			return false;
		for(L2Zone z : _zones)
			if(z != null && z.getType() == type)
				return true;
		return false;
	}

	public boolean isInZone(L2Zone zone)
	{
		if(_zones == null)
			return false;
		for(L2Zone z : _zones)
			if(z == zone)
				return true;
		return false;
	}

	/** Возвращает координаты поверхности воды, если мы находимся в ней, или над ней. */
	public int getWaterZ()
	{
		if(_zones == null || isInZone(ZoneType.no_water) || !isPlayer() || isInVehicle() || isVehicle() || isFlying())
			return Integer.MIN_VALUE;

		int z = GeoEngine.getHeight(getLoc(), getReflection().getGeoIndex()) + 1;
		int water_z = Integer.MIN_VALUE;

		GArray<L2Territory> terrlist = L2World.getTerritories(getX(), getY(), z);
		if(terrlist != null)
			for(L2Territory terr : terrlist)
				if(terr != null && terr.getZone() != null && terr.getZone().getType() == ZoneType.water && (water_z == Integer.MIN_VALUE || water_z < terr.getZmax()))
					water_z = terr.getZmax();

		return water_z;
	}

	public L2Zone getZone(ZoneType type)
	{
		if(_zones == null)
			return null;
		for(L2Zone z : _zones)
			if(z != null && z.getType() == type)
				return z;
		return null;
	}

	public boolean isActionBlocked(String action)
	{
		if(_zones == null)
			return false;
		for(L2Zone z : _zones)
			if(z != null && z.getType() == ZoneType.unblock_actions && z.isActionBlocked(action))
				return false;
		for(L2Zone z : _zones)
			if(z != null && z.getType() != ZoneType.unblock_actions && z.isActionBlocked(action))
				return true;
		return false;
	}

	public void clearTerritories()
	{
		territoriesLock.lock();
		try
		{
			if(_territories != null)
				for(L2Territory t : _territories)
					if(t != null)
						t.doLeave(this, false);
			_territories = null;
		}
		finally
		{
			territoriesLock.unlock();
		}
	}

	L2WorldRegion _currentRegion;

	public L2WorldRegion getCurrentRegion()
	{
		return _currentRegion;
	}

	public void setCurrentRegion(L2WorldRegion region)
	{
		_currentRegion = region;
	}

	public boolean hasAI()
	{
		return false;
	}

	public L2CharacterAI getAI()
	{
		return null;
	}

	public boolean inObserverMode()
	{
		return false;
	}

	public boolean isInOlympiadMode()
	{
		return false;
	}

	public void startAttackStanceTask()
	{}

	public boolean isInVehicle()
	{
		return false;
	}

	public boolean isFlying()
	{
		return false;
	}

	public float getColRadius()
	{
		_log.warning("getColRadius called directly from L2Object");
		Thread.dumpStack();
		return 0;
	}

	public float getColHeight()
	{
		_log.warning("getColHeight called directly from L2Object");
		Thread.dumpStack();
		return 0;
	}

	public void setHeading(int heading)
	{}

	// --------------------------- Listeners system test -----------------------------
	private DefaultListenerEngine<L2Object> listenerEngine;

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		getListenerEngine().addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		getListenerEngine().removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String value, PropertyChangeListener listener)
	{
		getListenerEngine().addPropertyChangeListener(value, listener);
	}

	public void removePropertyChangeListener(String value, PropertyChangeListener listener)
	{
		getListenerEngine().removePropertyChangeListener(value, listener);
	}

	public void firePropertyChanged(String value, Object oldValue, Object newValue)
	{
		getListenerEngine().firePropertyChanged(value, this, oldValue, newValue);
	}

	public void firePropertyChanged(PropertyEvent event)
	{
		getListenerEngine().firePropertyChanged(event);
	}

	public void addProperty(String property, Object value)
	{
		getListenerEngine().addProperty(property, value);
	}

	public Object getProperty(String property)
	{
		return getListenerEngine().getProperty(property);
	}

	public void addMethodInvokeListener(MethodInvokeListener listener)
	{
		getListenerEngine().addMethodInvokedListener(listener);
	}

	public void addMethodInvokeListener(String methodName, MethodInvokeListener listener)
	{
		getListenerEngine().addMethodInvokedListener(methodName, listener);
	}

	public void removeMethodInvokeListener(MethodInvokeListener listener)
	{
		getListenerEngine().removeMethodInvokedListener(listener);
	}

	public void removeMethodInvokeListener(String methodName, MethodInvokeListener listener)
	{
		getListenerEngine().removeMethodInvokedListener(methodName, listener);
	}

	public void fireMethodInvoked(MethodEvent event)
	{
		getListenerEngine().fireMethodInvoked(event);
	}

	public void fireMethodInvoked(String methodName, Object[] args)
	{
		getListenerEngine().fireMethodInvoked(methodName, this, args);
	}

	public ListenerEngine<L2Object> getListenerEngine()
	{
		if(listenerEngine == null)
			listenerEngine = new DefaultListenerEngine<L2Object>(this);
		return listenerEngine;
	}

	// ------------------------- Listeners system test end ---------------------------

	@Override
	protected void finalize()
	{
		getReflection().removeObject(this);
	}

	public boolean isCharacter()
	{
		return this instanceof L2Character;
	}

	public boolean isPlayable()
	{
		return this instanceof L2Playable;
	}

	public boolean isPlayer()
	{
		return this instanceof L2Player;
	}

	public boolean isPet()
	{
		return this instanceof L2PetInstance;
	}

	public boolean isSummon()
	{
		return this instanceof L2SummonInstance;
	}

	public boolean isMonster()
	{
		return this instanceof L2MonsterInstance;
	}

	public boolean isNpc()
	{
		return this instanceof L2NpcInstance;
	}

	public boolean isItem()
	{
		return this instanceof L2ItemInstance;
	}

	public boolean isRaid()
	{
		return this instanceof L2RaidBossInstance && !(this instanceof L2ReflectionBossInstance);
	}

	public boolean isBoss()
	{
		return this instanceof L2BossInstance;
	}

	public boolean isTrap()
	{
		return this instanceof L2TrapInstance;
	}

	public boolean isDoor()
	{
		return this instanceof L2DoorInstance;
	}

	public boolean isArtefact()
	{
		return this instanceof L2ArtefactInstance;
	}

	public boolean isSiegeGuard()
	{
		return this instanceof L2SiegeGuardInstance;
	}

	public boolean isVehicle()
	{
		return this instanceof L2Vehicle;
	}

	public boolean isShip()
	{
		return this instanceof L2Ship;
	}

	public boolean isAirShip()
	{
		return this instanceof L2AirShip;
	}

	public boolean isMinion()
	{
		return this instanceof L2MinionInstance;
	}

	public String getName()
	{
		return getClass().getSimpleName() + ":" + _objectId;
	}

	public String dump()
	{
		return dump(true);
	}

	public String dump(boolean simpleTypes)
	{
		return Util.dumpObject(this, simpleTypes, true, true);
	}
}