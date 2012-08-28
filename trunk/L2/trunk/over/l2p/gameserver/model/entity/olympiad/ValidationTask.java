package l2p.gameserver.model.entity.olympiad;

import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.instancemanager.OlympiadHistoryManager;
import l2p.gameserver.model.entity.Hero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationTask extends RunnableImpl
{
  private static final Logger _log = LoggerFactory.getLogger(ValidationTask.class);

  public void runImpl()
    throws Exception
  {
    OlympiadHistoryManager.getInstance().switchData();

    OlympiadDatabase.sortHerosToBe();
    OlympiadDatabase.saveNobleData();
    if (Hero.getInstance().computeNewHeroes(Olympiad._heroesToBe)) {
      _log.warn("Olympiad: Error while computing new heroes!");
    }

    Olympiad._period = 0;
    Olympiad._currentCycle += 1;

    OlympiadDatabase.cleanupNobles();
    OlympiadDatabase.loadNoblesRank();
    OlympiadDatabase.setNewOlympiadEnd();

    Olympiad.init();
    OlympiadDatabase.save();
  }
}