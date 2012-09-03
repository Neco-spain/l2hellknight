package l2rt.gameserver.model;

import l2rt.Config;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.instances.L2PetInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.util.GArray;
import l2rt.util.StrTable;

import java.util.logging.Logger;

/**
 * Общее "супер" хранилище для всех объектов L2Object,
 * объекты делятся на типы - максимум 32 (0 - 31), для каждого типа свое хранилище,
 * в каждом хранилище может хранится до 67 108 864 объектов (0 - 67108863),
 * каждому объекту назначается уникальный 64-битный индентификатор типа long
 * который бинарно складывается из objId + тип + индекс в хранилище
 * 
 * @author Drin & Diamond
 */
public class L2ObjectsStorage
{
	@SuppressWarnings("unused")
	private static final Logger _log = Logger.getLogger(L2ObjectsStorage.class.getName());

	private static final int STORAGE_PLAYERS = 0x00;
	private static final int STORAGE_SUMMONS = 0x01;
	private static final int STORAGE_NPCS = 0x02;
	private static final int STORAGE_ITEMS = 0x03;
	/** ......................................... */
	private static final int STORAGE_OTHER = 0x1E;
	private static final int STORAGE_NONE = 0x1F;
	// Чесно говоря херь эт какае то)
	// TODO упростить, почистить нах
	private static final L2ObjectArray[] storages = new L2ObjectArray[STORAGE_NONE];
	
	/*private final L2TIntObjectHashMap<L2Player> _allPlayers;
	private final L2TIntObjectHashMap<L2Playable> _allPlayers;
	private final L2TIntObjectHashMap<L2NpcInstance> _allPlayers;
	private final L2TIntObjectHashMap<L2ItemInstance> _allPlayers;
	private final L2TIntObjectHashMap<L2Object> _allPlayers;*/

	static
	{
		storages[STORAGE_PLAYERS] = new L2ObjectArray<L2Player>("PLAYERS", Config.MAXIMUM_ONLINE_USERS, 1);
		storages[STORAGE_SUMMONS] = new L2ObjectArray<L2Playable>("SUMMONS", Config.MAXIMUM_ONLINE_USERS, 1);
		storages[STORAGE_NPCS] = new L2ObjectArray<L2NpcInstance>("NPCS", 60000, 5000);
		storages[STORAGE_ITEMS] = new L2ObjectArray<L2ItemInstance>("ITEMS", Config.MAXIMUM_ONLINE_USERS * Config.INVENTORY_MAXIMUM_NO_DWARF / 3, Config.MAXIMUM_ONLINE_USERS);
		storages[STORAGE_OTHER] = new L2ObjectArray<L2Object>("OTHER", 2000, 1000);
	}

	@SuppressWarnings("unchecked")
	private static L2ObjectArray<L2Player> getStoragePlayers()
	{
		return storages[STORAGE_PLAYERS];
	}

	@SuppressWarnings( { "unchecked", "unused" })
	private static L2ObjectArray<L2Playable> getStorageSummons()
	{
		return storages[STORAGE_SUMMONS];
	}

	@SuppressWarnings("unchecked")
	private static L2ObjectArray<L2NpcInstance> getStorageNpcs()
	{
		return storages[STORAGE_NPCS];
	}

	@SuppressWarnings( { "unchecked" })
	private static L2ObjectArray<L2ItemInstance> getStorageItems()
	{
		return storages[STORAGE_ITEMS];
	}

	@SuppressWarnings( { "unchecked" })
	private static L2ObjectArray<L2Object> getStorageOther()
	{
		return storages[STORAGE_OTHER];
	}

	private static int selectStorageID(L2Object o)
	{
		if(o.isItem())
			return STORAGE_ITEMS;

		if(o.isNpc())
			return STORAGE_NPCS;

		if(o.isPlayable())
			return o.isPlayer() ? STORAGE_PLAYERS : STORAGE_SUMMONS;

		return STORAGE_OTHER;
	}

	public static L2Object get(long storedId)
	{
		int STORAGE_ID;
		if(storedId == 0 || (STORAGE_ID = getStorageID(storedId)) == STORAGE_NONE)
			return null;
		L2Object result = storages[STORAGE_ID].get(getStoredIndex(storedId));
		return result != null && result.getObjectId() == getStoredObjectId(storedId) ? result : null;
	}

