package net.sf.l2j.gameserver.model.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.HeroSkillTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExMailArrived;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.GiveItem;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public class FightClub
{
  private static final Logger _log = AbstractLogger.getLogger(FightClub.class.getName());
  private static Map<Integer, Fighter> _fcPlayers = new ConcurrentHashMap();
  private static Map<Integer, Contest> _fcFights = new ConcurrentHashMap();

  private static FastTable<FightClubArena> _arenas = new FastTable();
  private static Location _tpLoc = new Location(116530, 76141, -2730);
  private static long _maxTime = 300000L;

  public static void showFighters(L2PcInstance player, int npcObj)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(npcObj);
    TextBuilder tb = new TextBuilder("<html><body>\u0411\u043E\u0439\u0446\u043E\u0432\u0441\u043A\u0438\u0439 \u043A\u043B\u0443\u0431.<br>\u041E\u0436\u0438\u0434\u0430\u044E\u0449\u0438\u0435 \u0431\u043E\u044F:");
    if (_fcPlayers.isEmpty()) {
      tb.append("\u041D\u0435\u0442 \u0431\u043E\u0435\u0432.");
      tb.append("</body></html>");
      reply.setHtml(tb.toString());
      player.sendPacket(reply);
      tb.clear();
      tb = null;
      return;
    }

    tb.append("<br><br><table width=300>");
    for (Integer id : _fcPlayers.keySet()) {
      Fighter temp = (Fighter)_fcPlayers.get(id);
      L2PcInstance enemy = L2World.getInstance().getPlayer(id.intValue());
      FcItem item = temp.item;
      if ((item == null) || 
        (enemy == null))
      {
        continue;
      }
      String item_name = item.name + "(" + item.count + ")(+" + item.enchant + ")";
      String augm = "";
      if (item.aug_skillId > 0) {
        augm = "<br1>" + getAugmentSkill(item.aug_skillId, item.aug_lvl);
      }

      tb.append("<tr><td><img src=\"" + item.icon + "\" width=32 height=32></td><td><a action=\"bypass -h npc_" + npcObj + "_fc_enemy " + id + "\">\u0418\u0433\u0440\u043E\u043A: " + enemy.getName() + "<br1>" + item_name + ")</a> " + augm + "</td></tr>");
    }

    tb.append("</table><br><br>");
    tb.append("</body></html>");
    reply.setHtml(tb.toString());
    player.sendPacket(reply);
    tb.clear();
    tb = null;
  }

  public static void viewFights(L2PcInstance player, int npcObj) {
    NpcHtmlMessage reply = NpcHtmlMessage.id(npcObj);
    TextBuilder tb = new TextBuilder("<html><body>\u0411\u043E\u0439\u0446\u043E\u0432\u0441\u043A\u0438\u0439 \u043A\u043B\u0443\u0431:");
    if (_fcPlayers.isEmpty()) {
      tb.append("\u041D\u0435\u0442 \u0431\u043E\u0435\u0432.");
      tb.append("</body></html>");
      reply.setHtml(tb.toString());
      player.sendPacket(reply);
      tb.clear();
      tb = null;
      return;
    }
    tb.append("<br1> \u041F\u0440\u043E\u0441\u043C\u043E\u0442\u0440 \u0431\u043E\u0435\u0432<br><table width=300>");

    for (Integer id : _fcFights.keySet()) {
      Contest temp = (Contest)_fcFights.get(id);

      L2PcInstance fighter = L2World.getInstance().getPlayer(temp.fighter1.obj_id);
      L2PcInstance enemy = L2World.getInstance().getPlayer(temp.fighter2.obj_id);
      if ((fighter == null) || (enemy == null))
      {
        continue;
      }
      FcItem item1 = temp.fighter1.item;
      String item_name1 = item1.name + "(" + item1.count + ")(+" + item1.enchant + ")";
      String augm1 = "";
      if (item1.aug_skillId > 0) {
        augm1 = "<br1>" + getAugmentSkill(item1.aug_skillId, item1.aug_lvl);
      }

      FcItem item2 = temp.fighter2.item;
      String item_name2 = item2.name + "(" + item2.count + ")(+" + item2.enchant + ")";
      String augm2 = "";
      if (item2.aug_skillId > 0) {
        augm2 = "<br1>" + getAugmentSkill(item2.aug_skillId, item2.aug_lvl);
      }

      tb.append("<tr><td></td><td><a action=\"bypass -h npc_" + npcObj + "_fc_arview " + id + "\">\u0411\u043E\u0439 \u043C\u0435\u0436\u0434\u0443: " + fighter.getName() + " \u0438 " + enemy.getName() + "</a>\u041D\u0430 \u043A\u043E\u043D\u0443:<br> </td></tr>");
      tb.append("<tr><td><img src=\"" + item1.icon + "\" width=32 height=32></td><td>" + item_name1 + " " + augm1 + "</td></tr>");
      tb.append("<tr><td><img src=\"" + item2.icon + "\" width=32 height=32></td><td>" + item_name2 + " " + augm2 + "</td></tr>");
    }
    tb.append("</table><br><br><a action=\"bypass -h npc_" + npcObj + "_FightClub 1\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br>");
    tb.append("</body></html>");
    reply.setHtml(tb.toString());
    player.sendPacket(reply);
    tb.clear();
    tb = null;
  }

  public static void viewArena(L2PcInstance player, int id, int npcObj) {
    Contest ftemp = (Contest)_fcFights.get(Integer.valueOf(id));

    ftemp.stadium.addSpectator(1, player, true);
  }

  public static void showInventoryItems(L2PcInstance player, int type, int npcObj) {
    player.sendHtmlMessage("\u0418\u0432\u0435\u043D\u0442 \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
    type = 0;

    NpcHtmlMessage reply = NpcHtmlMessage.id(npcObj);
    TextBuilder tb = new TextBuilder("<html><body>");
    tb.append("\u0412\u044B\u0431\u043E\u0440 \u0448\u043C\u043E\u0442\u043A\u0438:<br>\u041D\u0430 \u0447\u0442\u043E \u0441\u044B\u0433\u0440\u0430\u0435\u043C?<br><br><table width=300>");

    int objectId = 0;
    String itemName = "";
    int enchantLevel = 0;
    String itemIcon = "";
    String augm = "";
    int itemType = 0;
    int itemId = 0;
    for (L2ItemInstance item : player.getInventory().getItems()) {
      if (item == null)
      {
        continue;
      }
      itemId = item.getItemId();
      if (!Config.FC_ALLOWITEMS.contains(Integer.valueOf(itemId)))
      {
        continue;
      }
      objectId = item.getObjectId();
      itemName = item.getItem().getName();
      itemIcon = item.getItem().getIcon();

      if (type == 0)
      {
        tb.append("<tr><td><img src=\"" + itemIcon + "\" width=32 height=32></td><td><a action=\"bypass -h npc_" + npcObj + "_fc_item_0_" + objectId + "\">" + itemName + " (" + item.getCount() + " \u0448\u0442\u0443\u043A)</a></td></tr>");
      } else {
        enchantLevel = item.getEnchantLevel();
        itemType = item.getItem().getType2();
        if ((item.canBeEnchanted()) && (!item.isEquipped()) && (item.isDestroyable()) && ((itemType == 0) || (itemType == 1) || (itemType == 2) || (item.isAugmented()))) {
          if ((item.isAugmented()) && (item.getAugmentation().getAugmentSkill() != null)) {
            L2Skill augment = item.getAugmentation().getAugmentSkill();
            augm = "<br1>" + getAugmentSkill(augment.getId(), augment.getLevel());
          }
          tb.append("<tr><td><img src=\"" + itemIcon + "\" width=32 height=32></td><td><a action=\"bypass -h npc_" + npcObj + "_fc_item_1_" + objectId + "\">" + itemName + " (+" + enchantLevel + ")</a> " + augm + "</td></tr>");
        }
      }
    }

    tb.append("</table><br><br><a action=\"bypass -h npc_" + npcObj + "_FightClub 1\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br>");
    tb.append("</body></html>");
    reply.setHtml(tb.toString());
    player.sendPacket(reply);
    tb.clear();
    tb = null;
  }

  public static void showItemFull(L2PcInstance player, int objectId, int type, int npcObj)
  {
    type = 0;

    L2ItemInstance item = player.getInventory().getItemByObjectId(objectId);
    if (item == null) {
      showError(player, "\u0428\u043C\u043E\u0442\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430");
      return;
    }

    NpcHtmlMessage reply = NpcHtmlMessage.id(npcObj);
    TextBuilder tb = new TextBuilder("<html><body>");

    String itemName = item.getItem().getName();
    String enchantLevel = "";
    int count = item.getCount();
    String augm = "";
    int augId = -1;
    int encLvl = 0;

    tb.append("\u0412\u044B\u0431\u043E\u0440 \u0441\u0442\u0430\u0432\u043A\u0438:<br>\u041F\u043E\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0430\u0435\u0442\u0435?<br>");
    tb.append("<table width=300><tr><td><img src=\"" + item.getItem().getIcon() + "\" width=32 height=32></td><td><font color=LEVEL>" + itemName + " (" + count + ")(+" + item.getEnchantLevel() + ")</font><br></td></tr></table><br><br>");
    if (type == 1) {
      tb.append("<br>\u0417\u0430\u0442\u043E\u0447\u043A\u0430: <font color=bef574>+" + enchantLevel + "</font><br>");
      if ((item.isAugmented()) && (item.getAugmentation().getAugmentSkill() != null)) {
        L2Skill augment = item.getAugmentation().getAugmentSkill();
        augm = "<br1>" + getAugmentSkill(augment.getId(), augment.getLevel());
        augId = item.getAugmentation().getAugmentationId();
      }

      tb.append("<button value=\"\u0412\u044B\u0441\u0442\u0430\u0432\u0438\u0442\u044C\" action=\"bypass -h npc_" + npcObj + "_fc_add " + objectId + " -1 $pass\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
      count = 1;
      encLvl = 0;
    }
    else {
      tb.append("\u0421\u043A\u043E\u043B\u044C\u043A\u043E \u0441\u0442\u0430\u0432\u0438\u043C? (max. " + count + "):<br1><edit var=\"count\" width=200 length=\"16\">");
      tb.append("<button value=\"\u0412\u044B\u0441\u0442\u0430\u0432\u0438\u0442\u044C\" action=\"bypass -h npc_" + npcObj + "_fc_add " + objectId + " $count $pass\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
      encLvl = item.getEnchantLevel();
    }
    player.setFCItem(objectId, encLvl, augId, count);

    tb.append("<br><br><a action=\"bypass -h npc_" + npcObj + "_FightClub 1\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br>");
    tb.append("</body></html>");
    reply.setHtml(tb.toString());
    player.sendPacket(reply);
    tb.clear();
    tb = null;
  }

  public static void finishItemFull(L2PcInstance player, int objectId, int count, String passw, int npcObj)
  {
    if (_fcPlayers.containsKey(Integer.valueOf(player.getObjectId()))) {
      showError(player, "\u0412\u044B \u0443\u0436\u0435 \u0436\u0434\u0435\u0442\u0435 \u0431\u043E\u0439");
      return;
    }

    if (count == 0) {
      showError(player, "\u0428\u043C\u043E\u0442\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430. 4.1");
      return;
    }

    if (player.getFcObj() != objectId) {
      showError(player, "\u0428\u043C\u043E\u0442\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430. 4.2");
      return;
    }

    L2ItemInstance item = player.getInventory().getItemByObjectId(objectId);
    if (item != null) {
      int enchantLevel = item.getEnchantLevel();
      if (player.getFcEnch() != enchantLevel) {
        showError(player, "\u0428\u043C\u043E\u0442\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430. 5");
        return;
      }
      int itemCount = item.getCount();
      if ((itemCount == 0) || (player.getFcCount() == 0) || (itemCount < player.getFcCount())) {
        showError(player, "\u0428\u043C\u043E\u0442\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430. 6");
        return;
      }

      NpcHtmlMessage reply = NpcHtmlMessage.id(npcObj);
      TextBuilder tb = new TextBuilder("<html><body>");

      int itemId = item.getItemId();
      int augmentId = 0;
      int augAttr = 0;
      int augLvl = 0;
      int shadow = 0;
      int encLvl = item.getEnchantLevel();
      String itemName = item.getItem().getName();
      String itemIcon = item.getItem().getIcon();

      tb.append("<br>\u0412\u044B\u0441\u0442\u0430\u0432\u043B\u0435\u043D\u0430 \u0448\u043C\u043E\u0442\u043A\u0430<br>");
      tb.append("<table width=300><tr><td><img src=\"" + itemIcon + "\" width=32 height=32></td><td><font color=LEVEL>" + itemName + "(" + count + ")(+" + encLvl + ")</font><br></td></tr></table><br><br>");

      if ((item.isAugmented()) && (item.getAugmentation().getAugmentSkill() != null)) {
        augAttr = item.getAugmentation().getAugmentationId();
        if (player.getFcAugm() != augAttr) {
          showError(player, "\u041E\u0448\u0438\u0431\u043A\u0430 \u0437\u0430\u043F\u0440\u043E\u0441\u0430.");
          return;
        }

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
        augLvl = augment.getLevel();

        tb.append("<br>\u0410\u0443\u0433\u043C\u0435\u043D\u0442: <font color=bef574>" + augName + " (" + type + ")</font><br>");
      }

      if (player.getItemCount(itemId) < count) {
        tb.clear();
        tb = null;
        showError(player, "\u0428\u043C\u043E\u0442\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430. 7");
        return;
      }

      int plObj = player.getObjectId();
      if (addToFC(player, itemId, enchantLevel, augmentId, augAttr, augLvl, count, passw, shadow))
      {
        player.destroyItem("FightClub", objectId, count, null, true);
        putFighter(plObj, new FcItem(itemId, count, enchantLevel, augAttr, augmentId, augLvl, itemName, itemIcon), passw, shadow);

        player.setFClub(true);
        player.sendItems(false);
        player.sendChanges();
        tb.append("\u0412\u044B\u0441\u0442\u0430\u0432\u043B\u0435\u043D\u0430!<br><br>");
      } else {
        tb.append("\u041F\u0440\u043E\u0438\u0437\u043E\u0448\u043B\u0430 \u043E\u0448\u0438\u0431\u043A\u0430!<br><br>");
      }

      player.setFCItem(0, 0, 0, 0);

      tb.append("<br><a action=\"bypass -h npc_" + npcObj + "_FightClub 1\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F.</a>");
      tb.append("</body></html>");
      reply.setHtml(tb.toString());
      player.sendPacket(reply);
      tb.clear();
      tb = null;
      reply = null;
    } else {
      showError(player, "\u0428\u043C\u043E\u0442\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430. 8");
      return;
    }
  }

  public static void putFighter(int obj, FcItem item, String pass, int shadow)
  {
    _fcPlayers.put(Integer.valueOf(obj), new Fighter(obj, item, pass, shadow));
  }

  private static boolean addToFC(L2PcInstance player, int itemId, int enchantLevel, int augmentId, int augAttr, int augLvl, int count, String pass, int shadow)
  {
    if (count == 0) {
      return false;
    }

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("REPLACE INTO z_fc_players (id, name, itemId, enchant, augment, augAttr, augLvl, count, password, shadow) VALUES (?,?,?,?,?,?,?,?,?,?)");
      st.setInt(1, player.getObjectId());
      st.setString(2, player.getName());
      st.setInt(3, itemId);
      st.setInt(4, enchantLevel);
      st.setInt(5, augmentId);
      st.setInt(6, augAttr);
      st.setInt(7, augLvl);
      st.setInt(8, count);
      st.setString(9, pass);
      st.setInt(10, shadow);
      st.execute();
      int i = 1;
      return i;
    }
    catch (SQLException e)
    {
      _log.warning("FC: addToFC() error: " + e);
      e.printStackTrace();
    } finally {
      Close.CS(con, st);
    }
    return false;
  }

  public static void showEnemyDetails(L2PcInstance player, int id, int npcObj) {
    if (!_fcPlayers.containsKey(Integer.valueOf(id))) {
      showError(player, "\u0411\u043E\u0435\u0446 \u043D\u0438 \u043D\u0430\u0439\u0434\u0435\u043D");
      return;
    }

    NpcHtmlMessage reply = NpcHtmlMessage.id(npcObj);
    TextBuilder tb = new TextBuilder("<html><body>");

    Fighter temp = (Fighter)_fcPlayers.get(Integer.valueOf(id));
    L2PcInstance enemy = L2World.getInstance().getPlayer(id);
    FcItem item = temp.item;
    if (enemy == null) {
      showError(player, "\u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u0432 \u0438\u0433\u0440\u0435");
      sendLetter(id, item, false);
      _fcPlayers.remove(Integer.valueOf(id));
      return;
    }

    String item_name = item.name + "(" + item.count + ")(+" + item.enchant + ")";

    tb.append("\u0411\u043E\u0439\u0446\u043E\u0432\u0441\u043A\u0438\u0439 \u043A\u043B\u0443\u0431:<br>\u0418\u0433\u0440\u043E\u043A: " + enemy.getName() + "<br>");
    tb.append("\u041A\u043B\u0430\u0441\u0441: " + CharTemplateTable.getClassNameById(enemy.getActiveClass()) + "<br>");
    tb.append("\u0423\u0440\u043E\u0432\u0435\u043D\u044C: " + enemy.getLevel() + "<br>");
    if (enemy.isHero()) {
      tb.append("\u0421\u0442\u0430\u0442\u0443\u0441: \u0413\u0435\u0440\u043E\u0439<br>");
    }

    tb.append("<table width=300><tr><td><img src=\"" + item.icon + "\" width=32 height=32></td><td><font color=LEVEL>" + item_name + ")</font><br></td></tr></table><br><br>");

    if (item.aug_skillId > 0) {
      tb.append(getAugmentSkill(item.aug_skillId, item.aug_lvl) + "<br><br>");
    }

    L2ItemInstance coins = player.getInventory().getItemByItemId(item.id);
    if ((coins != null) && (coins.getCount() >= item.count))
      tb.append("<button value=\"\u041F\u0440\u0438\u043D\u044F\u0442\u044C\" action=\"bypass -h npc_" + npcObj + "_fc_accept " + id + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
    else {
      tb.append("<font color=999999>[\u041F\u0440\u0438\u043D\u044F\u0442\u044C \u0431\u043E\u0439]</font><br>");
    }

    tb.append("<br><br><a action=\"bypass -h npc_" + npcObj + "_FightClub 1\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br>");
    tb.append("</body></html>");
    reply.setHtml(tb.toString());
    player.sendPacket(reply);
    tb.clear();
    tb = null;
  }

  public static void startFight(L2PcInstance player, int id, int npcObj) {
    if (!_fcPlayers.containsKey(Integer.valueOf(id))) {
      showError(player, "\u0411\u043E\u0435\u0446 \u043D\u0438 \u043D\u0430\u0439\u0434\u0435\u043D");
      return;
    }

    Fighter temp = (Fighter)_fcPlayers.get(Integer.valueOf(id));
    L2PcInstance enemy = L2World.getInstance().getPlayer(id);
    FcItem item = temp.item;
    if (enemy == null) {
      showError(player, "\u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u0432 \u0438\u0433\u0440\u0435");
      sendLetter(id, item, false);
      _fcPlayers.remove(Integer.valueOf(id));
      return;
    }
    String pass = temp.pass;
    int shadow = temp.active;

    FightClubArena arenaf = null;
    for (int i = _arenas.size() - 1; i > -1; i--) {
      FightClubArena arena = (FightClubArena)_arenas.get(i);
      if (arena == null)
      {
        continue;
      }
      if (arena.isFreeToUse()) {
        arena.setStadiaBusy();
        arenaf = arena;
        break;
      }
    }
    if (arenaf == null) {
      showError(player, "\u041D\u0435\u0442 \u0441\u0432\u043E\u0431\u043E\u0434\u043D\u044B\u0445 \u0430\u0440\u0435\u043D");
      return;
    }

    L2ItemInstance coins = player.getInventory().getItemByItemId(item.id);
    if ((coins == null) || (coins.getCount() < item.count)) {
      showError(player, "\u041D\u0435\u043F\u0440\u0430\u0432\u0438\u043B\u044C\u043D\u0430\u044F \u0441\u0442\u0430\u0432\u043A\u0430");
      return;
    }

    if (!player.destroyItemByItemId("FC accept", item.id, item.count, player, true)) {
      showError(player, "\u041D\u0435\u043F\u0440\u0430\u0432\u0438\u043B\u044C\u043D\u0430\u044F \u0441\u0442\u0430\u0432\u043A\u0430");
      return;
    }

    _fcPlayers.remove(Integer.valueOf(id));

    putBattle(temp, new Fighter(player.getObjectId(), item, pass, shadow), arenaf);

    player.sendCritMessage("\u0427\u0435\u0440\u0435\u0437 30 \u0441\u0435\u043A\u0443\u043D\u0434 \u0442\u0435\u043B\u0435\u043F\u043E\u0440\u0442 \u043D\u0430 \u0430\u0440\u0435\u043D\u0443.");
    enemy.sendCritMessage("\u0427\u0435\u0440\u0435\u0437 30 \u0441\u0435\u043A\u0443\u043D\u0434 \u0442\u0435\u043B\u0435\u043F\u043E\u0440\u0442 \u043D\u0430 \u0430\u0440\u0435\u043D\u0443.");
    player.setFClub(true);
    enemy.setFClub(true);
    ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(id), 30000L);
  }

  public static void putBattle(Fighter fighter1, Fighter fighter2, FightClubArena stadium) {
    _fcFights.put(Integer.valueOf(obj_id), new Contest(fighter1, fighter2, stadium));
  }

  private static boolean haveWinner(int id)
  {
    Contest temp = (Contest)_fcFights.get(Integer.valueOf(id));
    L2PcInstance fighter = L2World.getInstance().getPlayer(id);
    int eid = temp.fighter2.obj_id;
    L2PcInstance enemy = L2World.getInstance().getPlayer(eid);
    if ((fighter == null) || (enemy == null) || (!fighter.inFightClub()) || (!enemy.inFightClub())) {
      if ((fighter != null) && (fighter.inFightClub())) {
        fighter.sendCritMessage("\u0411\u043E\u0439\u0446\u043E\u0432\u0441\u043A\u0438\u0439 \u043A\u043B\u0443\u0431: \u041F\u043E\u0431\u0435\u0434\u0430! \u041F\u0440\u043E\u0432\u0435\u0440\u044C \u043F\u043E\u0447\u0442\u0443.");
        fighter.sendPacket(new ExMailArrived());
        sendLetter(id, temp.fighter1.item, true);
        sendLetter(id, temp.fighter2.item, true);
        return true;
      }
      if ((enemy != null) && (enemy.inFightClub())) {
        enemy.sendCritMessage("\u0411\u043E\u0439\u0446\u043E\u0432\u0441\u043A\u0438\u0439 \u043A\u043B\u0443\u0431: \u041F\u043E\u0431\u0435\u0434\u0430! \u041F\u0440\u043E\u0432\u0435\u0440\u044C \u043F\u043E\u0447\u0442\u0443.");
        enemy.sendPacket(new ExMailArrived());
        sendLetter(eid, temp.fighter1.item, true);
        sendLetter(eid, temp.fighter2.item, true);
        return true;
      }

      sendLetter(id, temp.fighter1.item, false);
      sendLetter(id, temp.fighter2.item, false);
      return true;
    }
    return false;
  }

  public static void unReg(int obj, boolean battle) {
    if (_fcFights.containsKey(Integer.valueOf(obj))) {
      return;
    }

    if (_fcPlayers.containsKey(Integer.valueOf(obj))) {
      Fighter temp = (Fighter)_fcPlayers.get(Integer.valueOf(obj));
      if (!battle) {
        sendLetter(obj, temp.item, false);
      }

      _fcPlayers.remove(Integer.valueOf(obj));
    }
  }

  public static boolean isRegged(int obj) {
    if (_fcPlayers.containsKey(Integer.valueOf(obj))) {
      return true;
    }

    return _fcFights.containsKey(Integer.valueOf(obj));
  }

  private static void sendLetter(int char_id, FcItem item, boolean victory)
  {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet result = null;
    TextBuilder text = new TextBuilder();
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("DELETE FROM `z_fc_players` WHERE `id`=?");
      st.setInt(1, char_id);
      st.execute();
      Close.S(st);

      if (Config.FC_INSERT_INVENTORY) {
        L2PcInstance player = L2World.getInstance().getPlayer(char_id);
        if (player == null) {
          GiveItem.insertOffline(con, char_id, item.id, item.count, item.enchant, 0, 0, "INVENTORY");
        } else {
          L2ItemInstance reward = player.getInventory().addItem("auc1", item.id, item.count, player, player.getTarget());
          if (reward == null) {
            return;
          }
          if ((item.enchant > 0) && (item.count == 1)) {
            reward.setEnchantLevel(item.enchant);
          }

          player.sendItems(true);
          player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(item.id));
        }
        return;
      }
      String item_name = item.name + "(" + item.count + ")(+" + item.enchant + ")";
      String augm = "";
      if (item.aug_skillId > 0) {
        augm = "<br1>" + getAugmentSkill(item.aug_skillId, item.aug_lvl);
      }

      String theme = "\u0412\u043E\u0437\u0432\u0440\u0430\u0442";
      if (victory) {
        theme = "\u041F\u043E\u0431\u0435\u0434\u0430!";
      }

      text.append("\u0418\u0442\u0435\u043C: <font color=FF3399>" + item_name + " <br>" + augm + "</font>.<br1>");
      text.append("\u0411\u043B\u0430\u0433\u043E\u0434\u0430\u0440\u0438\u043C \u0437\u0430 \u0441\u043E\u0442\u0440\u0443\u0434\u043D\u0438\u0447\u0435\u0441\u0442\u0432\u043E.");

      Date date = new Date();
      SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
      SimpleDateFormat timef = new SimpleDateFormat("HH:mm:ss");

      st = con.prepareStatement("INSERT INTO `z_bbs_mail` (`from`, `to`, `tema`, `text`, `datetime`, `read`, `item_id`, `item_count`, `item_ench`, `aug_hex`, `aug_id`, `aug_lvl`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      st.setInt(1, 334);
      st.setInt(2, char_id);
      st.setString(3, theme);
      st.setString(4, text.toString());
      st.setLong(5, System.currentTimeMillis());
      st.setInt(6, 0);
      st.setInt(7, item.id);
      st.setInt(8, item.count);
      st.setInt(9, item.enchant);
      st.setInt(10, item.aug_hex);
      st.setInt(11, item.aug_skillId);
      st.setInt(12, item.aug_lvl);
      st.execute();
    } catch (Exception e) {
      _log.warning("FightClub: sendLetter() error: " + e);
    } finally {
      Close.CSR(con, st, result);
      text.clear();
      text = null;
    }
  }

  private static String getAugmentSkill(int skillId, int skillLvl)
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

    return "<font color=336699>\u0410\u0443\u0433\u043C\u0435\u043D\u0442:</font> <font color=bef574>" + augName + " (" + type + ":" + skillLvl + "lvl)</font>";
  }

  private static void showError(L2PcInstance player, String errorText) {
    player.setFCItem(0, 0, 0, 0);
    player.sendHtmlMessage("\u041E\u0448\u0438\u0431\u043A\u0430", errorText);
    player.sendActionFailed();
  }

  public static void init() {
    _arenas.add(new FightClubArena(-20814, -21189, -3030));
    _arenas.add(new FightClubArena(-120324, -225077, -3331));
    _arenas.add(new FightClubArena(-102495, -209023, -3331));
    _arenas.add(new FightClubArena(-120156, -207378, -3331));
    _arenas.add(new FightClubArena(-87628, -225021, -3331));
    _arenas.add(new FightClubArena(-81705, -213209, -3331));
    _arenas.add(new FightClubArena(-87593, -207339, -3331));
    _arenas.add(new FightClubArena(-93709, -218304, -3331));
    _arenas.add(new FightClubArena(-77157, -218608, -3331));
    _arenas.add(new FightClubArena(-69682, -209027, -3331));
    _arenas.add(new FightClubArena(-76887, -201256, -3331));
    _arenas.add(new FightClubArena(-109985, -218701, -3331));
    _arenas.add(new FightClubArena(-126367, -218228, -3331));
    _arenas.add(new FightClubArena(-109629, -201292, -3331));
    _arenas.add(new FightClubArena(-87523, -240169, -3331));
    _arenas.add(new FightClubArena(-81748, -245950, -3331));
    _arenas.add(new FightClubArena(-77123, -251473, -3331));
    _arenas.add(new FightClubArena(-69778, -241801, -3331));
    _arenas.add(new FightClubArena(-76754, -234014, -3331));
    _arenas.add(new FightClubArena(-93742, -251032, -3331));
    _arenas.add(new FightClubArena(-87466, -257752, -3331));
    _arenas.add(new FightClubArena(-114413, -213241, -3331));

    _log.info("Fight Club - loaded " + _arenas.size() + "arenas.");
  }

  public static class StartFight
    implements Runnable
  {
    private int _id;

    public StartFight(int id)
    {
      _id = id;
    }

    public void run() {
      if (!FightClub._fcFights.containsKey(Integer.valueOf(_id))) {
        return;
      }

      FightClub.Contest temp = (FightClub.Contest)FightClub._fcFights.get(Integer.valueOf(_id));
      L2PcInstance fighter = L2World.getInstance().getPlayer(_id);
      int eid = temp.fighter2.obj_id;
      FightClubArena arena = temp.stadium;
      L2PcInstance enemy = L2World.getInstance().getPlayer(eid);
      if ((fighter == null) || (enemy == null)) {
        if (fighter != null) {
          FightClub.access$100(fighter, "\u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u0432 \u0438\u0433\u0440\u0435");
          fighter.sendCritMessage("\u0411\u043E\u0439\u0446\u043E\u0432\u0441\u043A\u0438\u0439 \u043A\u043B\u0443\u0431: \u0432\u043E\u0437\u0432\u0440\u0430\u0442 \u0441\u0442\u0430\u0432\u043A\u0438, \u043F\u0440\u043E\u0432\u0435\u0440\u044C \u043F\u043E\u0447\u0442\u0443");
          fighter.sendPacket(new ExMailArrived());
          fighter.teleToLocation(FightClub._tpLoc.x + Rnd.get(100), FightClub._tpLoc.y + Rnd.get(100), FightClub._tpLoc.x);
        }
        if (enemy != null) {
          FightClub.access$100(enemy, "\u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u0432 \u0438\u0433\u0440\u0435");
          enemy.sendCritMessage("\u0411\u043E\u0439\u0446\u043E\u0432\u0441\u043A\u0438\u0439 \u043A\u043B\u0443\u0431: \u0432\u043E\u0437\u0432\u0440\u0430\u0442 \u0441\u0442\u0430\u0432\u043A\u0438, \u043F\u0440\u043E\u0432\u0435\u0440\u044C \u043F\u043E\u0447\u0442\u0443");
          enemy.sendPacket(new ExMailArrived());
          enemy.teleToLocation(FightClub._tpLoc.x + Rnd.get(100), FightClub._tpLoc.y + Rnd.get(100), FightClub._tpLoc.x);
        }

        FightClub.access$200(_id, temp.fighter1.item, false);
        FightClub.access$200(eid, temp.fighter2.item, false);
        arena.setStadiaFree();
        FightClub._fcFights.remove(Integer.valueOf(_id));
        return;
      }

      SystemMessage sm = null;
      for (int i = 5; i > 0; i--) {
        sm = SystemMessage.id(SystemMessageId.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS).addNumber(i);
        fighter.sendPacket(sm);
        enemy.sendPacket(sm);
        try {
          Thread.sleep(1000L);
        }
        catch (InterruptedException e) {
        }
      }
      FastList _fighters = new FastList();
      _fighters.add(fighter);
      _fighters.add(enemy);

      sm = SystemMessage.id(SystemMessageId.LET_THE_DUEL_BEGIN);
      FastList.Node n = _fighters.head(); for (FastList.Node end = _fighters.tail(); (n = n.getNext()) != end; ) {
        L2PcInstance plyr = (L2PcInstance)n.getValue();

        plyr.setFightClub(true);
        plyr.setCurrentCp(plyr.getMaxCp());
        plyr.setCurrentHp(plyr.getMaxHp());
        plyr.setCurrentMp(plyr.getMaxMp());
        plyr.setTeam(2);
        plyr.setEventWait(false);

        plyr.setInsideZone(1, true);
        plyr.sendPacket(Static.ENTERED_COMBAT_ZONE);
        plyr.sendPacket(sm);
      }
      sm = null;

      boolean victory = false;
      for (int i = 0; i < FightClub._maxTime; i += 10000)
        try {
          Thread.sleep(10000L);
          if (FightClub.access$500(_id)) {
            victory = true;
            break;
          }
        }
        catch (InterruptedException e)
        {
        }
      FastList.Node n = _fighters.head(); for (FastList.Node end = _fighters.tail(); (n = n.getNext()) != end; ) {
        L2PcInstance plyr = (L2PcInstance)n.getValue();
        if (plyr == null)
        {
          continue;
        }
        if (!victory) {
          plyr.sendMessage("\u0411\u043E\u0439\u0446\u043E\u0432\u0441\u043A\u0438\u0439 \u043A\u043B\u0443\u0431: \u043F\u0440\u043E\u0432\u0435\u0440\u044C \u043F\u043E\u0447\u0442\u0443");
          plyr.sendPacket(new ExMailArrived());
          if (plyr.getObjectId() == temp.fighter1.obj_id)
            FightClub.access$200(plyr.getObjectId(), temp.fighter1.item, false);
          else {
            FightClub.access$200(plyr.getObjectId(), temp.fighter2.item, false);
          }
        }
        try
        {
          plyr.teleToLocation(116530 + Rnd.get(100), 76141 + Rnd.get(100), -2730);
        }
        catch (Exception e) {
        }
        if (plyr.isDead()) {
          plyr.doRevive();
        }

        plyr.setCurrentCp(plyr.getMaxCp());
        plyr.setCurrentHp(plyr.getMaxHp());
        plyr.setCurrentMp(plyr.getMaxMp());
        plyr.setChannel(1);
        plyr.setFClub(false);
        plyr.setFightClub(false);
        plyr.setEventWait(false);
        plyr.setInsideZone(1, false);
        plyr.sendPacket(Static.LEFT_COMBAT_ZONE);
        plyr.setTeam(0);
      }
      _fighters.clear();
      FightClub._fcFights.remove(Integer.valueOf(_id));
      arena.setStadiaFree();
    }
  }

  public static class Teleport
    implements Runnable
  {
    private int _id;

    public Teleport(int id)
    {
      _id = id;
    }

    public void run() {
      if (!FightClub._fcFights.containsKey(Integer.valueOf(_id))) {
        return;
      }

      FightClub.Contest temp = (FightClub.Contest)FightClub._fcFights.get(Integer.valueOf(_id));
      L2PcInstance fighter = L2World.getInstance().getPlayer(_id);
      int eid = temp.fighter2.obj_id;
      L2PcInstance enemy = L2World.getInstance().getPlayer(eid);
      if ((fighter == null) || (enemy == null)) {
        if (fighter != null) {
          FightClub.access$100(fighter, "\u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u0432 \u0438\u0433\u0440\u0435");
          fighter.sendCritMessage("\u0411\u043E\u0439\u0446\u043E\u0432\u0441\u043A\u0438\u0439 \u043A\u043B\u0443\u0431: \u0432\u043E\u0437\u0432\u0440\u0430\u0442 \u0441\u0442\u0430\u0432\u043A\u0438, \u043F\u0440\u043E\u0432\u0435\u0440\u044C \u043F\u043E\u0447\u0442\u0443");
          fighter.sendPacket(new ExMailArrived());
        }
        if (enemy != null) {
          FightClub.access$100(enemy, "\u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u0432 \u0438\u0433\u0440\u0435");
          enemy.sendCritMessage("\u0411\u043E\u0439\u0446\u043E\u0432\u0441\u043A\u0438\u0439 \u043A\u043B\u0443\u0431: \u0432\u043E\u0437\u0432\u0440\u0430\u0442 \u0441\u0442\u0430\u0432\u043A\u0438, \u043F\u0440\u043E\u0432\u0435\u0440\u044C \u043F\u043E\u0447\u0442\u0443");
          enemy.sendPacket(new ExMailArrived());
        }
        FightClub.access$200(_id, temp.fighter1.item, false);
        FightClub.access$200(eid, temp.fighter2.item, false);
        FightClub._fcFights.remove(Integer.valueOf(_id));
        return;
      }
      FightClubArena arena = temp.stadium;
      int[] coords = arena.getCoordinates();

      FastList _fighters = new FastList();
      _fighters.add(fighter);
      _fighters.add(enemy);

      ThreadPoolManager.getInstance().scheduleGeneral(new FightClub.StartFight(_id), 60000L);

      FastList.Node n = _fighters.head(); for (FastList.Node end = _fighters.tail(); (n = n.getNext()) != end; ) {
        L2PcInstance plyr = (L2PcInstance)n.getValue();

        plyr.setEventWait(true);
        plyr.setCurrentCp(plyr.getMaxCp());
        plyr.setCurrentHp(plyr.getMaxHp());
        plyr.setCurrentMp(plyr.getMaxMp());
        plyr.teleToLocation(coords[0] + Rnd.get(300), coords[1] + Rnd.get(300), coords[2]);

        plyr.sendCritMessage("\u0411\u0438\u0442\u0432\u0430 \u043D\u0430\u0447\u043D\u0435\u0442\u0441\u044F \u0447\u0435\u0440\u0435\u0437 \u043C\u0438\u043D\u0443\u0442\u0443, \u0431\u0430\u0444\u0444\u0430\u0439\u0441\u044F. \u0415\u0441\u043B\u0438 \u043F\u0440\u043E\u0442\u0438\u0432\u043D\u0438\u043A \u0432\u044B\u043B\u0435\u0442\u0435\u043B, \u0434\u043E\u0436\u0434\u0438\u0441\u044C \u043D\u0430\u0447\u0430\u043B\u0430 \u0431\u043E\u044F.");
        if (plyr.getClan() != null) {
          for (L2Skill skill : plyr.getClan().getAllSkills()) {
            plyr.removeSkill(skill, false);
          }

        }

        if (plyr.isCastingNow()) {
          plyr.abortCast();
        }

        plyr.setChannel(6);

        if (plyr.isHero()) {
          for (L2Skill skill : HeroSkillTable.getHeroSkills()) {
            plyr.removeSkill(skill, false);
          }

        }

        if (plyr.getPet() != null) {
          L2Summon summon = plyr.getPet();
          summon.stopAllEffects();

          if (summon.isPet()) {
            summon.unSummon(plyr);
          }
        }

        if (plyr.getCubics() != null) {
          for (L2CubicInstance cubic : plyr.getCubics().values()) {
            cubic.stopAction();
            plyr.delCubic(cubic.getId());
          }
          plyr.getCubics().clear();
        }

        if (plyr.getParty() != null) {
          plyr.getParty().removePartyMember(plyr);
        }

        plyr.sendSkillList();

        plyr.setCurrentCp(plyr.getMaxCp());
        plyr.setCurrentHp(plyr.getMaxHp());
        plyr.setCurrentMp(plyr.getMaxMp());
        SkillTable.getInstance().getInfo(1204, 2).getEffects(plyr, plyr);
        if (!plyr.isMageClass())
          SkillTable.getInstance().getInfo(1086, 2).getEffects(plyr, plyr);
        else {
          SkillTable.getInstance().getInfo(1085, 3).getEffects(plyr, plyr);
        }
        plyr.broadcastUserInfo();
      }
    }
  }

  public static class Contest
  {
    public FightClub.Fighter fighter1;
    public FightClub.Fighter fighter2;
    public FightClubArena stadium;

    public Contest(FightClub.Fighter fighter1, FightClub.Fighter fighter2, FightClubArena stadium)
    {
      this.fighter1 = fighter1;
      this.fighter2 = fighter2;
      this.stadium = stadium;
    }
  }

  public static class FcItem
  {
    public int id;
    public int count;
    public int enchant;
    public int aug_hex;
    public int aug_skillId;
    public int aug_lvl;
    public String name;
    public String icon;

    public FcItem(int id, int count, int enchant, int aug_hex, int aug_skillId, int aug_lvl, String name, String icon)
    {
      this.id = id;
      this.count = count;
      this.enchant = enchant;
      this.aug_hex = aug_hex;
      this.aug_skillId = aug_skillId;
      this.aug_lvl = aug_lvl;
      this.name = name;
      this.icon = icon;
    }
  }

  public static class Fighter
  {
    public int obj_id;
    public FightClub.FcItem item;
    public String pass;
    public int active;

    public Fighter(int obj_id, FightClub.FcItem item, String pass, int shadow)
    {
      this.obj_id = obj_id;
      this.item = item;
      this.pass = pass;
      active = shadow;
    }
  }
}