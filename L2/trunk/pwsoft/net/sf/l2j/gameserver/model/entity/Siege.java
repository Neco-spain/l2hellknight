package net.sf.l2j.gameserver.model.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.MercTicketManager;
import net.sf.l2j.gameserver.instancemanager.SiegeGuardManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager.SiegeSpawn;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.L2SiegeClan.SiegeClanType;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2ArtefactInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2ControlTowerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.model.entity.olympiad.OlympiadDiary;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2j.gameserver.network.serverpackets.SiegeInfo;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.clanhalls.BanditStronghold;
import scripts.zone.type.L2CastleZone;
import scripts.zone.type.L2SiegeWaitZone;

public class Siege
{
  private static final Logger _log = AbstractLogger.getLogger(ClanHallManager.class.getName());

  private List<L2SiegeClan> _attackerClans = new FastList();
  private List<L2SiegeClan> _defenderClans = new FastList();
  private List<L2SiegeClan> _defenderWaitingClans = new FastList();
  private int _defenderRespawnDelayPenalty;
  private List<L2ArtefactInstance> _artifacts = new FastList();
  private List<L2ControlTowerInstance> _controlTowers = new FastList();
  private Castle[] _castle;
  private boolean _isInProgress = false;
  private boolean _isNormalSide = true;
  protected boolean _isRegistrationOver = false;
  protected Calendar _siegeEndDate;
  private SiegeGuardManager _siegeGuardManager;
  protected Calendar _siegeRegistrationEndDate;

  public Siege(Castle[] castle)
  {
    _castle = castle;
    _siegeGuardManager = new SiegeGuardManager(getCastle());

    startAutoTask();
  }

  public void endSiege()
  {
    if (getIsInProgress()) {
      announceToPlayer("\u0411\u0438\u0442\u0432\u0430 \u0437\u0430 " + getCastleName(getCastle().getCastleId()) + " \u043E\u043A\u043E\u043D\u0447\u0435\u043D\u0430!", false);

      removeFlags();
      teleportPlayer(TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.Town);
      teleportPlayer(TeleportWhoType.DefenderNotOwner, MapRegionTable.TeleportWhereType.Town);
      teleportPlayer(TeleportWhoType.Spectator, MapRegionTable.TeleportWhereType.Town);
      _isInProgress = false;
      updatePlayerSiegeStateFlags(true);
      saveCastleSiege();
      clearSiegeClan();
      removeArtifact();
      removeControlTower();
      if (Config.SIEGE_GUARDS_SPAWN) {
        _siegeGuardManager.unspawnSiegeGuard();
      }
      getCastle().spawnDoor();
      getCastle().getZone().updateZoneStatusForCharactersInside();

      if (getCastle().getOwnerId() > 0) {
        if (Config.SIEGE_GUARDS_SPAWN) {
          _siegeGuardManager.removeMercs();
        }
        getCastle().giveOwnerBonus();
        getCastle().giveClanBonus();
      }
    }
  }

  private void removeDefender(L2SiegeClan sc) {
    if (sc != null)
      getDefenderClans().remove(sc);
  }

  private void removeAttacker(L2SiegeClan sc)
  {
    if (sc != null)
      getAttackerClans().remove(sc);
  }

  private void addDefender(L2SiegeClan sc, L2SiegeClan.SiegeClanType type)
  {
    if (sc == null) {
      return;
    }
    sc.setType(type);
    getDefenderClans().add(sc);
  }

  private void addAttacker(L2SiegeClan sc) {
    if (sc == null) {
      return;
    }
    sc.setType(L2SiegeClan.SiegeClanType.ATTACKER);
    getAttackerClans().add(sc);
  }

  public void midVictory()
  {
    if (getIsInProgress())
    {
      if ((getCastle().getOwnerId() > 0) && 
        (Config.SIEGE_GUARDS_SPAWN)) {
        _siegeGuardManager.removeMercs();
      }

      if ((getDefenderClans().isEmpty()) && (getAttackerClans().size() == 1))
      {
        L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
        removeAttacker(sc_newowner);
        addDefender(sc_newowner, L2SiegeClan.SiegeClanType.OWNER);
        endSiege();
        return;
      }
      if (getCastle().getOwnerId() > 0)
      {
        int allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
        if (getDefenderClans().isEmpty())
        {
          if (allyId != 0) {
            boolean allinsamealliance = true;
            for (L2SiegeClan sc : getAttackerClans()) {
              if ((sc != null) && 
                (ClanTable.getInstance().getClan(sc.getClanId()).getAllyId() != allyId)) {
                allinsamealliance = false;
              }
            }

            if (allinsamealliance) {
              L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
              removeAttacker(sc_newowner);
              addDefender(sc_newowner, L2SiegeClan.SiegeClanType.OWNER);
              endSiege();
              return;
            }
          }
        }

        for (L2SiegeClan sc : getDefenderClans()) {
          if (sc != null) {
            removeDefender(sc);
            addAttacker(sc);
          }
        }

        L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
        removeAttacker(sc_newowner);
        addDefender(sc_newowner, L2SiegeClan.SiegeClanType.OWNER);

        if (allyId != 0) {
          FastTable cn = new FastTable();
          cn.addAll(ClanTable.getInstance().getClans());
          for (L2Clan clan : cn) {
            if (clan.getAllyId() == allyId) {
              L2SiegeClan sc = getAttackerClan(clan.getClanId());
              if (sc != null) {
                removeAttacker(sc);
                addDefender(sc, L2SiegeClan.SiegeClanType.DEFENDER);
              }
            }
          }
        }
        teleportPlayer(TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.SiegeFlag);
        teleportPlayer(TeleportWhoType.Spectator, MapRegionTable.TeleportWhereType.Town);

        removeDefenderFlags();
        getCastle().removeUpgrade();
        getCastle().spawnDoor(true);
        updatePlayerSiegeStateFlags(false);
      }
    }
  }

