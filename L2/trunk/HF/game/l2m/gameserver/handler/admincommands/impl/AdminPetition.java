package l2m.gameserver.handler.admincommands.impl;

import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.instancemanager.PetitionManager;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.network.serverpackets.SystemMessage;
import org.apache.commons.lang3.math.NumberUtils;

public class AdminPetition
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    if (!activeChar.getPlayerAccess().CanEditChar) {
      return false;
    }
    int petitionId = NumberUtils.toInt(wordList.length > 1 ? wordList[1] : "-1", -1);
    Commands command = (Commands)comm;
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminPetition$Commands[command.ordinal()])
    {
    case 1:
      PetitionManager.getInstance().sendPendingPetitionList(activeChar);
      break;
    case 2:
      PetitionManager.getInstance().viewPetition(activeChar, petitionId);
      break;
    case 3:
      if (petitionId < 0)
      {
        activeChar.sendMessage("Usage: //accept_petition id");
        return false;
      }
      if (PetitionManager.getInstance().isPlayerInConsultation(activeChar))
      {
        activeChar.sendPacket(new SystemMessage(390));
        return true;
      }

      if (PetitionManager.getInstance().isPetitionInProcess(petitionId))
      {
        activeChar.sendPacket(new SystemMessage(407));
        return true;
      }

      if (PetitionManager.getInstance().acceptPetition(activeChar, petitionId)) break;
      activeChar.sendPacket(new SystemMessage(388)); break;
    case 4:
      if (petitionId < 0)
      {
        activeChar.sendMessage("Usage: //accept_petition id");
        return false;
      }
      if (!PetitionManager.getInstance().rejectPetition(activeChar, petitionId))
        activeChar.sendPacket(new SystemMessage(393));
      PetitionManager.getInstance().sendPendingPetitionList(activeChar);

      break;
    case 5:
      if (PetitionManager.getInstance().isPetitionInProcess())
      {
        activeChar.sendPacket(new SystemMessage(407));
        return false;
      }
      PetitionManager.getInstance().clearPendingPetitions();
      PetitionManager.getInstance().sendPendingPetitionList(activeChar);
      break;
    case 6:
      if (fullString.length() < 11)
      {
        activeChar.sendMessage("Usage: //force_peti text");
        return false;
      }
      try
      {
        GameObject targetChar = activeChar.getTarget();
        if ((targetChar == null) || (!(targetChar instanceof Player)))
        {
          activeChar.sendPacket(new SystemMessage(109));
          return false;
        }
        Player targetPlayer = (Player)targetChar;

        petitionId = PetitionManager.getInstance().submitPetition(targetPlayer, fullString.substring(10), 9);
        PetitionManager.getInstance().acceptPetition(activeChar, petitionId);
      }
      catch (StringIndexOutOfBoundsException e)
      {
        activeChar.sendMessage("Usage: //force_peti text");
        return false;
      }
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_view_petitions, 
    admin_view_petition, 
    admin_accept_petition, 
    admin_reject_petition, 
    admin_reset_petitions, 
    admin_force_peti;
  }
}