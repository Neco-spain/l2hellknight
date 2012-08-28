package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2TvTEventNpcInstance extends L2NpcInstance
{
  private static final String htmlPath = "data/html/mods/TvTEvent/";

  public L2TvTEventNpcInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance playerInstance, String command)
  {
    TvTEvent.onBypass(command, playerInstance);
  }

  public void showChatWindow(L2PcInstance playerInstance, int val)
  {
    if (playerInstance == null) {
      return;
    }
    if (TvTEvent.isParticipating())
    {
      boolean isParticipant = TvTEvent.isPlayerParticipant(playerInstance.getObjectId());
      String htmContent;
      if (!isParticipant)
        htmContent = HtmCache.getInstance().getHtm(htmlPath + "Participation.htm");
      else {
        htmContent = HtmCache.getInstance().getHtm(htmlPath + "RemoveParticipation.htm");
      }
      if (htmContent != null)
      {
        int[] teamsPlayerCounts = TvTEvent.getTeamsPlayerCounts();
        NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

        npcHtmlMessage.setHtml(htmContent);
        npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
        npcHtmlMessage.replace("%team1name%", Config.TVT_EVENT_TEAM_1_NAME);
        npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
        npcHtmlMessage.replace("%team2name%", Config.TVT_EVENT_TEAM_2_NAME);
        npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
        npcHtmlMessage.replace("%playercount%", String.valueOf(teamsPlayerCounts[0] + teamsPlayerCounts[1]));
        if (!isParticipant) {
          npcHtmlMessage.replace("%fee%", TvTEvent.getParticipationFee());
        }
        playerInstance.sendPacket(npcHtmlMessage);
      }
    }
    else if ((TvTEvent.isStarting()) || (TvTEvent.isStarted()))
    {
      String htmContent = HtmCache.getInstance().getHtm(htmlPath + "Status.htm");

      if (htmContent != null)
      {
        int[] teamsPlayerCounts = TvTEvent.getTeamsPlayerCounts();
        int[] teamsPointsCounts = TvTEvent.getTeamsPoints();
        NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

        npcHtmlMessage.setHtml(htmContent);

        npcHtmlMessage.replace("%team1name%", Config.TVT_EVENT_TEAM_1_NAME);
        npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
        npcHtmlMessage.replace("%team1points%", String.valueOf(teamsPointsCounts[0]));
        npcHtmlMessage.replace("%team2name%", Config.TVT_EVENT_TEAM_2_NAME);
        npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
        npcHtmlMessage.replace("%team2points%", String.valueOf(teamsPointsCounts[1]));
        playerInstance.sendPacket(npcHtmlMessage);
      }
    }

    playerInstance.sendPacket(new ActionFailed());
  }
}