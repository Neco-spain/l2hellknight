package l2rt.extensions.scripts;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.ServerVariables;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.instances.L2DoorInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExShowTrace;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.network.serverpackets.NpcSay;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.MapRegion;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.ReflectionTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Strings;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * @Author: Diamond
 * @Date: 7/6/2007
 * @Time: 5:22:23
 */
public class Functions
{
	public Long self, npc;

	/**
	 * Вызывает метод с задержкой
	 * @param object - от чьего имени вызывать
	 * @param sClass<?> - вызываемый класс
	 * @param sMethod - вызываемый метод
	 * @param args - массив аргуметов
	 * @param variables - список выставляемых переменных
	 * @param delay - задержка в миллисекундах
	 */
	public static ScheduledFuture<?> executeTask(final L2Object object, final String sClass, final String sMethod, final Object[] args, final HashMap<String, Object> variables, long delay)
	{
		return ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(){
			@Override
			public void run()
			{
				if(object != null)
					object.callScripts(sClass, sMethod, args, variables);
			}
		}, delay);
	}

	public static ScheduledFuture<?> executeTask(final String sClass, final String sMethod, final Object[] args, final HashMap<String, Object> variables, long delay)
	{
		return ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(){
			@Override
			public void run()
			{
				callScripts(sClass, sMethod, args, variables);
			}
		}, delay);
	}

	public static ScheduledFuture<?> executeTask(final L2Object object, final String sClass, final String sMethod, final Object[] args, long delay)
	{
		return executeTask(object, sClass, sMethod, args, null, delay);
	}

	public static ScheduledFuture<?> executeTask(final String sClass, final String sMethod, final Object[] args, long delay)
	{
		return executeTask(sClass, sMethod, args, null, delay);
	}

	public static Object callScripts(String _class, String method, Object[] args)
	{
		return callScripts(_class, method, args, null);
	}

	public static Object callScripts(String _class, String method, Object[] args, HashMap<String, Object> variables)
	{
		if(l2rt.extensions.scripts.Scripts.loading)
			return null;

		ScriptObject o;

		Script scriptClass = Scripts.getInstance().getClasses().get(_class);

		if(scriptClass == null)
			return null;

		try
		{
			o = scriptClass.newInstance();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

		if(variables != null)
			for(Map.Entry<String, Object> obj : variables.entrySet())
				try
				{
					o.setProperty(obj.getKey(), obj.getValue());
				}
				catch(Exception e)
				{}

		return o.invokeMethod(method, args);
	}

	/** Вызывать только из скриптов */
	public void show(String text, L2Player self)
	{
		show(text, self, getNpc());
	}

	/** Статический метод, для вызова из любых мест */
	public static void show(String text, L2Player self, L2NpcInstance npc)
	{
		if(text == null || self == null)
			return;

		NpcHtmlMessage msg = new NpcHtmlMessage(self, npc);

		// Не указываем явно язык
		if(text.endsWith(".html-ru") || text.endsWith(".htm-ru"))
			text = text.substring(0, text.length() - 3);

		// приводим нашу html-ку в нужный вид
		if(text.endsWith(".html") || text.endsWith(".htm"))
			msg.setFile(text);
		else
			msg.setHtml(Strings.bbParse(text));
		self.sendPacket(msg);
	}

	public static void show(CustomMessage message, L2Player self)
	{
		show(message.toString(), self, null);
	}

	public static void sendMessage(String text, L2Player self)
	{
		self.sendMessage(text);
	}

	public static void sendMessage(CustomMessage message, L2Player self)
	{
		self.sendMessage(message);
	}

	// Белый чат
	public static void npcSayInRange(L2NpcInstance npc, String text, int range)
	{
		if(npc == null)
			return;
		NpcSay cs = new NpcSay(npc, 0, text);
		for(L2Player player : L2World.getAroundPlayers(npc, range, 200))
			if(player != null && !player.isBlockAll())
				player.sendPacket(cs);
	}

	// Белый чат
	public static void npcSay(L2NpcInstance npc, String text)
	{
		npcSayInRange(npc, text, 1500);
	}

	// Белый чат
	public static void npcSayInRangeCustomMessage(L2NpcInstance npc, int range, String address, Object... replacements)
	{
		if(npc == null)
			return;
		for(L2Player player : L2World.getAroundPlayers(npc, range, 200))
			if(player != null && !player.isBlockAll())
				player.sendPacket(new NpcSay(npc, 0, new CustomMessage(address, player, replacements).toString()));
	}

	// Белый чат
	public static void npcSayCustomMessage(L2NpcInstance npc, String address, Object... replacements)
	{
		npcSayInRangeCustomMessage(npc, 1500, address, replacements);
	}

	// private message
	public static void npcSayToPlayer(L2NpcInstance npc, L2Player player, String text)
	{
		if(npc == null || player.isBlockAll())
			return;
		player.sendPacket(new NpcSay(npc, 2, text));
	}

	// Shout (желтый) чат
	public static void npcShout(L2NpcInstance npc, String text)
	{
		if(npc == null)
			return;
		NpcSay cs = new NpcSay(npc, 1, text);
		if(Config.SHOUT_CHAT_MODE == 1)
		{
			for(L2Player player : L2World.getAroundPlayers(npc, 10000, 1500))
				if(player != null && !player.isBlockAll())
					player.sendPacket(cs);
		}
		else
		{
			int mapregion = MapRegion.getInstance().getMapRegion(npc.getX(), npc.getY());
			for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
				if(player != null && MapRegion.getInstance().getMapRegion(player.getX(), player.getY()) == mapregion && !player.isBlockAll())
					player.sendPacket(cs);
		}
	}

	// Shout (желтый) чат
	public static void npcShoutCustomMessage(L2NpcInstance npc, String address, Object... replacements)
	{
		if(npc == null)
			return;
		if(Config.SHOUT_CHAT_MODE == 1)
		{
			for(L2Player player : L2World.getAroundPlayers(npc, 10000, 1500))
				if(player != null && !player.isBlockAll())
					player.sendPacket(new NpcSay(npc, 1, new CustomMessage(address, player, replacements).toString()));
		}
		else
		{
			int mapregion = MapRegion.getInstance().getMapRegion(npc.getX(), npc.getY());
			for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
				if(player != null && MapRegion.getInstance().getMapRegion(player.getX(), player.getY()) == mapregion && !player.isBlockAll())
					player.sendPacket(new NpcSay(npc, 1, new CustomMessage(address, player, replacements).toString()));
		}
	}

	/**
	 * Добавляет предмет в инвентарь чара
	 * @param playable Владелец инвентаря
	 * @param item_id ID предмета
	 * @param count количество
	 */
	public static void addItem(L2Playable playable, int item_id, long count)
	{
		if(playable == null || count < 1)
			return;

		L2Playable player;
		if(playable.isSummon())
			player = playable.getPlayer();
		else
			player = playable;

		L2ItemInstance item = ItemTemplates.getInstance().createItem(item_id);
		if(item.isStackable())
		{
			item.setCount(count);
			player.getInventory().addItem(item);
		}
		else
		{
			player.getInventory().addItem(item);
			for(int i = 1; i < count; i++)
				player.getInventory().addItem(ItemTemplates.getInstance().createItem(item_id));
		}

		player.sendPacket(SystemMessage.obtainItems(item_id, count, 0));
	}

	/**
	 * Возвращает количество предметов в инвентаре чара.
	 * @param playable Владелец инвентаря
	 * @param item_id ID предмета
	 * @return количество
	 */
	public static long getItemCount(L2Playable playable, int item_id)
	{
		long count = 0;
		L2Playable player;
		if(playable != null && playable.isSummon())
			player = playable.getPlayer();
		else
			player = playable;
		Inventory inv = player.getInventory();
		if(inv == null)
			return 0;
		L2ItemInstance[] items = inv.getItems();
		for(L2ItemInstance item : items)
			if(item.getItemId() == item_id)
				count += item.getCount();
		return count;
	}

	/**
	 * Удаляет предметы из инвентаря чара.
	 * @param playable Владелец инвентаря
	 * @param item_id ID предмета
	 * @param count количество
	 * @return количество удаленных
	 */
	public static long removeItem(L2Playable playable, int item_id, long count)
	{
		if(playable == null || count < 1)
			return 0;

		L2Playable player;
		if(playable.isSummon())
			player = playable.getPlayer();
		else
			player = playable;
		Inventory inv = player.getInventory();
		if(inv == null)
			return 0;
		long removed = count;
		L2ItemInstance[] items = inv.getItems();
		for(L2ItemInstance item : items)
			if(item.getItemId() == item_id && count > 0)
			{
				long item_count = item.getCount();
				long rem = count <= item_count ? count : item_count;
				player.getInventory().destroyItemByItemId(item_id, rem, true);
				count -= rem;
			}
		removed -= count;

		if(removed > 0)
			player.sendPacket(SystemMessage.removeItems(item_id, removed));
		return removed;
	}

	public static void removeItemByObjId(L2Playable playable, int item_obj_id, int count)
	{
		if(playable == null || count < 1)
			return;

		L2Playable player;
		if(playable.isSummon())
			player = playable.getPlayer();
		else
			player = playable;
		Inventory inv = player.getInventory();
		if(inv == null)
			return;
		L2ItemInstance[] items = inv.getItems();
		for(L2ItemInstance item : items)
			if(item.getObjectId() == item_obj_id && count > 0)
			{
				long item_count = item.getCount();
				int item_id = item.getItemId();
				long removed = count <= item_count ? count : item_count;
				player.getInventory().destroyItem(item, removed, true);
				if(removed > 1)
					player.sendPacket(SystemMessage.removeItems(item_id, removed));
			}
	}

	public static boolean ride(L2Player player, int pet)
	{
		if(player.isMounted())
			player.setMount(0, 0, 0);

		if(player.getPet() != null)
		{
			player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
			return false;
		}

		player.setMount(pet, 0, 0);
		return true;
	}

	public static void unRide(L2Player player)
	{
		if(player.isMounted())
			player.setMount(0, 0, 0);
	}

	public static void unSummonPet(L2Player player, boolean onlyPets)
	{
		L2Summon pet = player.getPet();
		if(pet == null)
			return;
		if(pet.isPet() || !onlyPets)
			pet.unSummon();
	}

	public static L2NpcInstance spawn(Location loc, int npcId)
	{
		try
		{
			L2NpcInstance npc = NpcTable.getTemplate(npcId).getNewInstance();
			npc.setSpawnedLoc(loc.correctGeoZ());
			npc.onSpawn();
			npc.spawnMe(npc.getSpawnedLoc());
			return npc;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public L2Object getSelf()
	{
		return L2ObjectsStorage.get(self);
	}

	public L2Player getSelfPlayer()
	{
		return L2ObjectsStorage.getAsPlayer(self);
	}

	public L2NpcInstance getNpc()
	{
		return L2ObjectsStorage.getAsNpc(npc);
	}

	public static String htmlButton(String value, String action, int width)
	{
		return Strings.htmlButton(value, action, width);
	}

	public static String htmlButton(String value, String action, int width, int height)
	{
		return Strings.htmlButton(value, action, width, height);
	}

	public static ExShowTrace Points2Trace(GArray<int[]> points, int step, boolean auto_compleate, boolean maxz)
	{
		ExShowTrace result = new ExShowTrace();

		int[] prev = null;
		int[] first = null;
		for(int[] p : points)
		{
			if(first == null)
				first = p;

			if(prev != null)
				result.addLine(prev[0], prev[1], maxz ? prev[3] : prev[2], p[0], p[1], maxz ? p[3] : p[2], step, 60000);

			prev = p;
		}

		if(prev == null || first == null)
			return result;

		if(auto_compleate)
			result.addLine(prev[0], prev[1], maxz ? prev[3] : prev[2], first[0], first[1], maxz ? first[3] : first[2], step, 60000);

		return result;
	}

	public static void SpawnNPCs(int npcId, int[][] locations, GArray<L2Spawn> list)
	{
		L2NpcTemplate template = NpcTable.getTemplate(npcId);
		if(template == null)
		{
			System.out.println("WARNING! Functions.SpawnNPCs template is null for npc: " + npcId);
			Thread.dumpStack();
			return;
		}
		for(int[] location : locations)
			try
			{
				L2Spawn sp = new L2Spawn(template);
				sp.setLoc(new Location(location));
				sp.setAmount(1);
				sp.setRespawnDelay(0);
				sp.init();
				if(list != null)
					list.add(sp);
			}
			catch(ClassNotFoundException e)
			{
				e.printStackTrace();
			}
	}

	public static void deSpawnNPCs(GArray<L2Spawn> list)
	{
		for(L2Spawn sp : list)
		{
			sp.stopRespawn();
			sp.getLastSpawn().deleteMe();
		}
		list.clear();
	}

	public static boolean IsActive(String name)
	{
		return ServerVariables.getString(name, "off").equalsIgnoreCase("on");
	}

	public static boolean SetActive(String name, boolean active)
	{
		if(active == IsActive(name))
			return false;
		if(active)
			ServerVariables.set(name, "on");
		else
			ServerVariables.unset(name);
		return true;
	}

	public static boolean SimpleCheckDrop(L2Character mob, L2Character killer)
	{
		return mob != null && mob.isMonster() && !mob.isRaid() && killer != null && killer.getPlayer() != null && killer.getLevel() - mob.getLevel() < 9;
	}

	public static boolean isPvPEventStarted()
	{
		if((Boolean) callScripts("events.TvT.TvT", "isRunned", new Object[] {}))
			return true;
		if((Boolean) callScripts("events.lastHero.LastHero", "isRunned", new Object[] {}))
			return true;
		if((Boolean) callScripts("events.CtF.CtF", "isRunned", new Object[] {}))
			return true;
		return false;
	}

	public static L2NpcInstance spawn(int x, int y, int z, int npcId)
	{
		return spawn(new Location(x, y, z), npcId, 0);
	}

	public static L2NpcInstance spawn(Location loc, int npcId, int resp)
	{
		try
		{
			L2Spawn spawn = new L2Spawn(NpcTable.getTemplate(npcId));
			spawn.setLoc(loc);
			spawn.setRespawnDelay(resp);
			return spawn.doSpawn(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static L2NpcInstance spawn(Location loc, int npcId, int resp, int resprnd)
	{
		try
		{
			L2Spawn spawn = new L2Spawn(NpcTable.getTemplate(npcId));
			spawn.setLoc(loc);
			spawn.setRespawnDelay(resp, resprnd);
			return spawn.doSpawn(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}	
	
    public static void openDoor(int doorId, int instanceId)
    {
        for(L2DoorInstance door : ReflectionTable.getInstance().get(instanceId).getDoors())
            if(door.getDoorId() == doorId)
                door.openMe();
    }

    public static void closeDoor(int doorId, int instanceId)
    {
        for(L2DoorInstance door : ReflectionTable.getInstance().get(instanceId).getDoors())
            if(door.getDoorId() == doorId)
                door.closeMe();
    }	
}