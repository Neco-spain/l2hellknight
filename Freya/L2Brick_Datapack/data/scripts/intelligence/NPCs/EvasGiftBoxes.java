package intelligence.NPCs;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.util.Rnd;

public class EvasGiftBoxes extends Quest
{
	final private static int GIFTBOX = 32342;
	
	final private static int KISSOFEVA = 1073;
	
	// index 0: without kiss of eva
	// index 1: with kiss of eva
	// chance,itemId,...
	final private static int[][] CHANCES = {{2,9692,1,9693},{100,9692,50,9693}};
	
	final private static String qn = "EvasGiftBoxes";
	
	public EvasGiftBoxes(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addKillId(GIFTBOX);
		addSpawnId(GIFTBOX);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == GIFTBOX)
		{
			QuestState st = killer.getQuestState(qn);
			if (st == null)
				st = newQuestState(killer);
			int isKissOfEvaBuffed = 0;
			if (killer.getFirstEffect(KISSOFEVA) != null)
				isKissOfEvaBuffed = 1;
			for (int i = 0; i < CHANCES[isKissOfEvaBuffed].length; i += 2)
				if (Rnd.get(100) < CHANCES[isKissOfEvaBuffed][i])
					st.giveItems(CHANCES[isKissOfEvaBuffed][i+1],1);
		}
		return super.onKill(npc,killer,isPet);
	}
	
	public static void main(String[] args)
	{
		new EvasGiftBoxes(-1,qn,"ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded NPC: Evas Gift Boxes");
	}
}