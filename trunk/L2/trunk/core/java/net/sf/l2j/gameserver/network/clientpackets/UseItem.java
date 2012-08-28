//L2E
package net.sf.l2j.gameserver.network.clientpackets;

import java.util.Arrays;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.ShowCalculator;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.FloodProtector;

public final class UseItem extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(UseItem.class.getName());
	private static final String _C__14_USEITEM = "[C] 14 UseItem";

	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{

		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
            return;

		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

		activeChar.cancelActiveTrade();

		if (activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			activeChar.sendPacket(new ActionFailed());
			return;
		}
		
		if (activeChar.getActiveTradeList() != null) 
		{ 
			activeChar.sendMessage("You can't use items while trading!"); 
			return; 
		} 
		// NOTE: disabled due to deadlocks
		//synchronized (activeChar.getInventory())
		//{

		if (item == null)
			return;
			
		// Flood protect UseItem
		if (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_USEITEM))
			return;

		if (item.isWear())
		{
			// No unequipping wear-items
			return;
		}

			int itemId = item.getItemId();
			/*
			 * Alt game - Karma punishment // SOE
			 * 736  	Scroll of Escape
			 * 1538  	Blessed Scroll of Escape
			 * 1829  	Scroll of Escape: Clan Hall
			 * 1830  	Scroll of Escape: Castle
			 * 3958  	L2Day - Blessed Scroll of Escape
			 * 5858  	Blessed Scroll of Escape: Clan Hall
			 * 5859  	Blessed Scroll of Escape: Castle
			 * 6663  	Scroll of Escape: Orc Village
			 * 6664  	Scroll of Escape: Silenos Village
			 * 7117  	Scroll of Escape to Talking Island
			 * 7118  	Scroll of Escape to Elven Village
			 * 7119  	Scroll of Escape to Dark Elf Village
			 * 7120  	Scroll of Escape to Orc Village
			 * 7121  	Scroll of Escape to Dwarven Village
			 * 7122  	Scroll of Escape to Gludin Village
			 * 7123  	Scroll of Escape to the Town of Gludio
			 * 7124  	Scroll of Escape to the Town of Dion
			 * 7125  	Scroll of Escape to Floran
			 * 7126  	Scroll of Escape to Giran Castle Town
			 * 7127  	Scroll of Escape to Hardin's Private Academy
			 * 7128  	Scroll of Escape to Heine
			 * 7129  	Scroll of Escape to the Town of Oren
			 * 7130  	Scroll of Escape to Ivory Tower
			 * 7131  	Scroll of Escape to Hunters Village
			 * 7132  	Scroll of Escape to Aden Castle Town
			 * 7133  	Scroll of Escape to the Town of Goddard
			 * 7134  	Scroll of Escape to the Rune Township
			 * 7135  	Scroll of Escape to the Town of Schuttgart.
			 * 7554  	Scroll of Escape to Talking Island
			 * 7555  	Scroll of Escape to Elven Village
			 * 7556  	Scroll of Escape to Dark Elf Village
			 * 7557  	Scroll of Escape to Orc Village
			 * 7558  	Scroll of Escape to Dwarven Village
			 * 7559  	Scroll of Escape to Giran Castle Town
			 * 7618  	Scroll of Escape - Ketra Orc Village
			 * 7619  	Scroll of Escape - Varka Silenos Village
			 */
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0
				&& (itemId == 736 || itemId == 1538 || itemId == 1829 || itemId == 1830
				|| itemId == 3958 || itemId == 5858 || itemId == 5859 || itemId == 6663
				|| itemId == 6664 || (itemId >= 7117 && itemId <= 7135)
				|| (itemId >= 7554 && itemId <= 7559) || itemId == 7618 || itemId == 7619))
				return;

			// Items that cannot be used
			if (itemId == 57)
                return;

            if (activeChar.isFishing() && (itemId < 6535 || itemId > 6540))
            {
                // You cannot do anything else while fishing
                SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
                getClient().getActiveChar().sendPacket(sm);
                sm = null;
                return;
            }

			// Char cannot use item when dead
			if (activeChar.isDead())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addItemName(itemId);
				getClient().getActiveChar().sendPacket(sm);
				sm = null;
				return;
			}

			// Char cannot use pet items
			if (item.getItem().isForWolf() || item.getItem().isForHatchling()
				|| item.getItem().isForStrider() || item.getItem().isForBabyPet())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_EQUIP_PET_ITEM); // You cannot equip a pet item.
				sm.addItemName(itemId);
				getClient().getActiveChar().sendPacket(sm);
				sm = null;
				return;
			}
			
			// Tanks cant use Bows - by Blackmouse
			if (Config.BOWTANK_PENALTY)
			{
				int classid = activeChar.getClassId().getId();
				if (!activeChar.isInOlympiadMode() && (classid == 88 || classid == 89 || classid == 6 || classid == 90 || classid == 91 || classid == 100 || classid == 99 || classid == 113 || classid == 114))
				{
					if (item.getItemType() == L2WeaponType.BOW)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
						sm.addItemName(itemId);
						getClient().getActiveChar().sendPacket(sm);
						sm = null;
						return;
					}
				}
			}

			if (Config.DEBUG)
                _log.finest(activeChar.getObjectId() + ": use item " + _objectId);

			if (item.isEquipable())
			{
				// No unequipping/equipping while the player is in special conditions
				if (activeChar.isStunned() || activeChar.isSleeping() || activeChar.isMeditation() || activeChar.isParalyzed()
						|| activeChar.isAlikeDead())
				{
					activeChar.sendMessage("Your status does not allow you to do that.");
					return;
				}

				int bodyPart = item.getItem().getBodyPart();
                if ((activeChar.isAttackingNow() || activeChar.isCastingNow() || activeChar.isMounted() || (activeChar._inEventCTF && activeChar._haveFlagCTF))
                        && (bodyPart == L2Item.SLOT_LR_HAND 
                            || bodyPart == L2Item.SLOT_L_HAND 
                            || bodyPart == L2Item.SLOT_R_HAND))
                {
                    if (activeChar._inEventCTF && activeChar._haveFlagCTF)
                    	activeChar.sendMessage("This item can not be equipped when you have the flag.");
                	return;
                }

				/////////////////////////// Фикс бага с лайф стонами
				if (bodyPart == L2Item.SLOT_L_HAND || bodyPart == L2Item.SLOT_R_HAND || bodyPart == L2Item.SLOT_LR_HAND)
				{
					if (activeChar.getInventory().getPaperdollItemByL2ItemId(0x4000) != null && activeChar.getInventory().getPaperdollItemByL2ItemId(0x4000).getAugmentation() !=null)
					{
						activeChar.getInventory().getPaperdollItemByL2ItemId(0x4000).getAugmentation().removeBoni(activeChar);
					}
				}

				L2Effect[] effects = activeChar.getAllEffects();

				for (L2Effect e : effects)
				{
					if ((e.getSkill().getSkillType() == L2Skill.SkillType.CONT ||
						 e.getSkill().getSkillType() == L2Skill.SkillType.BUFF ||
						 e.getSkill().getSkillType() == L2Skill.SkillType.HEAL_PERCENT ||
						 e.getSkill().getSkillType() == L2Skill.SkillType.REFLECT)
						 && ((e.getSkill().getId() >= 3124 && e.getSkill().getId() <= 3259) ||
							 // e.getSkill().getId() == 222 || 
							  e.getSkill().getId() == 422)
						 && (bodyPart == L2Item.SLOT_LR_HAND
						 || bodyPart == L2Item.SLOT_L_HAND
						 || bodyPart == L2Item.SLOT_R_HAND))
					{
						activeChar.stopSkillEffects(e.getSkill().getId());
						break;
					}
				}
				///////////////////////////

                // Don't allow weapon/shield equipment if wearing formal wear
                if (activeChar.isWearingFormalWear()
                	&& (bodyPart == L2Item.SLOT_LR_HAND
                            || bodyPart == L2Item.SLOT_L_HAND
                            || bodyPart == L2Item.SLOT_R_HAND))
                {
        				SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR);
        				activeChar.sendPacket(sm);
                        return;
                }
				
                if (activeChar.isCursedWeaponEquiped()
                		&& ((bodyPart == L2Item.SLOT_LR_HAND
                				|| bodyPart == L2Item.SLOT_L_HAND
                				|| bodyPart == L2Item.SLOT_R_HAND)
                		|| itemId == 6408)) // Don't allow to put formal wear
                {
                	return;
                }
				
				if (activeChar.isInOlympiadMode() && (item.isHeroItem() || item.isOlyRestrictedItem()))
				{
					return;
				}
                if (activeChar.isInOlympiadMode() && ((item.getItemId() >= 6611 && item.getItemId() <= 6621) || item.getItemId() == 6842)) return;

                L2ItemInstance[] items = null;
                boolean isEquiped = item.isEquipped();
	            SystemMessage sm = null;
	            L2ItemInstance old = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
	            if (old == null)
	            	old = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);

	            activeChar.checkSSMatch(item, old);

	            if (isEquiped)
                {
		            if (item.getEnchantLevel() > 0)
		            {
		            	sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
		            	sm.addNumber(item.getEnchantLevel());
		            	sm.addItemName(itemId);
		            }
		            else
		            {
			            sm = new SystemMessage(SystemMessageId.S1_DISARMED);
			            sm.addItemName(itemId);
		            }
		            activeChar.sendPacket(sm);

		            // Remove augementation boni on unequip
		            if (item.isAugmented())
		            	item.getAugmentation().removeBoni(activeChar);

		            int slot = activeChar.getInventory().getSlotFromItem(item);
                	items = activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
                }
                else
                {
                	int tempBodyPart = item.getItem().getBodyPart();
                	L2ItemInstance tempItem = activeChar.getInventory().getPaperdollItemByL2ItemId(tempBodyPart);

                	// remove augmentation stats for replaced items
                	// currently weapons only..
                	if (tempItem != null && tempItem.isAugmented())
                		tempItem.getAugmentation().removeBoni(activeChar);
                	else if (tempBodyPart == 0x4000)
                	{
                		L2ItemInstance tempItem2 = activeChar.getInventory().getPaperdollItem(7);
                		if (tempItem2 != null && tempItem2.isAugmented())
                			tempItem2.getAugmentation().removeBoni(activeChar);
                		tempItem2 = activeChar.getInventory().getPaperdollItem(8);
                    	if (tempItem2 != null && tempItem2.isAugmented())
                    		tempItem2.getAugmentation().removeBoni(activeChar);
                	}

                	//check if the item replaces a wear-item
                	if (tempItem != null && tempItem.isWear())
                	{
                		// dont allow an item to replace a wear-item
                		return;
                	}
                	else if (tempBodyPart == 0x4000) // left+right hand equipment
                	{
                		// this may not remove left OR right hand equipment
                		tempItem = activeChar.getInventory().getPaperdollItem(7);
                		if (tempItem != null && tempItem.isWear()) return;

                		tempItem = activeChar.getInventory().getPaperdollItem(8);
                		if (tempItem != null && tempItem.isWear()) return;
                	}
                	else if (tempBodyPart == 0x8000) // fullbody armor
                	{
                		// this may not remove chest or leggins
                		tempItem = activeChar.getInventory().getPaperdollItem(10);
                		if (tempItem != null && tempItem.isWear()) return;

                		tempItem = activeChar.getInventory().getPaperdollItem(11);
                		if (tempItem != null && tempItem.isWear()) return;
                	}

					if (item.getEnchantLevel() > 0)
					{
						sm = new SystemMessage(SystemMessageId.S1_S2_EQUIPPED);
						sm.addNumber(item.getEnchantLevel());
						sm.addItemName(itemId);
					}
					else
					{
						sm = new SystemMessage(SystemMessageId.S1_EQUIPPED);
						sm.addItemName(itemId);
					}
					activeChar.sendPacket(sm);
					
					if (item.isWeapon())
			        {
			          ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			          {
			            public void run()
			            {
			              L2PcInstance activeChar = ((L2GameClient)UseItem.this.getClient()).getActiveChar();
			              L2ItemInstance item = activeChar.getInventory().getItemByObjectId(UseItem.this._objectId);
			              item.setChargedSpiritshot(0);
			              item.setChargedSoulshot(0);
			              activeChar.rechargeAutoSoulShot(true, true, false);
			            }
			          }
			          , 250);
			        }
					
		            // Apply augementation boni on equip
		            if (item.isAugmented())
		            	item.getAugmentation().applyBoni(activeChar);

					items = activeChar.getInventory().equipItemAndRecord(item);

		            // Consume mana - will start a task if required; returns if item is not a shadow item
		            item.decreaseMana(false);
                }
                sm = null;

                activeChar.refreshExpertisePenalty();

				if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
					activeChar.checkIfWeaponIsAllowed();

				InventoryUpdate iu = new InventoryUpdate();
				iu.addItems(Arrays.asList(items));
				activeChar.sendPacket(iu);
				activeChar.abortAttack();
				activeChar.broadcastUserInfo();
			}
			else
			{
                L2Weapon weaponItem = activeChar.getActiveWeaponItem();
                int itemid = item.getItemId();
				//_log.finest("item not equipable id:"+ item.getItemId());
                if (itemid == 4393)
                {
                        activeChar.sendPacket(new ShowCalculator(4393));
                }
                else if ((weaponItem != null && weaponItem.getItemType() == L2WeaponType.ROD)
                    && ((itemid >= 6519 && itemid <= 6527) || (itemid >= 7610 && itemid <= 7613) || (itemid >= 7807 && itemid <= 7809) || (itemid >= 8484 && itemid <= 8486) || (itemid >= 8505 && itemid <= 8513)))
                {
                    activeChar.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
                    activeChar.broadcastUserInfo();
                    // Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
                    ItemList il = new ItemList(activeChar, false);
                    sendPacket(il);
                    return;
                }
				else
				{
					IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());

					if (handler == null)
					{
                        //_log.warning("No item handler registered for item ID " + item.getItemId() + ".");
					}
					else
                        handler.useItem(activeChar, item);
				}
			}
//		}
	}

	@Override
	public String getType()
	{
		return _C__14_USEITEM;
	}

}
