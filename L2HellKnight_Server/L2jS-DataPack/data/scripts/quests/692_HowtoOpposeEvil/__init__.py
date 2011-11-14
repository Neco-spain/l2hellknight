import sys
from com.l2js import Config
from com.l2js.gameserver.model.quest					import State
from com.l2js.gameserver.model.quest					import QuestState
from com.l2js.gameserver.model.quest.jython 			import QuestJython as JQuest

qn = "692_HowtoOpposeEvil"

#NPCs
DILIOS = 32549
KUTRAN = 32550
DESTRUCTION_MOBS = [ 22537, 22538, 22539, 22540, 22541, 22542, 22543, 22544, 22546, 22547, 22548, 22549, 22550, 22551, 22552, 22593, 22596, 22597 ]
IMMORTALITY_MOBS = [ 22510, 22511, 22512, 22513, 22514, 22515 ]

#items
LEKONS_CERTIFICATE = 13857
NUCLEUS_OF_A_FREED_SOUL = 13796
FLEET_STEED_TROUPS_CHARM = 13841

#etc(100%=1000)
DROP_CHANCE = 50

class Quest (JQuest) :
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
		self.questItemIds = [NUCLEUS_OF_A_FREED_SOUL, FLEET_STEED_TROUPS_CHARM]

	def onAdvEvent (self,event,npc, player) :
		htmltext = event
		st = player.getQuestState(qn)
		if not st : return
		if event == "32549-03.htm" :
			st.set("cond","1")
			st.setState(State.STARTED)
			st.playSound("ItemSound.quest_accept")
		elif event == "32550-04.htm" :
			st.set("cond","3")
		return htmltext

	def onTalk (self,npc,player):
		htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
		st = player.getQuestState(qn)
		if not st : return htmltext
		npcId = npc.getNpcId()
		state = st.getState()
		cond = st.getInt("cond")
		if state == State.CREATED :
			if player.getLevel() >= 75 :
				htmltext = "32549-01.htm"
			else :
				htmltext = "32549-00.htm"
		else :
			if npcId == DILIOS :
				if cond == 1 and st.getQuestItemsCount(LEKONS_CERTIFICATE) >= 1 :
					htmltext = "32549-04.htm"
					st.set("cond","2")
				elif cond == 2 :
					htmltext = "32549-05.htm"
			else :
				if cond == 2 :
					htmltext = "32550-01.htm"
				elif cond == 3 :
					htmltext = "32550-04.htm"
		return htmltext

	def onKill(self,npc,player,isPet):
		partyMember = self.getRandomPartyMember(player,"3")
		if not partyMember: return
		st = partyMember.getQuestState(qn)
		if st :
			chance = DROP_CHANCE * Config.RATE_QUEST_DROP
			numItems, chance = divmod(chance,1000)
			if st.getRandom(1000) < chance : 
				numItems += 1
			if numItems : 
				if npc.getNpcId() in DESTRUCTION_MOBS :
					st.giveItems(FLEET_STEED_TROUPS_CHARM,int(numItems))
				else :
					st.giveItems(NUCLEUS_OF_A_FREED_SOUL,int(numItems))
				st.playSound("ItemSound.quest_itemget")
		return

QUEST		= Quest(692,qn,"How to Oppose Evil")

QUEST.addStartNpc(DILIOS)
QUEST.addTalkId(DILIOS)
QUEST.addTalkId(KUTRAN)

for i in DESTRUCTION_MOBS :
	QUEST.addKillId(i)

for i in IMMORTALITY_MOBS :
	QUEST.addKillId(i)
