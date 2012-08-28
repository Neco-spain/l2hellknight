package scripts.communitybbs.Manager;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.util.Log;
import net.sf.l2j.util.TimeLogger;

public class AugmentBBSManager extends BaseBBSManager
{
  private static AugmentBBSManager _instance;

  public static void init()
  {
    _instance = new AugmentBBSManager();
  }

  public static AugmentBBSManager getInstance() {
    return _instance;
  }

  public void parsecmd(String command, L2PcInstance player)
  {
    if (command.equalsIgnoreCase("_bbstransaug")) {
      showIndex(player);
    } else if (command.startsWith("_bbstransaug_")) {
      String choise = command.substring(13).trim();
      if (choise.startsWith("show")) {
        int obj = Integer.parseInt(choise.substring(4).trim());
        show1Item(player, obj);
      } else if (choise.equalsIgnoreCase("step2")) {
        showNextItems(player);
      } else if (choise.startsWith("step3")) {
        int obj = Integer.parseInt(choise.substring(5).trim());
        show2Item(player, obj);
      } else if (choise.equalsIgnoreCase("finish")) {
        transFinish(player);
      }
    }
  }

  private void showIndex(L2PcInstance player) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getPwHtm("menu"));
    tb.append("&nbsp;&nbsp;\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u041B\u0421:<br>&nbsp;&nbsp;\u041E\u0442\u043A\u0443\u0434\u0430 \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0438\u043C?<br><br><table width=300>");

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

      if (item.isAugmented()) {
        L2Skill skill = item.getAugmentation().getAugmentSkill();
        if (skill == null)
        {
          continue;
        }
        tb.append("<tr><td><img src=\"" + itemIcon + "\" width=32 height=32></td><td><a action=\"bypass _bbstransaug_show " + objectId + "\">" + itemName + " (+" + enchantLevel + ")</a><br1>" + getAugmentSkill(skill.getId(), skill.getLevel()) + "</td></tr>");
      }
    }

    player.setTrans1Item(0);
    player.setTrans2Item(0);
    player.setTransAugment(0);

    tb.append("</table><br>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
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
    return "<font color=336699>\u0410\u0443\u0433\u043C\u0435\u043D\u0442:</font> <font color=bef574>" + augName + " (" + type + ":" + skillLvl + "lvl)</font>";
  }

  private void show1Item(L2PcInstance player, int objectId) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getPwHtm("menu"));
    L2ItemInstance item = player.getInventory().getItemByObjectId(objectId);
    if (item == null) {
      tb.append("&nbsp;&nbsp; \u041F\u0443\u0448\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430.");
      separateAndSend(tb.toString(), player);
      tb.clear();
      tb = null;
      return;
    }

    if (!item.isAugmented()) {
      tb.append("&nbsp;&nbsp; \u041F\u0443\u0448\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430.");
      separateAndSend(tb.toString(), player);
      tb.clear();
      tb = null;
      return;
    }

    tb.append("&nbsp;&nbsp;\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u041B\u0421:<br>&nbsp;&nbsp;\u0418\u0437 \u044D\u0442\u043E\u0439 \u043F\u0443\u0448\u043A\u0438 \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0438\u043C?<br>");
    tb.append("<table width=300><tr><td><img src=\"" + item.getItem().getIcon() + "\" width=32 height=32></td><td><font color=LEVEL>" + item.getItem().getName() + " (+" + item.getEnchantLevel() + ")</font><br></td></tr></table><br><br>");

    L2Skill augment = item.getAugmentation().getAugmentSkill();
    if (augment == null) {
      tb.append("&nbsp;&nbsp; \u041F\u0443\u0448\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430.");
      separateAndSend(tb.toString(), player);
      tb.clear();
      tb = null;
      return;
    }

    tb.append("<br>&nbsp;&nbsp;" + getAugmentSkill(augment.getId(), augment.getLevel()) + "<br>");

    L2ItemInstance coin = player.getInventory().getItemByItemId(Config.AUGMENT_COIN);
    if ((coin != null) && (coin.getCount() >= Config.AUGMENT_PRICE)) {
      tb.append("&nbsp;&nbsp; <font color=33CC00>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: " + Config.AUGMENT_PRICE + " " + Config.AUGMENT_COIN_NAME + ".</font><br>");
      tb.append("&nbsp;&nbsp;&nbsp;&nbsp;<button value=\"\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C\" action=\"bypass _bbstransaug_step2\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
      player.setTrans1Item(objectId);
      player.setTransAugment(augment.getId());
    } else {
      tb.append("&nbsp;&nbsp; <font color=FF6666>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: " + Config.AUGMENT_PRICE + " " + Config.AUGMENT_COIN_NAME + ".</font><br>");
      tb.append("&nbsp;&nbsp;&nbsp;&nbsp;<font color=999999>[\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C]</font>");
    }

    tb.append("</body></html>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
  }

  private void showNextItems(L2PcInstance player) {
    TextBuilder tb = new TextBuilder("<html><body>");
    tb.append(getPwHtm("menu"));
    tb.append("&nbsp;&nbsp;\u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u041B\u0421:<br>&nbsp;&nbsp;\u041A\u0443\u0434\u0430 \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0438\u043C?<br><br><table width=300>");
    int objectId = 0;
    String itemName = "";
    int enchantLevel = 0;
    int itemType = 0;
    String itemIcon = "";

    for (L2ItemInstance item : player.getInventory().getItems()) {
      if (item == null) {
        continue;
      }
      if (!item.canBeEnchanted())
      {
        continue;
      }
      objectId = item.getObjectId();
      itemName = item.getItem().getName();
      enchantLevel = item.getEnchantLevel();
      itemType = item.getItem().getType2();
      itemIcon = item.getItem().getIcon();

      if ((player.getTrans1Item() != objectId) && (itemType == 0) && (item.getItem().getItemGrade() >= 2) && (!item.isAugmented()) && (!item.isWear()) && (!item.isEquipped()) && (!item.isHeroItem()) && (item.isDestroyable())) {
        tb.append("<tr><td><img src=\"" + itemIcon + "\" width=32 height=32></td><td><a action=\"bypass _bbstransaug_step3 " + objectId + "\">" + itemName + " (+" + enchantLevel + ")</a></td></tr>");
      }
    }

    tb.append("</table><br></body></html>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
  }

  private void show2Item(L2PcInstance player, int objectId) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getPwHtm("menu"));
    L2ItemInstance item = player.getInventory().getItemByObjectId(objectId);
    if (item == null) {
      tb.append("&nbsp;&nbsp; \u041F\u0443\u0448\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430.");
      separateAndSend(tb.toString(), player);
      tb.clear();
      tb = null;
      return;
    }

    if (item.isAugmented()) {
      tb.append("&nbsp;&nbsp; \u041F\u0443\u0448\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430.");
      separateAndSend(tb.toString(), player);
      tb.clear();
      tb = null;
      return;
    }

    tb.append("&nbsp;&nbsp; \u041F\u0435\u0440\u0435\u043D\u043E\u0441 \u041B\u0421:<br>&nbsp;&nbsp;\u0412 \u044D\u0442\u0443 \u043F\u0443\u0448\u043A\u0443 \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0438\u043C?<br>");
    tb.append("<table width=300><tr><td><img src=\"" + item.getItem().getIcon() + "\" width=32 height=32></td><td><font color=LEVEL>" + item.getItem().getName() + " (+" + item.getEnchantLevel() + ")</font><br></td></tr></table><br><br>");

    L2ItemInstance coin = player.getInventory().getItemByItemId(Config.AUGMENT_COIN);
    if ((coin != null) && (coin.getCount() >= Config.AUGMENT_PRICE)) {
      tb.append("&nbsp;&nbsp; <font color=33CC00>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: " + Config.AUGMENT_PRICE + " " + Config.AUGMENT_COIN_NAME + ".</font><br>");
      tb.append("&nbsp;&nbsp;&nbsp;&nbsp;<button value=\"\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C\" action=\"bypass _bbstransaug_finish\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
      player.setTrans2Item(objectId);
    } else {
      tb.append("&nbsp;&nbsp; <font color=FF6666>\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: " + Config.AUGMENT_PRICE + " Ble Eva</font><br>");
      tb.append("&nbsp;&nbsp;&nbsp;&nbsp;<font color=999999>[\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C]</font>");
    }

    tb.append("</body></html>");
    separateAndSend(tb.toString(), player);
    tb.clear();
    tb = null;
  }

  private void transFinish(L2PcInstance player) {
    TextBuilder tb = new TextBuilder("");
    tb.append(getPwHtm("menu"));
    L2ItemInstance item1 = player.getInventory().getItemByObjectId(player.getTrans1Item());
    L2ItemInstance item2 = player.getInventory().getItemByObjectId(player.getTrans2Item());
    if ((item1 == null) || (item2 == null)) {
      tb.append("</body></html>");
      separateAndSend(tb.toString(), player);
      tb.clear();
      tb = null;
      return;
    }

    if ((item2.getItem().getItemGrade() < 2) || (item2.getItem().getType2() != 0) || (!item2.isDestroyable()) || (item2.isShadowItem())) {
      tb.append("&nbsp;&nbsp; \u041F\u0443\u0448\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430.");
      separateAndSend(tb.toString(), player);
      tb.clear();
      tb = null;
      return;
    }

    if (!item1.isAugmented()) {
      tb.append("&nbsp;&nbsp; \u041F\u0443\u0448\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430.");
      separateAndSend(tb.toString(), player);
      tb.clear();
      tb = null;
      return;
    }

    if (item2.isAugmented()) {
      tb.append("&nbsp;&nbsp; \u041F\u0443\u0448\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430.");
      separateAndSend(tb.toString(), player);
      tb.clear();
      tb = null;
      return;
    }

    L2ItemInstance coin = player.getInventory().getItemByItemId(Config.AUGMENT_COIN);
    if ((coin == null) || (coin.getCount() < Config.AUGMENT_PRICE)) {
      tb.append("&nbsp;&nbsp; \u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0430 " + Config.AUGMENT_PRICE + " " + Config.AUGMENT_COIN_NAME + ".");
      separateAndSend(tb.toString(), player);
      tb.clear();
      tb = null;
      return;
    }

    if (!player.destroyItemByItemId("bbsl24transaug", Config.AUGMENT_COIN, Config.AUGMENT_PRICE, player, true)) {
      tb.append("&nbsp;&nbsp; \u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u043F\u0435\u0440\u0435\u043D\u043E\u0441\u0430 " + Config.AUGMENT_PRICE + " " + Config.AUGMENT_COIN_NAME + ".");
      separateAndSend(tb.toString(), player);
      tb.clear();
      tb = null;
      return;
    }

    L2Skill augment = item1.getAugmentation().getAugmentSkill();
    if (augment == null) {
      tb.append("&nbsp;&nbsp; \u041F\u0443\u0448\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430.");
      separateAndSend(tb.toString(), player);
      tb.clear();
      tb = null;
      return;
    }

    if (player.getTransAugment() != augment.getId()) {
      tb.append("&nbsp;&nbsp; \u041F\u0443\u0448\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430.");
      separateAndSend(tb.toString(), player);
      tb.clear();
      tb = null;
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

    tb.append("<br>&nbsp;&nbsp; \u0410\u0443\u0433\u043C\u0435\u043D\u0442: <font color=bef574>" + augName + "" + type + "</font> <font color=33CC00>...\u043F\u0435\u0440\u0435\u043D\u0435\u0441\u0435\u043D!");

    player.sendItems(false);
    player.broadcastUserInfo();

    tb.append("</body></html>");
    separateAndSend(tb.toString(), player);
    Log.add(TimeLogger.getTime() + "player: " + player.getName() + "; augment: " + augName + " (id: " + augId + "; level: " + augLevel + "; effect: " + augEffId + "); weapon1: " + player.getTrans1Item() + "; weapon2: " + player.getTrans2Item() + ";", "augment_trans");

    player.setTrans1Item(0);
    player.setTrans2Item(0);
    player.setTransAugment(0);
    tb.clear();
    tb = null;
  }

  public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance player)
  {
  }
}