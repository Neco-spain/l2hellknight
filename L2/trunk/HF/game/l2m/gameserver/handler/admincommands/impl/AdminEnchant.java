package l2m.gameserver.handler.admincommands.impl;

import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.Element;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.serverpackets.InventoryUpdate;

public class AdminEnchant
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanEditChar) {
      return false;
    }
    int armorType = -1;

    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminEnchant$Commands[command.ordinal()])
    {
    case 1:
    case 2:
      armorType = 6;
      break;
    case 3:
    case 4:
      armorType = 10;
      break;
    case 5:
    case 6:
      armorType = 9;
      break;
    case 7:
    case 8:
      armorType = 12;
      break;
    case 9:
    case 10:
      armorType = 11;
      break;
    case 11:
    case 12:
      armorType = 7;
      break;
    case 13:
      armorType = 8;
      break;
    case 14:
      armorType = 2;
      break;
    case 15:
      armorType = 1;
      break;
    case 16:
      armorType = 5;
      break;
    case 17:
      armorType = 4;
      break;
    case 18:
      armorType = 3;
      break;
    case 19:
      armorType = 0;
      break;
    case 20:
      armorType = 13;
      break;
    case 21:
      armorType = 15;
      break;
    case 22:
      armorType = 15;
      break;
    case 23:
      armorType = 18;
      break;
    case 24:
      armorType = 17;
      break;
    case 25:
      armorType = 25;
      break;
    case 26:
    }

    if ((armorType == -1) || (wordList.length < 2))
    {
      return true;
    }

    try
    {
      int ench = Integer.parseInt(wordList[1]);
      if ((ench < 0) || (ench > 65535))
        activeChar.sendMessage("You must set the enchant level to be between 0-65535.");
      else if (wordList.length == 2)
        setEnchant(activeChar, ench, armorType);
      else if (wordList.length == 3)
        setEnchantAttribute(activeChar, ench, wordList[2], armorType);
    }
    catch (StringIndexOutOfBoundsException e)
    {
      activeChar.sendMessage("Please specify a new enchant value.");
    }
    catch (NumberFormatException e)
    {
      activeChar.sendMessage("Please specify a valid new enchant value.");
    }

    return true;
  }

  private void setEnchant(Player activeChar, int ench, int armorType)
  {
    GameObject target = activeChar.getTarget();
    if (target == null)
      target = activeChar;
    if (!target.isPlayer())
    {
      activeChar.sendMessage("Wrong target type.");
      return;
    }

    Player player = (Player)target;

    int curEnchant = 0;

    ItemInstance itemInstance = player.getInventory().getPaperdollItem(armorType);

    if (itemInstance != null)
    {
      curEnchant = itemInstance.getEnchantLevel();

      player.getInventory().unEquipItem(itemInstance);
      itemInstance.setEnchantLevel(ench);
      player.getInventory().equipItem(itemInstance);

      player.sendPacket(new InventoryUpdate().addModifiedItem(itemInstance));
      player.broadcastCharInfo();

      activeChar.sendMessage("Changed enchantment of " + player.getName() + "'s " + itemInstance.getName() + " from " + curEnchant + " to " + ench + ".");
      player.sendMessage("Admin has changed the enchantment of your " + itemInstance.getName() + " from " + curEnchant + " to " + ench + ".");
    }
  }

  private void setEnchantAttribute(Player activeChar, int ench, String attribute, int armorType)
  {
    GameObject target = activeChar.getTarget();
    if (target == null)
      target = activeChar;
    if (!target.isPlayer())
    {
      activeChar.sendMessage("Wrong target type.");
      return;
    }
    Player player = (Player)target;
    ItemInstance itemInstance = player.getInventory().getPaperdollItem(armorType);

    if (itemInstance == null) {
      return;
    }
    Element _element = null;
    if (attribute.equalsIgnoreCase("Fire"))
      _element = Element.FIRE;
    else if (attribute.equalsIgnoreCase("Water"))
      _element = Element.WATER;
    else if (attribute.equalsIgnoreCase("Wind"))
      _element = Element.WIND;
    else if (attribute.equalsIgnoreCase("Earth"))
      _element = Element.EARTH;
    else if (attribute.equalsIgnoreCase("Holy"))
      _element = Element.HOLY;
    else if (attribute.equalsIgnoreCase("Dark")) {
      _element = Element.UNHOLY;
    }
    if (_element == null)
    {
      activeChar.sendMessage("Choose attribute element.");
      return;
    }

    int value = Math.min(ench, itemInstance.isWeapon() ? 300 : 120);

    itemInstance.setAttributeElement(itemInstance.isWeapon() ? _element : Element.getReverseElement(_element), value);

    player.sendPacket(new InventoryUpdate().addModifiedItem(itemInstance));
    activeChar.sendMessage("Changed attribute enchantment of " + player.getName() + "'s " + itemInstance.getName() + " to " + value + " " + attribute + ".");
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_seteh, 
    admin_seteah, 
    admin_setec, 
    admin_seteac, 
    admin_seteg, 
    admin_seteag, 
    admin_setel, 
    admin_seteal, 
    admin_seteb, 
    admin_seteab, 
    admin_setew, 
    admin_seteaw, 
    admin_setes, 
    admin_setle, 
    admin_setre, 
    admin_setlf, 
    admin_setrf, 
    admin_seten, 
    admin_setun, 
    admin_setba, 
    admin_setha, 
    admin_setdha, 
    admin_setlbr, 
    admin_setrbr, 
    admin_setbelt, 
    admin_enchant;
  }
}