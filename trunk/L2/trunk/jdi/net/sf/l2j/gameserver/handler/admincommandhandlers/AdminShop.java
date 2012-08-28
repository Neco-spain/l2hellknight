package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.BuyList;

public class AdminShop
  implements IAdminCommandHandler
{
  private static Logger _log = Logger.getLogger(AdminShop.class.getName());

  private static final String[] ADMIN_COMMANDS = { "admin_buy", "admin_gmshop" };

  private static final int REQUIRED_LEVEL = Config.GM_CREATE_ITEM;

  public boolean useAdminCommand(String command, L2PcInstance activeChar) {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }
    if (command.startsWith("admin_buy"))
    {
      try
      {
        handleBuyRequest(activeChar, command.substring(10));
      }
      catch (IndexOutOfBoundsException e)
      {
        activeChar.sendMessage("Please specify buylist.");
      }
    }
    else if (command.equals("admin_gmshop"))
    {
      AdminHelpPage.showHelpPage(activeChar, "gmshops.htm");
    }
    String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
    GMAudit.auditGMAction(activeChar.getName(), command, target, "");
    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }

  private void handleBuyRequest(L2PcInstance activeChar, String command)
  {
    int val = -1;
    try
    {
      val = Integer.parseInt(command);
    }
    catch (Exception e)
    {
      _log.warning("admin buylist failed:" + command);
    }

    L2TradeList list = TradeController.getInstance().getBuyList(val);

    if (list != null)
    {
      activeChar.sendPacket(new BuyList(list, activeChar.getAdena()));
      if (Config.DEBUG)
        _log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") opened GM shop id " + val);
    }
    else
    {
      _log.warning("no buylist with id:" + val);
    }
    activeChar.sendPacket(new ActionFailed());
  }
}