package intelligence.Monsters;

import l2.brick.Config;
import l2.brick.bflmpsvz.a.L2AttackableAIScript;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;

public class Gargos extends L2AttackableAIScript
{
	private static final int GARGOS = 18607;

	boolean _isStarted = false;

	public Gargos(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAttackId(GARGOS);
		addKillId(GARGOS);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("TimeToFire"))
		{
			_isStarted = false;
			player.sendMessage("Oooo... Ooo...");
			npc.doCast(SkillTable.getInstance().getInfo(5705, 1));
		}
		return "";
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		int npcId = npc.getNpcId();

		if (npcId == GARGOS)
		{
			if (_isStarted == false)
			{
				startQuestTimer("TimeToFire", 60000, npc, player);
				_isStarted = true;
			}
		}
		return "";
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();

		if (npcId == GARGOS)
			cancelQuestTimer("TimeToFire", npc, player);

		return "";
	}

	public static void main(String[] args)
	{
		new Gargos(-1, "Gargos", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Gargos");
	}
}