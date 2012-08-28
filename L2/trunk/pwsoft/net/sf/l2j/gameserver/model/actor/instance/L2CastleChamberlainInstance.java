package net.sf.l2j.gameserver.model.actor.instance;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.serverpackets.BuyList;
import net.sf.l2j.gameserver.network.serverpackets.ExShowCropInfo;
import net.sf.l2j.gameserver.network.serverpackets.ExShowCropSetting;
import net.sf.l2j.gameserver.network.serverpackets.ExShowManorDefaultInfo;
import net.sf.l2j.gameserver.network.serverpackets.ExShowSeedInfo;
import net.sf.l2j.gameserver.network.serverpackets.ExShowSeedSetting;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;

public class L2CastleChamberlainInstance extends L2FolkInstance
{
  protected static final int COND_ALL_FALSE = 0;
  protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
  protected static final int COND_OWNER = 2;

  public L2CastleChamberlainInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;

    player.setLastFolkNPC(this);

    if (this != player.getTarget())
    {
      player.setTarget(this);

      MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
      player.sendPacket(my);

      player.sendPacket(new ValidateLocation(this));
    }
    else if (!canInteract(player))
    {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
    }
    else
    {
      showMessageWindow(player);
    }

    player.sendActionFailed();
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (player.getLastFolkNPC().getObjectId() != getObjectId()) {
      return;
    }
    int condition = validateCondition(player);
    if (condition <= 0) {
      return;
    }
    if (condition == 1)
      return;
    if (condition == 2)
    {
      StringTokenizer st = new StringTokenizer(command, " ");
      String actualCommand = st.nextToken();

      String val = "";
      if (st.countTokens() >= 1) val = st.nextToken();

      if (actualCommand.equalsIgnoreCase("banish_foreigner"))
      {
        getCastle().banishForeigners();
        return;
      }
      if (actualCommand.equalsIgnoreCase("list_siege_clans"))
      {
        getCastle().getSiege().listRegisterClan(player);
        return;
      }
      if (actualCommand.equalsIgnoreCase("receive_report"))
      {
        NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
        html.setFile("data/html/chamberlain/chamberlain-report.htm");
        html.replace("%objectId%", String.valueOf(getObjectId()));
        L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());

        String clanname = clan.getName();
        clanname = clanname.replaceAll("<", "&lt;");
        clanname = clanname.replaceAll(">", "&gt;");
        clanname = clanname.replaceAll("\\$", "");
        html.replace("%clanname%", clanname);
        html.replace("%clanleadername%", clan.getLeaderName());
        html.replace("%castlename%", getCastle().getName());

        int currentPeriod = SevenSigns.getInstance().getCurrentPeriod();
        switch (currentPeriod)
        {
        case 0:
          html.replace("%ss_event%", "Quest Event Initialization");
          break;
        case 1:
          html.replace("%ss_event%", "Competition (Quest Event)");
          break;
        case 2:
          html.replace("%ss_event%", "Quest Event Results");
          break;
        case 3:
          html.replace("%ss_event%", "Seal Validation");
        }

        int sealOwner1 = SevenSigns.getInstance().getSealOwner(1);
        switch (sealOwner1)
        {
        case 0:
          html.replace("%ss_avarice%", "Not in Possession");
          break;
        case 2:
          html.replace("%ss_avarice%", "Lords of Dawn");
          break;
        case 1:
          html.replace("%ss_avarice%", "Revolutionaries of Dusk");
        }

        int sealOwner2 = SevenSigns.getInstance().getSealOwner(2);
        switch (sealOwner2)
        {
        case 0:
          html.replace("%ss_gnosis%", "Not in Possession");
          break;
        case 2:
          html.replace("%ss_gnosis%", "Lords of Dawn");
          break;
        case 1:
          html.replace("%ss_gnosis%", "Revolutionaries of Dusk");
        }

        int sealOwner3 = SevenSigns.getInstance().getSealOwner(3);
        switch (sealOwner3)
        {
        case 0:
          html.replace("%ss_strife%", "Not in Possession");
          break;
        case 2:
          html.replace("%ss_strife%", "Lords of Dawn");
          break;
        case 1:
          html.replace("%ss_strife%", "Revolutionaries of Dusk");
        }

        player.sendPacket(html);
        return;
      }
      if (actualCommand.equalsIgnoreCase("items"))
      {
        if (val == "") return;
        player.tempInvetoryDisable();

        int castleId = getCastle().getCastleId();
        int circlet = CastleManager.getInstance().getCircletByCastleId(castleId);
        PcInventory s = player.getInventory();
        int buy;
        int buy;
        if (s.getItemByItemId(circlet) == null)
        {
          buy = Integer.parseInt(val + "1");
        }
        else
        {
          buy = Integer.parseInt(val + "2");
        }

        L2TradeList list = TradeController.getInstance().getBuyList(buy);
        if ((list != null) && (list.getNpcId().equals(String.valueOf(getNpcId()))))
        {
          BuyList bl = new BuyList(list, player.getAdena(), 0.0D);
          player.sendPacket(bl);
        }
        else
        {
          _log.warning("player: " + player.getName() + " attempting to buy from chamberlain that don't have buylist!");

          _log.warning("buylist id:" + buy);
        }
        player.sendActionFailed();
      } else {
        if (actualCommand.equalsIgnoreCase("manage_siege_defender"))
        {
          getCastle().getSiege().listRegisterClan(player);
          return;
        }
        if (actualCommand.equalsIgnoreCase("manage_vault")) {
          String filename = "data/html/chamberlain/chamberlain-vault.htm";
          int amount = 0;
          if (val.equalsIgnoreCase("deposit")) {
            try {
              amount = Integer.parseInt(st.nextToken()); } catch (NoSuchElementException e) {
            }
            if ((amount > 0) && (getCastle().getTreasury() + amount < 2147483647L)) {
              if (player.reduceAdena("Castle", amount, this, true))
                getCastle().addToTreasuryNoTax(amount);
              else
                sendPacket(Static.YOU_NOT_ENOUGH_ADENA);
            }
          }
          else if (val.equalsIgnoreCase("withdraw")) {
            try {
              amount = Integer.parseInt(st.nextToken()); } catch (NoSuchElementException e) {
            }
            if (amount > 0) {
              if (getCastle().getTreasury() < amount) {
                filename = "data/html/chamberlain/chamberlain-vault-no.htm";
              }
              else if (getCastle().addToTreasuryNoTax(-1 * amount)) {
                player.addAdena("Castle", amount, this, true);
              }
            }

          }

          NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
          html.setFile(filename);
          html.replace("%objectId%", String.valueOf(getObjectId()));
          html.replace("%npcname%", getName());
          html.replace("%tax_income%", Util.formatAdena(getCastle().getTreasury()));
          html.replace("%withdraw_amount%", Util.formatAdena(amount));
          player.sendPacket(html);

          return;
        }
        if (actualCommand.equalsIgnoreCase("Clan_Gate"))
        {
          L2PcInstance leader = L2World.getInstance().getPlayer(player.getClan().getLeaderId());

          if (leader.atEvent)
          {
            player.sendMessage("Your leader is in an event.");
            return;
          }

          if (leader.isInJail())
          {
            player.sendMessage("Your leader is in Jail.");
            return;
          }

          if (leader.isInOlympiadMode())
          {
            player.sendMessage("Your leader is in the Olympiad now.");
            return;
          }

          if (leader.inObserverMode())
          {
            player.sendMessage("Your leader is in Observ Mode.");
            return;
          }

          if (leader.isInDuel())
          {
            player.sendMessage("Your leader is in a duel.");
            return;
          }

          if (leader.isFestivalParticipant())
          {
            player.sendMessage("Your leader is in a festival.");

            return;
          }

          if ((leader.isInParty()) && (leader.getParty().isInDimensionalRift()))
          {
            player.sendMessage("Your leader is in dimensional rift.");
            return;
          }

          if ((leader.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(leader.getClan()) != null) && (CastleManager.getInstance().getCastleByOwner(leader.getClan()).getSiege().getIsInProgress()))
          {
            player.sendMessage("Your leader is in siege, you can't go to your leader.");
            return;
          }

          int leaderx = leader.getX();
          int leadery = leader.getY();
          int leaderz = leader.getZ();

          player.teleToLocation(leaderx, leadery, leaderz);
          player.sendMessage("You have been teleported to your leader.");
        } else {
          if (actualCommand.equalsIgnoreCase("manor")) {
            String filename = "";
            if (CastleManorManager.getInstance().isDisabled()) {
              filename = "data/html/npcdefault.htm";
            } else {
              int cmd = Integer.parseInt(val);
              switch (cmd) {
              case 0:
                filename = "data/html/chamberlain/manor/manor.htm";
                break;
              case 4:
                filename = "data/html/chamberlain/manor/manor_help00" + st.nextToken() + ".htm";
                break;
              default:
                filename = "data/html/chamberlain/chamberlain-no.htm";
              }

            }

            if (filename.length() != 0) {
              NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
              html.setFile(filename);
              html.replace("%objectId%", String.valueOf(getObjectId()));
              html.replace("%npcname%", getName());
              player.sendPacket(html);
            }
            return;
          }
          if (command.startsWith("manor_menu_select"))
          {
            if (CastleManorManager.getInstance().isUnderMaintenance())
            {
              player.sendActionFailed();
              player.sendPacket(Static.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
              return;
            }

            String params = command.substring(command.indexOf("?") + 1);
            StringTokenizer str = new StringTokenizer(params, "&");
            int ask = Integer.parseInt(str.nextToken().split("=")[1]);
            int state = Integer.parseInt(str.nextToken().split("=")[1]);
            int time = Integer.parseInt(str.nextToken().split("=")[1]);
            int castleId;
            int castleId;
            if (state == -1)
              castleId = getCastle().getCastleId();
            else {
              castleId = state;
            }
            switch (ask) {
            case 3:
              if ((time == 1) && (!CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved()))
                player.sendPacket(new ExShowSeedInfo(castleId, null));
              else
                player.sendPacket(new ExShowSeedInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getSeedProduction(time)));
              break;
            case 4:
              if ((time == 1) && (!CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved()))
                player.sendPacket(new ExShowCropInfo(castleId, null));
              else
                player.sendPacket(new ExShowCropInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getCropProcure(time)));
              break;
            case 5:
              player.sendPacket(new ExShowManorDefaultInfo());
              break;
            case 7:
              if (getCastle().isNextPeriodApproved())
                player.sendPacket(Static.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
              else {
                player.sendPacket(new ExShowSeedSetting(getCastle().getCastleId()));
              }
              break;
            case 8:
              if (getCastle().isNextPeriodApproved())
                player.sendPacket(Static.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
              else
                player.sendPacket(new ExShowCropSetting(getCastle().getCastleId()));
            case 6:
            }
          }
          else {
            if (actualCommand.equalsIgnoreCase("operate_door"))
            {
              if (val != "")
              {
                boolean open = Integer.parseInt(val) == 1;
                while (st.hasMoreTokens())
                {
                  getCastle().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
                }
              }

              NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
              html.setFile("data/html/chamberlain/" + getTemplate().npcId + "-d.htm");
              html.replace("%objectId%", String.valueOf(getObjectId()));
              html.replace("%npcname%", getName());
              player.sendPacket(html);
              return;
            }
            if (actualCommand.equalsIgnoreCase("tax_set"))
            {
              if (val != "") {
                getCastle().setTaxPercent(player, Integer.parseInt(val));
              }
              TextBuilder msg = new TextBuilder("<html><body>");
              msg.append(getName() + ":<br>");
              msg.append("Current tax rate: " + getCastle().getTaxPercent() + "%<br>");
              msg.append("<table>");
              msg.append("<tr>");
              msg.append("<td>Change tax rate to:</td>");
              msg.append("<td><edit var=\"value\" width=40><br>");
              msg.append("<button value=\"Adjust\" action=\"bypass -h npc_%objectId%_tax_set $value\" width=80 height=15></td>");
              msg.append("</tr>");
              msg.append("</table>");
              msg.append("</center>");
              msg.append("</body></html>");

              sendHtmlMessage(player, msg.toString());
              return;
            }
          }
        }
      }
    }
    super.onBypassFeedback(player, command);
  }

  private void sendHtmlMessage(L2PcInstance player, String htmlMessage)
  {
    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    html.setHtml(htmlMessage);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }

  private void showMessageWindow(L2PcInstance player)
  {
    player.sendActionFailed();
    String filename = "data/html/chamberlain/chamberlain-no.htm";

    int condition = validateCondition(player);
    if (condition > 0)
    {
      if (condition == 1)
        filename = "data/html/chamberlain/chamberlain-busy.htm";
      else if (condition == 2) {
        filename = "data/html/chamberlain/chamberlain.htm";
      }
    }
    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcId%", String.valueOf(getNpcId()));
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }

  protected int validateCondition(L2PcInstance player)
  {
    if ((getCastle() != null) && (getCastle().getCastleId() > 0))
    {
      if (player.getClan() != null)
      {
        if (getCastle().getSiege().getIsInProgress())
          return 1;
        if ((getCastle().getOwnerId() == player.getClanId()) && (player.isClanLeader()))
        {
          return 2;
        }
      }
    }
    return 0;
  }
}