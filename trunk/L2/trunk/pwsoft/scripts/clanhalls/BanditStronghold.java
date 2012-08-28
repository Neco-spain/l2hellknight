package scripts.clanhalls;

import java.util.Calendar;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;

public class BanditStronghold
{
  private FastMap<Integer, ClanInfo> _owner = new FastMap();
  private FastMap<Integer, ClanInfo> _clans = new FastMap();
  private FastTable<Location> _locs = new FastTable();
  private FastTable<Integer> _camps = new FastTable();

  private static int _ownerClan = 0;
  private static boolean _finalBattle = false;
  private static boolean _inProgress = false;
  private static BanditStronghold _ch;

  public static BanditStronghold getCH()
  {
    return _ch;
  }

  public static void init()
  {
    _ch = new BanditStronghold();
    _ch.load();
  }

  private void load()
  {
    _locs.clear();
    _clans.clear();
    _camps.clear();
    _owner.clear();

    _ownerClan = 0;
    _finalBattle = false;
    _inProgress = false;

    Location loc = new Location(83699, -17468, -1774, 19048);
    _locs.add(loc);
    loc = new Location(82053, -17060, -1784, 5432);
    _locs.add(loc);
    loc = new Location(82142, -15528, -1799, 58792);
    _locs.add(loc);
    loc = new Location(83544, -15266, -1770, 44976);
    _locs.add(loc);
    loc = new Location(84609, -16041, -1769, 35816);
    _locs.add(loc);
    loc = null;

    _camps.add(Integer.valueOf(35423));
    _camps.add(Integer.valueOf(35424));
    _camps.add(Integer.valueOf(35425));
    _camps.add(Integer.valueOf(35426));
    _camps.add(Integer.valueOf(35427));
    if (!ClanHallManager.getInstance().isFree(35))
    {
      ClanHall ch = ClanHallManager.getInstance().getClanHallById(35);

      _ownerClan = ch.getOwnerId();
      ClanInfo ci = new ClanInfo(ch.getOwnerId(), 35428, 0, new FastList());

      _owner.put(Integer.valueOf(ch.getOwnerId()), ci);
    }
  }

  public synchronized void regClan(L2PcInstance player, L2NpcInstance npc)
  {
    int size = _clans.size();
    if (size >= 5)
    {
      npc.showChatWindow(player, "data/html/siege/35437-full.htm");
      return;
    }

    FastList pcs = new FastList();
    pcs.add(player);

    ClanInfo ci = new ClanInfo(player.getClan().getClanId(), 0, 0, pcs);
    _clans.put(Integer.valueOf(player.getClan().getClanId()), ci);

    npc.showChatWindow(player, "data/html/siege/35437-reged_" + size + ".htm");
  }

  public FastTable<String> getClanNames()
  {
    L2Clan clan = null;
    ClanTable ct = ClanTable.getInstance();
    FastTable names = new FastTable();
    FastMap.Entry e = _clans.head(); for (FastMap.Entry end = _clans.tail(); (e = e.getNext()) != end; )
    {
      Integer id = (Integer)e.getKey();
      if (id == null) {
        continue;
      }
      clan = ct.getClan(id.intValue());
      if (clan == null) {
        continue;
      }
      names.add(clan.getName());
    }
    return names;
  }

