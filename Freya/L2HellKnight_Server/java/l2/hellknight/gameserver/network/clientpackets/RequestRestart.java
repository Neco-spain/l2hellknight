package l2.hellknight.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2.hellknight.Config;
import l2.hellknight.gameserver.SevenSignsFestival;
import l2.hellknight.gameserver.instancemanager.AntiFeedManager;
import l2.hellknight.gameserver.model.L2Party;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.L2GameClient;
import l2.hellknight.gameserver.network.L2GameClient.GameClientState;
import l2.hellknight.gameserver.network.MultiBoxProtection;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.CharSelectionInfo;
import l2.hellknight.gameserver.network.serverpackets.RestartResponse;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.taskmanager.AttackStanceTaskManager;

public final class RequestRestart extends L2GameClientPacket
{
	private static final String _C__46_REQUESTRESTART = "[C] 46 RequestRestart";
	private static final Logger _log = Logger.getLogger(RequestRestart.class.getName());
	protected static final Logger _logAccounting = Logger.getLogger("accounting");
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		if(player.getActiveEnchantItem() != null || player.getActiveEnchantAttrItem() != null)
		{
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.isLocked())
		{
			_log.warning("Player " + player.getName() + " tried to restart during class change.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (!Config.OFFLINE_SUPER_MODE_ENABLE)
		{
           if (player.getPrivateStoreType() != 0)
           {
               player.sendMessage("Cannot restart while trading");
               sendPacket(RestartResponse.valueOf(false));
               return;
           }
		}
		
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player) && !(player.isGM() && Config.GM_RESTART_FIGHTING))
		{
			if (Config.DEBUG)
				_log.fine("Player " + player.getName() + " tried to logout while fighting.");
			
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_RESTART_WHILE_FIGHTING));
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		// Prevent player from restarting if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if (player.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendMessage("You cannot restart while you are a participant in a festival.");
				sendPacket(RestartResponse.valueOf(false));
				return;
			}
			
			final L2Party playerParty = player.getParty();
			
			if (playerParty != null)
				player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
		}
		// Remove player from Boss Zone
		player.removeFromBossZone();
       // Support for Offline Trade / Craft Super Mode
       if (Config.OFFLINE_SUPER_MODE_ENABLE)
       {
           if ((player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE)
                   || (player.isInCraftMode() && Config.OFFLINE_CRAFT_ENABLE))
           {
               player.leaveParty();
               if (Config.OFFLINE_SET_NAME_COLOR)
               {
                   player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
                   player.broadcastUserInfo();
               }
               if (player.getOfflineStartTime() == 0)
                   player.setOfflineStartTime(System.currentTimeMillis());
               
               player.setOfflineSuperMode(true);
               LogRecord record = new LogRecord(Level.INFO, "Entering offline mode");
               record.setParameters(new Object[]{this.getClient()});
               _logAccounting.log(record);
           }
       }
     
		final L2GameClient client = getClient();
		
		LogRecord record = new LogRecord(Level.INFO, "Logged out");
		record.setParameters(new Object[]{client});
		_logAccounting.log(record);
		
		if (Config.ALLOW_MAX_PLAYERS_FROM_ONE_PC) 
		{
			if (Config.MAX_PLAYERS_FROM_ONE_PC > 0) 
			{
				MultiBoxProtection.getInstance().removeConnection(client);
			}
		}
		
		// detach the client from the char so that the connection isnt closed in the deleteMe
		player.setClient(null);
		
		if (!player.getOfflineSuperMode())
		{
			player.deleteMe();
		}
		
		client.setActiveChar(null);
		AntiFeedManager.getInstance().onDisconnect(client);
		
		// return the client to the authed status
		client.setState(GameClientState.AUTHED);
		
		sendPacket(RestartResponse.valueOf(true));
		
		// send char list
		final CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
	
	/* (non-Javadoc)
	 * @see l2.hellknight.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__46_REQUESTRESTART;
	}
}