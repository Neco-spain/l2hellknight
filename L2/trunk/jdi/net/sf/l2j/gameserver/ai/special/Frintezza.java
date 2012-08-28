package net.sf.l2j.gameserver.ai.special;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillCanceld;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.Rnd;

public class Frintezza extends L2AttackableAIScript
{
  private static Logger _log = Logger.getLogger(Frintezza.class.getName());
  Calendar time = Calendar.getInstance();

  private static final int[][] _invadeLoc = { { 173967, -76099, -5108 }, { 173967, -76039, -5108 }, { 173867, -76099, -5108 }, { 173967, -76199, -5108 }, { 173767, -76199, -5108 } };

  private static final int[][] _skill = { { 5015, 1, 5000 }, { 5015, 4, 5000 }, { 5015, 2, 5000 }, { 5015, 5, 5000 }, { 5018, 1, 10000 }, { 5016, 1, 5000 }, { 5015, 3, 5000 }, { 5015, 6, 5000 }, { 5018, 2, 10000 }, { 5019, 1, 10000 }, { 5016, 1, 5000 } };

  private static final int[][] _mobLoc = { { 18328, 172894, -76019, -5107, 243 }, { 18328, 174095, -77279, -5107, 16216 }, { 18328, 174111, -74833, -5107, 49043 }, { 18328, 175344, -76042, -5107, 32847 }, { 18330, 173489, -76227, -5134, 63565 }, { 18330, 173498, -75724, -5107, 58498 }, { 18330, 174365, -76745, -5107, 22424 }, { 18330, 174570, -75584, -5107, 31968 }, { 18330, 174613, -76179, -5107, 31471 }, { 18332, 173620, -75981, -5107, 4588 }, { 18332, 173630, -76340, -5107, 62454 }, { 18332, 173755, -75613, -5107, 57892 }, { 18332, 173823, -76688, -5107, 2411 }, { 18332, 174000, -75411, -5107, 54718 }, { 18332, 174487, -75555, -5107, 33861 }, { 18332, 174517, -76471, -5107, 21893 }, { 18332, 174576, -76122, -5107, 31176 }, { 18332, 174600, -75841, -5134, 35927 }, { 18329, 173481, -76043, -5107, 61312 }, { 18329, 173539, -75678, -5107, 59524 }, { 18329, 173584, -76386, -5107, 3041 }, { 18329, 173773, -75420, -5107, 51115 }, { 18329, 173777, -76650, -5107, 12588 }, { 18329, 174585, -76510, -5107, 21704 }, { 18329, 174623, -75571, -5107, 40141 }, { 18329, 174744, -76240, -5107, 29202 }, { 18329, 174769, -75895, -5107, 29572 }, { 18333, 173861, -76011, -5107, 383 }, { 18333, 173872, -76461, -5107, 8041 }, { 18333, 173898, -75668, -5107, 51856 }, { 18333, 174422, -75689, -5107, 42878 }, { 18333, 174460, -76355, -5107, 27311 }, { 18333, 174483, -76041, -5107, 30947 }, { 18331, 173515, -76184, -5107, 6971 }, { 18331, 173516, -75790, -5134, 3142 }, { 18331, 173696, -76675, -5107, 6757 }, { 18331, 173766, -75502, -5134, 60827 }, { 18331, 174473, -75321, -5107, 37147 }, { 18331, 174493, -76505, -5107, 34503 }, { 18331, 174568, -75654, -5134, 41661 }, { 18331, 174584, -76263, -5107, 31729 }, { 18339, 173892, -81592, -5123, 50849 }, { 18339, 173958, -81820, -5123, 7459 }, { 18339, 174128, -81805, -5150, 21495 }, { 18339, 174245, -81566, -5123, 41760 }, { 18334, 173264, -81529, -5072, 1646 }, { 18334, 173265, -81656, -5072, 441 }, { 18334, 173267, -81889, -5072, 0 }, { 18334, 173271, -82015, -5072, 65382 }, { 18334, 174867, -81655, -5073, 32537 }, { 18334, 174868, -81890, -5073, 32768 }, { 18334, 174869, -81485, -5073, 32315 }, { 18334, 174871, -82017, -5073, 33007 }, { 18335, 173074, -80817, -5107, 8353 }, { 18335, 173128, -82702, -5107, 5345 }, { 18335, 173181, -82544, -5107, 65135 }, { 18335, 173191, -80981, -5107, 6947 }, { 18335, 174859, -80889, -5134, 24103 }, { 18335, 174924, -82666, -5107, 38710 }, { 18335, 174947, -80733, -5107, 22449 }, { 18335, 175096, -82724, -5107, 42205 }, { 18336, 173435, -80512, -5107, 65215 }, { 18336, 173440, -82948, -5107, 417 }, { 18336, 173443, -83120, -5107, 1094 }, { 18336, 173463, -83064, -5107, 286 }, { 18336, 173465, -80453, -5107, 174 }, { 18336, 173465, -83006, -5107, 2604 }, { 18336, 173468, -82889, -5107, 316 }, { 18336, 173469, -80570, -5107, 65353 }, { 18336, 173469, -80628, -5107, 166 }, { 18336, 173492, -83121, -5107, 394 }, { 18336, 173493, -80683, -5107, 0 }, { 18336, 173497, -80510, -5134, 417 }, { 18336, 173499, -82947, -5107, 0 }, { 18336, 173521, -83063, -5107, 316 }, { 18336, 173523, -82889, -5107, 128 }, { 18336, 173524, -80627, -5134, 65027 }, { 18336, 173524, -83007, -5107, 0 }, { 18336, 173526, -80452, -5107, 64735 }, { 18336, 173527, -80569, -5134, 65062 }, { 18336, 174602, -83122, -5107, 33104 }, { 18336, 174604, -82949, -5107, 33184 }, { 18336, 174609, -80514, -5107, 33234 }, { 18336, 174609, -80684, -5107, 32851 }, { 18336, 174629, -80627, -5107, 33346 }, { 18336, 174632, -80570, -5107, 32896 }, { 18336, 174632, -83066, -5107, 32768 }, { 18336, 174635, -82893, -5107, 33594 }, { 18336, 174636, -80456, -5107, 32065 }, { 18336, 174639, -83008, -5107, 33057 }, { 18336, 174660, -80512, -5107, 33057 }, { 18336, 174661, -83121, -5107, 32768 }, { 18336, 174663, -82948, -5107, 32768 }, { 18336, 174664, -80685, -5107, 32676 }, { 18336, 174687, -83008, -5107, 32520 }, { 18336, 174691, -83066, -5107, 32961 }, { 18336, 174692, -80455, -5107, 33202 }, { 18336, 174692, -80571, -5107, 32768 }, { 18336, 174693, -80630, -5107, 32994 }, { 18336, 174693, -82889, -5107, 32622 }, { 18337, 172837, -82382, -5107, 58363 }, { 18337, 172867, -81123, -5107, 64055 }, { 18337, 172883, -82495, -5107, 64764 }, { 18337, 172916, -81033, -5107, 7099 }, { 18337, 172940, -82325, -5107, 58998 }, { 18337, 172946, -82435, -5107, 58038 }, { 18337, 172971, -81198, -5107, 14768 }, { 18337, 172992, -81091, -5107, 9438 }, { 18337, 173032, -82365, -5107, 59041 }, { 18337, 173064, -81125, -5107, 5827 }, { 18337, 175014, -81173, -5107, 26398 }, { 18337, 175061, -82374, -5107, 43290 }, { 18337, 175096, -81080, -5107, 24719 }, { 18337, 175169, -82453, -5107, 37672 }, { 18337, 175172, -80972, -5107, 32315 }, { 18337, 175174, -82328, -5107, 41760 }, { 18337, 175197, -81157, -5107, 27617 }, { 18337, 175245, -82547, -5107, 40275 }, { 18337, 175249, -81075, -5107, 28435 }, { 18337, 175292, -82432, -5107, 42225 }, { 18338, 173014, -82628, -5107, 11874 }, { 18338, 173033, -80920, -5107, 10425 }, { 18338, 173095, -82520, -5107, 49152 }, { 18338, 173115, -80986, -5107, 9611 }, { 18338, 173144, -80894, -5107, 5345 }, { 18338, 173147, -82602, -5107, 51316 }, { 18338, 174912, -80825, -5107, 24270 }, { 18338, 174935, -80899, -5107, 18061 }, { 18338, 175016, -82697, -5107, 39533 }, { 18338, 175041, -80834, -5107, 25420 }, { 18338, 175071, -82549, -5107, 39163 }, { 18338, 175154, -82619, -5107, 36345 } };
  private static final int SCARLET1 = 29046;
  private static final int SCARLET2 = 29047;
  private static final int FRINTEZZA = 29045;
  private static final int GUIDE = 32011;
  private static final int CUBE = 29061;
  private static final byte DORMANT = 0;
  private static final byte WAITING = 1;
  private static final byte FIGHTING = 2;
  private static final byte DEAD = 3;
  private static long _LastAction = 0L;
  private static int _Angle = 0;
  private static int _Heading = 0;
  private static int _LocCycle = 0;
  private static int _Bomber = 0;
  private static int _CheckDie = 0;
  private static int _OnCheck = 0;
  private static int _OnSong = 0;
  private static int _Abnormal = 0;
  private static int _OnMorph = 0;
  private static int _Scarlet_x = 0;
  private static int _Scarlet_y = 0;
  private static int _Scarlet_z = 0;
  private static int _Scarlet_h = 0;
  private static double _Scarlet_hp = 0.0D;
  private static double _Scarlet_mp = 0.0D;
  private static int _SecondMorph = 0;
  private static int _ThirdMorph = 0;
  private static int _KillHallAlarmDevice = 0;
  private static int _KillDarkChoirPlayer = 0;
  private static int _KillDarkChoirCaptain = 0;
  private static L2BossZone _Zone;
  private L2GrandBossInstance frintezza;
  private L2GrandBossInstance weakScarlet;
  private L2GrandBossInstance strongScarlet;
  private L2GrandBossInstance activeScarlet;
  private L2MonsterInstance demon1;
  private L2MonsterInstance demon2;
  private L2MonsterInstance demon3;
  private L2MonsterInstance demon4;
  private L2MonsterInstance portrait1;
  private L2MonsterInstance portrait2;
  private L2MonsterInstance portrait3;
  private L2MonsterInstance portrait4;
  private L2NpcInstance _frintezzaDummy;
  private L2NpcInstance _overheadDummy;
  private L2NpcInstance _portraitDummy1;
  private L2NpcInstance _portraitDummy3;
  private L2NpcInstance _scarletDummy;
  private static List<L2PcInstance> _PlayersInside = new FastList();
  private static List<L2NpcInstance> _Room1Mobs = new FastList();
  private static List<L2NpcInstance> _Room2Mobs = new FastList();
  private static List<L2Attackable> Minions = new FastList();

