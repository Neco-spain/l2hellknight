package ai.custom;

import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.Fighter;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.Creature;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.NpcUtils;

public class GuardPustosh extends Fighter
{
    private final static int KANILOV = 27459;
    private final static int POSLOF = 27460;
    private final static int SAKUM = 27453;

    private final static int JOEL = 33516;
    private final static int SCHUAZEN = 33517;
    private final static int SELON = 33518;
    // This NPC is spawned when spawned Poslof and despawned when Kanilof is defeated.
    private final static int POSLOF_OFFICER = 19163;
    private final static int COMMANDO = 19126;
    private final static int COMMANDO_CAPTAIN = 19127;

    private final static int[][] COMMANDO_SPAWNS = {
            {-36405, 191635, -3632, 63583},
            {-36408, 191784, -3665, 63583},
            {-36312, 191720, -3665, 63583},
            {-36264, 191656, -3665, 63583},
    };

    public GuardPustosh(NpcInstance actor)
    {
        super(actor);
    }

    @Override
    protected boolean thinkActive()
    {
        NpcInstance actor = getActor();
        int actorId = actor.getNpcId();
        actor.setRunning();

        if (actorId != SELON)
        {
            boolean monsterFound = false;
            NpcInstance poslofAttacker = null;
            for (NpcInstance npc : actor.getAroundNpc(1000, 1000))
            {
                int npcId = npc.getNpcId();
                if (npcId == KANILOV || npcId == POSLOF || npcId == SAKUM)
                {
                    monsterFound = true;
                    actor.getAggroList().addDamageHate(npc, 0, 1000);
                    setIntention(CtrlIntention.AI_INTENTION_ATTACK);
                    if (actorId == SCHUAZEN)
                    {
                        NpcUtils.spawnSingle(POSLOF_OFFICER, -29533, 187059, -3912, 45362);
                    }
                }
                else if (npcId == POSLOF_OFFICER)
                    poslofAttacker = npc;
            }
            if (!monsterFound && poslofAttacker != null)
            {
                poslofAttacker.deleteMe();
            }
        }

        return true;
    }

    @Override
    protected void onEvtDead(Creature killer)
    {
        NpcInstance actor = getActor();

        int currentNpcId = actor.getNpcId();
        int respawnNpcId = (currentNpcId == COMMANDO) ? COMMANDO_CAPTAIN : COMMANDO;

        // When Sakum kills all Commandos, Commando Captains will be spawned and vice versas
        boolean needRespawn = true;
        for (NpcInstance npc : actor.getAroundNpc(1000, 1000))
        {
            if (npc.getNpcId() == currentNpcId && !npc.isDead())
                needRespawn = false;
        }
        if (needRespawn)
        {
            for (int i = 0, j = COMMANDO_SPAWNS.length; i < j; ++i)
                NpcUtils.spawnSingle(respawnNpcId, new Location(COMMANDO_SPAWNS[i][0], COMMANDO_SPAWNS[i][1], COMMANDO_SPAWNS[i][2]));
        }

        super.onEvtDead(killer);
    }
}