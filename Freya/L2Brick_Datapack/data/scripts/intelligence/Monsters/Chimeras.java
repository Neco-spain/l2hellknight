package intelligence.Monsters;

import l2.brick.Config;
import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.instancemanager.HellboundManager;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.actor.L2Attackable;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.util.Rnd;

import l2.brick.bflmpsvz.a.L2AttackableAIScript;

public class Chimeras extends L2AttackableAIScript
{
	private static final int[] NPCS =
	{
		22349, 22350, 22351, 22352
	};

	private static final int CELTUS = 22353;
	private static final int[][] LOCATIONS =
	{
		{ 3678, 233418, -3319 },
		{ 2038, 237125, -3363 },
		{ 7222, 240617, -2033 },
		{ 9969, 235570, -1993 }
	};

	private static final int BOTTLE = 9672;

	private static final int DIM_LIFE_FORCE = 9680;
	private static final int LIFE_FORCE = 9681;
	private static final int CONTAINED_LIFE_FORCE = 9682;

	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (HellboundManager.getInstance().getLevel() == 7 && !npc.isTeleporting()) //Have random spawn points only in 7 lvl
		{	
			final int[] spawn = LOCATIONS[Rnd.get(LOCATIONS.length)];
			if (!npc.isInsideRadius(spawn[0], spawn[1], spawn[2], 200, false, false))
			{
				npc.getSpawn().setLocx(spawn[0]);
				npc.getSpawn().setLocy(spawn[1]);
				npc.getSpawn().setLocz(spawn[2]);
				ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(npc, spawn), 100);
			}
		}

		return super.onSpawn(npc);
	}

	@Override
	public final String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (skill.getId() == BOTTLE)
		{
			if (!npc.isDead())
			{
				if (targets.length > 0 && targets[0] == npc)
				{
					if (npc.getCurrentHp() < npc.getMaxHp() * 0.1)
					{
						if (HellboundManager.getInstance().getLevel() == 7)
							HellboundManager.getInstance().updateTrust(3, true);

						npc.setIsDead(true);

						if (npc.getNpcId() == CELTUS)
							((L2Attackable)npc).dropItem(caster, CONTAINED_LIFE_FORCE, 1);
						else
						{
							if (Rnd.get(100) < 80)
								((L2Attackable)npc).dropItem(caster, DIM_LIFE_FORCE, 1);
							else if (Rnd.get(100) < 80)
								((L2Attackable)npc).dropItem(caster, LIFE_FORCE, 1);
						}

						npc.onDecay();
					}
				}
			}
		}

		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	private static class Teleport implements Runnable
	{
		private final L2Npc _npc;
		private final int[] _coords;

		public Teleport(L2Npc npc, int[] coords)
		{
			_npc = npc;
			_coords = coords;
		}

		@Override
		public void run()
		{
			_npc.teleToLocation(_coords[0], _coords[1], _coords[2]);
		}
	}

	public Chimeras(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int npcId : NPCS)
			addSkillSeeId(npcId);

		addSpawnId(CELTUS);
		addSkillSeeId(CELTUS);
	}

	public static void main(String[] args)
	{
		new Chimeras(-1, Chimeras.class.getSimpleName(), "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Chimeras");
	}
}
