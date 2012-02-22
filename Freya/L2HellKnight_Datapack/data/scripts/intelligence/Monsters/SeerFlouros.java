package intelligence.Monsters;

import l2.hellknight.Config;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

public class SeerFlouros extends L2AttackableAIScript
{
    private static L2Npc SeerFlouros, Follower;
    private static final int duration = 300000;
    private static final int SeerFlourosId = 18559;
    private static final int FollowerId = 18560;
    private static long _LastAttack = 0;
    private static boolean successDespawn = false;
    private static boolean minion = false;

    public SeerFlouros()
    {
        super(-1, "SeerFlouros", "ai");
        registerMobs(new int[] {SeerFlourosId, FollowerId});
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Seer Flouros");
    }

	@Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (event.equalsIgnoreCase("despawn"))
        {
            if (!successDespawn && SeerFlouros != null && _LastAttack + 300000 < System.currentTimeMillis())
            {
				cancelQuestTimer("despawn", npc, null);
				SeerFlouros.deleteMe();
				InstanceManager.getInstance().getInstance(SeerFlouros.getInstanceId()).setDuration(duration);
				successDespawn = true;
                if (Follower != null)
                    Follower.deleteMe();
            }
        }
        else if (event.equalsIgnoreCase("respMinion") && SeerFlouros != null)
        {
            Follower = addSpawn(FollowerId, SeerFlouros.getX(), SeerFlouros.getY(), SeerFlouros.getZ(), SeerFlouros.getHeading(), false, 0);
            L2Attackable target = (L2Attackable) SeerFlouros;
            Follower.setRunning();
            ((L2Attackable)Follower).addDamageHate(target.getMostHated(), 0, 999);
            Follower.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
        }
        return null;
    }

	@Override
    public String onSpawn(L2Npc npc)
    {
        if (npc.getNpcId() == SeerFlourosId)
        {
            _LastAttack = System.currentTimeMillis();
            startQuestTimer("despawn", 60000, npc, null, true);
            SeerFlouros = npc;
        }
        return null;
    }

	@Override
    public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        if (!minion)
        {
            Follower = addSpawn(FollowerId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
            minion = true;
        }
        _LastAttack = System.currentTimeMillis();
        return super.onAttack(npc, attacker, damage, isPet);
    }

	@Override
    public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
    {
        if (npc.getNpcId() == SeerFlourosId)
        {
            cancelQuestTimer("despawn", npc, null);
            if (Follower != null)
                Follower.deleteMe();
        }
        else if (npc.getNpcId() == FollowerId && SeerFlouros != null)
            startQuestTimer("respMinion", 30000, npc, null);
        return null;
    }
}
