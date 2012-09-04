package quests._735_MakeSpearsDull;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.instancemanager.QuestManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Party;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.ExShowScreenMessage;
import l2rt.util.Rnd;

public class _735_MakeSpearsDull extends Quest implements ScriptFile
{
	private static final List<ClassId> ClassList = Arrays.asList(new ClassId[] { ClassId.gladiator, ClassId.warlord,
			ClassId.treasureHunter, ClassId.hawkeye, ClassId.plainsWalker, ClassId.silverRanger, ClassId.abyssWalker,
			ClassId.phantomRanger, ClassId.destroyer, ClassId.tyrant, ClassId.bountyHunter, ClassId.duelist,
			ClassId.dreadnought, ClassId.sagittarius, ClassId.adventurer, ClassId.windRider, ClassId.moonlightSentinel,
			ClassId.ghostHunter, ClassId.ghostSentinel, ClassId.titan, ClassId.grandKhauatari, ClassId.fortuneSeeker,
			ClassId.berserker, ClassId.maleSoulbreaker, ClassId.femaleSoulbreaker, ClassId.arbalester, ClassId.doombringer,
			ClassId.maleSoulhound, ClassId.femaleSoulhound, ClassId.trickster });

	private static final int REWARD = 10;
	private static final int RANDOM_MIN = 15;
	private static final int RANDOM_MAX = 20;
	private static final String[] Text = new String[] { "Out of MAX Warriors and Rogues you have defeated KILL.",
			"You weakened the enemy's attack!" };

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _735_MakeSpearsDull()
	{
		super(false);
	}

	@Override
	public String onPlayerKill(L2Player killed, QuestState st)
	{
		L2Player killer = st.getPlayer();
		if(killed == null || killer == null || !checkPlayers(killed, killer))
			return null;

		int max = st.getInt("max");
		int kill = st.getInt("kill") + 1;
		st.set("kill", kill);

		if(kill >= max)
		{
			st.removeNotifyOfPlayerKill();

			String var = killer.getVar("badges" + killer.getTerritorySiege());
			int badges = 0;
			if(var != null)
				badges = Integer.parseInt(var);
			killer.setVar("badges" + killer.getTerritorySiege(), "" + (badges + REWARD));

			st.addExpAndSp(534000, 51000);
			st.exitCurrentQuest(false);
			killer.sendPacket(new ExShowScreenMessage(Text[1], 10000));
		}
		else
			killer.sendPacket(new ExShowScreenMessage(Text[0].replace("MAX", String.valueOf(max)).replace("KILL", String.valueOf(kill)), 10000));
		return null;
	}

	public static void OnDie(L2Character killed, L2Character killer)
	{
		if(killer == null || killed == null)
			return;
		L2Player pkiller = killer.getPlayer();
		L2Player pkilled = killed.getPlayer();
		if(pkiller == null || pkilled == null)
			return;

		Quest q = QuestManager.getQuest(_735_MakeSpearsDull.class);
		L2Party party = pkiller.getParty();
		if(party == null)
			takeQuest(q, pkilled, pkiller);
		else
			for(L2Player member : party.getPartyMembers())
				if(member != null && member.isInRange(pkiller, 2000))
					takeQuest(q, pkilled, member);
	}

	private static void takeQuest(Quest q, L2Player pkilled, L2Player pkiller)
	{
		if(!checkPlayers(pkilled, pkiller))
			return;
		QuestState st = pkiller.getQuestState(q.getClass());
		if(st == null)
			st = q.newQuestState(pkiller, Quest.CREATED);
		if(st.getState() == CREATED && st.getInt("doneDate") != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)|| st.getState() == COMPLETED && st.getInt("doneDate") != Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.set("max", Rnd.get(RANDOM_MIN, RANDOM_MAX));
			st.set("kill", 0);
			st.set("doneDate", String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)));
			st.addNotifyOfPlayerKill();
		}
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
		if(killer.getLevel() < 61 || killed.getLevel() < 61 || !ClassList.contains(killed.getClassId()))
			return false;
		return true;
	}
}