package net.sf.l2j.gameserver.network.clientpackets;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.WebStat;
import net.sf.l2j.util.Rnd;

public final class RequestEnchantItemAlt extends RequestEnchantItem
{
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if ((player == null) || (_objectId == 0)) {
      return;
    }

    if (System.currentTimeMillis() - player.gCPJ() < 700L) {
      cancelActiveEnchant(player);
      return;
    }

    player.sCPJ();

    if (player.isOutOfControl()) {
      cancelActiveEnchant(player);
      return;
    }
    if ((player.isDead()) || (player.isAlikeDead()) || (player.isFakeDeath())) {
      cancelActiveEnchant(player);
      return;
    }
    if (player.isInOlympiadMode()) {
      cancelActiveEnchant(player);
      return;
    }

    if (Config.ENCH_ANTI_CLICK) {
      if (player.getEnchClicks() >= Config.ENCH_ANTI_CLICK_STEP) {
        cancelActiveEnchant(player);
        player.showAntiClickPWD();
        return;
      }
      player.updateEnchClicks();
    }

    Inventory inventory = player.getInventory();
    L2ItemInstance item = inventory.getItemByObjectId(_objectId);
    L2ItemInstance scroll = player.getActiveEnchantItem();
    player.setActiveEnchantItem(null);

    if ((item == null) || (scroll == null)) {
      cancelActiveEnchant(player);
      return;
    }

    if (player.isTransactionInProgress()) {
      cancelActiveEnchant(player);
      return;
    }

    if (!player.tradeLeft()) {
      cancelActiveEnchant(player);
      return;
    }

    if (player.isOnline() == 0) {
      cancelActiveEnchant(player);
      return;
    }

    if (player.getActiveWarehouse() != null)
    {
      cancelActiveEnchant(player);
      return;
    }

    if (player.getActiveTradeList() != null)
    {
      cancelActiveEnchant(player);
      return;
    }

    if (player.getPrivateStoreType() != 0) {
      cancelActiveEnchant(player);
      return;
    }

    if (!item.canBeEnchanted()) {
      cancelActiveEnchant(player);
      return;
    }

    if ((item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY) && (item.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL)) {
      cancelActiveEnchant(player);
      return;
    }

    if (item.isWear()) {
      cancelActiveEnchant(player);

      return;
    }

    if (item.getOwnerId() != player.getObjectId()) {
      cancelActiveEnchant(player);
      return;
    }

    short crystalId = item.getEnchantCrystalId(scroll);

    if (crystalId == 0) {
      player.sendPacket(new EnchantResult(item.getEnchantLevel(), true));
      player.sendPacket(Static.INAPPROPRIATE_ENCHANT_CONDITION);
      player.sendActionFailed();
      return;
    }

    int maxEnchant = item.getMaxEnchant();

    if (item.getEnchantLevel() >= maxEnchant) {
      player.sendPacket(new EnchantResult(item.getEnchantLevel(), true));
      player.sendMessage("\u0414\u043E\u0441\u0442\u0438\u0433\u043D\u0443\u0442 \u043F\u0440\u0435\u0434\u0435\u043B \u0437\u0430\u0442\u043E\u0447\u043A\u0438 +" + maxEnchant);
      player.sendActionFailed();
      return;
    }

    synchronized (inventory)
    {
      if (!player.destroyItemByItemId("Enchant", scroll.getItemId(), 1, player, false)) {
        cancelActiveEnchant(player);
        return;
      }

    }

    int safeEnchantLevel = item.getItem().getBodyPart() == 32768 ? Config.ENCHANT_SAFE_MAX_FULL : Config.ENCHANT_SAFE_MAX;
    int chance = item.getEnchantLevel() < safeEnchantLevel ? 100 : calculateChance(item, item.getEnchantLevel(), scroll.isCrystallEnchantScroll());

