package zone_scripts.StakatoNest;

import gnu.trove.TIntObjectHashMap;

import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

import java.util.List;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2Party;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.util.Rnd;

public class CannibalisticStakatoChief extends L2AttackableAIScript
{
	private static final int CANNIBALISTIC_CHIEF = 25667;
	private static final int[] BIZARRE_COCOONS = { 18795, 18798 };
	private static final int LARGE_COCOON = 14834;
	private static final int SMALL_COCOON = 14833;
	private static TIntObjectHashMap<Integer> _captainSpawn = new TIntObjectHashMap<Integer>();
	
	public CannibalisticStakatoChief(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addKillId(CANNIBALISTIC_CHIEF);
		for (int i : BIZARRE_COCOONS)
			addSkillSeeId(i);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		if (contains(BIZARRE_COCOONS, npcId) && skill.getId() == 2905 && caster.getTarget() == npc)
		{
			npc.getSpawn().stopRespawn();
			npc.doDie(npc);
			final L2Npc captain = addSpawn(CANNIBALISTIC_CHIEF, npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, 0);
			_captainSpawn.put(captain.getObjectId(), npc.getNpcId());
			caster.getInventory().destroyItemByItemId("removeAccelerator", 14832, 1, caster, caster);
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet) 
	{
		final L2Party partyK = killer.getParty();
		if (partyK != null)
		{
			final List<L2PcInstance> party = partyK.getPartyMembers();
			for (L2PcInstance member : party)
			{
				if (Rnd.get(100) > 80)
					member.addItem("BigCocoon", LARGE_COCOON, 1, npc, true);
				else
					member.addItem("SMALL_COCOON", SMALL_COCOON, 1, npc, true);
			}
		}
		else
		{
			if (Rnd.get(100) > 80)
				killer.addItem("BigCocoon", LARGE_COCOON, 1, npc, true);
			else
				killer.addItem("SMALL_COCOON", SMALL_COCOON, 1, npc, true);
		}
		
		addSpawn(_captainSpawn.get(npc.getObjectId()), npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, 0);
		_captainSpawn.remove(npc.getObjectId());
		return super.onKill(npc, killer, isPet);
	}

	public static void main(String[] args)
	{
		new CannibalisticStakatoChief(-1, "CannibalisticStakatoChief", "zone_scripts");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Stakato Nest: Cannibalistic Stakato Chief");
	}
}
