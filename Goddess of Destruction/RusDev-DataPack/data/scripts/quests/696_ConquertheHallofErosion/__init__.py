#authoor by d0S

import sys

from com.l2js.gameserver.datatables import SkillTable
from com.l2js.gameserver.model.quest        			import State
from com.l2js.gameserver.model.quest        			import QuestState
from com.l2js.gameserver.model.quest.jython 			import QuestJython as JQuest

qn = "696_ConquertheHallofErosion"

#NPCs
Keucereus = 32548
Tepios = 32603
Mouthofekimus=32537
Klodekus = 25665
Klanikus = 25666

#items
Mark = 13692

class Quest (JQuest) :
    def __init__(self,id,name,descr):
        JQuest.__init__(self,id,name,descr)
        self.questItemIds = [Mark]

    def onAdvEvent (self,event,npc, player) :
        htmltext = event
        st = player.getQuestState(qn)
        if not st : return
        if event == "32530-02.htm" :
            st.setState(State.STARTED)
            st.playSound("ItemSound.quest_accept")
        return htmltext

    def onTalk (self,npc,player):
        htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
        st = player.getQuestState(qn)
        if not st : return htmltext
        npcId = npc.getNpcId()
        id = st.getState()
	if id == State.COMPLETED :
            htmltext = "32530-03.htm"
        elif id == State.CREATED and npcId == Tepios:
            if player.getLevel() >= 80:
	       htmltext = "32530-01.htm"	
            else :
                htmltext = "32530-00.htm"
        elif id == State.STARTED and npcId == Mouthofekimus:
            htmltext = "32537-01.htm"
	elif id == State.STARTED and npcId == Tepios:
	    htmltext = "32530-04.htm"
	    st.exitQuest(True)
	    if st.getQuestItemsCount(Mark) == 0:
	    	st.giveItems(13692,1)
	    st.playSound("ItemSound.quest_finish")    
	return htmltext
       

QUEST       = Quest(696,qn,"Conquer the Hall of Erosion")

QUEST.addStartNpc(Tepios)
QUEST.addTalkId(Tepios)
QUEST.addStartNpc(Mouthofekimus)
QUEST.addTalkId(Mouthofekimus)
