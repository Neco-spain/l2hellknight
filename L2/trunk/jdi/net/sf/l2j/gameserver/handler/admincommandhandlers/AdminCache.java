package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.io.File;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class AdminCache
  implements IAdminCommandHandler
{
  private static final int REQUIRED_LEVEL = Config.GM_CACHE;
  private static final String[] ADMIN_COMMANDS = { "admin_cache_htm_rebuild", "admin_cache_htm_reload", "admin_cache_reload_path", "admin_cache_reload_file", "admin_cache_crest_rebuild", "admin_cache_crest_reload", "admin_cache_crest_fix" };

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) return false;

    if ((command.startsWith("admin_cache_htm_rebuild")) || (command.equals("admin_cache_htm_reload")))
    {
      HtmCache.getInstance().reload(Config.DATAPACK_ROOT);
      activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " MB on " + HtmCache.getInstance().getLoadedFiles() + " file(s) loaded.");
    }
    else if (command.startsWith("admin_cache_reload_path "))
    {
      try
      {
        String path = command.split(" ")[1];
        HtmCache.getInstance().reloadPath(new File(Config.DATAPACK_ROOT, path));
        activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " MB in " + HtmCache.getInstance().getLoadedFiles() + " file(s) loaded.");
      }
      catch (Exception e)
      {
        activeChar.sendMessage("Usage: //cache_reload_path <path>");
      }
    }
    else if (command.startsWith("admin_cache_reload_file "))
    {
      try
      {
        String path = command.split(" ")[1];
        if (HtmCache.getInstance().loadFile(new File(Config.DATAPACK_ROOT, path)) != null)
        {
          activeChar.sendMessage("Cache[HTML]: file was loaded");
        }
        else
        {
          activeChar.sendMessage("Cache[HTML]: file can't be loaded");
        }
      }
      catch (Exception e)
      {
        activeChar.sendMessage("Usage: //cache_reload_file <relative_path/file>");
      }
    }
    else if ((command.startsWith("admin_cache_crest_rebuild")) || (command.startsWith("admin_cache_crest_reload")))
    {
      CrestCache.getInstance().reload();
      activeChar.sendMessage("Cache[Crest]: " + String.format("%.3f", new Object[] { Float.valueOf(CrestCache.getInstance().getMemoryUsage()) }) + " megabytes on " + CrestCache.getInstance().getLoadedFiles() + " files loaded");
    }
    else if (command.startsWith("admin_cache_crest_fix"))
    {
      CrestCache.getInstance().convertOldPedgeFiles();
      activeChar.sendMessage("Cache[Crest]: crests fixed");
    }
    String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
    GMAudit.auditGMAction(activeChar.getName(), command, target, "");
    return true;
  }

  private boolean checkLevel(int level)
  {
    return level >= REQUIRED_LEVEL;
  }
}