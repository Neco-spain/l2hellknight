package net.sf.l2j.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.Auction;
import net.sf.l2j.gameserver.model.entity.Auction.Bidder;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;

public final class L2AuctioneerInstance extends L2FolkInstance
{
  private static final int COND_ALL_FALSE = 0;
  private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
  private static final int COND_REGULAR = 3;
  private Map<Integer, Auction> _pendingAuctions = new FastMap();

  public L2AuctioneerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onAction(L2PcInstance player)
  {
    if (!Config.ALT_ALLOW_AUC)
    {
      player.sendActionFailed();
      return;
    }

    if (!canTarget(player)) return;

    player.setLastFolkNPC(this);

    if (this != player.getTarget())
    {
      player.setTarget(this);

      player.sendPacket(new MyTargetSelected(getObjectId(), 0));

      player.sendPacket(new ValidateLocation(this));
    }
    else if (!canInteract(player))
    {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
    }
    else
    {
      if (!Config.ALT_ALLOW_AUC)
      {
        player.sendMessage("\u0410\u0443\u043A\u0446\u0438\u043E\u043D \u0431\u0443\u0434\u0435\u0442 \u0437\u0430\u043F\u0443\u0449\u0435\u043D \u0447\u0435\u0440\u0435\u0437 \u043F\u0430\u0440\u0443 \u0434\u043D\u0435\u0439");
        return;
      }
      showMessageWindow(player);
    }

    player.sendActionFailed();
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (!Config.ALT_ALLOW_AUC)
    {
      player.sendActionFailed();
      return;
    }

    int condition = validateCondition(player);

    if (condition == 0)
    {
      player.sendMessage("\u0412 \u0434\u0440\u0443\u0433\u043E\u0435 \u0432\u0440\u0435\u043C\u044F");
      return;
    }
    if (condition == 1)
    {
      player.sendMessage("\u041E\u0441\u0430\u0434\u0430!");
      return;
    }
    if (condition == 3)
    {
      StringTokenizer st = new StringTokenizer(command, " ");
      String actualCommand = st.nextToken();

      String val = "";
      if (st.countTokens() >= 1)
      {
        val = st.nextToken();
      }

      if (actualCommand.equalsIgnoreCase("auction"))
      {
        if (val == "") return;

        try
        {
          int days = Integer.parseInt(val);
          try
          {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            int bid = 0;
            if (st.countTokens() >= 1) bid = Integer.parseInt(st.nextToken());

            Auction a = new Auction(player.getClan().getHasHideout(), player.getClan(), days * 86400000L, bid, ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getName());
            if (_pendingAuctions.get(Integer.valueOf(a.getId())) != null) {
              _pendingAuctions.remove(Integer.valueOf(a.getId()));
            }
            _pendingAuctions.put(Integer.valueOf(a.getId()), a);

            String filename = "data/html/auction/AgitSale3.htm";
            NpcHtmlMessage html = NpcHtmlMessage.id(1);
            html.setFile(filename);
            html.replace("%x%", val);
            html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(Long.valueOf(a.getEndDate()))));
            html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
            html.replace("%AGIT_AUCTION_MIN%", String.valueOf(a.getStartingBid()));
            html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getDesc());
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale2");
            html.replace("%objectId%", String.valueOf(getObjectId()));
            player.sendPacket(html);
          }
          catch (Exception e)
          {
            player.sendMessage("\u041D\u0435\u043F\u0440\u0430\u0432\u0438\u043B\u044C\u043D\u0430\u044F \u0441\u0442\u0430\u0432\u043A\u0430!");
          }
        }
        catch (Exception e)
        {
          player.sendMessage("\u041D\u0435\u0432\u0435\u0440\u043D\u043E\u0435 \u0432\u0440\u0435\u043C\u044F \u0430\u0443\u043A\u0446\u0438\u043E\u043D\u0430!");
        }
        return;
      }
      if (actualCommand.equalsIgnoreCase("confirmAuction"))
      {
        try
        {
          Auction a = (Auction)_pendingAuctions.get(Integer.valueOf(player.getClan().getHasHideout()));
          a.confirmAuction();
          _pendingAuctions.remove(Integer.valueOf(player.getClan().getHasHideout()));
        }
        catch (Exception e)
        {
          player.sendMessage("\u0410\u0443\u043A\u0446\u0438\u043E\u043D \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.");
        }
        return;
      }
      if (actualCommand.equalsIgnoreCase("bidding"))
      {
        if (val == "") return;

        try
        {
          SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
          int auctionId = Integer.parseInt(val);

          String filename = "data/html/auction/AgitAuctionInfo.htm";
          Auction a = AuctionManager.getInstance().getAuction(auctionId);

          NpcHtmlMessage html = NpcHtmlMessage.id(1);
          html.setFile(filename);
          if (a != null) {
            html.replace("%AGIT_NAME%", a.getItemName());
            html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
            html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
            html.replace("%AGIT_SIZE%", "30 ");
            html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
            html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
            html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(Long.valueOf(a.getEndDate()))));
            html.replace("%AGIT_AUCTION_REMAIN%", String.valueOf((a.getEndDate() - System.currentTimeMillis()) / 3600000L) + " hours " + String.valueOf((a.getEndDate() - System.currentTimeMillis()) / 60000L % 60L) + " minutes");
            html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
            html.replace("%AGIT_AUCTION_COUNT%", String.valueOf(a.getBidders().size()));
            html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_list");
            html.replace("%AGIT_LINK_BIDLIST%", "bypass -h npc_" + getObjectId() + "_bidlist " + a.getId());
            html.replace("%AGIT_LINK_RE%", "bypass -h npc_" + getObjectId() + "_bid1 " + a.getId());
          } else {
            _log.warning("Auctioneer Auction null for AuctionId : " + auctionId);
          }
          player.sendPacket(html);
        }
        catch (Exception e)
        {
          player.sendMessage("\u0410\u0443\u043A\u0446\u0438\u043E\u043D \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D!");
        }

        return;
      }
      if (actualCommand.equalsIgnoreCase("bid"))
      {
        if (val == "") return;

        try
        {
          int auctionId = Integer.parseInt(val);
          try
          {
            int bid = 0;
            if (st.countTokens() >= 1) bid = Integer.parseInt(st.nextToken());

            AuctionManager.getInstance().getAuction(auctionId).setBid(player, bid);
          }
          catch (Exception e)
          {
            player.sendMessage("\u041D\u0435\u043F\u0440\u0430\u0432\u0438\u043B\u044C\u043D\u0430\u044F \u0441\u0442\u0430\u0432\u043A\u0430!");
          }
        }
        catch (Exception e)
        {
          player.sendMessage("\u0410\u0443\u043A\u0446\u0438\u043E\u043D \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D!");
        }

        return;
      }
      if (actualCommand.equalsIgnoreCase("bid1"))
      {
        if ((player.getClan() == null) || (player.getClan().getLevel() < 2))
        {
          player.sendMessage("\u0412\u0430\u0448 \u043A\u043B\u0430\u043D \u0434\u043E\u043B\u0436\u0435\u043D \u0431\u044B\u0442\u044C \u0432\u044B\u0448\u0435 2 \u0443\u0440\u043E\u0432\u043D\u044F");
          return;
        }

        if (val == "") return;
        if (((player.getClan().getAuctionBiddedAt() > 0) && (player.getClan().getAuctionBiddedAt() != Integer.parseInt(val))) || (player.getClan().getHasHideout() > 0))
        {
          player.sendMessage("\u0414\u0435\u043B\u0430\u0442\u044C \u0441\u0442\u0430\u0432\u043A\u0438 \u043C\u043E\u0436\u043D\u043E \u0442\u043E\u043B\u044C\u043A\u043E \u043D\u0430 \u043E\u0434\u0438\u043D \u043A\u043B\u0430\u043D\u0445\u043E\u043B\u043B");
          return;
        }

        try
        {
          String filename = "data/html/auction/AgitBid1.htm";

          int minimumBid = AuctionManager.getInstance().getAuction(Integer.parseInt(val)).getHighestBidderMaxBid();
          if (minimumBid == 0) minimumBid = AuctionManager.getInstance().getAuction(Integer.parseInt(val)).getStartingBid();

          NpcHtmlMessage html = NpcHtmlMessage.id(1);
          html.setFile(filename);
          html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_bidding " + val);
          html.replace("%PLEDGE_ADENA%", Util.formatAdena(player.getClan().getWarehouse().getItemCount(Config.CLANHALL_PAYMENT)));
          html.replace("%AGIT_AUCTION_MINBID%", Util.formatAdena(minimumBid));
          html.replace("npc_%objectId%_bid", "npc_" + getObjectId() + "_bid " + val);
          player.sendPacket(html);
          return;
        }
        catch (Exception e)
        {
          player.sendMessage("\u0410\u0443\u043A\u0446\u0438\u043E\u043D \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D!");

          return;
        }
      }
      if (actualCommand.equalsIgnoreCase("list"))
      {
        List auctions = AuctionManager.getInstance().getAuctions();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

        int limit = 15;

        int i = 1;
        double npage = Math.ceil(auctions.size() / limit);
        int start;
        int start;
        if (val == "") {
          start = 1;
        } else {
          start = limit * (Integer.parseInt(val) - 1) + 1;
          limit *= Integer.parseInt(val);
        }

        TextBuilder items = new TextBuilder("<table width=280 border=0><tr>");
        for (int j = 1; j <= npage; j++) {
          items.append("<td><center><a action=\"bypass -h npc_" + getObjectId() + "_list " + j + "\"> Page " + j + " </a></center></td>");
        }
        items.append("</tr></table> <table width=280 border=0>");
        for (Auction a : auctions)
        {
          if (i > limit) break;
          if (i < start) {
            i++;
            continue;
          }i++;
          items.append("<tr><td>" + ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation() + "</td>" + "<td><a action=\"bypass -h npc_" + getObjectId() + "_bidding " + a.getId() + "\">" + a.getItemName() + "</a></td>" + "<td>" + format.format(Long.valueOf(a.getEndDate())) + "</td>" + "<td>" + a.getStartingBid() + "</td>" + "</tr>");
        }

        items.append("</table>");
        String filename = "data/html/auction/AgitAuctionList.htm";

        NpcHtmlMessage html = NpcHtmlMessage.id(1);
        html.setFile(filename);
        html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
        html.replace("%itemsField%", items.toString());
        player.sendPacket(html);
        return;
      }
      if (actualCommand.equalsIgnoreCase("bidlist"))
      {
        int auctionId = 0;
        if (val == "")
        {
          if (player.getClan().getAuctionBiddedAt() <= 0) {
            return;
          }
          auctionId = player.getClan().getAuctionBiddedAt();
        }
        else {
          auctionId = Integer.parseInt(val);
        }
        TextBuilder biders = new TextBuilder();
        Map bidders = AuctionManager.getInstance().getAuction(auctionId).getBidders();
        for (Auction.Bidder b : bidders.values())
        {
          biders.append("<tr><td>" + b.getClanName() + "</td><td>" + Util.htmlSpecialChars(b.getName()) + "</td><td>" + b.getTimeBid().get(1) + "/" + (b.getTimeBid().get(2) + 1) + "/" + b.getTimeBid().get(5) + "</td><td>" + b.getBid() + "</td>" + "</tr>");
        }

        String filename = "data/html/auction/AgitBidderList.htm";

        NpcHtmlMessage html = NpcHtmlMessage.id(1);
        html.setFile(filename);
        html.replace("%AGIT_LIST%", biders.toString());
        html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
        html.replace("%x%", val);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        player.sendPacket(html);
        biders.clear();
        biders = null;
        return;
      }
      if (actualCommand.equalsIgnoreCase("selectedItems"))
      {
        if ((player.getClan() != null) && (player.getClan().getHasHideout() == 0) && (player.getClan().getAuctionBiddedAt() > 0))
        {
          SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
          String filename = "data/html/auction/AgitBidInfo.htm";
          NpcHtmlMessage html = NpcHtmlMessage.id(1);
          html.setFile(filename);
          Auction a = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
          if (a != null) {
            html.replace("%AGIT_NAME%", a.getItemName());
            html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
            html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
            html.replace("%AGIT_SIZE%", "30 ");
            html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
            html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
            html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(Long.valueOf(a.getEndDate()))));
            html.replace("%AGIT_AUCTION_REMAIN%", String.valueOf((a.getEndDate() - System.currentTimeMillis()) / 3600000L) + " hours " + String.valueOf((a.getEndDate() - System.currentTimeMillis()) / 60000L % 60L) + " minutes");
            html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
            html.replace("%AGIT_AUCTION_MYBID%", String.valueOf(((Auction.Bidder)a.getBidders().get(Integer.valueOf(player.getClanId()))).getBid()));
            html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
            html.replace("%objectId%", String.valueOf(getObjectId()));
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
          } else {
            _log.warning("Auctioneer Auction null for AuctionBiddedAt : " + player.getClan().getAuctionBiddedAt());
          }
          player.sendPacket(html);
          return;
        }
        if ((player.getClan() != null) && (AuctionManager.getInstance().getAuction(player.getClan().getHasHideout()) != null))
        {
          SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
          String filename = "data/html/auction/AgitSaleInfo.htm";
          NpcHtmlMessage html = NpcHtmlMessage.id(1);
          html.setFile(filename);
          Auction a = AuctionManager.getInstance().getAuction(player.getClan().getHasHideout());
          if (a != null) {
            html.replace("%AGIT_NAME%", a.getItemName());
            html.replace("%AGIT_OWNER_PLEDGE_NAME%", a.getSellerClanName());
            html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
            html.replace("%AGIT_SIZE%", "30 ");
            html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
            html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
            html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(Long.valueOf(a.getEndDate()))));
            html.replace("%AGIT_AUCTION_REMAIN%", String.valueOf((a.getEndDate() - System.currentTimeMillis()) / 3600000L) + " hours " + String.valueOf((a.getEndDate() - System.currentTimeMillis()) / 60000L % 60L) + " minutes");
            html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
            html.replace("%AGIT_AUCTION_BIDCOUNT%", String.valueOf(a.getBidders().size()));
            html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
            html.replace("%id%", String.valueOf(a.getId()));
            html.replace("%objectId%", String.valueOf(getObjectId()));
          } else {
            _log.warning("Auctioneer Auction null for getHasHideout : " + player.getClan().getHasHideout());
          }
          player.sendPacket(html);
          return;
        }
        if ((player.getClan() != null) && (player.getClan().getHasHideout() != 0))
        {
          int ItemId = player.getClan().getHasHideout();
          String filename = "data/html/auction/AgitInfo.htm";
          NpcHtmlMessage html = NpcHtmlMessage.id(1);
          html.setFile(filename);
          if (ClanHallManager.getInstance().getClanHallById(ItemId) != null)
          {
            html.replace("%AGIT_NAME%", ClanHallManager.getInstance().getClanHallById(ItemId).getName());
            html.replace("%AGIT_OWNER_PLEDGE_NAME%", player.getClan().getName());
            html.replace("%OWNER_PLEDGE_MASTER%", Util.htmlSpecialChars(player.getClan().getLeaderName()));
            html.replace("%AGIT_SIZE%", "30 ");
            html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(ItemId).getLease()));
            html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(ItemId).getLocation());
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
            html.replace("%objectId%", String.valueOf(getObjectId()));
          }
          else {
            _log.warning("Clan Hall ID NULL : " + ItemId + " Can be caused by concurent write in ClanHallManager");
          }player.sendPacket(html);
          return;
        }
      } else {
        if (actualCommand.equalsIgnoreCase("cancelBid"))
        {
          int bid = ((Auction.Bidder)AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).getBidders().get(Integer.valueOf(player.getClanId()))).getBid();
          String filename = "data/html/auction/AgitBidCancel.htm";
          NpcHtmlMessage html = NpcHtmlMessage.id(1);
          html.setFile(filename);
          html.replace("%AGIT_BID%", String.valueOf(bid));
          html.replace("%AGIT_BID_REMAIN%", String.valueOf((int)(bid * 0.9D)));
          html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
          html.replace("%objectId%", String.valueOf(getObjectId()));
          player.sendPacket(html);
          return;
        }
        if (actualCommand.equalsIgnoreCase("doCancelBid"))
        {
          if (AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()) != null)
          {
            AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).cancelBid(player.getClanId());
            player.sendMessage("\u0421\u0442\u0430\u0432\u043A\u0430 \u043E\u0442\u043C\u0435\u043D\u0435\u043D\u0430");
          }
          return;
        }
        if (actualCommand.equalsIgnoreCase("cancelAuction"))
        {
          if ((player.getClanPrivileges() & 0x1000) != 4096)
          {
            player.sendMessage("\u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u043F\u0440\u0430\u0432");
            return;
          }
          String filename = "data/html/auction/AgitSaleCancel.htm";
          NpcHtmlMessage html = NpcHtmlMessage.id(1);
          html.setFile(filename);
          html.replace("%AGIT_DEPOSIT%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
          html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
          html.replace("%objectId%", String.valueOf(getObjectId()));
          player.sendPacket(html);
          return;
        }
        if (actualCommand.equalsIgnoreCase("doCancelAuction"))
        {
          if (AuctionManager.getInstance().getAuction(player.getClan().getHasHideout()) != null)
          {
            AuctionManager.getInstance().getAuction(player.getClan().getHasHideout()).cancelAuction();
            player.sendMessage("\u0410\u0443\u043A\u0446\u0438\u043E\u043D \u043E\u0442\u043C\u0435\u043D\u0435\u043D");
          }
          return;
        }
        if (actualCommand.equalsIgnoreCase("sale2"))
        {
          String filename = "data/html/auction/AgitSale2.htm";
          NpcHtmlMessage html = NpcHtmlMessage.id(1);
          html.setFile(filename);
          html.replace("%AGIT_LAST_PRICE%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
          html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale");
          html.replace("%objectId%", String.valueOf(getObjectId()));
          player.sendPacket(html);
          return;
        }
        if (actualCommand.equalsIgnoreCase("sale"))
        {
          if ((player.getClanPrivileges() & 0x1000) != 4096)
          {
            player.sendMessage("\u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u043F\u0440\u0430\u0432");
            return;
          }
          String filename = "data/html/auction/AgitSale1.htm";
          NpcHtmlMessage html = NpcHtmlMessage.id(1);
          html.setFile(filename);
          html.replace("%AGIT_DEPOSIT%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
          html.replace("%AGIT_PLEDGE_ADENA%", String.valueOf(player.getClan().getWarehouse().getItemCount(Config.CLANHALL_PAYMENT)));
          html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
          html.replace("%objectId%", String.valueOf(getObjectId()));
          player.sendPacket(html);
          return;
        }
        if (actualCommand.equalsIgnoreCase("rebid"))
        {
          SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
          if ((player.getClanPrivileges() & 0x1000) != 4096)
          {
            player.sendMessage("\u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u043F\u0440\u0430\u0432");
            return;
          }
          try
          {
            String filename = "data/html/auction/AgitBid2.htm";
            NpcHtmlMessage html = NpcHtmlMessage.id(1);
            html.setFile(filename);
            Auction a = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
            if (a != null) {
              html.replace("%AGIT_AUCTION_BID%", String.valueOf(((Auction.Bidder)a.getBidders().get(Integer.valueOf(player.getClanId()))).getBid()));
              html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
              html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(Long.valueOf(a.getEndDate()))));
              html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
              html.replace("npc_%objectId%_bid1", "npc_" + getObjectId() + "_bid1 " + a.getId());
            } else {
              _log.warning("Auctioneer Auction null for AuctionBiddedAt : " + player.getClan().getAuctionBiddedAt());
            }
            player.sendPacket(html);
          }
          catch (Exception e)
          {
            player.sendMessage("\u0410\u0443\u043A\u0446\u0438\u043E\u043D \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D!");
          }
          return;
        }
        if (actualCommand.equalsIgnoreCase("location"))
        {
          NpcHtmlMessage html = NpcHtmlMessage.id(1);
          html.setFile("data/html/auction/location.htm");
          html.replace("%location%", MapRegionTable.getInstance().getClosestTownName(player));
          html.replace("%LOCATION%", getPictureName(player));
          html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
          player.sendPacket(html);
          return;
        }
        if (actualCommand.equalsIgnoreCase("start"))
        {
          showMessageWindow(player);
          return;
        }
      }
    }
    super.onBypassFeedback(player, command);
  }

  public void showMessageWindow(L2PcInstance player)
  {
    String filename = "data/html/auction/auction-no.htm";

    int condition = validateCondition(player);
    if (condition == 1) filename = "data/html/auction/auction-busy.htm"; else {
      filename = "data/html/auction/auction.htm";
    }
    NpcHtmlMessage html = NpcHtmlMessage.id(1);
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcId%", String.valueOf(getNpcId()));
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }

  private int validateCondition(L2PcInstance player)
  {
    if ((getCastle() != null) && (getCastle().getCastleId() > 0))
    {
      if (getCastle().getSiege().getIsInProgress()) return 1;
      return 3;
    }

    return 0;
  }

  private String getPictureName(L2PcInstance plyr) {
    int nearestTownId = MapRegionTable.getInstance().getMapRegion(plyr.getX(), plyr.getY());
    String nearestTown;
    switch (nearestTownId) {
    case 5:
      nearestTown = "GLUDIO"; break;
    case 6:
      nearestTown = "GLUDIN"; break;
    case 7:
      nearestTown = "DION"; break;
    case 8:
      nearestTown = "GIRAN"; break;
    case 14:
      nearestTown = "RUNE"; break;
    case 15:
      nearestTown = "GODARD"; break;
    case 16:
      nearestTown = "SCHUTTGART"; break;
    case 9:
    case 10:
    case 11:
    case 12:
    case 13:
    default:
      nearestTown = "ADEN";
    }

    return nearestTown;
  }
}