  public void startSiege()
  {
    if (!getIsInProgress()) {
      int id = getCastle().getCastleId();
      String CastleName = getCastleName(id);

      if ((getAttackerClans().size() <= 0) && (id != 21) && (id != 35))
      {
        SystemMessage sm;
        if (getCastle().getOwnerId() <= 0)
          sm = SystemMessage.id(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
        else {
          sm = SystemMessage.id(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
        }
        sm.addString(CastleName);
        Announcements.getInstance().announceToAll(sm);
        SystemMessage sm = null;
        return;
      }

      if (id == 35) {
        if (BanditStronghold.getCH().getAttackers().size() <= 1) {
          BanditStronghold.getCH().cancel();
          Announcements.getInstance().announceToAll(SystemMessage.id(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED).addString(CastleName));
          return;
        }
        BanditStronghold.getCH().startSiege();
      }

      _isNormalSide = true;
      _isInProgress = true;

      loadSiegeClan();

      if ((id != 21) && (id != 35)) {
        updatePlayerSiegeStateFlags(false);
      }

      teleportPlayer(TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.Town);

      spawnArtifact(id);
      spawnControlTower(id);
      getCastle().spawnDoor();
      if (Config.SIEGE_GUARDS_SPAWN) {
        spawnSiegeGuard();
      }
      MercTicketManager.getInstance().deleteTickets(id);
      _defenderRespawnDelayPenalty = 0;

      getCastle().getZone().updateZoneStatusForCharactersInside();

      _siegeEndDate = Calendar.getInstance();

      if ((id == 34) || (id == 64))
        _siegeEndDate.add(12, 60);
      else {
        _siegeEndDate.add(12, SiegeManager.getInstance().getSiegeLength());
      }

      ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(getCastle()), 1000L);

      if (!getCastle().isClanhall())
        ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleWaitersTeleportTask(getCastle()), getDefenderRespawnDelay());
      else if (!ClanHallManager.getInstance().isFree(id)) {
        ClanHallManager.getInstance().setFree(id);
      }

      announceToPlayer("\u041D\u0430\u0447\u0430\u043B\u0430\u0441\u044C \u0431\u0438\u0442\u0432\u0430 \u0437\u0430 " + CastleName + "", false);
      _log.info("Siege of " + CastleName + " started.");
    }
  }

  public void announceToPlayer(String message, boolean inAreaOnly)
  {
    if (inAreaOnly) {
      getCastle().getZone().announceToPlayers(message);
      return;
    }

    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
      player.sendMessage(message);
  }

  public void announceToPlayerSiegeEnd(int countdown)
  {
    getCastle().getZone().announceSmToPlayers(SystemMessage.id(SystemMessageId.SIEGE_END_FOR_S1_SECONDS).addNumber(countdown));
  }

