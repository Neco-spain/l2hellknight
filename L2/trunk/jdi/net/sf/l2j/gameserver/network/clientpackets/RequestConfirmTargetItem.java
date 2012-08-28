package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExConfirmVariationItem;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;

public final class RequestConfirmTargetItem extends L2GameClientPacket
{
  private static final String _C__D0_29_REQUESTCONFIRMTARGETITEM = "[C] D0:29 RequestConfirmTargetItem";
  private int _itemObjId;

  protected void readImpl()
  {
    _itemObjId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    L2ItemInstance item = (L2ItemInstance)L2World.getInstance().findObject(_itemObjId);

    if (item == null) return;

    if (activeChar.getLevel() < 46)
    {
      activeChar.sendMessage("You have to be level 46 in order to augment an item");
      return;
    }

    int itemGrade = item.getItem().getItemGrade();
    int itemType = item.getItem().getType2();

    if (item.isAugmented())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN));
      return;
    }

    if ((itemGrade < 2) || (itemType != 0) || (!item.isDestroyable()) || (item.isShadowItem()))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM));
      return;
    }

    if (activeChar.getPrivateStoreType() != 0)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION));
      return;
    }
    if (activeChar.isDead())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD));
      return;
    }
    if (activeChar.isParalyzed())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED));
      return;
    }
    if (activeChar.isFishing())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING));
      return;
    }
    if (activeChar.isSitting())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN));
      return;
    }

    activeChar.sendPacket(new ExConfirmVariationItem(_itemObjId));
    activeChar.sendPacket(new SystemMessage(SystemMessageId.SELECT_THE_CATALYST_FOR_AUGMENTATION));
  }

  public String getType()
  {
    return "[C] D0:29 RequestConfirmTargetItem";
  }
}