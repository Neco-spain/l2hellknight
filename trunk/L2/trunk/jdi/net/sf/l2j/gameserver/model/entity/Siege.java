package net.sf.l2j.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
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
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2ArtefactInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2ControlTowerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.model.zone.type.L2CastleZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2j.gameserver.network.serverpackets.SiegeInfo;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class Siege
{
  protected static final Logger _log = Logger.getLogger(Siege.class.getName());

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
    if (getIsInProgress())
    {
      announceToPlayer("The siege of " + getCastle().getName() + " has finished!", false);

      if (getCastle().getOwnerId() <= 0) {
        announceToPlayer("The siege of " + getCastle().getName() + " has ended in a draw.", false);
      }

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
      _siegeGuardManager.unspawnSiegeGuard();
      if (getCastle().getOwnerId() > 0) _siegeGuardManager.removeMercs();
      getCastle().spawnDoor();
      getCastle().getZone().updateZoneStatusForCharactersInside();
      SiegeManager.getInstance().removeSiege(this);
    }
  }

  private void removeDefender(L2SiegeClan sc)
  {
    if (sc != null) getDefenderClans().remove(sc);
  }

  private void removeAttacker(L2SiegeClan sc)
  {
    if (sc != null) getAttackerClans().remove(sc);
  }

  private void addDefender(L2SiegeClan sc, L2SiegeClan.SiegeClanType type)
  {
    if (sc == null) return;
    sc.setType(type);
    getDefenderClans().add(sc);
  }

  private void addAttacker(L2SiegeClan sc)
  {
    if (sc == null) return;
    sc.setType(L2SiegeClan.SiegeClanType.ATTACKER);
    getAttackerClans().add(sc);
  }

  public void midVictory()
  {
    if (getIsInProgress())
    {
      if (getCastle().getOwnerId() > 0) _siegeGuardManager.removeMercs();

      if ((getDefenderClans().size() == 0) && (getAttackerClans().size() == 1))
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
        if (getDefenderClans().size() == 0)
        {
          if (allyId != 0)
          {
            boolean allinsamealliance = true;
            for (L2SiegeClan sc : getAttackerClans())
            {
              if (sc != null)
              {
                if (ClanTable.getInstance().getClan(sc.getClanId()).getAllyId() != allyId)
                  allinsamealliance = false;
              }
            }
            if (allinsamealliance)
            {
              L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
              removeAttacker(sc_newowner);
              addDefender(sc_newowner, L2SiegeClan.SiegeClanType.OWNER);
              endSiege();
              return;
            }
          }
        }

        for (L2SiegeClan sc : getDefenderClans())
        {
          if (sc != null) {
            removeDefender(sc);
            addAttacker(sc);
          }
        }

        L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
        removeAttacker(sc_newowner);
        addDefender(sc_newowner, L2SiegeClan.SiegeClanType.OWNER);

        if (allyId != 0)
        {
          L2Clan[] clanList = ClanTable.getInstance().getClans();

          for (L2Clan clan : clanList) {
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
    if (!getIsInProgress())
    {
      if (getAttackerClans().size() <= 0)
      {
        SystemMessage sm;
        SystemMessage sm;
        if (getCastle().getOwnerId() <= 0)
          sm = new SystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
        else
          sm = new SystemMessage(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
        sm.addString(getCastle().getName());
        Announcements.getInstance().announceToAll(sm);
        return;
      }

      _isNormalSide = true;
      _isInProgress = true;

      loadSiegeClan();
      updatePlayerSiegeStateFlags(false);
      teleportPlayer(TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.Town);

      spawnArtifact(getCastle().getCastleId());
      spawnControlTower(getCastle().getCastleId());
      getCastle().spawnDoor();
      spawnSiegeGuard();
      MercTicketManager.getInstance().deleteTickets(getCastle().getCastleId());
      _defenderRespawnDelayPenalty = 0;

      getCastle().getZone().updateZoneStatusForCharactersInside();

      _siegeEndDate = Calendar.getInstance();
      _siegeEndDate.add(12, SiegeManager.getInstance().getSiegeLength());
      ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(getCastle()), 1000L);

      announceToPlayer("The siege of " + getCastle().getName() + " has started!", false);
      SiegeManager.getInstance().addSiege(this);
    }
  }

  public void announceToPlayer(String message, boolean inAreaOnly)
  {
    if (inAreaOnly)
    {
      getCastle().getZone().announceToPlayers(message);
      return;
    }

    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
    {
      player.sendMessage(message);
    }
  }

  public void updatePlayerSiegeStateFlags(boolean clear)
  {
    new Thread(new Runnable(clear)
    {
      public void run()
      {
        for (L2SiegeClan siegeclan : getAttackerClans())
        {
          L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
          L2PcInstance member;
          for (member : clan.getOnlineMembers(""))
          {
            if (val$clear) member.setSiegeState(0); else
              member.setSiegeState(1);
            member.sendPacket(new UserInfo(member));
            for (L2PcInstance player : member.getKnownList().getKnownPlayers().values()) {
              player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
            }
          }
        }
        for (L2SiegeClan siegeclan : getDefenderClans())
        {
          L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
          L2PcInstance member;
          for (member : clan.getOnlineMembers(""))
          {
            if (val$clear) member.setSiegeState(0); else
              member.setSiegeState(2);
            member.sendPacket(new UserInfo(member));
            for (L2PcInstance player : member.getKnownList().getKnownPlayers().values())
              player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
          }
        }
      }
    }).start();
  }

  public void approveSiegeDefenderClan(int clanId)
  {
    if (clanId <= 0) return;
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
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=?");
      statement.setInt(1, getCastle().getCastleId());
      statement.execute();
      statement.close();

      if (getCastle().getOwnerId() > 0)
      {
        PreparedStatement statement2 = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?");
        statement2.setInt(1, getCastle().getOwnerId());
        statement2.execute();
        statement2.close();
      }

      getAttackerClans().clear();
      getDefenderClans().clear();
      getDefenderWaitingClans().clear();
    }
    catch (Exception e)
    {
      _log.warning("Exception: clearSiegeClan(): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public void clearSiegeWaitingClan()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and type = 2");
      statement.setInt(1, getCastle().getCastleId());
      statement.execute();
      statement.close();

      getDefenderWaitingClans().clear();
    }
    catch (Exception e)
    {
      _log.warning("Exception: clearSiegeWaitingClan(): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public List<L2PcInstance> getAttackersInZone()
  {
    List players = new FastList();

    for (L2SiegeClan siegeclan : getAttackerClans())
    {
      L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
      for (L2PcInstance player : clan.getOnlineMembers(""))
      {
        if (!checkIfInZone(player.getX(), player.getY(), player.getZ())) continue; players.add(player);
      }
    }
    return players;
  }

  public List<L2PcInstance> getDefendersButNotOwnersInZone()
  {
    List players = new FastList();

    for (L2SiegeClan siegeclan : getDefenderClans())
    {
      L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
      if (clan.getClanId() != getCastle().getOwnerId())
        for (L2PcInstance player : clan.getOnlineMembers(""))
        {
          if (!checkIfInZone(player.getX(), player.getY(), player.getZ())) continue; players.add(player);
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

    for (L2SiegeClan siegeclan : getDefenderClans())
    {
      L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
      if (clan.getClanId() == getCastle().getOwnerId())
        for (L2PcInstance player : clan.getOnlineMembers(""))
        {
          if (!checkIfInZone(player.getX(), player.getY(), player.getZ())) continue; players.add(player);
        }
    }
    return players;
  }

  public List<L2PcInstance> getSpectatorsInZone()
  {
    List players = new FastList();

    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
    {
      if ((player.isInsideZone(4)) && (player.getSiegeState() == 0)) {
        if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
          players.add(player);
      }
    }
    return players;
  }

  public void killedCT(L2NpcInstance ct)
  {
    _defenderRespawnDelayPenalty += SiegeManager.getInstance().getControlTowerLosePenalty();
  }

  public void killedFlag(L2NpcInstance flag)
  {
    if (flag == null) return;
    for (int i = 0; i < getAttackerClans().size(); i++)
    {
      if (getAttackerClan(i).removeFlag(flag)) return;
    }
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
    if (player.getClan() == null) return;
    int allyId = 0;
    if (getCastle().getOwnerId() != 0)
      allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
    if (allyId != 0)
    {
      if ((player.getClan().getAllyId() == allyId) && (!force))
      {
        player.sendMessage("You cannot register as an attacker because your alliance owns the castle");
        return;
      }
    }
    if (SiegeManager.getInstance().checkIsRegisteredType(player.getClan(), 1))
    {
      player.sendMessage("You cannot register as an attacker because your clan registered other castle");
      return;
    }
    if ((force) || (checkIfCanRegister(player))) saveSiegeClan(player.getClan(), 1, false);
  }

  public void registerDefender(L2PcInstance player)
  {
    registerDefender(player, false);
  }

  public void registerDefender(L2PcInstance player, boolean force)
  {
    if (SiegeManager.getInstance().checkIsRegisteredType(player.getClan(), 2))
    {
      player.sendMessage("You cannot register as an defender because your clan registered other castle");
      return;
    }
    if (getCastle().getOwnerId() <= 0) player.sendMessage("You cannot register as a defender because " + getCastle().getName() + " is owned by NPC.");
    else if ((force) || (checkIfCanRegister(player))) saveSiegeClan(player.getClan(), 2, false);
  }

  public void removeSiegeClan(int clanId)
  {
    if (clanId <= 0) return;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and clan_id=?");
      statement.setInt(1, getCastle().getCastleId());
      statement.setInt(2, clanId);
      statement.execute();
      statement.close();

      loadSiegeClan();
    }
    catch (Exception e)
    {
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public void removeSiegeClan(L2Clan clan)
  {
    if ((clan == null) || (clan.getHasCastle() == getCastle().getCastleId()) || (!SiegeManager.getInstance().checkIsRegistered(clan, getCastle().getCastleId())))
      return;
    removeSiegeClan(clan.getClanId());
  }

  public void removeSiegeClan(L2PcInstance player)
  {
    removeSiegeClan(player.getClan());
  }

  public void startAutoTask()
  {
    correctSiegeDateTime();

    _log.info("Siege of " + getCastle().getName() + ": " + getCastle().getSiegeDate().getTime());

    loadSiegeClan();

    _siegeRegistrationEndDate = Calendar.getInstance();
    _siegeRegistrationEndDate.setTimeInMillis(getCastle().getSiegeDate().getTimeInMillis());
    _siegeRegistrationEndDate.add(5, -1);

    ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(getCastle()), 1000L);
  }

  public void teleportPlayer(TeleportWhoType teleportWho, MapRegionTable.TeleportWhereType teleportWhere)
  {
    List players;
    switch (2.$SwitchMap$net$sf$l2j$gameserver$model$entity$Siege$TeleportWhoType[teleportWho.ordinal()])
    {
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

    for (L2PcInstance player : players)
    {
      if ((!player.isGM()) && (!player.isInJail()))
        player.teleToLocation(teleportWhere);
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
    if (getIsRegistrationOver()) {
      player.sendMessage("The deadline to register for the siege of " + getCastle().getName() + " has passed.");
    }
    else if (getIsInProgress())
      player.sendMessage("This is not the time for siege registration and so registration and cancellation cannot be done.");
    else if ((player.getClan() == null) || (player.getClan().getLevel() < SiegeManager.getInstance().getSiegeClanMinLevel())) {
      player.sendMessage("Only clans with Level " + SiegeManager.getInstance().getSiegeClanMinLevel() + " and higher may register for a castle siege.");
    }
    else if (player.getClan().getHasCastle() > 0)
      player.sendMessage("You cannot register because your clan already owns a castle.");
    else if (player.getClan().getClanId() == getCastle().getOwnerId())
      player.sendPacket(new SystemMessage(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING));
    else if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), getCastle().getCastleId()))
      player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE));
    else if (checkIfAlreadyRegisteredForSameDay(player.getClan()))
      player.sendPacket(new SystemMessage(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE));
    else return true;

    return false;
  }

  public boolean checkIfAlreadyRegisteredForSameDay(L2Clan clan)
  {
    for (Siege siege : SiegeManager.getInstance().getSieges())
    {
      if (siege != this)
        if (siege.getSiegeDate().get(7) == getSiegeDate().get(7))
        {
          if (siege.checkIsAttacker(clan)) return true;
          if (siege.checkIsDefender(clan)) return true;
          if (siege.checkIsDefenderWaiting(clan)) return true;
        }
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

    if (getCastle().getSiegeDate().get(7) != getCastle().getSiegeDayOfWeek())
    {
      corrected = true;
      getCastle().getSiegeDate().set(7, getCastle().getSiegeDayOfWeek());
    }

    if (getCastle().getSiegeDate().get(11) != getCastle().getSiegeHourOfDay())
    {
      corrected = true;
      getCastle().getSiegeDate().set(11, getCastle().getSiegeHourOfDay());
    }

    getCastle().getSiegeDate().set(12, 0);

    if (corrected) saveSiegeDate();
  }

  private void loadSiegeClan()
  {
    Connection con = null;
    try
    {
      getAttackerClans().clear();
      getDefenderClans().clear();
      getDefenderWaitingClans().clear();

      if (getCastle().getOwnerId() > 0) {
        addDefender(getCastle().getOwnerId(), L2SiegeClan.SiegeClanType.OWNER);
      }
      PreparedStatement statement = null;
      ResultSet rs = null;

      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("SELECT clan_id,type FROM siege_clans where castle_id=?");
      statement.setInt(1, getCastle().getCastleId());
      rs = statement.executeQuery();

      while (rs.next())
      {
        int typeId = rs.getInt("type");
        if (typeId == 0) { addDefender(rs.getInt("clan_id")); continue; }
        if (typeId == 1) { addAttacker(rs.getInt("clan_id")); continue; }
        if (typeId != 2) continue; addDefenderWaiting(rs.getInt("clan_id"));
      }

      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Exception: loadSiegeClan(): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  private void removeArtifact()
  {
    if (_artifacts != null)
    {
      for (L2ArtefactInstance art : _artifacts)
      {
        if (art != null) art.decayMe();
      }
      _artifacts = null;
    }
  }

  private void removeControlTower()
  {
    if (_controlTowers != null)
    {
      for (L2ControlTowerInstance ct : _controlTowers)
      {
        if (ct != null) ct.decayMe();
      }

      _controlTowers = null;
    }
  }

  private void removeFlags()
  {
    for (L2SiegeClan sc : getAttackerClans())
    {
      if (sc != null) sc.removeFlags();
    }
    for (L2SiegeClan sc : getDefenderClans())
    {
      if (sc != null) sc.removeFlags();
    }
  }

  private void removeDefenderFlags()
  {
    for (L2SiegeClan sc : getDefenderClans())
    {
      if (sc != null) sc.removeFlags();
    }
  }

  private void saveCastleSiege()
  {
    setNextSiegeDate();
    saveSiegeDate();
    startAutoTask();
  }

  private void saveSiegeDate()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Update castle set siegeDate = ? where id = ?");
      statement.setLong(1, getSiegeDate().getTimeInMillis());
      statement.setInt(2, getCastle().getCastleId());
      statement.execute();

      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Exception: saveSiegeDate(): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  private void saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration)
  {
    if (clan.getHasCastle() > 0) return;

    Connection con = null;
    try
    {
      if ((typeId == 0) || (typeId == 2) || (typeId == -1))
      {
        if (getDefenderClans().size() + getDefenderWaitingClans().size() >= SiegeManager.getInstance().getDefenderMaxClans()) {
          return;
        }
      }
      else if (getAttackerClans().size() >= SiegeManager.getInstance().getAttackerMaxClans()) {
        return;
      }
      con = L2DatabaseFactory.getInstance().getConnection();

      if (!isUpdateRegistration)
      {
        PreparedStatement statement = con.prepareStatement("INSERT INTO siege_clans (clan_id,castle_id,type,castle_owner) values (?,?,?,0)");
        statement.setInt(1, clan.getClanId());
        statement.setInt(2, getCastle().getCastleId());
        statement.setInt(3, typeId);
        statement.execute();
        statement.close();
      }
      else
      {
        PreparedStatement statement = con.prepareStatement("Update siege_clans set type = ? where castle_id = ? and clan_id = ?");
        statement.setInt(1, typeId);
        statement.setInt(2, getCastle().getCastleId());
        statement.setInt(3, clan.getClanId());
        statement.execute();
        statement.close();
      }

      if ((typeId == 0) || (typeId == -1))
      {
        addDefender(clan.getClanId());
        announceToPlayer(clan.getName() + " has been registered to defend " + getCastle().getName(), false);
      }
      else if (typeId == 1)
      {
        addAttacker(clan.getClanId());
        announceToPlayer(clan.getName() + " has been registered to attack " + getCastle().getName(), false);
      }
      else if (typeId == 2)
      {
        addDefenderWaiting(clan.getClanId());
        announceToPlayer(clan.getName() + " has requested to defend " + getCastle().getName(), false);
      }

    }
    catch (Exception e)
    {
      _log.warning("Exception: saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration): " + e.getMessage());

      e.printStackTrace();
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  private void setNextSiegeDate()
  {
    while (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
    {
      getCastle().getSiegeDate().add(5, Config.DAY_TO_SIEGE);
    }
    _isRegistrationOver = false;
  }

  private void spawnArtifact(int Id)
  {
    if (_artifacts == null) {
      _artifacts = new FastList();
    }
    for (SiegeManager.SiegeSpawn _sp : SiegeManager.getInstance().getArtefactSpawnList(Id))
    {
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
    for (SiegeManager.SiegeSpawn _sp : SiegeManager.getInstance().getControlTowerSpawnList(Id))
    {
      L2NpcTemplate template = NpcTable.getInstance().getTemplate(_sp.getNpcId());

      L2ControlTowerInstance ct = new L2ControlTowerInstance(IdFactory.getInstance().getNextId(), template);

      ct.setCurrentHpMp(_sp.getHp(), ct.getMaxMp());
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
      for (L2Spawn spawn : getSiegeGuardManager().getSiegeGuardSpawn())
      {
        if (spawn != null) {
          L2ControlTowerInstance closestCt = null;
          distanceClosest = 0.0D;
          for (L2ControlTowerInstance ct : _controlTowers)
          {
            if (ct != null) {
              double x = spawn.getLocx() - ct.getX();
              double y = spawn.getLocy() - ct.getY();
              double z = spawn.getLocz() - ct.getZ();

              double distance = x * x + y * y + z * z;

              if ((closestCt == null) || (distance < distanceClosest))
              {
                closestCt = ct;
                distanceClosest = distance;
              }
            }
          }
          if (closestCt != null) closestCt.registerGuard(spawn); 
        }
      }
    }
  }

  public final L2SiegeClan getAttackerClan(L2Clan clan)
  {
    if (clan == null) return null;
    return getAttackerClan(clan.getClanId());
  }

  public final L2SiegeClan getAttackerClan(int clanId)
  {
    for (L2SiegeClan sc : getAttackerClans())
      if ((sc != null) && (sc.getClanId() == clanId)) return sc;
    return null;
  }

  public final List<L2SiegeClan> getAttackerClans()
  {
    if (_isNormalSide) return _attackerClans;
    return _defenderClans;
  }

  public final int getAttackerRespawnDelay()
  {
    return SiegeManager.getInstance().getAttackerRespawnDelay();
  }

  public final Castle getCastle()
  {
    if ((_castle == null) || (_castle.length <= 0)) return null;
    return _castle[0];
  }

  public final L2SiegeClan getDefenderClan(L2Clan clan)
  {
    if (clan == null) return null;
    return getDefenderClan(clan.getClanId());
  }

  public final L2SiegeClan getDefenderClan(int clanId)
  {
    for (L2SiegeClan sc : getDefenderClans())
      if ((sc != null) && (sc.getClanId() == clanId)) return sc;
    return null;
  }

  public final List<L2SiegeClan> getDefenderClans()
  {
    if (_isNormalSide) return _defenderClans;
    return _attackerClans;
  }

  public final L2SiegeClan getDefenderWaitingClan(L2Clan clan)
  {
    if (clan == null) return null;
    return getDefenderWaitingClan(clan.getClanId());
  }

  public final L2SiegeClan getDefenderWaitingClan(int clanId)
  {
    for (L2SiegeClan sc : getDefenderWaitingClans())
      if ((sc != null) && (sc.getClanId() == clanId)) return sc;
    return null;
  }

  public final List<L2SiegeClan> getDefenderWaitingClans()
  {
    return _defenderWaitingClans;
  }

  public final int getDefenderRespawnDelay()
  {
    return SiegeManager.getInstance().getDefenderRespawnDelay() + _defenderRespawnDelayPenalty;
  }

  public final boolean getIsInProgress()
  {
    return _isInProgress;
  }

  public final boolean getIsRegistrationOver()
  {
    return _isRegistrationOver;
  }

  public final Calendar getSiegeDate()
  {
    return getCastle().getSiegeDate();
  }

  public List<L2NpcInstance> getFlag(L2Clan clan)
  {
    if (clan != null)
    {
      L2SiegeClan sc = getAttackerClan(clan);
      if (sc != null) return sc.getFlag();
    }
    return null;
  }

  public final SiegeGuardManager getSiegeGuardManager()
  {
    if (_siegeGuardManager == null)
    {
      _siegeGuardManager = new SiegeGuardManager(getCastle());
    }
    return _siegeGuardManager;
  }

  public class ScheduleStartSiegeTask
    implements Runnable
  {
    private Castle _castleInst;

    public ScheduleStartSiegeTask(Castle pCastle)
    {
      _castleInst = pCastle;
    }

    public void run()
    {
      if (getIsInProgress()) return;

      try
      {
        long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

        if (timeRemaining > 86400000L)
        {
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(Siege.this, _castleInst), timeRemaining - 86400000L);
        }
        else if ((timeRemaining <= 86400000L) && (timeRemaining > 13600000L))
        {
          announceToPlayer("The registration term for " + getCastle().getName() + " has ended.", false);

          _isRegistrationOver = true;
          clearSiegeWaitingClan();
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(Siege.this, _castleInst), timeRemaining - 13600000L);
        }
        else if ((timeRemaining <= 13600000L) && (timeRemaining > 600000L))
        {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " minute(s) until " + getCastle().getName() + " siege begin.", false);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(Siege.this, _castleInst), timeRemaining - 600000L);
        }
        else if ((timeRemaining <= 600000L) && (timeRemaining > 300000L))
        {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " minute(s) until " + getCastle().getName() + " siege begin.", false);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(Siege.this, _castleInst), timeRemaining - 300000L);
        }
        else if ((timeRemaining <= 300000L) && (timeRemaining > 10000L))
        {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " minute(s) until " + getCastle().getName() + " siege begin.", false);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(Siege.this, _castleInst), timeRemaining - 10000L);
        }
        else if ((timeRemaining <= 10000L) && (timeRemaining > 0L))
        {
          announceToPlayer(getCastle().getName() + " siege " + Math.round((float)(timeRemaining / 1000L)) + " second(s) to start!", false);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(Siege.this, _castleInst), timeRemaining);
        }
        else
        {
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

    public void run()
    {
      if (!getIsInProgress()) return;

      try
      {
        long timeRemaining = _siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

        if (timeRemaining > 3600000L)
        {
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(Siege.this, _castleInst), timeRemaining - 3600000L);
        }
        else if ((timeRemaining <= 3600000L) && (timeRemaining > 600000L))
        {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " minute(s) until " + getCastle().getName() + " siege conclusion.", true);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(Siege.this, _castleInst), timeRemaining - 600000L);
        }
        else if ((timeRemaining <= 600000L) && (timeRemaining > 300000L))
        {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " minute(s) until " + getCastle().getName() + " siege conclusion.", true);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(Siege.this, _castleInst), timeRemaining - 300000L);
        }
        else if ((timeRemaining <= 300000L) && (timeRemaining > 10000L))
        {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " minute(s) until " + getCastle().getName() + " siege conclusion.", true);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(Siege.this, _castleInst), timeRemaining - 10000L);
        }
        else if ((timeRemaining <= 10000L) && (timeRemaining > 0L))
        {
          announceToPlayer(getCastle().getName() + " siege " + Math.round((float)(timeRemaining / 1000L)) + " second(s) left!", true);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(Siege.this, _castleInst), timeRemaining);
        }
        else
        {
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