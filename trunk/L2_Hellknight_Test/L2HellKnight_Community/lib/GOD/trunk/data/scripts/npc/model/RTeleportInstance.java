package npc.model;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.network.serverpackets.ExShowUsmVideo;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Files;
import l2rt.util.Location;

public class RTeleportInstance extends L2NpcInstance implements ScriptFile
{
	
	public RTeleportInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;
		
		if(command.startsWith("rtele"))
		{
			int val = Integer.parseInt(command.substring(4));
			switch(val)
			{
				case 1:
					player.teleToLocation(new Location(-114675, 230171, -1648));
					if (player.getVar("ruin") == null) {
						player.setVar("ruin", "1");
						player.sendPacket(new ExShowUsmVideo(1));
					}
					break;
				case 2:
					player.teleToLocation(new Location(-115005, 23783, -3088));
					break;
				default:
					showChatWindow(player, 0);
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}