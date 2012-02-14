package l2rt.gameserver.network.clientpackets;

import l2rt.config.ConfigSystem;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.PcInventory;
import l2rt.gameserver.network.serverpackets.InventoryUpdate;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.templates.L2Item;
import l2rt.util.Log;
import l2rt.util.Rnd;

/**
 * @author SYS
 * Format: d
 */
public class RequestEnchantItemAttribute extends L2GameClientPacket
{
	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_objectId == -1)
		{
			activeChar.setEnchantScroll(null);
			activeChar.sendPacket(Msg.ELEMENTAL_POWER_ENCHANCER_USAGE_HAS_BEEN_CANCELLED);
			return;
		}

		if(activeChar.isOutOfControl() || activeChar.isActionsDisabled())
		{
            activeChar.setEnchantScroll(null);
			activeChar.sendActionFailed();
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		L2ItemInstance itemToEnchant = inventory.getItemByObjectId(_objectId);
		L2ItemInstance stone = activeChar.getEnchantScroll();
        byte stoneElement = stone.getEnchantAttributeStoneElement(false);
        byte oppElement = stone.getEnchantAttributeStoneElement(true);
		activeChar.setEnchantScroll(null);

		if(itemToEnchant == null || stone == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		L2Item item = itemToEnchant.getItem();

		if(!itemToEnchant.canBeEnchanted() || item.getCrystalType().cry < L2Item.CRYSTAL_S)
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, Msg.ActionFail);
			return;
		}

		if(itemToEnchant.getLocation() != L2ItemInstance.ItemLocation.INVENTORY && itemToEnchant.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL)
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, Msg.ActionFail);
			return;
		}

		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP, Msg.ActionFail);
			return;
		}

		if(itemToEnchant.isStackable() || (stone = inventory.getItemByObjectId(stone.getObjectId())) == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		/*int itemType = item.getType2();

		if(itemToEnchant.getAttackAttributeElement() != L2Item.ATTRIBUTE_NONE && itemToEnchant.getAttackAttributeElement() != stone.getEnchantAttributeStoneElement(itemType == L2Item.TYPE2_SHIELD_ARMOR))
		{
			activeChar.sendPacket(Msg.ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED, Msg.ActionFail);
			return;
		}*/

		if(item.isUnderwear() || item.isCloak() || item.isBracelet() || item.isBelt() || item.isPvP())
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, Msg.ActionFail);
			return;
		}

        if(item.isWeapon() && itemToEnchant.getWeaponElementLevel() > stone.getAttributeElementLevel()
                || item.isArmor() && itemToEnchant.getArmorAttributeLevel()[oppElement] > stone.getAttributeElementLevel()) {
            activeChar.sendPacket(new SystemMessage(SystemMessage.ELEMENTAL_POWER_ENCHANCER_USAGE_REQUIREMENT_IS_NOT_SUFFICIENT));
            activeChar.sendActionFailed();
            return;
        }

        int minValue = 0;
        int maxValue = item.isWeapon() ? 150 : 60;
        int maxValueCrystal = item.isWeapon() ? 300 : 120;

        int[] deffAttr = itemToEnchant.getDeffAttr();

        if(item.isArmor() && deffAttr[stoneElement] != 0) {// проверка на энчат противоположных элементов
            activeChar.sendPacket(new SystemMessage(3117));
            activeChar.sendActionFailed();
            return;
        }

        int attrValue = item.isWeapon() ? itemToEnchant.getAttackElementValue() : itemToEnchant.getElementDefAttr(oppElement);
        if (stone.isAttributeCrystal() && attrValue < maxValue) {
            activeChar.sendPacket(new SystemMessage(SystemMessage.ELEMENTAL_POWER_ENCHANCER_USAGE_REQUIREMENT_IS_NOT_SUFFICIENT));
            activeChar.sendActionFailed();
            return;
        } else if (!stone.isAttributeCrystal() && attrValue >= maxValue) {
            activeChar.sendPacket(new SystemMessage(SystemMessage.ELEMENTAL_POWER_ENCHANCER_USAGE_REQUIREMENT_IS_NOT_SUFFICIENT));
            activeChar.sendActionFailed();
            return;
        }
        if (attrValue >= maxValueCrystal || attrValue < minValue) {
            activeChar.sendPacket(new SystemMessage(SystemMessage.ELEMENTAL_POWER_ENCHANCER_USAGE_REQUIREMENT_IS_NOT_SUFFICIENT));
            activeChar.sendActionFailed();
            return;
        }
		// Запрет на заточку чужих вещей, баг может вылезти на серверных лагах
		if(itemToEnchant.getOwnerId() != activeChar.getObjectId())
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS, Msg.ActionFail);
			return;
		}

		Log.add(activeChar.getName() + "|Trying to attribute enchant|" + itemToEnchant.getItemId() + "|attribute:" + stone.getEnchantAttributeStoneElement(item.getType2() == L2Item.TYPE2_SHIELD_ARMOR) + "|" + itemToEnchant.getObjectId(), "enchants");

		L2ItemInstance removedStone;
		synchronized (inventory)
		{
			removedStone = inventory.destroyItem(stone.getObjectId(), 1, true);
		}

		if(removedStone == null)
		{
			activeChar.sendActionFailed();
			return;
		}

        if (Rnd.chance(stone.isAttributeCrystal() ? ConfigSystem.getInt("EnchantAttributeCrystalChance") : ConfigSystem.getInt("EnchantAttributeChance"))) {
            if (item.isWeapon()) {
                if (itemToEnchant.getEnchantLevel() == 0) {
                    SystemMessage sm = new SystemMessage(SystemMessage.S2_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO_S1);
                    sm.addItemName(item.getItemId());
                  //  sm.addElementName(stoneElement);
                    activeChar.sendPacket(sm);
                } else {
                    SystemMessage sm = new SystemMessage(SystemMessage.S3_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO__S1S2);
                    sm.addNumber(itemToEnchant.getEnchantLevel());
                    sm.addItemName(item.getItemId());
                 //   sm.addElementName(stoneElement);
                    activeChar.sendPacket(sm);
                }
            } else {
                if (itemToEnchant.getEnchantLevel() == 0) {
                    SystemMessage sm = new SystemMessage(3144);
                    sm.addItemName(item.getItemId());
                  //  sm.addElementName(stoneElement);
                  //  sm.addElementName(oppElement);
                    activeChar.sendPacket(sm);
                } else {
                    SystemMessage sm = new SystemMessage(3163);
                    sm.addNumber(itemToEnchant.getEnchantLevel());
                    sm.addItemName(item.getItemId());
                 //   sm.addElementName(stoneElement);
                 //   sm.addElementName(oppElement);
                    activeChar.sendPacket(sm);
                }
            }

            int value = item.isWeapon()? 5 : 6;

            // Для оружия 1й камень дает +20 атрибута
            if (itemToEnchant.getAttackElementValue() == 0 && item.isWeapon())
                value = 20;

            byte attackElement = itemToEnchant.getAttackAttributeElement();
            int attackElementValue = itemToEnchant.getAttackElementValue();
            if (item.isArmor()) {
                deffAttr[oppElement] = itemToEnchant.getElementDefAttr(oppElement) + value;
            } else if (item.isWeapon()) {
                attackElement = stoneElement;
                attackElementValue += value;
            }
            itemToEnchant.setAttributeElement(attackElement, attackElementValue, deffAttr, true);
            activeChar.getInventory().refreshListeners();

            activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToEnchant));

            //Log.add(player.getName() + "|Successfully enchanted by attribute|" + item.getItemId() + "|to+" + item.getAttackElementValue() + "|" + Config.ENCHANT_ATTRIBUTE_CHANCE, "enchants");
            Log.LogItem(activeChar, Log.EnchantItem, itemToEnchant);
        } else {
            activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_ADD_ELEMENTAL_POWER));
            //Log.add(player.getName() + "|Failed to enchant attribute|" + item.getItemId() + "|+" + item.getAttackElementValue() + "|" + Config.ENCHANT_ATTRIBUTE_CHANCE, "enchants");
        }

		activeChar.sendChanges();
	}
}