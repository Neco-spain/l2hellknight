package scripts.autoevents.lasthero;

import java.io.PrintStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.Config.EventReward;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.olympiad.Olympiad;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;
import scripts.autoevents.basecapture.BaseCapture;
import scripts.autoevents.masspvp.massPvp;

public class LastHero
{
  protected static final Logger _log = Logger.getLogger(LastHero.class.getName());
  private static final long _arRestart = Config.ELH_ARTIME * 60000L;
  private static final long _regTime = Config.ELH_REGTIME * 60000L;
  private static final long _anTime = Config.ELH_ANNDELAY * 60000L;
  private static final long _tpDelay = Config.ELH_TPDELAY * 60000L;
  private static final long _nextTime = Config.ELH_NEXT * 60000L;
  private static final int _minPlayers = Config.ELH_MINP;
  private static final int _maxPlayers = Config.ELH_MAXP;
  private static Location _tpLoc = Config.ELH_TPLOC;
  private static final int _ticketId = Config.ELH_TICKETID;
  private static final int _ticketCount = Config.ELH_TICKETCOUNT;
  private static FastList<L2PcInstance> _players = new FastList();
  private static FastList<Config.EventReward> _rewards = Config.ELH_REWARDS;
  private static ConcurrentLinkedQueue<String> _ips = new ConcurrentLinkedQueue();

  private static EventState _state = EventState.WAIT;
  private static LastHero _event;

  public static void init()
  {
    _event = new LastHero();
    _event.load();
  }

  public static LastHero getEvent() {
    return _event;
  }

  public void load() {
    checkTimer();
  }

  public void checkTimer() {
    ThreadPoolManager.getInstance().scheduleGeneral(new StartTask(), _arRestart);
    System.out.println("EventManager: Last Hero, start after " + _arRestart / 60000L + " min.");
  }