    if (chance > 0) if (Rnd.calcEnchant(chance, (Config.PREMIUM_ENABLE) && (player.isPremium()))) {
        notifyEnchant(player.getName(), item.getItemName(), item.getEnchantLevel(), 1);
        SystemMessage sm;
        SystemMessage sm;
        if (item.getEnchantLevel() == 0)
          sm = SystemMessage.id(SystemMessageId.S1_SUCCESSFULLY_ENCHANTED).addItemName(item.getItemId());
        else {
          sm = SystemMessage.id(SystemMessageId.S1_S2_SUCCESSFULLY_ENCHANTED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
        }

        player.sendUserPacket(sm);
        item.setEnchantLevel(calcNewEnchant(item.getEnchantLevel(), maxEnchant));
        item.updateDatabase(); break label949;
      }
    notifyEnchant(player.getName(), item.getItemName(), item.getEnchantLevel(), 0);
    if ((scroll.isBlessedEnchantScroll()) || (scroll.isCrystallEnchantScroll())) {
      int fail = calculateFail(item, item.getEnchantLevel(), scroll.isCrystallEnchantScroll(), (Config.PREMIUM_ENABLE) && (player.isPremium()));
      item.setEnchantLevel(fail);

      player.sendMessage("\u0421\u0443\u043F\u0435\u0440-\u0443\u043B\u0443\u0447\u0448\u0435\u043D\u0438\u0435 \u043D\u0435\u0443\u0434\u0430\u0447\u043D\u043E. \u0417\u0430\u0442\u043E\u0447\u043A\u0430 \u0441\u0431\u0440\u043E\u0448\u0435\u043D\u0430 \u043D\u0430 +" + fail);
    } else {
      if (item.isEquipped())
        inventory.unEquipItemInSlot(item.getEquipSlot());
      SystemMessage sm;
      if (item.getEnchantLevel() > 0)
        sm = SystemMessage.id(SystemMessageId.ENCHANTMENT_FAILED_S1_S2_EVAPORATED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
      else {
        sm = SystemMessage.id(SystemMessageId.ENCHANTMENT_FAILED_S1_EVAPORATED).addItemName(item.getItemId());
      }

      player.sendUserPacket(sm);

      L2ItemInstance destroyedItem = inventory.destroyItem("Enchant", item, player, null);
      if (destroyedItem == null)
      {
        player.setActiveEnchantItem(null);
        player.sendActionFailed();
        return;
      }

      int count = (int)(item.getItem().getCrystalCount() * 0.87D);
      if (destroyedItem.getEnchantLevel() > 3) {
        count = (int)(count + item.getItem().getCrystalCount() * 0.25D * (destroyedItem.getEnchantLevel() - 3));
      }
      if (count < 1) {
        count = 1;
      }

      L2ItemInstance crystals = inventory.addItem("Enchant", crystalId, count, player, destroyedItem);

      SystemMessage sm = SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(crystals.getItemId()).addNumber(count);
      player.sendUserPacket(sm);
      player.refreshExpertisePenalty();
      player.refreshOverloaded();
    }
    SystemMessage sm = null;

    label949: player.setInEnch(false);
    player.sendItems(true);

    player.sendUserPacket(new EnchantResult(65535, true));

    player.broadcastUserInfo();
  }

  private int calculateChance(L2ItemInstance item, int next, boolean crystall) {
    Integer chance = Integer.valueOf(0);
    switch (item.getItem().getType2()) {
    case 0:
      if (crystall) {
        return Config.ENCHANT_CHANCE_WEAPON_CRYSTAL;
      }

      if (item.isMagicWeapon()) {
        chance = (Integer)Config.ENCHANT_ALT_MAGICSTEPS.get(Integer.valueOf(next));
        if (chance != null) break;
        chance = Integer.valueOf(Config.ENCHANT_ALT_MAGICCAHNCE);
      }
      else {
        chance = (Integer)Config.ENCHANT_ALT_WEAPONSTEPS.get(Integer.valueOf(next));
        if (chance != null) break;
        chance = Integer.valueOf(Config.ENCHANT_ALT_WEAPONCAHNCE); } break;
    case 1:
      if (crystall) {
        return Config.ENCHANT_CHANCE_ARMOR_CRYSTAL;
      }

      chance = (Integer)Config.ENCHANT_ALT_ARMORSTEPS.get(Integer.valueOf(next));
      if (chance != null) break;
      chance = Integer.valueOf(Config.ENCHANT_ALT_ARMORCAHNCE); break;
    case 2:
      if (crystall) {
        return Config.ENCHANT_CHANCE_JEWELRY_CRYSTAL;
      }

      chance = (Integer)Config.ENCHANT_ALT_JEWERLYSTEPS.get(Integer.valueOf(next));
      if (chance != null) break;
      chance = Integer.valueOf(Config.ENCHANT_ALT_JEWERLYCAHNCE);
    }

    return chance.intValue();
  }

  private int calculateFail(L2ItemInstance item, int ench, boolean crystall, boolean premium) {
    if ((premium) && (Config.PREMIUM_ENCHANT_FAIL))
    {
      ench -= 3;
      ench = Math.max(ench, 0);
      return ench;
    }
    int fail = 0;
    switch (item.getItem().getType2()) {
    case 0:
      fail = Config.ENCHANT_ALT_WEAPONFAILBLESS;
      if (!crystall) break;
      fail = Config.ENCHANT_ALT_WEAPONFAILCRYST; break;
    case 1:
      fail = Config.ENCHANT_ALT_ARMORFAILBLESS;
      if (!crystall) break;
      fail = Config.ENCHANT_ALT_ARMORFAILCRYST; break;
    case 2:
      fail = Config.ENCHANT_ALT_JEWERLYFAILBLESS;
      if (!crystall) break;
      fail = Config.ENCHANT_ALT_JEWERLYFAILCRYST;
    }

    if (fail < 0) {
      fail = ench + fail;
    }

    fail = Math.max(fail, 0);
    return fail;
  }

  private void notifyEnchant(String name, String item, int ench, int sucess)
  {
    if ((Config.WEBSTAT_ENABLE) && (ench >= Config.WEBSTAT_ENCHANT))
      WebStat.getInstance().addEnchant(name, item, ench, sucess);
  }

  private int calcNewEnchant(int enchantLevel, int max)
  {
    enchantLevel += Config.ENCHANT_ALT_STEP;
    return Math.min(enchantLevel, max);
  }
}