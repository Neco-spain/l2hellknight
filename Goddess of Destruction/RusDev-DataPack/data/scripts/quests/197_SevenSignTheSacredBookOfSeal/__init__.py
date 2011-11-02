# by knoxville OpenTeamFree 9.04.2010
# based on Freya PTS

import sys
from com.l2js.gameserver.ai import CtrlIntention
from com.l2js.gameserver.model.quest import State
from com.l2js.gameserver.model.quest import QuestState
from com.l2js.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2js.gameserver.network.serverpackets import NpcSay

qn = "197_SevenSignTheSacredBookOfSeal"

# NPCs
WOOD		= 32593
ORVEN		= 30857
LEOPARD		= 32594
LAWRENCE	= 32595
SOFIA		= 32596
SHILENSEVIL	= 27343

# ITEMS
TEXT		= 13829
SCULPTURE	= 14356

class Quest (JQuest) :
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
		self.questItemIds = [TEXT, SCULPTURE]

	def onAdvEvent(self, event, npc, player) :
		htmltext = event
		st = player.getQuestState(qn)
		if not st : return

		if event == "32593-04.htm" :
			st.set("cond","1")
			st.setState(State.STARTED)
			st.playSound("ItemSound.quest_accept")
		elif event == "30857-04.htm" :
			st.set("cond","2")
			st.playSound("ItemSound.quest_middle")
		elif event == "32594-03.htm" :
			st.set("cond","3")
			st.playSound("ItemSound.quest_middle")
		elif event == "32595-04.htm" :
				monster = self.addSpawn(SHILENSEVIL, 152520, -57685, -3438, 0, False, 60000, True)
				monster.broadcastPacket(NpcSay(monster.getObjectId(),0,monster.getNpcId(),"You are not the owner of that item!"))
				monster.setRunning()
				monster.addDamageHate(player,0,999)
				monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st.getPlayer())
		elif event == "32595-08.htm" :
			st.set("cond","5")
			st.playSound("ItemSound.quest_middle")
		elif event == "32596-04.htm" :
			st.set("cond","6")
			st.giveItems(TEXT, 1)
			st.playSound("ItemSound.quest_middle")
		elif event == "32593-08.htm" :
			st.addExpAndSp(52518015,5817676)
			st.unset("cond")
			st.takeItems(TEXT, 1)
			st.takeItems(SCULPTURE, 1)
			st.setState(State.COMPLETED)
			st.exitQuest(False)
			st.playSound("ItemSound.quest_finish")
		return htmltext

	def onTalk (self, npc, player) :
		htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
		st = player.getQuestState(qn)
		if not st : return htmltext

		npcId = npc.getNpcId()
		cond = st.getInt("cond")
		id = st.getState()
		if npcId == WOOD :
			first = player.getQuestState("196_SevenSignSealOfTheEmperor")
			if first and first.getState() == State.COMPLETED and id == State.CREATED and player.getLevel() >= 79 :
				htmltext = "32593-01.htm"
			elif cond == 1 :
				htmltext = "32593-05.htm"
			elif cond == 6 :
				htmltext = "32593-06.htm"
			elif cond == 0 :
				htmltext = "32593-00.htm"
				st.exitQuest(True)
		elif npcId == ORVEN :
			if cond == 1 :
				htmltext = "30857-01.htm"
			elif cond == 2 :
				htmltext = "30857-05.htm"
		elif npcId == LEOPARD :
			if cond == 2 :
				htmltext = "32594-01.htm"
			elif cond == 3 :
				htmltext = "32594-04.htm"
		elif npcId == LAWRENCE :
			if cond == 3:
				htmltext = "32595-01.htm"
			elif cond == 4 :
				htmltext = "32595-05.htm"
			elif cond == 5 :
				htmltext = "32595-09.htm"
		elif npcId == SOFIA :
			if cond == 5 :
				htmltext = "32596-01.htm"
			elif cond == 6 :
				htmltext = "32596-05.htm"
		return htmltext

	def onKill(self, npc, player, isPet) :
		st = player.getQuestState(qn)
		if not st : return
		if npc.getNpcId() == SHILENSEVIL and st.getInt("cond") == 3 :
			npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),player.getName() + "... You may have won this time... But next time, I will surely capture you!"))
			st.giveItems(SCULPTURE, 1)
			st.set("cond", "4")
		return

QUEST	= Quest(197,qn,"Seven Sign The Sacred Book Of Seal")

QUEST.addStartNpc(WOOD)
QUEST.addTalkId(WOOD)
QUEST.addTalkId(ORVEN)
QUEST.addTalkId(LEOPARD)
QUEST.addTalkId(LAWRENCE)
QUEST.addTalkId(SOFIA)
QUEST.addKillId(SHILENSEVIL)
