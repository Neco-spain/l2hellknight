package l2p.gameserver.clientpackets;

import l2p.commons.dao.JdbcEntityState;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.ItemInstance.ItemLocation;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.ExAttributeEnchantResult;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.item.ItemTemplate.Grade;
import l2p.gameserver.utils.ItemFunctions;

public class RequestEnchantItemAttribute extends L2GameClientPacket
{
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (_objectId == -1)
    {
      activeChar.setEnchantScroll(null);
      activeChar.sendPacket(Msg.ELEMENTAL_POWER_ENCHANCER_USAGE_HAS_BEEN_CANCELLED);
      return;
    }

    if (activeChar.isActionsDisabled())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendPacket(new IStaticPacket[] { Msg.YOU_CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP, ActionFail.STATIC });
      return;
    }

    if (activeChar.isInTrade())
    {
      activeChar.sendActionFailed();
      return;
    }

    PcInventory inventory = activeChar.getInventory();
    ItemInstance itemToEnchant = inventory.getItemByObjectId(_objectId);
    ItemInstance stone = activeChar.getEnchantScroll();
    activeChar.setEnchantScroll(null);

    if ((itemToEnchant == null) || (stone == null))
    {
      activeChar.sendActionFailed();
      return;
    }

    ItemTemplate item = itemToEnchant.getTemplate();

    if ((!itemToEnchant.canBeEnchanted(true)) || (item.getCrystalType().cry < 1462))
    {
      activeChar.sendPacket(new IStaticPacket[] { Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC });
      return;
    }

    if ((itemToEnchant.getLocation() != ItemInstance.ItemLocation.INVENTORY) && (itemToEnchant.getLocation() != ItemInstance.ItemLocation.PAPERDOLL))
    {
      activeChar.sendPacket(new IStaticPacket[] { Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC });
      return;
    }

    if ((itemToEnchant.isStackable()) || ((stone = inventory.getItemByObjectId(stone.getObjectId())) == null))
    {
      activeChar.sendActionFailed();
      return;
    }

    Element element = ItemFunctions.getEnchantAttributeStoneElement(stone.getItemId(), itemToEnchant.isArmor());

    if (itemToEnchant.isArmor())
    {
      if (itemToEnchant.getAttributeElementValue(Element.getReverseElement(element), false) != 0)
      {
        activeChar.sendPacket(new IStaticPacket[] { Msg.ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED, ActionFail.STATIC });
        return;
      }
    }
    else if (itemToEnchant.isWeapon())
    {
      if ((itemToEnchant.getAttributeElement() != Element.NONE) && (itemToEnchant.getAttributeElement() != element))
      {
        activeChar.sendPacket(new IStaticPacket[] { Msg.ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED, ActionFail.STATIC });
        return;
      }
    }
    else
    {
      activeChar.sendPacket(new IStaticPacket[] { Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC });
      return;
    }

    if ((item.isUnderwear()) || (item.isCloak()) || (item.isBracelet()) || (item.isBelt()) || (!item.isAttributable()))
    {
      activeChar.sendPacket(new IStaticPacket[] { Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC });
      return;
    }

    int maxValue = itemToEnchant.isWeapon() ? 150 : 60;

    if (stone.getTemplate().isAttributeCrystal()) {
      maxValue += (itemToEnchant.isWeapon() ? 150 : 60);
    }
    if (itemToEnchant.getAttributeElementValue(element, false) >= maxValue)
    {
      activeChar.sendPacket(new IStaticPacket[] { Msg.ELEMENTAL_POWER_ENCHANCER_USAGE_HAS_BEEN_CANCELLED, ActionFail.STATIC });
      return;
    }

    if (itemToEnchant.getOwnerId() != activeChar.getObjectId())
    {
      activeChar.sendPacket(new IStaticPacket[] { Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC });
      return;
    }

    if (!inventory.destroyItem(stone, 1L))
    {
      activeChar.sendActionFailed();
      return;
    }

    if (Rnd.chance(stone.getTemplate().isAttributeCrystal() ? Config.ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE : Config.ENCHANT_ATTRIBUTE_STONE_CHANCE))
    {
      if (itemToEnchant.getEnchantLevel() == 0)
      {
        SystemMessage sm = new SystemMessage(2147);
        sm.addItemName(itemToEnchant.getItemId());
        sm.addItemName(stone.getItemId());
        activeChar.sendPacket(sm);
      }
      else
      {
        SystemMessage sm = new SystemMessage(2148);
        sm.addNumber(itemToEnchant.getEnchantLevel());
        sm.addItemName(itemToEnchant.getItemId());
        sm.addItemName(stone.getItemId());
        activeChar.sendPacket(sm);
      }

      int value = itemToEnchant.isWeapon() ? 5 : 6;

      if ((itemToEnchant.getAttributeElementValue(element, false) == 0) && (itemToEnchant.isWeapon())) {
        value = 20;
      }
      boolean equipped = false;
      if ((equipped = itemToEnchant.isEquipped()))
      {
        activeChar.getInventory().isRefresh = true;
        activeChar.getInventory().unEquipItem(itemToEnchant);
      }

      itemToEnchant.setAttributeElement(element, itemToEnchant.getAttributeElementValue(element, false) + value);
      itemToEnchant.setJdbcState(JdbcEntityState.UPDATED);
      itemToEnchant.update();

      if (equipped)
      {
        activeChar.getInventory().equipItem(itemToEnchant);
        activeChar.getInventory().isRefresh = false;
      }

      activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToEnchant));
      activeChar.sendPacket(new ExAttributeEnchantResult(value));
    }
    else {
      activeChar.sendPacket(Msg.YOU_HAVE_FAILED_TO_ADD_ELEMENTAL_POWER);
    }
    activeChar.setEnchantScroll(null);
    activeChar.updateStats();
  }
}