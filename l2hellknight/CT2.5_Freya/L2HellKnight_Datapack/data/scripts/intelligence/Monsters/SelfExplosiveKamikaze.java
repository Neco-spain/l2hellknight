package intelligence.Monsters;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.skills.SkillHolder;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

import java.util.Map;
import javolution.util.FastMap;

public class SelfExplosiveKamikaze extends L2AttackableAIScript
{
	private static final Map<Integer, SkillHolder> MONSTERS = new FastMap<Integer, SkillHolder>();
	
	static
	{
		MONSTERS.put(18817, new SkillHolder(5376, 4));
		MONSTERS.put(18818, new SkillHolder(5376, 4));
		MONSTERS.put(18821, new SkillHolder(5376, 5));
		MONSTERS.put(21666, new SkillHolder(4614, 3));
		MONSTERS.put(21689, new SkillHolder(4614, 4));
		MONSTERS.put(21712, new SkillHolder(4614, 5));
		MONSTERS.put(21735, new SkillHolder(4614, 6));
		MONSTERS.put(21758, new SkillHolder(4614, 7));
		MONSTERS.put(21781, new SkillHolder(4614, 9));
	}

	public SelfExplosiveKamikaze(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int npcId : MONSTERS.keySet())
		{
			addAttackId(npcId);
			addSpellFinishedId(npcId);
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skil)
	{
      if (player != null)
      {
				if (MONSTERS.containsKey(npc.getNpcId()) && !npc.isDead() && Util.checkIfInRange(MONSTERS.get(npc.getNpcId()).getSkill().getSkillRadius(), player, npc, true))
          npc.doCast(MONSTERS.get(npc.getNpcId()).getSkill());
      }

      return super.onAttack(npc, player, damage, isPet, skil);
	}

	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (MONSTERS.containsKey(npc.getNpcId()) && !npc.isDead() && (skill.getId() == 4614 || skill.getId() == 5376))
			npc.doDie(null);

		return super.onSpellFinished(npc, player, skill);
	}

	public static void main(String[] args)
	{
		new SelfExplosiveKamikaze(-1, "SelfExplosiveKamikaze", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Self Explosive Kamikaze");
	}
}