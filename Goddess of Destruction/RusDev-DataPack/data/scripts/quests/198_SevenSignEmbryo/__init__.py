# by knoxville OpenTeamFree 10.04.2010
# based on Freya PTS

import sys
from com.l2js.gameserver.ai import CtrlIntention
from com.l2js.gameserver.model.quest import State
from com.l2js.gameserver.model.quest import QuestState
from com.l2js.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2js.gameserver.network.serverpackets import NpcSay
from com.l2js.gameserver.network.serverpackets import ExStartScenePlayer

qn = "198_SevenSignEmbryo"

# NPCs
WOOD		= 32593
FRANZ		= 32597
JAINA		= 32582
SHILENSEVIL1	= 27346
SHILENSEVIL2	= 27343

# ITEMS
SCULPTURE	= 14360
BRACELET	= 15312
AA		= 5575
# Rates for reward Ancient Adena
AARATE		= 1

class PyObject :
	pass

class Quest (JQuest) :
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
		self.questItemIds = [SCULPTURE, BRACELET, AA]

	def onAdvEvent(self, event, npc, player) :
		htmltext = event
		st = player.getQuestState(qn)
		if not st : return

		if event == "32593-02.htm" :
			st.set("cond","1")
			st.setState(State.STARTED)
			st.playSound("ItemSound.quest_accept")
		elif event == "32597-10.htm" :
			st.set("cond","3")
			st.takeItems(SCULPTURE, 1)
			st.playSound("ItemSound.quest_middle")
		elif event == "32597-05.htm" :
				monster = self.addSpawn(SHILENSEVIL1, -23801, -9004, -5385, 0, False, 0, False, npc.getInstanceId())
				monster.broadcastPacket(NpcSay(monster.getObjectId(),0,monster.getNpcId(),"You are not the owner of that item!"))
				monster.setRunning()
				monster.addDamageHate(player,0,999)
				monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st.getPlayer())
				monster1 = self.addSpawn(SHILENSEVIL2, -23801, -9004, -5385, 0, False, 0, False, npc.getInstanceId())
				monster1.setRunning()
				monster1.addDamageHate(player,0,999)
				monster1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st.getPlayer())
				monster2 = self.addSpawn(SHILENSEVIL2, -23801, -9004, -5385, 0, False, 0, False, npc.getInstanceId())
				monster2.setRunning()
				monster2.addDamageHate(player,0,999)
				monster2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st.getPlayer())
		return htmltext

	def onTalk (self, npc, player) :
		htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
		st = player.getQuestState(qn)
		if not st : return htmltext
		npcId = npc.getNpcId()
		cond = st.getInt("cond")
		id = st.getState()
		if npcId == WOOD :
			first = player.getQuestState("197_SevenSignTheSacredBookOfSeal")
			if first and first.getState() == State.COMPLETED and id == State.CREATED and player.getLevel() >= 79 :
				htmltext = "32593-01.htm"
			elif cond == 1 :
				htmltext = "32593-02.htm"
			elif cond == 3 :
				htmltext = "32593-04.htm"
				st.addExpAndSp(315108096,34906059)
				st.giveItems(BRACELET, 1)
				st.giveItems(AA, 1500000*AARATE)
				st.unset("cond")
				st.takeItems(SCULPTURE, 1)
				st.setState(State.COMPLETED)
				st.exitQuest(False)
				st.playSound("ItemSound.quest_finish")
			elif cond == 0 :
				htmltext = "32593-00.htm"
				st.exitQuest(True)
		elif npcId == FRANZ :
			if cond == 1 :
				htmltext = "32597-01.htm"
			elif cond == 2 :
				htmltext = "32597-06.htm"
			elif cond == 3 :
				htmltext = "32597-11.htm"
		return htmltext

	def onKill(self, npc, player, isPet) :
		st = player.getQuestState(qn)
		if not st : return
		if npc.getNpcId() == SHILENSEVIL1 and st.getInt("cond") == 1 :
			npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),player.getName() + "... You may have won this time... But next time, I will surely capture you!"))
			st.giveItems(SCULPTURE, 1)
			st.set("cond", "2")
			player.showQuestMovie(14)
		return

QUEST	= Quest(198,qn,"Seven Signs Embryo")

QUEST.addStartNpc(WOOD)
QUEST.addTalkId(WOOD)
QUEST.addTalkId(FRANZ)
QUEST.addTalkId(JAINA)
QUEST.addKillId(SHILENSEVIL1)
QUEST.addKillId(SHILENSEVIL2)
