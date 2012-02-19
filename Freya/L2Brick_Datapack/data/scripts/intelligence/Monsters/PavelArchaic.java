package intelligence.Monsters;

import l2.brick.Config;
import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.model.actor.L2Attackable;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.util.Util;
import l2.brick.util.Rnd;

import l2.brick.bflmpsvz.a.L2AttackableAIScript;

public class PavelArchaic extends L2AttackableAIScript
{
	private static final int[] _mobs1 = { 22801, 22804 };
	private static final int[] _mobs2 = { 18917 };
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (!npc.isDead() && Util.contains(_mobs2, npc.getNpcId()))
		{
			npc.doDie(attacker);
			
			if (Rnd.get(100) < 40)
			{
				L2Attackable _golem1 = (L2Attackable) addSpawn(22801, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
				attackPlayer(_golem1, attacker);
				
				L2Attackable _golem2 = (L2Attackable) addSpawn(22804, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
				attackPlayer(_golem2, attacker);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (Util.contains(_mobs1, npc.getNpcId()))
		{
			L2Attackable _golem = (L2Attackable) addSpawn(npc.getNpcId() + 1, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
			attackPlayer(_golem, killer);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	private void attackPlayer(L2Attackable npc, L2PcInstance player)
	{
		npc.setIsRunning(true);
		npc.addDamageHate(player, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
	}
	
	public PavelArchaic(int questId, String name, String descr)
	{
		super(questId, name, descr);
		registerMobs(_mobs1, QuestEventType.ON_KILL);
		registerMobs(_mobs2, QuestEventType.ON_ATTACK);
	}
	
	public static void main(String[] args)
	{
		new PavelArchaic(-1, "PavelArchaic", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Pavel Archaic");
	}
}
