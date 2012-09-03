package l2rt.gameserver.model.instances;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.model.L2Multisell;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2TerritoryManagerInstance extends L2NpcInstance
{
	public L2TerritoryManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		int npcId = getNpcId();
		int terr = npcId - 36489;
		if(terr > 9 || terr < 1)
			return;

		String badges = player.getVar("badges" + terr);
		int count = badges != null ? Integer.parseInt(badges) : 0;
		int territoryBadgeId = 13756 + terr;

		if(command.equalsIgnoreCase("buyspecial"))
		{
			if(Functions.getItemCount(player, territoryBadgeId) < 1)
				showChatWindow(player, getHtmlPath(npcId, 1));
			else
				L2Multisell.getInstance().SeparateAndSend(npcId, player, 0);
			return;
		}

		if(command.equalsIgnoreCase("calculate"))
		{
			if(count < 1)
			{
				showChatWindow(player, getHtmlPath(npcId, 4));
				return;
			}
			NpcHtmlMessage html = new NpcHtmlMessage(player, this, getHtmlPath(npcId, 5), 5);
			html.replace("%territory%", CastleManager.getInstance().getCastleByIndex(terr).getName());
			html.replace("%badges%", "" + count);
			html.replace("%adena%", "" + (count * 520));
			player.sendPacket(html);
		}
		else if(command.equalsIgnoreCase("recivelater"))
			showChatWindow(player, getHtmlPath(npcId, 6));
		else if(command.equalsIgnoreCase("recive"))
		{
			if(count < 1)
			{
				showChatWindow(player, getHtmlPath(npcId, 4));
				return;
			}
			player.unsetVar("badges" + terr);
			Functions.addItem(player, territoryBadgeId, count);
			Functions.addItem(player, 57, count * 520);
			showChatWindow(player, getHtmlPath(npcId, 7));
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		if(val == 0)
			return "data/html/TerritoryManager/TerritoryManager.htm";
		return "data/html/TerritoryManager/TerritoryManager-" + val + ".htm";
	}
}