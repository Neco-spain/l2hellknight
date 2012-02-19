package l2.brick.gameserver.model.actor.instance;

import l2.brick.gameserver.instancemanager.KrateisCubeManager;
import l2.brick.gameserver.templates.L2NpcTemplate;
import l2.brick.gameserver.model.actor.L2Summon;
import l2.brick.gameserver.network.SystemMessageId;

public class L2KrateisCubeManagerInstance extends L2NpcInstance
{

	public L2KrateisCubeManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Register"))
		{
			if (player.getInventoryLimit() * 0.8 <= player.getInventory().getSize())
			{
				player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
				showChatWindow(player, "data/html/krateisCube/32503-9.htm");
				return;
			}

			int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
			switch (cmdChoice)
			{
				case 1:
					if (player.getLevel() < 70 || player.getLevel() > 75)
					{
						showChatWindow(player, "data/html/krateisCube/32503-7.htm");
						return;
					}
					break;
				case 2:
					if (player.getLevel() < 76 || player.getLevel() > 79)
					{
						showChatWindow(player, "data/html/krateisCube/32503-7.htm");
						return;
					}
					break;
				case 3:
					if (player.getLevel() < 80)
					{
						showChatWindow(player, "data/html/krateisCube/32503-7.htm");
						return;
					}
					break;
					//Case 4:
				//this option was made since there are no different instances
				//just checks for minimum level requirement
				case 4:
					if (player.getLevel() < 70)
					{
						showChatWindow(player, "data/html/krateisCube/32503-10.htm");
						return;
					}
					break;
			}
	
			if (KrateisCubeManager.getInstance().isTimeToRegister())
			{
				if (KrateisCubeManager.getInstance().registerPlayer(player))
				{
					showChatWindow(player, "data/html/krateisCube/32503-4.htm");
					return;
				}
				else
				{
					showChatWindow(player, "data/html/krateisCube/32503-5.htm");
					return;
				}
			}
			else
			{
				showChatWindow(player, "data/html/krateisCube/32503-8.htm");
				return;
			}
		}
		else if (command.startsWith("Cancel"))
		{
			KrateisCubeManager.getInstance().removePlayer(player);
			showChatWindow(player, "data/html/krateisCube/32503-6.htm");
			return;
		}
		else if (command.startsWith("TeleportToFI"))
		{
			player.teleToLocation(-59193, -56893, -2034);
			L2Summon pet = player.getPet();
			if (pet != null)
				pet.teleToLocation(-59193, -56893, -2034);
			
				return;
			}
		else if (command.startsWith("TeleportIn"))
		{
			KrateisCubeManager.getInstance().teleportPlayerIn(player);
			return;
		}
		else
		{
			super.onBypassFeedback(player,command);
		}
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";

		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "data/html/krateisCube/" + pom + ".htm";
	}
}