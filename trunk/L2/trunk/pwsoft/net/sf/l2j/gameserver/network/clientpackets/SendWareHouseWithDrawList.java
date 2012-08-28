package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.ClanWarehouse;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Log;

public final class SendWareHouseWithDrawList extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(SendWareHouseWithDrawList.class.getName());
  private int _count;
  private int[] _items;
  private int[] counts;

  protected void readImpl()
  {
    _count = readD();
    if ((_count * 8 > _buf.remaining()) || (_count > 32767) || (_count <= 0))
    {
      _items = null;
      return;
    }
    _items = new int[_count * 2];
    counts = new int[_count];
    for (int i = 0; i < _count; i++)
    {
      _items[(i * 2 + 0)] = readD();
      _items[(i * 2 + 1)] = readD();
      if (_items[(i * 2 + 1)] > 0)
        continue;
      _items = null;
      break;
    }
  }

  protected void runImpl()
  {
    if (_items == null) {
      return;
    }
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPAZ() < 100L) {
      return;
    }
    player.sCPAZ();

    ItemContainer warehouse = player.getActiveWarehouse();
    if (warehouse == null) {
      return;
    }
    L2FolkInstance manager = player.getLastFolkNPC();
    if (((manager == null) || (!player.isInsideRadius(manager, 150, false, false))) && (!player.isGM())) {
      return;
    }

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE) && (player.getKarma() > 0)) return;

    if (Config.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH)
    {
      if (((warehouse instanceof ClanWarehouse)) && ((player.getClanPrivileges() & 0x8) != 8))
      {
        return;
      }

    }
    else if (((warehouse instanceof ClanWarehouse)) && (!player.isClanLeader()))
    {
      player.sendPacket(Static.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE);
      return;
    }

    if ((warehouse instanceof ClanWarehouse))
    {
      if (warehouse.getOwnerId() != player.getClan().getClanId())
      {
        String[] noway = { "\u041E\u0445\u0440\u0430\u043D\u043D\u0438\u043A\u0438! \u041D\u0435\u043C\u0435\u0434\u043B\u0435\u043D\u043D\u043E \u043E\u0442\u0433\u043E\u043D\u0438\u0442\u0435 \u044D\u0442\u043E\u0433\u043E \u0447\u0435\u043B\u043E\u0432\u0435\u043A\u0430!", "\u0412\u044B \u0443\u0432\u0435\u0440\u0435\u043D\u044B, \u0447\u0442\u043E \u043D\u0435 \u043E\u0448\u0438\u0431\u043B\u0438\u0441\u044C? \u0423 \u043D\u0430\u0441 \u043D\u0430 \u0445\u0440\u0430\u043D\u0435\u043D\u0438\u0438 \u043D\u0435\u0442 \u0432\u0430\u0448\u0438\u0445 \u0432\u0435\u0449\u0435\u0439. \u0412\u043E\u0437\u043C\u043E\u0436\u043D\u043E, \u0432\u0430\u0441 \u043F\u043E\u0434\u0432\u043E\u0434\u0438\u0442 \u043F\u0430\u043C\u044F\u0442\u044C.", "\u0427\u0442\u043E, \u044F \u0432\u044B\u0433\u043B\u044F\u0436\u0443 \u0442\u0430\u043A\u0438\u043C \u0434\u0443\u0440\u0430\u043A\u043E\u043C? \u0417\u0434\u0435\u0441\u044C \u043D\u0438\u0447\u0435\u0433\u043E \u043D\u0435\u0442 \u043D\u0430 \u0432\u0430\u0448\u0435 \u0438\u043C\u044F, \u0438\u0434\u0438\u0442\u0435 \u0438\u0449\u0438\u0442\u0435 \u043A\u0443\u0434\u0430-\u043D\u0438\u0431\u0443\u0434\u044C \u0432 \u0434\u0440\u0443\u0433\u043E\u0435 \u043C\u0435\u0441\u0442\u043E!" };
        player.sendHtmlMessage("<font color=\"009900\">Warehouse Keeper " + manager.getName() + "</font>", noway[net.sf.l2j.util.Rnd.get(noway.length - 1)]);
        return;
      }
    }

    int weight = 0;
    int finalCount = player.getInventory().getSize();
    L2ItemInstance[] olditems = new L2ItemInstance[_count];

    for (int i = 0; i < _count; i++)
    {
      int itemObjId = _items[(i * 2 + 0)];
      int count = _items[(i * 2 + 1)];
      L2ItemInstance oldinst = L2ItemInstance.restoreFromDb(itemObjId);

      if (count <= 0)
      {
        player.sendPacket(Static.WRONG_COUNT);
        return;
      }

      if (oldinst == null)
      {
        player.sendPacket(Static.ITEM_NOT_FOUND);
        for (int f = 0; f < i; f++)
          L2World.getInstance().removeObject(olditems[i]);
        return;
      }

      if (oldinst.getCount() < count)
      {
        count = oldinst.getCount();
      }

      counts[i] = count;
      olditems[i] = oldinst;
      weight += oldinst.getItem().getWeight() * count;
      finalCount++;

      if (oldinst.isShadowItem()) {
        oldinst.setOwnerId(player.getObjectId());
      }
      if ((oldinst.getItem().isStackable()) && (player.getInventory().getItemByItemId(oldinst.getItemId()) != null)) {
        finalCount--;
      }

    }

    String date = "";
    TextBuilder tb = null;
    if (Config.LOG_ITEMS)
    {
      date = Log.getTime();
      tb = new TextBuilder();
    }
    for (int i = 0; i < olditems.length; i++)
    {
      L2ItemInstance TransferItem = warehouse.getItemByObj(olditems[i].getObjectId(), counts[i], player);
      if (TransferItem == null)
      {
        _log.warning("Error getItem from warhouse player: " + player.getName());
      }
      else {
        L2ItemInstance item = player.getInventory().addItem("WH finish", TransferItem, player, player.getLastFolkNPC());
        if ((!Config.LOG_ITEMS) || (item == null))
          continue;
        String act = "WITHDRAW " + item.getItemName() + "(" + counts[i] + ")(+" + item.getEnchantLevel() + ")(" + item.getObjectId() + ")(npc:" + manager.getTemplate().npcId + ") #(player " + player.getName() + ", account: " + player.getAccountName() + ", ip: " + player.getIP() + ", hwid: " + player.getHWID() + ")";
        tb.append(date + act + "\n");
      }

    }

    if ((Config.LOG_ITEMS) && (tb != null))
    {
      Log.item(tb.toString(), 2);
      tb.clear();
      tb = null;
    }

    player.sendItems(true);
    player.sendChanges();
  }
}