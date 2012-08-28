package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.TradeList.TradeItem;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.TradeOtherAdd;
import net.sf.l2j.gameserver.network.serverpackets.TradeOwnAdd;
import net.sf.l2j.gameserver.network.serverpackets.TradeUpdate;

public final class AddTradeItem extends L2GameClientPacket
{
  private static final Logger _log = Logger.getLogger(AddTradeItem.class.getName());
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
    if ((player == null) || (_count < 1)) {
      return;
    }

    if (System.currentTimeMillis() - player.gCPN() < 300L) {
      return;
    }

    player.sCPN();

    TradeList trade = player.getActiveTradeList();
    if (trade == null) {
      _log.warning("Character: " + player.getName() + " requested item:" + _objectId + " add without active tradelist:" + _tradeId);
      return;
    }

    if ((trade.getPartner() == null) || (L2World.getInstance().getPlayer(trade.getPartner().getObjectId()) == null))
    {
      if (trade.getPartner() != null) {
        _log.warning("Character:" + player.getName() + " requested invalid trade object: " + _objectId);
      }
      player.sendPacket(Static.TARGET_IS_NOT_FOUND_IN_THE_GAME);
      player.cancelActiveTrade();
      return;
    }

    L2ItemInstance InvItem = player.getInventory().getItemByObjectId(_objectId);

    if (!player.validateItemManipulation(_objectId, "trade")) {
      player.sendPacket(Static.NOTHING_HAPPENED);
      return;
    }

    TradeList.TradeItem item = trade.addItem(_objectId, _count);
    if (item != null) {
      player.sendPacket(new TradeOwnAdd(item));
      player.sendPacket(new TradeUpdate(InvItem));
      trade.getPartner().sendPacket(new TradeOtherAdd(item));
    }
  }
}