  public void updatePlayerSiegeStateFlags(boolean clear)
  {
    for (L2SiegeClan siegeclan : getAttackerClans()) {
      if (siegeclan == null)
      {
        continue;
      }
      L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
      if (clan == null)
      {
        continue;
      }
      for (L2PcInstance member : clan.getOnlineMembers("")) {
        if (member == null)
        {
          continue;
        }
        if (clear)
          member.setSiegeState(0);
        else {
          member.setSiegeState(1);
        }
        member.sendPacket(new UserInfo(member));

        FastList players = member.getKnownList().getListKnownPlayers();
        L2PcInstance pc = null;
        FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
          {
            continue;
          }
          pc.sendPacket(new RelationChanged(member, member.getRelation(pc), member.isAutoAttackable(pc)));
        }
        players.clear();
        players = null;
        pc = null;
      }
    }
    for (L2SiegeClan siegeclan : getDefenderClans()) {
      if (siegeclan == null)
      {
        continue;
      }
      L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
      if (clan == null)
      {
        continue;
      }
      for (L2PcInstance member : clan.getOnlineMembers("")) {
        if (member == null)
        {
          continue;
        }
        if (clear)
          member.setSiegeState(0);
        else {
          member.setSiegeState(2);
        }
        member.sendPacket(new UserInfo(member));

        FastList players = member.getKnownList().getListKnownPlayers();
        L2PcInstance pc = null;
        FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
          {
            continue;
          }
          pc.sendPacket(new RelationChanged(member, member.getRelation(pc), member.isAutoAttackable(pc)));
        }
        players.clear();
        players = null;
        pc = null;
      }
    }
  }

  public void approveSiegeDefenderClan(int clanId)
  {
    if (clanId <= 0) {
      return;
    }
    saveSiegeClan(ClanTable.getInstance().getClan(clanId), 0, true);
    loadSiegeClan();
  }

  public boolean checkIfInZone(L2Object object)
  {
    return checkIfInZone(object.getX(), object.getY(), object.getZ());
  }

  public boolean checkIfInZone(int x, int y, int z)
  {
    return (getIsInProgress()) && (getCastle().checkIfInZone(x, y, z));
  }

  public boolean checkIsAttacker(L2Clan clan)
  {
    return getAttackerClan(clan) != null;
  }

  public boolean checkIsDefender(L2Clan clan)
  {
    return getDefenderClan(clan) != null;
  }

  public boolean checkIsDefenderWaiting(L2Clan clan)
  {
    return getDefenderWaitingClan(clan) != null;
  }

  public void clearSiegeClan()
  {
    Connect con = null;
    PreparedStatement statement = null;
    PreparedStatement statement2 = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=?");
      statement.setInt(1, getCastle().getCastleId());
      statement.execute();
      Close.S(statement);

      if (getCastle().getOwnerId() > 0) {
        statement2 = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?");
        statement2.setInt(1, getCastle().getOwnerId());
        statement2.execute();
        Close.S(statement2);
      }
      getAttackerClans().clear();
      getDefenderClans().clear();
      getDefenderWaitingClans().clear();
    } catch (Exception e) {
      _log.warning("Exception: clearSiegeClan(): " + e.getMessage());
      e.printStackTrace();
    } finally {
      Close.S(statement2);
      Close.CS(con, statement);
    }
  }

  public void clearSiegeWaitingClan()
  {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and type = 2");
      statement.setInt(1, getCastle().getCastleId());
      statement.execute();

      getDefenderWaitingClans().clear();
    } catch (Exception e) {
      _log.warning("Exception: clearSiegeWaitingClan(): " + e.getMessage());
      e.printStackTrace();
    } finally {
      Close.CS(con, statement);
    }
  }

  public List<L2PcInstance> getAttackersInZone()
  {
    List players = new FastList();

    for (L2SiegeClan siegeclan : getAttackerClans()) {
      if (siegeclan == null) {
        continue;
      }
      L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
      if (clan == null) {
        continue;
      }
      for (L2PcInstance player : clan.getOnlineMembers("")) {
        if (checkIfInZone(player.getX(), player.getY(), player.getZ())) {
          players.add(player);
        }
      }
    }
    return players;
  }

  public List<L2PcInstance> getDefendersButNotOwnersInZone()
  {
    List players = new FastList();

    for (L2SiegeClan siegeclan : getDefenderClans()) {
      if (siegeclan == null) {
        continue;
      }
      L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
      if ((clan == null) || 
        (clan.getClanId() == getCastle().getOwnerId())) {
        continue;
      }
      for (L2PcInstance player : clan.getOnlineMembers("")) {
        if (checkIfInZone(player.getX(), player.getY(), player.getZ())) {
          players.add(player);
        }
      }
    }
    return players;
  }

  public List<L2PcInstance> getPlayersInZone()
  {
    return getCastle().getZone().getAllPlayers();
  }

  public List<L2PcInstance> getOwnersInZone()
  {
    List players = new FastList();

    for (L2SiegeClan siegeclan : getDefenderClans()) {
      if (siegeclan == null) {
        continue;
      }
      L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
      if ((clan == null) || 
        (clan.getClanId() != getCastle().getOwnerId())) {
        continue;
      }
      for (L2PcInstance player : clan.getOnlineMembers("")) {
        if (checkIfInZone(player.getX(), player.getY(), player.getZ())) {
          players.add(player);
        }
      }
    }
    return players;
  }

  public List<L2PcInstance> getSpectatorsInZone()
  {
    List players = new FastList();

    for (L2PcInstance player : L2World.getInstance().getAllPlayers()) {
      if ((player == null) || 
        (!player.isInsideZone(4)) || (player.getSiegeState() != 0)) {
        continue;
      }
      if (checkIfInZone(player.getX(), player.getY(), player.getZ())) {
        players.add(player);
      }
    }

    return players;
  }

  public void killedCT(L2NpcInstance ct)
  {
    _defenderRespawnDelayPenalty += SiegeManager.getInstance().getControlTowerLosePenalty();
  }

  public void listRegisterClan(L2PcInstance player)
  {
    player.sendPacket(new SiegeInfo(getCastle()));
  }

  public void registerAttacker(L2PcInstance player)
  {
    registerAttacker(player, false);
  }

  public void registerAttacker(L2PcInstance player, boolean force)
  {
    if (player.getClan() == null) {
      return;
    }
    int allyId = 0;
    if (getCastle().getOwnerId() != 0) {
      try {
        allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
      } catch (Exception e) {
        allyId = 0;
      }
    }
    if ((allyId != 0) && 
      (player.getClan().getAllyId() == allyId) && (!force)) {
      player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u0442\u044C\u0441\u044F \u043D\u0430 \u0430\u0442\u0430\u043A\u0443 \u043A\u043B\u0430\u043D\u0430 \u0432\u0430\u0448\u0435\u0433\u043E \u0430\u043B\u044C\u044F\u043D\u0441\u0430");
      return;
    }

    if ((force) || (checkIfCanRegister(player)))
      saveSiegeClan(player.getClan(), 1, false);
  }

  public void registerDefender(L2PcInstance player)
  {
    registerDefender(player, false);
  }

  public void registerDefender(L2PcInstance player, boolean force) {
    if (getCastle().getOwnerId() <= 0)
      player.sendMessage("\u041D\u0435\u043B\u044C\u0437\u044F \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u0442\u044C\u0441\u044F \u043D\u0430 \u0437\u0430\u0449\u0438\u0442\u0443");
    else if ((getCastle().getCastleId() == 34) || (getCastle().getCastleId() == 64) || (getCastle().getCastleId() == 21))
      player.sendMessage("\u041D\u0435\u043B\u044C\u0437\u044F \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u0442\u044C\u0441\u044F \u043D\u0430 \u0437\u0430\u0449\u0438\u0442\u0443");
    else if ((force) || (checkIfCanRegister(player)))
      saveSiegeClan(player.getClan(), 2, false);
  }

  public void removeSiegeClan(int clanId)
  {
    if (clanId <= 0) {
      return;
    }

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and clan_id=?");
      statement.setInt(1, getCastle().getCastleId());
      statement.setInt(2, clanId);
      statement.execute();

      loadSiegeClan();
    } catch (Exception e) {
    } finally {
      Close.CS(con, statement);
    }
  }

  public void removeSiegeClan(L2Clan clan)
  {
    if ((clan == null) || (clan.getHasCastle() == getCastle().getCastleId()) || (!SiegeManager.getInstance().checkIsRegistered(clan, getCastle().getCastleId())))
    {
      return;
    }
    removeSiegeClan(clan.getClanId());
  }

  public void removeSiegeClan(L2PcInstance player)
  {
    removeSiegeClan(player.getClan());
  }

  public void startAutoTask()
  {
    correctSiegeDateTime();

    _log.info("Siege of " + getCastleName(getCastle().getCastleId()) + ": " + getCastle().getSiegeDate().getTime());

    loadSiegeClan();

    _siegeRegistrationEndDate = Calendar.getInstance();
    _siegeRegistrationEndDate.setTimeInMillis(getCastle().getSiegeDate().getTimeInMillis());
    _siegeRegistrationEndDate.add(5, -1);

    ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(getCastle()), 1000L);
  }

  public void teleportPlayer(TeleportWhoType teleportWho, MapRegionTable.TeleportWhereType teleportWhere)
  {
    List players;
    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$entity$Siege$TeleportWhoType[teleportWho.ordinal()]) {
    case 1:
      players = getOwnersInZone();
      break;
    case 2:
      players = getAttackersInZone();
      break;
    case 3:
      players = getDefendersButNotOwnersInZone();
      break;
    case 4:
      players = getSpectatorsInZone();
      break;
    default:
      players = getPlayersInZone();
    }

    for (L2PcInstance player : players) {
      if ((player == null) || 
        (player.isGM()) || (player.isInJail())) {
        continue;
      }
      player.teleToLocation(teleportWhere);

      if ((player.isHero()) && (player.getClan() != null) && (player.getClan().getClanId() == getCastle().getOwnerId()))
        OlympiadDiary.addRecord(player, "\u041F\u043E\u0431\u0435\u0434\u0430 \u0432 \u0431\u0438\u0442\u0432\u0435 \u0437\u0430 \u0437\u0430\u043C\u043E\u043A" + getCastleName(getCastle().getCastleId()) + ".");
    }
  }

  private void addAttacker(int clanId)
  {
    getAttackerClans().add(new L2SiegeClan(clanId, L2SiegeClan.SiegeClanType.ATTACKER));
  }

  private void addDefender(int clanId)
  {
    getDefenderClans().add(new L2SiegeClan(clanId, L2SiegeClan.SiegeClanType.DEFENDER));
  }

  private void addDefender(int clanId, L2SiegeClan.SiegeClanType type)
  {
    getDefenderClans().add(new L2SiegeClan(clanId, type));
  }

  private void addDefenderWaiting(int clanId)
  {
    getDefenderWaitingClans().add(new L2SiegeClan(clanId, L2SiegeClan.SiegeClanType.DEFENDER_PENDING));
  }

  private boolean checkIfCanRegister(L2PcInstance player)
  {
    if (getIsRegistrationOver())
      player.sendMessage("\u0412\u0440\u0435\u043C\u044F \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438 \u043D\u0430 \u0431\u0438\u0442\u0432\u0443 \u0437\u0430 " + getCastle().getName() + " \u0438\u0441\u0442\u0435\u043A\u043B\u043E");
    else if (getIsInProgress())
      player.sendPacket(Static.SIEGE_NO_REG);
    else if ((player.getClan() == null) || (player.getClan().getLevel() < SiegeManager.getInstance().getSiegeClanMinLevel()))
    {
      player.sendMessage("\u0422\u043E\u043B\u044C\u043A\u043E \u043A\u043B\u0430\u043D\u044B \u0432\u044B\u0448\u0435 " + SiegeManager.getInstance().getSiegeClanMinLevel() + " \u0443\u0440\u043E\u0432\u043D\u044F \u043C\u043E\u0433\u0443\u0442 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u0442\u044C\u0441\u044F");
    } else if (player.getClan().getHasCastle() > 0)
      player.sendPacket(Static.SIEGE_HAVE_CASTLE);
    else if (player.getClan().getClanId() == getCastle().getOwnerId())
      player.sendPacket(Static.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
    else if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), getCastle().getCastleId())) {
      player.sendMessage("\u0412\u044B \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u044B");
    }
    else
    {
      return true;
    }

    return false;
  }

  private void correctSiegeDateTime()
  {
    boolean corrected = false;

    if (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
    {
      corrected = true;
      setNextSiegeDate();
    }

    if (getCastle().getSiegeDate().get(7) != getCastle().getSiegeDayOfWeek()) {
      corrected = true;
      getCastle().getSiegeDate().set(7, getCastle().getSiegeDayOfWeek());
    }

    if (getCastle().getSiegeDate().get(11) != getCastle().getSiegeHourOfDay()) {
      corrected = true;
      getCastle().getSiegeDate().set(11, getCastle().getSiegeHourOfDay());
    }

    getCastle().getSiegeDate().set(12, 0);

    if (corrected)
      saveSiegeDate();
  }

  private void loadSiegeClan()
  {
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      getAttackerClans().clear();
      getDefenderClans().clear();
      getDefenderWaitingClans().clear();

      if ((getCastle().getOwnerId() > 0) && ((getCastle().getCastleId() != 34) || (getCastle().getCastleId() != 64))) {
        addDefender(getCastle().getOwnerId(), L2SiegeClan.SiegeClanType.OWNER);
      }

      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);

      statement = con.prepareStatement("SELECT clan_id,type FROM siege_clans where castle_id=?");
      statement.setInt(1, getCastle().getCastleId());
      rs = statement.executeQuery();

      while (rs.next())
        switch (rs.getInt("type")) {
        case 0:
          addDefender(rs.getInt("clan_id"));
          break;
        case 1:
          addAttacker(rs.getInt("clan_id"));
          break;
        case 2:
          addDefenderWaiting(rs.getInt("clan_id"));
        }
    }
    catch (Exception e)
    {
      _log.warning("Exception: loadSiegeClan(): " + e.getMessage());
      e.printStackTrace();
    } finally {
      Close.CSR(con, statement, rs);
    }
  }

  private void removeArtifact()
  {
    if (_artifacts != null)
    {
      for (L2ArtefactInstance art : _artifacts) {
        if (art != null) {
          art.decayMe();
        }
      }
      _artifacts = null;
    }
  }

  private void removeControlTower()
  {
    if (_controlTowers != null)
    {
      for (L2ControlTowerInstance ct : _controlTowers) {
        if (ct != null) {
          ct.decayMe();
        }
      }

      _controlTowers = null;
    }
  }

  private void removeFlags()
  {
    for (L2SiegeClan sc : getAttackerClans()) {
      if (sc != null) {
        sc.removeFlag();
      }
    }
    for (L2SiegeClan sc : getDefenderClans())
      if (sc != null)
        sc.removeFlag();
  }

  private void removeDefenderFlags()
  {
    for (L2SiegeClan sc : getDefenderClans())
      if (sc != null)
        sc.removeFlag();
  }

  private void saveCastleSiege()
  {
    setNextSiegeDate();
    saveSiegeDate();
    startAutoTask();
  }

  private void saveSiegeDate()
  {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("Update castle set siegeDate = ? where id = ?");
      statement.setLong(1, getSiegeDate().getTimeInMillis());
      statement.setInt(2, getCastle().getCastleId());
      statement.execute();
    } catch (Exception e) {
      _log.warning("Exception: saveSiegeDate(): " + e.getMessage());
      e.printStackTrace();
    } finally {
      Close.CS(con, statement);
    }
  }

  private void saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration)
  {
    if (clan.getHasCastle() > 0) {
      return;
    }

    if ((getAttackerClan(clan) != null) || (getDefenderClan(clan) != null)) {
      return;
    }

    Connect con = null;
    PreparedStatement statement = null;
    try {
      if ((typeId == 0) || (typeId == 2) || (typeId == -1)) {
        if (getDefenderClans().size() + getDefenderWaitingClans().size() >= SiegeManager.getInstance().getDefenderMaxClans())
          return;
      }
      else if (getAttackerClans().size() >= SiegeManager.getInstance().getAttackerMaxClans())
      {
        return;
      }
      con = L2DatabaseFactory.getInstance().getConnection();
      if (!isUpdateRegistration) {
        statement = con.prepareStatement("INSERT INTO siege_clans (clan_id,castle_id,type,castle_owner) values (?,?,?,0)");
        statement.setInt(1, clan.getClanId());
        statement.setInt(2, getCastle().getCastleId());
        statement.setInt(3, typeId);
        statement.execute();
        Close.S(statement);
      } else {
        statement = con.prepareStatement("Update siege_clans set type = ? where castle_id = ? and clan_id = ?");
        statement.setInt(1, typeId);
        statement.setInt(2, getCastle().getCastleId());
        statement.setInt(3, clan.getClanId());
        statement.execute();
        Close.S(statement);
      }

      String CastleName = getCastleName(getCastle().getCastleId());

      if ((typeId == 0) || (typeId == -1)) {
        addDefender(clan.getClanId());
        announceToPlayer(clan.getName() + " \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043B\u0441\u044F \u043D\u0430 \u0437\u0430\u0449\u0438\u0442\u0443 " + CastleName, false);
      } else if (typeId == 1) {
        addAttacker(clan.getClanId());
        announceToPlayer(clan.getName() + " \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043B\u0441\u044F \u043D\u0430 \u0430\u0442\u0430\u043A\u0443 " + CastleName, false);
      } else if (typeId == 2) {
        addDefenderWaiting(clan.getClanId());
        announceToPlayer(clan.getName() + " \u0441\u0434\u0435\u043B\u0430\u043B \u0437\u0430\u043F\u0440\u043E\u0441 \u043D\u0430 \u0437\u0430\u0449\u0438\u0442\u0443 " + CastleName, false);
      }
    } catch (Exception e) {
      _log.warning("Exception: saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration): " + e.getMessage());
      e.printStackTrace();
    } finally {
      Close.CS(con, statement);
    }
  }

  private void setNextSiegeDate()
  {
    while (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
    {
      if (getCastle().isClanhall()) {
        getCastle().getSiegeDate().add(5, 7); continue;
      }
      getCastle().getSiegeDate().add(5, Config.ALT_SIEGE_INTERVAL);
    }

    _isRegistrationOver = false;
  }

  private void spawnArtifact(int Id)
  {
    if (_artifacts == null) {
      _artifacts = new FastList();
    }

    for (SiegeManager.SiegeSpawn _sp : SiegeManager.getInstance().getArtefactSpawnList(Id)) {
      if (_sp == null)
      {
        continue;
      }
      L2ArtefactInstance art = new L2ArtefactInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(_sp.getNpcId()));
      art.setCurrentHpMp(art.getMaxHp(), art.getMaxMp());
      art.setHeading(_sp.getLocation().getHeading());
      art.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 50);
      _artifacts.add(art);
    }
  }

  private void spawnControlTower(int Id)
  {
    if (_controlTowers == null) {
      _controlTowers = new FastList();
    }

    for (SiegeManager.SiegeSpawn _sp : SiegeManager.getInstance().getControlTowerSpawnList(Id)) {
      if (_sp == null)
      {
        continue;
      }
      L2NpcTemplate template = NpcTable.getInstance().getTemplate(_sp.getNpcId());
      template.getStatsSet().set("baseHpMax", _sp.getHp());

      L2ControlTowerInstance ct = new L2ControlTowerInstance(IdFactory.getInstance().getNextId(), template);
      ct.setCurrentHpMp(ct.getMaxHp(), ct.getMaxMp());
      ct.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 20);

      _controlTowers.add(ct);
    }
  }

  private void spawnSiegeGuard()
  {
    getSiegeGuardManager().spawnSiegeGuard();
    double distanceClosest;
    if ((getSiegeGuardManager().getSiegeGuardSpawn().size() > 0) && (_controlTowers.size() > 0))
    {
      distanceClosest = 0.0D;
      for (L2Spawn spawn : getSiegeGuardManager().getSiegeGuardSpawn()) {
        if (spawn == null) {
          continue;
        }
        L2ControlTowerInstance closestCt = null;
        distanceClosest = 0.0D;
        for (L2ControlTowerInstance ct : _controlTowers) {
          if (ct == null) {
            continue;
          }
          double x = spawn.getLocx() - ct.getX();
          double y = spawn.getLocy() - ct.getY();
          double z = spawn.getLocz() - ct.getZ();

          double distance = x * x + y * y + z * z;

          if ((closestCt == null) || (distance < distanceClosest)) {
            closestCt = ct;
            distanceClosest = distance;
          }
        }

        if (closestCt != null)
          closestCt.registerGuard(spawn);
      }
    }
  }

  public final L2SiegeClan getAttackerClan(L2Clan clan)
  {
    if (clan == null) {
      return null;
    }
    return getAttackerClan(clan.getClanId());
  }

  public final L2SiegeClan getAttackerClan(int clanId) {
    for (L2SiegeClan sc : getAttackerClans()) {
      if ((sc != null) && (sc.getClanId() == clanId)) {
        return sc;
      }
    }
    return null;
  }

  public final List<L2SiegeClan> getAttackerClans() {
    if (_isNormalSide) {
      return _attackerClans;
    }
    return _defenderClans;
  }

  public final int getAttackerRespawnDelay() {
    return SiegeManager.getInstance().getAttackerRespawnDelay();
  }

  public final Castle getCastle() {
    if ((_castle == null) || (_castle.length <= 0)) {
      return null;
    }
    return _castle[0];
  }

  public final L2SiegeClan getDefenderClan(L2Clan clan) {
    if (clan == null) {
      return null;
    }
    return getDefenderClan(clan.getClanId());
  }

  public final L2SiegeClan getDefenderClan(int clanId) {
    for (L2SiegeClan sc : getDefenderClans()) {
      if ((sc != null) && (sc.getClanId() == clanId)) {
        return sc;
      }
    }
    return null;
  }

  public final List<L2SiegeClan> getDefenderClans() {
    if (_isNormalSide) {
      return _defenderClans;
    }
    return _attackerClans;
  }

  public final L2SiegeClan getDefenderWaitingClan(L2Clan clan) {
    if (clan == null) {
      return null;
    }
    return getDefenderWaitingClan(clan.getClanId());
  }

  public final L2SiegeClan getDefenderWaitingClan(int clanId) {
    for (L2SiegeClan sc : getDefenderWaitingClans()) {
      if ((sc != null) && (sc.getClanId() == clanId)) {
        return sc;
      }
    }
    return null;
  }

  public final List<L2SiegeClan> getDefenderWaitingClans() {
    return _defenderWaitingClans;
  }

  public final int getDefenderRespawnDelay() {
    return SiegeManager.getInstance().getDefenderRespawnDelay() + _defenderRespawnDelayPenalty;
  }

  public final boolean getIsInProgress() {
    return _isInProgress;
  }

  public final boolean getIsRegistrationOver() {
    return _isRegistrationOver;
  }

  public final Calendar getSiegeDate() {
    return getCastle().getSiegeDate();
  }

  public void addFlag(L2Clan clan, L2NpcInstance flag) {
    if (clan != null) {
      L2SiegeClan sc = getAttackerClan(clan);
      if (sc != null)
        sc.addFlag(flag);
    }
  }

  public L2NpcInstance getFlag(L2Clan clan)
  {
    L2NpcInstance flag = null;
    if (clan != null) {
      L2SiegeClan sc = getAttackerClan(clan);
      if (sc != null) {
        flag = sc.getFlag();
      }
    }
    return flag;
  }

  public final SiegeGuardManager getSiegeGuardManager() {
    if (_siegeGuardManager == null) {
      _siegeGuardManager = new SiegeGuardManager(getCastle());
    }
    return _siegeGuardManager;
  }

  private String getCastleName(int castleId) {
    String castleName = "";
    switch (castleId) {
    case 1:
      castleName = "Gludio";
      break;
    case 2:
      castleName = "Dion";
      break;
    case 3:
      castleName = "Giran";
      break;
    case 4:
      castleName = "Oren";
      break;
    case 5:
      castleName = "Aden";
      break;
    case 6:
      castleName = "Innadril";
      break;
    case 7:
      castleName = "Goddard";
      break;
    case 8:
      castleName = "Rune";
      break;
    case 9:
      castleName = "Schuttgart";
      break;
    case 21:
      castleName = "Fortress of Resistance";
      break;
    case 34:
      castleName = "Devastated Castle";
      break;
    case 35:
      castleName = "Bandit Stronghold";
      break;
    case 64:
      castleName = "Fortress of the Dead";
    }

    return castleName;
  }

  public class ScheduleWaitersTeleportTask
    implements Runnable
  {
    private Castle _castleInst;

    public ScheduleWaitersTeleportTask(Castle pCastle)
    {
      _castleInst = pCastle;
    }

    public void run() {
      if (!getIsInProgress()) {
        return;
      }
      try
      {
        getCastle().getWaitZone().oustDefenders();
        ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleWaitersTeleportTask(Siege.this, _castleInst), getDefenderRespawnDelay());
      }
      catch (Throwable t)
      {
      }
    }
  }

  public class ScheduleStartSiegeTask
    implements Runnable
  {
    private Castle _castleInst;

    public ScheduleStartSiegeTask(Castle pCastle)
    {
      _castleInst = pCastle;
    }

    public void run() {
      if (getIsInProgress()) {
        return;
      }
      try
      {
        long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
        String CastleName = Siege.this.getCastleName(getCastle().getCastleId());

        if (timeRemaining > 86400000L) {
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(Siege.this, _castleInst), timeRemaining - 86400000L);
        } else if ((timeRemaining <= 86400000L) && (timeRemaining > 13600000L)) {
          announceToPlayer("\u0417\u0430\u043A\u043E\u043D\u0447\u0435\u043D\u0430 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F \u043D\u0430 \u043E\u0441\u0430\u0434\u0443 " + CastleName, false);
          _isRegistrationOver = true;
          clearSiegeWaitingClan();
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(Siege.this, _castleInst), timeRemaining - 13600000L);
        } else if ((timeRemaining <= 13600000L) && (timeRemaining > 600000L)) {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " \u043C\u0438\u043D\u0443\u0442 \u0434\u043E \u043D\u0430\u0447\u0430\u043B\u0430 \u043E\u0441\u0430\u0434\u044B " + CastleName, false);
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(Siege.this, _castleInst), timeRemaining - 600000L);
        } else if ((timeRemaining <= 600000L) && (timeRemaining > 300000L)) {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " \u043C\u0438\u043D\u0443\u0442 \u0434\u043E \u043D\u0430\u0447\u0430\u043B\u0430 \u043E\u0441\u0430\u0434\u044B " + CastleName, false);
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(Siege.this, _castleInst), timeRemaining - 300000L);
        } else if ((timeRemaining <= 300000L) && (timeRemaining > 10000L)) {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " \u043C\u0438\u043D\u0443\u0442 \u0434\u043E \u043D\u0430\u0447\u0430\u043B\u0430 \u043E\u0441\u0430\u0434\u044B " + CastleName, false);
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(Siege.this, _castleInst), timeRemaining - 10000L);
        } else if ((timeRemaining <= 10000L) && (timeRemaining > 0L)) {
          announceToPlayer(Math.round((float)(timeRemaining / 1000L)) + " \u0441\u0435\u043A\u0443\u043D\u0434 \u0434\u043E \u043D\u0430\u0447\u0430\u043B\u0430 \u043E\u0441\u0430\u0434\u044B " + CastleName + "!", false);
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(Siege.this, _castleInst), timeRemaining);
        } else {
          _castleInst.getSiege().startSiege();
        }
      }
      catch (Throwable t)
      {
      }
    }
  }

  public class ScheduleEndSiegeTask
    implements Runnable
  {
    private Castle _castleInst;

    public ScheduleEndSiegeTask(Castle pCastle)
    {
      _castleInst = pCastle;
    }

    public void run() {
      if (!getIsInProgress()) {
        return;
      }
      try
      {
        long timeRemaining = _siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
        String CastleName = Siege.this.getCastleName(getCastle().getCastleId());

        if (timeRemaining > 3600000L) {
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(Siege.this, _castleInst), timeRemaining - 3600000L);
        } else if ((timeRemaining <= 3600000L) && (timeRemaining > 600000L)) {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " \u043C\u0438\u043D\u0443\u0442 \u0434\u043E \u043E\u043A\u043E\u043D\u0447\u0430\u043D\u0438\u044F \u043E\u0441\u0430\u0434\u044B " + CastleName, true);
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(Siege.this, _castleInst), timeRemaining - 600000L);
        } else if ((timeRemaining <= 600000L) && (timeRemaining > 300000L)) {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " \u043C\u0438\u043D\u0443\u0442 \u0434\u043E \u043E\u043A\u043E\u043D\u0447\u0430\u043D\u0438\u044F \u043E\u0441\u0430\u0434\u044B " + CastleName, true);
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(Siege.this, _castleInst), timeRemaining - 300000L);
        } else if ((timeRemaining <= 300000L) && (timeRemaining > 10000L)) {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " \u043C\u0438\u043D\u0443\u0442 \u0434\u043E \u043E\u043A\u043E\u043D\u0447\u0430\u043D\u0438\u044F \u043E\u0441\u0430\u0434\u044B " + CastleName, true);
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(Siege.this, _castleInst), timeRemaining - 10000L);
        } else if ((timeRemaining <= 10000L) && (timeRemaining > 0L)) {
          for (int i = 10; i > 0; i--) {
            announceToPlayerSiegeEnd(i);
            try {
              Thread.sleep(1000L);
            } catch (InterruptedException e) {
            }
          }
          _castleInst.getSiege().endSiege();
        }
      }
      catch (Throwable t)
      {
      }
    }
  }

  public static enum TeleportWhoType
  {
    All, Attacker, DefenderNotOwner, Owner, Spectator;
  }
}