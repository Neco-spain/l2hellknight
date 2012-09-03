package l2rt.gameserver.model.instances;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.templates.L2NpcTemplate;

import java.util.StringTokenizer;

public class L2CastleDoormenInstance extends L2NpcInstance
{
	private static int Cond_All_False = 0;
	private static int Cond_Busy_Because_Of_Siege = 1;
	private static int Cond_Castle_Owner = 2;

	public L2CastleDoormenInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		int condition = validateCondition(player);
		if(condition <= Cond_All_False)
			return;
		if(condition == Cond_Busy_Because_Of_Siege)
			return;
		if(condition == Cond_Castle_Owner)
			if((player.getClanPrivileges() & L2Clan.CP_CS_ENTRY_EXIT) == L2Clan.CP_CS_ENTRY_EXIT)
			{
				if(command.startsWith("open_doors"))
				{
					StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid
					Castle castle = getCastle();
					while(st.hasMoreTokens())
						castle.openDoor(player, Integer.parseInt(st.nextToken()));
				}
				else if(command.startsWith("close_doors"))
				{
					StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid
					if(condition == 2)
					{
						Castle castle = getCastle();
						while(st.hasMoreTokens())
							castle.closeDoor(player, Integer.parseInt(st.nextToken()));
					}
				}
			}
			else
				player.sendMessage(new CustomMessage("common.Privilleges", player));
	}

	@Override
	public void showChatWindow(L2Player player, int val)
	{
		String filename = "data/html/doormen/no.htm";
		int condition = validateCondition(player);
		if(condition == Cond_Busy_Because_Of_Siege)
			filename = "data/html/doormen/busy.htm"; // Busy because of siege
		else if(condition == Cond_Castle_Owner) // Clan owns castle
			filename = "data/html/doormen/" + getTemplate().npcId + ".htm"; // Owner message window
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}

	private int validateCondition(L2Player player)
	{
		if(player.isGM())
			return Cond_Castle_Owner;

		if(player.getClan() != null)
		{
			Castle castle = getCastle();
			if(castle != null && castle.getId() >= 0)
				if(castle.getOwnerId() == player.getClanId())
				{
					if(castle.getSiege().isInProgress() || TerritorySiege.isInProgress())
					{
						if(Config.SIEGE_OPERATE_DOORS)
						{
							if(Config.SIEGE_OPERATE_DOORS_LORD_ONLY && !player.isCastleLord(castle.getId()))
								return Cond_Busy_Because_Of_Siege;
							return Cond_Castle_Owner;
						}
						return Cond_Busy_Because_Of_Siege;

					}
					return Cond_Castle_Owner;
				}
		}

		return Cond_All_False;
	}
}