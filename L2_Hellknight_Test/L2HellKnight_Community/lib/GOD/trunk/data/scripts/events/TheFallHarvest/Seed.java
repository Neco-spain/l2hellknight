package events.TheFallHarvest;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import npc.model.SquashInstance;

public class Seed implements IItemHandler, ScriptFile
{
	public class DeSpawnScheduleTimerTask implements Runnable
	{
		L2Spawn spawnedPlant = null;

		public DeSpawnScheduleTimerTask(L2Spawn spawn)
		{
			spawnedPlant = spawn;
		}

		public void run()
		{
			try
			{
				spawnedPlant.getLastSpawn().decayMe();
				spawnedPlant.getLastSpawn().deleteMe();
			}
			catch(Throwable t)
			{}
		}
	}

	private static int[] _itemIds = { 6389, // small seed
			6390 // large seed
	};

	private static int[] _npcIds = { 12774, // Young Pumpkin
			12777 // Large Young Pumpkin
	};

	public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
	{
		L2Player activeChar = (L2Player) playable;
		if(activeChar.isInZone(ZoneType.Castle))
		{
			activeChar.sendMessage("Нельзя взращивать тыкву в замке");
			return;
		}
		if(activeChar.isInZone(ZoneType.Fortress))
		{
			activeChar.sendMessage("Нельзя взращивать тыкву в форте");
			return;
		}
		if(activeChar.isInZone(ZoneType.OlympiadStadia))
		{
			activeChar.sendMessage("Нельзя взращивать тыкву на стадионе");
			return;
		}

		L2NpcTemplate template = null;

		int itemId = item.getItemId();
		for(int i = 0; i < _itemIds.length; i++)
			if(_itemIds[i] == itemId)
			{
				template = NpcTable.getTemplate(_npcIds[i]);
				break;
			}

		if(template == null)
			return;

		L2Object target = activeChar.getTarget();
		if(target == null)
			target = activeChar;

		try
		{
			L2Spawn spawn = new L2Spawn(template);
			spawn.setLoc(GeoEngine.findPointToStay(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 30, 70, activeChar.getReflection().getGeoIndex()));
			L2NpcInstance npc = spawn.doSpawn(true);
			npc.setAI(new SquashAI(npc));
			((SquashInstance) npc).setSpawner(activeChar);

			ThreadPoolManager.getInstance().scheduleAi(new DeSpawnScheduleTimerTask(spawn), 180000, false);
			activeChar.getInventory().destroyItem(item.getObjectId(), 1, false);
		}
		catch(Exception e)
		{
			activeChar.sendPacket(Msg.YOUR_TARGET_CANNOT_BE_FOUND);
		}
	}

	public int[] getItemIds()
	{
		return _itemIds;
	}

	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}