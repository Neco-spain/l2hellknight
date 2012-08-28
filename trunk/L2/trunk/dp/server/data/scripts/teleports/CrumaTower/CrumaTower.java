package teleports.CrumaTower;

import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;

public class CrumaTower extends Quest
{
	private final static int NPC = 30483;

	public CrumaTower(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(NPC);
		addTalkId(NPC);
	}

	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (player.getLevel() > 56)
			htmltext = "30483.htm";
		else
			player.teleToLocation(17724, 114004, -11672);

		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new CrumaTower(-1, "CrumaTower", "teleports");
	}
}