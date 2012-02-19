import sys
from l2.brick.gameserver.model.quest           import State
from l2.brick.gameserver.model.quest           import QuestState
from l2.brick.gameserver.model.quest.jython    import QuestJython as JQuest
from l2.brick.gameserver.model.actor.instance  import L2PcInstance
from l2.brick.util                             import Rnd
from l2.brick.gameserver.datatables            import ItemTable
from l2.brick.gameserver.datatables            import DoorTable
from l2.brick.gameserver.network.serverpackets import NpcSay

qn = "HellboundTraitor"

#NPC
TRAITOR = 32364

D1 = 19250003
D2 = 19250004

class Quest (JQuest):
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
	
	def onAdvEvent (self,event,npc,player):
		if event == "Close_Door1":
			DoorTable.getInstance().getDoor(D1).closeMe()
		elif event == "Close_Door2":
			DoorTable.getInstance().getDoor(D2).closeMe()
		return
		
	def onTalk (self, npc, player) :
		npcId = npc.getNpcId()
		st = player.getQuestState(qn)
		if not st :
			st = self.newQuestState(player)
		if npcId == TRAITOR :
			if st.getQuestItemsCount(9676) >= 10 :
				st.takeItems(9676,10)
				DoorTable.getInstance().getDoor(D1).openMe()
				self.startQuestTimer("Close_Door1",600000,None,None)
				DoorTable.getInstance().getDoor(D2).openMe()
				self.startQuestTimer("Close_Door2",600000,None,None)
				npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"Brothers! This stranger wants to kill our Commander!!!"))
				htmltext = ""
			else :
				htmltext = "32364-1.htm"
				st.exitQuest(1) 
		return htmltext

QUEST = Quest(-1, qn, "zone_scripts/Hellbound")

QUEST.addStartNpc(TRAITOR)

QUEST.addTalkId(TRAITOR)