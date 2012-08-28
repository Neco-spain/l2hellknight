package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.templates.L2Item;

public class AdminCreateItem
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_itemcreate", "admin_create_item" };

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
          createItem(activeChar, idval, numval);
        }
        else if (st.countTokens() == 1)
        {
          String id = st.nextToken();
          int idval = Integer.parseInt(id);
          createItem(activeChar, idval, 1);
        }
      }
      catch (StringIndexOutOfBoundsException e)
      {
        activeChar.sendMessage("Usage: //itemcreate <itemId> [amount]");
      }
      catch (NumberFormatException nfe)
      {
        activeChar.sendMessage("Specify a valid number.");
      }
      AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
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

  private void createItem(L2PcInstance activeChar, int id, int num)
  {
    if (num > 20)
    {
      L2Item template = ItemTable.getInstance().getTemplate(id);
      if (!template.isStackable())
      {
        activeChar.sendMessage("This item does not stack - Creation aborted.");
        return;
      }
    }

    activeChar.getInventory().addItem("Admin", id, num, activeChar, null);

    ItemList il = new ItemList(activeChar, true);
    activeChar.sendPacket(il);

    activeChar.sendMessage("You have spawned " + num + " item(s) number " + id + " in your inventory.");
  }
}