#Created 10.06.2011 12:47
#Author Sarkazm
#Not tested
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest
from java.lang import System
from net.sf.l2j.gameserver.model import L2Party
from net.sf.l2j.gameserver.model import L2Character
from net.sf.l2j.gameserver.model.actor.instance import L2PcInstance

qn = "6000_PrimevalVervato"
VERVATO = 32104
ADENA = 57

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
        
 def onTalk (self,npc,player):
   st = player.getQuestState("6000_PrimevalVervato")  
   npcId = npc.getNpcId()
   party = player.getParty()
   if npcId == VERVATO :
     if st.getQuestItemsCount(ADENA) >= 2000000:
       if party:
         for player in party.getPartyMembers() :
           if player.isAlikeDead():
             st.takeItems(ADENA,2000000)
             player.teleToLocation(11563,-23429,-3643)
             htmltext = "ok.htm"
           else:
             htmltext = "netmertvih.htm"
       else :
         htmltext = "netparty.htm"
     else :
       htmltext = "nehvataet.htm"
   return htmltext

QUEST       = Quest(6000,qn,"Teleports")

QUEST.addStartNpc(VERVATO)
QUEST.addTalkId(VERVATO)
