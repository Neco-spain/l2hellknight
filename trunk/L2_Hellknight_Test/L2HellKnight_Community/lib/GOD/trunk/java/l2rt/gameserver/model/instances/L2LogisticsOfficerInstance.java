package l2rt.gameserver.model.instances;

import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.instancemanager.ServerVariables;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2LogisticsOfficerInstance extends L2NpcInstance
{
	private static final int RECHARGE_TIME = 60 * 60 * 6; // каждые 6 часов
	private static final int ITEM_ID = 9910; // Blood Oath
	private int _lastRecive = 0;

	public L2LogisticsOfficerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(getFortress() == null)
			return;

		if(!player.isClanLeader() || getFortress().getOwnerId() != player.getClanId())
		{
			player.sendMessage(new CustomMessage("l2rt.gameserver.model.instances.L2ResidenceManager.NotAuthorizedToDoThis", player));
			return;
		}

		if(command.equalsIgnoreCase("ReciveBloodOath"))
		{
			String filename;
			if(getAvailableItemsCount(player) > 0)
			{
				filename = "data/html/fortress/LogisticsOfficer-3.htm";
				Functions.addItem(player, ITEM_ID, getAvailableItemsCount(player));
				_lastRecive = (int) (System.currentTimeMillis() / 1000);
				ServerVariables.set("ReciveBloodOath_" + player.getClan().getClanId(), _lastRecive);
			}
			else
				filename = "data/html/fortress/LogisticsOfficer-4.htm";

			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile(filename);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			player.sendPacket(html);
		}

		super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(L2Player player, int val)
	{
		if(getFortress() == null)
			return;

		player.sendActionFailed();

		String filename = "data/html/fortress/LogisticsOfficer-no.htm";

		if(player.isClanLeader() && getFortress().getOwnerId() == player.getClanId())
			if(val == 0)
				filename = "data/html/fortress/LogisticsOfficer.htm";
			else
				filename = "data/html/fortress/LogisticsOfficer-" + val + ".htm";

		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		html.replace("%count%", String.valueOf(getAvailableItemsCount(player)));
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private long getAvailableItemsCount(L2Player player)
	{
		if(player.getClan() == null)
			return 0;

		int lastRecive = ServerVariables.getInt("ReciveBloodOath_" + player.getClan().getClanId(), 0);

		if(_lastRecive > lastRecive)
			lastRecive = _lastRecive;

		if(lastRecive == 0 || lastRecive < getFortress().getOwnDate())
			lastRecive = getFortress().getOwnDate();

		return (long) Math.floor((System.currentTimeMillis() / 1000 - lastRecive) / RECHARGE_TIME);
	}
}