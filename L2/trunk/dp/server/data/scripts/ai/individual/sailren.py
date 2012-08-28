import sys
from net.sf.l2j.gameserver.ai.special import Sailren
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest
from net.sf.l2j.gameserver.network.serverpackets import SocialAction

#ENTRY_SATAT 0 = Sailren is not spawned
#ENTRY_SATAT 1 = Sailren is already dead
#ENTRY_SATAT 2 = Sailren is already entered by a other party
#ENTRY_SATAT 3 = Sailren is in interval
#ENTRY_SATAT 4 = You have no Party

STATUE        = 32109
VELOCIRAPTOR  = 22218
PTEROSAUR     = 22199
TYRANNOSAURUS = 22217
SAILREN       = 29065

GAZKH = 8784

class sailren (JQuest):

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onTalk (self,npc,player):
    st = player.getQuestState("sailren")
    if not st : return "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
    npcId = npc.getNpcId()
    if npcId == STATUE :
      if st.getQuestItemsCount(GAZKH) :
        ENTRY_SATAT = Sailren.getInstance().canIntoSailrenLair(player)
        if ENTRY_SATAT == 1 or ENTRY_SATAT == 2 :
          st.exitQuest(1)
          return "<html><body>Shilen's Stone Statue:<br>Another adventurers have already fought against the sailren. Do not obstruct them.</body></html>"
        elif ENTRY_SATAT == 3 :
          st.exitQuest(1)
          return "<html><body>Shilen's Stone Statue:<br>The sailren is very powerful now. It is not possible to enter the inside.</body></html>"
        elif ENTRY_SATAT == 4 :
          st.exitQuest(1)
          return "<html><body>Shilen's Stone Statue:<br>You seal the sailren alone? You should not do so! Bring the companion.</body></html>"
        elif ENTRY_SATAT == 0 :
          st.takeItems(GAZKH,1)
          Sailren.getInstance().setSailrenSpawnTask(VELOCIRAPTOR)
          Sailren.getInstance().entryToSailrenLair(player)
          return "<html><body>Shilen's Stone Statue:<br>Please seal the sailren by your ability.</body></html>"
      else :
        st.exitQuest(1)
        return "<html><body>Shilen's Stone Statue:<br><font color=""LEVEL"">Gazkh</font> is necessary for seal the sailren.</body></html>"

  def onKill (self,npc,player,isPet):
    st = player.getQuestState("sailren")
    if not st: return
    npcId = npc.getNpcId()
    if npcId == VELOCIRAPTOR :
      Sailren.getInstance().setSailrenSpawnTask(PTEROSAUR)
    elif npcId == PTEROSAUR :
      Sailren.getInstance().setSailrenSpawnTask(TYRANNOSAURUS)
    elif npcId == TYRANNOSAURUS :
      Sailren.getInstance().setSailrenSpawnTask(SAILREN)
    elif npcId == SAILREN :
      Sailren.getInstance().setCubeSpawn()
      st.exitQuest(1)
    return

QUEST = sailren(-1, "sailren", "ai")

QUEST.addStartNpc(STATUE)
QUEST.addTalkId(STATUE)
QUEST.addKillId(VELOCIRAPTOR)
QUEST.addKillId(PTEROSAUR)
QUEST.addKillId(TYRANNOSAURUS)
QUEST.addKillId(SAILREN)