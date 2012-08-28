package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public final class RequestEnchantItem extends L2GameClientPacket
{
    protected static final Logger _log = Logger.getLogger(Inventory.class.getName());
    private static final String _C__58_REQUESTENCHANTITEM = "[C] 58 RequestEnchantItem";
    private static final int[] CRYSTAL_SCROLLS = { 731, 732, 949, 950, 953, 954, 957, 958, 961, 962 };
    private static final int[] ENCHANT_SCROLLS = { 729, 730, 947, 948, 951, 952, 955, 956, 959, 960 };
    private static final int[] BLESSED_SCROLLS = { 6569, 6570, 6571, 6572, 6573, 6574, 6575, 6576, 6577, 6578 };
    private int _objectId;

    @Override
	protected void readImpl()
    {
        _objectId = readD();
    }

    @SuppressWarnings("unused")
	@Override
	protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null || _objectId == 0) 
        	return;

		activeChar.cancelActiveTrade();
		
		if ((activeChar.isProcessingTransaction()) || (activeChar.isInStoreMode())) 
		{
		      activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
		      activeChar.setActiveEnchantItem(null);
		      return;
		}
		
        L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
        L2ItemInstance scroll = activeChar.getActiveEnchantItem();
        activeChar.setActiveEnchantItem(null);
        if (item == null || scroll == null) 
        	return;
        
    	if (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_ENCH))
    	{
    		return;
    	}
         // can't enchant rods, hero weapons and shadow items
        if(item.getItem().getItemType() == L2WeaponType.ROD || ((!Config.ENCHANT_HERO_WEAPONS) && item.getItemId() >= 6611 && item.getItemId() <= 6621 || item.isShadowItem()))
        {
        	activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
        	activeChar.setActiveEnchantItem(null);
        	activeChar.sendPacket(new EnchantResult(1));
            return;
        }
        if(item.isWear())
        {
            Util.handleIllegalPlayerAction(activeChar,"Player "+activeChar.getName()+" tried to enchant a weared Item", IllegalPlayerAction.PUNISH_KICK);
            return;
        }
        int itemType2 = item.getItem().getType2();
        boolean enchantItem = false;
        boolean blessedScroll = false;
		boolean blesseddScroll = false;
		boolean crystallScroll = false;
        int crystalId = 0;
        
        if (activeChar.getActiveTradeList() != null) 
        {
            activeChar.cancelActiveTrade();
            activeChar.sendMessage("Торговля отменена");
            return;
        }
        
        if (activeChar.isMoving())
        {
            activeChar.sendPacket(new EnchantResult(item.getEnchantLevel()));
            activeChar.setActiveEnchantItem(null); 
            activeChar.sendMessage("Вы не можете точить в даных условиях.");
            return; 
        } 
        /** pretty code ;D */
        switch (item.getItem().getCrystalType())
        {
            case L2Item.CRYSTAL_A:
                crystalId = 1461;
                switch(scroll.getItemId())
                {
                    case 729: case 731: case 6569:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 730: case 732: case 6570:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
            case L2Item.CRYSTAL_B:
                crystalId = 1460;
                switch(scroll.getItemId())
                {
                    case 947: case 949: case 6571:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 948: case 950: case 6572:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
            case L2Item.CRYSTAL_C:
                crystalId = 1459;
                switch(scroll.getItemId())
                {
                    case 951: case 953: case 6573:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 952: case 954: case 6574:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
            case L2Item.CRYSTAL_D:
                crystalId = 1458;
                switch(scroll.getItemId())
                {
                    case 955: case 957: case 6575:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 956: case 958: case 6576:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
            case L2Item.CRYSTAL_S:
                crystalId = 1462;
                switch(scroll.getItemId())
                {
                    case 959: case 961: case 6577:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 960: case 962: case 6578:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
        }

        if (!enchantItem)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
            activeChar.setActiveEnchantItem(null);
        	activeChar.sendPacket(new EnchantResult(1));
            return;
        }

        // Get the scroll type - Yesod
        if (scroll.getItemId() >= 6569 && scroll.getItemId() <= 6578)
		{
			blesseddScroll = true;
            blessedScroll = true;
		}
		else
            for (int crystalscroll : CRYSTAL_SCROLLS)
                if(scroll.getItemId() == crystalscroll)
                {
					crystallScroll = true;
                    blessedScroll = true;
					break;
                }
		

        // SystemMessage sm = new SystemMessage(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
        // activeChar.sendPacket(sm);

        SystemMessage sm;

        int chance = 0;
        int maxEnchantLevel = 0;

        if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
        {
          for (int scrollId : ENCHANT_SCROLLS)
          {
            if (scroll.getItemId() != scrollId)
              continue;
            if (item.getEnchantLevel() + 1 > Config.NORMAL_WEAPON_ENCHANT_LEVEL.size())
            {
            	if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
            	{
            		if (Config.MAGE_WEAPON_ID_LIST.contains(Integer.valueOf(item.getItemId())))
            			chance = ((Integer)Config.NORMAL_WEAPON_MAGE_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.NORMAL_WEAPON_MAGE_ENCHANT_LEVEL_DONATE.size()))).intValue();
            		else
            			chance = ((Integer)Config.NORMAL_WEAPON_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.NORMAL_WEAPON_ENCHANT_LEVEL_DONATE.size()))).intValue();
            	}
            	else if (Config.MAGE_WEAPON_ID_LIST.contains(Integer.valueOf(item.getItemId()))) 		
            		chance = ((Integer)Config.NORMAL_WEAPON_MAGE_ENCHANT_LEVEL.get(Integer.valueOf(Config.NORMAL_WEAPON_MAGE_ENCHANT_LEVEL.size()))).intValue();
            	else
            		chance = ((Integer)Config.NORMAL_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(Config.NORMAL_WEAPON_ENCHANT_LEVEL.size()))).intValue();
            }
            else if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
            {
            	if (Config.MAGE_WEAPON_ID_LIST.contains(Integer.valueOf(item.getItemId())))
            		chance = ((Integer)Config.NORMAL_WEAPON_MAGE_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            	else
            		chance = ((Integer)Config.NORMAL_WEAPON_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            }
            else if (Config.MAGE_WEAPON_ID_LIST.contains(Integer.valueOf(item.getItemId())))
            	chance = ((Integer)Config.NORMAL_WEAPON_MAGE_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            else
            	chance = ((Integer)Config.NORMAL_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
            	maxEnchantLevel = Config.ENCHANT_MAX_WEAPON_DONATE;
            else 
            {
            	maxEnchantLevel = Config.ENCHANT_MAX_WEAPON;
            }
          }

          for (int scrollId : CRYSTAL_SCROLLS)
          {
            if (scroll.getItemId() != scrollId)
              continue;
            if (item.getEnchantLevel() + 1 > Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.size())
            {
              if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              {
                if (Config.MAGE_WEAPON_ID_LIST.contains(Integer.valueOf(item.getItemId())))
                  chance = ((Integer)Config.CRYSTAL_WEAPON_MAGE_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.CRYSTAL_WEAPON_MAGE_ENCHANT_LEVEL_DONATE.size()))).intValue();
                else
                  chance = ((Integer)Config.CRYSTAL_WEAPON_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.CRYSTAL_WEAPON_ENCHANT_LEVEL_DONATE.size()))).intValue();
              }
              else if (Config.MAGE_WEAPON_ID_LIST.contains(Integer.valueOf(item.getItemId())))
                chance = ((Integer)Config.CRYSTAL_WEAPON_MAGE_ENCHANT_LEVEL.get(Integer.valueOf(Config.CRYSTAL_WEAPON_MAGE_ENCHANT_LEVEL.size()))).intValue();
              else
                chance = ((Integer)Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.size()))).intValue();
            }
            else if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
            {
              if (Config.MAGE_WEAPON_ID_LIST.contains(Integer.valueOf(item.getItemId())))
                chance = ((Integer)Config.CRYSTAL_WEAPON_MAGE_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
              else
                chance = ((Integer)Config.CRYSTAL_WEAPON_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            }
            else if (Config.MAGE_WEAPON_ID_LIST.contains(Integer.valueOf(item.getItemId())))
              chance = ((Integer)Config.CRYSTAL_WEAPON_MAGE_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            else
              chance = ((Integer)Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              maxEnchantLevel = Config.ENCHANT_MAX_WEAPON_DONATE;
            else {
              maxEnchantLevel = Config.ENCHANT_MAX_WEAPON;
            }
          }

          for (int scrollId : BLESSED_SCROLLS)
          {
            if (scroll.getItemId() != scrollId)
              continue;
            if (item.getEnchantLevel() + 1 > Config.BLESS_WEAPON_ENCHANT_LEVEL.size())
            {
              if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              {
                if (Config.MAGE_WEAPON_ID_LIST.contains(Integer.valueOf(item.getItemId())))
                  chance = ((Integer)Config.BLESS_WEAPON_MAGE_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.BLESS_WEAPON_MAGE_ENCHANT_LEVEL_DONATE.size()))).intValue();
                else
                  chance = ((Integer)Config.BLESS_WEAPON_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.BLESS_WEAPON_ENCHANT_LEVEL_DONATE.size()))).intValue();
              }
              else if (Config.MAGE_WEAPON_ID_LIST.contains(Integer.valueOf(item.getItemId())))
                chance = ((Integer)Config.BLESS_WEAPON_MAGE_ENCHANT_LEVEL.get(Integer.valueOf(Config.BLESS_WEAPON_MAGE_ENCHANT_LEVEL.size()))).intValue();
              else
                chance = ((Integer)Config.BLESS_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(Config.BLESS_WEAPON_ENCHANT_LEVEL.size()))).intValue();
            }
            else if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
            {
              if (Config.MAGE_WEAPON_ID_LIST.contains(Integer.valueOf(item.getItemId())))
                chance = ((Integer)Config.BLESS_WEAPON_MAGE_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
              else
                chance = ((Integer)Config.BLESS_WEAPON_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            }
            else if (Config.MAGE_WEAPON_ID_LIST.contains(Integer.valueOf(item.getItemId())))
              chance = ((Integer)Config.BLESS_WEAPON_MAGE_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            else
              chance = ((Integer)Config.BLESS_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              maxEnchantLevel = Config.ENCHANT_MAX_WEAPON_DONATE;
            else {
              maxEnchantLevel = Config.ENCHANT_MAX_WEAPON;
            }

          }

        }
        else if (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR)
        {
          for (int scrollId : ENCHANT_SCROLLS)
          {
            if (scroll.getItemId() != scrollId)
              continue;
            if (item.getEnchantLevel() + 1 > Config.NORMAL_ARMOR_ENCHANT_LEVEL.size())
            {
              if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              {
                if (item.getItem().getBodyPart() == 32768)
                  chance = ((Integer)Config.NORMAL_FULL_ARMOR_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.NORMAL_FULL_ARMOR_ENCHANT_LEVEL_DONATE.size()))).intValue();
                else
                  chance = ((Integer)Config.NORMAL_ARMOR_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.NORMAL_ARMOR_ENCHANT_LEVEL_DONATE.size()))).intValue();
              }
              else if (item.getItem().getBodyPart() == 32768)
                chance = ((Integer)Config.NORMAL_FULL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(Config.NORMAL_FULL_ARMOR_ENCHANT_LEVEL.size()))).intValue();
              else
                chance = ((Integer)Config.NORMAL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(Config.NORMAL_ARMOR_ENCHANT_LEVEL.size()))).intValue();
            }
            else if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
            {
              if (item.getItem().getBodyPart() == 32768)
                chance = ((Integer)Config.NORMAL_FULL_ARMOR_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
              else
                chance = ((Integer)Config.NORMAL_ARMOR_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            }
            else if (item.getItem().getBodyPart() == 32768)
              chance = ((Integer)Config.NORMAL_FULL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            else
              chance = ((Integer)Config.NORMAL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              maxEnchantLevel = Config.ENCHANT_MAX_ARMOR_DONATE;
            else {
              maxEnchantLevel = Config.ENCHANT_MAX_ARMOR;
            }
          }

          for (int scrollId : CRYSTAL_SCROLLS)
          {
            if (scroll.getItemId() != scrollId)
              continue;
            if (item.getEnchantLevel() + 1 > Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.size())
            {
              if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              {
                if (item.getItem().getBodyPart() == 32768)
                  chance = ((Integer)Config.CRYSTAL_FULL_ARMOR_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.CRYSTAL_FULL_ARMOR_ENCHANT_LEVEL_DONATE.size()))).intValue();
                else
                  chance = ((Integer)Config.CRYSTAL_ARMOR_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.CRYSTAL_ARMOR_ENCHANT_LEVEL_DONATE.size()))).intValue();
              }
              else if (item.getItem().getBodyPart() == 32768)
                chance = ((Integer)Config.CRYSTAL_FULL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(Config.CRYSTAL_FULL_ARMOR_ENCHANT_LEVEL.size()))).intValue();
              else
                chance = ((Integer)Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.size()))).intValue();
            }
            else if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
            {
              if (item.getItem().getBodyPart() == 32768)
                chance = ((Integer)Config.CRYSTAL_FULL_ARMOR_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
              else
                chance = ((Integer)Config.CRYSTAL_ARMOR_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            }
            else if (item.getItem().getBodyPart() == 32768)
              chance = ((Integer)Config.CRYSTAL_FULL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            else
              chance = ((Integer)Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              maxEnchantLevel = Config.ENCHANT_MAX_ARMOR_DONATE;
            else {
              maxEnchantLevel = Config.ENCHANT_MAX_ARMOR;
            }
          }

          for (int scrollId : BLESSED_SCROLLS)
          {
            if (scroll.getItemId() != scrollId)
              continue;
            if (item.getEnchantLevel() + 1 > Config.BLESS_ARMOR_ENCHANT_LEVEL.size())
            {
              if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              {
                if (item.getItem().getBodyPart() == 32768)
                  chance = ((Integer)Config.BLESS_FULL_ARMOR_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.BLESS_FULL_ARMOR_ENCHANT_LEVEL_DONATE.size()))).intValue();
                else
                  chance = ((Integer)Config.BLESS_ARMOR_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.BLESS_ARMOR_ENCHANT_LEVEL_DONATE.size()))).intValue();
              }
              else if (item.getItem().getBodyPart() == 32768)
                chance = ((Integer)Config.BLESS_FULL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(Config.BLESS_FULL_ARMOR_ENCHANT_LEVEL.size()))).intValue();
              else
                chance = ((Integer)Config.BLESS_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(Config.BLESS_ARMOR_ENCHANT_LEVEL.size()))).intValue();
            }
            else if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
            {
              if (item.getItem().getBodyPart() == 32768)
                chance = ((Integer)Config.BLESS_FULL_ARMOR_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
              else
                chance = ((Integer)Config.BLESS_ARMOR_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            }
            else if (item.getItem().getBodyPart() == 32768)
              chance = ((Integer)Config.BLESS_FULL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            else
              chance = ((Integer)Config.BLESS_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              maxEnchantLevel = Config.ENCHANT_MAX_ARMOR_DONATE;
            else {
              maxEnchantLevel = Config.ENCHANT_MAX_ARMOR;
            }

          }

        }
        else if (item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)
        {
          for (int scrollId : ENCHANT_SCROLLS)
          {
            if (scroll.getItemId() != scrollId)
              continue;
            if (item.getEnchantLevel() + 1 > Config.NORMAL_JEWELRY_ENCHANT_LEVEL.size())
            {
              if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
                chance = ((Integer)Config.NORMAL_JEWELRY_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.NORMAL_JEWELRY_ENCHANT_LEVEL_DONATE.size()))).intValue();
              else
                chance = ((Integer)Config.NORMAL_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(Config.NORMAL_JEWELRY_ENCHANT_LEVEL.size()))).intValue();
            }
            else if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              chance = ((Integer)Config.NORMAL_JEWELRY_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            else
              chance = ((Integer)Config.NORMAL_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              maxEnchantLevel = Config.ENCHANT_MAX_JEWELRY_DONATE;
            else {
              maxEnchantLevel = Config.ENCHANT_MAX_JEWELRY;
            }
          }
          for (int scrollId : CRYSTAL_SCROLLS)
          {
            if (scroll.getItemId() != scrollId)
              continue;
            if (item.getEnchantLevel() + 1 > Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.size())
            {
              if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
                chance = ((Integer)Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL_DONATE.size()))).intValue();
              else
                chance = ((Integer)Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.size()))).intValue();
            }
            else if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              chance = ((Integer)Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            else
              chance = ((Integer)Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              maxEnchantLevel = Config.ENCHANT_MAX_JEWELRY_DONATE;
            else {
              maxEnchantLevel = Config.ENCHANT_MAX_JEWELRY;
            }
          }

          for (int scrollId : BLESSED_SCROLLS)
          {
            if (scroll.getItemId() != scrollId)
              continue;
            if (item.getEnchantLevel() + 1 > Config.BLESS_JEWELRY_ENCHANT_LEVEL.size())
            {
              if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
                chance = ((Integer)Config.BLESS_JEWELRY_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(Config.BLESS_JEWELRY_ENCHANT_LEVEL_DONATE.size()))).intValue();
              else
                chance = ((Integer)Config.BLESS_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(Config.BLESS_JEWELRY_ENCHANT_LEVEL.size()))).intValue();
            }
            else if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              chance = ((Integer)Config.BLESS_JEWELRY_ENCHANT_LEVEL_DONATE.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            else
              chance = ((Integer)Config.BLESS_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1))).intValue();
            if (Config.USE_PREMIUMSERVICE && activeChar.getPremiumService() == 1)
              maxEnchantLevel = Config.ENCHANT_MAX_JEWELRY_DONATE;
            else {
              maxEnchantLevel = Config.ENCHANT_MAX_JEWELRY;
            }

          }

        }

       /* if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
        {
			if((item.getEnchantLevel() < 10) && !blesseddScroll && !crystallScroll) chance = Config.ENCHANT_CHANCE_WEAPON;
			else
			if((item.getEnchantLevel() < 16 && item.getEnchantLevel() > 9) && !blesseddScroll && !crystallScroll) chance = Config.ENCHANT_CHANCE_WEAPON_1015;
			else
			if((item.getEnchantLevel() > 15) && !blesseddScroll && !crystallScroll) chance = Config.ENCHANT_CHANCE_WEAPON_16;
			else
			if(item.getEnchantLevel() < 10 && blesseddScroll) chance = Config.BLESSED_CHANCE_WEAPON;
			else
			if((item.getEnchantLevel() < 16 && item.getEnchantLevel() > 9) && blesseddScroll) chance = Config.BLESSED_CHANCE_WEAPON_1015;
			else
			if(item.getEnchantLevel() > 15 && blesseddScroll) chance = Config.BLESSED_CHANCE_WEAPON_16;
			else
			if(item.getEnchantLevel() < 10 && crystallScroll) chance = Config.CRYSTAL_CHANCE_WEAPON;
			else
			if((item.getEnchantLevel() < 16 && item.getEnchantLevel() > 9) && crystallScroll) chance = Config.CRYSTAL_CHANCE_WEAPON_1015;
			else
			if(item.getEnchantLevel() > 15 && crystallScroll) chance = Config.CRYSTAL_CHANCE_WEAPON_16;
	        maxEnchantLevel = Config.ENCHANT_MAX_WEAPON;

			if (Config.ENABLE_MODIFY_ENCHANT_CHANCE_WEAPON && blesseddScroll)
			{
				if (Config.ENCHANT_CHANCE_LIST_WEAPON.containsKey(item.getEnchantLevel()))
				{
					chance = Config.ENCHANT_CHANCE_LIST_WEAPON.get(item.getEnchantLevel());
				}
			}
        }
        else if (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR)
        {
			if((item.getEnchantLevel() < 10) && !blesseddScroll && !crystallScroll) chance = Config.ENCHANT_CHANCE_ARMOR;
			else
			if((item.getEnchantLevel() < 16 && item.getEnchantLevel() > 9) && !blesseddScroll && !crystallScroll) chance = Config.ENCHANT_CHANCE_ARMOR_1015;
			else
			if((item.getEnchantLevel() > 15) && !blesseddScroll && !crystallScroll) chance = Config.ENCHANT_CHANCE_ARMOR_16;
			else
			if(item.getEnchantLevel() < 10 && blesseddScroll) chance = Config.BLESSED_CHANCE_ARMOR;
			else
			if((item.getEnchantLevel() < 16 && item.getEnchantLevel() > 9) && blesseddScroll) chance = Config.BLESSED_CHANCE_ARMOR_1015;
			else
			if(item.getEnchantLevel() > 15 && blesseddScroll) chance = Config.BLESSED_CHANCE_ARMOR_16;
			else
			if(item.getEnchantLevel() < 10 && crystallScroll) chance = Config.CRYSTAL_CHANCE_ARMOR;
			else
			if((item.getEnchantLevel() < 16 && item.getEnchantLevel() > 9) && crystallScroll) chance = Config.CRYSTAL_CHANCE_ARMOR_1015;
			else
			if(item.getEnchantLevel() > 15 && crystallScroll) chance = Config.CRYSTAL_CHANCE_ARMOR_16;
	        maxEnchantLevel = Config.ENCHANT_MAX_ARMOR;

			if (Config.ENABLE_MODIFY_ENCHANT_CHANCE_ARMOR && blesseddScroll)
			{
				if (Config.ENCHANT_CHANCE_LIST_ARMOR.containsKey(item.getEnchantLevel()))
				{
					chance = Config.ENCHANT_CHANCE_LIST_ARMOR.get(item.getEnchantLevel());
				}
			}
        }
        else if (item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)
        {
			if((item.getEnchantLevel() < 10) && !blesseddScroll && !crystallScroll) chance = Config.ENCHANT_CHANCE_JEWELRY;
			else
			if((item.getEnchantLevel() < 16 && item.getEnchantLevel() > 9) && !blesseddScroll && !crystallScroll) chance = Config.ENCHANT_CHANCE_JEWELRY_1015;
			else
			if((item.getEnchantLevel() > 15) && !blesseddScroll && !crystallScroll) chance = Config.ENCHANT_CHANCE_JEWELRY_16;
			else
			if(item.getEnchantLevel() < 10 && blesseddScroll) chance = Config.BLESSED_CHANCE_JEWELRY;
			else
			if((item.getEnchantLevel() < 16 && item.getEnchantLevel() > 9) && blesseddScroll) chance = Config.BLESSED_CHANCE_JEWELRY_1015;
			else
			if(item.getEnchantLevel() > 15 && blesseddScroll) chance = Config.BLESSED_CHANCE_JEWELRY_16;
			else
			if(item.getEnchantLevel() < 10 && crystallScroll) chance = Config.CRYSTAL_CHANCE_JEWELRY;
			else
			if((item.getEnchantLevel() < 16 && item.getEnchantLevel() > 9) && crystallScroll) chance = Config.CRYSTAL_CHANCE_JEWELRY_1015;
			else
			if(item.getEnchantLevel() > 15 && crystallScroll) chance = Config.CRYSTAL_CHANCE_JEWELRY_16;
        	maxEnchantLevel = Config.ENCHANT_MAX_JEWELRY;

			if (Config.ENABLE_MODIFY_ENCHANT_CHANCE_JEWELRY && blesseddScroll)
			{
				if (Config.ENCHANT_CHANCE_LIST_JEWELRY.containsKey(item.getEnchantLevel()))
				{
					chance = Config.ENCHANT_CHANCE_LIST_JEWELRY.get(item.getEnchantLevel());
				}
			}
        }*/

	// donate test
	// list.contains(activeChar.getName());
        //Good evo :D
	/**if (activeChar.getName().equals("ad3sdsadass2"))
	{
		chance = 100;
	}*/

        if (item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX
           || (item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR
           && item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL))
            chance = 100;

        if ((item.getEnchantLevel() >= maxEnchantLevel) && (maxEnchantLevel != 0))
        {
          activeChar.sendMessage("Достигнут максимальный уровень заточки.");
          return;
        }
		if (Config.ENCHANT_STACKABLE)
			scroll = activeChar.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1, activeChar, item);
		else
			scroll = activeChar.getInventory().destroyItem("Enchant", scroll, activeChar, item);
        if(scroll == null)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
            Util.handleIllegalPlayerAction(activeChar,"Player "+activeChar.getName()+" tried to enchant with a scroll he doesnt have", Config.DEFAULT_PUNISH);
            return;
        }
        
        if (Rnd.get(100) < chance)
        {
            synchronized(item)
            {
            	if (item.getOwnerId() != activeChar.getObjectId()) // has just lost the item
            	{
            		activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
            		return;
            	}
            	if (item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY && item.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL)
            	{
            		activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
            		activeChar.setActiveEnchantItem(null);
                	activeChar.sendPacket(new EnchantResult(1));
            		return;
            	}
            	if (item.getEnchantLevel() == 0)
            	{
            		sm = new SystemMessage(SystemMessageId.S1_SUCCESSFULLY_ENCHANTED);
            		sm.addItemName(item.getItemId());
            		activeChar.sendPacket(sm);
            	}
            	else
            	{
            		sm = new SystemMessage(SystemMessageId.S1_S2_SUCCESSFULLY_ENCHANTED);
            		sm.addNumber(item.getEnchantLevel());
            		sm.addItemName(item.getItemId());
            		activeChar.sendPacket(sm);
            	}
            	item.setEnchantLevel(item.getEnchantLevel()+1);
            	item.updateDatabase();
            }
        }
        else
        {
            if (!blessedScroll)
            {
                if (item.getEnchantLevel() > 0)
                {
                    sm = new SystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_S2_EVAPORATED);
                    sm.addNumber(item.getEnchantLevel());
                    sm.addItemName(item.getItemId());
                    activeChar.sendPacket(sm);
                }
                else
                {
                    sm = new SystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_EVAPORATED);
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
                
                int count = item.getCrystalCount() - (item.getItem().getCrystalCount() +1) / 2;
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
                    if (destroyItem.getCount() == 0) iu.addRemovedItem(destroyItem);
                    else iu.addModifiedItem(destroyItem);
                    iu.addItem(crystals);
                    
                    activeChar.sendPacket(iu);
                }
                else activeChar.sendPacket(new ItemList(activeChar, true));
            
                StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
                su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
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
        sm = null;
        
        StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
        su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
        activeChar.sendPacket(su);      
        su = null;
        
        activeChar.sendPacket(new EnchantResult(item.getEnchantLevel())); //FIXME i'm really not sure about this...
        activeChar.sendPacket(new ItemList(activeChar, false)); //TODO update only the enchanted item
        activeChar.broadcastUserInfo();
    }
    
    @Override
	public String getType()
    {
        return _C__58_REQUESTENCHANTITEM;
    }
}
