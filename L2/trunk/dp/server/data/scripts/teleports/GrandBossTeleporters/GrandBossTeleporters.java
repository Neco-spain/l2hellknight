package teleports.GrandBossTeleporters;

import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.Config;

public class GrandBossTeleporters extends Quest
{
	private final static int[] NPCs =
	{
		31384,31385,31540,31686,31687,31759
	};

	private Quest valakasAI()
	{
		return QuestManager.getInstance().getQuest("valakas");
	}

	private int count = 0;

	public GrandBossTeleporters(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int id : NPCs)
		{
			addStartNpc(id);
			addTalkId(id);
		}
	}

	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}

		if (event.equalsIgnoreCase("31540"))
		{
			if (st.getQuestItemsCount(7267) > 0)
			{
				st.takeItems(7267, 1);
				player.teleToLocation(183813, -115157, -3303);
				st.set("allowEnter","1");
			}
			else
				htmltext = "31540-06.htm";
		}

		return htmltext;
	}

	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());

		int npcId = npc.getNpcId();

		if (npcId == 31385)
		{
			if (valakasAI() != null)
			{
				int status = GrandBossManager.getInstance().getBossStatus(29028);
				if (status == 0 || status == 1)
				{
					if (count >= 200)
						htmltext = "31385-03.htm";
					else if (st.getInt("allowEnter") == 1)
					{
						st.unset("allowEnter");
						L2BossZone zone = GrandBossManager.getInstance().getZone(212852, -114842, -1632);
						if (zone != null)
							zone.allowPlayerEntry(player, 30);
						int x = 204328 + Rnd.get(600);
						int y = -111874 + Rnd.get(600);
						player.teleToLocation(x, y, 70);
						count++;
						if (status == 0)
						{
							L2GrandBossInstance valakas = GrandBossManager.getInstance().getBoss(29028);
							valakasAI().startQuestTimer("1001", Config.Valakas_Wait_Time, valakas, null);
							GrandBossManager.getInstance().setBossStatus(29028, 1);
						}
					}
					else //player cheated, wasn't ported via npc Klein
						htmltext = "31385-04.htm";
				}
				else if (status == 2)
					htmltext = "31385-02.htm";
				else
					htmltext = "31385-01.htm";
			}
			else
				htmltext = "31385-01.htm";
		}
		else if (npcId == 31384)
			DoorTable.getInstance().getDoor(24210004).openMe();
		else if (npcId == 31686)
			DoorTable.getInstance().getDoor(24210005).openMe();
		else if (npcId == 31687)
			DoorTable.getInstance().getDoor(24210006).openMe();
		else if (npcId == 31540)
		{
			if (count < 50)
				htmltext = "31540-01.htm";
			else if (count < 100)
				htmltext = "31540-02.htm";
			else if (count < 150)
				htmltext = "31540-03.htm";
			else if (count < 200)
				htmltext = "31540-04.htm";
			else
				htmltext = "31540-05.htm";
		}
		else if (npcId == 31759)
		{
			int x = 150037 + Rnd.get(500);
			int y = -57720 + Rnd.get(500);
			player.teleToLocation(x, y, -2976);
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		new GrandBossTeleporters(-1, "GrandBossTeleporters", "teleports");
	}
}