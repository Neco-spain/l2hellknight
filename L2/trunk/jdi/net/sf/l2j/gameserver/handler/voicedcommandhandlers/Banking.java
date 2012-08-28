package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;

public class Banking
  implements IVoicedCommandHandler
{
  private static final String[] _voicedCommands = { "bank", "withdraw", "deposit" };

  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    if (command.equalsIgnoreCase("bank"))
    {
      activeChar.sendMessage(".deposit (" + Config.BANKING_SYSTEM_1ITEMCOUNT + " " + Config.BANKING_SYSTEM_1ITEMNAME + " = " + Config.BANKING_SYSTEM_2ITEMCOUNT + " " + Config.BANKING_SYSTEM_2ITEMNAME + ") / .withdraw (" + Config.BANKING_SYSTEM_2ITEMCOUNT + " " + Config.BANKING_SYSTEM_2ITEMNAME + " = " + Config.BANKING_SYSTEM_1ITEMCOUNT + " " + Config.BANKING_SYSTEM_1ITEMNAME + ")");
    }
    else if (command.equalsIgnoreCase("deposit"))
    {
      if (activeChar.getInventory().getInventoryItemCount(Config.BANKING_SYSTEM_1ITEMID, 0) >= Config.BANKING_SYSTEM_1ITEMCOUNT)
      {
        InventoryUpdate iu = new InventoryUpdate();
        activeChar.getInventory().reduceAdena("Goldbar", Config.BANKING_SYSTEM_1ITEMCOUNT, activeChar, null);
        activeChar.getInventory().addItem("Goldbar", Config.BANKING_SYSTEM_2ITEMID, Config.BANKING_SYSTEM_2ITEMCOUNT, activeChar, null);
        activeChar.getInventory().updateDatabase();
        activeChar.sendPacket(iu);
        activeChar.sendMessage("Thank you, you now have " + Config.BANKING_SYSTEM_2ITEMCOUNT + " " + Config.BANKING_SYSTEM_2ITEMNAME + "(s), and " + Config.BANKING_SYSTEM_1ITEMCOUNT + " less " + Config.BANKING_SYSTEM_1ITEMNAME + ".");
      }
      else
      {
        activeChar.sendMessage("You do not have enough " + Config.BANKING_SYSTEM_1ITEMNAME + " to convert to " + Config.BANKING_SYSTEM_2ITEMNAME + "(s), you need " + Config.BANKING_SYSTEM_1ITEMCOUNT + " " + Config.BANKING_SYSTEM_1ITEMNAME + ".");
      }
    }
    else if (command.equalsIgnoreCase("withdraw"))
    {
      if (activeChar.getInventory().getInventoryItemCount(Config.BANKING_SYSTEM_2ITEMID, 0) >= Config.BANKING_SYSTEM_2ITEMCOUNT)
      {
        InventoryUpdate iu = new InventoryUpdate();
        activeChar.getInventory().destroyItemByItemId("Adena", Config.BANKING_SYSTEM_2ITEMID, Config.BANKING_SYSTEM_2ITEMCOUNT, activeChar, null);
        activeChar.getInventory().addAdena("Adena", Config.BANKING_SYSTEM_1ITEMCOUNT, activeChar, null);
        activeChar.getInventory().updateDatabase();
        activeChar.sendPacket(iu);
        activeChar.sendMessage("Thank you, you now have " + Config.BANKING_SYSTEM_1ITEMCOUNT + " " + Config.BANKING_SYSTEM_1ITEMNAME + ", and " + Config.BANKING_SYSTEM_2ITEMCOUNT + " less " + Config.BANKING_SYSTEM_2ITEMNAME + "(s).");
      }
      else
      {
        activeChar.sendMessage("You do not have any " + Config.BANKING_SYSTEM_2ITEMNAME + " to turn into " + Config.BANKING_SYSTEM_1ITEMCOUNT + " " + Config.BANKING_SYSTEM_1ITEMNAME + ".");
      }
    }
    return true;
  }

  public String[] getVoicedCommandList()
  {
    return _voicedCommands;
  }
}