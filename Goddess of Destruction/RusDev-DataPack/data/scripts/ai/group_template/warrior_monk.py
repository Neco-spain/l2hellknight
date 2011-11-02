import sys
from com.l2js.gameserver.ai import CtrlIntention
from com.l2js.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2js.gameserver.network.serverpackets import NpcSay
from com.l2js.util import Rnd

class warrior_monk(JQuest) :

  def __init__(self, id, name, descr) :
    self.warrior_monk = 22129
    self.FirstAttacked = False
    JQuest.__init__(self, id, name, descr)

  def onAttack(self, npc, player, damage, isPet, skill) :
    objId = npc.getObjectId()
    if self.FirstAttacked :
       if Rnd.get(50) : return
       npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "Brother " + player.getName() + ", move your weapon away!!"))
    else :
       self.FirstAttacked = True
    return

  def onSpawn(self, npc) :
    objId = npc.getObjectId()
    for player in npc.getKnownList().getKnownPlayers().values() :
      if player.isInsideRadius(npc, 500, False, False) :
        if player.getActiveWeaponItem() :
          npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "Brother " + player.getName() + ", move your weapon away!!"))
          npc.addDamageHate(player, 0, 999)
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player)
        else :
          npc.getAggroList().remove(player)
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, None, None)
    return

  def onAggroRangeEnter(self, npc, player, isPet) :
    objId = npc.getObjectId()
    for player in npc.getKnownList().getKnownPlayers().values() :
      if player.isInsideRadius(npc, 500, False, False) :
        if player.getActiveWeaponItem() :
          npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "Brother " + player.getName() + ", move your weapon away!!"))
          npc.addDamageHate(player, 0, 999)
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player)
        else :
          npc.getAggroList().remove(player)
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, None, None)
    return

QUEST = warrior_monk(-1, "warrior_monk", "ai")

QUEST.addAttackId(QUEST.warrior_monk)

QUEST.addSpawnId(QUEST.warrior_monk)

QUEST.addAggroRangeEnterId(QUEST.warrior_monk)
