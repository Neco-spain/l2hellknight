package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class AdminPetition
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_view_petitions", "admin_view_petition", "admin_accept_petition", "admin_reject_petition", "admin_reset_petitions" };

  private static final int REQUIRED_LEVEL = Config.GM_MIN;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }
    int petitionId = -1;
    try
    {
      petitionId = Integer.parseInt(command.split(" ")[1]);
    }
    catch (Exception e) {
    }
    if (command.equals("admin_view_petitions")) {
      PetitionManager.getInstance().sendPendingPetitionList(activeChar);
    } else if (command.startsWith("admin_view_petition")) {
      PetitionManager.getInstance().viewPetition(activeChar, petitionId);
    } else if (command.startsWith("admin_accept_petition"))
    {
      if (PetitionManager.getInstance().isPlayerInConsultation(activeChar))
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.ONLY_ONE_ACTIVE_PETITION_AT_TIME));
        return true;
      }

      if (PetitionManager.getInstance().isPetitionInProcess(petitionId))
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.PETITION_UNDER_PROCESS));
        return true;
      }

      if (!PetitionManager.getInstance().acceptPetition(activeChar, petitionId))
        activeChar.sendPacket(new SystemMessage(SystemMessageId.NOT_UNDER_PETITION_CONSULTATION));
    }
    else if (command.startsWith("admin_reject_petition"))
    {
      if (!PetitionManager.getInstance().rejectPetition(activeChar, petitionId))
        activeChar.sendPacket(new SystemMessage(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER));
    }
    else if (command.equals("admin_reset_petitions"))
    {
      if (PetitionManager.getInstance().isPetitionInProcess())
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.PETITION_UNDER_PROCESS));
        return false;
      }
      PetitionManager.getInstance().clearPendingPetitions();
    }
    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}