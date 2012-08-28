package net.sf.l2j.gameserver.instancemanager.clanhallsiege;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeFlagInstance;
import net.sf.l2j.gameserver.model.actor.status.NpcStatus;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.ClanHallSiege;
import net.sf.l2j.gameserver.model.zone.type.L2ClanHallZone;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.taskmanager.ExclusiveTask;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class BanditStrongholdSiege extends ClanHallSiege
{
  protected static final Logger _log = Logger.getLogger(BanditStrongholdSiege.class.getName());
  private static BanditStrongholdSiege _instance;
  private boolean _registrationPeriod = false;
  private int _clanCounter = 0;
  private Map<Integer, clanPlayersInfo> _clansInfo = new HashMap();
  private L2ClanHallZone zone;
  public ClanHall clanhall = ClanHallManager.getInstance().getClanHallById(35);
  private clanPlayersInfo _ownerClanInfo = new clanPlayersInfo(null);
  private boolean _finalStage = false;
  private ScheduledFuture<?> _midTimer;
  private final ExclusiveTask _startSiegeTask = new ExclusiveTask()
  {
    protected void onElapsed()
    {
      if (getIsInProgress())
      {
        cancel();
        return;
      }
      Calendar siegeStart = Calendar.getInstance();
      siegeStart.setTimeInMillis(getSiegeDate().getTimeInMillis());
      long registerTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
      siegeStart.add(10, 1);
      long siegeTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
      long remaining = registerTimeRemaining;
      if (registerTimeRemaining <= 0L)
      {
        if (!isRegistrationPeriod())
        {
          if (clanhall.getOwnerId() != 0)
            _ownerClanInfo._clanName = ClanTable.getInstance().getClan(clanhall.getOwnerId()).getName();
          else
            _ownerClanInfo._clanName = "";
          setRegistrationPeriod(true);
          anonce("Attention! The period of registration at the siege clan hall, Bandit Stronghold.", 2);
          remaining = siegeTimeRemaining;
        }
      }
      if (siegeTimeRemaining <= 0L)
      {
        startSiege();
        cancel();
        return;
      }
      schedule(remaining);
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
        endSiege(true);
        cancel();
        return;
      }
      schedule(timeRemaining);
    }
  };

  private final ExclusiveTask _mobControlTask = new ExclusiveTask()
  {
    protected void onElapsed()
    {
      int mobCount = 0;
      for (BanditStrongholdSiege.clanPlayersInfo cl : _clansInfo.values())
        if (cl._mob.isDead())
        {
          L2Clan clan = ClanTable.getInstance().getClanByName(cl._clanName);
          unRegisterClan(clan);
        }
        else {
          mobCount++;
        }
      teleportPlayers();
      if (mobCount < 2) {
        if (_finalStage)
        {
          _siegeEndDate = Calendar.getInstance();
          _endSiegeTask.cancel();
          _endSiegeTask.schedule(5000L);
        }
        else
        {
          _midTimer.cancel(false);
          ThreadPoolManager.getInstance().scheduleGeneral(new BanditStrongholdSiege.midSiegeStep(BanditStrongholdSiege.this, null), 5000L);
        }
      }
      else schedule(3000L);
    }
  };

  public static final BanditStrongholdSiege getInstance()
  {
    if (_instance == null)
      _instance = new BanditStrongholdSiege();
    return _instance;
  }

  private BanditStrongholdSiege()
  {
    _log.info("SiegeManager of Bandit Stronghold");
    long siegeDate = restoreSiegeDate(35);
    Calendar tmpDate = Calendar.getInstance();
    tmpDate.setTimeInMillis(siegeDate);
    setSiegeDate(tmpDate);
    setNewSiegeDate(siegeDate, 35, 22);

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
    if ((_clansInfo.size() == 1) && (clanhall.getOwnerId() == 0))
    {
      endSiege(false);
      return;
    }
    if ((_clansInfo.size() == 1) && (clanhall.getOwnerId() != 0))
    {
      L2Clan clan = null;
      for (clanPlayersInfo a : _clansInfo.values())
        clan = ClanTable.getInstance().getClanByName(a._clanName);
      setIsInProgress(true);

      startSecondStep(clan);
      anonce("Take place at the siege of his headquarters.", 1);
      _siegeEndDate = Calendar.getInstance();
      _siegeEndDate.add(12, 30);
      _endSiegeTask.schedule(1000L);
      return;
    }
    setIsInProgress(true);

    spawnFlags();
    gateControl(1);
    anonce("Take place at the siege of his headquarters.", 1);
    ThreadPoolManager.getInstance().scheduleGeneral(new startFirstStep(null), 300000L);
    _midTimer = ThreadPoolManager.getInstance().scheduleGeneral(new midSiegeStep(null), 1500000L);
    _siegeEndDate = Calendar.getInstance();
    _siegeEndDate.add(12, 60);
    _endSiegeTask.schedule(1000L);
  }

  public void startSecondStep(L2Clan winner)
  {
    FastList winPlayers = getInstance().getRegisteredPlayers(winner);
    unSpawnAll();
    _clansInfo.clear();
    clanPlayersInfo regPlayers = new clanPlayersInfo(null);
    regPlayers._clanName = winner.getName();
    regPlayers._players = winPlayers;
    _clansInfo.put(Integer.valueOf(winner.getClanId()), regPlayers);
    _clansInfo.put(Integer.valueOf(clanhall.getOwnerId()), _ownerClanInfo);
    spawnFlags();
    gateControl(1);
    _finalStage = true;
    anonce("Take place at the siege of his headquarters.", 1);
    ThreadPoolManager.getInstance().scheduleGeneral(new startFirstStep(null), 300000L);
  }

  public void endSiege(boolean par)
  {
    _mobControlTask.cancel();
    _finalStage = false;
    if (par)
    {
      L2Clan winner = checkHaveWinner();
      if (winner != null)
      {
        ClanHallManager.getInstance().setOwner(clanhall.getId(), winner);
        anonce("Attention! Clan hall, Bandit Stronghold was conquered by the clan " + winner.getName(), 2);
      }
      else {
        anonce("Attention! Clan hall, Bandit Stronghold did not get a new owner", 2);
      }
    }
    setIsInProgress(false);

    unSpawnAll();
    _clansInfo.clear();
    _clanCounter = 0;
    teleportPlayers();
    setNewSiegeDate(getSiegeDate().getTimeInMillis(), 35, 22);
    _startSiegeTask.schedule(1000L);
  }

  public void unSpawnAll()
  {
    for (String clanName : getRegisteredClans())
    {
      L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
      L2MonsterInstance mob = getQuestMob(clan);
      L2SiegeFlagInstance flag = getSiegeFlag(clan);
      if (mob != null)
        mob.deleteMe();
      if (flag != null)
        flag.deleteMe();
    }
  }

  public void gateControl(int val)
  {
    if (val == 1)
    {
      DoorTable.getInstance().getDoor(Integer.valueOf(22170001)).openMe();
      DoorTable.getInstance().getDoor(Integer.valueOf(22170002)).openMe();
      DoorTable.getInstance().getDoor(Integer.valueOf(22170003)).closeMe();
      DoorTable.getInstance().getDoor(Integer.valueOf(22170004)).closeMe();
    }
    else if (val == 2)
    {
      DoorTable.getInstance().getDoor(Integer.valueOf(22170001)).closeMe();
      DoorTable.getInstance().getDoor(Integer.valueOf(22170002)).closeMe();
      DoorTable.getInstance().getDoor(Integer.valueOf(22170003)).closeMe();
      DoorTable.getInstance().getDoor(Integer.valueOf(22170004)).closeMe();
    }
  }

  public void teleportPlayers()
  {
    for (L2Character cha : zone.getCharactersInside().values())
      if ((cha instanceof L2PcInstance))
      {
        L2Clan clan = ((L2PcInstance)cha).getClan();
        if (!isPlayerRegister(clan, cha.getName()))
          cha.teleToLocation(88404, -21821, -2276);
      }
  }

  public L2Clan checkHaveWinner()
  {
    L2Clan res = null;
    int questMobCount = 0;
    for (String clanName : getRegisteredClans())
    {
      L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
      if (getQuestMob(clan) != null)
      {
        res = clan;
        questMobCount++;
      }
    }
    if (questMobCount > 1)
      return null;
    return res;
  }

  public void spawnFlags()
  {
  }

  public void setRegistrationPeriod(boolean par)
  {
    _registrationPeriod = par;
  }

  public boolean isRegistrationPeriod()
  {
    return _registrationPeriod;
  }

  public boolean isPlayerRegister(L2Clan playerClan, String playerName)
  {
    if (playerClan == null)
      return false;
    clanPlayersInfo regPlayers = (clanPlayersInfo)_clansInfo.get(Integer.valueOf(playerClan.getClanId()));

    return (regPlayers != null) && 
      (regPlayers._players.contains(playerName));
  }

  public boolean isClanOnSiege(L2Clan playerClan)
  {
    if (playerClan.getClanId() == clanhall.getOwnerId())
      return true;
    clanPlayersInfo regPlayers = (clanPlayersInfo)_clansInfo.get(Integer.valueOf(playerClan.getClanId()));

    return regPlayers != null;
  }

  public synchronized int registerClanOnSiege(L2PcInstance player, L2Clan playerClan)
  {
    if (_clanCounter == 5)
      return 2;
    L2ItemInstance item = player.getInventory().getItemByItemId(5009);
    if ((item != null) && (player.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false)))
    {
      _clanCounter += 1;
      clanPlayersInfo regPlayers = (clanPlayersInfo)_clansInfo.get(Integer.valueOf(playerClan.getClanId()));
      if (regPlayers == null)
      {
        regPlayers = new clanPlayersInfo(null);
        regPlayers._clanName = playerClan.getName();
        _clansInfo.put(Integer.valueOf(playerClan.getClanId()), regPlayers);
      }
    }
    else {
      return 1;
    }return 0;
  }

  public boolean unRegisterClan(L2Clan playerClan)
  {
    if (_clansInfo.remove(Integer.valueOf(playerClan.getClanId())) != null)
    {
      _clanCounter -= 1;
      return true;
    }
    return false;
  }

  public FastList<String> getRegisteredClans()
  {
    FastList clans = new FastList();
    for (clanPlayersInfo a : _clansInfo.values())
    {
      clans.add(a._clanName);
    }
    return clans;
  }

  public FastList<String> getRegisteredPlayers(L2Clan playerClan)
  {
    if (playerClan.getClanId() == clanhall.getOwnerId())
      return _ownerClanInfo._players;
    clanPlayersInfo regPlayers = (clanPlayersInfo)_clansInfo.get(Integer.valueOf(playerClan.getClanId()));
    if (regPlayers != null)
      return regPlayers._players;
    return null;
  }

  public L2SiegeFlagInstance getSiegeFlag(L2Clan playerClan)
  {
    clanPlayersInfo clanInfo = (clanPlayersInfo)_clansInfo.get(Integer.valueOf(playerClan.getClanId()));
    if (clanInfo != null)
      return clanInfo._flag;
    return null;
  }

  public L2MonsterInstance getQuestMob(L2Clan clan)
  {
    clanPlayersInfo clanInfo = (clanPlayersInfo)_clansInfo.get(Integer.valueOf(clan.getClanId()));
    if (clanInfo != null)
      return clanInfo._mob;
    return null;
  }

  public int getPlayersCount(String playerClan)
  {
    for (clanPlayersInfo a : _clansInfo.values())
      if (a._clanName == playerClan)
        return a._players.size();
    return 0;
  }

  public void addPlayer(L2Clan playerClan, String playerName)
  {
    if ((playerClan.getClanId() == clanhall.getOwnerId()) && 
      (_ownerClanInfo._players.size() < 18) && 
      (!_ownerClanInfo._players.contains(playerName)))
    {
      _ownerClanInfo._players.add(playerName);
      return;
    }
    clanPlayersInfo regPlayers = (clanPlayersInfo)_clansInfo.get(Integer.valueOf(playerClan.getClanId()));
    if ((regPlayers != null) && 
      (regPlayers._players.size() < 18) && 
      (!regPlayers._players.contains(playerName)))
      regPlayers._players.add(playerName);
  }

  public void removePlayer(L2Clan playerClan, String playerName)
  {
    if ((playerClan.getClanId() == clanhall.getOwnerId()) && 
      (_ownerClanInfo._players.contains(playerName)))
    {
      _ownerClanInfo._players.remove(playerName);
      return;
    }
    clanPlayersInfo regPlayers = (clanPlayersInfo)_clansInfo.get(Integer.valueOf(playerClan.getClanId()));
    if ((regPlayers != null) && 
      (regPlayers._players.contains(playerName)))
      regPlayers._players.remove(playerName);
  }

  public void anonce(String text, int type)
  {
    CreatureSay cs;
    CreatureSay cs;
    if (type == 1)
    {
      cs = new CreatureSay(0, 1, "Messenger", text);
      for (String clanName : getRegisteredClans())
      {
        L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
        for (String playerName : getRegisteredPlayers(clan))
        {
          L2PcInstance cha = L2World.getInstance().getPlayer(playerName);
          if (cha != null)
            cha.sendPacket(cs);
        }
      }
    }
    else
    {
      cs = new CreatureSay(0, 1, "Messenger", text);

      for (L2PcInstance player : L2World.getInstance().getAllPlayers())
      {
        if (player.getInstanceId() == 0)
        {
          player.sendPacket(cs);
        }
      }
    }
  }

  private class clanPlayersInfo
  {
    public String _clanName;
    public L2SiegeFlagInstance _flag = null;
    public L2MonsterInstance _mob = null;
    public FastList<String> _players = new FastList();

    private clanPlayersInfo()
    {
    }
  }

  private class startFirstStep
    implements Runnable
  {
    private startFirstStep()
    {
    }

    public void run()
    {
      teleportPlayers();
      gateControl(2);
      int mobCounter = 1;
      for (String clanName : getRegisteredClans())
      {
        L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
        L2NpcTemplate template = NpcTable.getInstance().getTemplate(35427 + mobCounter);

        L2MonsterInstance questMob = new L2MonsterInstance(IdFactory.getInstance().getNextId(), template);
        questMob.setHeading(100);
        questMob.getStatus().setCurrentHpMp(questMob.getMaxHp(), questMob.getMaxMp());
        if (mobCounter == 1)
          questMob.spawnMe(83752, -17354, -1828);
        else if (mobCounter == 2)
          questMob.spawnMe(82018, -15126, -1829);
        else if (mobCounter == 3)
          questMob.spawnMe(85320, -16191, -1823);
        else if (mobCounter == 4)
          questMob.spawnMe(81522, -16503, -1829);
        else if (mobCounter == 5)
          questMob.spawnMe(83786, -15369, -1828);
        BanditStrongholdSiege.clanPlayersInfo regPlayers = (BanditStrongholdSiege.clanPlayersInfo)_clansInfo.get(Integer.valueOf(clan.getClanId()));
        regPlayers._mob = questMob;
        mobCounter++;
      }
      _mobControlTask.schedule(3000L);
      anonce("The battle began. Kill the enemy NPC", 1);
    }
  }

  private class midSiegeStep
    implements Runnable
  {
    private midSiegeStep()
    {
    }

    public void run()
    {
      _mobControlTask.cancel();
      L2Clan winner = checkHaveWinner();
      if (winner != null)
      {
        if (clanhall.getOwnerId() == 0)
        {
          ClanHallManager.getInstance().setOwner(clanhall.getId(), winner);
          anonce("Attention! Clan hall, Bandit Stronghold was conquered by the clan " + winner.getName(), 2);
          endSiege(false);
        }
        else {
          startSecondStep(winner);
        }
      }
      else
        endSiege(true);
    }
  }
}