package quests.Q194_SevenSignContractOfMammon;

import l2.brick.Config;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;

public class Q194_SevenSignContractOfMammon extends Quest
{
    	private static final String qn = "Q194_SevenSignContractOfMammon";

	//NPC
    	private static final int ATHEBALDT 	= 30760;
    	private static final int COLIN 		= 32571;
    	private static final int FROG 		= 32572;
    	private static final int TESS 		= 32573;
    	private static final int KUTA 		= 32574;
    	private static final int CLAUDIA 	= 31001;

	// ITEMS
    	private static final int INTRODUCTION 	= 13818;
    	private static final int FROG_KING_BEAD = 13820;
    	private static final int CANDY_POUCH 	= 13821;
    	private static final int NATIVES_GLOVE 	= 13819;

    	public Q194_SevenSignContractOfMammon(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	addStartNpc(ATHEBALDT);
        	addTalkId(ATHEBALDT);
        	addTalkId(COLIN);
        	addTalkId(FROG);
        	addTalkId(TESS);
        	addTalkId(KUTA);
        	addTalkId(CLAUDIA);

		questItemIds = new int[] { INTRODUCTION, FROG_KING_BEAD, CANDY_POUCH, NATIVES_GLOVE };
    	}

	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    	{
        	String htmltext = event;
        	QuestState st = player.getQuestState(qn);
        	if(st == null)
            		return htmltext;
        	if(npc.getNpcId() == ATHEBALDT)
        	{
            		if(event.equalsIgnoreCase("30760-02.htm"))
            		{
                		st.setState((byte)1);
                		st.set("cond", "1");
                		st.playSound("ItemSound.quest_accept");
            		}
			else if(event.equalsIgnoreCase("30760-07.htm"))
            		{
                		st.set("cond", "3");
                		st.giveItems(INTRODUCTION, 1L);
                		st.playSound("ItemSound.quest_middle");
            		}
			else if(event.equalsIgnoreCase("10"))
            		{
                		st.set("cond", "2");
                		st.playSound("ItemSound.quest_middle");
                		player.showQuestMovie(10);
                		return "";
            		}
        	}
		else if(npc.getNpcId() == COLIN)
        	{
            		if(event.equalsIgnoreCase("32571-04.htm"))
            		{
                		st.set("cond", "4");
                		st.takeItems(INTRODUCTION, 1L);
                		transformPlayer(npc, player, 6201);
                		st.playSound("ItemSound.quest_middle");
            		}
            		if(event.equalsIgnoreCase("32571-06.htm") || event.equalsIgnoreCase("32571-14.htm") || event.equalsIgnoreCase("32571-22.htm"))
            		{
                		if(player.isTransformed())
                    			player.untransform();
           		}
			else if(event.equalsIgnoreCase("32571-08.htm"))
                		transformPlayer(npc, player, 6201);
            		else if(event.equalsIgnoreCase("32571-10.htm"))
            		{
                		st.set("cond", "6");
                		st.takeItems(FROG_KING_BEAD, 1L);
                		st.playSound("ItemSound.quest_middle");
            		}
			else if(event.equalsIgnoreCase("32571-12.htm"))
            		{
                		st.set("cond", "7");
                		transformPlayer(npc, player, 6202);
                		st.playSound("ItemSound.quest_middle");
            		}
			else if(event.equalsIgnoreCase("32571-16.htm"))
                		transformPlayer(npc, player, 6202);
            		else if(event.equalsIgnoreCase("32571-18.htm"))
            		{
                		st.set("cond", "9");
                		st.takeItems(CANDY_POUCH, 1L);
                		st.playSound("ItemSound.quest_middle");
            		}
			else if(event.equalsIgnoreCase("32571-20.htm"))
            		{
                		st.set("cond", "10");
                		transformPlayer(npc, player, 6203);
                		st.playSound("ItemSound.quest_middle");
            		}
			else if(event.equalsIgnoreCase("32571-24.htm"))
                		transformPlayer(npc, player, 6203);
            		else if(event.equalsIgnoreCase("32571-26.htm"))
            		{
                		st.set("cond", "12");
                		st.takeItems(NATIVES_GLOVE, 1L);
                		st.playSound("ItemSound.quest_middle");
            		}
        	}
		else if(npc.getNpcId() == FROG)
        	{
            		if(event.equalsIgnoreCase("32572-04.htm"))
            		{
                		st.set("cond", "5");
                		st.giveItems(FROG_KING_BEAD, 1L);
                		st.playSound("ItemSound.quest_middle");
            		}
        	}
		else if(npc.getNpcId() == TESS)
        	{
            		if(event.equalsIgnoreCase("32573-03.htm"))
            		{
                		st.set("cond", "8");
                		st.giveItems(CANDY_POUCH, 1L);
                		st.playSound("ItemSound.quest_middle");
            		}
        	}
		else if(npc.getNpcId() == KUTA)
        	{
            		if(event.equalsIgnoreCase("32574-04.htm"))
            		{
                		st.set("cond", "11");
                		st.giveItems(NATIVES_GLOVE, 1L);
                		st.playSound("ItemSound.quest_middle");
            		}
        	}
		else if(npc.getNpcId() == CLAUDIA && event.equalsIgnoreCase("31001-03.htm"))
        	{
            		st.addExpAndSp(0x17d7840, 0x2625a0);
            		st.unset("cond");
            		st.setState((byte)2);
            		st.exitQuest(false);
            		st.playSound("ItemSound.quest_finish");
        	}
        	return htmltext;
    	}

