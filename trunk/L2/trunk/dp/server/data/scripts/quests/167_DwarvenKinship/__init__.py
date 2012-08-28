# Made by Mr. - version 0.4 by DrLecter
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

qn = "167_DwarvenKinship"

COLLETTE_LETTER = 1076
NORMANS_LETTER = 1106
ADENA = 57

COLLETTE,NORMAN,HAPROCK = 30350,30210,30255

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [COLLETTE_LETTER, NORMANS_LETTER]

 def onEvent (self,event,st) :
    if st.getState() != State.COMPLETED :
     htmltext = event
     cond = st.getInt("cond")
     collette = st.getQuestItemsCount(COLLETTE_LETTER)
     if event == "30350-04.htm" and cond == 0 :
       st.giveItems(COLLETTE_LETTER,1)
       st.set("cond","1")
       st.setState(State.STARTED)
       st.playSound("ItemSound.quest_accept")
     elif event == "30255-03.htm" and cond == 1 and collette :
       st.set("cond","2")
       st.takeItems(COLLETTE_LETTER,1)
       st.giveItems(NORMANS_LETTER,1)
       st.giveItems(ADENA,2000)
     elif event == "30255-04.htm" and cond == 1 and collette :
       st.takeItems(COLLETTE_LETTER,1)
       st.giveItems(ADENA,3000)
       st.unset("cond")
       st.exitQuest(False)
       st.playSound("ItemSound.quest_finish")
     elif event == "30210-02.htm" and cond == 2 and st.getQuestItemsCount(NORMANS_LETTER) :
       st.takeItems(NORMANS_LETTER,1)
       st.giveItems(ADENA,20000)
       st.unset("cond")
       st.exitQuest(False)
       st.playSound("ItemSound.quest_finish")
    return htmltext


 def onTalk (self,npc,player):
   htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
   st = player.getQuestState(qn)
   if not st : return htmltext

   npcId = npc.getNpcId()
   collette = st.getQuestItemsCount(COLLETTE_LETTER)
   norman = st.getQuestItemsCount(NORMANS_LETTER)
   id = st.getState()
   cond = st.getInt("cond")
   if id == State.COMPLETED :
     htmltext = "<html><body>This quest has already been completed.</body></html>"

   elif npcId == COLLETTE :
     if cond == 0 :
       if player.getLevel() >= 15 :
         htmltext = "30350-03.htm"
       else:
         htmltext = "30350-02.htm"
         st.exitQuest(1)
     elif cond == 1 and collette :
       htmltext = "30350-05.htm"
   elif id == State.STARTED :    
       if npcId == HAPROCK :
         if cond == 1 and collette :
           htmltext = "30255-01.htm"
         elif cond == 2 and norman :
           htmltext = "30255-05.htm"
       elif npcId == NORMAN and cond == 2 and norman :
          htmltext = "30210-01.htm"
   return htmltext

QUEST       = Quest(167,qn,"Dwarven Kinship")

QUEST.addStartNpc(COLLETTE)

QUEST.addTalkId(COLLETTE)

QUEST.addTalkId(NORMAN)
QUEST.addTalkId(HAPROCK)