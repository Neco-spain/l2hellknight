package l2m.gameserver.instancemanager;

import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2m.gameserver.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BloodAltarManager
{
  private static final Logger _log = LoggerFactory.getLogger(BloodAltarManager.class);
  private static BloodAltarManager _instance;
  private static final long delay = 1800000L;
  private static long bossRespawnTimer = 0L;
  private static boolean bossesSpawned = false;
  private static final String[] bossGroups = { "bloodaltar_boss_aden", "bloodaltar_boss_darkelf", "bloodaltar_boss_dion", "bloodaltar_boss_dwarw", "bloodaltar_boss_giran", "bloodaltar_boss_gludin", "bloodaltar_boss_gludio", "bloodaltar_boss_goddart", "bloodaltar_boss_heine", "bloodaltar_boss_orc", "bloodaltar_boss_oren", "bloodaltar_boss_schutgart" };

  public static BloodAltarManager getInstance()
  {
    if (_instance == null)
      _instance = new BloodAltarManager();
    return _instance;
  }

  public BloodAltarManager()
  {
    _log.info("Blood Altar Manager: Initializing...");
    manageNpcs(true);
    ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
    {
      public void runImpl()
        throws Exception
      {
        if ((Rnd.chance(30)) && (BloodAltarManager.bossRespawnTimer < System.currentTimeMillis()))
          if (!BloodAltarManager.bossesSpawned)
          {
            BloodAltarManager.access$200(false);
            BloodAltarManager.access$300(true);
            BloodAltarManager.access$102(true);
          }
          else
          {
            BloodAltarManager.access$300(false);
            BloodAltarManager.access$200(true);
            BloodAltarManager.access$102(false);
          }
      }
    }
    , 1800000L, 1800000L);
  }

  private static void manageNpcs(boolean spawnAlive)
  {
    if (spawnAlive)
    {
      SpawnManager.getInstance().despawn("bloodaltar_dead_npc");
      SpawnManager.getInstance().spawn("bloodaltar_alive_npc");
    }
    else
    {
      SpawnManager.getInstance().despawn("bloodaltar_alive_npc");
      SpawnManager.getInstance().spawn("bloodaltar_dead_npc");
    }
  }

  private static void manageBosses(boolean spawn)
  {
    if (spawn) {
      for (String s : bossGroups)
        SpawnManager.getInstance().spawn(s);
    }
    else {
      bossRespawnTimer = System.currentTimeMillis() + 14400000L;
      for (String s : bossGroups)
        SpawnManager.getInstance().despawn(s);
    }
  }
}