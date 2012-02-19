package zone_scripts.Hellbound.Budenka;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;

public class Budenka extends Quest
{
	private static final int BUDENKA = 32294;
	private static final int STANDART_CERT = 9851;
	private static final int PREMIUM_CERT = 9852;
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) > 0)
			return "32294-premium.htm";
		if (player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) > 0)
			return "32294-standart.htm";
		
		npc.showChatWindow(player);
		return null;
	}
	
	public Budenka(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(BUDENKA);
	}
	
	public static void main(String[] args)
	{
		new Budenka(-1, Budenka.class.getSimpleName(), "zone_scripts/Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Hellbound: Budenka");
	}
}