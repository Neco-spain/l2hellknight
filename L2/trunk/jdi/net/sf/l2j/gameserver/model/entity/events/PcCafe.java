package net.sf.l2j.gameserver.model.entity.events;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.util.Rnd;

public class PcCafe
{
  Logger _log;
  private static PcCafe ins;
  private Future scoreTask;

  public static PcCafe getInstance()
  {
    if (ins == null)
      ins = new PcCafe();
    return ins;
  }

  public PcCafe()
  {
    _log = Logger.getLogger(PcCafe.class.getName());
    scoreTask = null;
    if (scoreTask == null)
      startTask();
  }

  private void startTask()
  {
    _log.info("PC Cafe: Task initialization in 35 seconds.");
    scoreTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new OnlineUpdateTask(null), 35000L, Config.pccafe_interval * 1000);
  }

  public void stopTask()
  {
    scoreTask.cancel(true);
    scoreTask = null;
  }

  private class OnlineUpdateTask
    implements Runnable
  {
    private OnlineUpdateTask()
    {
    }

    public void run()
    {
      int score = 0;
      int counter = 0;
      boolean duble = false;
      for (Iterator iterator = L2World.getInstance().getAllPlayers().iterator(); iterator.hasNext(); )
      {
        L2PcInstance cha = (L2PcInstance)iterator.next();
        if (cha.getLevel() > Config.pccafe_min_lvl)
        {
          score = Rnd.get(Config.pccafe_score_min, Config.pccafe_score_max);
          if (Rnd.get(100) <= Config.pccafe_score_double)
          {
            duble = true;
            score *= 2;
          }
          cha.addPcCafeScore(score);
          cha.sendPacket(new SystemMessage(duble ? 1708 : 1707).addNumber(score));
          cha.updatePcCafeWnd(score, true, duble);
        }
        counter++;
      }
    }
  }
}