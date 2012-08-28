package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Map;
import javolution.text.TextBuilder;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.Config.EventReward;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.CustomServerData;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.ClassLevel;
import net.sf.l2j.gameserver.model.base.PlayerClass;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Log;

public final class L2ClassMasterInstance extends L2FolkInstance
{
  private static final int[] SECONDN_CLASS_IDS = { 2, 3, 5, 6, 9, 8, 12, 13, 14, 16, 17, 20, 21, 23, 24, 27, 28, 30, 33, 34, 36, 37, 40, 41, 43, 46, 48, 51, 52, 55, 57 };

  private final int CLAN_COIN = Config.MCLAN_COIN;
  private final String CLAN_COIN_NAME = Config.MCLAN_COIN_NAME;
  private final int CLAN_LVL6 = Config.CLAN_LVL6;
  private final int CLAN_LVL7 = Config.CLAN_LVL7;
  private final int CLAN_LVL8 = Config.CLAN_LVL8;

  public L2ClassMasterInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;

    if (player.isCursedWeaponEquiped())
    {
      player.sendActionFailed();
      return;
    }

    if (getObjectId() != player.getTargetId())
    {
      player.setTarget(this);

      player.sendPacket(new MyTargetSelected(getObjectId(), 0));

      player.sendPacket(new ValidateLocation(this));
    }
    else
    {
      if (!canInteract(player))
      {
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
        return;
      }

      NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
      html.setFile("data/html/classmaster/index.htm");
      html.replace("%objectId%", String.valueOf(getObjectId()));
      html.replace("%config_master_npcname%", Config.MASTER_NPCNAME);
      player.sendPacket(html);
    }
    player.sendActionFailed();
  }

  public String getHtmlPath(int npcId, int val)
  {
    return "data/html/classmaster/" + val + ".htm";
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.equalsIgnoreCase("class_master"))
    {
      ClassId classId = player.getClassId();
      int jobLevel = 0;
      int level = player.getLevel();
      ClassLevel lvl = PlayerClass.values()[classId.getId()].getLevel();
      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$base$ClassLevel[lvl.ordinal()])
      {
      case 1:
        jobLevel = 1;
        break;
      case 2:
        jobLevel = 2;
        break;
      default:
        jobLevel = 3;
      }

      if (!Config.ALLOW_CLASS_MASTERS) {
        jobLevel = 3;
      }
      if (((level >= 20) && (jobLevel == 1)) || ((level >= 40) && (jobLevel == 2) && (Config.ALLOW_CLASS_MASTERS)))
      {
        showChatWindow(player, classId.getId());
      }
      else if ((level >= 76) && (Config.ALLOW_CLASS_MASTERS) && (classId.getId() < 88))
      {
        for (int i = 0; i < SECONDN_CLASS_IDS.length; i++)
        {
          if (classId.getId() != SECONDN_CLASS_IDS[i])
            continue;
          NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
          TextBuilder sb = new TextBuilder();
          sb.append("<html><body><table width=200>");
          sb.append("<tr><td><br></td></tr>");
          sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_change_class " + (88 + i) + "\">\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C \u0437\u0430 " + CharTemplateTable.getClassNameById(88 + i) + "</a></td></tr>");
          sb.append("<tr><td><br></td></tr>");
          sb.append("</table></body></html>");
          html.setHtml(sb.toString());
          sb.clear();
          sb = null;
          player.sendPacket(html);
          break;
        }

      }
      else
      {
        NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
        TextBuilder sb = new TextBuilder();
        sb.append("<html><body>");
        switch (jobLevel)
        {
        case 1:
          sb.append("\u041F\u0440\u0438\u0445\u043E\u0434\u0438\u0442\u0435, \u043A\u043E\u0433\u0434\u0430 \u043F\u043E\u043B\u0443\u0447\u0438\u0442\u0435 20 \u0443\u0440\u043E\u0432\u0435\u043D\u044C.<br>");
          break;
        case 2:
          sb.append("\u041F\u0440\u0438\u0445\u043E\u0434\u0438\u0442\u0435, \u043A\u043E\u0433\u0434\u0430 \u043F\u043E\u043B\u0443\u0447\u0438\u0442\u0435 40 \u0443\u0440\u043E\u0432\u0435\u043D\u044C.<br>");
          break;
        case 3:
          sb.append("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u043D\u044B\u0445 \u043F\u0440\u043E\u0444\u0435\u0441\u0441\u0438\u0439.<br>");
        }

        sb.append("</body></html>");
        html.setHtml(sb.toString());
        sb.clear();
        sb = null;
        player.sendPacket(html);
      }
    }
    else if (command.equalsIgnoreCase("clan_level"))
    {
      if (!player.isClanLeader())
      {
        player.sendPacket(Static.WAR_NOT_LEADER);
        return;
      }

      if (player.getClan().getLevel() < 5)
      {
        player.sendPacket(Static.CLAN_5LVL_HIGHER);
        return;
      }

      L2Clan clan = player.getClan();
      int level = clan.getLevel();
      NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
      TextBuilder replyMSG = new TextBuilder("<html><body>");
      replyMSG.append("\u041F\u043E\u0432\u044B\u0448\u0435\u043D\u0438\u0435 \u0443\u0440\u043E\u0432\u043D\u044F \u043A\u043B\u0430\u043D\u0430:<br1>");
      if (level < 8)
      {
        switch (level)
        {
        case 5:
          replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_clanLevel_6\">6 \u0443\u0440\u043E\u0432\u0435\u043D\u044C</a> (" + CLAN_LVL6 + " " + CLAN_COIN_NAME + ")<br>");

          break;
        case 6:
          replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_clanLevel_7\">7 \u0443\u0440\u043E\u0432\u0435\u043D\u044C.</a> (" + CLAN_LVL7 + " " + CLAN_COIN_NAME + ")<br>");

          break;
        case 7:
          replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_clanLevel_8\">8 \u0443\u0440\u043E\u0432\u0435\u043D\u044C.</a> (" + CLAN_LVL8 + " " + CLAN_COIN_NAME + ")<br>");
        }
      }
      else
      {
        replyMSG.append("<font color=66CC00>\u0423\u0436\u0435 \u043C\u0430\u043A\u0441\u0438\u043C\u0430\u043B\u044C\u043D\u044B\u0439!</font><br>");
      }replyMSG.append("</body></html>");
      reply.setHtml(replyMSG.toString());
      player.sendPacket(reply);
    }
    else if (command.startsWith("change_class"))
    {
      int val = Integer.parseInt(command.substring(13));

      ClassId classId = player.getClassId();
      int level = player.getLevel();
      int jobLevel = 0;
      int newJobLevel = 0;

      ClassLevel lvlnow = PlayerClass.values()[classId.getId()].getLevel();

      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$base$ClassLevel[lvlnow.ordinal()])
      {
      case 1:
        jobLevel = 1;
        break;
      case 2:
        jobLevel = 2;
        break;
      case 3:
        jobLevel = 3;
        break;
      default:
        jobLevel = 4;
      }

      if (jobLevel == 4) {
        return;
      }
      ClassLevel lvlnext = PlayerClass.values()[val].getLevel();
      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$base$ClassLevel[lvlnext.ordinal()])
      {
      case 1:
        newJobLevel = 1;
        break;
      case 2:
        newJobLevel = 2;
        break;
      case 3:
        newJobLevel = 3;
        break;
      default:
        newJobLevel = 4;
      }

      if (newJobLevel != jobLevel + 1) {
        return;
      }
      if ((level < 20) && (newJobLevel > 1))
        return;
      if ((level < 40) && (newJobLevel > 2))
        return;
      if ((level < 75) && (newJobLevel > 3)) {
        return;
      }

      Config.EventReward pay = (Config.EventReward)Config.CLASS_MASTERS_PRICES.get(Integer.valueOf(newJobLevel));
      if (pay != null)
      {
        if (player.getItemCount(pay.id) < pay.count)
        {
          player.sendHtmlMessage("Class Master", "C\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u043F\u043E\u043B\u0443\u0447\u0435\u043D\u0438\u044F \u043F\u0440\u043E\u0444\u044B " + pay.count + " " + ItemTable.getInstance().getTemplate(pay.id).getName() + "!");
          return;
        }
        player.destroyItemByItemId("clasmaster", pay.id, pay.count, player, true);
      }

      changeClass(player, val);
      player.checkAllowedSkills();

      if (val >= 88)
        player.sendPacket(Static.THIRD_CLASS_TRANSFER);
      else {
        player.sendPacket(Static.CLASS_TRANSFER);
      }
      NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
      TextBuilder sb = new TextBuilder();
      sb.append("<html><body>");
      sb.append("\u041F\u043E\u043B\u0443\u0447\u0435\u043D\u0430 \u043F\u0440\u043E\u0444\u0435\u0441\u0441\u0438\u044F <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(player.getClassId().getId()) + "</font>.");
      if (Config.REWARD_SHADOW)
      {
        player.setShadeItems(true);
        if ((newJobLevel == 3) && (level >= 40))
        {
          sb.append("<br>\u0412\u044B\u0431\u0435\u0440\u0438 \u0436\u0435\u043B\u0430\u0435\u043C\u044B\u0439 \u0441\u0435\u0442:<br>");
          sb.append("<table width=300><tr><td><a action=\"bypass -h npc_" + getObjectId() + "_getArmor 1\">Avadon Robe Set</a><br1><font color=666666>//P. Def. +5.26% and Casting Spd. +15%.</font></td></tr>");
          sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_getArmor 2\">Leather Armor of Doom</a><br1><font color=666666>//P. Atk. +2.7%, MP recovery rate +2.5%, STR -1, CON -2, DEX +3.</font></td></tr>");
          sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_getArmor 3\">Doom Plate Armor</a><br1><font color=666666>//Maximum HP +320, Breath Gauge increase, STR-3, and CON+3.</font></td></tr>");
          sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_getArmor 4\">Blue Wolf Breastplate</a><br1><font color=666666>//Speed +7, and HP recovery rate +5.24%, STR+3, CON-1, and DEX-2.</font></td></tr></table><br>");
        }

        if (val >= 88)
        {
          sb.append("<table width=300><tr><td><a action=\"bypass -h npc_" + getObjectId() + "_getArmor 5\">Robe Flame Armor</a><br1><font color=666666>//CP + 177, MP + 400, C.Spd 15%, M.Atk 15%, M.Def/P.Def 4%.</font></td></tr>");
          sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_getArmor 6\">Light Flame Armor</a><br1><font color=666666>//CP + 195, HP/MP + 200, Crit.Dmg 25%, Atk.Spd 10% M.Def/P.Def 8%.</font></td></tr>");
          sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_getArmor 7\">Heavy Flame Armor</a><br1><font color=666666>//CP + 232, HP + 400, Atk.Dmg 15%, Atk.Spd 15%, M.Def/P.Def 12%.</font></td></tr></table><br>");
        }
      }
      sb.append("</body></html>");
      html.setHtml(sb.toString());
      sb.clear();
      sb = null;
      player.sendPacket(html);

      if (Config.VS_CKEY_CHARLEVEL)
        player.setUserKeyOnLevel();
    }
    else if (command.startsWith("clanLevel_"))
    {
      int level = Integer.parseInt(command.substring(10).trim());
      clanSetLevel(player, level);
    }
    else if (command.startsWith("getArmor"))
    {
      int val = Integer.parseInt(command.substring(9));

      if (player.getShadeItems()) {
        return;
      }
      player.setShadeItems(false);

      Inventory inventory = player.getInventory();
      int[] shadowSet = CustomServerData.getInstance().getShadeItems(val);
      for (int i = 0; i < shadowSet.length; i++)
      {
        L2ItemInstance item = ItemTable.getInstance().createItem("China3", shadowSet[i], 1, player, null);
        if (val < 5) {
          item.setEnchantLevel(30);
        }
        else {
          item.setEnchantLevel(37);
          item.setMana(180);
        }
        inventory.addItem("China3", item, player, null);
        inventory.equipItemAndRecord(item);
        item.decreaseMana(true);
      }
      if (val >= 5) {
        player.addItem("China3", 50009, 1, player, true);
      }
      player.sendItems(true);
    }
    else {
      super.onBypassFeedback(player, command);
    }
  }

  private void clanSetLevel(L2PcInstance player, int level) {
    if (CLAN_COIN > 0)
    {
      int price = 99999;
      switch (level)
      {
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
      if ((coin == null) || (coin.getCount() < price))
      {
        player.sendMessage("\u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C");
        return;
      }

      if (!player.destroyItemByItemId("DS clanSetLevel", CLAN_COIN, price, player, true))
      {
        player.sendMessage("\u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u0441\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C");
        return;
      }
      Log.addDonate(player, "Clan Level: " + level, price);
    }

    player.getClan().changeLevel(level);
    player.sendMessage("\u0423\u0440\u043E\u0432\u0435\u043D\u044C \u043A\u043B\u0430\u043D\u0430 \u0443\u0432\u0435\u043B\u0438\u0447\u0435\u043D \u0434\u043E " + level);
  }

  private void changeClass(L2PcInstance player, int val)
  {
    player.abortAttack();
    player.abortCast();
    player.setIsParalyzed(true);
    try
    {
      Thread.sleep(100L);
    }
    catch (InterruptedException e)
    {
    }
    player.setClassId(val);
    if (player.isSubClassActive())
      ((SubClass)player.getSubClasses().get(Integer.valueOf(player.getClassIndex()))).setClassId(player.getActiveClass());
    else {
      player.setBaseClass(player.getActiveClass());
    }
    player.rewardSkills();
    player.store();
    player.broadcastUserInfo();
    player.setIsParalyzed(false);
  }
}