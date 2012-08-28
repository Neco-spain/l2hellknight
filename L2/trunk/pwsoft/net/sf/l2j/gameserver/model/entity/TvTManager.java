package net.sf.l2j.gameserver.model.entity;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.util.log.AbstractLogger;

public class TvTManager
  implements Runnable
{
  private static final Logger _log = AbstractLogger.getLogger(TvTManager.class.getName());

  private static TvTManager _instance = null;

  private TvTManager()
  {
    if (Config.TVT_EVENT_ENABLED) {
      ThreadPoolManager.getInstance().scheduleGeneral(this, 0L);
      _log.info("TvTEventEngine[TvTManager.TvTManager()]: Started.");
    } else {
      _log.info("TvTEventEngine[TvTManager.TvTManager()]: Engine is disabled.");
    }
  }

  public static TvTManager getInstance()
  {
    if (_instance == null) {
      _instance = new TvTManager();
    }

    return _instance;
  }

  public void run()
  {
    TvTEvent.init();
    while (true)
    {
      waiter(Config.TVT_EVENT_INTERVAL * 60);

      if (!TvTEvent.startParticipation()) {
        Announcements.getInstance().announceToAll(Static.TVT_CANCELED);
        _log.warning("TvTEventEngine[TvTManager.run()]: Error spawning event npc for participation.");
        continue;
      }
      Announcements.getInstance().announceToAll(Static.TVT_REG_FOR_S1.replaceAll("%a%", String.valueOf(Config.TVT_EVENT_PARTICIPATION_TIME)));

      waiter(Config.TVT_EVENT_PARTICIPATION_TIME * 60);

      if (!TvTEvent.startFight()) {
        Announcements.getInstance().announceToAll(Static.TVT_CANCELED);
        _log.info("TvTEventEngine[TvTManager.run()]: Lack of registration, abort event.");
        continue;
      }
      TvTEvent.sysMsgToAllParticipants(Static.TVT_TELE_ARENA_S1.replaceAll("%a%", String.valueOf(Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY)));

      waiter(Config.TVT_EVENT_RUNNING_TIME * 60);
      Announcements.getInstance().announceToAll(TvTEvent.calculateRewards());
      TvTEvent.sysMsgToAllParticipants(Static.TVT_RETURN_TOWN_S1.replaceAll("%a%", String.valueOf(Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY)));
      TvTEvent.stopFight();
    }
  }

  void waiter(int seconds)
  {
    while (seconds > 1) {
      seconds--;

      if ((TvTEvent.isParticipating()) || (TvTEvent.isStarted())) {
        String winner = "C\u0438\u043D\u0438\u0435";
        if (TvTEvent.isStarted()) {
          int[] teamsPointsCounts = TvTEvent.getTeamsPoints();
          if (teamsPointsCounts[1] > teamsPointsCounts[0]) {
            winner = "\u041A\u0440\u0430\u0441\u043D\u044B\u0435";
          }
        }

        switch (seconds) {
        case 3600:
          if (TvTEvent.isParticipating()) {
            Announcements.getInstance().announceToAll(Static.TVT_REG_FOR_S1_HOURS.replaceAll("%a%", String.valueOf(seconds / 60 / 60))); } else {
            if (!TvTEvent.isStarted()) break;
            TvTEvent.sysMsgToAllParticipants(Static.TVT_FINISH_FOR_S1_HOURS.replaceAll("%a%", String.valueOf(seconds / 60 / 60))); } break;
        case 600:
        case 900:
        case 1800:
          if (!TvTEvent.isStarted()) break;
          String event_end = Static.TVT_GAME_END_S1_MIN_WINNERS_S2.replaceAll("%a%", String.valueOf(seconds / 60));
          TvTEvent.spMsgToAllParticipants(event_end.replaceAll("%b%", winner));
          break;
        case 300:
          if (!TvTEvent.isStarted()) break;
          String event_end = Static.TVT_GAME_END_S1_MIN_WINNERS_S2.replaceAll("%a%", String.valueOf(seconds / 60));
          TvTEvent.spMsgToAllParticipants(event_end.replaceAll("%b%", winner));
          break;
        case 60:
        case 120:
        case 180:
        case 240:
          if (TvTEvent.isParticipating()) {
            Announcements.getInstance().announceToAll(Static.TVT_REG_FOR_S1_MIN.replaceAll("%a%", String.valueOf(seconds / 60)));
            if (!Config.CMD_EVENTS) break;
            Announcements.getInstance().announceToAll("\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F \u043D\u0430 \u0438\u0432\u0435\u043D\u0442\u044B: \u043A\u043E\u043C\u0430\u043D\u0434\u0430 .eventhelp");
          } else {
            if (!TvTEvent.isStarted()) break;
            String event_end = Static.TVT_GAME_END_S1_MIN_WINNERS_S2.replaceAll("%a%", String.valueOf(seconds / 60));
            TvTEvent.spMsgToAllParticipants(event_end.replaceAll("%b%", winner));
          }break;
        case 30:
          if (!TvTEvent.isStarted()) break;
          String event_end = Static.TVT_GAME_END_S1_SECS_WINNERS_S2.replaceAll("%a%", String.valueOf(seconds));
          TvTEvent.spMsgToAllParticipants(event_end.replaceAll("%b%", winner));
          break;
        case 15:
          if (!TvTEvent.isStarted()) break;
          String event_end = Static.TVT_GAME_END_S1_SECS_WINNERS_S2.replaceAll("%a%", String.valueOf(seconds));
          TvTEvent.spMsgToAllParticipants(event_end.replaceAll("%b%", winner));
          break;
        case 10:
          if (!TvTEvent.isStarted()) break;
          String event_end = Static.TVT_GAME_END_S1_SECS_WINNERS_S2.replaceAll("%a%", String.valueOf(seconds));
          TvTEvent.spMsgToAllParticipants(event_end.replaceAll("%b%", winner));
          break;
        case 5:
          if (!TvTEvent.isStarted()) break;
          String event_end = Static.TVT_GAME_END_S1_SECS_WINNERS_S2.replaceAll("%a%", String.valueOf(seconds));
          TvTEvent.spMsgToAllParticipants(event_end.replaceAll("%b%", winner));
          break;
        case 4:
          if (!TvTEvent.isStarted()) break;
          String event_end = Static.TVT_GAME_END_S1_SEC_WINNERS_S2.replaceAll("%a%", String.valueOf(seconds));
          TvTEvent.spMsgToAllParticipants(event_end.replaceAll("%b%", winner));
          break;
        case 3:
          if (!TvTEvent.isStarted()) break;
          String event_end = Static.TVT_GAME_END_S1_SEC_WINNERS_S2.replaceAll("%a%", String.valueOf(seconds));
          TvTEvent.spMsgToAllParticipants(event_end.replaceAll("%b%", winner));
          break;
        case 2:
          if (!TvTEvent.isStarted()) break;
          String event_end = Static.TVT_GAME_END_S1_SEC_WINNERS_S2.replaceAll("%a%", String.valueOf(seconds));
          TvTEvent.spMsgToAllParticipants(event_end.replaceAll("%b%", winner));
          break;
        case 1:
          if (TvTEvent.isParticipating()) {
            Announcements.getInstance().announceToAll(Static.TVT_REG_FOR_S1_SEC.replaceAll("%a%", String.valueOf(seconds))); } else {
            if (!TvTEvent.isStarted()) break;
            TvTEvent.spMsgToAllParticipants(Static.TVT_GAME_END_S1_SECS.replaceAll("%a%", String.valueOf(seconds)));
          }
        }

      }

      long oneSecWaitStart = System.currentTimeMillis();

      while (oneSecWaitStart + 1000L > System.currentTimeMillis())
        try {
          Thread.sleep(1L);
        }
        catch (InterruptedException ie)
        {
        }
    }
  }
}