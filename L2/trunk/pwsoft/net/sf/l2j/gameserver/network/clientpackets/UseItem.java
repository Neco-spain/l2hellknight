package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.ShowCalculator;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import scripts.items.IItemHandler;
import scripts.items.ItemHandler;

public final class UseItem extends L2GameClientPacket
{
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
    if (item == null) {
      return;
    }

    int itemId = item.getItemId();

    if ((itemId == 57) || (player.isParalyzed())) {
      return;
    }

    if (!item.isEquipable()) {
      if (System.currentTimeMillis() - player.gCPBA() < 200L) {
        return;
      }
      player.sCPBA();
    }

    if ((player.isStunned()) || (player.isSleeping()) || (player.isAfraid()) || (player.isFakeDeath())) {
      return;
    }

    if ((player.isDead()) || (player.isAlikeDead())) {
      return;
    }

    if (player.getPrivateStoreType() != 0) {
      player.sendPacket(Static.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
      return;
    }

    if (player.getActiveTradeList() != null) {
      player.cancelActiveTrade();
      return;
    }

    if (player.getActiveWarehouse() != null) {
      player.cancelActiveWarehouse();
      return;
    }

    if (player.getActiveEnchantItem() != null) {
      player.setActiveEnchantItem(null);
      player.sendPacket(new EnchantResult(0, true));
      return;
    }

    synchronized (player.getInventory())
    {
      if ((player.isFishing()) && ((itemId < 6535) || (itemId > 6540)))
      {
        player.sendPacket(Static.CANNOT_DO_WHILE_FISHING_3);
        return;
      }

      if ((item.getItem().isForWolf()) || (item.getItem().isForHatchling()) || (item.getItem().isForStrider()) || (item.getItem().isForBabyPet())) {
        player.sendPacket(Static.CANNOT_EQUIP_PET_ITEM);
        return;
      }

      if (item.isEquipable())
      {
        if (((player.isInOlympiadMode()) || ((Config.FORBIDDEN_EVENT_ITMES) && (player.isInEventChannel()))) && ((item.isHeroItem()) || (item.notForOly()))) {
          player.sendActionFailed();
          return;
        }

        if ((player.getChannel() == 67) && (item.notForBossZone())) {
          player.sendActionFailed();
          return;
        }

        if ((item.getItem().isHippy()) && (player.underAttack())) {
          player.sendActionFailed();
          return;
        }

        switch (item.getItem().getBodyPart())
        {
        case 128:
        case 256:
        case 16384:
          if ((player.isCursedWeaponEquiped()) || (itemId == 6408))
          {
            player.sendActionFailed();
            return;
          }
          player.equipWeapon(item);
          break;
        default:
          player.useEquippableItem(item, true);
        }
        return;
      }

      if (itemId == 4393) {
        player.sendPacket(new ShowCalculator(4393));
        return;
      }

      L2Weapon weaponItem = player.getActiveWeaponItem();
      if (weaponItem != null) {
        if ((item.isLure()) && (weaponItem.getItemType() == L2WeaponType.ROD)) {
          player.getInventory().setPaperdollItem(8, item);
          player.broadcastUserInfo();

          player.sendItems(false);
          return;
        }

        if ((itemId == 8192) && (weaponItem.getItemType() != L2WeaponType.BOW)) {
          player.sendMessage("\u0418\u0441\u043F\u043E\u043B\u044C\u0437\u0443\u0435\u0442\u0441\u044F \u0441 \u043B\u0443\u043A\u043E\u043C");
          return;
        }
      }

      IItemHandler handler = ItemHandler.getInstance().getItemHandler(itemId);
      if (handler != null)
        handler.useItem(player, item);
    }
  }
}