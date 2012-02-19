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
package l2.brick.gameserver.model.actor.instance;

import l2.brick.Config;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.datatables.SkillTreeTable;
import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.L2SkillLearn;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.status.FolkStatus;
import l2.brick.gameserver.model.base.ClassId;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.AcquireSkillList;
import l2.brick.gameserver.network.serverpackets.ActionFailed;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.skills.effects.EffectBuff;
import l2.brick.gameserver.skills.effects.EffectDebuff;
import l2.brick.gameserver.templates.L2NpcTemplate;
import l2.brick.util.StringUtil;

public class L2NpcInstance extends L2Npc
{
	private final ClassId[] _classesToTeach;
	
	public L2NpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2NpcInstance);
		setIsInvul(false);
		_classesToTeach = template.getTeachInfo();
	}
	
	@Override
	public FolkStatus getStatus()
	{
		return (FolkStatus)super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new FolkStatus(this));
	}
	
	@Override
	public void addEffect(L2Effect newEffect)
	{
		if (newEffect instanceof EffectDebuff || newEffect instanceof EffectBuff)
			super.addEffect(newEffect);
		else if (newEffect != null)
			newEffect.stopEffectTask();
	}
	
	public ClassId[] getClassesToTeach()
	{
		return _classesToTeach;
	}
	
	/**
	 * this displays SkillList to the player.
	 * @param player
	 */
	public static void showSkillList(L2PcInstance player, L2Npc npc, ClassId classId)
	{
		if (Config.DEBUG)
			_log.fine("SkillList activated on: "+npc.getObjectId());
		
		int npcId = npc.getTemplate().npcId;
		
		if (npcId == 32611)
		{
			L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSpecialSkills(player);
			AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Special);
			
			int counts = 0;
			
			for (L2SkillLearn s : skills)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				
				if (sk == null)
					continue;
				
				counts++;
				asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), 0, 1);
			}
			
			if (counts == 0) // No more skills to learn, come back when you level.
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN));
			else
				player.sendPacket(asl);
			
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!npc.getTemplate().canTeach(classId))
		{
			npc.showNoTeachHtml(player);
			return;
		}
		
		if (((L2NpcInstance)npc).getClassesToTeach() == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			final String sb = StringUtil.concat(
					"<html><body>" +
					"I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:",
					String.valueOf(npcId),
					", Your classId:",
					String.valueOf(player.getClassId().getId()),
					"<br>" +
					"</body></html>"
			);
			html.setHtml(sb);
			player.sendPacket(html);
			return;
		}
		
		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, classId);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Usual);
		int counts = 0;
		
		for (L2SkillLearn s: skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if (sk == null)
				continue;
			
			int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			counts++;
			
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}
		
		if (counts == 0)
		{
			int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player, classId);
			if (minlevel > 0)
			{
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
