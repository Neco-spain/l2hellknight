package l2rt.gameserver.model;

import l2rt.Config;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.DeleteObject;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.tables.SpawnTable;
import l2rt.gameserver.tables.TerritoryTable;
import l2rt.util.GArray;
import l2rt.util.Location;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Diamond
 * @Date: 15/5/2007
 * @Time: 10:06:34
 */
public final class L2WorldRegion
{
	private L2Object[] _objects = null;
	private GArray<L2Territory> territories = null;
	private int tileX, tileY, tileZ, _objectsSize = 0, _playersSize = 0;
	private boolean _small = false;
	private boolean _spawned = false;
	private final ReentrantLock objects_lock = new ReentrantLock();
	private final ReentrantLock spawn_lock = new ReentrantLock();
	private final ReentrantLock territories_lock = new ReentrantLock();

	public L2WorldRegion(int pTileX, int pTileY, int pTileZ, boolean small)
	{
		tileX = pTileX;
		tileY = pTileY;
		tileZ = pTileZ;
		_small = small;
	}

	private void spawn()
	{
		if(!_spawned && Config.DELAYED_SPAWN)
		{
			spawn_lock.lock();
			try
			{
				if(_spawned)
					return;
				_spawned = true;

				for(L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
				{
					Location loc = null;
					if(spawn.getLocx() == 0)
					{
						if(spawn.getLocation() <= 0)
							continue;
						L2Territory terr = TerritoryTable.getInstance().getLocation(spawn.getLocation());
						if(terr == null)
							continue;
						loc = terr.getCenter();
					}
					else
						loc = spawn.getLoc();

					int x = loc.x / L2World.DIV_BY + L2World.OFFSET_X;
					int y = loc.y / L2World.DIV_BY + L2World.OFFSET_Y;

					if(L2World.validRegion(x, y, 0, false))
					{
						int z = 0;
						if(L2World.getRegions()[x][y].length > 1)
						{
							z = loc.z / L2World.DIV_BY_FOR_Z + L2World.OFFSET_Z;
							if(!L2World.validRegion(x, y, z, false))
								continue;
						}

						if(x == tileX && y == tileY && z == tileZ)
							spawn.init();
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				spawn_lock.unlock();
			}
		}
	}

	public void addToPlayers(L2Object object, L2Character dropper)
	{
		if(_objects == null)
		{
			_objectsSize = 0;
			_playersSize = 0;
			return;
		}

		L2Player player = null;
		if(object.isPlayer())
			player = (L2Player) object;

		// Если object - игрок, показать ему все видимые обьекты в регионе
		if(player != null)
		{
			GArray<L2GameServerPacket> result = new GArray<L2GameServerPacket>(_objectsSize);
			for(L2Object obj : getObjectsList(new GArray<L2Object>(_objectsSize), object.getObjectId(), object.getReflection()))
			{
				// Если это фэйк обсервера - не показывать.
				if(obj.inObserverMode() && !obj.isInOlympiadMode() && (obj.getCurrentRegion() == null || !obj.getCurrentRegion().equals(this)))
					continue;
				result.addAll(player.addVisibleObject(obj, dropper));
			}
			player.sendPackets(result);
		}

		// Показать обьект всем игрокам в регионе
		for(L2Player pl : getPlayersList(new GArray<L2Player>(_playersSize), object.getObjectId(), object.getReflection()))
			pl.sendPackets(pl.addVisibleObject(object, dropper));
	}

	public void removeFromPlayers(L2Object object, boolean move)
	{
		if(_objects == null)
		{
			_objectsSize = 0;
			_playersSize = 0;
			return;
		}

		L2Player player = null;
		if(object.isPlayer())
			player = (L2Player) object;

		// Если object - игрок, убрать у него все видимые обьекты в регионе
		if(player != null)
		{
			GArray<L2GameServerPacket> result = new GArray<L2GameServerPacket>(_objectsSize);
			for(L2Object obj : getObjectsList(new GArray<L2Object>(_objectsSize), object.getObjectId(), object.getReflection()))
			{
				L2GameServerPacket p = player.removeVisibleObject(obj, null, move);
				if(p != null)
					result.add(p);
			}
			player.sendPackets(result);
		}

		DeleteObject p = new DeleteObject(object);
		// Убрать обьект у всех игроков в регионе
		for(L2Player pl : getPlayersList(new GArray<L2Player>(_playersSize), object.getObjectId(), object.getReflection()))
		{
			L2GameServerPacket d = pl.removeVisibleObject(object, p, move);
			if(d != null)
				pl.sendPacket(d);
		}
	}

	public L2Object[] getObjects()
	{
		objects_lock.lock();
		try
		{
			if(_objects == null)
			{
				_objects = new L2Object[50];
				_objectsSize = 0;
				_playersSize = 0;
			}
			return _objects;
		}
		finally
		{
			objects_lock.unlock();
		}
	}

	public void addObject(L2Object obj)
	{
		if(obj == null)
			return;

		objects_lock.lock();
		try
		{
			if(_objects == null)
			{
				_objects = new L2Object[50];
				_objectsSize = 0;
			}
			else if(_objectsSize >= _objects.length)
			{
				L2Object[] temp = new L2Object[_objects.length * 2];
				for(int i = 0; i < _objectsSize; i++)
					temp[i] = _objects[i];
				_objects = temp;
			}

			_objects[_objectsSize] = obj;
			_objectsSize++;

			if(obj.isPlayer())
			{
				_playersSize++;
				for(L2WorldRegion neighbor : getNeighbors())
					neighbor.spawn();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}

		if(obj.isNpc() && obj.getAI() instanceof DefaultAI && obj.getAI().isGlobalAI() && !obj.getAI().isActive())
			obj.getAI().startAITask();
	}

	public void removeObject(L2Object obj, boolean move)
	{
		if(obj == null)
			return;

		objects_lock.lock();
		try
		{
			if(_objects == null)
			{
				_objectsSize = 0;
				_playersSize = 0;
				return;
			}

			if(_objectsSize > 1)
			{
				int k = -1;
				for(int i = 0; i < _objectsSize; i++)
					if(_objects[i] == obj)
					{
						k = i;
						break;
					}
				if(k > -1)
				{
					_objects[k] = _objects[_objectsSize - 1];
					_objects[_objectsSize - 1] = null;
					_objectsSize--;
				}
			}
			else if(_objectsSize == 1 && _objects[0] == obj)
			{
				_objects[0] = null;
				_objects = null;
				_objectsSize = 0;
				_playersSize = 0;
			}

			if(obj.isPlayer())
			{
				_playersSize--;
				if(_playersSize <= 0)
					_playersSize = 0;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}

		if(!move && obj.isNpc() && obj.getAI() instanceof DefaultAI && !obj.getAI().isGlobalAI())
			obj.getAI().stopAITask();
	}

	public GArray<L2Object> getObjectsList(GArray<L2Object> result, int exclude, Reflection reflection)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; i++)
			{
				L2Object obj = _objects[i];
				if(obj != null && obj.getObjectId() != exclude && (reflection.getId() == -1 || obj.getReflection() == reflection))
					result.add(obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public GArray<L2Object> getObjectsList(GArray<L2Object> result, int exclude, Reflection reflection, int x, int y, int z, long sqrad, int height)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; i++)
			{
				L2Object obj = _objects[i];
				if(obj == null || obj.getObjectId() == exclude || reflection.getId() != -1 && obj.getReflection() != reflection)
					continue;
				if(Math.abs(obj.getZ() - z) > height)
					continue;
				long dx = obj.getX() - x;
				dx *= dx;
				if(dx > sqrad)
					continue;
				long dy = obj.getY() - y;
				dy *= dy;
				if(dx + dy < sqrad)
					result.add(obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public GArray<L2Character> getCharactersList(GArray<L2Character> result, int exclude, long reflection)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; i++)
			{
				L2Object obj = _objects[i];
				if(obj != null && obj.isCharacter() && obj.getObjectId() != exclude && (reflection == -1 || obj.getReflection().getId() == reflection))
					result.add((L2Character) obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public GArray<L2Character> getCharactersList(GArray<L2Character> result, int exclude, long reflection, int x, int y, int z, long sqrad, int height)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; i++)
			{
				L2Object obj = _objects[i];
				if(obj == null || !obj.isCharacter() || obj.getObjectId() == exclude || reflection != -1 && obj.getReflection().getId() != reflection)
					continue;
				if(Math.abs(obj.getZ() - z) > height)
					continue;
				long dx = obj.getX() - x;
				dx *= dx;
				if(dx > sqrad)
					continue;
				long dy = obj.getY() - y;
				dy *= dy;
				if(dx + dy < sqrad)
					result.add((L2Character) obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public GArray<L2NpcInstance> getNpcsList(GArray<L2NpcInstance> result, int exclude, long reflection)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; i++)
			{
				L2Object obj = _objects[i];
				if(obj != null && obj.isNpc() && obj.getObjectId() != exclude && (reflection == -1 || obj.getReflection().getId() == reflection))
					result.add((L2NpcInstance) obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public GArray<L2NpcInstance> getNpcsList(GArray<L2NpcInstance> result, int exclude, long reflection, int x, int y, int z, long sqrad, int height)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; i++)
			{
				L2Object obj = _objects[i];
				if(obj == null || !obj.isNpc() || obj.getObjectId() == exclude || reflection != -1 && obj.getReflection().getId() != reflection)
					continue;
				if(Math.abs(obj.getZ() - z) > height)
					continue;
				long dx = obj.getX() - x;
				dx *= dx;
				if(dx > sqrad)
					continue;
				long dy = obj.getY() - y;
				dy *= dy;
				if(dx + dy < sqrad)
					result.add((L2NpcInstance) obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public GArray<L2Player> getPlayersList(GArray<L2Player> result, int exclude, Reflection reflection)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; i++)
			{
				L2Object obj = _objects[i];
				if(obj != null && obj.isPlayer() && obj.getObjectId() != exclude && (reflection.getId() == -1 || obj.getReflection() == reflection))
					result.add((L2Player) obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public GArray<L2Player> getPlayersList(GArray<L2Player> result, int exclude, Reflection reflection, int x, int y, int z, long sqrad, int height)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; i++)
			{
				L2Object obj = _objects[i];
				if(obj == null || !obj.isPlayer() || obj.getObjectId() == exclude || reflection.getId() != -1 && obj.getReflection().getId() != reflection.getId())
					continue;
				if(Math.abs(obj.getZ() - z) > height)
					continue;
				long dx = obj.getX() - x;
				dx *= dx;
				if(dx > sqrad)
					continue;
				long dy = obj.getY() - y;
				dy *= dy;
				if(dx + dy < sqrad)
					result.add((L2Player) obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public GArray<L2Playable> getPlayablesList(GArray<L2Playable> result, int exclude, Reflection reflection)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; i++)
			{
				L2Object obj = _objects[i];
				if(obj != null && obj.isPlayable() && obj.getObjectId() != exclude && (reflection.getId() == -1 || obj.getReflection() == reflection))
					result.add((L2Playable) obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public GArray<L2Playable> getPlayablesList(GArray<L2Playable> result, int exclude, Reflection reflection, int x, int y, int z, long sqrad, int height)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; i++)
			{
				L2Object obj = _objects[i];
				if(obj == null || !obj.isPlayable() || obj.getObjectId() == exclude || reflection.getId() != -1 && obj.getReflection().getId() != reflection.getId())
					continue;
				if(Math.abs(obj.getZ() - z) > height)
					continue;
				long dx = obj.getX() - x;
				dx *= dx;
				if(dx > sqrad)
					continue;
				long dy = obj.getY() - y;
				dy *= dy;
				if(dx + dy < sqrad)
					result.add((L2Playable) obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	/**
	 * Удаляет весь видимый спаун из региона.
	 */
	public void deleteVisibleNpcSpawns()
	{
		GArray<L2NpcInstance> toRemove = new GArray<L2NpcInstance>(_objectsSize);

		objects_lock.lock();
		try
		{
			if(_objects != null)
				for(int i = 0; i < _objectsSize; i++)
				{
					L2Object obj = _objects[i];
					if(obj != null && obj.isNpc())
						toRemove.add((L2NpcInstance) obj);
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}

		for(L2NpcInstance npc : toRemove)
		{
			L2Spawn spawn = npc.getSpawn();
			if(spawn != null)
			{
				npc.deleteMe();
				spawn.stopRespawn();
			}
		}
	}

	/**
	 * Показывает игроку все видимые обьекты в регионе
	 */
	public void showObjectsToPlayer(L2Player player)
	{
		if(player != null && _objects != null)
		{
			GArray<L2GameServerPacket> result = new GArray<L2GameServerPacket>(_objectsSize);
			for(L2Object obj : getObjectsList(new GArray<L2Object>(_objectsSize), player.getObjectId(), player.getReflection()))
				result.addAll(player.addVisibleObject(obj, null));
			player.sendPackets(result);
		}
	}

	/**
	 * Убирает у игрока все видимые обьекты в регионе
	 */
	public void removeObjectsFromPlayer(L2Player player)
	{
		if(player != null && _objects != null)
		{
			GArray<L2GameServerPacket> result = new GArray<L2GameServerPacket>(_objectsSize);
			for(L2Object obj : getObjectsList(new GArray<L2Object>(_objectsSize), player.getObjectId(), player.getReflection()))
			{
				L2GameServerPacket p = player.removeVisibleObject(obj, null, true);
				if(p != null)
					result.add(p);
			}
			player.sendPackets(result);
		}
	}

	/**
	 * Убирает обьект у всех игроков в регионе
	 */
	public void removePlayerFromOtherPlayers(L2Object object)
	{
		if(object != null && _objects != null)
		{
			DeleteObject p = new DeleteObject(object);
			for(L2Player pl : getPlayersList(new GArray<L2Player>(_playersSize), object.getObjectId(), object.getReflection()))
			{
				L2GameServerPacket d = pl.removeVisibleObject(object, p, true);
				if(d != null)
					pl.sendPacket(d);
			}
		}
	}

	public boolean areNeighborsEmpty()
	{
		if(!isEmpty())
			return false;
		for(L2WorldRegion neighbor : getNeighbors())
			if(!neighbor.isEmpty())
				return false;
		return true;
	}

	public GArray<L2WorldRegion> getNeighbors()
	{
		return L2World.getNeighbors(tileX, tileY, tileZ, _small);
	}

	public int getObjectsSize()
	{
		return _objectsSize;
	}

	public int getPlayersSize()
	{
		return _playersSize;
	}

	public boolean isEmpty()
	{
		return _playersSize <= 0;
	}

	public boolean isNull()
	{
		return _objectsSize <= 0;
	}

	public String getName()
	{
		return "(" + tileX + ", " + tileY + ", " + tileZ + ")";
	}

	public void addTerritory(L2Territory territory)
	{
		territories_lock.lock();
		try
		{
			if(territories == null)
				territories = new GArray<L2Territory>(5);
			if(!territories.contains(territory))
				territories.add(territory);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			territories_lock.unlock();
		}
	}

	public void removeTerritory(L2Territory territory)
	{
		territories_lock.lock();
		try
		{
			if(territories == null)
				return;
			territories.remove(territory);
			if(territories.isEmpty())
				territories = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			territories_lock.unlock();
		}
	}

	public GArray<L2Territory> getTerritories(int x, int y, int z)
	{
		territories_lock.lock();
		try
		{
			if(territories == null)
				return null;
			GArray<L2Territory> result = new GArray<L2Territory>(territories.size());
			for(L2Territory terr : territories)
				if(terr != null && terr.isInside(x, y, z))
					result.add(terr);
			return result.isEmpty() ? null : result;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			territories_lock.unlock();
		}
		return null;
	}
}