package intelligence.NPCs.EngineerLekon;

import l2.brick.Config;
import l2.brick.gameserver.instancemanager.AirShipManager;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.SystemMessage;

public class EngineerLekon extends Quest
{
	private static final int LEKON = 32557;
	
	private static final int LICENSE = 13559;
	private static final int STARSTONE = 13277;
	private static final int LICENSE_COST = 10;
	
	private static final SystemMessage SM_NEED_CLANLVL5 = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NEED_CLANLVL_5_TO_SUMMON);
	private static final SystemMessage SM_NO_PRIVS = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NO_PRIVILEGES);
	private static final SystemMessage SM_LICENSE_ALREADY_ACQUIRED = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_SUMMON_LICENSE_ALREADY_ACQUIRED);
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ("license".equalsIgnoreCase(event))
		{
			if (player.getClan() == null || player.getClan().getLevel() < 5)
			{
				player.sendPacket(SM_NEED_CLANLVL5);
				return null;
			}
			if (!player.isClanLeader())
			{
				player.sendPacket(SM_NO_PRIVS);
				return null;
			}
			if (AirShipManager.getInstance().hasAirShipLicense(player.getClanId()))
			{
				player.sendPacket(SM_LICENSE_ALREADY_ACQUIRED);
				return null;
			}
			if (player.getInventory().getItemByItemId(LICENSE) != null)
			{
				player.sendPacket(SM_LICENSE_ALREADY_ACQUIRED);
				return null;
			}
			if (!player.destroyItemByItemId("AirShipLicense", STARSTONE, LICENSE_COST, npc, true))
				return null;
			
			player.addItem("AirShipLicense", LICENSE, 1, npc, true);
			return null;
		}
		else
			return event;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);
		
		return npc.getNpcId() + ".htm";
	}
	
	public EngineerLekon(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(LEKON);
		addFirstTalkId(LEKON);
		addTalkId(LEKON);
	}
	
	public static void main(String[] args)
	{
		new EngineerLekon(-1, EngineerLekon.class.getSimpleName(), "intelligence/NPCs");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded NPC: Engineer Lekon");
	}
}