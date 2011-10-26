package teleports.NoblesseTeleport;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;

public class NoblesseTeleport extends Quest
{
	private static final String qn = "NoblesseTeleport";

	private final static int[] NPCs =
	{
		30006,30059,30080,30134,30146,30177,30233,30256,30320,30540,
		30576,30836,30848,30878,30899,31275,31320,31964,32163
	};

	private final static String html = "<html><body><br>" +
			"Ah, you\'re a Noblesse! I can offer you a special service then.<br><br>" +
			"You may use this Olympiad Token.<br>" +
			"<a action=\"bypass -h %bypass%\">Teleport to Hunting Grounds</a><br><br>" +
			"Don\'t use Olympiad Token.<br>" +
			"<a action=\"bypass -h npc_%objectId%_Chat 2\">Teleport to Hunting Grounds</a><br>" +
			"<a action=\"bypass -h npc_%objectId%_Chat 0\">Back</a>" +
			"</body></html>";

	public NoblesseTeleport(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int id : NPCs)
		{
			addStartNpc(id);
			addTalkId(id);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = ""; 
		QuestState st = player.getQuestState(getName());

		if (player.isNoble())
		{
			String bypass = "Quest NoblesseTeleport noble-nopass.htm";
			if (st.getQuestItemsCount(13722) > 0)
				bypass = "npc_%objectId%_Chat 3";
			htmltext = html.replace("%bypass%", bypass).replace("%objectId%", String.valueOf(npc.getObjectId()));
		}
		else
			htmltext = "nobleteleporter-no.htm";

		return htmltext;
	}

	public static void main(String[] args)
	{
		new NoblesseTeleport(-1, qn, "teleports");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Teleport: Noblesse Teleport");
	}
}