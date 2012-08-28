package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.TvTEventTeleporter;

public class AdminTvTEvent
implements IAdminCommandHandler {
	private static final String[] ADMIN_COMMANDS = {"admin_tvt_add", "admin_tvt_remove"};

	public boolean useAdminCommand( String command, L2PcInstance activeChar )
	{
		if ( command.equals( "admin_tvt_add" ) ) {
			L2Object target = activeChar.getTarget();

			if (!( target instanceof L2PcInstance ) )
			{
				activeChar.sendMessage( "You should select a player!" );
				return true;
			}

			add( activeChar, ( L2PcInstance )target );
		}
		else if ( command.equals( "admin_tvt_remove" ) )
		{
			L2Object target = activeChar.getTarget();

			if (!( target instanceof L2PcInstance ) )
			{
				activeChar.sendMessage( "You should select a player!" );
				return true;
			}

			remove( activeChar, ( L2PcInstance )target );
		}

		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void add( L2PcInstance activeChar, L2PcInstance playerInstance ) {
		if ( TvTEvent.isPlayerParticipant( playerInstance.getObjectId() ) ) {
			activeChar.sendMessage( "Player already participated in the event!" );
			return;
		}

		if ( !TvTEvent.addParticipant( playerInstance ) ) {
			activeChar.sendMessage( "Player instance could not be added, it seems to be null!" );
			return;
		}

		if ( TvTEvent.isStarted() ) {
			new TvTEventTeleporter( playerInstance, TvTEvent.getParticipantTeamCoordinates( playerInstance.getObjectId() ), true, false );
		}
	}

	private void remove( L2PcInstance activeChar, L2PcInstance playerInstance ) {
		if ( !TvTEvent.removeParticipant( playerInstance.getObjectId() ) ) {
			activeChar.sendMessage( "Player is not part of the event!" );
			return;
		}

		new TvTEventTeleporter( playerInstance, Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES, true, true );
	}
}
