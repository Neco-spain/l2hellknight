package other.KetraOrcSupport;

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

public class KetraOrcSupport extends Quest
{
	private static final String qn = "KetraOrcSupport";
    
    	private static final int KADUN = 31370;  // Hierarch
    	private static final int WAHKAN = 31371; // Messenger
    	private static final int ASEFA = 31372;  // Soul Guide
    	private static final int ATAN = 31373;   // Grocer
    	private static final int JAFF = 31374;   // Warehouse Keeper
    	private static final int JUMARA = 31375; // Trader
    	private static final int KURFA = 31376;  // Gate Keeper
    	private static final int[] NPCS = {KADUN, WAHKAN, ATAN, JAFF, JUMARA, KURFA, ASEFA };
    
    	private static final int HORN = 7186;
    
    	private static final int[][] BUFF = 
    	{
        	{0, 0, 0},    // null for correct numbers
        	{4359, 1, 2}, // Focus: Requires 2 Buffalo HORNs
        	{4360, 1, 2}, // Death Whisper: Requires 2 Buffalo HORNs
        	{4345, 1, 3}, // Might: Requires 3 Buffalo HORNs
        	{4355, 1, 3}, // Acumen: Requires 3 Buffalo HORNs
        	{4352, 1, 3}, // Berserker: Requires 3 Buffalo HORNs
        	{4354, 1, 3}, // Vampiric Rage: Requires 3 Buffalo HORNs
        	{4356, 1, 6}, // Empower: Requires 6 Buffalo HORNs
        	{4357, 1, 6}  // Haste: Requires 6 Buffalo HORNs
    	};
    
    	public KetraOrcSupport(int id, String name, String descr)
    	{
        	super(id, name, descr);
        
        	for (int i : NPCS)
            		addFirstTalkId(i);
        	addTalkId(ASEFA);
        	addTalkId(KURFA);
        	addTalkId(JAFF);
        	addStartNpc(KURFA);
        	addStartNpc(JAFF);
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
           		final int eventId = Integer.parseInt(event);
            		if (eventId > 0 && eventId <= 8)
            		{
	            		if (st.getQuestItemsCount(HORN) >= BUFF[eventId][2])
	            		{
	                		st.takeItems(HORN, BUFF[eventId][2]);
	                		npc.setTarget(player);
	                		npc.doCast(SkillTable.getInstance().getInfo(BUFF[eventId][0], BUFF[eventId][1]));
	                		npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
	                		htmltext = "31372-4.htm";
	            		}
            		}
        	}
        	else if (event.equals("Withdraw"))
        	{
            		if (player.getWarehouse().getSize() == 0)
                		htmltext = "31374-0.htm";
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
        			case 4:
        				htmltext = "31376-4.htm";
        				break;
        			case 5:
        				htmltext = "31376-5.htm";
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
        		case KADUN:
                		if (Alevel > 0)
                    			htmltext = "31370-friend.htm";
                		else
                    			htmltext = "31370-no.htm";
                			break;
        		case WAHKAN:
                		if (Alevel > 0)
                    			htmltext = "31371-friend.htm";
                		else
                    			htmltext = "31371-no.htm";
                			break;
        		case ASEFA:
                		st.setState(State.STARTED);
                		if (Alevel < 1)
                    			htmltext = "31372-3.htm";
                		else if (Alevel < 3 && Alevel > 0)
                    			htmltext = "31372-1.htm";
                		else if (Alevel > 2)
                		{
                    			if (st.getQuestItemsCount(HORN) != 0)
                        			htmltext = "31372-4.htm";
                    			else
                        			htmltext = "31372-2.htm";
                		}
                		break;
        		case ATAN:
                		if (player.getKarma() >= 1)
                    			htmltext = "31373-pk.htm";
                		else if (Alevel <= 0)
                    			htmltext = "31373-no.htm";
                		else if (Alevel == 1 || Alevel == 2)
                    			htmltext = "31373-1.htm";
                		else
                    			htmltext = "31373-2.htm";
                			break;
        		case JAFF:
        			switch (Alevel)
        			{
        				case 1:
        					htmltext = "31374-1.htm";
        					break;
        				case 2:
        				case 3:
        					htmltext = "31374-2.htm";
        					break;
       					default:
       	                			if (Alevel <= 0)
       	                    				htmltext = "31374-no.htm";
       	                			else if (player.getWarehouse().getSize() == 0)
       	                    				htmltext = "31374-3.htm";
       	                			else
       	                    				htmltext = "31374-4.htm";
       							break;
        			}
                		break;
        		case JUMARA:
        			switch (Alevel)
        			{
        				case 2:
        					htmltext = "31375-1.htm";
        					break;
        				case 3:
        				case 4:
        					htmltext = "31375-2.htm";
        					break;
        				case 5:
        					htmltext = "31375-3.htm";
        					break;
       					default:
       						htmltext = "31375-no.htm";
       						break;
        			}
                		break;
        		case KURFA:
                		if (Alevel <= 0)
                    			htmltext = "31376-no.htm";
                		else if (Alevel > 0 && Alevel < 4)
                    			htmltext = "31376-1.htm";
                		else if (Alevel == 4)
                    			htmltext = "31376-2.htm";
                		else
                    			htmltext = "31376-3.htm";
                			break;
        	}
        	return htmltext;
    	}
    
    	public static void main(String[] args)
    	{
        	new KetraOrcSupport(-1, qn, "other");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Other: Ketra Orc Support");
    	}
}