package scripts.autoevents.basecapture;

import java.io.PrintStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.Config;
import net.sf.l2j.Config.EventReward;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.EventManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.olympiad.Olympiad;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;
import scripts.ai.CaptureBase;
import scripts.autoevents.lasthero.LastHero;
import scripts.autoevents.masspvp.massPvp;

public class BaseCapture
{
  protected static final Logger _log = Logger.getLogger(BaseCapture.class.getName());

  private static final long _arRestart = Config.EBC_ARTIME * 60000L;
  private static final long _regTime = Config.EBC_REGTIME * 60000L;
  private static final long _anTime = Config.EBC_ANNDELAY * 60000L;
  private static final long _tpDelay = Config.EBC_TPDELAY * 60000L;
  private static final long _deathDelay = Config.EBC_DEATHLAY * 1000L;
  private static final long _nextTime = Config.EBC_NEXT * 60000L;

  private static final int _minLvl = Config.EBC_MINLVL;

  private static CaptureBase _base1 = null;
  private static CaptureBase _base2 = null;

  private static final Location _tpLoc1 = Config.EBC_TPLOC1;
  private static final Location _tpLoc2 = Config.EBC_TPLOC2;

  private static FastMap<Integer, FastList<L2PcInstance>> _teams = new FastMap();
  private static FastList<Config.EventReward> _rewards = Config.EBC_REWARDS;
  private static FastList<Location> _locs = new FastList();
  private static FastList<String> _teamNames = new FastList();
  private static ConcurrentLinkedQueue<String> _ips = new ConcurrentLinkedQueue();

  private static EventState _state = EventState.WAIT;
  private static BaseCapture _event;

  public static void init()
  {
    _event = new BaseCapture();
    _event.load();
  }

  public static BaseCapture getEvent()
  {
    return _event;
  }

  public void load()
  {
    _locs.add(_tpLoc1);
    _locs.add(_tpLoc2);

    _teamNames.add(Config.EBC_BASE1NAME);
    _teamNames.add(Config.EBC_BASE2NAME);

    _ips.clear();
    _teams.clear();
    _teams.put(Integer.valueOf(0), new FastList());
    _teams.put(Integer.valueOf(1), new FastList());

    checkTimer();
  }

  public void checkTimer()
  {
    ThreadPoolManager.getInstance().scheduleGeneral(new StartTask(), _arRestart);
    System.out.println("EventManager: Base Capture, start after " + _arRestart / 60000L + " min.");
  }

