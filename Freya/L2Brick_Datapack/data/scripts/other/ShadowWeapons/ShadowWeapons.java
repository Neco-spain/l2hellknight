package other.ShadowWeapons;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;

public class ShadowWeapons extends Quest
{
	private final static int[] NPCs =
	{
		30037,30066,30070,30109,30115,30120,30174,30175,30176,30187,30191,30195,
		30288,30289,30290,30297,30373,30462,30474,30498,30499,30500,30503,30504, 
		30505,30508,30511,30512,30513,30595,30676,30677,30681,30685,30687,30689,30694, 
		30699,30704,30845,30847,30849,30854,30857,30862,30865,30894,30897,30900, 
		30905,30910,30913,31269,31272,31288,31314,31317,31321,31324,31326,31328, 
		31331,31334,31336,31965,31974,31276,31285,31958,31961,31996,31968,31977, 
		32092,32093,32094,32095,32096,32097,32098,32193,32196,32199,32202,32205, 
		32206,32213,32214,32221,32222,32229,32230,32233,32234
	};
	
	private final static int D_GRADE_COUPON = 8869;
	private final static int C_GRADE_COUPON = 8870;

	public ShadowWeapons(int questId, String name, String descr) {
		super(questId, name, descr);
		for (int id : NPCs)
		{
			addStartNpc(id);
			addTalkId(id);
		}
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		
		boolean playerOwnsDGradeExchangeCoupons = st.getQuestItemsCount(D_GRADE_COUPON) > 0;
		boolean playerOwnsCGradeExchangeCoupons = st.getQuestItemsCount(C_GRADE_COUPON) > 0;
		
		if(playerOwnsDGradeExchangeCoupons || playerOwnsCGradeExchangeCoupons)
		{
			int multisellId = 306893003;
			if(!playerOwnsDGradeExchangeCoupons)
				multisellId = 306893002;
			else if(!playerOwnsCGradeExchangeCoupons)
				multisellId = 306893001;
			
			htmltext = st.showHtmlFile("exchange.htm").replace("%msid%", String.valueOf(multisellId));
		} else {
		      htmltext = "exchange-no.htm";
		}
		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new ShadowWeapons(-1, "ShadowWeapons", "other");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Other: Shadow Weapons");
	}
}