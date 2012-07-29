package l2p.gameserver.clientpackets;


import l2p.commons.dao.JdbcEntityState;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.EnchantItemHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.EnchantResult;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.item.support.EnchantScroll;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Log;

public class RequestEnchantItem extends L2GameClientPacket {
    private int _objectId, _catalystObjId;

    @Override
    protected void readImpl() {
        _objectId = readD();
        _catalystObjId = readD();
    }

    @Override
    protected void runImpl() {
        Player player = getClient().getActiveChar();
        if (player == null)
            return;

        if (player.isActionsDisabled()) {
            player.setEnchantScroll(null);
            player.sendActionFailed();
            return;
        }

        if (player.isInTrade()) {
            player.setEnchantScroll(null);
            player.sendActionFailed();
            return;
        }

        if (player.isInStoreMode()) {
            player.setEnchantScroll(null);
            player.sendPacket(EnchantResult.CANCEL);
            player.sendPacket(SystemMsg.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
            player.sendActionFailed();
            return;
        }

        PcInventory inventory = player.getInventory();
        inventory.writeLock();
        try {
            ItemInstance item = inventory.getItemByObjectId(_objectId);
            ItemInstance catalyst = _catalystObjId > 0 ? inventory.getItemByObjectId(_catalystObjId) : null;
            ItemInstance scroll = player.getEnchantScroll();

            if (item == null || scroll == null) {
                player.sendActionFailed();
                return;
            }

            EnchantScroll enchantScroll = EnchantItemHolder.getInstance().getEnchantScroll(scroll.getItemId());
            if (enchantScroll == null) {
                doEnchantOld(player, item, scroll, catalyst);
                return;
            }

            if (enchantScroll.getMaxEnchant() != -1 && item.getEnchantLevel() >= enchantScroll.getMaxEnchant()) {
                player.sendPacket(EnchantResult.CANCEL);
                player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
                player.sendActionFailed();
                return;
            }

            if (enchantScroll.getItems().size() > 0) {
                if (!enchantScroll.getItems().contains(item.getItemId())) {
                    player.sendPacket(EnchantResult.CANCEL);
                    player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
                    player.sendActionFailed();
                    return;
                }
            } else {
                if (!enchantScroll.getGrades().contains(item.getCrystalType())) {
                    player.sendPacket(EnchantResult.CANCEL);
                    player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
                    player.sendActionFailed();
                    return;
                }
            }

            if (!item.canBeEnchanted(false)) {
                player.sendPacket(EnchantResult.CANCEL);
                player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
                player.sendActionFailed();
                return;
            }

            if (!inventory.destroyItem(scroll, 1L) || catalyst != null && !inventory.destroyItem(catalyst, 1L)) {
                player.sendPacket(EnchantResult.CANCEL);
                player.sendActionFailed();
                return;
            }

            boolean equipped;
            if (equipped = item.isEquipped())
                inventory.unEquipItem(item);

            int safeEnchantLevel = item.getTemplate().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR ? 4 : 3;

            int chance = enchantScroll.getChance();
            if (item.getEnchantLevel() < safeEnchantLevel)
                chance = 100;

            if (Rnd.chance(chance)) {
                item.setEnchantLevel(item.getEnchantLevel() + 1);
                item.setJdbcState(JdbcEntityState.UPDATED);
                item.update();

                if (equipped)
                    inventory.equipItem(item);

                player.sendPacket(new InventoryUpdate().addModifiedItem(item));

                player.sendPacket(new EnchantResult(0, 0, 0, item.getEnchantLevel()));

                if (enchantScroll.isHasVisualEffect() && item.getEnchantLevel() > 3)
                    player.broadcastPacket(new MagicSkillUse(player, player, 0, 5965, 1, 500, 1500, -1));
            } else {
                switch (enchantScroll.getResultType()) {
                    case CRYSTALS:
                        if (item.isEquipped())
                            player.sendDisarmMessage(item);

                        Log.LogItem(player, Log.EnchantFail, item);

                        if (!inventory.destroyItem(item, 1L)) {
                            player.sendActionFailed();
                            return;
                        }

                        int crystalId = item.getCrystalType().cry;
                        if (crystalId > 0 && item.getTemplate().getCrystalCount() > 0) {
                            int crystalAmount = (int) (item.getTemplate().getCrystalCount() * 0.87);
                            if (item.getEnchantLevel() > 3)
                                crystalAmount += item.getTemplate().getCrystalCount() * 0.25 * (item.getEnchantLevel() - 3);
                            if (crystalAmount < 1)
                                crystalAmount = 1;

                            player.sendPacket(new EnchantResult(1, crystalId, crystalAmount, 0));
                            ItemFunctions.addItem(player, crystalId, crystalAmount, true);
                        } else
                            player.sendPacket(EnchantResult.FAILED_NO_CRYSTALS);

                        if (enchantScroll.isHasVisualEffect())
                            player.broadcastPacket(new MagicSkillUse(player, player, 0, 5949, 1, 500, 1500, -1));
                        break;
                    case DROP_ENCHANT:
                        item.setEnchantLevel(0);
                        item.setJdbcState(JdbcEntityState.UPDATED);
                        item.update();

                        if (equipped)
                            inventory.equipItem(item);

                        player.sendPacket(new InventoryUpdate().addModifiedItem(item));
                        player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED);
                        player.sendPacket(EnchantResult.BLESSED_FAILED);
                        break;
                    case NOTHING:
                        player.sendPacket(EnchantResult.ANCIENT_FAILED);
                        break;
                }
            }
        } finally {
            inventory.writeUnlock();

            player.setEnchantScroll(null);
            player.updateStats();
        }
    }

