package l2rt.gameserver.model.instances;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.CoupleManager;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.Couple;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2WeddingManagerInstance extends L2NpcInstance
{
	public L2WeddingManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(L2Player player, int val)
	{
		String filename = "data/html/wedding/start.htm";
		String replace = "";
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		// standard msg
		String filename = "data/html/wedding/start.htm";
		String replace = "";

		// if player has no partner
		if(player.getPartnerId() == 0)
		{
			filename = "data/html/wedding/nopartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}

		L2Player ptarget = L2ObjectsStorage.getPlayer(player.getPartnerId());

		// partner online ?
		if(ptarget == null || !ptarget.isOnline())
		{
			filename = "data/html/wedding/notfound.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if(player.isMaried()) // already married ?
		{
			filename = "data/html/wedding/already.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if(command.startsWith("AcceptWedding"))
		{
			// accept the wedding request
			player.setMaryAccepted(true);
			Couple couple = CoupleManager.getInstance().getCouple(player.getCoupleId());
			couple.marry();

			//messages to the couple
			player.sendMessage(new CustomMessage("l2rt.gameserver.model.instances.L2WeddingManagerMessage", player));
			player.setMaried(true);
			player.setMaryRequest(false);
			ptarget.sendMessage(new CustomMessage("l2rt.gameserver.model.instances.L2WeddingManagerMessage", ptarget));
			ptarget.setMaried(true);
			ptarget.setMaryRequest(false);

			//wedding march
			player.broadcastPacket(new MagicSkillUse(player, player, 2230, 1, 1, 0));
			ptarget.broadcastPacket(new MagicSkillUse(ptarget, ptarget, 2230, 1, 1, 0));

			// fireworks
			player.broadcastPacket(new MagicSkillUse(player, player, 2025, 1, 1, 0));
			ptarget.broadcastPacket(new MagicSkillUse(ptarget, ptarget, 2025, 1, 1, 0));

			Announcements.getInstance().announceToAll("Gratulations, " + player.getName() + " and " + ptarget.getName() + " has married.");

			filename = "data/html/wedding/accepted.htm";
			replace = ptarget.getName();
			sendHtmlMessage(ptarget, filename, replace);
			return;
		}
		else if(player.isMaryRequest())
		{
			// check for formalwear
			if(Config.WEDDING_FORMALWEAR && !isWearingFormalWear(player))
			{
				filename = "data/html/wedding/noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			filename = "data/html/wedding/ask.htm";
			player.setMaryRequest(false);
			ptarget.setMaryRequest(false);
			replace = ptarget.getName();
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if(command.startsWith("AskWedding"))
		{
			// check for formalwear
			if(Config.WEDDING_FORMALWEAR && !isWearingFormalWear(player))
			{
				filename = "data/html/wedding/noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			else if(player.getAdena() < Config.WEDDING_PRICE)
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			else
			{
				player.setMaryAccepted(true);
				ptarget.setMaryRequest(true);
				replace = ptarget.getName();
				filename = "data/html/wedding/requested.htm";
				player.reduceAdena(Config.WEDDING_PRICE, true);
				sendHtmlMessage(player, filename, replace);
				return;
			}
		}
		else if(command.startsWith("DeclineWedding"))
		{
			player.setMaryRequest(false);
			ptarget.setMaryRequest(false);
			player.setMaryAccepted(false);
			ptarget.setMaryAccepted(false);
			player.sendMessage("You declined");
			ptarget.sendMessage("Your partner declined");
			replace = ptarget.getName();
			filename = "data/html/wedding/declined.htm";
			sendHtmlMessage(ptarget, filename, replace);
			return;
		}
		else if(player.isMaryAccepted())
		{
			filename = "data/html/wedding/waitforpartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		sendHtmlMessage(player, filename, replace);
	}

	private static boolean isWearingFormalWear(L2Player player)
	{
		if(player != null && player.getInventory() != null && player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST) == Inventory.FORMAL_WEAR_ID)
			return true;
		return false;
	}

	private void sendHtmlMessage(L2Player player, String filename, String replace)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}