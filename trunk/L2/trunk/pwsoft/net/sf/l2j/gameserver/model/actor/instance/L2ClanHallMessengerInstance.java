package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Calendar;
import javolution.util.FastMap;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import scripts.clanhalls.BanditStronghold;

public class L2ClanHallMessengerInstance extends L2NpcInstance
{
  public L2ClanHallMessengerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (CastleManager.getInstance().getCastleById(35).getSiege().getIsInProgress()) {
      showNextSiege(player);
      return;
    }
    String choise;
    if (command.equalsIgnoreCase("info"))
    {
      showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-info.htm");
    } else if (command.equalsIgnoreCase("payinfo"))
    {
      showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-payinfo.htm");
    } else if (command.equalsIgnoreCase("clansinfo"))
    {
      FastTable clans = BanditStronghold.getCH().getClanNames();
      NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
      html.setFile("data/html/siege/" + getTemplate().npcId + "-03.htm");

      int i = 0;
      for (String s : clans) {
        if (!s.equals(""))
          html.replace("%pledge" + i + "%", s);
        else {
          html.replace("%pledge" + i + "%", "** has not registered **");
        }
        i++;
      }
      player.sendPacket(html);
    } else if (command.equalsIgnoreCase("npcselect")) {
      if (player.isClanLeader())
        showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-npcselect.htm");
      else
        showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-npcselect_0.htm");
    }
    else if (command.startsWith("npcshow")) {
      int id = 0;
      try {
        id = Integer.parseInt(command.substring(7).trim());
      }
      catch (Exception e) {
      }
      showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-npcshow_" + id + ".htm");
    } else if (command.startsWith("npcaccept")) {
      int id = 0;
      try {
        id = Integer.parseInt(command.substring(7).trim());
      }
      catch (Exception e) {
      }
      BanditStronghold.getCH().acceptNpc(id, player, this);
      showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-accept.htm");
    } else if (command.equalsIgnoreCase("npcinfo")) {
      showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-npcinfo.htm");
    } else if (command.startsWith("reg")) {
      if (!BanditStronghold.getCH().isRegTime()) {
        showNextSiege(player);
        return;
      }

      int type = 0;
      try {
        type = Integer.parseInt(command.substring(3).trim());
      }
      catch (Exception e) {
      }
      regClan(player, type);
    } else if (command.startsWith("bandit_")) {
      choise = command.substring(7).trim();
    }
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) {
      return;
    }

    if (this != player.getTarget())
    {
      player.setTarget(this);

      MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
      player.sendPacket(my);
      player.sendPacket(new ValidateLocation(this));
    }
    else if (!canInteract(player)) {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
    } else {
      showCHInfoWindow(player);
    }

    player.sendActionFailed();
  }

  private void showCHInfoWindow(L2PcInstance player) {
    if (getTemplate().npcId == 35437) {
      if ((player.getClan() != null) && (BanditStronghold.getCH().isRegistered(player.getClan()))) {
        showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-menu.htm");
        return;
      }

      NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
      if (!ClanHallManager.getInstance().isFree(35)) {
        ClanHall ch = ClanHallManager.getInstance().getClanHallById(35);
        if (player.getClan().getClanId() == ch.getOwnerId()) {
          html = null;
          if (BanditStronghold.getCH().isRegTime())
            showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-menu.htm");
          else {
            showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-owner.htm");
          }
          return;
        }
        L2Clan owner = ClanTable.getInstance().getClan(ch.getOwnerId());
        html.setFile("data/html/siege/35437-01.htm");
        html.replace("%owner%", owner.getName());
      } else {
        showChatWindow(player, "data/html/siege/35437-02.htm");
        return;
      }

      html.replace("%objectId%", String.valueOf(getObjectId()));
      player.sendPacket(html);
      player.sendActionFailed();
      return;
    }
  }

  private void showNextSiege(L2PcInstance player) {
    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    html.setFile("data/html/siege/35437-next.htm");
    html.replace("%next_siege%", String.valueOf(CastleManager.getInstance().getCastleById(35).getSiegeDate().getTime()));
    player.sendPacket(html);
    player.sendActionFailed();
  }

  private void regClan(L2PcInstance player, int pay) {
    if (isMyLord(player)) {
      showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-mylord.htm");
      return;
    }

    if (pay == 3) {
      if ((player.getClan() != null) && (BanditStronghold.getCH().isRegistered(player.getClan())))
        BanditStronghold.getCH().regPlayer(player, this);
      else {
        showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-noclan.htm");
      }
      return;
    }

    if (BanditStronghold.getCH().getAttackers().size() >= 5) {
      showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-full.htm");
      return;
    }

    if ((player.getClan() != null) && (player.getClan().getHasHideout() == 0)) {
      if (player.isClanLeader()) {
        if (player.getClan().getLevel() >= 4) {
          int payId = 0;
          int count = 1;
          switch (pay)
          {
          case 0:
            payId = 5009;
            count = 1;
            break;
          case 1:
            payId = 57;
            count = 200000;
          }

          L2ItemInstance coins = player.getInventory().getItemByItemId(payId);
          if ((coins != null) && (coins.getCount() >= count)) {
            player.destroyItemByItemId("China", payId, count, player, true);
            BanditStronghold.getCH().regClan(player, this);
          } else if (payId == 57) {
            showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-nomoney.htm");
          } else {
            showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-noquest.htm");
          }
        } else {
          showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-4level.htm");
        }
      }
      else showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-notleader.htm");
    }
    else
      showChatWindow(player, "data/html/siege/" + getTemplate().npcId + "-havech.htm");
  }

  private boolean isMyLord(L2PcInstance player)
  {
    return (player.getClan() != null) && (ClanHallManager.getInstance().getClanHallById(35).getOwnerId() == player.getClan().getClanId());
  }
}