    @Deprecated
    private static void doEnchantOld(Player player, ItemInstance item, ItemInstance scroll, ItemInstance catalyst) {
        PcInventory inventory = player.getInventory();
        // Затычка, ибо клиент криво обрабатывает RequestExTryToPutEnchantSupportItem
        if (!ItemFunctions.checkCatalyst(item, catalyst))
            catalyst = null;

        if (!item.canBeEnchanted(true)) {
            player.sendPacket(EnchantResult.CANCEL);
            player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
            player.sendActionFailed();
            return;
        }

        int crystalId = ItemFunctions.getEnchantCrystalId(item, scroll, catalyst);

        if (crystalId == -1) {
            player.sendPacket(EnchantResult.CANCEL);
            player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
            player.sendActionFailed();
            return;
        }

        int scrollId = scroll.getItemId();

        if (scrollId == 13540 && item.getItemId() != 13539) {
            player.sendPacket(EnchantResult.CANCEL);
            player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
            player.sendActionFailed();
            return;
        }

        // ольф 21580(21581/21582)
        if ((scrollId == 21581 || scrollId == 21582) && item.getItemId() != 21580) {
            player.sendPacket(EnchantResult.CANCEL);
            player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
            player.sendActionFailed();
            return;
        }

        // TODO: [pchayka] временный хардкод до улучения системы описания свитков заточки
        if (ItemFunctions.isDestructionWpnEnchantScroll(scrollId) && item.getEnchantLevel() >= 15 || ItemFunctions.isDestructionArmEnchantScroll(scrollId) && item.getEnchantLevel() >= 6) {
            player.sendPacket(EnchantResult.CANCEL);
            player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
            player.sendActionFailed();
            return;
        }

        boolean fail = false;
        switch (item.getItemId()) {
            case 13539:
                if (item.getEnchantLevel() >= Config.ENCHANT_MAX_MASTER_YOGI_STAFF)
                    fail = true;
                break;
            case 21580:
                if (item.getEnchantLevel() >= 9)
                    fail = true;
                break;
            default:
                if (item.getEnchantLevel() >= Config.ENCHANT_MAX)
                    fail = true;
                break;
        }

        if (!inventory.destroyItem(scroll, 1L) || catalyst != null && !inventory.destroyItem(catalyst, 1L)) {
            player.sendPacket(EnchantResult.CANCEL);
            player.sendActionFailed();
            return;
        }

        if (fail) {
            player.sendPacket(EnchantResult.CANCEL);
            player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
            player.sendActionFailed();
            return;
        }

        int itemType = item.getTemplate().getType2();
        int safeEnchantLevel = item.getTemplate().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR ? Config.SAFE_ENCHANT_FULL_BODY : Config.SAFE_ENCHANT_COMMON;

        double chance;
        if (item.getEnchantLevel() < safeEnchantLevel)
            chance = 100;
        else if (itemType == ItemTemplate.TYPE2_WEAPON) {
            chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? Config.ENCHANT_CHANCE_CRYSTAL_WEAPON : Config.ENCHANT_CHANCE_WEAPON;
            /*int itemGrade = item.getTemplate().getItemGrade().cry;
                               //TODO: Добавить флаг на is_mage_weapon и раскомминтить
                               WeaponTemplate wepToEnchant = (WeaponTemplate) item.getTemplate();
                               boolean magewep = (itemGrade >= ItemTemplate.CRYSTAL_C) && (wepToEnchant.getPDamage() - wepToEnchant.getMDamage() <= wepToEnchant.getPDamage() * 0.4D);
                               if(magewep)
                                   //TODO шанс сверить с оффовскими сурсами...
                                   chance *= 0.67; // Шанс заточки магического оружия равняется 2/3 шанса заточки воинского оружия.*/
        } else if (itemType == ItemTemplate.TYPE2_SHIELD_ARMOR)
            chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? Config.ENCHANT_CHANCE_CRYSTAL_ARMOR : Config.ENCHANT_CHANCE_ARMOR;
        else if (itemType == ItemTemplate.TYPE2_ACCESSORY)
            chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? Config.ENCHANT_CHANCE_CRYSTAL_ACCESSORY : Config.ENCHANT_CHANCE_ACCESSORY;
        else {
            player.sendPacket(EnchantResult.CANCEL);
            player.sendActionFailed();
            return;
        }

        if (ItemFunctions.isDivineEnchantScroll(scrollId)) // Item Mall divine
            chance = 100;
        else if (ItemFunctions.isItemMallEnchantScroll(scrollId)) // Item Mall normal/ancient
            chance += 10;

        if (catalyst != null)
            chance += ItemFunctions.getCatalystPower(catalyst.getItemId());

        if (scrollId == 13540)
            chance = item.getEnchantLevel() < Config.SAFE_ENCHANT_MASTER_YOGI_STAFF ? 100 : Config.ENCHANT_CHANCE_MASTER_YOGI_STAFF;
        else if (scrollId == 21581 || scrollId == 21582)
            chance = item.getEnchantLevel() < 3 ? 100 : Config.ENCHANT_CHANCE_CRYSTAL_ARMOR;

        boolean equipped = false;
        if (equipped = item.isEquipped())
            inventory.unEquipItem(item);
        if (Rnd.chance(chance)) {
            item.setEnchantLevel(item.getEnchantLevel() + 1);
            item.setJdbcState(JdbcEntityState.UPDATED);
            item.update();

            if (equipped)
                inventory.equipItem(item);

            player.sendPacket(new InventoryUpdate().addModifiedItem(item));

            player.sendPacket(new EnchantResult(0, 0, 0, item.getEnchantLevel()));

            if (scrollId == 13540 && item.getEnchantLevel() > 3 || Config.SHOW_ENCHANT_EFFECT_RESULT)
                player.broadcastPacket(new MagicSkillUse(player, player, 0, 5965, 1, 500, 1500, -1));
        } else if (ItemFunctions.isBlessedEnchantScroll(scrollId)) // фейл, но заточка блесед
        {
            item.setEnchantLevel(0);
            item.setJdbcState(JdbcEntityState.UPDATED);
            item.update();

            if (equipped)
                inventory.equipItem(item);

            player.sendPacket(new InventoryUpdate().addModifiedItem(item));
            player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED);
            player.sendPacket(EnchantResult.BLESSED_FAILED);
        } else if (ItemFunctions.isAncientEnchantScroll(scrollId) || ItemFunctions.isDestructionWpnEnchantScroll(scrollId) || ItemFunctions.isDestructionArmEnchantScroll(scrollId)) // фейл, но заточка ancient или destruction
            player.sendPacket(EnchantResult.ANCIENT_FAILED);
        else
        // фейл, разбиваем вещь
        {
            if (item.isEquipped())
                player.sendDisarmMessage(item);

            Log.LogItem(player, Log.EnchantFail, item);

            if (!inventory.destroyItem(item, 1L)) {
                //TODO audit
                player.sendActionFailed();
                return;
            }

            if (crystalId > 0 && item.getTemplate().getCrystalCount() > 0) {
                int crystalAmount = (int) (item.getTemplate().getCrystalCount() * 0.87);
                if (item.getEnchantLevel() > 3)
                    crystalAmount += item.getTemplate().getCrystalCount() * 0.25 * (item.getEnchantLevel() - 3);
                if (crystalAmount < 1)
                    crystalAmount = 1;

                player.sendPacket(new EnchantResult(1, crystalId, crystalAmount, 0));
                ItemFunctions.addItem(player, crystalId, crystalAmount, true);
            } else
                player.sendPacket(EnchantResult.FAILED_NO_CRYSTALS);

            if (scrollId == 13540 || Config.SHOW_ENCHANT_EFFECT_RESULT)
                player.broadcastPacket(new MagicSkillUse(player, player, 0, 5949, 1, 500, 1500, -1));
        }
    }
}