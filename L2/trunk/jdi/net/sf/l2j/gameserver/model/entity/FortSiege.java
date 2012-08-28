package net.sf.l2j.gameserver.model.entity;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.FortSiegeGuardManager;
import net.sf.l2j.gameserver.instancemanager.FortSiegeManager;
import net.sf.l2j.gameserver.instancemanager.FortSiegeManager.SiegeSpawn;
import net.sf.l2j.gameserver.instancemanager.MercTicketManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.L2SiegeClan.SiegeClanType;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2ArtefactInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2CommanderInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.model.zone.type.L2FortZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.FortressSiegeInfo;
import net.sf.l2j.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public class FortSiege
{
  protected static final Logger _log = Logger.getLogger(FortSiege.class.getName());

  private List<L2SiegeClan> _attackerClans = new FastList();

  private List<L2SiegeClan> _defenderClans = new FastList();
  private List<L2SiegeClan> _defenderWaitingClans = new FastList();
  private int _defenderRespawnDelayPenalty;
  private List<L2CommanderInstance> _commanders = new FastList();
  private List<L2ArtefactInstance> _combatflag = new FastList();
  private Fort[] _fort;
  private boolean _isInProgress = false;
  private boolean _isScheduled = false;
  private boolean _isNormalSide = true;
  protected boolean _isRegistrationOver = false;
  protected Calendar _siegeEndDate;
  private FortSiegeGuardManager _siegeGuardManager;
  protected Calendar _siegeRegistrationEndDate;

  public FortSiege(Fort[] fort)
  {
    _fort = fort;

    checkAutoTask();
  }

  public void endSiege()
  {
    if (getIsInProgress())
    {
      announceToPlayer("The siege of " + getFort().getName() + " has finished!", false);

      if (getFort().getOwnerId() <= 0)
      {
        announceToPlayer("The siege of " + getFort().getName() + " has ended in a draw.", false);
      }

      removeFlags();
      unSpawnFlags();

      teleportPlayer(TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.Town);

      teleportPlayer(TeleportWhoType.DefenderNotOwner, MapRegionTable.TeleportWhereType.Town);

      teleportPlayer(TeleportWhoType.Spectator, MapRegionTable.TeleportWhereType.Town);

      _isInProgress = false;

      updatePlayerSiegeStateFlags(true);

      saveFortSiege();

      clearSiegeClan();

      removeCommander();

      if (getFort().getOwnerId() > 0)
      {
        _siegeGuardManager.removeMercs();
      }

      getFort().spawnDoor();
      getFort().getZone().updateZoneStatusForCharactersInside();
    }
  }

  private void removeDefender(L2SiegeClan sc)
  {
    if (sc != null)
    {
      getDefenderClans().remove(sc);
    }
  }

  private void removeAttacker(L2SiegeClan sc)
  {
    if (sc != null)
    {
      getAttackerClans().remove(sc);
    }
  }

  private void addDefender(L2SiegeClan sc, L2SiegeClan.SiegeClanType type)
  {
    if (sc == null) {
      return;
    }
    sc.setType(type);
    getDefenderClans().add(sc);
  }

  private void addAttacker(L2SiegeClan sc)
  {
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
      for (L2SiegeClan sc : getDefenderClans())
      {
        if (sc != null)
        {
          removeDefender(sc);
          addAttacker(sc);
        }

      }

      L2SiegeClan sc_newowner = getAttackerClan(getFort().getOwnerId());
      removeAttacker(sc_newowner);
      addDefender(sc_newowner, L2SiegeClan.SiegeClanType.OWNER);
      endSiege();
      sc_newowner = null;

      return;
    }
  }

  public void startSiege()
  {
    if (!getIsInProgress())
    {
      if (getAttackerClans().size() <= 0)
      {
        SystemMessage sm;
        if (getFort().getOwnerId() <= 0)
        {
          sm = new SystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
        }
        else
        {
          sm = new SystemMessage(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
        }

        sm.addString(getFort().getName());
        Announcements.getInstance().announceToAll(sm);
        SystemMessage sm = null;

        return;
      }

      _isNormalSide = true;
      _isInProgress = true;
      _isScheduled = false;

      loadSiegeClan();
      updatePlayerSiegeStateFlags(false);

      teleportPlayer(TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.Town);

      spawnCommander(getFort().getFortId());

      getFort().spawnDoor();

      MercTicketManager.getInstance().deleteTickets(getFort().getFortId());

      _defenderRespawnDelayPenalty = 0;

      getFort().getZone().updateZoneStatusForCharactersInside();

      _siegeEndDate = Calendar.getInstance();
      _siegeEndDate.add(12, FortSiegeManager.getInstance().getSiegeLength());
      ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(getFort()), 1000L);

      announceToPlayer("The siege of " + getFort().getName() + " has started!", false);
      saveFortSiege();
      FortSiegeManager.getInstance().addSiege(this);
    }
  }

  public void announceToPlayer(String message, boolean inAreaOnly)
  {
    if (inAreaOnly)
    {
      getFort().getZone().announceToPlayers(message);
      return;
    }

    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
    {
      player.sendMessage(message);
    }
  }

  public void updatePlayerSiegeStateFlags(boolean clear)
  {
    for (L2SiegeClan siegeclan : getAttackerClans())
    {
      L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
      L2PcInstance member;
      for (member : clan.getOnlineMembers(""))
      {
        if (clear)
        {
          member.setSiegeState(0);
        }
        else
        {
          member.setSiegeState(1);
        }

        member.sendPacket(new UserInfo(member));

        for (L2PcInstance player : member.getKnownList().getKnownPlayers().values())
        {
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
        if (clear)
        {
          member.setSiegeState(0);
        }
        else
        {
          member.setSiegeState(2);
        }

        member.sendPacket(new UserInfo(member));

        for (L2PcInstance player : member.getKnownList().getKnownPlayers().values())
        {
          player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
        }
      }
    }

    L2Clan clan = null;
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
    return (getIsInProgress()) && (getFort().checkIfInZone(x, y, z));
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
      PreparedStatement statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?");
      statement.setInt(1, getFort().getFortId());
      statement.execute();
      statement.close();
      statement = null;

      if (getFort().getOwnerId() > 0)
      {
        PreparedStatement statement2 = con.prepareStatement("DELETE FROM fortsiege_clans WHERE clan_id=?");
        statement2.setInt(1, getFort().getOwnerId());
        statement2.execute();
        statement2.close();
        statement2 = null;
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
      try {
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      con = null;
    }
  }

  private void clearSiegeDate()
  {
    getFort().getSiegeDate().setTimeInMillis(0L);
    _isRegistrationOver = false;
  }

  public void clearSiegeWaitingClan()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=? and type = 2");
      statement.setInt(1, getFort().getFortId());
      statement.execute();
      statement.close();
      statement = null;

      getDefenderWaitingClans().clear();
    }
    catch (Exception e)
    {
      _log.warning("Exception: clearSiegeWaitingClan(): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      try {
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      con = null;
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
        if (!checkIfInZone(player.getX(), player.getY(), player.getZ()))
          continue;
        players.add(player);
      }

    }

    L2Clan clan = null;

    return players;
  }

  public List<L2PcInstance> getDefendersButNotOwnersInZone()
  {
    List players = new FastList();

    for (L2SiegeClan siegeclan : getDefenderClans())
    {
      L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());

      if (clan.getClanId() == getFort().getOwnerId())
      {
        continue;
      }

      for (L2PcInstance player : clan.getOnlineMembers(""))
      {
        if (!checkIfInZone(player.getX(), player.getY(), player.getZ()))
          continue;
        players.add(player);
      }

    }

    L2Clan clan = null;

    return players;
  }

  public List<L2PcInstance> getPlayersInZone()
  {
    return getFort().getZone().getAllPlayers();
  }

  public List<L2PcInstance> getOwnersInZone()
  {
    List players = new FastList();

    for (L2SiegeClan siegeclan : getDefenderClans())
    {
      L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());

      if (clan.getClanId() != getFort().getOwnerId())
      {
        continue;
      }

      for (L2PcInstance player : clan.getOnlineMembers(""))
      {
        if (!checkIfInZone(player.getX(), player.getY(), player.getZ()))
          continue;
        players.add(player);
      }

    }

    L2Clan clan = null;

    return players;
  }

  public List<L2PcInstance> getSpectatorsInZone()
  {
    List players = new FastList();

    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
    {
      if ((!player.isInsideZone(4)) || (player.getSiegeState() != 0))
      {
        continue;
      }

      if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
      {
        players.add(player);
      }
    }

    return players;
  }

  public void killedCT(L2NpcInstance ct)
  {
    _defenderRespawnDelayPenalty += FortSiegeManager.getInstance().getControlTowerLosePenalty();
  }

  public void killedCommander(L2CommanderInstance ct)
  {
    if (_commanders != null)
    {
      _commanders.remove(ct);

      if (_commanders.size() == 0)
      {
        spawnFlag(getFort().getFortId());
      }
    }
  }

  public void killedFlag(L2NpcInstance flag)
  {
    if (flag == null) {
      return;
    }
    for (int i = 0; i < getAttackerClans().size(); i++)
    {
      if (getAttackerClan(i).removeFlag(flag))
        return;
    }
  }

  public void listRegisterClan(L2PcInstance player)
  {
    player.sendPacket(new FortressSiegeInfo(getFort()));
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

    if (getFort().getOwnerId() != 0)
    {
      allyId = ClanTable.getInstance().getClan(getFort().getOwnerId()).getAllyId();
    }

    if (allyId != 0)
    {
      if ((player.getClan().getAllyId() == allyId) && (!force))
      {
        player.sendMessage("You cannot register as an attacker because your alliance owns the fort");
        return;
      }
    }

    if ((player.getInventory().getItemByItemId(57) != null) && (player.getInventory().getItemByItemId(57).getCount() < 250000))
    {
      player.sendMessage("You do not have enough adena.");
      return;
    }

    if ((force) || (checkIfCanRegister(player)))
    {
      player.getInventory().destroyItemByItemId("Siege", 57, 250000, player, player.getTarget());
      player.getInventory().updateDatabase();

      saveSiegeClan(player.getClan(), 1, false);

      if (getAttackerClans().size() == 1)
      {
        startAutoTask(true);
      }
    }
  }

  public void registerDefender(L2PcInstance player)
  {
    registerDefender(player, false);
  }

  public void registerDefender(L2PcInstance player, boolean force)
  {
    if (getFort().getOwnerId() <= 0)
    {
      player.sendMessage("You cannot register as a defender because " + getFort().getName() + " is owned by NPC.");
    }
    else if ((force) || (checkIfCanRegister(player)))
    {
      saveSiegeClan(player.getClan(), 2, false);
    }
  }

  public void removeSiegeClan(int clanId)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement;
      if (clanId != 0)
      {
        statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=? and clan_id=?");
      }
      else
      {
        statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?");
      }

      statement.setInt(1, getFort().getFortId());

      if (clanId != 0)
      {
        statement.setInt(2, clanId);
      }

      statement.execute();
      statement.close();
      PreparedStatement statement = null;

      loadSiegeClan();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      try {
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      con = null;
    }
  }

  public void removeSiegeClan(L2Clan clan)
  {
    if ((clan == null) || (clan.getHasFort() == getFort().getFortId()) || (!FortSiegeManager.getInstance().checkIsRegistered(clan, getFort().getFortId()))) {
      return;
    }
    removeSiegeClan(clan.getClanId());
  }

  public void removeSiegeClan(L2PcInstance player)
  {
    removeSiegeClan(player.getClan());
  }

  public void checkAutoTask()
  {
    if (getFort().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
    {
      clearSiegeDate();
      saveSiegeDate();
      removeSiegeClan(0);
      return;
    }

    startAutoTask(false);
  }

  public void startAutoTask(boolean setTime)
  {
    if (setTime)
    {
      setSiegeDateTime();
    }

    System.out.println("Siege of " + getFort().getName() + ": " + getFort().getSiegeDate().getTime());
    setIsScheduled(true);
    loadSiegeClan();

    _siegeRegistrationEndDate = Calendar.getInstance();
    _siegeRegistrationEndDate.setTimeInMillis(getFort().getSiegeDate().getTimeInMillis());
    _siegeRegistrationEndDate.add(12, -10);

    ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(getFort()), 1000L);
  }

  public void teleportPlayer(TeleportWhoType teleportWho, MapRegionTable.TeleportWhereType teleportWhere)
  {
    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$entity$FortSiege$TeleportWhoType[teleportWho.ordinal()])
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
      if ((player.isGM()) || (player.isInJail()))
      {
        continue;
      }

      player.teleToLocation(teleportWhere);
    }

    List players = null;
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
    {
      player.sendMessage("The deadline to register for the siege of " + getFort().getName() + " has passed.");
    }
    else if (getIsInProgress())
    {
      player.sendMessage("This is not the time for siege registration and so registration and cancellation cannot be done.");
    }
    else if ((player.getClan() == null) || (player.getClan().getLevel() < FortSiegeManager.getInstance().getSiegeClanMinLevel()))
    {
      player.sendMessage("Only clans with Level " + FortSiegeManager.getInstance().getSiegeClanMinLevel() + " and higher may register for a fort siege.");
    }
    else if (player.getClan().getHasFort() > 0)
    {
      player.sendMessage("You cannot register because your clan already own a fort.");
    }
    else if (player.getClan().getHasCastle() > 0)
    {
      player.sendMessage("You cannot register because your clan already own a castle.");
    }
    else if (player.getClan().getClanId() == getFort().getOwnerId())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING));
    }
    else if (FortSiegeManager.getInstance().checkIsRegistered(player.getClan(), getFort().getFortId()))
    {
      player.sendMessage("You are already registered in a Siege.");
    }
    else {
      return true;
    }
    return false;
  }

  private void setSiegeDateTime()
  {
    Calendar newDate = Calendar.getInstance();
    newDate.add(12, 60);
    getFort().setSiegeDate(newDate);
    saveSiegeDate();
    newDate = null;
  }

  private void loadSiegeClan()
  {
    Connection con = null;
    try
    {
      getAttackerClans().clear();
      getDefenderClans().clear();
      getDefenderWaitingClans().clear();

      if (getFort().getOwnerId() > 0)
      {
        addDefender(getFort().getOwnerId(), L2SiegeClan.SiegeClanType.OWNER);
      }

      PreparedStatement statement = null;
      ResultSet rs = null;

      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("SELECT clan_id,type FROM fortsiege_clans where fort_id=?");
      statement.setInt(1, getFort().getFortId());
      rs = statement.executeQuery();

      while (rs.next())
      {
        int typeId = rs.getInt("type");

        if (typeId == 0)
        {
          addDefender(rs.getInt("clan_id")); continue;
        }
        if (typeId == 1)
        {
          addAttacker(rs.getInt("clan_id")); continue;
        }
        if (typeId != 2)
          continue;
        addDefenderWaiting(rs.getInt("clan_id"));
      }

      rs.close();
      statement.close();
      statement = null;
      rs = null;
    }
    catch (Exception e)
    {
      _log.warning("Exception: loadSiegeClan(): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      try {
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      con = null;
    }
  }

  private void removeCommander()
  {
    if (_commanders != null)
    {
      for (L2CommanderInstance commander : _commanders)
      {
        if (commander != null)
        {
          commander.decayMe();
        }
      }
      _commanders = null;
    }
  }

  private void removeFlags()
  {
    for (L2SiegeClan sc : getAttackerClans())
    {
      if (sc != null)
      {
        sc.removeFlags();
      }
    }
    for (L2SiegeClan sc : getDefenderClans())
    {
      if (sc != null)
      {
        sc.removeFlags();
      }
    }
  }

  private void saveFortSiege()
  {
    clearSiegeDate();
    saveSiegeDate();
    setIsScheduled(false);
  }

  private void saveSiegeDate()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Update fort set siegeDate = ? where id = ?");
      statement.setLong(1, getSiegeDate().getTimeInMillis());
      statement.setInt(2, getFort().getFortId());
      statement.execute();

      statement.close();
      statement = null;
    }
    catch (Exception e)
    {
      _log.warning("Exception: saveSiegeDate(): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      try {
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      con = null;
    }
  }

  private void saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration)
  {
    if (clan.getHasFort() > 0) {
      return;
    }
    Connection con = null;
    try
    {
      if ((typeId == 0) || (typeId == 2) || (typeId == -1))
      {
        if (getDefenderClans().size() + getDefenderWaitingClans().size() >= FortSiegeManager.getInstance().getDefenderMaxClans()) {
          return;
        }
      }
      else if (getAttackerClans().size() >= FortSiegeManager.getInstance().getAttackerMaxClans()) {
        return;
      }
      con = L2DatabaseFactory.getInstance().getConnection();

      if (!isUpdateRegistration)
      {
        PreparedStatement statement = con.prepareStatement("INSERT INTO fortsiege_clans (clan_id,fort_id,type,fort_owner) values (?,?,?,0)");
        statement.setInt(1, clan.getClanId());
        statement.setInt(2, getFort().getFortId());
        statement.setInt(3, typeId);
        statement.execute();
        statement.close();
        statement = null;
      }
      else
      {
        PreparedStatement statement = con.prepareStatement("Update fortsiege_clans set type = ? where fort_id = ? and clan_id = ?");
        statement.setInt(1, typeId);
        statement.setInt(2, getFort().getFortId());
        statement.setInt(3, clan.getClanId());
        statement.execute();
        statement.close();
        statement = null;
      }

      if ((typeId == 0) || (typeId == -1))
      {
        addDefender(clan.getClanId());
        announceToPlayer(clan.getName() + " has been registered to defend " + getFort().getName(), false);
      }
      else if (typeId == 1)
      {
        addAttacker(clan.getClanId());
        announceToPlayer(clan.getName() + " has been registered to attack " + getFort().getName(), false);
      }
      else if (typeId == 2)
      {
        addDefenderWaiting(clan.getClanId());
        announceToPlayer(clan.getName() + " has requested to defend " + getFort().getName(), false);
      }
    }
    catch (Exception e)
    {
      _log.warning("Exception: saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      try {
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      con = null;
    }
  }

  private void spawnCommander(int Id)
  {
    if (_commanders == null)
    {
      _commanders = new FastList();
    }

    for (FortSiegeManager.SiegeSpawn _sp : FortSiegeManager.getInstance().getCommanderSpawnList(Id))
    {
      L2CommanderInstance commander = new L2CommanderInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(_sp.getNpcId()));
      commander.setCurrentHpMp(commander.getMaxHp(), commander.getMaxMp());
      commander.setHeading(_sp.getLocation().getHeading());
      commander.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 50);

      _commanders.add(commander);
      commander = null;
    }
  }

  private void spawnFlag(int Id)
  {
    if (_combatflag == null)
    {
      _combatflag = new FastList();
    }

    for (FortSiegeManager.SiegeSpawn _sp : FortSiegeManager.getInstance().getFlagList(Id))
    {
      L2ArtefactInstance combatflag = new L2ArtefactInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(_sp.getNpcId()));
      combatflag.setCurrentHpMp(combatflag.getMaxHp(), combatflag.getMaxMp());
      combatflag.setHeading(_sp.getLocation().getHeading());
      combatflag.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 10);

      _combatflag.add(combatflag);
      combatflag = null;
    }
  }

  private void unSpawnFlags()
  {
    if (_combatflag != null)
    {
      for (L2ArtefactInstance _sp : _combatflag)
      {
        if (_sp != null)
        {
          _sp.decayMe();
        }
      }
      _combatflag = null;
    }
  }

  private void spawnSiegeGuard()
  {
    getSiegeGuardManager().spawnSiegeGuard();
  }

  public final L2SiegeClan getAttackerClan(L2Clan clan)
  {
    if (clan == null) {
      return null;
    }
    return getAttackerClan(clan.getClanId());
  }

  public final L2SiegeClan getAttackerClan(int clanId)
  {
    for (L2SiegeClan sc : getAttackerClans()) {
      if ((sc != null) && (sc.getClanId() == clanId))
        return sc;
    }
    return null;
  }

  public final List<L2SiegeClan> getAttackerClans()
  {
    if (_isNormalSide) {
      return _attackerClans;
    }
    return _defenderClans;
  }

  public final int getAttackerRespawnDelay()
  {
    return FortSiegeManager.getInstance().getAttackerRespawnDelay();
  }

  public final Fort getFort()
  {
    if ((_fort == null) || (_fort.length <= 0)) {
      return null;
    }
    return _fort[0];
  }

  public final L2SiegeClan getDefenderClan(L2Clan clan)
  {
    if (clan == null) {
      return null;
    }
    return getDefenderClan(clan.getClanId());
  }

  public final L2SiegeClan getDefenderClan(int clanId)
  {
    for (L2SiegeClan sc : getDefenderClans()) {
      if ((sc != null) && (sc.getClanId() == clanId))
        return sc;
    }
    return null;
  }

  public final List<L2SiegeClan> getDefenderClans()
  {
    if (_isNormalSide) {
      return _defenderClans;
    }
    return _attackerClans;
  }

  public final L2SiegeClan getDefenderWaitingClan(L2Clan clan)
  {
    if (clan == null) {
      return null;
    }
    return getDefenderWaitingClan(clan.getClanId());
  }

  public final L2SiegeClan getDefenderWaitingClan(int clanId)
  {
    for (L2SiegeClan sc : getDefenderWaitingClans()) {
      if ((sc != null) && (sc.getClanId() == clanId))
        return sc;
    }
    return null;
  }

  public final List<L2SiegeClan> getDefenderWaitingClans()
  {
    return _defenderWaitingClans;
  }

  public final int getDefenderRespawnDelay()
  {
    return FortSiegeManager.getInstance().getDefenderRespawnDelay() + _defenderRespawnDelayPenalty;
  }

  public final boolean getIsInProgress()
  {
    return _isInProgress;
  }

  public final boolean getIsScheduled()
  {
    return _isScheduled;
  }

  public final void setIsScheduled(boolean isScheduled)
  {
    _isScheduled = isScheduled;
  }

  public final boolean getIsRegistrationOver()
  {
    return _isRegistrationOver;
  }

  public final Calendar getSiegeDate()
  {
    return getFort().getSiegeDate();
  }

  public List<L2NpcInstance> getFlag(L2Clan clan)
  {
    if (clan != null)
    {
      L2SiegeClan sc = getAttackerClan(clan);
      if (sc != null) {
        return sc.getFlag();
      }
    }
    return null;
  }

  public final FortSiegeGuardManager getSiegeGuardManager()
  {
    if (_siegeGuardManager == null)
    {
      _siegeGuardManager = new FortSiegeGuardManager(getFort());
    }

    return _siegeGuardManager;
  }

  public class ScheduleStartSiegeTask
    implements Runnable
  {
    private Fort _fortInst;

    public ScheduleStartSiegeTask(Fort pFort)
    {
      _fortInst = pFort;
    }

    public void run()
    {
      if (getIsInProgress()) {
        return;
      }
      try
      {
        long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
        if (timeRemaining > 86400000L)
        {
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(FortSiege.this, _fortInst), timeRemaining - 86400000L);
        }
        else if ((timeRemaining <= 86400000L) && (timeRemaining > 13600000L))
        {
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(FortSiege.this, _fortInst), timeRemaining - 13600000L);
        }
        else if ((timeRemaining <= 13600000L) && (timeRemaining > 600000L))
        {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " minute(s) until " + getFort().getName() + " siege begin.", false);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(FortSiege.this, _fortInst), timeRemaining - 600000L);
        }
        else if ((timeRemaining <= 600000L) && (timeRemaining > 300000L))
        {
          announceToPlayer("The registration term for " + getFort().getName() + " has ended.", false);

          _isRegistrationOver = true;

          clearSiegeWaitingClan();

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(FortSiege.this, _fortInst), timeRemaining - 300000L);
        }
        else if ((timeRemaining <= 300000L) && (timeRemaining > 10000L))
        {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " minute(s) until " + getFort().getName() + " siege begin.", false);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(FortSiege.this, _fortInst), timeRemaining - 10000L);
        }
        else if ((timeRemaining <= 10000L) && (timeRemaining > 0L))
        {
          announceToPlayer(getFort().getName() + " siege " + Math.round((float)(timeRemaining / 1000L)) + " second(s) to start!", false);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(FortSiege.this, _fortInst), timeRemaining);
        }
        else
        {
          _fortInst.getSiege().startSiege();
        }
      }
      catch (Throwable t)
      {
        t.printStackTrace();
      }
    }
  }

  public class ScheduleEndSiegeTask
    implements Runnable
  {
    private Fort _fortInst;

    public ScheduleEndSiegeTask(Fort pFort)
    {
      _fortInst = pFort;
    }

    public void run()
    {
      if (!getIsInProgress()) {
        return;
      }
      try
      {
        long timeRemaining = _siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

        if (timeRemaining > 3600000L)
        {
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(FortSiege.this, _fortInst), timeRemaining - 3600000L);
        }
        else if ((timeRemaining <= 3600000L) && (timeRemaining > 600000L))
        {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " minute(s) until " + getFort().getName() + " siege conclusion.", true);
          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(FortSiege.this, _fortInst), timeRemaining - 600000L);
        }
        else if ((timeRemaining <= 600000L) && (timeRemaining > 300000L))
        {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " minute(s) until " + getFort().getName() + " siege conclusion.", true);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(FortSiege.this, _fortInst), timeRemaining - 300000L);
        }
        else if ((timeRemaining <= 300000L) && (timeRemaining > 10000L))
        {
          announceToPlayer(Math.round((float)(timeRemaining / 60000L)) + " minute(s) until " + getFort().getName() + " siege conclusion.", true);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(FortSiege.this, _fortInst), timeRemaining - 10000L);
        }
        else if ((timeRemaining <= 10000L) && (timeRemaining > 0L))
        {
          announceToPlayer(getFort().getName() + " siege " + Math.round((float)(timeRemaining / 1000L)) + " second(s) left!", true);

          ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(FortSiege.this, _fortInst), timeRemaining);
        }
        else
        {
          _fortInst.getSiege().endSiege();
        }
      }
      catch (Throwable t)
      {
        t.printStackTrace();
      }
    }
  }

  public static enum TeleportWhoType
  {
    All, 
    Attacker, 
    DefenderNotOwner, 
    Owner, 
    Spectator;
  }
}