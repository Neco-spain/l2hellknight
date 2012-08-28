package scripts.autoevents.masspvp;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.HeroSkillTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.EventManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;
import scripts.autoevents.basecapture.BaseCapture;
import scripts.autoevents.lasthero.LastHero;

public class massPvp
{
  private static EventManager _event = EventManager.getInstance();
  private ScheduledFuture<?> _autoStart = null;
  private ScheduledFuture<?> _cycleStart = null;
  private static long _arRestart = Config.MPVP_RTIME * 60000L;
  private static long _regTime = Config.MPVP_REG * 60000L;
  private static long _anTime = Config.MPVP_ANC * 60000L;
  private static long _tpTime = Config.MPVP_TP * 60000L;
  private static long _prTime = Config.MPVP_PR * 1000L;

  private static long _maxTime = Config.MPVP_MAX * 60000L;
  private static long _nextTime = Config.MPVP_NEXT * 24L * 60000L;
  private int _curCycle = -1;
  private static Location _npcLoc = Config.MPVP_NPCLOC;
  private static Location _tpLoc = Config.MPVP_TPLOC;
  private static Location _clLoc = Config.MPVP_CLOC;
  private static Location _winLoc = Config.MPVP_WLOC;
  private boolean _active = false;
  private boolean _safe = true;
  private boolean _reg = false;
  private FastList<L2PcInstance> _curent = new FastList();
  private FastList<L2PcInstance> _next = new FastList();
  private FastList<L2PcInstance> _winners = new FastList();
  private static ConcurrentLinkedQueue<String> _ips = new ConcurrentLinkedQueue();
  private String _winName = "d";
  private static massPvp _instance;
  private long _lastt = 0L;

  public static massPvp getEvent()
  {
    if (_instance == null) {
      _instance = new massPvp();
    }
    return _instance;
  }

  public void load() {
    checkTimer();
  }

  public void checkTimer()
  {
    long nextStart = _event.GetDBValue("massPvp", "nextStart") - System.currentTimeMillis();
    if (nextStart < _arRestart) {
      nextStart = _arRestart;
    }

    _autoStart = ThreadPoolManager.getInstance().scheduleGeneral(new StartTask(), nextStart);
    System.out.println("EventManager: MassPvP, start after " + nextStart / 60000L + " min.");
  }

  protected boolean haveWinner()
  {
    return _curent.size() == 1;
  }

  private void startEvent()
  {
    _active = true;
    _reg = true;

    announce(Static.MPVP_STARTED);
    announce(Static.MPVP_REG_FOR.replaceAll("%m%", String.valueOf(_regTime / 60000L)));
    ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceTask(), _anTime);

    _curent = new FastList();
    _next = new FastList();
    _winners = new FastList();
    _winners.clear();
    _ips.clear();

