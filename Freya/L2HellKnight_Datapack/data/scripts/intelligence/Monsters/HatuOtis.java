package intelligence.Monsters;

import l2.hellknight.Config;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class HatuOtis extends L2AttackableAIScript
{
	private static final int OTIS = 18558;
	boolean _isAlreadyUsedSkill = false;
	boolean _isAlreadyUsedSkill1 = false;

	public HatuOtis(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAttackId(OTIS);
		addKillId(OTIS);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_to_skill"))
		{
			if (_isAlreadyUsedSkill == true)
			{
				npc.setTarget(npc);
				npc.doCast(SkillTable.getInstance().getInfo(4737, 3));
				_isAlreadyUsedSkill = false;
			}
			else
				return "";
		}

		return "";
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		int npcId = npc.getNpcId();
		int maxHp = npc.getMaxHp();
		int nowHp = (int) npc.getStatus().getCurrentHp();

		if (npcId == OTIS)
		{
			if (nowHp < maxHp * 0.3)
			{
				if (_isAlreadyUsedSkill1 == false)
				{
					player.sendMessage("I will be with you, and to take care of you !");
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4175, 3));
					_isAlreadyUsedSkill1 = true;
				}
			}
			if (_isAlreadyUsedSkill == false)
			{
				startQuestTimer("time_to_skill", 30000, npc, player);
				_isAlreadyUsedSkill = true;
			}
		}

		return "";
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();

		if (npcId == OTIS)
			cancelQuestTimer("time_to_skill", npc, player);

		return "";
	}

	public static void main(String[] args)
	{
		new HatuOtis(-1, "HatuOtis", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Hatu otis");
	}
}