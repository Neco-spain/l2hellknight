package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.LeaveWorld;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class AdminDisconnect
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_character_disconnect" };
  private static final int REQUIRED_LEVEL = Config.GM_KICK;

  public boolean useAdminCommand(String command, L2PcInstance activeChar) {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) return false;

    if (command.equals("admin_character_disconnect"))
    {
      disconnectCharacter(activeChar);
    }

    String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
    GMAudit.auditGMAction(activeChar.getName(), command, target, "");
    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }

  private void disconnectCharacter(L2PcInstance activeChar)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if ((target instanceof L2PcInstance))
      player = (L2PcInstance)target;
    else {
      return;
    }

    if (player.getObjectId() == activeChar.getObjectId())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
      sm.addString("You cannot logout your character.");
      activeChar.sendPacket(sm);
    }
    else
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
      sm.addString("Character " + player.getName() + " disconnected from server.");
      activeChar.sendPacket(sm);

      LeaveWorld ql = new LeaveWorld();
      player.sendPacket(ql);

      RegionBBSManager.getInstance().changeCommunityBoard();

      player.closeNetConnection(false);
    }
  }
}