package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExPutItemResultForVariationCancel;

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