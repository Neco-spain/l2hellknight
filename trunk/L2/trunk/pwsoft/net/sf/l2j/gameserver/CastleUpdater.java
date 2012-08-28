package net.sf.l2j.gameserver;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.util.log.AbstractLogger;

public class CastleUpdater
  implements Runnable
{
  protected static final Logger _log = AbstractLogger.getLogger(CastleUpdater.class.getName());
  private L2Clan _c;
  private int _rc = 0;

  public CastleUpdater(L2Clan c, int rc) {
    _c = c;
    _rc = rc;
  }

  public void run()
  {
    try {
      ItemContainer warehouse = _c.getWarehouse();
      if ((warehouse != null) && (_c.getHasCastle() > 0)) {
        Castle castle = CastleManager.getInstance().getCastleById(_c.getHasCastle());
        if ((!Config.ALT_MANOR_SAVE_ALL_ACTIONS) && 
          (_rc % Config.ALT_MANOR_SAVE_PERIOD_RATE == 0)) {
          castle.saveSeedData();
          castle.saveCropData();
          _log.info("Manor System: all data for " + castle.getName() + " saved");
        }

        _rc += 1;
        CastleUpdater cu = new CastleUpdater(_c, _rc);
        ThreadPoolManager.getInstance().scheduleGeneral(cu, 3600000L);
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
}