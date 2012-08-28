package net.sf.l2j.gameserver.model.actor.instance;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.MonsterRace;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.DeleteObject;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.MonRaceInfo;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Broadcast;

public class L2RaceManagerInstance extends L2NpcInstance
{
  public static final int LANES = 8;
  public static final int WINDOW_START = 0;
  private static List<Race> _history;
  private static List<L2RaceManagerInstance> _managers;
  protected static int _raceNumber = 4;
  private static final long SECOND = 1000L;
  private static final long MINUTE = 60000L;
  private static int _minutes = 5;
  private static final int ACCEPTING_BETS = 0;
  private static final int WAITING = 1;
  private static final int STARTING_RACE = 2;
  private static final int RACE_END = 3;
  private static int _state = 3;

  protected static final int[][] _codes = { { -1, 0 }, { 0, 15322 }, { 13765, -1 } };
  private static boolean _notInitialized = true;
  protected static MonRaceInfo _packet;
  protected static final int[] _cost = { 100, 500, 1000, 5000, 10000, 20000, 50000, 100000 };

  public L2RaceManagerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);

    if (_notInitialized)
    {
      _notInitialized = false;
      _history = new FastList();
      _managers = new FastList();

      ThreadPoolManager s = ThreadPoolManager.getInstance();
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE), 0L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE), 30000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE), 60000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE), 90000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 120000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 180000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 240000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 300000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 360000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKET_SALES_CLOSED), 420000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_MINUTES), 420000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_MINUTES), 480000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_30_SECONDS), 510000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_COUNTDOWN_IN_FIVE_SECONDS), 530000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS), 535000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS), 536000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS), 537000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS), 538000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS), 539000L, 600000L);
      s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_RACE_START), 540000L, 600000L);
    }
    _managers.add(this);
  }

  public void makeAnnouncement(SystemMessageId type)
  {
    SystemMessage sm = SystemMessage.id(type);
    switch (type.getId())
    {
    case 816:
    case 817:
      if (_state != 0)
      {
        _state = 0;
        startRace();
      }
      sm.addNumber(_raceNumber);
      break;
    case 818:
    case 820:
    case 823:
      sm.addNumber(_minutes);
      sm.addNumber(_raceNumber);
      _minutes -= 1;
      break;
    case 819:
      sm.addNumber(_raceNumber);
      _state = 1;
      _minutes = 2;
      break;
    case 822:
    case 825:
      sm.addNumber(_raceNumber);
      _minutes = 5;
      break;
    case 826:
      _state = 3;
      sm.addNumber(MonsterRace.getInstance().getFirstPlace());
      sm.addNumber(MonsterRace.getInstance().getSecondPlace());
    case 821:
    case 824:
    }

    broadcast(sm);
    sm = null;

    if (type == SystemMessageId.MONSRACE_RACE_START)
    {
      _state = 2;
      startRace();
      _minutes = 5;
    }
  }

  protected void broadcast(L2GameServerPacket pkt)
  {
    for (L2RaceManagerInstance manager : _managers)
    {
      Broadcast.toKnownPlayersInRadius(manager, pkt, 2000);
    }
  }

  public void sendMonsterInfo()
  {
    broadcast(_packet);
  }

  private void startRace()
  {
    MonsterRace race = MonsterRace.getInstance();
    if (_state == 2)
    {
      broadcast(new PlaySound(1, "S_Race", 0, 0, 0, 0, 0));
      broadcast(new PlaySound(0, "ItemSound2.race_start", 1, 121209259, 12125, 182487, -3559));
      _packet = new MonRaceInfo(_codes[1][0], _codes[1][1], race.getMonsters(), race.getSpeeds());
      sendMonsterInfo();

      ThreadPoolManager.getInstance().scheduleGeneral(new RunRace(), 5000L);
    }
    else
    {
      race.newRace();
      race.newSpeeds();
      _packet = new MonRaceInfo(_codes[0][0], _codes[0][1], race.getMonsters(), race.getSpeeds());
      sendMonsterInfo();
    }
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if ((command.startsWith("BuyTicket")) && (_state != 0))
    {
      player.sendPacket(Static.MONSRACE_TICKETS_NOT_AVAILABLE);
      command = "Chat 0";
    }
    if ((command.startsWith("ShowOdds")) && (_state == 0))
    {
      player.sendPacket(Static.MONSRACE_NO_PAYOUT_INFO);
      command = "Chat 0";
    }

    if (command.startsWith("BuyTicket"))
    {
      int val = Integer.parseInt(command.substring(10));
      if (val == 0)
      {
        player.setRace(0, 0);
        player.setRace(1, 0);
      }
      if (((val == 10) && (player.getRace(0) == 0)) || ((val == 20) && (player.getRace(0) == 0) && (player.getRace(1) == 0)))
        val = 0;
      showBuyTicket(player, val);
    }
    else if (command.equals("ShowOdds")) { showOdds(player);
    } else if (command.equals("ShowInfo")) { showMonsterInfo(player);
    } else if (!command.equals("calculateWin"))
    {
      if (!command.equals("viewHistory"))
      {
        super.onBypassFeedback(player, command);
      }
    }
  }

  public void showOdds(L2PcInstance player) {
    if (_state == 0) return;
    int npcId = getTemplate().npcId;

    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    String filename = getHtmlPath(npcId, 5);
    html.setFile(filename);
    for (int i = 0; i < 8; i++)
    {
      int n = i + 1;
      String search = "Mob" + n;
      html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
    }
    html.replace("1race", String.valueOf(_raceNumber));
    html.replace("%objectId%", String.valueOf(getObjectId()));
    player.sendPacket(html);
    player.sendActionFailed();
  }

  public void showMonsterInfo(L2PcInstance player)
  {
    int npcId = getTemplate().npcId;

    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    String filename = getHtmlPath(npcId, 6);
    html.setFile(filename);
    for (int i = 0; i < 8; i++)
    {
      int n = i + 1;
      String search = "Mob" + n;
      html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
    }
    html.replace("%objectId%", String.valueOf(getObjectId()));
    player.sendPacket(html);
    player.sendActionFailed();
  }

  public void showBuyTicket(L2PcInstance player, int val)
  {
    if (_state != 0) return;
    int npcId = getTemplate().npcId;

    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    if (val < 10)
    {
      String filename = getHtmlPath(npcId, 2);
      html.setFile(filename);
      for (int i = 0; i < 8; i++)
      {
        int n = i + 1;
        String search = "Mob" + n;
        html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
      }
      String search = "No1";
      if (val == 0) { html.replace(search, "");
      } else
      {
        html.replace(search, "" + val);
        player.setRace(0, val);
      }
    }
    else if (val < 20)
    {
      if (player.getRace(0) == 0) return;
      String filename = getHtmlPath(npcId, 3);
      html.setFile(filename);
      html.replace("0place", "" + player.getRace(0));
      String search = "Mob1";
      String replace = MonsterRace.getInstance().getMonsters()[(player.getRace(0) - 1)].getTemplate().name;
      html.replace(search, replace);
      search = "0adena";
      if (val == 10) { html.replace(search, "");
      } else
      {
        html.replace(search, "" + _cost[(val - 11)]);
        player.setRace(1, val - 10);
      }
    }
    else if (val == 20)
    {
      if ((player.getRace(0) == 0) || (player.getRace(1) == 0)) return;
      String filename = getHtmlPath(npcId, 4);
      html.setFile(filename);
      html.replace("0place", "" + player.getRace(0));
      String search = "Mob1";
      String replace = MonsterRace.getInstance().getMonsters()[(player.getRace(0) - 1)].getTemplate().name;
      html.replace(search, replace);
      search = "0adena";
      int price = _cost[(player.getRace(1) - 1)];
      html.replace(search, "" + price);
      search = "0tax";
      int tax = 0;
      html.replace(search, "" + tax);
      search = "0total";
      int total = price + tax;
      html.replace(search, "" + total);
    }
    else
    {
      if ((player.getRace(0) == 0) || (player.getRace(1) == 0)) return;
      int ticket = player.getRace(0);
      int priceId = player.getRace(1);
      if (!player.reduceAdena("Race", _cost[(priceId - 1)], this, true)) return;
      player.setRace(0, 0);
      player.setRace(1, 0);
      player.sendPacket(SystemMessage.id(SystemMessageId.ACQUIRED).addNumber(_raceNumber).addItemName(4443));
      L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), 4443);
      item.setCount(1);
      item.setEnchantLevel(_raceNumber);
      item.setCustomType1(ticket);
      item.setCustomType2(_cost[(priceId - 1)] / 100);
      player.getInventory().addItem("Race", item, player, this);
      InventoryUpdate iu = new InventoryUpdate();
      iu.addItem(item);
      L2ItemInstance adenaupdate = player.getInventory().getItemByItemId(57);
      iu.addModifiedItem(adenaupdate);
      player.sendPacket(iu);
      return;
    }
    String search;
    String filename;
    html.replace("1race", String.valueOf(_raceNumber));
    html.replace("%objectId%", String.valueOf(getObjectId()));
    player.sendPacket(html);
    player.sendActionFailed();
  }

  class RunEnd
    implements Runnable
  {
    RunEnd()
    {
    }

    public void run()
    {
      makeAnnouncement(SystemMessageId.MONSRACE_FIRST_PLACE_S1_SECOND_S2);
      makeAnnouncement(SystemMessageId.MONSRACE_RACE_END);
      L2RaceManagerInstance._raceNumber += 1;

      DeleteObject obj = null;
      for (int i = 0; i < 8; i++)
      {
        obj = new DeleteObject(MonsterRace.getInstance().getMonsters()[i]);
        broadcast(obj);
      }
    }
  }

  class RunRace
    implements Runnable
  {
    RunRace()
    {
    }

    public void run()
    {
      L2RaceManagerInstance._packet = new MonRaceInfo(L2RaceManagerInstance._codes[2][0], L2RaceManagerInstance._codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds());
      sendMonsterInfo();
      ThreadPoolManager.getInstance().scheduleGeneral(new L2RaceManagerInstance.RunEnd(L2RaceManagerInstance.this), 30000L);
    }
  }

  public static class Race
  {
    private Info[] _info;

    public Race(Info[] pInfo)
    {
      _info = pInfo;
    }

    public Info getLaneInfo(int lane)
    {
      return _info[lane];
    }
    public static class Info {
      private int _id;
      private int _place;
      private int _odds;
      private int _payout;

      public Info(int pId, int pPlace, int pOdds, int pPayout) { _id = pId;
        _place = pPlace;
        _odds = pOdds;
        _payout = pPayout;
      }

      public int getId()
      {
        return _id;
      }

      public int getOdds()
      {
        return _odds;
      }

      public int getPayout()
      {
        return _payout;
      }

      public int getPlace()
      {
        return _place;
      }
    }
  }

  class Announcement
    implements Runnable
  {
    private SystemMessageId _type;

    public Announcement(SystemMessageId pType)
    {
      _type = pType;
    }

    public void run()
    {
      makeAnnouncement(_type);
    }
  }
}