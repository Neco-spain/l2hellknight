import sys
from com.l2js.gameserver.instancemanager		import InstanceManager
from com.l2js.gameserver.model.entity			import Instance
from com.l2js.gameserver.model.quest          import State
from com.l2js.gameserver.model.quest          import QuestState
from com.l2js.gameserver.model.quest.jython   import QuestJython as JQuest

qn = "182_NewRecruits"

#NPC'S
KEKROPUS     = 32138
MACHINE      = 32258

#need retail html's for 32258 and more retail for kekropus.

class PyObject:
	pass
	
def isWithinLevel(player):
	if player.getLevel() > 20:
		return False
	if player.getLevel() < 18:
		return False
	return True

class Quest (JQuest):

    def __init__(self,id,name,descr):
	    JQuest.__init__(self,id,name,descr)
	    self.condition = 0
 
    def onAdvEvent (self,event,npc, player):
	    htmltext = event
	    st = player.getQuestState(qn)
	    if not st: return
	    player = st.getPlayer()
	    if event == "32138-03.htm":
		    st.set("cond","1")
		    st.setState(State.STARTED)
		    st.playSound("ItemSound.quest_accept")
	    elif event == "32258-03.htm":
		    self.condition = 1
	    elif event == "32258-04.htm":
		    st.exitQuest(False)
		    st.playSound("ItemSound.quest_finish")
		    st.giveItems(890,2)
	    elif event == "32258-05.htm":
		    st.exitQuest(False)
		    st.playSound("ItemSound.quest_finish")
		    st.giveItems(847,2)
	    elif event == "32258-06.htm":
		    player.setInstanceId(0)
		    player.teleToLocation(-84757,60009,-2581)
		    if player.getPet() != None:
			    pet.setInstanceId(0)
			    pet.teleToLocation(-84757,60009,-2581)
	    return htmltext

    def onTalk (self,npc,player):
	    npcId = npc.getNpcId()
	    htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
	    st = player.getQuestState(qn)
	    if not st: return htmltext
	    id = st.getState()
	    cond = st.getInt("cond")
	    if npcId == KEKROPUS:
		    if id == State.COMPLETED:
			    htmltext = "<html><body>This quest has already been completed.</body></html>"
		    elif id == State.CREATED:
			    if player.getRace().ordinal() == 5:
				    return "32138-00.htm"
			    if not isWithinLevel(player):
				    htmltext = "32138-00.htm"
				    st.exitQuest(1)
			    else:
				    htmltext = "32138-01.htm"
		    if cond == 1:
			    htmltext = "32138-04.htm"
	    elif npcId == MACHINE:
		    if self.condition == 0:
		        if cond == 1:
			        htmltext = "32258-01.htm"
		    elif self.condition == 1:
			    if cond == 1:
			        htmltext = "32258-03.htm"
		    if id == State.COMPLETED:
			    htmltext = "32258-05.htm"
	    return htmltext

QUEST       = Quest(182, qn, "New Recruits")

QUEST.addStartNpc(KEKROPUS)
QUEST.addTalkId(KEKROPUS)
QUEST.addTalkId(MACHINE)
