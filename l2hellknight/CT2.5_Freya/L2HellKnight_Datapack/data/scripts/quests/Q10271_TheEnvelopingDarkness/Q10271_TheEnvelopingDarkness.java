package quests.Q10271_TheEnvelopingDarkness;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q10271_TheEnvelopingDarkness extends Quest
{
	private static final String qn = "10271_TheEnvelopingDarkness";
	// Npc
	private static final int ORBYU = 32560;
	private static final int EL = 32556;
  private static final int MEDIBAL_CORPSE = 32528;
  // Item
  private static final int MEDIBAL_DOCUMENT = 13852;


   @Override
	public String onTalk(L2Npc npc, L2PcInstance player)
  {
		String htmltext = getNoQuestMsg(player);
		QuestState st =  player.getQuestState(qn);
    if (st == null)
			return htmltext;

    if (npc.getNpcId() == ORBYU)
    {
     switch (st.getState())
      {
    	  case State.CREATED:
      QuestState _prev = player.getQuestState("10269_ToTheSeedOfDestruction");
      if ((_prev != null) && (_prev.getState() == State.COMPLETED) && (player.getLevel() >= 75))
					htmltext = "32560-01.htm";
        else 
          htmltext = "32560-02.htm"; 
		        break;
            // HTML      
			case State.STARTED:
					htmltext = "32560-05.htm";
          break;
      case State.COMPLETED:
					htmltext = "32560-03.htm";
          break;   
      }
         // HTML 
    if (st.getInt("cond") == 2)
         {
      htmltext = "32560-06.htm";
         }
         // HTML 
     else if (st.getInt("cond") == 3)
         {
      htmltext = "32560-07.htm";
         }
     else if (st.getInt("cond") == 4)
         {
      htmltext = "32560-08.htm";
      st.unset("cond");
      st.setState(State.COMPLETED);
      st.giveItems(57, 62516);
      st.addExpAndSp(377403, 37867);
      st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
         }         
         
     }
     
     else if (npc.getNpcId() == EL)
       { 
       switch (st.getState())
      {  
       case State.COMPLETED:
					htmltext = "32556-02.htm";
          break;   
      }
       // HTML  
				    if (st.getInt("cond") == 1)
        		{
        		  htmltext = "32556-01.htm";
        		}
            // HTML 
        		else if (st.getInt("cond") == 2)
        		{
					    htmltext = "32556-07.htm";
        		}
            // HTML 
            else if (st.getInt("cond") == 3)
        		{
        		  htmltext = "32556-08.htm";
        		}
            // HTML 
            else if (st.getInt("cond") == 4)
        		{
        		  htmltext = "32556-09.htm";
        		}
		   }
       
       else if (npc.getNpcId() == MEDIBAL_CORPSE)
       { 
       switch (st.getState())
      {  
       case State.COMPLETED:
					htmltext = "32528-02.htm";
          break;   
      }  
       // HTML 
       if (st.getInt("cond") == 2)
        		{
        		   htmltext = "32528-01.htm";
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "3");
               st.giveItems(MEDIBAL_DOCUMENT, 1);
        		} 
         // HTML   
				else if (st.getInt("cond") == 3)
        		{
        		  htmltext = "32528-03.htm";
        		}
             // HTML   
				else if (st.getInt("cond") == 4)
        		{
        		  htmltext = "32528-03.htm";
        		}   
		   }
       	return htmltext;
  }
      
   	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		// Quest 
		if (event.equalsIgnoreCase("32560-05.htm"))
		   {
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		   }
       // Quest update
	  else	if (event.equalsIgnoreCase("32556-06.htm"))
		   {
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		   }
       // Quest update 
       else	if (event.equalsIgnoreCase("32556-09.htm"))
		   {
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
      st.takeItems(MEDIBAL_DOCUMENT, 1);
      
		   }    
		return htmltext;
	}  
          
    
	public Q10271_TheEnvelopingDarkness(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(ORBYU);
		addTalkId(ORBYU);
    addTalkId(EL);
    addTalkId(MEDIBAL_CORPSE);
    questItemIds = new int[] {MEDIBAL_DOCUMENT};
	}
  
	public static void main(String[] args)
	{
		new Q10271_TheEnvelopingDarkness(10271, qn, "The Enveloping Darkness");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: The Enveloping Darkness");
	}
}