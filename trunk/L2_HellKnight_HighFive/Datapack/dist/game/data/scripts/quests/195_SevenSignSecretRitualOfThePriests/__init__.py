# Made by d0S

import sys
from l2.hellknight.gameserver.datatables            import SkillTable
from l2.hellknight.gameserver.instancemanager       import InstanceManager
from l2.hellknight.gameserver.model.actor.instance  import L2PcInstance
from l2.hellknight.gameserver.model.actor.instance  import L2DoorInstance
from l2.hellknight.gameserver.model.quest           import State
from l2.hellknight.gameserver.model.quest           import QuestState
from l2.hellknight.gameserver.model.quest.jython    import QuestJython as JQuest
from l2.hellknight.gameserver.network.serverpackets import ExStartScenePlayer

qn = "195_SevenSignSecretRitualOfThePriests"

# NPCs 
ClaudiaAthebalt          = 31001
John                     = 32576
Raymond                  = 30289
LightOfDawn              = 32575
Device                   = 32578
IasonHeine               = 30969
PasswordEntryDevice      = 32577
Shkaf                    = 32580
Black                    = 32579
# ITEMS
EmperorShunaimanContract = 13823
IdentityCard             = 13822
# Transformation's skills
GuardofDawn              = 6204

class Quest (JQuest):

	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
		self.questItemIds = [IdentityCard, EmperorShunaimanContract]

	def onAdvEvent (self,event,npc,player):
		htmltext = event
		st = player.getQuestState(qn)
		if not st: return

		if event == "31001-05.htm":
			st.set("cond","1")
			st.setState(State.STARTED)
			st.playSound("ItemSound.quest_middle")
		elif event == "32576-02.htm":
			st.giveItems(IdentityCard,1)
			st.set("cond","2")
			st.playSound("ItemSound.quest_middle")
		elif event == "30289-04.htm":
			player.stopAllEffects()
			SkillTable.getInstance().getInfo(6204,1).getEffects(player,player)
			st.set("cond","3")
		elif event == "30289-07.htm":
			player.stopAllEffects()
			SkillTable.getInstance().getInfo(6204,1).getEffects(player,player)
		elif event == "30289-08.htm":
			player.stopAllEffects()
		elif event == "30289-11.htm":
			st.set("cond","4")
			st.playSound("ItemSound.quest_middle")
			player.stopAllEffects()
		elif event == "30969-03.htm":
			st.addExpAndSp(52518015, 5817677)
			st.unset("cond") 
			st.exitQuest(False)
			st.playSound("ItemSound.quest_finish")
		elif event == "quit":
			instanceId = player.getInstanceId()
			instance = InstanceManager.getInstance().getInstance(instanceId)
			player.setInstanceId(0)
			player.teleToLocation(-12491,122331,-2984)
			htmltext = ""
		return htmltext

	def onTalk (self,npc,player):
		htmltext = "<html><body>195_SevenSignSecretRitualOfThePriests</body></html>"
		st = player.getQuestState(qn)
		if not st: return htmltext

		npcId = npc.getNpcId()
		id = st.getState()
		cond = st.getInt("cond")

		if id == State.COMPLETED:
			htmltext = "<html><body>195_SevenSignSecretRitualOfThePriests</body></html>"
		elif id == State.CREATED :
			if npcId == ClaudiaAthebalt and cond == 0:
				third = player.getQuestState("194_SevenSignContractOfMammon")
				if third and third.getState() == State.COMPLETED and player.getLevel() >= 79 :
					htmltext = "31001-01.htm"
				else:
					htmltext = "31001-00.htm"
					st.exitQuest(1)
		elif id == State.STARTED:
			if npcId == ClaudiaAthebalt:
				if cond == 1:
					htmltext = "31001-06.htm"
			elif npcId == John:
				if cond == 1:
					htmltext = "32576-01.htm"
				elif cond == 2:
					htmltext = "32576-03.htm"
			elif npcId == Raymond:
				if cond == 2:
					htmltext = "30289-01.htm"
				elif cond == 3:
					if st.getQuestItemsCount(EmperorShunaimanContract) == 1:
						htmltext = "30289-09.htm"
					else:
						htmltext = "30289-06.htm"
				elif cond == 4:
					htmltext = "30289-12.htm"
			elif npcId == LightOfDawn:
				if cond == 3 and st.getQuestItemsCount(IdentityCard) == 1:
					htmltext = "32575-03.htm"
				else :
					htmltext = "32575-01.htm"
			elif npcId == Device:
				if player.getFirstEffect(GuardofDawn) != None:
					htmltext = "32578-03.htm"
				elif player.getFirstEffect(GuardofDawn) == None:
					htmltext = "32578-02.htm"
			elif npcId == PasswordEntryDevice:
				if player.getFirstEffect(GuardofDawn) != None:
					htmltext = "32577-01.htm"
				elif player.getFirstEffect(GuardofDawn) == None:
					htmltext = "32577-03.htm"
			elif npcId == Shkaf and st.getQuestItemsCount(EmperorShunaimanContract) == 0:
				htmltext = "32580-01.htm" 
				st.giveItems(EmperorShunaimanContract,1)
				st.set("cond","4")
			elif npcId == Black and st.getQuestItemsCount(EmperorShunaimanContract) == 1:
				htmltext = "32579-01.htm"
			elif npcId == IasonHeine and st.getQuestItemsCount(EmperorShunaimanContract) == 1:
				htmltext = "30969-01.htm" 
		return htmltext

QUEST		= Quest(195,qn,"Seven Sign Secret Ritual Of The Priests") 

QUEST.addStartNpc(ClaudiaAthebalt)
QUEST.addTalkId(ClaudiaAthebalt)
QUEST.addTalkId(John)
QUEST.addTalkId(Raymond)
QUEST.addTalkId(LightOfDawn)
QUEST.addTalkId(Device)
QUEST.addTalkId(PasswordEntryDevice)
QUEST.addTalkId(Shkaf)
QUEST.addTalkId(Black)
QUEST.addTalkId(IasonHeine)