import sys
from com.l2js.gameserver.model.quest.jython       import QuestJython as JQuest
from com.l2js.gameserver.model.actor.instance     import L2NpcInstance
from com.l2js.gameserver.model                    import L2CharPosition
from com.l2js.gameserver.ai                       import CtrlIntention
from com.l2js.gameserver.network.serverpackets    import CreatureSay
from com.l2js.util                                import Rnd

# flee onAttack (current version is rather fear than retreat)
# ToDo: find a way to check position instead of using a timer to stop fleeing
#       make zones or a list of "basements" for mobs to retreat to

class retreat_onattack(JQuest) :

    # init function.  Add in here variables that you'd like to be inherited by subclasses (if any)
    def __init__(self,id,name,descr):
        # firstly, call the parent constructor to prepare the event triggering
        # mechanisms etc.
        JQuest.__init__(self,id,name,descr)
        self.MobSpawns ={
                20432: {'HP': 100, 'chance': 100}, # Elpy
                20058: {'HP': 50, 'chance': 10}, # Ol Mahum Guard
                20207: {'HP': 30, 'chance': 1}, # Ol Mahum Guerilla
                20208: {'HP': 50, 'chance': 1}, # Ol Mahum Raider
                20158: {'HP': 30, 'chance': 1}  # Medusa
                }
        # made a second dictionary for the texts
        self.MobTexts ={
                20058: ["Ca ne restera pas ainsi.",
                        "YpK me vengera."],
                20432: ["Notre chef Despote sera mis au courant de tes actes.",
                        "Heeee ! La chasse n'est pas ouverte !",
                        "Maman ! je ne veux pas finir en civet !",
                        "Despote is back !",
                        "Despote is back !",
                        "Despote te fera ta fete !"],
                20207: ["Un intrus !",
                        "Je te donnes 10M d'adena si tu me laisse vivre !",
                        "Tu me paieras cette humiliation !",
                        "J'irai me plaindre a un GM !",
                        "J'ai oublie le lait sur le feu !",
                        "Ne me frappes pas !",
                        "Je te tuerai la prochaine fois..."],
                20208: ["Retraite !",
                        "Tu es trop fort !",
                        "Maman !",
                        "Je vais tout dire a Yamaneko !"],
                20158: ["Agamir sera mis au courant de tes actes !",
                        "Je te ferai la peau !",
                        "Je reviendrai un jour !",
                        "Ton crime ne restera pas impuni !",
                        "Aaaahh !!! Je vais mourir !!!!"]
}
    def onAdvEvent(self,event,npc,player) :
        if event == "Retreat" and npc and player:
            npc.stopFear(False)
            npc.addDamageHate(player,0,100)
            npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player)

    def onAttack(self,npc,player,damage,isPet,skill) :
        npcId = npc.getNpcId()
        objId = npc.getObjectId()
        if self.MobSpawns.has_key(npcId) :
            if npc.getStatus().getCurrentHp() <= npc.getMaxHp() * self.MobSpawns[npcId]['HP'] / 100.0 and Rnd.get(100) < self.MobSpawns[npcId]['chance'] :
                if self.MobTexts.has_key(npcId) :
                    text = self.MobTexts[npcId][Rnd.get(len(self.MobTexts[npcId]))]
                    npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),text))
                posX = npc.getX()
                posY = npc.getY()
                posZ = npc.getZ()
                signX = -500
                signY = -500
                if posX > player.getX() :
                    signX = 500
                if posY > player.getY() :
                    signY = 500
                posX = posX + signX
                posY = posY + signY
                npc.startFear()
                npc.setRunning()
                npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,L2CharPosition(posX,posY,posZ,0))
                self.startQuestTimer("Retreat", 10000, npc, player)
        return

# now call the constructor (starts up the ai)
QUEST      = retreat_onattack(-1,"retreat_onattack","ai")
for i in QUEST.MobSpawns.keys() :
    QUEST.addAttackId(i)

print "AI : Retreat on Attack = > OK"
