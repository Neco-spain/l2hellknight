package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Map;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.PcFreight;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.PackageToList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.WareHouseDepositList;
import net.sf.l2j.gameserver.network.serverpackets.WareHouseWithdrawalList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2WarehouseInstance extends L2FolkInstance
{
  public L2WarehouseInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";
    if (val == 0)
    {
      pom = "" + npcId;
    }
    else
    {
      pom = npcId + "-" + val;
    }
    return "data/html/warehouse/" + pom + ".htm";
  }

  private void showRetrieveWindow(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    player.setActiveWarehouse(player.getWarehouse());

    if (player.getActiveWarehouse().getSize() == 0)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
      return;
    }

    if (Config.DEBUG) _log.fine("Showing stored items");
    player.sendPacket(new WareHouseWithdrawalList(player, 1));
  }

  private void showDepositWindow(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    player.setActiveWarehouse(player.getWarehouse());
    player.tempInvetoryDisable();
    if (Config.DEBUG) _log.fine("Showing items to deposit");

    player.sendPacket(new WareHouseDepositList(player, 1));
  }

  private void showDepositWindowClan(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    if (player.getClan() != null)
    {
      if (player.getClan().getLevel() == 0) {
        player.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
      }
      else {
        player.setActiveWarehouse(player.getClan().getWarehouse());
        player.tempInvetoryDisable();
        if (Config.DEBUG) _log.fine("Showing items to deposit - clan");

        WareHouseDepositList dl = new WareHouseDepositList(player, 2);
        player.sendPacket(dl);
      }
    }
  }

  private void showWithdrawWindowClan(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    if ((player.getClanPrivileges() & 0x8) != 8)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE));
      return;
    }

    if (player.getClan().getLevel() == 0) {
      player.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
    }
    else {
      player.setActiveWarehouse(player.getClan().getWarehouse());
      if (Config.DEBUG) _log.fine("Showing items to deposit - clan");
      player.sendPacket(new WareHouseWithdrawalList(player, 2));
    }
  }

  private void showWithdrawWindowFreight(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    if (Config.DEBUG) _log.fine("Showing freightened items");

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
        player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
      }

    }
    else if (Config.DEBUG) _log.fine("no items freightened");
  }

  private void showDepositWindowFreight(L2PcInstance player)
  {
    if (player.getAccountChars().size() == 0)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CHARACTER_DOES_NOT_EXIST));
    }
    else
    {
      Map chars = player.getAccountChars();

      if (chars.size() < 1)
      {
        player.sendPacket(new ActionFailed());
        return;
      }

      player.sendPacket(new PackageToList(chars));

      if (Config.DEBUG)
        _log.fine("Showing destination chars to freight - char src: " + player.getName());
    }
  }

  private void showDepositWindowFreight(L2PcInstance player, int obj_Id)
  {
    player.sendPacket(new ActionFailed());
    L2PcInstance destChar = L2PcInstance.load(obj_Id);
    if (destChar == null)
    {
      if (Config.DEBUG) {
        _log.warning("Error retrieving a target object for char " + player.getName() + " - using freight.");
      }
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

    if (Config.DEBUG) _log.fine("Showing items to freight");
    player.sendPacket(new WareHouseDepositList(player, 4));
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (player.getActiveEnchantItem() != null)
    {
      player.sendPacket(new ActionFailed());
      player.sendMessage("Close your ecnhant window first!");
      return;
    }
    if (player.isDead())
    {
      player.sendPacket(new ActionFailed());
      player.sendMessage("Cannot use WH while dead!");

      return;
    }
    if (command.startsWith("WithdrawP"))
    {
      showRetrieveWindow(player);
    }
    else if (command.equals("DepositP"))
    {
      showDepositWindow(player);
    }
    else if (command.equals("WithdrawC"))
    {
      showWithdrawWindowClan(player);
    }
    else if (command.equals("DepositC"))
    {
      showDepositWindowClan(player);
    }
    else if (command.startsWith("WithdrawF"))
    {
      if (Config.ALLOW_FREIGHT)
      {
        showWithdrawWindowFreight(player);
      }
    }
    else if (command.startsWith("DepositF"))
    {
      if (Config.ALLOW_FREIGHT)
      {
        showDepositWindowFreight(player);
      }
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
    else
    {
      super.onBypassFeedback(player, command);
    }
  }
}