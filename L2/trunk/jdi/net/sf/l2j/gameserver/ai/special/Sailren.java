package net.sf.l2j.gameserver.ai.special;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.NpcKnownList;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public class Sailren
{
  protected static Logger _log = Logger.getLogger(Sailren.class.getName());
  private static Sailren _instance = new Sailren();

  private final int[][] _SailrenCubeLocation = { { 27734, -6838, -1982, 0 } };

  protected List<L2Spawn> _SailrenCubeSpawn = new FastList();
  protected List<L2NpcInstance> _SailrenCube = new FastList();

  protected List<L2PcInstance> _PlayersInSailrenLair = new FastList();
  protected L2Spawn _VelociraptorSpawn;
  protected L2Spawn _PterosaurSpawn;
  protected L2Spawn _TyrannoSpawn;
  protected L2Spawn _SailrenSapwn;
  protected L2NpcInstance _Velociraptor;
  protected L2NpcInstance _Pterosaur;
  protected L2NpcInstance _Tyranno;
  protected L2NpcInstance _Sailren;
  protected ScheduledFuture<?> _CubeSpawnTask = null;
  protected ScheduledFuture<?> _SailrenSpawnTask = null;
  protected ScheduledFuture<?> _IntervalEndTask = null;
  protected ScheduledFuture<?> _ActivityTimeEndTask = null;
  protected ScheduledFuture<?> _OnPartyAnnihilatedTask = null;
  protected ScheduledFuture<?> _SocialTask = null;
  protected String _ZoneType;
  protected String _QuestName;
  protected boolean _IsAlreadyEnteredOtherParty = false;
  protected StatsSet _StateSet;
  protected int _Alive;
  protected int _BossId = 29065;

  public static Sailren getInstance()
  {
    if (_instance == null) _instance = new Sailren();

    return _instance;
  }

  public void init()
  {
    _PlayersInSailrenLair.clear();
    _IsAlreadyEnteredOtherParty = false;
    _ZoneType = "LairofSailren";
    _QuestName = "sailren";
    _StateSet = GrandBossManager.getInstance().getStatsSet(_BossId);
    _Alive = GrandBossManager.getInstance().getBossStatus(_BossId);
    try
    {
      L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(22218);
      _VelociraptorSpawn = new L2Spawn(template1);
      _VelociraptorSpawn.setLocx(27852);
      _VelociraptorSpawn.setLocy(-5536);
      _VelociraptorSpawn.setLocz(-1983);
      _VelociraptorSpawn.setHeading(44732);
      _VelociraptorSpawn.setAmount(1);
      _VelociraptorSpawn.setRespawnDelay(Config.FWS_ACTIVITYTIMEOFMOBS * 2);
      SpawnTable.getInstance().addNewSpawn(_VelociraptorSpawn, false);

      template1 = NpcTable.getInstance().getTemplate(22199);
      _PterosaurSpawn = new L2Spawn(template1);
      _PterosaurSpawn.setLocx(27852);
      _PterosaurSpawn.setLocy(-5536);
      _PterosaurSpawn.setLocz(-1983);
      _PterosaurSpawn.setHeading(44732);
      _PterosaurSpawn.setAmount(1);
      _PterosaurSpawn.setRespawnDelay(Config.FWS_ACTIVITYTIMEOFMOBS * 2);
      SpawnTable.getInstance().addNewSpawn(_PterosaurSpawn, false);

      template1 = NpcTable.getInstance().getTemplate(22217);
      _TyrannoSpawn = new L2Spawn(template1);
      _TyrannoSpawn.setLocx(27852);
      _TyrannoSpawn.setLocy(-5536);
      _TyrannoSpawn.setLocz(-1983);
      _TyrannoSpawn.setHeading(44732);
      _TyrannoSpawn.setAmount(1);
      _TyrannoSpawn.setRespawnDelay(Config.FWS_ACTIVITYTIMEOFMOBS * 2);
      SpawnTable.getInstance().addNewSpawn(_TyrannoSpawn, false);

      template1 = NpcTable.getInstance().getTemplate(29065);
      _SailrenSapwn = new L2Spawn(template1);
      _SailrenSapwn.setLocx(27810);
      _SailrenSapwn.setLocy(-5655);
      _SailrenSapwn.setLocz(-1983);
      _SailrenSapwn.setHeading(44732);
      _SailrenSapwn.setAmount(1);
      _SailrenSapwn.setRespawnDelay(Config.FWS_ACTIVITYTIMEOFMOBS * 2);
      SpawnTable.getInstance().addNewSpawn(_SailrenSapwn, false);
    }
    catch (Exception e)
    {
      _log.warning(e.getMessage());
    }

    try
    {
      L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(32107);

      for (int i = 0; i < _SailrenCubeLocation.length; i++)
      {
        L2Spawn spawnDat = new L2Spawn(Cube);
        spawnDat.setAmount(1);
        spawnDat.setLocx(_SailrenCubeLocation[i][0]);
        spawnDat.setLocy(_SailrenCubeLocation[i][1]);
        spawnDat.setLocz(_SailrenCubeLocation[i][2]);
        spawnDat.setHeading(_SailrenCubeLocation[i][3]);
        spawnDat.setRespawnDelay(60);
        spawnDat.setLocation(0);
        SpawnTable.getInstance().addNewSpawn(spawnDat, false);
        _SailrenCubeSpawn.add(spawnDat);
      }
    }
    catch (Exception e)
    {
      _log.warning(e.getMessage());
    }

    _log.info("SailrenManager : State of Sailren is " + _Alive + ".");
    if (_Alive != 0) {
      setInetrvalEndTask();
    }
    Date dt = new Date(_StateSet.getLong("respawn_time"));
    _log.info("SailrenManager : Next spawn date of Sailren is " + dt + ".");
    _log.info("SailrenManager : Init SailrenManager.");
  }

  public List<L2PcInstance> getPlayersInLair()
  {
    return _PlayersInSailrenLair;
  }

  public int canIntoSailrenLair(L2PcInstance pc)
  {
    if ((!Config.FWS_ENABLESINGLEPLAYER) && (pc.getParty() == null)) return 4;
    if (_IsAlreadyEnteredOtherParty) return 2;
    if (_Alive == 0) return 0;
    if ((_Alive == 1) || (_Alive != 2)) return 1;
    if (_Alive == 3) return 3;
    return 0;
  }

  public void setSailrenSpawnTask(int NpcId)
  {
    if ((NpcId == 22218) && (_PlayersInSailrenLair.size() >= 1)) return;

    if (_SailrenSpawnTask == null)
    {
      _SailrenSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SailrenSpawn(NpcId), Config.FWS_INTERVALOFNEXTMONSTER);
    }
  }

  public void addPlayerToSailrenLair(L2PcInstance pc)
  {
    if (!_PlayersInSailrenLair.contains(pc)) _PlayersInSailrenLair.add(pc);
  }

  public void entryToSailrenLair(L2PcInstance pc)
  {
    if (canIntoSailrenLair(pc) != 0)
    {
      pc.sendMessage("\u043F\u0457\u0405\u043F\u0457\u0405\u043F\u0457\u0405\u043F\u0457\u0405\u043F\u0457\u0405\u043F\u0457\u0405\u043F\u0457\u0405\u043F\u0457\u0405 \u043F\u0457\u0405\u043F\u0457\u0405\u043F\u0457\u0405\u043F\u0457\u0405\u043F\u0457\u0405\u043F\u0457\u0405\u043F\u0457\u0405\u043F\u0457\u0405");
      _IsAlreadyEnteredOtherParty = false;
      return;
    }

    if (pc.getParty() == null)
    {
      int driftx = Rnd.get(-80, 80);
      int drifty = Rnd.get(-80, 80);
      pc.teleToLocation(27734 + driftx, -6938 + drifty, -1982);
      addPlayerToSailrenLair(pc);
    }
    else
    {
      List members = new FastList();
      for (L2PcInstance mem : pc.getParty().getPartyMembers())
      {
        if ((!mem.isDead()) && (Util.checkIfInRange(700, pc, mem, true)))
        {
          members.add(mem);
        }
      }
      for (L2PcInstance mem : members)
      {
        int driftx = Rnd.get(-80, 80);
        int drifty = Rnd.get(-80, 80);
        mem.teleToLocation(27734 + driftx, -6938 + drifty, -1982);
        addPlayerToSailrenLair(mem);
      }
    }
    _IsAlreadyEnteredOtherParty = true;
  }

  public void checkAnnihilated(L2PcInstance pc)
  {
    if (isPartyAnnihilated(pc))
    {
      _OnPartyAnnihilatedTask = ThreadPoolManager.getInstance().scheduleGeneral(new OnPartyAnnihilatedTask(pc), 5000L);
    }
  }

  public synchronized boolean isPartyAnnihilated(L2PcInstance pc)
  {
    if (pc.getParty() != null)
    {
      for (L2PcInstance mem : pc.getParty().getPartyMembers())
      {
        if ((!mem.isDead()) && (GrandBossManager.getInstance().checkIfInZone("LairofSailren", pc)))
        {
          return false;
        }
      }
      return true;
    }

    return true;
  }

  public void banishesPlayers()
  {
    for (L2PcInstance pc : _PlayersInSailrenLair)
    {
      if (pc.getQuestState("sailren") != null) pc.getQuestState("sailren").exitQuest(true);
      if (GrandBossManager.getInstance().checkIfInZone("LairofSailren", pc))
      {
        int driftX = Rnd.get(-80, 80);
        int driftY = Rnd.get(-80, 80);
        pc.teleToLocation(10468 + driftX, -24569 + driftY, -3650);
      }
    }
    _PlayersInSailrenLair.clear();
    _IsAlreadyEnteredOtherParty = false;
  }

  public void setUnspawn()
  {
    banishesPlayers();

    for (L2NpcInstance cube : _SailrenCube)
    {
      cube.getSpawn().stopRespawn();
      cube.deleteMe();
    }
    _SailrenCube.clear();

    if (_CubeSpawnTask != null)
    {
      _CubeSpawnTask.cancel(true);
      _CubeSpawnTask = null;
    }
    if (_SailrenSpawnTask != null)
    {
      _SailrenSpawnTask.cancel(true);
      _SailrenSpawnTask = null;
    }
    if (_IntervalEndTask != null)
    {
      _IntervalEndTask.cancel(true);
      _IntervalEndTask = null;
    }
    if (_ActivityTimeEndTask != null)
    {
      _ActivityTimeEndTask.cancel(true);
      _ActivityTimeEndTask = null;
    }

    _Velociraptor = null;
    _Pterosaur = null;
    _Tyranno = null;
    _Sailren = null;

    setInetrvalEndTask();
  }

  public void spawnCube()
  {
    for (L2Spawn spawnDat : _SailrenCubeSpawn)
    {
      _SailrenCube.add(spawnDat.doSpawn());
    }
  }

  public void setCubeSpawn()
  {
    _Alive = 2;
    _StateSet.set("respawn_time", Calendar.getInstance().getTimeInMillis() + Rnd.get(Config.FWS_FIXINTERVALOFSAILRENSPAWN, Config.FWS_FIXINTERVALOFSAILRENSPAWN + Config.FWS_RANDOMINTERVALOFSAILRENSPAWN));
    GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
    GrandBossManager.getInstance().setStatsSet(_BossId, _StateSet);
    GrandBossManager.getInstance().save();

    _CubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(), 10000L);

    Date dt = new Date(_StateSet.getLong("respawn_time"));
    _log.info("SailrenManager : Sailren is dead.");
    _log.info("SailrenManager : Next spawn date of Sailren is " + dt + ".");
  }

  public void setInetrvalEndTask()
  {
    if (_Alive != 3)
    {
      _Alive = 3;
      GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
      GrandBossManager.getInstance().save();
    }

    _IntervalEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new IntervalEnd(), GrandBossManager.getInstance().getInterval(_BossId));
    _log.info("SailrenManager : Interval START.");
  }

  protected void updateKnownList(L2NpcInstance boss)
  {
    boss.getKnownList().getKnownPlayers().clear();
    for (L2PcInstance pc : _PlayersInSailrenLair)
    {
      boss.getKnownList().getKnownPlayers().put(Integer.valueOf(pc.getObjectId()), pc);
    }
  }

  protected void doIntervalEnd()
  {
    _PlayersInSailrenLair.clear();
    _Alive = 0;
    GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
    GrandBossManager.getInstance().save();
    _log.info("SailrenManager : Interval END.");
  }

  private class Social
    implements Runnable
  {
    private int _action;
    private L2NpcInstance _npc;

    public Social(L2NpcInstance npc, int actionId)
    {
      _npc = npc;
      _action = actionId;
    }

    public void run()
    {
      updateKnownList(_npc);

      SocialAction sa = new SocialAction(_npc.getObjectId(), _action);
      _npc.broadcastPacket(sa);
    }
  }

  private class OnPartyAnnihilatedTask
    implements Runnable
  {
    L2PcInstance _player;

    public OnPartyAnnihilatedTask(L2PcInstance player)
    {
      _player = player;
    }

    public void run()
    {
      setUnspawn();
    }
  }

  private class IntervalEnd
    implements Runnable
  {
    public IntervalEnd()
    {
    }

    public void run()
    {
      doIntervalEnd();
    }
  }

  private class ActivityTimeEnd
    implements Runnable
  {
    L2NpcInstance _Mob;

    public ActivityTimeEnd(L2NpcInstance npc)
    {
      _Mob = npc;
    }

    public void run()
    {
      if (!_Mob.isDead())
      {
        _Mob.deleteMe();
        _Mob.getSpawn().stopRespawn();
        _Mob = null;
      }

      setUnspawn();
    }
  }

  private class CubeSpawn
    implements Runnable
  {
    public CubeSpawn()
    {
    }

    public void run()
    {
      spawnCube();
    }
  }

  private class SailrenSpawn
    implements Runnable
  {
    int _NpcId;
    L2CharPosition _pos = new L2CharPosition(27628, -6109, -1982, 44732);

    public SailrenSpawn(int NpcId) {
      _NpcId = NpcId;
    }

    public void run()
    {
      switch (_NpcId)
      {
      case 22218:
        _Velociraptor = _VelociraptorSpawn.doSpawn();
        _Velociraptor.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
        if (_SocialTask != null)
        {
          _SocialTask.cancel(true);
          _SocialTask = null;
        }
        _SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new Sailren.Social(Sailren.this, _Velociraptor, 2), 6000L);

        if (_ActivityTimeEndTask != null)
        {
          _ActivityTimeEndTask.cancel(true);
          _ActivityTimeEndTask = null;
        }
        _ActivityTimeEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new Sailren.ActivityTimeEnd(Sailren.this, _Velociraptor), Config.FWS_ACTIVITYTIMEOFMOBS);

        break;
      case 22199:
        _VelociraptorSpawn.stopRespawn();
        _Pterosaur = _PterosaurSpawn.doSpawn();
        _Pterosaur.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
        if (_SocialTask != null)
        {
          _SocialTask.cancel(true);
          _SocialTask = null;
        }
        _SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new Sailren.Social(Sailren.this, _Pterosaur, 2), 6000L);

        if (_ActivityTimeEndTask != null)
        {
          _ActivityTimeEndTask.cancel(true);
          _ActivityTimeEndTask = null;
        }
        _ActivityTimeEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new Sailren.ActivityTimeEnd(Sailren.this, _Pterosaur), Config.FWS_ACTIVITYTIMEOFMOBS);

        break;
      case 22217:
        _PterosaurSpawn.stopRespawn();
        _Tyranno = _TyrannoSpawn.doSpawn();
        _Tyranno.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
        if (_SocialTask != null)
        {
          _SocialTask.cancel(true);
          _SocialTask = null;
        }
        _SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new Sailren.Social(Sailren.this, _Tyranno, 2), 6000L);

        if (_ActivityTimeEndTask != null)
        {
          _ActivityTimeEndTask.cancel(true);
          _ActivityTimeEndTask = null;
        }
        _ActivityTimeEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new Sailren.ActivityTimeEnd(Sailren.this, _Tyranno), Config.FWS_ACTIVITYTIMEOFMOBS);

        break;
      case 29065:
        _TyrannoSpawn.stopRespawn();
        _Sailren = _SailrenSapwn.doSpawn();

        _StateSet.set("respawn_time", Calendar.getInstance().getTimeInMillis() + Rnd.get(Config.FWS_FIXINTERVALOFSAILRENSPAWN, Config.FWS_FIXINTERVALOFSAILRENSPAWN + Config.FWS_RANDOMINTERVALOFSAILRENSPAWN) + Config.FWS_ACTIVITYTIMEOFMOBS);
        _Alive = 1;
        GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
        GrandBossManager.getInstance().setStatsSet(_BossId, _StateSet);
        GrandBossManager.getInstance().save();
        Sailren._log.info("SailrenManager : Spawn Sailren.");

        _Sailren.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
        if (_SocialTask != null)
        {
          _SocialTask.cancel(true);
          _SocialTask = null;
        }
        _SocialTask = ThreadPoolManager.getInstance().scheduleGeneral(new Sailren.Social(Sailren.this, _Sailren, 2), 6000L);

        if (_ActivityTimeEndTask != null)
        {
          _ActivityTimeEndTask.cancel(true);
          _ActivityTimeEndTask = null;
        }
        _ActivityTimeEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new Sailren.ActivityTimeEnd(Sailren.this, _Sailren), Config.FWS_ACTIVITYTIMEOFMOBS);

        break;
      }

      if (_SailrenSpawnTask != null)
      {
        _SailrenSpawnTask.cancel(true);
        _SailrenSpawnTask = null;
      }
    }
  }
}