package l2rt.gameserver.model.instances;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2rt.gameserver.templates.L2NpcTemplate;

import java.util.StringTokenizer;

public class L2FameManagerInstance extends L2NpcInstance
{
	public L2FameManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		if(actualCommand.equalsIgnoreCase("PK_Count"))
		{
			if(player.getFame() >= 50000)
			{
				if(player.getPkKills() > 0)
				{
					player.setFame(player.getFame() - 50000, "PK_Count");
					player.setPkKills(player.getPkKills() - 1);
					html.setFile("data/html/default/" + getNpcId() + "-3.htm");
				}
				else
					html.setFile("data/html/default/" + getNpcId() + "-4.htm");
			}
			else
				html.setFile("data/html/default/" + getNpcId() + "-lowfame.htm");
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else if(actualCommand.equalsIgnoreCase("CRP"))
		{
			if(player.getFame() >= 10000 && player.getClassId().level() >= 2 && player.getClan() != null && player.getClan().getLevel() >= 5)
			{
				player.setFame(player.getFame() - 10000, "CRP");
				player.getClan().incReputation(50, false, "FameManager from " + player.getName());
				player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
				player.sendPacket(Msg.ACQUIRED_50_CLAN_FAME_POINTS);
				html.setFile("data/html/default/" + getNpcId() + "-5.htm");
			}
			else
				html.setFile("data/html/default/" + getNpcId() + "-lowfame.htm");
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else
			super.onBypassFeedback(player, command);
	}
}