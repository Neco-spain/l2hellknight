package net.sf.l2j.gameserver.util;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.Config.PvpColor;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public class PcCafe
{
  private static final Logger _log = AbstractLogger.getLogger(PcCafe.class.getName());

  private static final int pc_min = Config.PC_CAFE_BONUS.nick;
  private static final int pc_max = Config.PC_CAFE_BONUS.title;
  private static PcCafe _instance;

  public static PcCafe getInstance()
  {
    return _instance;
  }

  public static void init() {
    _instance = new PcCafe();
    _instance.load();
  }

  private void load()
  {
    ThreadPoolManager.getInstance().scheduleGeneral(new UpdateTask(), Config.PC_CAFE_INTERVAL);
  }

  private void updatePoints()
  {
    new Thread(new Runnable()
    {
      public void run()
      {
        try {
          for (L2PcInstance player : L2World.getInstance().getAllPlayers())
          {
            if ((player == null) || (player.isInOfflineMode())) {
              continue;
            }
            player.updatePcPoints(Rnd.get(PcCafe.pc_min, PcCafe.pc_max), 2, Rnd.get(100) < Config.PC_CAFE_DOUBLE_CHANCE);
          }
        }
        catch (Exception ignored)
        {
        }

        ThreadPoolManager.getInstance().scheduleGeneral(new PcCafe.UpdateTask(PcCafe.this), Config.PC_CAFE_INTERVAL);
      }
    }).start();
  }

  class UpdateTask
    implements Runnable
  {
    UpdateTask()
    {
    }

    public void run()
    {
      PcCafe.this.updatePoints();
    }
  }
}