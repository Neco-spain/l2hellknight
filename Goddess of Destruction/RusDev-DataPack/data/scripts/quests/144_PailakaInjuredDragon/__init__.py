import sys
from com.l2js.gameserver.datatables            import SkillTable
from com.l2js.gameserver.instancemanager       import InstanceManager
from com.l2js.gameserver.model.actor.instance  import L2PcInstance
from com.l2js.gameserver.model.entity	         import Instance
from com.l2js.gameserver.model.quest           import State
from com.l2js.gameserver.model.quest           import QuestState
from com.l2js.gameserver.model.quest.jython    import QuestJython as JQuest
from com.l2js.gameserver.network.serverpackets import PlaySound
from com.l2js.util                             import Rnd

qn = "144_PailakaInjuredDragon"

#NPC
KETRAOSHAMAN = 32499
KOSUPPORTER  = 32502
KOIO         = 32509
KOSUPPORTER2 = 32512
Pailaka3rd   = [18635,18636,18638,18639,18640,18641,18642,18644,18645,18646,18648,18649,18650,18652,18653,18654,18655,18656,18657,18658,18659]
Antelopes    = [18637,18643,18647,18651]
#BOSS
LATANA    = 18660
#ITEMS
SPEAR     = 13052
ENCHSPEAR = 13053
LASTSPEAR = 13054
STAGE1    = 13056
STAGE2    = 13057
PAILAKA3DROP = [8600,8601,8603,8604]
ANTELOPDROP  = [13032,13033]
#REWARDS
PSHIRT    = 13296
#ETC
AMOUNTS1  = [1,2,3,4,5,6,7,8,9,10]

BUFFS={
"1":[4357,2],#Haste Lv2
"2":[4342,2],#Wind Walk Lv2
"3":[4356,3],#Empower Lv3
"4":[4355,3],#Acumen Lv3
"5":[4351,6],#Concentration Lv6
"6":[4345,3],#Might Lv3
"7":[4358,3],#Guidance Lv3
"8":[4359,3],#Focus Lv3
"9":[4360,3],#Death Wisper Lv3
"10":[4352,2],#Berserker Spirit Lv2
"11":[4354,4],#Vampiric Rage Lv4
"12":[4347,6],#Blessed Body Lv6
}

def isWithinLevel(player):
	 if player.getLevel() > 77:
		 return False
	 if player.getLevel() < 73:
		 return False
	 return True

