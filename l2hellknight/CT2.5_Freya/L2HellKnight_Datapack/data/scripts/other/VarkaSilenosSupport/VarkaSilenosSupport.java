package other.VarkaSilenosSupport;

import l2.hellknight.Config;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.WareHouseWithdrawalList;
import l2.hellknight.gameserver.util.Util;

public class VarkaSilenosSupport extends Quest
{
    	private static final String qn = "VarkaSilenosSupport";
    
    	private static final int ASHAS = 31377;  // Hierarch
    	private static final int NARAN = 31378;  // Messenger
    	private static final int UDAN = 31379;   // Buffer
    	private static final int DIYABU = 31380; // Grocer
    	private static final int HAGOS = 31381;  // Warehouse Keeper
    	private static final int SHIKON = 31382; // Trader
    	private static final int TERANU = 31383; // Teleporter
    	private static final int[] NPCS = {ASHAS, NARAN, UDAN, DIYABU, HAGOS, SHIKON, TERANU};

    	private static final int SEED = 7187;
    
    	private static final int[][] BUFF = 
    	{
        	{0, 0, 0},    // null for correct numbers
        	{4359, 1, 2}, // Focus: Requires 2 Nepenthese SEEDs
        	{4360, 1, 2}, // Death Whisper: Requires 2 Nepenthese SEEDs
        	{4345, 1, 3}, // Might: Requires 3 Nepenthese SEEDs
        	{4355, 1, 3}, // Acumen: Requires 3 Nepenthese SEEDs
        	{4352, 1, 3}, // Berserker: Requires 3 Nepenthese SEEDs
        	{4354, 1, 3}, // Vampiric Rage: Requires 3 Nepenthese SEEDs
        	{4356, 1, 6}, // Empower: Requires 6 Nepenthese SEEDs
        	{4357, 1, 6}  // Haste: Requires 6 Nepenthese SEEDs
    	};
    
    	public VarkaSilenosSupport(int id, String name, String descr)
    	{
        	super(id, name, descr);

        	for (int i : NPCS)
            		addFirstTalkId(i);
        	addTalkId(UDAN);
        	addTalkId(HAGOS);
        	addTalkId(TERANU);
        	addStartNpc(HAGOS);
        	addStartNpc(TERANU);
        
    	}

    	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;
		
        	if (Util.isDigit(event))
        	{
           		final  int eventId = Integer.parseInt(event);
            		if (eventId > 0 && eventId <= 8)
            		{
                		if (st.getQuestItemsCount(SEED) >= BUFF[eventId][2])
                		{
                    			st.takeItems(SEED, BUFF[eventId][2]);
                    			npc.setTarget(player);
                    			npc.doCast(SkillTable.getInstance().getInfo(BUFF[eventId][0], BUFF[eventId][1]));
                    			npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
                    			htmltext = "31379-4.htm";
                		}
            		}
        	}
        	else if (event.equals("Withdraw"))
        	{
            		if (player.getWarehouse().getSize() == 0)
                		htmltext = "31381-0.htm";
            		else
            		{
                		player.sendPacket(ActionFailed.STATIC_PACKET);
                		player.setActiveWarehouse(player.getWarehouse());
                		player.sendPacket(new WareHouseWithdrawalList(player, 1));
            		}
        	}
        	else if (event.equals("Teleport"))
        	{
        		switch (player.getAllianceWithVarkaKetra())
        		{
        			case -4:
        				htmltext = "31383-4.htm";
        				break;
        			case -5:
        				htmltext = "31383-5.htm";
        				break;
        		}
        	}
        	return htmltext;
    	}

    	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
    	{
    		String htmltext = getNoQuestMsg(player);
        	QuestState st = player.getQuestState(qn);
        	if (st == null)        	
            		st = newQuestState(player);
        
        	final int Alevel = player.getAllianceWithVarkaKetra();

        	switch (npc.getNpcId())
        	{
        		case ASHAS:
                		if (Alevel < 0)
                    			htmltext = "31377-friend.htm";
                		else
                    			htmltext = "31377-no.htm";
                			break;
        		case NARAN:
                		if (Alevel < 0)
                    			htmltext = "31378-friend.htm";
                		else
                    			htmltext = "31378-no.htm";
                			break;
        		case UDAN:
                		st.setState(State.STARTED);
                		if (Alevel > -1)
                    			htmltext = "31379-3.htm";
                		else if (Alevel > -3 && Alevel < 0)
                    			htmltext = "31379-1.htm";
                		else if (Alevel < -2)
                		{
                    			if (st.getQuestItemsCount(SEED) != 0)
                        			htmltext = "31379-4.htm";
                    			else
                        			htmltext = "31379-2.htm";
                		}
                		break;
        		case DIYABU:
                		if (player.getKarma() >= 1)
                    			htmltext = "31380-pk.htm";
                		else if (Alevel >= 0)
                    			htmltext = "31380-no.htm";
                		else if (Alevel == -1 || Alevel == -2)
                    			htmltext = "31380-1.htm";
                		else
                    			htmltext = "31380-2.htm";
                			break;
        		case HAGOS:
        			switch (Alevel)
        			{
        				case -1:
        					htmltext = "31381-1.htm";
        					break;
        				case -2:
        				case -3:
        					htmltext = "31381-2.htm";
        					break;
       					default:
       		            			if (Alevel >= 0)
       		                			htmltext = "31381-no.htm";
       		            			else if (player.getWarehouse().getSize() == 0)
       		                			htmltext = "31381-3.htm";
       		            			else
       		                			htmltext = "31381-4.htm";
       							break;
        			}
                		break;
        		case SHIKON:
        			switch (Alevel)
        			{
        				case -2:
        					htmltext = "31382-1.htm";
        					break;
        				case -3:
        				case -4:
        					htmltext = "31382-2.htm";
        					break;
        				case -5:
        					htmltext = "31382-3.htm";
        					break;
       					default:
       						htmltext = "31382-no.htm";
       						break;
        			}
                		break;
        		case TERANU:
                		if (Alevel >= 0)
                    			htmltext = "31383-no.htm";
                		else if (Alevel < 0 && Alevel > -4)
                    			htmltext = "31383-1.htm";
                		else if (Alevel == -4)
                    			htmltext = "31383-2.htm";
               	 		else
                    			htmltext = "31383-3.htm";
                			break;
        	} 
        	return htmltext;
    	}
    
    	public static void main(String[] args)
    	{
        	new VarkaSilenosSupport(-1, qn, "other");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Other: Varka Silenos Support");
    	}
}