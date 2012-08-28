package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2SiegeNpcInstance extends L2FolkInstance
{
	//private static Logger _log = Logger.getLogger(L2SiegeNpcInstance.class.getName());

	public L2SiegeNpcInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	/**
	 * this is called when a player interacts with this NPC
	 * @param player
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player)) return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showSiegeInfoWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}

    /**
     * If siege is in progress shows the Busy HTML<BR>
     * else Shows the SiegeInfo window
     * @param player
     */
	public void showSiegeInfoWindow(L2PcInstance player)
	{
	    if (validateCondition(player))
            getCastle().getSiege().listRegisterClan(player);
        else
        {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/siege/" + getTemplate().npcId + "-busy.htm");
            html.replace("%castlename%",getCastle().getName());
            html.replace("%objectId%",String.valueOf(getObjectId()));
            player.sendPacket(html);
            player.sendPacket( new ActionFailed() );
        }
	}

	private boolean validateCondition(L2PcInstance player)
	{
        if (getCastle().getSiege().getIsInProgress())
            return false;       // Busy because of siege

		return true;
	}
}