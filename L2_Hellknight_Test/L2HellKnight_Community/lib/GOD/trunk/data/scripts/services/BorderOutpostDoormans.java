package services;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2DoorInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.DoorTable;

/**
 * Используется в локации Eastern Border Outpost
 * @Author: SYS
 */
public class BorderOutpostDoormans extends Functions implements ScriptFile
{
	private static int DoorId = 24170001;

	public void onLoad()
	{
		System.out.println("Loaded Service: Border Outpost Doormans");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public void openDoor()
	{
		L2Player player = (L2Player) getSelf();

		if(!L2NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;

		L2DoorInstance door = DoorTable.getInstance().getDoor(DoorId);
		door.openMe();
	}

	public void closeDoor()
	{
		L2Player player = (L2Player) getSelf();

		if(player == null || !L2NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;

		L2DoorInstance door = DoorTable.getInstance().getDoor(DoorId);
		door.closeMe();
	}
}