package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExPutCommissionResultForVariationMake;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.item.ItemTemplate.Grade;
import l2p.gameserver.utils.ItemFunctions;

public class RequestConfirmGemStone extends L2GameClientPacket
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
    if (_gemstoneCount <= 0L) {
      return;
    }
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
    ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
    ItemInstance gemstoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);

    if ((targetItem == null) || (refinerItem == null) || (gemstoneItem == null))
    {
      activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
      return;
    }

    int gemstoneItemId = gemstoneItem.getTemplate().getItemId();
    if ((gemstoneItemId != 2130) && (gemstoneItemId != 2131) && (gemstoneItemId != 2132))
    {
      activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
      return;
    }

    boolean isAccessoryLifeStone = ItemFunctions.isAccessoryLifeStone(refinerItem.getItemId());
    if (!targetItem.canBeAugmented(activeChar, isAccessoryLifeStone))
    {
      activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
      return;
    }

    if ((!isAccessoryLifeStone) && (!ItemFunctions.isLifeStone(refinerItem.getItemId())))
    {
      activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
      return;
    }

    ItemTemplate.Grade itemGrade = targetItem.getTemplate().getItemGrade();

    if (isAccessoryLifeStone) {
      switch (1.$SwitchMap$l2p$gameserver$templates$item$ItemTemplate$Grade[itemGrade.ordinal()])
      {
      case 1:
        if ((_gemstoneCount == 200L) && (gemstoneItemId == 2130))
          break;
        activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
        return;
      case 2:
        if ((_gemstoneCount == 300L) && (gemstoneItemId == 2130))
          break;
        activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
        return;
      case 3:
        if ((_gemstoneCount == 200L) && (gemstoneItemId == 2131))
          break;
        activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
        return;
      case 4:
        if ((_gemstoneCount == 250L) && (gemstoneItemId == 2131))
          break;
        activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
        return;
      case 5:
        if ((_gemstoneCount == 250L) && (gemstoneItemId == 2132))
          break;
        activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
        return;
      case 6:
        if ((_gemstoneCount == 250L) && (gemstoneItemId == 2132))
          break;
        activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
        return;
      }
    }
    else
    {
      switch (1.$SwitchMap$l2p$gameserver$templates$item$ItemTemplate$Grade[itemGrade.ordinal()])
      {
      case 1:
        if ((_gemstoneCount == 20L) && (gemstoneItemId == 2130))
          break;
        activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
        return;
      case 2:
        if ((_gemstoneCount == 30L) && (gemstoneItemId == 2130))
          break;
        activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
        return;
      case 3:
        if ((_gemstoneCount == 20L) && (gemstoneItemId == 2131))
          break;
        activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
        return;
      case 4:
        if ((_gemstoneCount == 25L) && (gemstoneItemId == 2131))
          break;
        activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
        return;
      case 5:
        if ((targetItem.getTemplate().getCrystalCount() == 10394) && ((_gemstoneCount != 36L) || (gemstoneItemId != 2132)))
        {
          activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
          return;
        }

        if ((_gemstoneCount == 28L) && (gemstoneItemId == 2132))
          break;
        activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
        return;
      case 6:
        if ((_gemstoneCount == 36L) && (gemstoneItemId == 2132))
          break;
        activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
        return;
      }

    }

    activeChar.sendPacket(new IStaticPacket[] { new ExPutCommissionResultForVariationMake(_gemstoneItemObjId, _gemstoneCount), Msg.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN });
  }
}