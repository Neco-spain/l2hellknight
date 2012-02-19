package zone_scripts.Hellbound.Kief;

import l2.brick.Config;
import l2.brick.gameserver.instancemanager.HellboundManager;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;

public class Kief extends Quest
{
	private static final int KIEF = 32354;

	private static final int BOTTLE = 9672;
	private static final int DARION_BADGE = 9674;
	private static final int DIM_LIFE_FORCE = 9680;
	private static final int LIFE_FORCE = 9681;
	private static final int CONTAINED_LIFE_FORCE = 9682;
	private static final int STINGER = 10012;

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ("Badges".equalsIgnoreCase(event))
		{
			switch (HellboundManager.getInstance().getLevel())
			{
				case 2:
				case 3:
					final long num = player.getInventory().getInventoryItemCount(DARION_BADGE, -1, false);
					if (num > 0)
					{
						if (player.destroyItemByItemId("Quest", DARION_BADGE, num, npc, true))
						{
							HellboundManager.getInstance().updateTrust((int)num * 10, true);
							return "32354-10.htm";
						}
					}
			}
			return "32354-10a.htm";
		}
		else if ("Bottle".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() >= 7)
			{
				if (player.getInventory().getInventoryItemCount(STINGER, -1, false) >= 20)
				{
					if (player.destroyItemByItemId("Quest", STINGER, 20, npc, true))
					{
						player.addItem("Quest", BOTTLE, 1, npc, true);
						return "32354-11h.htm";
					}
				}
				return "32354-11i.htm";
			}
		}
		else if ("dlf".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() == 7)
			{
				final long num = player.getInventory().getInventoryItemCount(DIM_LIFE_FORCE, -1, false);
				if (num > 0)
				{
					if (player.destroyItemByItemId("Quest", DIM_LIFE_FORCE, num, npc, true))
					{
						HellboundManager.getInstance().updateTrust((int)num * 20, true);
						return "32354-11a.htm";
					}
				}
				return "32354-11b.htm";
			}
		}
		else if ("lf".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() == 7)
			{
				final long num = player.getInventory().getInventoryItemCount(LIFE_FORCE, -1, false);
				if (num > 0)
				{
					if (player.destroyItemByItemId("Quest", LIFE_FORCE, num, npc, true))
					{
						HellboundManager.getInstance().updateTrust((int)num * 80, true);
						return "32354-11c.htm";
					}
				}
				return "32354-nolifeforce.htm";
			}
		}
		else if ("clf".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() == 7)
			{
				final long num = player.getInventory().getInventoryItemCount(CONTAINED_LIFE_FORCE, -1, false);
				if (num > 0)
				{
					if (player.destroyItemByItemId("Quest", CONTAINED_LIFE_FORCE, num, npc, true))
					{
						HellboundManager.getInstance().updateTrust((int)num * 200, true);
						return "32354-11e.htm";
					}
				}
				return "32354-11f.htm";
			}
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
				return "32354-01.htm";
			case 2:
			case 3:
				return "32354-01a.htm";
			case 4:
				return "32354-01e.htm";
			case 5:
				return "32354-01d.htm";
			case 6:
				return "32354-01b.htm";
			case 7:
				return "32354-01c.htm";
			default:
				return "32354-01f.htm";
		}
	}

	public Kief(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(KIEF);
		addStartNpc(KIEF);
		addTalkId(KIEF);
	}

	public static void main(String[] args)
	{
		new Kief(-1, Kief.class.getSimpleName(), "zone_scripts/Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Hellbound: Kief");
	}
}
