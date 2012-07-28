/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.voicedcommandhandlers;

import l2.brick.gameserver.handler.IVoicedCommandHandler;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.entity.CTF;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;

public class CTFCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = { "joinctf", "leavectf", "infoctf" };
	
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("joinctf"))
		{
			JoinCTF(activeChar);
		}
		else if (command.startsWith("leavectf"))
		{
			LeaveCTF(activeChar);
		}
		
		else if (command.startsWith("infoctf"))
		{
			SendCTFinfo(activeChar);
		}
		
		return true;
	}
	
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	public boolean JoinCTF(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
		
		if (!CTF._joining) //check if ctf event is running or/and joining is avaliable
		{
			npcHtmlMessage.setHtml("<html><body>You can not register now: CTF event joining is not avaliable.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
		else if (activeChar._inEventCTF) //check if player is already registered in ctf event
		{
			npcHtmlMessage.setHtml("<html><body>You are participating this event already.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
		else if (activeChar.isCursedWeaponEquipped()) //check if player is holding a cursed weapon
		{
			npcHtmlMessage.setHtml("<html><body>You are not allowed to participate to the Event holding a Cursed Weapon.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
		else if (activeChar.isInOlympiadMode()) //check if player is in olympiads - dunno why, but sometimes simple doesnt work this check =/
		{
			npcHtmlMessage.setHtml("<html><body>You are not allowed to participate to the Event while in Olympiad.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
		else if (activeChar.isInJail()) //check if player is in jail
		{
			npcHtmlMessage.setHtml("<html><body>You are not allowed to participate to the Event while in Jail.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
		else if (activeChar.getLevel() < CTF._minlvl)
		{
			npcHtmlMessage.setHtml("<html><body>Your level is too low to participate at this Event.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
		else if (activeChar.getKarma() > 0) //check player  karma - dunno why karma players cant participate - looks useless since player can register while not with karma and do pk after that ;)
		{
			npcHtmlMessage.setHtml("<html><body>You are not allowed to participate to the Event with Karma.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
		else if (CTF._maxPlayers == CTF._playersShuffle.size()) //check player  karma - dunno why karma players cant participate - looks useless since player can register while not with karma and do pk after that ;)
		{
			npcHtmlMessage.setHtml("<html><body>Sorry,CTF Event is full.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
		else
		//send dialog confirmation to  player that he is registered in ctf event and add him
		{
			npcHtmlMessage.setHtml("<html><body>Your participation in the CTF event has been aproved.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			CTF.addPlayer(activeChar, "");
			return true;
		}
	}
	
	public boolean LeaveCTF(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
		
		if (!CTF._joining) //check if CTF event is running or joining is avaliable
		{
			npcHtmlMessage.setHtml("<html><body>You can not unregister now cause there is no CTF event running.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
		else if (CTF._teleport || CTF._started) //check if ctf event has already teleported and started
		{
			npcHtmlMessage.setHtml("<html><body>You can not leave after CTF event has started.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
		else
		//send dialog confirmation to  player that he is unregistered from ctf event and remove him
		{
			npcHtmlMessage.setHtml("<html><body>Your participation in the CTF event has been removed.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			CTF.removePlayer(activeChar);
			return true;
		}
	}
	
	public boolean SendCTFinfo(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
		
		if (CTF._joining)
		{
			npcHtmlMessage.setHtml("<html><body>There are " + CTF._playersShuffle.size() + " players participating in this event.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			return true;
		}
		else
		{
			npcHtmlMessage.setHtml("<html><body>There is no Event in progress.</body></html>");
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
	}
}