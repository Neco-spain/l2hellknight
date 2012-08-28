package l2p.gameserver.model.entity.olympiad;

import java.util.Map;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Announcements;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CompEndTask extends RunnableImpl
{
  private static final Logger _log = LoggerFactory.getLogger(CompEndTask.class);

  public void runImpl()
    throws Exception
  {
    if (Olympiad.isOlympiadEnd()) {
      return;
    }
    Olympiad._inCompPeriod = false;
    try
    {
      OlympiadManager manager = Olympiad._manager;

      if ((manager != null) && (!manager.getOlympiadGames().isEmpty()))
      {
        ThreadPoolManager.getInstance().schedule(new CompEndTask(), 60000L);
        return;
      }

      Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_ENDED);
      _log.info("Olympiad System: Olympiad Game Ended");
      OlympiadDatabase.save();
    }
    catch (Exception e)
    {
      _log.warn("Olympiad System: Failed to save Olympiad configuration:");
      _log.error("", e);
    }
    Olympiad.init();
  }
}