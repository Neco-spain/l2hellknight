package quests._1002_Antharas;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Location;
import bosses.AntharasManager;

public class _1002_Antharas extends Quest implements ScriptFile
{
	private static final int HEART = 13001;

	// Items
	private static final int PORTAL_STONE = 3865;

	private static final Location TELEPORT_POSITION = new Location(179892, 114915, -7704);

	public void onLoad()
	{
		System.out.println("Loaded Quest: 1002: Antharas");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _1002_Antharas()
	{
		super("Antharas", false);

		addStartNpc(HEART);
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == HEART)
		{
			if(st.getPlayer().isFlying())
				return "<html><body>Heart of Warding:<br>You may not enter while flying a wyvern</body></html>";
			if(AntharasManager.isEnableEnterToLair())
			{
				if(st.getQuestItemsCount(PORTAL_STONE) >= 1)
				{
					st.takeItems(PORTAL_STONE, 1);
					AntharasManager.setAntharasSpawnTask();
					st.getPlayer().teleToLocation(TELEPORT_POSITION);
					st.exitCurrentQuest(true);
					return null;
				}
				st.exitCurrentQuest(true);
				return "<html><body>Heart of Warding:<br>You do not have the proper stones needed for teleport.<br>It is for the teleport where does 1 stone to you need.</body></html>";
			}
			st.exitCurrentQuest(true);
			return "<html><body>Heart of Warding:<br>Antharas has already awoke!<br>You are not allowed to enter into Lair of Antharas.</body></html>";
		}
		return null;
	}
}