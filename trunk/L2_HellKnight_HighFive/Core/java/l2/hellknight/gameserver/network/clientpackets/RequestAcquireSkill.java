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
package l2.hellknight.gameserver.network.clientpackets;

import l2.hellknight.Config;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.datatables.SkillTreesData;
import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.model.L2Clan;
import l2.hellknight.gameserver.model.L2SkillLearn;
import l2.hellknight.gameserver.model.L2SquadTrainer;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2FishermanInstance;
import l2.hellknight.gameserver.model.actor.instance.L2NpcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2TrainerHealersInstance;
import l2.hellknight.gameserver.model.actor.instance.L2TransformManagerInstance;
import l2.hellknight.gameserver.model.actor.instance.L2VillageMasterInstance;
import l2.hellknight.gameserver.model.base.AcquireSkillType;
import l2.hellknight.gameserver.model.holders.ItemHolder;
import l2.hellknight.gameserver.model.holders.SkillHolder;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.AcquireSkillDone;
import l2.hellknight.gameserver.network.serverpackets.ExStorageMaxCount;
import l2.hellknight.gameserver.network.serverpackets.PledgeSkillList;
import l2.hellknight.gameserver.network.serverpackets.StatusUpdate;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.util.Util;

/**
 * @author Zoey76
 */
public final class RequestAcquireSkill extends L2GameClientPacket
{
	private static final String _C__7C_REQUESTACQUIRESKILL = "[C] 7C RequestAcquireSkill";
	
	private int _id;
	private int _level;
	private int _skillType;
	private int _subType;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_skillType = readD();
		if (_skillType == AcquireSkillType.SubPledge.ordinal())
		{
			_subType = readD();
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((_level < 1) || (_level > 1000) || (_id < 1) || (_id > 32000))
		{
			Util.handleIllegalPlayerAction(activeChar, "Wrong Packet Data in Aquired Skill", Config.DEFAULT_PUNISH);
			_log.warning("Recived Wrong Packet Data in Aquired Skill - id: " + _id + " level: " + _level + " for " + activeChar);
			return;
		}
		
		final L2Npc trainer = activeChar.getLastFolkNPC();
		if (!(trainer instanceof L2NpcInstance))
		{
			return;
		}
		
		if (!trainer.canInteract(activeChar) && !activeChar.isGM())
		{
			return;
		}
		
		final L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		if (skill == null)
		{
			_log.warning(RequestAcquireSkill.class.getSimpleName() + ": Player " + activeChar.getName() + " is trying to learn a null skill Id: " + _id + " level: " + _level + "!");
			return;
		}
		
		// Hack check. Doesn't apply to all Skill Types
		final int prevSkillLevel = activeChar.getSkillLevel(_id);
		final AcquireSkillType skillType = AcquireSkillType.values()[_skillType];
		if ((prevSkillLevel > 0) && !((skillType == AcquireSkillType.Transfer) || (skillType == AcquireSkillType.SubPledge)))
		{
			if (prevSkillLevel == _level)
			{
				_log.warning("Player " + activeChar.getName() + " is trying to learn a skill that already knows, Id: " + _id + " level: " + _level + "!");
				return;
			}
			else if (prevSkillLevel != (_level - 1))
			{
				// The previous level skill has not been learned.
				activeChar.sendPacket(SystemMessageId.PREVIOUS_LEVEL_SKILL_NOT_LEARNED);
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " without knowing it's previous level!", 0);
				return;
			}
		}
		
		final L2SkillLearn s = SkillTreesData.getInstance().getSkillLearn(skillType, _id, _level, activeChar);
		if (s == null)
		{
			return;
		}
		
