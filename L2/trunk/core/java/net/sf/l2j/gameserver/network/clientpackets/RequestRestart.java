/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CustomZoneManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.network.serverpackets.RestartResponse;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.util.FloodProtector;

/**
 * This class ...
 *
 * @version $Revision: 1.11.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestRestart extends L2GameClientPacket
{
    private static final String _C__46_REQUESTRESTART = "[C] 46 RequestRestart";
    private static Logger _log = Logger.getLogger(RequestRestart.class.getName());


    @Override
	protected void readImpl()
    {
    	// trigger
    }

    @Override
	protected void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        if (player == null)
        {
            _log.warning("[RequestRestart] activeChar null!?");
            return;
        }
        if (player.isInFunEvent())
       	{
        	player.sendMessage("You can't logout in event.");
        	return;
        }
        
        if(player.getPet() != null)
        {
        	player.sendMessage("You can't logout when your pet is summoned.");
        	return;
        }
        	 
        if (Olympiad.getInstance().isRegisteredInComp(player) || player.getOlympiadGameId() > 0)
        {
            player.sendMessage("You cant logout in olympiad mode");
            return;
        }
        
        //sub class exploit fix
        if(!FloodProtector.getInstance().tryPerformAction(player.getObjectId(), FloodProtector.PROTECTED_SUBCLASSPROTECT))
        {
        	player.sendMessage("You can change Subclass only every " + Config.SUBCLASS_PROTECT + " Millisecond(s)");
        	return;
        }
        
		if (player.isTeleporting())
		{
        	//player.abortCast();
        	//player.setIsTeleporting(false);
			player.sendPacket(SystemMessage.sendString("�� �� ������ ����� ���� ����������������!"));
		    return;
        }
		
		if (player.isMounted())
		{
			player.sendPacket(SystemMessage.sendString("�� �� ������ �����. ������ � �������!"));
		    return;
		}
		
		if (player.getActiveTradeList() != null)
		{
			player.sendPacket(SystemMessage.sendString("�� �� ������ ����� ���� ��������!"));
		    return;
		}
		
		if (player.isCastingNow())
	    {
	      player.sendPacket(SystemMessage.sendString("�� �� ������ ����� ���� ��������!"));
	      return;
	    }
		
		if (player.getActiveEnchantItem() != null)
	    {
	      player.sendMessage("�� �� ������ ����� ���� ����������� �������!");
	      return;
	    }
		
        if (!(player.isGM()))
        {
            if(CustomZoneManager.getInstance().checkIfInZone("NoEscape", player)){
                player.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
                player.sendPacket(new ActionFailed());
                return;
            }
        }

        player.getInventory().updateDatabase();

        if (player.getPrivateStoreType() != 0)
        {
            player.sendMessage("Cannot restart while trading");
            return;
        }

        if (player.getActiveRequester() != null)
        {
            player.getActiveRequester().onTradeCancel(player);
            player.onTradeCancel(player.getActiveRequester());
        }

        if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
        {
            if (Config.DEBUG)
                _log.fine("Player " + player.getName() + " tried to logout while fighting.");

            player.sendPacket(new SystemMessage(SystemMessageId.CANT_RESTART_WHILE_FIGHTING));
            player.sendPacket(new ActionFailed());
            return;
        }

        // Prevent player from restarting if they are a festival participant
        // and it is in progress, otherwise notify party members that the player
        // is not longer a participant.
        if (player.isFestivalParticipant())
        {
            if (SevenSignsFestival.getInstance().isFestivalInitialized())
            {
                player.sendPacket(SystemMessage.sendString("You cannot restart while you are a participant in a festival."));
                player.sendPacket(new ActionFailed());
                return;
            }
            L2Party playerParty = player.getParty();

            if (playerParty != null)
                player.getParty().broadcastToPartyMembers(
                                                          SystemMessage.sendString(player.getName()
                                                              + " has been removed from the upcoming festival."));
        }
        if (player.isFlying())
        {
        	player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
        }

        L2GameClient client = getClient();

        // detach the client from the char so that the connection isnt closed in the deleteMe
        player.setClient(null);

        RegionBBSManager.getInstance().changeCommunityBoard();

        // removing player from the world
        player.deleteMe();
        L2GameClient.saveCharToDisk(client.getActiveChar());


        getClient().setActiveChar(null);

        // return the client to the authed status
        client.setState(GameClientState.AUTHED);

        TvTEvent.onLogout(player);
        RestartResponse response = new RestartResponse();
        sendPacket(response);

        // send char list
        CharSelectInfo cl = new CharSelectInfo(client.getAccountName(),
        client.getSessionId().playOkID1);
        sendPacket(cl);
        client.setCharSelection(cl.getCharInfo());
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
	public String getType()
    {
        return _C__46_REQUESTRESTART;
    }
}