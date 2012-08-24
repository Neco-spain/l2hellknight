#Instance Event by Bloodshed
from l2.hellknight.gameserver.instancemanager        import InstanceManager
from l2.hellknight.gameserver.model                  import L2ItemInstance
from l2.hellknight.gameserver.model.actor            import L2Summon
from l2.hellknight.gameserver.model.entity           import Instance
from l2.hellknight.gameserver.model.itemcontainer    import PcInventory
from l2.hellknight.gameserver.model.quest            import State
from l2.hellknight.gameserver.model.quest            import QuestState
from l2.hellknight.gameserver.model.quest.jython     import QuestJython as JQuest
from l2.hellknight.gameserver.network.serverpackets  import CreatureSay
from l2.hellknight.gameserver.network.serverpackets  import InventoryUpdate
from l2.hellknight.gameserver.network.serverpackets  import MagicSkillUse
from l2.hellknight.gameserver.network.serverpackets  import SystemMessage
from l2.hellknight.gameserver.network.serverpackets  import ExShowScreenMessage
from l2.hellknight.gameserver.network.serverpackets  import Earthquake
from l2.hellknight.gameserver.network                import SystemMessageId
from l2.hellknight.gameserver.util                   import Util
from l2.hellknight.util                              import Rnd
from java.lang 										import System

qn = "Bloodshed"

#Items
E_APIGA	= 14720
ADENA	= 57
STONE	= 9576
SCROLL	= 960

#NPCs
ROSE	= 2009001
CHEST	= 2009002

#Monsters
NAGLFAR	= 2009010
SENTRY1	= 2009011
SENTRY2	= 2009012
HOUND	= 2009013

#Doors
DOOR1	= 12240001
DOOR2	= 12240002

#Timelimit
LIMITTIME = 86400

class PyObject:
	pass

def openDoor(doorId,instanceId):
	for door in InstanceManager.getInstance().getInstance(instanceId).getDoors():
		if door.getDoorId() == doorId:
			door.openMe()
			
def closeDoor(doorId,instanceId):
	for door in InstanceManager.getInstance().getInstance(instanceId).getDoors():
		if door.getDoorId() == doorId:
			door.closeMe()
			
def checkConditions(player, new):
	st = player.getQuestState(qn)
	ptime = st.getInt("ptime")
	time = System.currentTimeMillis()/1000
	if ptime > time :
		remainingTime = ptime - time
		hours = remainingTime / 3600
		minutes = (remainingTime%3600)/60
		sm = SystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES)
		sm.addString("Instance")
		sm.addNumber(hours)
		sm.addNumber(minutes)
		player.sendPacket(sm)
		return False
	party = player.getParty()
	if party:
		player.sendPacket(SystemMessage.sendString("You may not enter with a party."))
		return False
	if not player.getLevel() >= 78:
		player.sendPacket(SystemMessage.sendString("You must be level 78 or higher to enter."))
		return False
	if not party:
		return True
	return False

def teleportplayer(self,player,teleto):
	player.setInstanceId(teleto.instanceId)
	player.teleToLocation(teleto.x, teleto.y, teleto.z)
	pet = player.getPet()
	if pet != None :
		pet.setInstanceId(teleto.instanceId)
		pet.teleToLocation(teleto.x, teleto.y, teleto.z)
	return

def enterInstance(self,player,template,teleto):
	instanceId = 0
	party = player.getParty()
	if party :
		for partyMember in party.getPartyMembers().toArray():
			st = partyMember.getQuestState(qn)
			if not st : st = self.newQuestState(partyMember)
			if partyMember.getInstanceId()!=0:
				instanceId = partyMember.getInstanceId()
	else :
		if player.getInstanceId()!=0:
			instanceId = player.getInstanceId()
	if instanceId != 0:
		if not checkConditions(player,False):
			return 0
		foundworld = False
		for worldid in self.world_ids:
			if worldid == instanceId:
				foundworld = True
		if not foundworld:
			player.sendPacket(SystemMessage.sendString("You have entered another zone, therefore you cannot enter this one."))
			return 0
		st = player.getQuestState(qn)
		st.set("ptime",str((System.currentTimeMillis() / 1000 + LIMITTIME)))
		teleto.instanceId = instanceId
		teleportplayer(self,player,teleto)
		return instanceId
	else:
		if not checkConditions(player,True):
			return 0
		instanceId = InstanceManager.getInstance().createDynamicInstance(template)
		if not instanceId in self.world_ids:
			world = PyObject()
			world.rewarded=[]
			world.instanceId = instanceId
			self.worlds[instanceId]=world
			self.world_ids.append(instanceId)
			print "Instance: Started " + template + " Instance: " +str(instanceId) + " created by " + str(player.getName())
		st = player.getQuestState(qn)
		st.set("ptime",str((System.currentTimeMillis() / 1000 + LIMITTIME)))
		teleto.instanceId = instanceId
		teleportplayer(self,player,teleto)
		return instanceId
	return instanceId

