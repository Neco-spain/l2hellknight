package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.AugmentationData;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExVariationResult;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.Util;

public final class RequestRefine extends L2GameClientPacket
{
  private static final String _C__D0_2C_REQUESTREFINE = "[C] D0:2C RequestRefine";
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
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;
    L2ItemInstance targetItem = (L2ItemInstance)L2World.getInstance().findObject(_targetItemObjId);
    L2ItemInstance refinerItem = (L2ItemInstance)L2World.getInstance().findObject(_refinerItemObjId);
    L2ItemInstance gemstoneItem = (L2ItemInstance)L2World.getInstance().findObject(_gemstoneItemObjId);

    if ((targetItem == null) || (refinerItem == null) || (gemstoneItem == null) || (targetItem.getOwnerId() != activeChar.getObjectId()) || (refinerItem.getOwnerId() != activeChar.getObjectId()) || (gemstoneItem.getOwnerId() != activeChar.getObjectId()) || (activeChar.getLevel() < 46))
    {
      activeChar.sendPacket(new ExVariationResult(0, 0, 0));
      activeChar.sendPacket(new SystemMessage(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS));
      return;
    }

    if (targetItem.isEquipped()) activeChar.disarmWeapons();

    if (TryAugmentItem(activeChar, targetItem, refinerItem, gemstoneItem))
    {
      int stat12 = 0xFFFF & targetItem.getAugmentation().getAugmentationId();
      int stat34 = targetItem.getAugmentation().getAugmentationId() >> 16;
      activeChar.sendPacket(new ExVariationResult(stat12, stat34, 1));
      activeChar.sendPacket(new SystemMessage(SystemMessageId.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED));
    }
    else
    {
      activeChar.sendPacket(new ExVariationResult(0, 0, 0));
      activeChar.sendPacket(new SystemMessage(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS));
    }
  }

  boolean TryAugmentItem(L2PcInstance player, L2ItemInstance targetItem, L2ItemInstance refinerItem, L2ItemInstance gemstoneItem)
  {
    if ((targetItem.isAugmented()) || (targetItem.isWear())) return false;

    if (player.getInventory().getItemByObjectId(refinerItem.getObjectId()) == null)
    {
      Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to refine an item with wrong LifeStone-id.", Config.DEFAULT_PUNISH);
      return false;
    }
    if (player.getInventory().getItemByObjectId(targetItem.getObjectId()) == null)
    {
      Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to refine an item with wrong Weapon-id.", Config.DEFAULT_PUNISH);
      return false;
    }
    if (player.getInventory().getItemByObjectId(gemstoneItem.getObjectId()) == null)
    {
      Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to refine an item with wrong Gemstone-id.", Config.DEFAULT_PUNISH);
      return false;
    }

    int itemGrade = targetItem.getItem().getItemGrade();
    int itemType = targetItem.getItem().getType2();
    int lifeStoneId = refinerItem.getItemId();
    int gemstoneItemId = gemstoneItem.getItemId();

    if ((lifeStoneId < 8723) || (lifeStoneId > 8762)) return false;

    if ((itemGrade < 2) || (itemType != 0) || (!targetItem.isDestroyable())) return false;

    if ((player.getPrivateStoreType() != 0) || (player.isDead()) || (player.isParalyzed()) || (player.isFishing()) || (player.isSitting())) {
      return false;
    }
    int modifyGemstoneCount = _gemstoneCount;
    int lifeStoneLevel = getLifeStoneLevel(lifeStoneId);
    int lifeStoneGrade = getLifeStoneGrade(lifeStoneId);
    switch (itemGrade)
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

    if (gemstoneItem.getCount() - modifyGemstoneCount < 0) return false;

    if (!player.destroyItem("RequestRefine", refinerItem, null, false)) {
      return false;
    }

    InventoryUpdate iu = new InventoryUpdate();

    if (gemstoneItem.getCount() - modifyGemstoneCount == 0)
    {
      player.destroyItem("RequestRefine", gemstoneItem, null, false);
      iu.addRemovedItem(gemstoneItem);
    }
    else
    {
      player.destroyItem("RequestRefine", _gemstoneItemObjId, modifyGemstoneCount, null, false);
      iu.addModifiedItem(gemstoneItem);
    }

    targetItem.setAugmentation(AugmentationData.getInstance().generateRandomAugmentation(targetItem, lifeStoneLevel, lifeStoneGrade));

    iu.addModifiedItem(targetItem);
    iu.addRemovedItem(refinerItem);
    player.sendPacket(iu);

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
    return "[C] D0:2C RequestRefine";
  }
}