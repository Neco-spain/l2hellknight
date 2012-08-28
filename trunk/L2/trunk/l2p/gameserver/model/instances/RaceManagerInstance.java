package l2p.gameserver.model.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.ServerVariables;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.MonsterRace;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.DeleteObject;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.MonRaceInfo;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.PlaySound;
import l2p.gameserver.serverpackets.PlaySound.Type;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Location;

public class RaceManagerInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;
  public static final int LANES = 8;
  public static final int WINDOW_START = 0;
  private static List<Race> history;
  private static Set<RaceManagerInstance> managers;
  private static int _raceNumber = 1;
  private static final long SECOND = 1000L;
  private static final long MINUTE = 60000L;
  private static int minutes = 5;
  private static final int ACCEPTING_BETS = 0;
  private static final int WAITING = 1;
  private static final int STARTING_RACE = 2;
  private static final int RACE_END = 3;
  private static int state = 3;

  protected static final int[][] codes = { { -1, 0 }, { 0, 15322 }, { 13765, -1 } };
  private static boolean notInitialized = true;
  protected static MonRaceInfo packet;
  protected static int[] cost = { 100, 500, 1000, 5000, 10000, 20000, 50000, 100000 };

  public RaceManagerInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
    if (notInitialized)
    {
      notInitialized = false;

      _raceNumber = ServerVariables.getInt("monster_race", 1);
      history = new ArrayList();
      managers = new CopyOnWriteArraySet();

      ThreadPoolManager s = ThreadPoolManager.getInstance();
      s.scheduleAtFixedRate(new Announcement(816), 0L, 600000L);
      s.scheduleAtFixedRate(new Announcement(817), 30000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(816), 60000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(817), 90000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(818), 120000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(818), 180000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(818), 240000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(818), 300000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(819), 360000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(819), 420000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(820), 420000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(820), 480000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(821), 510000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(822), 530000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(823), 535000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(823), 536000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(823), 537000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(823), 538000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(823), 539000L, 600000L);
      s.scheduleAtFixedRate(new Announcement(824), 540000L, 600000L);
    }
    managers.add(this);
  }

  public void removeKnownPlayer(Player player)
  {
    for (int i = 0; i < 8; i++)
      player.sendPacket(new DeleteObject(MonsterRace.getInstance().getMonsters()[i]));
  }

  public void makeAnnouncement(int type)
  {
    SystemMessage sm = new SystemMessage(type);
    switch (type)
    {
    case 816:
    case 817:
      if (state != 0)
      {
        state = 0;
        startRace();
      }
      sm.addNumber(_raceNumber);
      break;
    case 818:
    case 820:
    case 823:
      sm.addNumber(minutes);
      sm.addNumber(_raceNumber);
      minutes -= 1;
      break;
    case 819:
      sm.addNumber(_raceNumber);
      state = 1;
      minutes = 2;
      break;
    case 822:
    case 825:
      sm.addNumber(_raceNumber);
      minutes = 5;
      break;
    case 826:
      state = 3;
      sm.addNumber(MonsterRace.getInstance().getFirstPlace());
      sm.addNumber(MonsterRace.getInstance().getSecondPlace());
    case 821:
    case 824:
    }
    broadcast(sm);

    if (type == 824)
    {
      state = 2;
      startRace();
      minutes = 5;
    }
  }

  protected void broadcast(L2GameServerPacket pkt)
  {
    for (RaceManagerInstance manager : managers)
      if (!manager.isDead())
        manager.broadcastPacketToOthers(new L2GameServerPacket[] { pkt });
  }

  public void sendMonsterInfo()
  {
    broadcast(packet);
  }

  private void startRace()
  {
    MonsterRace race = MonsterRace.getInstance();
    if (state == 2)
    {
      PlaySound SRace = new PlaySound("S_Race");
      broadcast(SRace);

      PlaySound SRace2 = new PlaySound(PlaySound.Type.SOUND, "ItemSound2.race_start", 1, 121209259, new Location(12125, 182487, -3559));
      broadcast(SRace2);
      packet = new MonRaceInfo(codes[1][0], codes[1][1], race.getMonsters(), race.getSpeeds());
      sendMonsterInfo();

      ThreadPoolManager.getInstance().schedule(new RunRace(), 5000L);
    }
    else
    {
      race.newRace();
      race.newSpeeds();
      packet = new MonRaceInfo(codes[0][0], codes[0][1], race.getMonsters(), race.getSpeeds());
      sendMonsterInfo();
    }
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    if ((command.startsWith("BuyTicket")) && (state != 0))
    {
      player.sendPacket(Msg.MONSTER_RACE_TICKETS_ARE_NO_LONGER_AVAILABLE);
      command = "Chat 0";
    }
    if ((command.startsWith("ShowOdds")) && (state == 0))
    {
      player.sendPacket(Msg.MONSTER_RACE_PAYOUT_INFORMATION_IS_NOT_AVAILABLE_WHILE_TICKETS_ARE_BEING_SOLD);
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
    else if (command.equals("ShowOdds")) {
      showOdds(player);
    } else if (command.equals("ShowInfo")) {
      showMonsterInfo(player);
    } else if (!command.equals("calculateWin"))
    {
      if (!command.equals("viewHistory"))
      {
        super.onBypassFeedback(player, command);
      }
    }
  }

  public void showOdds(Player player) {
    if (state == 0)
      return;
    int npcId = getTemplate().npcId;

    NpcHtmlMessage html = new NpcHtmlMessage(player, this);
    String filename = getHtmlPath(npcId, 5, player);
    html.setFile(filename);
    for (int i = 0; i < 8; i++)
    {
      int n = i + 1;
      String search = "Mob" + n;
      html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
    }
    html.replace("1race", String.valueOf(_raceNumber));
    player.sendPacket(html);
    player.sendActionFailed();
  }

  public void showMonsterInfo(Player player)
  {
    int npcId = getTemplate().npcId;

    NpcHtmlMessage html = new NpcHtmlMessage(player, this);
    String filename = getHtmlPath(npcId, 6, player);
    html.setFile(filename);
    for (int i = 0; i < 8; i++)
    {
      int n = i + 1;
      String search = "Mob" + n;
      html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
    }
    player.sendPacket(html);
    player.sendActionFailed();
  }

  public void showBuyTicket(Player player, int val)
  {
    if (state != 0)
      return;
    int npcId = getTemplate().npcId;

    NpcHtmlMessage html = new NpcHtmlMessage(player, this);
    if (val < 10)
    {
      String filename = getHtmlPath(npcId, 2, player);
      html.setFile(filename);
      for (int i = 0; i < 8; i++)
      {
        int n = i + 1;
        String search = "Mob" + n;
        html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
      }
      String search = "No1";
      if (val == 0) {
        html.replace(search, "");
      }
      else {
        html.replace(search, "" + val);
        player.setRace(0, val);
      }
    }
    else if (val < 20)
    {
      if (player.getRace(0) == 0)
        return;
      String filename = getHtmlPath(npcId, 3, player);
      html.setFile(filename);
      html.replace("0place", "" + player.getRace(0));
      String search = "Mob1";
      String replace = MonsterRace.getInstance().getMonsters()[(player.getRace(0) - 1)].getTemplate().name;
      html.replace(search, replace);
      search = "0adena";
      if (val == 10) {
        html.replace(search, "");
      }
      else {
        html.replace(search, "" + cost[(val - 11)]);
        player.setRace(1, val - 10);
      }
    }
    else if (val == 20)
    {
      if ((player.getRace(0) == 0) || (player.getRace(1) == 0))
        return;
      String filename = getHtmlPath(npcId, 4, player);
      html.setFile(filename);
      html.replace("0place", "" + player.getRace(0));
      String search = "Mob1";
      String replace = MonsterRace.getInstance().getMonsters()[(player.getRace(0) - 1)].getTemplate().name;
      html.replace(search, replace);
      search = "0adena";
      int price = cost[(player.getRace(1) - 1)];
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
      if ((player.getRace(0) == 0) || (player.getRace(1) == 0))
        return;
      if (player.getAdena() < cost[(player.getRace(1) - 1)])
      {
        player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        return;
      }
      int ticket = player.getRace(0);
      int priceId = player.getRace(1);
      player.setRace(0, 0);
      player.setRace(1, 0);
      player.reduceAdena(cost[(priceId - 1)], true);
      SystemMessage sm = new SystemMessage(371);
      sm.addNumber(_raceNumber);
      sm.addItemName(4443);
      player.sendPacket(sm);
      ItemInstance item = ItemFunctions.createItem(4443);
      item.setEnchantLevel(_raceNumber);
      item.setCustomType1(ticket);
      item.setCustomType2(cost[(priceId - 1)] / 100);
      player.getInventory().addItem(item);
      return;
    }
    String search;
    String filename;
    html.replace("1race", String.valueOf(_raceNumber));
    player.sendPacket(html);
    player.sendActionFailed();
  }

  public MonRaceInfo getPacket()
  {
    return packet;
  }

  class RunEnd extends RunnableImpl
  {
    RunEnd()
    {
    }

    public void runImpl()
      throws Exception
    {
      makeAnnouncement(826);
      makeAnnouncement(825);
      RaceManagerInstance.access$008();
      ServerVariables.set("monster_race", RaceManagerInstance._raceNumber);

      for (int i = 0; i < 8; i++)
        broadcast(new DeleteObject(MonsterRace.getInstance().getMonsters()[i]));
    }
  }

  class RunRace extends RunnableImpl
  {
    RunRace()
    {
    }

    public void runImpl()
      throws Exception
    {
      RaceManagerInstance.packet = new MonRaceInfo(RaceManagerInstance.codes[2][0], RaceManagerInstance.codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds());
      sendMonsterInfo();
      ThreadPoolManager.getInstance().schedule(new RaceManagerInstance.RunEnd(RaceManagerInstance.this), 30000L);
    }
  }

  public class Race
  {
    private Info[] info;

    public Race(Info[] info)
    {
      this.info = info;
    }

    public Info getLaneInfo(int lane)
    {
      return info[lane];
    }
    public class Info {
      private int id;
      private int place;
      private int odds;
      private int payout;

      public Info(int id, int place, int odds, int payout) { this.id = id;
        this.place = place;
        this.odds = odds;
        this.payout = payout;
      }

      public int getId()
      {
        return id;
      }

      public int getOdds()
      {
        return odds;
      }

      public int getPayout()
      {
        return payout;
      }

      public int getPlace()
      {
        return place;
      }
    }
  }

  class Announcement extends RunnableImpl
  {
    private int type;

    public Announcement(int type)
    {
      this.type = type;
    }

    public void runImpl()
      throws Exception
    {
      makeAnnouncement(type);
    }
  }
}