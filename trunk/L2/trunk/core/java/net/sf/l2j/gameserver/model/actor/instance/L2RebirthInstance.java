/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author DaRkRaGe [L2JOneo]
 */
public class L2RebirthInstance extends L2FolkInstance
{
	public L2RebirthInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@SuppressWarnings("unused")
	private final static Log _log = LogFactory.getLog(L2RebirthInstance.class.getName());

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		String val = "";
		if (st.countTokens() >= 1)
		{
			val = st.nextToken();
		}
		if (actualCommand.equalsIgnoreCase("rebirth"))
		{
			setTarget(player);
			int lvl = player.getLevel();
			if (lvl >= 80)
			{
				if (val.equalsIgnoreCase("79"))
				{
					long delexp = 0;
					delexp = player.getStat().getExp() - player.getStat().getExpForLevel(lvl - 79);
					player.getStat().addExp(-delexp);
					player.broadcastKarma();
					player.store();
					player.broadcastStatusUpdate();
					player.broadcastUserInfo();
					player.sendMessage("Rebirth Accepted.");
					int itemReward = 1;
					player.addItem("Loot", Config.REBIRTH_ITEM, itemReward, player, true);
					player.sendMessage("You have win " + itemReward + " Rebirth Item");
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/Rebirth/Rebirth.htm");
					html.replace("%lvl%", String.valueOf(player.getLevel()));
					sendHtmlMessage(player, html);
				}
			}
			else
			{
				player.sendMessage("You have to be at least level 80 to use Rebirth Engine.");
			}
			return;
		}
		else if (actualCommand.equalsIgnoreCase("Reward"))
		{
			setTarget(player);
			L2ItemInstance invItem = player.getInventory().getItemByItemId(Config.REBIRTH_ITEM);
			{
				if (invItem != null)
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/Rebirth/reward.htm");
					html.replace("%lvl%", String.valueOf(player.getLevel()));
					sendHtmlMessage(player, html);
				}
				else
				{
					player.sendMessage("You Need Rebirth Book.");
				}
			}
		}
		else if (actualCommand.equalsIgnoreCase("skill"))
	    {
		      NpcHtmlMessage html = new NpcHtmlMessage(1);
		      html.setFile("data/html/Rebirth/skills.html");
		      player.sendPacket(html);
	    
		      return;
		}
		
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			player.sendPacket(new ValidateLocation(this));
		}
		else if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
			broadcastPacket(sa);
			player.setLastFolkNPC(this);
			showMessageWindow(player);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	private void showMessageWindow(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/Rebirth/main.htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}
}