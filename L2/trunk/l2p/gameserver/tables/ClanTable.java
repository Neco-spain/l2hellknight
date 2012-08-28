package l2p.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.pledge.Alliance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.SubUnit;
import l2p.gameserver.model.pledge.UnitMember;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.PledgeShowMemberListDeleteAll;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.utils.SiegeUtils;
import l2p.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClanTable
{
  private static final Logger _log = LoggerFactory.getLogger(ClanTable.class);
  private static ClanTable _instance;
  private final Map<Integer, Clan> _clans = new ConcurrentHashMap();
  private final Map<Integer, Alliance> _alliances = new ConcurrentHashMap();

  public static ClanTable getInstance()
  {
    if (_instance == null)
    {
      new ClanTable();
    }
    return _instance;
  }

  public Clan[] getClans()
  {
    return (Clan[])_clans.values().toArray(new Clan[_clans.size()]);
  }

  public Alliance[] getAlliances()
  {
    return (Alliance[])_alliances.values().toArray(new Alliance[_alliances.size()]);
  }

  private ClanTable()
  {
    _instance = this;

    restoreClans();
    restoreAllies();
    restoreWars();
  }

  public Clan getClan(int clanId)
  {
    if (clanId <= 0)
    {
      return null;
    }
    return (Clan)_clans.get(Integer.valueOf(clanId));
  }

  public String getClanName(int clanId)
  {
    Clan c = getClan(clanId);
    return c != null ? c.getName() : "";
  }

  public Clan getClanByCharId(int charId)
  {
    if (charId <= 0)
    {
      return null;
    }
    for (Clan clan : getClans())
    {
      if ((clan != null) && (clan.isAnyMember(charId)))
      {
        return clan;
      }
    }
    return null;
  }

  public Alliance getAlliance(int allyId)
  {
    if (allyId <= 0)
    {
      return null;
    }
    return (Alliance)_alliances.get(Integer.valueOf(allyId));
  }

  public Alliance getAllianceByCharId(int charId)
  {
    if (charId <= 0)
    {
      return null;
    }
    Clan charClan = getClanByCharId(charId);
    return charClan == null ? null : charClan.getAlliance();
  }

  public Map.Entry<Clan, Alliance> getClanAndAllianceByCharId(int charId)
  {
    Player player = GameObjectsStorage.getPlayer(charId);
    Clan charClan = player != null ? player.getClan() : getClanByCharId(charId);
    return new AbstractMap.SimpleEntry(charClan, charClan == null ? null : charClan.getAlliance());
  }

  public void restoreClans()
  {
    List clanIds = new ArrayList();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet result = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT clan_id FROM clan_data");
      result = statement.executeQuery();
      while (result.next())
      {
        clanIds.add(Integer.valueOf(result.getInt("clan_id")));
      }
    }
    catch (Exception e)
    {
      _log.warn("Error while restoring clans!!! " + e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, result);
    }

    for (Iterator i$ = clanIds.iterator(); i$.hasNext(); ) { int clanId = ((Integer)i$.next()).intValue();

      Clan clan = Clan.restore(clanId);
      if (clan == null)
      {
        _log.warn("Error while restoring clanId: " + clanId);
        continue;
      }

      if (clan.getAllSize() <= 0)
      {
        _log.warn("membersCount = 0 for clanId: " + clanId);
        continue;
      }

      if (clan.getLeader() == null)
      {
        _log.warn("Not found leader for clanId: " + clanId);
        continue;
      }

      _clans.put(Integer.valueOf(clan.getClanId()), clan);
    }
  }

  public void restoreAllies()
  {
    List allyIds = new ArrayList();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet result = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT ally_id FROM ally_data");
      result = statement.executeQuery();
      while (result.next())
      {
        allyIds.add(Integer.valueOf(result.getInt("ally_id")));
      }
    }
    catch (Exception e)
    {
      _log.warn("Error while restoring allies!!! " + e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, result);
    }

    for (Iterator i$ = allyIds.iterator(); i$.hasNext(); ) { int allyId = ((Integer)i$.next()).intValue();

      Alliance ally = new Alliance(allyId);

      if (ally.getMembersCount() <= 0)
      {
        _log.warn("membersCount = 0 for allyId: " + allyId);
        continue;
      }

      if (ally.getLeader() == null)
      {
        _log.warn("Not found leader for allyId: " + allyId);
        continue;
      }

      _alliances.put(Integer.valueOf(ally.getAllyId()), ally);
    }
  }

  public Clan getClanByName(String clanName)
  {
    if (!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE))
    {
      return null;
    }

    for (Clan clan : _clans.values())
    {
      if (clan.getName().equalsIgnoreCase(clanName)) {
        return clan;
      }
    }
    return null;
  }

  public Alliance getAllyByName(String allyName)
  {
    if (!Util.isMatchingRegexp(allyName, Config.ALLY_NAME_TEMPLATE))
    {
      return null;
    }

    for (Alliance ally : _alliances.values())
    {
      if (ally.getAllyName().equalsIgnoreCase(allyName)) {
        return ally;
      }
    }
    return null;
  }

  public Clan createClan(Player player, String clanName)
  {
    if (getClanByName(clanName) == null)
    {
      UnitMember leader = new UnitMember(player);
      leader.setLeaderOf(0);

      Clan clan = new Clan(IdFactory.getInstance().getNextId());

      SubUnit unit = new SubUnit(clan, 0, leader, clanName);
      unit.addUnitMember(leader);
      clan.addSubUnit(unit, false);

      clan.store();

      player.setPledgeType(0);
      player.setClan(clan);
      player.setPowerGrade(6);

      leader.setPlayerInstance(player, false);

      _clans.put(Integer.valueOf(clan.getClanId()), clan);

      return clan;
    }

    return null;
  }

  public void dissolveClan(Player player)
  {
    Clan clan = player.getClan();
    long curtime = System.currentTimeMillis();
    SiegeUtils.removeSiegeSkills(player);
    for (Player clanMember : clan.getOnlineMembers(0))
    {
      clanMember.setClan(null);
      clanMember.setTitle(null);
      clanMember.sendPacket(new IStaticPacket[] { PledgeShowMemberListDeleteAll.STATIC, Msg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS });
      clanMember.broadcastCharInfo();
      clanMember.setLeaveClanTime(curtime);
    }
    clan.flush();
    deleteClanFromDb(clan.getClanId());
    _clans.remove(Integer.valueOf(clan.getClanId()));
    player.sendPacket(Msg.CLAN_HAS_DISPERSED);
    player.setDeleteClanTime(curtime);
  }

  public void deleteClanFromDb(int clanId)
  {
    long curtime = System.currentTimeMillis();

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE characters SET clanid=0,title='',pledge_type=0,pledge_rank=0,lvl_joined_academy=0,apprentice=0,leaveclan=? WHERE clanid=?");
      statement.setLong(1, curtime / 1000L);
      statement.setInt(2, clanId);
      statement.execute();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
      statement.setInt(1, clanId);
      statement.execute();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
      statement.setInt(1, clanId);
      statement.execute();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");
      statement.setInt(1, clanId);
      statement.execute();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");
      statement.setInt(1, clanId);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.warn("could not dissolve clan:" + e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public Alliance createAlliance(Player player, String allyName)
  {
    Alliance alliance = null;

    if (getAllyByName(allyName) == null)
    {
      Clan leader = player.getClan();
      alliance = new Alliance(IdFactory.getInstance().getNextId(), allyName, leader);
      alliance.store();
      _alliances.put(Integer.valueOf(alliance.getAllyId()), alliance);

      player.getClan().setAllyId(alliance.getAllyId());
      for (Player temp : player.getClan().getOnlineMembers(0))
      {
        temp.broadcastCharInfo();
      }
    }

    return alliance;
  }

  public void dissolveAlly(Player player)
  {
    int allyId = player.getAllyId();
    for (Clan member : player.getAlliance().getMembers())
    {
      member.setAllyId(0);
      member.broadcastClanStatus(false, true, false);
      member.broadcastToOnlineMembers(new L2GameServerPacket[] { Msg.YOU_HAVE_WITHDRAWN_FROM_THE_ALLIANCE });
      member.setLeavedAlly();
    }
    deleteAllyFromDb(allyId);
    _alliances.remove(Integer.valueOf(allyId));
    player.sendPacket(Msg.THE_ALLIANCE_HAS_BEEN_DISSOLVED);
    player.getClan().setDissolvedAlly();
  }

  public void deleteAllyFromDb(int allyId)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE clan_data SET ally_id=0 WHERE ally_id=?");
      statement.setInt(1, allyId);
      statement.execute();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM ally_data WHERE ally_id=?");
      statement.setInt(1, allyId);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.warn("could not dissolve clan:" + e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void startClanWar(Clan clan1, Clan clan2)
  {
    clan1.setEnemyClan(clan2);
    clan2.setAttackerClan(clan1);
    clan1.broadcastClanStatus(false, false, true);
    clan2.broadcastClanStatus(false, false, true);

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2) VALUES(?,?)");
      statement.setInt(1, clan1.getClanId());
      statement.setInt(2, clan2.getClanId());
      statement.execute();
    }
    catch (Exception e)
    {
      _log.warn("could not store clan war data:" + e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }

    clan1.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage(1562).addString(clan2.getName()) });
    clan2.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage(1561).addString(clan1.getName()) });
  }

  public void stopClanWar(Clan clan1, Clan clan2)
  {
    clan1.deleteEnemyClan(clan2);
    clan2.deleteAttackerClan(clan1);

    clan1.broadcastClanStatus(false, false, true);
    clan2.broadcastClanStatus(false, false, true);

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
      statement.setInt(1, clan1.getClanId());
      statement.setInt(2, clan2.getClanId());
      statement.execute();
    }
    catch (Exception e)
    {
      _log.warn("could not delete war data:" + e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }

    clan1.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage(1567).addString(clan2.getName()) });
    clan2.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage(1566).addString(clan1.getName()) });
  }

  private void restoreWars()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT clan1, clan2 FROM clan_wars");
      rset = statement.executeQuery();

      while (rset.next())
      {
        Clan clan1 = getClan(rset.getInt("clan1"));
        Clan clan2 = getClan(rset.getInt("clan2"));
        if ((clan1 == null) || (clan2 == null))
          continue;
        clan1.setEnemyClan(clan2);
        clan2.setAttackerClan(clan1);
      }

    }
    catch (Exception e)
    {
      _log.warn("could not restore clan wars data:");
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public static void unload()
  {
    if (_instance != null)
    {
      try
      {
        _instance.finalize();
      }
      catch (Throwable e)
      {
      }
    }
  }
}