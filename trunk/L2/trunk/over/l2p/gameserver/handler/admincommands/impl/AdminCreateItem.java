package l2p.gameserver.handler.admincommands.impl;

import l2p.commons.dao.JdbcEntityState;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;

public class AdminCreateItem
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().UseGMShop) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminCreateItem$Commands[command.ordinal()])
    {
    case 1:
      activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
      break;
    case 2:
    case 3:
      try
      {
        if (wordList.length < 2)
        {
          activeChar.sendMessage("USAGE: create_item id [count]");
          return false;
        }

        int item_id = Integer.parseInt(wordList[1]);
        long item_count = wordList.length < 3 ? 1L : Long.parseLong(wordList[2]);
        createItem(activeChar, item_id, item_count);
      }
      catch (NumberFormatException nfe)
      {
        activeChar.sendMessage("USAGE: create_item id [count]");
      }
      activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
      break;
    case 4:
      try
      {
        int id = Integer.parseInt(wordList[1]);
        int num = wordList.length > 2 ? Integer.parseInt(wordList[2]) : 1;
        long count = wordList.length > 3 ? Long.parseLong(wordList[3]) : 1L;
        for (int i = 0; i < num; i++)
        {
          ItemInstance createditem = ItemFunctions.createItem(id);
          createditem.setCount(count);
          createditem.dropMe(activeChar, Location.findPointToStay(activeChar, 100));
        }
      }
      catch (NumberFormatException nfe)
      {
        activeChar.sendMessage("Specify a valid number.");
      }
      catch (StringIndexOutOfBoundsException e)
      {
        activeChar.sendMessage("Can't create this item.");
      }

    case 5:
      try
      {
        if (wordList.length < 4)
        {
          activeChar.sendMessage("USAGE: create_item_attribue [id] [element id] [value]");
          return false;
        }

        int item_id = Integer.parseInt(wordList[1]);
        int elementId = Integer.parseInt(wordList[2]);
        int value = Integer.parseInt(wordList[3]);
        if ((elementId > 5) || (elementId < 0))
        {
          activeChar.sendMessage("Improper element Id");
          return false;
        }
        if ((value < 1) || (value > 300))
        {
          activeChar.sendMessage("Improper element value");
          return false;
        }

        ItemInstance item = createItem(activeChar, item_id, 1L);
        Element element = Element.getElementById(elementId);
        item.setAttributeElement(element, item.getAttributeElementValue(element, false) + value);
        item.setJdbcState(JdbcEntityState.UPDATED);
        item.update();
        activeChar.sendPacket(new InventoryUpdate().addModifiedItem(item));
      }
      catch (NumberFormatException nfe)
      {
        activeChar.sendMessage("USAGE: create_item id [count]");
      }
      activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/itemcreation.htm"));
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private ItemInstance createItem(Player activeChar, int itemId, long count)
  {
    ItemInstance createditem = ItemFunctions.createItem(itemId);
    createditem.setCount(count);
    Log.LogItem(activeChar, "Create", createditem);
    activeChar.getInventory().addItem(createditem);
    if (!createditem.isStackable())
      for (long i = 0L; i < count - 1L; i += 1L)
      {
        createditem = ItemFunctions.createItem(itemId);
        Log.LogItem(activeChar, "Create", createditem);
        activeChar.getInventory().addItem(createditem);
      }
    activeChar.sendPacket(SystemMessage2.obtainItems(itemId, count, 0));
    return createditem;
  }

  private static enum Commands
  {
    admin_itemcreate, 
    admin_create_item, 
    admin_ci, 
    admin_spreaditem, 
    admin_create_item_element;
  }
}