  private void startEvent()
  {
    _state = EventState.REG;

    announce(Static.LH_STARTED);
    announce(Static.LH_REG_FOR_S1.replace("%a%", String.valueOf(_regTime / 60000L + 1L)));
    System.out.println("EventManager: Last Hero, registration opened.");

    _ips.clear();
    _players.clear();
    ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceTask(), _anTime);
  }

  public void announceWinner(L2PcInstance player)
  {
    player.setChannel(1);
    _state = EventState.WAIT;
    announce(Static.LH_DONE);
    announce(Static.LH_WINNER.replace("%a%", player.getName()));
    announce(Static.LH_NEXT_AFTER.replace("%a%", String.valueOf(_nextTime / 60000L)));
    System.out.println("EventManager: Last Hero, finished; palyer " + player.getName() + " win.");
    System.out.println("EventManager: Last Hero, next start after " + _nextTime / 60000L + " min.");
    ThreadPoolManager.getInstance().scheduleGeneral(new StartTask(), _nextTime);
    if (!player.isHero()) {
      player.setHero(Config.ELH_HERO_DAYS);
    }
    player.setPVPArena(false);
    FastList.Node k = _rewards.head(); for (FastList.Node endk = _rewards.tail(); (k = k.getNext()) != endk; ) {
      Config.EventReward reward = (Config.EventReward)k.getValue();
      if (reward == null)
      {
        continue;
      }
      if (Rnd.get(100) < reward.chance) {
        L2ItemInstance rewItem = ItemTable.getInstance().createItem("LastHero", reward.id, reward.count, player, null);
        player.getInventory().addItem("LastHero", rewItem, player, null);
      }
    }
    player.teleToLocation(82737, 148571, -3470);
  }

  private boolean foundIp(String ip) {
    return _ips.contains(ip);
  }

  public void regPlayer(L2PcInstance player) {
    if (_state != EventState.REG) {
      player.sendHtmlMessage("-\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-", "\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F \u0435\u0449\u0435 \u043D\u0435 \u043E\u0431\u044C\u044F\u0432\u043B\u044F\u043B\u0430\u0441\u044C<br1> \u041F\u0440\u0438\u0445\u043E\u0434\u0438\u0442\u0435 \u043F\u043E\u0437\u0436\u0435 ;).");
      return;
    }

    if ((!TvTEvent.isInactive()) && (TvTEvent.isPlayerParticipant(player.getName()))) {
      player.sendHtmlMessage("-\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-", "\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u043D\u0430 TvT.");
      return;
    }

    if ((Config.MASS_PVP) && (massPvp.getEvent().isReg(player))) {
      player.sendHtmlMessage("-\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-", "\u0423\u0434\u0430\u0447\u0438 \u043D\u0430 \u0435\u0432\u0435\u043D\u0442\u0435 -\u041C\u0430\u0441\u0441 \u041F\u0412\u041F-");
      return;
    }
    if ((Config.EBC_ENABLE) && (BaseCapture.getEvent().isRegged(player))) {
      player.sendHtmlMessage("-\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-", "\u0423\u0434\u0430\u0447\u0438 \u043D\u0430 \u0435\u0432\u0435\u043D\u0442\u0435 -\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-");
      return;
    }

    if ((Olympiad.isRegisteredInComp(player)) || (player.isInOlympiadMode())) {
      player.sendHtmlMessage("-\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-", "\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B \u043D\u0430 \u043E\u043B\u0438\u043C\u043F\u0438\u0430\u0434\u0435.");
      return;
    }

    if (player.isCursedWeaponEquiped()) {
      player.sendHtmlMessage("-\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-", "\u0421 \u043F\u0440\u043E\u043A\u043B\u044F\u0442\u044B\u043C \u043E\u0440\u0443\u0436\u0438\u0435\u043C \u043D\u0435\u043B\u044C\u0437\u044F.");
      return;
    }

    if (_players.size() >= _maxPlayers) {
      player.sendHtmlMessage("-\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-", "\u0414\u043E\u0441\u0442\u0438\u0433\u043D\u0443\u0442 \u043F\u0440\u0435\u0434\u0435\u043B \u0438\u0433\u0440\u043E\u043A\u043E\u0432.");
      return;
    }

    if (_players.contains(player)) {
      player.sendHtmlMessage("-\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-", "\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B.");
      return;
    }

    if ((!Config.EVENTS_SAME_IP) && (foundIp(player.getIP()))) {
      player.sendHtmlMessage("-\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-", "\u0421 \u0432\u0430\u0448\u0435\u0433\u043E IP \u0443\u0436\u0435 \u0435\u0441\u0442\u044C \u0438\u0433\u0440\u043E\u043A.");
      return;
    }

    if (_ticketId > 0) {
      L2ItemInstance coin = player.getInventory().getItemByItemId(_ticketId);
      if ((coin == null) || (coin.getCount() < _ticketCount)) {
        player.sendHtmlMessage("-\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-", "\u0423\u0447\u0430\u0441\u0442\u0438\u0432 \u0432 \u0438\u0432\u0435\u043D\u0442\u0435 \u043F\u043B\u0430\u0442\u043D\u043E\u0435.");
        return;
      }
      player.destroyItemByItemId("lasthero", _ticketId, _ticketCount, player, true);
    }
    _players.add(player);
    if (!Config.EVENTS_SAME_IP) {
      _ips.add(player.getIP());
    }
    player.sendHtmlMessage("-\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-", "\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043D\u0430.");
  }

  public void delPlayer(L2PcInstance player) {
    if (!_players.contains(player)) {
      player.sendHtmlMessage("-\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-", "\u0412\u044B \u043D\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B.");
      return;
    }

    _players.remove(_players.indexOf(player));
    if (!Config.EVENTS_SAME_IP) {
      _ips.remove(player.getIP());
    }
    player.sendHtmlMessage("-\u041F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0439 \u0433\u0435\u0440\u043E\u0439-", "\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F \u043E\u0442\u043C\u0435\u043D\u0435\u043D\u0430.");
  }

  public void notifyFail(L2PcInstance player) {
    if (_state == EventState.WAIT) {
      return;
    }

    if (_players.contains(player)) {
      _players.remove(_players.indexOf(player));
      if (!Config.EVENTS_SAME_IP) {
        _ips.remove(player.getIP());
      }
      player.setChannel(1);
      player.setXYZ(82737, 148571, -3470);
      player.setPVPArena(false);
    }
  }

  public void notifyDeath(L2PcInstance player) {
    if (_state == EventState.WAIT) {
      return;
    }

    if (_players.contains(player)) {
      _players.remove(_players.indexOf(player));
      if (!Config.EVENTS_SAME_IP) {
        _ips.remove(player.getIP());
      }

      player.sendCritMessage("\u0412\u044B \u043F\u0440\u043E\u0438\u0433\u0440\u0430\u043B\u0438...");
      try {
        player.teleToLocationEvent(82737, 148571, -3470);
      } catch (Exception e) {
      }
      player.setChannel(1);
      player.doRevive();
      player.setCurrentHp(player.getMaxHp());
      player.setCurrentMp(player.getMaxMp());
      player.setCurrentCp(player.getMaxCp());
      player.setPVPArena(false);
      player.setTeam(0);
    }
  }

  public boolean isRegged(L2PcInstance player) {
    if (_state == EventState.WAIT) {
      return false;
    }

    return _players.contains(player);
  }

  private void announce(String text)
  {
    Announcements.getInstance().announceToAll(text);
  }

  public boolean isInBattle() {
    return _state == EventState.BATTLE;
  }

  public class WinTask
    implements Runnable
  {
    public WinTask()
    {
    }

    public void run()
    {
      if (LastHero._players.size() == 1)
        announceWinner((L2PcInstance)LastHero._players.getFirst());
      else
        ThreadPoolManager.getInstance().scheduleGeneral(new WinTask(LastHero.this), 10000L);
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
      L2PcInstance player = null;
      FastList.Node n = LastHero._players.head(); for (FastList.Node endp = LastHero._players.tail(); (n = n.getNext()) != endp; ) {
        player = (L2PcInstance)n.getValue();
        if ((player == null) || 
          (!player.isDead())) continue;
        notifyDeath(player);
      }

      FastList.Node n = LastHero._players.head(); for (FastList.Node endp = LastHero._players.tail(); (n = n.getNext()) != endp; ) {
        player = (L2PcInstance)n.getValue();
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

        player.setChannel(6);

        if (player.getParty() != null) {
          player.getParty().oustPartyMember(player);
        }

        player.teleToLocationEvent(LastHero._tpLoc.x + Rnd.get(300), LastHero._tpLoc.y + Rnd.get(300), LastHero._tpLoc.z);
        player.stopAllEffects();
        player.setCurrentCp(player.getMaxCp());
        player.setCurrentHp(player.getMaxHp());
        player.setCurrentMp(player.getMaxMp());
        player.setPVPArena(true);
        player.setTeam(1);
      }
      player = null;
      ThreadPoolManager.getInstance().scheduleGeneral(new LastHero.WinTask(LastHero.this), 10000L);
      System.out.println("EventManager: Last Hero, battle started.");
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
      if (LastHero._state != LastHero.EventState.REG) {
        return;
      }

      long regMin = LastHero._regTime;
      for (int i = 0; i < LastHero._regTime; i = (int)(i + LastHero._anTime))
        try {
          regMin -= LastHero._anTime;
          LastHero.this.announce(Static.LH_REG_IN);
          LastHero.this.announce(Static.LH_REG_LOST_S1.replace("%a%", String.valueOf(regMin / 60000L + 1L)));
          if (LastHero._players.isEmpty())
            LastHero.this.announce(Static.LH_NO_PLAYESR_YET);
          else {
            LastHero.this.announce(Static.LH_REGD_PLAYESR.replace("%a%", String.valueOf(LastHero._players.size())));
          }
          Thread.sleep(LastHero._anTime);
        }
        catch (InterruptedException e) {
        }
      LastHero.this.announce(Static.LH_REG_CLOSED);
      LastHero.access$002(LastHero.EventState.BATTLE);

      if (LastHero._players.size() < LastHero._minPlayers) {
        LastHero.access$002(LastHero.EventState.WAIT);
        LastHero.this.announce(Static.LH_CANC_NO_PLAYERS);
        LastHero.this.announce(Static.LH_NEXT_TIME.replace("%a%", String.valueOf(LastHero._nextTime / 60000L)));
        System.out.println("EventManager: Last Hero, canceled: no players.");
        System.out.println("EventManager: Last Hero, next start after " + LastHero._nextTime / 60000L + " min.");
        ThreadPoolManager.getInstance().scheduleGeneral(new LastHero.StartTask(LastHero.this), LastHero._nextTime);
        return;
      }
      LastHero.this.announce(Static.LH_BATTLE_AFTER.replace("%a%", String.valueOf(LastHero._tpDelay / 60000L)));
      System.out.println("EventManager: Last Hero, battle start after " + LastHero._tpDelay / 60000L + " min.");
      ThreadPoolManager.getInstance().scheduleGeneral(new LastHero.StartBattle(LastHero.this), LastHero._tpDelay);
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
      if (LastHero._state == LastHero.EventState.WAIT)
        LastHero.this.startEvent();
    }
  }

  static enum EventState
  {
    WAIT, 
    REG, 
    BATTLE;
  }
}