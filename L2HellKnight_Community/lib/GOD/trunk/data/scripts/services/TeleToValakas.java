package services;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Location;
import bosses.ValakasManager;

public class TeleToValakas extends Functions implements ScriptFile
{
	// Items
	private static final int FLOATING_STONE = 7267;

	private static final Location TELEPORT_POSITION1 = new Location(183831, -115457, -3296);
	private static final Location TELEPORT_POSITION2 = new Location(203940, -111840, 66);

	public void teleToCorridor()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;
		String var = player.getVar("ValakasEnter");
		boolean canenter = var != null && Long.parseLong(var) > System.currentTimeMillis();
		if(ValakasManager.isEnableEnterToLair())
			if(Functions.getItemCount(player, FLOATING_STONE) > 0 || canenter)
			{
				if(!canenter)
				{
					player.setVar("ValakasEnter", String.valueOf(System.currentTimeMillis() + 1000 * 60 * 60));
					Functions.removeItem(player, FLOATING_STONE, 1);
				}
				player.teleToLocation(TELEPORT_POSITION1);
			}
			else
				show("<html><body>Klein:<br>You do not have the Floating Stone. Go get one and then come back to me.</body></html>", player, npc);
		else
			show("<html><body>Klein:<br>Valakas is already awake!<br>You may not enter the Lair of Valakas.</body></html>", player, npc);
	}

	public void teleToValakas()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;
		String var = player.getVar("ValakasEnter");
		if(var != null)
			if(ValakasManager.isEnableEnterToLair())
			{
				ValakasManager.setValakasSpawnTask();
				player.teleToLocation(TELEPORT_POSITION2);
				player.unsetVar("ValakasEnter");
			}
			else
				show("<html><body>Heart of Volcano:<br>Valakas is already awake!<br>You may not enter the Lair of Valakas.</body></html>", player, npc);
		else
			show("Conditions are not right to enter to Lair of Valakas.", player, npc);
	}

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}