	public static L2Object get(Long storedId)
	{
		int STORAGE_ID;
		if(storedId == null || storedId == 0 || (STORAGE_ID = getStorageID(storedId)) == STORAGE_NONE)
			return null;
		L2Object result = storages[STORAGE_ID].get(getStoredIndex(storedId));
		return result != null && result.getObjectId() == getStoredObjectId(storedId) ? result : null;
	}

	public static boolean isStored(long storedId)
	{
		int STORAGE_ID;
		if(storedId == 0 || (STORAGE_ID = getStorageID(storedId)) == STORAGE_NONE)
			return false;
		L2Object o = storages[STORAGE_ID].get(getStoredIndex(storedId));
		return o != null && o.getObjectId() == getStoredObjectId(storedId);
	}

	public static L2NpcInstance getAsNpc(long storedId)
	{
		return (L2NpcInstance) get(storedId);
	}

	public static L2NpcInstance getAsNpc(Long storedId)
	{
		return (L2NpcInstance) get(storedId);
	}

	public static L2Player getAsPlayer(long storedId)
	{
		return (L2Player) get(storedId);
	}

	public static L2Playable getAsPlayable(long storedId)
	{
		return (L2Playable) get(storedId);
	}

	public static L2Character getAsCharacter(long storedId)
	{
		return (L2Character) get(storedId);
	}

	public static L2MonsterInstance getAsMonster(long storedId)
	{
		return (L2MonsterInstance) get(storedId);
	}

	public static L2PetInstance getAsPet(long storedId)
	{
		return (L2PetInstance) get(storedId);
	}

	public static L2ItemInstance getAsItem(long storedId)
	{
		return (L2ItemInstance) get(storedId);
	}

	public static boolean contains(long storedId)
	{
		return get(storedId) != null;
	}

	public static L2Player getPlayer(String name)
	{
		return getStoragePlayers().findByName(name);
	}

	public static L2Player getPlayer(int objId)
	{
		return getStoragePlayers().findByObjectId(objId);
	}

	/**
	 * копия списка игроков из хранилища, пригодна для последующих манипуляций над ней
	 * для перебора в основном лучше использовать getAllPlayersForIterate()
	 */
	public static GArray<L2Player> getAllPlayers()
	{
		return getStoragePlayers().getAll();
	}

	/**
	 * использовать только для перебора типа for(L2Player player : getAllPlayersForIterate()) ...
	 */
	public static Iterable<L2Player> getAllPlayersForIterate()
	{
		return getStoragePlayers();
	}

	/**
	 * Возвращает онлайн с офф трейдерами, но без накрутки.
	 */
	public static int getAllPlayersCount()
	{
		return getStoragePlayers().getRealSize();
	}

	public static int getAllObjectsCount()
	{
		int result = 0;
		for(L2ObjectArray<?> storage : storages)
			if(storage != null)
				result += storage.getRealSize();
		return result;
	}

	@SuppressWarnings( { "unchecked" })
	public static GArray<L2Object> getAllObjects()
	{
		GArray<L2Object> result = new GArray<L2Object>(getAllObjectsCount());
		for(L2ObjectArray storage : storages)
			if(storage != null)
				storage.getAll(result);
		return result;
	}

	public static L2Object findObject(int objId)
	{
		L2Object result = null;
		for(L2ObjectArray<?> storage : storages)
			if(storage != null)
				if((result = storage.findByObjectId(objId)) != null)
					return result;
		return null;
	}

	private static long offline_refresh = 0;
	private static int offline_count = 0;

	public static int getAllOfflineCount()
	{
		if(!Config.SERVICES_OFFLINE_TRADE_ALLOW)
			return 0;

		long now = System.currentTimeMillis();
		if(now > offline_refresh)
		{
			offline_refresh = now + 10000;
			offline_count = 0;
			for(L2Player player : getStoragePlayers())
				if(player.isInOfflineMode())
					offline_count++;
		}

		return offline_count;
	}

	public static GArray<Reflection> getAllReflections()
	{
		GArray<Reflection> result = new GArray<Reflection>();
		for(L2Object obj : getStorageOther())
			if(obj instanceof Reflection)
				result.add((Reflection) obj);
		return result;
	}

