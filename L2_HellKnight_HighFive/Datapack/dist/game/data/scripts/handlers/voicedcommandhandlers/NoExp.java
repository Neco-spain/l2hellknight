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

import l2.hellknight.gameserver.handler.IVoicedCommandHandler;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class allows user to turn XP-gain off and on.
 *
 * @author Notorious
 */
public class NoExp implements IVoicedCommandHandler
{
   private static final String[] _voicedCommands =
   {
      "xpoff",
      "xpon"
   };
   
   /**
    * 
    * @see l2.hellknight.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, l2.hellknight.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
    */
   @Override
public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
   {
      if (command.equalsIgnoreCase("xpoff"))
      {
         activeChar.cantGainXP(true);
         activeChar.sendMessage("You have turned XP-gain OFF!");
      }
      else if (command.equalsIgnoreCase("xpon"))
      {
         activeChar.cantGainXP(false);
         activeChar.sendMessage("You have turned XP-gain ON!");
      }
      return true;
   }
   
   /**
    * 
    * @see l2.hellknight.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
    */
   @Override
public String[] getVoicedCommandList()
   {
      return _voicedCommands;
   }
}