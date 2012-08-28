package l2m.gameserver.utils;

import java.util.List;
import l2m.gameserver.Config;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.instancemanager.ReflectionManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.Zone.ZoneType;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.items.TradeItem;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.templates.item.ItemTemplate;

public final class TradeHelper
{
  public static boolean checksIfCanOpenStore(Player player, int storeType)
  {
    if (!player.getPlayerAccess().UseTrade)
    {
      player.sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
      return false;
    }

    if (player.getLevel() < Config.SERVICES_TRADE_MIN_LEVEL)
    {
      player.sendMessage(new CustomMessage("trade.NotHavePermission", player, new Object[0]).addNumber(Config.SERVICES_TRADE_MIN_LEVEL));
      return false;
    }

    String tradeBan = player.getVar("tradeBan");
    if ((tradeBan != null) && ((tradeBan.equals("-1")) || (Long.parseLong(tradeBan) >= System.currentTimeMillis())))
    {
      player.sendPacket(Msg.YOU_ARE_CURRENTLY_BANNED_FROM_ACTIVITIES_RELATED_TO_THE_PRIVATE_STORE_AND_PRIVATE_WORKSHOP);
      return false;
    }

    String BLOCK_ZONE = storeType == 5 ? "open_private_workshop" : "open_private_store";
    if ((player.isActionBlocked(BLOCK_ZONE)) && (
      (!Config.SERVICES_NO_TRADE_ONLY_OFFLINE) || ((Config.SERVICES_NO_TRADE_ONLY_OFFLINE) && (player.isInOfflineMode()))))
    {
      player.sendPacket(storeType == 5 ? new SystemMessage(1297) : Msg.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_IN_THIS_AREA);
      return false;
    }

    if (player.isCastingNow())
    {
      player.sendPacket(Msg.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL);
      return false;
    }

    if (player.isInCombat())
    {
      player.sendPacket(Msg.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
      return false;
    }

    if ((player.isActionsDisabled()) || (player.isMounted()) || (player.isInOlympiadMode()) || (player.isInDuel()) || (player.isProcessingRequest())) {
      return false;
    }
    if (Config.SERVICES_TRADE_ONLY_FAR)
    {
      boolean tradenear = false;
      for (Player p : World.getAroundPlayers(player, Config.SERVICES_TRADE_RADIUS, 200)) {
        if (p.isInStoreMode())
        {
          tradenear = true;
          break;
        }
      }
      if (World.getAroundNpc(player, Config.SERVICES_TRADE_RADIUS + 100, 200).size() > 0) {
        tradenear = true;
      }
      if (tradenear)
      {
        player.sendMessage(new CustomMessage("trade.OtherTradersNear", player, new Object[0]));
        return false;
      }
    }

    return true;
  }

  public static final void purchaseItem(Player buyer, Player seller, TradeItem item)
  {
    long price = item.getCount() * item.getOwnersPrice();
    if (!item.getItem().isStackable())
    {
      if (item.getEnchantLevel() > 0)
      {
        seller.sendPacket(new SystemMessage(1155).addString(buyer.getName()).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()).addNumber(price));
        buyer.sendPacket(new SystemMessage(1156).addString(seller.getName()).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()).addNumber(price));
      }
      else
      {
        seller.sendPacket(new SystemMessage(1151).addString(buyer.getName()).addItemName(item.getItemId()).addNumber(price));
        buyer.sendPacket(new SystemMessage(1153).addString(seller.getName()).addItemName(item.getItemId()).addNumber(price));
      }
    }
    else
    {
      seller.sendPacket(new SystemMessage(1152).addString(buyer.getName()).addItemName(item.getItemId()).addNumber(item.getCount()).addNumber(price));
      buyer.sendPacket(new SystemMessage(1154).addString(seller.getName()).addItemName(item.getItemId()).addNumber(item.getCount()).addNumber(price));
    }
  }

  public static final long getTax(Player seller, long price)
  {
    long tax = ()(price * Config.SERVICES_TRADE_TAX / 100.0D);
    if (seller.isInZone(Zone.ZoneType.offshore))
      tax = ()(price * Config.SERVICES_OFFSHORE_TRADE_TAX / 100.0D);
    if ((Config.SERVICES_TRADE_TAX_ONLY_OFFLINE) && (!seller.isInOfflineMode()))
      tax = 0L;
    if ((Config.SERVICES_PARNASSUS_NOTAX) && (seller.getReflection() == ReflectionManager.PARNASSUS)) {
      tax = 0L;
    }
    return tax;
  }

  public static void cancelStore(Player activeChar)
  {
    activeChar.setPrivateStoreType(0);
    if (activeChar.isInOfflineMode())
    {
      activeChar.setOfflineMode(false);
      activeChar.kick();
    }
    else {
      activeChar.broadcastCharInfo();
    }
  }
}