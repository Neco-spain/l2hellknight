# Made by disKret
import sys
from l2.hellknight.gameserver.model.quest import State
from l2.hellknight.gameserver.model.quest import QuestState
from l2.hellknight.gameserver.model.quest.jython import QuestJython as JQuest

qn = "246_PossessorOfAPreciousSoul_3"

#NPC
LADD = 30721
CARADINE = 31740
OSSIAN = 31741

#QUEST ITEM
CARADINE_LETTER = 7678
CARADINE_LETTER_LAST = 7679
WATERBINDER = 7591
EVERGREEN = 7592
RAIN_SONG = 7593
RELIC_BOX = 7594
FRAGMENTS = 21725

#MOBS
PILGRIM_OF_SPLENDOR = 21541
JUDGE_OF_SPLENDOR = 21544
BARAKIEL = 25325
MOBS = [21535,21536,21537,21538,21539,21540]

#CHANCE FOR DROP
CHANCE_FOR_DROP = 5
CHANCE_FOR_DROP_FRAGMENTS = 30 # Not verifed!

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [WATERBINDER, EVERGREEN, RAIN_SONG, RELIC_BOX]

 def onAdvEvent (self,event,npc, player) :
   htmltext = event
   st = player.getQuestState(qn)
   if not st : return
   cond = st.getInt("cond")
   if event == "31740-4.htm" :
     if cond == 0 :
       st.setState(State.STARTED)
       st.takeItems(CARADINE_LETTER,1)
       st.set("cond","1")
       st.playSound("ItemSound.quest_accept")
   elif event == "31741-2.htm" :
     if cond == 1 :
       st.set("cond","2")
       st.set("awaitsWaterbinder","1")
       st.set("awaitsEvergreen","1")
       st.playSound("ItemSound.quest_middle")
   elif event == "31741-5.htm" :
     if cond == 3 :
        st.set("cond","4")
        st.takeItems(WATERBINDER,1)
        st.takeItems(EVERGREEN,1)
        st.playSound("ItemSound.quest_middle")
   elif event == "31741-9.htm" :
     if cond == 5 :
       st.set("cond","6")
       if st.getQuestItemsCount(RAIN_SONG) == 1:
          st.takeItems(RAIN_SONG,1)
       if st.getQuestItemsCount(FRAGMENTS) >= 100:
          st.takeItems(FRAGMENTS,-1)
       st.giveItems(RELIC_BOX,1)
       st.playSound("ItemSound.quest_middle")
   elif event == "30721-2.htm" :
     if cond == 6 :
       st.set("cond","0")
       st.takeItems(RELIC_BOX,1)
       st.giveItems(CARADINE_LETTER_LAST,1)
       st.addExpAndSp(719843,0)
       st.playSound("ItemSound.quest_finish")
       st.exitQuest(False)
   return htmltext

 def onTalk (self,npc,player):
   htmltext = Quest.getNoQuestMsg(player)
   st = player.getQuestState(qn)
   if not st : return htmltext
   npcId = npc.getNpcId()
   id = st.getState()
   if npcId != CARADINE and id != State.STARTED : return htmltext
   cond=st.getInt("cond")
   if player.isSubClassActive() :
     if npcId == CARADINE :
         if cond == 0 and st.getQuestItemsCount(CARADINE_LETTER) == 1 :
           if id == State.COMPLETED :
             htmltext = Quest.getAlreadyCompletedMsg(player)
           elif player.getLevel() < 65 : 
             htmltext = "31740-2.htm"
             st.exitQuest(1)
           elif player.getLevel() >= 65 :
             htmltext = "31740-1.htm"
         elif cond == 1 :
             htmltext = "31740-5.htm"
     elif npcId == OSSIAN:
         if cond == 1 :
             htmltext = "31741-1.htm"
         elif cond == 2 :
           htmltext = "31741-4.htm"
         elif cond == 3 and st.getQuestItemsCount(WATERBINDER) == 1 and st.getQuestItemsCount(EVERGREEN) == 1 :
           htmltext = "31741-3.htm"
         elif cond == 4 :
           htmltext = "31741-8.htm"
         elif cond == 5 and st.getQuestItemsCount(RAIN_SONG) == 1 or st.getQuestItemsCount(FRAGMENTS) >= 100:
           htmltext = "31741-7.htm"
         elif cond == 6 and st.getQuestItemsCount(RELIC_BOX) == 1 :
           htmltext = "31741-11.htm"
     elif npcId == LADD and cond == 6 :
       htmltext = "30721-1.htm"
   else :
     htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>"
   return htmltext

 def onKill(self,npc,player,isPet):
   npcId = npc.getNpcId()
   if npcId == PILGRIM_OF_SPLENDOR :
     #get a random party member who is doing this quest and needs this drop 
     partyMember = self.getRandomPartyMember(player,"awaitsWaterbinder","1")
     if partyMember :
         st = partyMember.getQuestState(qn)
         chance = st.getRandom(100)
         cond = st.getInt("cond")
         if st.getQuestItemsCount(WATERBINDER) < 1 :
           if chance < CHANCE_FOR_DROP :
             st.giveItems(WATERBINDER,1)
             st.unset("awaitsWaterbinder")
             if st.getQuestItemsCount(EVERGREEN) < 1 :
               st.playSound("ItemSound.quest_itemget")
             else:
               st.playSound("ItemSound.quest_middle")
               st.set("cond","3")
   elif npcId == JUDGE_OF_SPLENDOR :
     #get a random party member who is doing this quest and needs this drop 
     partyMember = self.getRandomPartyMember(player,"awaitsEvergreen","1")
     if partyMember :
         st = partyMember.getQuestState(qn)
         chance = st.getRandom(100)
         cond = st.getInt("cond")
         if cond == 2 and st.getQuestItemsCount(EVERGREEN) < 1 :
           if chance < CHANCE_FOR_DROP :
             st.giveItems(EVERGREEN,1)
             st.unset("awaitsEvergreen")
             if st.getQuestItemsCount(WATERBINDER) < 1 :
               st.playSound("ItemSound.quest_itemget")
             else:
               st.playSound("ItemSound.quest_middle")
               st.set("cond","3")
   elif npcId == BARAKIEL :
     #give the quest item and update variables for ALL PARTY MEMBERS who are doing the quest,
     #so long as they each qualify for the drop (cond == 4 and item not in inventory)
     #note: the killer WILL participate in the loop as a party member (no need to handle separately)
     party = player.getParty()
     if party :
        for partyMember in party.getPartyMembers().toArray() :
            pst = partyMember.getQuestState(qn)
            if pst :
                if pst.getInt("cond") == 4 and pst.getQuestItemsCount(RAIN_SONG) < 1 :
                    pst.giveItems(RAIN_SONG,1)
                    pst.playSound("ItemSound.quest_middle")
                    pst.set("cond","5")
     else :
        pst = player.getQuestState(qn)
        if pst :
            if pst.getInt("cond") == 4 and pst.getQuestItemsCount(RAIN_SONG) < 1 :
                pst.giveItems(RAIN_SONG,1)
                pst.playSound("ItemSound.quest_middle")
                pst.set("cond","5")
   else :
        st = player.getQuestState(qn)
        if not st or st.getQuestItemsCount(FRAGMENTS) >= 100 or st.getInt("cond") != 4:
            return
        for id in MOBS:
            if npcId == id and st.getRandom(100) < CHANCE_FOR_DROP_FRAGMENTS:
                st.giveItems(FRAGMENTS,1)
                if st.getQuestItemsCount(FRAGMENTS) >= 100:
                    st.set("cond","5")
                
   return 

QUEST       = Quest(246,qn,"Possessor Of A Precious Soul - 3")

QUEST.addStartNpc(CARADINE)
QUEST.addTalkId(CARADINE)

QUEST.addTalkId(OSSIAN)
QUEST.addTalkId(LADD)

QUEST.addKillId(PILGRIM_OF_SPLENDOR)
QUEST.addKillId(JUDGE_OF_SPLENDOR)
QUEST.addKillId(BARAKIEL)

for id in MOBS:
    QUEST.addKillId(id)
