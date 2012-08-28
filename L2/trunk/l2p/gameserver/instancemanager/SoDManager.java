package l2p.gameserver.instancemanager;

import java.util.List;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.instances.DoorInstance;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.ReflectionUtils;
import l2p.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoDManager
{
  private static final String SPAWN_GROUP = "sod_free";
  private static final Logger _log = LoggerFactory.getLogger(SoDManager.class);
  private static SoDManager _instance;
  private static long SOD_OPEN_TIME = 43200000L;
  private static Zone _zone;
  private static boolean _isOpened = false;

  public static SoDManager getInstance()
  {
    if (_instance == null)
      _instance = new SoDManager();
    return _instance;
  }

  public SoDManager()
  {
    _log.info("Seed of Destruction Manager: Loaded");
    _zone = ReflectionUtils.getZone("[inner_destruction01]");
    if (!isAttackStage())
      openSeed(getOpenedTimeLimit());
  }

  public static boolean isAttackStage()
  {
    return getOpenedTimeLimit() <= 0L;
  }

  public static void addTiatKill()
  {
    if (!isAttackStage())
      return;
    if (getTiatKills() < 9)
      ServerVariables.set("Tial_kills", getTiatKills() + 1);
    else
      openSeed(SOD_OPEN_TIME);
  }

  public static int getTiatKills()
  {
    return ServerVariables.getInt("Tial_kills", 0);
  }

  private static long getOpenedTimeLimit()
  {
    return ServerVariables.getLong("SoD_opened", 0L) * 1000L - System.currentTimeMillis();
  }

  private static Zone getZone()
  {
    return _zone;
  }

  public static void teleportIntoSeed(Player p)
  {
    p.teleToLocation(new Location(-245800, 220488, -12112));
  }

  public static void handleDoors(boolean doOpen)
  {
    for (int i = 12240003; i <= 12240031; i++)
    {
      if (doOpen)
        ReflectionUtils.getDoor(i).openMe();
      else
        ReflectionUtils.getDoor(i).closeMe();
    }
  }

  public static void openSeed(long timelimit)
  {
    if (_isOpened)
      return;
    _isOpened = true;
    ServerVariables.unset("Tial_kills");
    ServerVariables.set("SoD_opened", (System.currentTimeMillis() + timelimit) / 1000L);

    _log.info("Seed of Destruction Manager: Opening the seed for " + Util.formatTime((int)timelimit / 1000));
    SpawnManager.getInstance().spawn("sod_free");
    handleDoors(true);

    ThreadPoolManager.getInstance().schedule(new RunnableImpl()
    {
      public void runImpl()
        throws Exception
      {
        SoDManager.closeSeed();
      }
    }
    , timelimit);
  }

  public static void closeSeed()
  {
    if (!_isOpened)
      return;
    _isOpened = false;
    _log.info("Seed of Destruction Manager: Closing the seed.");
    ServerVariables.unset("SoD_opened");
    SpawnManager.getInstance().despawn("sod_free");

    for (Playable p : getZone().getInsidePlayables())
      p.teleToLocation((Location)getZone().getRestartPoints().get(0));
    handleDoors(false);
  }
}