    _cycleStart = ThreadPoolManager.getInstance().scheduleGeneral(new CycleTask(1), _regTime);
  }

  public void startScript() {
    if (_autoStart != null) {
      _autoStart.cancel(true);
    }

    startEvent();
  }

  public void stopScript(L2PcInstance player) {
    if ((!_active) || (_autoStart != null)) {
      player.sendHtmlMessage("MassPvP", "\u041D\u0435 \u0437\u0430\u043F\u0443\u0449\u0435\u043D.");
      return;
    }

    announce(Static.MPVP_ADMIN_CANCEL);

    FastList.Node n = _curent.head(); for (FastList.Node end = _curent.tail(); (n = n.getNext()) != end; ) {
      L2PcInstance gamer = (L2PcInstance)n.getValue();
      if (gamer == null) {
        _curent.remove(gamer);
        continue;
      }

      if (gamer.isDead()) {
        gamer.doRevive();
      }

      gamer.setChannel(1);
      gamer.setTeam(0);
      gamer.setEventWait(false);
      gamer.setCurrentCp(gamer.getMaxCp());
      gamer.setCurrentHp(gamer.getMaxHp());
      gamer.setCurrentMp(gamer.getMaxMp());
      gamer.setInsideZone(1, false);
      gamer.sendPacket(SystemMessage.id(SystemMessageId.LEFT_COMBAT_ZONE));
      gamer.teleToLocationEvent(_tpLoc.x + Rnd.get(300), _tpLoc.y + Rnd.get(300), _tpLoc.z);
      onExit(gamer);
    }

    _autoStart = ThreadPoolManager.getInstance().scheduleGeneral(new StartTask(), _nextTime);

    _event.SetDBValue("massPvp", "nextStart", "" + (System.currentTimeMillis() + _nextTime));
    _active = false;
    _reg = false;
    _safe = true;
    _curent.clear();
    _winners.clear();
    _next.clear();
    _ips.clear();
    _curCycle = -1;
  }

  private void announce(String text) {
    Announcements.getInstance().announceToAll(text);
  }

  private synchronized void anWinner(L2PcInstance player, boolean cycle)
  {
    if (!_active) {
      return;
    }

    if (System.currentTimeMillis() - _lastt < 11000L) {
      if ((cycle) && (_curent.contains(player))) {
        _curent.remove(player);
        _next.add(player);
        return;
      }

      if ((!cycle) && (_winners.contains(player))) {
        _winners.remove(player);
        return;
      }
      return;
    }
    _lastt = System.currentTimeMillis();

    if (cycle) {
      _winners.add(player);
      announce(Static.MPVP_ROUND_WINNER.replaceAll("%player%", player.getName()));
      player.giveItem(Config.MPVP_CREW, Config.MPVP_CREWC);
      player.sendCritMessage("\u041E\u0441\u0442\u0430\u0432\u0430\u0439\u0442\u0435\u0441\u044C \u0432 \u0438\u0433\u0440\u0435 \u0438 \u0436\u0434\u0438\u0442\u0435 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043D\u0438\u044F \u0432\u0441\u0435\u0445 \u0440\u0430\u0443\u043D\u0434\u043E\u0432.");
      player.sendCritMessage("\u0412\u0430\u0441 \u0436\u0434\u0435\u0442 \u0444\u0438\u043D\u0430\u043B\u044C\u043D\u0430\u044F \u0431\u0438\u0442\u0432\u0430!");
    } else {
      announce(Static.MPVP_ENDED);
      announce(Static.MPVP_WINNER_IS.replaceAll("%player%", player.getName()));
      announce(Static.MPVP_GRATS_WINNER);
      player.giveItem(Config.MPVP_EREW, Config.MPVP_EREWC);
      _winName = player.getName();
      _active = false;
      _curCycle = -1;
    }

    player.setChannel(1);
    player.setTeam(0);
    player.setCurrentCp(player.getMaxCp());
    player.setCurrentHp(player.getMaxHp());
    player.setCurrentMp(player.getMaxMp());
    player.setPVPArena(false);
    player.sendPacket(SystemMessage.id(SystemMessageId.LEFT_COMBAT_ZONE));
    player.teleToLocationEvent(_tpLoc.x + Rnd.get(300), _tpLoc.y + Rnd.get(300), _tpLoc.z);
  }

  private boolean foundIp(String ip) {
    return _ips.contains(ip);
  }

  public void regPlayer(L2PcInstance player) {
    if (!_active) {
      player.sendHtmlMessage("\u041C\u0430\u0441\u0441 \u041F\u0412\u041F", "\u041D\u0435 \u0437\u0430\u043F\u0443\u0449\u0435\u043D!");
      return;
    }
    if (isReg(player)) {
      player.sendHtmlMessage("\u041C\u0430\u0441\u0441 \u041F\u0412\u041F", "\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B!");
      return;
    }
    if (!_reg) {
      player.sendHtmlMessage("\u041C\u0430\u0441\u0441 \u041F\u0412\u041F", "\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F \u043E\u043A\u043E\u043D\u0447\u0435\u043D\u0430!");
      return;
    }
    if (_curent.size() > Config.MPVP_MAXP) {
      player.sendHtmlMessage("\u041C\u0430\u0441\u0441 \u041F\u0412\u041F", "\u0414\u043E\u0441\u0442\u0438\u0433\u043D\u0443\u0442 \u043F\u0440\u0435\u0434\u0435\u043B \u0443\u0447\u0430\u0441\u0442\u043D\u0438\u043A\u043E\u0432: " + Config.MPVP_MAXP);
      return;
    }
    if ((Config.MPVP_NOBL) && (!player.isNoble())) {
      player.sendHtmlMessage("\u041C\u0430\u0441\u0441 \u041F\u0412\u041F", "\u0422\u043E\u043B\u044C\u043A\u043E \u0434\u043B\u044F \u043D\u043E\u0431\u043B\u0435\u0441\u0441\u043E\u0432");
      return;
    }
    if (player.getLevel() < Config.MPVP_LVL) {
      player.sendHtmlMessage("\u041C\u0430\u0441\u0441 \u041F\u0412\u041F", "\u041C\u0438\u043D\u0438\u043C\u0430\u043B\u044C\u043D\u044B\u0439 \u0443\u0440\u043E\u0432\u0435\u043D\u044C \u0434\u043B\u044F \u0443\u0447\u0430\u0441\u0442\u0438\u044F: " + Config.MPVP_LVL);
      return;
    }
    if ((Config.ELH_ENABLE) && (LastHero.getEvent().isRegged(player))) {
      player.sendHtmlMessage("\u041C\u0430\u0441\u0441 \u041F\u0412\u041F", "\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u0432 \u0435\u0432\u0435\u043D\u0442\u0435 -\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-");
      return;
    }
    if ((Config.EBC_ENABLE) && (BaseCapture.getEvent().isRegged(player))) {
      player.sendHtmlMessage("\u041C\u0430\u0441\u0441 \u041F\u0412\u041F", "\u0423\u0434\u0430\u0447\u0438 \u043D\u0430 \u0435\u0432\u0435\u043D\u0442\u0435 -\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-");
      return;
    }
    if ((Olympiad.isRegisteredInComp(player)) || (player.isInOlympiadMode())) {
      player.sendHtmlMessage("\u041C\u0430\u0441\u0441 \u041F\u0412\u041F", "\u0423\u0434\u0430\u0447\u0438 \u043D\u0430 \u043E\u043B\u0438\u043C\u043F\u0435");
      return;
    }if ((!TvTEvent.isInactive()) && (TvTEvent.isPlayerParticipant(player.getName()))) {
      player.sendHtmlMessage("\u041C\u0430\u0441\u0441 \u041F\u0412\u041F", "\u0423\u0434\u0430\u0447\u0438 \u043D\u0430 \u0442\u0432\u0442");
      return;
    }
    if ((!Config.EVENTS_SAME_IP) && (foundIp(player.getIP()))) {
      player.sendHtmlMessage("-\u041C\u0430\u0441\u0441 \u041F\u0412\u041F-", "\u0421 \u0432\u0430\u0448\u0435\u0433\u043E IP \u0443\u0436\u0435 \u0435\u0441\u0442\u044C \u0438\u0433\u0440\u043E\u043A.");
      return;
    }
    player.sendHtmlMessage("\u041C\u0430\u0441\u0441 \u041F\u0412\u041F", "\u0412\u044B \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043B\u0438\u0441\u044C \u043D\u0430 -\u041C\u0430\u0441\u0441 \u041F\u0412\u041F-");
    _curent.add(player);
    if (!Config.EVENTS_SAME_IP)
      _ips.add(player.getIP());
  }

  public boolean isReg(L2PcInstance player)
  {
    if (!_active) {
      return false;
    }

    return (_curent.contains(player)) || (_next.contains(player)) || (_winners.contains(player));
  }

  public boolean isRegAndBattle(L2PcInstance player)
  {
    if (!_active) {
      return false;
    }

    return (!_safe) && (_curent.contains(player));
  }

  public boolean isOpenReg()
  {
    if (!_active) {
      return false;
    }

    return _reg;
  }

  public boolean isActive() {
    return _active;
  }

  public void doDie(L2PcInstance player, L2Character killer)
  {
    if (_curCycle == Config.MPVP_MAXC)
      player.sendCritMessage("\u0412\u044B \u043F\u0440\u043E\u0438\u0433\u0440\u0430\u043B\u0438...");
    else {
      player.sendCritMessage("\u041F\u043E\u043F\u044B\u0442\u0430\u0439\u0442\u0435 \u0441\u0443\u0434\u044C\u0431\u0443 \u0432 \u0441\u043B\u0435\u0434\u0443\u044E\u0449\u0435\u043C \u0440\u0430\u0443\u043D\u0434\u0435!");
    }

    if (_curent.contains(player)) {
      _curent.remove(player);
      _next.add(player);
    }
    try
    {
      player.teleToLocationEvent(_tpLoc.x + Rnd.get(300), _tpLoc.y + Rnd.get(300), _tpLoc.z);
    }
    catch (Exception e) {
    }
    player.setChannel(1);
    player.doRevive();
    player.setTeam(0);
    player.setCurrentHp(player.getMaxHp());
    player.setCurrentMp(player.getMaxMp());
    player.setCurrentCp(player.getMaxCp());
  }

  public void onExit(L2PcInstance player)
  {
    if (!_active) {
      return;
    }

    if (_curent.contains(player)) {
      _curent.remove(player);
    }

    if (_next.contains(player)) {
      _next.remove(player);
    }

    if (!Config.EVENTS_SAME_IP) {
      _ips.remove(player.getIP());
    }

    player.setChannel(1);
  }

  public int getRound() {
    return _curCycle;
  }

  public FastList<L2PcInstance> getWinners() {
    return _winners;
  }

  public String getWinner() {
    return _winName;
  }

  public class StopFight
    implements Runnable
  {
    private int _cycle;
    private boolean _last;

    public StopFight(int cycle, boolean last)
    {
      _cycle = cycle;
      _last = last;
    }

    public void run() {
      if (!_last)
        for (int i = 5; i > 0; i--) {
          String round_end_for = Static.MPVP_ROUND_END_FOR.replaceAll("%s%", String.valueOf(i));
          massPvp.this.announce(round_end_for.replaceAll("%r%", String.valueOf(_cycle)));
          try {
            Thread.sleep(1000L);
          }
          catch (InterruptedException e) {
          }
        }
      massPvp.access$1302(massPvp.this, true);
      boolean round = true;
      if (_cycle < Config.MPVP_MAXC) {
        massPvp.this.announce(Static.MPVP_ROUND_ENDED.replaceAll("%r%", String.valueOf(_cycle)));
      } else {
        round = false;
        massPvp.this.announce(Static.MPVP_FINAL_ENDED);
      }
      FastList.Node n;
      if (_curent.size() == 1) {
        L2PcInstance player = (L2PcInstance)_curent.getFirst();
        if (player != null)
          massPvp.this.anWinner(player, round);
      }
      else {
        L2PcInstance wplayer = (L2PcInstance)_curent.get(Rnd.get(_curent.size() - 1));
        if (wplayer != null) {
          _curent.remove(wplayer);
          massPvp.this.anWinner(wplayer, round);
        }

        n = _curent.head(); for (FastList.Node end = _curent.tail(); (n = n.getNext()) != end; ) {
          L2PcInstance player = (L2PcInstance)n.getValue();
          if (player == null) {
            _curent.remove(player);
            continue;
          }
          _curent.remove(player);
          _next.add(player);

          player.setChannel(1);
          player.setTeam(0);
          player.setCurrentCp(player.getMaxCp());
          player.setCurrentHp(player.getMaxHp());
          player.setCurrentMp(player.getMaxMp());
          player.setPVPArena(false);
          player.sendPacket(SystemMessage.id(SystemMessageId.LEFT_COMBAT_ZONE));
          player.teleToLocationEvent(massPvp._tpLoc.x + Rnd.get(300), massPvp._tpLoc.y + Rnd.get(300), massPvp._tpLoc.z);
        }
      }

      _curent.clear();

      if (round) {
        _curent.addAll(_next);
        _next.clear();
        _cycle += 1;
        if (_curent.size() <= 1) {
          ThreadPoolManager.getInstance().scheduleGeneral(new massPvp.CycleTask(massPvp.this, Config.MPVP_MAXC), 60000L);
          L2PcInstance player = (L2PcInstance)_curent.get(0);
          if (player != null) {
            player.setChannel(1);
            player.sendCritMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u0443\u0447\u0430\u0441\u0442\u043D\u0438\u043A\u043E\u0432 \u0434\u043B\u044F \u0441\u043B\u0435\u0434\u0443\u044E\u0449\u0435\u0433\u043E \u0440\u0430\u0443\u043D\u0434\u0430.");
            player.sendCritMessage("\u041F\u043E\u043F\u044B\u0442\u0430\u0439\u0442\u0435 \u0441\u0443\u0434\u044C\u0431\u0443 \u0432 \u0441\u043B\u0435\u0434\u0443\u044E\u0449\u0435\u043C \u0442\u0443\u0440\u043D\u0438\u0440\u0435.");
          }
          massPvp.this.announce(Static.MPVP_GO_FINAL);
        } else {
          ThreadPoolManager.getInstance().scheduleGeneral(new massPvp.CycleTask(massPvp.this, _cycle), 60000L);
        }
      } else {
        massPvp.access$902(massPvp.this, ThreadPoolManager.getInstance().scheduleGeneral(new massPvp.StartTask(massPvp.this), massPvp._nextTime));

        massPvp._event.SetDBValue("massPvp", "nextStart", "" + (System.currentTimeMillis() + massPvp._nextTime));
        massPvp.access$002(massPvp.this, false);
        massPvp.access$202(massPvp.this, false);
        massPvp.access$1302(massPvp.this, true);
        _curent.clear();

        _next.clear();
        massPvp.access$602(massPvp.this, -1);
      }
    }
  }

  public class StartFight
    implements Runnable
  {
    private int _cycle;

    public StartFight(int cycle)
    {
      _cycle = cycle;
    }

    public void run() {
      for (int i = 5; i > 0; i--) {
        massPvp.this.announce(Static.MPVP_BATTLE_BEGIN_FOR.replaceAll("%s%", String.valueOf(i)));
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException e) {
        }
      }
      massPvp.access$1302(massPvp.this, false);

      FastList.Node n = _curent.head(); for (FastList.Node end = _curent.tail(); (n = n.getNext()) != end; ) {
        L2PcInstance player = (L2PcInstance)n.getValue();
        if (player == null) {
          _curent.remove(player);
          continue;
        }
        player.setChannel(7);
        player.setCurrentCp(player.getMaxCp());
        player.setCurrentHp(player.getMaxHp());
        player.setCurrentMp(player.getMaxMp());
        player.setTeam(2);
        player.setEventWait(false);

        player.setPVPArena(true);
        player.sendPacket(SystemMessage.id(SystemMessageId.ENTERED_COMBAT_ZONE));
        player.broadcastUserInfo();
      }
      if (_cycle < Config.MPVP_MAXC)
        massPvp.this.announce(Static.MPVP_FIGHT);
      else {
        massPvp.this.announce(Static.MPVP_FINAL_FIGHT);
      }

      massPvp.access$1602(massPvp.this, ThreadPoolManager.getInstance().scheduleGeneral(new massPvp.StopFight(massPvp.this, _cycle, false), massPvp._maxTime));

      for (int i = 0; i < massPvp._maxTime; i += 10000) {
        try {
          Thread.sleep(10000L);
          if (haveWinner())
            break;
        }
        catch (InterruptedException e)
        {
        }
      }
      if (_active) {
        _cycleStart.cancel(true);
        ThreadPoolManager.getInstance().scheduleGeneral(new massPvp.StopFight(massPvp.this, _cycle, true), 4000L);
      }
    }
  }

  public class StartTeleport
    implements Runnable
  {
    private int _cycle;

    public StartTeleport(int cycle)
    {
      _cycle = cycle;
    }

    public void run() {
      massPvp.access$1302(massPvp.this, true);

      FastList.Node n = _curent.head(); for (FastList.Node end = _curent.tail(); (n = n.getNext()) != end; ) {
        L2PcInstance player = (L2PcInstance)n.getValue();
        if (player == null)
        {
          continue;
        }
        if (Config.FORBIDDEN_EVENT_ITMES)
        {
          for (L2ItemInstance item : player.getInventory().getItems()) {
            if (item == null)
            {
              continue;
            }
            if (item.notForOly()) {
              player.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
            }
          }
        }

        player.setChannel(7);
        player.teleToLocationEvent(massPvp._clLoc.x + Rnd.get(300), massPvp._clLoc.y + Rnd.get(300), massPvp._clLoc.z);
        player.setCurrentCp(player.getMaxCp());
        player.setCurrentHp(player.getMaxHp());
        player.setCurrentMp(player.getMaxMp());
        player.setEventWait(true);
      }

      try
      {
        Thread.sleep(2000L);
      }
      catch (InterruptedException e) {
      }
      massPvp.this.announce(Static.MPVP_BATTLA_PREPARE_FOR.replaceAll("%s%", String.valueOf(massPvp._prTime / 1000L)));

      FastList.Node n = _curent.head(); for (FastList.Node end = _curent.tail(); (n = n.getNext()) != end; ) {
        L2PcInstance player = (L2PcInstance)n.getValue();
        if (player == null)
        {
          continue;
        }

        if (player.getClan() != null) {
          for (L2Skill skill : player.getClan().getAllSkills()) {
            player.removeSkill(skill, false);
          }

        }

        if (player.isCastingNow()) {
          player.abortCast();
        }

        if (player.isHero()) {
          for (L2Skill skill : HeroSkillTable.getHeroSkills()) {
            player.removeSkill(skill, false);
          }

        }

        if (player.getPet() != null) {
          L2Summon summon = player.getPet();
          summon.stopAllEffects();

          if (summon.isPet()) {
            summon.unSummon(player);
          }
        }

        if (player.getCubics() != null) {
          for (L2CubicInstance cubic : player.getCubics().values()) {
            cubic.stopAction();
            player.delCubic(cubic.getId());
          }
          player.getCubics().clear();
        }

        if (player.getParty() != null) {
          player.getParty().removePartyMember(player);
        }

        player.sendSkillList();

        player.setCurrentCp(player.getMaxCp());
        player.setCurrentHp(player.getMaxHp());
        player.setCurrentMp(player.getMaxMp());
        SkillTable.getInstance().getInfo(1204, 2).getEffects(player, player);
        if (!player.isMageClass())
          SkillTable.getInstance().getInfo(1086, 2).getEffects(player, player);
        else {
          SkillTable.getInstance().getInfo(1085, 3).getEffects(player, player);
        }
        player.broadcastUserInfo();
      }
      ThreadPoolManager.getInstance().scheduleGeneral(new massPvp.StartFight(massPvp.this, _cycle), massPvp._prTime);
    }
  }

  public class CycleTask
    implements Runnable
  {
    private int _cycle;

    public CycleTask(int cycle)
    {
      _cycle = cycle;
    }

    public void run() {
      massPvp.access$202(massPvp.this, false);
      massPvp.access$602(massPvp.this, _cycle);
      if (_cycle == 1) {
        if (_curent.size() < 2) {
          _curent.clear();
          massPvp._ips.clear();
          _autoStart.cancel(true);
          massPvp.access$902(massPvp.this, ThreadPoolManager.getInstance().scheduleGeneral(new massPvp.StartTask(massPvp.this), massPvp._arRestart));
          massPvp.this.announce(Static.MPVP_CANCELED_NO_PLAYERS);
          return;
        }
        massPvp.this.announce(Static.MPVP_REG_CLOSED);
        massPvp.this.announce(Static.MPVP_TELE_ARENA_FOR.replaceAll("%m%", String.valueOf(massPvp._tpTime / 60000L)));

        ThreadPoolManager.getInstance().scheduleGeneral(new massPvp.StartTeleport(massPvp.this, _cycle), massPvp._tpTime);
        return;
      }

      if (_cycle < Config.MPVP_MAXC) {
        massPvp.this.announce(Static.MPVP_BEGIN_ROUND.replaceAll("%r%", String.valueOf(_cycle)));
      } else {
        _curent.clear();
        _curent.addAll(_winners);
        massPvp.this.announce(Static.MPVP_FINAL_ROUND);
      }
      massPvp.this.announce(Static.MPVP_TELE_ARENA_FOR.replaceAll("%m%", String.valueOf(massPvp._tpTime / 60000L)));
      ThreadPoolManager.getInstance().scheduleGeneral(new massPvp.StartTeleport(massPvp.this, _cycle), massPvp._tpTime);
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
      if (!_reg) {
        return;
      }

      long regMin = massPvp._regTime;

      for (int i = 0; i < massPvp._regTime; i = (int)(i + massPvp._anTime))
        try {
          if (!_reg)
          {
            break;
          }
          regMin -= massPvp._anTime;
          massPvp.this.announce(Static.MPVP_STARTED);
          massPvp.this.announce(Static.MPVP_REG_FOR.replaceAll("%m%", String.valueOf(regMin / 60000L)));
          Thread.sleep(massPvp._anTime);
        }
        catch (InterruptedException e)
        {
        }
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
      if (!_active)
        massPvp.this.startEvent();
    }
  }
}