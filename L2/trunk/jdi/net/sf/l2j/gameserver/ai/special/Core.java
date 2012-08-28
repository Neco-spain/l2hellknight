package net.sf.l2j.gameserver.ai.special;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.gameserver.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.Rnd;

public class Core extends L2AttackableAIScript
{
  private static Logger _log = Logger.getLogger(Core.class.getName());
  Calendar time = Calendar.getInstance();
  private static final int CORE = 29006;
  private static final int DEATH_KNIGHT = 29007;
  private static final int DOOM_WRAITH = 29008;
  private static final int SUSCEPTOR = 29011;
  private static final byte ALIVE = 0;
  private static final byte DEAD = 1;
  private static boolean _FirstAttacked;
  List<L2Attackable> Minions = new FastList();

  public Core(int id, String name, String descr)
  {
    super(id, name, descr);

    int[] mobs = { 29006, 29007, 29008, 29011 };
    registerMobs(mobs);

    _FirstAttacked = false;
    StatsSet info = GrandBossManager.getInstance().getStatsSet(29006);
    int status = GrandBossManager.getInstance().getBossStatus(29006);
    if (status == 1)
    {
      long temp = info.getLong("respawn_time") - System.currentTimeMillis();

      if (temp > 0L) {
        startQuestTimer("core_unlock", temp, null, null);
      }
      else
      {
        L2GrandBossInstance core = (L2GrandBossInstance)addSpawn(29006, 17726, 108915, -6480, 0, false, 0);
        GrandBossManager.getInstance().setBossStatus(29006, 0);
        spawnBoss(core);
      }
    }
    else
    {
      String test = loadGlobalQuestVar("Core_Attacked");
      if (test.equalsIgnoreCase("true"))
        _FirstAttacked = true;
      int loc_x = info.getInteger("loc_x");
      int loc_y = info.getInteger("loc_y");
      int loc_z = info.getInteger("loc_z");
      int heading = info.getInteger("heading");
      int hp = info.getInteger("currentHP");
      int mp = info.getInteger("currentMP");
      L2GrandBossInstance core = (L2GrandBossInstance)addSpawn(29006, loc_x, loc_y, loc_z, heading, false, 0);
      core.setCurrentHpMp(hp, mp);
      spawnBoss(core);
    }
  }

  public void saveGlobalData()
  {
    String val = "" + _FirstAttacked;
    saveGlobalQuestVar("Core_Attacked", val);
  }

  public void spawnBoss(L2GrandBossInstance npc)
  {
    GrandBossManager.getInstance().addBoss(npc);
    npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));

    for (int i = 0; i < 5; i++)
    {
      int x = 16800 + i * 360;
      Minions.add((L2Attackable)addSpawn(29007, x, 110000, npc.getZ(), 280 + Rnd.get(40), false, 0));
      Minions.add((L2Attackable)addSpawn(29007, x, 109000, npc.getZ(), 280 + Rnd.get(40), false, 0));
      int x2 = 16800 + i * 600;
      Minions.add((L2Attackable)addSpawn(29008, x2, 109300, npc.getZ(), 280 + Rnd.get(40), false, 0));
    }
    for (int i = 0; i < 4; i++)
    {
      int x = 16800 + i * 450;
      Minions.add((L2Attackable)addSpawn(29011, x, 110300, npc.getZ(), 280 + Rnd.get(40), false, 0));
    }
  }

  public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
  {
    if (event.equalsIgnoreCase("core_unlock"))
    {
      L2GrandBossInstance core = (L2GrandBossInstance)addSpawn(29006, 17726, 108915, -6480, 0, false, 0);
      GrandBossManager.getInstance().setBossStatus(29006, 0);
      spawnBoss(core);
    }
    else if (event.equalsIgnoreCase("spawn_minion")) {
      Minions.add((L2Attackable)addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0));
    } else if (event.equalsIgnoreCase("despawn_minions"))
    {
      for (int i = 0; i < Minions.size(); i++)
      {
        L2Attackable mob = (L2Attackable)Minions.get(i);
        if (mob != null)
          mob.decayMe();
      }
      Minions.clear();
    }
    return super.onAdvEvent(event, npc, player);
  }

  public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    if (npc.getNpcId() == 29006)
    {
      if (_FirstAttacked)
      {
        if (Rnd.get(100) == 0)
          npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Removing intruders."));
      }
      else
      {
        _FirstAttacked = true;
        npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "A non-permitted target has been discovered."));
        npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Starting intruder removal system."));
      }
    }
    return super.onAttack(npc, attacker, damage, isPet);
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    int npcId = npc.getNpcId();
    if (npcId == 29006)
    {
      int objId = npc.getObjectId();
      npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, objId, npc.getX(), npc.getY(), npc.getZ()));
      npc.broadcastPacket(new NpcSay(objId, 0, npcId, "A fatal error has occurred."));
      npc.broadcastPacket(new NpcSay(objId, 0, npcId, "System is being shut down..."));
      npc.broadcastPacket(new NpcSay(objId, 0, npcId, "......"));
      _FirstAttacked = false;
      addSpawn(31842, 16502, 110165, -6394, 0, false, 900000);
      addSpawn(31842, 18948, 110166, -6397, 0, false, 900000);
      GrandBossManager.getInstance().setBossStatus(29006, 1);
      _log.warning(" - Epic: Core killed: " + time.getTime());

      long respawnTime = (27 + Rnd.get(47)) * 3600000;
      startQuestTimer("core_unlock", respawnTime, null, null);

      StatsSet info = GrandBossManager.getInstance().getStatsSet(29006);
      info.set("respawn_time", System.currentTimeMillis() + respawnTime);
      GrandBossManager.getInstance().setStatsSet(29006, info);
      startQuestTimer("despawn_minions", 20000L, null, null);
      cancelQuestTimers("spawn_minion");
    }
    else if ((GrandBossManager.getInstance().getBossStatus(29006) == 0) && (Minions != null) && (Minions.contains(npc)))
    {
      Minions.remove(npc);
      startQuestTimer("spawn_minion", 60000L, npc, null);
    }
    return super.onKill(npc, killer, isPet);
  }
}