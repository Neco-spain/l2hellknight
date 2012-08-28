package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExPutItemResultForVariationMake;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;

public class RequestConfirmTargetItem extends L2GameClientPacket
{
  private int _itemObjId;

  protected void readImpl()
  {
    _itemObjId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjId);

    if (item == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.getLevel() < 46)
    {
      activeChar.sendMessage("You have to be level 46 in order to augment an item");
      return;
    }

    if (item.isAugmented())
    {
      activeChar.sendPacket(Msg.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
      return;
    }

    if (!item.canBeAugmented(activeChar, item.isAccessory()))
    {
      activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
      return;
    }
    if (activeChar.isInTrade())
    {
      activeChar.sendActionFailed();
      return;
    }
    if (activeChar.isDead())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
      return;
    }
    if (activeChar.isParalyzed())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
      return;
    }
    if (activeChar.isFishing())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
      return;
    }
    if (activeChar.isSitting())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
      return;
    }
    if (activeChar.isActionsDisabled())
    {
      activeChar.sendActionFailed();
      return;
    }
    activeChar.sendPacket(new IStaticPacket[] { new ExPutItemResultForVariationMake(_itemObjId), Msg.SELECT_THE_CATALYST_FOR_AUGMENTATION });
  }
}