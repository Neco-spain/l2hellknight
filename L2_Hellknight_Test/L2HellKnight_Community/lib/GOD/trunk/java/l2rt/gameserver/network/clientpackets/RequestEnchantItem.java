package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.config.ConfigSystem;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.PcInventory;
import l2rt.gameserver.network.serverpackets.EnchantResult;
import l2rt.gameserver.network.serverpackets.InventoryUpdate;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Log;
import l2rt.util.Rnd;

import java.util.logging.Logger;

public class RequestEnchantItem extends L2GameClientPacket
{
	protected static Logger _log = Logger.getLogger(RequestEnchantItem.class.getName());

	// Format: cd
	private int _objectId, _catalystObjId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_catalystObjId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl() || activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		L2ItemInstance itemToEnchant = inventory.getItemByObjectId(_objectId);
		L2ItemInstance catalyst = _catalystObjId > 0 ? inventory.getItemByObjectId(_catalystObjId) : null;
		L2ItemInstance scroll = activeChar.getEnchantScroll();
		activeChar.setEnchantScroll(null);

		if(itemToEnchant == null || scroll == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		// Затычка, ибо клиент криво обрабатывает RequestExTryToPutEnchantSupportItem
		if(!RequestExTryToPutEnchantSupportItem.checkCatalyst(itemToEnchant, catalyst))
			catalyst = null;

		// С помощью Master Yogi's Scroll: Enchant Weapon можно точить только Staff of Master Yogi
		if(scroll.getItemId() == 13540 && itemToEnchant.getItemId() != 13539 || itemToEnchant.getItemId() == 13539 && scroll.getItemId() != 13540)
		{
			activeChar.sendPacket(EnchantResult.CANCEL);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.sendActionFailed();
			return;
		}

		Log.add(activeChar.getName() + "|Trying to enchant|" + itemToEnchant.getItemId() + "|+" + itemToEnchant.getEnchantLevel() + "|" + itemToEnchant.getObjectId(), "enchants");

		// Затычка, разрешающая точить Staff of Master Yogi
		if(!itemToEnchant.canBeEnchanted() && !isYogiStaffEnchanting(scroll, itemToEnchant))
		{
			activeChar.sendPacket(EnchantResult.CANCEL);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.sendActionFailed();
			return;
		}

		if(itemToEnchant.getLocation() != L2ItemInstance.ItemLocation.INVENTORY && itemToEnchant.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL)
		{
			activeChar.sendPacket(EnchantResult.CANCEL);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(EnchantResult.CANCEL);
			activeChar.sendPacket(Msg.YOU_CANNOT_PRACTICE_ENCHANTING_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_MANUFACTURING_WORKSHOP);
			activeChar.sendActionFailed();
			return;
		}

		if(itemToEnchant.isStackable() || (scroll = inventory.getItemByObjectId(scroll.getObjectId())) == null)
		{
			activeChar.sendPacket(EnchantResult.CANCEL);
			activeChar.sendActionFailed();
			return;
		}

		int crystalId = itemToEnchant.getEnchantCrystalId(scroll, catalyst);

		// Затычка, разрешающая точить Staff of Master Yogi
		if(crystalId == 0 && !isYogiStaffEnchanting(scroll, itemToEnchant))
		{
			activeChar.sendPacket(EnchantResult.CANCEL);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.sendActionFailed();
			return;
		}

		// Staff of Master Yogi можно точить до 23
		if(!isYogiStaffEnchanting(scroll, itemToEnchant) && itemToEnchant.getEnchantLevel() >= Config.ENCHANT_MAX || isYogiStaffEnchanting(scroll, itemToEnchant) && itemToEnchant.getEnchantLevel() >= 23)
		{
			activeChar.sendPacket(EnchantResult.CANCEL);
			activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.RequestEnchantItem.MaxLevel", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		// Запрет на заточку чужих вещей, баг может вылезти на серверных лагах
		if(itemToEnchant.getOwnerId() != activeChar.getObjectId())
		{
			activeChar.sendPacket(EnchantResult.CANCEL);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.sendActionFailed();
			return;
		}

		L2ItemInstance removedScroll, removedCatalyst = null;
		synchronized (inventory)
		{
			removedScroll = inventory.destroyItem(scroll.getObjectId(), 1, true);
			if(catalyst != null)
				removedCatalyst = inventory.destroyItem(catalyst.getObjectId(), 1, true);
		}

		//tries enchant without scrolls
		if(removedScroll == null || catalyst != null && removedCatalyst == null)
		{
			activeChar.sendPacket(EnchantResult.CANCEL);
			activeChar.sendActionFailed();
			return;
		}

		int itemType = itemToEnchant.getItem().getType2();
		int safeEnchantLevel = itemToEnchant.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR ? Config.SAFE_ENCHANT_FULL_BODY : Config.SAFE_ENCHANT_COMMON;

		double chance;
		if(itemToEnchant.getEnchantLevel() < safeEnchantLevel)
			chance = 100;
		else if(itemType == L2Item.TYPE2_WEAPON)
			chance = removedScroll.isCrystallEnchantScroll() ? Config.ENCHANT_CHANCE_CRYSTAL_WEAPON : Config.ENCHANT_CHANCE_WEAPON;
		else if(itemType == L2Item.TYPE2_SHIELD_ARMOR)
			chance = removedScroll.isCrystallEnchantScroll() ? Config.ENCHANT_CHANCE_CRYSTAL_ARMOR : Config.ENCHANT_CHANCE_ARMOR;
		else if(itemType == L2Item.TYPE2_ACCESSORY)
			chance = removedScroll.isCrystallEnchantScroll() ? Config.ENCHANT_CHANCE_CRYSTAL_ACCESSORY : Config.ENCHANT_CHANCE_ACCESSORY;
		else
		{
			System.out.println("WTF? Request to enchant " + itemToEnchant.getItemId());
			activeChar.sendPacket(EnchantResult.CANCEL);
			activeChar.sendActionFailed();
			activeChar.sendPacket(Msg.SYSTEM_ERROR);
			inventory.addItem(removedScroll);
			return;
		}

		if(scroll.isDivineEnchantScroll()) // Item Mall divine
			chance = 100;
		else if(scroll.isItemMallEnchantScroll()) // Item Mall normal/ancient
			chance += 10;

		if(removedCatalyst != null)
			chance += removedCatalyst.getCatalystPower();

        if (ConfigSystem.getBoolean("AltEnchantFormulaForPvPServers")) {
            int enchlvl = itemToEnchant.getEnchantLevel();
            L2Item.Grade crystaltype = itemToEnchant.getItem().getCrystalType();

            //для уровнения шансов дуальщиков и остальных на победу в PvP вставка SA в дули халявная
            if (itemType == L2Item.TYPE2_WEAPON && itemToEnchant.getItemType() == L2Weapon.WeaponType.DUAL)
                safeEnchantLevel += 1;

            if (enchlvl < safeEnchantLevel)
                chance = 100;
            else if (enchlvl > 11)
                chance = 1;
            else {
                // Выборка базового шанса
                if (itemType == L2Item.TYPE2_WEAPON) {
                    L2Weapon wepToEnchant = (L2Weapon) itemToEnchant.getItem();
                    boolean magewep = itemType == L2Item.TYPE2_WEAPON && crystaltype.cry >= L2Item.CRYSTAL_C && wepToEnchant.getPDamage() - wepToEnchant.getMDamage() <= wepToEnchant.getPDamage() * 0.4;
                    chance = !magewep ? ConfigSystem.getInt("EChanceWeapon") : ConfigSystem.getInt("EChanceMageWeapon");

                    // Штраф на двуручное оружие(немагическое)
                    if (itemToEnchant.getItem().getBodyPart() == L2Item.SLOT_LR_HAND && itemToEnchant.getItem().getItemType() == L2Weapon.WeaponType.BLUNT && !magewep)
                        chance -= ConfigSystem.getInt("PenaltyforEChanceToHandBlunt");
                } else
                    chance = ConfigSystem.getInt("EChanceArmor");

                int DeltaChance = 15;

                // Основная прогрессия
                for (int i = safeEnchantLevel; i < enchlvl; i++) {
                    if (i == safeEnchantLevel + 2)
                        DeltaChance -= 5;
                    if (i == safeEnchantLevel + 6)
                        DeltaChance -= 5;
                    chance -= DeltaChance;
                }

                // Учёт грейда
                int Delta2 = 5;
                for (int in = 0x00; in < crystaltype.ordinal(); in++) {
                    if (in == L2Item.CRYSTAL_C)
                        Delta2 -= 5;
                    if (in == L2Item.CRYSTAL_B)
                        Delta2 -= 5;
                    if (in == L2Item.CRYSTAL_A)
                        Delta2 -= 2;
                    if (in == L2Item.CRYSTAL_S)
                        Delta2 -= 1;
                }
                chance += Delta2;

                if (scroll.isBlessedEnchantScroll())
                    chance += 2;
                if (chance < 1)
                    chance = 1;
            }
        }
		
		if(Rnd.chance(chance))
		{
			itemToEnchant.setEnchantLevel(itemToEnchant.getEnchantLevel() + 1);
			itemToEnchant.updateDatabase();

			activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToEnchant));

			Log.add(activeChar.getName() + "|Successfully enchanted|" + itemToEnchant.getItemId() + "|to+" + itemToEnchant.getEnchantLevel() + "|" + chance, "enchants");
			Log.LogItem(activeChar, Log.EnchantItem, itemToEnchant);
			activeChar.sendPacket(EnchantResult.SUCESS);

			if(itemToEnchant.getEnchantLevel() >= (itemType == L2Item.TYPE2_WEAPON ? 6 : 5) && ConfigSystem.getBoolean("Enchant6AnnounceToAll"))
			{
				activeChar.altUseSkill(SkillTable.getInstance().getInfo(21006, 1), activeChar);
				activeChar.broadcastPacket(new SystemMessage(SystemMessage.C1_HAS_SUCCESSFULY_ENCHANTED_A__S2_S3).addName(activeChar).addNumber(itemToEnchant.getEnchantLevel()).addItemName(itemToEnchant.getItemId()));
			}
		}
		else
		{
			Log.add(activeChar.getName() + "|Failed to enchant|" + itemToEnchant.getItemId() + "|+" + itemToEnchant.getEnchantLevel() + "|" + chance, "enchants");

			if(scroll.isBlessedEnchantScroll()) // фейл, но заточка блесед
			{
				itemToEnchant.setEnchantLevel(ConfigSystem.getInt("SafeEnchant"));
				activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToEnchant));
				activeChar.sendPacket(Msg.FAILED_IN_BLESSED_ENCHANT_THE_ENCHANT_VALUE_OF_THE_ITEM_BECAME_0);
				activeChar.sendPacket(EnchantResult.BLESSED_FAILED);
			}
			else if(scroll.isAncientEnchantScroll()) // фейл, но заточка ancient
				activeChar.sendPacket(EnchantResult.ANCIENT_FAILED);
			else
			// фейл, разбиваем вещь
			{
				if(itemToEnchant.isEquipped())
					inventory.unEquipItemInSlot(itemToEnchant.getEquipSlot());

				L2ItemInstance destroyedItem = inventory.destroyItem(itemToEnchant.getObjectId(), 1, true);
				if(destroyedItem == null)
				{
					_log.warning("failed to destroy " + itemToEnchant.getObjectId() + " after unsuccessful enchant attempt by char " + activeChar.getName());
					activeChar.sendActionFailed();
					return;
				}

				Log.LogItem(activeChar, Log.EnchantItemFail, itemToEnchant);

				if(crystalId > 0)
				{
					L2ItemInstance crystalsToAdd = ItemTemplates.getInstance().createItem(crystalId);

					int count = (int) (itemToEnchant.getItem().getCrystalCount() * 0.87);
					if(destroyedItem.getEnchantLevel() > 3)
						count += itemToEnchant.getItem().getCrystalCount() * 0.25 * (destroyedItem.getEnchantLevel() - 3);
					if(count < 1)
						count = 1;
					crystalsToAdd.setCount(count);

					inventory.addItem(crystalsToAdd);
					Log.LogItem(activeChar, Log.Sys_GetItem, crystalsToAdd);

					activeChar.sendPacket(new EnchantResult(1, crystalsToAdd.getItemId(), count), SystemMessage.obtainItems(crystalId, count, 0));
				}
				else
					activeChar.sendPacket(EnchantResult.FAILED_NO_CRYSTALS);

				activeChar.validateItemExpertisePenalties(false, destroyedItem.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR || destroyedItem.getItem().getType2() == L2Item.TYPE2_ACCESSORY, destroyedItem.getItem().getType2() == L2Item.TYPE2_WEAPON);
			}
		}

		activeChar.refreshOverloaded();
		activeChar.setEnchantScroll(null);
		activeChar.sendChanges();
	}

	private static boolean isYogiStaffEnchanting(L2ItemInstance scroll, L2ItemInstance itemToEnchant)
	{
		if(scroll.getItemId() == 13540 && itemToEnchant.getItemId() == 13539)
			return true;
		return false;
	}
}