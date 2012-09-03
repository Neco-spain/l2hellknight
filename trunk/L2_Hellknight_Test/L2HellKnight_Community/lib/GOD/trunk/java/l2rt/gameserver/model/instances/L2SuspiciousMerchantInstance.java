package l2rt.gameserver.model.instances;

import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.residence.Fortress;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.tables.ClanTable;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2SuspiciousMerchantInstance extends L2NpcInstance
{
	public L2SuspiciousMerchantInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.startsWith("showSiegeInfo"))
			showSiegeInfoWindow(player);
		else if(command.startsWith("Chat"))
			try
			{
				int val = Integer.parseInt(command.substring(5));
				showChatWindow(player, val);
			}
			catch(NumberFormatException nfe)
			{
				String filename = command.substring(5).trim();
				if(filename.length() == 0)
					showChatWindow(player, "data/html/npcdefault.htm");
				else
					showChatWindow(player, filename);
			}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(L2Player player, int val)
	{
		String filename;

		L2Clan clan = player.getClan();
		Fortress fortress = getFortress();

		if(val == 0)
			filename = "data/html/fortress/merchant.htm";
		else
			filename = "data/html/fortress/merchant-" + val + ".htm";

		if(fortress.getSiege().isInProgress() || TerritorySiege.isInProgress())
			filename = "data/html/fortress/merchant-busy.htm";

		if(clan == null)
			filename = "data/html/fortress/merchant-noclan.htm";

		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		html.replace("%fortname%", fortress.getName());

		if(getFortress().getOwnerId() > 0)
			html.replace("%clanname%", ClanTable.getInstance().getClan(getFortress().getOwnerId()).getName());
		else
			html.replace("%clanname%", "NPC");

		player.sendPacket(html);
	}

	public void showSiegeInfoWindow(L2Player player)
	{
		if(!getFortress().getSiege().isInProgress() && !TerritorySiege.isInProgress())
			getFortress().getSiege().listRegisterClan(player);
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("data/html/fortress/merchant-busy.htm");
			html.replace("%fortname%", getFortress().getName());
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
	}
}