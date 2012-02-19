package zone_scripts.Hellbound.Falk;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;

public class Falk extends Quest
{
	private static final int FALK = 32297;
	private static final int BASIC_CERT = 9850;
	private static final int STANDART_CERT = 9851;
	private static final int PREMIUM_CERT = 9852;
	private static final int DARION_BADGE = 9674;

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getInventory().getInventoryItemCount(BASIC_CERT, -1, false) > 0
				|| player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) > 0
				|| player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) > 0)
			return "32297-01a.htm";
		
		else
			return "32297-01.htm";
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getInventory().getInventoryItemCount(BASIC_CERT, -1, false) > 0
				|| player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) > 0
				|| player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) > 0)
			return "32297-01a.htm";
		else
			return "32297-02.htm";
	}

	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("badges"))
		{
			if (player.getInventory().getInventoryItemCount(BASIC_CERT, -1, false) < 1
					&& player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) < 1
					&& player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) < 1)
			{
				if (player.getInventory().getInventoryItemCount(DARION_BADGE, -1, false) >= 20)
				{
					if (player.destroyItemByItemId("Quest", DARION_BADGE, 20, npc, true))
					{
						player.addItem("Quest", BASIC_CERT, 1, npc, true);
						return "32297-02a.htm";
					}
				}
				
				return "32297-02b.htm";
			}
		}
		
		return event;
	}

	public Falk(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(FALK);
		addStartNpc(FALK);
		addTalkId(FALK);
	}

	public static void main(String[] args)
	{
		new Falk(-1, Falk.class.getSimpleName(), "zone_scripts/Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Hellbound: Falk");
	}
}
