import sys
from com.l2js.gameserver.model.quest import State
from com.l2js.gameserver.model.quest import QuestState
from com.l2js.gameserver.model.quest.jython import QuestJython as JQuest

qn = "10282_GuardianOfSafety"

Lekon = 32557
ADENA = 57
MOBS = [22614,22615,25633,25623]
FEATHER = 13871
CHANCE = 90

class Quest (JQuest) :

 def __init__(self,id,name,descr):
	 JQuest.__init__(self,id,name,descr)
	 self.questItemIds = [FEATHER]

 def onEvent (self,event,st) :
	 htmltext = event
	 if event == "30892-00a.htm" :
		 htmltext = "30892-00a.htm"
		 st.exitQuest(1)
	 elif event == "30892-02.htm" :
		 st.setState(State.STARTED)
		 st.set("cond","1")
		 st.playSound("ItemSound.quest_accept")
	 elif event == "Quest Finished" :
		 st.exitQuest(1)
		 st.playSound("ItemSound.quest_finish")
	 return htmltext

 def onTalk (self,npc,player):
	 htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
	 st = player.getQuestState(qn)
	 if not st : return htmltext

	 npcId = npc.getNpcId()
	 id = st.getState()
	 level = player.getLevel()
	 cond = st.getInt("cond")
	 amount = st.getQuestItemsCount(FEATHER)*1500
	 if id == State.CREATED :
			qs = player.getQuestState("10273_GoodDayToFly")
			if qs:
				if qs.getState() == State.COMPLETED and player.getLevel() >= 75 :
					htmltext = "30892-01.htm"
				else:
					htmltext = "32557-00.htm"
			else :
				htmltext = "32557-00.htm"
	 elif cond==1 :
		if amount :
			htmltext = "30892-03.htm"
			st.giveItems(ADENA,amount)
			st.takeItems(FEATHER,-1)
		else :
			htmltext = "30892-04.htm"
	 return htmltext

 def onKill(self,npc,player,isPet):
	 st = player.getQuestState(qn)
	 if not st : return 
	 if st.getState() != State.STARTED : return 
	
	 npcId = npc.getNpcId()
	 if st.getRandom(100)<CHANCE :
		 st.giveItems(FEATHER,1)
		 st.playSound("ItemSound.quest_itemget")
	 return

QUEST		= Quest(10282,qn,"GuardianOfSafety")

QUEST.addStartNpc(32557)
QUEST.addTalkId(32557)
for mobid in MOBS :
	QUEST.addKillId(mobid)