		switch (skillType)
		{
			case Class:
			{
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					giveSkill(activeChar, trainer, skill);
				}
				break;
			}
			case Transform:
			{
				// Hack check.
				if (!L2TransformManagerInstance.canTransform(activeChar))
				{
					activeChar.sendPacket(SystemMessageId.NOT_COMPLETED_QUEST_FOR_SKILL_ACQUISITION);
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " without required quests!", 0);
					return;
				}
				
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					giveSkill(activeChar, trainer, skill);
				}
				break;
			}
			case Fishing:
			{
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					giveSkill(activeChar, trainer, skill);
				}
				break;
			}
			case Pledge:
			{
				if (!activeChar.isClanLeader())
				{
					return;
				}
				
				final L2Clan clan = activeChar.getClan();
				int repCost = s.getLevelUpSp();
				if (clan.getReputationScore() >= repCost)
				{
					if (Config.LIFE_CRYSTAL_NEEDED)
					{
						for (ItemHolder item : s.getRequiredItems())
						{
							if (!activeChar.destroyItemByItemId("Consume", item.getId(), item.getCount(), trainer, false))
							{
								// Doesn't have required item.
								activeChar.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
								L2VillageMasterInstance.showPledgeSkillList(activeChar);
								return;
							}
							
							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
							sm.addItemName(item.getId());
							sm.addItemNumber(item.getCount());
							activeChar.sendPacket(sm);
						}
					}
					
					clan.takeReputationScore(repCost, true);
					
					final SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(repCost);
					activeChar.sendPacket(cr);
					
					clan.addNewSkill(skill);
					
					clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
					
					activeChar.sendPacket(new AcquireSkillDone());
					
					L2VillageMasterInstance.showPledgeSkillList(activeChar);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
					L2VillageMasterInstance.showPledgeSkillList(activeChar);
				}
				break;
			}
			case SubPledge:
			{
				if (!activeChar.isClanLeader())
				{
					return;
				}
				
				final L2Clan clan = activeChar.getClan();
				if ((clan.getFortId() == 0) && (clan.getCastleId() == 0))
				{
					return;
				}
				
				if (trainer instanceof L2SquadTrainer)
				{
					// Hack check. Check if SubPledge can accept the new skill:
					if (!clan.isLearnableSubPledgeSkill(skill, _subType))
					{
						activeChar.sendPacket(SystemMessageId.SQUAD_SKILL_ALREADY_ACQUIRED);
						Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " without knowing it's previous level!", 0);
						return;
					}
					
					int rep = s.getLevelUpSp();
					if (clan.getReputationScore() < rep)
					{
						activeChar.sendPacket(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
						return;
					}
					
					for (ItemHolder item : s.getRequiredItems())
					{
						if (!activeChar.destroyItemByItemId("SubSkills", item.getId(), item.getCount(), trainer, false))
						{
							activeChar.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
							return;
						}
						
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(item.getId());
						sm.addItemNumber(item.getCount());
						activeChar.sendPacket(sm);
					}
					
					if (rep > 0)
					{
						clan.takeReputationScore(rep, true);
						final SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
						cr.addNumber(rep);
						activeChar.sendPacket(cr);
					}
					
					clan.addNewSkill(skill, _subType);
					clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
					activeChar.sendPacket(new AcquireSkillDone());
					
					((L2SquadTrainer) trainer).showSubUnitSkillList(activeChar);
				}
				break;
			}
			case Transfer:
			{
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					giveSkill(activeChar, trainer, skill);
				}
				break;
			}
			case SubClass:
			{
				// Hack check.
				if (activeChar.isSubClassActive())
				{
					activeChar.sendPacket(SystemMessageId.SKILL_NOT_FOR_SUBCLASS);
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " while Sub-Class is active!", 0);
					return;
				}
				
				QuestState st = activeChar.getQuestState("SubClassSkills");
				if (st == null)
				{
					final Quest subClassSkilllsQuest = QuestManager.getInstance().getQuest("SubClassSkills");
					if (subClassSkilllsQuest != null)
					{
						st = subClassSkilllsQuest.newQuestState(activeChar);
					}
					else
					{
						_log.warning("Null SubClassSkills quest, for Sub-Class skill Id: " + _id + " level: " + _level + " for player " + activeChar.getName() + "!");
						return;
					}
				}
				
				for (String varName : L2TransformManagerInstance._questVarNames)
				{
					for (int i = 1; i <= Config.MAX_SUBCLASS; i++)
					{
						final String itemOID = st.getGlobalQuestVar(varName + i);
						if (!itemOID.isEmpty() && !itemOID.endsWith(";") && !itemOID.equals("0"))
						{
							if (Util.isDigit(itemOID))
							{
								final int itemObjId = Integer.parseInt(itemOID);
								final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(itemObjId);
								if (item != null)
								{
									for (ItemHolder itemIdCount : s.getRequiredItems())
									{
										if (item.getItemId() == itemIdCount.getId())
										{
											if (checkPlayerSkill(activeChar, trainer, s))
											{
												giveSkill(activeChar, trainer, skill);
												// Logging the given skill.
												st.saveGlobalQuestVar(varName + i, skill.getId() + ";");
											}
											return;
										}
									}
								}
								else
								{
									_log.warning("Inexistent item for object Id " + itemObjId + ", for Sub-Class skill Id: " + _id + " level: " + _level + " for player " + activeChar.getName() + "!");
								}
							}
							else
							{
								_log.warning("Invalid item object Id " + itemOID + ", for Sub-Class skill Id: " + _id + " level: " + _level + " for player " + activeChar.getName() + "!");
							}
						}
					}
				}
				
				// Player doesn't have required item.
				activeChar.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
				showSkillList(trainer, activeChar);
				break;
			}
			case Collect:
			{
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					giveSkill(activeChar, trainer, skill);
				}
				break;
			}
			default:
			{
				_log.warning("Recived Wrong Packet Data in Aquired Skill, unknown skill type:" + _skillType);
				break;
			}
		}
	}
	
	/**
	 * Perform a simple check for current player and skill.<br>
	 * Takes the needed SP if the skill require it and all requirements are meet.<br>
	 * Consume required items if the skill require it and all requirements are meet.<br>
	 * @param player the skill learning player.
	 * @param trainer the skills teaching Npc.
	 * @param s the skill to be learn.
	 * @return {@code true} if all requirements are meet, {@code false} otherwise.
	 */
	private boolean checkPlayerSkill(L2PcInstance player, L2Npc trainer, L2SkillLearn s)
	{
		if (s != null)
		{
			if ((s.getSkillId() == _id) && (s.getSkillLevel() == _level))
			{
				// Hack check.
				if (s.getGetLevel() > player.getLevel())
				{
					player.sendPacket(SystemMessageId.YOU_DONT_MEET_SKILL_LEVEL_REQUIREMENTS);
					Util.handleIllegalPlayerAction(player, "Player " + player.getName() + ", level " + player.getLevel() + " is requesting skill Id: " + _id + " level " + _level + " without having minimum required level, " + s.getGetLevel() + "!", 0);
					return false;
				}
				
				// First it checks that the skill require SP and the player has enough SP to learn it.
				final int levelUpSp = s.getCalculatedLevelUpSp(player.getClassId(), player.getLearningClass());
				if ((levelUpSp > 0) && (levelUpSp > player.getSp()))
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
					showSkillList(trainer, player);
					return false;
				}
				
				if (!Config.DIVINE_SP_BOOK_NEEDED && (_id == L2Skill.SKILL_DIVINE_INSPIRATION))
				{
					return true;
				}
				
				// Check for required skills.
				if (!s.getPreReqSkills().isEmpty())
				{
					for (SkillHolder skill : s.getPreReqSkills())
					{
						if (player.getSkillLevel(skill.getSkillId()) != skill.getSkillLvl())
						{
							return false;
						}
					}
				}
				
				// Check for required items.
				if (!s.getRequiredItems().isEmpty())
				{
					// Then checks that the player has all the items
					long reqItemCount = 0;
					for (ItemHolder item : s.getRequiredItems())
					{
						reqItemCount = player.getInventory().getInventoryItemCount(item.getId(), -1);
						if (reqItemCount < item.getCount())
						{
							// Player doesn't have required item.
							player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
							showSkillList(trainer, player);
							return false;
						}
					}
					// If the player has all required items, they are consumed.
					for (ItemHolder itemIdCount : s.getRequiredItems())
					{
						if (!player.destroyItemByItemId("SkillLearn", itemIdCount.getId(), itemIdCount.getCount(), trainer, true))
						{
							Util.handleIllegalPlayerAction(player, "Somehow player " + player.getName() + ", level " + player.getLevel() + " lose required item Id: " + itemIdCount.getId() + " to learn skill while learning skill Id: " + _id + " level " + _level + "!", 0);
						}
					}
				}
				// If the player has SP and all required items then consume SP.
				if (levelUpSp > 0)
				{
					player.setSp(player.getSp() - levelUpSp);
					final StatusUpdate su = new StatusUpdate(player);
					su.addAttribute(StatusUpdate.SP, player.getSp());
					player.sendPacket(su);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Add the skill to the player and makes proper updates.
	 * @param player the player acquiring a skill.
	 * @param trainer the Npc teaching a skill.
	 * @param skill the skill to be learn.
	 */
	private void giveSkill(L2PcInstance player, L2Npc trainer, L2Skill skill)
	{
		// Send message.
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1);
		sm.addSkillName(skill);
		player.sendPacket(sm);
		
		player.sendPacket(new AcquireSkillDone());
		
		player.addSkill(skill, true);
		player.sendSkillList();
		
		player.updateShortCuts(_id, _level);
		showSkillList(trainer, player);
		
		// If skill is expand type then sends packet:
		if ((_id >= 1368) && (_id <= 1372))
		{
			player.sendPacket(new ExStorageMaxCount(player));
		}
	}
	
	/**
	 * Wrapper for returning the skill list to the player after it's done with current skill.
	 * @param trainer the Npc which the {@code player} is interacting.
	 * @param player the active character.
	 */
	private void showSkillList(L2Npc trainer, L2PcInstance player)
	{
		if ((trainer instanceof L2TrainerHealersInstance) && (_skillType == AcquireSkillType.Transfer.ordinal()))
		{
			L2TrainerHealersInstance.showTransferSkillList(player);
		}
		else if (trainer instanceof L2FishermanInstance)
		{
			L2FishermanInstance.showFishSkillList(player);
		}
		else if ((trainer instanceof L2TransformManagerInstance) && (_skillType == AcquireSkillType.Transform.ordinal()))
		{
			L2TransformManagerInstance.showTransformSkillList(player);
		}
		else if ((trainer instanceof L2TransformManagerInstance) && (_skillType == AcquireSkillType.SubClass.ordinal()))
		{
			L2TransformManagerInstance.showSubClassSkillList(player);
		}
		else
		{
			L2NpcInstance.showSkillList(player, trainer, player.getLearningClass());
		}
	}
	
	@Override
	public String getType()
	{
		return _C__7C_REQUESTACQUIRESKILL;
	}
}
