package zone_scripts.StakatoNest;

import l2.brick.bflmpsvz.a.L2AttackableAIScript;

import l2.brick.Config;
import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.model.actor.L2Attackable;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2MonsterInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.util.Rnd;

public class SpikedStakatoNurse extends L2AttackableAIScript
{
	private static final int SPIKED_STAKATO_BABY = 22632;
	//private static final int SPIKED_STAKATO_NURSE = 22630;
	private static final int SPIKED_STAKATO_NURSE_2ND_FORM = 22631;
	
	public SpikedStakatoNurse(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addKillId(SPIKED_STAKATO_BABY);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		final L2Npc nurse = getNurse(npc);
		if (nurse != null && !nurse.isDead())
		{
			getNurse(npc).doDie(getNurse(npc));
			final L2Npc newForm = addSpawn(SPIKED_STAKATO_NURSE_2ND_FORM, npc.getX() + Rnd.get(10, 50), npc.getY() + Rnd.get(10, 50), npc.getZ(), 0, false, 0, true);
			newForm.setRunning();
			((L2Attackable) newForm).addDamageHate(killer, 1, 99999);
			newForm.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
		}
		return super.onKill(npc, killer, isPet);
	}

	public L2Npc getNurse(L2Npc couple)
	{
		// For now, minions are set as minionInstance. If they change to only monster, use the above code
		return ((L2MonsterInstance)couple).getLeader();
	}

	public static void main(String[] args)
	{
		new SpikedStakatoNurse(-1, "SpikedStakatoNurse", "zone_scripts");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Stakato Nest: Spiked Stakato Nurse");
	}
}
