package quests._10322_Quest3;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.ExShowScreenMessage; 
import l2rt.gameserver.tables.SkillTable;
import quests._10321_Quest2._10321_Quest2;
import l2rt.util.GArray;

public class _10322_Quest3 extends Quest implements ScriptFile
{
	int SHENON = 32974;
	int IVEN = 33464;
	int POMOSH = 32981;
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _10322_Quest3()
	{
		super(false);
		addStartNpc(SHENON);
		addTalkId(IVEN);
		addTalkId(POMOSH);
		addKillId(27457);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		L2Player player = st.getPlayer();
		if(event.equalsIgnoreCase("3.htm"))
		{
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("7.htm"))
		{
			st.set("cond", "5");
			L2Skill skill = null;
			for (int a = 4322; a < 4328; a++) {
				skill = SkillTable.getInstance().getInfo(a, 1);
				GArray<L2Character> target = new GArray<L2Character>();
				target.add(player);
				npc.broadcastPacket(new MagicSkillUse(npc, player, a, 1, 0, 0));
				npc.callSkill(skill, target, true);
			}
			st.playSound(SOUND_ACCEPT);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		L2Player player = st.getPlayer();
		if(npcId == SHENON)
		{
			QuestState qs = player.getQuestState(_10321_Quest2.class);
			if(cond == 0 && qs != null && qs.getState() == COMPLETED)
				htmltext = "1.htm";
			else if(cond == 1)
				htmltext = "3.htm";
		}
		else if(npcId == IVEN)
		{
			if(cond == 1) {
				htmltext = "4.htm";
				st.set("cond", "2");
			}
			if(cond == 3) {
				htmltext = "5.htm";
				st.set("cond", "4");
			}
			if(cond == 6) {
				htmltext = "8.htm";
				player.sendPacket(new ExShowScreenMessage("Open the inventory and check for weapons.", 1100)); //wtf rus??
				st.giveItems(57, 7000);
				//TODO: weapon
				st.addExpAndSp(300, 800, true);
				st.playSound(SOUND_FINISH);
				st.exitCurrentQuest(false);
			}
		}
		else if(npcId == POMOSH)
		{
			if(cond == 4) {
				htmltext = "6.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		if(st.getState() != STARTED)
			return null;
		if(st.getInt("cond") == 2)
			if(npc.getNpcId() == 27457)
				st.set("cond", "3");
		if(st.getInt("cond") == 5)
			if(npc.getNpcId() == 27457)
				st.set("cond", "6");
		
		return null;
	}
}
