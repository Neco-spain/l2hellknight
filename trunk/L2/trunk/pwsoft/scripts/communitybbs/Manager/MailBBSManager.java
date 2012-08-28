package scripts.communitybbs.Manager;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import javolution.text.TextBuilder;
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
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Log;
import net.sf.l2j.util.TimeLogger;

public class MailBBSManager extends BaseBBSManager
{
  private static MailBBSManager _instance;
  private static final int PAGE_LIMIT = 30;
  private static Date _date = new Date();
  private static SimpleDateFormat _sdf = new SimpleDateFormat("yyyy.MM.dd");
  private static SimpleDateFormat _sdfTime = new SimpleDateFormat("HH:mm");
  private static SimpleDateFormat _sdfDate = new SimpleDateFormat("d MMM.");

  public static void init()
  {
    _instance = new MailBBSManager();
  }

  public static MailBBSManager getInstance() {
    return _instance;
  }

  public void parsecmd(String command, L2PcInstance player)
  {
    if (command.equalsIgnoreCase("_bbsmail")) {
      showIndex(player, 1, 1);
    } else if (command.startsWith("_bbsmail_menu")) {
      String[] opaopa = command.substring(14).split("_");

      switch (Integer.parseInt(opaopa[0])) {
      case 1:
        int itemObj = Integer.parseInt(opaopa[1]);
        if ((itemObj > 0) && (player.getBriefItem() == 0)) {
          player.setBriefItem(itemObj);
        }

        showNewBrief(player);
        break;
      case 2:
        showIndex(player, 1, Integer.parseInt(opaopa[2]));
        break;
      case 3:
        showIndex(player, 2, Integer.parseInt(opaopa[2]));
        break;
      case 4:
        break;
      case 5:
        addAugTo(player, Integer.parseInt(opaopa[1]));
      }
    }
    else if (command.startsWith("_bbsmail_show")) {
      showBrief(player, Integer.parseInt(command.substring(13).trim()));
    } else if (command.startsWith("_bbsmail_send")) {
      Integer act = Integer.valueOf(Integer.parseInt(command.substring(13, 15).trim()));
      switch (act.intValue()) {
      case 1:
        try {
          String[] data = command.substring(16).split(" _ ");
          String target = data[0];
          String tema = data[1];
          String text = data[2];
          sendBrief(player, target, tema, text);
        } catch (Exception e) {
          TextBuilder tb = new TextBuilder("<html><body><br>\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430.<br></body></html>");
          separateAndSend(tb.toString(), player);
          tb.clear();
          tb = null;
        }

      case 2:
        showAddItem(player);
      }
    }
    else if (command.startsWith("_bbsmail_act")) {
      String[] data = command.split("_");
      int act = Integer.parseInt(data[3]);
      int id = Integer.parseInt(data[4]);
      switch (act) {
      case 1:
        showNewBrief(player);
        break;
      case 2:
        deleteBrief(player, id, false);
        break;
      case 3:
        getItemFrom(player, id);
        break;
      case 4:
        deleteBrief(player, id, true);
        break;
      case 5:
        getAugFrom(player, id);
      }
    }
    else if (command.startsWith("_bbsmail_search")) {
      try {
        String[] data = command.substring(16).split(" _ ");
        String type = data[0];
        String word = data[1];
        showSearchResult(player, word, type);
      } catch (Exception e) {
        TextBuilder tb = new TextBuilder("<html><body><br>\u041E\u0448\u0438\u0431\u043A\u0430 \u043F\u0440\u0438 \u043E\u0431\u0440\u0430\u0431\u043E\u0442\u043A\u0435 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.<br></body></html>");
        separateAndSend(tb.toString(), player);
        tb.clear();
        tb = null;
      }
    }
  }