  public Frintezza(int id, String name, String descr)
  {
    super(id, name, descr);
    int[] mob = { 29046, 29047, 29045, 18328, 18329, 18330, 18331, 18332, 18333, 18334, 18335, 18336, 18337, 18338, 18339, 29048, 29049, 29050, 29051 };
    _Zone = GrandBossManager.getInstance().getZone(174232, -88020, -5116);
    registerMobs(mob);
    addStartNpc(32011);
    addTalkId(32011);
    addStartNpc(29061);
    addTalkId(29061);
    StatsSet info = GrandBossManager.getInstance().getStatsSet(29045);
    int status = GrandBossManager.getInstance().getBossStatus(29045);
    if (status == 3)
    {
      long temp = info.getLong("respawn_time") - System.currentTimeMillis();
      if (temp > 0L) {
        startQuestTimer("frintezza_unlock", temp, null, null);
      }
      else {
        GrandBossManager.getInstance().setBossStatus(29045, 0);
      }
    }
    else if (status != 0)
    {
      GrandBossManager.getInstance().setBossStatus(29045, 0);
    }

    for (int i = 0; i < 8; i++)
      DoorTable.getInstance().getDoor(Integer.valueOf(25150051 + i)).setRange(0, 0, 0, 0, 0, 0);
  }

