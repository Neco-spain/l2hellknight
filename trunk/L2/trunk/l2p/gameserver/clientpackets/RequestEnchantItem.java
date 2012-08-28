package l2p.gameserver.clientpackets;

import java.util.Set;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.EnchantItemHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.EnchantResult;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.item.ItemTemplate.Grade;
import l2p.gameserver.templates.item.support.EnchantScroll;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Log;
import org.napile.primitive.sets.IntSet;

public class RequestEnchantItem extends L2GameClientPacket
{
  private int _objectId;
  private int _catalystObjId;

  protected void readImpl()
  {
    _objectId = readD();
    _catalystObjId = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (player.isActionsDisabled())
    {
      player.setEnchantScroll(null);
      player.sendActionFailed();
      return;
    }

    if (player.isInTrade())
    {
      player.setEnchantScroll(null);
      player.sendActionFailed();
      return;
    }

    if (player.isInStoreMode())
    {
      player.setEnchantScroll(null);
      player.sendPacket(EnchantResult.CANCEL);
      player.sendPacket(SystemMsg.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
      player.sendActionFailed();
      return;
    }

    PcInventory inventory = player.getInventory();
    inventory.writeLock();
    try
    {
      ItemInstance item = inventory.getItemByObjectId(_objectId);
      ItemInstance catalyst = _catalystObjId > 0 ? inventory.getItemByObjectId(_catalystObjId) : null;
      ItemInstance scroll = player.getEnchantScroll();

      if ((item == null) || (scroll == null)) {
        player.sendActionFailed();
        return;
      }
      EnchantScroll enchantScroll = EnchantItemHolder.getInstance().getEnchantScroll(scroll.getItemId());
      if (enchantScroll == null) {
        doEnchantOld(player, item, scroll, catalyst);
        return;
      }
      if ((enchantScroll.getMaxEnchant() != -1) && (item.getEnchantLevel() >= enchantScroll.getMaxEnchant())) { player.sendPacket(EnchantResult.CANCEL);
        player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
        player.sendActionFailed();
        return; }
      if (enchantScroll.getItems().size() > 0)
      {
        if (!enchantScroll.getItems().contains(item.getItemId())) {
          player.sendPacket(EnchantResult.CANCEL);
          player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
          player.sendActionFailed();
          return;
        }
      } else if (!enchantScroll.getGrades().contains(item.getCrystalType())) {
        player.sendPacket(EnchantResult.CANCEL);
        player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
        player.sendActionFailed();
        return;
      }if (!item.canBeEnchanted(false)) { player.sendPacket(EnchantResult.CANCEL);
        player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
        player.sendActionFailed();
        return; }
      if ((!inventory.destroyItem(scroll, 1L)) || ((catalyst != null) && (!inventory.destroyItem(catalyst, 1L)))) {
        player.sendPacket(EnchantResult.CANCEL);
        player.sendActionFailed();
        return;
      }boolean equipped = false;
      if ((equipped = item.isEquipped())) {
        inventory.unEquipItem(item);
      }
      int safeEnchantLevel = item.getTemplate().getBodyPart() == 32768 ? 4 : 3;

      int chance = enchantScroll.getChance();
      if (item.getEnchantLevel() < safeEnchantLevel) {
        chance = 100;
      }
      if (Rnd.chance(chance))
      {
        item.setEnchantLevel(item.getEnchantLevel() + 1);
        item.setJdbcState(JdbcEntityState.UPDATED);
        item.update();

        if (equipped) {
          inventory.equipItem(item);
        }
        player.sendPacket(new InventoryUpdate().addModifiedItem(item));

        player.sendPacket(EnchantResult.SUCESS);

        if ((enchantScroll.isHasVisualEffect()) && (item.getEnchantLevel() > 3))
          player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(player, player, 5965, 1, 500, 1500L) });
      }
      else
      {
        switch (1.$SwitchMap$l2p$gameserver$templates$item$support$FailResultType[enchantScroll.getResultType().ordinal()])
        {
        case 1:
          if (item.isEquipped()) {
            player.sendDisarmMessage(item);
          }
          Log.LogItem(player, "EnchantFail", item);

          if (!inventory.destroyItem(item, 1L)) {
            player.sendActionFailed();
            return;
          }
          int crystalId = item.getCrystalType().cry;
          if ((crystalId > 0) && (item.getTemplate().getCrystalCount() > 0))
          {
            int crystalAmount = (int)(item.getTemplate().getCrystalCount() * 0.87D);
            if (item.getEnchantLevel() > 3)
              crystalAmount = (int)(crystalAmount + item.getTemplate().getCrystalCount() * 0.25D * (item.getEnchantLevel() - 3));
            if (crystalAmount < 1) {
              crystalAmount = 1;
            }
            player.sendPacket(new EnchantResult(1, crystalId, crystalAmount));
            ItemFunctions.addItem(player, crystalId, crystalAmount, true);
          }
          else {
            player.sendPacket(EnchantResult.FAILED_NO_CRYSTALS);
          }
          if (!enchantScroll.isHasVisualEffect()) break;
          player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(player, player, 5949, 1, 500, 1500L) }); break;
        case 2:
          item.setEnchantLevel(0);
          item.setJdbcState(JdbcEntityState.UPDATED);
          item.update();

          if (equipped) {
            inventory.equipItem(item);
          }
          player.sendPacket(new InventoryUpdate().addModifiedItem(item));
          player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED);
          player.sendPacket(EnchantResult.BLESSED_FAILED);
          break;
        case 3:
          player.sendPacket(EnchantResult.ANCIENT_FAILED);
        }
      }

    }
    finally
    {
      inventory.writeUnlock();

      player.setEnchantScroll(null);
      player.updateStats();
    }
  }

  private static void doEnchantOld(Player player, ItemInstance item, ItemInstance scroll, ItemInstance catalyst)
  {
    PcInventory inventory = player.getInventory();

    if (!ItemFunctions.checkCatalyst(item, catalyst)) {
      catalyst = null;
    }
    if (!item.canBeEnchanted(true))
    {
      player.sendPacket(EnchantResult.CANCEL);
      player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
      player.sendActionFailed();
      return;
    }

    int crystalId = ItemFunctions.getEnchantCrystalId(item, scroll, catalyst);

    if (crystalId == -1)
    {
      player.sendPacket(EnchantResult.CANCEL);
      player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
      player.sendActionFailed();
      return;
    }

    int scrollId = scroll.getItemId();

    if ((scrollId == 13540) && (item.getItemId() != 13539))
    {
      player.sendPacket(EnchantResult.CANCEL);
      player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
      player.sendActionFailed();
      return;
    }

    if (((scrollId == 21581) || (scrollId == 21582)) && (item.getItemId() != 21580))
    {
      player.sendPacket(EnchantResult.CANCEL);
      player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
      player.sendActionFailed();
      return;
    }

    if (((ItemFunctions.isDestructionWpnEnchantScroll(scrollId)) && (item.getEnchantLevel() >= 15)) || ((ItemFunctions.isDestructionArmEnchantScroll(scrollId)) && (item.getEnchantLevel() >= 6)))
    {
      player.sendPacket(EnchantResult.CANCEL);
      player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
      player.sendActionFailed();
      return;
    }

    int itemType = item.getTemplate().getType2();
    int ENCHANT_MAX_LEVEL = itemType == 2 ? Config.ENCHANT_MAX_JEWELRY : itemType == 0 ? Config.ENCHANT_MAX_WEAPON : Config.ENCHANT_MAX_ARMOR;

    boolean fail = false;

    switch (item.getItemId())
    {
    case 13539:
      if (item.getEnchantLevel() < Config.ENCHANT_MAX_MASTER_YOGI_STAFF) break;
      fail = true; break;
    case 21580:
      if (item.getEnchantLevel() < 9) break;
      fail = true; break;
    default:
      if (item.getEnchantLevel() < ENCHANT_MAX_LEVEL) break;
      fail = true;
    }

    if ((!inventory.destroyItem(scroll, 1L)) || ((catalyst != null) && (!inventory.destroyItem(catalyst, 1L))))
    {
      player.sendPacket(EnchantResult.CANCEL);
      player.sendActionFailed();
      return;
    }

    if (fail)
    {
      player.sendPacket(EnchantResult.CANCEL);
      player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
      player.sendActionFailed();
      return;
    }

    int safeEnchantLevel = item.getTemplate().getBodyPart() == 32768 ? Config.SAFE_ENCHANT_FULL_BODY : Config.SAFE_ENCHANT_COMMON;
    double chance;
    if (item.getEnchantLevel() < safeEnchantLevel) {
      chance = 100.0D;
    }
    else
    {
      double chance;
      if (itemType == 0)
      {
        chance = Config.ENCHANT_CHANCE_WEAPON;
      }
      else
      {
        double chance;
        if (itemType == 1) {
          chance = Config.ENCHANT_CHANCE_ARMOR;
        }
        else
        {
          double chance;
          if (itemType == 2) {
            chance = Config.ENCHANT_CHANCE_ACCESSORY;
          }
          else {
            player.sendPacket(EnchantResult.CANCEL);
            player.sendActionFailed();
            return;
          }
        }
      }
    }
    double chance;
    if (ItemFunctions.isDivineEnchantScroll(scrollId))
      chance = 100.0D;
    else if (ItemFunctions.isItemMallEnchantScroll(scrollId)) {
      chance += 10.0D;
    }
    if (catalyst != null) {
      chance += ItemFunctions.getCatalystPower(catalyst.getItemId());
    }
    if (scrollId == 13540)
      chance = Config.ENCHANT_CHANCE_MASTER_YOGI_STAFF;
    else if ((scrollId == 21581) || (scrollId == 21582)) {
      chance = Config.ENCHANT_CHANCE_CRYSTAL_ARMOR;
    }
    boolean equipped = false;
    if ((equipped = item.isEquipped()))
      inventory.unEquipItem(item);
    if (Rnd.chance(chance))
    {
      item.setEnchantLevel(item.getEnchantLevel() + 1);
      item.setJdbcState(JdbcEntityState.UPDATED);
      item.update();

      if (equipped) {
        inventory.equipItem(item);
      }
      player.sendPacket(new InventoryUpdate().addModifiedItem(item));

      player.sendPacket(EnchantResult.SUCESS);

      if (((scrollId == 13540) && (item.getEnchantLevel() > 3)) || (Config.SHOW_ENCHANT_EFFECT_RESULT))
        player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(player, player, 5965, 1, 500, 1500L) });
    }
    else if (ItemFunctions.isBlessedEnchantScroll(scrollId))
    {
      if (item.getEnchantLevel() < Config.SAFE_ENCHANT_BLESSED)
        item.setEnchantLevel(0);
      else {
        item.setEnchantLevel(Config.SAFE_ENCHANT_BLESSED);
      }
      item.setJdbcState(JdbcEntityState.UPDATED);
      item.update();

      if (equipped) {
        inventory.equipItem(item);
      }
      player.sendPacket(new InventoryUpdate().addModifiedItem(item));
      player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED);
      player.sendPacket(EnchantResult.BLESSED_FAILED);
    }
    else if ((ItemFunctions.isAncientEnchantScroll(scrollId)) || (ItemFunctions.isDestructionWpnEnchantScroll(scrollId)) || (ItemFunctions.isDestructionArmEnchantScroll(scrollId))) {
      player.sendPacket(EnchantResult.ANCIENT_FAILED);
    }
    else
    {
      if (item.isEquipped()) {
        player.sendDisarmMessage(item);
      }
      Log.LogItem(player, "EnchantFail", item);

      if (!inventory.destroyItem(item, 1L))
      {
        player.sendActionFailed();
        return;
      }

      if ((crystalId > 0) && (item.getTemplate().getCrystalCount() > 0))
      {
        int crystalAmount = (int)(item.getTemplate().getCrystalCount() * 0.87D);
        if (item.getEnchantLevel() > 3)
          crystalAmount = (int)(crystalAmount + item.getTemplate().getCrystalCount() * 0.25D * (item.getEnchantLevel() - 3));
        if (crystalAmount < 1) {
          crystalAmount = 1;
        }
        player.sendPacket(new EnchantResult(1, crystalId, crystalAmount));
        ItemFunctions.addItem(player, crystalId, crystalAmount, true);
      }
      else {
        player.sendPacket(EnchantResult.FAILED_NO_CRYSTALS);
      }
      if ((scrollId == 13540) || (Config.SHOW_ENCHANT_EFFECT_RESULT))
        player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(player, player, 5949, 1, 500, 1500L) });
    }
  }
}