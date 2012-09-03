package services;

import quests._240_ImTheOnlyOneYouCanTrust._240_ImTheOnlyOneYouCanTrust;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Party;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Files;
import l2rt.util.Location;

public class TeleToStakatoNest extends Functions implements ScriptFile
{
	private final static Location[] teleports = { new Location(80456, -52322, -5640), new Location(88718, -46214, -4640),
			new Location(87464, -54221, -5120), new Location(80848, -49426, -5128), new Location(87682, -43291, -4128) };

	public void list()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		QuestState qs = player.getQuestState(_240_ImTheOnlyOneYouCanTrust.class);
		if(qs == null || !qs.isCompleted())
		{
			show(Files.read("data/scripts/services/TeleToStakatoNest-no.htm", player), player);
			return;
		}

		show(Files.read("data/scripts/services/TeleToStakatoNest.htm", player), player);
	}

	public void teleTo(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;
		if(args.length != 1)
			return;

		Location loc = teleports[Integer.parseInt(args[0]) - 1];
		L2Party party = player.getParty();
		if(party == null)
			player.teleToLocation(loc);
		else
			for(L2Player member : party.getPartyMembers())
				if(member != null && member.isInRange(player, 1000))
					member.teleToLocation(loc);
	}

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}