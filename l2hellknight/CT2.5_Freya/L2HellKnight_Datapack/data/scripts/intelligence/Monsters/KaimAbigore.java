package intelligence.Monsters;

import l2.hellknight.Config;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class KaimAbigore extends L2AttackableAIScript
{
	private static final int KAIM  = 18566;
	private static final int GUARD = 18567;

	boolean _isAlreadyStarted = false;
	boolean _isAlreadySpawned = false;
	int _isLockSpawned = 0;

	public KaimAbigore(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		int x = player.getX();
		int y = player.getY();

		if (event.equalsIgnoreCase("time_to_skill"))
		{
			npc.setTarget(player);
			npc.doCast(SkillTable.getInstance().getInfo(5260, 5));
			_isAlreadyStarted = false;
		}
		else if (event.equalsIgnoreCase("time_to_spawn"))
		{
			addSpawn(GUARD, x + 100, y + 50, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			addSpawn(GUARD, x - 100, y - 50, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			addSpawn(GUARD, x, y - 80, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			_isAlreadySpawned = false;
			_isLockSpawned = 3;
		}

		return "";
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		int npcId = npc.getNpcId();
		if (npcId == KAIM)
		{
			if (_isAlreadyStarted == false)
			{
				startQuestTimer("time_to_skill", 45000, npc, player);
				_isAlreadyStarted = true;
			}
			else if (_isAlreadyStarted == true)
				return "";
			if (_isAlreadySpawned == false)
			{
				if (_isLockSpawned == 0)
				{
					startQuestTimer("time_to_spawn", 60000, npc, player);
					_isAlreadySpawned = true;
				}
				if (_isLockSpawned == 3)
					return "";
			}
			else if (_isAlreadySpawned == true)
				return "";
		}

		return "";
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == GUARD)
			_isLockSpawned = 1;
		else if (npcId == KAIM)
		{
			cancelQuestTimer("time_to_spawn", npc, player);
			cancelQuestTimer("time_to_skill", npc, player);
		}

		return "";
	}

	public static void main(String[] args)
	{
		new KaimAbigore(-1, "KaimAbigore", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Kaim Abigore");
	}
}