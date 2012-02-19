package zone_scripts.FantasyIsle;

import l2.brick.Config;
import l2.brick.gameserver.instancemanager.HandysBlockCheckerManager;
import l2.brick.gameserver.instancemanager.HandysBlockCheckerManager.ArenaParticipantsHolder;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.ExCubeGameChangeTimeToStart;
import l2.brick.gameserver.network.serverpackets.ExCubeGameRequestReady;
import l2.brick.gameserver.network.serverpackets.ExCubeGameTeamList;
import l2.brick.gameserver.network.serverpackets.SystemMessage;

public class HandysBlockCheckerEvent extends Quest 
{
	private static final String qn = "HandysBlockCheckerEvent";
	
	// Arena Managers
	private static final int A_MANAGER_1 = 32521;
	private static final int A_MANAGER_2 = 32522;
	private static final int A_MANAGER_3 = 32523;
	private static final int A_MANAGER_4 = 32524;
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc == null || player == null) return null;
		
		int npcId = npc.getNpcId();
		
		int arena = -1;
		switch(npcId)
		{
		case A_MANAGER_1:
			arena = 0;
			break;
		case A_MANAGER_2:
			arena = 1;
			break;
		case A_MANAGER_3:
			arena = 2;
			break;
		case A_MANAGER_4:
			arena = 3;
			break;
		}
		
		if (arena != -1)
		{
			if (eventIsFull(arena))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_REGISTER_CAUSE_QUEUE_FULL));
				return null;
			}
			if (HandysBlockCheckerManager.getInstance().arenaIsBeingUsed(arena))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MATCH_BEING_PREPARED_TRY_LATER));
				return null;
			}
			if(HandysBlockCheckerManager.getInstance().addPlayerToArena(player, arena))
			{
				ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(arena);
				
				final ExCubeGameTeamList tl = new ExCubeGameTeamList(holder.getRedPlayers(), holder.getBluePlayers(), arena);
				
				player.sendPacket(tl);

				int countBlue = holder.getBlueTeamSize();
				int countRed = holder.getRedTeamSize();
				int minMembers = Config.MIN_BLOCK_CHECKER_TEAM_MEMBERS;
				
				if(countBlue >= minMembers && countRed >= minMembers)
				{
					holder.updateEvent();
					holder.broadCastPacketToTeam(new ExCubeGameRequestReady());
					holder.broadCastPacketToTeam(new ExCubeGameChangeTimeToStart(10));
				}
			}
		}
		return null;
	}
		
	private boolean eventIsFull(int arena)
	{
		if(HandysBlockCheckerManager.getInstance().getHolder(arena).getAllPlayers().size() == 12)
			return true;
		return false;
	}
		
	public HandysBlockCheckerEvent(int questId, String name, String descr) 
	{
		super(questId, name, descr);
		addFirstTalkId(A_MANAGER_1);
		addFirstTalkId(A_MANAGER_2);
		addFirstTalkId(A_MANAGER_3);
		addFirstTalkId(A_MANAGER_4);
	}
	
	public static void main(String[] args)
	{
		if(!Config.ENABLE_BLOCK_CHECKER_EVENT)
			_log.info("Handy's Block Checker Event is disabled");
		else
		{
			new HandysBlockCheckerEvent(-1, qn, "Handy's Block Checker Event");
			HandysBlockCheckerManager.getInstance().startUpParticipantsQueue();
			if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
				_log.info("Handy's Block Checker Event is enabled");
		}
	}
}