  private void spawnClans()
  {
    DoorTable dt = DoorTable.getInstance();
    dt.getDoor(Integer.valueOf(22170003)).closeMe();
    dt.getDoor(Integer.valueOf(22170004)).closeMe();

    L2PcInstance player = null;
    FastList.Node n;
    if (!_owner.isEmpty())
    {
      if (_finalBattle)
      {
        dt.getDoor(Integer.valueOf(22170003)).openMe();
        dt.getDoor(Integer.valueOf(22170004)).openMe();
      }

      ClanInfo ci = (ClanInfo)_owner.get(Integer.valueOf(_ownerClan));
      FastList opcs = ci._players;
      n = opcs.head(); for (FastList.Node fend = opcs.tail(); (n = n.getNext()) != fend; )
      {
        player = (L2PcInstance)n.getValue();
        if (player == null) {
          continue;
        }
        if (_finalBattle) {
          player.sendCritMessage("\u0412\u0441\u0435 \u043D\u0430 \u0432\u044B\u0445\u043E\u0434, \u043D\u0430 \u0437\u0430\u0449\u0438\u0442\u0443 \u043A\u043B\u0430\u043D \u0445\u043E\u043B\u043B\u0430!"); continue;
        }

        player.teleToLocation(80339, -15442, -1804, false);
        player.sendCritMessage("\u041E\u0436\u0438\u0434\u0430\u0439\u0442\u0435 \u0444\u0438\u043D\u0430\u043B\u044C\u043D\u0443\u044E \u0431\u0438\u0442\u0432\u0443 \u0432\u043D\u0443\u0442\u0440\u0438 \u043A\u043B\u0430\u043D \u0445\u043E\u043B\u043B\u0430!");
      }

    }

    int team = 0;
    int bossId = 0;
    Location loc = null;
    ClanInfo clan = null;
    L2MonsterInstance boss = null;
    ClanTable ct = ClanTable.getInstance();
    GrandBossManager gm = GrandBossManager.getInstance();
    FastMap.Entry e = _clans.head(); for (FastMap.Entry end = _clans.tail(); (e = e.getNext()) != end; )
    {
      Integer id = (Integer)e.getKey();
      clan = (ClanInfo)e.getValue();
      if ((id == null) || 
        (clan == null)) {
        continue;
      }
      loc = getSpawns(team);

      bossId = clan._boss;
      if (bossId < 10)
        bossId = 35430;
      gm.createOnePrivateEx(getCamp(team), loc.x, loc.y, loc.z, loc.h);
      boss = (L2MonsterInstance)gm.createOnePrivateEx(bossId, loc.x + Rnd.get(110, 130), loc.y + Rnd.get(110, 130), loc.z, loc.h);
      clan._bossObj = boss.getObjectId();
      boss.setTitle(ct.getClan(id.intValue()).getName());

      if (id.intValue() == _ownerClan) {
        continue;
      }
      FastList pcs = clan._players;
      FastList.Node n = pcs.head(); for (FastList.Node fend = pcs.tail(); (n = n.getNext()) != fend; )
      {
        player = (L2PcInstance)n.getValue();
        if (player == null)
          continue;
        player.teleToLocation(loc.x + Rnd.get(70, 110), loc.y + Rnd.get(70, 110), loc.z, false);
      }
      team++;
    }
  }

  public void startSiege()
  {
    try
    {
      spawnClans();
    }
    catch (Exception e)
    {
    }
    ThreadPoolManager.getInstance().scheduleGeneral(new StartBattle(), 1000L);
  }

  protected boolean haveWinner()
  {
    return _clans.size() == 1;
  }

  private void setOwner(int clanId)
  {
    if (!ClanHallManager.getInstance().isFree(35)) {
      ClanHallManager.getInstance().setFree(35);
    }
    ClanHallManager.getInstance().setOwner(35, ClanTable.getInstance().getClan(clanId));
    CastleManager.getInstance().getCastleById(35).getSiege().endSiege();
    load();
  }

  public void cancel()
  {
    load();
  }

  public void notifyDeath(int bossObj)
  {
    int clanId = 0;
    ClanInfo clan = null;
    L2PcInstance player = null;
    FastMap.Entry e = _clans.head(); for (FastMap.Entry end = _clans.tail(); (e = e.getNext()) != end; )
    {
      Integer id = (Integer)e.getKey();
      clan = (ClanInfo)e.getValue();
      if ((id == null) || 
        (clan == null) || 
        (clan._bossObj != bossObj)) {
        continue;
      }
      clanId = id.intValue();
      FastList pcs = clan._players;
      n = pcs.head(); for (FastList.Node fend = pcs.tail(); (n = n.getNext()) != fend; )
      {
        player = (L2PcInstance)n.getValue();
        if (player == null)
          continue;
        player.teleToLocation(77310, 50317, -3160, false);
      }
    }
    FastList.Node n;
    if (clanId > 1)
      _clans.remove(Integer.valueOf(clanId));
  }

  public synchronized void acceptNpc(int id, L2PcInstance player, L2NpcInstance npc)
  {
    if (_camps.isEmpty())
    {
      npc.showChatWindow(player, "data/html/siege/35437-closed.htm");
      return;
    }

    int bossId = 0;
    switch (id)
    {
    case 0:
      bossId = 35428;
      break;
    case 1:
      bossId = 35429;
      break;
    case 2:
      bossId = 35430;
      break;
    case 3:
      bossId = 35431;
      break;
    case 4:
      bossId = 35432;
    }

    ClanInfo ci = null;
    if (player.getClan().getClanId() == _ownerClan)
      ci = (ClanInfo)_owner.get(Integer.valueOf(_ownerClan));
    else
      ci = (ClanInfo)_clans.get(Integer.valueOf(player.getClan().getClanId()));
    ci._boss = bossId;

    npc.showChatWindow(player, "data/html/siege/35437-accept.htm");
  }

