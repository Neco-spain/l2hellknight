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

import java.util.Collection;
import java.util.List;

import l2.hellknight.Config;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.datatables.SkillTreesData;
import l2.hellknight.gameserver.model.L2SkillLearn;
import l2.hellknight.gameserver.model.actor.templates.L2NpcTemplate;
import l2.hellknight.gameserver.model.base.AcquireSkillType;
import l2.hellknight.gameserver.model.holders.ItemHolder;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.AcquireSkillList;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Zoey76
 */
public final class L2TrainerHealersInstance extends L2TrainerInstance
{
	public L2TrainerHealersInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2TrainerHealersInstance);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/trainer/skilltransfer/" + pom + ".htm";
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		if (command.equals("SkillTransfer_Learn"))
		{
			if (!getTemplate().canTeach(player.getClassId()))
			{
				showNoTeachHtml(player);
				return;
			}
			if ((player.getLevel() < 76) || (player.getClassId().level() < 3))
			{
				html.setFile(player.getHtmlPrefix(), "data/html/trainer/skilltransfer/learn-lowlevel.htm");
				player.sendPacket(html);
				return;
			}
			showTransferSkillList(player);
		}
		else if (command.equals("SkillTransfer_Cleanse"))
		{
			if (!getTemplate().canTeach(player.getClassId()))
			{
				html.setFile(player.getHtmlPrefix(), "data/html/trainer/skilltransfer/cleanse-no.htm");
				player.sendPacket(html);
				return;
			}
			if ((player.getLevel() < 76) || (player.getClassId().level() < 3))
			{
				html.setFile(player.getHtmlPrefix(), "data/html/trainer/skilltransfer/cleanse-no.htm");
				player.sendPacket(html);
				return;
			}
			if (player.getAdena() < Config.FEE_DELETE_TRANSFER_SKILLS)
			{
				player.sendPacket(SystemMessageId.CANNOT_RESET_SKILL_LINK_BECAUSE_NOT_ENOUGH_ADENA);
				return;
			}
			
			boolean hasSkills = false;
			if (!hasTransferSkillItems(player))
			{
				final Collection<L2SkillLearn> skills = SkillTreesData.getInstance().getTransferSkillTree(player.getClassId()).values();
				for (L2SkillLearn s : skills)
				{
					final L2Skill sk = player.getKnownSkill(s.getSkillId());
					if (sk != null)
					{
						player.removeSkill(sk);
						for (ItemHolder item : s.getRequiredItems())
						{
							player.addItem("Cleanse", item.getId(), item.getCount(), this, true);
						}
						hasSkills = true;
					}
				}
				
				// Adena gets reduced once.
				if (hasSkills)
				{
					player.reduceAdena("Cleanse", Config.FEE_DELETE_TRANSFER_SKILLS, this, true);
				}
			}
			else
			{
				// Come back when you have used all transfer skill items for this class.
				html.setFile(player.getHtmlPrefix(), "data/html/trainer/skilltransfer/cleanse-no_skills.htm");
				player.sendPacket(html);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	/**
	 * This displays Transfer Skill List to the player.
	 * @param player the active character.
	 */
	public static void showTransferSkillList(L2PcInstance player)
	{
		final List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableTransferSkills(player);
		final AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.Transfer);
		int count = 0;
		
		for (L2SkillLearn s : skills)
		{
			if (SkillTable.getInstance().getInfo(s.getSkillId(), s.getSkillLevel()) != null)
			{
				count++;
				asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getLevelUpSp(), 0);
			}
		}
		
		if (count > 0)
		{
			player.sendPacket(asl);
		}
		else
		{
			player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
		}
	}
	
	private boolean hasTransferSkillItems(L2PcInstance player)
	{
		int itemId;
		switch (player.getClassId())
		{
			case cardinal:
				itemId = 15307;
				break;
			case evaSaint:
				itemId = 15308;
				break;
			case shillienSaint:
				itemId = 15309;
				break;
			default:
				itemId = -1;
		}
		return (player.getInventory().getInventoryItemCount(itemId, -1) > 0);
	}
}
