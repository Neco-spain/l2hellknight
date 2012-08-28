package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.TradeList.TradeItem;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.TradeOtherAdd;
import net.sf.l2j.gameserver.network.serverpackets.TradeOwnAdd;
import net.sf.l2j.gameserver.network.serverpackets.TradeUpdate;

public final class AddTradeItem extends L2GameClientPacket
{
  private static final String _C__16_ADDTRADEITEM = "[C] 16 AddTradeItem";
  private static Logger _log = Logger.getLogger(AddTradeItem.class.getName());
  private int _tradeId;
  private int _objectId;
  private int _count;

  protected void readImpl()
  {
    _tradeId = readD();
    _objectId = readD();
    _count = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    TradeList trade = player.getActiveTradeList();
    if (trade == null)
    {
      _log.warning("Character: " + player.getName() + " requested item:" + _objectId + " add without active tradelist:" + _tradeId);

      return;
    }

    if ((trade.getPartner() == null) || (L2World.getInstance().findObject(trade.getPartner().getObjectId()) == null))
    {
      if (trade.getPartner() != null)
        _log.warning("Character:" + player.getName() + " requested invalid trade object: " + _objectId);
      SystemMessage msg = new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
      player.sendPacket(msg);
      player.cancelActiveTrade();
      return;
    }

    if ((Config.GM_DISABLE_TRANSACTION) && (player.getAccessLevel() >= Config.GM_TRANSACTION_MIN) && (player.getAccessLevel() <= Config.GM_TRANSACTION_MAX))
    {
      player.sendMessage("Transactions are disable for your Access Level");
      player.cancelActiveTrade();
      return;
    }

    if (!player.isInsideRadius(trade.getPartner(), 150, true, false))
    {
      player.cancelActiveTrade();
      player.sendPacket(new ActionFailed());
      return;
    }

    if (!player.validateItemManipulation(_objectId, "trade"))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
      return;
    }

    if (trade.isConfirmed())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
      return;
    }

    if (trade.getTradeItem(_objectId) != null)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
      return;
    }

    L2ItemInstance _tmpitem = ItemContainer.getItemByObjectId(_objectId, player.getInventory());
    if (_tmpitem == null)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
      return;
    }

    int _inventoryItemsCount = _tmpitem.getCount();

    if (_count > _inventoryItemsCount) {
      _count = _inventoryItemsCount;
    }
    TradeList.TradeItem item = null;

    if ((player.getInventory().getItemByObjectId(_objectId) == null) || (_count <= 0))
    {
      _log.info("JES: Player " + player.getName() + " tried to trade exploit.");
      return;
    }

    item = trade.addItem(_objectId, _count);
    if (item != null)
    {
      _inventoryItemsCount -= item.getCount();
      player.sendPacket(new TradeOwnAdd(item));
      player.sendPacket(new TradeUpdate(trade, player));
      trade.getPartner().sendPacket(new TradeOtherAdd(item));
      return;
    }
  }

  public String getType()
  {
    return "[C] 16 AddTradeItem";
  }
}