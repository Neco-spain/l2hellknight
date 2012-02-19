package other.SubClassSkills;

import l2.brick.Config;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.model.L2ItemInstance;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.AcquireSkillInfo;
import l2.brick.gameserver.network.serverpackets.AcquireSkillList;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.util.Util;

public class SubClassSkills extends Quest
{
	private static final String qn = "SubClassSkills";
	
	private static final int NPC = 32323;
	private static final int[] SKILLITEMS = { 10280, 10281, 10282, 10283, 10284, 10285, 10286, 10287, 10288, 10289, 10290, 10291, 10292, 10293, 10294, 10612 };
	private static final int[][] SUBSKILLS = { { 10280, 631, 632, 633, 634 }, // Common
			{ 10612, 637, 638, 639, 640, 799, 800 }, // Enhanced
			{ 10281, 801, 650, 651 },   // Warriors
			{ 10282, 804, 641, 652 },   // Knights
			{ 10283, 644, 645, 653 },   // Rogues
			{ 10284, 802, 646, 654 },   // Wizards
			{ 10285, 803, 648, 1490 },  // Healers
			{ 10286, 643, 1489, 1491 }, // Summoners
			{ 10287, 642, 647, 655 },   // Enchanters
			{ 10289, 656 }, 	    // Warriors
			{ 10288, 657 }, 	    // Knights
			{ 10290, 658 }, 	    // Rogues
			{ 10292, 659 }, 	    // Wizards
			{ 10291, 661 }, 	    // Healers
			{ 10294, 660 }, 	    // Summoners
			{ 10293, 662 } 		    // Enchanters
	};
	
	private static final String[] QUESTVARSITEMSS = { "EmergentAbility65-", "EmergentAbility70-", "ClassAbility75-", "ClassAbility80-" };
	private static final int[][] QUESTVARSITEMSI = 
	{ 
		{ 10280 }, 
		{ 10280 }, 
		{ 10612, 10281, 10282, 10283, 10284, 10285, 10286, 10287 }, 
		{ 10288, 10289, 10290, 10291, 10292, 10293, 10294 } 
	};

	private SubClassSkills()
	{
		super(-1, qn, "other");
		
		addStartNpc(NPC);
		addTalkId(NPC);
		addAcquireSkillId(NPC);
	}

	@Override
	public String onAcquireSkillList(L2Npc npc, L2PcInstance player)
	{
		final AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.unk4);
		QuestState st = player.getQuestState(qn);
		final L2Skill[] oldSkills = player.getAllSkills();
		int count = 0;
		for (int i : SKILLITEMS)
		{
			if (st.getQuestItemsCount(i) > 0)
			{
				for (int[] subsk : SUBSKILLS)
				{
					if (i != subsk[0])
						continue;

					for (int j = 1; j < subsk.length; j++)
					{
						int minLevel = 0;
						final int maxLevel = SkillTable.getInstance().getMaxLevel(subsk[j]);
						for (L2Skill oldsk : oldSkills)
						{
							if (oldsk.getId() == subsk[j])
								minLevel = oldsk.getLevel();
						}
						
						if (minLevel < maxLevel)
						{
							count += 1;
							asl.addSkill(subsk[j], minLevel + 1, maxLevel, 0, 0);
						}
					}
					break;
				}
			}
		}
		player.sendPacket(asl);
		
		if (count == 0)
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN));
		return "";
	}

	@Override
	public String onAcquireSkill(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (player.isSubClassActive())
		{
			player.sendMessage("You are trying to learn skill that u can't..");
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", Config.DEFAULT_PUNISH);
			return "false";
		}
		
		QuestState st = player.getQuestState(qn);
		for (int i : SKILLITEMS)
		{
			for (int[] subsk : SUBSKILLS)
			{
				if (i != subsk[0] || !Util.contains(subsk, skill.getId()))
					continue;

				for (int k = 0; k < QUESTVARSITEMSS.length; k++)
				{
					if (!Util.contains(QUESTVARSITEMSI[k], i))
						continue;

					for (int j = 0; j < Config.MAX_SUBCLASS; j++)
					{
						final String qvarName = QUESTVARSITEMSS[k] + (j + 1);
						final String qvar = st.getGlobalQuestVar(qvarName);
						if (qvar.length() > 0 && !qvar.equals("0") && !qvar.endsWith(";"))
						{
							final L2ItemInstance item = player.getInventory().getItemByItemId(i);
							if (item != null)
							{
								player.destroyItem(qn, item.getObjectId(), 1, player, false);
								st.saveGlobalQuestVar(qvarName, skill.getId() + ";");
								return "true";
							}
						}
					}
				}
			}
		}
		
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
		return "false";
	}

	@Override
	public String onAcquireSkillInfo(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		final AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), 0, 4);
		for (int i : SKILLITEMS)
		{
			for (int[] subsk : SUBSKILLS)
			{
				if (i == subsk[0] && Util.contains(subsk, skill.getId()))
					asi.addRequirement(99, i, 1, 50);
			}
		}
		
		player.sendPacket(asi);
		return "";
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equals("learn"))
		{
			htmltext = "";
			if (player.isSubClassActive())
				htmltext = "8005-04.htm";
			else
			{
				int j = 0;
				for (int i : SKILLITEMS)
					j += st.getQuestItemsCount(i);
				
				if (j > 0)
					onAcquireSkillList(npc, player);
				else
					htmltext = "8005-04.htm";
			}
		}
		else if (event.equals("cancel"))
		{
			if (st.getQuestItemsCount(57) < 10000000)
				htmltext = "8005-07.htm";
			else if (player.getSubClasses().size() == 0)
				htmltext = "8005-03.htm";
			else if (player.isSubClassActive())
				htmltext = "8005-04.htm";
			else
			{
				int activeCertifications = 0;
                for (String QUESTVARSITEMS : QUESTVARSITEMSS)
                {
                    for (int i = 0; i < Config.MAX_SUBCLASS; i++)
                    {
                        String qvarName = QUESTVARSITEMS + (i + 1);
                        String qvar = st.getGlobalQuestVar(qvarName);
                        if (qvar.endsWith(";") || (qvar.length() > 0 && !qvar.equals("0")))
                            activeCertifications++;
                    }
                }
                
				if (activeCertifications == 0)
					htmltext = "8005-08.htm";
				else
				{
                    for (String QUESTVARSITEMS : QUESTVARSITEMSS)
                    {
                        for (int i = 0; i < Config.MAX_SUBCLASS; i++)
                        {
                            String qvarName = QUESTVARSITEMS + (i + 1);
                            String qvar = st.getGlobalQuestVar(qvarName);
                            if (qvar.endsWith(";"))
                            {
                                final int skillId = Integer.valueOf(qvar.replace(";", ""));
                                final L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
                                if (skill != null)
                                {
                                    player.removeSkill(skill);
                                    st.saveGlobalQuestVar(qvarName, "0");
                                }
                            }
                            else if (qvar.length() > 0)
                                st.saveGlobalQuestVar(qvarName, "0");
                        }
                    }
                    
					for (int bookId : SKILLITEMS)
					{
						for (L2ItemInstance book : player.getInventory().getAllItemsByItemId(bookId))
							player.destroyItem(qn, book, player, true);
					}
					
					st.takeItems(57, 10000000);
					htmltext = "8005-09.htm";
					player.sendSkillList();
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return "";
		
		if (npc.getNpcId() == NPC)
		{
			st.set("cond", "0");
			st.setState(State.STARTED);
		}
		return "8005-01.htm";
	}

	public static void main(String[] args)
	{
		new SubClassSkills();
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Other: SubClass Skills");
	}
}