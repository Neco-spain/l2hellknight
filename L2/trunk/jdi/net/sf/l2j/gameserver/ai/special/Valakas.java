package net.sf.l2j.gameserver.ai.special;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.NpcKnownList;
import net.sf.l2j.gameserver.model.quest.QuestTimer;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public class Valakas extends L2AttackableAIScript
  implements Runnable
{
  private static Logger _log = Logger.getLogger(Valakas.class.getName());
  private int i_ai0 = 0;
  private int i_ai1 = 0;
  private int i_ai2 = 0;
  private int i_ai3 = 0;
  private int i_ai4 = 0;
  private int i_quest0 = 0;
  private int i_quest2 = 0;
  private int i_quest3 = 0;
  private int i_quest4 = 0;
  private L2Character c_quest2 = null;
  private L2Character c_quest3 = null;
  private L2Character c_quest4 = null;
  private static final int VALAKAS = 29028;
  private static final byte DORMANT = 0;
  private static final byte WAITING = 1;
  private static final byte FIGHTING = 2;
  private static final byte DEAD = 3;
  private static L2BossZone _Zone;
  private static long _LastAttackVsValakasTime = 0L;

  public Valakas(int id, String name, String descr)
  {
    super(id, name, descr);
    int[] mob = { 29028 };
    registerMobs(mob);
    i_ai0 = 0;
    i_ai1 = 0;
    i_ai2 = 0;
    i_ai3 = 0;
    i_ai4 = 0;
    i_quest0 = 0;
    _LastAttackVsValakasTime = System.currentTimeMillis();
    _Zone = GrandBossManager.getInstance().getZone(212852, -114842, -1632);
    StatsSet info = GrandBossManager.getInstance().getStatsSet(29028);

    Integer status = Integer.valueOf(GrandBossManager.getInstance().getBossStatus(29028));

    if (status.intValue() == 3)
    {
      long temp = info.getLong("respawn_time") - System.currentTimeMillis();

      if (temp > 0L)
      {
        startQuestTimer("valakas_unlock", temp, null, null);
      }
      else
      {
        GrandBossManager.getInstance().setBossStatus(29028, 0);
      }

    }
    else if (status.intValue() == 2)
    {
      int loc_x = 213004;
      int loc_y = -114890;
      int loc_z = -1595;
      int heading = 0;

      int hp = info.getInteger("currentHP");
      int mp = info.getInteger("currentMP");
      L2GrandBossInstance valakas = (L2GrandBossInstance)addSpawn(29028, loc_x, loc_y, loc_z, heading, false, 0);
      GrandBossManager.getInstance().addBoss(valakas);
      L2NpcInstance _valakas = valakas;

      ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(_valakas, hp, mp)
      {
        public void run()
        {
          try
          {
            val$_valakas.setCurrentHpMp(val$hp, val$mp);
            val$_valakas.setRunning();
          }
          catch (Throwable e)
          {
          }
        }
      }
      , 100L);

      startQuestTimer("1003", 60000L, valakas, null, true);

      startQuestTimer("1002", 60000L, valakas, null, true);
    }
    else if (status.intValue() == 1)
    {
      startQuestTimer("valakas_spawn", Config.Valakas_Wait_Time, null, null);
    }
  }

  public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
  {
    if (npc != null)
    {
      if (event.equalsIgnoreCase("1002"))
      {
        int lvl = 0;
        int sk_4691 = 0;
        L2Effect[] effects = npc.getAllEffects();
        if ((effects != null) && (effects.length != 0))
        {
          for (L2Effect e : effects)
          {
            if (e.getSkill().getId() != 4629)
              continue;
            sk_4691 = 1;
            lvl = e.getSkill().getLevel();
            break;
          }

        }

        Integer status = Integer.valueOf(GrandBossManager.getInstance().getBossStatus(29028));

        if (status.intValue() == 2)
        {
          if (_LastAttackVsValakasTime + 1800000L < System.currentTimeMillis())
          {
            try
            {
              _log.warning("Valakas Despawned : " + new Date());
              GrandBossManager.getInstance().setBossStatus(29028, 0);

              npc.deleteMe();
              _Zone.oustAllPlayers();
              cancelQuestTimer("1002", npc, null);
              i_quest2 = 0;
              i_quest3 = 0;
              i_quest4 = 0;
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }
          }
          else if (!_Zone.isInsideZone(npc))
            npc.teleToLocation(213004, -114890, -1595);
        }
        else if (npc.getCurrentHp() > npc.getMaxHp() * 1 / 4)
        {
          if ((sk_4691 == 0) || ((sk_4691 == 1) && (lvl != 4)))
          {
            npc.setTarget(npc);
            npc.doCast(SkillTable.getInstance().getInfo(4691, 4));
          }
        }
        else if (npc.getCurrentHp() > npc.getMaxHp() * 2 / 4.0D)
        {
          if ((sk_4691 == 0) || ((sk_4691 == 1) && (lvl != 3)))
          {
            npc.setTarget(npc);
            npc.doCast(SkillTable.getInstance().getInfo(4691, 3));
          }
        }
        else if (npc.getCurrentHp() > npc.getMaxHp() * 3 / 4.0D)
        {
          if ((sk_4691 == 0) || ((sk_4691 == 1) && (lvl != 2)))
          {
            npc.setTarget(npc);
            npc.doCast(SkillTable.getInstance().getInfo(4691, 2));
          }
        }
        else if ((sk_4691 == 0) || ((sk_4691 == 1) && (lvl != 1)))
        {
          npc.setTarget(npc);
          npc.doCast(SkillTable.getInstance().getInfo(4691, 1));
        }
      }
      else if (event.equalsIgnoreCase("1003"))
      {
        if (!npc.isInvul())
          getRandomSkill(npc);
        else
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
      }
      else if (event.equalsIgnoreCase("1004"))
      {
        startQuestTimer("1102", 1500L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1300, 180, -5, 3000, 15000));
      }
      else if (event.equalsIgnoreCase("1102"))
      {
        startQuestTimer("1103", 3300L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 500, 180, -8, 600, 15000));
      }
      else if (event.equalsIgnoreCase("1103"))
      {
        startQuestTimer("1104", 2900L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 800, 180, -8, 2700, 15000));
      }
      else if (event.equalsIgnoreCase("1104"))
      {
        startQuestTimer("1105", 2700L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 200, 250, 70, 0, 15000));
      }
      else if (event.equalsIgnoreCase("1105"))
      {
        startQuestTimer("1106", 1L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 250, 70, 2500, 15000));
      }
      else if (event.equalsIgnoreCase("1106"))
      {
        startQuestTimer("1107", 3200L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 700, 150, 30, 0, 15000));
      }
      else if (event.equalsIgnoreCase("1107"))
      {
        startQuestTimer("1108", 1400L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1200, 150, 20, 2900, 15000));
      }
      else if (event.equalsIgnoreCase("1108"))
      {
        startQuestTimer("1109", 6700L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 750, 170, 15, 3400, 15000));
      }
      else if (event.equalsIgnoreCase("1109"))
      {
        startQuestTimer("1110", 5700L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 750, 170, -10, 3400, 15000));
      }
      else if (event.equalsIgnoreCase("1110"))
      {
        GrandBossManager.getInstance().setBossStatus(29028, 2);
        startQuestTimer("1002", 60000L, npc, null, true);
        npc.setIsInvul(false);
        getRandomSkill(npc);
      }
      else if (event.equalsIgnoreCase("1111"))
      {
        startQuestTimer("1112", 3500L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 210, -5, 3000, 10000));
      }
      else if (event.equalsIgnoreCase("1112"))
      {
        startQuestTimer("1113", 4500L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1300, 200, -8, 3000, 10000));
      }
      else if (event.equalsIgnoreCase("1113"))
      {
        startQuestTimer("1114", 500L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1000, 190, 0, 3000, 10000));
      }
      else if (event.equalsIgnoreCase("1114"))
      {
        startQuestTimer("1115", 4600L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 120, 0, 2500, 10000));
      }
      else if (event.equalsIgnoreCase("1115"))
      {
        startQuestTimer("1116", 750L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 20, 0, 3000, 10000));
      }
      else if (event.equalsIgnoreCase("1116"))
      {
        startQuestTimer("1117", 2500L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 10, 0, 3000, 10000));
      }
      else if (event.equalsIgnoreCase("1117"))
      {
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 10, 0, 300, 15000, 250));
        addSpawn(31759, 212852, -114842, -1632, 0, false, 900000);
        int radius = 1500;
        for (int i = 0; i < 20; i++)
        {
          int x = (int)(radius * Math.cos(i * 0.331D));
          int y = (int)(radius * Math.sin(i * 0.331D));
          addSpawn(31759, 212852 + x, -114842 + y, -1632, 0, false, 900000);
        }
        cancelQuestTimer("1002", npc, null);
        startQuestTimer("remove_players", 900000L, null, null);

        GrandBossManager.getInstance().setBossStatus(29028, 3);
      }

    }
    else if (event.equalsIgnoreCase("valakas_spawn"))
    {
      try
      {
        int loc_x = 213004;
        int loc_y = -114890;
        int loc_z = -1595;
        int heading = 0;

        L2GrandBossInstance valakas = (L2GrandBossInstance)addSpawn(29028, loc_x, loc_y, loc_z, heading, false, 0);
        GrandBossManager.getInstance().addBoss(valakas);

        _log.warning("Valakas Spawned :" + new Date());
        _LastAttackVsValakasTime = System.currentTimeMillis();
        L2NpcInstance _valakas = valakas;
        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(_valakas)
        {
          public void run()
          {
            try
            {
              broadcastSpawn(val$_valakas);
            }
            catch (Throwable e)
            {
            }
          }
        }
        , 1L);

        startQuestTimer("1004", 2000L, valakas, null);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }

    }
    else if (event.equalsIgnoreCase("valakas_unlock"))
    {
      GrandBossManager.getInstance().setBossStatus(29028, 0);
    }
    else if (event.equalsIgnoreCase("remove_players"))
    {
      _Zone.oustAllPlayers();
    }

    return super.onAdvEvent(event, npc, player);
  }

  public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    if (npc.isInvul())
    {
      return null;
    }
    _LastAttackVsValakasTime = System.currentTimeMillis();

    if (attacker.getMountType() == 1)
    {
      int sk_4258 = 0;
      L2Effect[] effects = attacker.getAllEffects();
      if ((effects != null) && (effects.length != 0))
      {
        for (L2Effect e : effects)
        {
          if (e.getSkill().getId() != 4258)
            continue;
          sk_4258 = 1;
        }
      }

      if (sk_4258 == 0)
      {
        npc.setTarget(attacker);
        npc.doCast(SkillTable.getInstance().getInfo(4258, 1));
      }
    }
    if (attacker.getZ() < npc.getZ() + 200)
    {
      if (i_ai2 == 0)
      {
        i_ai1 += damage;
      }
      if (i_quest0 == 0)
      {
        i_ai4 += damage;
      }
      if (i_quest0 == 0)
      {
        i_ai3 += damage;
      }
      else if (i_ai2 == 0)
      {
        i_ai0 += damage;
      }
      if (i_quest0 == 0)
      {
        if (i_ai4 / npc.getMaxHp() * 100 > 1)
        {
          if (i_ai3 > i_ai4 - i_ai3)
          {
            i_ai3 = 0;
            i_ai4 = 0;
            npc.setTarget(npc);
            npc.doCast(SkillTable.getInstance().getInfo(4687, 1));
            i_quest0 = 1;
          }
        }
      }
    }

    int i1 = 0;

    if (attacker == c_quest2)
    {
      if (damage * 1000 + 1000 > i_quest2)
      {
        i_quest2 = (damage * 1000 + Rnd.get(3000));
      }
    }
    else if (attacker == c_quest3)
    {
      if (damage * 1000 + 1000 > i_quest3)
      {
        i_quest3 = (damage * 1000 + Rnd.get(3000));
      }
    }
    else if (attacker == c_quest4)
    {
      if (damage * 1000 + 1000 > i_quest4)
      {
        i_quest4 = (damage * 1000 + Rnd.get(3000));
      }
    }
    else if (i_quest2 > i_quest3)
    {
      i1 = 3;
    }
    else if (i_quest2 == i_quest3)
    {
      if (Rnd.get(100) < 50)
      {
        i1 = 2;
      }
      else
      {
        i1 = 3;
      }
    }
    else if (i_quest2 < i_quest3)
    {
      i1 = 2;
    }
    if (i1 == 2)
    {
      if (i_quest2 > i_quest4)
      {
        i1 = 4;
      }
      else if (i_quest2 == i_quest4)
      {
        if (Rnd.get(100) < 50)
        {
          i1 = 2;
        }
        else
        {
          i1 = 4;
        }
      }
      else if (i_quest2 < i_quest4)
      {
        i1 = 2;
      }
    }
    else if (i1 == 3)
    {
      if (i_quest3 > i_quest4)
      {
        i1 = 4;
      }
      else if (i_quest3 == i_quest4)
      {
        if (Rnd.get(100) < 50)
        {
          i1 = 3;
        }
        else
        {
          i1 = 4;
        }
      }
      else if (i_quest3 < i_quest4)
      {
        i1 = 3;
      }
    }
    if (i1 == 2)
    {
      i_quest2 = (damage * 1000 + Rnd.get(3000));
      c_quest2 = attacker;
    }
    else if (i1 == 3)
    {
      i_quest3 = (damage * 1000 + Rnd.get(3000));
      c_quest3 = attacker;
    }
    else if (i1 == 4)
    {
      i_quest4 = (damage * 1000 + Rnd.get(3000));
      c_quest4 = attacker;
    }

    if (i1 == 2)
    {
      if (i_quest2 > i_quest4)
      {
        i1 = 4;
      }
      else if (i_quest2 == i_quest4)
      {
        if (Rnd.get(100) < 50)
        {
          i1 = 2;
        }
        else
        {
          i1 = 4;
        }
      }
      else if (i_quest2 < i_quest4)
      {
        i1 = 2;
      }
    }
    else if (i1 == 3)
    {
      if (i_quest3 > i_quest4)
      {
        i1 = 4;
      }
      else if (i_quest3 == i_quest4)
      {
        if (Rnd.get(100) < 50)
        {
          i1 = 3;
        }
        else
        {
          i1 = 4;
        }
      }
      else if (i_quest3 < i_quest4)
      {
        i1 = 3;
      }
    }
    if (i1 == 2)
    {
      i_quest2 = (damage / 150 * 1000 + Rnd.get(3000));
      c_quest2 = attacker;
    }
    else if (i1 == 3)
    {
      i_quest3 = (damage / 150 * 1000 + Rnd.get(3000));
      c_quest3 = attacker;
    }
    else if (i1 == 4)
    {
      i_quest4 = (damage / 150 * 1000 + Rnd.get(3000));
      c_quest4 = attacker;
    }
    getRandomSkill(npc);
    return super.onAttack(npc, attacker, damage, isPet);
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 2000, 130, -1, 0));
    npc.broadcastPacket(new PlaySound(1, "B03_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
    startQuestTimer("1111", 500L, npc, null);

    GrandBossManager.getInstance().setBossStatus(29028, 3);

    long respawnTime = Config.Interval_Of_Valakas_Spawn + Rnd.get(Config.Random_Of_Valakas_Spawn);

    startQuestTimer("valakas_unlock", respawnTime, null, null);

    StatsSet info = GrandBossManager.getInstance().getStatsSet(29028);
    info.set("respawn_time", System.currentTimeMillis() + respawnTime);
    GrandBossManager.getInstance().setStatsSet(29028, info);

    return super.onKill(npc, killer, isPet);
  }

  public void getRandomSkill(L2NpcInstance npc)
  {
    if ((npc.isInvul()) || (npc.isCastingNow()))
    {
      return;
    }
    L2Skill skill = null;
    int i0 = 0;
    int i1 = 0;
    int i2 = 0;
    L2Character c2 = null;
    if (c_quest2 == null)
      i_quest2 = 0;
    else if ((!Util.checkIfInRange(5000, npc, c_quest2, true)) || (c_quest2.isDead()))
      i_quest2 = 0;
    if (c_quest3 == null)
      i_quest3 = 0;
    else if ((!Util.checkIfInRange(5000, npc, c_quest3, true)) || (c_quest3.isDead()))
      i_quest3 = 0;
    if (c_quest4 == null)
      i_quest4 = 0;
    else if ((!Util.checkIfInRange(5000, npc, c_quest4, true)) || (c_quest4.isDead()))
      i_quest4 = 0;
    if (i_quest2 > i_quest3)
    {
      i1 = 2;
      i2 = i_quest2;
      c2 = c_quest2;
    }
    else
    {
      i1 = 3;
      i2 = i_quest3;
      c2 = c_quest3;
    }
    if (i_quest4 > i2)
    {
      i1 = 4;
      i2 = i_quest4;
      c2 = c_quest4;
    }
    if (i2 == 0)
      c2 = getRandomTarget(npc);
    if (i2 > 0)
    {
      if (Rnd.get(100) < 70)
      {
        if (i1 == 2)
          i_quest2 = 500;
        else if (i1 == 3)
          i_quest3 = 500;
        else if (i1 == 4)
          i_quest4 = 500;
      }
      if (npc.getCurrentHp() > npc.getMaxHp() * 1 / 4)
      {
        i0 = 0;
        i1 = 0;
        if (Util.checkIfInRange(1423, npc, c2, true))
        {
          i0 = 1;
          i1 = 1;
        }
        if (c2.getZ() < npc.getZ() + 200)
        {
          if (Rnd.get(100) < 20)
          {
            skill = SkillTable.getInstance().getInfo(4690, 1);
          }
          else if (Rnd.get(100) < 15)
          {
            skill = SkillTable.getInstance().getInfo(4689, 1);
          }
          else if ((Rnd.get(100) < 15) && (i0 == 1) && (i_quest0 == 1))
          {
            skill = SkillTable.getInstance().getInfo(4685, 1);
            i_quest0 = 0;
          }
          else if ((Rnd.get(100) < 10) && (i1 == 1))
          {
            skill = SkillTable.getInstance().getInfo(4688, 1);
          }
          else if (Rnd.get(100) < 35)
          {
            skill = SkillTable.getInstance().getInfo(4683, 1);
          }
          else if (Rnd.get(2) == 0) {
            skill = SkillTable.getInstance().getInfo(4681, 1);
          } else {
            skill = SkillTable.getInstance().getInfo(4682, 1);
          }
        }
        else if (Rnd.get(100) < 20)
        {
          skill = SkillTable.getInstance().getInfo(4690, 1);
        }
        else if (Rnd.get(100) < 15)
        {
          skill = SkillTable.getInstance().getInfo(4689, 1);
        }
        else
        {
          skill = SkillTable.getInstance().getInfo(4684, 1);
        }
      }
      else if (npc.getCurrentHp() > npc.getMaxHp() * 2 / 4)
      {
        i0 = 0;
        i1 = 0;
        if (Util.checkIfInRange(1423, npc, c2, true))
        {
          i0 = 1;
          i1 = 1;
        }
        if (c2.getZ() < npc.getZ() + 200)
        {
          if (Rnd.get(100) < 5)
          {
            skill = SkillTable.getInstance().getInfo(4690, 1);
          }
          else if (Rnd.get(100) < 10)
          {
            skill = SkillTable.getInstance().getInfo(4689, 1);
          }
          else if ((Rnd.get(100) < 10) && (i0 == 1) && (i_quest0 == 1))
          {
            skill = SkillTable.getInstance().getInfo(4685, 1);
            i_quest0 = 0;
          }
          else if ((Rnd.get(100) < 10) && (i1 == 1))
          {
            skill = SkillTable.getInstance().getInfo(4688, 1);
          }
          else if (Rnd.get(100) < 20)
          {
            skill = SkillTable.getInstance().getInfo(4683, 1);
          }
          else if (Rnd.get(2) == 0) {
            skill = SkillTable.getInstance().getInfo(4681, 1);
          } else {
            skill = SkillTable.getInstance().getInfo(4682, 1);
          }
        }
        else if (Rnd.get(100) < 5)
        {
          skill = SkillTable.getInstance().getInfo(4690, 1);
        }
        else if (Rnd.get(100) < 10)
        {
          skill = SkillTable.getInstance().getInfo(4689, 1);
        }
        else
        {
          skill = SkillTable.getInstance().getInfo(4684, 1);
        }
      }
      else if (npc.getCurrentHp() > npc.getMaxHp() * 3 / 4.0D)
      {
        i0 = 0;
        i1 = 0;
        if (Util.checkIfInRange(1423, npc, c2, true))
        {
          i0 = 1;
          i1 = 1;
        }
        if (c2.getZ() < npc.getZ() + 200)
        {
          if (Rnd.get(100) < 0)
          {
            skill = SkillTable.getInstance().getInfo(4690, 1);
          }
          else if (Rnd.get(100) < 5)
          {
            skill = SkillTable.getInstance().getInfo(4689, 1);
          }
          else if ((Rnd.get(100) < 5) && (i0 == 1) && (i_quest0 == 1))
          {
            skill = SkillTable.getInstance().getInfo(4685, 1);
            i_quest0 = 0;
          }
          else if ((Rnd.get(100) < 10) && (i1 == 1))
          {
            skill = SkillTable.getInstance().getInfo(4688, 1);
          }
          else if (Rnd.get(100) < 15)
          {
            skill = SkillTable.getInstance().getInfo(4683, 1);
          }
          else if (Rnd.get(2) == 0) {
            skill = SkillTable.getInstance().getInfo(4681, 1);
          } else {
            skill = SkillTable.getInstance().getInfo(4682, 1);
          }
        }
        else if (Rnd.get(100) < 0)
        {
          skill = SkillTable.getInstance().getInfo(4690, 1);
        }
        else if (Rnd.get(100) < 5)
        {
          skill = SkillTable.getInstance().getInfo(4689, 1);
        }
        else
        {
          skill = SkillTable.getInstance().getInfo(4684, 1);
        }
      }
      else
      {
        i0 = 0;
        i1 = 0;
        if (Util.checkIfInRange(1423, npc, c2, true))
        {
          i0 = 1;
          i1 = 1;
        }
        if (c2.getZ() < npc.getZ() + 200)
        {
          if (Rnd.get(100) < 0)
          {
            skill = SkillTable.getInstance().getInfo(4690, 1);
          }
          else if (Rnd.get(100) < 10)
          {
            skill = SkillTable.getInstance().getInfo(4689, 1);
          }
          else if ((Rnd.get(100) < 5) && (i0 == 1) && (i_quest0 == 1))
          {
            skill = SkillTable.getInstance().getInfo(4685, 1);
            i_quest0 = 0;
          }
          else if ((Rnd.get(100) < 10) && (i1 == 1))
          {
            skill = SkillTable.getInstance().getInfo(4688, 1);
          }
          else if (Rnd.get(100) < 15)
          {
            skill = SkillTable.getInstance().getInfo(4683, 1);
          }
          else if (Rnd.get(2) == 0) {
            skill = SkillTable.getInstance().getInfo(4681, 1);
          } else {
            skill = SkillTable.getInstance().getInfo(4682, 1);
          }
        }
        else if (Rnd.get(100) < 0)
        {
          skill = SkillTable.getInstance().getInfo(4690, 1);
        }
        else if (Rnd.get(100) < 10)
        {
          skill = SkillTable.getInstance().getInfo(4689, 1);
        }
        else
        {
          skill = SkillTable.getInstance().getInfo(4684, 1);
        }
      }
    }
    if (skill != null)
      callSkillAI(npc, c2, skill);
  }

  public void callSkillAI(L2NpcInstance npc, L2Character c2, L2Skill skill)
  {
    QuestTimer timer = getQuestTimer("1003", npc, null);

    if (npc == null)
    {
      if (timer != null)
        timer.cancel();
      return;
    }

    if (npc.isInvul()) {
      return;
    }
    if ((c2 == null) || (c2.isDead()) || (timer == null))
    {
      c2 = getRandomTarget(npc);
      if (timer == null)
      {
        startQuestTimer("1003", 500L, npc, null, true);
        return;
      }
    }
    L2Character target = c2;
    if ((target == null) || (target.isDead()))
    {
      return;
    }

    if (Util.checkIfInRange(skill.getCastRange(), npc, target, true))
    {
      timer.cancel();
      npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

      npc.setTarget(target);
      npc.doCast(skill);
    }
    else
    {
      npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, null);
    }
  }

  public void broadcastSpawn(L2NpcInstance npc)
  {
    Collection objs = npc.getKnownList().getKnownObjects().values();

    for (L2Object obj : objs)
    {
      if ((obj instanceof L2PcInstance))
      {
        if (Util.checkIfInRange(10000, npc, obj, true))
        {
          ((L2Character)obj).sendPacket(new PlaySound(1, "B03_A", 1, npc.getObjectId(), 212852, -114842, -1632));
          ((L2Character)obj).sendPacket(new SocialAction(npc.getObjectId(), 3));
        }
      }
    }
  }

  public L2Character getRandomTarget(L2NpcInstance npc)
  {
    FastList result = new FastList();
    Collection objs = npc.getKnownList().getKnownObjects().values();

    for (L2Object obj : objs)
    {
      if (((obj instanceof L2PcInstance)) || ((obj instanceof L2Summon)))
      {
        if ((Util.checkIfInRange(5000, npc, obj, true)) && (!((L2Character)obj).isDead()) && ((obj instanceof L2PcInstance)) && (!((L2PcInstance)obj).isGM())) {
          result.add((L2Character)obj);
        }
      }
    }
    if ((!result.isEmpty()) && (result.size() != 0))
    {
      Object[] characters = result.toArray();
      return (L2Character)characters[Rnd.get(characters.length)];
    }
    return null;
  }

  public String onSpellFinished(L2NpcInstance npc, L2PcInstance player, L2Skill skill)
  {
    if (npc.isInvul())
    {
      return null;
    }
    if ((npc.getNpcId() == 29028) && (!npc.isInvul()))
    {
      getRandomSkill(npc);
    }
    return super.onSpellFinished(npc, player, skill);
  }

  public String onAggroRangeEnter(L2NpcInstance npc, L2PcInstance player, boolean isPet)
  {
    int i1 = 0;

    Integer status = Integer.valueOf(GrandBossManager.getInstance().getBossStatus(29028));

    if (status.intValue() == 2)
    {
      if (npc.getCurrentHp() > npc.getMaxHp() * 1 / 4)
      {
        if (player == c_quest2)
        {
          if (11000 > i_quest2)
          {
            i_quest2 = (10000 + Rnd.get(3000));
          }
        }
        else if (player == c_quest3)
        {
          if (11000 > i_quest3)
          {
            i_quest3 = (10000 + Rnd.get(3000));
          }
        }
        else if (player == c_quest4)
        {
          if (11000 > i_quest4)
          {
            i_quest4 = (10000 + Rnd.get(3000));
          }
        }
        else if (i_quest2 > i_quest3)
        {
          i1 = 3;
        }
        else if (i_quest2 == i_quest3)
        {
          if (Rnd.get(100) < 50)
          {
            i1 = 2;
          }
          else
          {
            i1 = 3;
          }
        }
        else if (i_quest2 < i_quest3)
        {
          i1 = 2;
        }
        if (i1 == 2)
        {
          if (i_quest2 > i_quest4)
          {
            i1 = 4;
          }
          else if (i_quest2 == i_quest4)
          {
            if (Rnd.get(100) < 50)
            {
              i1 = 2;
            }
            else
            {
              i1 = 4;
            }
          }
          else if (i_quest2 < i_quest4)
          {
            i1 = 2;
          }
        }
        else if (i1 == 3)
        {
          if (i_quest3 > i_quest4)
          {
            i1 = 4;
          }
          else if (i_quest3 == i_quest4)
          {
            if (Rnd.get(100) < 50)
            {
              i1 = 3;
            }
            else
            {
              i1 = 4;
            }
          }
          else if (i_quest3 < i_quest4)
          {
            i1 = 3;
          }
        }
        if (i1 == 2)
        {
          i_quest2 = (10000 + Rnd.get(3000));
          c_quest2 = player;
        }
        else if (i1 == 3)
        {
          i_quest3 = (10000 + Rnd.get(3000));
          c_quest3 = player;
        }
        else if (i1 == 4)
        {
          i_quest4 = (10000 + Rnd.get(3000));
          c_quest4 = player;
        }
      }
      else if (npc.getCurrentHp() > npc.getMaxHp() * 2 / 4)
      {
        if (player == c_quest2)
        {
          if (7000 > i_quest2)
          {
            i_quest2 = (6000 + Rnd.get(3000));
          }
        }
        else if (player == c_quest3)
        {
          if (7000 > i_quest3)
          {
            i_quest3 = (6000 + Rnd.get(3000));
          }
        }
        else if (player == c_quest4)
        {
          if (7000 > i_quest4)
          {
            i_quest4 = (6000 + Rnd.get(3000));
          }
        }
        else if (i_quest2 > i_quest3)
        {
          i1 = 3;
        }
        else if (i_quest2 == i_quest3)
        {
          if (Rnd.get(100) < 50)
          {
            i1 = 2;
          }
          else
          {
            i1 = 3;
          }
        }
        else if (i_quest2 < i_quest3)
        {
          i1 = 2;
        }
        if (i1 == 2)
        {
          if (i_quest2 > i_quest4)
          {
            i1 = 4;
          }
          else if (i_quest2 == i_quest4)
          {
            if (Rnd.get(100) < 50)
            {
              i1 = 2;
            }
            else
            {
              i1 = 4;
            }
          }
          else if (i_quest2 < i_quest4)
          {
            i1 = 2;
          }
        }
        else if (i1 == 3)
        {
          if (i_quest3 > i_quest4)
          {
            i1 = 4;
          }
          else if (i_quest3 == i_quest4)
          {
            if (Rnd.get(100) < 50)
            {
              i1 = 3;
            }
            else
            {
              i1 = 4;
            }
          }
          else if (i_quest3 < i_quest4)
          {
            i1 = 3;
          }
        }
        if (i1 == 2)
        {
          i_quest2 = (6000 + Rnd.get(3000));
          c_quest2 = player;
        }
        else if (i1 == 3)
        {
          i_quest3 = (6000 + Rnd.get(3000));
          c_quest3 = player;
        }
        else if (i1 == 4)
        {
          i_quest4 = (6000 + Rnd.get(3000));
          c_quest4 = player;
        }
      }
      else if (npc.getCurrentHp() > npc.getMaxHp() * 3 / 4.0D)
      {
        if (player == c_quest2)
        {
          if (4000 > i_quest2)
          {
            i_quest2 = (3000 + Rnd.get(3000));
          }
        }
        else if (player == c_quest3)
        {
          if (4000 > i_quest3)
          {
            i_quest3 = (3000 + Rnd.get(3000));
          }
        }
        else if (player == c_quest4)
        {
          if (4000 > i_quest4)
          {
            i_quest4 = (3000 + Rnd.get(3000));
          }
        }
        else if (i_quest2 > i_quest3)
        {
          i1 = 3;
        }
        else if (i_quest2 == i_quest3)
        {
          if (Rnd.get(100) < 50)
          {
            i1 = 2;
          }
          else
          {
            i1 = 3;
          }
        }
        else if (i_quest2 < i_quest3)
        {
          i1 = 2;
        }
        if (i1 == 2)
        {
          if (i_quest2 > i_quest4)
          {
            i1 = 4;
          }
          else if (i_quest2 == i_quest4)
          {
            if (Rnd.get(100) < 50)
            {
              i1 = 2;
            }
            else
            {
              i1 = 4;
            }
          }
          else if (i_quest2 < i_quest4)
          {
            i1 = 2;
          }
        }
        else if (i1 == 3)
        {
          if (i_quest3 > i_quest4)
          {
            i1 = 4;
          }
          else if (i_quest3 == i_quest4)
          {
            if (Rnd.get(100) < 50)
            {
              i1 = 3;
            }
            else
            {
              i1 = 4;
            }
          }
          else if (i_quest3 < i_quest4)
          {
            i1 = 3;
          }
        }
        if (i1 == 2)
        {
          i_quest2 = (3000 + Rnd.get(3000));
          c_quest2 = player;
        }
        else if (i1 == 3)
        {
          i_quest3 = (3000 + Rnd.get(3000));
          c_quest3 = player;
        }
        else if (i1 == 4)
        {
          i_quest4 = (3000 + Rnd.get(3000));
          c_quest4 = player;
        }
      }
      else if (player == c_quest2)
      {
        if (3000 > i_quest2)
        {
          i_quest2 = (2000 + Rnd.get(3000));
        }
      }
      else if (player == c_quest3)
      {
        if (3000 > i_quest3)
        {
          i_quest3 = (2000 + Rnd.get(3000));
        }
      }
      else if (player == c_quest4)
      {
        if (3000 > i_quest4)
        {
          i_quest4 = (2000 + Rnd.get(3000));
        }
      }
      else if (i_quest2 > i_quest3)
      {
        i1 = 3;
      }
      else if (i_quest2 == i_quest3)
      {
        if (Rnd.get(100) < 50)
        {
          i1 = 2;
        }
        else
        {
          i1 = 3;
        }
      }
      else if (i_quest2 < i_quest3)
      {
        i1 = 2;
      }
      if (i1 == 2)
      {
        if (i_quest2 > i_quest4)
        {
          i1 = 4;
        }
        else if (i_quest2 == i_quest4)
        {
          if (Rnd.get(100) < 50)
          {
            i1 = 2;
          }
          else
          {
            i1 = 4;
          }
        }
        else if (i_quest2 < i_quest4)
        {
          i1 = 2;
        }
      }
      else if (i1 == 3)
      {
        if (i_quest3 > i_quest4)
        {
          i1 = 4;
        }
        else if (i_quest3 == i_quest4)
        {
          if (Rnd.get(100) < 50)
          {
            i1 = 3;
          }
          else
          {
            i1 = 4;
          }
        }
        else if (i_quest3 < i_quest4)
        {
          i1 = 3;
        }
      }
      if (i1 == 2)
      {
        i_quest2 = (2000 + Rnd.get(3000));
        c_quest2 = player;
      }
      else if (i1 == 3)
      {
        i_quest3 = (2000 + Rnd.get(3000));
        c_quest3 = player;
      }
      else if (i1 == 4)
      {
        i_quest4 = (2000 + Rnd.get(3000));
        c_quest4 = player;
      }
    }
    else if (player == c_quest2)
    {
      if (2000 > i_quest2)
      {
        i_quest2 = (1000 + Rnd.get(3000));
      }
    }
    else if (player == c_quest3)
    {
      if (2000 > i_quest3)
      {
        i_quest3 = (1000 + Rnd.get(3000));
      }
    }
    else if (player == c_quest4)
    {
      if (2000 > i_quest4)
      {
        i_quest4 = (1000 + Rnd.get(3000));
      }
    }
    else if (i_quest2 > i_quest3)
    {
      i1 = 3;
    }
    else if (i_quest2 == i_quest3)
    {
      if (Rnd.get(100) < 50)
      {
        i1 = 2;
      }
      else
      {
        i1 = 3;
      }
    }
    else if (i_quest2 < i_quest3)
    {
      i1 = 2;
    }
    if (i1 == 2)
    {
      if (i_quest2 > i_quest4)
      {
        i1 = 4;
      }
      else if (i_quest2 == i_quest4)
      {
        if (Rnd.get(100) < 50)
        {
          i1 = 2;
        }
        else
        {
          i1 = 4;
        }
      }
      else if (i_quest2 < i_quest4)
      {
        i1 = 2;
      }
    }
    else if (i1 == 3)
    {
      if (i_quest3 > i_quest4)
      {
        i1 = 4;
      }
      else if (i_quest3 == i_quest4)
      {
        if (Rnd.get(100) < 50)
        {
          i1 = 3;
        }
        else
        {
          i1 = 4;
        }
      }
      else if (i_quest3 < i_quest4)
      {
        i1 = 3;
      }
    }
    if (i1 == 2)
    {
      i_quest2 = (1000 + Rnd.get(3000));
      c_quest2 = player;
    }
    else if (i1 == 3)
    {
      i_quest3 = (1000 + Rnd.get(3000));
      c_quest3 = player;
    }
    else if (i1 == 4)
    {
      i_quest4 = (1000 + Rnd.get(3000));
      c_quest4 = player;
    }
    if ((status.intValue() == 2) && (!npc.isInvul()))
    {
      getRandomSkill(npc);
    }
    else
      return null;
    return super.onAggroRangeEnter(npc, player, isPet);
  }

  public String onSkillUse(L2NpcInstance npc, L2PcInstance caster, L2Skill skill)
  {
    if (npc.isInvul())
    {
      return null;
    }
    npc.setTarget(caster);
    return super.onSkillUse(npc, caster, skill);
  }

  public void run()
  {
  }
}