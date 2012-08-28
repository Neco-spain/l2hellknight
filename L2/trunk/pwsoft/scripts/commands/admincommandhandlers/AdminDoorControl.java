package scripts.commands.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import scripts.commands.IAdminCommandHandler;

public class AdminDoorControl
  implements IAdminCommandHandler
{
  private static final int REQUIRED_LEVEL = Config.GM_DOOR;
  private static DoorTable _doorTable;
  private static final String[] ADMIN_COMMANDS = { "admin_open", "admin_close", "admin_openall", "admin_closeall" };

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) return false;

    _doorTable = DoorTable.getInstance();
    try
    {
      int doorId;
      int doorId;
      if (command.startsWith("admin_open "))
      {
        doorId = Integer.parseInt(command.substring(11));
        if (_doorTable.getDoor(Integer.valueOf(doorId)) != null)
          _doorTable.getDoor(Integer.valueOf(doorId)).openMe();
        else {
          for (Castle castle : CastleManager.getInstance().getCastles()) {
            if (castle.getDoor(doorId) != null)
              castle.getDoor(doorId).openMe();
          }
        }
      }
      else if (command.startsWith("admin_close "))
      {
        doorId = Integer.parseInt(command.substring(12));
        if (_doorTable.getDoor(Integer.valueOf(doorId)) != null)
          _doorTable.getDoor(Integer.valueOf(doorId)).closeMe();
        else {
          for (Castle castle : CastleManager.getInstance().getCastles()) {
            if (castle.getDoor(doorId) != null)
              castle.getDoor(doorId).closeMe();
          }
        }
      }
      if (command.equals("admin_closeall"))
      {
        for (L2DoorInstance door : _doorTable.getDoors())
          door.closeMe();
        for (Castle castle : CastleManager.getInstance().getCastles())
          for (L2DoorInstance door : castle.getDoors())
            door.closeMe();
      }
      if (command.equals("admin_openall"))
      {
        for (L2DoorInstance door : _doorTable.getDoors())
          door.openMe();
        for (Castle castle : CastleManager.getInstance().getCastles())
          for (L2DoorInstance door : castle.getDoors())
            door.openMe();
      }
      if (command.equals("admin_open"))
      {
        L2Object target = activeChar.getTarget();
        if (target.isL2Door())
        {
          ((L2DoorInstance)target).openMe();
        }
        else
        {
          activeChar.sendAdmResultMessage("Incorrect target.");
        }
      }

      if (command.equals("admin_close"))
      {
        L2Object target = activeChar.getTarget();
        if (target.isL2Door())
        {
          ((L2DoorInstance)target).closeMe();
        }
        else
        {
          activeChar.sendAdmResultMessage("Incorrect target.");
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
    GMAudit.auditGMAction(activeChar.getName(), command, target, "");
    return true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level)
  {
    return level >= REQUIRED_LEVEL;
  }
}