package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.data.xml.holder.BuyListHolder;
import l2p.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.serverpackets.ExBuySellList.BuyList;
import l2p.gameserver.serverpackets.ExBuySellList.SellRefundList;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.utils.GameStats;

public class AdminShop
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().UseGMShop) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminShop$Commands[command.ordinal()])
    {
    case 1:
      try
      {
        handleBuyRequest(activeChar, fullString.substring(10));
      }
      catch (IndexOutOfBoundsException e)
      {
        activeChar.sendMessage("Please specify buylist.");
      }

    case 2:
      activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/gmshops.htm"));
      break;
    case 3:
      activeChar.sendMessage("TaxSum: " + GameStats.getTaxSum());
      break;
    case 4:
      GameStats.addTax(-GameStats.getTaxSum());
      activeChar.sendMessage("TaxSum: " + GameStats.getTaxSum());
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private void handleBuyRequest(Player activeChar, String command)
  {
    int val = -1;
    try
    {
      val = Integer.parseInt(command);
    }
    catch (Exception e)
    {
    }

    BuyListHolder.NpcTradeList list = BuyListHolder.getInstance().getBuyList(val);

    if (list != null) {
      activeChar.sendPacket(new IStaticPacket[] { new ExBuySellList.BuyList(list, activeChar, 0.0D), new ExBuySellList.SellRefundList(activeChar, false) });
    }
    activeChar.sendActionFailed();
  }

  private static enum Commands
  {
    admin_buy, 
    admin_gmshop, 
    admin_tax, 
    admin_taxclear;
  }
}