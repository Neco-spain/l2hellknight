package l2.hellknight.gameserver.network.clientpackets;

import java.util.logging.Logger;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.BlockList;
import l2.hellknight.gameserver.model.L2Party;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.AskJoinParty;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.util.BotPunish;

public final class RequestJoinParty extends L2GameClientPacket
{
	private static final String _C__29_REQUESTJOINPARTY = "[C] 29 RequestJoinParty";
	private static Logger _log = Logger.getLogger(RequestJoinParty.class.getName());
	
	private String _name;
	private int _itemDistribution;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		_itemDistribution = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance requestor = getClient().getActiveChar();
		L2PcInstance target = L2World.getInstance().getPlayer(_name);
		
		if (requestor == null)
			return;
		
		if (target == null)
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FIRST_SELECT_USER_TO_INVITE_TO_PARTY));
			return;
		}
		
        // Check for bot punishment on target
        if(target.isBeingPunished())
        {
        	// Check conditions
        	if(target.getPlayerPunish().canJoinParty() && target.getBotPunishType() == BotPunish.Punish.PARTYBAN)
        	{
        		target.endPunishment();
        	}
        	else
        	{
        		// Inform the player cannot join party
        		requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USER_REPORTED_AND_CANNOT_JOIN_PARTY));
        		return;
        	}
        		
        }
        
        // Check for bot punishment on requestor
        if(requestor.isBeingPunished())
        {
        	// Check conditions
        	if(requestor.getPlayerPunish().canJoinParty() && requestor.getBotPunishType() == BotPunish.Punish.PARTYBAN)
        	{
        		requestor.endPunishment();
        	}
        	else
        	{
        		SystemMessageId msgId = null;
        		switch(requestor.getPlayerPunish().getDuration())
        		{
        			case 3600:
        				msgId = SystemMessageId.REPORTED_60_MINS_WITHOUT_JOIN_PARTY;
        				break;
        			case 7200:
        				msgId = SystemMessageId.REPORTED_120_MINS_WITHOUT_JOIN_PARTY;
        				break;
        			case 10800:
        				msgId = SystemMessageId.REPORTED_180_MINS_WITHOUT_JOIN_PARTY;
        				break;
        				default:
        		}	
        		requestor.sendPacket(SystemMessage.getSystemMessage(msgId));
        		return;
        	}	
        }
        
		if (target.getAppearance().getInvisible())
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}
		
		if (target.isInParty())
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_IN_PARTY);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			return;
		}
		
		if (BlockList.isBlocked(target, requestor))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addCharName(target);
			requestor.sendPacket(sm);
			return;
		}
		
		if (target == requestor)
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
			return;
		}
		
		if (target.isCursedWeaponEquipped() || requestor.isCursedWeaponEquipped())
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		if (target.isInJail() || requestor.isInJail())
		{
			requestor.sendMessage("Player is in Jail");
			return;
		}
		
		if (target.getClient().isDetached() || target.getOfflineSuperMode())
		{
			requestor.sendMessage("Player is in offline mode.");
			return;
		}
		
		if (target.isInOlympiadMode() || requestor.isInOlympiadMode())
		{
			if (target.isInOlympiadMode() != requestor.isInOlympiadMode()
					|| target.getOlympiadGameId() != requestor.getOlympiadGameId()
					|| target.getOlympiadSide() != requestor.getOlympiadSide())
				return;
		}
		
		SystemMessage info = SystemMessage.getSystemMessage(SystemMessageId.C1_INVITED_TO_PARTY);
		info.addCharName(target);
		requestor.sendPacket(info);
		
		if (!requestor.isInParty())     //Asker has no party
		{
			createNewParty(target, requestor);
		}
		else                            //Asker is in party
		{
			if(requestor.getParty().isInDimensionalRift())
			{
				requestor.sendMessage("You can't invite a player when in Dimensional Rift.");
			}
			else
			{
				addTargetToParty(target, requestor);
			}
		}
	}
	
	/**
	 * @param client
	 * @param itemDistribution
	 * @param target
	 * @param requestor
	 */
	private void addTargetToParty(L2PcInstance target, L2PcInstance requestor)
	{
		SystemMessage msg;
		L2Party party = requestor.getParty();
		
		// summary of ppl already in party and ppl that get invitation
		if (!party.isLeader(requestor))
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_LEADER_CAN_INVITE));
			return;
		}
		if (party.getMemberCount() >= 9 )
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_FULL));
			return;
		}
		if (party.getPendingInvitation() && !party.isInvitationRequestExpired())
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WAITING_FOR_ANOTHER_REPLY));
			return;
		}
		
		if (!target.isProcessingRequest())
		{
			requestor.onTransactionRequest(target);
			// in case a leader change has happened, use party's mode
			target.sendPacket(new AskJoinParty(requestor.getName(), party.getLootDistribution()));
			party.setPendingInvitation(true);
			
			if (Config.DEBUG)
				_log.fine("sent out a party invitation to:"+target.getName());
			
		}
		else
		{
			msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			
			if (Config.DEBUG)
				_log.warning(requestor.getName() + " already received a party invitation");
		}
		msg = null;
	}
	
	
	/**
	 * @param client
	 * @param itemDistribution
	 * @param target
	 * @param requestor
	 */
	private void createNewParty(L2PcInstance target, L2PcInstance requestor)
	{
		if (!target.isProcessingRequest())
		{
			requestor.setParty(new L2Party(requestor, _itemDistribution));
			
			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), _itemDistribution));
			requestor.getParty().setPendingInvitation(true);
			
			if (Config.DEBUG)
				_log.fine("sent out a party invitation to:"+target.getName());
			
		}
		else
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WAITING_FOR_ANOTHER_REPLY));
			
			if (Config.DEBUG)
				_log.warning(requestor.getName() + " already received a party invitation");
		}
	}
	
	/* (non-Javadoc)
	 * @see l2.hellknight.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__29_REQUESTJOINPARTY;
	}
}
