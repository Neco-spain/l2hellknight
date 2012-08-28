package net.sf.l2j.gameserver.model.actor.instance;

import java.util.logging.Logger;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.instancemanager.EventManager;
import net.sf.l2j.gameserver.model.entity.FightClub;
import net.sf.l2j.gameserver.network.serverpackets.ExMailArrived;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.autoevents.basecapture.BaseCapture;
import scripts.autoevents.encounter.Encounter;
import scripts.autoevents.fighting.Fighting;
import scripts.autoevents.lasthero.LastHero;
import scripts.autoevents.masspvp.massPvp;
import scripts.autoevents.openseason.OpenSeason;

public class L2EventerInstance extends L2NpcInstance
{
  private static Logger _log = AbstractLogger.getLogger(L2EventerInstance.class.getName());
  private FastTable<L2PcInstance> _winners;
  private static final FastTable<Integer> _allowItems = Config.FC_ALLOWITEMS;

  public L2EventerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if ((player.getKarma() > 0) || (player.isCursedWeaponEquiped()))
    {
      player.sendHtmlMessage("\u0423 \u0432\u0430\u0441 \u043F\u043B\u043E\u0445\u0430\u044F \u043A\u0430\u0440\u043C\u0430.");
      return;
    }

    if ((!command.equalsIgnoreCase("fc_delme")) && ((player.inFClub()) || (FightClub.isRegged(player.getObjectId()))))
    {
      showError(player, new StringBuilder().append("\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u0432 \u0411\u043E\u0439\u0446\u043E\u0432\u0441\u043A\u043E\u043C \u043A\u043B\u0443\u0431\u0435.<br><a action=\"bypass -h npc_").append(getObjectId()).append("_fc_delme\">\u041E\u0442\u043A\u0430\u0437\u0430\u0442\u044C\u0441\u044F \u043E\u0442 \u0443\u0447\u0430\u0441\u0442\u0438\u044F</a>").toString());
      return;
    }

