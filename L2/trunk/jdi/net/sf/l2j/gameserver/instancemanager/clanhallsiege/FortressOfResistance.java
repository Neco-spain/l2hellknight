package net.sf.l2j.gameserver.instancemanager.clanhallsiege;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class FortressOfResistance
{
  private static final Logger _log = Logger.getLogger(FortressOfResistance.class.getName());
  private static FortressOfResistance _instance;
  private FastMap<Integer, DamageInfo> _clansDamageInfo;
  private static int START_DAY = 1;
  private static int HOUR = Config.PARTISAN_HOUR;
  private static int MINUTES = Config.PARTISAN_MINUTES;
  private ScheduledFuture<?> _nurka;
  private ScheduledFuture<?> _announce;
  private Calendar _capturetime = Calendar.getInstance();

  public static FortressOfResistance getInstance()
  {
    if (_instance == null)
    {
      _instance = new FortressOfResistance();
    }
    return _instance;
  }

  private FortressOfResistance()
  {
    if (Config.PARTISAN_DAY == 1)
      START_DAY = 2;
    else if (Config.PARTISAN_DAY == 2)
      START_DAY = 3;
    else if (Config.PARTISAN_DAY == 3)
      START_DAY = 4;
    else if (Config.PARTISAN_DAY == 4)
      START_DAY = 5;
    else if (Config.PARTISAN_DAY == 5)
      START_DAY = 6;
    else if (Config.PARTISAN_DAY == 6)
      START_DAY = 7;
    else if (Config.PARTISAN_DAY == 7)
      START_DAY = 1;
    else {
      START_DAY = 6;
    }
    if ((HOUR < 0) || (HOUR > 23))
      HOUR = 21;
    if ((MINUTES < 0) || (MINUTES > 59)) {
      MINUTES = 0;
    }
    _clansDamageInfo = new FastMap();

    synchronized (this)
    {
      setCalendarForNextCaprture();
      long milliToCapture = getMilliToCapture();

      RunMessengerSpawn rms = new RunMessengerSpawn();
      ThreadPoolManager.getInstance().scheduleGeneral(rms, milliToCapture);
      _log.info("Fortress of Resistanse: " + milliToCapture / 1000L + " sec. to capture");
    }
  }

  private void setCalendarForNextCaprture()
  {
    int daysToChange = getDaysToCapture();

    if (daysToChange == 7) {
      if (_capturetime.get(11) < HOUR)
        daysToChange = 0;
      else if ((_capturetime.get(11) == HOUR) && (_capturetime.get(12) < MINUTES))
        daysToChange = 0;
    }
    if (daysToChange > 0) {
      _capturetime.add(5, daysToChange);
    }
    _capturetime.set(11, HOUR);
    _capturetime.set(12, MINUTES);
  }

  private int getDaysToCapture()
  {
    int numDays = _capturetime.get(7) - START_DAY;

    if (numDays < 0) {
      return 0 - numDays;
    }
    return 7 - numDays;
  }

  private long getMilliToCapture()
  {
    long currTimeMillis = System.currentTimeMillis();
    long captureTimeMillis = _capturetime.getTimeInMillis();

    return captureTimeMillis - currTimeMillis;
  }

  public void MessengerSpawn()
  {
    if (!ClanHallManager.getInstance().isFree(21))
    {
      ClanHallManager.getInstance().setFree(21);
    }

    Announce("Capture registration of Partisan Hideout has begun!");
    Announce("Now its open for 1 hours!");

    L2NpcInstance result = null;
    try
    {
      L2NpcTemplate template = NpcTable.getInstance().getTemplate(35382);

      L2Spawn spawn = new L2Spawn(template);
      spawn.setLocx(50335);
      spawn.setLocy(111275);
      spawn.setLocz(-1970);
      spawn.stopRespawn();
      result = spawn.spawnOne(false);
      template = null;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    RunBossSpawn rbs = new RunBossSpawn();
    ThreadPoolManager.getInstance().scheduleGeneral(rbs, 3600000L);
    _log.info("Fortress of Resistanse: Messenger spawned!");
    ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTimer(result), 3600000L);
  }

  public void BossSpawn()
  {
    if (!_clansDamageInfo.isEmpty()) {
      _clansDamageInfo.clear();
    }
    L2NpcInstance result = null;
    try
    {
      L2NpcTemplate template = NpcTable.getInstance().getTemplate(35368);

      L2Spawn spawn = new L2Spawn(template);
      spawn.setLocx(44525);
      spawn.setLocy(108867);
      spawn.setLocz(-2020);
      spawn.stopRespawn();
      result = spawn.spawnOne(false);
      template = null;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    _log.info("Fortress of Resistanse: Boss spawned!");
    Announce("Capture of Partisan Hideout has begun!");
    Announce("You have one hour to kill Nurka!");

    _nurka = ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTimer(result), 3600000L);
    _announce = ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceInfo("No one can`t kill Nurka! Partisan Hideout set free until next week!"), 3600000L);
  }

  public final boolean Conditions(L2PcInstance player)
  {
    return (player != null) && (player.getClan() != null) && (player.isClanLeader()) && (player.getClan().getAuctionBiddedAt() <= 0) && (ClanHallManager.getInstance().getClanHallByOwner(player.getClan()) == null) && (player.getClan().getLevel() > 2);
  }

  public void Announce(String message)
  {
    Announcements.getInstance().announceToAll(message);
  }

  public void CaptureFinish()
  {
    L2Clan clanIdMaxDamage = null;
    long tempMaxDamage = 0L;
    for (DamageInfo damageInfo : _clansDamageInfo.values())
    {
      if (damageInfo != null)
      {
        if (damageInfo._damage > tempMaxDamage)
        {
          tempMaxDamage = damageInfo._damage;
          clanIdMaxDamage = damageInfo._clan;
        }
      }
    }
    if (clanIdMaxDamage != null)
    {
      ClanHallManager.getInstance().setOwner(21, clanIdMaxDamage);
      clanIdMaxDamage.setReputationScore(clanIdMaxDamage.getReputationScore() + 600, true);
      update();

      Announce("Capture of Partisan Hideout is over.");
      Announce("Now its belong to: '" + clanIdMaxDamage.getName() + "' until next capture.");
    }
    else
    {
      Announce("Capture of Partisan Hideout is over.");
      Announce("No one can`t capture Partisan Hideout.");
    }

    _nurka.cancel(true);
    _announce.cancel(true);
  }

  public void addSiegeDamage(L2Clan clan, long damage)
  {
    DamageInfo clanDamage = (DamageInfo)_clansDamageInfo.get(Integer.valueOf(clan.getClanId()));
    if (clanDamage != null) {
      clanDamage._damage += damage;
    }
    else {
      clanDamage = new DamageInfo(null);
      clanDamage._clan = clan;
      clanDamage._damage += damage;
      _clansDamageInfo.put(Integer.valueOf(clan.getClanId()), clanDamage);
    }
  }

  private void update()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("UPDATE clanhall SET paidUntil=?, paid=? WHERE id=?");
      statement.setLong(1, System.currentTimeMillis() + 59760000L);
      statement.setInt(2, 1);
      statement.setInt(3, 21);
      statement.execute();
      statement.close();
      statement = null;
    }
    catch (Exception e)
    {
      System.out.println("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      try
      {
        con.close();
        con = null;
      }
      catch (Exception e)
      {
      }
    }
  }

  private class DamageInfo
  {
    public L2Clan _clan;
    public long _damage;

    private DamageInfo()
    {
    }
  }

  protected class RunMessengerSpawn
    implements Runnable
  {
    protected RunMessengerSpawn()
    {
    }

    public void run()
    {
      MessengerSpawn();
    }
  }

  protected class RunBossSpawn
    implements Runnable
  {
    protected RunBossSpawn()
    {
    }

    public void run()
    {
      BossSpawn();
    }
  }

  protected class DeSpawnTimer
    implements Runnable
  {
    L2NpcInstance _npc = null;

    public DeSpawnTimer(L2NpcInstance npc) {
      _npc = npc;
    }

    public void run()
    {
      _npc.onDecay();
    }
  }

  protected class AnnounceInfo
    implements Runnable
  {
    String _message;

    public AnnounceInfo(String message)
    {
      _message = message;
    }

    public void run() {
      Announce(_message);
    }
  }
}