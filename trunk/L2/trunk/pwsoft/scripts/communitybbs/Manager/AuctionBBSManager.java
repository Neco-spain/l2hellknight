package scripts.communitybbs.Manager;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javolution.text.TextBuilder;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExMailArrived;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Rnd;

public class AuctionBBSManager extends BaseBBSManager
{
  private static AuctionBBSManager _instance;
  private static final int PAGE_LIMIT = 20;
  private static final int SORT_LIMIT = 8;
  private static final String CENCH = "CCCC33";
  private static final String CPRICE = "669966";
  private static final String CITEM = "993366";
  private static final String CAUG1 = "333366";
  private static final String CAUG2 = "006699";
  private static String MONEY_VARS = "Adena;";
  private static String AUC_MENU = "";
  private static final FastMap<Integer, String> moneys = Config.BBS_AUC_MONEYS;

  public static void init()
  {
    _instance = new AuctionBBSManager();
    _instance.cacheMoneys();
    _instance.cacheMenu();
    _instance.returnExpiredLots();
  }

  public static AuctionBBSManager getInstance() {
    return _instance;
  }

  public void parsecmd(String command, L2PcInstance player)
  {
    if (command.equalsIgnoreCase("_bbsauc")) {
      showIndex(player);
    } else if (command.equalsIgnoreCase("_bbsauc_office")) {
      showOffice(player);
    } else if (command.startsWith("_bbsauc_fsearch")) {
      showSearch(player);
    } else if (command.equalsIgnoreCase("_bbsauc_add")) {
      showAdd(player);
    } else if (command.startsWith("_bbsauc_show")) {
      showItem(player, Integer.parseInt(command.substring(12).trim()));
    } else if (command.startsWith("_bbsauc_enchanted")) {
      showAddEnch(player, Integer.parseInt(command.substring(17).trim()));
    } else if (command.startsWith("_bbsauc_augment")) {
      showAddAug(player, Integer.parseInt(command.substring(15).trim()));
    } else if (command.startsWith("_bbsauc_custom")) {
      showAddCustom(player, Integer.parseInt(command.substring(14).trim()));
    } else if (command.startsWith("_bbsauc_bue")) { String[] opaopa = command.substring(11).split(" ");
      int id = Integer.parseInt(opaopa[2]);
      String pass;
      try { pass = opaopa[3];
      } catch (Exception e) {
        pass = "no";
      }
      switch (Integer.parseInt(opaopa[1])) {
      case 0:
      case 1:
      case 2:
      case 3:
        getItemFrom(player, id, pass);
        break;
      case 4:
        getAugFrom(player, id, pass);
        break;
      case 5:
        getSkillFrom(player, id, pass);
        break;
      case 6:
        getHeroFrom(player, id, pass);
      }
    }
    else if (command.startsWith("_bbsauc_menu")) {
      String[] opaopa = command.substring(13).split("_");
      switch (Integer.parseInt(opaopa[0])) {
      case 5:
        addAugTo(player, Integer.parseInt(opaopa[1]), opaopa[2]);
      }
    }
    else if (command.startsWith("_bbsauc_step2_"))
    {
      String[] opaopa = command.substring(14).split("_");
      try {
        Integer item_obj = Integer.valueOf(Integer.parseInt(opaopa[0]));
        Integer item_type = Integer.valueOf(Integer.parseInt(opaopa[1]));
        Integer item_price = Integer.valueOf(Integer.parseInt(opaopa[2].trim()));
        String price_type = opaopa[3].trim();
        String pay_type = opaopa[4].trim();
        String pwd = opaopa[5].trim();
        String pwduse = opaopa[6].trim();

        if ((item_obj == null) || (item_type == null) || (item_price == null) || (price_type.length() < 2)) {
          TextBuilder tb = new TextBuilder("");
          tb.append(getMenu());
          tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430, step2<br></body></html>");
          separateAndSend(tb.toString(), player);
          return;
        }
        addToAuc(player, item_obj.intValue(), item_type.intValue(), item_price.intValue(), price_type, pay_type.equals("YES") ? 0 : 1, pwd, pwduse.equals("YES") ? 1 : 0);
      } catch (Exception e) {
        TextBuilder tb = new TextBuilder("");
        tb.append(getMenu());
        tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430, step2.2<br></body></html>");
        separateAndSend(tb.toString(), player);
      }
    } else if (command.startsWith("_bbsauc_pageshow"))
    {
      player.setBriefItem(0);
      String[] opaopa = command.substring(17).split("_");
      TextBuilder tb = new TextBuilder("");
      try {
        Integer page = Integer.valueOf(Integer.parseInt(opaopa[0]));
        Integer self = Integer.valueOf(Integer.parseInt(opaopa[1]));
        Integer item_id = Integer.valueOf(Integer.parseInt(opaopa[2]));
        Integer aug_id = Integer.valueOf(Integer.parseInt(opaopa[3]));
        Integer type = Integer.valueOf(Integer.parseInt(opaopa[4]));
        tb.append(getMenu());

        tb.append(showSellItems(player, page.intValue(), self.intValue(), 0, item_id.intValue(), aug_id.intValue(), type.intValue()));

        tb.append("<br></body></html>");
      } catch (Exception e) {
        tb.clear();
        tb.append(getMenu());

        tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430, pageshow<br></body></html>");
      }
      separateAndSend(tb.toString(), player);
    } else if (command.startsWith("_bbsauc_search"))
    {
      TextBuilder tb = new TextBuilder("");
      String[] opaopa = command.substring(15).split("_");
      tb.append(getMenu());

      tb.append(showSellItems(player, 1, 0, 0, Integer.parseInt(opaopa[1]), Integer.parseInt(opaopa[2]), Integer.parseInt(opaopa[0])));

      tb.append("<br></body></html>");
      separateAndSend(tb.toString(), player);
    }
  }

  private void showIndex(L2PcInstance player) {
    player.setBriefItem(0);
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    tb.append(showSellItems(player, 1, 0, 0, 0, 0, -1));

    tb.append("<br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private String showSellItems(L2PcInstance player, int page, int me, int last, int itemId, int augment, int type2) {
    TextBuilder text = new TextBuilder(new StringBuilder().append("&nbsp;&nbsp;\u0421\u0442\u0440\u0430\u043D\u0438\u0446\u0430 ").append(page).append(":<br>").toString());
    text.append("<table width=650 border=0>");
    int limit1 = (page - 1) * 20;
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      if (type2 >= 0) {
        st = con.prepareStatement("SELECT id, itemId, itemName, enchant, augment, augLvl, price, money, type, ownerId, shadow, pwd FROM `z_bbs_auction` WHERE `type` = ? ORDER BY `id` DESC LIMIT ?, ?");
        st.setInt(1, type2);
        st.setInt(2, limit1);
        st.setInt(3, 20);
      } else if (itemId > 0) {
        st = con.prepareStatement("SELECT id, itemId, itemName, enchant, augment, augLvl, price, money, type, ownerId, shadow, pwd FROM `z_bbs_auction` WHERE `itemId` = ? ORDER BY `id` DESC LIMIT ?, ?");
        st.setInt(1, itemId);
        st.setInt(2, limit1);
        st.setInt(3, 20);
      } else if (augment > 0) {
        st = con.prepareStatement("SELECT id, itemId, itemName, enchant, augment, augLvl, price, money, type, ownerId, shadow, pwd FROM `z_bbs_auction` WHERE `augment` = ? ORDER BY `id` DESC LIMIT ?, ?");
        st.setInt(1, augment);
        st.setInt(2, limit1);
        st.setInt(3, 20);
      } else if (me == 1) {
        st = con.prepareStatement("SELECT id, itemId, itemName, enchant, augment, augLvl, price, money, type, ownerId, shadow, pwd FROM `z_bbs_auction` WHERE `ownerId` = ? ORDER BY `id` DESC LIMIT ?, ?");
        st.setInt(1, player.getObjectId());
        st.setInt(2, limit1);
        st.setInt(3, 20);
      } else {
        st = con.prepareStatement("SELECT id, itemId, itemName, enchant, augment, augLvl, price, money, type, ownerId, shadow, pwd FROM `z_bbs_auction` ORDER BY `id` DESC LIMIT ?, ?");
        st.setInt(1, limit1);
        st.setInt(2, 20);
      }
      int i = 0;
      rs = st.executeQuery();
      while (rs.next()) {
        int id = rs.getInt("id");
        int itmId = rs.getInt("itemId");
        String name = rs.getString("itemName");
        int ownerId = rs.getInt("ownerId");
        int augId = rs.getInt("augment");
        int augLvl = rs.getInt("augLvl");
        int enchant = rs.getInt("enchant");
        int type = rs.getInt("type");
        String pwd = rs.getString("pwd");

        String priceB = new StringBuilder().append("<font color=669966>").append(Util.formatAdena(rs.getInt("price"))).append(" ").append(getMoneyCall(rs.getInt("money"))).append("; \"").append(getSellerName(con, ownerId)).append("</font>").toString();

        String icon = "";
        String ench = "";
        String augm = "";
        switch (type) {
        case 0:
        case 1:
        case 2:
        case 3:
          L2Item item = ItemTable.getInstance().getTemplate(itmId);
          if (item == null)
          {
            continue;
          }
          name = item.getName();
          icon = item.getIcon();
          ench = new StringBuilder().append("+").append(enchant).append("").toString();
          if (augId > 0)
            augm = new StringBuilder().append(" ").append(getAugmentSkill(augId, augLvl)).append("<br1>").toString(); break;
        case 4:
          name = getAugmentSkill(augId, augLvl);
          icon = "Icon.skill3123";
          break;
        case 5:
          name = new StringBuilder().append("\u0421\u043A\u0438\u043B\u043B: ").append(name).toString();
          icon = "Icon.etc_spell_books_element_i00";
          ench = new StringBuilder().append(enchant).append("lvl").toString();
          break;
        case 6:
          name = name.replace("Hero: ", "\u0413\u0435\u0440\u043E\u0439\u0441\u0442\u0432\u043E: ");
          icon = "Icon.skill1374";
        default:
          if (i == 0) {
            text.append(new StringBuilder().append("<tr><td><table width=300><tr><td><img src=\"").append(icon).append("\" width=32 height=32></td>").toString());
            text.append(new StringBuilder().append("<td width=270><a action=\"bypass _bbsauc_show ").append(id).append("\">").append(name).append("</a> <font color=993366> ").append(ench).append("</font><br1> ").append(augm).append("").toString());
            text.append(new StringBuilder().append("<font color=CC3366> ").append(pwd.equals(" ") ? "" : "***").append("</font> ").append(priceB).append("</td></tr></table></td>").toString());
            i = 1;
          } else {
            text.append(new StringBuilder().append("<td><table width=300><tr><td><img src=\"").append(icon).append("\" width=32 height=32></td>").toString());
            text.append(new StringBuilder().append("<td width=270><a action=\"bypass _bbsauc_show ").append(id).append("\">").append(name).append("</a> <font color=993366> ").append(ench).append("</font><br1> ").append(augm).append("").toString());
            text.append(new StringBuilder().append("<font color=CC3366> ").append(pwd.equals(" ") ? "" : "***").append("</font> ").append(priceB).append("</td></tr></table></td></tr>").toString());
            i = 0;
          }
        }
      }
      text.append("</table><br>");
      if (last == 1) {
        text.append("<br>");
      } else {
        int pages = getPageCount(con, me, itemId, augment, type2, player.getObjectId());
        if (pages >= 2)
          text.append(sortPages(page, pages, me, itemId, augment, type2));
      }
    }
    catch (SQLException e) {
      System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, showSellItems() error: ").append(e).toString());
    } finally {
      Close.CSR(con, st, rs);
    }
    return text.toString();
  }

