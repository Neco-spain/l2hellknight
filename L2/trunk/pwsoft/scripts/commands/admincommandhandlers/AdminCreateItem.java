package scripts.commands.admincommandhandlers;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;
import scripts.commands.IAdminCommandHandler;

public class AdminCreateItem
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_itemcreate", "admin_create_item", "admin_giveitem" };

  private static final int REQUIRED_LEVEL = Config.GM_CREATE_ITEM;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if (!Config.ALT_PRIVILEGES_ADMIN)
    {
      if ((!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM())) {
        return false;
      }
    }
    GMAudit.auditGMAction(activeChar.getName(), command, activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target", "");

    if (command.equals("admin_itemcreate"))
    {
      AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
    }
    else if (command.startsWith("admin_create_item"))
    {
      try
      {
        String val = command.substring(17);
        StringTokenizer st = new StringTokenizer(val);
        if (st.countTokens() == 2)
        {
          String id = st.nextToken();
          int idval = Integer.parseInt(id);
          String num = st.nextToken();
          int numval = Integer.parseInt(num);
          giveItem(activeChar, null, idval, numval);
        }
        else if (st.countTokens() == 1)
        {
          String id = st.nextToken();
          int idval = Integer.parseInt(id);
          giveItem(activeChar, null, idval, 1);
        }
      }
      catch (StringIndexOutOfBoundsException e)
      {
        activeChar.sendAdmResultMessage("Usage: //itemcreate <itemId> [amount]");
      }
      catch (NumberFormatException nfe)
      {
        activeChar.sendAdmResultMessage("Specify a valid number.");
      }
      AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
    }
    else if (command.startsWith("admin_giveitem"))
    {
      try
      {
        L2Object obj = activeChar.getTarget();
        if ((obj == null) || (!obj.isPlayer()))
        {
          activeChar.sendAdmResultMessage("\u041D\u0435 \u0442\u043E\u0433\u043E \u0432\u0437\u044F\u043B\u0438 \u0432 \u0442\u0430\u0440\u0433\u0435\u0442.");
          return false;
        }

        L2PcInstance rewdr = (L2PcInstance)obj;

        String val = command.substring(15);
        StringTokenizer st = new StringTokenizer(val);
        if (st.countTokens() == 2)
        {
          String id = st.nextToken();
          int idval = Integer.parseInt(id);
          String num = st.nextToken();
          int numval = Integer.parseInt(num);
          giveItem(activeChar, rewdr, idval, numval);
        }
        else if (st.countTokens() == 1)
        {
          String id = st.nextToken();
          int idval = Integer.parseInt(id);
          giveItem(activeChar, rewdr, idval, 1);
        }
      }
      catch (StringIndexOutOfBoundsException e)
      {
        activeChar.sendAdmResultMessage("Usage: \u0412\u0437\u044F\u0442\u044C \u043D\u0443\u0436\u043D\u043E\u0433\u043E \u0438\u0433\u0440\u043E\u043A\u0430 \u0432 \u0442\u0430\u0440\u0433\u0435\u0442 \u0438 \u043D\u0430\u0431\u0440\u0430\u0442\u044C //giveitem itemId count");
      }
      catch (NumberFormatException nfe)
      {
        activeChar.sendAdmResultMessage("Specify a valid number.");
      }
    }
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

  private void giveItem(L2PcInstance admin, L2PcInstance rewdr, int itemId, int count)
  {
    L2Item template = ItemTable.getInstance().getTemplate(itemId);
    if (template == null)
    {
      admin.sendAdmResultMessage("\u041E\u0448\u0438\u0431\u043A\u0430: \u0438\u0442\u0435\u043C " + itemId + " \u043D\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442.");
      return;
    }
    if ((count > 1) && (!template.isStackable()))
      count = 1;
    if (rewdr != null)
    {
      rewdr.addItem("Admin", itemId, count, admin, true);
      admin.sendAdmResultMessage("\u0412\u044B\u0434\u0430\u043B\u0438 " + template.getName() + "(" + count + ") \u0438\u0433\u0440\u043E\u043A\u0443 " + rewdr.getName() + ".");
    }
    else
    {
      admin.addItem("Admin", itemId, count, admin, true);
      admin.sendAdmResultMessage("\u041F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 " + template.getName() + "(" + count + ").");
    }
  }
}