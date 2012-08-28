package l2m.gameserver.utils;

import l2m.gameserver.Config;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.items.Warehouse;
import l2m.gameserver.model.items.Warehouse.WarehouseType;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.serverpackets.WareHouseDepositList;
import l2m.gameserver.network.serverpackets.WareHouseWithdrawList;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.item.ItemTemplate.ItemClass;

public final class WarehouseFunctions
{
  public static void showFreightWindow(Player player)
  {
    if (!canShowWarehouseWithdrawList(player, Warehouse.WarehouseType.FREIGHT))
    {
      player.sendActionFailed();
      return;
    }

    player.setUsingWarehouseType(Warehouse.WarehouseType.FREIGHT);
    player.sendPacket(new WareHouseWithdrawList(player, Warehouse.WarehouseType.FREIGHT, ItemTemplate.ItemClass.ALL));
  }

  public static void showRetrieveWindow(Player player, int val)
  {
    if (!canShowWarehouseWithdrawList(player, Warehouse.WarehouseType.PRIVATE))
    {
      player.sendActionFailed();
      return;
    }

    player.setUsingWarehouseType(Warehouse.WarehouseType.PRIVATE);
    player.sendPacket(new WareHouseWithdrawList(player, Warehouse.WarehouseType.PRIVATE, ItemTemplate.ItemClass.values()[val]));
  }

  public static void showDepositWindow(Player player)
  {
    if (!canShowWarehouseDepositList(player, Warehouse.WarehouseType.PRIVATE))
    {
      player.sendActionFailed();
      return;
    }

    player.setUsingWarehouseType(Warehouse.WarehouseType.PRIVATE);
    player.sendPacket(new WareHouseDepositList(player, Warehouse.WarehouseType.PRIVATE));
  }

  public static void showDepositWindowClan(Player player)
  {
    if (!canShowWarehouseDepositList(player, Warehouse.WarehouseType.CLAN))
    {
      player.sendActionFailed();
      return;
    }

    if ((!player.isClanLeader()) && (((!Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE) && (!player.getVarB("canWhWithdraw"))) || ((player.getClanPrivileges() & 0x8) != 8))) {
      player.sendPacket(Msg.ITEMS_LEFT_AT_THE_CLAN_HALL_WAREHOUSE_CAN_ONLY_BE_RETRIEVED_BY_THE_CLAN_LEADER_DO_YOU_WANT_TO_CONTINUE);
    }
    player.setUsingWarehouseType(Warehouse.WarehouseType.CLAN);
    player.sendPacket(new WareHouseDepositList(player, Warehouse.WarehouseType.CLAN));
  }

  public static void showWithdrawWindowClan(Player player, int val)
  {
    if (!canShowWarehouseWithdrawList(player, Warehouse.WarehouseType.CLAN))
    {
      player.sendActionFailed();
      return;
    }

    player.setUsingWarehouseType(Warehouse.WarehouseType.CLAN);
    player.sendPacket(new WareHouseWithdrawList(player, Warehouse.WarehouseType.CLAN, ItemTemplate.ItemClass.values()[val]));
  }

  public static boolean canShowWarehouseWithdrawList(Player player, Warehouse.WarehouseType type)
  {
    if (!player.getPlayerAccess().UseWarehouse) {
      return false;
    }
    Warehouse warehouse = null;
    switch (1.$SwitchMap$l2p$gameserver$model$items$Warehouse$WarehouseType[type.ordinal()])
    {
    case 1:
      warehouse = player.getWarehouse();
      break;
    case 2:
      warehouse = player.getFreight();
      break;
    case 3:
    case 4:
      if ((player.getClan() == null) || (player.getClan().getLevel() == 0))
      {
        player.sendPacket(Msg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
        return false;
      }

      boolean canWithdrawCWH = false;
      if ((player.getClan() != null) && 
        ((player.getClanPrivileges() & 0x8) == 8))
        canWithdrawCWH = true;
      if (!canWithdrawCWH)
      {
        player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE);
        return false;
      }
      warehouse = player.getClan().getWarehouse();
      break;
    default:
      return false;
    }

    if (warehouse.getSize() == 0)
    {
      player.sendPacket(type == Warehouse.WarehouseType.FREIGHT ? SystemMsg.NO_PACKAGES_HAVE_ARRIVED : Msg.YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE);
      return false;
    }

    return true;
  }

  public static boolean canShowWarehouseDepositList(Player player, Warehouse.WarehouseType type)
  {
    if (!player.getPlayerAccess().UseWarehouse) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$model$items$Warehouse$WarehouseType[type.ordinal()])
    {
    case 1:
      return true;
    case 3:
    case 4:
      if ((player.getClan() == null) || (player.getClan().getLevel() == 0))
      {
        player.sendPacket(Msg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
        return false;
      }

      boolean canWithdrawCWH = false;
      if ((player.getClan() != null) && 
        ((player.getClanPrivileges() & 0x8) == 8))
        canWithdrawCWH = true;
      if (!canWithdrawCWH)
      {
        player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE);
        return false;
      }
      return true;
    case 2:
    }return false;
  }
}