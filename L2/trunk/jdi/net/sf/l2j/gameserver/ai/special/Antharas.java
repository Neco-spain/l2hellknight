package net.sf.l2j.gameserver.ai.special;

import java.util.Calendar;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.Rnd;

public class Antharas extends Quest
{
  private static Logger _log = Logger.getLogger(Antharas.class.getName());
  Calendar time = Calendar.getInstance();
  private static final int ANTHARAS = 29019;
  private static final int HEART = 13001;
  private static final int STONE = 3865;
  private static final byte DORMANT = 0;
  private static final byte WAITING = 1;
  private static final byte FIGHTING = 2;
  private static final byte DEAD = 3;
  private static long _LastAction = 0L;
  private static L2BossZone _Zone;
  L2GrandBossInstance antharas = null;

  public Antharas(int id, String name, String descr)
  {
    super(id, name, descr);

    addEventId(29019, Quest.QuestEventType.ON_KILL);
    addEventId(29019, Quest.QuestEventType.ON_ATTACK);

    addEventId(13001, Quest.QuestEventType.QUEST_START);
    addEventId(13001, Quest.QuestEventType.ON_TALK);

    addStartNpc(13001);
    addTalkId(13001);

    _Zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);
    StatsSet info = GrandBossManager.getInstance().getStatsSet(29019);
    int status = GrandBossManager.getInstance().getBossStatus(29019);
    if (status == 3)
    {
      long temp = info.getLong("respawn_time") - System.currentTimeMillis();
      if (temp > 0L)
      {
        startQuestTimer("antharas_unlock", temp, null, null);
      }
      else
      {
        antharas = ((L2GrandBossInstance)addSpawn(29019, 185708, 114298, -8221, 32768, false, 0));
        GrandBossManager.getInstance().setBossStatus(29019, 0);
        antharas.broadcastPacket(new Earthquake(185708, 114298, -8221, 20, 10));
        GrandBossManager.getInstance().addBoss(antharas);
      }
    }
    else if (status == 2)
    {
      int loc_x = info.getInteger("loc_x");
      int loc_y = info.getInteger("loc_y");
      int loc_z = info.getInteger("loc_z");
      int heading = info.getInteger("heading");
      int hp = info.getInteger("currentHP");
      int mp = info.getInteger("currentMP");
      antharas = ((L2GrandBossInstance)addSpawn(29019, loc_x, loc_y, loc_z, heading, false, 0));
      GrandBossManager.getInstance().addBoss(antharas);
      antharas.setCurrentHpMp(hp, mp);
      _LastAction = System.currentTimeMillis();
      L2NpcInstance _antharas = antharas;

      startQuestTimer("antharas_despawn", 60000L, _antharas, null);
    }
    else
    {
      antharas = ((L2GrandBossInstance)addSpawn(29019, 185708, 114298, -8221, 32768, false, 0));
      antharas.broadcastPacket(new Earthquake(185708, 114298, -8221, 20, 10));
      GrandBossManager.getInstance().addBoss(antharas);
      L2NpcInstance _antharas = antharas;
      if (status == 1)
      {
        startQuestTimer("waiting", Config.ANTHARAS_WAIT_TIME, _antharas, null);
      }
    }
  }

  public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
  {
    if (npc != null)
    {
      long temp = 0L;
      if (event.equalsIgnoreCase("waiting"))
      {
        npc.teleToLocation(185452, 114835, -8221);
        npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(181911, 114835, -7678, 0));
        startQuestTimer("antharas_has_arrived", 2000L, npc, null);
        npc.broadcastPacket(new PlaySound(1, "BS02_A", 1, npc.getObjectId(), 185452, 114835, -8221));
        GrandBossManager.getInstance().setBossStatus(29019, 2);
      }
      else if (event.equalsIgnoreCase("camera_1"))
      {
        startQuestTimer("camera_2", 3000L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 700, 13, -19, 0, 20000));
      }
      else if (event.equalsIgnoreCase("camera_2"))
      {
        startQuestTimer("camera_3", 10000L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 700, 13, 0, 6000, 20000));
      }
      else if (event.equalsIgnoreCase("camera_3"))
      {
        startQuestTimer("camera_4", 200L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 3700, 0, -3, 0, 10000));
      }
      else if (event.equalsIgnoreCase("camera_4"))
      {
        startQuestTimer("camera_5", 10800L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 0, -3, 22000, 30000));
      }
      else if (event.equalsIgnoreCase("camera_5"))
      {
        startQuestTimer("antharas_despawn", 60000L, npc, null);
        npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 0, -3, 300, 7000));
        _LastAction = System.currentTimeMillis();
      }
      else if (event.equalsIgnoreCase("antharas_despawn"))
      {
        startQuestTimer("antharas_despawn", 60000L, npc, null);
        temp = System.currentTimeMillis() - _LastAction;
        if (temp > 900000L)
        {
          npc.teleToLocation(185708, 114298, -8221);
          npc.getSpawn().setLocx(185708);
          npc.getSpawn().setLocy(114298);
          npc.getSpawn().setLocz(-8221);
          GrandBossManager.getInstance().setBossStatus(29019, 0);
          npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
          _Zone.oustAllPlayers();
          cancelQuestTimer("antharas_despawn", npc, null);
        }
      }
      else if (event.equalsIgnoreCase("antharas_has_arrived"))
      {
        int dx = Math.abs(npc.getX() - 181911);
        int dy = Math.abs(npc.getY() - 114835);
        if ((dx <= 50) && (dy <= 50))
        {
          startQuestTimer("camera_1", 2000L, npc, null);
          npc.getSpawn().setLocx(180908);
          npc.getSpawn().setLocy(114838);
          npc.getSpawn().setLocz(-7677);
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
          cancelQuestTimer("antharas_has_arrived", npc, null);
        }
        else
        {
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(181911, 114835, -7678, 0));
          startQuestTimer("antharas_has_arrived", 2000L, npc, null);
        }
      }
      else if (event.equalsIgnoreCase("spawn_cubes"))
      {
        addSpawn(31859, 177615, 114941, -7709, 0, false, 900000);
        int radius = 1500;
        for (int i = 0; i < 20; i++)
        {
          int x = (int)(radius * Math.cos(i * 0.331D));
          int y = (int)(radius * Math.sin(i * 0.331D));
          addSpawn(31859, 177615 + x, 114941 + y, -7709, 0, false, 900000);
        }
        cancelQuestTimer("antharas_despawn", npc, null);
        startQuestTimer("remove_players", 900000L, null, null);
      }

    }
    else if (event.equalsIgnoreCase("antharas_unlock"))
    {
      antharas = ((L2GrandBossInstance)addSpawn(29019, 185708, 114298, -8221, 32768, false, 0));
      GrandBossManager.getInstance().addBoss(antharas);
      GrandBossManager.getInstance().setBossStatus(29019, 0);
      antharas.broadcastPacket(new Earthquake(185708, 114298, -8221, 20, 10));
    }
    else if (event.equalsIgnoreCase("remove_players"))
    {
      _Zone.oustAllPlayers();
    }

    return super.onAdvEvent(event, npc, player);
  }

  public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    _LastAction = System.currentTimeMillis();
    if (GrandBossManager.getInstance().getBossStatus(29019) != 2)
    {
      _Zone.oustAllPlayers();
    }
    return super.onAttack(npc, attacker, damage, isPet);
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
    startQuestTimer("spawn_cubes", 10000L, npc, null);
    GrandBossManager.getInstance().setBossStatus(29019, 3);
    long respawnTime = Config.ANTHARAS_INTERVAL_SPAWN + Rnd.get(Config.ANTHARAS_RANDOM_SPAWN);
    startQuestTimer("antharas_unlock", respawnTime, null, null);

    StatsSet info = GrandBossManager.getInstance().getStatsSet(29019);
    info.set("respawn_time", System.currentTimeMillis() + respawnTime);
    GrandBossManager.getInstance().setStatsSet(29019, info);
    _log.warning(" - Epic: Antharas killed: " + time.getTime());
    return super.onKill(npc, killer, isPet);
  }

  public String onTalk(L2NpcInstance npc, L2PcInstance player)
  {
    if (npc.getNpcId() == 13001)
    {
      int status = GrandBossManager.getInstance().getBossStatus(29019);

      if ((status != 2) && (status != 3))
      {
        if ((player.getInventory().getItemByItemId(3865) != null) && (player.getInventory().getItemByItemId(3865).getCount() >= 1))
        {
          if (status == 0)
          {
            player.getInventory().destroyItemByItemId("antharas", 3865, 1, player, player.getTarget());
            GrandBossManager.getInstance().getZone(177615, 114941, -7709).allowPlayerEntry(player, 30);
            player.teleToLocation(177615, 114941, -7709);
            GrandBossManager.getInstance().setBossStatus(29019, 1);
            L2NpcInstance _antharas = antharas;
            startQuestTimer("waiting", Config.ANTHARAS_WAIT_TIME, _antharas, null);
          }
          else if (status == 1)
          {
            player.getInventory().destroyItemByItemId("antharas", 3865, 1, player, player.getTarget());
            GrandBossManager.getInstance().getZone(177615, 114941, -7709).allowPlayerEntry(player, 30);
            player.teleToLocation(177615, 114941, -7709);
          }
        }
        else
          return "<html><body><tr><td>You hear something...</td></tr><br>You need <font color=LEVEL>Portal stone</font> to enter...</body></html>";
      }
      else
        return "<html><body><tr><td>You hear something...</td></tr><br><font color=LEVEL>Antharas was killed...</font><br>Try another time.</body></html>";
    }
    return super.onTalk(npc, player);
  }
}