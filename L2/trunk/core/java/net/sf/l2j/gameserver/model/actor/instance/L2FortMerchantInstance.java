package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * @author programmos, scoria dev
 */
public class L2FortMerchantInstance extends L2NpcWalkerInstance
{
	public L2FortMerchantInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if(!canTarget(player))
			return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if(this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			my = null;

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if(!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		String par = "";
		if(st.countTokens() >= 1)
		{
			par = st.nextToken();
		}

		//_log.info("actualCommand : " + actualCommand);
		//_log.info("par : " + par);

		if(actualCommand.equalsIgnoreCase("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(par);
			}
			catch(IndexOutOfBoundsException ioobe)
			{
					ioobe.printStackTrace();
			}
			catch(NumberFormatException nfe)
			{
					nfe.printStackTrace();
			}
			showMessageWindow(player, val);
		}
		else if(actualCommand.equalsIgnoreCase("showSiegeInfo"))
		{
			showSiegeInfoWindow(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
		st = null;
		actualCommand = null;
	}

	private void showMessageWindow(L2PcInstance player)
	{
		showMessageWindow(player, 0);
	}

	private void showMessageWindow(L2PcInstance player, int val)
	{
		player.sendPacket(new ActionFailed());

		String filename;

		if(val == 0)
		{
			filename = "data/html/fortress/merchant.htm";
		}
		else
		{
			filename = "data/html/fortress/merchant-" + val + ".htm";
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		if(getFort().getOwnerClan()!=null)
		{
			html.replace("%clanname%", getFort().getOwnerClan().getName());
		}
		else
		{
			html.replace("%clanname%", "NPC");
		}

		html.replace("%castleid%", Integer.toString(getCastle().getCastleId()));
		player.sendPacket(html);
		filename = null;
		html = null;
	}

	/**
	 * If siege is in progress shows the Busy HTML<BR>
	 * else Shows the SiegeInfo window
	 * 
	 * @param player
	 */
	public void showSiegeInfoWindow(L2PcInstance player)
	{
		if(validateCondition(player))
		{
			getFort().getSiege().listRegisterClan(player);
		}
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/fortress/merchant-busy.htm");
			html.replace("%fortname%", getFort().getName());
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
			player.sendPacket(new ActionFailed());
			html = null;
		}
	}

	private boolean validateCondition(L2PcInstance player)
	{
		if(getFort().getSiege().getIsInProgress())
			return false; // Busy because of siege
		return true;
	}

}