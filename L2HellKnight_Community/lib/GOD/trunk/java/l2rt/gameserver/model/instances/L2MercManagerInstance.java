package l2rt.gameserver.model.instances;

import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.templates.L2NpcTemplate;

import java.util.StringTokenizer;

public final class L2MercManagerInstance extends L2MerchantInstance
{
	private static int COND_ALL_FALSE = 0;
	private static int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static int COND_OWNER = 2;

	public L2MercManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		int condition = validateCondition(player);
		if(condition <= COND_ALL_FALSE || condition == COND_BUSY_BECAUSE_OF_SIEGE)
			return;

		if(condition == COND_OWNER)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken(); // Get actual command

			String val = "";
			if(st.countTokens() >= 1)
				val = st.nextToken();

			if(actualCommand.equalsIgnoreCase("hire"))
			{
				if(val.equals(""))
					return;

				showShopWindow(player, Integer.parseInt(val), false);
			}
			else
				super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void showChatWindow(L2Player player, int val)
	{
		String filename = "data/html/castle/mercmanager/mercmanager-no.htm";
		int condition = validateCondition(player);
		if(condition == COND_BUSY_BECAUSE_OF_SIEGE)
			filename = "data/html/castle/mercmanager/mercmanager-busy.htm"; // Busy because of siege
		else if(condition == COND_OWNER)
				filename = "data/html/castle/mercmanager/mercmanager_nohire.htm";
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}

	private int validateCondition(L2Player player)
	{
		if(player.isGM())
			return COND_OWNER;
		if(getCastle() != null && getCastle().getId() > 0)
			if(player.getClan() != null)
				if(getCastle().getSiege().isInProgress() || TerritorySiege.isInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				else if(getCastle().getOwnerId() == player.getClanId() // Clan owns castle
						&& (player.getClanPrivileges() & L2Clan.CP_CS_MERCENARIES) == L2Clan.CP_CS_MERCENARIES) // has merc rights
					return COND_OWNER; // Owner

		return COND_ALL_FALSE;
	}
}