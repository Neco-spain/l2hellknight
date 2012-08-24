# pmq

import sys
from l2.hellknight.gameserver.model.quest        import State
from l2.hellknight.gameserver.model.quest        import QuestState
from l2.hellknight.gameserver.model.quest.jython import QuestJython as JQuest

qn = "Zhongyuan_Festival"

# ITEM
SOULMAGICBOX = 20630

class Quest (JQuest) :

	def __init__(self,id,name,descr) : JQuest.__init__(self,id,name,descr)

	def onAdvEvent (self,event,npc,player) :
		htmltext = event
		st = player.getQuestState(qn)
		if not st: return
		if event == "4302-4.htm" :
			if st.getQuestItemsCount(SOULMAGICBOX):
				htmltext = "4302-5.htm"
			else :
				st.giveItems(SOULMAGICBOX,1)
				htmltext = "4302-4.htm"
		return htmltext

	def onFirstTalk (self,npc,player):
		npcId = npc.getNpcId()
		st = player.getQuestState(qn)
		if not st :
			st = self.newQuestState(player)
		if npcId == 4302 :
			htmltext = "4302.htm"
		return htmltext

QUEST = Quest(-1, qn, "events")

QUEST.addStartNpc(4302)
QUEST.addFirstTalkId(4302)
QUEST.addTalkId(4302)