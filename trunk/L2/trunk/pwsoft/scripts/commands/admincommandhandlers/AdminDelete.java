package scripts.commands.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IAdminCommandHandler;

public class AdminDelete
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_delete" };

  private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if (!Config.ALT_PRIVILEGES_ADMIN)
    {
      if ((!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM())) return false;
    }

    if (command.equals("admin_delete")) handleDelete(activeChar);
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

  private void handleDelete(L2PcInstance activeChar)
  {
    L2Object obj = activeChar.getTarget();
    if ((obj != null) && (obj.isL2Npc()))
    {
      L2NpcInstance target = (L2NpcInstance)obj;
      target.deleteMe();

      L2Spawn spawn = target.getSpawn();
      if (spawn != null)
      {
        spawn.stopRespawn();

        if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcid())) RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
        else
        {
          SpawnTable.getInstance().deleteSpawn(spawn, true);
        }
      }
      SystemMessage sm = SystemMessage.id(SystemMessageId.S1_S2);
      sm.addString("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
      activeChar.sendPacket(sm);
    }
    else
    {
      SystemMessage sm = SystemMessage.id(SystemMessageId.S1_S2);
      sm.addString("Incorrect target.");
      activeChar.sendPacket(sm);
    }
  }
}