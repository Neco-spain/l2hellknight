package teleports.SteelCitadelTeleport;

import l2.hellknight.Config;
import l2.hellknight.gameserver.instancemanager.GrandBossManager;
import l2.hellknight.gameserver.instancemanager.ZoneManager;
import l2.hellknight.gameserver.model.L2CommandChannel;
import l2.hellknight.gameserver.model.L2Party;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.zone.type.L2BossZone;

public class SteelCitadelTeleport extends Quest
{
	private static final int BELETH = 29118;
	private static final int NAIA_CUBE = 32376;
	
	public SteelCitadelTeleport(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NAIA_CUBE);
		addTalkId(NAIA_CUBE);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		switch(npc.getNpcId())
		{
			case NAIA_CUBE:
			if (GrandBossManager.getInstance().getBossStatus(BELETH) == 3)
				return "32376-02.htm";
			
			final L2CommandChannel channel = player.getParty() == null ? null : player.getParty().getCommandChannel();
			
			if (channel == null || channel.getChannelLeader().getObjectId() != player.getObjectId() || channel.getMemberCount() < Config.BELETH_MIN_PLAYERS)
				return "32376-02a.htm";
			
			if (GrandBossManager.getInstance().getBossStatus(BELETH) > 0)
				return "32376-03.htm";

			L2BossZone zone = (L2BossZone) ZoneManager.getInstance().getZoneById(12018);
			
			if (zone != null)
			{
				GrandBossManager.getInstance().setBossStatus(BELETH, 1);

				for (L2Party party : channel.getPartys())
				{
					if (party == null)
						continue;

					for (L2PcInstance pl : party.getPartyMembers())
					{
						if (pl.isInsideRadius(npc.getX(),npc.getY(),npc.getZ(), 3000, true, false))
						{
							zone.allowPlayerEntry(pl, 30);
							pl.teleToLocation(16342, 209557, -9352, true);
						}
					}
				}
			}
		}

		return null;
	}

	public static void main(String[] args)
	{
		new SteelCitadelTeleport(-1, "SteelCitadelTeleport", "teleports");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Teleport: Steel Citadel Teleport");
	}
}
