package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.Config;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.serverpackets.components.CustomMessage;

public class AdminGeodata
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanReload) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminGeodata$Commands[command.ordinal()])
    {
    case 1:
      activeChar.sendMessage("GeoEngine: Geo_Z = " + GeoEngine.getHeight(activeChar.getLoc(), activeChar.getReflectionId()) + " Loc_Z = " + activeChar.getZ());
      break;
    case 2:
      int type = GeoEngine.getType(activeChar.getX(), activeChar.getY(), activeChar.getReflectionId());
      activeChar.sendMessage("GeoEngine: Geo_Type = " + type);
      break;
    case 3:
      String result = "";
      byte nswe = GeoEngine.getNSWE(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getReflectionId());
      if ((nswe & 0x8) == 0)
        result = result + " N";
      if ((nswe & 0x4) == 0)
        result = result + " S";
      if ((nswe & 0x2) == 0)
        result = result + " W";
      if ((nswe & 0x1) == 0)
        result = result + " E";
      activeChar.sendMessage("GeoEngine: Geo_NSWE -> " + nswe + "->" + result);
      break;
    case 4:
      if (activeChar.getTarget() != null) {
        if (GeoEngine.canSeeTarget(activeChar, activeChar.getTarget(), false))
          activeChar.sendMessage("GeoEngine: Can See Target");
        else
          activeChar.sendMessage("GeoEngine: Can't See Target");
      }
      else activeChar.sendMessage("None Target!");
      break;
    case 5:
      if (wordList.length != 3)
        activeChar.sendMessage("Usage: //geo_load <regionX> <regionY>");
      else {
        try
        {
          byte rx = Byte.parseByte(wordList[1]);
          byte ry = Byte.parseByte(wordList[2]);
          if (GeoEngine.LoadGeodataFile(rx, ry))
          {
            GeoEngine.LoadGeodata(rx, ry, 0);
            activeChar.sendMessage("GeoEngine: Region [" + rx + "," + ry + "] loaded.");
          }
          else {
            activeChar.sendMessage("GeoEngine: Region [" + rx + "," + ry + "] not loaded.");
          }
        }
        catch (Exception e) {
          activeChar.sendMessage(new CustomMessage("common.Error", activeChar, new Object[0]));
        }
      }
    case 6:
      if (wordList.length > 2)
      {
        GeoEngine.DumpGeodataFileMap(Byte.parseByte(wordList[1]), Byte.parseByte(wordList[2]));
        activeChar.sendMessage("Geo square saved " + wordList[1] + "_" + wordList[2]);
      }
      GeoEngine.DumpGeodataFile(activeChar.getX(), activeChar.getY());
      activeChar.sendMessage("Actual geo square saved.");
      break;
    case 7:
      if (wordList.length < 2)
      {
        activeChar.sendMessage("Usage: //geo_trace on|off");
        return false;
      }
      if (wordList[1].equalsIgnoreCase("on"))
        activeChar.setVar("trace", "1", -1L);
      else if (wordList[1].equalsIgnoreCase("off"))
        activeChar.unsetVar("trace");
      else
        activeChar.sendMessage("Usage: //geo_trace on|off");
      break;
    case 8:
      int x = (activeChar.getX() - World.MAP_MIN_X >> 15) + Config.GEO_X_FIRST;
      int y = (activeChar.getY() - World.MAP_MIN_Y >> 15) + Config.GEO_Y_FIRST;

      activeChar.sendMessage("GeoMap: " + x + "_" + y);
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_geo_z, 
    admin_geo_type, 
    admin_geo_nswe, 
    admin_geo_los, 
    admin_geo_load, 
    admin_geo_dump, 
    admin_geo_trace, 
    admin_geo_map;
  }
}