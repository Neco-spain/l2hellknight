package l2p.gameserver.model.pledge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import l2p.commons.collections.JoinedIterator;
import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.Config;
import l2p.gameserver.cache.CrestCache;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.database.mysql;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.entity.boat.ClanAirShip;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.entity.residence.Fortress;
import l2p.gameserver.model.entity.residence.ResidenceType;
import l2p.gameserver.model.items.ClanWarehouse;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.PledgeReceiveSubPledgeCreated;
import l2p.gameserver.serverpackets.PledgeShowInfoUpdate;
import l2p.gameserver.serverpackets.PledgeShowMemberListAll;
import l2p.gameserver.serverpackets.PledgeShowMemberListDeleteAll;
import l2p.gameserver.serverpackets.PledgeSkillList;
import l2p.gameserver.serverpackets.PledgeSkillListAdd;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.tables.ClanTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.utils.Log;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Clan
  implements Iterable<UnitMember>
{
  private static final Logger _log = LoggerFactory.getLogger(Clan.class);
  private final int _clanId;
  private int _allyId;
  private int _level;
  private int _hasCastle;
  private int _hasFortress;
  private int _hasHideout;
  private int _warDominion;
  private int _crestId;
  private int _crestLargeId;
  private long _expelledMemberTime;
  private long _leavedAllyTime;
  private long _dissolvedAllyTime;
  private ClanAirShip _airship;
  private boolean _airshipLicense;
  private int _airshipFuel;
  public static long EXPELLED_MEMBER_PENALTY = 86400000L;
  public static long LEAVED_ALLY_PENALTY = 86400000L;
  public static long DISSOLVED_ALLY_PENALTY = 86400000L;
  private final ClanWarehouse _warehouse;
  private int _whBonus = -1;
  private String _notice = null;

  private List<Clan> _atWarWith = new ArrayList();
  private List<Clan> _underAttackFrom = new ArrayList();

  protected IntObjectMap<Skill> _skills = new CTreeIntObjectMap();
  protected IntObjectMap<RankPrivs> _privs = new CTreeIntObjectMap();
  protected IntObjectMap<SubUnit> _subUnits = new CTreeIntObjectMap();

  private int _reputation = 0;
  public static final int CP_NOTHING = 0;
  public static final int CP_CL_INVITE_CLAN = 2;
  public static final int CP_CL_MANAGE_TITLES = 4;
  public static final int CP_CL_WAREHOUSE_SEARCH = 8;
  public static final int CP_CL_MANAGE_RANKS = 16;
  public static final int CP_CL_CLAN_WAR = 32;
  public static final int CP_CL_DISMISS = 64;
  public static final int CP_CL_EDIT_CREST = 128;
  public static final int CP_CL_APPRENTICE = 256;
  public static final int CP_CL_TROOPS_FAME = 512;
  public static final int CP_CL_SUMMON_AIRSHIP = 1024;
  public static final int CP_CH_ENTRY_EXIT = 2048;
  public static final int CP_CH_USE_FUNCTIONS = 4096;
  public static final int CP_CH_AUCTION = 8192;
  public static final int CP_CH_DISMISS = 16384;
  public static final int CP_CH_SET_FUNCTIONS = 32768;
  public static final int CP_CS_ENTRY_EXIT = 65536;
  public static final int CP_CS_MANOR_ADMIN = 131072;
  public static final int CP_CS_MANAGE_SIEGE = 262144;
  public static final int CP_CS_USE_FUNCTIONS = 524288;
  public static final int CP_CS_DISMISS = 1048576;
  public static final int CP_CS_TAXES = 2097152;
  public static final int CP_CS_MERCENARIES = 4194304;
  public static final int CP_CS_SET_FUNCTIONS = 8388606;
  public static final int CP_ALL = 16777214;
  public static final int RANK_FIRST = 1;
  public static final int RANK_LAST = 9;
  public static final int SUBUNIT_NONE = -128;
  public static final int SUBUNIT_ACADEMY = -1;
  public static final int SUBUNIT_MAIN_CLAN = 0;
  public static final int SUBUNIT_ROYAL1 = 100;
  public static final int SUBUNIT_ROYAL2 = 200;
  public static final int SUBUNIT_KNIGHT1 = 1001;
  public static final int SUBUNIT_KNIGHT2 = 1002;
  public static final int SUBUNIT_KNIGHT3 = 2001;
  public static final int SUBUNIT_KNIGHT4 = 2002;
  private static final ClanReputationComparator REPUTATION_COMPARATOR = new ClanReputationComparator(null);
  private static final int REPUTATION_PLACES = 100;

  public Clan(int clanId)
  {
    _clanId = clanId;
    InitializePrivs();
    _warehouse = new ClanWarehouse(this);
    _warehouse.restore();
  }

  public int getClanId()
  {
    return _clanId;
  }

  public int getLeaderId()
  {
    return getLeaderId(0);
  }

  public UnitMember getLeader()
  {
    return getLeader(0);
  }

  public String getLeaderName()
  {
    return getLeaderName(0);
  }

  public String getName()
  {
    return getUnitName(0);
  }

  public UnitMember getAnyMember(int id)
  {
    for (SubUnit unit : getAllSubUnits())
    {
      UnitMember m = unit.getUnitMember(id);
      if (m != null)
      {
        return m;
      }
    }
    return null;
  }

  public UnitMember getAnyMember(String name)
  {
    for (SubUnit unit : getAllSubUnits())
    {
      UnitMember m = unit.getUnitMember(name);
      if (m != null)
      {
        return m;
      }
    }
    return null;
  }

  public int getAllSize()
  {
    int size = 0;

    for (SubUnit unit : getAllSubUnits())
    {
      size += unit.size();
    }

    return size;
  }

  public String getUnitName(int unitType)
  {
    if ((unitType == -128) || (!_subUnits.containsKey(unitType)))
    {
      return "";
    }

    return getSubUnit(unitType).getName();
  }

  public String getLeaderName(int unitType)
  {
    if ((unitType == -128) || (!_subUnits.containsKey(unitType)))
    {
      return "";
    }

    return getSubUnit(unitType).getLeaderName();
  }

  public int getLeaderId(int unitType)
  {
    if ((unitType == -128) || (!_subUnits.containsKey(unitType)))
    {
      return 0;
    }

    return getSubUnit(unitType).getLeaderObjectId();
  }

  public UnitMember getLeader(int unitType)
  {
    if ((unitType == -128) || (!_subUnits.containsKey(unitType)))
    {
      return null;
    }

    return getSubUnit(unitType).getLeader();
  }

  public void flush()
  {
    for (UnitMember member : this)
      removeClanMember(member.getObjectId());
    _warehouse.writeLock();
    try
    {
      for (ItemInstance item : _warehouse.getItems())
        _warehouse.destroyItem(item);
    }
    finally
    {
      _warehouse.writeUnlock();
    }
    if (_hasCastle != 0)
      ((Castle)ResidenceHolder.getInstance().getResidence(Castle.class, _hasCastle)).changeOwner(null);
    if (_hasFortress != 0)
      ((Fortress)ResidenceHolder.getInstance().getResidence(Fortress.class, _hasFortress)).changeOwner(null);
  }

  public void removeClanMember(int id)
  {
    if (id == getLeaderId(0))
    {
      return;
    }

    for (SubUnit unit : getAllSubUnits())
    {
      if (unit.isUnitMember(id))
      {
        removeClanMember(unit.getType(), id);
        break;
      }
    }
  }

  public void removeClanMember(int subUnitId, int objectId)
  {
    SubUnit subUnit = getSubUnit(subUnitId);
    if (subUnit == null) {
      return;
    }
    subUnit.removeUnitMember(objectId);
  }

  public List<UnitMember> getAllMembers()
  {
    Collection units = getAllSubUnits();
    int size = 0;

    for (SubUnit unit : units)
    {
      size += unit.size();
    }
    List members = new ArrayList(size);

    for (SubUnit unit : units)
    {
      members.addAll(unit.getUnitMembers());
    }
    return members;
  }

  public List<Player> getOnlineMembers(int exclude)
  {
    List result = new ArrayList(getAllSize() - 1);

    for (UnitMember temp : this) {
      if ((temp != null) && (temp.isOnline()) && (temp.getObjectId() != exclude))
        result.add(temp.getPlayer());
    }
    return result;
  }

  public int getAllyId()
  {
    return _allyId;
  }

  public int getLevel()
  {
    return _level;
  }

  public int getCastle()
  {
    return _hasCastle;
  }

  public int getHasFortress()
  {
    return _hasFortress;
  }

  public int getHasHideout()
  {
    return _hasHideout;
  }

  public int getResidenceId(ResidenceType r)
  {
    switch (1.$SwitchMap$l2p$gameserver$model$entity$residence$ResidenceType[r.ordinal()])
    {
    case 1:
      return _hasCastle;
    case 2:
      return _hasFortress;
    case 3:
      return _hasHideout;
    }
    return 0;
  }

  public void setAllyId(int allyId)
  {
    _allyId = allyId;
  }

  public void setHasCastle(int castle)
  {
    if (_hasFortress == 0)
      _hasCastle = castle;
  }

  public void setHasFortress(int fortress)
  {
    if (_hasCastle == 0)
      _hasFortress = fortress;
  }

  public void setHasHideout(int hasHideout)
  {
    _hasHideout = hasHideout;
  }

  public void setLevel(int level)
  {
    _level = level;
  }

  public boolean isAnyMember(int id)
  {
    for (SubUnit unit : getAllSubUnits())
    {
      if (unit.isUnitMember(id))
      {
        return true;
      }
    }
    return false;
  }

  public void updateClanInDB()
  {
    if (getLeaderId() == 0)
    {
      _log.warn("updateClanInDB with empty LeaderId");
      Thread.dumpStack();
      return;
    }

    if (getClanId() == 0)
    {
      _log.warn("updateClanInDB with empty ClanId");
      Thread.dumpStack();
      return;
    }
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE clan_data SET ally_id=?,reputation_score=?,expelled_member=?,leaved_ally=?,dissolved_ally=?,clan_level=?,warehouse=?,airship=? WHERE clan_id=?");
      statement.setInt(1, getAllyId());
      statement.setInt(2, getReputationScore());
      statement.setLong(3, getExpelledMemberTime() / 1000L);
      statement.setLong(4, getLeavedAllyTime() / 1000L);
      statement.setLong(5, getDissolvedAllyTime() / 1000L);
      statement.setInt(6, _level);
      statement.setInt(7, getWhBonus());
      statement.setInt(8, isHaveAirshipLicense() ? getAirshipFuel() : -1);
      statement.setInt(9, getClanId());
      statement.execute();
    }
    catch (Exception e)
    {
      _log.warn("error while updating clan '" + getClanId() + "' data in db");
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void store()
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_level,hasCastle,hasFortress,hasHideout,ally_id,expelled_member,leaved_ally,dissolved_ally,airship) values (?,?,?,?,?,?,?,?,?,?)");
      statement.setInt(1, _clanId);
      statement.setInt(2, _level);
      statement.setInt(3, _hasCastle);
      statement.setInt(4, _hasFortress);
      statement.setInt(5, _hasHideout);
      statement.setInt(6, _allyId);
      statement.setLong(7, getExpelledMemberTime() / 1000L);
      statement.setLong(8, getLeavedAllyTime() / 1000L);
      statement.setLong(9, getDissolvedAllyTime() / 1000L);
      statement.setInt(10, isHaveAirshipLicense() ? getAirshipFuel() : -1);
      statement.execute();
      DbUtils.close(statement);

      SubUnit mainSubUnit = (SubUnit)_subUnits.get(0);

      statement = con.prepareStatement("INSERT INTO clan_subpledges (clan_id, type, leader_id, name) VALUES (?,?,?,?)");
      statement.setInt(1, _clanId);
      statement.setInt(2, mainSubUnit.getType());
      statement.setInt(3, mainSubUnit.getLeaderObjectId());
      statement.setString(4, mainSubUnit.getName());
      statement.execute();
      DbUtils.close(statement);

      statement = con.prepareStatement("UPDATE characters SET clanid=?,pledge_type=? WHERE obj_Id=?");
      statement.setInt(1, getClanId());
      statement.setInt(2, mainSubUnit.getType());
      statement.setInt(3, getLeaderId());
      statement.execute();
    }
    catch (Exception e)
    {
      _log.warn("Exception: " + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public static Clan restore(int clanId)
  {
    if (clanId == 0) {
      return null;
    }
    Clan clan = null;

    Connection con1 = null;
    PreparedStatement statement1 = null;
    ResultSet clanData = null;
    try
    {
      con1 = DatabaseFactory.getInstance().getConnection();
      statement1 = con1.prepareStatement("SELECT clan_level,hasCastle,hasFortress,hasHideout,ally_id,reputation_score,expelled_member,leaved_ally,dissolved_ally,warehouse,airship FROM clan_data where clan_id=?");
      statement1.setInt(1, clanId);
      clanData = statement1.executeQuery();

      if (clanData.next())
      {
        clan = new Clan(clanId);
        clan.setLevel(clanData.getInt("clan_level"));
        clan.setHasCastle(clanData.getInt("hasCastle"));
        clan.setHasFortress(clanData.getInt("hasFortress"));
        clan.setHasHideout(clanData.getInt("hasHideout"));
        clan.setAllyId(clanData.getInt("ally_id"));
        clan._reputation = clanData.getInt("reputation_score");
        clan.setExpelledMemberTime(clanData.getLong("expelled_member") * 1000L);
        clan.setLeavedAllyTime(clanData.getLong("leaved_ally") * 1000L);
        clan.setDissolvedAllyTime(clanData.getLong("dissolved_ally") * 1000L);
        clan.setWhBonus(clanData.getInt("warehouse"));
        clan.setAirshipLicense(clanData.getInt("airship") != -1);
        if (clan.isHaveAirshipLicense())
          clan.setAirshipFuel(clanData.getInt("airship"));
      }
      else
      {
        _log.warn("Clan " + clanId + " doesnt exists!");
        Object localObject1 = null;
        return localObject1;
      }
    }
    catch (Exception e)
    {
      _log.error("Error while restoring clan!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con1, statement1, clanData);
    }

    if (clan == null)
    {
      _log.warn("Clan " + clanId + " does't exist");
      return null;
    }

    clan.restoreSkills();
    clan.restoreSubPledges();

    for (SubUnit unit : clan.getAllSubUnits())
    {
      unit.restore();
      unit.restoreSkills();
    }

    clan.restoreRankPrivs();
    clan.setCrestId(CrestCache.getInstance().getPledgeCrestId(clanId));
    clan.setCrestLargeId(CrestCache.getInstance().getPledgeCrestLargeId(clanId));

    return clan;
  }

  public void broadcastToOnlineMembers(IStaticPacket[] packets)
  {
    for (UnitMember member : this)
      if (member.isOnline())
        member.getPlayer().sendPacket(packets);
  }

  public void broadcastToOnlineMembers(L2GameServerPacket[] packets)
  {
    for (UnitMember member : this)
      if (member.isOnline())
        member.getPlayer().sendPacket(packets);
  }

  public void broadcastToOtherOnlineMembers(L2GameServerPacket packet, Player player)
  {
    for (UnitMember member : this)
      if ((member.isOnline()) && (member.getPlayer() != player))
        member.getPlayer().sendPacket(packet);
  }

  public String toString()
  {
    return getName();
  }

  public void setCrestId(int newcrest)
  {
    _crestId = newcrest;
  }

  public int getCrestId()
  {
    return _crestId;
  }

  public boolean hasCrest()
  {
    return _crestId > 0;
  }

  public int getCrestLargeId()
  {
    return _crestLargeId;
  }

  public void setCrestLargeId(int newcrest)
  {
    _crestLargeId = newcrest;
  }

  public boolean hasCrestLarge()
  {
    return _crestLargeId > 0;
  }

  public long getAdenaCount()
  {
    return _warehouse.getCountOfAdena();
  }

  public ClanWarehouse getWarehouse()
  {
    return _warehouse;
  }

  public int isAtWar()
  {
    if ((_atWarWith != null) && (!_atWarWith.isEmpty()))
      return 1;
    return 0;
  }

  public int isAtWarOrUnderAttack()
  {
    if (((_atWarWith != null) && (!_atWarWith.isEmpty())) || ((_underAttackFrom != null) && (!_underAttackFrom.isEmpty())))
      return 1;
    return 0;
  }

  public boolean isAtWarWith(int id)
  {
    Clan clan = ClanTable.getInstance().getClan(id);

    return (_atWarWith != null) && (!_atWarWith.isEmpty()) && 
      (_atWarWith.contains(clan));
  }

  public boolean isUnderAttackFrom(int id)
  {
    Clan clan = ClanTable.getInstance().getClan(id);

    return (_underAttackFrom != null) && (!_underAttackFrom.isEmpty()) && 
      (_underAttackFrom.contains(clan));
  }

  public void setEnemyClan(Clan clan)
  {
    _atWarWith.add(clan);
  }

  public void deleteEnemyClan(Clan clan)
  {
    _atWarWith.remove(clan);
  }

  public void setAttackerClan(Clan clan)
  {
    _underAttackFrom.add(clan);
  }

  public void deleteAttackerClan(Clan clan)
  {
    _underAttackFrom.remove(clan);
  }

  public List<Clan> getEnemyClans()
  {
    return _atWarWith;
  }

  public int getWarsCount()
  {
    return _atWarWith.size();
  }

  public List<Clan> getAttackerClans()
  {
    return _underAttackFrom;
  }

  public void broadcastClanStatus(boolean updateList, boolean needUserInfo, boolean relation)
  {
    List listAll = updateList ? listAll() : null;
    PledgeShowInfoUpdate update = new PledgeShowInfoUpdate(this);

    for (UnitMember member : this)
      if (member.isOnline())
      {
        if (updateList)
        {
          member.getPlayer().sendPacket(PledgeShowMemberListDeleteAll.STATIC);
          member.getPlayer().sendPacket(listAll);
        }
        member.getPlayer().sendPacket(update);
        if (needUserInfo)
          member.getPlayer().broadcastCharInfo();
        if (relation)
          member.getPlayer().broadcastRelationChanged();
      }
  }

  public Alliance getAlliance()
  {
    return _allyId == 0 ? null : ClanTable.getInstance().getAlliance(_allyId);
  }

  public void setExpelledMemberTime(long time)
  {
    _expelledMemberTime = time;
  }

  public long getExpelledMemberTime()
  {
    return _expelledMemberTime;
  }

  public void setExpelledMember()
  {
    _expelledMemberTime = System.currentTimeMillis();
    updateClanInDB();
  }

  public void setLeavedAllyTime(long time)
  {
    _leavedAllyTime = time;
  }

  public long getLeavedAllyTime()
  {
    return _leavedAllyTime;
  }

  public void setLeavedAlly()
  {
    _leavedAllyTime = System.currentTimeMillis();
    updateClanInDB();
  }

  public void setDissolvedAllyTime(long time)
  {
    _dissolvedAllyTime = time;
  }

  public long getDissolvedAllyTime()
  {
    return _dissolvedAllyTime;
  }

  public void setDissolvedAlly()
  {
    _dissolvedAllyTime = System.currentTimeMillis();
    updateClanInDB();
  }

  public boolean canInvite()
  {
    return System.currentTimeMillis() - _expelledMemberTime >= EXPELLED_MEMBER_PENALTY;
  }

  public boolean canJoinAlly()
  {
    return System.currentTimeMillis() - _leavedAllyTime >= LEAVED_ALLY_PENALTY;
  }

  public boolean canCreateAlly()
  {
    return System.currentTimeMillis() - _dissolvedAllyTime >= DISSOLVED_ALLY_PENALTY;
  }

  public int getRank()
  {
    Clan[] clans = ClanTable.getInstance().getClans();
    Arrays.sort(clans, REPUTATION_COMPARATOR);

    int place = 1;
    for (int i = 0; i < clans.length; i++)
    {
      if (i == 100) {
        return 0;
      }
      Clan clan = clans[i];
      if (clan == this) {
        return place + i;
      }
    }
    return 0;
  }

  public int getReputationScore()
  {
    return _reputation;
  }

  private void setReputationScore(int rep)
  {
    if ((_reputation >= 0) && (rep < 0))
    {
      broadcastToOnlineMembers(new L2GameServerPacket[] { Msg.SINCE_THE_CLAN_REPUTATION_SCORE_HAS_DROPPED_TO_0_OR_LOWER_YOUR_CLAN_SKILLS_WILL_BE_DE_ACTIVATED });
      for (UnitMember member : this)
        if ((member.isOnline()) && (member.getPlayer() != null))
          disableSkills(member.getPlayer());
    }
    else if ((_reputation < 0) && (rep >= 0))
    {
      broadcastToOnlineMembers(new L2GameServerPacket[] { Msg.THE_CLAN_SKILL_WILL_BE_ACTIVATED_BECAUSE_THE_CLANS_REPUTATION_SCORE_HAS_REACHED_TO_0_OR_HIGHER });
      for (UnitMember member : this) {
        if ((member.isOnline()) && (member.getPlayer() != null))
          enableSkills(member.getPlayer());
      }
    }
    if (_reputation != rep)
    {
      _reputation = rep;
      broadcastToOnlineMembers(new L2GameServerPacket[] { new PledgeShowInfoUpdate(this) });
    }

    updateClanInDB();
  }

  public int incReputation(int inc, boolean rate, String source)
  {
    if (_level < 5) {
      return 0;
    }
    if ((rate) && (Math.abs(inc) <= Config.RATE_CLAN_REP_SCORE_MAX_AFFECTED)) {
      inc = (int)Math.round(inc * Config.RATE_CLAN_REP_SCORE);
    }
    setReputationScore(_reputation + inc);
    Log.add(getName() + "|" + inc + "|" + _reputation + "|" + source, "clan_reputation");

    return inc;
  }

  private void restoreSkills()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?");
      statement.setInt(1, getClanId());
      rset = statement.executeQuery();

      while (rset.next())
      {
        int id = rset.getInt("skill_id");
        int level = rset.getInt("skill_level");

        Skill skill = SkillTable.getInstance().getInfo(id, level);

        _skills.put(skill.getId(), skill);
      }
    }
    catch (Exception e)
    {
      _log.warn("Could not restore clan skills: " + e);
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public Collection<Skill> getSkills()
  {
    return _skills.values();
  }

  public final Skill[] getAllSkills()
  {
    if (_reputation < 0) {
      return Skill.EMPTY_ARRAY;
    }
    return (Skill[])_skills.values().toArray(new Skill[_skills.values().size()]);
  }

  public Skill addSkill(Skill newSkill, boolean store)
  {
    Skill oldSkill = null;
    PledgeSkillListAdd p;
    PledgeSkillList p2;
    if (newSkill != null)
    {
      oldSkill = (Skill)_skills.put(newSkill.getId(), newSkill);

      if (store)
      {
        Connection con = null;
        PreparedStatement statement = null;
        try
        {
          con = DatabaseFactory.getInstance().getConnection();

          if (oldSkill != null)
          {
            statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?");
            statement.setInt(1, newSkill.getLevel());
            statement.setInt(2, oldSkill.getId());
            statement.setInt(3, getClanId());
            statement.execute();
          }
          else
          {
            statement = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level) VALUES (?,?,?)");
            statement.setInt(1, getClanId());
            statement.setInt(2, newSkill.getId());
            statement.setInt(3, newSkill.getLevel());
            statement.execute();
          }
        }
        catch (Exception e)
        {
          _log.warn("Error could not store char skills: " + e);
          _log.error("", e);
        }
        finally
        {
          DbUtils.closeQuietly(con, statement);
        }
      }

      p = new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel());
      p2 = new PledgeSkillList(this);
      for (UnitMember temp : this)
      {
        if (temp.isOnline())
        {
          Player player = temp.getPlayer();
          if (player != null)
          {
            addSkill(player, newSkill);
            player.sendPacket(new IStaticPacket[] { p, p2, new SkillList(player) });
          }
        }
      }
    }

    return oldSkill;
  }

  public void addSkillsQuietly(Player player)
  {
    for (Skill skill : _skills.values()) {
      addSkill(player, skill);
    }
    SubUnit subUnit = getSubUnit(player.getPledgeType());
    if (subUnit != null)
      subUnit.addSkillsQuietly(player);
  }

  public void enableSkills(Player player)
  {
    if (player.isInOlympiadMode()) {
      return;
    }
    for (Skill skill : _skills.values()) {
      if (skill.getMinPledgeClass() <= player.getPledgeClass())
        player.removeUnActiveSkill(skill);
    }
    SubUnit subUnit = getSubUnit(player.getPledgeType());
    if (subUnit != null)
      subUnit.enableSkills(player);
  }

  public void disableSkills(Player player)
  {
    for (Skill skill : _skills.values()) {
      player.addUnActiveSkill(skill);
    }
    SubUnit subUnit = getSubUnit(player.getPledgeType());
    if (subUnit != null)
      subUnit.disableSkills(player);
  }

  private void addSkill(Player player, Skill skill)
  {
    if (skill.getMinPledgeClass() <= player.getPledgeClass())
    {
      player.addSkill(skill, false);
      if ((_reputation < 0) || (player.isInOlympiadMode()))
        player.addUnActiveSkill(skill);
    }
  }

  public void removeSkill(int skill)
  {
    _skills.remove(skill);
    PledgeSkillListAdd p = new PledgeSkillListAdd(skill, 0);
    for (UnitMember temp : this)
    {
      Player player = temp.getPlayer();
      if ((player != null) && (player.isOnline()))
      {
        player.removeSkillById(Integer.valueOf(skill));
        player.sendPacket(new IStaticPacket[] { p, new SkillList(player) });
      }
    }
  }

  public void broadcastSkillListToOnlineMembers()
  {
    for (UnitMember temp : this)
    {
      Player player = temp.getPlayer();
      if ((player != null) && (player.isOnline()))
      {
        player.sendPacket(new PledgeSkillList(this));
        player.sendPacket(new SkillList(player));
      }
    }
  }

  public static boolean isAcademy(int pledgeType)
  {
    return pledgeType == -1;
  }

  public static boolean isRoyalGuard(int pledgeType)
  {
    return (pledgeType == 100) || (pledgeType == 200);
  }

  public static boolean isOrderOfKnights(int pledgeType)
  {
    return (pledgeType == 1001) || (pledgeType == 1002) || (pledgeType == 2001) || (pledgeType == 2002);
  }

  public int getAffiliationRank(int pledgeType)
  {
    if (isAcademy(pledgeType))
      return 9;
    if (isOrderOfKnights(pledgeType))
      return 8;
    if (isRoyalGuard(pledgeType)) {
      return 7;
    }
    return 6;
  }

  public final SubUnit getSubUnit(int pledgeType)
  {
    return (SubUnit)_subUnits.get(pledgeType);
  }

  public final void addSubUnit(SubUnit sp, boolean updateDb)
  {
    _subUnits.put(sp.getType(), sp);

    if (updateDb)
    {
      broadcastToOnlineMembers(new L2GameServerPacket[] { new PledgeReceiveSubPledgeCreated(sp) });
      Connection con = null;
      PreparedStatement statement = null;
      try
      {
        con = DatabaseFactory.getInstance().getConnection();
        statement = con.prepareStatement("INSERT INTO `clan_subpledges` (clan_id,type,leader_id,name) VALUES (?,?,?,?)");
        statement.setInt(1, getClanId());
        statement.setInt(2, sp.getType());
        statement.setInt(3, sp.getLeaderObjectId());
        statement.setString(4, sp.getName());
        statement.execute();
      }
      catch (Exception e)
      {
        _log.warn("Could not store clan Sub pledges: " + e);
        _log.error("", e);
      }
      finally
      {
        DbUtils.closeQuietly(con, statement);
      }
    }
  }

  public int createSubPledge(Player player, int pledgeType, UnitMember leader, String name)
  {
    int temp = pledgeType;
    pledgeType = getAvailablePledgeTypes(pledgeType);

    if (pledgeType == -128)
    {
      if (temp == -1)
        player.sendPacket(Msg.YOUR_CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY);
      else
        player.sendMessage("You can't create any more sub-units of this type");
      return -128;
    }

    switch (pledgeType)
    {
    case -1:
      break;
    case 100:
    case 200:
      if (getReputationScore() < 5000)
      {
        player.sendPacket(Msg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
        return -128;
      }
      incReputation(-5000, false, "SubunitCreate");
      break;
    case 1001:
    case 1002:
    case 2001:
    case 2002:
      if (getReputationScore() < 10000)
      {
        player.sendPacket(Msg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
        return -128;
      }
      incReputation(-10000, false, "SubunitCreate");
    }

    addSubUnit(new SubUnit(this, pledgeType, leader, name), true);
    return pledgeType;
  }

  public int getAvailablePledgeTypes(int pledgeType)
  {
    if (pledgeType == 0) {
      return -128;
    }
    if (_subUnits.get(pledgeType) != null)
      switch (pledgeType)
      {
      case -1:
        return -128;
      case 100:
        pledgeType = getAvailablePledgeTypes(200);
        break;
      case 200:
        return -128;
      case 1001:
        pledgeType = getAvailablePledgeTypes(1002);
        break;
      case 1002:
        pledgeType = getAvailablePledgeTypes(2001);
        break;
      case 2001:
        pledgeType = getAvailablePledgeTypes(2002);
        break;
      case 2002:
        return -128;
      }
    return pledgeType;
  }

  private void restoreSubPledges()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM clan_subpledges WHERE clan_id=?");
      statement.setInt(1, getClanId());
      rset = statement.executeQuery();

      while (rset.next())
      {
        int type = rset.getInt("type");
        int leaderId = rset.getInt("leader_id");
        String name = rset.getString("name");
        SubUnit pledge = new SubUnit(this, type, leaderId, name);
        addSubUnit(pledge, false);
      }
    }
    catch (Exception e)
    {
      _log.warn("Could not restore clan SubPledges: " + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public int getSubPledgeLimit(int pledgeType)
  {
    int limit;
    switch (_level)
    {
    case 0:
      limit = 10;
      break;
    case 1:
      limit = 15;
      break;
    case 2:
      limit = 20;
      break;
    case 3:
      limit = 30;
      break;
    default:
      limit = 40;
    }

    switch (pledgeType)
    {
    case -1:
    case 100:
    case 200:
      if (getLevel() >= 11)
        limit = 30;
      else
        limit = 20;
      break;
    case 1001:
    case 1002:
      if (getLevel() >= 9)
        limit = 25;
      else
        limit = 10;
      break;
    case 2001:
    case 2002:
      if (getLevel() >= 10)
        limit = 25;
      else {
        limit = 10;
      }
    }
    return limit;
  }

  public int getUnitMembersSize(int pledgeType)
  {
    if ((pledgeType == -128) || (!_subUnits.containsKey(pledgeType)))
    {
      return 0;
    }
    return getSubUnit(pledgeType).size();
  }

  private void restoreRankPrivs()
  {
    if (_privs == null)
      InitializePrivs();
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT privilleges,rank FROM clan_privs WHERE clan_id=?");
      statement.setInt(1, getClanId());
      rset = statement.executeQuery();

      while (rset.next())
      {
        int rank = rset.getInt("rank");

        int privileges = rset.getInt("privilleges");

        RankPrivs p = (RankPrivs)_privs.get(rank);
        if (p != null)
          p.setPrivs(privileges);
        else
          _log.warn("Invalid rank value (" + rank + "), please check clan_privs table");
      }
    }
    catch (Exception e)
    {
      _log.warn("Could not restore clan privs by rank: " + e);
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public void InitializePrivs()
  {
    for (int i = 1; i <= 9; i++)
      _privs.put(i, new RankPrivs(i, 0, 0));
  }

  public void updatePrivsForRank(int rank)
  {
    for (UnitMember member : this)
      if ((member.isOnline()) && (member.getPlayer() != null) && (member.getPlayer().getPowerGrade() == rank))
      {
        if (member.getPlayer().isClanLeader())
          continue;
        member.getPlayer().sendUserInfo();
      }
  }

  public RankPrivs getRankPrivs(int rank)
  {
    if ((rank < 1) || (rank > 9))
    {
      _log.warn("Requested invalid rank value: " + rank);
      Thread.dumpStack();
      return null;
    }
    if (_privs.get(rank) == null)
    {
      _log.warn("Request of rank before init: " + rank);
      Thread.dumpStack();
      setRankPrivs(rank, 0);
    }
    return (RankPrivs)_privs.get(rank);
  }

  public int countMembersByRank(int rank)
  {
    int ret = 0;
    for (UnitMember m : this)
      if (m.getPowerGrade() == rank)
        ret++;
    return ret;
  }

  public void setRankPrivs(int rank, int privs)
  {
    if ((rank < 1) || (rank > 9))
    {
      _log.warn("Requested set of invalid rank value: " + rank);
      Thread.dumpStack();
      return;
    }

    if (_privs.get(rank) != null)
      ((RankPrivs)_privs.get(rank)).setPrivs(privs);
    else {
      _privs.put(rank, new RankPrivs(rank, countMembersByRank(rank), privs));
    }
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("REPLACE INTO clan_privs (clan_id,rank,privilleges) VALUES (?,?,?)");
      statement.setInt(1, getClanId());
      statement.setInt(2, rank);
      statement.setInt(3, privs);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.warn("Could not store clan privs for rank: " + e);
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public final RankPrivs[] getAllRankPrivs()
  {
    if (_privs == null)
      return new RankPrivs[0];
    return (RankPrivs[])_privs.values().toArray(new RankPrivs[_privs.values().size()]);
  }

  public int getWhBonus()
  {
    return _whBonus;
  }

  public void setWhBonus(int i)
  {
    if (_whBonus != -1)
      mysql.set("UPDATE `clan_data` SET `warehouse`=? WHERE `clan_id`=?", new Object[] { Integer.valueOf(i), Integer.valueOf(getClanId()) });
    _whBonus = i;
  }

  public void setAirshipLicense(boolean val)
  {
    _airshipLicense = val;
  }

  public boolean isHaveAirshipLicense()
  {
    return _airshipLicense;
  }

  public ClanAirShip getAirship()
  {
    return _airship;
  }

  public void setAirship(ClanAirShip airship)
  {
    _airship = airship;
  }

  public int getAirshipFuel()
  {
    return _airshipFuel;
  }

  public void setAirshipFuel(int fuel)
  {
    _airshipFuel = fuel;
  }

  public final Collection<SubUnit> getAllSubUnits()
  {
    return _subUnits.values();
  }

  public List<L2GameServerPacket> listAll()
  {
    List p = new ArrayList(_subUnits.size());
    for (SubUnit unit : getAllSubUnits()) {
      p.add(new PledgeShowMemberListAll(this, unit));
    }
    return p;
  }

  public String getNotice()
  {
    return _notice;
  }

  public void setNotice(String notice)
  {
    _notice = notice;
  }

  public int getSkillLevel(int id, int def)
  {
    Skill skill = (Skill)_skills.get(id);
    return skill == null ? def : skill.getLevel();
  }

  public int getSkillLevel(int id)
  {
    return getSkillLevel(id, -1);
  }

  public int getWarDominion()
  {
    return _warDominion;
  }

  public void setWarDominion(int warDominion)
  {
    _warDominion = warDominion;
  }

  public Iterator<UnitMember> iterator()
  {
    List iterators = new ArrayList(_subUnits.size());
    for (SubUnit subUnit : _subUnits.values())
      iterators.add(subUnit.getUnitMembers().iterator());
    return new JoinedIterator(iterators);
  }

  private static class ClanReputationComparator
    implements Comparator<Clan>
  {
    public int compare(Clan o1, Clan o2)
    {
      if ((o1 == null) || (o2 == null))
        return 0;
      return o2.getReputationScore() - o1.getReputationScore();
    }
  }
}