package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ExConfirmVariationItem;

public final class RequestConfirmTargetItem extends L2GameClientPacket
{
  private int _itemObjId;

  protected void readImpl()
  {
    _itemObjId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPBD() < 200L) {
      return;
    }
    player.sCPBD();

    L2ItemInstance item = L2World.getInstance().getItem(_itemObjId);
    if (item == null) {
      return;
    }
    if (player.getLevel() < 46)
    {
      player.sendPacket(Static.WRONG_LVL_46);
      return;
    }

    if (item.isAugmented())
    {
      player.sendPacket(Static.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
      return;
    }

    if (!item.canBeAugmented())
    {
      player.sendPacket(Static.THIS_IS_NOT_A_SUITABLE_ITEM);
      return;
    }

    if (player.getPrivateStoreType() != 0)
    {
      player.sendPacket(Static.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
      return;
    }
    if (player.isDead())
    {
      player.sendPacket(Static.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
      return;
    }
    if (player.isParalyzed())
    {
      player.sendPacket(Static.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
      return;
    }
    if (player.isFishing())
    {
      player.sendPacket(Static.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
      return;
    }
    if (player.isSitting())
    {
      player.sendPacket(Static.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
      return;
    }

    player.sendPacket(new ExConfirmVariationItem(_itemObjId));
    player.sendPacket(Static.SELECT_THE_CATALYST_FOR_AUGMENTATION);
  }
}