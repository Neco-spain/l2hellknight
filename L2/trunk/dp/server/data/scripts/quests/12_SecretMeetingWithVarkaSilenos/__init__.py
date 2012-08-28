#made by Emperorc
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

qn = "12_SecretMeetingWithVarkaSilenos"

#NPCs
Cadmon = 31296
Helmut = 31258
Naran = 31378

#Item
Box = 7232

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
     htmltext = event
     if event == "31296-03.htm" :
       if st.getPlayer().getLevel() >= 74 :
            st.set("cond","1")
            st.setState(State.STARTED)
            st.playSound("ItemSound.quest_accept")
            htmltext = "31296-03.htm"
       else :
            htmltext = "31296-02.htm"
            st.exitQuest(1)
     elif event == "31258-02.htm" :
         st.set("cond","2")
         htmltext = "31258-02.htm"
         st.giveItems(Box,1)
     elif event == "31378-02.htm" :
         htmltext = "31378-02.htm"
         st.takeItems(Box,-1)
         st.addExpAndSp(79761,0)
         st.set("cond","0")
         st.set("onlyone","1")
         st.exitQuest(False)
         st.playSound("ItemSound.quest_finish")
     return htmltext

 def onTalk (self,npc,player):
     npcId = npc.getNpcId()
     htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
     st = player.getQuestState(qn)
     if not st : return htmltext

     cond = st.getInt("cond")
     onlyone = st.getInt("onlyone")
     if st.getState() == State.CREATED :
        st.set("cond","0")
        st.set("onlyone","0")
     if onlyone == 0 :
         if npcId == Cadmon :
             if cond == 0 :
                 htmltext = "31296-01.htm"
             elif cond == 1 :
                 htmltext = "31296-04.htm"
         elif st.getState() == State.STARTED :   
             if npcId == Helmut :
                 if cond == 1 :
                     htmltext = "31258-01.htm"
                 elif cond == 2 :
                     htmltext = "31258-03.htm"
             elif npcId == Naran and cond == 2 :
                 htmltext = "31378-01.htm"
     return htmltext
     
QUEST       = Quest(12, qn, "Secret Meeting With Varka Silenos")


QUEST.addStartNpc(Cadmon)

QUEST.addTalkId(Cadmon)

QUEST.addTalkId(Helmut)
QUEST.addTalkId(Naran)