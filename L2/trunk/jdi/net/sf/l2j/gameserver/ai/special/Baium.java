package net.sf.l2j.gameserver.ai.special;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.NpcKnownList;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.quest.QuestTimer;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public class Baium extends L2AttackableAIScript
{
  private static Logger _log = Logger.getLogger(Baium.class.getName());
  Calendar time = Calendar.getInstance();
  private L2Character _target;
  private L2Skill _skill;
  private static final int STONE_BAIUM = 29025;
  private static final int ANGELIC_VORTEX = 31862;
  private static final int LIVE_BAIUM = 29020;
  private static final int ARCHANGEL = 29021;
  private static FastList<L2Attackable> _Minions = new FastList();
  private static final int[][] ANGEL_LOCATION = { { 113004, 16209, 10076, 60242 }, { 114053, 16642, 10076, 4411 }, { 114563, 17184, 10076, 49241 }, { 116356, 16402, 10076, 31109 }, { 115015, 16393, 10076, 32760 }, { 115481, 15335, 10076, 16241 }, { 114680, 15407, 10051, 32485 }, { 114886, 14437, 10076, 16868 }, { 115391, 17593, 10076, 55346 }, { 115245, 17558, 10076, 35536 } };
  private static final byte ASLEEP = 0;
  private static final byte AWAKE = 1;
  private static final byte DEAD = 2;
  private static long _LastAttackVsBaiumTime = 0L;
  private static L2BossZone _Zone;

  public Baium(int questId, String name, String descr)
  {
    super(questId, name, descr);

    int[] mob = { 29020 };
    registerMobs(mob);

    addStartNpc(29025);
    addStartNpc(31862);
    addTalkId(29025);
    addTalkId(31862);
    _Zone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
    StatsSet info = GrandBossManager.getInstance().getStatsSet(29020);
    int status = GrandBossManager.getInstance().getBossStatus(29020);
    if (status == 2)
    {
      long temp = info.getLong("respawn_time") - System.currentTimeMillis();
      if (temp > 0L)
      {
        startQuestTimer("baium_unlock", temp, null, null);
      }
      else
      {
        addSpawn(29025, 116033, 17447, 10104, 40188, false, 0);
        GrandBossManager.getInstance().setBossStatus(29020, 0);
      }
    }
    else if (status == 1)
    {
      int loc_x = info.getInteger("loc_x");
      int loc_y = info.getInteger("loc_y");
      int loc_z = info.getInteger("loc_z");
      int heading = info.getInteger("heading");
      int hp = info.getInteger("currentHP");
      int mp = info.getInteger("currentMP");
      L2GrandBossInstance baium = (L2GrandBossInstance)addSpawn(29020, loc_x, loc_y, loc_z, heading, false, 0);
      GrandBossManager.getInstance().addBoss(baium);
      L2NpcInstance _baium = baium;
      ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(_baium, hp, mp)
      {
        public void run()
        {
          try {
            val$_baium.setCurrentHpMp(val$hp, val$mp);
            val$_baium.setIsInvul(true);
            val$_baium.setIsImobilised(true);
            val$_baium.setRunning();
            val$_baium.broadcastPacket(new SocialAction(val$_baium.getObjectId(), 2));
            startQuestTimer("baium_wakeup", 15000L, val$_baium, null);
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
      }
      , 100L);
    }
    else
    {
      addSpawn(29025, 116033, 17447, 10104, 40188, false, 0);
    }
  }

  public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
  {
    boolean updateTarget;
    if (event.equalsIgnoreCase("baium_unlock"))
    {
      GrandBossManager.getInstance().setBossStatus(29020, 0);
      addSpawn(29025, 116033, 17447, 10104, 40188, false, 0);
    }
    else if ((event.equalsIgnoreCase("skill_range")) && (npc != null))
    {
      callSkillAI(npc);
    }
    else if (event.equalsIgnoreCase("clean_player"))
    {
      _target = getRandomTarget(npc);
    }
    else if ((event.equalsIgnoreCase("baium_wakeup")) && (npc != null))
    {
      if (npc.getNpcId() == 29020)
      {
        npc.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
        npc.broadcastPacket(new Earthquake(npc.getX(), npc.getY(), npc.getZ(), 40, 5));

        _LastAttackVsBaiumTime = System.currentTimeMillis();
        startQuestTimer("baium_despawn", 60000L, npc, null, true);
        startQuestTimer("skill_range", 10000L, npc, null, true);

        if ((player != null) && (_Zone.isInsideZone(player)) && (Rnd.get(100) < 50)) {
          player.reduceCurrentHp(player.getCurrentHp(), player);
        }
        L2NpcInstance baium = npc;
        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(baium)
        {
          public void run()
          {
            try {
              val$baium.setIsInvul(false);
              val$baium.setIsImobilised(false);
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }
          }
        }
        , 11100L);

        for (int i = 0; i < 5; i++) {
          _Minions.add((L2Attackable)addSpawn(29021, ANGEL_LOCATION[i][0], ANGEL_LOCATION[i][1], ANGEL_LOCATION[i][2], ANGEL_LOCATION[i][3], false, 0));
        }
        startQuestTimer("ai_angels", 1000L, npc, null, true);
      }

    }
    else if ((event.equalsIgnoreCase("baium_despawn")) && (npc != null))
    {
      if (npc.getNpcId() == 29020)
      {
        if (_Zone == null)
          _Zone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
        if (_LastAttackVsBaiumTime + 1800000L < System.currentTimeMillis())
        {
          npc.deleteMe();
          addSpawn(29025, 116033, 17447, 10104, 40188, false, 0);
          GrandBossManager.getInstance().setBossStatus(29020, 0);
          _Zone.oustAllPlayers();
          cancelQuestTimer("baium_despawn", npc, null);
          for (L2Attackable minion : _Minions)
            if (minion != null)
              minion.deleteMe();
          _Minions.clear();
        }
        else if (!_Zone.isInsideZone(npc)) {
          npc.teleToLocation(116033, 17447, 10104);
        }
      }
    } else if (event.equalsIgnoreCase("ai_angels"))
    {
      updateTarget = false;

      for (L2NpcInstance minion : _Minions)
      {
        if (!_Zone.isInsideZone(minion))
        {
          int rnd = Rnd.get(5);
          minion.teleToLocation(ANGEL_LOCATION[rnd][0], ANGEL_LOCATION[rnd][1], ANGEL_LOCATION[rnd][2]);
        }

        L2Attackable angel = (L2Attackable)minion;
        if (angel == null) {
          continue;
        }
        L2Character victim = angel.getMostHated();

        if (Rnd.get(100) == 0) {
          updateTarget = true;
        }
        else if (victim != null)
        {
          if (((victim instanceof L2PcInstance)) && (victim.getActiveWeaponInstance() == null))
          {
            angel.stopHating(victim);
            updateTarget = true;
          }
        }
        else {
          updateTarget = true;
        }

        if (updateTarget)
        {
          L2Character newVictim = getRandomTarget(minion);
          if ((newVictim != null) && (victim != newVictim))
          {
            angel.addDamageHate(newVictim, 0, 10000);

            if (angel.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
              angel.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
          }
        }
      }
    }
    return super.onAdvEvent(event, npc, player);
  }

  public String onTalk(L2NpcInstance npc, L2PcInstance player)
  {
    int npcId = npc.getNpcId();
    String htmltext = "";
    if (_Zone == null)
      _Zone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
    if (_Zone == null)
      return "<html><body>Angelic Vortex:<br>You may not enter while admin disabled this zone</body></html>";
    if ((npcId == 29025) && (GrandBossManager.getInstance().getBossStatus(29020) == 0))
    {
      if (_Zone.isPlayerAllowed(player))
      {
        GrandBossManager.getInstance().setBossStatus(29020, 1);
        npc.deleteMe();
        L2GrandBossInstance baium = (L2GrandBossInstance)addSpawn(29020, npc);
        GrandBossManager.getInstance().addBoss(baium);
        L2NpcInstance _baium = baium;
        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(_baium, player)
        {
          public void run()
          {
            try {
              val$_baium.setIsInvul(true);
              val$_baium.setRunning();
              val$_baium.broadcastPacket(new SocialAction(val$_baium.getObjectId(), 2));
              startQuestTimer("baium_wakeup", 15000L, val$_baium, val$player);
            }
            catch (Throwable e)
            {
            }
          }
        }
        , 100L);
      }
      else
      {
        htmltext = "Conditions are not right to wake up Baium";
      }
    } else if (npcId == 31862)
    {
      if (player.isFlying())
      {
        return "<html><body>Angelic Vortex:<br>You may not enter while flying a wyvern</body></html>";
      }

      if ((GrandBossManager.getInstance().getBossStatus(29020) == 0) && (player.getQuestState("baium").getQuestItemsCount(4295) > 0))
      {
        player.getQuestState("baium").takeItems(4295, 1);

        _Zone.allowPlayerEntry(player, 30);
        player.teleToLocation(113100, 14500, 10077);
      }
      else {
        npc.showChatWindow(player, 1);
      }
    }
    return htmltext;
  }

  public String onSpellFinished(L2NpcInstance npc, L2PcInstance player, L2Skill skill)
  {
    if (npc.isInvul())
    {
      npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
      return null;
    }
    if ((npc.getNpcId() == 29020) && (!npc.isInvul()))
    {
      callSkillAI(npc);
    }
    return super.onSpellFinished(npc, player, skill);
  }

  public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet) {
    if (!_Zone.isInsideZone(attacker))
    {
      attacker.reduceCurrentHp(attacker.getCurrentHp(), attacker);
      return null;
    }
    if (npc.isInvul())
    {
      npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
      return null;
    }
    if ((npc.getNpcId() == 29020) && (!npc.isInvul()))
    {
      if (attacker.getMountType() == 1)
      {
        int sk_4258 = 0;
        L2Effect[] effects = attacker.getAllEffects();
        if ((effects.length != 0) || (effects != null))
        {
          for (L2Effect e : effects)
          {
            if (e.getSkill().getId() == 4258)
              sk_4258 = 1;
          }
        }
        if (sk_4258 == 0)
        {
          npc.setTarget(attacker);
          npc.doCast(SkillTable.getInstance().getInfo(4258, 1));
        }
      }

      _LastAttackVsBaiumTime = System.currentTimeMillis();
      callSkillAI(npc);
    }
    return super.onAttack(npc, attacker, damage, isPet);
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    cancelQuestTimer("baium_despawn", npc, null);
    npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));

    addSpawn(29055, 115203, 16620, 10078, 0, false, 900000);

    long respawnTime = Config.Interval_Of_Baium_Spawn + Rnd.get(Config.Random_Of_Baium_Spawn);
    GrandBossManager.getInstance().setBossStatus(29020, 2);
    startQuestTimer("baium_unlock", respawnTime, null, null);
    _log.warning(" - Epic: Baium killed: " + time.getTime());

    StatsSet info = GrandBossManager.getInstance().getStatsSet(29020);
    info.set("respawn_time", System.currentTimeMillis() + respawnTime);
    GrandBossManager.getInstance().setStatsSet(29020, info);
    if (getQuestTimer("skill_range", npc, null) != null)
      getQuestTimer("skill_range", npc, null).cancel();
    if (getQuestTimer("angels_aggro_reconsider", npc, null) != null)
      getQuestTimer("angels_aggro_reconsider", npc, null).cancel();
    for (L2Attackable minion : _Minions)
      if (minion != null)
        minion.deleteMe();
    _Minions.clear();
    return super.onKill(npc, killer, isPet);
  }

  public L2Character getRandomTarget(L2NpcInstance npc)
  {
    int npcId = npc.getNpcId();
    FastList result = new FastList();
    Collection objs = npc.getKnownList().getKnownObjects().values();

    for (L2Object obj : objs)
    {
      if (((obj instanceof L2Character)) && (
        ((((L2Character)obj).getZ() < npc.getZ() - 100) && (((L2Character)obj).getZ() > npc.getZ() + 100)) || (!GeoData.getInstance().canSeeTarget(((L2Character)obj).getX(), ((L2Character)obj).getY(), ((L2Character)obj).getZ(), npc.getX(), npc.getY(), npc.getZ()))))
      {
        continue;
      }
      if (((obj instanceof L2PcInstance)) || ((obj instanceof L2SummonInstance)))
      {
        if ((Util.checkIfInRange(9000, npc, obj, true)) && (!((L2Character)obj).isDead())) {
          result.add((L2Character)obj);
        }
      }
      if ((npcId == 29021) && ((obj instanceof L2GrandBossInstance))) {
        result.add((L2Character)obj);
      }
    }

    if (result.isEmpty())
    {
      if (npcId == 29020)
      {
        for (L2NpcInstance minion : _Minions) {
          if (minion != null)
            result.add(minion);
        }
      }
    }
    if ((!result.isEmpty()) && (result.size() != 0))
    {
      Object[] characters = result.toArray();
      QuestTimer timer = getQuestTimer("clean_player", npc, null);
      if (timer != null)
        timer.cancel();
      startQuestTimer("clean_player", 20000L, npc, null);
      return (L2Character)characters[Rnd.get(characters.length)];
    }
    return null;
  }

  public synchronized void callSkillAI(L2NpcInstance npc)
  {
    if ((npc.isInvul()) || (npc.isCastingNow())) return;

    if ((_target == null) || (_target.isDead()) || (!_Zone.isInsideZone(_target)))
    {
      _target = getRandomTarget(npc);
      if (_target != null) {
        _skill = SkillTable.getInstance().getInfo(getRandomSkill(npc), 1);
      }
    }
    L2Character target = _target;
    L2Skill skill = _skill;
    if (skill == null)
      skill = SkillTable.getInstance().getInfo(getRandomSkill(npc), 1);
    if ((target == null) || (target.isDead()) || (!_Zone.isInsideZone(target)))
    {
      return;
    }

    if (Util.checkIfInRange(skill.getCastRange(), npc, target, true))
    {
      npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
      npc.setTarget(target);
      _target = null;
      npc.doCast(skill);
    }
    else
    {
      npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, null);
    }
  }

  private int getSurroundingAngelsNumber(L2NpcInstance npc)
  {
    int count = 0;
    Collection objs = npc.getKnownList().getKnownObjects().values();

    for (L2Object obj : objs)
    {
      if ((obj instanceof L2MonsterInstance))
      {
        if ((((L2NpcInstance)obj).getNpcId() == 29021) && 
          (Util.checkIfInRange(600, npc, obj, true))) {
          count++;
        }
      }
    }
    return count;
  }

  public int getRandomSkill(L2NpcInstance npc)
  {
    int skill = 4127;

    int chance = Rnd.get(100);

    if (getSurroundingAngelsNumber(npc) >= 2)
    {
      if (chance < 25)
        skill = 4130;
      else if ((chance >= 25) && (chance < 50))
        skill = 4131;
      else if ((chance >= 50) && (chance < 75))
        skill = 4128;
      else if ((chance >= 75) && (chance < 100)) {
        skill = 4129;
      }

    }
    else if (npc.getCurrentHp() > npc.getMaxHp() * 3 / 4.0D)
    {
      if (Rnd.get(100) < 10)
        skill = 4128;
      else if (Rnd.get(100) < 10)
        skill = 4129;
      else
        skill = 4127;
    }
    else if (npc.getCurrentHp() > npc.getMaxHp() * 2 / 4.0D)
    {
      if (Rnd.get(100) < 10)
        skill = 4131;
      else if (Rnd.get(100) < 10)
        skill = 4128;
      else if (Rnd.get(100) < 10)
        skill = 4129;
      else
        skill = 4127;
    }
    else if (npc.getCurrentHp() > npc.getMaxHp() * 1 / 4.0D)
    {
      if (Rnd.get(100) < 10)
        skill = 4130;
      else if (Rnd.get(100) < 10)
        skill = 4131;
      else if (Rnd.get(100) < 10)
        skill = 4128;
      else if (Rnd.get(100) < 10)
        skill = 4129;
      else
        skill = 4127;
    }
    else if (Rnd.get(100) < 10)
      skill = 4130;
    else if (Rnd.get(100) < 10)
      skill = 4131;
    else if (Rnd.get(100) < 10)
      skill = 4128;
    else if (Rnd.get(100) < 10)
      skill = 4129;
    else {
      skill = 4127;
    }
    return skill;
  }

  public String onSkillSee(L2NpcInstance npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
  {
    if (npc.isInvul())
    {
      npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
      return null;
    }
    npc.setTarget(caster);
    return super.onSkillSee(npc, caster, skill, targets, isPet);
  }

  public int getDist(int range)
  {
    int dist = 0;
    switch (range)
    {
    case -1:
      break;
    case 100:
      dist = 85;
      break;
    default:
      dist = range - 85;
    }

    return dist;
  }
}