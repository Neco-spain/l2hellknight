package net.sf.l2j.gameserver.network.clientpackets;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.CropProcure;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2ManorManagerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;

public class RequestBuyProcure extends L2GameClientPacket
{
  private int _listId;
  private int _count;
  private int[] _items;
  private List<CastleManorManager.CropProcure> _procureList = new FastList();

  protected void readImpl()
  {
    _listId = readD();
    _count = readD();
    if (_count > 500)
    {
      _count = 0;
      return;
    }

    _items = new int[_count * 2];
    for (int i = 0; i < _count; i++)
    {
      long servise = readD();
      int itemId = readD(); _items[(i * 2 + 0)] = itemId;
      long cnt = readD();
      if ((cnt > 2147483647L) || (cnt < 1L))
      {
        _count = 0; _items = null;
        return;
      }
      _items[(i * 2 + 1)] = (int)cnt;
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP) && (player.getKarma() > 0)) return;

    L2Object target = player.getTarget();

    if (_count < 1)
    {
      player.sendActionFailed();
      return;
    }

    long subTotal = 0L;
    int tax = 0;

    int slots = 0;
    int weight = 0;
    L2ManorManagerInstance manor = (target != null) && ((target instanceof L2ManorManagerInstance)) ? (L2ManorManagerInstance)target : null;

    for (int i = 0; i < _count; i++)
    {
      int itemId = _items[(i * 2 + 0)];
      int count = _items[(i * 2 + 1)];
      int price = 0;
      if (count > 2147483647)
      {
        player.sendPacket(Static.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
        return;
      }

      L2Item template = ItemTable.getInstance().getTemplate(L2Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, 0).getReward()));

      weight += count * template.getWeight();

      if (!template.isStackable()) { slots += count; } else {
        if (player.getInventory().getItemByItemId(itemId) != null) continue; slots++;
      }
    }
    if (!player.getInventory().validateWeight(weight))
    {
      player.sendPacket(Static.WEIGHT_LIMIT_EXCEEDED);
      return;
    }

    if (!player.getInventory().validateCapacity(slots))
    {
      player.sendPacket(Static.SLOTS_FULL);
      return;
    }

    InventoryUpdate playerIU = new InventoryUpdate();
    _procureList = manor.getCastle().getCropProcure(0);

    for (int i = 0; i < _count; i++)
    {
      int itemId = _items[(i * 2 + 0)];
      int count = _items[(i * 2 + 1)];
      if (count < 0) count = 0;

      int rewradItemId = L2Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, 0).getReward());

      int rewradItemCount = 1;

      rewradItemCount = count / rewradItemCount;

      L2ItemInstance item = player.getInventory().addItem("Manor", rewradItemId, rewradItemCount, player, manor);
      L2ItemInstance iteme = player.getInventory().destroyItemByItemId("Manor", itemId, count, player, manor);

      if ((item == null) || (iteme == null)) {
        continue;
      }
      playerIU.addRemovedItem(iteme);
      if (item.getCount() > rewradItemCount) playerIU.addModifiedItem(item); else {
        playerIU.addNewItem(item);
      }

      player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(rewradItemId).addNumber(rewradItemCount));
    }

    player.sendPacket(playerIU);

    StatusUpdate su = new StatusUpdate(player.getObjectId());
    su.addAttribute(14, player.getCurrentLoad());
    player.sendPacket(su);
  }
}