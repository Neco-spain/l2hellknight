package zone_scripts.Hellbound.Jude;

import l2.hellknight.Config;
import l2.hellknight.gameserver.instancemanager.HellboundManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;

public class Jude extends Quest
{
	private static final int JUDE = 32356;
	private static final int TREASURE = 9684;

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ("TreasureSacks".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() == 3)
			{
				if (player.getInventory().getInventoryItemCount(TREASURE, -1, false) >= 40)
				{
					if (player.destroyItemByItemId("Quest", TREASURE, 40, npc, true))
					{
						player.addItem("Quest", 9677, 1, npc, true);
						return "32356-02.htm";
					}
				}
			}
			return "32356-02a.htm";
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
			case 0:
			case 1:
			case 2:
				return "32356-01.htm";
			case 3:
			case 4:
				return "32356-01c.htm";
			case 5:
				return "32356-01a.htm";
			default:
				return "32356-01b.htm";
		}
	}

	public Jude(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(JUDE);
		addStartNpc(JUDE);
		addTalkId(JUDE);
	}

	public static void main(String[] args)
	{
		new Jude(-1, Jude.class.getSimpleName(), "zone_scripts/Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Hellbound: Jude");
	}
}
