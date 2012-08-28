package net.sf.l2j.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2TeleportLocation;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.ClanHall.ClanHallFunction;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.BuyList;
import net.sf.l2j.gameserver.network.serverpackets.ClanHallDecoration;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.network.serverpackets.WareHouseDepositList;
import net.sf.l2j.gameserver.network.serverpackets.WareHouseWithdrawalList;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.FloodProtector;

public class L2ClanHallManagerInstance extends L2FolkInstance
{
  protected static final int COND_OWNER_FALSE = 0;
  protected static final int COND_ALL_FALSE = 1;
  protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 2;
  protected static final int COND_OWNER = 3;
  private int _clanHallId = -1;

  public L2ClanHallManagerInstance(int objectId, L2NpcTemplate template)
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

    if (Config.DEBUG) _log.fine("Showing stored items");
    player.sendPacket(new WareHouseWithdrawalList(player, 1));
  }

  private void showDepositWindow(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    player.setActiveWarehouse(player.getWarehouse());
    player.tempInvetoryDisable();
    if (Config.DEBUG) _log.fine("Showing items to deposit");

    player.sendPacket(new WareHouseDepositList(player, 1));
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.startsWith("WithdrawP"))
    {
      showRetrieveWindow(player);
    }
    else if (command.equals("DepositP"))
    {
      showDepositWindow(player);
    }
    else if (command.startsWith("Subclass"))
    {
      int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());

      if ((player.isCastingNow()) || (player.isAllSkillsDisabled()))
      {
        player.sendPacket(new SystemMessage(SystemMessageId.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE));
        return;
      }

      TextBuilder content = new TextBuilder("<html><body>");
      NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

      int paramOne = 0;
      try
      {
        int endIndex = command.length();

        if (command.length() > 13)
        {
          endIndex = 13;
        }

        paramOne = Integer.parseInt(command.substring(11, endIndex).trim());
      }
      catch (Exception NumberFormatException)
      {
      }
      Iterator subList;
      switch (cmdChoice)
      {
      case 2:
        if ((Olympiad.getInstance().isRegisteredInComp(player)) || (player.getOlympiadGameId() > 0))
        {
          player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT));
          return;
        }
        content.append("Change Subclass:<br>");

        int baseClassId = player.getBaseClass();

        if (player.getSubClasses().isEmpty())
        {
          player.sendMessage("You have no subclass. Get them in the town.");
          return;
        }

        content.append("Which class would you like to switch to?<br>");

        if (baseClassId == player.getActiveClass()) content.append(CharTemplateTable.getClassNameById(baseClassId) + "&nbsp;<font color=\"LEVEL\">(Base Class)</font><br><br>");
        else {
          content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 0\">" + CharTemplateTable.getClassNameById(baseClassId) + "</a>&nbsp;" + "<font color=\"LEVEL\">(Base Class)</font><br><br>");
        }

        for (subList = iterSubClasses(player); subList.hasNext(); )
        {
          SubClass subClass = (SubClass)subList.next();
          int subClassId = subClass.getClassId();

          if (subClassId == player.getActiveClass()) content.append(CharTemplateTable.getClassNameById(subClassId) + "<br>");
          else {
            content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 " + subClass.getClassIndex() + "\">" + CharTemplateTable.getClassNameById(subClassId) + "</a><br>");
          }

        }

        break;
      case 5:
        if (!FloodProtector.getInstance().tryPerformAction(player.getObjectId(), 6))
        {
          player.sendMessage("Sorry for the inconvenience, but you'll have to wait a bit, too often you change your class.");
          return;
        }
        if (player._inEventCTF)
        {
          player.sendMessage("You are member of event.");
          return;
        }
        if ((Olympiad.getInstance().isRegisteredInComp(player)) || (player.getOlympiadGameId() > 0))
        {
          player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT));
          return;
        }
        if (player.isCursedWeaponEquiped())
          return;
        removeWeapon(player);
        player.setActiveClass(paramOne);
        player.broadcastUserInfo();
        player.abortCast();
        player.stopAllEffects();

        content.append("Change Subclass:<br>Your active sub class is now a <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(player.getActiveClass()) + "</font>.");

        player.sendPacket(new SystemMessage(SystemMessageId.SUBCLASS_TRANSFER_COMPLETED));
        if ((Config.CHECK_SKILLS_ON_ENTER) && (!Config.ALT_GAME_SKILL_LEARN)) player.checkAllowedSkills();
        CoupleManager.getInstance().checkCouple(player);
      }

      content.append("</body></html>");
      if (content.length() > 26) html.setHtml(content.toString());
      player.sendPacket(html);
      return;
    }
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    int condition = validateCondition(player);
    if (condition <= 1)
      return;
    if (condition == 3)
    {
      StringTokenizer st = new StringTokenizer(command, " ");
      String actualCommand = st.nextToken();
      String val = "";
      if (st.countTokens() >= 1) val = st.nextToken();

      if (actualCommand.equalsIgnoreCase("banish_foreigner"))
      {
        getClanHall().banishForeigners();
        return;
      }
      if (actualCommand.equalsIgnoreCase("manage_vault"))
      {
        if ((player.getClanPrivileges() & 0x8) == 8)
        {
          if (val.equalsIgnoreCase("deposit")) {
            showVaultWindowDeposit(player);
          } else if (val.equalsIgnoreCase("withdraw")) {
            showVaultWindowWithdraw(player);
          }
          else {
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setFile("data/html/clanHallManager/vault.htm");
            sendHtmlMessage(player, html);
          }
        }
        else
          player.sendMessage("You are not authorized to do this!");
        return;
      }
      if (actualCommand.equalsIgnoreCase("door"))
      {
        if ((player.getClanPrivileges() & 0x400) == 1024)
        {
          if (val.equalsIgnoreCase("open")) {
            getClanHall().openCloseDoors(true);
          } else if (val.equalsIgnoreCase("close")) {
            getClanHall().openCloseDoors(false);
          }
          else {
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setFile("data/html/clanHallManager/door.htm");
            sendHtmlMessage(player, html);
          }
        }
        else
          player.sendMessage("You are not authorized to do this!");
      }
      else if (actualCommand.equalsIgnoreCase("functions"))
      {
        if (val.equalsIgnoreCase("tele"))
        {
          NpcHtmlMessage html = new NpcHtmlMessage(1);
          if (getClanHall().getFunction(1) == null)
            html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
          else
            html.setFile("data/html/clanHallManager/tele" + getClanHall().getLocation() + getClanHall().getFunction(1).getLvl() + ".htm");
          sendHtmlMessage(player, html);
        }
        else if (val.equalsIgnoreCase("item_creation"))
        {
          if (getClanHall().getFunction(2) == null) {
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
            sendHtmlMessage(player, html);
            return;
          }
          if (st.countTokens() < 1) return;
          int valbuy = Integer.parseInt(st.nextToken()) + getClanHall().getFunction(2).getLvl() * 100000;
          showBuyWindow(player, valbuy);
        }
        else if (val.equalsIgnoreCase("support"))
        {
          NpcHtmlMessage html = new NpcHtmlMessage(1);
          if (getClanHall().getFunction(6) == null) {
            html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
          } else {
            html.setFile("data/html/clanHallManager/support" + getClanHall().getFunction(6).getLvl() + ".htm");
            html.replace("%mp%", String.valueOf(getCurrentMp()));
          }
          sendHtmlMessage(player, html);
        }
        else if (val.equalsIgnoreCase("back")) {
          showMessageWindow(player);
        } else {
          NpcHtmlMessage html = new NpcHtmlMessage(1);
          html.setFile("data/html/clanHallManager/functions.htm");
          if (getClanHall().getFunction(5) != null)
            html.replace("%xp_regen%", String.valueOf(getClanHall().getFunction(5).getLvl()) + "%");
          else
            html.replace("%xp_regen%", "0");
          if (getClanHall().getFunction(3) != null)
            html.replace("%hp_regen%", String.valueOf(getClanHall().getFunction(3).getLvl()) + "%");
          else
            html.replace("%hp_regen%", "0");
          if (getClanHall().getFunction(4) != null)
            html.replace("%mp_regen%", String.valueOf(getClanHall().getFunction(4).getLvl()) + "%");
          else
            html.replace("%mp_regen", "0");
          sendHtmlMessage(player, html);
        }
      } else {
        if (actualCommand.equalsIgnoreCase("manage"))
        {
          if ((player.getClanPrivileges() & 0x4000) == 16384)
          {
            if (val.equalsIgnoreCase("recovery"))
            {
              if (st.countTokens() >= 1)
              {
                if (getClanHall().getOwnerId() == 0) {
                  player.sendMessage("This clan Hall have no owner, you cannot change configuration");
                  return;
                }
                val = st.nextToken();
                if (val.equalsIgnoreCase("hp"))
                {
                  if (st.countTokens() >= 1)
                  {
                    val = st.nextToken();
                    int percent = Integer.valueOf(val).intValue();
                    int fee;
                    switch (percent)
                    {
                    case 0:
                      fee = 0;
                      break;
                    case 20:
                      fee = Config.CH_HPREG1_FEE;
                      break;
                    case 40:
                      fee = Config.CH_HPREG2_FEE;
                      break;
                    case 80:
                      fee = Config.CH_HPREG3_FEE;
                      break;
                    case 100:
                      fee = Config.CH_HPREG4_FEE;
                      break;
                    case 120:
                      fee = Config.CH_HPREG5_FEE;
                      break;
                    case 140:
                      fee = Config.CH_HPREG6_FEE;
                      break;
                    case 160:
                      fee = Config.CH_HPREG7_FEE;
                      break;
                    case 180:
                      fee = Config.CH_HPREG8_FEE;
                      break;
                    case 200:
                      fee = Config.CH_HPREG9_FEE;
                      break;
                    case 220:
                      fee = Config.CH_HPREG10_FEE;
                      break;
                    case 240:
                      fee = Config.CH_HPREG11_FEE;
                      break;
                    case 260:
                      fee = Config.CH_HPREG12_FEE;
                      break;
                    default:
                      fee = Config.CH_HPREG13_FEE;
                    }

                    if (!getClanHall().updateFunctions(3, percent, fee, Config.CH_HPREG_FEE_RATIO, getClanHall().getFunction(3) == null))
                      player.sendMessage("You don't have enough adena in your clan's warehouse");
                    else
                      revalidateDeco(player);
                  }
                }
                else if (val.equalsIgnoreCase("mp"))
                {
                  if (st.countTokens() >= 1)
                  {
                    val = st.nextToken();
                    int percent = Integer.valueOf(val).intValue();
                    int fee;
                    switch (percent)
                    {
                    case 0:
                      fee = 0;
                      break;
                    case 5:
                      fee = Config.CH_MPREG1_FEE;
                      break;
                    case 10:
                      fee = Config.CH_MPREG2_FEE;
                      break;
                    case 15:
                      fee = Config.CH_MPREG3_FEE;
                      break;
                    case 30:
                      fee = Config.CH_MPREG4_FEE;
                      break;
                    default:
                      fee = Config.CH_MPREG5_FEE;
                    }

                    if (!getClanHall().updateFunctions(4, percent, fee, Config.CH_MPREG_FEE_RATIO, getClanHall().getFunction(4) == null))
                      player.sendMessage("You don't have enough adena in your clan's warehouse");
                    else
                      revalidateDeco(player);
                  }
                }
                else if (val.equalsIgnoreCase("exp"))
                {
                  if (st.countTokens() >= 1)
                  {
                    val = st.nextToken();
                    int percent = Integer.valueOf(val).intValue();
                    int fee;
                    switch (percent)
                    {
                    case 0:
                      fee = 0;
                      break;
                    case 5:
                      fee = Config.CH_EXPREG1_FEE;
                      break;
                    case 10:
                      fee = Config.CH_EXPREG2_FEE;
                      break;
                    case 15:
                      fee = Config.CH_EXPREG3_FEE;
                      break;
                    case 25:
                      fee = Config.CH_EXPREG4_FEE;
                      break;
                    case 35:
                      fee = Config.CH_EXPREG5_FEE;
                      break;
                    case 40:
                      fee = Config.CH_EXPREG6_FEE;
                      break;
                    default:
                      fee = Config.CH_EXPREG7_FEE;
                    }

                    if (!getClanHall().updateFunctions(5, percent, fee, Config.CH_EXPREG_FEE_RATIO, getClanHall().getFunction(5) == null))
                      player.sendMessage("You don't have enough adena in your clan's warehouse");
                    else
                      revalidateDeco(player);
                  }
                }
              }
              NpcHtmlMessage html = new NpcHtmlMessage(1);
              html.setFile("data/html/clanHallManager/edit_recovery" + getClanHall().getGrade() + ".htm");
              if (getClanHall().getFunction(3) != null) {
                html.replace("%hp%", String.valueOf(getClanHall().getFunction(3).getLvl()) + "%");
                html.replace("%hpPrice%", String.valueOf(getClanHall().getFunction(3).getLease()));
                html.replace("%hpDate%", format.format(Long.valueOf(getClanHall().getFunction(3).getEndTime())));
              } else {
                html.replace("%hp%", "0");
                html.replace("%hpPrice%", "0");
                html.replace("%hpDate%", "0");
              }
              if (getClanHall().getFunction(5) != null) {
                html.replace("%exp%", String.valueOf(getClanHall().getFunction(5).getLvl()) + "%");
                html.replace("%expPrice%", String.valueOf(getClanHall().getFunction(5).getLease()));
                html.replace("%expDate%", format.format(Long.valueOf(getClanHall().getFunction(5).getEndTime())));
              } else {
                html.replace("%exp%", "0");
                html.replace("%expPrice%", "0");
                html.replace("%expDate%", "0");
              }
              if (getClanHall().getFunction(4) != null) {
                html.replace("%mp%", String.valueOf(getClanHall().getFunction(4).getLvl()) + "%");
                html.replace("%mpPrice%", String.valueOf(getClanHall().getFunction(4).getLease()));
                html.replace("%mpDate%", format.format(Long.valueOf(getClanHall().getFunction(4).getEndTime())));
              } else {
                html.replace("%mp%", "0");
                html.replace("%mpPrice%", "0");
                html.replace("%mpDate%", "0");
              }
              sendHtmlMessage(player, html);
            }
            else if (val.equalsIgnoreCase("other"))
            {
              if (st.countTokens() >= 1)
              {
                if (getClanHall().getOwnerId() == 0) {
                  player.sendMessage("This clan Hall have no owner, you cannot change configuration");
                  return;
                }
                val = st.nextToken();
                if (val.equalsIgnoreCase("item"))
                {
                  if (st.countTokens() >= 1)
                  {
                    if (getClanHall().getOwnerId() == 0) {
                      player.sendMessage("This clan Hall have no owner, you cannot change configuration");
                      return;
                    }
                    val = st.nextToken();

                    int lvl = Integer.valueOf(val).intValue();
                    int fee;
                    switch (lvl)
                    {
                    case 0:
                      fee = 0;
                      break;
                    case 1:
                      fee = Config.CH_ITEM1_FEE;
                      break;
                    case 2:
                      fee = Config.CH_ITEM2_FEE;
                      break;
                    default:
                      fee = Config.CH_ITEM3_FEE;
                    }

                    if (!getClanHall().updateFunctions(2, lvl, fee, Config.CH_ITEM_FEE_RATIO, getClanHall().getFunction(2) == null))
                      player.sendMessage("You don't have enough adena in your clan's warehouse");
                    else
                      revalidateDeco(player);
                  }
                }
                else if (val.equalsIgnoreCase("tele"))
                {
                  if (st.countTokens() >= 1)
                  {
                    val = st.nextToken();
                    int lvl = Integer.valueOf(val).intValue();
                    int fee;
                    switch (lvl)
                    {
                    case 0:
                      fee = 0;
                      break;
                    case 1:
                      fee = Config.CH_TELE1_FEE;
                      break;
                    default:
                      fee = Config.CH_TELE2_FEE;
                    }

                    if (!getClanHall().updateFunctions(1, lvl, fee, Config.CH_TELE_FEE_RATIO, getClanHall().getFunction(1) == null))
                      player.sendMessage("You don't have enough adena in your clan's warehouse");
                    else
                      revalidateDeco(player);
                  }
                }
                else if (val.equalsIgnoreCase("support"))
                {
                  if (st.countTokens() >= 1)
                  {
                    val = st.nextToken();
                    int lvl = Integer.valueOf(val).intValue();
                    int fee;
                    switch (lvl)
                    {
                    case 0:
                      fee = 0;
                      break;
                    case 1:
                      fee = Config.CH_SUPPORT1_FEE;
                      break;
                    case 2:
                      fee = Config.CH_SUPPORT2_FEE;
                      break;
                    case 3:
                      fee = Config.CH_SUPPORT3_FEE;
                      break;
                    case 4:
                      fee = Config.CH_SUPPORT4_FEE;
                      break;
                    case 5:
                      fee = Config.CH_SUPPORT5_FEE;
                      break;
                    case 6:
                      fee = Config.CH_SUPPORT6_FEE;
                      break;
                    case 7:
                      fee = Config.CH_SUPPORT7_FEE;
                      break;
                    default:
                      fee = Config.CH_SUPPORT8_FEE;
                    }

                    if (!getClanHall().updateFunctions(6, lvl, fee, Config.CH_SUPPORT_FEE_RATIO, getClanHall().getFunction(6) == null))
                      player.sendMessage("You don't have enough adena in your clan's warehouse");
                    else
                      revalidateDeco(player);
                  }
                }
              }
              NpcHtmlMessage html = new NpcHtmlMessage(1);
              html.setFile("data/html/clanHallManager/edit_other" + getClanHall().getGrade() + ".htm");
              if (getClanHall().getFunction(1) != null) {
                html.replace("%tele%", String.valueOf(getClanHall().getFunction(1).getLvl()));
                html.replace("%telePrice%", String.valueOf(getClanHall().getFunction(1).getLease()));
                html.replace("%teleDate%", format.format(Long.valueOf(getClanHall().getFunction(1).getEndTime())));
              } else {
                html.replace("%tele%", "0");
                html.replace("%telePrice%", "0");
                html.replace("%teleDate%", "0");
              }
              if (getClanHall().getFunction(6) != null) {
                html.replace("%support%", String.valueOf(getClanHall().getFunction(6).getLvl()));
                html.replace("%supportPrice%", String.valueOf(getClanHall().getFunction(6).getLease()));
                html.replace("%supportDate%", format.format(Long.valueOf(getClanHall().getFunction(6).getEndTime())));
              } else {
                html.replace("%support%", "0");
                html.replace("%supportPrice%", "0");
                html.replace("%supportDate%", "0");
              }
              if (getClanHall().getFunction(2) != null) {
                html.replace("%item%", String.valueOf(getClanHall().getFunction(2).getLvl()));
                html.replace("%itemPrice%", String.valueOf(getClanHall().getFunction(2).getLease()));
                html.replace("%itemDate%", format.format(Long.valueOf(getClanHall().getFunction(2).getEndTime())));
              } else {
                html.replace("%item%", "0");
                html.replace("%itemPrice%", "0");
                html.replace("%itemDate%", "0");
              }
              sendHtmlMessage(player, html);
            }
            else if (val.equalsIgnoreCase("deco"))
            {
              if (st.countTokens() >= 1)
              {
                if (getClanHall().getOwnerId() == 0) {
                  player.sendMessage("This clan Hall have no owner, you cannot change configuration");
                  return;
                }
                val = st.nextToken();
                if (val.equalsIgnoreCase("curtains")) {
                  if (st.countTokens() >= 1)
                  {
                    val = st.nextToken();
                    int lvl = Integer.valueOf(val).intValue();
                    int fee;
                    switch (lvl)
                    {
                    case 0:
                      fee = 0;
                      break;
                    case 1:
                      fee = Config.CH_CURTAIN1_FEE;
                      break;
                    default:
                      fee = Config.CH_CURTAIN2_FEE;
                    }

                    if (!getClanHall().updateFunctions(8, lvl, fee, Config.CH_CURTAIN_FEE_RATIO, getClanHall().getFunction(8) == null))
                      player.sendMessage("You don't have enough adena in your clan's warehouse");
                    else
                      revalidateDeco(player);
                  }
                } else if ((val.equalsIgnoreCase("porch")) && 
                  (st.countTokens() >= 1))
                {
                  val = st.nextToken();
                  int lvl = Integer.valueOf(val).intValue();
                  int fee;
                  switch (lvl)
                  {
                  case 0:
                    fee = 0;
                    break;
                  case 1:
                    fee = Config.CH_FRONT1_FEE;
                    break;
                  default:
                    fee = Config.CH_FRONT2_FEE;
                  }

                  if (!getClanHall().updateFunctions(7, lvl, fee, Config.CH_FRONT_FEE_RATIO, getClanHall().getFunction(7) == null))
                    player.sendMessage("You don't have enough adena in your clan's warehouse");
                  else {
                    revalidateDeco(player);
                  }
                }
              }
              NpcHtmlMessage html = new NpcHtmlMessage(1);
              html.setFile("data/html/clanHallManager/deco.htm");
              if (getClanHall().getFunction(8) != null) {
                html.replace("%curtain%", String.valueOf(getClanHall().getFunction(8).getLvl()));
                html.replace("%curtainPrice%", String.valueOf(getClanHall().getFunction(8).getLease()));
                html.replace("%curtainDate%", format.format(Long.valueOf(getClanHall().getFunction(8).getEndTime())));
              } else {
                html.replace("%curtain%", "0");
                html.replace("%curtainPrice%", "0");
                html.replace("%curtainDate%", "0");
              }
              if (getClanHall().getFunction(7) != null) {
                html.replace("%porch%", String.valueOf(getClanHall().getFunction(7).getLvl()));
                html.replace("%porchPrice%", String.valueOf(getClanHall().getFunction(7).getLease()));
                html.replace("%porchDate%", format.format(Long.valueOf(getClanHall().getFunction(7).getEndTime())));
              } else {
                html.replace("%porch%", "0");
                html.replace("%porchPrice%", "0");
                html.replace("%porchDate%", "0");
              }
              sendHtmlMessage(player, html);
            }
            else if (val.equalsIgnoreCase("back")) {
              showMessageWindow(player);
            }
            else {
              NpcHtmlMessage html = new NpcHtmlMessage(1);
              html.setFile("data/html/clanHallManager/manage.htm");
              sendHtmlMessage(player, html);
            }
          }
          else
            player.sendMessage("You are not authorized to do this!");
          return;
        }
        if (actualCommand.equalsIgnoreCase("support"))
        {
          setTarget(player);

          if (val == "") return;

          try
          {
            int skill_id = Integer.parseInt(val);
            try
            {
              int skill_lvl = 0;
              if (st.countTokens() >= 1) skill_lvl = Integer.parseInt(st.nextToken());
              L2Skill skill = SkillTable.getInstance().getInfo(skill_id, skill_lvl);

              if (skill.getSkillType() == L2Skill.SkillType.SUMMON)
              {
                player.doCast(skill);
              }
              else
              {
                if (player.getShowAnim())
                {
                  player.setTarget(player);
                  player.broadcastPacket(new MagicSkillUser(player, skill.getId(), skill.getLevel(), 170, 300));
                }
                skill.getEffects(this, player);
              }

              if (getClanHall().getFunction(6) == null)
                return;
              NpcHtmlMessage html = new NpcHtmlMessage(1);
              if (getClanHall().getFunction(6).getLvl() == 0)
                return;
              html.setFile("data/html/clanHallManager/support" + getClanHall().getFunction(6).getLvl() + ".htm");
              html.replace("%mp%", String.valueOf(getCurrentMp()));
              sendHtmlMessage(player, html);
            }
            catch (Exception e)
            {
              player.sendMessage("Invalid skill level!");
            }
          }
          catch (Exception e)
          {
            player.sendMessage("Invalid skill!");
          }
          return;
        }
        if (actualCommand.equalsIgnoreCase("goto"))
        {
          int whereTo = Integer.parseInt(val);
          doTeleport(player, whereTo);
          return;
        }
      }
    }
    super.onBypassFeedback(player, command);
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;

    if (this != player.getTarget())
    {
      player.setTarget(this);

      MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
      player.sendPacket(my);

      player.sendPacket(new ValidateLocation(this));
    }
    else if (canInteract(player))
    {
      showMessageWindow(player);
    }

    player.sendPacket(new ActionFailed());
  }

  private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
  {
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcname%", getName());
    html.replace("%npcId%", String.valueOf(getNpcId()));
    player.sendPacket(html);
  }

  private void showMessageWindow(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    String filename = "data/html/clanHallManager/chamberlain-no.htm";

    int condition = validateCondition(player);
    if (condition == 3)
      filename = "data/html/clanHallManager/chamberlain.htm";
    if (condition == 0)
      filename = "data/html/clanHallManager/chamberlain-of.htm";
    NpcHtmlMessage html = new NpcHtmlMessage(1);
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcId%", String.valueOf(getNpcId()));
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }

  protected int validateCondition(L2PcInstance player)
  {
    if (getClanHall() == null) return 1;
    if (player.isGM()) return 3;
    if (player.getClan() != null)
    {
      if (getClanHall().getOwnerId() == player.getClanId()) {
        return 3;
      }
      return 0;
    }
    return 1;
  }

  public final ClanHall getClanHall()
  {
    if (_clanHallId < 0)
    {
      ClanHall temp = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);

      if (temp != null) {
        _clanHallId = temp.getId();
      }
      if (_clanHallId < 0) return null;
    }
    return ClanHallManager.getInstance().getClanHallById(_clanHallId);
  }

  private void showVaultWindowDeposit(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    player.setActiveWarehouse(player.getClan().getWarehouse());
    player.sendPacket(new WareHouseDepositList(player, 2));
  }

  private void showVaultWindowWithdraw(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    player.setActiveWarehouse(player.getClan().getWarehouse());
    player.sendPacket(new WareHouseWithdrawalList(player, 2));
  }

  private void doTeleport(L2PcInstance player, int val)
  {
    L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
    if (list != null)
    {
      if (SiegeManager.getInstance().getSiege(list.getLocX(), list.getLocY(), list.getLocZ()) != null)
      {
        player.sendPacket(new SystemMessage(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE));
        return;
      }
      if (player.reduceAdena("Teleport", list.getPrice(), this, true))
      {
        player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
      }
    }
    else
    {
      _log.warning("No teleport destination with id:" + val);
    }
    player.sendPacket(new ActionFailed());
  }

  private void showBuyWindow(L2PcInstance player, int val) {
    double taxRate = 0.0D;

    if (getIsInTown()) taxRate = getCastle().getTaxRate();

    player.tempInvetoryDisable();

    L2TradeList list = TradeController.getInstance().getBuyList(val);

    if ((list != null) && (list.getNpcId().equals(String.valueOf(getNpcId()))))
    {
      BuyList bl = new BuyList(list, player.getAdena(), taxRate);
      player.sendPacket(bl);
    }
    else
    {
      _log.warning("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");

      _log.warning("buylist id:" + val);
    }

    player.sendPacket(new ActionFailed());
  }

  private void revalidateDeco(L2PcInstance player) {
    ClanHallDecoration bl = new ClanHallDecoration(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()));
    player.sendPacket(bl);
  }

  private Iterator<SubClass> iterSubClasses(L2PcInstance player)
  {
    return player.getSubClasses().values().iterator();
  }

  public void removeWeapon(L2PcInstance player)
  {
    L2ItemInstance wpn = player.getInventory().getPaperdollItem(7);
    if (wpn == null) wpn = player.getInventory().getPaperdollItem(14);
    if (wpn != null)
    {
      L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
      InventoryUpdate iu = new InventoryUpdate();
      for (int i = 0; i < unequiped.length; i++)
        iu.addModifiedItem(unequiped[i]);
      player.sendPacket(iu);
      player.abortAttack();
      player.broadcastUserInfo();

      if (unequiped.length > 0)
      {
        if (unequiped[0].isWear())
          return;
        SystemMessage sm = null;
        if (unequiped[0].getEnchantLevel() > 0) {
          sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
          sm.addNumber(unequiped[0].getEnchantLevel());
          sm.addItemName(unequiped[0].getItemId());
        } else {
          sm = new SystemMessage(SystemMessageId.S1_DISARMED);
          sm.addItemName(unequiped[0].getItemId());
        }
        player.sendPacket(sm);
      }
    }
  }
}