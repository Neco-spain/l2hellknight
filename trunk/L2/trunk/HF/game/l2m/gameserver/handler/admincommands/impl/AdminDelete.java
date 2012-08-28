package l2m.gameserver.handler.admincommands.impl;

import java.io.File;
import l2m.gameserver.aConfig;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Spawner;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.Location;
import org.apache.commons.lang3.math.NumberUtils;

public class AdminDelete
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanEditNPC) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminDelete$Commands[command.ordinal()])
    {
    case 1:
      GameObject obj = wordList.length == 1 ? activeChar.getTarget() : GameObjectsStorage.getNpc(NumberUtils.toInt(wordList[1]));
      if ((obj != null) && (obj.isNpc()))
      {
        NpcInstance target = (NpcInstance)obj;
        target.deleteMe();

        Spawner spawn = target.getSpawn();
        if (spawn != null) {
          spawn.stopRespawn();
        }
        if (aConfig.get("RemoveSpawnMonster", false))
          deleteSpawn(target);
      }
      else {
        activeChar.sendPacket(Msg.INVALID_TARGET);
      }
    }

    return true;
  }

  private void deleteSpawn(NpcInstance npc)
  {
    String dir = aConfig.get("DatapackRoot", ".") + "/data/spawn/custom";
    File _content = new File(dir);
    try
    {
      for (File _file : _content.listFiles())
        if ((!_file.isDirectory()) && (_file.getName().endsWith(npc.getTemplate().getNpcId() + "_" + npc.getSpawnedLoc().getX() + "_" + npc.getSpawnedLoc().getY() + "_" + npc.getSpawnedLoc().getZ() + ".xml")))
          _file.delete();
    }
    catch (Exception e)
    {
    }
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_delete;
  }
}