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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - delete = deletes target
 *
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/04/11 10:05:56 $
 */
public class AdminDelete implements IAdminCommandHandler
{
    //private static Logger _log = Logger.getLogger(AdminDelete.class.getName());

    private static final String[] ADMIN_COMMANDS = {"admin_delete"};

    private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        }

        if (command.equals("admin_delete")) handleDelete(activeChar);
        String target = (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target");
        GMAudit.auditGMAction(activeChar.getName(), command, target, "");
        return true;
    }

    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }

    private boolean checkLevel(int level)
    {
        return (level >= REQUIRED_LEVEL);
    }

    // TODO: add possibility to delete any L2Object (except L2PcInstance)
    private void handleDelete(L2PcInstance activeChar)
    {
        L2Object obj = activeChar.getTarget();
        if ((obj != null) && (obj instanceof L2NpcInstance))
        {
            L2NpcInstance target = (L2NpcInstance) obj;
            target.deleteMe();

            L2Spawn spawn = target.getSpawn();
            if (spawn != null)
            {
                spawn.stopRespawn();

                if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcid())) RaidBossSpawnManager.getInstance().deleteSpawn(
                                                                                                                                   spawn,
                                                                                                                                   true);
                else SpawnTable.getInstance().deleteSpawn(spawn, true);
            }

            SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
            sm.addString("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
            activeChar.sendPacket(sm);
        }
        else
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
            sm.addString("Incorrect target.");
            activeChar.sendPacket(sm);
        }
    }
}
