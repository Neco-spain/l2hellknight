package other.PinsAndPouchUnseal;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.network.serverpackets.NpcSay;
import l2.brick.util.Rnd;

public class PinsAndPouchUnseal extends Quest
{
	private final static int[] NPCs =
	{
		32610,32612
	};
	
	private final static int[] UNSEALPRICE = {3200,11800,26500,136600};
	// failed, low, mid, high, top  
	private final static int[] CHANCES = {49,78,95,99,100};
	
	// sealdId, lowId, midId, highId, topId
	private final static int[][] PINS = {{13898,13902,13903,13904,13905},
		{13899,13906,13907,13908,13909},
		{13900,13910,13911,13912,13913},
		{13901,13914,13915,13916,13917}
	};
	
	// sealdId, lowId, midId, highId, topId
	private final static int[][] POUCHS = {{13918,13922,13923,13924,13925},
		{13919,13926,13927,13928,13929},
		{13920,13930,13931,13932,13933},
		{13921,13934,13935,13936,13937}
	};
	
	// "B,C grade" is the Magic Clip
	// "A,S grade" is the Magic Ornament
	// sealdId, lowId, midId, highId, topId
	private final static int[][] CLIPSORNAMENTS = {{14902,14906,14907,14908,14909},
		{14903,14910,14911,14912,14913},
		{14904,14914,14915,14916,14917},
		{14905,14918,14919,14920,14921}
	};
	
	public PinsAndPouchUnseal(int questId, String name, String descr)
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
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		
		htmltext = event;
		if (event.contains("_grade_"))
		{
			int grade = Integer.parseInt(event.substring(0, 1));
			int price;
			int[] itemIds;
			if (event.endsWith("_pin"))
			{
				price = UNSEALPRICE[grade];
				itemIds = PINS[grade];
			}
			else if (event.endsWith("_pouch"))
			{
				price = UNSEALPRICE[grade];
				itemIds = POUCHS[grade];
			}
			else if (event.endsWith("_clip"))
			{
				price = UNSEALPRICE[grade];
				itemIds = CLIPSORNAMENTS[grade - 2];
			}
			else if (event.endsWith("_ornament"))
			{
				price = UNSEALPRICE[grade];
				itemIds = CLIPSORNAMENTS[grade];
			}
			else
				// this should not happen!
				return "";
			if (st.getQuestItemsCount(itemIds[0]) > 0)
			{
				if (st.getQuestItemsCount(57) > price)
				{
					htmltext = "";
					st.takeItems(57, price);
					st.takeItems(itemIds[0], 1);
					int rand = Rnd.get(100);
					if (rand < CHANCES[0])
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), 1300162));
					else if (rand < CHANCES[1])
						st.giveItems(itemIds[1], 1);
					else if (rand < CHANCES[2])
						st.giveItems(itemIds[2], 1);
					else if (rand < CHANCES[3])
						st.giveItems(itemIds[3], 1);
					else
						st.giveItems(itemIds[4], 1);
				}
				else
					htmltext = npc.getNpcId() + "-low.htm";
			}
			else
				htmltext = npc.getNpcId() + "-no.htm";
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		htmltext = npc.getNpcId() + "-1.htm";
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new PinsAndPouchUnseal(-1, "PinsAndPouchUnseal", "other");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Other: Pins And Pouch Unseal");
	}
}
