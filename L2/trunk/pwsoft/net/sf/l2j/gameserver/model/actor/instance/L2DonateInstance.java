package net.sf.l2j.gameserver.model.actor.instance;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.AugmentationData;
import net.sf.l2j.gameserver.datatables.CustomServerData;
import net.sf.l2j.gameserver.datatables.CustomServerData.ChinaItem;
import net.sf.l2j.gameserver.datatables.CustomServerData.DonateItem;
import net.sf.l2j.gameserver.datatables.CustomServerData.DonateSkill;
import net.sf.l2j.gameserver.datatables.CustomServerData.StatCastle;
import net.sf.l2j.gameserver.datatables.CustomServerData.StatClan;
import net.sf.l2j.gameserver.datatables.CustomServerData.StatPlayer;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExMailArrived;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Log;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public class L2DonateInstance extends L2NpcInstance
{
  private static final Logger _log = AbstractLogger.getLogger(L2DonateInstance.class.getName());
  private static final int STOCK_SERTIFY = Config.STOCK_SERTIFY;
  private static final int SERTIFY_PRICE = Config.SERTIFY_PRICE;
  private static final int FIRST_BALANCE = Config.FIRST_BALANCE;
  private static final int DONATE_COIN = Config.DONATE_COIN;
  private static final String DONATE_COIN_NEMA = Config.DONATE_COIN_NEMA;
  private static final int DONATE_RATE = Config.DONATE_RATE;
  private static final int NALOG_NPS = 100 - Config.NALOG_NPS;
  private static final String VAL_NAME = Config.VAL_NAME;
  private static final int PAGE_LIMIT = Config.PAGE_LIMIT;

  private static final int AUGMENT_COIN = Config.AUGMENT_COIN;
  private static final int ENCHANT_COIN = Config.ENCHANT_COIN;
  private static final String AUGMENT_COIN_NAME = Config.AUGMENT_COIN_NAME;
  private static final String ENCHANT_COIN_NAME = Config.ENCHANT_COIN_NAME;
  private static final int AUGMENT_PRICE = Config.AUGMENT_PRICE;
  private static final int ENCHANT_PRICE = Config.ENCHANT_PRICE;

  private static final int CLAN_COIN = Config.CLAN_COIN;
  private static final String CLAN_COIN_NAME = Config.CLAN_COIN_NAME;
  private static final int CLAN_LVL6 = Config.CLAN_LVL6;
  private static final int CLAN_LVL7 = Config.CLAN_LVL7;
  private static final int CLAN_LVL8 = Config.CLAN_LVL8;
  private static final int CLAN_POINTS = Config.CLAN_POINTS;
  private static final int CLAN_POINTS_PRICE = Config.CLAN_POINTS_PRICE;

  private static final int AUGSALE_COIN = Config.AUGSALE_COIN;
  private static final int AUGSALE_PRICE = Config.AUGSALE_PRICE;
  private static final String AUGSALE_COIN_NAME = Config.AUGSALE_COIN_NAME;
  private static final FastMap<Integer, Integer> AUGSALE_TABLE = Config.AUGSALE_TABLE;
  private static final FastMap<Integer, Integer> PREMIUM_DAY_PRICES = Config.PREMIUM_DAY_PRICES;

  private static final int SOB_COIN = Config.SOB_COIN;
  private static final int SOB_PRICE_ONE = Config.SOB_PRICE_ONE;
  private static final int SOB_PRICE_TWO = Config.SOB_PRICE_TWO;
  private static final String SOB_COIN_NAME = Config.SOB_COIN_NAME;

  public static FastMap<Integer, FastTable<CustomServerData.DonateSkill>> _donateSkills = CustomServerData._donateSkills;

  private static int _statLimit = 11;
  private static int _statSortLimit = 9;

  public L2DonateInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.startsWith("StockExchange")) {
      int val = Integer.parseInt(command.substring(13).trim());

      switch (val) {
      case 1:
        L2ItemInstance setify = player.getInventory().getItemByItemId(STOCK_SERTIFY);
        if ((setify != null) && (setify.getCount() >= 1))
          showWelcome(player, 1);
        else {
          showWelcome(player, 0);
        }
        break;
      case 2:
        showStockSellList(player, 1, 0);
        break;
      case 3:
        showInventoryItems(player);
        break;
      case 4:
        showPrivateInfo(player);
        break;
      case 5:
        showSertifyInfo(player);
        break;
      default:
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      }
    }
    else if (command.startsWith("StockShowItem"))
    {
      int sellId = Integer.parseInt(command.substring(13).trim());
      if (sellId == 0) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }

      showStockItem(player, sellId);
    } else if (command.startsWith("StockInventoryItem"))
    {
      int objectId = Integer.parseInt(command.substring(18).trim());
      if (objectId == 0) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }

      showInventoryItem(player, objectId);
    } else if (command.startsWith("StockBuyItem"))
    {
      int sellId = Integer.parseInt(command.substring(12).trim());
      if (sellId == 0) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }
      buyStockItem(player, sellId);
    } else if (command.startsWith("StockAddItem"))
    {
      try {
        String[] opaopa = command.split(" ");
        int objectId = Integer.parseInt(opaopa[1]);
        int price = Integer.parseInt(opaopa[2]);
        if ((objectId == 0) || (price == 0)) {
          showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
          return;
        }
        addStockItem(player, objectId, price);
      }
      catch (Exception e) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }
    } else if (command.startsWith("showPrivateInfo"))
    {
      int val = Integer.parseInt(command.substring(15).trim());

      switch (val) {
      case 1:
        incBalance(player);
        break;
      case 2:
        showStockSellList(player, 1, 1);
        break;
      case 14:
        break;
      default:
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      }
    }
    else if (command.startsWith("StockAddBalance"))
    {
      try {
        int val = Integer.parseInt(command.substring(15).trim());
        StockAddBalance(player, val);
      }
      catch (Exception e) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }
    } else if (command.startsWith("StockShowPage"))
    {
      try {
        String[] opaopa = command.split(" ");
        int page = Integer.parseInt(opaopa[1]);
        int self = Integer.parseInt(opaopa[2]);
        if (page == 0) {
          showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
          return;
        }
        showStockSellList(player, page, self);
      }
      catch (Exception e) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }
    } else if (command.equalsIgnoreCase("StockSertifyBuy")) {
      StockSertifyBuy(player);
    } else if (command.equalsIgnoreCase("voteHome")) {
      showChatWindow(player, "data/html/default/80007.htm");
    } else if (command.startsWith("voteService")) {
      String[] opaopa = command.split(" ");
      int service = Integer.parseInt(opaopa[1]);
      int page = Integer.parseInt(opaopa[2]);
      if (service > 2) {
        return;
      }

      showVoteServiceWindow(player, service, page);
    } else if (command.startsWith("voteItemShow_")) {
      String[] opaopa = command.split("_");
      int service = Integer.parseInt(opaopa[1]);
      int objectId = Integer.parseInt(opaopa[2]);

      if (service > 2) {
        return;
      }

      showVote1Item(player, service, objectId);
    } else if (command.startsWith("voteItem2Show_")) {
      String[] opaopa = command.split("_");
      int service = Integer.parseInt(opaopa[1]);
      int objectId = Integer.parseInt(opaopa[2]);

      if (service > 2) {
        return;
      }

      showVote2Item(player, service, objectId);
    } else if (command.startsWith("voteStep2")) {
      String[] opaopa = command.split(" ");
      int service = Integer.parseInt(opaopa[1]);
      int page = Integer.parseInt(opaopa[2]);
      if (service > 2) {
        return;
      }

      showVoteNextItems(player, service, page);
    } else if (command.startsWith("voteStep3_")) {
      int service = Integer.parseInt(command.substring(10).trim());

      if (service > 2) {
        return;
      }

      showVoteAgree(player, service);
    } else if (command.startsWith("voteComplete_")) {
      int service = Integer.parseInt(command.substring(13).trim());

      if (service > 2) {
        return;
      }

      showDoVoteFinish(player, service);
    } else if (command.startsWith("clanService_")) {
      int service = Integer.parseInt(command.substring(12).trim());

      if (service > 2) {
        return;
      }

      if (!player.isClanLeader()) {
        player.sendPacket(Static.WAR_NOT_LEADER);
        return;
      }

      if (player.getClan().getLevel() < 5) {
        player.sendPacket(Static.CLAN_5LVL_HIGHER);
        return;
      }

      clanWelcome(player);
    } else if (command.startsWith("clanLevel_")) {
      int level = Integer.parseInt(command.substring(10).trim());

      clanSetLevel(player, level);
    } else if (command.equalsIgnoreCase("clanPoints")) {
      clanPoints(player);
    } else if (command.equalsIgnoreCase("clanSkills")) {
      clanSkills(player);
    } else if (command.equalsIgnoreCase("Augsale")) {
      AugSaleWelcome(player);
    } else if (command.equalsIgnoreCase("augSaleItems")) {
      AugSaleItems(player);
    } else if (command.equalsIgnoreCase("AugsaleFinish")) {
      AugsaleFinish(player);
    } else if (command.startsWith("augsaleShow")) {
      int augId = Integer.parseInt(command.substring(11).trim());

      augSaleShow(player, augId);
    } else if (command.startsWith("augsItem")) {
      int objectId = Integer.parseInt(command.substring(8).trim());
      if (objectId == 0) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }

      AugsItem(player, objectId);
    } else if (command.equalsIgnoreCase("chinaItems")) {
      itemsChina(player);
    } else if (command.startsWith("chinaShow")) {
      int itId = Integer.parseInt(command.substring(9).trim());
      if (itId == 0) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }

      chinaShow(player, itId);
    } else if (command.startsWith("bueChina")) {
      int itId = Integer.parseInt(command.substring(8).trim());
      if (itId == 0) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }

      bueChina(player, itId);
    } else if (command.startsWith("bueSOB")) {
      if (Config.SOB_ID == 0) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }
      int itId = Integer.parseInt(command.substring(6).trim());
      if (itId == 0) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }

      bueSOB(player, itId);
    } else if (command.equalsIgnoreCase("donateShop")) {
      if (!Config.ALLOW_DSHOP) {
        showError(player, "\u041C\u0430\u0433\u0430\u0437\u0438\u043D \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
        return;
      }

      donateShop(player);
    } else if (command.startsWith("dShopShow")) {
      if (!Config.ALLOW_DSHOP) {
        showError(player, "\u041C\u0430\u0433\u0430\u0437\u0438\u043D \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
        return;
      }

      donateShopShow(player, Integer.parseInt(command.substring(9).trim()));
    } else if (command.startsWith("dShopBue")) {
      if (!Config.ALLOW_DSHOP) {
        showError(player, "\u041C\u0430\u0433\u0430\u0437\u0438\u043D \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
        return;
      }

      donateShopBue(player, Integer.parseInt(command.substring(8).trim()));
    } else if (command.startsWith("addPremium")) {
      if (!Config.PREMIUM_ENABLE) {
        showError(player, "\u041F\u0440\u0435\u043C\u0438\u0443\u043C \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
        return;
      }

      addPremium(player, Integer.parseInt(command.substring(10).trim()));
    }
    else if (command.equalsIgnoreCase("addNoble")) {
      if (!Config.NOBLES_ENABLE) {
        showError(player, "\u0421\u0435\u0440\u0432\u0438\u0441 \u0432\u044B\u0434\u0430\u0447\u0438 \u043D\u043E\u0431\u043B\u0435\u0441\u0441\u0430 \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
        return;
      }

      addNoble(player);
    } else if (command.equalsIgnoreCase("donateSkillsShop")) {
      if (!Config.ALLOW_DSKILLS) {
        showError(player, "\u041C\u0430\u0433\u0430\u0437\u0438\u043D \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
        return;
      }
      donateSkillShop(player);
    } else if (command.startsWith("dsShopShow")) {
      if (!Config.ALLOW_DSKILLS) {
        showError(player, "\u041C\u0430\u0433\u0430\u0437\u0438\u043D \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
        return;
      }

      donateSkillShopShow(player, Integer.parseInt(command.substring(10).trim()));
    } else if (command.startsWith("dsShopBue")) {
      if (!Config.ALLOW_DSKILLS) {
        showError(player, "\u041C\u0430\u0433\u0430\u0437\u0438\u043D \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
        return;
      }

      donateSkillShopBue(player, Integer.parseInt(command.substring(9).trim()));
    } else if (command.equalsIgnoreCase("statHome")) {
      if (!Config.CACHED_SERVER_STAT) {
        showError(player, "\u0421\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043A\u0430 \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u0430.");
        return;
      }

      statHome(player);
    } else if (command.startsWith("statPvp")) {
      statShowPvp(player, Integer.parseInt(command.substring(7).trim()));
    } else if (command.startsWith("statPk")) {
      statShowPk(player, Integer.parseInt(command.substring(6).trim()));
    } else if (command.startsWith("statClans")) {
      statClans(player, Integer.parseInt(command.substring(9).trim()));
    } else if (command.equalsIgnoreCase("statCastles")) {
      statCastles(player);
    } else {
      super.onBypassFeedback(player, command);
    }
  }

  private void showWelcome(L2PcInstance player, int hasSertify)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body>");
    replyMSG.append(new StringBuilder().append("<table width=280><tr><td>").append(player.getName()).append("</td><td align=right><font color=336699>\u0411\u0430\u043B\u0430\u043D\u0441:</font> <font color=33CCFF>").append(getStockBalance(player)).append(" ").append(VAL_NAME).append("</font></td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td> </td><td align=right> <a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 4\">\u041B\u0438\u0447\u043D\u044B\u0439 \u043A\u0430\u0431\u0438\u043D\u0435\u0442</a></td></tr></table><br><br>").toString());
    replyMSG.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 2\">\u041F\u0440\u043E\u0441\u043C\u043E\u0442\u0440 \u0431\u0438\u0440\u0436\u0438</a><br>").toString());
    if (hasSertify == 1)
      replyMSG.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 3\">\u0412\u044B\u0441\u0442\u0430\u0432\u0438\u0442\u044C \u043F\u0440\u0435\u0434\u043C\u0435\u0442</a><br>").toString());
    else {
      replyMSG.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 5\">?\u041F\u043E\u043B\u0443\u0447\u0438\u0442\u044C \u0441\u0435\u0440\u0442\u0438\u0444\u0438\u043A\u0430\u0442</a><br>").toString());
    }
    replyMSG.append("</body></html>");

    player.setStockItem(-1, 0, 0, 0, 0);
    player.setStockInventoryItem(0, 0);
    player.setStockSelf(0);

    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private void showPrivateInfo(L2PcInstance player)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body> \u041B\u0438\u0447\u043D\u044B\u0439 \u043A\u0430\u0431\u0438\u043D\u0435\u0442:");
    replyMSG.append(new StringBuilder().append("<table width=280><tr><td>").append(player.getName()).append("</td><td align=right><font color=336699>\u0411\u0430\u043B\u0430\u043D\u0441:</font> <font color=33CCFF>").append(getStockBalance(player)).append(" ").append(VAL_NAME).append("</font></td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td> </td><td align=right> <a action=\"bypass -h npc_").append(getObjectId()).append("_showPrivateInfo 1\">\u041F\u043E\u043F\u043E\u043B\u043D\u0438\u0442\u044C \u0441\u0447\u0435\u0442</a></td></tr></table><br><br>").toString());
    replyMSG.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_showPrivateInfo 2\">\u041C\u043E\u0438 \u0442\u043E\u0432\u0430\u0440\u044B</a><br>").toString());

    replyMSG.append(new StringBuilder().append("<br><a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 1\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br>").toString());
    replyMSG.append("</body></html>");
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private void showSertifyInfo(L2PcInstance player)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body> <font color=99CC66>\u0422\u043E\u0440\u0433\u043E\u0432\u044B\u0439 \u0441\u0435\u0440\u0442\u0438\u0444\u0438\u043A\u0430\u0442.<br>");
    replyMSG.append("\u0421\u0435\u0440\u0442\u0438\u0444\u0438\u043A\u0430\u0442 \u043F\u043E\u0437\u0432\u043E\u043B\u044F\u0435\u0442 \u0442\u043E\u0440\u0433\u043E\u0432\u0430\u0442\u044C \u0432\u0435\u0449\u0430\u043C\u0438 \u043D\u0430 \u0431\u0438\u0440\u0436\u0435:<br>");
    replyMSG.append("\u0417\u0430\u0442\u043E\u0447\u0435\u043D\u043D\u044B\u043C \u0438 \u0430\u0443\u0433\u043C\u0435\u043D\u0442\u0438\u0440\u043E\u0432\u0430\u043D\u043D\u044B\u043C \u043E\u0440\u0443\u0436\u0438\u0435\u043C;<br1>");
    replyMSG.append("\u0417\u0430\u0442\u043E\u0447\u0435\u043D\u043D\u043E\u0439 \u0431\u0440\u043E\u043D\u0435\u0439;<br1>");
    replyMSG.append("\u042D\u043F\u0438\u043A \u0431\u0438\u0436\u0443\u0442\u0435\u0440\u0438\u0435\u0439;<br>");
    replyMSG.append("\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u0441\u0435\u0440\u0442\u0438\u0444\u0438\u043A\u0430\u0442\u0430 \u0441\u043E\u0441\u0442\u0430\u0432\u043B\u044F\u0435\u0442:</font><br1>");
    replyMSG.append(new StringBuilder().append("<table border=1 width=290><tr><td>").append(SERTIFY_PRICE).append(" ").append(DONATE_COIN_NEMA).append("</td></tr></table><br>").toString());
    L2ItemInstance coins = player.getInventory().getItemByItemId(DONATE_COIN);
    if ((coins != null) && (coins.getCount() >= SERTIFY_PRICE))
      replyMSG.append(new StringBuilder().append("<button value=\"\u041A\u0443\u043F\u0438\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_StockSertifyBuy\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
    else {
      replyMSG.append("<font color=999999>[\u041F\u0440\u0438\u043E\u0431\u0440\u0435\u0441\u0442\u0438 \u0441\u0435\u0440\u0442\u0438\u0444\u0438\u043A\u0430\u0442]</font><br>");
    }
    replyMSG.append(new StringBuilder().append("<br><a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 1\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br>").toString());
    replyMSG.append("</body></html>");
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private int getStockBalance(L2PcInstance player)
  {
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet result = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      statement = con.prepareStatement("SELECT balance FROM `z_stock_accounts` WHERE `charId`=? LIMIT 1");
      statement.setInt(1, player.getObjectId());
      result = statement.executeQuery();

      if (result.next()) {
        int i = result.getInt("balance");
        return i;
      }
      Close.SR(statement, result);
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("StockExchange: getStockBalance() error: ").append(e).toString());
    } finally {
      Close.CSR(con, statement, result);
    }

    return 0;
  }

  private void showStockSellList(L2PcInstance player, int page, int self)
  {
    if (System.currentTimeMillis() - player.getStockLastAction() < 2000L) {
      showError(player, "\u0420\u0430\u0437 \u0432 2 \u0441\u0435\u043A\u0443\u043D\u0434\u044B.");
      return;
    }

    player.setStockLastAction(System.currentTimeMillis());

    int limit1 = 0;

    if (page == 1)
      limit1 = 0;
    else if (page == 2)
      limit1 = PAGE_LIMIT;
    else {
      limit1 = page * PAGE_LIMIT;
    }

    int limit2 = limit1 + PAGE_LIMIT;

    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body>");
    replyMSG.append(new StringBuilder().append("<table width=300><tr><td width=36></td><td width=264>\u0421\u0442\u0440\u0430\u043D\u0438\u0446\u0430 ").append(page).append(":</td></tr>").toString());

    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      if (self == 0) {
        statement = con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, ownerId, shadow FROM `z_stock_items` WHERE `id` > '0' ORDER BY `id` DESC LIMIT ?, ?");
        statement.setInt(1, limit1);
        statement.setInt(2, limit2);
      } else {
        statement = con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, ownerId, shadow FROM `z_stock_items` WHERE `ownerId` = ? ORDER BY `id` DESC LIMIT ?, ?");
        statement.setInt(1, player.getObjectId());
        statement.setInt(2, limit1);
        statement.setInt(3, limit2);
        player.setStockSelf(self);
      }
      rs = statement.executeQuery();
      while (rs.next()) {
        L2Item brokeItem = ItemTable.getInstance().getTemplate(rs.getInt("itemId"));
        if (brokeItem != null) {
          replyMSG.append(new StringBuilder().append("<tr><td><img src=\"Icon.").append(brokeItem.getIcon()).append("\" width=32 height=32></td><td><a action=\"bypass -h npc_").append(getObjectId()).append("_StockShowItem ").append(rs.getInt("id")).append("\"> ").append(brokeItem.getName()).append("</a>+").append(rs.getInt("enchant")).append(" <br1><font color=336699>\u0426\u0435\u043D\u0430: ").append(rs.getInt("price")).append(" ").append(VAL_NAME).append("; \u041F\u0440\u043E\u0434\u0430\u0432\u0435\u0446: ").append(getSellerName(rs.getInt("ownerId"))).append("</font><br1>").append(getAugmentSkill(rs.getInt("augment"), rs.getInt("augLvl"))).append("</td></tr>").toString());
        }
      }
      Close.SR(statement, rs);
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("StockExchange: showStockSellList() error: ").append(e).toString());
    } finally {
      Close.CSR(con, statement, rs);
    }
    replyMSG.append("</table><br>");

    int pages = getPageCount();
    if (pages >= 2) {
      replyMSG.append("\u0421\u0442\u0440\u0430\u043D\u0438\u0446\u044B:<br1><table width=300><tr>");
      int step = 0;
      for (int i = 1; i <= pages; i++) {
        if (i == page)
          replyMSG.append(new StringBuilder().append("<td>").append(i).append("</td>").toString());
        else {
          replyMSG.append(new StringBuilder().append("<td><a action=\"bypass -h npc_").append(getObjectId()).append("_StockShowPage ").append(i).append(" ").append(self).append("\">").append(i).append("</a></td>").toString());
        }
        if (step == 10) {
          replyMSG.append("</tr><tr>");
          step = 0;
        }
        step++;
      }
      replyMSG.append("</tr></table><br>");
    }

    replyMSG.append(new StringBuilder().append("<br><br><a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 1\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br>").toString());
    replyMSG.append("</body></html>");
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private int getPageCount() {
    int rowCount = 0;
    int pages = 0;

    Connect con = null;
    PreparedStatement statement = null;
    ResultSet result = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      statement = con.prepareStatement("SELECT COUNT(id) FROM z_stock_items WHERE id > ?");
      statement.setInt(1, 0);
      result = statement.executeQuery();

      result.next();
      rowCount = result.getInt(1);
      Close.SR(statement, result);
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("StockExchange: getPageCount() error: ").append(e).toString());
    } finally {
      Close.CSR(con, statement, result);
    }

    if (rowCount == 0) {
      return 0;
    }

    pages = rowCount / PAGE_LIMIT + 1;

    return pages;
  }

  private void showInventoryItems(L2PcInstance player)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body>");
    replyMSG.append("\u0412\u044B\u0431\u043E\u0440 \u0448\u043C\u043E\u0442\u043A\u0438:<br>\u0427\u0442\u043E \u0432\u044B\u0441\u0442\u0430\u0432\u043B\u044F\u0435\u043C \u043D\u0430 \u0431\u0438\u0440\u0436\u0443?<br><br><table width=300>");

    int objectId = 0;
    String itemName = "";
    int enchantLevel = 0;
    String itemIcon = "";
    int itemType = 0;
    for (L2ItemInstance item : player.getInventory().getItems()) {
      if (item == null)
      {
        continue;
      }
      objectId = item.getObjectId();
      itemName = item.getItem().getName();
      enchantLevel = item.getEnchantLevel();
      itemIcon = item.getItem().getIcon();
      itemType = item.getItem().getType2();

      if ((item.canBeEnchanted()) && (!item.isEquipped()) && (item.isDestroyable()) && ((itemType == 0) || (itemType == 1) || (itemType == 2) || (item.isAugmented()))) {
        replyMSG.append(new StringBuilder().append("<tr><td><img src=\"").append(itemIcon).append("\" width=32 height=32></td><td><a action=\"bypass -h npc_").append(getObjectId()).append("_StockInventoryItem ").append(objectId).append("\">").append(itemName).append(" (+").append(enchantLevel).append(")</a></td></tr>").toString());
      }
    }

    replyMSG.append(new StringBuilder().append("</table><br><br><a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 1\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br>").toString());
    replyMSG.append("</body></html>");
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private void showInventoryItem(L2PcInstance player, int objectId)
  {
    L2ItemInstance item = player.getInventory().getItemByObjectId(objectId);
    if (item != null) {
      NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
      TextBuilder replyMSG = new TextBuilder("<html><body>");

      String itemName = item.getItem().getName();
      int enchantLevel = item.getEnchantLevel();
      String itemIcon = item.getItem().getIcon();

      replyMSG.append("\u0412\u044B\u0441\u0442\u0430\u0432\u043B\u0435\u043D\u0438\u0435 \u043D\u0430 \u0431\u0438\u0440\u0436\u0443:<br>\u041F\u043E\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0430\u0435\u0442\u0435 \u0448\u043C\u043E\u0442\u043A\u0443?<br>");

      replyMSG.append(new StringBuilder().append("<table width=300><tr><td><img src=\"").append(itemIcon).append("\" width=32 height=32></td><td><font color=LEVEL>").append(itemName).append(" (+").append(enchantLevel).append(")</font>g<br></td></tr></table><br><br>").toString());
      replyMSG.append(new StringBuilder().append("<br>\u0417\u0430\u0442\u043E\u0447\u043A\u0430: <font color=bef574>+").append(enchantLevel).append("</font><br>").toString());

      if ((item.isAugmented()) && (item.getAugmentation().getAugmentSkill() != null)) {
        L2Skill augment = item.getAugmentation().getAugmentSkill();
        String augName = augment.getName();
        String type = "";
        if (augment.isActive())
          type = "\u0410\u043A\u0442\u0438\u0432";
        else if (augment.isPassive())
          type = "\u041F\u0430\u0441\u0441\u0438\u0432";
        else {
          type = "\u0428\u0430\u043D\u0441";
        }

        replyMSG.append(new StringBuilder().append("<br>\u0410\u0443\u0433\u043C\u0435\u043D\u0442: <font color=bef574>").append(augName).append(" (").append(type).append(")</font><br>").toString());
      }

      replyMSG.append("\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u0436\u0435\u043B\u0430\u0435\u043C\u0443\u044E \u0446\u0435\u043D\u0443:<br><edit var=\"price\" width=200 length=\"16\">");
      replyMSG.append(new StringBuilder().append("<button value=\"\u0412\u044B\u0441\u0442\u0430\u0432\u0438\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_StockAddItem ").append(objectId).append(" $price\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
      player.setStockInventoryItem(objectId, enchantLevel);

      replyMSG.append(new StringBuilder().append("<br><br><a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 1\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br>").toString());
      replyMSG.append("</body></html>");
      reply.setHtml(replyMSG.toString());
      player.sendPacket(reply);
    } else {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }
  }

  private void showStockItem(L2PcInstance player, int sellId)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body>");

    Connect con = null;
    PreparedStatement statement = null;
    ResultSet result = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);

      statement = con.prepareStatement("SELECT itemId, enchant, augment, augLvl, price, ownerId, shadow FROM `z_stock_items` WHERE `id` = ? LIMIT 1");
      statement.setInt(1, sellId);
      result = statement.executeQuery();

      if (result.next()) {
        int itemId = result.getInt("itemId");
        L2Item brokeItem = ItemTable.getInstance().getTemplate(itemId);
        if (brokeItem == null) { showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
          return; }
        int price = result.getInt("price");
        int augment = result.getInt("augment");
        int auhLevel = result.getInt("augLvl");
        int enchant = result.getInt("enchant");
        int self = player.getStockSelf();
        int charId = result.getInt("ownerId");

        replyMSG.append(new StringBuilder().append("<table width=300><tr><td><img src=\"").append(brokeItem.getIcon()).append("\" width=32 height=32></td><td><font color=LEVEL>").append(brokeItem.getName()).append(" +").append(enchant).append("</font><br></td></tr></table><br><br>").toString());
        replyMSG.append(new StringBuilder().append("\u041F\u0440\u043E\u0434\u0430\u0432\u0435\u0446: ").append(getSellerName(charId)).append("<br><br>").toString());
        replyMSG.append(new StringBuilder().append(getAugmentSkill(augment, auhLevel)).append("<br>").toString());

        if (self == 1) {
          if (player.getObjectId() != charId) { showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
            return; }
          replyMSG.append(new StringBuilder().append("<font color=6699CC>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(price).append(" P.</font><br>").toString());
          replyMSG.append(new StringBuilder().append("<button value=\"\u0417\u0430\u0431\u0440\u0430\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_StockBuyItem ").append(sellId).append("\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
          player.setStockItem(sellId, itemId, enchant, augment, auhLevel);
        }
        else if (getStockBalance(player) >= price) {
          replyMSG.append(new StringBuilder().append("<font color=33CC00>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(price).append(" P.</font><br>").toString());
          replyMSG.append(new StringBuilder().append("<button value=\"\u041A\u0443\u043F\u0438\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_StockBuyItem ").append(sellId).append("\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
          player.setStockItem(sellId, itemId, enchant, augment, auhLevel);
        } else {
          replyMSG.append(new StringBuilder().append("<font color=FF6666>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(price).append(" P.</font><br>").toString());
          replyMSG.append("<font color=999999>[\u041A\u0443\u043F\u0438\u0442\u044C]</font>");
        }
      }
      else {
        replyMSG.append("\u041D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430 \u0438\u043B\u0438 \u0443\u0436\u0435 \u043A\u0443\u043F\u0438\u043B\u0438.");
      }
      Close.SR(statement, result);
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("StockExchange: showStockItem() error: ").append(e).toString());
    } finally {
      Close.CSR(con, statement, result);
    }

    replyMSG.append(new StringBuilder().append("<br><br><a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 1\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br>").toString());
    replyMSG.append("</body></html>");
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private void addStockItem(L2PcInstance player, int objectId, int price)
  {
    if (player.getObjectIdStockI() != objectId) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    L2ItemInstance item = player.getInventory().getItemByObjectId(objectId);
    if (item != null) {
      int enchantLevel = item.getEnchantLevel();
      if (player.getEnchantStockI() != enchantLevel) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }

      NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
      TextBuilder replyMSG = new TextBuilder("<html><body>");

      int itemId = item.getItemId();
      int augmentId = 0;
      int augAttr = 0;
      int augLvl = 0;
      int shadow = 0;
      String itemName = item.getItem().getName();
      String itemIcon = item.getItem().getIcon();

      replyMSG.append("\u0412\u044B\u0441\u0442\u0430\u0432\u043B\u0435\u043D\u0438\u0435 \u043D\u0430 \u0431\u0438\u0440\u0436\u0443:<br>\u0412\u044B\u0441\u0442\u0430\u0432\u043B\u0435\u043D\u0430 \u0448\u043C\u043E\u0442\u043A\u0430<br>");
      replyMSG.append(new StringBuilder().append("<table width=300><tr><td><img src=\"").append(itemIcon).append("\" width=32 height=32></td><td><font color=LEVEL>").append(itemName).append(" (+").append(enchantLevel).append(")</font>g<br></td></tr></table><br><br>").toString());
      replyMSG.append(new StringBuilder().append("<br>\u0417\u0430\u0442\u043E\u0447\u043A\u0430: <font color=bef574>+").append(enchantLevel).append("</font><br>").toString());

      if ((item.isAugmented()) && (item.getAugmentation().getAugmentSkill() != null)) {
        L2Skill augment = item.getAugmentation().getAugmentSkill();
        String augName = augment.getName();
        String type = "";
        if (augment.isActive())
          type = "\u0410\u043A\u0442\u0438\u0432";
        else if (augment.isPassive())
          type = "\u041F\u0430\u0441\u0441\u0438\u0432";
        else {
          type = "\u0428\u0430\u043D\u0441";
        }

        augmentId = augment.getId();
        augAttr = item.getAugmentation().getAugmentationId();
        augLvl = augment.getLevel();

        replyMSG.append(new StringBuilder().append("<br>\u0410\u0443\u0433\u043C\u0435\u043D\u0442: <font color=bef574>").append(augName).append(" (").append(type).append(")</font><br>").toString());
      }

      if (!player.destroyItemByItemId("DS addStockItem", itemId, 1, player, true)) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }

      if (addToStockList(itemId, enchantLevel, augmentId, augAttr, augLvl, price, player.getObjectId(), shadow))
      {
        replyMSG.append("\u0412\u044B\u0441\u0442\u0430\u0432\u043B\u0435\u043D\u0430!<br><br>");
      }
      else replyMSG.append("\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430!<br><br>");

      player.setStockInventoryItem(0, 0);

      replyMSG.append(new StringBuilder().append("<br><a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 1\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F.</a>").toString());
      replyMSG.append("</body></html>");
      reply.setHtml(replyMSG.toString());
      player.sendPacket(reply);
    } else {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }
  }

  private void buyStockItem(L2PcInstance player, int sellId)
  {
    if (player.getSellIdStock() != sellId) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body>\u041F\u043E\u0434\u043E\u0436\u0434\u0438\u0442\u0435...</body></html>");
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
    try
    {
      Thread.sleep(Rnd.get(1000, 2000));
    }
    catch (InterruptedException e) {
    }
    buyStockItemFinish(player, sellId);
  }

  private void buyStockItemFinish(L2PcInstance player, int sellId)
  {
    if (player.getSellIdStock() != sellId) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    Connect con = null;
    PreparedStatement statement = null;
    ResultSet result = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);

      statement = con.prepareStatement("SELECT itemId, enchant, augment, augAttr, augLvl, price, ownerId, shadow FROM `z_stock_items` WHERE `id` = ? LIMIT 1");
      statement.setInt(1, sellId);
      result = statement.executeQuery();

      if (result.next()) {
        int itemId = result.getInt("itemId");
        if (player.getItemIdStock() != itemId) { showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
          return; }
        L2Item brokeItem = ItemTable.getInstance().getTemplate(itemId);
        if (brokeItem == null) { showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
          return; }
        int price = result.getInt("price");
        int augment = result.getInt("augment");
        int auhLevel = result.getInt("augLvl");
        int enchant = result.getInt("enchant");
        int augAttr = result.getInt("augAttr");
        int ownerId = result.getInt("ownerId");
        int self = player.getStockSelf();

        if ((self == 1) && (player.getObjectId() != ownerId)) { showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
          return;
        }
        if ((player.getEnchantStock() != enchant) || (player.getAugmentStock() != augment) || (player.getAuhLeveStock() != auhLevel)) { showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
          return;
        }
        if ((self == 0) && 
          (!updateBalance(player, ownerId, price))) {
          showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0431\u0430\u043B\u0430\u043D\u0441\u0430.");
          return;
        }
        if (!deleteFromList(player, sellId, itemId, enchant, augment, auhLevel, price, ownerId, self)) { showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
          return;
        }
        player.setStockItem(-1, 0, 0, 0, 0);
        player.setStockSelf(0);

        L2ItemInstance buyItem = player.getInventory().addItem("DS buyStockItemFinish", itemId, 1, player, player.getTarget());
        if (buyItem == null) { showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
          return;
        }
        if (enchant > 0) {
          buyItem.setEnchantLevel(enchant);
        }
        if (augment > 0) {
          buyItem.setAugmentation(new L2Augmentation(buyItem, augAttr, augment, auhLevel, true));
        }

        NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
        TextBuilder replyMSG = new TextBuilder("<html><body>");
        if (self == 0) {
          L2PcInstance owner = L2World.getInstance().getPlayer(ownerId);
          if (owner != null) {
            owner.sendMessage("\u0423\u0432\u0435\u0434\u043E\u043C\u043B\u0435\u043D\u0438\u0435 \u0441 \u0431\u0438\u0440\u0436\u0438: \u043F\u0440\u043E\u0432\u0435\u0440\u044C \u043F\u043E\u0447\u0442\u0443");
            owner.sendPacket(new ExMailArrived());
            sendLetter(getSellerName(ownerId), itemId, enchant, augment, auhLevel, price);
          }
          replyMSG.append(new StringBuilder().append("\u0421\u0434\u0435\u043B\u043A\u0430 \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0430, \u0441 \u0432\u0430\u0448\u0435\u0433\u043E \u0441\u0447\u0435\u0442\u0430 \u0441\u043F\u0438\u0441\u0430\u043D\u043E: ").append(price).append(" ").append(VAL_NAME).append("").toString());
        } else {
          replyMSG.append("<br>\u041F\u0440\u0435\u0434\u043C\u0435\u0442 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0441\u043D\u044F\u0442 \u0441 \u0431\u0438\u0440\u0436\u0438");
        }
        replyMSG.append(new StringBuilder().append("<br><a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 1\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F.</a></body></html>").toString());
        reply.setHtml(replyMSG.toString());
        player.sendPacket(reply);

        player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(itemId));

        player.sendItems(false);
        player.sendChanges(); } else { showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }
      Close.SR(statement, result);
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("StockExchange: buyStockItemFinish() error: ").append(e).toString());
    } finally {
      Close.CSR(con, statement, result);
    }
  }

  private void sendLetter(String sName, int itemId, int enchant, int augment, int auhLevel, int price) {
    L2Item brokeItem = ItemTable.getInstance().getTemplate(itemId);
    if (brokeItem == null) {
      return;
    }

    Date date = new Date();
    SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timef = new SimpleDateFormat("HH:mm:ss");

    int plus = price / 100 * NALOG_NPS;

    StringBuilder text = new StringBuilder();
    text.append(new StringBuilder().append("\u0418\u0442\u0435\u043C: <font color=FF3399>").append(brokeItem.getName()).append(" +").append(enchant).append(" ").append(getAugmentSkill(augment, auhLevel)).append("</font><br1> \u0431\u044B\u043B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u043F\u0440\u043E\u0434\u0430\u043D.<br1>").toString());
    text.append(new StringBuilder().append("\u0417\u0430 \u0432\u044B\u0447\u0435\u0442\u043E\u043C \u043D\u0430\u043B\u043E\u0433\u0430 ").append(NALOG_NPS).append("%, \u0432\u0430\u0448 \u0431\u0430\u043B\u0430\u043D\u0441 \u043F\u043E\u043F\u043E\u043B\u043D\u0435\u043D \u043D\u0430 ").append(plus).append(" P.<br1>").toString());
    text.append("\u0411\u043B\u0430\u0433\u043E\u0434\u0430\u0440\u0438\u043C \u0437\u0430 \u0441\u043E\u0442\u0440\u0443\u0434\u043D\u0438\u0447\u0435\u0441\u0442\u0432\u043E.");

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("INSERT INTO `z_post_in` (`id`,`tema`,`text`,`from`,`to`,`type`,`date`,`time`) VALUES (NULL,?,?,?,?,?,?,?)");
      statement.setString(1, "\u0428\u043C\u043E\u0442\u043A\u0430 \u043F\u0440\u043E\u0434\u0430\u043D\u0430");
      statement.setString(2, text.toString());
      statement.setString(3, "~\u0411\u0438\u0440\u0436\u0430.");
      statement.setString(4, sName);
      statement.setInt(5, 0);
      statement.setString(6, datef.format(date).toString());
      statement.setString(7, timef.format(date).toString());

      statement.execute();
      Close.S(statement);
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("StockExchange: sendLetter() error: ").append(e).toString());
    } finally {
      Close.CS(con, statement);
    }
  }

  private void StockSertifyBuy(L2PcInstance player)
  {
    L2ItemInstance coins = player.getInventory().getItemByItemId(DONATE_COIN);
    if ((coins == null) || (coins.getCount() < SERTIFY_PRICE)) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    player.destroyItemByItemId("DS StockSertifyBuy", DONATE_COIN, SERTIFY_PRICE, player, true);

    L2ItemInstance buyItem = player.getInventory().addItem("DS buyStockItemFinish", STOCK_SERTIFY, 1, player, player.getTarget());
    if (buyItem == null) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(buyItem.getItemId()));
    player.sendItems(false);
    player.sendChanges();

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO `z_stock_accounts` (`charId`,`balance`,`ban`) VALUES (?,?,?)");
      statement.setInt(1, player.getObjectId());
      statement.setInt(2, FIRST_BALANCE);
      statement.setInt(3, 0);
      statement.execute();
      Close.S(statement);
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("StockExchange: StockSertifyBuy() error: ").append(e).toString());
    } finally {
      Close.CS(con, statement);
    }

    showWelcome(player, 1);
  }

  private boolean deleteFromList(L2PcInstance player, int sellId, int itemId, int enchant, int augment, int auhLevel, int price, int ownerId, int self)
  {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM z_stock_items WHERE id = ? LIMIT 1");
      statement.setInt(1, sellId);
      statement.execute();
      Close.S(statement);

      if (self == 0) {
        date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd mm (HH:mm:ss)");

        statement = con.prepareStatement("INSERT INTO z_stock_logs (date, charId, itemId, enchant, augment, augLvl, price, ownerId) VALUES (?,?,?,?,?,?,?,?)");
        statement.setString(1, new StringBuilder().append(sdf.format(date)).append(":").toString());
        statement.setInt(2, player.getObjectId());
        statement.setInt(3, itemId);
        statement.setInt(4, enchant);
        statement.setInt(5, augment);
        statement.setInt(6, auhLevel);
        statement.setInt(7, price);
        statement.setInt(8, ownerId);
        statement.execute();
        Close.S(statement);
      }
      Date date = 1;
      return date;
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("StockExchange: deleteFromList() error: ").append(e).toString());
    } finally {
      Close.CS(con, statement);
    }
    return false;
  }

  private boolean addToStockList(int itemId, int enchantLevel, int augmentId, int augAttr, int augLvl, int price, int ownerId, int shadow)
  {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO z_stock_items (id,itemId, enchant, augment, augAttr, augLvl, price, ownerId, shadow) VALUES (NULL,?,?,?,?,?,?,?,?)");
      statement.setInt(1, itemId);
      statement.setInt(2, enchantLevel);
      statement.setInt(3, augmentId);
      statement.setInt(4, augAttr);
      statement.setInt(5, augLvl);
      statement.setInt(6, price);
      statement.setInt(7, ownerId);
      statement.setInt(8, shadow);
      statement.execute();
      Close.S(statement);
      int i = 1;
      return i;
    }
    catch (SQLException e)
    {
      _log.warning(new StringBuilder().append("StockExchange: addToStockList() error: ").append(e).toString());
      e.printStackTrace();
    } finally {
      Close.CS(con, statement);
    }
    return false;
  }

  private boolean updateBalance(L2PcInstance player, int ownerId, int price)
  {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE z_stock_accounts SET `balance` = `balance`-? WHERE charId=? LIMIT 1");
      statement.setInt(1, price);
      statement.setInt(2, player.getObjectId());
      statement.executeUpdate();
      Close.S(statement);

      int plus = price / 100 * NALOG_NPS;

      statement = con.prepareStatement("UPDATE z_stock_accounts SET `balance` = `balance`+? WHERE charId=? LIMIT 1");
      statement.setInt(1, plus);
      statement.setInt(2, ownerId);
      statement.executeUpdate();
      Close.S(statement);
      int i = 1;
      return i;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    } finally {
      Close.CS(con, statement);
    }
    return false;
  }

  private void showError(L2PcInstance player, String errorText)
  {
    player.setStockItem(-1, 0, 0, 0, 0);
    player.setStockInventoryItem(0, 0);
    player.setStockSelf(0);

    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder(new StringBuilder().append("<html><body> \u041E\u0439!<br> ").append(errorText).append("</body></html>").toString());
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);

    player.sendActionFailed();
  }

  private String getAugmentSkill(int skillId, int skillLvl)
  {
    L2Skill augment = SkillTable.getInstance().getInfo(skillId, 1);
    if (augment == null) {
      return "";
    }

    String augName = augment.getName();
    String type = "";
    if (augment.isActive())
      type = "\u0410\u043A\u0442\u0438\u0432";
    else if (augment.isPassive())
      type = "\u041F\u0430\u0441\u0441\u0438\u0432";
    else {
      type = "\u0428\u0430\u043D\u0441";
    }

    augName = augName.replace("Item Skill: ", "");

    return new StringBuilder().append("<font color=336699>\u0410\u0443\u0433\u043C\u0435\u043D\u0442:</font> <font color=bef574>").append(augName).append(" (").append(type).append(":").append(skillLvl).append("lvl)</font>").toString();
  }

  private String getSellerName(int charId)
  {
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet result = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("SELECT char_name FROM `characters` WHERE `Obj_Id`=? LIMIT 1");
      statement.setInt(1, charId);
      result = statement.executeQuery();

      if (result.next()) {
        String str = result.getString("char_name");
        return str;
      }
      Close.SR(statement, result);
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("StockExchange: getSellerName() error: ").append(e).toString());
    } finally {
      Close.CSR(con, statement, result);
    }

    return "";
  }

  private void incBalance(L2PcInstance player)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder(new StringBuilder().append("<html><body>\u041F\u0435\u0440\u0435\u0432\u043E\u0434 ").append(DONATE_COIN_NEMA).append(" \u0432 \u0432\u0430\u043B\u044E\u0442\u0443 \u0431\u0438\u0440\u0436\u0438:<br>").toString());

    L2ItemInstance coin = player.getInventory().getItemByItemId(DONATE_COIN);
    if ((coin != null) && (coin.getCount() >= 1)) {
      long dnCount = coin.getCount();
      long stCount = dnCount * DONATE_RATE;
      replyMSG.append(new StringBuilder().append("<table border=1 width=290><tr><td>\u041A\u0443\u0440\u0441: 1").append(DONATE_COIN_NEMA).append(" \u0437\u0430 ").append(DONATE_RATE).append(" P.</td></tr></table><br>").toString());
      replyMSG.append(new StringBuilder().append("<font color=99CC99>\u0423 \u0432\u0430\u0441 \u0435\u0441\u0442\u044C ").append(dnCount).append(" ").append(DONATE_COIN_NEMA).append(";</font><br>").toString());
      replyMSG.append(new StringBuilder().append("\u0412\u044B \u043C\u043E\u0436\u0435\u0442\u0435 \u043F\u043E\u043F\u043E\u043B\u043D\u0438\u0442\u044C \u0441\u0447\u0435\u0442 \u0431\u0438\u0440\u0436\u0438 \u043D\u0430 ").append(stCount).append(" P. <button value=\"\u041D\u0430 \u0432\u0441\u0435!\" action=\"bypass -h npc_").append(getObjectId()).append("_StockAddBalance 0\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">").toString());
      replyMSG.append("\u0418\u043B\u0438 \u0432\u0432\u0435\u0434\u0438\u0442\u0435 \u0441\u0432\u043E\u0435 \u0447\u0438\u0441\u043B\u043E..<br>");
      replyMSG.append("\u0422\u0430\u043A \u0441\u043A\u043E\u043B\u044C\u043A\u043E?<br><edit var=\"coins\" width=200 length=\"16\">");
      replyMSG.append(new StringBuilder().append("<button value=\"\u041F\u043E\u043F\u043E\u043B\u043D\u0438\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_StockAddBalance $coins\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
    } else {
      replyMSG.append(new StringBuilder().append("<font color=CC0000>\u041A \u043E\u0431\u043C\u0435\u043D\u0443 \u043F\u0440\u0438\u043D\u0438\u043C\u0430\u044E\u0442\u0441\u044F \u0442\u043E\u043B\u044C\u043A\u043E ").append(DONATE_COIN_NEMA).append("</font>").toString());
    }

    replyMSG.append(new StringBuilder().append("<br><a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 4\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F.</a></body></html>").toString());
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private void StockAddBalance(L2PcInstance player, int coins)
  {
    int plus = 0;
    int cnCount = 0;
    L2ItemInstance coin = player.getInventory().getItemByItemId(DONATE_COIN);
    if (coin != null) {
      cnCount = coin.getCount();
      if ((coins == 0) && (cnCount >= 1)) {
        plus = cnCount * DONATE_RATE;
      } else if (cnCount >= coins) {
        plus = coins * DONATE_RATE;
      } else {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }
    }

    if (coins == 0) {
      coins = cnCount;
    }

    if (!player.destroyItemByItemId("DS StockAddBalance", DONATE_COIN, coins, player, true)) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    Connect con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE z_stock_accounts SET `balance` = `balance`+? WHERE charId=? LIMIT 1");
      statement.setInt(1, plus);
      statement.setInt(2, player.getObjectId());
      statement.executeUpdate();
      Close.S(statement);

      NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
      TextBuilder replyMSG = new TextBuilder(new StringBuilder().append("<html><body>\u041F\u0435\u0440\u0435\u0432\u043E\u0434 ").append(DONATE_COIN_NEMA).append(" \u0432 \u0432\u0430\u043B\u044E\u0442\u0443 \u0431\u0438\u0440\u0436\u0438:<br>").toString());
      replyMSG.append(new StringBuilder().append("<font color=99CC99>\u0411\u0430\u043B\u0430\u043D\u0441 \u043F\u043E\u043F\u043E\u043B\u043D\u0435\u043D \u043D\u0430 ").append(plus).append(" ").append(VAL_NAME).append("</font>").toString());
      replyMSG.append(new StringBuilder().append("<br><a action=\"bypass -h npc_").append(getObjectId()).append("_StockExchange 4\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F.</a></body></html>").toString());
      reply.setHtml(replyMSG.toString());
      player.sendPacket(reply);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      Close.CS(con, statement);
    }
  }

  private void showVoteServiceWindow(L2PcInstance player, int service, int page)
  {
    FastTable items = null;
    if (service == 1)
      items = player.getInventory().getAllItemsAug();
    else {
      items = player.getInventory().getAllItemsEnch();
    }

    if ((items == null) || (items.isEmpty())) {
      player.sendHtmlMessage("\u0413\u043E\u043B\u043E\u0441\u043E\u0432\u0430\u043D\u0438\u0435", "\u0412\u0430\u0448 \u0438\u043D\u0432\u0435\u043D\u0442\u0430\u0440\u044C \u043F\u0443\u0441\u0442.");
      return;
    }

    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body>");
    if (service == 1)
      replyMSG.append("\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u041B\u0421:<br>\u041E\u0442\u043A\u0443\u0434\u0430 \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0438\u043C?<br><br><table width=300>");
    else {
      replyMSG.append("\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u0437\u0430\u0442\u043E\u0447\u043A\u0438:<br>\u041E\u0442\u043A\u0443\u0434\u0430 \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0438\u043C?<br><br><table width=300>");
    }

    int objectId = 0;
    String itemName = "";
    int enchantLevel = 0;
    String itemIcon = "";
    int itemType = 0;

    int begin = page == 2 ? PAGE_LIMIT : page == 1 ? 0 : page * PAGE_LIMIT;
    int end = begin + PAGE_LIMIT;
    if (end > items.size()) {
      end = items.size();
    }
    L2ItemInstance item = null;
    int i = begin; for (int n = end; i < n; i++) {
      item = (L2ItemInstance)items.get(i);
      if (item == null)
      {
        continue;
      }
      objectId = item.getObjectId();
      itemName = item.getItem().getName();
      enchantLevel = item.getEnchantLevel();
      itemIcon = item.getItem().getIcon();
      itemType = item.getItem().getType2();

      if (service == 1) {
        if ((item.isAugmented()) && (item.getAugmentation().getAugmentSkill() != null)) {
          L2Skill aug = item.getAugmentation().getAugmentSkill();
          replyMSG.append(new StringBuilder().append("<tr><td><img src=\"").append(itemIcon).append("\" width=32 height=32></td><td><a action=\"bypass -h npc_").append(getObjectId()).append("_voteItemShow_1_").append(objectId).append("\">").append(itemName).append(" (+").append(enchantLevel).append(")</a><br1> ").append(getAugmentSkill(aug.getId(), aug.getLevel())).append(" </td></tr>").toString());
        }
      } else {
        if ((service != 2) || (
          (itemType != 0) && (itemType != 1) && (itemType != 2))) continue;
        replyMSG.append(new StringBuilder().append("<tr><td><img src=\"").append(itemIcon).append("\" width=32 height=32></td><td><a action=\"bypass -h npc_").append(getObjectId()).append("_voteItemShow_2_").append(objectId).append("\">").append(itemName).append(" (+").append(enchantLevel).append(")</a></td></tr>").toString());
      }

    }

    player.setVote1Item(0);
    player.setVote2Item(0);
    player.setVoteEnchant(0);
    player.setVoteAugment(null);

    replyMSG.append("</table><br><br>");
    if (items.size() > PAGE_LIMIT) {
      replyMSG.append("\u0421\u0442\u0440\u0430\u043D\u0438\u0446\u044B: <br1>");
      int pages = items.size() / PAGE_LIMIT + 1;
      for (int i = 0; i < pages; i++) {
        int cur = i + 1;
        if (page == cur)
          replyMSG.append(new StringBuilder().append("&nbsp;&nbsp;").append(cur).append("&nbsp;&nbsp;").toString());
        else {
          replyMSG.append(new StringBuilder().append("&nbsp;&nbsp;<a action=\"bypass -h npc_").append(getObjectId()).append("_voteService ").append(service).append(" ").append(cur).append("\">").append(cur).append("</a>&nbsp;&nbsp;").toString());
        }
      }
    }
    replyMSG.append(new StringBuilder().append("<br><a action=\"bypass -h npc_").append(getObjectId()).append("_voteHome\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F.</a></body></html>").toString());
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
    items.clear();
    items = null;
  }

  private void showVoteNextItems(L2PcInstance player, int service, int page) {
    FastTable items = player.getInventory().getAllItemsNext(player.getVote1Item(), service);
    if ((items == null) || (items.isEmpty())) {
      player.sendHtmlMessage("\u0413\u043E\u043B\u043E\u0441\u043E\u0432\u0430\u043D\u0438\u0435", "\u0412\u0430\u0448 \u0438\u043D\u0432\u0435\u043D\u0442\u0430\u0440\u044C \u043F\u0443\u0441\u0442.");
      return;
    }

    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body>");
    if (service == 1)
      replyMSG.append("\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u041B\u0421:<br>\u041A\u0443\u0434\u0430 \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0438\u043C?<br><br><table width=300>");
    else {
      replyMSG.append("\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u0437\u0430\u0442\u043E\u0447\u043A\u0438:<br>\u041A\u0443\u0434\u0430 \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0438\u043C?<br><br><table width=300>");
    }

    int begin = page == 2 ? PAGE_LIMIT : page == 1 ? 0 : page * PAGE_LIMIT;
    int end = begin + PAGE_LIMIT;
    if (end > items.size()) {
      end = items.size();
    }
    L2ItemInstance item = null;
    int i = begin; for (int n = end; i < n; i++) {
      item = (L2ItemInstance)items.get(i);
      if (item == null)
      {
        continue;
      }
      if (service == 1)
        replyMSG.append(new StringBuilder().append("<tr><td><img src=\"").append(item.getItem().getIcon()).append("\" width=32 height=32></td><td><a action=\"bypass -h npc_").append(getObjectId()).append("_voteItem2Show_1_").append(item.getObjectId()).append("\">").append(item.getItem().getName()).append(" (+").append(item.getEnchantLevel()).append(")</a></td></tr>").toString());
      else {
        replyMSG.append(new StringBuilder().append("<tr><td><img src=\"").append(item.getItem().getIcon()).append("\" width=32 height=32></td><td><a action=\"bypass -h npc_").append(getObjectId()).append("_voteItem2Show_2_").append(item.getObjectId()).append("\">").append(item.getItem().getName()).append(" (+").append(item.getEnchantLevel()).append(")</a></td></tr>").toString());
      }
    }

    replyMSG.append("</table><br><br>");
    if (items.size() > PAGE_LIMIT) {
      replyMSG.append("\u0421\u0442\u0440\u0430\u043D\u0438\u0446\u044B: <br1>");
      int pages = items.size() / PAGE_LIMIT + 1;
      for (int i = 0; i < pages; i++) {
        int cur = i + 1;
        if (page == cur)
          replyMSG.append(new StringBuilder().append("&nbsp;&nbsp;").append(cur).append("&nbsp;&nbsp;").toString());
        else {
          replyMSG.append(new StringBuilder().append("&nbsp;&nbsp;<a action=\"bypass -h npc_").append(getObjectId()).append("_voteStep2 ").append(service).append(" ").append(cur).append("\">").append(cur).append("</a>&nbsp;&nbsp;").toString());
        }
      }
    }
    replyMSG.append(new StringBuilder().append("<br><a action=\"bypass -h npc_").append(getObjectId()).append("_voteHome\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F.</a></body></html>").toString());
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private void showVote1Item(L2PcInstance player, int service, int objectId) {
    L2ItemInstance item = player.getInventory().getItemByObjectId(objectId);
    if (item != null) {
      if (!item.canBeEnchanted()) {
        showVoteErr0r(player, service, 1);
        return;
      }

      if ((service == 1) && (!item.isAugmented())) {
        showVoteErr0r(player, service, 1);
        return;
      }

      NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
      TextBuilder replyMSG = new TextBuilder("<html><body>");

      int coinId = 0;
      String itemName = item.getItem().getName();
      int enchantLevel = item.getEnchantLevel();
      String itemIcon = item.getItem().getIcon();

      if (service == 1) {
        replyMSG.append("\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u041B\u0421:<br>\u0418\u0437 \u044D\u0442\u043E\u0439 \u0448\u043C\u043E\u0442\u043A\u0438 \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0438\u043C?<br>");
        coinId = AUGMENT_COIN;
      } else {
        replyMSG.append("\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u0437\u0430\u0442\u043E\u0447\u043A\u0438:<br>\u0418\u0437 \u044D\u0442\u043E\u0439 \u0448\u043C\u043E\u0442\u043A\u0438 \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0438\u043C?<br>");
        coinId = ENCHANT_COIN;
      }

      L2ItemInstance coin = player.getInventory().getItemByItemId(coinId);

      if (service == 1) {
        replyMSG.append(new StringBuilder().append("<table width=300><tr><td><img src=\"").append(itemIcon).append("\" width=32 height=32></td><td><font color=LEVEL>").append(itemName).append(" (+").append(enchantLevel).append(")</font>g<br></td></tr></table><br><br>").toString());
        L2Skill augment = item.getAugmentation().getAugmentSkill();
        if (augment == null) {
          showVoteErr0r(player, service, 1);
          return;
        }

        String augName = augment.getName();
        String type = "";
        if (augment.isActive())
          type = "(\u0410\u043A\u0442\u0438\u0432\u043D\u044B\u0439)";
        else if (augment.isPassive())
          type = "(\u041F\u0430\u0441\u0441\u0438\u0432\u043D\u044B\u0439)";
        else {
          type = "(\u0428\u0430\u043D\u0441\u043E\u0432\u044B\u0439)";
        }

        replyMSG.append(new StringBuilder().append("<br>\u0410\u0443\u0433\u043C\u0435\u043D\u0442: <font color=bef574>").append(augName).append("").append(type).append("</font><br>").toString());
        if ((coin != null) && (coin.getCount() >= AUGMENT_PRICE)) {
          replyMSG.append(new StringBuilder().append("<font color=33CC00>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(AUGMENT_PRICE).append(" ").append(AUGMENT_COIN_NAME).append("</font><br>").toString());
          replyMSG.append(new StringBuilder().append("<button value=\"\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_voteStep2 1 1\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
          player.setVote1Item(objectId);
          player.setVoteAugment(augment);
        } else {
          replyMSG.append(new StringBuilder().append("<font color=FF6666>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(AUGMENT_PRICE).append(" ").append(AUGMENT_COIN_NAME).append("</font><br>").toString());
          replyMSG.append("<font color=999999>[\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C]</font>");
        }
      } else {
        int vePrice = ENCHANT_PRICE * enchantLevel;
        replyMSG.append(new StringBuilder().append("<table width=300><tr><td><img src=\"").append(itemIcon).append("\" width=32 height=32></td><td><font color=LEVEL>").append(itemName).append(" (+").append(enchantLevel).append(")</font>g<br></td></tr></table><br><br>").toString());
        replyMSG.append(new StringBuilder().append("<br>\u0417\u0430\u0442\u043E\u0447\u043A\u0430: <font color=bef574>+").append(enchantLevel).append("</font><br>").toString());
        if ((coin != null) && (coin.getCount() >= vePrice)) {
          replyMSG.append(new StringBuilder().append("<font color=33CC00>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(vePrice).append(" ").append(ENCHANT_COIN_NAME).append("</font><br>").toString());
          replyMSG.append(new StringBuilder().append("<button value=\"\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_voteStep2 2 1\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
          player.setVote1Item(objectId);
          player.setVoteEnchant(enchantLevel);
        } else {
          replyMSG.append(new StringBuilder().append("<font color=FF6666>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(vePrice).append(" ").append(ENCHANT_COIN_NAME).append("</font><br>").toString());
          replyMSG.append("<font color=999999>[\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C]</font>");
        }
      }
      replyMSG.append(new StringBuilder().append("<br><a action=\"bypass -h npc_").append(getObjectId()).append("_voteHome\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F.</a>").toString());
      replyMSG.append("</body></html>");
      reply.setHtml(replyMSG.toString());
      player.sendPacket(reply);
    } else {
      showVoteErr0r(player, service, 2);
      return;
    }
  }

  private void showVote2Item(L2PcInstance player, int service, int objectId) {
    L2ItemInstance item = player.getInventory().getItemByObjectId(objectId);
    if (item != null) {
      if (!item.canBeEnchanted()) {
        showVoteErr0r(player, service, 1);
        return;
      }

      if ((service == 1) && (item.isAugmented())) {
        showVoteErr0r(player, service, 1);
        return;
      }

      NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
      TextBuilder replyMSG = new TextBuilder("<html><body>");

      int coinId = 0;
      String itemName = item.getItem().getName();
      int enchantLevel = item.getEnchantLevel();
      String itemIcon = item.getItem().getIcon();

      if (service == 1) {
        replyMSG.append("\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u041B\u0421:<br>\u0412 \u044D\u0442\u0443 \u0448\u043C\u043E\u0442\u043A\u0443 \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0438\u043C?<br>");
        coinId = AUGMENT_COIN;
      } else {
        replyMSG.append("\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u0437\u0430\u0442\u043E\u0447\u043A\u0438:<br>\u0412 \u044D\u0442\u0443 \u0448\u043C\u043E\u0442\u043A\u0443 \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0438\u043C?<br>");
        coinId = ENCHANT_COIN;
      }

      L2ItemInstance coin = player.getInventory().getItemByItemId(coinId);

      if (service == 1) {
        replyMSG.append(new StringBuilder().append("<table width=300><tr><td><img src=\"").append(itemIcon).append("\" width=32 height=32></td><td><font color=LEVEL>").append(itemName).append(" (+").append(enchantLevel).append(")</font>g<br></td></tr></table><br><br>").toString());
        if ((coin != null) && (coin.getCount() >= AUGMENT_PRICE)) {
          replyMSG.append(new StringBuilder().append("<font color=33CC00>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(AUGMENT_PRICE).append(" ").append(AUGMENT_COIN_NAME).append("</font><br>").toString());
          replyMSG.append(new StringBuilder().append("<button value=\"\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_voteStep3_1\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
          player.setVote2Item(objectId);
        } else {
          replyMSG.append(new StringBuilder().append("<font color=FF6666>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(AUGMENT_PRICE).append(" Ble Eva</font><br>").toString());
          replyMSG.append("<font color=999999>[\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C]</font>");
        }
      } else {
        int enchPrice = ENCHANT_PRICE * player.getVoteEnchant();
        replyMSG.append(new StringBuilder().append("<table width=300><tr><td><img src=\"").append(itemIcon).append("\" width=32 height=32></td><td><font color=LEVEL>").append(itemName).append(" (+").append(enchantLevel).append(")</font>g<br></td></tr></table><br><br>").toString());
        replyMSG.append(new StringBuilder().append("<br>\u0417\u0430\u0442\u043E\u0447\u043A\u0430: <font color=bef574>+").append(enchantLevel).append("</font><br>").toString());
        if ((coin != null) && (coin.getCount() >= enchPrice)) {
          if (enchantLevel > 0) {
            replyMSG.append("<font color=CC6633>\u0410\u043A\u043A\u0443\u0440\u0430\u0442\u043D\u043E! \u041F\u0443\u0448\u043A\u0430 \u0443\u0436\u0435 \u0437\u0430\u0442\u043E\u0447\u0435\u043D\u0430 \u0438 \u043F\u0440\u0438 \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0435 \u0442\u043E\u0447\u043A\u0430 \u043D\u0430 \u043D\u0435\u0439 \u043F\u0440\u043E\u043F\u0430\u0434\u0435\u0442!</font><br><br>");
          }
          replyMSG.append(new StringBuilder().append("<font color=33CC00>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(enchPrice).append(" ").append(ENCHANT_COIN_NAME).append("</font><br>").toString());
          replyMSG.append(new StringBuilder().append("<button value=\"\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_voteStep3_2\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
          player.setVote2Item(objectId);
        } else {
          replyMSG.append(new StringBuilder().append("<font color=FF6666>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(enchPrice).append(" ").append(ENCHANT_COIN_NAME).append("</font><br>").toString());
          replyMSG.append("<font color=999999>[\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C]</font>");
        }
      }
      replyMSG.append(new StringBuilder().append("<br><a action=\"bypass -h npc_").append(getObjectId()).append("_voteHome\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F.</a>").toString());
      replyMSG.append("</body></html>");
      reply.setHtml(replyMSG.toString());
      player.sendPacket(reply);
    } else {
      showVoteErr0r(player, service, 2);
      return;
    }
  }

  private void showVoteAgree(L2PcInstance player, int service) {
    L2ItemInstance item1 = player.getInventory().getItemByObjectId(player.getVote1Item());
    L2ItemInstance item2 = player.getInventory().getItemByObjectId(player.getVote2Item());
    if ((item1 != null) && (item2 != null)) {
      if ((!item1.canBeEnchanted()) || (!item2.canBeEnchanted())) {
        showVoteErr0r(player, service, 1);
        return;
      }

      if ((service == 1) && (!item1.isAugmented())) {
        showVoteErr0r(player, service, 1);
        return;
      }

      if ((service == 1) && (item2.isAugmented())) {
        showVoteErr0r(player, service, 1);
        return;
      }

      NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
      TextBuilder replyMSG = new TextBuilder("<html><body>");

      int coinId = 0;
      String itemName1 = item1.getItem().getName();
      int enchantLevel1 = item1.getEnchantLevel();
      String itemName2 = item2.getItem().getName();
      int enchantLevel2 = item2.getEnchantLevel();
      String itemIcon1 = item1.getItem().getIcon();
      String itemIcon2 = item2.getItem().getIcon();

      if (service == 1) {
        replyMSG.append("\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u041B\u0421:<br>\u041F\u043E\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0430\u0435\u0442\u0435?<br>");
        coinId = AUGMENT_COIN;
      } else {
        replyMSG.append("\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u0437\u0430\u0442\u043E\u0447\u043A\u0438:<br>\u041F\u043E\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0430\u0435\u0442\u0435?<br>");
        coinId = ENCHANT_COIN;
      }

      L2ItemInstance coin = player.getInventory().getItemByItemId(coinId);

      if (service == 1) {
        L2Skill augment = item1.getAugmentation().getAugmentSkill();

        if ((augment == null) || (player.getVoteAugment() != augment)) {
          showVoteErr0r(player, service, 1);
          return;
        }

        String augName = augment.getName();
        String type = "";
        if (augment.isActive())
          type = "(\u0410\u043A\u0442\u0438\u0432\u043D\u044B\u0439)";
        else if (augment.isPassive())
          type = "(\u041F\u0430\u0441\u0441\u0438\u0432\u043D\u044B\u0439)";
        else {
          type = "(\u0428\u0430\u043D\u0441\u043E\u0432\u044B\u0439)";
        }

        replyMSG.append(new StringBuilder().append("<br>\u0410\u0443\u0433\u043C\u0435\u043D\u0442: <font color=bef574>").append(augName).append("").append(type).append("</font><br>").toString());
        replyMSG.append(new StringBuilder().append("<table width=300><tr><td><img src=\"").append(itemIcon1).append("\" width=32 height=32></td><td> >>>>>> </td><td><img src=\"").append(itemIcon2).append("\" width=32 height=32></td></tr></table><br><br>").toString());
        replyMSG.append(new StringBuilder().append("\u0418\u0437: <font color=LEVEL>").append(itemName1).append(" (+").append(enchantLevel1).append(")</font><br>").toString());
        replyMSG.append(new StringBuilder().append("\u0412: <font color=LEVEL>").append(itemName2).append(" (+").append(enchantLevel2).append(")</font><br>").toString());

        if ((coin != null) && (coin.getCount() >= AUGMENT_PRICE)) {
          replyMSG.append(new StringBuilder().append("<font color=33CC00>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(AUGMENT_PRICE).append(" ").append(AUGMENT_COIN_NAME).append("</font><br>").toString());
          replyMSG.append(new StringBuilder().append("<button value=\"\u041F\u0435\u0440\u0435\u043D\u0435\u0441\u0442\u0438\" action=\"bypass -h npc_").append(getObjectId()).append("_voteComplete_1\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
        } else {
          replyMSG.append(new StringBuilder().append("<font color=FF6666>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(AUGMENT_PRICE).append(" Ble Eva</font><br>").toString());
          replyMSG.append("<font color=999999>[\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C]</font>");
        }
      } else {
        if ((player.getVoteEnchant() != enchantLevel1) || (enchantLevel1 == 0)) {
          showVoteErr0r(player, service, 1);
          return;
        }

        int enchPrice = ENCHANT_PRICE * enchantLevel1;

        replyMSG.append(new StringBuilder().append("<br>\u0417\u0430\u0442\u043E\u0447\u043A\u0430: <font color=bef574>+").append(enchantLevel1).append("</font><br>").toString());
        replyMSG.append(new StringBuilder().append("<table width=220><tr><td><img src=\"").append(itemIcon1).append("\" width=32 height=32></td><td> >>>>>> </td><td><img src=\"").append(itemIcon2).append("\" width=32 height=32></td></tr></table><br><br>").toString());
        replyMSG.append(new StringBuilder().append("\u0418\u0437: <font color=LEVEL>").append(itemName1).append(" (+").append(enchantLevel1).append(")</font><br>").toString());
        replyMSG.append(new StringBuilder().append("\u0412: <font color=LEVEL>").append(itemName2).append(" (+").append(enchantLevel2).append(")</font><br>").toString());

        if ((coin != null) && (coin.getCount() >= enchPrice)) {
          replyMSG.append(new StringBuilder().append("<font color=33CC00>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(enchPrice).append(" ").append(ENCHANT_COIN_NAME).append("</font><br>").toString());
          replyMSG.append(new StringBuilder().append("<button value=\"\u041F\u0435\u0440\u0435\u043D\u0435\u0441\u0442\u0438\" action=\"bypass -h npc_").append(getObjectId()).append("_voteComplete_2\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
        } else {
          replyMSG.append(new StringBuilder().append("<font color=FF6666>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(enchPrice).append(" ").append(ENCHANT_COIN_NAME).append("</font><br>").toString());
          replyMSG.append("<font color=999999>[\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C]</font>");
        }
      }
      replyMSG.append(new StringBuilder().append("<br><a action=\"bypass -h npc_").append(getObjectId()).append("_voteHome\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F.</a>").toString());
      replyMSG.append("</body></html>");
      reply.setHtml(replyMSG.toString());
      player.sendPacket(reply);
    } else {
      showVoteErr0r(player, service, 2);
      return;
    }
  }

  private void showDoVoteFinish(L2PcInstance player, int service) {
    L2ItemInstance item1 = player.getInventory().getItemByObjectId(player.getVote1Item());
    L2ItemInstance item2 = player.getInventory().getItemByObjectId(player.getVote2Item());
    if ((item1 != null) && (item2 != null)) {
      if ((!item1.canBeEnchanted()) || (!item2.canBeEnchanted())) {
        showVoteErr0r(player, service, 1);
        return;
      }

      if ((service == 1) && (!item1.isAugmented())) {
        showVoteErr0r(player, service, 1);
        return;
      }

      if ((service == 1) && (item2.isAugmented())) {
        showVoteErr0r(player, service, 1);
        return;
      }

      NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
      TextBuilder replyMSG = new TextBuilder("<html><body>");

      int coinId = 5962;
      int price = 99999;
      int enchantLevel1 = item1.getEnchantLevel();

      if (service == 1) {
        coinId = AUGMENT_COIN;
        price = AUGMENT_PRICE;
      } else {
        coinId = ENCHANT_COIN;
        price = ENCHANT_PRICE * enchantLevel1;
      }

      L2ItemInstance coin = player.getInventory().getItemByItemId(coinId);

      if ((coin == null) || (coin.getCount() < price)) {
        showVoteErr0r(player, service, 3);
        return;
      }

      if (!player.destroyItemByItemId("DS showDoVoteFinish", coinId, price, player, true)) {
        showVoteErr0r(player, service, 3);
        return;
      }

      if (service == 1) {
        L2Skill augment = item1.getAugmentation().getAugmentSkill();

        if ((augment == null) || (player.getVoteAugment() != augment)) {
          showVoteErr0r(player, service, 1);
          return;
        }

        int augId = augment.getId();
        int augLevel = augment.getLevel();
        int augEffId = item1.getAugmentation().getAugmentationId();

        String augName = augment.getName();
        String type = "";
        if (augment.isActive())
          type = "(\u0410\u043A\u0442\u0438\u0432\u043D\u044B\u0439)";
        else if (augment.isPassive())
          type = "(\u041F\u0430\u0441\u0441\u0438\u0432\u043D\u044B\u0439)";
        else {
          type = "(\u0428\u0430\u043D\u0441\u043E\u0432\u044B\u0439)";
        }

        item1.getAugmentation().removeBoni(player);
        item1.removeAugmentation();

        item2.setAugmentation(new L2Augmentation(item2, augEffId, augId, augLevel, true));

        replyMSG.append(new StringBuilder().append("<br>\u0410\u0443\u0433\u043C\u0435\u043D\u0442: <font color=bef574>").append(augName).append("").append(type).append("</font>...<br>").toString());
        replyMSG.append("<font color=33CC00>...\u043F\u0435\u0440\u0435\u043D\u0435\u0441\u0435\u043D!<br>");
      } else {
        if (player.getVoteEnchant() != enchantLevel1) {
          showVoteErr0r(player, service, 1);
          return;
        }

        item1.setEnchantLevel(0);
        item1.updateDatabase();

        item2.setEnchantLevel(enchantLevel1);
        item2.updateDatabase();

        replyMSG.append(new StringBuilder().append("<br>\u0417\u0430\u0442\u043E\u0447\u043A\u0430: <font color=bef574>+").append(enchantLevel1).append("</font><br>").toString());
        replyMSG.append("<font color=33CC00>...\u043F\u0435\u0440\u0435\u043D\u0435\u0441\u0435\u043D\u0430!<br>");
      }

      player.sendItems(false);
      player.broadcastUserInfo();

      player.setVote1Item(0);
      player.setVote2Item(0);
      player.setVoteEnchant(0);
      player.setVoteAugment(null);

      replyMSG.append(new StringBuilder().append("<br><a action=\"bypass -h npc_").append(getObjectId()).append("_voteHome\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F.</a>").toString());
      replyMSG.append("</body></html>");
      reply.setHtml(replyMSG.toString());
      player.sendPacket(reply);
    } else {
      showVoteErr0r(player, service, 0);
      return;
    }
  }

  private void showVoteErr0r(L2PcInstance player, int serviceId, int errorId)
  {
    String Service;
    String Service;
    if (serviceId == 1)
      Service = "\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u0430\u0443\u0433\u043C\u0435\u043D\u0442\u0430\u0446\u0438\u0438:";
    else {
      Service = "\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u0437\u0430\u0442\u043E\u0447\u043A\u0438:";
    }

    String Error = "\u041E\u0448\u0438\u0431\u043A\u0430!";

    switch (errorId) {
    case 0:
      Error = "\u043F\u0443\u0448\u043A\u0438 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u044B";
      break;
    case 1:
      Error = "\u043F\u0443\u0448\u043A\u0430 \u043D\u0435 \u0441\u043E\u043E\u0442\u0432\u0435\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u043F\u0430\u0440\u0430\u043C\u0435\u0442\u0440\u0430\u043C";
      break;
    case 2:
      Error = "\u043F\u0443\u0448\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430";
      break;
    case 3:
      Error = "\u043F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C";
    }

    player.setVote1Item(0);
    player.setVote2Item(0);
    player.setVoteEnchant(0);
    player.setVoteAugment(null);

    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder(new StringBuilder().append("<html><body> ").append(Service).append(" ").append(Error).append("</body></html>").toString());
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
    player.sendActionFailed();
  }

  private void clanWelcome(L2PcInstance player)
  {
    L2Clan clan = player.getClan();
    int level = clan.getLevel();
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body>");
    replyMSG.append(new StringBuilder().append("<table width=280><tr><td><font color=336699>\u041A\u043B\u0430\u043D:</font> <font color=33CCFF>").append(clan.getName()).append(" (").append(level).append(" \u0443\u0440.)</font></td><td align=right><font color=336699>\u041B\u0438\u0434\u0435\u0440:</font> <font color=33CCFF>").append(player.getName()).append("</font></td></tr></table><br><br>").toString());
    replyMSG.append("\u041F\u043E\u0432\u044B\u0448\u0435\u043D\u0438\u0435 \u0443\u0440\u043E\u0432\u043D\u044F \u043A\u043B\u0430\u043D\u0430:<br1>");
    if (level < 8) {
      replyMSG.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_clanLevel_8\" msg=\"\u041F\u043E\u043A\u0443\u043F\u043A\u0430 8 \u0443\u0440\u043E\u0432\u0435\u043D\u044C \u043A\u043B\u0430\u043Da. \u0423\u0432\u0435\u0440\u0435\u043D\u044B?\">8 \u0443\u0440\u043E\u0432\u0435\u043D\u044C.</a> (").append(CLAN_LVL8).append(" ").append(CLAN_COIN_NAME).append(")<br>").toString());
    }
    else
    {
      replyMSG.append("<font color=66CC00>\u0423\u0436\u0435 \u043C\u0430\u043A\u0441\u0438\u043C\u0430\u043B\u044C\u043D\u044B\u0439!</font><br>");
    }

    replyMSG.append("\u0414\u043E\u043F\u043E\u043B\u043D\u0438\u0442\u0435\u043B\u044C\u043D\u043E:<br1>");
    if (level >= 5) {
      replyMSG.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_clanPoints\" msg=\"\u041F\u043E\u043A\u0443\u043F\u043A\u0430 ").append(CLAN_POINTS).append(" \u043A\u043B\u0430\u043D \u043E\u0447\u043A\u043E\u0432. \u0423\u0432\u0435\u0440\u0435\u043D\u044B?\">").append(CLAN_POINTS).append(" \u043A\u043B\u0430\u043D \u043E\u0447\u043A\u043E\u0432. </a> (").append(CLAN_POINTS_PRICE).append(" ").append(CLAN_COIN_NAME).append(")<br>").toString());
      replyMSG.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_clanSkills\" msg=\"\u041F\u043E\u043A\u0443\u043F\u043A\u0430 \u0444\u0443\u043B\u043B \u043A\u043B\u0430\u043D \u0441\u043A\u0438\u043B\u043B\u043E\u0432. \u0423\u0432\u0435\u0440\u0435\u043D\u044B?\">\u0424\u0443\u043B\u043B \u043A\u043B\u0430\u043D \u0441\u043A\u0438\u043B\u043B\u044B. </a> (").append(Config.CLAN_SKILLS_PRICE).append(" ").append(CLAN_COIN_NAME).append(")<br>").toString());
    } else {
      replyMSG.append(new StringBuilder().append("<font color=999999>[").append(CLAN_POINTS).append(" \u043A\u043B\u0430\u043D \u043E\u0447\u043A\u043E\u0432]</font> (").append(CLAN_POINTS_PRICE).append(" ").append(CLAN_COIN_NAME).append(") \u0414\u043B\u044F \u043A\u043B\u0430\u043D\u043E\u0432 \u0432\u044B\u0448\u0435 5 \u0443\u0440.<br>").toString());
      replyMSG.append(new StringBuilder().append("<font color=999999>[\u0424\u0443\u043B\u043B \u043A\u043B\u0430\u043D \u0441\u043A\u0438\u043B\u043B\u044B]</font> (").append(Config.CLAN_SKILLS_PRICE).append(" ").append(CLAN_COIN_NAME).append(") \u0414\u043B\u044F \u043A\u043B\u0430\u043D\u043E\u0432 \u0432\u044B\u0448\u0435 5 \u0443\u0440.<br>").toString());
    }

    replyMSG.append("</body></html>");
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private void clanSetLevel(L2PcInstance player, int level) {
    int price = 99999;
    switch (level) {
    case 6:
      price = CLAN_LVL6;
      break;
    case 7:
      price = CLAN_LVL7;
      break;
    case 8:
      price = CLAN_LVL8;
    }

    L2ItemInstance coin = player.getInventory().getItemByItemId(CLAN_COIN);
    if ((coin == null) || (coin.getCount() < price)) {
      player.sendMessage("\u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C");
      return;
    }

    if (!player.destroyItemByItemId("DS clanSetLevel", CLAN_COIN, price, player, true)) {
      player.sendMessage("\u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C");
      return;
    }

    player.getClan().changeLevel(level);
    player.sendMessage(new StringBuilder().append("\u0423\u0440\u043E\u0432\u0435\u043D\u044C \u043A\u043B\u0430\u043D\u0430 \u0443\u0432\u0435\u043B\u0438\u0447\u0435\u043D \u0434\u043E ").append(level).toString());
    Log.addDonate(player, new StringBuilder().append("Clan Level: ").append(level).toString(), CLAN_POINTS_PRICE);
  }

  private void clanPoints(L2PcInstance player) {
    L2Clan clan = player.getClan();
    if ((clan == null) || (clan.getLevel() < 5)) {
      player.sendMessage("\u0422\u043E\u043B\u044C\u043A\u043E \u0434\u043B\u044F \u043A\u043B\u0430\u043D\u043E\u0432 \u0432\u044B\u0448\u0435 5 \u0443\u0440\u043E\u0432\u043D\u044F");
      return;
    }

    L2ItemInstance coin = player.getInventory().getItemByItemId(CLAN_COIN);
    if ((coin == null) || (coin.getCount() < CLAN_POINTS_PRICE)) {
      player.sendMessage("\u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C.");
      return;
    }

    if (!player.destroyItemByItemId("DS clanSetLevel", CLAN_COIN, CLAN_POINTS_PRICE, player, true)) {
      player.sendMessage("\u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C");
      return;
    }

    clan.addPoints(CLAN_POINTS);

    player.sendMessage(new StringBuilder().append("\u0414\u043E\u0431\u0430\u0432\u043B\u0435\u043D\u043E ").append(CLAN_POINTS).append(" \u043E\u0447\u043A\u043E\u0432, \u043F\u0435\u0440\u0435\u0437\u0430\u0439\u0434\u0438\u0442\u0435, \u0447\u0442\u043E-\u0431\u044B \u0443\u0432\u0438\u0434\u0435\u0442\u044C \u0438\u0437\u043C\u0435\u043D\u0435\u043D\u0438\u044F.").toString());
    Log.addDonate(player, new StringBuilder().append(CLAN_POINTS).append(" Clan Points").toString(), CLAN_POINTS_PRICE);
  }

  private void clanSkills(L2PcInstance player) {
    L2Clan clan = player.getClan();
    if ((clan == null) || (clan.getLevel() < 5)) {
      player.sendMessage("\u0422\u043E\u043B\u044C\u043A\u043E \u0434\u043B\u044F \u043A\u043B\u0430\u043D\u043E\u0432 \u0432\u044B\u0448\u0435 5 \u0443\u0440\u043E\u0432\u043D\u044F");
      return;
    }

    L2ItemInstance coin = player.getInventory().getItemByItemId(CLAN_COIN);
    if ((coin == null) || (coin.getCount() < Config.CLAN_SKILLS_PRICE)) {
      player.sendMessage("\u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C.");
      return;
    }

    if (!player.destroyItemByItemId("DS clanSetLevel", CLAN_COIN, Config.CLAN_SKILLS_PRICE, player, true)) {
      player.sendMessage("\u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C");
      return;
    }

    CustomServerData.getInstance().addClanSkills(player, clan);

    player.sendMessage("\u0414\u043E\u0431\u0430\u0432\u043B\u0435\u043D\u044B \u0444\u0443\u043B\u043B \u043A\u043B\u0430\u043D \u0441\u043A\u0438\u043B\u043B\u044B, \u043F\u0435\u0440\u0435\u0437\u0430\u0439\u0434\u0438\u0442\u0435, \u0447\u0442\u043E-\u0431\u044B \u0443\u0432\u0438\u0434\u0435\u0442\u044C \u0438\u0437\u043C\u0435\u043D\u0435\u043D\u0438\u044F.");
    Log.addDonate(player, new StringBuilder().append("Clan ").append(clan.getName()).append(": Full Skills").toString(), Config.CLAN_SKILLS_PRICE);
  }

  private void AugSaleWelcome(L2PcInstance player)
  {
    if (AUGSALE_TABLE.size() == 0) {
      player.sendMessage("\u0421\u0435\u0440\u0432\u0438\u0441 \u043D\u0435 \u0430\u043A\u0442\u0438\u0432\u0435\u043D");
      return;
    }

    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body>");
    replyMSG.append(new StringBuilder().append(player.getName()).append(", \u0432\u044B\u0431\u0435\u0440\u0438 \u0430\u0443\u0433\u043C\u0435\u043D\u0442\u0430\u0446\u0438\u044E:<br>").toString());
    replyMSG.append("<table width=280><tr><td>\u0410\u0443\u0433\u043C\u0435\u043D\u0442<br></td></tr>");

    FastMap.Entry e = AUGSALE_TABLE.head(); for (FastMap.Entry end = AUGSALE_TABLE.tail(); (e = e.getNext()) != end; ) {
      Integer id = (Integer)e.getKey();
      Integer lvl = (Integer)e.getValue();
      if ((id == null) || (lvl == null))
      {
        continue;
      }
      L2Skill augment = SkillTable.getInstance().getInfo(id.intValue(), 1);
      if (augment == null)
      {
        continue;
      }
      String augName = augment.getName();
      String type = "";
      if (augment.isActive())
        type = "\u0410\u043A\u0442\u0438\u0432";
      else if (augment.isPassive())
        type = "\u041F\u0430\u0441\u0441\u0438\u0432";
      else {
        type = "\u0428\u0430\u043D\u0441";
      }
      augName = augName.replace("Item Skill: ", "");

      replyMSG.append(new StringBuilder().append("<tr><td><a action=\"bypass -h npc_").append(getObjectId()).append("_augsaleShow ").append(id).append("\"><font color=bef574>").append(augName).append(" (").append(type).append(":").append(lvl).append("lvl)</font></a><br></td></tr>").toString());
    }

    replyMSG.append(new StringBuilder().append("</table><br>* \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u043B\u044E\u0431\u043E\u0433\u043E \u0430\u0443\u0433\u043C\u0435\u043D\u0442\u0430:<br1>").append(AUGSALE_PRICE).append(" ").append(AUGSALE_COIN_NAME).append("</body></html>").toString());

    player.setAugSale(0, 0);
    player.setAugSaleItem(0);

    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private void augSaleShow(L2PcInstance player, int augId)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body>");
    replyMSG.append(new StringBuilder().append(player.getName()).append(", \u043F\u043E\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0430\u0435\u0448\u044C?:<br>").toString());

    int lvl = ((Integer)AUGSALE_TABLE.get(Integer.valueOf(augId))).intValue();
    replyMSG.append(new StringBuilder().append("<table width=280><tr><td><img src=\"Icon.skill0375\" width=32 height=32></td><td>").append(getAugmentSkill(augId, lvl)).append("</td></tr></table><br>").toString());

    L2ItemInstance coin = player.getInventory().getItemByItemId(AUGSALE_COIN);
    if ((coin != null) && (coin.getCount() >= AUGSALE_PRICE)) {
      player.setAugSale(augId, lvl);
      replyMSG.append(new StringBuilder().append("<font color=33CC00>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(AUGSALE_PRICE).append(" ").append(AUGSALE_COIN_NAME).append("</font><br>").toString());
      replyMSG.append(new StringBuilder().append("<button value=\"\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_augSaleItems\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">").toString());
    } else {
      replyMSG.append(new StringBuilder().append("<font color=FF6666>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(AUGSALE_PRICE).append(" ").append(AUGSALE_COIN_NAME).append("</font><br>").toString());
      replyMSG.append("<font color=999999>[\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C]</font>");
    }
    replyMSG.append(new StringBuilder().append("<br><br><a action=\"bypass -h npc_").append(getObjectId()).append("_Augsale\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br></body></html>").toString());

    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private void AugSaleItems(L2PcInstance player)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder(new StringBuilder().append("<html><body>").append(getAugmentSkill(player.getAugSaleId(), player.getAugSaleLvl())).append("<br>").toString());
    replyMSG.append("\u0412\u044B\u0431\u043E\u0440 \u0448\u043C\u043E\u0442\u043A\u0438:<br>\u041A\u0443\u0434\u0430 \u0432\u0442\u044B\u043A\u0430\u0435\u043C?<br><br><table width=300>");

    int objectId = 0;
    String itemName = "";
    int enchantLevel = 0;
    String itemIcon = "";
    int itemType = 0;
    for (L2ItemInstance item : player.getInventory().getItems()) {
      if (item == null)
      {
        continue;
      }
      if (!item.canBeEnchanted())
      {
        continue;
      }
      objectId = item.getObjectId();
      itemName = item.getItem().getName();
      enchantLevel = item.getEnchantLevel();
      itemIcon = item.getItem().getIcon();
      itemType = item.getItem().getType2();

      if ((item.canBeAugmented()) && (!item.isAugmented()) && (!item.isWear())) {
        replyMSG.append(new StringBuilder().append("<tr><td><img src=\"").append(itemIcon).append("\" width=32 height=32></td><td><a action=\"bypass -h npc_").append(getObjectId()).append("_augsItem ").append(objectId).append("\">").append(itemName).append(" (+").append(enchantLevel).append(")</a></td></tr>").toString());
      }
    }

    replyMSG.append("</table>");
    replyMSG.append(new StringBuilder().append("<br><br><a action=\"bypass -h npc_").append(getObjectId()).append("_Augsale\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br></body></html>").toString());
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private void AugsItem(L2PcInstance player, int objectId)
  {
    L2ItemInstance item = player.getInventory().getItemByObjectId(objectId);
    if (item != null) {
      if (!item.canBeEnchanted()) {
        showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
        return;
      }

      NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
      TextBuilder replyMSG = new TextBuilder(new StringBuilder().append("<html><body>").append(getAugmentSkill(player.getAugSaleId(), player.getAugSaleLvl())).append("<br>").toString());

      String itemName = item.getItem().getName();
      int enchantLevel = item.getEnchantLevel();
      String itemIcon = item.getItem().getIcon();

      replyMSG.append("\u041F\u043E\u043A\u0443\u043F\u043A\u0430 \u0430\u0443\u0433\u043C\u0435\u043D\u0442\u0430\u0446\u0438\u0438:<br>\u041F\u043E\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0430\u0435\u0442\u0435 \u0448\u043C\u043E\u0442\u043A\u0443?<br>");

      replyMSG.append(new StringBuilder().append("<table width=300><tr><td><img src=\"").append(itemIcon).append("\" width=32 height=32></td><td><font color=LEVEL>").append(itemName).append(" (+").append(enchantLevel).append(")</font>g<br></td></tr></table><br><br>").toString());
      replyMSG.append(new StringBuilder().append("<br>\u0417\u0430\u0442\u043E\u0447\u043A\u0430: <font color=bef574>+").append(enchantLevel).append("</font><br>").toString());

      replyMSG.append(new StringBuilder().append("<button value=\"\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_AugsaleFinish\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
      player.setAugSaleItem(objectId);

      replyMSG.append(new StringBuilder().append("<br><br><a action=\"bypass -h npc_").append(getObjectId()).append("_Augsale\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br></body></html>").toString());
      reply.setHtml(replyMSG.toString());
      player.sendPacket(reply);
    } else {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }
  }

  private void AugsaleFinish(L2PcInstance player)
  {
    L2ItemInstance targetItem = player.getInventory().getItemByObjectId(player.getAugSaleItem());
    if (targetItem != null) {
      if (targetItem.isAugmented()) {
        showVoteErr0r(player, 1, 1);
        return;
      }

      NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
      TextBuilder replyMSG = new TextBuilder("<html><body>");

      L2ItemInstance coin = player.getInventory().getItemByItemId(AUGSALE_COIN);
      if ((coin == null) || (coin.getCount() < AUGSALE_PRICE)) {
        showVoteErr0r(player, 1, 3);
        return;
      }

      if (!player.destroyItemByItemId("Ls bue", AUGSALE_COIN, AUGSALE_PRICE, player, true)) {
        showVoteErr0r(player, 1, 3);
        return;
      }
      int augId = player.getAugSaleId();
      int augLevel = player.getAugSaleLvl();

      L2Skill augment = SkillTable.getInstance().getInfo(augId, 1);
      if (augment == null) {
        showVoteErr0r(player, 1, 0);
        return;
      }

      int type = 0;
      if (augment.isActive())
        type = 2;
      else if (augment.isPassive())
        type = 3;
      else {
        type = 1;
      }

      targetItem.setAugmentation(AugmentationData.getInstance().generateAugmentation(targetItem, augId, augLevel, type));

      replyMSG.append(new StringBuilder().append("").append(getAugmentSkill(augId, augLevel)).append("<br>").toString());
      replyMSG.append("<font color=33CC00>...\u043A\u0443\u043F\u043B\u0435\u043D!<br>");

      player.sendItems(false);
      player.broadcastUserInfo();

      player.setAugSale(0, 0);
      player.setAugSaleItem(0);

      replyMSG.append("</body></html>");
      reply.setHtml(replyMSG.toString());
      player.sendPacket(reply);

      Log.addDonate(player, new StringBuilder().append("LS ").append(augId).append(":").append(augLevel).toString(), AUGSALE_PRICE);
    } else {
      showVoteErr0r(player, 1, 0);
      return;
    }
  }

  private void itemsChina(L2PcInstance player)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body>\u041A\u0438\u0442\u0430\u0439\u0441\u043A\u0438\u0439 \u043C\u0430\u0433\u0430\u0437\u0438\u043D<br>");
    replyMSG.append("\u0422\u043E\u0432\u0430\u0440\u044B:<br>");
    replyMSG.append("<table width=260><tr><td></td><td></td></tr>");

    CustomServerData.ChinaItem ds = null;
    FastMap chinaShop = CustomServerData.getInstance().getChinaShop();
    FastMap.Entry e = chinaShop.head(); for (FastMap.Entry end = chinaShop.tail(); (e = e.getNext()) != end; ) {
      Integer id = (Integer)e.getKey();
      ds = (CustomServerData.ChinaItem)e.getValue();

      if (ds == null)
      {
        continue;
      }
      L2Item china = ItemTable.getInstance().getTemplate(id.intValue());
      if (china == null)
      {
        continue;
      }
      replyMSG.append(new StringBuilder().append("<tr><td><img src=\"").append(china.getIcon()).append("\" width=32 height=32></td><td><a action=\"bypass -h npc_").append(getObjectId()).append("_chinaShow ").append(id).append("\"><font color=99FF66>").append(ds.name).append("</font></a><br1><font color=336633>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(ds.price).append(" CoL</font><br></td></tr>").toString());
    }

    replyMSG.append("</table><br><br>");

    replyMSG.append("<br><br></body></html>");
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private void chinaShow(L2PcInstance player, int itemId) {
    L2Item china = ItemTable.getInstance().getTemplate(itemId);
    if (china == null) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    CustomServerData.ChinaItem ds = CustomServerData.getInstance().getChinaItem(itemId);
    if (ds == null) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder replyMSG = new TextBuilder("<html><body>KuT\u0430u\u0441kuu M\u0430r\u04303uH*<br>");

    replyMSG.append(new StringBuilder().append("<table width=250><tr><td align=right><img src=\"").append(china.getIcon()).append("\" width=32 height=32></td><td><font color=33FFFF>").append(ds.name).append("</font></td></tr></table><br><br>").toString());
    replyMSG.append(new StringBuilder().append("<font color=336699>\u0418\u043D\u0444\u043E: </font><font color=3399CC> ").append(ds.info).append("</font><br>").toString());
    replyMSG.append(new StringBuilder().append("<font color=336699>\u0414\u043B\u0438\u0442\u0435\u043B\u044C\u043D\u043E\u0441\u0442\u044C: </font><font color=3399CC> ").append(ds.days).append(" \u0447\u0430\u0441\u043E\u0432</font><br>").toString());
    L2ItemInstance coin = player.getInventory().getItemByItemId(ds.coin);
    if ((coin != null) && (coin.getCount() >= ds.price)) {
      replyMSG.append(new StringBuilder().append("<font color=33CC00>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C:<br1> ").append(ds.price).append(" Coin Of Luck</font><br>").toString());
      replyMSG.append(new StringBuilder().append("<button value=\"\u041A\u0443\u043F\u0438\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_bueChina ").append(itemId).append("\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">").toString());
    } else {
      replyMSG.append(new StringBuilder().append("<font color=FF6666>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C:<br1> ").append(ds.price).append(" Coin Of Luck</font><br>").toString());
      replyMSG.append("<font color=999999>[\u041A\u0443\u043F\u0438\u0442\u044C]</font>");
    }
    replyMSG.append(new StringBuilder().append("<br><font color=CC33CC>*\u0427\u0435\u0440\u0435\u0437 ").append(ds.days).append(" \u0447\u0430\u0441\u043E\u0432 \u0448\u043C\u043E\u0442\u043A\u0430 \u0441\u043B\u043E\u043C\u0430\u0435\u0442\u0441\u044F!<br1>\u041E\u0442\u0441\u0447\u0435\u0442 \u043D\u0430\u0447\u0438\u043D\u0430\u0435\u0442\u0441\u044F \u043F\u0440\u0438 \u043D\u0430\u0434\u0435\u0432\u0430\u043D\u0438\u0438 \u0448\u043C\u043E\u0442\u043A\u0438.<br1>\u041F\u0440\u043E\u0441\u0442\u043E \u0442\u0430\u043A \u0432\u0430\u043B\u044F\u044F\u0441\u044C \u0432 \u0438\u043D\u0432\u0435\u043D\u0442\u0430\u0440\u0435 \u043D\u0438\u0447\u0435\u0433\u043E \u043D\u0435 \u0441\u043B\u043E\u043C\u0430\u0435\u0442\u0441\u044F.<br1>Made in China.</font><br></body></html>").toString());
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
  }

  private void bueChina(L2PcInstance player, int itemId) {
    L2Item china = ItemTable.getInstance().getTemplate(itemId);
    if (china == null) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    CustomServerData.ChinaItem ds = CustomServerData.getInstance().getChinaItem(itemId);
    if (ds == null) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    L2ItemInstance coins = player.getInventory().getItemByItemId(ds.coin);
    if ((coins == null) || (coins.getCount() < ds.price)) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }
    player.destroyItemByItemId("China", ds.coin, ds.price, player, true);

    L2ItemInstance item = ItemTable.getInstance().createItem("China", itemId, 1, player, null);
    item.setMana((int)TimeUnit.HOURS.toMinutes(ds.days));
    player.getInventory().addItem("Enchantt", item, player, null);

    player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(item.getItemId()));
    player.sendItems(true);
    player.sendChanges();
    player.broadcastUserInfo();
  }

  private void bueSOB(L2PcInstance player, int period)
  {
    int price = 99999;
    switch (period) {
    case 1:
      price = SOB_PRICE_ONE;
      break;
    case 2:
      price = SOB_PRICE_TWO;
    }

    L2ItemInstance coins = player.getInventory().getItemByItemId(SOB_COIN);
    if ((coins == null) || (coins.getCount() < price)) {
      showError(player, new StringBuilder().append("\u041D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E ").append(SOB_COIN_NAME).append(".").toString());
      return;
    }

    int skillId = Config.SOB_ID;
    if ((skillId == 0) || (player.getClassId().getId() < 88)) {
      showError(player, "\u0422\u043E\u043B\u044C\u043A\u043E \u0434\u043B\u044F 3\u0439 \u043F\u0440\u043E\u0444\u044B.");
      return;
    }

    if (skillId == 1) {
      switch (player.getClassId().getId()) {
      case 88:
      case 89:
      case 92:
      case 93:
      case 101:
      case 102:
      case 108:
      case 109:
      case 113:
      case 114:
        skillId = 7077;
        break;
      case 94:
      case 95:
      case 103:
      case 110:
        skillId = 7078;
        break;
      case 96:
      case 97:
      case 98:
      case 100:
      case 104:
      case 105:
      case 107:
      case 111:
      case 112:
      case 115:
      case 116:
        skillId = 7079;
        break;
      case 90:
      case 91:
      case 99:
      case 106:
      case 117:
      case 118:
        skillId = 7080;
      }

    }

    if (player.getKnownSkill(skillId) != null) {
      showError(player, "\u0423 \u0432\u0430\u0441 \u0443\u0436\u0435 \u0435\u0441\u0442\u044C Skill OF Balance.");
      return;
    }

    player.destroyItemByItemId("SOB", SOB_COIN, price, player, true);

    Log.addDonate(player, new StringBuilder().append("SoB [").append(period).append("]").toString(), price);

    long expire = TimeUnit.DAYS.toMillis(15L);
    if (period == 2) {
      expire *= 2L;
    }

    player.addDonateSkill(player.getClassId().getId(), skillId, period, System.currentTimeMillis() + expire);

    player.sendMessage("\u041A\u0443\u043F\u043B\u0435\u043D Skill Of Balance, \u043F\u0435\u0440\u0435\u0437\u0430\u0439\u0434\u0438\u0442\u0435!");
  }

  private void donateShop(L2PcInstance player)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder htm = new TextBuilder();
    htm.append("<html><body><table width=260><tr><td><font color=LEVEL>\u0423\u043D\u0438\u043A\u0430\u043B\u044C\u043D\u044B\u0439 \u043C\u0430\u0433\u0430\u0437\u0438\u043D:</font></td></tr></table><br>");
    htm.append("<table width=260><tr><td></td><td></td></tr>");

    L2Item item = null;
    String count = "";
    CustomServerData.DonateItem di = null;
    ItemTable it = ItemTable.getInstance();
    FastTable donShop = CustomServerData.getInstance().getDonateShop();
    int i = 0; for (int n = donShop.size(); i < n; i++) {
      di = (CustomServerData.DonateItem)donShop.get(i);
      if (di == null)
      {
        continue;
      }
      item = it.getTemplate(di.itemId);
      if (item == null)
      {
        continue;
      }
      if (di.itemCount > 1) {
        count = new StringBuilder().append("(").append(di.itemCount).append(")").toString();
      }

      htm.append(new StringBuilder().append("<tr><td><img src=").append(item.getIcon()).append(" width=32 height=32></td><td><a action=\"bypass -h npc_").append(getObjectId()).append("_dShopShow ").append(i).append("\"><font color=99FF66>").append(item.getName()).append("").append(count).append("</font></a><br1><font color=336633>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(di.priceCount).append(" ").append(di.priceName).append("</font></td></tr>").toString());
      htm.append("<tr><td><br></td><td></td></tr>");
      count = "";
    }
    htm.append("</table><br><br>");
    htm.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></body></html>");
    reply.setHtml(htm.toString());
    player.sendPacket(reply);
  }

  private void donateShopShow(L2PcInstance player, int saleId) {
    CustomServerData.DonateItem di = CustomServerData.getInstance().getDonateItem(saleId);
    if (di == null) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    L2Item item = ItemTable.getInstance().getTemplate(di.itemId);
    if (item == null) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    String count = "";
    if (di.itemCount > 1) {
      count = new StringBuilder().append("(").append(di.itemCount).append(")").toString();
    }

    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder htm = new TextBuilder();

    htm.append("<html><body><font color=LEVEL>\u0423\u043D\u0438\u043A\u0430\u043B\u044C\u043D\u044B\u0439 \u043C\u0430\u0433\u0430\u0437\u0438\u043D:<br>\u041F\u0440\u043E\u0441\u043C\u043E\u0442\u0440 \u0442\u043E\u0432\u0430\u0440\u0430:</font><br1>");
    htm.append(new StringBuilder().append("<table width=250><tr><td align=right><img src=").append(item.getIcon()).append(" width=32 height=32></td><td><font color=33FFFF>").append(item.getName()).append("").append(count).append("</font></td></tr></table><br><br>").toString());
    htm.append(new StringBuilder().append("<font color=336699>\u0414\u043E\u043F\u043E\u043B\u043D\u0438\u0442\u0435\u043B\u044C\u043D\u0430\u044F \u0438\u043D\u0444\u043E\u0440\u043C\u0430\u0446\u0438\u044F:</font><br1><font color=3399CC>").append(di.itemInfoRu).append("</font><br>").toString());
    htm.append("<font color=336699>\u041E\u043F\u0438\u0441\u0430\u043D\u0438\u0435:</font><br1>");
    htm.append(new StringBuilder().append("<font color=3399CC>").append(di.itemInfoDesc).append("</font><br>").toString());
    htm.append("<font color=336699>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C:</font><br1>");
    htm.append(new StringBuilder().append("<font color=3399CC>").append(di.priceCount).append(" ").append(di.priceName).append("</font><br><br>").toString());

    L2ItemInstance coins = player.getInventory().getItemByItemId(di.priceId);
    if ((coins == null) || (coins.getCount() < di.priceCount))
      htm.append("<font color=666699>[\u041A\u0443\u043F\u0438\u0442\u044C]</font><br><br>");
    else {
      htm.append(new StringBuilder().append("<button value=\"\u041A\u0443\u043F\u0438\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_dShopBue ").append(saleId).append("\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\" msg=\"\u041F\u043E\u043A\u0443\u043F\u0430\u0435\u043C ").append(item.getName()).append("").append(count).append(" \u0437\u0430 ").append(di.priceCount).append(" ").append(di.priceName).append("?\"><br><br>").toString());
    }

    htm.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_donateShop\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F.</a></body></html>").toString());
    reply.setHtml(htm.toString());
    player.sendPacket(reply);
  }

  private void donateShopBue(L2PcInstance player, int saleId) {
    CustomServerData.DonateItem di = CustomServerData.getInstance().getDonateItem(saleId);
    if (di == null) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    L2Item item = ItemTable.getInstance().getTemplate(di.itemId);
    if (item == null) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    L2ItemInstance coins = player.getInventory().getItemByItemId(di.priceId);
    if ((coins == null) || (coins.getCount() < di.priceCount)) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }
    player.destroyItemByItemId("Donate Shop", di.priceId, di.priceCount, player, true);
    String count = "";
    if (di.itemCount > 1) {
      count = new StringBuilder().append("(").append(di.itemCount).append(")").toString();
    }
    Log.addDonate(player, new StringBuilder().append("Donate Shop: ").append(item.getName()).append("").append(count).append("").toString(), di.priceCount);

    player.addItem("DonateShop", di.itemId, di.itemCount, player, true);
  }

  private void donateSkillShop(L2PcInstance player)
  {
    if (_donateSkills.get(Integer.valueOf(player.getClassId().getId())) == null) {
      showError(player, "\u0414\u043B\u044F \u0432\u0430\u0448\u0435\u0433\u043E \u043A\u043B\u0430\u0441\u0441\u0430 \u043D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u043D\u044B\u0445 \u0441\u043A\u0438\u043B\u043B\u043E\u0432.");
      return;
    }

    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder htm = new TextBuilder();
    htm.append("<html><body><table width=260><tr><td><font color=LEVEL>\u0423\u043D\u0438\u043A\u0430\u043B\u044C\u043D\u044B\u0439 \u043C\u0430\u0433\u0430\u0437\u0438\u043D \u0441\u043A\u0438\u043B\u043B\u043E\u0432:</font></td></tr></table><br>");
    htm.append("<table width=260><tr><td></td><td></td></tr>");

    CustomServerData.DonateSkill di = null;
    SkillTable st = SkillTable.getInstance();
    FastMap.Entry e = _donateSkills.head(); for (FastMap.Entry end = _donateSkills.tail(); (e = e.getNext()) != end; ) {
      Integer classId = (Integer)e.getKey();
      FastTable skills = (FastTable)e.getValue();
      if ((classId == null) || (skills == null) || 
        (skills.isEmpty()))
      {
        continue;
      }
      int i = 0; for (int n = skills.size(); i < n; i++) {
        di = (CustomServerData.DonateSkill)skills.get(i);
        if (di == null)
        {
          continue;
        }
        L2Skill skill = st.getInfo(di.id, di.lvl);
        if (skill == null)
        {
          continue;
        }
        if (player.getKnownSkill(di.id) != null)
        {
          continue;
        }
        htm.append(new StringBuilder().append("<tr><td><img src=").append(di.icon).append(" width=32 height=32></td><td><a action=\"bypass -h npc_").append(getObjectId()).append("_dsShopShow ").append(i).append("\"><font color=99FF66>").append(skill.getName()).append(" (").append(di.lvl).append(" \u0443\u0440.)</font></a><br1><font color=336633>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(di.priceCount).append(" ").append(di.priceName).append("</font></td></tr>").toString());
        htm.append("<tr><td><br></td><td></td></tr>");
      }
    }

    htm.append("</table><br><br>");
    htm.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></body></html>");
    reply.setHtml(htm.toString());
    player.sendPacket(reply);
  }

  private void donateSkillShopShow(L2PcInstance player, int saleId) {
    if (_donateSkills.get(Integer.valueOf(player.getClassId().getId())) == null) {
      showError(player, "\u0414\u043B\u044F \u0432\u0430\u0448\u0435\u0433\u043E \u043A\u043B\u0430\u0441\u0441\u0430 \u043D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u043D\u044B\u0445 \u0441\u043A\u0438\u043B\u043B\u043E\u0432.");
      return;
    }

    CustomServerData.DonateSkill di = (CustomServerData.DonateSkill)((FastTable)_donateSkills.get(Integer.valueOf(player.getClassId().getId()))).get(saleId);
    if (di == null) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    L2Skill skill = SkillTable.getInstance().getInfo(di.id, di.lvl);
    if (skill == null) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder htm = new TextBuilder();

    htm.append("<html><body><font color=LEVEL>\u0423\u043D\u0438\u043A\u0430\u043B\u044C\u043D\u044B\u0439 \u043C\u0430\u0433\u0430\u0437\u0438\u043D \u0441\u043A\u0438\u043B\u043B\u043E\u0432:<br>\u041F\u0440\u043E\u0441\u043C\u043E\u0442\u0440 \u0441\u043A\u0438\u043B\u043B\u0430:</font><br1>");
    htm.append(new StringBuilder().append("<table width=250><tr><td align=right><img src=").append(di.icon).append(" width=32 height=32></td><td><font color=33FFFF>").append(skill.getName()).append(" (").append(di.lvl).append(" \u0443\u0440.)</font></td></tr></table><br><br>").toString());
    htm.append("<font color=336699>\u041E\u043F\u0438\u0441\u0430\u043D\u0438\u0435:</font><br1>");
    htm.append(new StringBuilder().append("<font color=3399CC>").append(di.info).append("</font><br>").toString());

    htm.append("<font color=336699>\u0414\u043B\u0438\u0442\u0435\u043B\u044C\u043D\u043E\u0441\u0442\u044C:</font><br1>");
    if (di.expire < 0L)
      htm.append("<font color=3399CC>\u0411\u0435\u0441\u043A\u043E\u043D\u0435\u0447\u043D\u044B\u0439.</font><br>");
    else if (di.expire == 0L)
      htm.append("<font color=3399CC>\u0414\u043E \u043A\u043E\u043D\u0446\u0430 \u043C\u0435\u0441\u044F\u0446\u0430.</font><br>");
    else {
      htm.append(new StringBuilder().append("<font color=3399CC>").append(di.expire).append(" \u0434\u043D\u0435\u0439.</font><br>").toString());
    }

    htm.append("<font color=336699>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C:</font><br1>");
    htm.append(new StringBuilder().append("<font color=3399CC>").append(di.priceCount).append(" ").append(di.priceName).append("</font><br><br>").toString());

    L2ItemInstance coins = player.getInventory().getItemByItemId(di.priceId);
    if ((coins == null) || (coins.getCount() < di.priceCount))
      htm.append("<font color=666699>[\u041A\u0443\u043F\u0438\u0442\u044C]</font><br><br>");
    else {
      htm.append(new StringBuilder().append("<button value=\"\u041A\u0443\u043F\u0438\u0442\u044C\" action=\"bypass -h npc_").append(getObjectId()).append("_dsShopBue ").append(saleId).append("\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\" msg=\"\u041F\u043E\u043A\u0443\u043F\u0430\u0435\u043C ").append(skill.getName()).append(" (").append(di.lvl).append(" \u0443\u0440.) \u0437\u0430 ").append(di.priceCount).append(" ").append(di.priceName).append("?\"><br><br>").toString());
    }

    htm.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_donateSkillsShop\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F.</a></body></html>").toString());
    reply.setHtml(htm.toString());
    player.sendPacket(reply);
  }

  private void donateSkillShopBue(L2PcInstance player, int saleId) {
    if (_donateSkills.get(Integer.valueOf(player.getClassId().getId())) == null) {
      showError(player, "\u0414\u043B\u044F \u0432\u0430\u0448\u0435\u0433\u043E \u043A\u043B\u0430\u0441\u0441\u0430 \u043D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u043D\u044B\u0445 \u0441\u043A\u0438\u043B\u043B\u043E\u0432.");
      return;
    }

    CustomServerData.DonateSkill di = (CustomServerData.DonateSkill)((FastTable)_donateSkills.get(Integer.valueOf(player.getClassId().getId()))).get(saleId);
    if (di == null) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    L2Skill skill = SkillTable.getInstance().getInfo(di.id, di.lvl);
    if (skill == null) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }

    L2ItemInstance coins = player.getInventory().getItemByItemId(di.priceId);
    if ((coins == null) || (coins.getCount() < di.priceCount)) {
      showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      return;
    }
    player.destroyItemByItemId("Donate Skill Shop", di.priceId, di.priceCount, player, true);
    Log.addDonate(player, new StringBuilder().append("Donate Skill Shop: ").append(skill.getName()).append("(").append(di.lvl).append(" lvl)").toString(), di.priceCount);

    long expire = 0L;
    if (di.expire < 0L) {
      expire = -1L;
    } else if (di.expire == 0L) {
      Calendar calendar = Calendar.getInstance();
      int lastDate = calendar.getActualMaximum(5);
      calendar.set(5, lastDate);

      expire = calendar.getTimeInMillis();
    } else {
      expire = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(di.expire);
    }
    player.addSkill(skill, false);
    player.addDonateSkill(player.getClassId().getId(), di.id, di.lvl, expire);
    player.sendHtmlMessage("\u0423\u043D\u0438\u043A\u0430\u043B\u044C\u043D\u044B\u0439 \u043C\u0430\u0433\u0430\u0437\u0438\u043D \u0441\u043A\u0438\u043B\u043B\u043E\u0432:", new StringBuilder().append("\u0412\u044B \u043F\u0440\u0438\u043E\u0431\u0440\u0435\u043B\u0438 \u0441\u043A\u0438\u043B\u043B: <br> <font color=33FFFF>").append(skill.getName()).append(" (").append(di.lvl).append(" \u0443\u0440.)</font>").toString());
  }

  private void addPremium(L2PcInstance player, int days)
  {
    Integer price = (Integer)PREMIUM_DAY_PRICES.get(Integer.valueOf(days));
    if (price == null) {
      price = Integer.valueOf(Config.PREMIUM_PRICE * days);
    }

    L2ItemInstance coins = player.getInventory().getItemByItemId(Config.PREMIUM_COIN);
    if ((coins == null) || (coins.getCount() < price.intValue())) {
      showError(player, new StringBuilder().append("\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u043F\u0440\u0435\u043C\u0438\u0443\u043C ").append(price).append(" ").append(Config.PREMIUM_COINNAME).append(".").toString());
      return;
    }
    player.destroyItemByItemId("Donate Shop", Config.PREMIUM_COIN, price.intValue(), player, true);

    long expire = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days);
    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("UPDATE `characters` SET `premium`=? WHERE `obj_Id`=?");
      st.setLong(1, expire);
      st.setInt(2, player.getObjectId());
      st.execute();
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("addPremium(L2PcInstance player, int days) error: ").append(e).toString());
    } finally {
      Close.CS(con, st);
    }
    player.setPremium(true);
    player.broadcastUserInfo();
    String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(expire));
    player.sendCritMessage(new StringBuilder().append("\u0421\u0442\u0430\u0442\u0443\u0441 \u041F\u0440\u0435\u043C\u0438\u0443\u043C: \u0434\u043E ").append(date).toString());
    Log.addDonate(player, new StringBuilder().append("Premium, ").append(days).append(" days.").toString(), Config.PREMIUM_PRICE);
  }

  private void addNoble(L2PcInstance player) {
    if (player.isNoble()) {
      showError(player, "\u0412\u044B \u0443\u0436\u0435 \u043D\u043E\u0431\u043B\u0435\u0441\u0441.");
      return;
    }

    L2ItemInstance coins = player.getInventory().getItemByItemId(Config.SNOBLE_COIN);
    if ((coins == null) || (coins.getCount() < Config.SNOBLE_PRICE)) {
      showError(player, new StringBuilder().append("\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u043D\u043E\u0431\u043B\u0435\u0441\u0441\u0430 ").append(Config.SNOBLE_PRICE).append(" ").append(Config.SNOBLE_COIN_NAME).append(".").toString());
      return;
    }
    player.destroyItemByItemId("Donate Shop", Config.SNOBLE_COIN, Config.SNOBLE_PRICE, player, true);

    player.setNoble(true);
    player.addItem("rewardNoble", 7694, 1, this, true);
    player.sendUserPacket(new PlaySound("ItemSound.quest_finish"));

    if (!Config.ACADEMY_CLASSIC) {
      player.rewardAcademy(0);
    }
    Log.addDonate(player, "Noblesse.", Config.SNOBLE_PRICE);
  }

  private void statHome(L2PcInstance player)
  {
    TextBuilder htm = new TextBuilder();
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());

    htm.append("<html><body><center><br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>");
    htm.append("<table width=290> <tr><td><font color=0099CC>\u041B\u0438\u0434\u0435\u0440\u044B \u0441\u0435\u0440\u0432\u0435\u0440\u0430</font></td>");
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statPvp 1\">Top PvP</a></font></td>").toString());
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statPk 1\">Top Pk</a></font></td>").toString());
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statClans 1\">\u041A\u043B\u0430\u043D\u044B</a></font></td>").toString());
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statCastles\">\u0417\u0430\u043C\u043A\u0438</a></font></td>").toString());
    htm.append("</tr></table><br><img src=\"sek.cbui175\" width=150 height=3><br><table width=310><tr>");
    htm.append("<td align=center><font color=0099CC>Top 10 PvP</font></td><td align=center><font color=0099CC>Top 10 Pk</font></td>");
    htm.append("</tr><tr><td valign=top>");
    htm.append(CustomServerData.getInstance().getStatHome());
    htm.append("</td></tr></table><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center><a action=\"bypass -h npc_%objectId%_Chat 0\"><font color=666633>\u041D\u0430\u0437\u0430\u0434</font></a></body></html>");

    reply.setHtml(htm.toString());
    player.sendPacket(reply);

    htm.clear();
    htm = null;
  }

  private void statShowPvp(L2PcInstance player, int page) {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder htm = new TextBuilder();

    htm.append("<html><body><center><br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>");
    htm.append(new StringBuilder().append("<table width=290> <tr><td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statHome\">\u041B\u0438\u0434\u0435\u0440\u044B \u0441\u0435\u0440\u0432\u0435\u0440\u0430</a></font></td>").toString());
    htm.append("<td><font color=0099CC>Top Pvp</font></td>");
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statPk 1\">Top Pk</a></font></td>").toString());
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statClans 1\">\u041A\u043B\u0430\u043D\u044B</a></font></td>").toString());
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statCastles\">\u0417\u0430\u043C\u043A\u0438</a></font></td>").toString());
    htm.append("</tr></table><br><img src=\"sek.cbui175\" width=150 height=3><br>");
    htm.append("<font color=999966><table width=310><tr><td>#</td><td>\u041D\u0438\u043A</td><td>\u041A\u043B\u0430\u043D</td><td>\u041E\u043D\u043B\u0430\u0439\u043D</td><td>Pvp</td></tr>");

    FastTable pvp = CustomServerData.getInstance().getStatPvp();
    L2World world = L2World.getInstance();

    int count = 0;
    String online = "<font color=006600>Online</font>";
    int start = (page - 1) * _statLimit;
    int stop = start + _statLimit;
    if (stop > pvp.size()) {
      stop = pvp.size() - 1;
    }
    int pages = pvp.size() / _statLimit + 1;
    int i = start; for (int n = stop; i < n; i++) {
      CustomServerData.StatPlayer pc = (CustomServerData.StatPlayer)pvp.get(i);
      if (pc == null)
      {
        continue;
      }
      if (world.getPlayer(pc.id) == null) {
        online = "<font color=330033>Offline</font>";
      }

      htm.append(new StringBuilder().append("<tr><td>").append(i + 1).append("</td><td><font color=CCCC33>").append(pc.name).append("</td><td>").append(pc.clan).append("</font></td><td><font color=006600>").append(online).append("</font></td><td><font color=CCCC33>").append(pc.kills).append("</font></td></tr>").toString());
      count++;
    }

    htm.append("</table></font>");
    if (pages > 2) {
      htm.append(sortPvp(page, pages));
    }

    htm.append("</center></body></html>");

    reply.setHtml(htm.toString());
    player.sendPacket(reply);
    htm.clear();
    htm = null;
  }

  private String sortPvp(int page, int pages) {
    TextBuilder text = new TextBuilder("<br>\u0421\u0442\u0440\u0430\u043D\u0438\u0446\u044B:<br1><table width=300><tr>");
    int step = 1;
    int s = page - 3;
    int f = page + 3;
    if ((page < _statSortLimit) && (s < _statSortLimit)) {
      s = 1;
    }
    if (page >= _statSortLimit) {
      text.append(new StringBuilder().append("<td><a action=\"bypass -h npc_").append(getObjectId()).append("_statPvp ").append(s).append("\"> ... </a></td>").toString());
    }

    for (int i = s; i < pages + 1; i++) {
      int al = i + 1;
      if (i == page) {
        text.append(new StringBuilder().append("<td>").append(i).append("</td>").toString());
      }
      else if (al <= pages) {
        text.append(new StringBuilder().append("<td><a action=\"bypass -h npc_").append(getObjectId()).append("_statPvp ").append(i).append("\">").append(i).append("</a></td>").toString());
      }

      if ((step == _statSortLimit) && (f < pages)) {
        if (al >= pages) break;
        text.append(new StringBuilder().append("<td><a action=\"bypass -h npc_").append(getObjectId()).append("_statPvp ").append(al).append("\"> ... </a></td>").toString()); break;
      }

      step++;
    }
    text.append("</tr></table><br>");
    String htmltext = text.toString();
    text.clear();
    text = null;
    return htmltext;
  }

  private void statShowPk(L2PcInstance player, int page)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder htm = new TextBuilder();

    htm.append("<html><body><center><br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>");
    htm.append(new StringBuilder().append("<table width=290> <tr><td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statHome\">\u041B\u0438\u0434\u0435\u0440\u044B \u0441\u0435\u0440\u0432\u0435\u0440\u0430</a></font></td>").toString());
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statPvp 1\">Top Pvp</a></font></td>").toString());
    htm.append("<td><font color=0099CC>Top Pk</font></td>");
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statClans 1\">\u041A\u043B\u0430\u043D\u044B</a></font></td>").toString());
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statCastles\">\u0417\u0430\u043C\u043A\u0438</a></font></td>").toString());
    htm.append("</tr></table><br><img src=\"sek.cbui175\" width=150 height=3><br>");
    htm.append("<font color=999966><table width=310><tr><td>#</td><td>\u041D\u0438\u043A</td><td>\u041A\u043B\u0430\u043D</td><td>\u041E\u043D\u043B\u0430\u0439\u043D</td><td>Pvp</td></tr>");

    FastTable pk = CustomServerData.getInstance().getStatPk();
    L2World world = L2World.getInstance();

    int count = 0;
    String online = "<font color=006600>Online</font>";
    int start = (page - 1) * _statLimit;
    int stop = start + _statLimit;
    if (stop > pk.size()) {
      stop = pk.size() - 1;
    }
    int pages = pk.size() / _statLimit + 1;
    int i = start; for (int n = stop; i < n; i++) {
      CustomServerData.StatPlayer pc = (CustomServerData.StatPlayer)pk.get(i);
      if (pc == null)
      {
        continue;
      }
      if (world.getPlayer(pc.id) == null) {
        online = "<font color=330033>Offline</font>";
      }

      htm.append(new StringBuilder().append("<tr><td>").append(i + 1).append("</td><td><font color=CCCC33>").append(pc.name).append("</td><td>").append(pc.clan).append("</font></td><td><font color=006600>").append(online).append("</font></td><td><font color=CCCC33>").append(pc.kills).append("</font></td></tr>").toString());
      count++;
    }

    htm.append("</table></font>");
    if (pages > 2) {
      htm.append(sortPk(page, pages));
    }

    htm.append("</body></html>");

    reply.setHtml(htm.toString());
    player.sendPacket(reply);
    htm.clear();
    htm = null;
  }

  private String sortPk(int page, int pages) {
    TextBuilder text = new TextBuilder("<br>\u0421\u0442\u0440\u0430\u043D\u0438\u0446\u044B:<br1><table width=300><tr>");
    int step = 1;
    int s = page - 3;
    int f = page + 3;
    if ((page < _statSortLimit) && (s < _statSortLimit)) {
      s = 1;
    }
    if (page >= _statSortLimit) {
      text.append(new StringBuilder().append("<td><a action=\"bypass -h npc_").append(getObjectId()).append("_statPk ").append(s).append("\"> ... </a></td>").toString());
    }

    for (int i = s; i < pages + 1; i++) {
      int al = i + 1;
      if (i == page) {
        text.append(new StringBuilder().append("<td>").append(i).append("</td>").toString());
      }
      else if (al <= pages) {
        text.append(new StringBuilder().append("<td><a action=\"bypass -h npc_").append(getObjectId()).append("_statPk ").append(i).append("\">").append(i).append("</a></td>").toString());
      }

      if ((step == _statSortLimit) && (f < pages)) {
        if (al >= pages) break;
        text.append(new StringBuilder().append("<td><a action=\"bypass -h npc_").append(getObjectId()).append("_statPk ").append(al).append("\"> ... </a></td>").toString()); break;
      }

      step++;
    }
    text.append("</tr></table><br>");
    String htmltext = text.toString();
    text.clear();
    text = null;
    return htmltext;
  }

  private void statClans(L2PcInstance player, int page) {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder htm = new TextBuilder();

    htm.append("<html><body><center><br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>");
    htm.append(new StringBuilder().append("<table width=290> <tr><td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statHome\">\u041B\u0438\u0434\u0435\u0440\u044B \u0441\u0435\u0440\u0432\u0435\u0440\u0430</a></font></td>").toString());
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statPvp 1\">Top Pvp</a></font></td>").toString());
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statPk 1\">Top Pk</a></font></td>").toString());
    htm.append("<td><font color=0099CC>\u041A\u043B\u0430\u043D\u044B</font></td>");
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statCastles\">\u0417\u0430\u043C\u043A\u0438</a></font></td>").toString());
    htm.append("</tr></table><br><img src=\"sek.cbui175\" width=150 height=3><br>");
    htm.append("<font color=999966><table width=310><tr><td>#</td><td>\u041D\u0430\u0437\u0432\u0430\u043D\u0438\u0435</td><td>\u0423\u0440.</td><td>\u041B\u0438\u0434\u0435\u0440</td><td>\u041E\u0447\u043A\u0438</td><td>\u041B\u044E\u0434\u0435\u0439</td></tr>");

    FastTable clans = CustomServerData.getInstance().getStatClans();

    int count = 0;
    int start = (page - 1) * _statLimit;
    int stop = start + _statLimit;
    if (stop > clans.size()) {
      stop = clans.size() - 1;
    }
    int pages = clans.size() / _statLimit + 1;
    int i = start; for (int n = stop; i < n; i++) {
      CustomServerData.StatClan clan = (CustomServerData.StatClan)clans.get(i);
      if (clan == null)
      {
        continue;
      }
      htm.append(new StringBuilder().append("<tr><td>").append(i + 1).append("</td><td><font color=CCCC33>").append(clan.name).append("</td><td>").append(clan.level).append("</td><td>").append(clan.owner).append("</td><td>").append(clan.rep).append("</td><td>").append(clan.count).append("</td></tr>").toString());
      count++;
    }

    htm.append("</table></font>");
    if (pages > 2) {
      htm.append(sortClans(page, pages));
    }

    htm.append(new StringBuilder().append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center><a action=\"bypass -h npc_").append(getObjectId()).append("_Chat 0\"><font color=666633>\u041D\u0430\u0437\u0430\u0434</font></a></body></html>").toString());

    reply.setHtml(htm.toString());
    player.sendPacket(reply);
    htm.clear();
    htm = null;
  }

  private String sortClans(int page, int pages) {
    TextBuilder text = new TextBuilder("<br>\u0421\u0442\u0440\u0430\u043D\u0438\u0446\u044B:<br1><table width=300><tr>");
    int step = 1;
    int s = page - 3;
    int f = page + 3;
    if ((page < _statSortLimit) && (s < _statSortLimit)) {
      s = 1;
    }
    if (page >= _statSortLimit) {
      text.append(new StringBuilder().append("<td><a action=\"bypass -h npc_").append(getObjectId()).append("_statClans ").append(s).append("\"> ... </a></td>").toString());
    }

    for (int i = s; i < pages + 1; i++) {
      int al = i + 1;
      if (i == page) {
        text.append(new StringBuilder().append("<td>").append(i).append("</td>").toString());
      }
      else if (al <= pages) {
        text.append(new StringBuilder().append("<td><a action=\"bypass -h npc_").append(getObjectId()).append("_statClans ").append(i).append("\">").append(i).append("</a></td>").toString());
      }

      if ((step == _statSortLimit) && (f < pages)) {
        if (al >= pages) break;
        text.append(new StringBuilder().append("<td><a action=\"bypass -h npc_").append(getObjectId()).append("_statClans ").append(al).append("\"> ... </a></td>").toString()); break;
      }

      step++;
    }
    text.append("</tr></table><br>");
    String htmltext = text.toString();
    text.clear();
    text = null;
    return htmltext;
  }

  private void statCastles(L2PcInstance player) {
    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    TextBuilder htm = new TextBuilder();

    htm.append("<html><body><center><br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>");
    htm.append(new StringBuilder().append("<table width=290> <tr><td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statHome\">\u041B\u0438\u0434\u0435\u0440\u044B \u0441\u0435\u0440\u0432\u0435\u0440\u0430</a></font></td>").toString());
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statPvp 1\">Top Pvp</a></font></td>").toString());
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statPk 1\">Top Pk</a></font></td>").toString());
    htm.append(new StringBuilder().append("<td><font color=993366><a action=\"bypass -h npc_").append(getObjectId()).append("_statClans 1\">\u041A\u043B\u0430\u043D\u044B</a></font></td>").toString());
    htm.append("<td><font color=0099CC>\u0417\u0430\u043C\u043A\u0438</font></td>");
    htm.append("</tr></table><br><img src=\"sek.cbui175\" width=150 height=3><br>");
    htm.append("<font color=999966><table width=310><tr><td>\u0417\u0430\u043C\u043E\u043A</td><td>\u0412\u043B\u0430\u0434\u0435\u043B\u0435\u0446</td><td>\u0414\u0430\u0442\u0430 \u043E\u0441\u0430\u0434\u044B</td></tr>");

    for (CustomServerData.StatCastle castle : CustomServerData.getInstance().getStatCastles()) {
      if (castle == null)
      {
        continue;
      }
      htm.append(new StringBuilder().append("<tr><td><font color=CCCC33>").append(castle.name).append("</td><td>").append(castle.owner).append("</td><td>").append(castle.siege).append("</td></tr>").toString());
    }

    htm.append("</table></font>");

    htm.append(new StringBuilder().append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center><a action=\"bypass -h npc_").append(getObjectId()).append("_Chat 0\"><font color=666633>\u041D\u0430\u0437\u0430\u0434</font></a></body></html>").toString());

    reply.setHtml(htm.toString());
    player.sendPacket(reply);
    htm.clear();
    htm = null;
  }
}