class Quest (JQuest):

 def __init__(self,id,name,descr): 
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [STAGE1,STAGE2,SPEAR,ENCHSPEAR,LASTSPEAR,13033,13032]
     self.currentWorld = 0
     self.KilledMobs = 0
     self.Action = {}

 def onAdvEvent (self,event,npc,player) :
     st = player.getQuestState(qn)
     if not st: return
     if str(event) in BUFFS.keys():
         skillId,level=BUFFS[event]
         playerName = player.getName()
         if self.Action[playerName]['times'] < 4:
             npc.setTarget(player)
             npc.doCast(SkillTable.getInstance().getInfo(skillId,level))
             self.Action[playerName]['times'] += 1
             htmltext = "32509-06.htm"
             return htmltext
         if self.Action[playerName]['times'] == 4:
             npc.setTarget(player)
             npc.doCast(SkillTable.getInstance().getInfo(skillId,level))
             self.Action[playerName]['times'] = 5
             htmltext = "32509-05.htm"
             return htmltext
     if event == "Support":
         playerName = player.getName()
         if not playerName in self.Action:
		     htmltext = "32509-06.htm"
		     self.Action[playerName] = {}
		     self.Action[playerName]['times'] = 0
         if playerName in self.Action:
             if self.Action[playerName]['times'] < 5:
                 htmltext = "32509-06.htm"
         if self.Action[playerName]['times'] >= 5:
             htmltext = "32509-04.htm"
         return htmltext
     cond = st.getInt("cond")
     htmltext = event
     if event == "32499-02.htm":
	     st.set("cond","1")
	     st.setState(State.STARTED)
	     st.playSound("ItemSound.quest_accept")
     elif event == "32499-05.htm":
	     st.set("cond","2")
	     st.playSound("ItemSound.quest_accept")
     elif event == "32502-05.htm":
		 st.set("cond","3")
		 st.playSound("ItemSound.quest_middle")
		 st.giveItems(SPEAR,1)
     elif event == "32512-02.htm":
	     st.takeItems(SPEAR,1)
	     st.takeItems(ENCHSPEAR,1)
	     st.takeItems(LASTSPEAR,1)
     return htmltext


 def onTalk (self,npc,player):
     npcId = npc.getNpcId()
     htmltext = "32499-01.htm"
     st = player.getQuestState(qn)
     if not st: return htmltext
     id = st.getState()
     cond = st.getInt("cond")
     playerName = player.getName()
     if id == State.CREATED:
         st.setState(State.STARTED)
         st.set("cond","0")
     if npcId == KETRAOSHAMAN:
	     if cond == 0 and id == State.STARTED:
		     if not isWithinLevel(player):
			     htmltext = "32499-no.htm"
			     st.exitQuest(1)
		     else:
			     self.Action[playerName] = {}
			     self.Action[playerName]['times'] = 0
			     return htmltext
	     elif id == State.COMPLETED:
		     htmltext = "32499-no.htm"
	     elif cond == 1 or cond == 2 or cond == 3:
		     htmltext = "32499-06.htm"
	     else:
		     htmltext = "32499-07.htm"
     elif npcId == KOSUPPORTER:
	     if cond == 1 or cond == 2:
		     htmltext = "32502-01.htm"
	     else:
		     htmltext = "32502-05.htm"
     elif npcId == KOIO:
	     if st.getQuestItemsCount(SPEAR) > 0 and st.getQuestItemsCount(STAGE1) == 0: htmltext = "32509-01.htm"
	     if st.getQuestItemsCount(ENCHSPEAR) > 0 and st.getQuestItemsCount(STAGE2) == 0: htmltext = "32509-01.htm"
	     if st.getQuestItemsCount(SPEAR) == 0 and st.getQuestItemsCount(STAGE1) > 0: htmltext = "32509-07.htm"
	     if st.getQuestItemsCount(ENCHSPEAR) == 0 and st.getQuestItemsCount(STAGE2) > 0: htmltext = "32509-07.htm" 
	     if st.getQuestItemsCount(SPEAR) == 0 and st.getQuestItemsCount(ENCHSPEAR) == 0: htmltext = "32509-07.htm"
	     if st.getQuestItemsCount(STAGE1) == 0 and st.getQuestItemsCount(STAGE2) == 0: htmltext = "32509-01.htm"
	     if st.getQuestItemsCount(SPEAR) > 0 and st.getQuestItemsCount(STAGE1) > 0:
		     st.takeItems(SPEAR,1)
		     st.takeItems(STAGE1,1)
		     st.giveItems(ENCHSPEAR,1)
		     htmltext = "32509-02.htm"
	     if st.getQuestItemsCount(ENCHSPEAR) > 0 and st.getQuestItemsCount(STAGE2) > 0:
		     st.takeItems(ENCHSPEAR,1)
		     st.takeItems(STAGE2,1)
		     st.giveItems(LASTSPEAR,1)
		     htmltext = "32509-03.htm"
	     if st.getQuestItemsCount(LASTSPEAR) > 0: htmltext = "32509-03.htm"
     elif npcId == KOSUPPORTER2:
	     if cond == 4:
			 st.giveItems(736,1)
			 st.takeItems(13032,st.getQuestItemsCount(13032))
			 st.takeItems(13033,st.getQuestItemsCount(13033))
			 st.giveItems(PSHIRT,1)
			 st.addRewardExpAndSp(28000000, 2850000)
			 st.set("cond","5")
			 st.setState(State.COMPLETED)
			 st.playSound("ItemSound.quest_finish")
			 st.exitQuest(False)
			 instanceObj = InstanceManager.getInstance().getInstance(player.getInstanceId())
			 instanceObj.setDuration(300000)
			 htmltext = "32512-01.htm"
			 player.setVitalityPoints(20000,true)
	     elif id == State.COMPLETED:
		     htmltext = "32512-03.htm"
     return htmltext
   
 def onKill(self,npc,player,isPet):
     st = player.getQuestState(qn)
     if not st: return
     npcId = npc.getNpcId()
     cond = st.getInt("cond")
     if npcId == 18654:
	     if st.getQuestItemsCount(STAGE1) < 1 and st.getQuestItemsCount(SPEAR) > 0:
		     st.giveItems(STAGE1,1)
     elif npcId == 18649 and st.getQuestItemsCount(ENCHSPEAR) > 0:
	     if st.getQuestItemsCount(STAGE2) < 1:
		     st.giveItems(STAGE2,1)
     elif npcId == LATANA:
	     st.set("cond","4")
	     st.playSound("ItemSound.quest_middle")
	     Dwarf = self.addSpawn(KOSUPPORTER2,npc.getX(),npc.getY(),npc.getZ(),npc.getHeading(),False,0,False,npc.getInstanceId())
     elif npcId in Pailaka3rd:
	     st.dropItem(npc,player,PAILAKA3DROP[Rnd.get(len(PAILAKA3DROP))],1)
     elif npcId in Antelopes:
	     st.dropItem(npc,player,ANTELOPDROP[Rnd.get(len(ANTELOPDROP))],AMOUNTS1[Rnd.get(len(AMOUNTS1))])
 
QUEST       = Quest(144,qn,"Pailaka Injured Dragon")

QUEST.addStartNpc(KETRAOSHAMAN)
QUEST.addTalkId(KETRAOSHAMAN)
QUEST.addTalkId(KOSUPPORTER)
QUEST.addTalkId(KOIO)
QUEST.addTalkId(KOSUPPORTER2)
QUEST.addKillId(18654)
QUEST.addKillId(18649)
QUEST.addKillId(LATANA)
for i in Pailaka3rd:
    QUEST.addKillId(i)
for i in Antelopes:
    QUEST.addKillId(i)

