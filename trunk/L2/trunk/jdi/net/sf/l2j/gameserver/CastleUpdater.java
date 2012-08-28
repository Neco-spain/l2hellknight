package net.sf.l2j.gameserver;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.Castle;

public class CastleUpdater
  implements Runnable
{
  protected static Logger _log = Logger.getLogger(CastleUpdater.class.getName());
  private L2Clan _clan;
  private int _runCount = 0;

  public CastleUpdater(L2Clan clan, int runCount)
  {
    _clan = clan;
    _runCount = runCount;
  }

  public void run()
  {
    try
    {
      ItemContainer warehouse = _clan.getWarehouse();
      if ((warehouse != null) && (_clan.getHasCastle() > 0))
      {
        Castle castle = CastleManager.getInstance().getCastleById(_clan.getHasCastle());
        if (!Config.ALT_MANOR_SAVE_ALL_ACTIONS)
        {
          if (_runCount % Config.ALT_MANOR_SAVE_PERIOD_RATE == 0)
          {
            castle.saveSeedData();
            castle.saveCropData();
            _log.info("Manor System: all data for " + castle.getName() + " saved");
          }
        }
        _runCount += 1;
        CastleUpdater cu = new CastleUpdater(_clan, _runCount);
        ThreadPoolManager.getInstance().scheduleGeneral(cu, 3600000L);
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
}