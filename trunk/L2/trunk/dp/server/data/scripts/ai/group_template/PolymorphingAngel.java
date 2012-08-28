package ai.group_template;

import java.util.Map;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PolymorphingAngel extends L2AttackableAIScript
{

	private static final Map<Integer,Integer> ANGELSPAWNS = new FastMap<Integer,Integer>();
	static
	{
            ANGELSPAWNS.put(20830,20859);
            ANGELSPAWNS.put(21067,21068);
            ANGELSPAWNS.put(21062,21063);
            ANGELSPAWNS.put(20831,20860);
            ANGELSPAWNS.put(21070,21071);
	}
	
    public PolymorphingAngel(int questId, String name, String descr)
    {
		super(questId, name, descr);
		int[] temp = {20830,21067,21062,20831,21070};
        this.registerMobs(temp);
	}

    public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet)
    {
        int npcId = npc.getNpcId();
        if (ANGELSPAWNS.containsKey(npcId))
        {
            L2Attackable newNpc = (L2Attackable) this.addSpawn(ANGELSPAWNS.get(npcId),npc);
            L2Character originalKiller = isPet? killer.getPet(): killer;
            newNpc.setRunning();
            newNpc.addDamageHate(originalKiller,0,999);
            newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalKiller);
        }
        return super.onKill(npc,killer,isPet);
    }

    public static void main(String[] args)
    {
    	new PolymorphingAngel(-1,"polymorphing_angel","ai");
    }
}