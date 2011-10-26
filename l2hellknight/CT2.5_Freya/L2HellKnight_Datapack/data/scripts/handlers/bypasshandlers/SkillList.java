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
package handlers.bypasshandlers;

import l2.hellknight.Config;
import l2.hellknight.gameserver.datatables.SkillTreeTable;
import l2.hellknight.gameserver.handler.IBypassHandler;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2NpcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.base.ClassId;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;

public class SkillList implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"SkillList"
	};
	
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2NpcInstance))
			return false;
		
		if (Config.ALT_GAME_SKILL_LEARN)
		{
			try
			{
				String id = command.substring(9).trim();
				if (id.length() != 0)
				{
					activeChar.setSkillLearningClassId(ClassId.values()[Integer.parseInt(id)]);
					L2NpcInstance.showSkillList(activeChar, (L2Npc)target, ClassId.values()[Integer.parseInt(id)]);
				}
				else
				{
					boolean own_class = false;
					
					ClassId[] classesToTeach = ((L2NpcInstance)target).getClassesToTeach();
					if (classesToTeach != null)
					{
						for (ClassId cid : classesToTeach)
						{
							if (cid.equalsOrChildOf(activeChar.getClassId()))
							{
								own_class = true;
								break;
							}
						}
					}
					
					String text = "<html><body><center>Skill learning:</center><br>";
					
					if (!own_class)
					{
						String charType = activeChar.getClassId().isMage() ? "fighter" : "mage";
						text +=
							"Skills of your class are the easiest to learn.<br>"+
							"Skills of another class of your race are a little harder.<br>"+
							"Skills for classes of another race are extremely difficult.<br>"+
							"But the hardest of all to learn are the  "+ charType +"skills!<br>";
					}
					
					// make a list of classes
					if (classesToTeach != null)
					{
						int count = 0;
						ClassId classCheck = activeChar.getClassId();
						
						while ((count == 0) && (classCheck != null))
						{
							for (ClassId cid : classesToTeach)
							{
								if (cid.level() > classCheck.level())
									continue;
								
								if (SkillTreeTable.getInstance().getAvailableSkills(activeChar, cid).length == 0)
									continue;
								
								text += "<a action=\"bypass -h npc_%objectId%_SkillList "+cid.getId()+"\">Learn "+cid+"'s class Skills</a><br>\n";
								count++;
							}
							classCheck = classCheck.getParent();
						}
						classCheck = null;
					}
					else
						text += "No Skills.<br>";
					
					text += "</body></html>";
					
					NpcHtmlMessage html = new NpcHtmlMessage(((L2Npc)target).getObjectId());
					html.setHtml(text);
					html.replace("%objectId%", String.valueOf(((L2Npc)target).getObjectId()));
					activeChar.sendPacket(html);
					
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			catch (Exception e)
			{
				_log.info("Exception in " + getClass().getSimpleName());
			}
		}
		else
		{
			activeChar.setSkillLearningClassId(activeChar.getClassId());
			L2NpcInstance.showSkillList(activeChar, (L2Npc)target, activeChar.getClassId());
		}
		return true;
	}
	
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}