package net.sf.l2j.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.events.Heroes;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.AquireSkillList;
import net.sf.l2j.gameserver.network.serverpackets.AquireSkillList.skillType;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2DonateInstance extends L2FolkInstance
{
  private static int SETNAME = Config.COL_CHANGENAME;
  private static int SETNAMECOLOR = Config.COL_NICKCOLOR;
  private static int SETTITLECOLOR = Config.COL_TITLECOLOR;
  private static int SETCLANNAME = Config.COL_CHANGECLANNAME;
  private static int CLANLVL6 = Config.COL_6LVL_CLAN;
  private static int CLANLVL7 = Config.COL_7LVL_CLAN;
  private static int CLANLVL8 = Config.COL_8LVL_CLAN;
  private static int NOBLESS = Config.COL_NOBLESSE;
  private static int PREM1 = Config.COL_PREM1;
  private static int PREM2 = Config.COL_PREM2;
  private static int PREM3 = Config.COL_PREM3;
  private static int SEX = Config.COL_SEX;
  private static int PK = Config.COL_PK;
  private static int HERO = Config.COL_HERO;
  public static int CRP_ITEM_ID = Config.CRP_ITEM_ID;
  public static int ITEM_ID = Config.DON_ITEM_ID;
  String str = "";

  public static Calendar finishtime = Calendar.getInstance();

  public L2DonateInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";
    if (val == 0) pom = "" + npcId; else
      pom = npcId + "-" + val;
    return "data/html/donate/" + pom + ".htm";
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    StringTokenizer st = new StringTokenizer(command, " ");
    String actualCommand = st.nextToken();
    if (actualCommand.equalsIgnoreCase("Multisell"))
    {
      if (st.countTokens() < 1) return;
      int val = Integer.parseInt(st.nextToken());
      L2Multisell.getInstance().SeparateAndSend(val, player, false, getCastle().getTaxRate());
    }
    else if (actualCommand.equalsIgnoreCase("learn_skill"))
    {
      MyLearnSkill(player);
    }
    else if (actualCommand.equalsIgnoreCase("premadd1"))
    {
      if ((player.getInventory().getItemByItemId(ITEM_ID) == null) || (player.getInventory().getItemByItemId(ITEM_ID).getCount() < PREM1))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        return;
      }
      player.destroyItemByItemId("Consume", ITEM_ID, PREM1, player, false);
      try
      {
        addPremiumServices(1, player.getAccountName());
      }
      catch (StringIndexOutOfBoundsException e)
      {
        player.sendMessage("\u041E\u0448\u0438\u0431\u043A\u0430");
      }
      MagicSkillUser MSU = new MagicSkillUser(player, player, 2023, 1, 1, 0);
      player.sendPacket(MSU);
      player.broadcastPacket(MSU);
      player.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 \u043F\u0440\u0435\u043C\u0438\u0443\u043C \u0430\u043A\u043A\u0430\u0443\u043D\u0442 \u043D\u0430 1 \u043C\u0435\u0441\u044F\u0446. \u041D\u043E\u0432\u043E\u0435 \u043F\u043E\u043B\u0443\u0447\u0435\u043D\u0438\u0435 \u043D\u0435 \u0434\u043E\u0431\u0430\u0432\u043B\u044F\u0435\u0442 \u0432\u0440\u0435\u043C\u044F, \u0430 \u043E\u0431\u043D\u043E\u0432\u043B\u044F\u0435\u0442!");
      player.store();
    }
    else if (actualCommand.equalsIgnoreCase("premadd2"))
    {
      if ((player.getInventory().getItemByItemId(ITEM_ID) == null) || (player.getInventory().getItemByItemId(ITEM_ID).getCount() < PREM2))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        return;
      }
      player.destroyItemByItemId("Consume", ITEM_ID, PREM2, player, false);
      try
      {
        addPremiumServices(1, player.getAccountName());
      }
      catch (StringIndexOutOfBoundsException e)
      {
        player.sendMessage("\u041E\u0448\u0438\u0431\u043A\u0430");
      }
      MagicSkillUser MSU = new MagicSkillUser(player, player, 2023, 1, 1, 0);
      player.sendPacket(MSU);
      player.broadcastPacket(MSU);
      player.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 \u043F\u0440\u0435\u043C\u0438\u0443\u043C \u0430\u043A\u043A\u0430\u0443\u043D\u0442 \u043D\u0430 2 \u043C\u0435\u0441\u044F\u0446\u0430. \u041D\u043E\u0432\u043E\u0435 \u043F\u043E\u043B\u0443\u0447\u0435\u043D\u0438\u0435 \u043D\u0435 \u0434\u043E\u0431\u0430\u0432\u043B\u044F\u0435\u0442 \u0432\u0440\u0435\u043C\u044F, \u0430 \u043E\u0431\u043D\u043E\u0432\u043B\u044F\u0435\u0442!");
      player.store();
    }
    else if (actualCommand.equalsIgnoreCase("premadd3"))
    {
      if ((player.getInventory().getItemByItemId(ITEM_ID) == null) || (player.getInventory().getItemByItemId(ITEM_ID).getCount() < PREM1))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        return;
      }
      player.destroyItemByItemId("Consume", ITEM_ID, PREM3, player, false);
      try
      {
        addPremiumServices(1, player.getAccountName());
      }
      catch (StringIndexOutOfBoundsException e)
      {
        player.sendMessage("\u041E\u0448\u0438\u0431\u043A\u0430");
      }
      MagicSkillUser MSU = new MagicSkillUser(player, player, 2023, 1, 1, 0);
      player.sendPacket(MSU);
      player.broadcastPacket(MSU);
      player.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 \u043F\u0440\u0435\u043C\u0438\u0443\u043C \u0430\u043A\u043A\u0430\u0443\u043D\u0442 \u043D\u0430 3 \u043C\u0435\u0441\u044F\u0446\u0430. \u041D\u043E\u0432\u043E\u0435 \u043F\u043E\u043B\u0443\u0447\u0435\u043D\u0438\u0435 \u043D\u0435 \u0434\u043E\u0431\u0430\u0432\u043B\u044F\u0435\u0442 \u0432\u0440\u0435\u043C\u044F, \u0430 \u043E\u0431\u043D\u043E\u0432\u043B\u044F\u0435\u0442!");
      player.store();
    }
    else if (actualCommand.equalsIgnoreCase("Setname"))
    {
      if (st.countTokens() < 1) return;
      String newname = st.nextToken();
      if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < SETNAME)
      {
        player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E\u0435 \u043A\u043E\u043B-\u0432\u043E \u043C\u043E\u043D\u0435\u0442 \u0434\u043B\u044F \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438.");
        return;
      }
      if ((newname.length() < 3) || (newname.length() > 16))
      {
        player.sendMessage("\u042D\u0442\u043E \u0438\u043C\u044F \u043D\u0435 \u043C\u043E\u0436\u0435\u0442 \u0431\u044B\u0442\u044C \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u043D\u043E.");
        return;
      }
      if (CharNameTable.getInstance().doesCharNameExist(newname))
      {
        player.sendMessage("\u042D\u0442\u043E \u0438\u043C\u044F \u0443\u0436\u0435 \u0437\u0430\u043D\u044F\u0442\u043E.");
        return;
      }
      if (player.isClanLeader())
      {
        player.sendMessage("\u041F\u0435\u0440\u0435\u0434\u0430\u0439\u0442\u0435 \u043A\u043B\u0430\u043D \u043D\u0430 \u0432\u0440\u0435\u043C\u044F \u0441\u043C\u0435\u043D\u044B \u043D\u0438\u043A\u0430 \u0434\u0440\u0443\u0433\u043E\u043C\u0443 \u0438\u0433\u0440\u043E\u043A\u0443");
        return;
      }
      L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Setname " + newname + " for " + player.getName(), ITEM_ID, SETNAME, player, player);
      if (destritem != null)
      {
        player.setName(newname);
        player.sendMessage("\u0412\u044B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0441\u043C\u0435\u043D\u0438\u043B\u0438 \u0441\u0432\u043E\u0435 \u0438\u043C\u044F!");
        player.setClan(player.getClan());
        player.broadcastUserInfo();
        player.store();

        InventoryUpdate iu = new InventoryUpdate();
        if (destritem.getCount() == 0) iu.addRemovedItem(destritem); else
          iu.addModifiedItem(destritem);
        player.sendPacket(iu);
      }
      else
      {
        player.sendMessage("\u041E\u0448\u0438\u0431\u043A\u0430!");
      }
    }
    else if (actualCommand.equalsIgnoreCase("Setnamecolor"))
    {
      if (st.countTokens() < 1) return;
      String newcolor = st.nextToken();
      int color = 0;
      try
      {
        color = Integer.parseInt(newcolor);
      }
      catch (Exception e)
      {
        return;
      }
      newcolor = "";
      switch (color) {
      case 1:
        newcolor = "FFFF00";
        break;
      case 2:
        newcolor = "000000";
        break;
      case 3:
        newcolor = "FF0000";
        break;
      case 4:
        newcolor = "FF00FF";
        break;
      case 5:
        newcolor = "808080";
        break;
      case 6:
        newcolor = "008000";
        break;
      case 7:
        newcolor = "00FF00";
        break;
      case 8:
        newcolor = "800000";
        break;
      case 9:
        newcolor = "008080";
        break;
      case 10:
        newcolor = "800080";
        break;
      case 11:
        newcolor = "808000";
        break;
      case 12:
        newcolor = "FFFFFF";
        break;
      case 13:
        newcolor = "00FFFF";
        break;
      case 14:
        newcolor = "C0C0C0";
        break;
      case 15:
        newcolor = "17A0D4";
        break;
      default:
        return;
      }
      if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < SETNAMECOLOR)
      {
        player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E\u0435 \u043A\u043E\u043B-\u0432\u043E \u043C\u043E\u043D\u0435\u0442 \u0434\u043B\u044F \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438.");
        showServices(player);
        return;
      }
      L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Setnamecolor " + newcolor + " for " + player.getName(), ITEM_ID, SETNAMECOLOR, player, player);
      if (destritem != null)
      {
        player.sendMessage("\u0412\u044B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0438\u0437\u043C\u0435\u043D\u0438\u043B\u0438 \u0446\u0432\u0435\u0442 \u0438\u043C\u0435\u043D\u0438!");
        player.getAppearance().setNameColor(Integer.decode("0x" + newcolor).intValue(), false);
        player.broadcastUserInfo();
        player.store();
        showServices(player);
        InventoryUpdate iu = new InventoryUpdate();
        if (destritem.getCount() == 0) iu.addRemovedItem(destritem); else
          iu.addModifiedItem(destritem);
        player.sendPacket(iu);
      }
      else
      {
        player.sendMessage("\u041E\u0448\u0438\u0431\u043A\u0430!");
      }
    }
    else if (actualCommand.equalsIgnoreCase("Settitlecolor"))
    {
      if (st.countTokens() < 1) return;
      String newcolor = st.nextToken();
      int color = 0;
      try
      {
        color = Integer.parseInt(newcolor);
      }
      catch (Exception e)
      {
        return;
      }
      newcolor = "";
      switch (color) {
      case 1:
        newcolor = "FFFF00";
        break;
      case 2:
        newcolor = "000000";
        break;
      case 3:
        newcolor = "FF0000";
        break;
      case 4:
        newcolor = "FF00FF";
        break;
      case 5:
        newcolor = "808080";
        break;
      case 6:
        newcolor = "008000";
        break;
      case 7:
        newcolor = "00FF00";
        break;
      case 8:
        newcolor = "800000";
        break;
      case 9:
        newcolor = "008080";
        break;
      case 10:
        newcolor = "800080";
        break;
      case 11:
        newcolor = "808000";
        break;
      case 12:
        newcolor = "FFFFFF";
        break;
      case 13:
        newcolor = "00FFFF";
        break;
      case 14:
        newcolor = "C0C0C0";
        break;
      case 15:
        newcolor = "17A0D4";
        break;
      default:
        return;
      }
      if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < SETTITLECOLOR)
      {
        player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E\u0435 \u043A\u043E\u043B-\u0432\u043E \u043C\u043E\u043D\u0435\u0442 \u0434\u043B\u044F \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438.");
        showServices(player);
        return;
      }
      L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Settitlecolor " + newcolor + " for " + player.getName(), ITEM_ID, SETTITLECOLOR, player, player);
      if (destritem != null)
      {
        player.sendMessage("\u0412\u044B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0438\u0437\u043C\u0435\u043D\u0438\u043B\u0438 \u0446\u0432\u0435\u0442 \u0442\u0438\u0442\u0443\u043B\u0430!");
        player.getAppearance().setTitleColor(Integer.decode("0x" + newcolor).intValue());
        player.broadcastUserInfo();
        player.store();
        showServices(player);
        InventoryUpdate iu = new InventoryUpdate();
        if (destritem.getCount() == 0) iu.addRemovedItem(destritem); else
          iu.addModifiedItem(destritem);
        player.sendPacket(iu);
      }
      else
      {
        player.sendMessage("\u041E\u0448\u0438\u0431\u043A\u0430!");
      }
    }
    else if (actualCommand.equalsIgnoreCase("Setclanname"))
    {
      if (st.countTokens() < 1) return;
      String newname = st.nextToken();
      if (!player.isClanLeader())
      {
        player.sendMessage("\u0412\u044B \u043D\u0435 \u0433\u043B\u0430\u0432\u0430 \u043A\u043B\u0430\u043D\u0430. \u0422\u043E\u043B\u044C\u043A\u043E \u0433\u043B\u0430\u0432\u0430 \u043A\u043B\u0430\u043D\u0430 \u043C\u043E\u0436\u0435\u0442 \u044D\u0442\u043E \u0441\u0434\u0435\u043B\u0430\u0442\u044C.");
        showServices(player);
        return;
      }
      if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < SETCLANNAME)
      {
        player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E\u0435 \u043A\u043E\u043B-\u0432\u043E \u043C\u043E\u043D\u0435\u0442 \u0434\u043B\u044F \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438.");
        showServices(player);
        return;
      }
      if ((newname.length() < 3) || (newname.length() > 16))
      {
        player.sendMessage("\u042D\u0442\u043E \u0438\u043C\u044F \u043D\u0435 \u043C\u043E\u0436\u0435\u0442 \u0431\u044B\u0442\u044C \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u043D\u043E.");
        showServices(player);
        return;
      }
      if (ClanTable.getInstance().getClanByName(newname) != null)
      {
        player.sendMessage("\u042D\u0442\u043E \u0438\u043C\u044F \u0443\u0436\u0435 \u0437\u0430\u043D\u044F\u0442\u043E.");
        showServices(player);
        return;
      }
      L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Setclanname " + newname + " for " + player.getName(), ITEM_ID, SETCLANNAME, player, player);
      if (destritem != null)
      {
        player.getClan().setName(newname);
        player.getClan().updateClanInDB();
        player.getClan().broadcastClanStatus();
        player.sendMessage("\u0412\u044B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0441\u043C\u0435\u043D\u0438\u043B\u0438 \u0438\u043C\u044F \u043A\u043B\u0430\u043D\u0430!");
        showServices(player);
        InventoryUpdate iu = new InventoryUpdate();
        if (destritem.getCount() == 0) iu.addRemovedItem(destritem); else
          iu.addModifiedItem(destritem);
        player.sendPacket(iu);
      }
      else
      {
        player.sendMessage("\u041E\u0448\u0438\u0431\u043A\u0430!");
      }

    }
    else if (actualCommand.equalsIgnoreCase("Increaseclanlevel6"))
    {
      if (!player.isClanLeader())
      {
        player.sendMessage("\u0412\u044B \u043D\u0435 \u0433\u043B\u0430\u0432\u0430 \u043A\u043B\u0430\u043D\u0430. \u0422\u043E\u043B\u044C\u043A\u043E \u0433\u043B\u0430\u0432\u0430 \u043A\u043B\u0430\u043D\u0430 \u043C\u043E\u0436\u0435\u0442 \u044D\u0442\u043E \u0441\u0434\u0435\u043B\u0430\u0442\u044C.");
        showServices(player);
        return;
      }
      if (player.getClan().getLevel() != 5)
      {
        player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0432\u0435\u0440\u043D\u044B\u0439 \u0443\u0440\u043E\u0432\u0435\u043D\u044C \u043A\u043B\u0430\u043D\u0430. \u041C\u043E\u0436\u0435\u0442\u0435 \u0442\u043E\u043B\u044C\u043A\u043E \u0435\u0441\u043B\u0438 \u0443\u0440\u043E\u0432\u0435\u043D\u044C \u043A\u043B\u0430\u043D\u0430 5");
        showServices(player);
        return;
      }
      if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < CLANLVL6)
      {
        player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E\u0435 \u043A\u043E\u043B-\u0432\u043E \u043C\u043E\u043D\u0435\u0442 \u0434\u043B\u044F \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438.");
        showServices(player);
        return;
      }
      L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Setclanlvl 6 for " + player.getName(), ITEM_ID, CLANLVL6, player, player);
      if (destritem != null)
      {
        player.getClan().changeLevel(6);
        player.sendMessage("\u0412\u044B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u043F\u043E\u0432\u044B\u0441\u0438\u043B\u0438 \u0443\u0440\u043E\u0432\u0435\u043D\u044C \u043A\u043B\u0430\u043D\u0430!");
        showServices(player);
        InventoryUpdate iu = new InventoryUpdate();
        if (destritem.getCount() == 0) iu.addRemovedItem(destritem); else
          iu.addModifiedItem(destritem);
        player.sendPacket(iu);
      }
      else
      {
        player.sendMessage("\u041E\u0448\u0438\u0431\u043A\u0430!");
      }
    }
    else if (actualCommand.equalsIgnoreCase("Increaseclanlevel7"))
    {
      if (!player.isClanLeader())
      {
        player.sendMessage("\u0412\u044B \u043D\u0435 \u0433\u043B\u0430\u0432\u0430 \u043A\u043B\u0430\u043D\u0430. \u0422\u043E\u043B\u044C\u043A\u043E \u0433\u043B\u0430\u0432\u0430 \u043A\u043B\u0430\u043D\u0430 \u043C\u043E\u0436\u0435\u0442 \u044D\u0442\u043E \u0441\u0434\u0435\u043B\u0430\u0442\u044C.");
        showServices(player);
        return;
      }
      if (player.getClan().getLevel() != 6)
      {
        player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0432\u0435\u0440\u043D\u044B\u0439 \u0443\u0440\u043E\u0432\u0435\u043D\u044C \u043A\u043B\u0430\u043D\u0430. \u041C\u043E\u0436\u0435\u0442\u0435 \u0442\u043E\u043B\u044C\u043A\u043E \u0435\u0441\u043B\u0438 \u0443\u0440\u043E\u0432\u0435\u043D\u044C \u043A\u043B\u0430\u043D\u0430 6");
        showServices(player);
        return;
      }
      if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < CLANLVL7)
      {
        player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E\u0435 \u043A\u043E\u043B-\u0432\u043E \u043C\u043E\u043D\u0435\u0442 \u0434\u043B\u044F \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438.");
        showServices(player);
        return;
      }
      L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Setclanlvl 7 for " + player.getName(), ITEM_ID, CLANLVL7, player, player);
      if (destritem != null)
      {
        player.getClan().changeLevel(7);
        player.sendMessage("\u0412\u044B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u043F\u043E\u0432\u044B\u0441\u0438\u043B\u0438 \u0443\u0440\u043E\u0432\u0435\u043D\u044C \u043A\u043B\u0430\u043D\u0430!");
        showServices(player);
        InventoryUpdate iu = new InventoryUpdate();
        if (destritem.getCount() == 0) iu.addRemovedItem(destritem); else
          iu.addModifiedItem(destritem);
        player.sendPacket(iu);
      }
      else
      {
        player.sendMessage("\u041E\u0448\u0438\u0431\u043A\u0430!");
      }
    }
    else if (actualCommand.equalsIgnoreCase("Increaseclanlevel8"))
    {
      if (!player.isClanLeader())
      {
        player.sendMessage("\u0412\u044B \u043D\u0435 \u0433\u043B\u0430\u0432\u0430 \u043A\u043B\u0430\u043D\u0430. \u0422\u043E\u043B\u044C\u043A\u043E \u0433\u043B\u0430\u0432\u0430 \u043A\u043B\u0430\u043D\u0430 \u043C\u043E\u0436\u0435\u0442 \u044D\u0442\u043E \u0441\u0434\u0435\u043B\u0430\u0442\u044C.");
        showServices(player);
        return;
      }
      if (player.getClan().getLevel() != 7)
      {
        player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0432\u0435\u0440\u043D\u044B\u0439 \u0443\u0440\u043E\u0432\u0435\u043D\u044C \u043A\u043B\u0430\u043D\u0430. \u041C\u043E\u0436\u0435\u0442\u0435 \u0442\u043E\u043B\u044C\u043A\u043E \u0435\u0441\u043B\u0438 \u0443\u0440\u043E\u0432\u0435\u043D\u044C \u043A\u043B\u0430\u043D\u0430 7");
        showServices(player);
        return;
      }
      if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < CLANLVL8)
      {
        player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E\u0435 \u043A\u043E\u043B-\u0432\u043E \u043C\u043E\u043D\u0435\u0442 \u0434\u043B\u044F \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438.");
        showServices(player);
        return;
      }
      L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: Setclanlvl 8 for " + player.getName(), ITEM_ID, CLANLVL8, player, player);
      if (destritem != null)
      {
        player.getClan().changeLevel(8);
        player.sendMessage("\u0412\u044B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u043F\u043E\u0432\u044B\u0441\u0438\u043B\u0438 \u0443\u0440\u043E\u0432\u0435\u043D\u044C \u043A\u043B\u0430\u043D\u0430!");
        showServices(player);

        InventoryUpdate iu = new InventoryUpdate();
        if (destritem.getCount() == 0) iu.addRemovedItem(destritem); else
          iu.addModifiedItem(destritem);
        player.sendPacket(iu);
      }
      else
      {
        player.sendMessage("\u041E\u0448\u0438\u0431\u043A\u0430!");
      }

    }
    else if (actualCommand.equalsIgnoreCase("nobless"))
    {
      if (player.getInventory().getInventoryItemCount(ITEM_ID, 0) < NOBLESS)
      {
        player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E\u0435 \u043A\u043E\u043B-\u0432\u043E \u043C\u043E\u043D\u0435\u0442 \u0434\u043B\u044F \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438.");
        showServices(player);
        return;
      }
      L2ItemInstance destritem = player.getInventory().destroyItemByItemId(" GoldMerchant: NOBLESS for " + player.getName(), ITEM_ID, NOBLESS, player, player);
      if (destritem != null)
      {
        if (player.isNoble())
        {
          player.sendMessage("\u0412\u044B \u0443\u0436\u0435 \u043D\u043E\u0431\u043B\u0435\u0441\u0441");
          showServices(player);
          return;
        }
        player.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 \u043F\u0440\u043E\u0445\u043E\u0436\u0434\u0435\u043D\u0438\u044F \u043A\u0432\u0435\u0441\u0442\u043E\u0432 \u0434\u043B\u044F \u043D\u043E\u0431\u043B\u0435\u0441\u0441\u0430! \u041F\u043E\u0437\u0434\u0440\u0430\u0432\u043B\u044F\u0435\u043C! \u0422\u0435\u043F\u0435\u0440\u044C \u0412\u044B \u043D\u043E\u0431\u043B\u0435\u0441\u0441!");
        player.setNoble(true);
        player.addItem(" GoldMerchant: NOBLESS", 7694, 1, this, true);
        InventoryUpdate iu = new InventoryUpdate();
        if (destritem.getCount() == 0) iu.addRemovedItem(destritem); else
          iu.addModifiedItem(destritem);
        player.sendPacket(iu);
      }
      else
      {
        player.sendMessage("\u041E\u0448\u0438\u0431\u043A\u0430!");
        showServices(player);
      }

    }
    else if (actualCommand.equalsIgnoreCase("sex"))
    {
      if ((player.getInventory().getItemByItemId(ITEM_ID) == null) || (player.getInventory().getItemByItemId(ITEM_ID).getCount() < SEX))
      {
        player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E\u0435 \u043A\u043E\u043B-\u0432\u043E \u043C\u043E\u043D\u0435\u0442 \u0434\u043B\u044F \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438");
        showServices(player);
        return;
      }
      player.destroyItemByItemId("Consume", ITEM_ID, SEX, player, false);
      player.getAppearance().setSex(!player.getAppearance().getSex());
      player.decayMe();
      player.spawnMe(player.getX(), player.getY(), player.getZ());
      player.broadcastUserInfo();
      L2PcInstance.savePlayerSex(player, 1);
      player.sendMessage("\u0412\u044B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0441\u043C\u0435\u043D\u0438\u043B\u0438 \u0441\u0432\u043E\u0439 \u043F\u043E\u043B.");
      showServices(player);
    }
    else if (actualCommand.equalsIgnoreCase("pk"))
    {
      if ((player.getInventory().getItemByItemId(ITEM_ID) != null) && (player.getInventory().getItemByItemId(ITEM_ID).getCount() >= PK))
      {
        int inipkKills = player.getPkKills();
        if (inipkKills == 0)
        {
          player.sendMessage("\u0423 \u0432\u0430\u0441 \u043D\u0435\u0442 PK");
          return;
        }
        player.destroyItemByItemId("Consume", ITEM_ID, PK, player, false);
        player.setPkKills(0);
        if (player.getKarma() > 0)
        {
          player.setKarma(0);
        }
        player.sendPacket(new UserInfo(player));
        player.sendMessage("\u0412\u0430\u0448 \u0441\u0447\u0435\u0442\u0447\u0438\u043A PK \u043E\u0431\u043D\u0443\u043B\u0435\u043D");
        showServices(player);
      }
      else
      {
        player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E\u0435 \u043A\u043E\u043B-\u0432\u043E \u043C\u043E\u043D\u0435\u0442 \u0434\u043B\u044F \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438");
      }

    }
    else if (actualCommand.startsWith("hero"))
    {
      setHero(player, Integer.parseInt(st.nextToken()));
    }
    else if (actualCommand.startsWith("usl"))
    {
      showServices(player);
    }
    else if (actualCommand.startsWith("crp"))
    {
      if ((player.getClan() != null) && (player.getObjectId() == player.getClan().getLeaderId()))
      {
        if ((player.getInventory().getItemByItemId(CRP_ITEM_ID) != null) && (player.getInventory().getItemByItemId(CRP_ITEM_ID).getCount() >= Config.COL_CRP))
        {
          player.getClan().setReputationScore(player.getClan().getReputationScore() + Config.CRP_COUNT, true);
          player.sendMessage("\u0412\u0430\u0448 \u043A\u043B\u0430\u043D \u043F\u043E\u043B\u0443\u0447\u0438\u043B " + Config.CRP_COUNT + " \u043E\u0447\u043A\u043E\u0432 \u0440\u0435\u043F\u0443\u0442\u0430\u0446\u0438\u0438! \u0421\u0434\u0435\u043B\u0430\u0439\u0442\u0435 \u0440\u0435\u043B\u043E\u0433");
          player.destroyItemByItemId("Consume", CRP_ITEM_ID, Config.COL_CRP, player, false);
          showServices(player);
        }
        else
        {
          player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E\u0435 \u043A\u043E\u043B-\u0432\u043E \u043C\u043E\u043D\u0435\u0442 \u0434\u043B\u044F \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438");
          showServices(player);
        }
      }
      else
      {
        player.sendMessage("\u0412\u044B \u043D\u0435 \u044F\u0432\u043B\u044F\u0435\u0442\u0435\u0441\u044C \u043B\u0438\u0434\u0435\u0440\u043E\u043C \u043A\u043B\u0430\u043D\u0430");
        showServices(player);
      }

    }
    else
    {
      super.onBypassFeedback(player, command);
    }
  }

  public void showServices(L2PcInstance activeChar)
  {
    NpcHtmlMessage html = new NpcHtmlMessage(1);
    String file = "data/html/donate/" + getNpcId() + "-1.htm";
    html.setFile(file);
    sendHtmlMessage(activeChar, html);
    activeChar.sendPacket(new ActionFailed());
    html.replace("%crpcount%", str + Config.CRP_COUNT);
    html.replace("%colcount%", str + Config.COL_CRP);
    html.replace("%5-6%", str + Config.COL_6LVL_CLAN);
    html.replace("%6-7%", str + Config.COL_7LVL_CLAN);
    html.replace("%7-8%", str + Config.COL_8LVL_CLAN);
    html.replace("%namechange%", str + Config.COL_CHANGENAME);
    html.replace("%namecolor%", str + Config.COL_NICKCOLOR);
    html.replace("%titlecolor%", str + Config.COL_TITLECOLOR);
    html.replace("%hero%", str + Config.COL_HERO);
    html.replace("%pk%", str + Config.COL_PK);
    html.replace("%sex%", str + Config.COL_SEX);
    html.replace("%nooble%", str + Config.COL_NOBLESSE);
  }

  private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html) {
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcId%", String.valueOf(getNpcId()));
    player.sendPacket(html);
  }

  public static void addPremiumServices(int Day, String AccName)
  {
    Connection con = null;
    try
    {
      finishtime.setTimeInMillis(System.currentTimeMillis());
      finishtime.set(13, 0);
      finishtime.add(5, Day);

      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE account_premium SET premium_service=?,enddate=? WHERE account_name=?");
      statement.setInt(1, 1);
      statement.setLong(2, finishtime.getTimeInMillis());
      statement.setString(3, AccName);
      statement.execute();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.info("PremiumService: Could not increase data");
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (SQLException e)
      {
      }
    }
  }

  private void setHero(L2PcInstance player, int days)
  {
    if ((player.getInventory().getItemByItemId(ITEM_ID) != null) && (player.getInventory().getItemByItemId(ITEM_ID).getCount() >= days * HERO))
    {
      if ((days != 0) && (days > 0))
      {
        if (player.isHero())
        {
          player.sendMessage("\u0412\u044B \u0443\u0436\u0435 \u0433\u0435\u0440\u043E\u0439");
          return;
        }
        Heroes.getInstance().addHero(player, days);
        player.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 \u0441\u0442\u0430\u0442\u0443\u0441 \u0433\u0435\u0440\u043E\u044F \u043D\u0430 " + days + " \u0434\u043D\u0435\u0439!");
      }
      else
      {
        player.sendMessage("\u0412\u044B \u043D\u0435 \u0432\u0432\u0435\u043B\u0438 \u043A\u043E\u043B-\u0432\u043E \u0434\u043D\u0435\u0439!");
      }
      player.destroyItemByItemId("Consume", ITEM_ID, days * HERO, player, false);
    }
    else
    {
      player.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435 \u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E\u0435 \u043A\u043E\u043B-\u0432\u043E \u043C\u043E\u043D\u0435\u0442 \u0434\u043B\u044F \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u043E\u043F\u0435\u0440\u0430\u0446\u0438\u0438");
    }
  }

  public void MyLearnSkill(L2PcInstance player)
  {
    if (!Config.ENABLE_MY_SKILL_LEARN) return;

    AquireSkillList asl = new AquireSkillList(AquireSkillList.skillType.Fishing);

    int counts = 0;

    int iter = 0;
    for (L2SkillLearn s : Config.MY_L2SKILL_LEARN)
    {
      int class_id = ((Integer)Config.MY_L2SKILL_CLASS_ID.get(iter)).intValue();
      iter++;

      L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

      if (sk == null) {
        continue;
      }
      boolean CantTeach = false;

      for (L2Skill skill : player.getAllSkills()) {
        if ((skill.getId() != sk.getId()) || (sk.getLevel() > skill.getLevel()))
          continue;
        CantTeach = true;
        break;
      }

      if ((CantTeach) || (
        (class_id != -1) && 
        (class_id != player.getClassId().getId()))) {
        continue;
      }
      counts++;
      asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), 0, 1);
    }

    if (counts == 0)
    {
      NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
      TextBuilder sb = new TextBuilder();
      sb.append("<html><head><body>");
      sb.append("You've learned all skills.<br>");
      sb.append("</body></html>");
      html.setHtml(sb.toString());
      player.sendPacket(html);
    }
    else
    {
      player.sendPacket(asl);
    }
  }
}