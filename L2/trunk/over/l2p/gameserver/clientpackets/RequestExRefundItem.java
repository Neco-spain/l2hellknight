package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import l2p.commons.math.SafeMath;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.ItemContainer;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExBuySellList.SellRefundList;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;

public class RequestExRefundItem extends L2GameClientPacket
{
  private int _listId;
  private int _count;
  private int[] _items;

  protected void readImpl()
  {
    _listId = readD();
    _count = readD();
    if ((_count * 4 > _buf.remaining()) || (_count > 32767) || (_count < 1))
    {
      _count = 0;
      return;
    }
    _items = new int[_count];
    for (int i = 0; i < _count; i++)
    {
      _items[i] = readD();
      if (ArrayUtils.indexOf(_items, _items[i]) >= i)
        continue;
      _count = 0;
      break;
    }
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (_count == 0)) {
      return;
    }
    if (activeChar.isActionsDisabled())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
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

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP) && (activeChar.getKarma() > 0) && (!activeChar.isGM()))
    {
      activeChar.sendActionFailed();
      return;
    }

    NpcInstance npc = activeChar.getLastNpc();

    boolean isValidMerchant = (npc != null) && (npc.isMerchantNpc());
    if ((!activeChar.isGM()) && ((npc == null) || (!isValidMerchant) || (!activeChar.isInRange(npc, 200L))))
    {
      activeChar.sendActionFailed();
      return;
    }

    activeChar.getInventory().writeLock();
    try
    {
      int slots = 0;
      long weight = 0L;
      long totalPrice = 0L;

      List refundList = new ArrayList();
      for (int objId : _items)
      {
        ItemInstance item = activeChar.getRefund().getItemByObjectId(objId);
        if (item == null) {
          continue;
        }
        totalPrice = SafeMath.addAndCheck(totalPrice, SafeMath.mulAndCheck(item.getCount(), item.getReferencePrice()) / 2L);
        weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getCount(), item.getTemplate().getWeight()));

        if ((!item.isStackable()) || (activeChar.getInventory().getItemByItemId(item.getItemId()) == null)) {
          slots++;
        }
        refundList.add(item);
      }

      if (refundList.isEmpty()) {
        activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
        activeChar.sendActionFailed();
        return;
      }if (!activeChar.getInventory().validateWeight(weight)) {
        sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
        activeChar.sendActionFailed();
        return;
      }if (!activeChar.getInventory().validateCapacity(slots)) {
        sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
        activeChar.sendActionFailed();
        return;
      }if (!activeChar.reduceAdena(totalPrice)) {
        activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        activeChar.sendActionFailed();
        return;
      }for (ItemInstance item : refundList)
      {
        ItemInstance refund = activeChar.getRefund().removeItem(item);
        Log.LogItem(activeChar, "RefundReturn", refund);
        activeChar.getInventory().addItem(refund);
      }
    }
    catch (ArithmeticException ae) {
      sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      return;
    }
    finally {
      activeChar.getInventory().writeUnlock();
    }

    activeChar.sendPacket(new ExBuySellList.SellRefundList(activeChar, true));
    activeChar.sendChanges();
  }
}