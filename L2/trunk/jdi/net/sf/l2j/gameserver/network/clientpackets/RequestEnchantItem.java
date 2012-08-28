package net.sf.l2j.gameserver.network.clientpackets;

import java.util.Map;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public final class RequestEnchantItem extends L2GameClientPacket
{
  protected static final Logger _log = Logger.getLogger(Inventory.class.getName());
  private static final String _C__58_REQUESTENCHANTITEM = "[C] 58 RequestEnchantItem";
  private static final int[] CRYSTAL_SCROLLS = { 731, 732, 949, 950, 953, 954, 957, 958, 961, 962 };
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (_objectId == 0)) return;

    activeChar.cancelActiveTrade();
    if ((activeChar.isProcessingTransaction()) || (activeChar.isInStoreMode())) {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
      activeChar.setActiveEnchantItem(null);
      return;
    }

    L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
    L2ItemInstance scroll = activeChar.getActiveEnchantItem();
    activeChar.setActiveEnchantItem(null);
    if ((item == null) || (scroll == null)) return;

    if (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), 15))
    {
      return;
    }

    if ((item.getItem().getItemType() == L2WeaponType.ROD) || ((item.getItemId() >= 6611) && (item.getItemId() <= 6621)) || (item.isShadowItem()))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
      activeChar.setActiveEnchantItem(null);
      activeChar.sendPacket(new EnchantResult(1));
      return;
    }
    if (item.isWear())
    {
      Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant a weared Item", 2);
      return;
    }
    int itemType2 = item.getItem().getType2();
    boolean enchantItem = false;
    boolean blessedScroll = false;
    boolean blesseddScroll = false;
    boolean crystallScroll = false;
    int crystalId = 0;

    if (activeChar.getActiveTradeList() != null) {
      activeChar.cancelActiveTrade();
      activeChar.sendMessage("\u0422\u043E\u0440\u0433\u043E\u0432\u043B\u044F \u043E\u0442\u043C\u0435\u043D\u0435\u043D\u0430");
      return;
    }

    switch (item.getItem().getCrystalType())
    {
    case 4:
      crystalId = 1461;
      switch (scroll.getItemId()) { case 729:
      case 731:
      case 6569:
        if (itemType2 != 0) break;
        enchantItem = true; break;
      case 730:
      case 732:
      case 6570:
        if ((itemType2 != 1) && (itemType2 != 2)) break;
        enchantItem = true;
      }

      break;
    case 3:
      crystalId = 1460;
      switch (scroll.getItemId()) { case 947:
      case 949:
      case 6571:
        if (itemType2 != 0) break;
        enchantItem = true; break;
      case 948:
      case 950:
      case 6572:
        if ((itemType2 != 1) && (itemType2 != 2)) break;
        enchantItem = true;
      }

      break;
    case 2:
      crystalId = 1459;
      switch (scroll.getItemId()) { case 951:
      case 953:
      case 6573:
        if (itemType2 != 0) break;
        enchantItem = true; break;
      case 952:
      case 954:
      case 6574:
        if ((itemType2 != 1) && (itemType2 != 2)) break;
        enchantItem = true;
      }

      break;
    case 1:
      crystalId = 1458;
      switch (scroll.getItemId()) { case 955:
      case 957:
      case 6575:
        if (itemType2 != 0) break;
        enchantItem = true; break;
      case 956:
      case 958:
      case 6576:
        if ((itemType2 != 1) && (itemType2 != 2)) break;
        enchantItem = true;
      }

      break;
    case 5:
      crystalId = 1462;
      switch (scroll.getItemId()) { case 959:
      case 961:
      case 6577:
        if (itemType2 != 0) break;
        enchantItem = true; break;
      case 960:
      case 962:
      case 6578:
        if ((itemType2 != 1) && (itemType2 != 2)) break;
        enchantItem = true;
      }

    }

    if (!enchantItem)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
      activeChar.setActiveEnchantItem(null);
      activeChar.sendPacket(new EnchantResult(1));
      return;
    }

    if ((scroll.getItemId() >= 6569) && (scroll.getItemId() <= 6578))
    {
      blesseddScroll = true;
      blessedScroll = true;
    }
    else {
      for (int crystalscroll : CRYSTAL_SCROLLS) {
        if (scroll.getItemId() != crystalscroll)
          continue;
        crystallScroll = true;
        blessedScroll = true;
        break;
      }

    }

    int chance = 0;
    int maxEnchantLevel = 0;

    if (item.getItem().getType2() == 0)
    {
      if ((item.getEnchantLevel() < 10) && (!blesseddScroll) && (!crystallScroll)) chance = Config.ENCHANT_CHANCE_WEAPON;
      else if ((item.getEnchantLevel() < 16) && (item.getEnchantLevel() > 9) && (!blesseddScroll) && (!crystallScroll)) chance = Config.ENCHANT_CHANCE_WEAPON_1015;
      else if ((item.getEnchantLevel() > 15) && (!blesseddScroll) && (!crystallScroll)) chance = Config.ENCHANT_CHANCE_WEAPON_16;
      else if ((item.getEnchantLevel() < 10) && (blesseddScroll)) chance = Config.BLESSED_CHANCE_WEAPON;
      else if ((item.getEnchantLevel() < 16) && (item.getEnchantLevel() > 9) && (blesseddScroll)) chance = Config.BLESSED_CHANCE_WEAPON_1015;
      else if ((item.getEnchantLevel() > 15) && (blesseddScroll)) chance = Config.BLESSED_CHANCE_WEAPON_16;
      else if ((item.getEnchantLevel() < 10) && (crystallScroll)) chance = Config.CRYSTAL_CHANCE_WEAPON;
      else if ((item.getEnchantLevel() < 16) && (item.getEnchantLevel() > 9) && (crystallScroll)) chance = Config.CRYSTAL_CHANCE_WEAPON_1015;
      else if ((item.getEnchantLevel() > 15) && (crystallScroll)) chance = Config.CRYSTAL_CHANCE_WEAPON_16;
      maxEnchantLevel = Config.ENCHANT_MAX_WEAPON;

      if ((Config.ENABLE_MODIFY_ENCHANT_CHANCE_WEAPON) && (blesseddScroll))
      {
        if (Config.ENCHANT_CHANCE_LIST_WEAPON.containsKey(Integer.valueOf(item.getEnchantLevel())))
        {
          chance = ((Integer)Config.ENCHANT_CHANCE_LIST_WEAPON.get(Integer.valueOf(item.getEnchantLevel()))).intValue();
        }
      }
    }
    else if (item.getItem().getType2() == 1)
    {
      if ((item.getEnchantLevel() < 10) && (!blesseddScroll) && (!crystallScroll)) chance = Config.ENCHANT_CHANCE_ARMOR;
      else if ((item.getEnchantLevel() < 16) && (item.getEnchantLevel() > 9) && (!blesseddScroll) && (!crystallScroll)) chance = Config.ENCHANT_CHANCE_ARMOR_1015;
      else if ((item.getEnchantLevel() > 15) && (!blesseddScroll) && (!crystallScroll)) chance = Config.ENCHANT_CHANCE_ARMOR_16;
      else if ((item.getEnchantLevel() < 10) && (blesseddScroll)) chance = Config.BLESSED_CHANCE_ARMOR;
      else if ((item.getEnchantLevel() < 16) && (item.getEnchantLevel() > 9) && (blesseddScroll)) chance = Config.BLESSED_CHANCE_ARMOR_1015;
      else if ((item.getEnchantLevel() > 15) && (blesseddScroll)) chance = Config.BLESSED_CHANCE_ARMOR_16;
      else if ((item.getEnchantLevel() < 10) && (crystallScroll)) chance = Config.CRYSTAL_CHANCE_ARMOR;
      else if ((item.getEnchantLevel() < 16) && (item.getEnchantLevel() > 9) && (crystallScroll)) chance = Config.CRYSTAL_CHANCE_ARMOR_1015;
      else if ((item.getEnchantLevel() > 15) && (crystallScroll)) chance = Config.CRYSTAL_CHANCE_ARMOR_16;
      maxEnchantLevel = Config.ENCHANT_MAX_ARMOR;

      if ((Config.ENABLE_MODIFY_ENCHANT_CHANCE_ARMOR) && (blesseddScroll))
      {
        if (Config.ENCHANT_CHANCE_LIST_ARMOR.containsKey(Integer.valueOf(item.getEnchantLevel())))
        {
          chance = ((Integer)Config.ENCHANT_CHANCE_LIST_ARMOR.get(Integer.valueOf(item.getEnchantLevel()))).intValue();
        }
      }
    }
    else if (item.getItem().getType2() == 2)
    {
      if ((item.getEnchantLevel() < 10) && (!blesseddScroll) && (!crystallScroll)) chance = Config.ENCHANT_CHANCE_JEWELRY;
      else if ((item.getEnchantLevel() < 16) && (item.getEnchantLevel() > 9) && (!blesseddScroll) && (!crystallScroll)) chance = Config.ENCHANT_CHANCE_JEWELRY_1015;
      else if ((item.getEnchantLevel() > 15) && (!blesseddScroll) && (!crystallScroll)) chance = Config.ENCHANT_CHANCE_JEWELRY_16;
      else if ((item.getEnchantLevel() < 10) && (blesseddScroll)) chance = Config.BLESSED_CHANCE_JEWELRY;
      else if ((item.getEnchantLevel() < 16) && (item.getEnchantLevel() > 9) && (blesseddScroll)) chance = Config.BLESSED_CHANCE_JEWELRY_1015;
      else if ((item.getEnchantLevel() > 15) && (blesseddScroll)) chance = Config.BLESSED_CHANCE_JEWELRY_16;
      else if ((item.getEnchantLevel() < 10) && (crystallScroll)) chance = Config.CRYSTAL_CHANCE_JEWELRY;
      else if ((item.getEnchantLevel() < 16) && (item.getEnchantLevel() > 9) && (crystallScroll)) chance = Config.CRYSTAL_CHANCE_JEWELRY_1015;
      else if ((item.getEnchantLevel() > 15) && (crystallScroll)) chance = Config.CRYSTAL_CHANCE_JEWELRY_16;
      maxEnchantLevel = Config.ENCHANT_MAX_JEWELRY;

      if ((Config.ENABLE_MODIFY_ENCHANT_CHANCE_JEWELRY) && (blesseddScroll))
      {
        if (Config.ENCHANT_CHANCE_LIST_JEWELRY.containsKey(Integer.valueOf(item.getEnchantLevel())))
        {
          chance = ((Integer)Config.ENCHANT_CHANCE_LIST_JEWELRY.get(Integer.valueOf(item.getEnchantLevel()))).intValue();
        }

      }

    }

    if ((item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX) || ((item.getItem().getBodyPart() == 32768) && (item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL)))
    {
      chance = 100;
    }
    if ((item.getEnchantLevel() >= maxEnchantLevel) && (maxEnchantLevel != 0))
    {
      activeChar.sendMessage("\u0414\u043E\u0441\u0442\u0438\u0433\u043D\u0443\u0442 \u043C\u0430\u043A\u0441\u0438\u043C\u0430\u043B\u044C\u043D\u044B\u0439 \u0443\u0440\u043E\u0432\u0435\u043D\u044C \u0437\u0430\u0442\u043E\u0447\u043A\u0438.");
      return;
    }
    if (Config.ENCHANT_STACKABLE)
      scroll = activeChar.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1, activeChar, item);
    else
      scroll = activeChar.getInventory().destroyItem("Enchant", scroll, activeChar, item);
    if (scroll == null)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
      Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant with a scroll he doesnt have", Config.DEFAULT_PUNISH);
      return;
    }

    if (Rnd.get(100) < chance)
    {
      synchronized (item)
      {
        if (item.getOwnerId() != activeChar.getObjectId())
        {
          activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
          return;
        }
        if ((item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY) && (item.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL))
        {
          activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
          activeChar.setActiveEnchantItem(null);
          activeChar.sendPacket(new EnchantResult(1));
          return;
        }
        if (item.getEnchantLevel() == 0)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_SUCCESSFULLY_ENCHANTED);
          sm.addItemName(item.getItemId());
          activeChar.sendPacket(sm);
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2_SUCCESSFULLY_ENCHANTED);
          sm.addNumber(item.getEnchantLevel());
          sm.addItemName(item.getItemId());
          activeChar.sendPacket(sm);
        }
        item.setEnchantLevel(item.getEnchantLevel() + 1);
        item.updateDatabase();
      }
    }
    else
    {
      if (!blessedScroll)
      {
        if (item.getEnchantLevel() > 0)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_S2_EVAPORATED);
          sm.addNumber(item.getEnchantLevel());
          sm.addItemName(item.getItemId());
          activeChar.sendPacket(sm);
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_EVAPORATED);
          sm.addItemName(item.getItemId());
          activeChar.sendPacket(sm);
        }
      }
      else
      {
        sm = new SystemMessage(SystemMessageId.BLESSED_ENCHANT_FAILED);
        activeChar.sendPacket(sm);
      }

      if (!blessedScroll)
      {
        if (item.getEnchantLevel() > 0)
        {
          sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
          sm.addNumber(item.getEnchantLevel());
          sm.addItemName(item.getItemId());
          activeChar.sendPacket(sm);
        }
        else
        {
          sm = new SystemMessage(SystemMessageId.S1_DISARMED);
          sm.addItemName(item.getItemId());
          activeChar.sendPacket(sm);
        }

        L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
        if (item.isEquipped())
        {
          InventoryUpdate iu = new InventoryUpdate();
          for (int i = 0; i < unequiped.length; i++)
          {
            iu.addModifiedItem(unequiped[i]);
          }
          activeChar.sendPacket(iu);
          activeChar.broadcastUserInfo();
        }

        int count = item.getCrystalCount() - (item.getItem().getCrystalCount() + 1) / 2;
        if (count < 1) count = 1;

        L2ItemInstance destroyItem = activeChar.getInventory().destroyItem("Enchant", item, activeChar, null);
        if (destroyItem == null) return;

        L2ItemInstance crystals = activeChar.getInventory().addItem("Enchant", crystalId, count, activeChar, destroyItem);

        sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
        sm.addItemName(crystals.getItemId());
        sm.addNumber(count);
        activeChar.sendPacket(sm);

        if (!Config.FORCE_INVENTORY_UPDATE)
        {
          InventoryUpdate iu = new InventoryUpdate();
          if (destroyItem.getCount() == 0) iu.addRemovedItem(destroyItem); else
            iu.addModifiedItem(destroyItem);
          iu.addItem(crystals);

          activeChar.sendPacket(iu);
        } else {
          activeChar.sendPacket(new ItemList(activeChar, true));
        }
        StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
        su.addAttribute(14, activeChar.getCurrentLoad());
        activeChar.sendPacket(su);

        activeChar.broadcastUserInfo();

        L2World world = L2World.getInstance();
        world.removeObject(destroyItem);
      }
      else
      {
        item.setEnchantLevel(Config.ENCHANT_FAIL);
        item.updateDatabase();
      }
    }
    SystemMessage sm = null;

    StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
    su.addAttribute(14, activeChar.getCurrentLoad());
    activeChar.sendPacket(su);
    su = null;

    activeChar.sendPacket(new EnchantResult(item.getEnchantLevel()));
    activeChar.sendPacket(new ItemList(activeChar, false));
    activeChar.broadcastUserInfo();
  }

  public String getType()
  {
    return "[C] 58 RequestEnchantItem";
  }
}