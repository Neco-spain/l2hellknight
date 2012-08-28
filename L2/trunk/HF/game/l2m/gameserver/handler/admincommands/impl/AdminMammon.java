package l2m.gameserver.handler.admincommands.impl;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.network.serverpackets.SystemMessage;

public class AdminMammon
  implements IAdminCommandHandler
{
  List<Integer> npcIds = new ArrayList();

  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    npcIds.clear();

    if (!activeChar.getPlayerAccess().Menu) {
      return false;
    }
    if (fullString.startsWith("admin_find_mammon"))
    {
      npcIds.add(Integer.valueOf(31113));
      npcIds.add(Integer.valueOf(31126));
      npcIds.add(Integer.valueOf(31092));
      int teleportIndex = -1;
      try
      {
        if (fullString.length() > 16) {
          teleportIndex = Integer.parseInt(fullString.substring(18));
        }

      }
      catch (Exception NumberFormatException)
      {
      }

      findAdminNPCs(activeChar, npcIds, teleportIndex, -1);
    }
    else if (fullString.equals("admin_show_mammon"))
    {
      npcIds.add(Integer.valueOf(31113));
      npcIds.add(Integer.valueOf(31126));

      findAdminNPCs(activeChar, npcIds, -1, 1);
    }
    else if (fullString.equals("admin_hide_mammon"))
    {
      npcIds.add(Integer.valueOf(31113));
      npcIds.add(Integer.valueOf(31126));

      findAdminNPCs(activeChar, npcIds, -1, 0);
    }
    else if (fullString.startsWith("admin_list_spawns"))
    {
      int npcId = 0;
      try
      {
        npcId = Integer.parseInt(fullString.substring(18).trim());
      }
      catch (Exception NumberFormatException)
      {
        activeChar.sendMessage("Command format is //list_spawns <NPC_ID>");
      }

      npcIds.add(Integer.valueOf(npcId));
      findAdminNPCs(activeChar, npcIds, -1, -1);
    }
    else if (fullString.startsWith("admin_msg")) {
      activeChar.sendPacket(new SystemMessage(Integer.parseInt(fullString.substring(10).trim())));
    }
    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  public void findAdminNPCs(Player activeChar, List<Integer> npcIdList, int teleportIndex, int makeVisible)
  {
    int index = 0;

    for (NpcInstance npcInst : GameObjectsStorage.getAllNpcsForIterate())
    {
      int npcId = npcInst.getNpcId();
      if (npcIdList.contains(Integer.valueOf(npcId)))
      {
        if (makeVisible == 1)
          npcInst.spawnMe();
        else if (makeVisible == 0) {
          npcInst.decayMe();
        }
        if (npcInst.isVisible())
        {
          index++;

          if (teleportIndex > -1)
          {
            if (teleportIndex == index)
              activeChar.teleToLocation(npcInst.getLoc());
          }
          else
            activeChar.sendMessage(index + " - " + npcInst.getName() + " (" + npcInst.getObjectId() + "): " + npcInst.getX() + " " + npcInst.getY() + " " + npcInst.getZ());
        }
      }
    }
  }

  private static enum Commands
  {
    admin_find_mammon, 
    admin_show_mammon, 
    admin_hide_mammon, 
    admin_list_spawns;
  }
}