package net.sf.l2j.gameserver.instancemanager.clanhallsiege;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.ClanHallSiege;
import net.sf.l2j.gameserver.model.zone.type.L2ClanHallZone;
import net.sf.l2j.gameserver.taskmanager.ExclusiveTask;

public class FortressOfTheDeadManager extends ClanHallSiege
{
  protected static final Logger _log = Logger.getLogger(FortressOfTheDeadManager.class.getName());
  private static FortressOfTheDeadManager _instance;
  private boolean _registrationPeriod = true;
  private int _clanCounter = 0;
  private Map<Integer, clansInfo> _clansInfo = new HashMap();
  private L2ClanHallZone zone;
  public ClanHall clanhall = ClanHallManager.getInstance().getClanHallById(64);
  private clansInfo _ownerClanInfo = new clansInfo(null);
  private Map<Integer, DamageInfo> _clansDamageInfo;
  private L2Clan _clan;
  private final ExclusiveTask _startSiegeTask = new ExclusiveTask()
  {
    protected void onElapsed()
    {
      if (getIsInProgress())
      {
        cancel();
        return;
      }
      long timeRemaining = getSiegeDate().getTimeInMillis() - System.currentTimeMillis();
      if (timeRemaining <= 0L)
      {
        startSiege();
        cancel();
        return;
      }
      schedule(timeRemaining);
    }
  };

  private final ExclusiveTask _endSiegeTask = new ExclusiveTask()
  {
    protected void onElapsed()
    {
      if (!getIsInProgress())
      {
        cancel();
        return;
      }
      long timeRemaining = _siegeEndDate.getTimeInMillis() - System.currentTimeMillis();
      if (timeRemaining <= 0L)
      {
        endSiege(false);
        cancel();
        return;
      }
      schedule(timeRemaining);
    }
  };

  public static final FortressOfTheDeadManager getInstance()
  {
    if (_instance == null)
      _instance = new FortressOfTheDeadManager();
    return _instance;
  }

  private FortressOfTheDeadManager()
  {
    _log.info("Fortress of The Dead");
    long siegeDate = restoreSiegeDate(64);
    Calendar tmpDate = Calendar.getInstance();
    tmpDate.setTimeInMillis(siegeDate);
    setSiegeDate(tmpDate);
    setNewSiegeDate(siegeDate, 64, 22);
    _clansDamageInfo = new HashMap();

    _startSiegeTask.schedule(1000L);
  }

  public void startSiege()
  {
    setRegistrationPeriod(false);
    if (_clansInfo.size() == 0)
    {
      endSiege(false);
      return;
    }
    if ((_clansInfo.size() == 1) && (this.clanhall.getOwnerId() == 0))
    {
      endSiege(false);
      return;
    }
    if ((_clansInfo.size() == 1) && (this.clanhall.getOwnerId() != 0))
    {
      L2Clan clan = null;
      for (clansInfo a : _clansInfo.values())
        clan = ClanTable.getInstance().getClanByName(a._clanName);
      setIsInProgress(true);
      _siegeEndDate = Calendar.getInstance();
      _siegeEndDate.add(12, 60);
      _endSiegeTask.schedule(1000L);
      return;
    }
    if (!_clansDamageInfo.isEmpty())
      _clansDamageInfo.clear();
    setIsInProgress(true);
    ClanHall clanhall = ClanHallManager.getInstance().getClanHallById(64);
    if (!ClanHallManager.getInstance().isFree(clanhall.getId()))
    {
      ClanTable.getInstance().getClan(clanhall.getOwnerId()).broadcastClanStatus();
      ClanHallManager.getInstance().setFree(clanhall.getId());
      clanhall.banishForeigners();
    }
    _siegeEndDate = Calendar.getInstance();
    _siegeEndDate.add(12, 60);
    _endSiegeTask.schedule(1000L);
  }

  public void endSiege(boolean type)
  {
    setIsInProgress(false);
    if ((type = 1) != 0)
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
        ClanHall clanhall = null;
        clanhall = ClanHallManager.getInstance().getClanHallById(64);
        ClanHallManager.getInstance().setOwner(clanhall.getId(), clanIdMaxDamage);
        _clansInfo.clear();
        _clanCounter = 0;
        _clan.setReputationScore(_clan.getReputationScore() + 600, true);
      }
      _log.info("The siege of Fortress of the Dead has finished");
    }
    setNewSiegeDate(getSiegeDate().getTimeInMillis(), 64, 22);
    _startSiegeTask.schedule(1000L);
  }

  public L2Clan checkHaveWinner()
  {
    L2Clan res = null;
    for (String clanName : getRegisteredClans())
    {
      clan = ClanTable.getInstance().getClanByName(clanName);
    }
    L2Clan clan;
    return null;
  }

  public void setRegistrationPeriod(boolean par)
  {
    _registrationPeriod = par;
  }

  public boolean isRegistrationPeriod()
  {
    return _registrationPeriod;
  }

  public boolean isClanRegister(L2Clan Clan, String clanName)
  {
    if (Clan == null)
      return false;
    clansInfo regClans = (clansInfo)_clansInfo.get(Integer.valueOf(Clan.getClanId()));

    return (regClans != null) && 
      (regClans._clans.contains(clanName));
  }

  public boolean isClanOnSiege(L2Clan Clan)
  {
    if (Clan.getClanId() == clanhall.getOwnerId())
      return true;
    clansInfo regClans = (clansInfo)_clansInfo.get(Integer.valueOf(Clan.getClanId()));

    return regClans != null;
  }

  public synchronized int registerClanOnSiege(L2PcInstance player, L2Clan Clan)
  {
    if (_clanCounter == 5) {
      return 2;
    }
    _clanCounter += 1;
    clansInfo regClans = (clansInfo)_clansInfo.get(Integer.valueOf(Clan.getClanId()));
    if (regClans == null)
      regClans = new clansInfo(null);
    regClans._clanName = Clan.getName();
    _clansInfo.put(Integer.valueOf(Clan.getClanId()), regClans);

    return 1;
  }

  public boolean unRegisterClan(L2Clan Clan)
  {
    if (_clansInfo.remove(Integer.valueOf(Clan.getClanId())) != null)
    {
      _clanCounter -= 1;
      return true;
    }
    return false;
  }

  public FastList<String> getRegisteredClans()
  {
    FastList clans = new FastList();
    for (clansInfo a : _clansInfo.values())
    {
      clans.add(a._clanName);
    }
    return clans;
  }

  public void addSiegeDamage(L2Clan clan, double damage)
  {
    setIsInProgress(true);
    for (String clanName : getRegisteredClans())
    {
      clan = ClanTable.getInstance().getClanByName(clanName);
      DamageInfo clanDamage = (DamageInfo)_clansDamageInfo.get(Integer.valueOf(clan.getClanId()));
      if (clanDamage != null)
      {
        DamageInfo tmp73_71 = clanDamage; tmp73_71._damage = ()(tmp73_71._damage + damage);
      }
      else {
        clanDamage = new DamageInfo(null);
        clanDamage._clan = clan;
        DamageInfo tmp106_104 = clanDamage; tmp106_104._damage = ()(tmp106_104._damage + damage);
        _clansDamageInfo.put(Integer.valueOf(clan.getClanId()), tmp106_104);
      }
    }
  }

  public int getClansCount(String Clan)
  {
    for (clansInfo a : _clansInfo.values())
      if (a._clanName == Clan)
        return a._clans.size();
    return 0;
  }

  private class clansInfo
  {
    public String _clanName;
    public FastList<String> _clans = new FastList();

    private clansInfo()
    {
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
}