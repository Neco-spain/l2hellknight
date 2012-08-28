# Made by BiTi! v0.2
# v0.2.1 by DrLecter
import sys
from net.sf.l2j import Config
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

qn = "637_ThroughOnceMore"

#Drop rate
DROP_CHANCE=90
#Npc
FLAURON = 32010
#Items
VISITOR_MARK,FADEDMARK,NECROHEART,MARK = 8064,8065,8066,8067

class Quest (JQuest) :


 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [NECROHEART]

 def onEvent (self,event,st) :
    htmltext = event
    if htmltext == "32010-03.htm" :
       st.set("cond","1")
       st.setState(State.STARTED)
       st.takeItems(FADEDMARK,1)
       st.playSound("ItemSound.quest_accept")
    return htmltext

 def onTalk (self, npc, player):
   htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
   st = player.getQuestState(qn)
   if st :
     id = st.getState()
     cond = st.getInt("cond")
     if id == State.CREATED :
        if player.getLevel()>72 and st.getQuestItemsCount(FADEDMARK) :
           htmltext = "32010-02.htm"
        elif player.getLevel()>72 and st.getQuestItemsCount(VISITOR_MARK) :
           htmltext = "32010-01a.htm"
           st.exitQuest(1)
        else:
           htmltext = "32010-01.htm"
           st.exitQuest(1)
     elif id == State.STARTED :
       if cond == 2 and st.getQuestItemsCount(NECROHEART)==10:
          htmltext = "32010-05.htm"
          st.takeItems(NECROHEART,10)
          st.giveItems(MARK,1)
          st.giveItems(8273,10)
          st.exitQuest(False)
          st.playSound("ItemSound.quest_finish")
       else :
          htmltext = "32010-04.htm"
   return htmltext

 def onKill(self,npc,player,isPet):
   st = player.getQuestState(qn)
   if st :
     if st.getState() == State.STARTED :
       count = st.getQuestItemsCount(NECROHEART)
       if st.getInt("cond") == 1 and count < 10 :
          chance = DROP_CHANCE * Config.RATE_DROP_QUEST
          numItems, chance = divmod(int(chance),100)
          if st.getRandom(100) < chance : 
             numItems += 1
          if numItems :
             if count + numItems >= 10 :
                numItems = 10 - count
                st.playSound("ItemSound.quest_middle")
                st.set("cond","2")
             else:
                st.playSound("ItemSound.quest_itemget")
             st.giveItems(NECROHEART,int(numItems))
   return

QUEST       = Quest(637,qn,"Through the Gate Once More")

QUEST.addStartNpc(FLAURON)

QUEST.addTalkId(FLAURON)

for mob in range(21565,21568):
    QUEST.addKillId(mob)