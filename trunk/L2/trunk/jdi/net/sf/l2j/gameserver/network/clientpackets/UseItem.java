package net.sf.l2j.gameserver.network.clientpackets;

import java.util.Arrays;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
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

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

    activeChar.cancelActiveTrade();

    if (activeChar.getPrivateStoreType() != 0)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
      activeChar.sendPacket(new ActionFailed());
      return;
    }

    if (item == null) {
      return;
    }

    if (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), 0)) {
      return;
    }
    if (item.isWear())
    {
      return;
    }

    int itemId = item.getItemId();

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT) && (activeChar.getKarma() > 0) && ((itemId == 736) || (itemId == 1538) || (itemId == 1829) || (itemId == 1830) || (itemId == 3958) || (itemId == 5858) || (itemId == 5859) || (itemId == 6663) || (itemId == 6664) || ((itemId >= 7117) && (itemId <= 7135)) || ((itemId >= 7554) && (itemId <= 7559)) || (itemId == 7618) || (itemId == 7619)))
    {
      return;
    }

    if (itemId == 57) {
      return;
    }
    if ((activeChar.isFishing()) && ((itemId < 6535) || (itemId > 6540)))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
      ((L2GameClient)getClient()).getActiveChar().sendPacket(sm);
      sm = null;
      return;
    }

    if (activeChar.isDead())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
      sm.addItemName(itemId);
      ((L2GameClient)getClient()).getActiveChar().sendPacket(sm);
      sm = null;
      return;
    }

    if ((item.getItem().isForWolf()) || (item.getItem().isForHatchling()) || (item.getItem().isForStrider()) || (item.getItem().isForBabyPet()))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_EQUIP_PET_ITEM);
      sm.addItemName(itemId);
      ((L2GameClient)getClient()).getActiveChar().sendPacket(sm);
      sm = null;
      return;
    }

    if (Config.BOWTANK_PENALTY)
    {
      int classid = activeChar.getClassId().getId();
      if ((!activeChar.isInOlympiadMode()) && ((classid == 88) || (classid == 89) || (classid == 6) || (classid == 90) || (classid == 91) || (classid == 100) || (classid == 99) || (classid == 113) || (classid == 114)))
      {
        if (item.getItemType() == L2WeaponType.BOW)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
          sm.addItemName(itemId);
          ((L2GameClient)getClient()).getActiveChar().sendPacket(sm);
          sm = null;
          return;
        }
      }
    }

    if (Config.DEBUG) {
      _log.finest(activeChar.getObjectId() + ": use item " + _objectId);
    }
    if (item.isEquipable())
    {
      if ((activeChar.isStunned()) || (activeChar.isSleeping()) || (activeChar.isMeditation()) || (activeChar.isParalyzed()) || (activeChar.isAlikeDead()))
      {
        activeChar.sendMessage("Your status does not allow you to do that.");
        return;
      }

      int bodyPart = item.getItem().getBodyPart();
      if (((activeChar.isAttackingNow()) || (activeChar.isCastingNow()) || (activeChar.isMounted()) || ((activeChar._inEventCTF) && (activeChar._haveFlagCTF))) && ((bodyPart == 16384) || (bodyPart == 256) || (bodyPart == 128)))
      {
        if ((activeChar._inEventCTF) && (activeChar._haveFlagCTF))
          activeChar.sendMessage("This item can not be equipped when you have the flag.");
        return;
      }

      if ((bodyPart == 256) || (bodyPart == 128) || (bodyPart == 16384))
      {
        if ((activeChar.getInventory().getPaperdollItemByL2ItemId(16384) != null) && (activeChar.getInventory().getPaperdollItemByL2ItemId(16384).getAugmentation() != null))
        {
          activeChar.getInventory().getPaperdollItemByL2ItemId(16384).getAugmentation().removeBoni(activeChar);
        }
      }

      L2Effect[] effects = activeChar.getAllEffects();

      for (L2Effect e : effects)
      {
        if (((e.getSkill().getSkillType() != L2Skill.SkillType.CONT) && (e.getSkill().getSkillType() != L2Skill.SkillType.BUFF) && (e.getSkill().getSkillType() != L2Skill.SkillType.HEAL_PERCENT) && (e.getSkill().getSkillType() != L2Skill.SkillType.REFLECT)) || (((e.getSkill().getId() < 3124) || (e.getSkill().getId() > 3259)) && ((e.getSkill().getId() != 422) || ((bodyPart != 16384) && (bodyPart != 256) && (bodyPart != 128)))))
        {
          continue;
        }

        activeChar.stopSkillEffects(e.getSkill().getId());
        break;
      }

      if ((activeChar.isWearingFormalWear()) && ((bodyPart == 16384) || (bodyPart == 256) || (bodyPart == 128)))
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR);
        activeChar.sendPacket(sm);
        return;
      }

      if ((activeChar.isCursedWeaponEquiped()) && ((bodyPart == 16384) || (bodyPart == 256) || (bodyPart == 128) || (itemId == 6408)))
      {
        return;
      }

      if ((activeChar.isInOlympiadMode()) && ((item.isHeroItem()) || (item.isOlyRestrictedItem())))
      {
        return;
      }

      if ((activeChar.isInOlympiadMode()) && (((item.getItemId() >= 6611) && (item.getItemId() <= 6621)) || (item.getItemId() == 6842))) return;

      L2ItemInstance[] items = null;
      boolean isEquiped = item.isEquipped();
      SystemMessage sm = null;
      L2ItemInstance old = activeChar.getInventory().getPaperdollItem(14);
      if (old == null) {
        old = activeChar.getInventory().getPaperdollItem(7);
      }
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

        if (item.isAugmented()) {
          item.getAugmentation().removeBoni(activeChar);
        }
        int slot = activeChar.getInventory().getSlotFromItem(item);
        items = activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
      }
      else
      {
        int tempBodyPart = item.getItem().getBodyPart();
        L2ItemInstance tempItem = activeChar.getInventory().getPaperdollItemByL2ItemId(tempBodyPart);

        if ((tempItem != null) && (tempItem.isAugmented())) {
          tempItem.getAugmentation().removeBoni(activeChar);
        } else if (tempBodyPart == 16384)
        {
          L2ItemInstance tempItem2 = activeChar.getInventory().getPaperdollItem(7);

          if ((tempItem2 != null) && (tempItem2.isAugmented())) {
            tempItem2.getAugmentation().removeBoni(activeChar);
          }
          tempItem2 = activeChar.getInventory().getPaperdollItem(8);

          if ((tempItem2 != null) && (tempItem2.isAugmented())) {
            tempItem2.getAugmentation().removeBoni(activeChar);
          }
        }

        if ((tempItem != null) && (tempItem.isWear()))
        {
          return;
        }
        if (tempBodyPart == 16384)
        {
          tempItem = activeChar.getInventory().getPaperdollItem(7);

          if ((tempItem != null) && (tempItem.isWear())) {
            return;
          }
          tempItem = activeChar.getInventory().getPaperdollItem(8);

          if ((tempItem != null) && (tempItem.isWear()))
            return;
        }
        else if (tempBodyPart == 32768)
        {
          tempItem = activeChar.getInventory().getPaperdollItem(10);

          if ((tempItem != null) && (tempItem.isWear())) {
            return;
          }
          tempItem = activeChar.getInventory().getPaperdollItem(11);

          if ((tempItem != null) && (tempItem.isWear())) {
            return;
          }
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
              L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
              L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
              item.setChargedSpiritshot(0);
              item.setChargedSoulshot(0);
              activeChar.rechargeAutoSoulShot(true, true, false);
            }
          }
          , 250L);
        }

        if (item.isAugmented()) {
          item.getAugmentation().applyBoni(activeChar);
        }
        items = activeChar.getInventory().equipItemAndRecord(item);

        item.decreaseMana(false);
      }
      sm = null;

      activeChar.refreshExpertisePenalty();

      if (item.getItem().getType2() == 0) {
        activeChar.checkIfWeaponIsAllowed();
      }
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

      if (itemid == 4393)
      {
        activeChar.sendPacket(new ShowCalculator(4393));
      } else {
        if ((weaponItem != null) && (weaponItem.getItemType() == L2WeaponType.ROD) && (((itemid >= 6519) && (itemid <= 6527)) || ((itemid >= 7610) && (itemid <= 7613)) || ((itemid >= 7807) && (itemid <= 7809)) || ((itemid >= 8484) && (itemid <= 8486)) || ((itemid >= 8505) && (itemid <= 8513))))
        {
          activeChar.getInventory().setPaperdollItem(8, item);
          activeChar.broadcastUserInfo();

          ItemList il = new ItemList(activeChar, false);
          sendPacket(il);
          return;
        }

        IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());

        if (handler != null)
        {
          handler.useItem(activeChar, item);
        }
      }
    }
  }

  public String getType()
  {
    return "[C] 14 UseItem";
  }
}