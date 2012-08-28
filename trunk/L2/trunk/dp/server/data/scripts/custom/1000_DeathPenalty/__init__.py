import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest
from java.lang import System
from net.sf.l2j.gameserver.model import L2Party
from net.sf.l2j.gameserver.model import L2Character
from net.sf.l2j.gameserver.model.actor.instance import L2PcInstance

qn = "1000_DeathPenalty"
JUDGE = 30981
ADENA = 57

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
        
 def onTalk (self,npc,player):
   st = player.getQuestState("1000_DeathPenalty")  
   npcId = npc.getNpcId()
   party = player.getParty()
   if npcId == JUDGE :
     if st.getQuestItemsCount(ADENA) >= 3600:
           if player.getDeathPenaltyBuffLevel() > 0:
             st.takeItems(ADENA,3600)
             player.setDeathPenaltyBuffLevel(player.getDeathPenaltyBuffLevel()-1)
             htmltext = "ok.htm"
           else:
             htmltext = "netdeathpenalty.htm"
     else :
       htmltext = "nehvataet.htm"
   return htmltext

QUEST       = Quest(1000,qn,"custom")

QUEST.addStartNpc(JUDGE)
QUEST.addTalkId(JUDGE)
