package net.sf.l2j.gameserver.ai.special;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.Rnd;

public class QueenAnt extends L2AttackableAIScript
{
  private static Logger _log = Logger.getLogger(QueenAnt.class.getName());
  Calendar time = Calendar.getInstance();
  private static final int QUEEN = 29001;
  private static final int LARVA = 29002;
  private static final int NURSE = 29003;
  private static final int GUARD = 29004;
  private static final int ROYAL = 29005;
  private static final byte ALIVE = 0;
  private static final byte DEAD = 1;
  private static L2BossZone _Zone;
  private static List<L2Attackable> _Minions = new FastList();

  public QueenAnt(int questId, String name, String descr)
  {
    super(questId, name, descr);
    int[] mobs = { 29001, 29002, 29003, 29004, 29005 };
    registerMobs(mobs);
    _Zone = GrandBossManager.getInstance().getZone(-21610, 181594, -5734);

    StatsSet info = GrandBossManager.getInstance().getStatsSet(29001);
    int status = GrandBossManager.getInstance().getBossStatus(29001);
    if (status == 1)
    {
      long temp = info.getLong("respawn_time") - System.currentTimeMillis();

      if (temp > 0L) {
        startQuestTimer("queen_unlock", temp, null, null);
      }
      else
      {
        L2GrandBossInstance queen = (L2GrandBossInstance)addSpawn(29001, -21610, 181594, -5734, 0, false, 0);
        GrandBossManager.getInstance().setBossStatus(29001, 0);
        spawnBoss(queen);
      }
    }
    else
    {
      int loc_x = info.getInteger("loc_x");
      int loc_y = info.getInteger("loc_y");
      int loc_z = info.getInteger("loc_z");
      int heading = info.getInteger("heading");
      int hp = info.getInteger("currentHP");
      int mp = info.getInteger("currentMP");
      L2GrandBossInstance queen = (L2GrandBossInstance)addSpawn(29001, loc_x, loc_y, loc_z, heading, false, 0);
      queen.setCurrentHpMp(hp, mp);
      spawnBoss(queen);
    }
  }

