import sys
from com.l2js.gameserver.instancemanager import InstanceManager
from com.l2js.gameserver.model.actor.instance import L2PcInstance
from com.l2js.gameserver.model.entity import Instance
from com.l2js.gameserver.model.quest import State
from com.l2js.gameserver.model.quest import QuestState
from com.l2js.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2js.gameserver.network import SystemMessageId
from com.l2js.gameserver.network.serverpackets import SystemMessage

qn = "Nornil's_Garden"

InstanceTemplate = "NGarden.xml"
Class = [0, 10, 18, 25, 31, 38, 44, 49, 53, 123, 124]

dataIndex = 0

def getGardenIndex(level):
    if level == 19:
        return 0

def isWithinLevel(player):
     if player.getLevel() > 21:
         return False
     if player.getLevel() < 17:
         return False
     return True

def checkPrimaryConditions(player):
    if not player.getParty():
        return False
    return True

def checkNewInstanceConditions(player):
    party = player.getParty()
    if party == None:
        return True
    if not party.isLeader(player):
        player.sendPacket(SystemMessage(2185))
        return False
    if not party.getLeader().getRace().ordinal() == 5:
        player.sendPacket(SystemMessage.sendString("A party leader must be Kamael to enter."))
        return False
    st2 = party.getLeader().getQuestState("179_IntoTheLargeCavern")
    if st2 == None:
        player.sendPacket(SystemMessage.sendString("You must be be in the process of Kekropus quest."))
        return False
    if st2.getState() == State.COMPLETED:
        player.sendPacket(SystemMessage.sendString("You must be be in the process of Kekropus quest."))
        return False
    for partyMember in party.getPartyMembers().toArray():
        if not partyMember.isInsideRadius(player, 500, False, False):
            sm = SystemMessage(2096)
            sm.addCharName(partyMember)
            player.sendPacket(sm)
            return False
    for partyMember in party.getPartyMembers().toArray():
        if not isWithinLevel(partyMember):
            sm = SystemMessage(2097)
            sm.addCharName(partyMember)
            player.sendPacket(sm)
            return False
    for partyMember in party.getPartyMembers().toArray():
        if not partyMember.getClassId().getId() in Class:
            sm = SystemMessage(2098)
            sm.addCharName(partyMember)
            player.sendPacket(sm)
            return False
    return True

def getExistingInstanceId(player):
    instanceId = 0
    party = player.getParty()
    if party == None:
        return 0
    for partyMember in party.getPartyMembers().toArray():
        if partyMember.getInstanceId() != 0:
            instanceId = partyMember.getInstanceId()
    return instanceId

def teleportPlayer(self, player, teleto):
    player.setInstanceId(teleto.instanceId)
    player.teleToLocation(teleto.x, teleto.y, teleto.z)
    pet = player.getPet()
    if pet != None :
        pet.setInstanceId(teleto.instanceId)
        pet.teleToLocation(teleto.x, teleto.y, teleto.z)
    return

def exitInstance(player, tele):
    player.setInstanceId(0)
    player.teleToLocation(tele.x, tele.y, tele.z)
    pet = player.getPet()
    if pet != None :
        pet.setInstanceId(0)
        pet.teleToLocation(tele.x, tele.y, tele.z)

class PyObject:
    pass

def openDoor(doorId, instanceId):
    for door in InstanceManager.getInstance().getInstance(instanceId).getDoors():
        if door.getDoorId() == doorId:
            door.openMe()

class Quest (JQuest) :

    def __init__(self, id, name, desc):
        JQuest.__init__(self, id, name, desc)
        self.worlds = {}
        self.world_ids = []
        self.currentWorld = 0

    def onTalk (self, npc, player):
        if not checkPrimaryConditions(player):
            htmltext = "<html><body>Garden Guard:<br>Before you may enter, you must recruit comrades whose strengths compensate for your areas of weakness. You can overcome the trials here only if they are with you.</body></html>"
            return htmltext
        tele = PyObject()
        tele.x = -111183
        tele.y = 74553
        tele.z = -12432
        instanceId = getExistingInstanceId(player)
        if instanceId == 0:
            if not checkNewInstanceConditions(player):
                return
            instanceId = InstanceManager.getInstance().createDynamicInstance(InstanceTemplate)    
            if not self.worlds.has_key(instanceId):
                world = PyObject()            
                world.rewarded = []
                world.instanceId = instanceId
                self.worlds[instanceId] = world
                self.world_ids.append(instanceId)
                self.currentWorld = instanceId
                openDoor(16200001, world.instanceId)
                openDoor(16200013, world.instanceId)
                openDoor(16200017, world.instanceId)
                openDoor(16200018, world.instanceId)                
                openDoor(16200019, world.instanceId)
                openDoor(16200023, world.instanceId)
                openDoor(16200023, world.instanceId)
                openDoor(16200024, world.instanceId)
                openDoor(16200025, world.instanceId)
                player.stopAllEffects()
                tele.instanceId = instanceId
                teleportPlayer(self, player, tele)
                party = player.getParty()
                if party != None:
                    for partyMember in party.getPartyMembers().toArray():
                        partyMember.stopAllEffects()
                        teleportPlayer(self, partyMember, tele)
                        world.status = 1
        else:
            foundworld = False
            for worldid in self.world_ids:
                if worldid == instanceId:
                    foundworld = True
            if not foundworld:
                player.sendPacket(SystemMessage.sendString("Your Party Members are in another Instance."))    
                return
            instanceObj = InstanceManager.getInstance().getInstance(instanceId)
            if instanceObj.getCountPlayers() >= PartySize[dataIndex]:
                player.sendPacket(SystemMessage(2112))
                return
            tele.instanceId = instanceId
            teleportPlayer(self, player, tele)
        return

    def onAttack(self, npc, player, damage, isPet, skill):
        npcId = npc.getNpcId()
        if self.worlds.has_key(npc.getInstanceId()):
            world = self.worlds[npc.getInstanceId()]
            if npcId == 18347:
                if world.status == 1:
                    world.status = 2
                    newpath = self.addSpawn(18349, -110016, 74512, -12533, 0, False, 0, False, npc.getInstanceId())
                    newpath = self.addSpawn(18348, -109729, 74913, -12533, 0, False, 0, False, npc.getInstanceId())
                    newpath = self.addSpawn(18348, -109981, 74899, -12533, 0, False, 0, False, npc.getInstanceId())
        return


QUEST = Quest(-1, qn, "instances")

QUEST.addStartNpc(32330)
QUEST.addTalkId(32330)
QUEST.addAttackId(18347)
