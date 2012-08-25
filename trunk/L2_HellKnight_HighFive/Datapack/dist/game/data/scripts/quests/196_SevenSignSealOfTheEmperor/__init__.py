# by knoxville OpenTeamFree 03.09.2010
# based on Freya PTS

import sys
from l2.hellknight.gameserver.ai                    import CtrlIntention
from l2.hellknight.gameserver.model.quest           import State
from l2.hellknight.gameserver.model.quest           import QuestState
from l2.hellknight.gameserver.model.quest.jython    import QuestJython as JQuest
from l2.hellknight.gameserver.network.serverpackets import NpcSay
from l2.hellknight.gameserver.network.serverpackets import ExStartScenePlayer

qn = "196_SevenSignSealOfTheEmperor"

# NPCs
HEINE       = 30969
MAMMON      = 32584
SHUNAIMAN   = 32586
SEALDEVICE  = 27384
SEALDEVICE2 = 18833
MAGICAN     = 32598
WOOD        = 32593
# ITEMS
STONE       = 13824
WATER       = 13808
SWORD       = 15310
SEAL        = 13846
STAFF       = 13809

class PyObject :
	pass

class Quest (JQuest) :
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
		self.mammon = 0
		self.questItemIds = [STONE, SWORD, WATER, SEAL, STAFF]

	def onAdvEvent(self, event, npc, player) :
		htmltext = event
		st = player.getQuestState(qn)
		if not st : return

		if event == "30969-05.htm" :
			st.set("cond","1")
			st.setState(State.STARTED)
			st.playSound("ItemSound.quest_accept")
		elif event == "32598-02.htm" :
			st.giveItems(STAFF, 1)
			st.playSound("ItemSound.quest_middle")
		elif event == "30969-11.htm" :
			st.set("cond","6")
			st.playSound("ItemSound.quest_middle")
		elif event == "32584-05.htm" :
			st.set("cond","2")
			st.giveItems(STONE, 1)
			st.playSound("ItemSound.quest_middle")
		elif event == "32586-06.htm" :
			st.set("cond","4")
			st.takeItems(STONE, 1)
			st.giveItems(SWORD, 1)
			st.giveItems(WATER, 1)
			st.playSound("ItemSound.quest_middle")
		elif event == "32586-12.htm" :
			st.set("cond","5")
			st.takeItems(SEAL, 4)
			st.takeItems(SWORD, 1)
			st.takeItems(WATER, 1)
			st.takeItems(STAFF, 1)
			st.playSound("ItemSound.quest_middle")
		elif event == "32593-02.htm" :
			st.addExpAndSp(52518015,5817676)
			st.unset("cond")
			st.setState(State.COMPLETED)
			st.exitQuest(False)
			st.playSound("ItemSound.quest_finish")
		elif event == "30969-06.htm" :
			if not self.mammon :
				npc = st.addSpawn(MAMMON,60000)
				self.mammon = 1
				st.startQuestTimer("Despawn_Mammon",60000)
				npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"Who dares summon the Merchant of Mammon?!"))
		elif event == "Despawn_Mammon" :
			self.mammon = 0
			return
		return htmltext

	def onTalk (self, npc, player) :
		htmltext = "<html><body>目前沒有執行任務，或條件不符。</body></html>"
		st = player.getQuestState(qn)
		if not st : return htmltext

		npcId = npc.getNpcId()
		id = st.getState()
		cond = st.getInt("cond")

		if id == State.COMPLETED :
			htmltext = "<html><body>這是已經完成的任務。</body></html>"
		elif id == State.CREATED :
			if npcId == HEINE and cond == 0 :
				first = player.getQuestState("195_SevenSignSecretRitualOfThePriests")
				if first and first.getState() == State.COMPLETED and player.getLevel() >= 79 :
					htmltext = "30969-01.htm"
				else :
					htmltext = "30969-00.htm"
					st.exitQuest(True)
		elif id == State.STARTED :
			if npcId == HEINE :
				if cond == 1 :
					htmltext = "30969-05.htm"
				elif cond == 2 :
					htmltext = "30969-08.htm"
					st.set("cond", "3")
				elif cond == 5 :
					htmltext = "30969-09.htm"
				elif cond == 6 :
					htmltext = "30969-11.htm"
			elif npcId == MAMMON :
				if cond == 1 :
					htmltext = "32584-01.htm"
				elif cond == 2 :
					htmltext = "32584-05.htm"
			elif npcId == SHUNAIMAN :
				if cond == 3 :
					htmltext = "32586-01.htm"
				elif cond == 4 :
					if st.getQuestItemsCount(SEAL) == 4 :
						htmltext = "32586-08.htm"
					else :
						htmltext = "32586-07.htm"
				elif cond == 5 :
					htmltext = "32586-12.htm"
			elif npcId == MAGICAN :
				if cond == 4 :
					if st.getQuestItemsCount(STAFF) == 1 :
						htmltext = "32598-03.htm"
					else :
						htmltext = "32598-01.htm"
			elif npcId == WOOD :
				if cond == 6 :
					htmltext = "32593-01.htm"
		return htmltext

	def onKill(self, npc, player, isPet) :
		st = player.getQuestState(qn)
		if not st : return
		if npc.getNpcId() == SEALDEVICE :
			st.giveItems(SEAL, 1)
			st.addSpawn(SEALDEVICE2,6000000)
			if st.getQuestItemsCount(SEAL) < 4 :
				st.playSound("ItemSound.quest_itemget")
			else:
				st.playSound("ItemSound.quest_middle")
				if st.getQuestItemsCount(SEAL) >= 4 :
					player.showQuestMovie(13)
		return

QUEST	= Quest(196,qn,"七封印，皇帝的封印")

QUEST.addStartNpc(HEINE)
QUEST.addTalkId(HEINE)
QUEST.addTalkId(WOOD)
QUEST.addTalkId(MAMMON)
QUEST.addTalkId(MAGICAN)
QUEST.addTalkId(SHUNAIMAN)
QUEST.addKillId(SEALDEVICE)
QUEST.addTalkId(SEALDEVICE2)