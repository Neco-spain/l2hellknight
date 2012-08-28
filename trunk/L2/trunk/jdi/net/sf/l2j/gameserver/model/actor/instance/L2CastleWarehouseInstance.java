package net.sf.l2j.gameserver.model.actor.instance;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.WareHouseDepositList;
import net.sf.l2j.gameserver.network.serverpackets.WareHouseWithdrawalList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2CastleWarehouseInstance extends L2FolkInstance
{
  protected static final int COND_ALL_FALSE = 0;
  protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
  protected static final int COND_OWNER = 2;

  public L2CastleWarehouseInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
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

    player.sendPacket(new WareHouseWithdrawalList(player, 1));
  }

  private void showDepositWindow(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    player.setActiveWarehouse(player.getWarehouse());
    player.tempInvetoryDisable();

    player.sendPacket(new WareHouseDepositList(player, 1));
  }

  private void showDepositWindowClan(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    if (player.getClan() != null)
    {
      if (player.getClan().getLevel() == 0)
      {
        player.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
      }
      else
      {
        if ((player.getClanPrivileges() & 0x8) != 8)
        {
          player.sendPacket(new SystemMessage(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE));
        }
        player.setActiveWarehouse(player.getClan().getWarehouse());
        player.tempInvetoryDisable();

        WareHouseDepositList dl = new WareHouseDepositList(player, 2);
        player.sendPacket(dl);
      }
    }
  }

  private void showWithdrawWindowClan(L2PcInstance player) {
    player.sendPacket(new ActionFailed());
    if ((player.getClanPrivileges() & 0x8) != 8) {
      player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE));
      return;
    }
    if (player.getClan().getLevel() == 0) {
      player.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
    } else {
      player.setActiveWarehouse(player.getClan().getWarehouse());
      player.sendPacket(new WareHouseWithdrawalList(player, 2));
    }
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (player.getActiveEnchantItem() != null)
    {
      _log.info("Player " + player.getName() + " trying to use enchant exploit, ban this player!");
      player.sendMessage("you trying to use enchant exploit, ban this player!");
      player.closeNetConnection(false);
      return;
    }

    if (command.startsWith("WithdrawP")) {
      showRetrieveWindow(player);
    } else if (command.equals("DepositP")) {
      showDepositWindow(player);
    } else if (command.equals("WithdrawC")) {
      showWithdrawWindowClan(player);
    } else if (command.equals("DepositC")) {
      showDepositWindowClan(player);
    } else if (command.startsWith("Chat"))
    {
      int val = 0;
      try
      {
        val = Integer.parseInt(command.substring(5));
      } catch (IndexOutOfBoundsException ioobe) {
      } catch (NumberFormatException nfe) {
      }
      showChatWindow(player, val);
    }
    else {
      super.onBypassFeedback(player, command);
    }
  }

  public void showChatWindow(L2PcInstance player, int val) {
    player.sendPacket(new ActionFailed());
    String filename = "data/html/castlewarehouse/castlewarehouse-no.htm";

    int condition = validateCondition(player);
    if (condition > 0) {
      if (condition == 1)
        filename = "data/html/castlewarehouse/castlewarehouse-busy.htm";
      else if (condition == 2)
      {
        if (val == 0)
          filename = "data/html/castlewarehouse/castlewarehouse.htm";
        else {
          filename = "data/html/castlewarehouse/castlewarehouse-" + val + ".htm";
        }
      }
    }
    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }

  protected int validateCondition(L2PcInstance player)
  {
    if (player.isGM()) return 2;
    if ((getCastle() != null) && (getCastle().getCastleId() > 0))
    {
      if (player.getClan() != null)
      {
        if (getCastle().getSiege().getIsInProgress())
          return 1;
        if (getCastle().getOwnerId() == player.getClanId())
          return 2;
      }
    }
    return 0;
  }
}