  public synchronized void regPlayer(L2PcInstance player, L2NpcInstance npc)
  {
    ClanInfo ci = null;
    if (player.getClan().getClanId() == _ownerClan)
      ci = (ClanInfo)_owner.get(Integer.valueOf(_ownerClan));
    else {
      ci = (ClanInfo)_clans.get(Integer.valueOf(player.getClan().getClanId()));
    }
    if (ci._players.size() >= 18)
    {
      npc.showChatWindow(player, "data/html/siege/35437-max.htm");
      return;
    }
    if (!ci._players.contains(player))
      ci._players.add(player);
    npc.showChatWindow(player, "data/html/siege/35437-accept.htm");
  }

  private Location getSpawns(int team)
  {
    if (_finalBattle)
    {
      Location f = new Location(81981, -15708, -1858, 60392);
      if (team == 1)
        f = new Location(84375, -17060, -1860, 27712);
      return f;
    }
    return (Location)_locs.get(team);
  }

  public FastMap<Integer, ClanInfo> getAttackers()
  {
    return _clans;
  }

  private int getCamp(int team)
  {
    return ((Integer)_camps.get(team)).intValue();
  }

  public boolean isRegistered(L2Clan clan)
  {
    return _clans.containsKey(Integer.valueOf(clan.getClanId()));
  }

  public boolean isRegTime()
  {
    long siege = CastleManager.getInstance().getCastleById(35).getSiegeDate().getTimeInMillis();
    long time = Calendar.getInstance().getTimeInMillis();

    return (siege - time < 3600L) && (siege - time > 0L);
  }

  public boolean inProgress()
  {
    return _inProgress;
  }

  public class StartBattle
    implements Runnable
  {
    public StartBattle()
    {
    }

    public void run()
    {
      BanditStronghold.access$002(true);
      long maxTime = 1200000L;
      if (BanditStronghold._finalBattle)
      {
        DoorTable dt = DoorTable.getInstance();
        dt.getDoor(Integer.valueOf(22170003)).openMe();
        dt.getDoor(Integer.valueOf(22170004)).openMe();
        try
        {
          BanditStronghold.this.spawnClans();
        }
        catch (Exception e)
        {
        }
        maxTime = 5600000L;
      }

      for (int i = 0; i < maxTime; i += 3000)
      {
        try
        {
          Thread.sleep(3000L);
          if (haveWinner())
            break;
        }
        catch (InterruptedException e)
        {
        }
      }
      BanditStronghold.access$002(false);

      int winId = 0;
      BanditStronghold.ClanInfo clan = null;
      FastMap.Entry e = _clans.head(); for (FastMap.Entry end = _clans.tail(); (e = e.getNext()) != end; )
      {
        Integer id = (Integer)e.getKey();
        clan = (BanditStronghold.ClanInfo)e.getValue();
        if (id == null) {
          continue;
        }
        winId = id.intValue();
      }

      if (winId == 0)
      {
        CastleManager.getInstance().getCastleById(35).getSiege().endSiege();
        BanditStronghold.this.load();
        return;
      }

      if (BanditStronghold._finalBattle) {
        BanditStronghold.this.setOwner(winId);
      }
      else {
        if (_clans.size() >= 2)
        {
          if (BanditStronghold._ownerClan != 0)
            BanditStronghold.this.setOwner(BanditStronghold._ownerClan);
          else {
            CastleManager.getInstance().getCastleById(35).getSiege().endSiege();
          }
          BanditStronghold.this.load();
          return;
        }

        if (_owner.isEmpty())
        {
          BanditStronghold.this.setOwner(winId);
          return;
        }

        _camps.add(Integer.valueOf(35423));
        _camps.add(Integer.valueOf(35424));

        BanditStronghold.access$102(true);
        _clans.clear();

        BanditStronghold.ClanInfo ci = (BanditStronghold.ClanInfo)_owner.get(Integer.valueOf(BanditStronghold._ownerClan));
        _clans.put(Integer.valueOf(BanditStronghold._ownerClan), ci);

        clan._boss = 35430;
        _clans.put(Integer.valueOf(winId), clan);

        ThreadPoolManager.getInstance().scheduleGeneral(new StartBattle(BanditStronghold.this), 30000L);
      }
    }
  }

  private static class ClanInfo
  {
    public int _id;
    public int _boss;
    public int _bossObj;
    public FastList<L2PcInstance> _players;

    public ClanInfo(int id, int boss, int bossObj, FastList<L2PcInstance> players)
    {
      _id = id;
      _boss = boss;
      _bossObj = bossObj;
      _players = players;
    }
  }
}