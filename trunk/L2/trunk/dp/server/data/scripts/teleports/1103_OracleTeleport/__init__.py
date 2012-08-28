# Originally Created by Ham Wong on 2007.03.07
# updated by Kerberos on 2008.01.13
# fixed by Emperorc on 2008.02.28

import sys
from net.sf.l2j.gameserver.model.quest          import State
from net.sf.l2j.gameserver.model.quest          import QuestState
from net.sf.l2j.gameserver.model.quest.jython   import QuestJython as JQuest

qn = "1103_OracleTeleport"

TOWN_DAWN = [31078,31079,31080,31081,31083,31084,31082,31692,31694,31997,31168]
TOWN_DUSK = [31085,31086,31087,31088,31090,31091,31089,31693,31695,31998,31169]
TEMPLE_PRIEST = [31127,31128,31129,31130,31131,31137,31138,31139,31140,31141] + range(31488,31494)

TELEPORTERS = {
# Dawn
31078:0,
31079:1,
31080:2,
31081:3,
31082:4,
31083:5,
31084:6,
31692:7,
31694:8,
31997:9,
31168:10,
# Dusk
31085:11,
31086:12,
31087:13,
31088:14,
31089:15,
31090:16,
31091:17,
31693:18,
31695:19,
31998:20,
31169:21,
# Catacombs and Necropolis
31494:22,
31495:23,
31496:24,
31497:25,
31498:26,
31499:27,
31500:28,
31501:29,
31502:30,
31503:31,
31504:32,
31505:33,
31506:34,
31507:35
# Ziggurats
#
# will be done later
}

RETURN_LOCS = [[-80555,150337,-3040],[-13953,121404,-2984],[16354,142820,-2696],[83369,149253,-3400], \
              [111386,220858,-3544],[83106,53965,-1488],[146983,26595,-2200],[148256,-55454,-2779], \
              [45664,-50318,-800],[86795,-143078,-1341],[115136,74717,-2608],[-82368,151568,-3120], \
              [-14748,123995,-3112],[18482,144576,-3056],[81623,148556,-3464],[112486,220123,-3592], \
              [82819,54607,-1520],[147570,28877,-2264],[149888,-56574,-2979],[44528,-48370,-800], \
              [85129,-142103,-1542],[116642,77510,-2688],[-41572,209731,-5087],[-52872,-250283,-7908], \
              [45256,123906,-5411],[46192,170290,-4981],[111273,174015,-5437],[-20604,-250789,-8165], \
              [-21726, 77385,-5171],[140405, 79679,-5427],[-52366, 79097,-4741],[118311,132797,-4829], \
              [172185,-17602,-4901],[ 83000,209213,-5439],[-19500, 13508,-4901],[113865, 84543,-6541]]

class Quest (JQuest) :

 def __init__(self, id, name, descr): JQuest.__init__(self, id, name, descr)

 def onAdvEvent (self,event,npc,player):
    st = player.getQuestState(qn)
    if not st: 
       st = self.newQuestState(player)
    npcId = npc.getNpcId()
    htmltext = event
    if event == "Return":
       if npcId in TEMPLE_PRIEST and st.getState() == State.STARTED :
          x,y,z = RETURN_LOCS[st.getInt("id")]
          player.teleToLocation(x,y,z)
          st.exitQuest(1)
       return
    elif event == "Festival":
       id = st.getInt("id")
       if id in TOWN_DAWN:
          player.teleToLocation(-80157,111344,-4901)
          return
       elif id in TOWN_DUSK:
          player.teleToLocation(-81261,86531,-5157)
          return
       else :
          htmltext = "oracle1.htm"
    elif event == "Dimensional":
       htmltext = "oracle.htm"
       player.teleToLocation(-114755,-179466,-6752)
    elif event == "5.htm" :
       id = st.getInt("id")
       if id:
          htmltext="5a.htm"
       st.set("id",str(TELEPORTERS[npcId]))
       st.setState(State.STARTED)
       player.teleToLocation(-114755,-179466,-6752)
    elif event == "6.htm" :
       st.exitQuest(1)
    return htmltext

 def onTalk (Self, npc, player):
    st = player.getQuestState(qn)
    if not st: return
    npcId = npc.getNpcId()
    htmltext = None
    ##################
    # Dawn Locations #
    ##################
    if npcId in TOWN_DAWN: 
       st.setState(State.STARTED)
       st.set("id",str(TELEPORTERS[npcId]))
       st.playSound("ItemSound.quest_accept")
       player.teleToLocation(-80157,111344,-4901)
    ##################
    # Dusk Locations #
    ##################
    elif npcId in TOWN_DUSK: 
       st.setState(State.STARTED)
       st.set("id",str(TELEPORTERS[npcId]))
       st.playSound("ItemSound.quest_accept")
       player.teleToLocation(-81261,86531,-5157)
    elif npcId in range(31494,31508):
       if player.getLevel() < 20 :
          st.exitQuest(1)
          htmltext="1.htm"
       elif len(player.getAllActiveQuests()) > 23 :
          st.exitQuest(1)
          htmltext="1a.htm"
       elif not st.getQuestItemsCount(7079) :
          htmltext="3.htm"
       else :
          st.setState(State.CREATED)
          htmltext="4.htm"
    elif npcId in range(31095,31111)+range(31114,31125):
       if player.getLevel() < 20 :
          st.exitQuest(1)
          htmltext="ziggurats not supported yet"
       elif len(player.getAllActiveQuests()) > 23 :
          st.exitQuest(1)
          htmltext="ziggurats not supported yet"
       elif not st.getQuestItemsCount(7079) :
          htmltext="ziggurats not supported yet"
       else :
          #st.setState(State.CREATED)
          htmltext="ziggurats not supported yet"
    return htmltext

QUEST      = Quest(1103, qn, "Teleports")

for i in TELEPORTERS.keys() + TEMPLE_PRIEST + range(31494,31508)+range(31095,31111)+range(31114,31125):
    QUEST.addStartNpc(i)
    QUEST.addTalkId(i)