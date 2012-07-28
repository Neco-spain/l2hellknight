package teleports.SeparatedSoul;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.util.Util;

public class SeparatedSoul extends Quest
{
	private static final int BLOOD_CRY = 17268;
	private static final int W_OF_ANTHARAS = 17266;
	private static final int S_BLOOD_CRY = 17267;
	private static final int[] NPC = { 32864, 32865, 32866, 32867, 32868, 32869, 32870, 32891 };
	private static boolean _spawned = false;
	
	private static final int[][] TELEPORTS =
	{
		{73122, 118351, -3704}, // entrance
		{117046, 76798, -2696}, // village
		{99218, 110283, -3696}, // center
		{116992, 113716, -3056}, // North
		{113203, 121063, -3712}, // South
		{131116, 114333, -3704}, // Entrance LoA
		{146129, 111232, -3568}, // bridge
		{148447, 110582, -3944} // Deep LoA
	};
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getNpcId() + ".htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		String htmltext = "";	
		
		if (Util.contains(NPC,npc.getNpcId()))
		{
			if (event.equalsIgnoreCase("exchange"))
			{
				if (st.hasQuestItems(W_OF_ANTHARAS) && st.hasQuestItems(S_BLOOD_CRY))
				{
					st.takeItems(W_OF_ANTHARAS, 1);
					st.takeItems(S_BLOOD_CRY, 1);
					st.giveItems(BLOOD_CRY, 1);
				}
				else
				{
					htmltext = "no-item.htm";
				}
			}
			else if (event.equalsIgnoreCase("story"))
			{
					htmltext = "story.htm";
			}
			else if (player.getLevel() >= 80)
			{
				if (event.equalsIgnoreCase("eofdv"))
				{
					player.teleToLocation(TELEPORTS[0][0], TELEPORTS[0][1], TELEPORTS[0][2]);
				}
				else if (event.equalsIgnoreCase("hv"))
				{
					player.teleToLocation(TELEPORTS[1][0], TELEPORTS[1][1], TELEPORTS[1][2]);
				}
				else if (event.equalsIgnoreCase("tcodv"))
				{
					player.teleToLocation(TELEPORTS[2][0], TELEPORTS[2][1], TELEPORTS[2][2]);
				}
				else if (event.equalsIgnoreCase("didvn"))
				{
					player.teleToLocation(TELEPORTS[3][0], TELEPORTS[3][1], TELEPORTS[3][2]);
				}
				else if (event.equalsIgnoreCase("didvs"))
				{
					player.teleToLocation(TELEPORTS[4][0], TELEPORTS[4][1], TELEPORTS[4][2]);
				}
				else if (event.equalsIgnoreCase("eoal"))
				{
					player.teleToLocation(TELEPORTS[5][0], TELEPORTS[5][1], TELEPORTS[5][2]);
				}
				else if (event.equalsIgnoreCase("almffb"))
				{
					player.teleToLocation(TELEPORTS[6][0], TELEPORTS[6][1], TELEPORTS[6][2]);
				}
				else if (event.equalsIgnoreCase("dial"))
				{
					player.teleToLocation(TELEPORTS[7][0], TELEPORTS[7][1], TELEPORTS[7][2]);
				}
			}
			else
			{
				return "no-lvl.htm";
			}
		}
		return htmltext;
	}

	public SeparatedSoul(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for(int npc : NPC)
		{
			addStartNpc(npc);
			addFirstTalkId(npc);
			addTalkId(npc);
		}
		if(!_spawned)
		{
			addSpawn( 32864, 117168, 76834,  -2688, 35672, false, 0);
			addSpawn( 32865, 99111,  110361, -3688, 54054, false, 0);
			addSpawn( 32866, 116946, 113555, -3056, 45301, false, 0);
			addSpawn( 32867, 113071, 121043, -3712, 25933, false, 0);
			addSpawn( 32868, 148558, 110541, -3944, 28938, false, 0);
			addSpawn( 32869, 146014, 111226, -3560, 25240, false, 0);
			addSpawn( 32870, 73306,  118423, -3704, 42339, false, 0);
			addSpawn( 32891, 131156, 114177, -3704, 11547, false, 0);
			_spawned = true;
		}
	}
	
	public static void main(String[] args)
	{
		new SeparatedSoul(-1, "SeparatedSoul", "teleports");
	}
}