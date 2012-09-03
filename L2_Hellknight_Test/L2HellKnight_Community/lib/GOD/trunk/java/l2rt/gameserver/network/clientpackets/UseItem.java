package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.entity.vehicle.L2AirShip;
import l2rt.gameserver.model.entity.vehicle.L2Vehicle;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ItemList;
import l2rt.gameserver.network.serverpackets.ShowCalc;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.EffectType;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2Armor;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.xml.ItemTemplates;

import java.nio.BufferUnderflowException;

public class UseItem extends L2GameClientPacket
{
	private int _objectId;
	private boolean ctrl_pressed;

	/**
	 * packet type id 0x19
	 * format:		cdd
	 */
	@Override
	public void readImpl()
	{
		try
		{
			_objectId = readD();
			ctrl_pressed = readD() == 1;
		}
		catch(BufferUnderflowException e)
		{
			e.printStackTrace();
			_log.info("Attention! Possible cheater found! Login:" + getClient().getLoginName());
		}
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		//Нельзя использовать любые предметы в тюрьме. 
		if(activeChar.getVar("jailed") != null) { 
			activeChar.sendMessage("You cannot use items in Jail."); 
			return; 
		}

		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(Msg.YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP, Msg.ActionFail);
			return;
		}

		synchronized (activeChar.getInventory())
		{
			L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

			if(item == null)
			{
				activeChar.sendActionFailed();
				return;
			}
			
			if (item.getItem().getItem_r() != 0)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(item.getItem().getItem_r(), 1);
				activeChar.getAI().Cast(skill, activeChar, false, true);
			}			
			
			int itemId = item.getItemId();
			if(itemId == 57)
			{
				activeChar.sendActionFailed();
				return;
			}

			if(activeChar.isFishing() && (itemId < 6535 || itemId > 6540))
			{
				// You cannot do anything else while fishing
				activeChar.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
				return;
			}

			if(activeChar.isDead())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
				return;
			}

			if(item.getItem().isForPet())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_EQUIP_A_PET_ITEM).addItemName(itemId));
				return;
			}

			// Маги не могут вызывать Baby Buffalo Improved
			if(Config.ALT_IMPROVED_PETS_LIMITED_USE && activeChar.isMageClass() && item.getItemId() == 10311)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
				return;
			}

			// Войны не могут вызывать Improved Baby Kookaburra
			if(Config.ALT_IMPROVED_PETS_LIMITED_USE && !activeChar.isMageClass() && item.getItemId() == 10313)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
				return;
			}

			if(item.isEquipable())
			{
				if(activeChar.getEffectList().getEffectByType(EffectType.Disarm) != null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
					return;
				}

				if(activeChar.isCastingNow())
				{
					activeChar.sendPacket(Msg.YOU_MAY_NOT_EQUIP_ITEMS_WHILE_CASTING_OR_PERFORMING_A_SKILL);
					return;
				}

				// Нельзя снимать/одевать любое снаряжение при этих условиях
				if(activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAlikeDead())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
					return;
				}

				int bodyPart = item.getBodyPart();

				if(bodyPart == L2Item.SLOT_LR_HAND || bodyPart == L2Item.SLOT_L_HAND || bodyPart == L2Item.SLOT_R_HAND)
				{
					// Нельзя снимать/одевать оружие, сидя на пете
					if(activeChar.isMounted())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
						return;
					}

					// Нельзя снимать/одевать проклятое оружие и флаги
					if(activeChar.isCursedWeaponEquipped() || activeChar.isCombatFlagEquipped() || activeChar.isTerritoryFlagEquipped())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
						return;
					}

					// Нельзя одевать/снимать оружие/щит/сигил, управляя кораблем
					L2Vehicle vehicle = activeChar.getVehicle();
					if(vehicle != null && vehicle.isAirShip())
					{
						L2AirShip airship = (L2AirShip) vehicle;
						if(airship.getDriver() == activeChar)
						{
							activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
							return;
						}
					}
				}

				// Нельзя снимать/одевать проклятое оружие
				if(item.isCursed())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
					return;
				}

				if((item.getCustomFlags() & L2ItemInstance.FLAG_NO_UNEQUIP) == L2ItemInstance.FLAG_NO_UNEQUIP)
				{
					activeChar.sendActionFailed();
					return;
				}

				// Don't allow weapon/shield hero equipment during Olympiads
				if(activeChar.isInOlympiadMode() && item.isHeroWeapon())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(item.isEquipped())
				{
					activeChar.getInventory().unEquipItemInBodySlotAndNotify(item.getBodyPart(), item);
					return;
				}

				activeChar.getInventory().equipItem(item, true);
				if(!item.isEquipped())
					return;

				SystemMessage sm;
				if(item.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessage.EQUIPPED__S1_S2);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(itemId);
				}
				else
					sm = new SystemMessage(SystemMessage.YOU_HAVE_EQUIPPED_YOUR_S1).addItemName(itemId);
				activeChar.sendPacket(sm);
				if(item != null)
					activeChar.validateItemExpertisePenalties(false, item.getItem() instanceof L2Armor, item.getItem() instanceof L2Weapon);

				if(item.getItem().getType2() == L2Item.TYPE2_ACCESSORY || item.getItem().isTalisman())
				{
					activeChar.sendUserInfo(true);
					// TODO убрать, починив предварительно отображение бижы
					activeChar.sendPacket(new ItemList(activeChar, false));
				}
				else
					activeChar.broadcastUserInfo(true);
				return;
			}

			if(itemId == 4393)
			{
				activeChar.sendPacket(new ShowCalc(itemId));
				return;
			}

			if(ItemTemplates.useHandler(activeChar, item, ctrl_pressed))
				return;

			IItemHandler handler = ItemHandler.getInstance().getItemHandler(itemId);
			if(handler != null)
				handler.useItem(activeChar, item, ctrl_pressed);
		}
	}
}