    if (command.equalsIgnoreCase("massPvpReg"))
    {
      if (!Config.MASS_PVP)
      {
        showError(player, Static.EVENT_DISABLED);
        return;
      }
      massPvp.getEvent().regPlayer(player);
    }
    else if (command.equalsIgnoreCase("massPvpStat"))
    {
      if (!Config.MASS_PVP)
      {
        showError(player, Static.EVENT_DISABLED);
        return;
      }
      if (System.currentTimeMillis() - player.getMPVPLast() < 1000L)
      {
        showError(player, "\u041E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u0435 \u0440\u0430\u0437 \u0432 \u0441\u0435\u043A\u0443\u043D\u0434\u0443.");
        return;
      }
      player.setMPVPLast();
      showMassPvP(player);
    }
    else if (command.equalsIgnoreCase("lastHeroReg"))
    {
      if (!Config.ELH_ENABLE)
      {
        showError(player, Static.EVENT_DISABLED);
        return;
      }
      LastHero.getEvent().regPlayer(player);
    }
    else if (command.equalsIgnoreCase("lastHeroDel"))
    {
      if (!Config.ELH_ENABLE)
      {
        showError(player, Static.EVENT_DISABLED);
        return;
      }
      LastHero.getEvent().delPlayer(player);
    }
    else if (command.equalsIgnoreCase("fightingReg"))
    {
      if (!Config.FIGHTING_ENABLE)
      {
        showError(player, Static.EVENT_DISABLED);
        return;
      }
      Fighting.getEvent().regPlayer(player);
    }
    else if (command.equalsIgnoreCase("fightingDel"))
    {
      if (!Config.FIGHTING_ENABLE)
      {
        showError(player, Static.EVENT_DISABLED);
        return;
      }
      Fighting.getEvent().delPlayer(player);
    }
    else if (command.equalsIgnoreCase("baseCaptureReg"))
    {
      if (!Config.EBC_ENABLE)
      {
        showError(player, Static.EVENT_DISABLED);
        return;
      }
      BaseCapture.getEvent().regPlayer(player);
    }
    else if (command.equalsIgnoreCase("baseCaptureDel"))
    {
      if (!Config.EBC_ENABLE)
      {
        showError(player, Static.EVENT_DISABLED);
        return;
      }
      BaseCapture.getEvent().delPlayer(player);
    }
    else if (command.equalsIgnoreCase("openSeasonReg"))
    {
      if (!Config.OPEN_SEASON)
      {
        showError(player, Static.EVENT_DISABLED);
        return;
      }
      OpenSeason.getEvent().regPlayer(player);
    }
    else if (command.equalsIgnoreCase("encounterReg"))
    {
      if (!Config.EENC_ENABLE)
      {
        showError(player, Static.EVENT_DISABLED);
        return;
      }
      Encounter.getEvent().regPlayer(player);
    }
    else if (command.equalsIgnoreCase("encounterDel"))
    {
      if (!Config.EENC_ENABLE)
      {
        showError(player, Static.EVENT_DISABLED);
        return;
      }
      Encounter.getEvent().delPlayer(player);
    }
    else if (command.startsWith("fc_"))
    {
      if (!EventManager.getInstance().checkPlayer(player))
      {
        showError(player, "Oops!");
        return;
      }

      String choise = command.substring(3).trim();

      if (choise.equalsIgnoreCase("show")) {
        FightClub.showFighters(player, getObjectId());
      } else if (choise.equalsIgnoreCase("items")) {
        showAllowItems(player);
      } else if (choise.equalsIgnoreCase("delme"))
      {
        player.setFClub(false);
        player.setFightClub(false);
        player.setEventWait(false);
        FightClub.unReg(player.getObjectId(), false);
        player.sendCritMessage("\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F \u043E\u0442\u043C\u0435\u043D\u0435\u043D\u0430, \u0441\u0442\u0430\u0432\u043A\u0430 \u0432\u043E\u0437\u0432\u0440\u0430\u0449\u0435\u043D\u0430 \u043D\u0430 \u043F\u043E\u0447\u0442\u0443.");
        player.sendPacket(new ExMailArrived());
      }
      else if (choise.startsWith("reg"))
      {
        int type = 0;
        try
        {
          type = Integer.parseInt(choise.substring(3).trim());
        }
        catch (Exception e)
        {
        }

        FightClub.showInventoryItems(player, type, getObjectId());
      }
      else if (choise.startsWith("item_"))
      {
        try
        {
          String[] opaopa = choise.split("_");
          int type = Integer.parseInt(opaopa[1]);
          int obj = Integer.parseInt(opaopa[2]);
          if (obj == 0)
          {
            showError(player, "\u0428\u043C\u043E\u0442\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430. 1");
            return;
          }
          FightClub.showItemFull(player, obj, type, getObjectId());
        }
        catch (Exception e)
        {
          showError(player, "\u0428\u043C\u043E\u0442\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430. 2");
          return;
        }
      }
      else if (choise.startsWith("add"))
      {
        int obj = 0;
        int count = 0;
        String pass = "";
        try
        {
          String[] opaopa = choise.split(" ");
          obj = Integer.parseInt(opaopa[1]);
          count = Integer.parseInt(opaopa[2]);
        }
        catch (Exception e)
        {
          obj = 0;
          count = 0;
        }

        if ((obj == 0) || (count == 0))
        {
          showError(player, "\u0428\u043C\u043E\u0442\u043A\u0430 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430. 3");
          return;
        }

        FightClub.finishItemFull(player, obj, count, pass, getObjectId());
      }
      else if (choise.startsWith("enemy"))
      {
        int id = 0;
        try
        {
          id = Integer.parseInt(choise.substring(5).trim());
        }
        catch (Exception e)
        {
        }

        FightClub.showEnemyDetails(player, id, getObjectId());
      }
      else if (choise.startsWith("accept"))
      {
        int id = 0;
        try
        {
          id = Integer.parseInt(choise.substring(6).trim());
        }
        catch (Exception e)
        {
        }

        FightClub.startFight(player, id, getObjectId());
      }
      else if (choise.equalsIgnoreCase("view")) {
        FightClub.viewFights(player, getObjectId());
      } else if (choise.startsWith("arview"))
      {
        int arena = 0;
        try
        {
          arena = Integer.parseInt(choise.substring(6).trim());
        }
        catch (Exception e)
        {
        }

        FightClub.viewArena(player, arena, getObjectId());
      }
    }
    else {
      super.onBypassFeedback(player, command);
    }
    player.sendActionFailed();
  }

  private void showMassPvP(L2PcInstance player)
  {
    _winners = new FastTable();
    _winners.addAll(massPvp.getEvent().getWinners());

    String swinner = massPvp.getEvent().getWinner();
    int round = massPvp.getEvent().getRound();

    NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
    StringBuffer replyMSG = new StringBuffer("<html><body><font color=LEVEL>-\u041C\u0430\u0441\u0441 \u041F\u0412\u041F-</font>");
    if (round > 0)
      replyMSG.append(new StringBuilder().append("<table width=280><tr><td></td><td align=right><font color=336699>\u0421\u043B\u0435\u0434\u0443\u044E\u0449\u0438\u0439 \u0440\u0430\u0443\u043D\u0434:</font> <font color=33CCFF>").append(round).append(" </font></td></tr>").toString());
    else
      replyMSG.append("<table width=280><tr><td></td><td align=right><font color=336699>\u041E\u0436\u0438\u0434\u0430\u0435\u0442\u0441\u044F \u0437\u0430\u043F\u0443\u0441\u043A \u0435\u0432\u0435\u043D\u0442\u0430</font> <font color=33CCFF> </font></td></tr>");
    replyMSG.append("<tr><td>\u041F\u043E\u0431\u0435\u0434\u0438\u0442\u0435\u043B\u0438:</td><td align=right></td></tr>");

    int i = 0; for (int n = _winners.size(); i < n; i++)
    {
      L2PcInstance winner = (L2PcInstance)_winners.get(i);
      replyMSG.append(new StringBuilder().append("<tr><td>\u0420\u0430\u0443\u043D\u0434 ").append(i + 1).append("</td><td align=right> ").append(winner.getName()).append(" </td></tr>").toString());
    }

    if (!swinner.equalsIgnoreCase("d")) {
      replyMSG.append(new StringBuilder().append("<tr><td>\u041F\u0440\u043E\u0448\u043B\u044B\u0439 </td><td align=right> \u041F\u043E\u0431\u0435\u0434\u0438\u0442\u0435\u043B\u044C: ").append(swinner).append("</td></tr>").toString());
    }
    replyMSG.append("</table><br><br>");
    replyMSG.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_massPvpStat\">\u041E\u0431\u043D\u043E\u0432\u0438\u0442\u044C</a><br>").toString());
    replyMSG.append("</body></html>");

    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
    replyMSG = null;
    reply = null;
  }

  private void showAllowItems(L2PcInstance player)
  {
    NpcHtmlMessage reply = NpcHtmlMessage.id(0);
    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("\u0411\u043E\u0439\u0446\u043E\u0432\u0441\u043A\u0438\u0439 \u043A\u043B\u0443\u0431:<br>\u0420\u0430\u0437\u0440\u0435\u0448\u0435\u043D\u043D\u044B\u0435 \u0441\u0442\u0430\u0432\u043A\u0438<br><table width=300>");

    int i = 0; for (int n = _allowItems.size(); i < n; i++)
    {
      Integer itemId = (Integer)_allowItems.get(i);
      if (itemId == null) {
        continue;
      }
      L2Item item = ItemTable.getInstance().getTemplate(itemId.intValue());
      if (item == null) {
        continue;
      }
      replyMSG.append(new StringBuilder().append("<tr><td><img src=\"").append(item.getIcon()).append("\" width=32 height=32></td><td>").append(item.getName()).append("</td></tr>").toString());
    }

    replyMSG.append(new StringBuilder().append("</table><br><br><a action=\"bypass -h npc_").append(getObjectId()).append("_Chat 0\">\u0412\u0435\u0440\u043D\u0443\u0442\u044C\u0441\u044F</a><br>").toString());
    replyMSG.append("</body></html>");
    reply.setHtml(replyMSG.toString());
    player.sendPacket(reply);
    player.sendActionFailed();
    replyMSG = null;
    reply = null;
  }

  private void showError(L2PcInstance player, String errorText)
  {
    player.setFCItem(0, 0, 0, 0);
    player.sendHtmlMessage("-\u0411\u043E\u0439\u0446\u043E\u0441\u043A\u0438\u0439 \u043A\u043B\u0443\u0431-", errorText);
    player.sendActionFailed();
  }
}