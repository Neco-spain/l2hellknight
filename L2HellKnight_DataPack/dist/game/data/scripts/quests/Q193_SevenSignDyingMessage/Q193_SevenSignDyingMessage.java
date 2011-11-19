package quests.Q193_SevenSignDyingMessage;

import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2MonsterInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.network.serverpackets.NpcSay;

public class Q193_SevenSignDyingMessage extends Quest
{
    	private static final String qn = "Q193_SevenSignDyingMessage";

	// NPC
    	private static final int HOLLINT 	= 30191;
    	private static final int CAIN 		= 32569;
    	private static final int ERIC 		= 32570;
    	private static final int ATHEBALDT 	= 30760;

	// MOB
    	private static final int SHILENSEVIL 	= 27343;

	// ITEMS
    	private static final int JACOB_NECK 	= 13814;
    	private static final int DEADMANS_HERB 	= 13816;
    	private static final int SCULPTURE 	= 14353;

    	private boolean ShilensevilOnSpawn;

    	public Q193_SevenSignDyingMessage(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	ShilensevilOnSpawn = false;

        	addStartNpc(HOLLINT);
        	addTalkId(HOLLINT);
        	addTalkId(CAIN);
        	addTalkId(ERIC);
        	addTalkId(ATHEBALDT);

        	addKillId(SHILENSEVIL);
		questItemIds = new int[] { JACOB_NECK, DEADMANS_HERB, SCULPTURE };
    	}

    	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    	{
        	String htmltext = event;
        	QuestState st = player.getQuestState(qn);
        	if(st == null)
            		return htmltext;
        	if(npc.getNpcId() == HOLLINT)
        	{
            		if(event.equalsIgnoreCase("30191-02.htm"))
            		{
                		st.setState((byte)1);
                		st.set("cond", "1");
                		st.giveItems(JACOB_NECK, 1L);
                		st.playSound("ItemSound.quest_accept");
            		}
        	}
		else if(npc.getNpcId() == CAIN)
        	{
            		if(event.equalsIgnoreCase("32569-05.htm"))
            		{
                		st.set("cond", "2");
                		st.takeItems(JACOB_NECK, 1L);
                		st.playSound("ItemSound.quest_middle");
            		}
			else
            		{
                		if(event.equalsIgnoreCase("9"))
                		{
                    			st.takeItems(DEADMANS_HERB, 1L);
                    			st.set("cond", "4");
                    			st.playSound("ItemSound.quest_middle");
                    			player.showQuestMovie(9);
                    			return "";
                		}
                		if(event.equalsIgnoreCase("32569-09.htm"))
                		{
                    			if(ShilensevilOnSpawn)
                    			{
                        			htmltext = getNoQuestMsg(player);
                    			}
					else
                    			{
                        			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "! That stranger must be defeated!"));
                        			L2MonsterInstance monster = (L2MonsterInstance)addSpawn(SHILENSEVIL, 0x142c0, 47422, -3220, 0, false, 0x493e0L, true);
                        			monster.broadcastPacket(new NpcSay(monster.getObjectId(), 0, monster.getNpcId(), "You are not the owner of that item!!"));
                        			monster.setRunning();
                        			monster.addDamageHate(player, 0, 999);
                        			monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st.getPlayer());
                        			ShilensevilOnSpawn = true;
                        			startQuestTimer("spawnS", 0x497c8L, npc, player);
                        			startQuestTimer("aiplayer", 30000L, npc, player);
                        			startQuestTimer("stopaiplayer", 0x497c8L, npc, player);
                    			}
                		}
				else
                		{
                    			if(event.equalsIgnoreCase("spawnS"))
                    			{
                        			ShilensevilOnSpawn = false;
                        			return "";
                    			}
                    			if(event.equalsIgnoreCase("aiplayer"))
                    			{
                        			npc.setTarget(player);
                       	 			npc.doCast(SkillTable.getInstance().getInfo(1011, 18));
                        			startQuestTimer("aiplayer", 30000L, npc, player);
                        			return "";
                    			}
                    			if(event.equalsIgnoreCase("stopaiplayer"))
                    			{
                       	 			cancelQuestTimer("aiplayer", npc, player);
                        			return "";
                    			}
                    			if(event.equalsIgnoreCase("32569-13.htm"))
                    			{
                        			st.set("cond", "6");
                        			st.takeItems(SCULPTURE, 1L);
                        			st.playSound("ItemSound.quest_middle");
                    			}
                		}
            		}
        	}
		else if(npc.getNpcId() == ERIC)
        	{
            		if(event.equalsIgnoreCase("32570-02.htm"))
            		{
                		st.set("cond", "3");
                		st.giveItems(DEADMANS_HERB, 1L);
               		 	st.playSound("ItemSound.quest_middle");
            		}
        	}
		else if(npc.getNpcId() == ATHEBALDT && event.equalsIgnoreCase("30760-02.htm"))
        	{
           		st.addExpAndSp(0x17d7840, 0x2625a0);
            		st.unset("cond");
            		st.setState((byte)2);
            		st.exitQuest(false);
            		st.playSound("ItemSound.quest_finish");
        	}
        	return htmltext;
    	}

  	public String onTalk(L2Npc npc, L2PcInstance player)
    	{
  			String htmltext = getNoQuestMsg(player);
  			QuestState st = player.getQuestState(qn);
        	QuestState first = player.getQuestState("Q192_SevenSignSeriesOfDoubt");
        	if(st == null)
            		return htmltext;
        	if(npc.getNpcId() == HOLLINT)
            	switch(st.getState())
            	{
            		case 0:
                		if(first != null && first.getState() == 2 && player.getLevel() >= 79)
                		{
                   			htmltext = "30191-01.htm";
                		}
				else
                		{
                    			htmltext = "30191-00.htm";
                    			st.exitQuest(true);
                		}
                		break;

            		case 1:
                		if(st.getInt("cond") == 1)
                    			htmltext = "30191-03.htm";
                		break;

            		case 2:
                		htmltext = getAlreadyCompletedMsg(player);
                		break;
            	}
        	else if(npc.getNpcId() == CAIN)
        	{
            		if(st.getState() == 1)
                	switch(st.getInt("cond"))
                	{
                		case 1:
                    			htmltext = "32569-01.htm";
                    			break;

                		case 2:
                    			htmltext = "32569-06.htm";
                    			break;

                		case 3:
                    			htmltext = "32569-07.htm";
                    			break;

                		case 4:
                    			htmltext = "32569-08.htm";
                    			break;

                		case 5:
                   			htmltext = "32569-10.htm";
                    			break;
                	}
        	}
		else if(npc.getNpcId() == ERIC)
        	{
            		if(st.getState() == 1)
                	switch(st.getInt("cond"))
                	{
                		case 2:
                    			htmltext = "32570-01.htm";
                    			break;

                		case 3:
                    			htmltext = "32570-03.htm";
                    			break;
                	}
        	}
		else if(npc.getNpcId() == ATHEBALDT && st.getState() == 1 && st.getInt("cond") == 6)
            		htmltext = "30760-01.htm";
        	return htmltext;
    	}

    	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    	{
        	QuestState st = player.getQuestState(qn);
        	if(st == null)
            		return super.onKill(npc, player, isPet);
        	if(npc.getNpcId() == SHILENSEVIL && st.getInt("cond") == 4)
        	{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "... You may have won this time... But next time, I will surely capture you!"));
            		st.giveItems(SCULPTURE, 1L);
            		st.set("cond", "5");
            		st.playSound("ItemSound.quest_middle");
            		ShilensevilOnSpawn = false;
            		cancelQuestTimer("aiplayer", npc, player);
        	}
        	return super.onKill(npc, player, isPet);
    	}

    	public static void main(String args[])
    	{
        	new Q193_SevenSignDyingMessage(193, qn, "Seven Sign Dying Message");
    	}
}