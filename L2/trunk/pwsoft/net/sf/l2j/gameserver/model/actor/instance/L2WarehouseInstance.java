package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.PcFreight;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.PackageToList;
import net.sf.l2j.gameserver.network.serverpackets.WareHouseDepositList;
import net.sf.l2j.gameserver.network.serverpackets.WareHouseWithdrawalList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2WarehouseInstance extends L2FolkInstance
{
  public L2WarehouseInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (player.getActiveEnchantItem() != null)
    {
      player.setActiveEnchantItem(null);
      player.sendPacket(new EnchantResult(0, true));
    }

    if (player.getActiveTradeList() != null)
    {
      player.cancelActiveTrade();
    }

    if (command.equals("DepositP"))
      showDepositWindow(player);
    else if (command.startsWith("WithdrawP"))
      showRetrieveWindow(player);
    else if (command.equals("DepositC"))
    {
      if (player.getClan() == null)
        player.sendHtmlMessage("<font color=\"009900\">Warehouse Keeper " + getName() + "</font>", "\u0412\u0440\u044F\u0442\u043B\u0438 \u043C\u044B \u0441\u043C\u043E\u0436\u0435\u043C \u0441 \u0432\u0430\u043C\u0438 \u0434\u043E\u0433\u043E\u0432\u043E\u0440\u0438\u0442\u044C\u0441\u044F.");
      else
        showDepositWindowClan(player);
    }
    else if (command.equals("WithdrawC"))
    {
      if (player.getClan() == null)
        player.sendHtmlMessage("<font color=\"009900\">Warehouse Keeper " + getName() + "</font>", "\u0427\u0442\u043E, \u044F \u0432\u044B\u0433\u043B\u044F\u0436\u0443 \u0442\u0430\u043A\u0438\u043C \u0434\u0443\u0440\u0430\u043A\u043E\u043C? \u0417\u0434\u0435\u0441\u044C \u043D\u0438\u0447\u0435\u0433\u043E \u043D\u0435\u0442 \u043D\u0430 \u0432\u0430\u0448\u0435 \u0438\u043C\u044F, \u0438\u0434\u0438\u0442\u0435 \u0438\u0449\u0438\u0442\u0435 \u043A\u0443\u0434\u0430-\u043D\u0438\u0431\u0443\u0434\u044C \u0432 \u0434\u0440\u0443\u0433\u043E\u0435 \u043C\u0435\u0441\u0442\u043E!");
      else
        showWithdrawWindowClan(player);
    }
    else if (command.startsWith("WithdrawF"))
    {
      if (Config.ALLOW_FREIGHT)
        showWithdrawWindowFreight(player);
    }
    else if (command.startsWith("DepositF"))
    {
      if (Config.ALLOW_FREIGHT)
        showDepositWindowFreight(player);
    }
    else if (command.startsWith("FreightChar"))
    {
      if (Config.ALLOW_FREIGHT)
      {
        int startOfId = command.lastIndexOf("_") + 1;
        String id = command.substring(startOfId);
        showDepositWindowFreight(player, Integer.parseInt(id));
      }
    }
    else {
      super.onBypassFeedback(player, command);
    }
    player.sendActionFailed();
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";
    if (val == 0)
      pom = "" + npcId;
    else
      pom = npcId + "-" + val;
    return "data/html/warehouse/" + pom + ".htm";
  }

  private void showRetrieveWindow(L2PcInstance player)
  {
    player.setActiveWarehouse(player.getWarehouse());

    player.sendPacket(new WareHouseWithdrawalList(player, 1));
  }

  private void showDepositWindow(L2PcInstance player)
  {
    player.setActiveWarehouse(player.getWarehouse());
    player.tempInvetoryDisable();

    player.sendPacket(new WareHouseDepositList(player, 1));
  }

  private void showDepositWindowClan(L2PcInstance player)
  {
    if (player.getClan() != null)
    {
      if (player.getClan().getLevel() == 0) {
        player.sendPacket(Static.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
      }
      else {
        player.setActiveWarehouse(player.getClan().getWarehouse());
        player.tempInvetoryDisable();

        WareHouseDepositList dl = new WareHouseDepositList(player, 2);
        player.sendPacket(dl);
      }
    }
  }

  private void showWithdrawWindowClan(L2PcInstance player)
  {
    if ((player.getClanPrivileges() & 0x8) != 8)
    {
      player.sendPacket(Static.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
      return;
    }

    if (player.getClan().getLevel() == 0) {
      player.sendPacket(Static.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
    }
    else {
      player.setActiveWarehouse(player.getClan().getWarehouse());

      player.sendPacket(new WareHouseWithdrawalList(player, 2));
    }
  }

  private void showWithdrawWindowFreight(L2PcInstance player)
  {
    PcFreight freight = player.getFreight();
    if (freight != null)
    {
      if (freight.getSize() > 0)
      {
        if (Config.ALT_GAME_FREIGHTS)
        {
          freight.setActiveLocation(0);
        }
        else {
          freight.setActiveLocation(getWorldRegion().hashCode());
        }
        player.setActiveWarehouse(freight);
        player.sendPacket(new WareHouseWithdrawalList(player, 4));
      }
      else
      {
        player.sendPacket(Static.NO_ITEM_DEPOSITED_IN_WH);
      }
    }
  }

  private void showDepositWindowFreight(L2PcInstance player)
  {
    player.sendPacket(new PackageToList());
  }

  private void showDepositWindowFreight(L2PcInstance player, int obj_Id)
  {
    L2PcInstance destChar = L2PcInstance.load(obj_Id);
    if (destChar == null)
    {
      return;
    }

    PcFreight freight = destChar.getFreight();
    if (Config.ALT_GAME_FREIGHTS)
    {
      freight.setActiveLocation(0);
    }
    else {
      freight.setActiveLocation(getWorldRegion().hashCode());
    }
    player.setActiveWarehouse(freight);
    player.tempInvetoryDisable();
    destChar.deleteMe();

    player.sendPacket(new WareHouseDepositList(player, 4));
  }
}