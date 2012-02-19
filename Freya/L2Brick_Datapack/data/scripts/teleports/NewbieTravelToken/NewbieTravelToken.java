package teleports.NewbieTravelToken;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import javolution.util.FastMap;

import java.util.Map;

public class NewbieTravelToken extends Quest
{
	private static Map<String, int[]> data = new FastMap<String, int[]>();
	private final static int[] NPCs = { 30600, 30601, 30599, 30602, 30598, 32135 };


	public NewbieTravelToken(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int id : NPCs)
		{
			addStartNpc(id);
			addTalkId(id);
		}
		data.put("30600", new int[] { 12111, 16686, -4584 });
		data.put("30601", new int[] { 115632, -177996, -896 });
		data.put("30599", new int[] { 45475, 48359, -3056 });
		data.put("30602", new int[] { -45032, -113598, -192 });
		data.put("30598", new int[] { -84081, 243227, -3728 });
		data.put("32135", new int[] { -119697, 44532, 360 });
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);
		if (data.containsKey(event))
		{
			final int x = data.get(event)[0];
			final int y = data.get(event)[1];
			final int z = data.get(event)[2];
			player.teleToLocation(x, y, z);
			st.exitQuest(true);
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (player.getLevel() >= 20)
		{
			htmltext = "1.htm";
			st.exitQuest(true);
		}
		else
			htmltext = npc.getNpcId() + ".htm";
		return htmltext;
	}

	public static void main(String[] args)
	{
		new NewbieTravelToken(-1, "NewbieTravelToken", "teleports");
	}
}