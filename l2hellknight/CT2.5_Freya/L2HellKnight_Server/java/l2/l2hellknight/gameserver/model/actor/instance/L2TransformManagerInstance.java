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
package l2.hellknight.gameserver.model.actor.instance;

import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.datatables.SkillTreeTable;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.L2TransformSkillLearn;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.AcquireSkillList;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.templates.L2NpcTemplate;

public class L2TransformManagerInstance extends L2MerchantInstance
{
	/**
	 * @param objectId
	 * @param template
	 */
	public L2TransformManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2TransformManagerInstance);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		
		return "data/html/default/" + pom + ".htm";
	}
	
	/**
	 * this displays TransformationSkillList to the player.
	 * @param player
	 */
	public static void showTransformSkillList(L2PcInstance player)
	{
		L2TransformSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableTransformSkills(player);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Usual);
		int counts = 0;
		
		for (L2TransformSkillLearn s: skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if (sk == null)
				continue;
			
			counts++;
			
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 0);
		}
		
		if (counts == 0)
		{
			int minlevel = SkillTreeTable.getInstance().getMinLevelForNewTransformSkill(player);
			if (minlevel > 0)
			{
				// No more skills to learn, come back when you level.
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN));
		}
		else
			player.sendPacket(asl);
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
