package intelligence.RaidBosses;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.QuestState;

import l2.brick.bflmpsvz.a.L2AttackableAIScript;

public class Aenkinel extends L2AttackableAIScript
{
	private final int GK1 = 32658;
	private final int GK2 = 32659;
	private final int GK3 = 32660;
	private final int GK4 = 32661;
	private final int GK5 = 32662;
	private final int GK6 = 32663;
	private final int AENKINEL1 = 25690;
	private final int AENKINEL2 = 25691;
	private final int AENKINEL3 = 25692;
	private final int AENKINEL4 = 25693;
	private final int AENKINEL5 = 25694;
	private final int AENKINEL6 = 25695;
	public Aenkinel(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(GK1);
		addKillId(AENKINEL1);
		addStartNpc(GK2);
		addKillId(AENKINEL2);
		addStartNpc(GK3);
		addKillId(AENKINEL3);
		addStartNpc(GK4);
		addKillId(AENKINEL4);
		addStartNpc(GK5);
		addKillId(AENKINEL5);
		addStartNpc(GK6);
		addKillId(AENKINEL6);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState("aenkinel");
		int npcId = npc.getNpcId();
		if (npcId == AENKINEL1 || npcId == AENKINEL2 || npcId == AENKINEL3 || npcId == AENKINEL4 || npcId == AENKINEL5 || npcId == AENKINEL6)
		{
			int instanceId = npc.getInstanceId();
			addSpawn(18820, -121524,-155073,-6752, 64792, false, 0, false, instanceId);
			addSpawn(18819, -121486,-155070,-6752, 57739, false, 0, false, instanceId);
			addSpawn(18819, -121457,-155071,-6752, 49471, false, 0, false, instanceId);
			addSpawn(18819, -121428,-155070,-6752, 41113, false, 0, false, instanceId);
			if (st == null)
				return "";
			st.exitQuest(true);
		}
		return "";
	}

	public static void main(String[] args)
	{
		new Aenkinel(-1, "aenlinel", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded RaidBoss: Aenkiniel");
	}
}