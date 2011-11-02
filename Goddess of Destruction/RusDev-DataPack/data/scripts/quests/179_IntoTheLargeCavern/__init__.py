#rewriten for L2jEurope by surskis
import sys
from com.l2js.gameserver.instancemanager		 import InstanceManager
from com.l2js.gameserver.model.actor.instance  import L2DoorInstance
from com.l2js.gameserver.model.entity			 import Instance
from com.l2js.gameserver.model.quest           import State
from com.l2js.gameserver.model.quest           import QuestState
from com.l2js.gameserver.model.quest.jython    import QuestJython as JQuest
from com.l2js.gameserver.network.serverpackets import ExShowScreenMessage

qn = "179_IntoTheLargeCavern"

#NPC'S
KEKROPUS     = 32138
MACHINE      = 32258
GATEMPAST    = 32260
GATEMPRESENT = 32261
GATEMFUTURE  = 32262

#DOORS
PAST    = 16200014
PRESENT = 16200015
FUTURE  = 16200016

class PyObject:
	pass
	
def isWithinLevel(player):
	if player.getLevel() > 21:
		return False
	if player.getLevel() < 17:
		return False
	return True

def openDoor(doorId,player):
	for door in InstanceManager.getInstance().getInstance(player.getInstanceId()).getDoors():
		if door.getDoorId() == doorId:
			door.openMe()

class Quest (JQuest):

    def __init__(self,id,name,descr):
	    JQuest.__init__(self,id,name,descr)
	    self.condition = 0
 
    def onAdvEvent (self,event,npc, player):
	    htmltext = event
	    st = player.getQuestState(qn)
	    if not st: return
	    player = st.getPlayer()
	    passwrd = st.getInt("pass")
	    if event == "32138-03.htm":
		    st.set("cond","1")
		    st.setState(State.STARTED)
		    st.playSound("ItemSound.quest_accept")
	    elif event == "32260-02.htm":
		    st.set("pass","0")
	    elif event == "32260-03a.htm":
		    st.set("pass",str(passwrd+1))
		    htmltext = "32260-03.htm"
	    elif event == "32260-04a.htm":
		    st.set("pass",str(passwrd+1))
		    htmltext = "32260-04.htm"
	    elif event == "32260-05a.htm":
		    st.set("pass",str(passwrd+1))
		    htmltext = "32260-05.htm"
	    elif event == "32260-06a.htm": #need to add a check for the open past doors
		    st.set("pass",str(passwrd+1))
		    if st.getInt("pass") != 4:
			    return "32260-06.htm"
		    elif st.getInt("pass") == 4:
			    openDoor(PAST,player)
			    player.sendPacket(ExShowScreenMessage(1,-1,0x2,0,0,0,0,1,5000,0,"The Veiled Creator..."))
			    self.condition = 1
			    st.set("pass","0")
	    elif event == "32261-02.htm":
		    st.set("pass","0")
	    elif event == "32261-03a.htm":
		    st.set("pass",str(passwrd+1))
		    htmltext = "32261-03.htm"
	    elif event == "32261-04a.htm":
		    st.set("pass",str(passwrd+1))
		    htmltext = "32261-04.htm"
	    elif event == "32261-05a.htm":
		    st.set("pass",str(passwrd+1))
		    htmltext = "32261-05.htm"
	    elif event == "32261-06a.htm": #need to add a check for the open present doors
		    st.set("pass",str(passwrd+1))
		    if st.getInt("pass") != 4:
			    return "32261-06.htm"
		    elif st.getInt("pass") == 4:
			    openDoor(PRESENT,player)
			    player.sendPacket(ExShowScreenMessage(1,-1,0x2,0,0,0,0,1,5000,0,"The Conspiracy of the Ancient Race"))
			    self.condition = 2
			    st.set("pass","0")
	    elif event == "32262-02.htm":
		    st.set("pass","0")
	    elif event == "32262-03a.htm":
		    st.set("pass",str(passwrd+1))
		    htmltext = "32262-03.htm"
	    elif event == "32262-04a.htm":
		    st.set("pass",str(passwrd+1))
		    htmltext = "32262-04.htm"
	    elif event == "32262-05a.htm":
		    st.set("pass",str(passwrd+1))
		    htmltext = "32262-05.htm"
	    elif event == "32262-06a.htm":
		    st.set("pass",str(passwrd+1))
		    htmltext = "32262-06.htm"
	    elif event == "32262-07a.htm": #need to add a check for the open future doors
		    st.set("pass",str(passwrd+1))
		    if st.getInt("pass") != 5:
			    return "32262-07.htm"
		    elif st.getInt("pass") == 5:
			    openDoor(FUTURE,player)
			    player.sendPacket(ExShowScreenMessage(1,-1,0x2,0,0,0,0,1,5000,0,"Chaos and Time..."))
			    self.condition = 3
			    st.set("pass","0")
	    elif event == "32258-03.htm":
		    self.condition = 4
	    elif event == "32258-08.htm":
		    self.condition = 5
		    st.exitQuest(False)
		    st.playSound("ItemSound.quest_finish")
		    st.giveItems(391,1)
		    st.giveItems(413,1)
	    elif event == "32258-09.htm":
		    self.condition = 5
		    st.exitQuest(False)
		    st.playSound("ItemSound.quest_finish")
		    st.giveItems(849,1)
		    st.giveItems(890,1)
		    st.giveItems(910,1)
	    elif event == "32258-11.htm":
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
	    st2 = st.getPlayer().getQuestState("178_IconicTrinity")
	    id = st.getState()
	    cond = st.getInt("cond")
	    if npcId == KEKROPUS:
		    if id == State.COMPLETED:
			    htmltext = "<html><body>This quest has already been completed.</body></html>"
		    elif id == State.CREATED:
		        if player.getRace().ordinal() != 5:
			        return "32138-00.htm"
		        if st2:
		            if st2.getState() == State.COMPLETED:
			            if not isWithinLevel(player):
					        htmltext = "32138-00.htm"
					        st.exitQuest(1)
			            else:
				            htmltext = "32138-01.htm"
		            else:
			            htmltext = "32138-00.htm"
			            st.exitQuest(1)
		        if cond == 1:
			        htmltext = "32138-03.htm"
	    elif npcId == GATEMPAST:
		    if self.condition == 0:
			    if cond == 1:
				    htmltext = "32260-01.htm"
		    elif self.condition >= 1:
			    htmltext = "32260-06a.htm"
	    elif npcId == GATEMPRESENT:
		    if self.condition < 2:
			    if cond == 1:
				    htmltext = "32261-01.htm"
		    elif self.condition >= 2:
			    htmltext = "32261-06a.htm"
	    elif npcId == GATEMFUTURE:
		    if self.condition < 3:
			    if cond == 1:
				    htmltext = "32262-01.htm"
		    elif self.condition >= 3:
			    htmltext = "32262-07a.htm"
	    elif npcId == MACHINE:
		    if self.condition <= 3:
			    if cond == 1:
				    htmltext = "32258-01.htm"
		    elif self.condition == 4:
			    htmltext = "32258-03.htm"
		    if id == State.COMPLETED:
			    htmltext = "32258-10.htm"
	    return htmltext

QUEST       = Quest(179, qn, "Into The Large Cavern")

QUEST.addStartNpc(KEKROPUS)

QUEST.addTalkId(KEKROPUS)
QUEST.addTalkId(GATEMPAST)
QUEST.addTalkId(GATEMPRESENT)
QUEST.addTalkId(GATEMFUTURE)
QUEST.addTalkId(MACHINE)