  public void spawnBoss(L2GrandBossInstance npc)
  {
    GrandBossManager.getInstance().addBoss(npc);
    if (Rnd.get(100) < 33)
      _Zone.movePlayersTo(-19480, 187344, -5600);
    else if (Rnd.get(100) < 50)
      _Zone.movePlayersTo(-17928, 180912, -5520);
    else
      _Zone.movePlayersTo(-23808, 182368, -5600);
    GrandBossManager.getInstance().addBoss(npc);
    startQuestTimer("action", 10000L, npc, null, true);
    npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));

    addSpawn(29002, -21600, 179482, -5846, Rnd.get(360), false, 0);

    addSpawn(29003, -22000, 179482, -5846, 0, false, 0);
    addSpawn(29003, -21200, 179482, -5846, 0, false, 0);
    int radius = 400;
    for (int i = 0; i < 6; i++)
    {
      int x = (int)(radius * Math.cos(i * 1.407D));
      int y = (int)(radius * Math.sin(i * 1.407D));
      addSpawn(29003, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0);
    }
    for (int i = 0; i < 8; i++)
    {
      int x = (int)(radius * Math.cos(i * 0.7854D));
      int y = (int)(radius * Math.sin(i * 0.7854D));
      L2NpcInstance mob = addSpawn(29005, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0);
      _Minions.add((L2Attackable)mob);
    }
    startQuestTimer("check_royal__Zone", 120000L, npc, null, true);
  }

  public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
  {
    if ((event.equalsIgnoreCase("action")) && (npc != null))
    {
      if (Rnd.get(3) == 0)
      {
        if (Rnd.get(2) == 0)
        {
          npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
        }
        else
        {
          npc.broadcastPacket(new SocialAction(npc.getObjectId(), 4));
        }
      }
    }
    else if (event.equalsIgnoreCase("queen_unlock"))
    {
      L2GrandBossInstance queen = (L2GrandBossInstance)addSpawn(29001, -21610, 181594, -5734, 0, false, 0);
      GrandBossManager.getInstance().setBossStatus(29001, 0);
      spawnBoss(queen);
    }
    else if ((event.equalsIgnoreCase("check_royal__Zone")) && (npc != null))
    {
      for (int i = 0; i < _Minions.size(); i++)
      {
        L2Attackable mob = (L2Attackable)_Minions.get(i);
        if ((mob == null) || (_Zone.isInsideZone(mob)))
          continue;
        mob.teleToLocation(-21557, 181581, -5723);
      }

    }
    else if (event.equalsIgnoreCase("despawn_royals"))
    {
      for (int i = 0; i < _Minions.size(); i++)
      {
        L2Attackable mob = (L2Attackable)_Minions.get(i);
        if (mob == null)
          continue;
        mob.decayMe();
      }

      _Minions.clear();
    }
    else if (event.equalsIgnoreCase("spawn_royal"))
    {
      L2NpcInstance mob = addSpawn(29005, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0);
      _Minions.add((L2Attackable)mob);
    }
    else if (event.equalsIgnoreCase("spawn_nurse"))
    {
      addSpawn(29003, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0);
    }
    return super.onAdvEvent(event, npc, player);
  }

  public String onFactionCall(L2NpcInstance npc, L2NpcInstance caller, L2PcInstance attacker, boolean isPet)
  {
    if ((caller == null) || (npc == null))
      return super.onFactionCall(npc, caller, attacker, isPet);
    int npcId = npc.getNpcId();
    int callerId = caller.getNpcId();
    if (npcId == 29003)
    {
      if (callerId == 29002)
      {
        npc.setTarget(caller);
        npc.doCast(SkillTable.getInstance().getInfo(4020, 1));
        npc.doCast(SkillTable.getInstance().getInfo(4024, 1));
        return null;
      }
      if (callerId == 29001)
      {
        if ((npc.getTarget() != null) && ((npc.getTarget() instanceof L2NpcInstance)))
        {
          if (((L2NpcInstance)npc.getTarget()).getNpcId() == 29002)
          {
            return null;
          }
        }
        npc.setTarget(caller);
        npc.doCast(SkillTable.getInstance().getInfo(4020, 1));
        return null;
      }
    }
    return super.onFactionCall(npc, caller, attacker, isPet);
  }

  public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    int npcId = npc.getNpcId();
    if (npcId == 29003)
    {
      npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      return null;
    }

    return super.onAttack(npc, attacker, damage, isPet);
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    int npcId = npc.getNpcId();
    if (npcId == 29001)
    {
      npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
      GrandBossManager.getInstance().setBossStatus(29001, 1);

      long respawnTime = (Config.AQ_RESP_TIME + Rnd.get(Config.AQ_RND_RESP_TIME)) * 3600000;
      startQuestTimer("queen_unlock", respawnTime, null, null);
      cancelQuestTimer("action", npc, null);

      StatsSet info = GrandBossManager.getInstance().getStatsSet(29001);
      info.set("respawn_time", System.currentTimeMillis() + respawnTime);
      GrandBossManager.getInstance().setStatsSet(29001, info);
      startQuestTimer("despawn_royals", 20000L, null, null);
      cancelQuestTimers("spawn_minion");
      _log.warning(" - Epic: Queen Ant killed: " + time.getTime());
    }
    else if (GrandBossManager.getInstance().getBossStatus(29001) == 0)
    {
      if (npcId == 29005)
      {
        _Minions.remove(npc);
        startQuestTimer("spawn_royal", (Config.AQ_ROYAL_RESP_TIME + Rnd.get(40)) * 1000, npc, null);
      }
      else if (npcId == 29003)
      {
        startQuestTimer("spawn_nurse", Config.AQ_NURSE_RESP_TIME * 1000, npc, null);
      }
    }

    return super.onKill(npc, killer, isPet);
  }
}