    	private void transformPlayer(L2Npc npc, L2PcInstance player, int transId)
    	{
        	if(player.isTransformed())
        	{
            		player.untransform();
            		try
            		{
                		Thread.sleep(2000L);
            		}
            		catch(InterruptedException e)
            		{
                		e.printStackTrace();
            		}
        	}
        	L2Effect arr$[] = player.getAllEffects();
        	int len$ = arr$.length;
        	for(int i$ = 0; i$ < len$; i$++)
        	{
            		L2Effect effect = arr$[i$];
            		if(effect.getAbnormalType().equalsIgnoreCase("speed_up"))
                		effect.exit();
        	}
        	npc.setTarget(player);
        	npc.doCast(SkillTable.getInstance().getInfo(transId, 1));
    	}

    	public String onTalk(L2Npc npc, L2PcInstance player)
    	{
        	String htmltext = getNoQuestMsg(player);
       	 	QuestState st = player.getQuestState(qn);
        	QuestState second = player.getQuestState("Q193_SevenSignDyingMessage");
        	if(st == null)
            		return htmltext;
        	if(npc.getNpcId() == ATHEBALDT)
            	switch(st.getState())
            	{
            		case 0:
                		if(second != null && second.getState() == 2 && player.getLevel() >= 79)
                		{
                   			htmltext = "30760-01.htm";
                		}
				else
                		{
                    			htmltext = "30760-00.htm";
                    			st.exitQuest(true);
                		}
                		break;

            		case 1:
                		if(st.getInt("cond") == 1)
                    			htmltext = "30760-03.htm";
               	 		else if(st.getInt("cond") == 2)
                    			htmltext = "30760-05.htm";
                		else if(st.getInt("cond") == 3)
                    			htmltext = "30760-08.htm";
                		break;

            		case 2:
                		htmltext = getAlreadyCompletedMsg(player);
                		break;
            	}
        	else if(npc.getNpcId() == COLIN)
        	{
            		if(st.getState() == 1)
                	if(st.getInt("cond") == 3)
                    		htmltext = "32571-01.htm";
                	else if(st.getInt("cond") == 4)
                	{
                    		if(checkPlayer(player, 6201))
                        		htmltext = "32571-05.htm";
                    		else
                        		htmltext = "32571-07.htm";
                	}
			else if(st.getInt("cond") == 5)
                    		htmltext = "32571-09.htm";
                	else if(st.getInt("cond") == 6)
                    		htmltext = "32571-11.htm";
                	else if(st.getInt("cond") == 7)
                	{
                    		if(checkPlayer(player, 6202))
                        		htmltext = "32571-13.htm";
                    		else
                        		htmltext = "32571-15.htm";
                	}
			else if(st.getInt("cond") == 8)
                    		htmltext = "32571-17.htm";
                	else if(st.getInt("cond") == 9)
                    		htmltext = "32571-19.htm";
                	else if(st.getInt("cond") == 10)
                	{
                    		if(checkPlayer(player, 6203))
                        		htmltext = "32571-21.htm";
                    		else
                        		htmltext = "32571-23.htm";
                	}
			else if(st.getInt("cond") == 11)
                    		htmltext = "32571-25.htm";
                	else if(st.getInt("cond") == 12)
                    		htmltext = "32571-27.htm";
        	}
		else if(npc.getNpcId() == FROG)
        	{
            		if(checkPlayer(player, 6201))
            		{
                		if(st.getInt("cond") == 4)
                    			htmltext = "32572-01.htm";
                		else if(st.getInt("cond") == 5)
                    			htmltext = "32572-05.htm";
            		}
			else
            		{
                		htmltext = "32572-00.htm";
            		}
        	}
		else if(npc.getNpcId() == TESS)
        	{
            		if(checkPlayer(player, 6202))
            		{
                		if(st.getInt("cond") == 7)
                    			htmltext = "32573-01.htm";
                		else if(st.getInt("cond") == 8)
                    				htmltext = "32573-04.htm";
            		}
			else
            		{
                		htmltext = "32573-00.htm";
            		}
        	}
		else if(npc.getNpcId() == KUTA)
        	{
            		if(checkPlayer(player, 6203))
            		{
                		if(st.getInt("cond") == 10)
                    			htmltext = "32574-01.htm";
                		else if(st.getInt("cond") == 11)
                    			htmltext = "32574-05.htm";
            		}
			else
            		{
                		htmltext = "32574-00.htm";
            		}
        	}
		else if(npc.getNpcId() == CLAUDIA && st.getInt("cond") == 12)
            		htmltext = "31001-01.htm";
        	return htmltext;
    	}

    	private boolean checkPlayer(L2PcInstance player, int transId)
    	{
        	L2Effect effect = player.getFirstEffect(transId);
        	return effect != null;
    	}

    	public static void main(String args[])
    	{
        	new Q194_SevenSignContractOfMammon(194, qn, "Seven Sign Contract Of Mammon");
    		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
    			_log.info("Loaded Quest: Seven Sign Contract Of Mammon");
    	}
}