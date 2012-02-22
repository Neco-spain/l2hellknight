package zone_scripts.Hellbound.Bernarde;

import l2.hellknight.Config;
import l2.hellknight.gameserver.instancemanager.HellboundManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;

public class Bernarde extends Quest
{
	private static final int BERNARDE = 32300;
	private static final int NATIVE_TRANSFORM = 101;
	private static final int HOLY_WATER = 9673;
	private static final int DARION_BADGE = 9674;
	private static final int TREASURE = 9684;

	private static final boolean isTransformed(L2PcInstance player)
	{
		return player.isTransformed() && player.getTransformation().getId() == NATIVE_TRANSFORM;
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ("HolyWater".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() == 2)
			{
				if (player.getInventory().getInventoryItemCount(DARION_BADGE, -1, false) >= 5)
				{
					if (player.destroyItemByItemId("Quest", DARION_BADGE, 5, npc, true))
					{
						player.addItem("Quest", HOLY_WATER, 1, npc, true);
						return "32300-02b.htm";
					}
				}
			}
			return "32300-02c.htm";
		}

		else if ("Treasure".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() == 3)
			{
				if (player.getInventory().getInventoryItemCount(TREASURE, -1, false) > 0)
				{
					if (player.destroyItemByItemId("Quest", TREASURE, player.getInventory().getInventoryItemCount(TREASURE, -1, false), npc, true))
					{
						HellboundManager.getInstance().updateTrust((int)(player.getInventory().getInventoryItemCount(TREASURE, -1, false) * 1000), true);
						return "32300-02d.htm";
					}
				}
			}
			return "32300-02e.htm";
		}
		
		else if ("rumors".equalsIgnoreCase(event))
			return "32300-" + HellboundManager.getInstance().getLevel() + "r.htm";

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
				return isTransformed(player) ? "32300-01a.htm" : "32300-01.htm";
			case 2:
				return isTransformed(player) ? "32300-02.htm" : "32300-03.htm";
			case 3:
				return isTransformed(player) ? "32300-01c.htm" : "32300-03.htm";
			case 4:
				return isTransformed(player) ? "32300-01d.htm" : "32300-03.htm";
			default:
				return isTransformed(player) ? "32300-01f.htm" : "32300-03.htm"; 
		}
	}

	public Bernarde(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(BERNARDE);
		addStartNpc(BERNARDE);
		addTalkId(BERNARDE);
	}

	public static void main(String[] args)
	{
		new Bernarde(-1, Bernarde.class.getSimpleName(), "zone_scripts/Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Hellbound: Bernarde");
	}
}
