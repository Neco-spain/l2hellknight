package intelligence.Monsters;

import java.util.Map;

import javolution.util.FastMap;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

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
		this.registerMobs(temp, QuestEventType.ON_KILL);
	}
	
	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (ANGELSPAWNS.containsKey(npcId))
		{
			L2Attackable newNpc = (L2Attackable) this.addSpawn(ANGELSPAWNS.get(npcId),npc);
			newNpc.setRunning();
		}
		return super.onKill(npc,killer,isPet);
	}
	
	public static void main(String[] args)
	{
		// now call the constructor (starts up the ai)
		new PolymorphingAngel(-1,"polymorphing_angel","ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Polymorphing Angel");
	}
}