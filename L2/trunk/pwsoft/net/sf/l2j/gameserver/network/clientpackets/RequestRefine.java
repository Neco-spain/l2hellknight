package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.AugmentationData;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ExVariationResult;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.templates.L2Item;

public final class RequestRefine extends L2GameClientPacket
{
  private int _targetItemObjId;
  private int _refinerItemObjId;
  private int _gemstoneItemObjId;
  private int _gemstoneCount;

  protected void readImpl()
  {
    _targetItemObjId = readD();
    _refinerItemObjId = readD();
    _gemstoneItemObjId = readD();
    _gemstoneCount = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (!player.getAugFlag())
    {
      player.sendPacket(Static.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
      return;
    }
    player.setAugFlag(false);

    L2ItemInstance targetItem = (L2ItemInstance)L2World.getInstance().findObject(_targetItemObjId);
    L2ItemInstance refinerItem = (L2ItemInstance)L2World.getInstance().findObject(_refinerItemObjId);
    L2ItemInstance gemstoneItem = (L2ItemInstance)L2World.getInstance().findObject(_gemstoneItemObjId);

    if ((targetItem == null) || (refinerItem == null) || (gemstoneItem == null) || (targetItem.getOwnerId() != player.getObjectId()) || (refinerItem.getOwnerId() != player.getObjectId()) || (gemstoneItem.getOwnerId() != player.getObjectId()) || (player.getLevel() < 46))
    {
      player.sendPacket(new ExVariationResult(0, 0, 0));
      player.sendPacket(Static.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
      return;
    }

    if (targetItem.isEquipped()) player.disarmWeapons();

    if (tryAugmentItem(player, targetItem, refinerItem, gemstoneItem))
    {
      int stat12 = 0xFFFF & targetItem.getAugmentation().getAugmentationId();
      int stat34 = targetItem.getAugmentation().getAugmentationId() >> 16;
      player.sendPacket(new ExVariationResult(stat12, stat34, 1));
      player.sendPacket(Static.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED);
    }
    else
    {
      player.sendPacket(new ExVariationResult(0, 0, 0));
      player.sendPacket(Static.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
    }
  }

  boolean tryAugmentItem(L2PcInstance player, L2ItemInstance targetItem, L2ItemInstance refinerItem, L2ItemInstance gemstoneItem)
  {
    if ((targetItem.isAugmented()) || (targetItem.isWear())) {
      return false;
    }
    if (!targetItem.canBeAugmented()) {
      return false;
    }
    if (player.isDead())
    {
      player.sendPacket(Static.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
      return false;
    }
    if (player.isSitting())
    {
      player.sendPacket(Static.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
      return false;
    }
    if (player.isFishing())
    {
      player.sendPacket(Static.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
      return false;
    }
    if (player.isParalyzed())
    {
      player.sendPacket(Static.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
      return false;
    }
    if (player.getActiveTradeList() != null)
    {
      player.sendPacket(Static.AUGMENTED_ITEM_CANNOT_BE_DISCARDED);
      return false;
    }
    if (player.getPrivateStoreType() != 0)
    {
      player.sendPacket(Static.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
      return false;
    }

    if (player.getInventory().getItemByObjectId(refinerItem.getObjectId()) == null)
    {
      return false;
    }
    if (player.getInventory().getItemByObjectId(targetItem.getObjectId()) == null)
    {
      return false;
    }
    if (player.getInventory().getItemByObjectId(gemstoneItem.getObjectId()) == null)
    {
      return false;
    }

    int lifeStoneId = refinerItem.getItemId();
    int gemstoneItemId = gemstoneItem.getItemId();

    if ((lifeStoneId < 8723) || (lifeStoneId > 8762)) {
      return false;
    }
    int modifyGemstoneCount = _gemstoneCount;
    int lifeStoneLevel = getLifeStoneLevel(lifeStoneId);
    int lifeStoneGrade = getLifeStoneGrade(lifeStoneId);
    switch (targetItem.getItem().getItemGrade())
    {
    case 2:
      if ((player.getLevel() < 46) || (gemstoneItemId != 2130)) return false;
      modifyGemstoneCount = 20;
      break;
    case 3:
      if ((lifeStoneLevel < 3) || (player.getLevel() < 52) || (gemstoneItemId != 2130)) return false;
      modifyGemstoneCount = 30;
      break;
    case 4:
      if ((lifeStoneLevel < 6) || (player.getLevel() < 61) || (gemstoneItemId != 2131)) return false;
      modifyGemstoneCount = 20;
      break;
    case 5:
      if ((lifeStoneLevel != 10) || (player.getLevel() < 76) || (gemstoneItemId != 2131)) return false;
      modifyGemstoneCount = 25;
    }

    switch (lifeStoneLevel)
    {
    case 1:
      if (player.getLevel() >= 46) break; return false;
    case 2:
      if (player.getLevel() >= 49) break; return false;
    case 3:
      if (player.getLevel() >= 52) break; return false;
    case 4:
      if (player.getLevel() >= 55) break; return false;
    case 5:
      if (player.getLevel() >= 58) break; return false;
    case 6:
      if (player.getLevel() >= 61) break; return false;
    case 7:
      if (player.getLevel() >= 64) break; return false;
    case 8:
      if (player.getLevel() >= 67) break; return false;
    case 9:
      if (player.getLevel() >= 70) break; return false;
    case 10:
      if (player.getLevel() >= 76) break; return false;
    }

    if (!player.destroyItem("RequestRefine", refinerItem, null, false))
    {
      return false;
    }

    player.destroyItem("RequestRefine", _gemstoneItemObjId, modifyGemstoneCount, null, false);

    targetItem.setAugmentation(AugmentationData.getInstance().generateRandomAugmentation(targetItem, lifeStoneLevel, lifeStoneGrade, player.isPremium()));

    InventoryUpdate iu = new InventoryUpdate();
    iu.addModifiedItem(targetItem);
    player.sendPacket(iu);

    StatusUpdate su = new StatusUpdate(player.getObjectId());
    su.addAttribute(14, player.getCurrentLoad());
    player.sendPacket(su);

    return true;
  }

  private int getLifeStoneGrade(int itemId)
  {
    itemId -= 8723;
    if (itemId < 10) return 0;
    if (itemId < 20) return 1;
    if (itemId < 30) return 2;
    return 3;
  }

  private int getLifeStoneLevel(int itemId)
  {
    itemId -= 10 * getLifeStoneGrade(itemId);
    itemId -= 8722;
    return itemId;
  }

  public String getType()
  {
    return "C.Refine";
  }
}