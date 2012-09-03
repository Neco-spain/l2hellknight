package quests._147_PathtoBecominganEliteMercenary;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;

public class _147_PathtoBecominganEliteMercenary extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	// NPCs
	private static final int[] _merc = { 36481, 36482, 36483, 36484, 36485, 36486, 36487, 36488, 36489 };
	private static final int[] CATAPULT_ID = { 36499, 36500, 36501, 36502, 36503, 36504, 36505, 36506, 36507 };
	// Items
	private static final int _cert_ordinary = 13766;
	private static final int _cert_elite = 13767;

	public _147_PathtoBecominganEliteMercenary()
	{
		super(true);
		for(int _npc : _merc)
		{
			addStartNpc(_npc);
		}
		for(int _catap : CATAPULT_ID)
		{
			addKillId(_catap);
		}
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;

		if(st == null)
			return htmltext;

		if(arrayContains(_merc, npc.getNpcId()))
		{
			if(event.equalsIgnoreCase("elite-02.htm"))
			{
				if(st.getQuestItemsCount(_cert_ordinary) == 1)
					return "elite-02a.htm";
				st.giveItems(_cert_ordinary, 1);
			}
			else if(event.equalsIgnoreCase("elite-04.htm"))
			{
				st.setState(STARTED);
				st.set("cond", "1");
				st.set("kills", "0");
				st.playSound("ItemSound.quest_accept");
				st.addNotifyOfPlayerKill();
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		L2Player player = st.getPlayer();
		String htmltext = "noquest";
		int id = st.getState();

		if(arrayContains(_merc, npc.getNpcId()))
		{
			switch(id)
			{
				case CREATED:
					if(player.getClan() != null && player.getClan().getHasCastle() > 0)
						htmltext = "castle.htm";
					else
						htmltext = "elite-01.htm";
					break;
				case STARTED:
					if(st.getInt("cond") < 4)
					{
						htmltext = "elite-05.htm";
					}
					else if(st.getInt("cond") == 4)
					{
						st.unset("cond");
						st.unset("kills");
						st.takeItems(_cert_ordinary, -1);
						st.giveItems(_cert_elite, 1);
						st.exitCurrentQuest(false);
						htmltext = "elite-06.htm";
					}
					break;
				case COMPLETED:
					htmltext = "completed";
					break;
			}
		}
		return htmltext;
	}

	@Override
	public String onPlayerKill(L2Player killed, QuestState st)
	{
		int cond = st.getInt("cond");
		L2Player killer = st.getPlayer();

		if(killed == null || killer == null || !checkPlayers(killed, killer))
			return null;

		if(cond == 1 || cond == 3)
		{
			// Get
			int _kills = st.getInt("kills");
			// Increase
			_kills++;
			// Save
			st.set("kills", String.valueOf(_kills));
			// Check
			if(_kills >= 10)
			{
				if(cond == 1)
					st.set("cond", "2");
				else if(cond == 3)
					st.set("cond", "4");
			}
		}
		return null;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 2)
		{
			st.set("cond", "3");
		}
		return null;
	}

	public static boolean checkPlayers(L2Player killed, L2Player killer)
	{
		if(killer.getTerritorySiege() < 0 || killed.getTerritorySiege() < 0 || killer.getTerritorySiege() == killed.getTerritorySiege())
			return false;
		if(killer.getParty() != null && killer.getParty() == killed.getParty())
			return false;
		if(killer.getClan() != null && killer.getClan() == killed.getClan())
			return false;
		if(killer.getAllyId() > 0 && killer.getAllyId() == killed.getAllyId())
			return false;
		if(killer.getLevel() < 40 || killed.getLevel() < 61)
			return false;
		return true;
	}

	private boolean arrayContains(int[] array, int id)
	{
		for(int i : array)
			if(i == id)
				return true;
		return false;
	}

}