  private void showItem(L2PcInstance player, int id) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT * FROM `z_bbs_auction` WHERE `id`=? LIMIT 1");
      st.setInt(1, id);
      rs = st.executeQuery();
      if (rs.next()) {
        int item_id = rs.getInt("itemId");
        String name = rs.getString("itemName");
        int aug_id = rs.getInt("augment");
        int aug_lvl = rs.getInt("augLvl");
        int item_ench = rs.getInt("enchant");
        int owner = rs.getInt("ownerId");
        int price = rs.getInt("price");
        int money = rs.getInt("money");
        int item_type = rs.getInt("type");
        int pay_type = rs.getInt("pay");
        String pwd = rs.getString("pwd");

        if (pay_type == 1) {
          int coin_id = 0;
          int coin_price = 0;
          String coin_name = "";
          switch (item_type) {
          case 0:
          case 1:
          case 2:
            coin_id = Config.BBS_AUC_ITEM_COIN;
            coin_price = Config.BBS_AUC_ITEM_PRICE;
            coin_name = Config.BBS_AUC_ITEM_NAME;
            break;
          case 3:
          case 4:
            coin_id = Config.BBS_AUC_AUG_COIN;
            coin_price = Config.BBS_AUC_AUG_PRICE;
            coin_name = Config.BBS_AUC_AUG_NAME;
            break;
          case 5:
            coin_id = Config.BBS_AUC_SKILL_COIN;
            coin_price = Config.BBS_AUC_SKILL_PRICE;
            coin_name = Config.BBS_AUC_SKILL_NAME;
            break;
          case 6:
            coin_id = Config.BBS_AUC_HERO_COIN;
            coin_price = Config.BBS_AUC_HERO_PRICE;
            coin_name = Config.BBS_AUC_HERO_NAME;
          }

          if ((coin_id > 0) && (player.getObjectId() != owner)) {
            tb.append(new StringBuilder().append("<br><br>&nbsp;&nbsp;<font color=FF9933>\u041D\u0435\u043E\u0431\u0445\u043E\u0434\u0438\u043C\u043E \u043E\u043F\u043B\u0430\u0442\u0438\u0442\u044C \u043D\u0430\u043B\u043E\u0433 \u0430\u0443\u043A\u0446\u0438\u043E\u043D\u0430: ").append(coin_price).append(" ").append(coin_name).append(".</font>").toString());
          }
        }

        String icon = "";
        String ench = "";
        String augm = "";
        switch (item_type) {
        case 0:
        case 1:
        case 2:
        case 3:
          L2Item item = ItemTable.getInstance().getTemplate(item_id);
          if (item == null) { tb.append("&nbsp;&nbsp;\u041E\u0448\u0438\u0431\u043A\u0430.");
            separateAndSend(tb.toString(), player);
            return; }
          name = item.getName();
          icon = item.getIcon();
          ench = new StringBuilder().append("+").append(item_ench).append("").toString();
          if (aug_id <= 0) break;
          augm = new StringBuilder().append(" ").append(getAugmentSkill(aug_id, aug_lvl)).append("<br1>").toString(); break;
        case 4:
          name = getAugmentSkill(aug_id, aug_lvl);
          icon = "Icon.skill3123";
          break;
        case 5:
          icon = "Icon.etc_spell_books_element_i00";
          ench = new StringBuilder().append(item_ench).append("lvl").toString();
          break;
        case 6:
          icon = "Icon.skill1374";
        }

        tb.append(new StringBuilder().append("<br><table width=400 border=0><tr><td width=32><img src=\"").append(icon).append("\" width=32 height=32></td>").toString());
        tb.append(new StringBuilder().append("<td width=342 align=left><font color=LEVEL>").append(name).append(" ").append(ench).append(" </font> ").append(augm).append("</td></tr>").toString());
        tb.append("<tr><td width=32></td><td width=342 align=left><br><br><img src=\"sek.cbui355\" width=300 height=1><br></td></tr></table>");
        String priceB = new StringBuilder().append("<br>&nbsp;&nbsp;<font color=669966>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(Util.formatAdena(price)).append(" ").append(getMoneyCall(money)).append("; <br1> &nbsp;&nbsp;\u041F\u0440\u043E\u0434\u0430\u0432\u0435\u0446: ").append(getSellerName(con, owner)).append("</font><br>").toString();
        if (player.getObjectId() == owner) {
          priceB = new StringBuilder().append("<br>&nbsp;&nbsp;<button value=\"\u0417\u0430\u0431\u0440\u0430\u0442\u044C\" action=\"bypass _bbsauc_bue ").append(item_type).append(" ").append(id).append(" no\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString();
        }
        tb.append(priceB);
        if (player.getObjectId() != owner) {
          L2ItemInstance coin = player.getInventory().getItemByItemId(money);
          if ((coin == null) || (coin.getCount() < price)) {
            tb.append("&nbsp;&nbsp;<font color=999999>[\u041A\u0443\u043F\u0438\u0442\u044C]</font>");
          }
          else if (pwd.length() > 2) {
            tb.append("&nbsp;&nbsp;\u041B\u043E\u0442 \u0437\u0430\u0449\u0438\u0449\u0435\u043D \u043F\u0430\u0440\u043E\u043B\u0435\u043C, \u0432\u0432\u0435\u0434\u0438\u0442\u0435 \u043F\u0430\u0440\u043E\u043B\u044C:<br>");
            tb.append("&nbsp;&nbsp;<edit var=\"pass\" width=70 length=\"16\"><br>");
            tb.append(new StringBuilder().append("&nbsp;&nbsp;<button value=\"\u041A\u0443\u043F\u0438\u0442\u044C\" action=\"bypass _bbsauc_bue ").append(item_type).append(" ").append(id).append(" $pass\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
          } else {
            tb.append(new StringBuilder().append("&nbsp;&nbsp;<button value=\"\u041A\u0443\u043F\u0438\u0442\u044C\" action=\"bypass _bbsauc_bue ").append(item_type).append(" ").append(id).append(" no\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>").toString());
          }
        }
      }
      else {
        tb.append("&nbsp;&nbsp;\u041De \u043D\u0430\u0439\u0434\u0435\u043D\u0430 \u0438\u043B\u0438 \u0443\u0436\u0435 \u043A\u0443\u043F\u0438\u043B\u0438.");
      }
    } catch (SQLException e) {
      System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, showItem() error: ").append(e).toString());
    } finally {
      Close.CSR(con, st, rs);
    }

    tb.append("<br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private void showOffice(L2PcInstance player) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    tb.append(new StringBuilder().append("<table><tr><td width=260>\u041B\u0438\u0447\u043D\u044B\u0439 \u043A\u0430\u0431\u0438\u043D\u0435\u0442 ").append(player.getName()).append("</td></tr></table><br1>").toString());

    tb.append("<button value=\"\u041C\u043E\u0438 \u043B\u043E\u0442\u044B\" action=\"bypass _bbsauc_pageshow_1_1_0_0_-1\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
    tb.append("</body></html>");

    tb.append("<br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private void showSearch(L2PcInstance player) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    tb.append("<table width=500><tr><td width=5>&nbsp;&nbsp;\u041F\u043E\u0438\u0441\u043A: </td><td><font color=LEVEL>\u0427\u0442\u043E \u0438\u0449\u0435\u043C?</font><br1>");
    tb.append("\u041E\u0440\u0443\u0436\u0438\u0435:<br1>");
    tb.append("<table width=240><tr><td><button value=\"\u0417\u0430\u0442\u043E\u0447\u0435\u043D\u043D\u043E\u0435\" action=\"bypass _bbsauc_search 0_0_0\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
    tb.append("<td><button value=\"\u0410\u0443\u0433\u043C\u0435\u043D\u0442\u0438\u0440\u043E\u0432\u0430\u043D\u043D\u043E\u0435\" action=\"bypass _bbsauc_search 3_0_0\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
    tb.append("</tr></table><br>\u0417\u0430\u0442\u043E\u0447\u0435\u043D\u043D\u0430\u044F \u0431\u0440\u043E\u043D\u044F:<br1>");
    tb.append("<button value=\"\u041E\u0434\u0435\u0436\u0434\u0430\" action=\"bypass _bbsauc_search 1_0_0\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
    tb.append("<button value=\"\u0411\u0438\u0436\u0443\u0442\u0435\u0440\u0438\u044F\" action=\"bypass _bbsauc_search 2_0_0\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
    tb.append("\u041F\u0440\u043E\u0447\u0435\u0435:<br1>");
    tb.append("<button value=\"\u0410\u0443\u0433\u043C\u0435\u043D\u0442\" action=\"bypass _bbsauc_search 4_0_0\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
    tb.append("<button value=\"\u0421\u043A\u0438\u043B\u043B\" action=\"bypass _bbsauc_search 5_0_0\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
    tb.append("<button value=\"\u0413\u0435\u0440\u043E\u0439\u0441\u0442\u0432\u043E\" action=\"bypass _bbsauc_search 6_0_0\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");

    tb.append("</td></tr></table><br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private void showAdd(L2PcInstance player) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    String content = getPwHtm("menu-auction");
    if (content == null) {
      content = "<html><body><br><br><center>404 :File Not found: 'data/html/CommunityBoard/pw/menu-auction.htm' </center></body></html>";
    }
    tb.append(content);

    tb.append("<br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private void showAddEnch(L2PcInstance player, int type) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    int pwd = Rnd.get(1100, 9999);
    tb.append(new StringBuilder().append("<br><br>&nbsp;&nbsp;\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u0443\u0441\u043B\u0443\u0433\u0438: ").append(Config.BBS_AUC_ITEM_PRICE).append(" ").append(Config.BBS_AUC_ITEM_NAME).append(".<br><br>").toString());
    tb.append(new StringBuilder().append("&nbsp;&nbsp;\u0428\u0430\u0433 1. \u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u0436\u0435\u043B\u0430\u0435\u043C\u0443\u044E \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: <br><table width=300><tr><td><edit var=\"price\" width=70 length=\"16\"></td><td><combobox width=100 var=type list=\"").append(MONEY_VARS).append("\"></td></tr></table><br>").toString());
    tb.append("&nbsp;&nbsp;\u0428\u0430\u0433 2. \u041E\u043F\u043B\u0430\u0447\u0438\u0432\u0430\u0435\u0442\u0435 \u043D\u0430\u043B\u043E\u0433 \u0430\u0443\u043A\u0446\u0438\u043E\u043D\u0430? (\u0414\u0430: YES; \u041E\u043F\u043B\u0430\u0442\u0438\u0442 \u043F\u043E\u043A\u0443\u043F\u0430\u0442\u0435\u043B\u044C: NO)) <br><table width=300><tr><td></td><td><combobox width=100 var=payer list=\"YES;NO\"></td></tr></table><br>");
    tb.append(new StringBuilder().append("&nbsp;&nbsp;\u0428\u0430\u0433 3. \u0418\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u043F\u0430\u0440\u043E\u043B\u044C? \u0412\u0430\u0448 \u043F\u0430\u0440\u043E\u043B\u044C: <font color=LEVEL>").append(pwd).append("</font> <br><table width=300><tr><td></td><td><combobox width=100 var=pass list=\"NO;YES\"></td></tr></table><br>").toString());
    tb.append("<br>&nbsp;&nbsp;&nbsp;\u0428\u0430\u0433 4. \u0412\u044B\u0431\u0435\u0440\u0438\u0442\u0435 \u043F\u0440\u0435\u0434\u043C\u0435\u0442, \u043A\u043E\u0442\u043E\u0440\u044B\u0439 \u0445\u043E\u0442\u0438\u0442\u0435 \u0432\u044B\u0441\u0442\u0430\u0432\u0438\u0442\u044C:<br1><table width=650>");

    int i = 0;
    String augment = "";
    for (L2ItemInstance item : player.getInventory().getItems()) {
      if (item == null)
      {
        continue;
      }
      if ((item.getItem().getType2() == type) && (item.getEnchantLevel() > 0) && (item.canBeEnchanted()) && (!item.isEquipped()) && (item.isDestroyable())) {
        if ((item.isAugmented()) && (item.getAugmentation().getAugmentSkill() != null)) {
          augment = new StringBuilder().append("<br1>").append(getAugmentSkill(item.getAugmentation().getAugmentSkill().getId(), item.getAugmentation().getAugmentSkill().getLevel())).toString();
        }

        if (i == 0) {
          tb.append(new StringBuilder().append("<tr><td><table width=300><tr><td><img src=\"").append(item.getItem().getIcon()).append("\" width=32 height=32></td>").toString());
          tb.append(new StringBuilder().append("<td width=270><a action=\"bypass _bbsauc_step2_").append(item.getObjectId()).append("_0_ $price _ $type _ $payer _ ").append(pwd).append(" _ $pass\">").append(item.getItem().getName()).append("</a> <font color=993366> +").append(item.getEnchantLevel()).append("</font> ").append(augment).append("").toString());
          tb.append("</td></tr></table></td>");
          i = 1;
        } else {
          tb.append(new StringBuilder().append("<td><table width=300><tr><td><img src=\"").append(item.getItem().getIcon()).append("\" width=32 height=32></td>").toString());
          tb.append(new StringBuilder().append("<td width=270><a action=\"bypass _bbsauc_step2_").append(item.getObjectId()).append("_0_ $price _ $type _ $payer _ ").append(pwd).append(" _ $pass\">").append(item.getItem().getName()).append("</a> <font color=993366> +").append(item.getEnchantLevel()).append("</font> ").append(augment).append("").toString());
          tb.append("</td></tr></table></td></tr>");
          i = 0;
        }
        augment = "";
      }
    }

    tb.append("</table><br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private void showAddAug(L2PcInstance player, int type) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    int pwd = Rnd.get(1100, 9999);
    tb.append(new StringBuilder().append("<br><br>&nbsp;&nbsp;\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u0443\u0441\u043B\u0443\u0433\u0438: ").append(Config.BBS_AUC_AUG_PRICE).append(" ").append(Config.BBS_AUC_AUG_NAME).append(".<br><br>").toString());
    tb.append(new StringBuilder().append("&nbsp;&nbsp;\u0428\u0430\u0433 1. \u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u0436\u0435\u043B\u0430\u0435\u043C\u0443\u044E \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: <br><table width=300><tr><td><edit var=\"price\" width=70 length=\"16\"></td><td><combobox width=100 var=type list=\"").append(MONEY_VARS).append("\"></td></tr></table><br>").toString());
    tb.append("&nbsp;&nbsp;\u0428\u0430\u0433 2. \u041E\u043F\u043B\u0430\u0447\u0438\u0432\u0430\u0435\u0442\u0435 \u043D\u0430\u043B\u043E\u0433 \u0430\u0443\u043A\u0446\u0438\u043E\u043D\u0430? (\u0414\u0430: YES; \u041E\u043F\u043B\u0430\u0442\u0438\u0442 \u043F\u043E\u043A\u0443\u043F\u0430\u0442\u0435\u043B\u044C: NO)) <br><table width=300><tr><td></td><td><combobox width=100 var=payer list=\"YES;NO\"></td></tr></table><br>");
    tb.append(new StringBuilder().append("&nbsp;&nbsp;\u0428\u0430\u0433 3. \u0418\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u043F\u0430\u0440\u043E\u043B\u044C? \u0412\u0430\u0448 \u043F\u0430\u0440\u043E\u043B\u044C: <font color=LEVEL>").append(pwd).append("</font> <br><table width=300><tr><td></td><td><combobox width=100 var=pass list=\"NO;YES\"></td></tr></table><br>").toString());
    tb.append("<br>&nbsp;&nbsp;&nbsp;\u0428\u0430\u0433 4. \u0412\u044B\u0431\u0435\u0440\u0438\u0442\u0435 \u043F\u0440\u0435\u0434\u043C\u0435\u0442, \u043A\u043E\u0442\u043E\u0440\u044B\u0439 \u0445\u043E\u0442\u0438\u0442\u0435 \u0432\u044B\u0441\u0442\u0430\u0432\u0438\u0442\u044C:<br1><table width=650>");

    int i = 0;
    for (L2ItemInstance item : player.getInventory().getItems()) {
      if (item == null)
      {
        continue;
      }
      if ((item.isAugmented()) && (item.getAugmentation().getAugmentSkill() != null) && (item.getItem().getType2() == 0) && (item.canBeEnchanted()) && (!item.isEquipped()) && (item.isDestroyable())) {
        String name = item.getItem().getName();
        String icon = item.getItem().getIcon();
        String ench = new StringBuilder().append(" +").append(item.getEnchantLevel()).append("").toString();
        String augment = getAugmentSkill(item.getAugmentation().getAugmentSkill().getId(), item.getAugmentation().getAugmentSkill().getLevel());
        if (type == 4) {
          name = augment;
          icon = "Icon.skill3123";
          ench = "";
          augment = "";
        } else {
          augment = new StringBuilder().append("<br1>").append(augment).toString();
        }

        if (i == 0) {
          tb.append(new StringBuilder().append("<tr><td><table width=300><tr><td><img src=\"").append(icon).append("\" width=32 height=32></td>").toString());
          tb.append(new StringBuilder().append("<td width=270><a action=\"bypass _bbsauc_step2_").append(item.getObjectId()).append("_").append(type).append("_ $price _ $type _ $payer _ ").append(pwd).append(" _ $pass\">").append(name).append("</a> <font color=993366> ").append(ench).append("</font> ").append(augment).append("").toString());
          tb.append("</td></tr></table></td>");
          i = 1;
        } else {
          tb.append(new StringBuilder().append("<td><table width=300><tr><td><img src=\"").append(icon).append("\" width=32 height=32></td>").toString());
          tb.append(new StringBuilder().append("<td width=270><a action=\"bypass _bbsauc_step2_").append(item.getObjectId()).append("_").append(type).append("_ $price _ $type _ $payer _ ").append(pwd).append(" _ $pass\">").append(name).append("</a> <font color=993366> ").append(ench).append("</font> ").append(augment).append("").toString());
          tb.append("</td></tr></table></td></tr>");
          i = 0;
        }
      }
    }

    tb.append("</table><br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private void showAddCustom(L2PcInstance player, int type) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    int pwd = Rnd.get(1100, 9999);
    tb.append(new StringBuilder().append("&nbsp;&nbsp;\u0428\u0430\u0433 1. \u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u0436\u0435\u043B\u0430\u0435\u043C\u0443\u044E \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: <br><table width=300><tr><td><edit var=\"price\" width=70 length=\"16\"></td><td><combobox width=100 var=type list=\"").append(MONEY_VARS).append("\"></td></tr></table><br>").toString());
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    if (type == 5) {
      tb.append(new StringBuilder().append("<br><br>&nbsp;&nbsp;\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u0443\u0441\u043B\u0443\u0433\u0438: ").append(Config.BBS_AUC_SKILL_PRICE).append(" ").append(Config.BBS_AUC_SKILL_NAME).append(".<br><br>").toString());
      tb.append("&nbsp;&nbsp;\u0428\u0430\u0433 2. \u041E\u043F\u043B\u0430\u0447\u0438\u0432\u0430\u0435\u0442\u0435 \u043D\u0430\u043B\u043E\u0433 \u0430\u0443\u043A\u0446\u0438\u043E\u043D\u0430? (\u0414\u0430: YES; \u041E\u043F\u043B\u0430\u0442\u0438\u0442 \u043F\u043E\u043A\u0443\u043F\u0430\u0442\u0435\u043B\u044C: NO)) <br><table width=300><tr><td></td><td><combobox width=100 var=payer list=\"YES;NO\"></td></tr></table><br>");
      tb.append(new StringBuilder().append("&nbsp;&nbsp;\u0428\u0430\u0433 3. \u0418\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u043F\u0430\u0440\u043E\u043B\u044C? \u0412\u0430\u0448 \u043F\u0430\u0440\u043E\u043B\u044C: <font color=LEVEL>").append(pwd).append("</font> <br><table width=300><tr><td></td><td><combobox width=100 var=pass list=\"NO;YES\"></td></tr></table><br>").toString());
      tb.append("<br>&nbsp;&nbsp;&nbsp;<table width=280><tr><td>\u0428\u0430\u0433 4. \u0412\u044B\u0431\u0435\u0440\u0438\u0442\u0435 \u0441\u043A\u0438\u043B\u043B, \u043A\u043E\u0442\u043E\u0440\u044B\u0439 \u0445\u043E\u0442\u0438\u0442\u0435 \u0432\u044B\u0441\u0442\u0430\u0432\u0438\u0442\u044C: <br></td></tr>");
      SkillTable sst = SkillTable.getInstance();
      try {
        con = L2DatabaseFactory.getInstance().getConnection();
        st = con.prepareStatement("SELECT skill_id, skill_lvl FROM z_donate_skills WHERE char_id=?");
        st.setInt(1, player.getObjectId());
        rs = st.executeQuery();
        while (rs.next()) {
          int id = rs.getInt("skill_id");
          int lvl = rs.getInt("skill_lvl");
          L2Skill skill = sst.getInfo(id, lvl);
          tb.append(new StringBuilder().append("<tr><td><a action=\"bypass _bbsauc_step2_").append(id).append("_").append(type).append("_ $price _ $type _ $payer _ ").append(pwd).append(" _ $pass\"><font color=bef574>").append(skill.getName()).append(" (").append(lvl).append(" \u0443\u0440\u043E\u0432\u0435\u043D\u044C)</font></a><br></td></tr>").toString());
        }
      } catch (SQLException e) {
        System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, showAddCustom1 ").append(e).toString());
      } finally {
        Close.CSR(con, st, rs);
      }
      tb.append("</table><br></body></html>");
    } else {
      tb.append(new StringBuilder().append("<br><br>&nbsp;&nbsp;\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u0443\u0441\u043B\u0443\u0433\u0438: ").append(Config.BBS_AUC_HERO_PRICE).append(" ").append(Config.BBS_AUC_HERO_NAME).append(".<br><br>").toString());
      tb.append("&nbsp;&nbsp;\u0428\u0430\u0433 2. \u041E\u043F\u043B\u0430\u0447\u0438\u0432\u0430\u0435\u0442\u0435 \u043D\u0430\u043B\u043E\u0433 \u0430\u0443\u043A\u0446\u0438\u043E\u043D\u0430? (\u0414\u0430: YES; \u041E\u043F\u043B\u0430\u0442\u0438\u0442 \u043F\u043E\u043A\u0443\u043F\u0430\u0442\u0435\u043B\u044C: NO)) <br><table width=300><tr><td></td><td><combobox width=100 var=payer list=\"YES;NO\"></td></tr></table><br>");
      tb.append(new StringBuilder().append("&nbsp;&nbsp;\u0428\u0430\u0433 3. \u0418\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u043F\u0430\u0440\u043E\u043B\u044C? \u0412\u0430\u0448 \u043F\u0430\u0440\u043E\u043B\u044C: <font color=LEVEL>").append(pwd).append("</font> <br><table width=300><tr><td></td><td><combobox width=100 var=pass list=\"NO;YES\"></td></tr></table><br>").toString());
      tb.append("<br>&nbsp;&nbsp;&nbsp;\u0428\u0430\u0433 4. \u0421\u0442\u0430\u0442\u0443\u0441 \u0413\u0435\u0440\u043E\u044F: <br>");
      try {
        con = L2DatabaseFactory.getInstance().getConnection();
        st = con.prepareStatement("SELECT hero FROM characters WHERE obj_Id=? LIMIT 1");
        st.setInt(1, player.getObjectId());
        rs = st.executeQuery();
        if (rs.next()) {
          long expire = rs.getLong("hero");
          if (expire == 1L) {
            tb.append(new StringBuilder().append("&nbsp;&nbsp;<a action=\"bypass _bbsauc_step2_0_").append(type).append("_ $price _ $type _ $payer _ ").append(pwd).append(" _ $pass\">\u0411\u0435\u0441\u043A\u043E\u043D\u0435\u0447\u043D\u044B\u0439</font></a><br>").toString());
          } else if (System.currentTimeMillis() - expire < 0L) {
            String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(expire));
            tb.append(new StringBuilder().append("&nbsp;&nbsp;<a action=\"bypass _bbsauc_step2_0_").append(type).append("_ $price _ $type _ $payer _ ").append(pwd).append(" _ $pass\"> \u0418\u0441\u0442\u0435\u043A\u0430\u0435\u0442 ").append(date).append("</font></a><br>").toString());
          } else {
            tb.append("<br><br>&nbsp;\u0412\u044B \u043D\u0435 \u0433\u0435\u0440\u043E\u0439.");
          }
        } else {
          tb.append("<br><br>&nbsp;\u0412\u044B \u043D\u0435 \u0433\u0435\u0440\u043E\u0439.");
        }
      } catch (SQLException e) {
        System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, showAddCustom2 ").append(e).toString());
      } finally {
        Close.CSR(con, st, rs);
      }
    }

    tb.append("<br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private void getItemFrom(L2PcInstance player, int id, String tpwd) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT * FROM `z_bbs_auction` WHERE `id`=? LIMIT 1");
      st.setInt(1, id);
      rs = st.executeQuery();
      if (rs.next()) {
        int item_id = rs.getInt("itemId");
        String name = rs.getString("itemName");
        int aug_id = rs.getInt("augment");
        int aug_lvl = rs.getInt("augLvl");
        int aug_hex = rs.getInt("augAttr");
        int item_ench = rs.getInt("enchant");
        int owner = rs.getInt("ownerId");
        int price = rs.getInt("price");
        int money = rs.getInt("money");
        int type = rs.getInt("type");
        int pay = rs.getInt("pay");
        String pwd = rs.getString("pwd");
        if ((owner != player.getObjectId()) && (!pwd.equals(" ")) && 
          (!tpwd.equals(pwd))) { tb.append("&nbsp;&nbsp;\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 \u043F\u0430\u0440\u043E\u043B\u044C.");
          separateAndSend(tb.toString(), player);
          return;
        }
        if (item_id > 0) {
          L2Item item = ItemTable.getInstance().getTemplate(item_id);
          if (item == null) { tb.append("&nbsp;&nbsp;\u041F\u0440\u0435\u0434\u043C\u0435\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.");
            separateAndSend(tb.toString(), player);
            return; } if (!transferPay(con, owner, name, item_ench, aug_id, aug_lvl, price, money, player, pay, type)) { tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C.");
            separateAndSend(tb.toString(), player);
            return; }
          if ((pay == 0) && (owner == player.getObjectId())) {
            int coin_id = Config.BBS_AUC_ITEM_COIN;
            int coin_price = Config.BBS_AUC_ITEM_PRICE;
            if (type == 3) {
              coin_id = Config.BBS_AUC_AUG_COIN;
              coin_price = Config.BBS_AUC_AUG_PRICE;
            }
            if (coin_id > 0) {
              player.addItem("auc.return", coin_id, coin_price, player, true);
            }
          }

          L2ItemInstance reward = player.getInventory().addItem("auc1", item_id, 1, player, player.getTarget());
          if (reward == null) { tb.append("&nbsp;&nbsp;\u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u043E.");
            separateAndSend(tb.toString(), player);
            return; } if (item_ench > 0) {
            reward.setEnchantLevel(item_ench);
          }
          if (aug_id > 0) {
            reward.setAugmentation(new L2Augmentation(reward, aug_hex, aug_id, aug_lvl, true));
          }

          SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
          smsg.addItemName(item_id);
          player.sendPacket(smsg);
          player.sendItems(true); } else { tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430.");
          separateAndSend(tb.toString(), player);
          return;
        }
        Close.SR(st, rs);
        st = con.prepareStatement("DELETE FROM `z_bbs_auction` WHERE `id`=?");
        st.setInt(1, id);
        st.executeUpdate();
        if (owner == player.getObjectId())
          tb.append("&nbsp;&nbsp;\u0417\u0430\u0431\u0440\u0430\u043B\u0438.");
        else
          tb.append("&nbsp;&nbsp;\u041F\u0440\u0435\u0434\u043C\u0435\u0442 \u043A\u0443\u043F\u043B\u0435\u043D.");
      }
      else {
        tb.append("&nbsp;&nbsp;\u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u043E.");
      }
    } catch (SQLException e) {
      System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, getItemFrom() error: ").append(e).toString());
    } finally {
      Close.CSR(con, st, rs);
    }

    tb.append("<br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private void getAugFrom(L2PcInstance player, int id, String tpwd) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    tb.append("&nbsp;&nbsp;&nbsp;\u0412\u044B\u0431\u0435\u0440\u0438\u0442\u0435 \u043E\u0440\u0443\u0436\u0438\u0435, \u0432 \u043A\u043E\u0442\u043E\u0440\u043E\u0435 \u0445\u043E\u0442\u0438\u0442\u0435 \u0432\u0441\u0442\u0430\u0432\u0438\u0442\u044C \u0430\u0443\u0433\u043C\u0435\u043D\u0442:<br1><table width=370>");
    int i = 0;
    player.setBriefItem(id);
    for (L2ItemInstance item : player.getInventory().getItems()) {
      if (item == null)
      {
        continue;
      }
      int itemType = item.getItem().getType2();
      if ((item.canBeEnchanted()) && (!item.isAugmented()) && (!item.isEquipped()) && (item.isDestroyable()) && (itemType == 0)) {
        if (i == 0) {
          tb.append(new StringBuilder().append("<tr><td><img src=\"").append(item.getItem().getIcon()).append("\" width=32 height=32></td><td><a action=\"bypass _bbsauc_menu 5_").append(item.getObjectId()).append("_").append(tpwd).append("\"> ").append(item.getItem().getName()).append(" + ").append(item.getEnchantLevel()).append("</a></td><td width=30></td>").toString());
          i = 1;
        } else {
          tb.append(new StringBuilder().append("<td><img src=\"").append(item.getItem().getIcon()).append("\" width=32 height=32></td><td><a action=\"bypass _bbsauc_menu 5_").append(item.getObjectId()).append("_").append(tpwd).append("\"> ").append(item.getItem().getName()).append(" + ").append(item.getEnchantLevel()).append("</a></td></tr>").toString());
          i = 0;
        }
      }
    }

    tb.append("</table><br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private void getSkillFrom(L2PcInstance player, int id, String tpwd) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT * FROM `z_bbs_auction` WHERE `id`=? LIMIT 1");
      st.setInt(1, id);
      rs = st.executeQuery();
      if (rs.next()) {
        int item_id = rs.getInt("itemId");
        String name = rs.getString("itemName");
        int item_ench = rs.getInt("enchant");
        int owner = rs.getInt("ownerId");
        int price = rs.getInt("price");
        int money = rs.getInt("money");
        int type = rs.getInt("type");
        int pay = rs.getInt("pay");
        String pwd = rs.getString("pwd");
        if ((owner != player.getObjectId()) && (!pwd.equals(" ")) && 
          (!tpwd.equals(pwd))) { tb.append("&nbsp;&nbsp;\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 \u043F\u0430\u0440\u043E\u043B\u044C.");
          separateAndSend(tb.toString(), player);
          return;
        }
        L2Skill skill = null;
        if (item_id > 0) {
          skill = SkillTable.getInstance().getInfo(item_id, item_ench);
          if (skill == null) { tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 1.");
            separateAndSend(tb.toString(), player);
            return; } if (!transferPay(con, owner, name, item_ench, 0, 0, price, money, player, pay, type)) { tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C.");
            separateAndSend(tb.toString(), player);
            return; }
          if ((pay == 0) && (owner == player.getObjectId()) && 
            (Config.BBS_AUC_SKILL_COIN > 0)) {
            player.addItem("auc.return", Config.BBS_AUC_SKILL_COIN, Config.BBS_AUC_SKILL_PRICE, player, true);
          }

          Close.SR(st, rs);
          player.addSkill(skill, false);
          player.sendSkillList();
          player.addDonateSkill(0, item_id, item_ench, -1L); } else { tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 2.");
          separateAndSend(tb.toString(), player);
          return;
        }
        st = con.prepareStatement("DELETE FROM `z_bbs_auction` WHERE `id`=?");
        st.setInt(1, id);
        st.executeUpdate();
        if (owner == player.getObjectId())
          tb.append(new StringBuilder().append("&nbsp;&nbsp;\u0417\u0430\u0431\u0440\u0430\u043B\u0438 \u0441\u043A\u0438\u043B\u043B: <font color=bef574>").append(skill.getName()).append(" (").append(item_ench).append(" \u0443\u0440\u043E\u0432\u0435\u043D\u044C)</font>").toString());
        else
          tb.append(new StringBuilder().append("&nbsp;&nbsp;\u041A\u0443\u043F\u043B\u0435\u043D \u0441\u043A\u0438\u043B\u043B: <font color=bef574>").append(skill.getName()).append(" (").append(item_ench).append(" \u0443\u0440\u043E\u0432\u0435\u043D\u044C)</font>.").toString());
      }
      else {
        tb.append("&nbsp;&nbsp;\u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u043E.");
      }
    } catch (SQLException e) {
      System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, getItemFrom() error: ").append(e).toString());
    } finally {
      Close.CSR(con, st, rs);
    }

    tb.append("<br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private void getHeroFrom(L2PcInstance player, int id, String tpwd) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT * FROM `z_bbs_auction` WHERE `id`=? LIMIT 1");
      st.setInt(1, id);
      rs = st.executeQuery();
      if (rs.next()) {
        String name = rs.getString("itemName");
        int owner = rs.getInt("ownerId");
        int price = rs.getInt("price");
        int money = rs.getInt("money");
        long hero_expire = rs.getLong("shadow");
        int type = rs.getInt("type");
        int pay = rs.getInt("pay");
        String pwd = rs.getString("pwd");
        if ((owner != player.getObjectId()) && (!pwd.equals(" ")) && 
          (!tpwd.equals(pwd))) { tb.append("&nbsp;&nbsp;\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 \u043F\u0430\u0440\u043E\u043B\u044C.");
          separateAndSend(tb.toString(), player);
          return;
        }
        if ((hero_expire > 0L) && (hero_expire != 3L)) {
          if (!transferPay(con, owner, name, 0, 0, 0, price, money, player, pay, type)) { tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C.");
            separateAndSend(tb.toString(), player);
            return; }
          if ((pay == 0) && (owner == player.getObjectId()) && 
            (Config.BBS_AUC_HERO_COIN > 0)) {
            player.addItem("auc.return", Config.BBS_AUC_HERO_COIN, Config.BBS_AUC_HERO_PRICE, player, true);
          }

          Close.SR(st, rs);

          player.setHero(true);
          player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
          player.broadcastUserInfo();

          st = con.prepareStatement("UPDATE `characters` SET `hero`=? WHERE `obj_Id`=?");
          st.setLong(1, hero_expire);
          st.setInt(2, player.getObjectId());
          st.execute();
          Close.S(st); } else { tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430.");
          separateAndSend(tb.toString(), player);
          return; }
        st = con.prepareStatement("DELETE FROM `z_bbs_auction` WHERE `id`=?");
        st.setInt(1, id);
        st.executeUpdate();
        if (owner == player.getObjectId())
          tb.append(new StringBuilder().append("&nbsp;&nbsp;\u0412\u044B \u0441\u043D\u043E\u0432\u0430 \u0433\u0435\u0440\u043E\u0439, ").append(name).append("</font>").toString());
        else
          tb.append(new StringBuilder().append("&nbsp;&nbsp;\u041A\u0443\u043F\u043B\u0435\u043D\u043E \u0433\u0435\u0440\u043E\u0439\u0441\u0442\u0432\u043E: <font color=bef574>").append(name).append("</font>.").toString());
      }
      else {
        tb.append("&nbsp;&nbsp;\u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u043E.");
      }
    } catch (SQLException e) {
      System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, getItemFrom() error: ").append(e).toString());
    } finally {
      Close.CSR(con, st, rs);
    }

    tb.append("<br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private void addAugTo(L2PcInstance player, int item_id, String tpwd) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    int id = player.getBriefItem();
    if ((id == 0) || (id > 1000000)) {
      tb.append("&nbsp;&nbsp;\u041F\u0440\u0435\u0434\u043C\u0435\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.");
      separateAndSend(tb.toString(), player);
      return;
    }

    L2ItemInstance item = player.getInventory().getItemByObjectId(item_id);
    if (item == null) {
      tb.append("&nbsp;&nbsp;\u041F\u0440\u0435\u0434\u043C\u0435\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.");
      separateAndSend(tb.toString(), player);
      return;
    }

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT * FROM `z_bbs_auction` WHERE `id`=? LIMIT 1");
      st.setInt(1, id);
      rs = st.executeQuery();
      if (rs.next()) {
        String name = rs.getString("itemName");
        int aug_id = rs.getInt("augment");
        int aug_lvl = rs.getInt("augLvl");
        int aug_hex = rs.getInt("augAttr");
        int owner = rs.getInt("ownerId");
        int price = rs.getInt("price");
        int money = rs.getInt("money");
        int type = rs.getInt("type");
        int pay = rs.getInt("pay");
        String pwd = rs.getString("pwd");
        if ((owner != player.getObjectId()) && (!pwd.equals(" ")) && 
          (!tpwd.equals(pwd))) { tb.append("&nbsp;&nbsp;\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 \u043F\u0430\u0440\u043E\u043B\u044C.");
          separateAndSend(tb.toString(), player);
          return;
        }
        if (aug_id > 0) {
          L2Skill aug = SkillTable.getInstance().getInfo(aug_id, aug_lvl);
          if (aug == null) { tb.append("&nbsp;&nbsp;\u041F\u0440\u0435\u0434\u043C\u0435\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.");
            separateAndSend(tb.toString(), player);
            return; } if (!transferPay(con, owner, name, 0, aug_id, aug_lvl, price, money, player, pay, type)) { tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C.");
            separateAndSend(tb.toString(), player);
            return; }
          if ((pay == 0) && (owner == player.getObjectId()) && 
            (Config.BBS_AUC_AUG_COIN > 0)) {
            player.addItem("auc.return", Config.BBS_AUC_AUG_COIN, Config.BBS_AUC_AUG_PRICE, player, true);
          }

          item.setAugmentation(new L2Augmentation(item, aug_hex, aug_id, aug_lvl, true));
          player.sendItems(true);
          player.sendPacket(new SystemMessage(SystemMessageId.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED)); } else { tb.append("&nbsp;&nbsp;\u041F\u0440\u0435\u0434\u043C\u0435\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.");
          separateAndSend(tb.toString(), player);
          return;
        }
        Close.SR(st, rs);
        st = con.prepareStatement("DELETE FROM `z_bbs_auction` WHERE `id`=?");
        st.setInt(1, id);
        st.executeUpdate();
        if (owner == player.getObjectId())
          tb.append("&nbsp;&nbsp;\u0417\u0430\u0431\u0440\u0430\u043B\u0438.");
        else
          tb.append("&nbsp;&nbsp;\u0410\u0443\u0433\u043C\u0435\u043D\u0442 \u043A\u0443\u043F\u043B\u0435\u043D.");
      }
      else {
        tb.append("&nbsp;&nbsp;\u041F\u0438\u0441\u044C\u043C\u043E \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u043E.");
      }
    } catch (SQLException e) {
      System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, addAugTo() error: ").append(e).toString());
    } finally {
      Close.CSR(con, st, rs);
    }

    tb.append("<br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private void addToAuc(L2PcInstance player, int item_obj, int item_type, int item_price, String price_type, int pay_type, String pwd, int use_pwd) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getMenu());

    if ((use_pwd == 1) && (pwd.length() < 3)) {
      tb.append("&nbsp;&nbsp;\u041F\u0430\u0440\u043E\u043B\u044C \u0434\u043E\u043B\u0436\u0435\u043D \u0431\u044B\u0442\u044C \u0431\u043E\u043B\u0435\u0435 3 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432.");
      separateAndSend(tb.toString(), player);
      return;
    }
    if (item_price <= 0) {
      tb.append("&nbsp;&nbsp;\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
      separateAndSend(tb.toString(), player);
      return;
    }

    int coin_id = 0;
    int coin_price = 0;
    String coin_name = "";
    if (pay_type == 0) {
      switch (item_type) {
      case 0:
      case 1:
      case 2:
        coin_id = Config.BBS_AUC_ITEM_COIN;
        coin_price = Config.BBS_AUC_ITEM_PRICE;
        coin_name = Config.BBS_AUC_ITEM_NAME;
        break;
      case 3:
      case 4:
        coin_id = Config.BBS_AUC_AUG_COIN;
        coin_price = Config.BBS_AUC_AUG_PRICE;
        coin_name = Config.BBS_AUC_AUG_NAME;
        break;
      case 5:
        coin_id = Config.BBS_AUC_SKILL_COIN;
        coin_price = Config.BBS_AUC_SKILL_PRICE;
        coin_name = Config.BBS_AUC_SKILL_NAME;
        break;
      case 6:
        coin_id = Config.BBS_AUC_HERO_COIN;
        coin_price = Config.BBS_AUC_HERO_PRICE;
        coin_name = Config.BBS_AUC_HERO_NAME;
      }

      if (coin_id > 0) {
        L2ItemInstance coin = player.getInventory().getItemByItemId(coin_id);
        if ((coin == null) || (coin.getCount() < coin_price)) {
          tb.append(new StringBuilder().append("<br><br>&nbsp;&nbsp;\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u0443\u0441\u043B\u0443\u0433\u0438: ").append(coin_price).append(" ").append(coin_name).append(".").toString());
          separateAndSend(tb.toString(), player);
          return;
        }
      }
    }

    L2ItemInstance item = null;
    if ((item_type >= 0) && (item_type <= 4)) {
      item = player.getInventory().getItemByObjectId(item_obj);
      if ((item == null) || (item.isEquipped())) {
        tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 1.");
        separateAndSend(tb.toString(), player);
        return;
      }
      if (item.getOwnerId() != player.getObjectId()) {
        tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 6.");
        separateAndSend(tb.toString(), player);
        return;
      }
    }

    int money = getMoneyId(price_type);
    if (money == 0) {
      tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 2.");
      separateAndSend(tb.toString(), player);
      return;
    }

    int item_id = 0;
    int item_ench = 0;
    int item_type2 = 0;
    String item_name = "";

    int aug_id = 0;
    int aug_lvl = 0;
    int aug_hex = 0;

    long hero_expire = 0L;

    switch (item_type) {
    case 0:
    case 1:
    case 2:
      item_ench = item.getEnchantLevel();
      if (item_ench == 0) {
        tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 3.");
        separateAndSend(tb.toString(), player);
        return;
      }
      item_id = item.getItemId();
      item_name = item.getItem().getName();
      if (!item.isAugmented()) break;
      aug_hex = item.getAugmentation().getAugmentationId();
      if (item.getAugmentation().getAugmentSkill() == null) break;
      L2Skill augment = item.getAugmentation().getAugmentSkill();
      aug_id = augment.getId();
      aug_lvl = augment.getLevel();
      break;
    case 3:
    case 4:
      if ((!item.isAugmented()) || (item.getAugmentation().getAugmentSkill() == null)) {
        tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 4.");
        separateAndSend(tb.toString(), player);
        return;
      }
      L2Skill augment = item.getAugmentation().getAugmentSkill();
      aug_id = augment.getId();
      aug_lvl = augment.getLevel();
      aug_hex = item.getAugmentation().getAugmentationId();
      item_id = 0;
      item_ench = 0;
      if (item_type == 4) {
        item_name = augment.getName();
      } else {
        item_id = item.getItemId();
        item_ench = item.getEnchantLevel();
        item_name = item.getItem().getName();
      }
      break;
    case 5:
      if (player.getKnownSkill(item_obj) == null) {
        tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 7.");
        separateAndSend(tb.toString(), player);
        return;
      }
      Connect con = null;
      PreparedStatement st = null;
      ResultSet rs = null;
      try {
        con = L2DatabaseFactory.getInstance().getConnection();
        st = con.prepareStatement("SELECT skill_lvl FROM z_donate_skills WHERE char_id=? AND skill_id=?");
        st.setInt(1, player.getObjectId());
        st.setInt(2, item_obj);
        rs = st.executeQuery();
        if (rs.next()) {
          item_id = item_obj;
          item_ench = rs.getInt("skill_lvl");
          L2Skill skill = SkillTable.getInstance().getInfo(item_id, item_ench);
          if (skill == null) { tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 10.");
            separateAndSend(tb.toString(), player);
            return; } item_name = skill.getName(); } else { tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 8.");
          separateAndSend(tb.toString(), player);
          return; }
      } catch (SQLException e) {
        System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, addToAuc() error: ").append(e).toString());
      } finally {
        Close.CSR(con, st, rs);
      }
      break;
    case 6:
      if (!player.isHero()) {
        tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 8.");
        separateAndSend(tb.toString(), player);
        return;
      }
      Connect con2 = null;
      PreparedStatement st2 = null;
      ResultSet rs2 = null;
      try {
        con2 = L2DatabaseFactory.getInstance().getConnection();
        st2 = con2.prepareStatement("SELECT hero FROM characters WHERE obj_Id=? LIMIT 1");
        st2.setInt(1, player.getObjectId());
        rs2 = st2.executeQuery();
        if (rs2.next()) {
          hero_expire = rs2.getLong("hero");
          if (hero_expire == 1L)
            item_name = "Hero: \u0412\u0435\u0447\u043D\u043E\u0435";
          else
            item_name = new StringBuilder().append("Hero: ").append(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(hero_expire))).append("").toString(); 
        } else { tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 9.");
          separateAndSend(tb.toString(), player);
          return;
        }
      } catch (SQLException e) {
        System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, addToAuc() error: ").append(e).toString());
      } finally {
        Close.CSR(con2, st2, rs2);
      }

    }

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      switch (item_type) {
      case 5:
        st = con.prepareStatement("DELETE FROM z_donate_skills WHERE char_id=? AND skill_id=?");
        st.setInt(1, player.getObjectId());
        st.setInt(2, item_obj);
        st.execute();
        Close.S(st);
        break;
      case 6:
        st = con.prepareStatement("UPDATE `characters` SET `hero`=? WHERE `obj_Id`=?");
        st.setInt(1, 0);
        st.setInt(2, player.getObjectId());
        st.execute();
        Close.S(st);
      }

      st = con.prepareStatement("INSERT INTO `z_bbs_auction` (`id`,`itemId`,`itemName`,`enchant`,`augment`,`augAttr`,`augLvl`,`price`,`money`,`type`,`ownerId`,`shadow`,`pay`,`pwd`,`expire`) VALUES (NULL,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
      st.setInt(1, item_id);
      st.setString(2, item_name);
      st.setInt(3, item_ench);
      st.setInt(4, aug_id);
      st.setInt(5, aug_hex);
      st.setInt(6, aug_lvl);
      st.setInt(7, item_price);
      st.setInt(8, money);
      st.setInt(9, item_type);
      st.setInt(10, player.getObjectId());
      st.setLong(11, hero_expire);
      st.setInt(12, pay_type);
      st.setString(13, use_pwd == 0 ? " " : pwd);
      st.setLong(14, System.currentTimeMillis() + TimeUnit.DAYS.toMillis(Config.BBS_AUC_EXPIRE_DAYS));
      st.executeUpdate(); } catch (SQLException e) { System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, addToAuc() error: ").append(e).toString());
      tb.append("&nbsp;&nbsp;\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430 5.");
      separateAndSend(tb.toString(), player);
      return; } finally { Close.CS(con, st);
    }

    if (pay_type == 0) {
      player.destroyItemByItemId("auc", coin_id, coin_price, player, true);
    }

    switch (item_type) {
    case 0:
    case 1:
    case 2:
    case 3:
      player.destroyItem("addToAuc", item, player, true);
      break;
    case 4:
      item.removeAugmentation();
      player.sendItems(false);
      break;
    case 5:
      player.removeSkill(player.getKnownSkill(item_obj));
      player.sendSkillList();
      break;
    case 6:
      player.setHero(false);
      player.setHeroExpire(0L);
      player.broadcastUserInfo();
    }

    tb.append(new StringBuilder().append("<br>&nbsp;&nbsp;\u041B\u043E\u0442: ").append(item_name).append(" ").append(item_ench == 0 ? "" : new StringBuilder().append("+").append(item_ench).append("").toString()).append(" ").append(item_id != 0 ? getAugmentSkill(aug_id, aug_lvl) : "").append(" \u0432\u044B\u0441\u0442\u0430\u0432\u043B\u0435\u043D \u043D\u0430 \u0430\u0443\u043A\u0446\u0438\u043E\u043D.").toString());
    if (use_pwd == 1) {
      tb.append(new StringBuilder().append("<br>&nbsp;&nbsp;\u0412\u0430\u0448 \u043F\u0430\u0440\u043E\u043B\u044C: <font color=LEVEL>").append(pwd).append("</font>.").toString());
    }
    tb.append(new StringBuilder().append("<br>&nbsp;&nbsp;<font color=669966>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: ").append(Util.formatAdena(item_price)).append(" ").append(getMoneyCall(money)).append("</font><br>").toString());

    tb.append("<br></body></html>");
    separateAndSend(tb.toString(), player);
  }

  private boolean transferPay(Connect con, int charId, String name, int enchant, int augId, int augLvl, int price, int money, L2PcInstance player, int pay, int type) {
    if (charId == player.getObjectId()) {
      return true;
    }

    if (price <= 0)
    {
      return false;
    }
    L2ItemInstance coin2;
    if (pay == 1) {
      int coin_id = 0;
      int coin_price = 0;
      switch (type) {
      case 0:
      case 1:
      case 2:
        coin_id = Config.BBS_AUC_ITEM_COIN;
        coin_price = Config.BBS_AUC_ITEM_PRICE;
        break;
      case 3:
      case 4:
        coin_id = Config.BBS_AUC_AUG_COIN;
        coin_price = Config.BBS_AUC_AUG_PRICE;
        break;
      case 5:
        coin_id = Config.BBS_AUC_SKILL_COIN;
        coin_price = Config.BBS_AUC_SKILL_PRICE;
        break;
      case 6:
        coin_id = Config.BBS_AUC_HERO_COIN;
        coin_price = Config.BBS_AUC_HERO_PRICE;
      }

      if (coin_id > 0) {
        if (coin_id == money) {
          coin_price += price;

          L2ItemInstance coin = player.getInventory().getItemByItemId(coin_id);
          if ((coin == null) || (coin.getCount() < coin_price)) {
            return false;
          }
          player.destroyItemByItemId("auc1", coin_id, coin_price, player, true);
        } else {
          L2ItemInstance coin = player.getInventory().getItemByItemId(coin_id);
          if ((coin == null) || (coin.getCount() < coin_price)) {
            return false;
          }

          coin2 = player.getInventory().getItemByItemId(money);
          if ((coin2 == null) || (coin2.getCount() < price)) {
            return false;
          }

          player.destroyItemByItemId("auc1", money, price, player, true);
          player.destroyItemByItemId("auc1", coin_id, coin_price, player, true);
        }
      } else {
        L2ItemInstance coin = player.getInventory().getItemByItemId(money);
        if ((coin == null) || (coin.getCount() < price)) {
          return false;
        }
        player.destroyItemByItemId("auc1", money, price, player, true);
      }
    } else {
      L2ItemInstance coin = player.getInventory().getItemByItemId(money);
      if ((coin == null) || (coin.getCount() < price)) {
        return false;
      }
      player.destroyItemByItemId("auc1", money, price, player, true);
    }

    TextBuilder text = new TextBuilder();
    text.append(new StringBuilder().append("").append(name).append("<br1> \u0431\u044B\u043B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u043F\u0440\u043E\u0434\u0430\u043D.<br>").toString());
    text.append("\u0411\u043B\u0430\u0433\u043E\u0434\u0430\u0440\u0438\u043C \u0437\u0430 \u0441\u043E\u0442\u0440\u0443\u0434\u043D\u0438\u0447\u0435\u0441\u0442\u0432\u043E.");
    PreparedStatement st = null;
    try {
      st = con.prepareStatement("INSERT INTO `z_bbs_mail` (`id`,`from`,`to`,`tema`,`text`,`datetime`,`read`,`item_id`,`item_count`,`item_ench`,`aug_hex`,`aug_id`,`aug_lvl`) VALUES (NULL,?,?,?,?,?,?,?,?,?,?,?,?)");
      st.setInt(1, 777);
      st.setInt(2, charId);
      st.setString(3, "\u0412\u0430\u0448 \u043B\u043E\u0442 \u043F\u0440\u043E\u0434\u0430\u043D");
      st.setString(4, text.toString());
      st.setLong(5, System.currentTimeMillis());
      st.setInt(6, 0);
      st.setInt(7, money);
      st.setInt(8, price);
      st.setInt(9, 0);
      st.setInt(10, 0);
      st.setInt(11, 0);
      st.setInt(12, 0);
      st.execute();
      L2PcInstance trg = L2World.getInstance().getPlayer(getSellerName(con, charId));
      if (trg != null) {
        trg.sendPacket(new ExMailArrived());
        trg.sendMessage("\u0423\u0432\u0435\u0434\u043E\u043C\u043B\u0435\u043D\u0438\u0435 \u0441 \u0430\u0443\u043A\u0446\u0438\u043E\u043D\u0430: \u043F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u043F\u043E\u0447\u0442\u0443.");
      }
      coin2 = 1;
      return coin2;
    }
    catch (SQLException e)
    {
      System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, transferPay() error: ").append(e).toString());
    } finally {
      text.clear();
      Close.S(st);
    }
    return false;
  }

  private String getMoneyCall(int money) {
    return (String)moneys.get(Integer.valueOf(money));
  }

  private int getMoneyId(String price_type) {
    FastMap.Entry e = moneys.head(); for (FastMap.Entry end = moneys.tail(); (e = e.getNext()) != end; ) {
      Integer key = (Integer)e.getKey();
      String value = (String)e.getValue();
      if (key == null)
      {
        continue;
      }
      if (value.trim().equals(price_type)) {
        return key.intValue();
      }
    }
    return 0;
  }

  private void cacheMoneys() {
    TextBuilder text = new TextBuilder();
    FastMap.Entry e = moneys.head(); for (FastMap.Entry end = moneys.tail(); (e = e.getNext()) != end; ) {
      String value = (String)e.getValue();
      text.append(new StringBuilder().append(value).append(";").toString());
    }
    MONEY_VARS = text.toString();
    text.clear();
  }

  private void cacheMenu() {
    TextBuilder tb = new TextBuilder();
    tb.append(getPwHtm("menu"));
    tb.append("<html><body><table width=280><tr><td align=right><button value=\"\u0413\u043B\u0430\u0432\u043D\u0430\u044F\" action=\"bypass _bbsauc\" width=70 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td align=right><button value=\"\u041B\u0438\u0447\u043D\u044B\u0439 \u043A\u0430\u0431\u0438\u043D\u0435\u0442\" action=\"bypass _bbsauc_office\" width=86 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td align=right><button value=\"\u0414\u043E\u0431\u0430\u0432\u0438\u0442\u044C\" action=\"bypass _bbsauc_add\" width=70 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td align=right><button value=\"\u041F\u043E\u0438\u0441\u043A\" action=\"bypass _bbsauc_fsearch\" width=40 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br>");
    AUC_MENU = tb.toString();
    tb.clear();
  }

  private int getPageCount(Connect con, int me, int itemId, int augment, int type2, int charId) {
    int rowCount = 0;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      if (type2 >= 0) {
        st = con.prepareStatement("SELECT COUNT(`id`) FROM `z_bbs_auction` WHERE `type` = ?");
        st.setInt(1, type2);
      } else if (itemId > 0) {
        st = con.prepareStatement("SELECT COUNT(`id`) FROM `z_bbs_auction` WHERE `itemId` = ?");
        st.setInt(1, itemId);
      } else if (augment > 0) {
        st = con.prepareStatement("SELECT COUNT(`id`) FROM `z_bbs_auction` WHERE `augment` = ?");
        st.setInt(1, augment);
      } else if (me == 1) {
        st = con.prepareStatement("SELECT COUNT(`id`) FROM `z_bbs_auction` WHERE `ownerId` = ?");
        st.setInt(1, charId);
      } else {
        st = con.prepareStatement("SELECT COUNT(`id`) FROM `z_bbs_auction` WHERE `id` > 0");
      }

      rs = st.executeQuery();
      if (rs.next())
        rowCount = rs.getInt(1);
    }
    catch (SQLException e) {
      System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, getPageCount() error: ").append(e).toString());
    } finally {
      Close.SR(st, rs);
    }
    if (rowCount == 0) {
      return 0;
    }

    return rowCount / 20 + 1;
  }

  private String sortPages(int page, int pages, int me, int itemId, int augment, int type2) {
    TextBuilder text = new TextBuilder("<br>\u0421\u0442\u0440\u0430\u043D\u0438\u0446\u044B:<br1><table width=300><tr>");
    int step = 1;
    int s = page - 3;
    int f = page + 3;
    if ((page < 8) && (s < 8)) {
      s = 1;
    }
    if (page >= 8) {
      text.append(new StringBuilder().append("<td><a action=\"bypass _bbsauc_pageshow_").append(s).append("_").append(me).append("_").append(itemId).append("_").append(augment).append("_").append(type2).append("\"> ... </a></td>").toString());
    }
    for (int i = s; i <= pages; i++) {
      int al = i + 1;
      if (i == page) {
        text.append(new StringBuilder().append("<td>").append(i).append("</td>").toString());
      }
      else if (al <= pages) {
        text.append(new StringBuilder().append("<td><a action=\"bypass _bbsauc_pageshow_").append(i).append("_").append(me).append("_").append(itemId).append("_").append(augment).append("_").append(type2).append("\">").append(i).append("</a></td>").toString());
      }

      if ((step == 8) && (f < pages)) {
        if (al >= pages) break;
        text.append(new StringBuilder().append("<td><a action=\"bypass _bbsauc_pageshow_").append(al).append("_").append(me).append("_").append(itemId).append("_").append(augment).append("_").append(type2).append("\"> ... </a></td>").toString()); break;
      }

      step++;
    }
    text.append("</tr></table><br>");
    return text.toString();
  }

  private String getAugmentSkill(int skillId, int skillLvl) {
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

  private String getSellerName(Connect con, int objId) {
    PreparedStatement st = null;
    ResultSet rset = null;
    try {
      st = con.prepareStatement("SELECT char_name FROM `characters` WHERE `obj_Id` = ? LIMIT 0,1");
      st.setInt(1, objId);
      rset = st.executeQuery();
      if (rset.next()) {
        String str = rset.getString("char_name");
        return str;
      }
    }
    catch (Exception e)
    {
      System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, getCharName() error: ").append(e).toString());
    } finally {
      Close.SR(st, rset);
    }
    return "???";
  }

  private static String getMenu() {
    return AUC_MENU;
  }

  private void returnExpiredLots() {
    Connect con = null;
    PreparedStatement st = null;
    PreparedStatement st2 = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("SELECT id, itemId, itemName, enchant, augment, augAttr, augLvl, price, money, type, ownerId, shadow, pay, pwd, expire FROM `z_bbs_auction` WHERE `expire` < ?");
      st.setLong(1, System.currentTimeMillis());
      rs = st.executeQuery();
      while (rs.next()) {
        int type = rs.getInt("type");
        if (type > 3) {
          continue;
        }
        int id = rs.getInt("id");
        int itmId = rs.getInt("itemId");
        String name = rs.getString("itemName");
        int ownerId = rs.getInt("ownerId");
        int augAttr = rs.getInt("augAttr");
        int augId = rs.getInt("augment");
        int augLvl = rs.getInt("augLvl");
        int enchant = rs.getInt("enchant");
        int pay = rs.getInt("pay");

        st2 = con.prepareStatement("DELETE FROM `z_bbs_auction` WHERE `id`=?");
        st2.setInt(1, id);
        st2.execute();
        Close.S(st2);

        st2 = con.prepareStatement("INSERT INTO `z_bbs_mail` (`from`, `to`, `tema`, `text`, `datetime`, `read`, `item_id`, `item_count`, `item_ench`, `aug_hex`, `aug_id`, `aug_lvl`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        st2.setInt(1, 777);
        st2.setInt(2, ownerId);
        st2.setString(3, "\u0412\u043E\u0437\u0432\u0440\u0430\u0442");
        st2.setString(4, "\u0418\u0442\u0435\u043C \u0432\u043E\u0437\u0432\u0440\u0430\u0449\u0435\u043D.");
        st2.setLong(5, System.currentTimeMillis());
        st2.setInt(6, 0);
        st2.setInt(7, itmId);
        st2.setInt(8, 1);
        st2.setInt(9, enchant);
        st2.setInt(10, augAttr);
        st2.setInt(11, augId);
        st2.setInt(12, augLvl);
        st2.execute();
        Close.S(st2);

        if (pay == 0) {
          int coin_id = Config.BBS_AUC_ITEM_COIN;
          int coin_price = Config.BBS_AUC_ITEM_PRICE;
          if (type == 3) {
            coin_id = Config.BBS_AUC_AUG_COIN;
            coin_price = Config.BBS_AUC_AUG_PRICE;
          }
          if (coin_id > 0) {
            st2 = con.prepareStatement("INSERT INTO `z_bbs_mail` (`from`, `to`, `tema`, `text`, `datetime`, `read`, `item_id`, `item_count`, `item_ench`, `aug_hex`, `aug_id`, `aug_lvl`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            st2.setInt(1, 777);
            st2.setInt(2, ownerId);
            st2.setString(3, "\u0412\u043E\u0437\u0432\u0440\u0430\u0442");
            st2.setString(4, "\u0418\u0442\u0435\u043C \u0432\u043E\u0437\u0432\u0440\u0430\u0449\u0435\u043D.");
            st2.setLong(5, System.currentTimeMillis());
            st2.setInt(6, 0);
            st2.setInt(7, coin_id);
            st2.setInt(8, coin_price);
            st2.setInt(9, 0);
            st2.setInt(10, 0);
            st2.setInt(11, 0);
            st2.setInt(12, 0);
            st2.execute();
            Close.S(st2);
          }
        }
      }
    } catch (SQLException e) {
      System.out.println(new StringBuilder().append("[ERROR] AuctionBBSManager, showSellItems() error: ").append(e).toString());
    } finally {
      Close.CSR(con, st, rs);
    }
  }

  public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance player)
  {
  }
}