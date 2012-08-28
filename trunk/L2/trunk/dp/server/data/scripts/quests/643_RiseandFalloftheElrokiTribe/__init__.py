# Created by Gigiikun
# Changed by Sarkazm
import sys
from net.sf.l2j import Config
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

qn = "643_RiseandFalloftheElrokiTribe"

#Settings: drop chance in %
DROP_CHANCE = 75

BONES_OF_A_PLAINS_DINOSAUR = 8776
PLAIN_DINOSAURS = [22208,22209,22210,22211,22212,22213,22221,22222,22226,22227]
REWARDS = range(8712,8723)
REWARDSBOOK = [7667,7665,7666,7671,7659,7655,7647,7648,8881,8882,8880,8879,8878,8877,8883,8884,8892,8893,8894,8895,8896,8897,8898,8899,8900,8901,8902,8903,8904,8905,8906,8907,8908,7673,7676,8889,8888,8887,8886,8885]

class Quest (JQuest) :

 def __init__(self,id,name,descr):
 	JQuest.__init__(self,id,name,descr)
 	self.questItemIds = [BONES_OF_A_PLAINS_DINOSAUR]

 def onEvent (self,event,st) :
    htmltext = event
    count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR)
    if event == "None" :
        return
    elif event == "32106-03.htm" :
       st.set("cond","1")
       st.setState(State.STARTED)
       st.playSound("ItemSound.quest_accept")
    elif event == "32117-05.htm" :
       if count >= 300 :
          st.takeItems(BONES_OF_A_PLAINS_DINOSAUR,300)
          st.giveItems(REWARDSBOOK[st.getRandom(len(REWARDSBOOK))],int(1*Config.RATE_QUESTS_REWARD))
          st.giveItems(REWARDSBOOK[st.getRandom(len(REWARDSBOOK))],int(1*Config.RATE_QUESTS_REWARD))
          st.giveItems(REWARDSBOOK[st.getRandom(len(REWARDSBOOK))],int(1*Config.RATE_QUESTS_REWARD))
       else :
          htmltext = "32117-04.htm"
    elif event == "32117-03.htm" :
       if count >= 300 :
          st.takeItems(BONES_OF_A_PLAINS_DINOSAUR,300)
          st.giveItems(REWARDS[st.getRandom(len(REWARDS))],int(5*Config.RATE_QUESTS_REWARD))
       else :
          htmltext = "32117-04.htm"
    elif event == "Quit" :
       st.playSound("ItemSound.quest_finish")
       st.exitQuest(1)
       return
    return htmltext

 def onTalk (self, npc, player):
    st = player.getQuestState(qn)
    htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
    if st :
       npcId = npc.getNpcId()
       cond = st.getInt("cond")
       count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR)
       if cond == 0 and npcId == 32106:
          if player.getLevel() >= 75 :
             htmltext = "32106-01.htm"
          else :
             htmltext = "32106-00.htm"
             st.exitQuest(1)
       elif st.getState() == State.STARTED :
          if npcId == 32106 :
             if count == 0 :
                htmltext = "32106-05.htm"
             else :
                htmltext = "32106-06.htm"
                st.takeItems(BONES_OF_A_PLAINS_DINOSAUR,-1)
                st.giveItems(57,count*1374)
          elif npcId == 32117 :
             htmltext = "32117-01.htm"
    return htmltext

 def onKill (self, npc, player,isPet):
    partyMember = self.getRandomPartyMember(player,"1")
    if not partyMember: return
    st = partyMember.getQuestState(qn)
    if st :
       if st.getState() == State.STARTED :
          npcId = npc.getNpcId()
          cond = st.getInt("cond")
          count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR)
          if cond == 1 :
             chance = DROP_CHANCE*Config.RATE_DROP_QUEST
             numItems, chance = divmod(chance,100)
             if st.getRandom(100) < chance : 
                numItems += 1
             if numItems :
                if int(count + numItems)/300 > int(count)/300 :
                   st.playSound("ItemSound.quest_middle")
                else :
                   st.playSound("ItemSound.quest_itemget")
                st.giveItems(BONES_OF_A_PLAINS_DINOSAUR,int(numItems))
    return

QUEST = Quest(643,qn,"Rise and Fall of the Elroki Tribe")

QUEST.addStartNpc(32106)

QUEST.addTalkId(32106)
QUEST.addTalkId(32117)

for mob in PLAIN_DINOSAURS :
   QUEST.addKillId(mob)
