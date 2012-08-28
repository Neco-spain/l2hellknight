package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExPutIntensiveResultForVariationMake;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.item.ItemTemplate.Grade;
import l2p.gameserver.utils.ItemFunctions;

public class RequestConfirmRefinerItem extends L2GameClientPacket
{
  private static final int GEMSTONE_D = 2130;
  private static final int GEMSTONE_C = 2131;
  private static final int GEMSTONE_B = 2132;
  private int _targetItemObjId;
  private int _refinerItemObjId;

  protected void readImpl()
  {
    _targetItemObjId = readD();
    _refinerItemObjId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
    ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);

    if ((targetItem == null) || (refinerItem == null))
    {
      activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
      return;
    }

    int refinerItemId = refinerItem.getTemplate().getItemId();

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

    int gemstoneCount = 0;
    int gemstoneItemId = 0;

    if (isAccessoryLifeStone) {
      switch (1.$SwitchMap$l2p$gameserver$templates$item$ItemTemplate$Grade[itemGrade.ordinal()])
      {
      case 1:
        gemstoneCount = 200;
        gemstoneItemId = 2130;
        break;
      case 2:
        gemstoneCount = 300;
        gemstoneItemId = 2130;
        break;
      case 3:
        gemstoneCount = 200;
        gemstoneItemId = 2131;
        break;
      case 4:
        gemstoneCount = 250;
        gemstoneItemId = 2131;
        break;
      case 5:
        gemstoneCount = 250;
        gemstoneItemId = 2132;
        break;
      case 6:
        gemstoneCount = 250;
        gemstoneItemId = 2132;
      }
    }
    else {
      switch (1.$SwitchMap$l2p$gameserver$templates$item$ItemTemplate$Grade[itemGrade.ordinal()])
      {
      case 1:
        gemstoneCount = 20;
        gemstoneItemId = 2130;
        break;
      case 2:
        gemstoneCount = 30;
        gemstoneItemId = 2130;
        break;
      case 3:
        gemstoneCount = 20;
        gemstoneItemId = 2131;
        break;
      case 4:
        gemstoneCount = 25;
        gemstoneItemId = 2131;
        break;
      case 5:
        if (targetItem.getTemplate().getCrystalCount() == 10394)
          gemstoneCount = 36;
        else
          gemstoneCount = 28;
        gemstoneItemId = 2132;
        break;
      case 6:
        gemstoneCount = 36;
        gemstoneItemId = 2132;
      }
    }

    SystemMessage sm = new SystemMessage(1959).addNumber(gemstoneCount).addItemName(gemstoneItemId);
    activeChar.sendPacket(new IStaticPacket[] { new ExPutIntensiveResultForVariationMake(_refinerItemObjId, refinerItemId, gemstoneItemId, gemstoneCount), sm });
  }
}