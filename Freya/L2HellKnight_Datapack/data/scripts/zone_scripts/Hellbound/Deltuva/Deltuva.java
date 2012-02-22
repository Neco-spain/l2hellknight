package zone_scripts.Hellbound.Deltuva;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Deltuva extends Quest
{
	private static final int DELTUVA = 32313;
	
	public Deltuva(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(DELTUVA);
		addTalkId(DELTUVA);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		if (event.equalsIgnoreCase("teleport"))
		{
			QuestState hostQuest = player.getQuestState("132_MatrasCuriosity");

			if (hostQuest != null && hostQuest.getState() == State.COMPLETED)
				player.teleToLocation(17934, 283189, -9701);
			else
				htmltext = "32313-02.htm";  
		}
		
		return htmltext;
	}

	public static void main(String[] args)
	{
		new Deltuva(-1, Deltuva.class.getSimpleName(), "zone_scripts/Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Hellbound: Deltuva");
	}
}
