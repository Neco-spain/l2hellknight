package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExPutItemResultForVariationCancel;

public class RequestConfirmCancelItem extends L2GameClientPacket
{
  int _itemId;

  protected void readImpl()
  {
    _itemId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemId);

    if (item == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    if (!item.isAugmented())
    {
      activeChar.sendPacket(Msg.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
      return;
    }

    activeChar.sendPacket(new ExPutItemResultForVariationCancel(item));
  }
}