package ai.group_template;

import java.util.Map;

import javolution.util.FastMap;
import com.l2js.gameserver.model.actor.L2Attackable;
import com.l2js.gameserver.model.actor.L2Npc;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.util.Rnd;

/**
 * KAMALOKA BOSS AI
 */
public class kamalokaAi  extends L2AttackableAIScript 
{
	private static int Chants_Spawn = 20;

	private static final Map<Integer,Integer> KAMALOKASPAWNS = new FastMap<Integer,Integer>();
	static
	{
		KAMALOKASPAWNS.put(22452,22453);
		KAMALOKASPAWNS.put(22455,22456);
		KAMALOKASPAWNS.put(22458,22459);
		KAMALOKASPAWNS.put(22461,22462);
		KAMALOKASPAWNS.put(22464,22465);
		KAMALOKASPAWNS.put(22467,22468);
		KAMALOKASPAWNS.put(22470,22471);
		KAMALOKASPAWNS.put(22473,22474);
		KAMALOKASPAWNS.put(22476,22477);
		KAMALOKASPAWNS.put(22479,22480);
		KAMALOKASPAWNS.put(22482,22483);
	}

	public kamalokaAi(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] temp = {22452,22455,22458,22461,22464,22467,22470,22473,22476,22479,22482};
		this.registerMobs(temp);
	}

	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();

		if (KAMALOKASPAWNS.containsKey(npcId))
		{
			if (Rnd.get(100) < Chants_Spawn)
			{
				L2Attackable newNpc = null;
				if(Rnd.get(100) < 60)
				{
					newNpc = (L2Attackable) this.addSpawn(KAMALOKASPAWNS.get(npcId),npc);
					newNpc.setTitle("Doppler");
				}
				else
				{
					newNpc = (L2Attackable) this.addSpawn(((KAMALOKASPAWNS.get(npcId)) + 1),npc);
					newNpc.setTitle("Void");
				}

				newNpc.setInstanceId(npc.getInstanceId());
				newNpc.setRunning();
			}
		}
		return super.onKill(npc,killer,isPet);
	}

	public static void main(String[] args)
	{
		//Call constructor
		new kamalokaAi(-1, "kamalokaAi", "ai");
	}
}
