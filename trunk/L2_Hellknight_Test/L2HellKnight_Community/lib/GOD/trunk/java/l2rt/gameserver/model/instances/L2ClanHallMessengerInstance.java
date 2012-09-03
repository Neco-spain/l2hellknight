package l2rt.gameserver.model.instances;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.residence.ClanHall;
import l2rt.gameserver.model.entity.siege.clanhall.ClanHallSiege;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.network.serverpackets.SiegeInfo;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2ClanHallMessengerInstance extends L2NpcInstance
{
	public L2ClanHallMessengerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(L2Player player, int val)
	{
		ClanHall clanhall = getClanHall();
		if(clanhall == null)
			return;
		ClanHallSiege siege = clanhall.getSiege();
		if(siege == null)
			return;
		if(siege.isInProgress() || siege.isRegistrationOver() || TerritorySiege.isInProgress())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setHtml("<html><body><font color=\"LEVEL\">I can't do anything for you right now.</font></body></html>");
			player.sendPacket(html);
		}
		else
			player.sendPacket(new SiegeInfo(getClanHall()));
	}
}
