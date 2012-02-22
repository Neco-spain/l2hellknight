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
package handlers.actionhandlers;

import l2.hellknight.gameserver.datatables.CharTemplateTable;
import l2.hellknight.gameserver.handler.AdminCommandHandler;
import l2.hellknight.gameserver.handler.IActionHandler;
import l2.hellknight.gameserver.handler.IAdminCommandHandler;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2Object.InstanceType;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.itemcontainer.Inventory;
import l2.hellknight.gameserver.network.serverpackets.MyTargetSelected;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.network.serverpackets.ValidateLocation;
import l2.hellknight.gameserver.skills.BaseStats;
import l2.hellknight.gameserver.skills.Stats;
import l2.hellknight.util.StringUtil;

public class L2PcInstanceActionShift implements IActionHandler
{
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if (activeChar.isGM())
		{
			// Check if the gm already target this l2pcinstance
			if (activeChar.getTarget() != target)
			{
				// Set the target of the L2PcInstance activeChar
				activeChar.setTarget(target);
				
				// Send a Server->Client packet MyTargetSelected to the L2PcInstance activeChar
				activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
			}
			
			// Send a Server->Client packet ValidateLocation to correct the L2PcInstance position and heading on the client
			if (activeChar != target)
				activeChar.sendPacket(new ValidateLocation((L2Character)target));
			
			IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler("admin_character_info");
			if (ach != null)
				ach.useAdminCommand("admin_character_info " + target.getName(), activeChar);
		}
		else
		{
			// Set the target of the L2PcInstance activeChar
			activeChar.setTarget(target);
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			int hpMul = Math.round((float)(((L2Character)target).getStat().calcStat(Stats.MAX_HP, 1, (L2Character)target, null) / BaseStats.CON.calcBonus((L2Character)target)));
			if (hpMul == 0)
				hpMul = 1;
			final StringBuilder html1 = StringUtil.startAppend(
					1000,
					"<html><head><title>"+
					String.valueOf(((L2Character)target).getName()),
					"</title></head><body>" +
					"<br><center><font color=\"LEVEL\">[Character Info]</font></center>" +
					"<table border=0 width=\"100%\">" +
					"<tr><td>Name: </td><td>",
					String.valueOf(((L2Character)target).getName()),
					"</td></tr>" +
					"<tr><td>Clan: </td><td>",
					String.valueOf(((L2PcInstance)target).getClan() != null ? ((L2PcInstance)target).getClan().getName() : "No clan"),
					"</td></tr>" +
					"<tr><td>Level: </td><td>",
					String.valueOf(((L2Character)target).getLevel()),
					"</td></tr>" +
					"<tr><td>Class: </td><td>",
					String.valueOf(((L2PcInstance)target).getTemplate().className),
					"</td></tr>" +
					"<tr><td>Base Class: </td><td>",
					CharTemplateTable.getInstance().getClassNameById(((L2PcInstance)target).getBaseClass()),
					"</td></tr>" +
					"<tr><td>CP: </td><td>",
					String.valueOf((int) ((L2PcInstance)target).getCurrentCp()),
					"/",
					String.valueOf(((L2PcInstance)target).getMaxCp()),
					"</td></tr>" +
					"<tr><td>HP: </td><td>",
					String.valueOf((int) ((L2PcInstance)target).getCurrentHp()),
					"/",
					String.valueOf(((L2PcInstance)target).getMaxHp()),
					"</td></tr>" +
					"<tr><td>MP: </td><td>",
					String.valueOf((int) ((L2PcInstance)target).getCurrentMp()),
					"/",
					String.valueOf(((L2PcInstance)target).getMaxMp()),
					"</td><td></td><td></td></tr>" +
					"</table>"+
					"<br><center><font color=\"CC0000\">[PVP Stats]</font></center>" +
					"<table border=0 width=\"100%\">" +
					"<tr><td>PvP Kills: </td><td>",
					String.valueOf(((L2PcInstance)target).getPvpKills()),
					"</td><td>PvP Flag: </td><td>",
					String.valueOf(((L2PcInstance)target).getPvpFlag()==0 ? "False": "True"),
					"</td></tr>" +
					"<tr><td>PK Kills: </td><td>",
					String.valueOf(((L2PcInstance)target).getPkKills()),
					"</td><td>Karma: </td><td>",
					String.valueOf(((L2PcInstance)target).getKarma()),
					"</td></tr>"+
					"</table><br>"+
					"<br><br><center><font color=009900>[Character Items]</font></center>" +
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND).getEnchantLevel() : "No Weapon"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND).getEnchantLevel() : "No Shield"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD).getEnchantLevel() : "No Helmet"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getEnchantLevel() : "No Chest"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS).getEnchantLevel() : "No Legs"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES).getEnchantLevel() : "No Gloves"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET).getEnchantLevel() : "No Boots"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_CLOAK) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_CLOAK).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_CLOAK) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_CLOAK).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_CLOAK).getEnchantLevel() : "No Cloak"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_UNDER) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_UNDER).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_UNDER) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_UNDER).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_UNDER).getEnchantLevel() : "No Underwear"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_BELT) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_BELT).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_BELT) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_BELT).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_BELT).getEnchantLevel() : "No Belt"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_HAIR) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_HAIR).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_HAIR) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_HAIR).getItemName() : "No Accesorry"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_HAIR2) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_HAIR2).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_HAIR2) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_HAIR2).getItemName() : "No Accesorry"),
					"</td></tr>"+
					"</table></td></tr></table>"+
						
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR).getEnchantLevel() : "No Earring"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR).getItemName() +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR).getEnchantLevel() : "No Earring"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK).getEnchantLevel() : "No Necklace"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER).getEnchantLevel() : "No Ring"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER).getItemName() +" +"+((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER).getEnchantLevel() : "No Ring"),
					"</td></tr>"+
					"</table></td></tr></table>"+
					
					"<table><tr><td height=39 width=45>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET) != null ? "<img src=" +((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET).getItem().getIcon() + " width=32 height=32>" : "None"),
					"</td><td width=220 height=39><table>"+
					"<tr><td>"+
					String.valueOf(((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET) != null ? ((L2PcInstance)target).getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET).getItemName() : "No Bracelet"),
					"</td></tr>"+
					"</table></td></tr></table>"
					
					
			);
			html1.append("</body></html>");
			
			html.setHtml(html1.toString());
			activeChar.sendPacket(html);
			 		}
		return true;
		
	}
	
	public InstanceType getInstanceType()
	{
		return InstanceType.L2PcInstance;
	}
}
