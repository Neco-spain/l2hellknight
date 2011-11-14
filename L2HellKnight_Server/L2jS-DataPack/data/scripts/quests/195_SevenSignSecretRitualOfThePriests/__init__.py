# Made by d0S
import sys
from com.l2js.gameserver.datatables         import SkillTable
from com.l2js.gameserver.model.actor.instance import L2PcInstance
from com.l2js.gameserver.model.actor.instance import L2DoorInstance
from com.l2js.gameserver.model.quest        import State
from com.l2js.gameserver.model.quest        import QuestState
from com.l2js.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2js.gameserver.network.serverpackets import ExStartScenePlayer

qn = "195_SevenSignSecretRitualOfThePriests"

#NPCs 
ClaudiaAthebalt = 31001
John = 32576
Raymond = 30289
LightOfDawn = 32575
Device = 32578
IasonHeine = 30969
PasswordEntryDevice = 32577
Shkaf = 32580
Black = 32579  
#ITEMS
EmperorShunaimanContract = 13823
IdentityCard = 13822
#Transformation's skills
GuardofDawn = 6204


 
class Quest (JQuest):

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [IdentityCard, EmperorShunaimanContract]

 def onAdvEvent (self,event,npc,player):
   htmltext = event
   st = player.getQuestState(qn)
   if not st: return
   if event == "31001-02.htm":
     st.setState(State.STARTED)
     st.playSound("ItemSound.quest_accept")
   if event == "31001-05.htm":
     st.set("cond","1")
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
   elif event == "30969-03.htm":
     st.addExpAndSp(52518015, 5817677)
     st.unset("cond") 
     st.exitQuest(False)
     st.playSound("ItemSound.quest_finish")
   return htmltext

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   st1 = player.getQuestState("194_SevenSignContractOfMammon")
   htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>" 
   if not st: return htmltext
   npcId = npc.getNpcId()
   id = st.getState()
   cond = st.getInt("cond")
   if id == State.COMPLETED: htmltext = "<html><body>This quest has already been completed.</body></html>"
   elif npcId == ClaudiaAthebalt:
     if st1:
       if st1.getState() == State.COMPLETED:
         if cond == 0:
           if player.getLevel() >= 79: htmltext = "31001-01.htm"
           else:
             htmltext = "31001-0a.htm"
             st.exitQuest(1)
         elif cond == 1: htmltext = "31001-06.htm"
       else: return "31001-0b.htm"
     if not st1: return "31001-0b.htm"
   elif npcId == John:
      if cond == 1: htmltext = "32576-01.htm"
      elif cond == 2: htmltext = "32576-03.htm"
   elif npcId == Raymond:
      if cond == 2: htmltext = "30289-01.htm"
      elif cond == 3: htmltext = "30289-06.htm"
      elif cond == 4:
	 htmltext = "30289-08.htm" 
	 player.stopAllEffects()
      	 st.giveItems(7128,1)
         st.playSound("ItemSound.quest_middle")		
   elif npcId == LightOfDawn:
      if cond == 3 and st.getQuestItemsCount(IdentityCard) == 1: htmltext = "32575-03.htm"
      else : htmltext = "32575-01.htm"
   elif npcId == Device:
     if player.getFirstEffect(GuardofDawn) != None: htmltext = "32578-03.htm"
     elif player.getFirstEffect(GuardofDawn) == None: htmltext = "32578-02.htm"
   elif npcId == PasswordEntryDevice: 
     if player.getFirstEffect(GuardofDawn) != None: htmltext = "32577-01.htm"
     elif player.getFirstEffect(GuardofDawn) == None: htmltext = "32577-03.htm"   
   elif npcId == Shkaf and st.getQuestItemsCount(EmperorShunaimanContract) == 0:
      htmltext = "32580-01.htm" 
      st.giveItems(EmperorShunaimanContract,1)
      st.set("cond","4")
   elif npcId == Black and st.getQuestItemsCount(EmperorShunaimanContract) == 1: htmltext = "32579-01.htm"
   elif npcId == IasonHeine and st.getQuestItemsCount(EmperorShunaimanContract) == 1: 
      htmltext = "30969-01.htm" 
   return htmltext

QUEST     = Quest(195,qn,"Seven Sign Secret Ritual Of The Priests") 

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
