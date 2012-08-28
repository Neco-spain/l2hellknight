package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.handler.items.IItemHandler;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.utils.Location;

public class RequestDropItem extends L2GameClientPacket
{
  private int _objectId;
  private long _count;
  private Location _loc;

  protected void readImpl()
  {
    _objectId = readD();
    _count = readQ();
    _loc = new Location(readD(), readD(), readD());
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if ((_count < 1L) || (_loc.isNull()))
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isActionsDisabled())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (!Config.ALLOW_DISCARDITEM)
    {
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestDropItem.Disallowed", activeChar, new Object[0]));
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
      return;
    }

    if ((activeChar.isSitting()) || (activeChar.isDropDisabled()))
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInTrade())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isFishing())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
      return;
    }

    if ((!activeChar.isInRangeSq(_loc, 22500L)) || (Math.abs(_loc.z - activeChar.getZ()) > 50))
    {
      activeChar.sendPacket(Msg.THAT_IS_TOO_FAR_FROM_YOU_TO_DISCARD);
      return;
    }

    ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
    if (item == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    if (!item.canBeDropped(activeChar, false))
    {
      activeChar.sendPacket(Msg.THAT_ITEM_CANNOT_BE_DISCARDED);
      return;
    }

    item.getTemplate().getHandler().dropItem(activeChar, item, _count, _loc);
  }
}