  public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
  {
    long temp = 0L;
    if (event.equalsIgnoreCase("waiting"))
    {
      startQuestTimer("close", 27000L, npc, null);
      startQuestTimer("camera_1", 30000L, npc, null);
      _Zone.broadcastPacket(new Earthquake(174232, -88020, -5116, 45, 27));
    }
    else if (event.equalsIgnoreCase("room1_spawn"))
    {
      for (int i = 0; i <= 17; i++)
      {
        L2NpcInstance mob = addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0);
        _Room1Mobs.add(mob);
      }
    }
    else if (event.equalsIgnoreCase("room1_spawn2"))
    {
      for (int i = 18; i <= 26; i++)
      {
        L2NpcInstance mob = addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0);
        _Room1Mobs.add(mob);
      }
    }
    else if (event.equalsIgnoreCase("room1_spawn3"))
    {
      for (int i = 27; i <= 32; i++)
      {
        L2NpcInstance mob = addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0);
        _Room1Mobs.add(mob);
      }
    }
    else if (event.equalsIgnoreCase("room1_spawn4"))
    {
      for (int i = 33; i <= 40; i++)
      {
        L2NpcInstance mob = addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0);
        _Room1Mobs.add(mob);
      }
    }
    else if (event.equalsIgnoreCase("room2_spawn"))
    {
      for (int i = 41; i <= 44; i++)
      {
        L2NpcInstance mob = addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0);
        _Room2Mobs.add(mob);
      }
    }
    else if (event.equalsIgnoreCase("room2_spawn2"))
    {
      for (int i = 45; i <= 131; i++)
      {
        L2NpcInstance mob = addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0);
        _Room2Mobs.add(mob);
      }
    }
    else if (event.equalsIgnoreCase("room1_del"))
    {
      for (L2NpcInstance mob : _Room1Mobs)
      {
        if (mob != null)
          mob.deleteMe();
      }
      _Room1Mobs.clear();
    }
    else if (event.equalsIgnoreCase("room2_del"))
    {
      for (L2NpcInstance mob : _Room2Mobs)
      {
        if (mob != null)
          mob.deleteMe();
      }
      _Room2Mobs.clear();
    }
    else if (event.equalsIgnoreCase("room3_del"))
    {
      if (demon1 != null)
        demon1.deleteMe();
      if (demon2 != null)
        demon2.deleteMe();
      if (demon3 != null)
        demon3.deleteMe();
      if (demon4 != null)
        demon4.deleteMe();
      if (portrait1 != null)
        portrait1.deleteMe();
      if (portrait2 != null)
        portrait2.deleteMe();
      if (portrait3 != null)
        portrait3.deleteMe();
      if (portrait4 != null)
        portrait4.deleteMe();
      if (frintezza != null)
        frintezza.deleteMe();
      if (weakScarlet != null)
        weakScarlet.deleteMe();
      if (strongScarlet != null) {
        strongScarlet.deleteMe();
      }
      demon1 = null;
      demon2 = null;
      demon3 = null;
      demon4 = null;
      portrait1 = null;
      portrait2 = null;
      portrait3 = null;
      portrait4 = null;
      frintezza = null;
      weakScarlet = null;
      strongScarlet = null;
      activeScarlet = null;
    }
    else if (event.equalsIgnoreCase("clean"))
    {
      _LastAction = 0L;
      _LocCycle = 0;
      _CheckDie = 0;
      _OnCheck = 0;
      _Abnormal = 0;
      _OnMorph = 0;
      _SecondMorph = 0;
      _ThirdMorph = 0;
      _KillHallAlarmDevice = 0;
      _KillDarkChoirPlayer = 0;
      _KillDarkChoirCaptain = 0;
      _PlayersInside.clear();
    }
    else if (event.equalsIgnoreCase("close"))
    {
      for (int i = 25150051; i <= 25150058; i++)
        DoorTable.getInstance().getDoor(Integer.valueOf(i)).closeMe();
      for (int i = 25150061; i <= 25150070; i++) {
        DoorTable.getInstance().getDoor(Integer.valueOf(i)).closeMe();
      }
      DoorTable.getInstance().getDoor(Integer.valueOf(25150042)).closeMe();
      DoorTable.getInstance().getDoor(Integer.valueOf(25150043)).closeMe();
      DoorTable.getInstance().getDoor(Integer.valueOf(25150045)).closeMe();
      DoorTable.getInstance().getDoor(Integer.valueOf(25150046)).closeMe();
    }
    else if (event.equalsIgnoreCase("loc_check"))
    {
      if (GrandBossManager.getInstance().getBossStatus(29045) == 2)
      {
        if (!_Zone.isInsideZone(npc))
          npc.teleToLocation(174232, -88020, -5116);
        if ((npc.getX() < 171932) || (npc.getX() > 176532) || (npc.getY() < -90320) || (npc.getY() > -85720) || (npc.getZ() < -5130))
          npc.teleToLocation(174232, -88020, -5116);
      }
    }
    else if (event.equalsIgnoreCase("camera_1"))
    {
      GrandBossManager.getInstance().setBossStatus(29045, 2);

      _frintezzaDummy = addSpawn(29052, 174240, -89805, -5022, 16048, false, 0);
      _frintezzaDummy.setIsInvul(true);
      _frintezzaDummy.setIsImobilised(true);

      _overheadDummy = addSpawn(29052, 174232, -88020, -5116, 16384, false, 0);
      _overheadDummy.setIsInvul(true);
      _overheadDummy.setIsImobilised(true);
      _overheadDummy.setCollisionHeight(600);
      _Zone.broadcastPacket(new NpcInfo(_overheadDummy, null));

      _portraitDummy1 = addSpawn(29052, 172450, -87890, -5089, 16048, false, 0);
      _portraitDummy1.setIsImobilised(true);
      _portraitDummy1.setIsInvul(true);

      _portraitDummy3 = addSpawn(29052, 176012, -87890, -5089, 16048, false, 0);
      _portraitDummy3.setIsImobilised(true);
      _portraitDummy3.setIsInvul(true);

      _scarletDummy = addSpawn(29053, 174232, -88020, -5116, 16384, false, 0);
      _scarletDummy.setIsInvul(true);
      _scarletDummy.setIsImobilised(true);

      startQuestTimer("stop_pc", 0L, npc, null);
      startQuestTimer("camera_2", 1000L, _overheadDummy, null);
    }
    else if (event.equalsIgnoreCase("camera_2"))
    {
      _Zone.broadcastPacket(new SpecialCamera(_overheadDummy.getObjectId(), 0, 75, -89, 0, 100));
      startQuestTimer("camera_2b", 0L, _overheadDummy, null);
    }
    else if (event.equalsIgnoreCase("camera_2b"))
    {
      _Zone.broadcastPacket(new SpecialCamera(_overheadDummy.getObjectId(), 0, 75, -89, 0, 100));
      startQuestTimer("camera_3", 0L, _overheadDummy, null);
    }
    else if (event.equalsIgnoreCase("camera_3"))
    {
      _Zone.broadcastPacket(new SpecialCamera(_overheadDummy.getObjectId(), 300, 90, -10, 6500, 7000));

      frintezza = ((L2GrandBossInstance)addSpawn(29045, 174240, -89805, -5022, 16048, false, 0));
      GrandBossManager.getInstance().addBoss(frintezza);
      frintezza.setIsImobilised(true);
      frintezza.setIsInvul(true);
      frintezza.disableAllSkills();
      _Zone.updateKnownList(frintezza);

      demon2 = ((L2MonsterInstance)addSpawn(29051, 175876, -88713, -4972, 28205, false, 0));
      demon2.setIsImobilised(true);
      demon2.disableAllSkills();
      _Zone.updateKnownList(demon2);

      demon3 = ((L2MonsterInstance)addSpawn(29051, 172608, -88702, -4972, 64817, false, 0));
      demon3.setIsImobilised(true);
      demon3.disableAllSkills();
      _Zone.updateKnownList(demon3);

      demon1 = ((L2MonsterInstance)addSpawn(29050, 175833, -87165, -4972, 35048, false, 0));
      demon1.setIsImobilised(true);
      demon1.disableAllSkills();
      _Zone.updateKnownList(demon1);

      demon4 = ((L2MonsterInstance)addSpawn(29050, 172634, -87165, -4972, 57730, false, 0));
      demon4.setIsImobilised(true);
      demon4.disableAllSkills();
      _Zone.updateKnownList(demon4);

      startQuestTimer("camera_4", 6500L, _overheadDummy, null);
    }
    else if (event.equalsIgnoreCase("camera_4"))
    {
      _Zone.broadcastPacket(new SpecialCamera(_frintezzaDummy.getObjectId(), 1800, 90, 8, 6500, 7000));
      startQuestTimer("camera_5", 900L, _frintezzaDummy, null);
    }
    else if (event.equalsIgnoreCase("camera_5"))
    {
      _Zone.broadcastPacket(new SpecialCamera(_frintezzaDummy.getObjectId(), 140, 90, 10, 2500, 4500));
      startQuestTimer("camera_5b", 4000L, _frintezzaDummy, null);
    }
    else if (event.equalsIgnoreCase("camera_5b"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 40, 75, -10, 0, 1000));
      startQuestTimer("camera_6", 0L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("camera_6"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 40, 75, -10, 0, 12000));
      startQuestTimer("camera_7", 1350L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("camera_7"))
    {
      _Zone.broadcastPacket(new SocialAction(frintezza.getObjectId(), 2));
      startQuestTimer("camera_8", 7000L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("camera_8"))
    {
      startQuestTimer("camera_9", 1000L, frintezza, null);
      _frintezzaDummy.deleteMe();
      _frintezzaDummy = null;
    }
    else if (event.equalsIgnoreCase("camera_9"))
    {
      _Zone.broadcastPacket(new SocialAction(demon2.getObjectId(), 1));
      _Zone.broadcastPacket(new SocialAction(demon3.getObjectId(), 1));
      startQuestTimer("camera_9b", 400L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("camera_9b"))
    {
      _Zone.broadcastPacket(new SocialAction(demon1.getObjectId(), 1));
      _Zone.broadcastPacket(new SocialAction(demon4.getObjectId(), 1));

      for (L2Character pc : _Zone.getCharactersInside().values())
      {
        if ((pc instanceof L2PcInstance))
        {
          if (pc.getX() < 174232)
            pc.broadcastPacket(new SpecialCamera(_portraitDummy1.getObjectId(), 1000, 118, 0, 0, 1000));
          else
            pc.broadcastPacket(new SpecialCamera(_portraitDummy3.getObjectId(), 1000, 62, 0, 0, 1000));
        }
      }
      startQuestTimer("camera_9c", 0L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("camera_9c"))
    {
      for (L2Character pc : _Zone.getCharactersInside().values())
      {
        if ((pc instanceof L2PcInstance))
        {
          if (pc.getX() < 174232)
            pc.broadcastPacket(new SpecialCamera(_portraitDummy1.getObjectId(), 1000, 118, 0, 0, 10000));
          else
            pc.broadcastPacket(new SpecialCamera(_portraitDummy3.getObjectId(), 1000, 62, 0, 0, 10000));
        }
      }
      startQuestTimer("camera_10", 2000L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("camera_10"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 240, 90, 0, 0, 1000));
      startQuestTimer("camera_11", 0L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("camera_11"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 240, 90, 25, 5500, 10000));
      _Zone.broadcastPacket(new SocialAction(frintezza.getObjectId(), 3));
      _portraitDummy1.deleteMe();
      _portraitDummy3.deleteMe();
      _portraitDummy1 = null;
      _portraitDummy3 = null;

      startQuestTimer("camera_12", 4500L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("camera_12"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 100, 195, 35, 0, 10000));
      startQuestTimer("camera_13", 700L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("camera_13"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 100, 195, 35, 0, 10000));
      startQuestTimer("camera_14", 1300L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("camera_14"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 120, 180, 45, 1500, 10000));
      _Zone.broadcastPacket(new MagicSkillUser(frintezza, frintezza, 5006, 1, 34000, 0));
      startQuestTimer("camera_16", 1500L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("camera_16"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 520, 135, 45, 8000, 10000));
      startQuestTimer("camera_17", 7500L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("camera_17"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 1500, 110, 25, 10000, 13000));
      startQuestTimer("camera_18", 9500L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("camera_18"))
    {
      _Zone.broadcastPacket(new SpecialCamera(_overheadDummy.getObjectId(), 930, 160, -20, 0, 1000));
      startQuestTimer("camera_18b", 0L, _overheadDummy, null);
    }
    else if (event.equalsIgnoreCase("camera_18b"))
    {
      _Zone.broadcastPacket(new SpecialCamera(_overheadDummy.getObjectId(), 930, 160, -20, 0, 10000));

      weakScarlet = ((L2GrandBossInstance)addSpawn(29046, 174232, -88020, -5116, 16384, false, 0));
      weakScarlet.setIsInvul(true);
      weakScarlet.setIsImobilised(true);
      weakScarlet.disableAllSkills();
      _Zone.updateKnownList(weakScarlet);
      activeScarlet = weakScarlet;

      _Zone.broadcastPacket(new MagicSkillUser(_scarletDummy, _overheadDummy, 5004, 1, 5800, 0));

      startQuestTimer("camera_19", 5500L, _scarletDummy, null);
      startQuestTimer("camera_19b", 5400L, weakScarlet, null);
    }
    else if (event.equalsIgnoreCase("camera_19"))
    {
      _Zone.broadcastPacket(new SpecialCamera(weakScarlet.getObjectId(), 800, 160, 5, 1000, 10000));
      startQuestTimer("camera_20", 2100L, weakScarlet, null);
    }
    else if (event.equalsIgnoreCase("camera_19b"))
    {
      _Zone.broadcastPacket(new SocialAction(weakScarlet.getObjectId(), 3));
    }
    else if (event.equalsIgnoreCase("camera_20"))
    {
      _Zone.broadcastPacket(new SpecialCamera(weakScarlet.getObjectId(), 300, 60, 8, 0, 10000));
      startQuestTimer("camera_21", 2000L, weakScarlet, null);
    }
    else if (event.equalsIgnoreCase("camera_21"))
    {
      _Zone.broadcastPacket(new SpecialCamera(weakScarlet.getObjectId(), 500, 90, 10, 3000, 5000));
      startQuestTimer("camera_22", 3000L, weakScarlet, null);
    }
    else if (event.equalsIgnoreCase("camera_22"))
    {
      portrait2 = ((L2MonsterInstance)addSpawn(29049, 175876, -88713, -4972, 28205, false, 0));
      portrait2.setIsImobilised(true);
      portrait2.disableAllSkills();
      _Zone.updateKnownList(portrait2);

      portrait3 = ((L2MonsterInstance)addSpawn(29049, 172608, -88702, -4972, 64817, false, 0));
      portrait3.setIsImobilised(true);
      portrait3.disableAllSkills();
      _Zone.updateKnownList(portrait3);

      portrait1 = ((L2MonsterInstance)addSpawn(29048, 175833, -87165, -4972, 35048, false, 0));
      portrait1.setIsImobilised(true);
      portrait1.disableAllSkills();
      _Zone.updateKnownList(portrait1);

      portrait4 = ((L2MonsterInstance)addSpawn(29048, 172634, -87165, -4972, 57730, false, 0));
      portrait4.setIsImobilised(true);
      portrait4.disableAllSkills();
      _Zone.updateKnownList(portrait4);

      _overheadDummy.deleteMe();
      _scarletDummy.deleteMe();
      _overheadDummy = null;
      _scarletDummy = null;

      startQuestTimer("camera_23", 2000L, weakScarlet, null);
      startQuestTimer("start_pc", 2000L, weakScarlet, null);
      startQuestTimer("loc_check", 60000L, weakScarlet, null, true);
      startQuestTimer("songs_play", 10000 + Rnd.get(10000), frintezza, null);
      startQuestTimer("skill01", 10000 + Rnd.get(10000), weakScarlet, null);
    }
    else if (event.equalsIgnoreCase("camera_23"))
    {
      demon1.setIsImobilised(false);
      demon2.setIsImobilised(false);
      demon3.setIsImobilised(false);
      demon4.setIsImobilised(false);
      demon1.enableAllSkills();
      demon2.enableAllSkills();
      demon3.enableAllSkills();
      demon4.enableAllSkills();
      portrait1.setIsImobilised(false);
      portrait2.setIsImobilised(false);
      portrait3.setIsImobilised(false);
      portrait4.setIsImobilised(false);
      portrait1.enableAllSkills();
      portrait2.enableAllSkills();
      portrait3.enableAllSkills();
      portrait4.enableAllSkills();
      weakScarlet.setIsInvul(false);
      weakScarlet.setIsImobilised(false);
      weakScarlet.enableAllSkills();
      weakScarlet.setRunning();

      startQuestTimer("spawn_minion", 20000L, portrait1, null);
      startQuestTimer("spawn_minion", 20000L, portrait2, null);
      startQuestTimer("spawn_minion", 20000L, portrait3, null);
      startQuestTimer("spawn_minion", 20000L, portrait4, null);
    }
    else if (event.equalsIgnoreCase("stop_pc"))
    {
      for (L2Character cha : _Zone.getCharactersInside().values())
      {
        cha.abortAttack();
        cha.abortCast();
        cha.disableAllSkills();
        cha.setTarget(null);
        cha.stopMove(null);
        cha.setIsImobilised(true);
        cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
      }
    }
    else if (event.equalsIgnoreCase("stop_npc"))
    {
      _Heading = npc.getHeading();
      if (_Heading < 32768)
        _Angle = Math.abs(180 - (int)(_Heading / 182.04444444399999D));
      else
        _Angle = Math.abs(540 - (int)(_Heading / 182.04444444399999D));
    }
    else if (event.equalsIgnoreCase("start_pc"))
    {
      for (L2Character cha : _Zone.getCharactersInside().values())
      {
        if (cha != frintezza)
        {
          cha.enableAllSkills();
          cha.setIsImobilised(false);
        }
      }
    }
    else if (event.equalsIgnoreCase("start_npc"))
    {
      npc.setRunning();
      npc.setIsInvul(false);
    }
    else if (event.equalsIgnoreCase("morph_end"))
    {
      _OnMorph = 0;
    }
    else if (event.equalsIgnoreCase("morph_01"))
    {
      _Zone.broadcastPacket(new SpecialCamera(weakScarlet.getObjectId(), 250, _Angle, 12, 2000, 15000));
      startQuestTimer("morph_02", 3000L, weakScarlet, null);
    }
    else if (event.equalsIgnoreCase("morph_02"))
    {
      _Zone.broadcastPacket(new SocialAction(weakScarlet.getObjectId(), 1));
      weakScarlet.setRHandId(7903);
      startQuestTimer("morph_03", 4000L, weakScarlet, null);
    }
    else if (event.equalsIgnoreCase("morph_03"))
    {
      startQuestTimer("morph_04", 1500L, weakScarlet, null);
    }
    else if (event.equalsIgnoreCase("morph_04"))
    {
      _Zone.broadcastPacket(new SocialAction(weakScarlet.getObjectId(), 4));
      L2Skill skill = SkillTable.getInstance().getInfo(5017, 1);
      if (skill != null) {
        skill.getEffects(weakScarlet, weakScarlet);
      }
      startQuestTimer("morph_end", 6000L, weakScarlet, null);
      startQuestTimer("start_pc", 3000L, weakScarlet, null);
      startQuestTimer("start_npc", 3000L, weakScarlet, null);
      startQuestTimer("songs_play", 10000 + Rnd.get(10000), frintezza, null);
      startQuestTimer("skill02", 10000 + Rnd.get(10000), weakScarlet, null);
    }
    else if (event.equalsIgnoreCase("morph_05a"))
    {
      _Zone.broadcastPacket(new SocialAction(frintezza.getObjectId(), 4));
    }
    else if (event.equalsIgnoreCase("morph_05"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 250, 120, 15, 0, 1000));
      startQuestTimer("morph_06", 0L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("morph_06"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 250, 120, 15, 0, 10000));

      cancelQuestTimers("loc_check");

      _Scarlet_x = weakScarlet.getX();
      _Scarlet_y = weakScarlet.getY();
      _Scarlet_z = weakScarlet.getZ();
      _Scarlet_h = weakScarlet.getHeading();
      _Scarlet_hp = weakScarlet.getCurrentHp();
      _Scarlet_mp = weakScarlet.getCurrentMp();
      weakScarlet.deleteMe();
      weakScarlet = null;
      activeScarlet = null;
      weakScarlet = ((L2GrandBossInstance)addSpawn(29046, _Scarlet_x, _Scarlet_y, _Scarlet_z, _Scarlet_h, false, 0));
      weakScarlet.setIsInvul(true);
      weakScarlet.setIsImobilised(true);
      weakScarlet.disableAllSkills();
      weakScarlet.setRHandId(7903);
      _Zone.updateKnownList(weakScarlet);

      startQuestTimer("morph_07", 7000L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("morph_07"))
    {
      _Zone.broadcastPacket(new MagicSkillUser(frintezza, frintezza, 5006, 1, 34000, 0));
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 500, 70, 15, 3000, 10000));
      startQuestTimer("morph_08", 3000L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("morph_08"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 2500, 90, 12, 6000, 10000));
      startQuestTimer("morph_09", 3000L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("morph_09"))
    {
      _Zone.broadcastPacket(new SpecialCamera(weakScarlet.getObjectId(), 250, _Angle, 12, 0, 1000));
      startQuestTimer("morph_10", 0L, weakScarlet, null);
    }
    else if (event.equalsIgnoreCase("morph_10"))
    {
      _Zone.broadcastPacket(new SpecialCamera(weakScarlet.getObjectId(), 250, _Angle, 12, 0, 10000));
      startQuestTimer("morph_11", 500L, weakScarlet, null);
    }
    else if (event.equalsIgnoreCase("morph_11"))
    {
      weakScarlet.doDie(weakScarlet);
      _Zone.broadcastPacket(new SpecialCamera(weakScarlet.getObjectId(), 450, _Angle, 14, 8000, 8000));

      startQuestTimer("morph_12", 6250L, weakScarlet, null);
      startQuestTimer("morph_13", 7200L, weakScarlet, null);
    }
    else if (event.equalsIgnoreCase("morph_12"))
    {
      weakScarlet.deleteMe();
      weakScarlet = null;
    }
    else if (event.equalsIgnoreCase("morph_13"))
    {
      strongScarlet = ((L2GrandBossInstance)addSpawn(29047, _Scarlet_x, _Scarlet_y, _Scarlet_z, _Scarlet_h, false, 0));
      strongScarlet.setIsInvul(true);
      strongScarlet.setIsImobilised(true);
      strongScarlet.disableAllSkills();
      strongScarlet.setCurrentHpMp(_Scarlet_hp, _Scarlet_mp);
      _Zone.updateKnownList(strongScarlet);
      activeScarlet = strongScarlet;

      _Zone.broadcastPacket(new SpecialCamera(strongScarlet.getObjectId(), 450, _Angle, 12, 500, 14000));

      startQuestTimer("morph_14", 3000L, strongScarlet, null);
      startQuestTimer("loc_check", 60000L, strongScarlet, null, true);
    }
    else if (event.equalsIgnoreCase("morph_14"))
    {
      startQuestTimer("morph_15", 5100L, strongScarlet, null);
    }
    else if (event.equalsIgnoreCase("morph_15"))
    {
      _Zone.broadcastPacket(new SocialAction(strongScarlet.getObjectId(), 2));
      L2Skill skill = SkillTable.getInstance().getInfo(5017, 1);
      if (skill != null) {
        skill.getEffects(strongScarlet, strongScarlet);
      }
      startQuestTimer("morph_end", 9000L, strongScarlet, null);
      startQuestTimer("start_pc", 6000L, strongScarlet, null);
      startQuestTimer("start_npc", 6000L, strongScarlet, null);
      startQuestTimer("songs_play", 10000 + Rnd.get(10000), frintezza, null);
      startQuestTimer("skill03", 10000 + Rnd.get(10000), strongScarlet, null);
    }
    else if (event.equalsIgnoreCase("morph_16"))
    {
      _Zone.broadcastPacket(new SpecialCamera(strongScarlet.getObjectId(), 300, _Angle - 180, 5, 0, 7000));
      startQuestTimer("morph_17", 0L, strongScarlet, null);
    }
    else if (event.equalsIgnoreCase("morph_17"))
    {
      _Zone.broadcastPacket(new SpecialCamera(strongScarlet.getObjectId(), 200, _Angle, 85, 4000, 10000));
      startQuestTimer("morph_17b", 7400L, frintezza, null);
      startQuestTimer("morph_18", 7500L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("morph_17b"))
    {
      frintezza.doDie(frintezza);
    }
    else if (event.equalsIgnoreCase("morph_18"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 100, 120, 5, 0, 7000));
      startQuestTimer("morph_19", 0L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("morph_19"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 100, 90, 5, 5000, 15000));
      startQuestTimer("morph_20", 7000L, frintezza, null);
      startQuestTimer("spawn_cubes", 7000L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("morph_20"))
    {
      _Zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 900, 90, 25, 7000, 10000));
      startQuestTimer("start_pc", 7000L, frintezza, null);
    }
    else if (event.equalsIgnoreCase("songs_play"))
    {
      if ((frintezza != null) && (!frintezza.isDead()) && (_OnMorph == 0))
      {
        _OnSong = Rnd.get(1, 5);
        if ((_OnSong == 1) && (_ThirdMorph == 1) && (strongScarlet.getCurrentHp() < strongScarlet.getMaxHp() * 0.6D) && (Rnd.get(100) < 80))
        {
          _Zone.broadcastPacket(new MagicSkillUser(frintezza, frintezza, 5007, 1, 32000, 0));
          startQuestTimer("songs_effect", 5000L, frintezza, null);
          startQuestTimer("songs_play", 32000 + Rnd.get(10000), frintezza, null);
        }
        else if ((_OnSong == 2) || (_OnSong == 3))
        {
          _Zone.broadcastPacket(new MagicSkillUser(frintezza, frintezza, 5007, _OnSong, 32000, 0));
          startQuestTimer("songs_effect", 5000L, frintezza, null);
          startQuestTimer("songs_play", 32000 + Rnd.get(10000), frintezza, null);
        }
        else if ((_OnSong == 4) && (_SecondMorph == 1))
        {
          _Zone.broadcastPacket(new MagicSkillUser(frintezza, frintezza, 5007, 4, 31000, 0));
          startQuestTimer("songs_effect", 5000L, frintezza, null);
          startQuestTimer("songs_play", 31000 + Rnd.get(10000), frintezza, null);
        }
        else if ((_OnSong == 5) && (_ThirdMorph == 1) && (_Abnormal == 0))
        {
          _Abnormal = 1;
          _Zone.broadcastPacket(new MagicSkillUser(frintezza, frintezza, 5007, 5, 35000, 0));
          startQuestTimer("songs_effect", 5000L, frintezza, null);
          startQuestTimer("songs_play", 35000 + Rnd.get(10000), frintezza, null);
        }
        else {
          startQuestTimer("songs_play", 5000 + Rnd.get(5000), frintezza, null);
        }
      }
    } else if (event.equalsIgnoreCase("songs_effect"))
    {
      L2Skill skill = SkillTable.getInstance().getInfo(5008, _OnSong);
      if (skill == null) {
        return null;
      }
      if ((_OnSong == 1) || (_OnSong == 2) || (_OnSong == 3))
      {
        if ((frintezza != null) && (!frintezza.isDead()) && (activeScarlet != null) && (!activeScarlet.isDead()))
          skill.getEffects(frintezza, activeScarlet);
      }
      else if (_OnSong == 4)
      {
        for (L2Character cha : _Zone.getCharactersInside().values())
        {
          if (((cha instanceof L2PcInstance)) && (Rnd.get(100) < 80))
          {
            skill.getEffects(frintezza, cha);
            cha.sendPacket(new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(5008, 4));
          }
        }
      }
      else if (_OnSong == 5)
      {
        for (L2Character cha : _Zone.getCharactersInside().values())
        {
          if (((cha instanceof L2PcInstance)) && (Rnd.get(100) < 70))
          {
            cha.abortAttack();
            cha.abortCast();
            cha.disableAllSkills();
            cha.stopMove(null);
            cha.setIsParalyzed(true);
            cha.setIsImobilised(true);
            cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            skill.getEffects(frintezza, cha);
            cha.startAbnormalEffect(262144);
            cha.sendPacket(new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(5008, 5));
          }
        }
        startQuestTimer("stop_effect", 25000L, frintezza, null);
      }
    }
    else if (event.equalsIgnoreCase("stop_effect"))
    {
      for (L2Character cha : _Zone.getCharactersInside().values())
      {
        if ((cha instanceof L2PcInstance))
        {
          cha.stopAbnormalEffect(262144);
          cha.stopAbnormalEffect(131072);
          cha.enableAllSkills();
          cha.setIsImobilised(false);
          cha.setIsParalyzed(false);
        }
      }
      _Abnormal = 0;
    }
    else if (event.equalsIgnoreCase("attack_stop"))
    {
      cancelQuestTimers("skill01");
      cancelQuestTimers("skill02");
      cancelQuestTimers("skill03");
      cancelQuestTimers("songs_play");
      cancelQuestTimers("songs_effect");

      _Zone.broadcastPacket(new MagicSkillCanceld(frintezza.getObjectId()));
    }
    else if (event.equalsIgnoreCase("check_hp"))
    {
      if (npc.isDead())
      {
        _OnMorph = 1;
        _Zone.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));

        startQuestTimer("attack_stop", 0L, frintezza, null);
        startQuestTimer("stop_pc", 0L, npc, null);
        startQuestTimer("stop_npc", 0L, npc, null);
        startQuestTimer("morph_16", 0L, npc, null);
      }
      else
      {
        _CheckDie += 10;
        if (_CheckDie < 3000) {
          startQuestTimer("check_hp", 10L, npc, null);
        }
        else {
          _OnCheck = 0;
          _CheckDie = 0;
        }
      }
    }
    else if (event.equalsIgnoreCase("skill01"))
    {
      if ((weakScarlet != null) && (!weakScarlet.isDead()) && (_SecondMorph == 0) && (_ThirdMorph == 0) && (_OnMorph == 0))
      {
        int i = Rnd.get(0, 1);
        L2Skill skill = SkillTable.getInstance().getInfo(_skill[i][0], _skill[i][1]);
        if (skill != null) {
          weakScarlet.doCast(skill);
        }
        startQuestTimer("skill01", _skill[i][2] + 5000 + Rnd.get(10000), npc, null);
      }
    }
    else if (event.equalsIgnoreCase("skill02"))
    {
      if ((weakScarlet != null) && (!weakScarlet.isDead()) && (_SecondMorph == 1) && (_ThirdMorph == 0) && (_OnMorph == 0))
      {
        int i = 0;
        if (_Abnormal == 0)
          i = Rnd.get(2, 5);
        else {
          i = Rnd.get(2, 4);
        }
        L2Skill skill = SkillTable.getInstance().getInfo(_skill[i][0], _skill[i][1]);
        if (skill != null) {
          weakScarlet.doCast(skill);
        }
        startQuestTimer("skill02", _skill[i][2] + 5000 + Rnd.get(10000), npc, null);

        if (i == 5)
        {
          _Abnormal = 1;
          startQuestTimer("float_effect", 4000L, weakScarlet, null);
        }
      }
    }
    else if (event.equalsIgnoreCase("skill03"))
    {
      if ((strongScarlet != null) && (!strongScarlet.isDead()) && (_SecondMorph == 1) && (_ThirdMorph == 1) && (_OnMorph == 0))
      {
        int i = 0;
        if (_Abnormal == 0)
          i = Rnd.get(6, 10);
        else {
          i = Rnd.get(6, 9);
        }
        L2Skill skill = SkillTable.getInstance().getInfo(_skill[i][0], _skill[i][1]);
        if (skill != null) {
          strongScarlet.doCast(skill);
        }
        startQuestTimer("skill03", _skill[i][2] + 5000 + Rnd.get(10000), npc, null);

        if (i == 10)
        {
          _Abnormal = 1;
          startQuestTimer("float_effect", 3000L, npc, null);
        }
      }
    }
    else if (event.equalsIgnoreCase("float_effect"))
    {
      if (npc.isCastingNow())
      {
        startQuestTimer("float_effect", 500L, npc, null);
      }
      else
      {
        for (L2Character cha : _Zone.getCharactersInside().values())
        {
          if ((cha instanceof L2PcInstance))
          {
            if (cha.getFirstEffect(5016) != null)
            {
              cha.abortAttack();
              cha.abortCast();
              cha.disableAllSkills();
              cha.stopMove(null);
              cha.setIsParalyzed(true);
              cha.setIsImobilised(true);
              cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
              cha.startAbnormalEffect(131072);
            }
          }
        }
        startQuestTimer("stop_effect", 25000L, npc, null);
      }
    }
    else if (event.equalsIgnoreCase("action"))
    {
      _Zone.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
    }
    else if (event.equalsIgnoreCase("bomber"))
    {
      _Bomber = 0;
    }
    else if (event.equalsIgnoreCase("room_final"))
    {
      _Zone.broadcastPacket(new NpcSay(npc.getObjectId(), 1, npc.getNpcId(), "Exceeded his time limit, challenge failed!"));
      _Zone.oustAllPlayers();

      cancelQuestTimers("waiting");
      cancelQuestTimers("frintezza_despawn");
      startQuestTimer("clean", 1000L, npc, null);
      startQuestTimer("close", 1000L, npc, null);
      startQuestTimer("room1_del", 1000L, npc, null);
      startQuestTimer("room2_del", 1000L, npc, null);

      GrandBossManager.getInstance().setBossStatus(29045, 0);
    }
    else if (event.equalsIgnoreCase("frintezza_despawn"))
    {
      temp = System.currentTimeMillis() - _LastAction;
      if (temp > 900000L)
      {
        _Zone.oustAllPlayers();

        cancelQuestTimers("waiting");
        cancelQuestTimers("loc_check");
        cancelQuestTimers("room_final");
        cancelQuestTimers("spawn_minion");
        startQuestTimer("clean", 1000L, npc, null);
        startQuestTimer("close", 1000L, npc, null);
        startQuestTimer("attack_stop", 1000L, npc, null);
        startQuestTimer("room1_del", 1000L, npc, null);
        startQuestTimer("room2_del", 1000L, npc, null);
        startQuestTimer("room3_del", 1000L, npc, null);
        startQuestTimer("minions_despawn", 1000L, npc, null);

        GrandBossManager.getInstance().setBossStatus(29045, 0);

        cancelQuestTimers("frintezza_despawn");
      }
    }
    else if (event.equalsIgnoreCase("minions_despawn"))
    {
      for (int i = 0; i < Minions.size(); i++)
      {
        L2Attackable mob = (L2Attackable)Minions.get(i);
        if (mob != null)
          mob.decayMe();
      }
      Minions.clear();
    }
    else if (event.equalsIgnoreCase("spawn_minion"))
    {
      if ((npc != null) && (!npc.isDead()) && (frintezza != null) && (!frintezza.isDead()))
      {
        L2NpcInstance mob = addSpawn(npc.getNpcId() + 2, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
        mob.setIsRaidMinion(true);
        Minions.add((L2Attackable)mob);

        startQuestTimer("action", 200L, mob, null);
        startQuestTimer("spawn_minion", 18000L, npc, null);
      }
    }
    else if (event.equalsIgnoreCase("spawn_cubes"))
    {
      addSpawn(29061, 174232, -88020, -5114, 16384, false, 900000);
    }
    else if (event.equalsIgnoreCase("frintezza_unlock"))
    {
      GrandBossManager.getInstance().setBossStatus(29045, 0);
    }
    else if (event.equalsIgnoreCase("remove_players"))
    {
      _Zone.oustAllPlayers();
    }

    return super.onAdvEvent(event, npc, player);
  }

  public String onTalk(L2NpcInstance npc, L2PcInstance player)
  {
    if (npc.getNpcId() == 29061)
    {
      int x = 150037 + Rnd.get(500);
      int y = -57720 + Rnd.get(500);
      player.teleToLocation(x, y, -2976);
      return null;
    }

    String htmltext = "";
    if (GrandBossManager.getInstance().getBossStatus(29045) == 0)
    {
      if ((!player.isInParty()) || (!player.getParty().isLeader(player)) || (player.getParty().getCommandChannel() == null) || (player.getParty().getCommandChannel().getChannelLeader() != player))
      {
        htmltext = "<html><body>No reaction. Contact must be initiated by the Command Channel Leader.</body></html>";
      } else if (player.getInventory().getItemByItemId(8073) == null) {
        htmltext = "<html><body>You dont have required item.</body></html>";
      }
      else {
        player.destroyItemByItemId("Quest", 8073, 1, player, true);
        L2CommandChannel CC = player.getParty().getCommandChannel();
        GrandBossManager.getInstance().setBossStatus(29045, 1);

        startQuestTimer("close", 0L, npc, null);
        startQuestTimer("room1_spawn", 5000L, npc, null);
        startQuestTimer("room_final", 2100000L, npc, null);
        startQuestTimer("frintezza_despawn", 60000L, npc, null, true);

        _LastAction = System.currentTimeMillis();
        for (L2Party party : CC.getPartys())
        {
          if (party == null)
            continue;
          for (L2PcInstance member : party.getPartyMembers())
          {
            if ((member == null) || (member.getLevel() < 74) || 
              (!member.isInsideRadius(npc, 1100, false, false)))
              continue;
            if (_PlayersInside.size() > 45)
            {
              member.sendMessage("The number of challenges have been full, so can not enter.");
              break;
            }

            _PlayersInside.add(member);
            _Zone.allowPlayerEntry(member, 300);
            member.teleToLocation(_invadeLoc[_LocCycle][0] + Rnd.get(50), _invadeLoc[_LocCycle][1] + Rnd.get(50), _invadeLoc[_LocCycle][2]);
          }
          if (_PlayersInside.size() > 45) {
            break;
          }
          _LocCycle += 1;
          if (_LocCycle >= 5)
            _LocCycle = 0;
        }
      }
    }
    else {
      htmltext = "<html><body>Someone else is already inside the Magic Force Field. Try again later.</body></html>";
    }
    return htmltext;
  }

  public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    _LastAction = System.currentTimeMillis();
    if (npc.getNpcId() == 29045)
    {
      npc.setCurrentHpMp(npc.getMaxHp(), 0.0D);
      return null;
    }
    if ((npc.getNpcId() == 29046) && (_SecondMorph == 0) && (_ThirdMorph == 0) && (_OnMorph == 0) && (npc.getCurrentHp() < npc.getMaxHp() * 0.75D) && (GrandBossManager.getInstance().getBossStatus(29045) == 2))
    {
      startQuestTimer("attack_stop", 0L, frintezza, null);

      _SecondMorph = 1;
      _OnMorph = 1;

      startQuestTimer("stop_pc", 1000L, npc, null);
      startQuestTimer("stop_npc", 1000L, npc, null);
      startQuestTimer("morph_01", 1100L, npc, null);
    }
    else if ((npc.getNpcId() == 29046) && (_SecondMorph == 1) && (_ThirdMorph == 0) && (_OnMorph == 0) && (npc.getCurrentHp() < npc.getMaxHp() * 0.5D) && (GrandBossManager.getInstance().getBossStatus(29045) == 2))
    {
      startQuestTimer("attack_stop", 0L, frintezza, null);

      _ThirdMorph = 1;
      _OnMorph = 1;

      startQuestTimer("stop_pc", 2000L, npc, null);
      startQuestTimer("stop_npc", 2000L, npc, null);
      startQuestTimer("morph_05a", 2000L, npc, null);
      startQuestTimer("morph_05", 2100L, npc, null);
    }
    else if ((npc.getNpcId() == 29047) && (_SecondMorph == 1) && (_ThirdMorph == 1) && (_OnCheck == 0) && (damage >= npc.getCurrentHp()) && (GrandBossManager.getInstance().getBossStatus(29045) == 2))
    {
      _OnCheck = 1;
      startQuestTimer("check_hp", 0L, npc, null);
    }
    else if (((npc.getNpcId() == 29050) || (npc.getNpcId() == 29051)) && (_Bomber == 0))
    {
      if (npc.getCurrentHp() < npc.getMaxHp() * 0.1D)
      {
        if (Rnd.get(100) < 30)
        {
          _Bomber = 1;
          startQuestTimer("bomber", 3000L, npc, null);

          L2Skill skill = SkillTable.getInstance().getInfo(5011, 1);
          if (skill != null) {
            npc.doCast(skill);
          }
        }
      }
    }
    return super.onAttack(npc, attacker, damage, isPet);
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    if (npc.getNpcId() == 29045)
    {
      return null;
    }
    if ((npc.getNpcId() == 29047) && (_OnCheck == 0) && (GrandBossManager.getInstance().getBossStatus(29045) == 2))
    {
      _OnCheck = 1;
      startQuestTimer("stop_pc", 0L, npc, null);
      startQuestTimer("stop_npc", 0L, npc, null);
      startQuestTimer("morph_16", 0L, npc, null);
    }
    else if ((npc.getNpcId() == 29047) && (_OnCheck == 1) && (GrandBossManager.getInstance().getBossStatus(29045) == 2))
    {
      cancelQuestTimers("loc_check");
      cancelQuestTimers("spawn_minion");
      cancelQuestTimers("frintezza_despawn");
      startQuestTimer("clean", 30000L, npc, null);
      startQuestTimer("close", 30000L, npc, null);
      startQuestTimer("room3_del", 60000L, npc, null);
      startQuestTimer("minions_despawn", 60000L, npc, null);
      startQuestTimer("remove_players", 900000L, npc, null);

      GrandBossManager.getInstance().setBossStatus(29045, 3);
      long respawnTime = Config.LIT_FIXINTERVALOFFRINTEZZA + Rnd.get(Config.LIT_RANDOMINTERVALOFFRINTEZZA);
      _log.warning(" - Epic: Frintezza killed: " + time.getTime());
      startQuestTimer("frintezza_unlock", respawnTime, npc, null);

      StatsSet info = GrandBossManager.getInstance().getStatsSet(29045);
      info.set("respawn_time", System.currentTimeMillis() + respawnTime);
      GrandBossManager.getInstance().setStatsSet(29045, info);
    }
    else if (npc.getNpcId() == 18328)
    {
      _KillHallAlarmDevice += 1;
      if (_KillHallAlarmDevice == 4)
      {
        startQuestTimer("room1_del", 100L, npc, null);
        startQuestTimer("room2_spawn", 100L, npc, null);

        DoorTable.getInstance().getDoor(Integer.valueOf(25150042)).openMe();
        DoorTable.getInstance().getDoor(Integer.valueOf(25150043)).openMe();

        for (int i = 25150051; i <= 25150058; i++)
          DoorTable.getInstance().getDoor(Integer.valueOf(i)).openMe();
      }
    }
    else if (npc.getNpcId() == 18339)
    {
      _KillDarkChoirPlayer += 1;
      if (_KillDarkChoirPlayer == 2)
      {
        DoorTable.getInstance().getDoor(Integer.valueOf(25150042)).closeMe();
        DoorTable.getInstance().getDoor(Integer.valueOf(25150043)).closeMe();
        DoorTable.getInstance().getDoor(Integer.valueOf(25150045)).closeMe();
        DoorTable.getInstance().getDoor(Integer.valueOf(25150046)).closeMe();
        int outside = 0;
        for (L2PcInstance room2_pc : _PlayersInside)
        {
          if ((_Zone.isInsideZone(room2_pc)) && (room2_pc.getY() > -86130))
            outside++;
        }
        if (outside == 0)
        {
          startQuestTimer("room2_del", 100L, npc, null);
          startQuestTimer("waiting", 180000L, npc, null);
          cancelQuestTimers("room_final");
        }
        else
        {
          for (int i = 25150061; i <= 25150070; i++) {
            DoorTable.getInstance().getDoor(Integer.valueOf(i)).openMe();
          }
          startQuestTimer("room2_spawn2", 1000L, npc, null);
        }
      }
    }
    else if (npc.getNpcId() == 18334)
    {
      _KillDarkChoirCaptain += 1;
      if (_KillDarkChoirCaptain == 8)
      {
        startQuestTimer("room2_del", 100L, npc, null);

        DoorTable.getInstance().getDoor(Integer.valueOf(25150045)).openMe();
        DoorTable.getInstance().getDoor(Integer.valueOf(25150046)).openMe();

        startQuestTimer("waiting", 180000L, npc, null);
        cancelQuestTimers("room_final");
      }
    }

    return super.onKill(npc, killer, isPet);
  }
}