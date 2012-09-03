package quests._350_EnhanceYourWeapon;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;

public class _350_EnhanceYourWeapon extends Quest implements ScriptFile
{
	private static final int RED_SOUL_CRYSTAL0_ID = 4629;
	private static final int GREEN_SOUL_CRYSTAL0_ID = 4640;
	private static final int BLUE_SOUL_CRYSTAL0_ID = 4651;

	private static final int Jurek = 30115;
	private static final int Gideon = 30194;
	private static final int Winonin = 30856;

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _350_EnhanceYourWeapon()
	{
		super(false);
		addStartNpc(Jurek);
		addStartNpc(Gideon);
		addStartNpc(Winonin);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		if(event.equalsIgnoreCase(Jurek + "-04.htm") || event.equalsIgnoreCase(Gideon + "-04.htm") || event.equalsIgnoreCase(Winonin + "-04.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		if(event.equalsIgnoreCase(Jurek + "-09.htm") || event.equalsIgnoreCase(Gideon + "-09.htm") || event.equalsIgnoreCase(Winonin + "-09.htm"))
			st.giveItems(RED_SOUL_CRYSTAL0_ID, 1);
		if(event.equalsIgnoreCase(Jurek + "-10.htm") || event.equalsIgnoreCase(Gideon + "-10.htm") || event.equalsIgnoreCase(Winonin + "-10.htm"))
			st.giveItems(GREEN_SOUL_CRYSTAL0_ID, 1);
		if(event.equalsIgnoreCase(Jurek + "-11.htm") || event.equalsIgnoreCase(Gideon + "-11.htm") || event.equalsIgnoreCase(Winonin + "-11.htm"))
			st.giveItems(BLUE_SOUL_CRYSTAL0_ID, 1);
		if(event.equalsIgnoreCase("exit.htm"))
			st.exitCurrentQuest(true);
		return event;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String npcId = str(npc.getNpcId());
		String htmltext = "noquest";
		int id = st.getState();
		if(st.getQuestItemsCount(RED_SOUL_CRYSTAL0_ID) == 0 && st.getQuestItemsCount(GREEN_SOUL_CRYSTAL0_ID) == 0 && st.getQuestItemsCount(BLUE_SOUL_CRYSTAL0_ID) == 0)
			if(id == CREATED)
				htmltext = npcId + "-01.htm";
			else
				htmltext = npcId + "-21.htm";
		else
		{
			if(id == CREATED)
			{
				st.setCond(1);
				st.setState(STARTED);
			}
			htmltext = npcId + "-03.htm";
		}
		return htmltext;
	}
}