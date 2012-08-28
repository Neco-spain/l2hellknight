package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import scripts.autoevents.basecapture.BaseCapture;
import scripts.autoevents.lasthero.LastHero;
import scripts.autoevents.masspvp.massPvp;

public class L2TvTEventNpcInstance extends L2NpcInstance
{
  public L2TvTEventNpcInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    TvTEvent.onBypass(command, player);
  }

  public void showChatWindow(L2PcInstance player, int val)
  {
    if (player == null) {
      return;
    }
    if ((player.getKarma() > 0) || (player.isCursedWeaponEquiped()))
    {
      player.sendHtmlMessage("\u0423 \u0432\u0430\u0441 \u043F\u043B\u043E\u0445\u0430\u044F \u043A\u0430\u0440\u043C\u0430.");
      return;
    }
    if ((Config.TVT_NOBL) && (!player.isNoble()))
    {
      player.sendHtmlMessage("\u0422\u043E\u043B\u044C\u043A\u043E \u043D\u043E\u0431\u043B\u0435\u0441\u0441\u044B \u043C\u043E\u0433\u0443\u0442 \u0443\u0447\u0430\u0432\u0441\u0442\u0432\u043E\u0432\u0430\u0442\u044C");
      player.sendActionFailed();
      return;
    }
    if ((Config.MASS_PVP) && (massPvp.getEvent().isReg(player)))
    {
      player.sendHtmlMessage("\u0423\u0434\u0430\u0447\u0438 \u043D\u0430 \u0435\u0432\u0435\u043D\u0442\u0435 -\u041C\u0430\u0441\u0441 \u041F\u0412\u041F-");
      player.sendActionFailed();
      return;
    }
    if ((Config.ELH_ENABLE) && (LastHero.getEvent().isRegged(player)))
    {
      player.sendHtmlMessage("\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u0432 \u0435\u0432\u0435\u043D\u0442\u0435 -\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-");
      player.sendActionFailed();
      return;
    }
    if ((Config.EBC_ENABLE) && (BaseCapture.getEvent().isRegged(player)))
    {
      player.sendHtmlMessage("\u0423\u0434\u0430\u0447\u0438 \u043D\u0430 \u0435\u0432\u0435\u043D\u0442\u0435 -\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-");
      player.sendActionFailed();
      return;
    }
    if ((Olympiad.isRegisteredInComp(player)) || (player.isInOlympiadMode()))
    {
      player.sendHtmlMessage("\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u043D\u0430 \u043E\u043B\u0438\u043C\u043F\u0438\u0430\u0434\u0435.");
      player.sendActionFailed();
      return;
    }

    if (TvTEvent.isParticipating())
    {
      String htmFile = "data/html/mods/";

      if (!TvTEvent.isPlayerParticipant(player.getName()))
        htmFile = htmFile + "TvTEventParticipation";
      else {
        htmFile = htmFile + "TvTEventRemoveParticipation";
      }
      htmFile = htmFile + ".htm";

      String htmContent = HtmCache.getInstance().getHtm(htmFile);

      if (htmContent != null)
      {
        int[] teamsPlayerCounts = TvTEvent.getTeamsPlayerCounts();
        NpcHtmlMessage npcHtmlMessage = NpcHtmlMessage.id(getObjectId());

        npcHtmlMessage.setHtml(htmContent);
        npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
        npcHtmlMessage.replace("%team1name%", Config.TVT_EVENT_TEAM_1_NAME);
        npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
        npcHtmlMessage.replace("%team2name%", Config.TVT_EVENT_TEAM_2_NAME);
        npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
        player.sendPacket(npcHtmlMessage);
      }
    }
    else if ((TvTEvent.isStarting()) || (TvTEvent.isStarted()))
    {
      String htmFile = "data/html/mods/TvTEventStatus.htm";
      String htmContent = HtmCache.getInstance().getHtm(htmFile);

      if (htmContent != null)
      {
        int[] teamsPlayerCounts = TvTEvent.getTeamsPlayerCounts();
        int[] teamsPointsCounts = TvTEvent.getTeamsPoints();
        NpcHtmlMessage npcHtmlMessage = NpcHtmlMessage.id(getObjectId());

        npcHtmlMessage.setHtml(htmContent);

        npcHtmlMessage.replace("%team1name%", Config.TVT_EVENT_TEAM_1_NAME);
        npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
        npcHtmlMessage.replace("%team1points%", String.valueOf(teamsPointsCounts[0]));
        npcHtmlMessage.replace("%team2name%", Config.TVT_EVENT_TEAM_2_NAME);
        npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
        npcHtmlMessage.replace("%team2points%", String.valueOf(teamsPointsCounts[1]));
        player.sendPacket(npcHtmlMessage);
      }
    }
    else
    {
      String htmFile = "data/html/mods/TvTShop.htm";
      String htmContent = HtmCache.getInstance().getHtm(htmFile);

      if (htmContent != null)
      {
        NpcHtmlMessage npcHtmlMessage = NpcHtmlMessage.id(getObjectId());
        npcHtmlMessage.setHtml(htmContent);
        player.sendPacket(npcHtmlMessage);
      }
    }

    player.sendActionFailed();
  }
}