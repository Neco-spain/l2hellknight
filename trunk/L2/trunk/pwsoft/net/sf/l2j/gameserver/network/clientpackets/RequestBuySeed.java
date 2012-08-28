package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.SeedProduction;
import net.sf.l2j.gameserver.model.L2ItemInstance;
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

public class RequestBuySeed extends L2GameClientPacket
{
  private int _count;
  private int _manorId;
  private int[] _items;

  protected void readImpl()
  {
    _manorId = readD();
    _count = readD();

    if ((_count > 500) || (_count * 8 < _buf.remaining()))
    {
      _count = 0;
      return;
    }

    _items = new int[_count * 2];

    for (int i = 0; i < _count; i++)
    {
      int itemId = readD();
      _items[(i * 2 + 0)] = itemId;
      long cnt = readD();
      if ((cnt > 2147483647L) || (cnt < 1L))
      {
        _count = 0;
        _items = null;
        return;
      }
      _items[(i * 2 + 1)] = (int)cnt;
    }
  }

  protected void runImpl()
  {
    long totalPrice = 0L;
    int slots = 0;
    int totalWeight = 0;

    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null)
      return;
    if (_count < 1)
    {
      player.sendActionFailed();
      return;
    }

    L2Object target = player.getTarget();

    if (!(target instanceof L2ManorManagerInstance)) {
      target = player.getLastFolkNPC();
    }
    if (!(target instanceof L2ManorManagerInstance)) {
      return;
    }
    Castle castle = CastleManager.getInstance().getCastleById(_manorId);

    for (int i = 0; i < _count; i++)
    {
      int seedId = _items[(i * 2 + 0)];
      int count = _items[(i * 2 + 1)];
      int price = 0;
      int residual = 0;

      CastleManorManager.SeedProduction seed = castle.getSeed(seedId, 0);
      price = seed.getPrice();
      residual = seed.getCanProduce();

      if (price <= 0) {
        return;
      }
      if (residual < count) {
        return;
      }
      totalPrice += count * price;

      L2Item template = ItemTable.getInstance().getTemplate(seedId);
      totalWeight += count * template.getWeight();
      if (!template.isStackable())
        slots += count;
      else if (player.getInventory().getItemByItemId(seedId) == null) {
        slots++;
      }
    }
    if (totalPrice > 2147483647L)
    {
      return;
    }

    if (!player.getInventory().validateWeight(totalWeight))
    {
      player.sendPacket(Static.WEIGHT_LIMIT_EXCEEDED);
      return;
    }

    if (!player.getInventory().validateCapacity(slots))
    {
      player.sendPacket(Static.SLOTS_FULL);
      return;
    }

    if ((totalPrice < 0L) || (!player.reduceAdena("Buy", (int)totalPrice, target, false)))
    {
      player.sendPacket(Static.YOU_NOT_ENOUGH_ADENA);
      return;
    }

    castle.addToTreasuryNoTax((int)totalPrice);

    InventoryUpdate playerIU = new InventoryUpdate();
    for (int i = 0; i < _count; i++)
    {
      int seedId = _items[(i * 2 + 0)];
      int count = _items[(i * 2 + 1)];
      if (count < 0) {
        count = 0;
      }

      CastleManorManager.SeedProduction seed = castle.getSeed(seedId, 0);

      seed.setCanProduce(seed.getCanProduce() - count);
      if (Config.ALT_MANOR_SAVE_ALL_ACTIONS) {
        CastleManager.getInstance().getCastleById(_manorId).updateSeed(seed.getId(), seed.getCanProduce(), 0);
      }

      L2ItemInstance item = player.getInventory().addItem("Buy", seedId, count, player, target);

      if (item.getCount() > count)
        playerIU.addModifiedItem(item);
      else {
        playerIU.addNewItem(item);
      }

      player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(seedId).addNumber(count));
    }

    player.sendPacket(playerIU);

    StatusUpdate su = new StatusUpdate(player.getObjectId());
    su.addAttribute(14, player.getCurrentLoad());
    player.sendPacket(su);
  }
}