  private void showNewBrief(L2PcInstance player) {
    String ansSend = player.getBriefSender();
    String ansThem = player.getMailTheme();
    TextBuilder tb = new TextBuilder("");
    tb.append(new StringBuilder().append("<html><body><br><br><center><table width=650 border=0><tr><td><img src=\"Icon.etc_letter_envelope_i00\" width=32 height=32><font color=CC9933>@</font><font color=CC00FF>").append(player.getName()).append("</font></td>").toString());
    tb.append("<td width=480><table width=250><tr><td><edit var=\"search\" width=150 length=\"22\"></td><td><combobox width=51 var=keyword list=\"\u0422\u0435\u043C\u0430;\u0410\u0432\u0442\u043E\u0440\"></td>");
    tb.append("<td><button value=\"\u041D\u0430\u0439\u0442\u0438\" action=\"bypass _bbsmail_search $search _ $keyword\" width=40 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></td></tr>");
    tb.append("<tr><td><br><br><table width=100><tr><td><button value=\"\u041D\u043E\u0432\u043E\u0435 \u043F\u0438\u0441\u044C\u043C\u043E\" action=\"bypass _bbsmail_menu 1_0\" width=\"100\" height=\"14\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br></td>");
    tb.append("</tr><tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 2_0_1\">\u0412\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a></td></tr>");
    tb.append("<tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 3_0_1\">\u0418\u0441\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a><br></td></tr>");

    tb.append("</table></td>");
    tb.append("<td width=480>");

    tb.append("<font color=00CC99>\u0421\u043E\u0432\u0435\u0442: \u0415\u0441\u043B\u0438 \u0432\u044B \u0441\u043E\u0431\u0438\u0440\u0430\u0435\u0442\u0435\u0441\u044C \u043E\u0442\u043F\u0440\u0430\u0432\u0438\u0442\u044C \u043F\u0440\u0435\u0434\u043C\u0435\u0442, \u0442\u043E \u0441\u043D\u0430\u0447\u0430\u043B\u0430 \u043F\u0440\u0438\u043A\u0440\u0435\u043F\u0438\u0442\u0435 \u0435\u0433\u043E.<br>");
    tb.append(new StringBuilder().append("\u041E\u0442\u043F\u0440\u0430\u0432\u043A\u0430 \u043F\u0438\u0441\u044C\u043C\u0430: ").append(Config.EXPOSTB_PRICE).append(" ").append(Config.EXPOSTB_NAME).append(".<br1>").toString());
    tb.append(new StringBuilder().append("\u041E\u0442\u043F\u0440\u0430\u0432\u043A\u0430 \u043F\u0440\u0435\u0434\u043C\u0435\u0442\u0430: ").append(Config.EXPOSTA_PRICE).append(" ").append(Config.EXPOSTA_NAME).append(".</font><br>").toString());
    tb.append(new StringBuilder().append("<table width=370><tr><td width=10> </td><td>\u041A\u043E\u043C\u0443: ").append(ansSend.equals("n.a") ? "" : new StringBuilder().append("(").append(ansSend).append(")").toString()).append("</td><td><edit var=\"target\" width=150 length=\"22\"><br></td></tr>").toString());
    tb.append(new StringBuilder().append("<tr><td width=10> </td><td>\u0422\u0435\u043C\u0430: ").append(ansThem.equals("n.a") ? "" : new StringBuilder().append("(Re: ").append(ansThem).append(")").toString()).append("</td><td><edit var=\"tema\" width=150 length=\"22\"><br></td></tr>").toString());
    if (player.getBriefItem() == 0) {
      tb.append("<tr><td width=10> </td><td></td><td><a action=\"bypass _bbsmail_send 2\">$\u041F\u0440\u0438\u043A\u0440\u0435\u043F\u0438\u0442\u044C \u043F\u0440\u0435\u0434\u043C\u0435\u0442</a><br></td></tr></table>");
    } else {
      L2ItemInstance item = player.getInventory().getItemByObjectId(player.getBriefItem());
      if (item == null) {
        tb.append("<tr><td width=10> </td><td></td><td><a action=\"bypass _bbsmail_send 2\">$\u041F\u0440\u0438\u043A\u0440\u0435\u043F\u0438\u0442\u044C \u043F\u0440\u0435\u0434\u043C\u0435\u0442</a><br></td></tr></table>");
      } else {
        tb.append(new StringBuilder().append("</table>\u041F\u0440\u0435\u0434\u043C\u0435\u0442:<br><table width=200><tr><td><img src=\"").append(item.getItem().getIcon()).append("\" width=32 height=32></td><td><font color=LEVEL>").append(item.getItem().getName()).append(" +").append(item.getEnchantLevel()).append("</font><br></td></tr></table><br>").toString());
        if ((item.isAugmented()) && (item.getAugmentation().getAugmentSkill() != null)) {
          tb.append(new StringBuilder().append("\u0410\u0443\u0433\u043C\u0435\u043D\u0442: ").append(getAugmentSkill(item.getAugmentation().getAugmentSkill())).append("</font><br>").toString());
        }
      }
    }
    tb.append("<table width=400><tr><td>\u0422\u0435\u043A\u0441\u0442:<multiedit var=\"text\" width=280 height=70><br></td></tr>");
    if (ansThem.equals("n.a"))
      tb.append("<tr><td><button value=\"\u041E\u0442\u043F\u0440\u0430\u0432\u0438\u0442\u044C\" action=\"bypass _bbsmail_send 1 $target _ $tema _ $text\" width=\"100\" height=\"14\" back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
    else {
      tb.append(new StringBuilder().append("<tr><td><button value=\"\u041E\u0442\u043F\u0440\u0430\u0432\u0438\u0442\u044C\" action=\"bypass _bbsmail_send 1 ").append(ansSend).append(" _ ").append(ansThem).append(" _ $text\" width=\"100\" height=\"14\" back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>").toString());
    }

    tb.append("</td></tr></table><br></body></html>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
  }

  private void showIndex(L2PcInstance player, int type, int page) {
    player.setBriefItem(0);
    player.setBriefSender("n.a");
    player.setMailTheme("n.a");
    TextBuilder tb = new TextBuilder("");
    tb.append(new StringBuilder().append("<html><body><br><br><center><table width=650 border=0><tr><td><img src=\"Icon.etc_letter_envelope_i00\" width=32 height=32><font color=CC9933>@</font><font color=CC00FF>").append(player.getName()).append("</font></td>").toString());
    tb.append("<td width=480><table width=250 border=0><tr><td><edit var=\"search\" width=150 length=\"22\"></td><td><combobox width=51 var=keyword list=\"\u0422\u0435\u043C\u0430;\u0410\u0432\u0442\u043E\u0440\"></td>");
    tb.append("<td><button value=\"\u041D\u0430\u0439\u0442\u0438\" action=\"bypass _bbsmail_search $search _ $keyword\" width=40 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></td></tr>");
    tb.append("<tr><td><br><br><table width=100><tr><td><button value=\"\u041D\u043E\u0432\u043E\u0435 \u043F\u0438\u0441\u044C\u043C\u043E\" action=\"bypass _bbsmail_menu 1_0\" width=\"100\" height=\"14\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br></td>");
    if (type == 1) {
      tb.append("</tr><tr><td>&nbsp;\u0412\u0445\u043E\u0434\u044F\u0449\u0438\u0435:</td></tr>");
      tb.append("<tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 3_0_1\">\u0418\u0441\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a><br></td></tr>");
    } else {
      tb.append("</tr><tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 2_0_1\">\u0412\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a></td></tr>");
      tb.append("<tr><td>&nbsp;\u0418\u0441\u0445\u043E\u0434\u044F\u0449\u0438\u0435:<br></td></tr>");
    }

    tb.append("</table></td>");
    tb.append(new StringBuilder().append("<td width=480><font color=00CC99>\u041E\u0442\u043F\u0440\u0430\u0432\u043A\u0430 \u043F\u0438\u0441\u044C\u043C\u0430: ").append(Config.EXPOSTB_PRICE).append(" ").append(Config.EXPOSTB_NAME).append(".<br1>").toString());
    tb.append(new StringBuilder().append("\u041E\u0442\u043F\u0440\u0430\u0432\u043A\u0430 \u043F\u0440\u0435\u0434\u043C\u0435\u0442\u0430: ").append(Config.EXPOSTA_PRICE).append(" ").append(Config.EXPOSTA_NAME).append(".</font><br>").toString());
    tb.append("<table width=500><tr><td></td><td></td><td></td><td></td></tr>");

    int limit1 = (page - 1) * 30;
    int limit2 = 30;
    int pageCount = 0;
    Date date = null;
    String dateNow = getNow();
    String briefDate = "";
    String loc = "to";

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      if (type == 2) {
        st = con.prepareStatement("SELECT * FROM `z_bbs_mail` WHERE `from` = ? ORDER BY `datetime` DESC LIMIT ?, ?");
      } else {
        st = con.prepareStatement("SELECT * FROM `z_bbs_mail` WHERE `to` = ? ORDER BY `datetime` DESC LIMIT ?, ?");
        loc = "from";
      }
      st.setInt(1, player.getObjectId());
      st.setInt(2, limit1);
      st.setInt(3, limit2);
      rset = st.executeQuery();
      while (rset.next()) {
        int id = rset.getInt("id");
        String tema = rset.getString("tema");
        long time = rset.getLong("datetime");
        int read = rset.getInt("read");
        String name = getCharName(con, rset.getInt(loc));
        int item_id = rset.getInt("item_id");

        date = new Date(time);
        briefDate = _sdf.format(date);
        if (briefDate.equals(dateNow))
          briefDate = _sdfTime.format(date);
        else {
          briefDate = _sdfDate.format(date);
        }

        tb.append(new StringBuilder().append("<tr><td width=16><img src=\"Icon.etc_letter_envelope_i00\" width=16 height=16></td><td align=left><font color=").append(read == 0 ? "CC00FF" : "6699CC").append("> ").append(name).append(" </td>").toString());
        tb.append(new StringBuilder().append("<td align=left><a action=\"bypass _bbsmail_show ").append(id).append("\"> ").append(item_id > 1 ? "$" : "").append(tema).append(" </a></td><td align=right> ").append(briefDate).append(" </font></td></tr>").toString());
      }
      pageCount = getPageCount(con, player.getObjectId(), type);
    } catch (Exception e) {
      System.out.println(new StringBuilder().append("[ERROR] MailBBSManager, showIndex(").append(player.getName()).append(", ").append(type).append(") ").append(e).toString());
      e.printStackTrace();
    } finally {
      Close.CSR(con, st, rset);
    }

    tb.append("</table><br>");
    if (pageCount > 30) {
      tb.append("&nbsp;&nbsp;\u0421\u0442\u0440\u0430\u043D\u0438\u0446\u044B: <br1>");
      int pages = pageCount / 30 + 1;
      for (int i = 0; i < pages; i++) {
        int cur = i + 1;
        if (page == cur)
          tb.append(new StringBuilder().append("&nbsp;&nbsp;").append(cur).append("&nbsp;&nbsp;").toString());
        else {
          tb.append(new StringBuilder().append("&nbsp;&nbsp;<a action=\"bypass _bbsmail_menu 2_0_").append(cur).append("\">").append(cur).append("</a>&nbsp;&nbsp;").toString());
        }
      }
    }
    tb.append("</td></tr></table><br></body></html>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
  }

  private int getPageCount(Connect con, int charId, int type) {
    int rowCount = 0;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      if (type == 2)
        st = con.prepareStatement("SELECT COUNT(`id`) FROM `z_bbs_mail` WHERE `from` = ?");
      else {
        st = con.prepareStatement("SELECT COUNT(`id`) FROM `z_bbs_mail` WHERE `to` = ?");
      }
      st.setInt(1, charId);
      rs = st.executeQuery();
      if (rs.next())
        rowCount = rs.getInt(1);
    }
    catch (Exception e) {
      System.out.println(new StringBuilder().append("[ERROR] MailBBSManager, getPageCount(con, ").append(charId).append(", ").append(type).append(") ").append(e).toString());
      e.printStackTrace();
    } finally {
      Close.SR(st, rs);
    }
    if (rowCount == 0) {
      return 0;
    }

    return rowCount / 30 + 1;
  }

  private void showAddItem(L2PcInstance player) {
    TextBuilder tb = new TextBuilder("");
    tb.append(new StringBuilder().append("<html><body><br><br><center><table width=650 border=0><tr><td><img src=\"Icon.etc_letter_envelope_i00\" width=32 height=32><font color=CC9933>@</font><font color=CC00FF>").append(player.getName()).append("</font></td>").toString());
    tb.append("<td width=480><table width=250><tr><td><edit var=\"search\" width=150 length=\"22\"></td><td><combobox width=51 var=keyword list=\"\u0422\u0435\u043C\u0430;\u0410\u0432\u0442\u043E\u0440\"></td>");
    tb.append("<td><button value=\"\u041D\u0430\u0439\u0442\u0438\" action=\"bypass _bbsmail_search $search _ $keyword\" width=40 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></td></tr>");
    tb.append("<tr><td><br><br><table width=100><tr><td><button value=\"\u041D\u043E\u0432\u043E\u0435 \u043F\u0438\u0441\u044C\u043C\u043E\" action=\"bypass _bbsmail_menu 1_0\" width=\"100\" height=\"14\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br></td>");
    tb.append("</tr><tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 2_0_1\">\u0412\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a></td></tr>");
    tb.append("<tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 3_0_1\">\u0418\u0441\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a><br></td></tr>");

    tb.append("</table></td>");
    tb.append("<td width=480>");

    tb.append("&nbsp;&nbsp;&nbsp;\u0412\u044B\u0431\u0435\u0440\u0438\u0442\u0435 \u043F\u0440\u0435\u0434\u043C\u0435\u0442, \u043A\u043E\u0442\u043E\u0440\u044B\u0439 \u0445\u043E\u0442\u0438\u0442\u0435 \u043F\u0440\u0438\u043A\u0440\u0435\u043F\u0438\u0442\u044C:<br1><table width=370>");

    int i = 0;
    String augment = "";
    for (L2ItemInstance item : player.getInventory().getItems()) {
      if (item == null)
      {
        continue;
      }
      int itemType = item.getItem().getType2();
      if ((item.canBeEnchanted()) && (!item.isEquipped()) && (item.isDestroyable()) && ((itemType == 0) || (itemType == 1) || (itemType == 2))) {
        if ((item.isAugmented()) && (item.getAugmentation().getAugmentSkill() != null)) {
          augment = new StringBuilder().append("<br1>").append(getAugmentSkill(item.getAugmentation().getAugmentSkill())).toString();
        }

        if (i == 0) {
          tb.append(new StringBuilder().append("<tr><td><img src=\"").append(item.getItem().getIcon()).append("\" width=32 height=32></td><td><a action=\"bypass _bbsmail_menu 1_").append(item.getObjectId()).append("\"> ").append(item.getItem().getName()).append(" + ").append(item.getEnchantLevel()).append("</a>").append(augment).append("</td><td width=30></td>").toString());
          i = 1;
        } else {
          tb.append(new StringBuilder().append("<td><img src=\"").append(item.getItem().getIcon()).append("\" width=32 height=32></td><td><a action=\"bypass _bbsmail_menu 1_").append(item.getObjectId()).append("\"> ").append(item.getItem().getName()).append(" + ").append(item.getEnchantLevel()).append("</a>").append(augment).append("</td></tr>").toString());
          i = 0;
        }
        augment = "";
      }
    }

    tb.append("</table></td></tr></table><br></body></html>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
  }

  private void sendBrief(L2PcInstance player, String target, String tema, String text) {
    TextBuilder tb = new TextBuilder("");
    tb.append(new StringBuilder().append("<html><body><br><br><center><table width=650 border=0><tr><td><img src=\"Icon.etc_letter_envelope_i00\" width=32 height=32><font color=CC9933>@</font><font color=CC00FF>").append(player.getName()).append("</font></td>").toString());
    tb.append("<td width=480><table width=250><tr><td><edit var=\"search\" width=150 length=\"22\"></td><td><combobox width=51 var=keyword list=\"\u0422\u0435\u043C\u0430;\u0410\u0432\u0442\u043E\u0440\"></td>");
    tb.append("<td><button value=\"\u041D\u0430\u0439\u0442\u0438\" action=\"bypass _bbsmail_search $search _ $keyword\" width=40 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></td></tr>");
    tb.append("<tr><td><br><br><table width=100><tr><td><button value=\"\u041D\u043E\u0432\u043E\u0435 \u043F\u0438\u0441\u044C\u043C\u043E\" action=\"bypass _bbsmail_menu 1_0\" width=\"100\" height=\"14\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br></td>");
    tb.append("</tr><tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 2_0_1\">\u0412\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a></td></tr>");
    tb.append("<tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 3_0_1\">\u0418\u0441\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a><br></td></tr>");

    tb.append("</table></td>");
    tb.append("<td width=480>");

    int item_id = 0;
    int item_count = 0;
    int item_ench = 0;
    int aug_id = 0;
    int aug_lvl = 0;
    int aug_hex = 0;

    int item_obj = 0;

    if (player.getBriefItem() != 0) {
      L2ItemInstance item = player.getInventory().getItemByObjectId(player.getBriefItem());
      if (item == null) {
        tb.append("&nbsp;&nbsp;\u041F\u0440\u0435\u0434\u043C\u0435\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.");
        separateAndSend(tb.toString(), player);
        return;
      }
      item_id = item.getItemId();
      item_count = item.getCount();
      item_ench = item.getEnchantLevel();
      if ((item.isAugmented()) && (item.getAugmentation().getAugmentSkill() != null)) {
        L2Skill aug = item.getAugmentation().getAugmentSkill();
        aug_id = aug.getId();
        aug_lvl = aug.getLevel();
        aug_hex = item.getAugmentation().getAugmentationId();
      }
      item_obj = item.getObjectId();
    }

    int targetId = 0;
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("SELECT obj_Id FROM `characters` WHERE `char_name` = ? LIMIT 0,1");
      st.setString(1, target);
      rset = st.executeQuery();
      if (rset.next()) {
        targetId = rset.getInt("obj_Id");
        Close.SR(st, rset); } else { tb.append(new StringBuilder().append("&nbsp;&nbsp;\u041F\u0435\u0440\u0441\u043E\u043D\u0430\u0436 \u0441 \u043D\u0438\u043A\u043E\u043C <font color=LEVEL>").append(target).append("</font> \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.").toString());
        separateAndSend(tb.toString(), player);
        return; }
      if (player.getBriefItem() > 0) {
        L2ItemInstance coin = player.getInventory().getItemByItemId(Config.EXPOSTA_COIN);
        if ((coin == null) || (coin.getCount() < Config.EXPOSTA_PRICE)) { tb.append(new StringBuilder().append("<br><br>\u041E\u0442\u043F\u0440\u0430\u0432\u043A\u0430 \u043F\u0440\u0435\u0434\u043C\u0435\u0442\u0430: ").append(Config.EXPOSTA_PRICE).append(" ").append(Config.EXPOSTA_NAME).append(".").toString());
          separateAndSend(tb.toString(), player);
          return; } player.destroyItemByItemId("MailBBSManager", Config.EXPOSTA_COIN, Config.EXPOSTA_PRICE, player, true);
        player.destroyItem("MailBBSManager", player.getBriefItem(), item_count, player, true);
      } else {
        L2ItemInstance coin = player.getInventory().getItemByItemId(Config.EXPOSTB_COIN);
        if ((coin == null) || (coin.getCount() < Config.EXPOSTB_PRICE)) { tb.append(new StringBuilder().append("<br><br>\u041E\u0442\u043F\u0440\u0430\u0432\u043A\u0430 \u043F\u0438\u0441\u044C\u043C\u0430: ").append(Config.EXPOSTB_PRICE).append(" ").append(Config.EXPOSTB_NAME).append(".").toString());
          separateAndSend(tb.toString(), player);
          return; } player.destroyItemByItemId("MailBBSManager", Config.EXPOSTB_COIN, Config.EXPOSTB_PRICE, player, true);
      }

      st = con.prepareStatement("INSERT INTO `z_bbs_mail` (`from`, `to`, `tema`, `text`, `datetime`, `read`, `item_id`, `item_count`, `item_ench`, `aug_hex`, `aug_id`, `aug_lvl`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      st.setInt(1, player.getObjectId());
      st.setInt(2, targetId);
      st.setString(3, tema);
      st.setString(4, text);
      st.setLong(5, System.currentTimeMillis());
      st.setInt(6, 0);
      st.setInt(7, item_id);
      st.setInt(8, item_count);
      st.setInt(9, item_ench);
      st.setInt(10, aug_hex);
      st.setInt(11, aug_id);
      st.setInt(12, aug_lvl);
      st.execute();
      tb.append(new StringBuilder().append("&nbsp;&nbsp;\u0412\u0430\u0448\u0435 \u043F\u0438\u0441\u044C\u043C\u043E (").append(tema).append(") \u0434\u043B\u044F <font color=LEVEL>").append(target).append("</font> \u043E\u0442\u043F\u0440\u0430\u0432\u043B\u0435\u043D\u043E.").toString());
      L2PcInstance trg = L2World.getInstance().getPlayer(target);
      if (trg != null) {
        trg.sendPacket(new ExMailArrived());
      }

      Log.add(new StringBuilder().append(TimeLogger.getTime()).append("MAIL ").append(item_id).append("(").append(item_count).append(")(+").append(item_ench).append(")(").append(item_obj).append(")(aug: ").append(aug_hex).append(";skill: ").append(aug_id).append(";lvl: ").append(aug_lvl).append(";) #(player ").append(player.getName()).append(", account: ").append(player.getAccountName()).append(", ip: ").append(player.getIP()).append(", hwid: ").append(player.getHWID()).append(")->").append(trg != null ? new StringBuilder().append("(player ").append(trg.getName()).append(", account: ").append(trg.getAccountName()).append(", ip: ").append(trg.getIP()).append(", hwid: ").append(player.getHWID()).append(")").toString() : new StringBuilder().append("(targetId: ").append(targetId).append(")").toString()).toString(), "bbs_mail");
    } catch (Exception e) {
      System.out.println(new StringBuilder().append("[ERROR] MailBBSManager, sendBrief() error: ").append(e).toString());
    } finally {
      Close.CSR(con, st, rset);
    }

    tb.append("</td></tr></table><br></body></html>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
  }

  private void showBrief(L2PcInstance player, int id) {
    TextBuilder tb = new TextBuilder("");
    tb.append(new StringBuilder().append("<html><body><br><br><center><table width=650 border=0><tr><td><img src=\"Icon.etc_letter_envelope_i00\" width=32 height=32><font color=CC9933>@</font><font color=CC00FF>").append(player.getName()).append("</font></td>").toString());
    tb.append("<td width=480><table width=250><tr><td><edit var=\"search\" width=150 length=\"22\"></td><td><combobox width=51 var=keyword list=\"\u0422\u0435\u043C\u0430;\u0410\u0432\u0442\u043E\u0440\"></td>");
    tb.append("<td><button value=\"\u041D\u0430\u0439\u0442\u0438\" action=\"bypass _bbsmail_search $search _ $keyword\" width=40 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></td></tr>");
    tb.append("<tr><td><br><br><table width=100><tr><td><button value=\"\u041D\u043E\u0432\u043E\u0435 \u043F\u0438\u0441\u044C\u043C\u043E\" action=\"bypass _bbsmail_menu 1_0\" width=\"100\" height=\"14\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br></td>");
    tb.append("</tr><tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 2_0_1\">\u0412\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a></td></tr>");
    tb.append("<tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 3_0_1\">\u0418\u0441\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a><br></td></tr>");

    tb.append("</table></td>");
    tb.append("<td width=480>");

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("SELECT * FROM `z_bbs_mail` WHERE `id` = ? AND `to` = ? LIMIT 0, 1");
      st.setInt(1, id);
      st.setInt(2, player.getObjectId());
      rset = st.executeQuery();
      if (rset.next()) {
        String tema = rset.getString("tema");
        String text = rset.getString("text");
        long time = rset.getLong("datetime");
        int item_id = rset.getInt("item_id");
        int item_count = rset.getInt("item_count");
        int item_ench = rset.getInt("item_ench");
        int aug_id = rset.getInt("aug_id");
        int aug_lvl = rset.getInt("aug_lvl");

        String sender = getCharName(con, rset.getInt("from"));

        Date date = new Date(time);
        String briefDate = _sdf.format(date);
        if (briefDate.equals(getNow()))
          briefDate = new StringBuilder().append("\u0412\u0440\u0435\u043C\u044F \u043E\u0442\u043F\u0440\u0430\u0432\u043A\u0438 ").append(_sdfTime.format(date)).toString();
        else {
          briefDate = new StringBuilder().append("\u0414\u0430\u0442\u0430 \u043E\u0442\u043F\u0440\u0430\u0432\u043A\u0438 ").append(_sdfDate.format(date)).toString();
        }

        text = strip(text);
        tema = strip(tema);

        player.setBriefSender(sender);
        player.setMailTheme(tema);

        tb.append("<table width=500><tr><td></td><td><img src=\"Icon.etc_letter_envelope_i00\" width=16 height=16></td>");
        tb.append(new StringBuilder().append("<td width=340>").append(tema).append(" &nbsp;<a action=\"bypass _bbsmail_menu 2_0_1\"><-\u041D\u0430\u0437\u0430\u0434|x</a></td>").toString());
        tb.append(new StringBuilder().append("<td width=80><button value=\"\u041E\u0442\u0432\u0435\u0442\u0438\u0442\u044C\" action=\"bypass _bbsmail_act_1_").append(id).append("\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>").toString());
        tb.append(new StringBuilder().append("<td width=80><button value=\"\u0423\u0434\u0430\u043B\u0438\u0442\u044C\" action=\"bypass _bbsmail_act_2_").append(id).append("\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>").toString());

        tb.append("</tr></table><br><img src=\"sek.cbui355\" width=500 height=1><br><table width=600><tr>");
        tb.append(new StringBuilder().append("<td align=left width=380>\u041E\u0442 <font color=FFCC33> ").append(sender).append(" </font></td>").toString());
        tb.append(new StringBuilder().append("<td align=left width=220> ").append(briefDate).append("</td></tr></table><br>").toString());
        tb.append(new StringBuilder().append("").append(text).append("<br><br><br>").toString());
        tb.append("<img src=\"sek.cbui355\" width=500 height=1><br>");

        if (item_id > 0) {
          L2Item item = ItemTable.getInstance().getTemplate(item_id);
          if (item == null) {
            tb.append("");
          } else {
            String augment = "";
            if (aug_id > 0) {
              L2Skill aug = SkillTable.getInstance().getInfo(aug_id, aug_lvl);
              if (aug != null) {
                augment = new StringBuilder().append("<br1>").append(getAugmentSkill(aug)).toString();
              }
            }
            tb.append(new StringBuilder().append("\u041F\u0440\u0438\u043A\u0440\u0435\u043F\u043B\u0435\u043D\u043D\u044B\u0435 \u043F\u0440\u0435\u0434\u043C\u0435\u0442\u044B:<br><table width=400 border=0><tr><td width=32><img src=\"").append(item.getIcon()).append("\" width=32 height=32></td>").toString());
            tb.append(new StringBuilder().append("<td width=342 align=left><font color=LEVEL>").append(item.getName()).append("(").append(item_count).append(") +").append(item_ench).append(" </font>").append(augment).append("</td></tr>").toString());
            tb.append("<tr><td width=32></td><td width=342 align=left><br><br><img src=\"sek.cbui355\" width=300 height=1><br>");
            tb.append(new StringBuilder().append("<button value=\"\u0417\u0430\u0431\u0440\u0430\u0442\u044C\" action=\"bypass _bbsmail_act_3_").append(id).append("\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>").toString());
          }
        } else if (aug_id > 0) {
          String augment = "";
          L2Skill aug = SkillTable.getInstance().getInfo(aug_id, aug_lvl);
          if (aug != null) {
            augment = new StringBuilder().append("<br1>").append(getAugmentSkill(aug)).toString();
          }

          tb.append("\u041F\u0440\u0438\u043A\u0440\u0435\u043F\u043B\u0435\u043D\u043D\u044B\u0435 \u0430\u0443\u0433\u043C\u0435\u043D\u0442\u044B:<br>");
          tb.append(new StringBuilder().append("").append(augment).append("").toString());
          tb.append("<br><br><img src=\"sek.cbui355\" width=300 height=1><br>");
          tb.append(new StringBuilder().append("<button value=\"\u0417\u0430\u0431\u0440\u0430\u0442\u044C\" action=\"bypass _bbsmail_act_5_").append(id).append("\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">").toString());
        }
        Close.SR(st, rset);
        st = con.prepareStatement("UPDATE `z_bbs_mail` SET `read`= ? WHERE `id`= ?");
        st.setInt(1, 1);
        st.setInt(2, id);
        st.executeUpdate();
      } else {
        tb.append("&nbsp;&nbsp;\u041F\u0438\u0441\u044C\u043C\u043E \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u043E.");
      }
    } catch (Exception e) {
      System.out.println(new StringBuilder().append("[ERROR] MailBBSManager, showBrief() error: ").append(e).toString());
    } finally {
      Close.CSR(con, st, rset);
    }

    tb.append("</td></tr></table><br></body></html>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
  }

  private void getItemFrom(L2PcInstance player, int id) {
    TextBuilder tb = new TextBuilder("");
    tb.append(new StringBuilder().append("<html><body><br><br><center><table width=650 border=0><tr><td><img src=\"Icon.etc_letter_envelope_i00\" width=32 height=32><font color=CC9933>@</font><font color=CC00FF>").append(player.getName()).append("</font></td>").toString());
    tb.append("<td width=480><table width=250><tr><td><edit var=\"search\" width=150 length=\"22\"></td><td><combobox width=51 var=keyword list=\"\u0422\u0435\u043C\u0430;\u0410\u0432\u0442\u043E\u0440\"></td>");
    tb.append("<td><button value=\"\u041D\u0430\u0439\u0442\u0438\" action=\"bypass _bbsmail_search $search _ $keyword\" width=40 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></td></tr>");
    tb.append("<tr><td><br><br><table width=100><tr><td><button value=\"\u041D\u043E\u0432\u043E\u0435 \u043F\u0438\u0441\u044C\u043C\u043E\" action=\"bypass _bbsmail_menu 1_0\" width=\"100\" height=\"14\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br></td>");
    tb.append("</tr><tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 2_0_1\">\u0412\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a></td></tr>");
    tb.append("<tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 3_0_1\">\u0418\u0441\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a><br></td></tr>");

    tb.append("</table></td>");
    tb.append("<td width=480>");

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("SELECT * FROM `z_bbs_mail` WHERE `id` = ? AND `to` = ? LIMIT 0, 1");
      st.setInt(1, id);
      st.setInt(2, player.getObjectId());
      rset = st.executeQuery();
      if (rset.next())
      {
        int item_id = rset.getInt("item_id");
        int item_count = rset.getInt("item_count");
        int item_ench = rset.getInt("item_ench");
        int aug_id = rset.getInt("aug_id");
        int aug_lvl = rset.getInt("aug_lvl");
        int aug_hex = rset.getInt("aug_hex");

        if (item_id > 0) {
          L2Item item = ItemTable.getInstance().getTemplate(item_id);
          if (item == null) {
            tb.append("");
          } else {
            L2ItemInstance reward = player.getInventory().addItem("MailBBSManager", item_id, item_count, player, player.getTarget());
            if (reward == null) { tb.append("&nbsp;&nbsp;\u041F\u0438\u0441\u044C\u043C\u043E \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u043E.");
              separateAndSend(tb.toString(), player);
              return; } if (item_ench > 0) {
              reward.setEnchantLevel(item_ench);
            }
            if (aug_id > 0) {
              reward.setAugmentation(new L2Augmentation(reward, aug_hex, aug_id, aug_lvl, true));
            }

            if (item_count > 1)
              player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(item_id).addNumber(item_count));
            else {
              player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(item_id));
            }
            player.sendItems(true);
          }
        }
        Close.SR(st, rset);
        st = con.prepareStatement("UPDATE `z_bbs_mail` SET `item_id`=?, `item_count`=?, `item_ench`=?, `aug_hex`=?, `aug_id`=?, `aug_lvl`=? WHERE `id`= ?");
        st.setInt(1, 0);
        st.setInt(2, 0);
        st.setInt(3, 0);
        st.setInt(4, 0);
        st.setInt(5, 0);
        st.setInt(6, 0);
        st.setInt(7, id);
        st.executeUpdate();
        tb.append("&nbsp;&nbsp;\u0417\u0430\u0431\u0440\u0430\u043B\u0438.");
      } else {
        tb.append("&nbsp;&nbsp;\u041F\u0438\u0441\u044C\u043C\u043E \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u043E.");
      }
    } catch (Exception e) {
      System.out.println(new StringBuilder().append("[ERROR] MailBBSManager, showBrief() error: ").append(e).toString());
    } finally {
      Close.CSR(con, st, rset);
    }

    tb.append("</td></tr></table><br></body></html>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
  }

  private void getAugFrom(L2PcInstance player, int id) {
    TextBuilder tb = new TextBuilder("");
    tb.append(new StringBuilder().append("<html><body><br><br><center><table width=650 border=0><tr><td><img src=\"Icon.etc_letter_envelope_i00\" width=32 height=32><font color=CC9933>@</font><font color=CC00FF>").append(player.getName()).append("</font></td>").toString());
    tb.append("<td width=480><table width=250><tr><td><edit var=\"search\" width=150 length=\"22\"></td><td><combobox width=51 var=keyword list=\"\u0422\u0435\u043C\u0430;\u0410\u0432\u0442\u043E\u0440\"></td>");
    tb.append("<td><button value=\"\u041D\u0430\u0439\u0442\u0438\" action=\"bypass _bbsmail_search $search _ $keyword\" width=40 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></td></tr>");
    tb.append("<tr><td><br><br><table width=100><tr><td><button value=\"\u041D\u043E\u0432\u043E\u0435 \u043F\u0438\u0441\u044C\u043C\u043E\" action=\"bypass _bbsmail_menu 1_0\" width=\"100\" height=\"14\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br></td>");
    tb.append("</tr><tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 2_0_1\">\u0412\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a></td></tr>");
    tb.append("<tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 3_0_1\">\u0418\u0441\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a><br></td></tr>");

    tb.append("</table></td>");
    tb.append("<td width=480>");

    player.setBriefItem(id);
    tb.append("&nbsp;&nbsp;&nbsp;\u0412\u044B\u0431\u0435\u0440\u0438\u0442\u0435 \u043F\u0440\u0435\u0434\u043C\u0435\u0442, \u0432 \u043A\u043E\u0442\u043E\u0440\u044B\u0439 \u0445\u043E\u0442\u0438\u0442\u0435 \u0432\u0441\u0442\u0430\u0432\u0438\u0442\u044C \u0430\u0443\u0433\u043C\u0435\u043D\u0442:<br1><table width=370>");

    int i = 0;
    for (L2ItemInstance item : player.getInventory().getItems()) {
      if (item == null)
      {
        continue;
      }
      int itemType = item.getItem().getType2();
      if ((item.canBeEnchanted()) && (!item.isAugmented()) && (!item.isEquipped()) && (item.isDestroyable()) && (itemType == 0)) {
        if (i == 0) {
          tb.append(new StringBuilder().append("<tr><td><img src=\"").append(item.getItem().getIcon()).append("\" width=32 height=32></td><td><a action=\"bypass _bbsmail_menu 5_").append(item.getObjectId()).append("\"> ").append(item.getItem().getName()).append(" + ").append(item.getEnchantLevel()).append("</a></td><td width=30></td>").toString());
          i = 1;
        } else {
          tb.append(new StringBuilder().append("<td><img src=\"").append(item.getItem().getIcon()).append("\" width=32 height=32></td><td><a action=\"bypass _bbsmail_menu 5_").append(item.getObjectId()).append("\"> ").append(item.getItem().getName()).append(" + ").append(item.getEnchantLevel()).append("</a></td></tr>").toString());
          i = 0;
        }
      }
    }

    tb.append("</table></td></tr></table><br></body></html>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
  }

  private void addAugTo(L2PcInstance player, int item_id) {
    TextBuilder tb = new TextBuilder("");
    tb.append(new StringBuilder().append("<html><body><br><br><center><table width=650 border=0><tr><td><img src=\"Icon.etc_letter_envelope_i00\" width=32 height=32><font color=CC9933>@</font><font color=CC00FF>").append(player.getName()).append("</font></td>").toString());
    tb.append("<td width=480><table width=250><tr><td><edit var=\"search\" width=150 length=\"22\"></td><td><combobox width=51 var=keyword list=\"\u0422\u0435\u043C\u0430;\u0410\u0432\u0442\u043E\u0440\"></td>");
    tb.append("<td><button value=\"\u041D\u0430\u0439\u0442\u0438\" action=\"bypass _bbsmail_search $search _ $keyword\" width=40 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></td></tr>");
    tb.append("<tr><td><br><br><table width=100><tr><td><button value=\"\u041D\u043E\u0432\u043E\u0435 \u043F\u0438\u0441\u044C\u043C\u043E\" action=\"bypass _bbsmail_menu 1_0\" width=\"100\" height=\"14\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br></td>");
    tb.append("</tr><tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 2_0_1\">\u0412\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a></td></tr>");
    tb.append("<tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 3_0_1\">\u0418\u0441\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a><br></td></tr>");

    tb.append("</table></td>");
    tb.append("<td width=480>");

    int briefId = player.getBriefItem();
    if ((briefId == 0) || (briefId > 1000000)) {
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
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("SELECT * FROM `z_bbs_mail` WHERE `id` = ? AND `to` = ? LIMIT 0, 1");
      st.setInt(1, briefId);
      st.setInt(2, player.getObjectId());
      rset = st.executeQuery();
      if (rset.next()) {
        int aug_id = rset.getInt("aug_id");
        int aug_lvl = rset.getInt("aug_lvl");
        int aug_hex = rset.getInt("aug_hex");
        if (aug_id > 0) {
          L2Skill aug = SkillTable.getInstance().getInfo(aug_id, aug_lvl);
          if (aug == null) { tb.append("&nbsp;&nbsp;\u041F\u0440\u0435\u0434\u043C\u0435\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.");
            separateAndSend(tb.toString(), player);
            return; } item.setAugmentation(new L2Augmentation(item, aug_hex, aug_id, aug_lvl, true));
          player.sendItems(true);
          player.sendPacket(SystemMessage.id(SystemMessageId.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED)); } else { tb.append("&nbsp;&nbsp;\u041F\u0440\u0435\u0434\u043C\u0435\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.");
          separateAndSend(tb.toString(), player);
          return;
        }
        Close.SR(st, rset);
        st = con.prepareStatement("UPDATE `z_bbs_mail` SET `item_id`=?, `item_count`=?, `item_ench`=?, `aug_hex`=?, `aug_id`=?, `aug_lvl`=? WHERE `id`= ?");
        st.setInt(1, 0);
        st.setInt(2, 0);
        st.setInt(3, 0);
        st.setInt(4, 0);
        st.setInt(5, 0);
        st.setInt(6, 0);
        st.setInt(7, briefId);
        st.executeUpdate();
        tb.append("&nbsp;&nbsp;\u0417\u0430\u0431\u0440\u0430\u043B\u0438.");
      } else {
        tb.append("&nbsp;&nbsp;\u041F\u0438\u0441\u044C\u043C\u043E \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u043E.");
      }
    } catch (Exception e) {
      System.out.println(new StringBuilder().append("[ERROR] MailBBSManager, showBrief() error: ").append(e).toString());
    } finally {
      Close.CSR(con, st, rset);
    }

    tb.append("</td></tr></table><br></body></html>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
  }

  private void deleteBrief(L2PcInstance player, int id, boolean force) {
    TextBuilder tb = new TextBuilder("");
    tb.append(new StringBuilder().append("<html><body><br><br><center><table width=650 border=0><tr><td><img src=\"Icon.etc_letter_envelope_i00\" width=32 height=32><font color=CC9933>@</font><font color=CC00FF>").append(player.getName()).append("</font></td>").toString());
    tb.append("<td width=480><table width=250><tr><td><edit var=\"search\" width=150 length=\"22\"></td><td><combobox width=51 var=keyword list=\"\u0422\u0435\u043C\u0430;\u0410\u0432\u0442\u043E\u0440\"></td>");
    tb.append("<td><button value=\"\u041D\u0430\u0439\u0442\u0438\" action=\"bypass _bbsmail_search $search _ $keyword\" width=40 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></td></tr>");
    tb.append("<tr><td><br><br><table width=100><tr><td><button value=\"\u041D\u043E\u0432\u043E\u0435 \u043F\u0438\u0441\u044C\u043C\u043E\" action=\"bypass _bbsmail_menu 1_0\" width=\"100\" height=\"14\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br></td>");
    tb.append("</tr><tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 2_0_1\">\u0412\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a></td></tr>");
    tb.append("<tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 3_0_1\">\u0418\u0441\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a><br></td></tr>");

    tb.append("</table></td>");
    tb.append("<td width=480>");

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("SELECT * FROM `z_bbs_mail` WHERE `id` = ? AND `to` = ? LIMIT 0, 1");
      st.setInt(1, id);
      st.setInt(2, player.getObjectId());
      rset = st.executeQuery();
      if (rset.next()) {
        String tema = rset.getString("tema");
        int item_id = rset.getInt("item_id");
        int item_ench = rset.getInt("item_ench");
        int aug_id = rset.getInt("aug_id");
        int aug_lvl = rset.getInt("aug_lvl");
        if (item_id > 0) {
          L2Item item = ItemTable.getInstance().getTemplate(item_id);
          if (item == null) {
            tb.append("");
          } else {
            String augment = "";
            if (aug_id > 0) {
              L2Skill aug = SkillTable.getInstance().getInfo(aug_id, aug_lvl);
              if (aug != null) {
                augment = new StringBuilder().append("<br1>").append(getAugmentSkill(aug)).toString();
              }
            }
            tb.append(new StringBuilder().append("<font color=FF9966>!\u041F\u0438\u0441\u044C\u043C\u043E \u0441\u043E\u0434\u0435\u0440\u0436\u0438\u0442 \u043F\u0440\u0438\u043A\u0440\u0435\u043F\u043B\u0435\u043D\u043D\u044B\u0439 \u043F\u0440\u0435\u0434\u043C\u0435\u0442:</font><br><table width=400 border=0><tr><td width=32><img src=\"").append(item.getIcon()).append("\" width=32 height=32></td>").toString());
            tb.append(new StringBuilder().append("<td width=342 align=left><font color=LEVEL>").append(item.getName()).append(" +").append(item_ench).append(" </font>").append(augment).append("</td></tr>").toString());
            tb.append("<tr><td width=32></td><td width=342 align=left><br><br><img src=\"sek.cbui355\" width=300 height=1><br>");
            tb.append("<button value=\"\u0423\u0434\u0430\u043B\u0438\u0442\u044C\" action=\"bypass _bbsmail_act_2_4\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
          }
        } else if ((item_id == 0) || (force)) {
          Close.SR(st, rset);
          st = con.prepareStatement("DELETE FROM `z_bbs_mail` WHERE `id`= ?");
          st.setInt(1, id);
          st.executeUpdate();
          tb.append(new StringBuilder().append("&nbsp;&nbsp;\u041F\u0438\u0441\u044C\u043C\u043E (").append(tema).append(") \u0443\u0434\u0430\u043B\u0435\u043D\u043E.").toString());
        }
      } else {
        tb.append("&nbsp;&nbsp;\u041F\u0438\u0441\u044C\u043C\u043E \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u043E.");
      }
    } catch (Exception e) {
      System.out.println(new StringBuilder().append("[ERROR] MailBBSManager, deleteBrief() error: ").append(e).toString());
    } finally {
      Close.CSR(con, st, rset);
    }

    tb.append("</td></tr></table><br></body></html>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
  }

  private void showSearchResult(L2PcInstance player, String type, String word) {
    player.setBriefItem(0);
    player.setBriefSender("n.a");
    player.setMailTheme("n.a");
    TextBuilder tb = new TextBuilder("");
    tb.append(new StringBuilder().append("<html><body><br><br><center><table width=650 border=0><tr><td><img src=\"Icon.etc_letter_envelope_i00\" width=32 height=32><font color=CC9933>@</font><font color=CC00FF>").append(player.getName()).append("</font></td>").toString());
    tb.append("<td width=480><table width=250 border=0><tr><td><edit var=\"search\" width=150 length=\"22\"></td><td><combobox width=51 var=keyword list=\"\u0422\u0435\u043C\u0430;\u0410\u0432\u0442\u043E\u0440\"></td>");
    tb.append("<td><button value=\"\u041D\u0430\u0439\u0442\u0438\" action=\"bypass _bbsmail_search $search _ $keyword\" width=40 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></td></tr>");
    tb.append("<tr><td><br><br><table width=100><tr><td><button value=\"\u041D\u043E\u0432\u043E\u0435 \u043F\u0438\u0441\u044C\u043C\u043E\" action=\"bypass _bbsmail_menu 1_0\" width=\"100\" height=\"14\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br></td>");
    tb.append("<tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 3_0_1\">\u0418\u0441\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a><br></td></tr>");
    tb.append("</tr><tr><td>&nbsp;<a action=\"bypass _bbsmail_menu 2_0_1\">\u0412\u0445\u043E\u0434\u044F\u0449\u0438\u0435</a></td></tr>");

    tb.append("</table></td>");
    tb.append(new StringBuilder().append("<td width=480>\u041F\u043E\u0438\u0441\u043A \u043F\u043E: ").append(type).append("+").append(word).append("<table width=500><tr><td></td><td></td><td></td><td></td></tr>").toString());

    Date date = null;
    String dateNow = getNow();
    String briefDate = "";

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      if (type.equals("\u0422\u0435\u043C\u0430"))
        st = con.prepareStatement("SELECT * FROM `z_bbs_mail` WHERE `to` = ? AND `tema` LIKE '%?%' ORDER BY `datetime` DESC LIMIT 0, 20");
      else {
        st = con.prepareStatement("SELECT * FROM `z_bbs_mail` WHERE `to` = ? AND `from` LIKE '%?%' ORDER BY `datetime` DESC LIMIT 0, 20");
      }

      st.setInt(1, player.getObjectId());
      rset = st.executeQuery();
      while (rset.next()) {
        int id = rset.getInt("id");
        String tema = rset.getString("tema");
        long time = rset.getLong("datetime");
        int read = rset.getInt("read");
        String name = getCharName(con, rset.getInt("from"));
        int item_id = rset.getInt("item_id");

        date = new Date(time);
        briefDate = _sdf.format(date);
        if (briefDate.equals(dateNow))
          briefDate = _sdfTime.format(date);
        else {
          briefDate = _sdfDate.format(date);
        }

        tb.append(new StringBuilder().append("<tr><td width=16><img src=\"Icon.etc_letter_envelope_i00\" width=16 height=16></td><td align=left><font color=").append(read == 0 ? "CC00FF" : "6699CC").append("> ").append(name).append(" </td>").toString());
        tb.append(new StringBuilder().append("<td align=left><a action=\"bypass _bbsmail_show ").append(id).append("\"> ").append(item_id > 1 ? "$" : "").append(tema).append(" </a></td><td align=right> ").append(briefDate).append(" </font></td></tr>").toString());
      }
    } catch (Exception e) {
      System.out.println(new StringBuilder().append("[ERROR] MailBBSManager, showIndex(").append(player.getName()).append(", ").append(type).append(") ").append(e).toString());
      e.printStackTrace();
    } finally {
      Close.CSR(con, st, rset);
    }

    tb.append("</table></td></tr></table><br></body></html>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
  }

  private String getNow() {
    return _sdf.format(_date);
  }

  private String strip(String text) {
    text = text.replaceAll("<a>", "");
    text = text.replaceAll("</a>", "");
    text = text.replaceAll("<font>", "");
    text = text.replaceAll("</font>", "");
    text = text.replaceAll("<table>", "");
    text = text.replaceAll("<tr>", "");
    text = text.replaceAll("<td>", "");
    text = text.replaceAll("</table>", "");
    text = text.replaceAll("</tr>", "");
    text = text.replaceAll("</td>", "");
    text = text.replaceAll("<br>", "");
    text = text.replaceAll("<br1>", "");
    text = text.replaceAll("<button", "");
    return text;
  }

  private String getAugmentSkill(L2Skill augment) {
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

    return new StringBuilder().append("<font color=336699>\u0410\u0443\u0433\u043C\u0435\u043D\u0442:</font> <font color=bef574>").append(augName).append(" (").append(type).append(":").append(augment.getLevel()).append("lvl)</font>").toString();
  }

  private String getCharName(Connect con, int objId) {
    if (objId == 555) {
      return Config.POST_NPCNAME;
    }
    if (objId == 777) {
      return "=\u0410\u0443\u043A\u0446\u0438\u043E\u043D=";
    }
    if (objId == 334) {
      return "~Fight Club.";
    }

    PreparedStatement st = null;
    ResultSet rset = null;
    try
    {
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
      System.out.println(new StringBuilder().append("[ERROR] MailBBSManager, getCharName() error: ").append(e).toString());
    } finally {
      Close.SR(st, rset);
    }
    return "???";
  }

  public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance player)
  {
  }
}