	public static GArray<L2NpcInstance> getAllNpcs()
	{
		return getStorageNpcs().getAll();
	}

	/**
	 * использовать только для перебора типа for(L2Player player : getAllPlayersForIterate()) ...
	 */
	public static Iterable<L2NpcInstance> getAllNpcsForIterate()
	{
		return getStorageNpcs();
	}

	public static L2ItemInstance getItemByObjId(int objId)
	{
		return getStorageItems().findByObjectId(objId);
	}

	public static L2NpcInstance getByNpcId(int npc_id)
	{
		L2NpcInstance result = null;
		for(L2NpcInstance temp : getStorageNpcs())
			if(npc_id == temp.getNpcId())
			{
				if(!temp.isDead())
					return temp;
				result = temp;
			}
		return result;
	}

	public static GArray<L2NpcInstance> getAllByNpcId(int npc_id, boolean justAlive)
	{
		GArray<L2NpcInstance> result = new GArray<L2NpcInstance>(0);
		for(L2NpcInstance temp : getStorageNpcs())
			if(temp.getTemplate() != null && npc_id == temp.getTemplate().getNpcId() && (!justAlive || !temp.isDead()))
				result.add(temp);
		return result;
	}

	public static GArray<L2NpcInstance> getAllByNpcId(int[] npc_ids, boolean justAlive)
	{
		GArray<L2NpcInstance> result = new GArray<L2NpcInstance>(0);
		for(L2NpcInstance temp : getStorageNpcs())
			if(!justAlive || !temp.isDead())
				for(int npc_id : npc_ids)
					if(npc_id == temp.getNpcId())
						result.add(temp);
		return result;
	}

	public static L2NpcInstance getNpc(String s)
	{
		GArray<L2NpcInstance> npcs = getStorageNpcs().findAllByName(s);
		if(npcs.size() == 0)
			return null;
		for(L2NpcInstance temp : npcs)
			if(!temp.isDead())
				return temp;
		return npcs.removeLast();
	}

	public static L2NpcInstance getNpc(int objId)
	{
		return getStorageNpcs().findByObjectId(objId);
	}

	/**
	 * кладет объект в хранилище и возвращает уникальный индентификатор по которому его можно будет найти в хранилище
	 */
	@SuppressWarnings("unchecked")
	public static long put(L2Object o)
	{
		int STORAGE_ID = selectStorageID(o);
		return o.getObjectId() & 0xFFFFFFFFL | (STORAGE_ID & 0x1FL) << 32 | (storages[STORAGE_ID].add(o) & 0xFFFFFFFFL) << 37;
	}

	public static long putDummy(L2Object o)
	{
		return objIdNoStore(o.getObjectId());
	}

	/**
	 * генерирует уникальный индентификатор по которому будет ясно что объект вне хранилища но можно будет получить objectId
	 */
	public static long objIdNoStore(int objId)
	{
		return objId & 0xFFFFFFFFL | (STORAGE_NONE & 0x1FL) << 32;
	}

	/**
	 * пересчитывает StoredId, необходимо при изменении ObjectId
	 */
	public static long refreshId(L2Object o)
	{
		return o.getObjectId() & 0xFFFFFFFFL | o.getStoredId() >> 32 << 32;
	}

	public static L2Object remove(long storedId)
	{
		int STORAGE_ID = getStorageID(storedId);
		return STORAGE_ID == STORAGE_NONE ? null : storages[STORAGE_ID].remove(getStoredIndex(storedId), getStoredObjectId(storedId));
	}

	private static int getStorageID(long storedId)
	{
		return (int) (storedId >> 32) & 0x1F;
	}

	private static int getStoredIndex(long storedId)
	{
		return (int) (storedId >> 37);
	}

	public static int getStoredObjectId(long storedId)
	{
		return (int) storedId;
	}

	public static StrTable getStats()
	{
		StrTable table = new StrTable("L2 Objects Storage Stats");

		L2ObjectArray<?> storage;
		for(int i = 0; i < storages.length; i++)
		{
			if((storage = storages[i]) == null)
				continue;

			synchronized (storage)
			{
				table.set(i, "Name", storage.name);
				table.set(i, "Size / Real", storage.size() + " / " + storage.getRealSize());
				table.set(i, "Capacity / init", storage.capacity() + " / " + storage.initCapacity);
			}
		}

		return table;
	}
}