def exitInstance(player,tele):
	player.setInstanceId(0)
	player.teleToLocation(tele.x, tele.y, tele.z)
	pet = player.getPet()
	if pet != None :
		pet.setInstanceId(0)
		pet.teleToLocation(tele.x, tele.y, tele.z)

class Bloodshed(JQuest):
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
		self.worlds = {}
		self.world_ids = []
		
	def onTalk (self,npc,player):
		st = player.getQuestState(qn)
		npcId = npc.getNpcId()
		if npcId == ROSE :
			tele = PyObject()
			tele.x = -238599
			tele.y = 219983
			tele.z = -10144
			enterInstance(self, player, "Bloodshed.xml", tele)
			st.playSound("ItemSound.quest_middle")
		elif npcId == CHEST :
			npc.decayMe()
			tele = PyObject()
			tele.x = 82200
			tele.y = 148347
			tele.z = -3467
			exitInstance(player,tele)
			st.giveItems(ADENA,2750000)
			st.giveItems(STONE,1)
			st.giveItems(SCROLL,1)
			st.playSound("ItemSound.quest_finish")
			player.sendPacket(ExShowScreenMessage("Solo Instance Event (78+): Completed", 8000))
		return
		
	def onKill(self,npc,player,isPet):
		st = player.getQuestState(qn)
		npcId = npc.getNpcId()
		if npcId == SENTRY1 :
			if npc.getInstanceId() in self.worlds:
				world = self.worlds[npc.getInstanceId()]
				st.playSound("ItemSound.quest_middle")
				player.sendPacket(CreatureSay(npc.getObjectId(), 0, npc.getName(), "Master, Forgive Me!"))
				st.giveItems(E_APIGA,1)
				openDoor(DOOR1,npc.instanceId)
		elif npcId == SENTRY2 :
			if npc.getInstanceId() in self.worlds:
				world = self.worlds[npc.getInstanceId()]
				st.playSound("ItemSound.quest_middle")
				player.sendPacket(CreatureSay(npc.getObjectId(), 0, npc.getName(), "Master, Forgive Me!"))
				st.giveItems(E_APIGA,1)
				openDoor(DOOR2,npc.instanceId)
		elif npcId == HOUND :
			if npc.getInstanceId() in self.worlds:
				world = self.worlds[npc.getInstanceId()]
				st.playSound("ItemSound.quest_middle")
				st.giveItems(E_APIGA,2)
				player.sendPacket(ExShowScreenMessage("Demonic Lord Naglfar Has Appeared!", 8000))
				newNpc = self.addSpawn(NAGLFAR,-242754,219982,-9985,306,False,0,False,npc.instanceId)
				player.sendPacket(Earthquake(240826,219982,-9985,20,10))
		elif npcId == NAGLFAR :
			if npc.getInstanceId() in self.worlds:
				world = self.worlds[npc.getInstanceId()]
				player.sendPacket(CreatureSay(npc.getObjectId(), 0, npc.getName(), "Ugh.... Defeated.. How!?"))
				player.sendPacket(ExShowScreenMessage("Congratulations! You Have Defeated Demonic Lord Naglfar.", 12000))
				st.playSound("ItemSound.quest_fanfare_2")
				st.giveItems(E_APIGA,4)
				newNpc = self.addSpawn(CHEST,-242754,219982,-9985,306,False,0,False,npc.instanceId)
		return

QUEST = Bloodshed(-1, qn, "Bloodshed")
QUEST.addStartNpc(ROSE)
QUEST.addTalkId(ROSE)
QUEST.addTalkId(CHEST)

QUEST.addKillId(NAGLFAR)
QUEST.addKillId(HOUND)
QUEST.addKillId(SENTRY1)
QUEST.addKillId(SENTRY2)