  private void startEvent()
  {
    _ips.clear();
    _teams.clear();
    _teams.put(Integer.valueOf(0), new FastList());
    _teams.put(Integer.valueOf(1), new FastList());

    _state = EventState.REG;

    announce(Static.EBC_STARTED);
    announce(Static.EBC_REG_FOR_S1.replace("%a%", String.valueOf(_regTime / 60000L + 1L)));
    System.out.println("EventManager: Base Capture, registration opened.");
    ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceTask(), _anTime);
  }

  public void notifyBaseDestroy(int baseId)
  {
    if (_state != EventState.BATTLE) {
      return;
    }
    int winTeam = 0;
    if (baseId == Config.EBC_BASE1ID) {
      winTeam = 1;
    }
    if (_base1 != null) {
      _base1.deleteMe();
    }
    if (_base2 != null) {
      _base2.deleteMe();
    }
    _base1 = null;
    _base2 = null;

    _state = EventState.WAIT;
    announce(Static.EBC_FINISHED);
    announce(Static.EBC_TEAM_S1_WIN.replace("%a%", (CharSequence)_teamNames.get(winTeam)));
    announce(Static.EBC_NEXT_AFTER.replace("%a%", String.valueOf(_nextTime / 60000L)));

    System.out.println("EventManager: Base Capture, finished; team " + (winTeam + 1) + " win.");
    System.out.println("EventManager: Base Capture, next start after " + _nextTime / 60000L + " min.");
    ThreadPoolManager.getInstance().scheduleGeneral(new StartTask(), _nextTime);
    try
    {
      validateWinners(winTeam);
    }
    catch (Exception e)
    {
    }

    prepareNextEvent();
  }

  private void validateWinners(int team)
  {
    FastList players = (FastList)_teams.get(Integer.valueOf(team));
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; )
    {
      player = (L2PcInstance)n.getValue();
      if (player == null) {
        continue;
      }
      k = _rewards.head(); for (FastList.Node endk = _rewards.tail(); (k = k.getNext()) != endk; )
      {
        Config.EventReward reward = (Config.EventReward)k.getValue();
        if (reward == null) {
          continue;
        }
        if (Rnd.get(100) < reward.chance)
          player.addItem("Npc.giveItem", reward.id, reward.count, player, true); 
      }
    }L2PcInstance player;
    FastList.Node k;
  }

  private void prepareNextEvent() {
    L2PcInstance player = null;
    FastMap.Entry e = _teams.head(); for (FastMap.Entry end = _teams.tail(); (e = e.getNext()) != end; )
    {
      FastList players = (FastList)e.getValue();

      n = players.head(); for (FastList.Node endp = players.tail(); (n = n.getNext()) != endp; )
      {
        player = (L2PcInstance)n.getValue();
        if (player == null) {
          continue;
        }
        player.setCurrentCp(player.getMaxCp());
        player.setCurrentHp(player.getMaxHp());
        player.setCurrentMp(player.getMaxMp());
        player.setChannel(1);
        player.broadcastStatusUpdate();
        player.setTeam(0);
        player.setPVPArena(false);
        player.teleToLocationEvent(82737, 148571, -3470);
      }
    }
    FastList.Node n;
    _ips.clear();
    _teams.clear();
    _teams.put(Integer.valueOf(0), new FastList());
    _teams.put(Integer.valueOf(1), new FastList());
  }

  private boolean foundIp(String ip)
  {
    return _ips.contains(ip);
  }

  public void regPlayer(L2PcInstance player)
  {
    if (_state != EventState.REG)
    {
      player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F \u043D\u0430 \u044D\u0432\u0435\u043D\u0442 \u0437\u0430\u043A\u0440\u044B\u0442\u0430.");
      return;
    }
    if (_state == EventState.BATTLE)
    {
      player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0411\u0438\u0442\u0432\u0430 \u0443\u0436\u0435 \u043E\u0431\u044C\u044F\u0432\u043B\u0435\u043D\u0430!");
      return;
    }

    if ((!TvTEvent.isInactive()) && (TvTEvent.isPlayerParticipant(player.getName())))
    {
      player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u043D\u0430 TvT.");
      return;
    }

    if ((Config.MASS_PVP) && (massPvp.getEvent().isReg(player)))
    {
      player.sendMessage("\u0423\u0434\u0430\u0447\u0438 \u043D\u0430 \u0435\u0432\u0435\u043D\u0442\u0435 -\u041C\u0430\u0441\u0441 \u041F\u0412\u041F-");
      return;
    }

    if ((Config.ELH_ENABLE) && (LastHero.getEvent().isRegged(player)))
    {
      player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u0432 -\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439- \u044D\u0432\u0435\u043D\u0442\u0435.");
      return;
    }
    if ((Config.EBC_ENABLE) && (getEvent().isRegged(player)))
    {
      player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0423\u0434\u0430\u0447\u0438 \u043D\u0430 \u0435\u0432\u0435\u043D\u0442\u0435 -\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-");
      return;
    }

    if ((Olympiad.isRegisteredInComp(player)) || (player.isInOlympiadMode()))
    {
      player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u043D\u0430 \u043E\u043B\u0438\u043C\u043F\u0438\u0430\u0434\u0435.");
      return;
    }

    if (player.isCursedWeaponEquiped())
    {
      player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0421 \u043F\u0440\u043E\u043A\u043B\u044F\u0442\u044B\u043C \u043E\u0440\u0443\u0436\u0438\u0435\u043C \u043D\u0435\u043B\u044C\u0437\u044F.");
      return;
    }

    if ((((FastList)_teams.get(Integer.valueOf(0))).size() >= Config.EBC_MAXP) && (((FastList)_teams.get(Integer.valueOf(1))).size() >= Config.EBC_MAXP))
    {
      player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0414\u043E\u0441\u0442\u0438\u0433\u043D\u0443\u0442 \u043F\u0440\u0435\u0434\u0435\u043B \u0438\u0433\u0440\u043E\u043A\u043E\u0432.");
      return;
    }

    if ((((FastList)_teams.get(Integer.valueOf(0))).contains(player)) || (((FastList)_teams.get(Integer.valueOf(1))).contains(player)))
    {
      player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B.");
      return;
    }

    if ((!Config.EVENTS_SAME_IP) && (foundIp(player.getIP())))
    {
      player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0421 \u0432\u0430\u0448\u0435\u0433\u043E IP \u0443\u0436\u0435 \u0435\u0441\u0442\u044C \u0438\u0433\u0440\u043E\u043A.");
      return;
    }

    if (Config.EBC_TICKETID > 0)
    {
      L2ItemInstance coin = player.getInventory().getItemByItemId(Config.EBC_TICKETID);
      if ((coin == null) || (coin.getCount() < Config.EBC_TICKETCOUNT))
      {
        player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0423\u0447\u0430\u0441\u0442\u0438\u0432 \u0432 \u0438\u0432\u0435\u043D\u0442\u0435 \u043F\u043B\u0430\u0442\u043D\u043E\u0435.");
        return;
      }
    }

    int team = 0;
    if (((FastList)_teams.get(Integer.valueOf(0))).size() == ((FastList)_teams.get(Integer.valueOf(1))).size())
      team = Rnd.get(0, 1);
    else if (((FastList)_teams.get(Integer.valueOf(0))).size() > ((FastList)_teams.get(Integer.valueOf(1))).size()) {
      team = 1;
    }
    ((FastList)_teams.get(Integer.valueOf(team))).add(player);
    player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043D\u0430, \u0432\u0430\u0448\u0430 \u043A\u043E\u043C\u0430\u043D\u0434\u0430: <br> " + (String)_teamNames.get(team) + ".");
    if (!Config.EVENTS_SAME_IP)
      _ips.add(player.getIP());
  }

  public void delPlayer(L2PcInstance player)
  {
    if (_state != EventState.REG)
    {
      player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0421\u0435\u0439\u0447\u0430\u0441 \u043D\u0435 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u043E\u043D\u043D\u044B\u0439 \u043F\u0435\u0440\u0438\u043E\u0434.");
      return;
    }

    if ((!((FastList)_teams.get(Integer.valueOf(0))).contains(player)) && (!((FastList)_teams.get(Integer.valueOf(1))).contains(player)))
    {
      player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0412\u044B \u043D\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B.");
      return;
    }

    if (((FastList)_teams.get(Integer.valueOf(0))).contains(player))
      ((FastList)_teams.get(Integer.valueOf(0))).remove(((FastList)_teams.get(Integer.valueOf(0))).indexOf(player));
    else if (((FastList)_teams.get(Integer.valueOf(1))).contains(player)) {
      ((FastList)_teams.get(Integer.valueOf(1))).remove(((FastList)_teams.get(Integer.valueOf(1))).indexOf(player));
    }
    if (!Config.EVENTS_SAME_IP) {
      _ips.remove(player.getIP());
    }
    player.sendHtmlMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-", "\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F \u043E\u0442\u043C\u0435\u043D\u0435\u043D\u0430.");
  }

  public void notifyFail(L2PcInstance player)
  {
    if (_state != EventState.BATTLE) {
      return;
    }
    if (((FastList)_teams.get(Integer.valueOf(0))).contains(player))
    {
      ((FastList)_teams.get(Integer.valueOf(0))).remove(((FastList)_teams.get(Integer.valueOf(0))).indexOf(player));
      player.setXYZ(82737, 148571, -3470);
    }
    else if (((FastList)_teams.get(Integer.valueOf(1))).contains(player))
    {
      ((FastList)_teams.get(Integer.valueOf(1))).remove(((FastList)_teams.get(Integer.valueOf(1))).indexOf(player));
      player.setXYZ(82737, 148571, -3470);
    }
    if (!Config.EVENTS_SAME_IP) {
      _ips.remove(player.getIP());
    }
    player.setChannel(1);
  }

  public boolean isRegged(L2PcInstance player)
  {
    if (_state == EventState.WAIT) {
      return false;
    }
    if (((FastList)_teams.get(Integer.valueOf(0))).contains(player)) {
      return true;
    }
    return ((FastList)_teams.get(Integer.valueOf(1))).contains(player);
  }

  public boolean isInBattle(L2PcInstance player)
  {
    if (_state != EventState.BATTLE) {
      return false;
    }
    if (((FastList)_teams.get(Integer.valueOf(0))).contains(player)) {
      return true;
    }
    return ((FastList)_teams.get(Integer.valueOf(1))).contains(player);
  }

  public boolean isInTeam1(L2PcInstance player)
  {
    if (_state == EventState.WAIT) {
      return false;
    }

    return ((FastList)_teams.get(Integer.valueOf(0))).contains(player);
  }

  public boolean isInTeam2(L2PcInstance player)
  {
    if (_state == EventState.WAIT) {
      return false;
    }

    return ((FastList)_teams.get(Integer.valueOf(1))).contains(player);
  }

  private void announce(String text)
  {
    Announcements.getInstance().announceToAll(text);
  }

  public class DeathTask
    implements Runnable
  {
    public DeathTask()
    {
    }

    public void run()
    {
      L2PcInstance player = null;
      FastMap.Entry e = BaseCapture._teams.head(); for (FastMap.Entry end = BaseCapture._teams.tail(); (e = e.getNext()) != end; )
      {
        teamId = (Integer)e.getKey();
        FastList players = (FastList)e.getValue();
        loc = (Location)BaseCapture._locs.get(teamId.intValue());
        teamId = Integer.valueOf(teamId.intValue() + 1);

        n = players.head(); for (FastList.Node endp = players.tail(); (n = n.getNext()) != endp; )
        {
          player = (L2PcInstance)n.getValue();
          if ((player == null) || 
            (!player.isDead())) {
            continue;
          }
          player.doRevive();
          player.setCurrentCp(player.getMaxCp());
          player.setCurrentHp(player.getMaxHp());
          player.setCurrentMp(player.getMaxMp());
          player.stopAllEffects();
          player.broadcastStatusUpdate();
          player.setTeam(teamId.intValue());
          player.teleToLocationEvent(loc.x + Rnd.get(200), loc.y + Rnd.get(200), loc.z);
          player.setPVPArena(true);
        }
      }
      Integer teamId;
      Location loc;
      FastList.Node n;
      ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(BaseCapture.this), BaseCapture._deathDelay);
    }
  }

  public class StartBattle
    implements Runnable
  {
    public StartBattle()
    {
    }

    public void run()
    {
      BaseCapture.access$802((CaptureBase)EventManager.getInstance().doSpawn(Config.EBC_BASE1ID, (Location)BaseCapture._locs.get(0), 0L));
      BaseCapture._base1.setTitle("Command Post");
      BaseCapture._base1.setName(Config.EBC_BASE1NAME);
      BaseCapture.access$1002((CaptureBase)EventManager.getInstance().doSpawn(Config.EBC_BASE2ID, (Location)BaseCapture._locs.get(1), 0L));
      BaseCapture._base1.setTitle("Command Post");
      BaseCapture._base1.setName(Config.EBC_BASE2NAME);

      L2PcInstance player = null;
      FastMap.Entry e = BaseCapture._teams.head(); for (FastMap.Entry end = BaseCapture._teams.tail(); (e = e.getNext()) != end; )
      {
        teamId = (Integer)e.getKey();
        FastList players = (FastList)e.getValue();
        loc = (Location)BaseCapture._locs.get(teamId.intValue());
        teamId = Integer.valueOf(teamId.intValue() + 1);

        n = players.head(); for (FastList.Node endp = players.tail(); (n = n.getNext()) != endp; )
        {
          player = (L2PcInstance)n.getValue();
          if (player == null) {
            continue;
          }
          if (Config.FORBIDDEN_EVENT_ITMES)
          {
            for (L2ItemInstance item : player.getInventory().getItems())
            {
              if (item == null) {
                continue;
              }
              if (item.notForOly()) {
                player.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
              }
            }
          }
          player.setChannel(4);
          player.teleToLocationEvent(loc.x + Rnd.get(100), loc.y + Rnd.get(100), loc.z);
          player.stopAllEffects();
          player.setCurrentCp(player.getMaxCp());
          player.setCurrentHp(player.getMaxHp());
          player.setCurrentMp(player.getMaxMp());
          player.setTeam(teamId.intValue());
          player.setPVPArena(true);
        }
      }
      Integer teamId;
      Location loc;
      FastList.Node n;
      ThreadPoolManager.getInstance().scheduleGeneral(new BaseCapture.DeathTask(BaseCapture.this), BaseCapture._deathDelay);
      System.out.println("EventManager: Base Capture, battle started.");
    }
  }

  public class AnnounceTask
    implements Runnable
  {
    public AnnounceTask()
    {
    }

    public void run()
    {
      if (BaseCapture._state != BaseCapture.EventState.REG) {
        return;
      }
      long regMin = BaseCapture._regTime;
      for (int i = 0; i < BaseCapture._regTime; i = (int)(i + BaseCapture._anTime))
      {
        try
        {
          regMin -= BaseCapture._anTime;
          BaseCapture.this.announce(Static.EBC_STARTED);
          BaseCapture.this.announce(Static.EBC_REG_LOST_S1.replace("%a%", String.valueOf(regMin / 60000L + 1L)));
          if ((((FastList)BaseCapture._teams.get(Integer.valueOf(0))).isEmpty()) || (((FastList)BaseCapture._teams.get(Integer.valueOf(1))).isEmpty())) {
            BaseCapture.this.announce(Static.EBC_NO_PLAYESR_YET);
          }
          else {
            String announs = Static.EBC_PLAYER_TEAMS.replace("%a%", String.valueOf(((FastList)BaseCapture._teams.get(Integer.valueOf(0))).size()));
            announs = announs.replace("%b%", Config.EBC_BASE1NAME);
            announs = announs.replace("%c%", String.valueOf(((FastList)BaseCapture._teams.get(Integer.valueOf(1))).size()));
            announs = announs.replace("%d%", Config.EBC_BASE2NAME);
            BaseCapture.this.announce(announs);
          }
          Thread.sleep(BaseCapture._anTime);
        }
        catch (InterruptedException e)
        {
        }
      }
      BaseCapture.this.announce(Static.EBC_REG_CLOSED);
      BaseCapture.access$002(BaseCapture.EventState.BATTLE);

      if ((((FastList)BaseCapture._teams.get(Integer.valueOf(0))).size() < Config.EBC_MINP) || (((FastList)BaseCapture._teams.get(Integer.valueOf(1))).size() < Config.EBC_MINP))
      {
        BaseCapture.this.announce(Static.EBC_REG_CLOSED);
        BaseCapture.this.announce(Static.EBC_NEXT_AFTER.replace("%a%", String.valueOf(BaseCapture._nextTime / 60000L)));
        System.out.println("EventManager: Base Capture, canceled: no players.");
        System.out.println("EventManager: Base Capture, next start after " + BaseCapture._nextTime / 60000L + " min.");
        BaseCapture.access$002(BaseCapture.EventState.WAIT);
        ThreadPoolManager.getInstance().scheduleGeneral(new BaseCapture.StartTask(BaseCapture.this), BaseCapture._nextTime);
        return;
      }
      BaseCapture.this.announce(Static.EBC_BATTLE_STRTED_AFTER.replace("%a%", String.valueOf(BaseCapture._tpDelay / 60000L)));
      System.out.println("EventManager: Base Capture, battle start after " + BaseCapture._tpDelay / 60000L + " min.");
      ThreadPoolManager.getInstance().scheduleGeneral(new BaseCapture.StartBattle(BaseCapture.this), BaseCapture._tpDelay);
    }
  }

  public class StartTask
    implements Runnable
  {
    public StartTask()
    {
    }

    public void run()
    {
      if (BaseCapture._state == BaseCapture.EventState.WAIT)
        BaseCapture.this.startEvent();
    }
  }

  static enum EventState
  {
    WAIT, 
    REG, 
    BATTLE;
  }
}