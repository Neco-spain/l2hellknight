package net.sf.l2j.gameserver.ai.special;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.Rnd;

public class Orfen extends L2AttackableAIScript
{
  private static Logger _log = Logger.getLogger(Orfen.class.getName());
  Calendar time = Calendar.getInstance();

  private static final int[][] Pos = { { 43728, 17220, -4342 }, { 55024, 17368, -5412 }, { 53504, 21248, -5486 }, { 53248, 24576, -5262 } };

  private static final String[] Text = { "PLAYERNAME, stop kidding yourthis about your own powerlessness!", "PLAYERNAME, I\u2019ll make you feel what true fear is!", "You\u2019re really stupid to have challenged me. PLAYERNAME! Get ready!", "PLAYERNAME, do you think that\u2019s going to work?!" };
  private static final int ORFEN = 29014;
  private static final int RAIKEL_LEOS = 29016;
  private static final int RIBA_IREN = 29018;
  private static boolean _IsTeleported;
  private static List<L2Attackable> _Minions = new FastList();
  private static L2BossZone _Zone;
  private static final byte ALIVE = 0;
  private static final byte DEAD = 1;

  public Orfen(int id, String name, String descr)
  {
    super(id, name, descr);
    int[] mobs = { 29014, 29016, 29018 };
    registerMobs(mobs);
    _IsTeleported = false;
    _Zone = GrandBossManager.getInstance().getZone(Pos[0][0], Pos[0][1], Pos[0][2]);
    StatsSet info = GrandBossManager.getInstance().getStatsSet(29014);
    int status = GrandBossManager.getInstance().getBossStatus(29014);
    if (status == 1)
    {
      long temp = info.getLong("respawn_time") - System.currentTimeMillis();

      if (temp > 0L) {
        startQuestTimer("orfen_unlock", temp, null, null);
      }
      else
      {
        int i = Rnd.get(10);
        int x = 0;
        int y = 0;
        int z = 0;
        if (i < 4)
        {
          x = Pos[1][0];
          y = Pos[1][1];
          z = Pos[1][2];
        }
        else if (i < 7)
        {
          x = Pos[2][0];
          y = Pos[2][1];
          z = Pos[2][2];
        }
        else
        {
          x = Pos[3][0];
          y = Pos[3][1];
          z = Pos[3][2];
        }
        L2GrandBossInstance orfen = (L2GrandBossInstance)addSpawn(29014, x, y, z, 0, false, 0);
        GrandBossManager.getInstance().setBossStatus(29014, 0);
        spawnBoss(orfen);
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
      L2GrandBossInstance orfen = (L2GrandBossInstance)addSpawn(29014, loc_x, loc_y, loc_z, heading, false, 0);
      orfen.setCurrentHpMp(hp, mp);
      spawnBoss(orfen);
    }
  }

  public void setSpawnPoint(L2NpcInstance npc, int index)
  {
    ((L2Attackable)npc).clearAggroList();
    npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
    L2Spawn spawn = npc.getSpawn();
    spawn.setLocx(Pos[index][0]);
    spawn.setLocy(Pos[index][1]);
    spawn.setLocz(Pos[index][2]);
    npc.teleToLocation(Pos[index][0], Pos[index][1], Pos[index][2]);
  }

  public void spawnBoss(L2GrandBossInstance npc)
  {
    GrandBossManager.getInstance().addBoss(npc);
    npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
    startQuestTimer("check_orfen_pos", 10000L, npc, null, true);

    int x = npc.getX();
    int y = npc.getY();

    L2NpcInstance mob = addSpawn(29016, x + 100, y + 100, npc.getZ(), 0, false, 0);
    _Minions.add((L2Attackable)mob);
    mob = addSpawn(29016, x + 100, y - 100, npc.getZ(), 0, false, 0);
    _Minions.add((L2Attackable)mob);
    mob = addSpawn(29016, x - 100, y + 100, npc.getZ(), 0, false, 0);
    _Minions.add((L2Attackable)mob);
    mob = addSpawn(29016, x - 100, y - 100, npc.getZ(), 0, false, 0);
    _Minions.add((L2Attackable)mob);
    startQuestTimer("check_minion_loc", 10000L, npc, null, true);
  }

  public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
  {
    if (event.equalsIgnoreCase("orfen_unlock"))
    {
      int i = Rnd.get(10);
      int x = 0;
      int y = 0;
      int z = 0;
      if (i < 4)
      {
        x = Pos[1][0];
        y = Pos[1][1];
        z = Pos[1][2];
      }
      else if (i < 7)
      {
        x = Pos[2][0];
        y = Pos[2][1];
        z = Pos[2][2];
      }
      else
      {
        x = Pos[3][0];
        y = Pos[3][1];
        z = Pos[3][2];
      }
      L2GrandBossInstance orfen = (L2GrandBossInstance)addSpawn(29014, x, y, z, 0, false, 0);
      GrandBossManager.getInstance().setBossStatus(29014, 0);
      spawnBoss(orfen);
    }
    else if (event.equalsIgnoreCase("check_orfen_pos"))
    {
      if (((_IsTeleported) && (npc.getCurrentHp() > npc.getMaxHp() * 0.95D)) || ((!_Zone.isInsideZone(npc)) && (!_IsTeleported)))
      {
        setSpawnPoint(npc, Rnd.get(3) + 1);
        _IsTeleported = false;
      }
      else if ((_IsTeleported) && (!_Zone.isInsideZone(npc))) {
        setSpawnPoint(npc, 0);
      }
    } else if (event.equalsIgnoreCase("check_minion_loc"))
    {
      for (int i = 0; i < _Minions.size(); i++)
      {
        L2Attackable mob = (L2Attackable)_Minions.get(i);
        if (npc.isInsideRadius(mob, 3000, false, false))
          continue;
        mob.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
        ((L2Attackable)npc).clearAggroList();
        npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      }

    }
    else if (event.equalsIgnoreCase("despawn_minions"))
    {
      for (int i = 0; i < _Minions.size(); i++)
      {
        L2Attackable mob = (L2Attackable)_Minions.get(i);
        if (mob != null)
          mob.decayMe();
      }
      _Minions.clear();
    }
    else if (event.equalsIgnoreCase("spawn_minion"))
    {
      L2NpcInstance mob = addSpawn(29016, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0);
      _Minions.add((L2Attackable)mob);
    }
    return super.onAdvEvent(event, npc, player);
  }

  public String onSkillSee(L2NpcInstance npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
  {
    if (npc.getNpcId() == 29014)
    {
      L2Character originalCaster = isPet ? caster.getPet() : caster;
      if ((skill.getAggroPoints() > 0) && (Rnd.get(5) == 0) && (npc.isInsideRadius(originalCaster, 1000, false, false)))
      {
        npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), Text[Rnd.get(4)].replace("PLAYERNAME", caster.getName().toString())));
        originalCaster.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
        npc.setTarget(originalCaster);
        npc.doCast(SkillTable.getInstance().getInfo(4064, 1));
      }
    }
    return super.onSkillSee(npc, caster, skill, targets, isPet);
  }

  public String onFactionCall(L2NpcInstance npc, L2NpcInstance caller, L2PcInstance attacker, boolean isPet)
  {
    if ((caller == null) || (npc == null))
      return super.onFactionCall(npc, caller, attacker, isPet);
    int npcId = npc.getNpcId();
    int callerId = caller.getNpcId();
    if ((npcId == 29016) && (Rnd.get(20) == 0))
    {
      npc.setTarget(attacker);
      npc.doCast(SkillTable.getInstance().getInfo(4067, 4));
    }
    else if (npcId == 29018)
    {
      int chance = 1;
      if (callerId == 29014)
        chance = 9;
      if ((callerId != 29018) && (caller.getCurrentHp() < caller.getMaxHp() / 2) && (Rnd.get(10) < chance))
      {
        npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
        npc.setTarget(caller);
        npc.doCast(SkillTable.getInstance().getInfo(4516, 1));
      }
    }
    return super.onFactionCall(npc, caller, attacker, isPet);
  }

  public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    int npcId = npc.getNpcId();
    if (npcId == 29014)
    {
      if ((npc.getCurrentHp() - damage < npc.getMaxHp() / 2) && (!_IsTeleported))
      {
        setSpawnPoint(npc, 0);
        _IsTeleported = true;
      }
      else if ((npc.isInsideRadius(attacker, 1000, false, false)) && (!npc.isInsideRadius(attacker, 300, false, false)) && (Rnd.get(10) == 0))
      {
        npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npcId, Text[Rnd.get(3)].replace("PLAYERNAME", attacker.getName().toString())));
        attacker.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
        npc.setTarget(attacker);
        npc.doCast(SkillTable.getInstance().getInfo(4064, 1));
      }
    }
    else if (npcId == 29018)
    {
      if (npc.getCurrentHp() - damage < npc.getMaxHp() / 2)
      {
        npc.setTarget(attacker);
        npc.doCast(SkillTable.getInstance().getInfo(4516, 1));
      }
    }
    return super.onAttack(npc, attacker, damage, isPet);
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    if (npc.getNpcId() == 29014)
    {
      npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
      GrandBossManager.getInstance().setBossStatus(29014, 1);
      _log.warning(" - Epic: Orfen killed: " + time.getTime());

      long respawnTime = 28 + Rnd.get(41) * 3600000;
      startQuestTimer("orfen_unlock", respawnTime, null, null);

      StatsSet info = GrandBossManager.getInstance().getStatsSet(29014);
      info.set("respawn_time", System.currentTimeMillis() + respawnTime);
      GrandBossManager.getInstance().setStatsSet(29014, info);
      cancelQuestTimer("check_minion_loc", npc, null);
      cancelQuestTimer("check_orfen_pos", npc, null);
      startQuestTimer("despawn_minions", 20000L, null, null);
      cancelQuestTimers("spawn_minion");
    }
    else if ((GrandBossManager.getInstance().getBossStatus(29014) == 0) && (npc.getNpcId() == 29016))
    {
      _Minions.remove(npc);
      startQuestTimer("spawn_minion", 360000L, npc, null);
    }
    return super.onKill(npc, killer, isPet);
  }
}