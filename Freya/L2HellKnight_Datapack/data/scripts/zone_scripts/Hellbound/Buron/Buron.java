package zone_scripts.Hellbound.Buron;

import l2.hellknight.Config;
import l2.hellknight.gameserver.instancemanager.HellboundManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;

public class Buron extends Quest
{
	private static final int BURON = 32345;
	private static final int HELMET = 9669;
	private static final int TUNIC = 9670;
	private static final int PANTS = 9671;
	private static final int DARION_BADGE = 9674;

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ("Rumor".equalsIgnoreCase(event))
			return "32345-" + HellboundManager.getInstance().getLevel() + "r.htm";

		else if ("Tunic".equalsIgnoreCase(event) || "Helmet".equalsIgnoreCase(event) || "Pants".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() < 2)
				return "32345-lowlvl.htm";

			if (player.getInventory().getInventoryItemCount(DARION_BADGE, -1, false) >= 10)
			{
				if (player.destroyItemByItemId("Quest", DARION_BADGE, 10, npc, true))
				{
					if ("Tunic".equalsIgnoreCase(event))
						player.addItem("Quest", TUNIC, 1, npc, true);
					else if ("Helmet".equalsIgnoreCase(event))
						player.addItem("Quest", HELMET, 1, npc, true);
					else if ("Pants".equalsIgnoreCase(event))
						player.addItem("Quest", PANTS, 1, npc, true);
					return null;
				}
			}

			return "32345-noitems.htm";
		}
		
		return event;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);

		switch (HellboundManager.getInstance().getLevel())
		{
			case 1:
				return "32345-01.htm";
			case 2:
			case 3:
			case 4:
				return "32345-02.htm";
			default:
				return "32345-01a.htm";
		}
	}

	public Buron(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(BURON);
		addStartNpc(BURON);
		addTalkId(BURON);
	}

	public static void main(String[] args)
	{
		new Buron(-1, Buron.class.getSimpleName(), "zone_scripts/Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Hellbound: Buron");
	}
}
