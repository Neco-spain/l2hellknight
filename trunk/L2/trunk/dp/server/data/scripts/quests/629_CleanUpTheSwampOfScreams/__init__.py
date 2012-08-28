# Made by Hawkin
import sys
from net.sf.l2j import Config
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

qn = "629_CleanUpTheSwampOfScreams"

#NPC
CAPTAIN = 31553
#ITEMS
CLAWS = 7250
COIN = 7251
#CHANCES
MAX=1000
CHANCE={
    21508:500,
    21509:431,
    21510:521,
    21511:576,
    21512:746,
    21513:530,
    21514:538,
    21515:545,
    21516:553,
    21517:560
}

default="<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [CLAWS]
 
 def onEvent (self,event,st) :
   htmltext = event
   if event == "31553-1.htm" :
     if st.getPlayer().getLevel() >= 66 :
       st.set("cond","1")
       st.setState(State.STARTED)
       st.playSound("ItemSound.quest_accept")
     else:
       htmltext=default
       st.exitQuest(1)
   elif event == "31553-3.htm" :
     if st.getQuestItemsCount(CLAWS) >= 100 :
       st.takeItems(CLAWS,100)
       st.giveItems(COIN,20)
     else :
       htmltext = "31553-3a.htm"
   elif event == "31553-5.htm" :
     st.playSound("ItemSound.quest_finish")
     st.exitQuest(1)
   return htmltext

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   htmltext = default
   if st :
       npcId = npc.getNpcId()
       id = st.getState()
       cond = st.getInt("cond")
       if (st.getQuestItemsCount(7246) or st.getQuestItemsCount(7247)) :
         if cond == 0 :
           if player.getLevel() >= 66 :
             htmltext = "31553-0.htm"
           else:
             htmltext = "31553-0a.htm"
             st.exitQuest(1)
         elif id == State.STARTED :
             if st.getQuestItemsCount(CLAWS) >= 100 :
               htmltext = "31553-2.htm"
             else :
               htmltext = "31553-1a.htm"
       else :
         htmltext = "31553-6.htm"
         st.exitQuest(1)
   return htmltext

 def onKill(self,npc,player,isPet):
    partyMember = self.getRandomPartyMemberState(player, State.STARTED)
    if not partyMember : return
    st = partyMember.getQuestState(qn)
    if st :
        if st.getState() == State.STARTED :
            prevItems = st.getQuestItemsCount(CLAWS)
            random = st.getRandom(MAX)
            chance = CHANCE[npc.getNpcId()]*Config.RATE_DROP_QUEST
            numItems, chance = divmod(chance,MAX)
            if random<chance :
                numItems += 1
            st.giveItems(CLAWS,int(numItems))
            if int(prevItems+numItems)/100 > int(prevItems)/100 :
                st.playSound("ItemSound.quest_middle")
            else:
                st.playSound("ItemSound.quest_itemget")
    return

QUEST       = Quest(629,qn,"Clean Up the Swamp of Screams")

QUEST.addStartNpc(CAPTAIN)

QUEST.addTalkId(CAPTAIN)

for mobs in range(21508,21518) :
  QUEST.addKillId(mobs)