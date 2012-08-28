package l2m.gameserver.network.clientpackets;

import l2p.commons.dao.JdbcEntityState;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.actor.instances.player.ShortCut;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExVariationResult;
import l2m.gameserver.network.serverpackets.InventoryUpdate;
import l2m.gameserver.network.serverpackets.ShortCutRegister;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.data.tables.AugmentationData;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.templates.item.ItemTemplate.Grade;
import l2m.gameserver.utils.ItemFunctions;

public final class RequestRefine extends L2GameClientPacket
{
  private static final int GEMSTONE_D = 2130;
  private static final int GEMSTONE_C = 2131;
  private static final int GEMSTONE_B = 2132;
  private int _targetItemObjId;
  private int _refinerItemObjId;
  private int _gemstoneItemObjId;
  private long _gemstoneCount;

  protected void readImpl()
  {
    _targetItemObjId = readD();
    _refinerItemObjId = readD();
    _gemstoneItemObjId = readD();
    _gemstoneCount = readQ();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (_gemstoneCount < 1L)) {
      return;
    }
    if (activeChar.isActionsDisabled())
    {
      activeChar.sendPacket(new ExVariationResult(0, 0, 0));
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendPacket(new ExVariationResult(0, 0, 0));
      return;
    }

    if (activeChar.isInTrade())
    {
      activeChar.sendPacket(new ExVariationResult(0, 0, 0));
      return;
    }

    ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
    ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
    ItemInstance gemstoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);

    if ((targetItem == null) || (refinerItem == null) || (gemstoneItem == null) || (activeChar.getLevel() < 46))
    {
      activeChar.sendPacket(new IStaticPacket[] { new ExVariationResult(0, 0, 0), Msg.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS });
      return;
    }

    if (TryAugmentItem(activeChar, targetItem, refinerItem, gemstoneItem))
    {
      int stat12 = 0xFFFF & targetItem.getAugmentationId();
      int stat34 = targetItem.getAugmentationId() >> 16;
      activeChar.sendPacket(new IStaticPacket[] { new ExVariationResult(stat12, stat34, 1), Msg.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED });
    }
    else {
      activeChar.sendPacket(new IStaticPacket[] { new ExVariationResult(0, 0, 0), Msg.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS });
    }
  }

  boolean TryAugmentItem(Player player, ItemInstance targetItem, ItemInstance refinerItem, ItemInstance gemstoneItem) {
    ItemTemplate.Grade itemGrade = targetItem.getTemplate().getItemGrade();
    int lifeStoneId = refinerItem.getItemId();
    int gemstoneItemId = gemstoneItem.getItemId();

    boolean isAccessoryLifeStone = ItemFunctions.isAccessoryLifeStone(lifeStoneId);
    if (!targetItem.canBeAugmented(player, isAccessoryLifeStone)) {
      return false;
    }
    if ((!isAccessoryLifeStone) && (!ItemFunctions.isLifeStone(lifeStoneId))) {
      return false;
    }
    long modifyGemstoneCount = _gemstoneCount;
    int lifeStoneLevel = ItemFunctions.getLifeStoneLevel(lifeStoneId);
    int lifeStoneGrade = isAccessoryLifeStone ? 0 : ItemFunctions.getLifeStoneGrade(lifeStoneId);

    if (isAccessoryLifeStone) {
      switch (1.$SwitchMap$l2p$gameserver$templates$item$ItemTemplate$Grade[itemGrade.ordinal()])
      {
      case 1:
        if ((player.getLevel() < 46) || (gemstoneItemId != 2130))
          return false;
        modifyGemstoneCount = 200L;
        break;
      case 2:
        if ((player.getLevel() < 52) || (gemstoneItemId != 2130))
          return false;
        modifyGemstoneCount = 300L;
        break;
      case 3:
        if ((player.getLevel() < 61) || (gemstoneItemId != 2131))
          return false;
        modifyGemstoneCount = 200L;
        break;
      case 4:
        if ((player.getLevel() < 76) || (gemstoneItemId != 2131))
          return false;
        modifyGemstoneCount = 250L;
        break;
      case 5:
        if ((player.getLevel() < 80) || (gemstoneItemId != 2132))
          return false;
        modifyGemstoneCount = 250L;
        break;
      case 6:
        if ((player.getLevel() < 84) || (gemstoneItemId != 2132))
          return false;
        modifyGemstoneCount = 250L;
      }
    }
    else {
      switch (1.$SwitchMap$l2p$gameserver$templates$item$ItemTemplate$Grade[itemGrade.ordinal()])
      {
      case 1:
        if ((player.getLevel() < 46) || (gemstoneItemId != 2130))
          return false;
        modifyGemstoneCount = 20L;
        break;
      case 2:
        if ((player.getLevel() < 52) || (gemstoneItemId != 2130))
          return false;
        modifyGemstoneCount = 30L;
        break;
      case 3:
        if ((player.getLevel() < 61) || (gemstoneItemId != 2131))
          return false;
        modifyGemstoneCount = 20L;
        break;
      case 4:
        if ((player.getLevel() < 76) || (gemstoneItemId != 2131))
          return false;
        modifyGemstoneCount = 25L;
        break;
      case 5:
        if ((player.getLevel() < 80) || (gemstoneItemId != 2132))
          return false;
        if (targetItem.getTemplate().getCrystalCount() == 10394)
          modifyGemstoneCount = 36L;
        else
          modifyGemstoneCount = 28L;
        break;
      case 6:
        if ((player.getLevel() < 84) || (gemstoneItemId != 2132))
          return false;
        modifyGemstoneCount = 36L;
      }

    }

    switch (lifeStoneLevel)
    {
    case 1:
      if (player.getLevel() >= 46) break;
      return false;
    case 2:
      if (player.getLevel() >= 49) break;
      return false;
    case 3:
      if (player.getLevel() >= 52) break;
      return false;
    case 4:
      if (player.getLevel() >= 55) break;
      return false;
    case 5:
      if (player.getLevel() >= 58) break;
      return false;
    case 6:
      if (player.getLevel() >= 61) break;
      return false;
    case 7:
      if (player.getLevel() >= 64) break;
      return false;
    case 8:
      if (player.getLevel() >= 67) break;
      return false;
    case 9:
      if (player.getLevel() >= 70) break;
      return false;
    case 10:
      if (player.getLevel() >= 76) break;
      return false;
    case 11:
      if (player.getLevel() >= 80) break;
      return false;
    case 12:
      if (player.getLevel() >= 82) break;
      return false;
    case 13:
      if (player.getLevel() >= 84) break;
      return false;
    case 14:
      if (player.getLevel() >= 85) break;
      return false;
    case 15:
      if (player.getLevel() >= 85) break;
      return false;
    }

    if (!player.getInventory().destroyItemByObjectId(_gemstoneItemObjId, modifyGemstoneCount)) {
      return false;
    }

    if (!player.getInventory().destroyItemByObjectId(_refinerItemObjId, 1L)) {
      return false;
    }

    lifeStoneLevel = Math.min(lifeStoneLevel, 10) - 1;

    int augmentation = AugmentationData.getInstance().generateRandomAugmentation(lifeStoneLevel, lifeStoneGrade, targetItem.getTemplate().getBodyPart());

    boolean equipped = false;
    if ((equipped = targetItem.isEquipped())) {
      player.getInventory().unEquipItem(targetItem);
    }
    targetItem.setAugmentationId(augmentation);
    targetItem.setJdbcState(JdbcEntityState.UPDATED);
    targetItem.update();

    if (equipped) {
      player.getInventory().equipItem(targetItem);
    }
    player.sendPacket(new InventoryUpdate().addModifiedItem(targetItem));

    for (ShortCut sc : player.getAllShortCuts())
      if ((sc.getId() == targetItem.getObjectId()) && (sc.getType() == 1))
        player.sendPacket(new ShortCutRegister(player, sc));
    player.sendChanges();
    return true;
  }
}