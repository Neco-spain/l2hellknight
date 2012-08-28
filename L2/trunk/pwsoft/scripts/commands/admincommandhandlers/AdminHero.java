package scripts.commands.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import scripts.commands.IAdminCommandHandler;

public class AdminHero
  implements IAdminCommandHandler
{
  private static String[] _adminCommands = { "admin_sethero" };

  private static final Log _log = LogFactory.getLog(AdminHero.class.getName());
  private static final int REQUIRED_LEVEL = Config.GM_MENU;

  public boolean useAdminCommand(String command, L2PcInstance admin)
  {
    if (!Config.ALT_PRIVILEGES_ADMIN)
    {
      if ((!checkLevel(admin.getAccessLevel())) || (!admin.isGM()))
      {
        return false;
      }
    }
    if (command.startsWith("admin_sethero"))
    {
      L2Object obj = admin.getTarget();
      if (!obj.isPlayer()) {
        return false;
      }
      L2PcInstance target = (L2PcInstance)obj;
      if (target.isHero())
      {
        target.setHero(false);
        target.setHeroExpire(0L);
        target.broadcastUserInfo();
        admin.sendAdmResultMessage("\u0417\u0430\u0431\u0440\u0430\u043B\u0438 \u0441\u0442\u0443\u0442\u0430\u0441 \u0433\u0435\u0440\u043E\u044F \u0443 \u0438\u0433\u0440\u043E\u043A\u0430 " + target.getName());
      }
      else
      {
        int days = 30;
        try
        {
          days = Integer.parseInt(command.substring(14).trim());
        }
        catch (Exception e)
        {
          days = 30;
        }
        admin.sendAdmResultMessage("\u0412\u044B\u0434\u0430\u043D \u0441\u0442\u0443\u0442\u0430\u0441 \u0433\u0435\u0440\u043E\u044F \u0438\u0433\u0440\u043E\u043A\u0443 " + target.getName() + " \u043D\u0430 " + days + " \u0434\u043D\u0435\u0439.");
        target.setHero(days);
      }
    }
    return false;
  }

  public String[] getAdminCommandList()
  {
    return _adminCommands;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}