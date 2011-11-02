import sys
from com.l2js.gameserver.model.quest            import State
from com.l2js.gameserver.model.quest            import QuestState
from com.l2js.gameserver.model.quest.jython     import QuestJython as JQuest
from com.l2js.gameserver.network.serverpackets  import NpcSay
from com.l2js.util 							   import Rnd
from com.l2js.gameserver.ai                     import CtrlIntention
from com.l2js.gameserver.network.serverpackets  import MagicSkillUse
from com.l2js.gameserver.datatables             import ItemTable
from com.l2js.gameserver.network.serverpackets  import CreatureSay
from com.l2js.gameserver.model                  import L2ItemInstance
from com.l2js.gameserver.model.itemcontainer    import Inventory

BOOTY = 9144
APIGA = 9142

class PyObject:
	pass
	
def dropItem(npc,itemId,count):
	ditem = ItemTable.getInstance().createItem("Loot", itemId, count, None)
	ditem.dropMe(npc, npc.getX() + 30, npc.getY(), npc.getZ())

class CursedPigs (JQuest) :
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)

	def onSkillSee(self,npc,caster,skill,targets,isPet):
		skillId = skill.getId()
		rnd = Rnd.get(100)
		if not npc in targets: return
		npcId = npc.getNpcId()
		weapon = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		if not weapon: return
		weaponId = weapon.getItemId()
		if weaponId != 9141: return
		if skillId in [3261,3262]:
			if npcId in [13031,13032,13033] and rnd < 40:
				npc.broadcastPacket(MagicSkillUse(npc, npc, 5441, 1, 1, 0))
				npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "You saved me, thank you, Thanks for your help!", "Free! Thank you!", "Finally the curse is lifted!"))
				npc.reduceCurrentHp(9999999,npc)
				rnd2 = Rnd.get(100)
				if rnd2 < 10:
					dropItem(npc,BOOTY,1)
				elif rnd2 >= 10 and rnd2 <= 60:
					dropItem(npc,APIGA,1)
			elif npcId == 13034 and rnd < 20:
				npc.broadcastPacket(MagicSkillUse(npc, npc, 5441, 1, 1, 0))
				npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "You saved me, thank you, Thanks for your help!", "Free! Thank you!", "Finally the curse is lifted!"))
				npc.reduceCurrentHp(9999999,npc)
				rnd2 = Rnd.get(100)
				if rnd2 < 20:
					dropItem(npc,BOOTY,1)
				elif rnd2 >= 20 and rnd2 <= 80:
					dropItem(npc,APIGA,1)
			elif npcId == 13035 and rnd < 10:
				npc.broadcastPacket(MagicSkillUse(npc, npc, 5441, 1, 1, 0))
				npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "You saved me, thank you, Thanks for your help!", "Free! Thank you!", "Finally the curse is lifted!"))
				npc.reduceCurrentHp(9999999,npc)
				rnd2 = Rnd.get(100)
				if rnd2 < 30:
					dropItem(npc,BOOTY,1)
				elif rnd2 >= 30 and rnd2 <= 60:
					dropItem(npc,APIGA,1)

QUEST = CursedPigs(-2,"CursedPigs","ai")
for npc in [13031,13032,13033,13034,13035]:
	QUEST.addSkillSeeId(npc)
