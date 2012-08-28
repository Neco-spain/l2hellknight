package net.sf.l2j.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.communitybbs.BB.Forum;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CrownManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.PledgeReceiveSubPledgeCreated;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeSkillListAdd;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.util.Util;

public class L2Clan
{
  private static final Logger _log = Logger.getLogger(L2Clan.class.getName());
  private String _name;
  private int _clanId;
  private L2ClanMember _leader;
  private Map<String, L2ClanMember> _members = new FastMap();
  private String _allyName;
  private int _allyId;
  private int _level;
  private int _hasCastle;
  private int _hasHideout;
  private boolean _hasCrest;
  private int _hiredGuards;
  private int _crestId;
  private int _crestLargeId;
  private int _allyCrestId;
  private int _auctionBiddedAt = 0;
  private long _allyPenaltyExpiryTime;
  private int _allyPenaltyType;
  private long _charPenaltyExpiryTime;
  private long _dissolvingExpiryTime;
  public static final int PENALTY_TYPE_CLAN_LEAVED = 1;
  public static final int PENALTY_TYPE_CLAN_DISMISSED = 2;
  public static final int PENALTY_TYPE_DISMISS_CLAN = 3;
  public static final int PENALTY_TYPE_DISSOLVE_ALLY = 4;
  private ItemContainer _warehouse = new ClanWarehouse(this);
  private List<Integer> _atWarWith = new FastList();
  private List<Integer> _atWarAttackers = new FastList();
  private boolean _hasCrestLarge;
  private Forum _forum;
  private List<L2Skill> _skillList = new FastList();
  public static final int CP_NOTHING = 0;
  public static final int CP_CL_JOIN_CLAN = 2;
  public static final int CP_CL_GIVE_TITLE = 4;
  public static final int CP_CL_VIEW_WAREHOUSE = 8;
  public static final int CP_CL_MANAGE_RANKS = 16;
  public static final int CP_CL_PLEDGE_WAR = 32;
  public static final int CP_CL_DISMISS = 64;
  public static final int CP_CL_REGISTER_CREST = 128;
  public static final int CP_CL_MASTER_RIGHTS = 256;
  public static final int CP_CL_MANAGE_LEVELS = 512;
  public static final int CP_CH_OPEN_DOOR = 1024;
  public static final int CP_CH_OTHER_RIGHTS = 2048;
  public static final int CP_CH_AUCTION = 4096;
  public static final int CP_CH_DISMISS = 8192;
  public static final int CP_CH_SET_FUNCTIONS = 16384;
  public static final int CP_CS_OPEN_DOOR = 32768;
  public static final int CP_CS_MANOR_ADMIN = 65536;
  public static final int CP_CS_MANAGE_SIEGE = 131072;
  public static final int CP_CS_USE_FUNCTIONS = 262144;
  public static final int CP_CS_DISMISS = 524288;
  public static final int CP_CS_TAXES = 1048576;
  public static final int CP_CS_MERCENARIES = 2097152;
  public static final int CP_CS_SET_FUNCTIONS = 4194304;
  public static final int CP_ALL = 8388606;
  public static final int SUBUNIT_ACADEMY = -1;
  public static final int SUBUNIT_ROYAL1 = 100;
  public static final int SUBUNIT_ROYAL2 = 200;
  public static final int SUBUNIT_KNIGHT1 = 1001;
  public static final int SUBUNIT_KNIGHT2 = 1002;
  public static final int SUBUNIT_KNIGHT3 = 2001;
  public static final int SUBUNIT_KNIGHT4 = 2002;
  protected final Map<Integer, L2Skill> _skills = new FastMap();
  protected final Map<Integer, RankPrivs> _privs = new FastMap();
  protected final Map<Integer, SubPledge> _subPledges = new FastMap();

  private int _reputationScore = 0;
  private int _rank = 0;
  private String _notice;
  private boolean _noticeEnabled = false;
  private int _hasFort;

  public L2Clan(int clanId)
  {
    _clanId = clanId;
    initializePrivs();
    restore();
    getWarehouse().restore();
  }

  public L2Clan(int clanId, String clanName)
  {
    _clanId = clanId;
    _name = clanName;
    initializePrivs();
  }

  public int getClanId()
  {
    return _clanId;
  }

  public void setClanId(int clanId)
  {
    _clanId = clanId;
  }

  public int getLeaderId()
  {
    return _leader != null ? _leader.getObjectId() : 0;
  }

  public L2ClanMember getLeader()
  {
    return _leader;
  }

  public void setLeader(L2ClanMember leader)
  {
    _leader = leader;
    _members.put(leader.getName(), leader);
  }

  public void setNewLeader(L2ClanMember member, L2PcInstance activeChar)
  {
    if (((activeChar.isClanLeader()) && (activeChar.isRiding())) || ((activeChar.isClanLeader()) && (activeChar.isFlying())))
    {
      activeChar.sendPacket(new ActionFailed());
      return;
    }
    if (!getLeader().isOnline())
    {
      return;
    }
    if (member == null)
    {
      return;
    }
    if (!member.isOnline())
    {
      return;
    }

    L2PcInstance exLeader = getLeader().getPlayerInstance();
    SiegeManager.getInstance().removeSiegeSkills(exLeader);
    exLeader.setClan(this);
    exLeader.setClanPrivileges(0);
    exLeader.broadcastUserInfo();

    setLeader(member);
    updateClanInDB();

    exLeader.setPledgeClass(exLeader.getClan().getClanMember(exLeader.getObjectId()).calculatePledgeClass(exLeader));
    exLeader.broadcastUserInfo();
    L2PcInstance newLeader = member.getPlayerInstance();
    newLeader.setClan(this);
    newLeader.setPledgeClass(member.calculatePledgeClass(newLeader));
    newLeader.setClanPrivileges(8388606);
    if (getLevel() >= 4)
    {
      SiegeManager.getInstance().addSiegeSkills(newLeader);
    }
    newLeader.broadcastUserInfo();

    broadcastClanStatus();

    SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_S1);
    sm.addString(newLeader.getName());
    broadcastToOnlineMembers(sm);
    sm = null;
    CrownManager.getInstance().checkCrowns(exLeader);
    CrownManager.getInstance().checkCrowns(newLeader);
  }

  public String getLeaderName()
  {
    return _leader != null ? _leader.getName() : "";
  }

  public String getName()
  {
    return _name;
  }

  public void setName(String name)
  {
    _name = name;
  }

  private void addClanMember(L2ClanMember member)
  {
    _members.put(member.getName(), member);
  }

  public void addClanMember(L2PcInstance player)
  {
    L2ClanMember member = new L2ClanMember(this, player.getName(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getTitle());

    addClanMember(member);
    member.setPlayerInstance(player);
    player.setClan(this);
    player.setPledgeClass(member.calculatePledgeClass(player));

    player.sendPacket(new PledgeShowMemberListUpdate(player));
    player.sendPacket(new UserInfo(player));
  }

  public void updateClanMember(L2PcInstance player)
  {
    L2ClanMember member = new L2ClanMember(player);

    addClanMember(member);
  }

  public L2ClanMember getClanMember(String name)
  {
    return (L2ClanMember)_members.get(name);
  }

  public L2ClanMember getClanMember(int objectID)
  {
    for (L2ClanMember temp : _members.values())
    {
      if (temp.getObjectId() == objectID) return temp;
    }
    return null;
  }

  public void removeClanMember(String name, long clanJoinExpiryTime)
  {
    L2ClanMember exMember = (L2ClanMember)_members.remove(name);
    if (exMember == null)
    {
      _log.warning("Member " + name + " not found in clan while trying to remove");
      return;
    }
    int leadssubpledge = getLeaderSubPledge(name);
    if (leadssubpledge != 0)
    {
      getSubPledge(leadssubpledge).setLeaderName("");
      updateSubPledgeInDB(leadssubpledge);
    }

    if (exMember.getApprentice() != 0)
    {
      L2ClanMember apprentice = getClanMember(exMember.getApprentice());
      if (apprentice != null)
      {
        if (apprentice.getPlayerInstance() != null)
          apprentice.getPlayerInstance().setSponsor(0);
        else {
          apprentice.initApprenticeAndSponsor(0, 0);
        }
        apprentice.saveApprenticeAndSponsor(0, 0);
      }
    }
    if (exMember.getSponsor() != 0)
    {
      L2ClanMember sponsor = getClanMember(exMember.getSponsor());
      if (sponsor != null)
      {
        if (sponsor.getPlayerInstance() != null)
          sponsor.getPlayerInstance().setApprentice(0);
        else {
          sponsor.initApprenticeAndSponsor(0, 0);
        }
        sponsor.saveApprenticeAndSponsor(0, 0);
      }
    }
    exMember.saveApprenticeAndSponsor(0, 0);
    if (Config.REMOVE_CASTLE_CIRCLETS)
    {
      CastleManager.getInstance().removeCirclet(exMember, getHasCastle());
    }
    if (exMember.isOnline())
    {
      L2PcInstance player = exMember.getPlayerInstance();
      player.setTitle("");
      player.setApprentice(0);
      player.setSponsor(0);

      if (player.isClanLeader())
      {
        SiegeManager.getInstance().removeSiegeSkills(player);
        player.setClanCreateExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L);
      }

      for (L2Skill skill : player.getClan().getAllSkills())
        player.removeSkill(skill, false);
      player.setClan(null);
      player.setClanJoinExpiryTime(clanJoinExpiryTime);
      player.setPledgeClass(exMember.calculatePledgeClass(player));
      player.broadcastUserInfo();

      player.sendPacket(new PledgeShowMemberListDeleteAll());
    }
    else
    {
      removeMemberInDatabase(exMember, clanJoinExpiryTime, getLeaderName().equalsIgnoreCase(name) ? System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L : 0L);
    }
  }

  public L2ClanMember[] getMembers()
  {
    return (L2ClanMember[])_members.values().toArray(new L2ClanMember[_members.size()]);
  }

  public int getMembersCount()
  {
    return _members.size();
  }

  public int getSubPledgeMembersCount(int subpl)
  {
    int result = 0;
    for (L2ClanMember temp : _members.values())
    {
      if (temp.getPledgeType() == subpl) result++;
    }
    return result;
  }

  public int getMaxNrOfMembers(int pledgetype)
  {
    int limit = 0;

    switch (pledgetype)
    {
    case 0:
      switch (getLevel())
      {
      case 4:
        limit = 40;
        break;
      case 3:
        limit = 30;
        break;
      case 2:
        limit = 20;
        break;
      case 1:
        limit = 15;
        break;
      case 0:
        limit = 10;
        break;
      default:
        limit = 40;
      }break;
    case -1:
    case 100:
    case 200:
      limit = 20;
      break;
    case 1001:
    case 1002:
    case 2001:
    case 2002:
      limit = 10;
      break;
    }

    return limit;
  }

  public L2PcInstance[] getOnlineMembers(String exclude)
  {
    List result = new FastList();
    for (L2ClanMember temp : _members.values())
      try
      {
        if ((temp.isOnline()) && (!temp.getName().equals(exclude)))
          result.add(temp.getPlayerInstance());
      }
      catch (NullPointerException e) {
      }
    return (L2PcInstance[])result.toArray(new L2PcInstance[result.size()]);
  }

  public int getAllyId()
  {
    return _allyId;
  }

  public String getAllyName()
  {
    return _allyName;
  }

  public void setAllyCrestId(int allyCrestId)
  {
    _allyCrestId = allyCrestId;
  }

  public int getAllyCrestId()
  {
    return _allyCrestId;
  }

  public int getLevel()
  {
    return _level;
  }

  public int getHasCastle()
  {
    return _hasCastle;
  }

  public int getHasHideout()
  {
    return _hasHideout;
  }

  public void setCrestId(int crestId)
  {
    _crestId = crestId;
  }

  public int getCrestId()
  {
    return _crestId;
  }

  public void setCrestLargeId(int crestLargeId)
  {
    _crestLargeId = crestLargeId;
  }

  public int getCrestLargeId()
  {
    return _crestLargeId;
  }

  public void setAllyId(int allyId)
  {
    _allyId = allyId;
  }

  public void setAllyName(String allyName)
  {
    _allyName = allyName;
  }

  public void setHasCastle(int hasCastle)
  {
    _hasCastle = hasCastle;
  }

  public void setHasHideout(int hasHideout)
  {
    _hasHideout = hasHideout;
  }

  public void setLevel(int level)
  {
    _level = level;
    if (_forum == null)
    {
      if (_level >= 2)
      {
        _forum = ForumsBBSManager.getInstance().getForumByName("ClanRoot").getChildByName(_name);
        if (_forum == null)
        {
          _forum = ForumsBBSManager.getInstance().createNewForum(_name, ForumsBBSManager.getInstance().getForumByName("ClanRoot"), 2, 2, getClanId());
        }
      }
    }
  }

  public boolean isMember(String name)
  {
    return name == null ? false : _members.containsKey(name);
  }

  public void updateClanInDB()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=? WHERE clan_id=?");
      statement.setInt(1, getLeaderId());
      statement.setInt(2, getAllyId());
      statement.setString(3, getAllyName());
      statement.setInt(4, getReputationScore());
      statement.setLong(5, getAllyPenaltyExpiryTime());
      statement.setInt(6, getAllyPenaltyType());
      statement.setLong(7, getCharPenaltyExpiryTime());
      statement.setLong(8, getDissolvingExpiryTime());
      statement.setInt(9, getClanId());
      statement.execute();
      statement.close();
      if (Config.DEBUG) _log.fine("New clan leader saved in db: " + getClanId());
    }
    catch (Exception e)
    {
      _log.warning("error while saving new clan leader to db " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public void store() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id) values (?,?,?,?,?,?,?,?,?,?)");
      statement.setInt(1, getClanId());
      statement.setString(2, getName());
      statement.setInt(3, getLevel());
      statement.setInt(4, getHasCastle());
      statement.setInt(5, getAllyId());
      statement.setString(6, getAllyName());
      statement.setInt(7, getLeaderId());
      statement.setInt(8, getCrestId());
      statement.setInt(9, getCrestLargeId());
      statement.setInt(10, getAllyCrestId());
      statement.execute();
      statement.close();

      if (Config.DEBUG) _log.fine("New clan saved in db: " + getClanId());
    }
    catch (Exception e)
    {
      _log.warning("error while saving new clan to db " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void removeMemberInDatabase(L2ClanMember member, long clanJoinExpiryTime, long clanCreateExpiryTime) {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE obj_Id=?");
      statement.setString(1, "");
      statement.setLong(2, clanJoinExpiryTime);
      statement.setLong(3, clanCreateExpiryTime);
      statement.setInt(4, member.getObjectId());
      statement.execute();
      statement.close();
      if (Config.DEBUG) _log.fine("clan member removed in db: " + getClanId());

      statement = con.prepareStatement("UPDATE characters SET apprentice=0 WHERE apprentice=?");
      statement.setInt(1, member.getObjectId());
      statement.execute();
      statement.close();

      statement = con.prepareStatement("UPDATE characters SET sponsor=0 WHERE sponsor=?");
      statement.setInt(1, member.getObjectId());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("error while removing clan member in db " + e);
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  private void updateWarsInDB() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("UPDATE clan_wars SET wantspeace1=? WHERE clan1=?");
      statement.setInt(1, 0);
      statement.setInt(2, 0);

      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("could not update clans wars data:" + e);
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

  private void restore()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,reputation_score,auction_bid_at,ally_penalty_expiry_time,ally_penalty_type,char_penalty_expiry_time,dissolving_expiry_time FROM clan_data where clan_id=?");
      statement.setInt(1, getClanId());
      ResultSet clanData = statement.executeQuery();

      if (clanData.next())
      {
        setName(clanData.getString("clan_name"));
        setLevel(clanData.getInt("clan_level"));
        setHasCastle(clanData.getInt("hasCastle"));
        setAllyId(clanData.getInt("ally_id"));
        setAllyName(clanData.getString("ally_name"));
        setAllyPenaltyExpiryTime(clanData.getLong("ally_penalty_expiry_time"), clanData.getInt("ally_penalty_type"));
        if (getAllyPenaltyExpiryTime() < System.currentTimeMillis())
        {
          setAllyPenaltyExpiryTime(0L, 0);
        }
        setCharPenaltyExpiryTime(clanData.getLong("char_penalty_expiry_time"));
        if (getCharPenaltyExpiryTime() + Config.ALT_CLAN_JOIN_DAYS * 86400000L < System.currentTimeMillis())
        {
          setCharPenaltyExpiryTime(0L);
        }
        setDissolvingExpiryTime(clanData.getLong("dissolving_expiry_time"));

        setCrestId(clanData.getInt("crest_id"));
        if (getCrestId() != 0)
        {
          setHasCrest(true);
        }

        setCrestLargeId(clanData.getInt("crest_large_id"));
        if (getCrestLargeId() != 0)
        {
          setHasCrestLarge(true);
        }

        setAllyCrestId(clanData.getInt("ally_crest_id"));
        setReputationScore(clanData.getInt("reputation_score"), false);
        setAuctionBiddedAt(clanData.getInt("auction_bid_at"), false);

        int leaderId = clanData.getInt("leader_id");

        PreparedStatement statement2 = con.prepareStatement("SELECT char_name,level,classid,obj_Id,title,power_grade,subpledge,apprentice,sponsor FROM characters WHERE clanid=?");
        statement2.setInt(1, getClanId());
        ResultSet clanMembers = statement2.executeQuery();

        while (clanMembers.next())
        {
          L2ClanMember member = new L2ClanMember(this, clanMembers.getString("char_name"), clanMembers.getInt("level"), clanMembers.getInt("classid"), clanMembers.getInt("obj_id"), clanMembers.getInt("subpledge"), clanMembers.getInt("power_grade"), clanMembers.getString("title"));
          if (member.getObjectId() == leaderId)
            setLeader(member);
          else
            addClanMember(member);
          member.initApprenticeAndSponsor(clanMembers.getInt("apprentice"), clanMembers.getInt("sponsor"));
        }
        clanMembers.close();
        statement2.close();
      }

      clanData.close();
      statement.close();

      if ((Config.DEBUG) && (getName() != null))
        _log.config("Restored clan data for \"" + getName() + "\" from database.");
      restoreSubPledges();
      restoreRankPrivs();
      restoreSkills();
      restoreNotice();
    }
    catch (Exception e)
    {
      _log.warning("error while restoring clan " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void restoreSkills() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?");
      statement.setInt(1, getClanId());

      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        int id = rset.getInt("skill_id");
        int level = rset.getInt("skill_level");

        L2Skill skill = SkillTable.getInstance().getInfo(id, level);

        _skills.put(Integer.valueOf(skill.getId()), skill);
      }

      rset.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Could not restore clan skills: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void restoreNotice() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT enabled,notice FROM clan_notices WHERE clan_id=?");
      statement.setInt(1, getClanId());
      ResultSet noticeData = statement.executeQuery();

      while (noticeData.next())
      {
        _noticeEnabled = noticeData.getBoolean("enabled");
        _notice = noticeData.getString("notice");
      }

      noticeData.close();
      statement.close();
      con.close();
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Error restoring clan notice.", e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void storeNotice(String notice, boolean enabled) {
    if (notice == null) {
      notice = "";
    }
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      if (notice.length() > 8192) {
        notice = notice.substring(0, 8191);
      }
      if (_notice != null)
      {
        PreparedStatement statement = con.prepareStatement("UPDATE clan_notices SET enabled=?,notice=? WHERE clan_id=?");
        if (enabled)
          statement.setString(1, "true");
        else
          statement.setString(1, "false");
        statement.setString(2, notice);
        statement.setInt(3, getClanId());
        statement.execute();
        statement.close();
      }
      else
      {
        PreparedStatement statement = con.prepareStatement("INSERT INTO clan_notices (clan_id, enabled, notice) values (?,?,?)");
        statement.setInt(1, getClanId());
        if (enabled)
          statement.setString(2, "true");
        else
          statement.setString(2, "false");
        statement.setString(3, notice);
        statement.execute();
        statement.close();
      }
    }
    catch (Exception e)
    {
      _log.warning("Error could not store clan notice: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    _notice = notice;
    _noticeEnabled = enabled;
  }

  public void setNoticeEnabled(boolean noticeEnabled)
  {
    storeNotice(_notice, noticeEnabled);
  }

  public void setNotice(String notice)
  {
    storeNotice(notice, _noticeEnabled);
  }

  public boolean isNoticeEnabled()
  {
    return _noticeEnabled;
  }

  public String getNotice()
  {
    if (_notice == null)
      return "";
    return _notice;
  }

  public final L2Skill[] getAllSkills()
  {
    if (_skills == null) {
      return new L2Skill[0];
    }
    return (L2Skill[])_skills.values().toArray(new L2Skill[_skills.values().size()]);
  }

  public L2Skill addSkill(L2Skill newSkill)
  {
    L2Skill oldSkill = null;

    if (newSkill != null)
    {
      oldSkill = (L2Skill)_skills.put(Integer.valueOf(newSkill.getId()), newSkill);
    }

    return oldSkill;
  }

  public L2Skill addNewSkill(L2Skill newSkill)
  {
    L2Skill oldSkill = null;
    Connection con = null;

    if (newSkill != null)
    {
      oldSkill = (L2Skill)_skills.put(Integer.valueOf(newSkill.getId()), newSkill);
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();

        if (oldSkill != null)
        {
          PreparedStatement statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?");
          statement.setInt(1, newSkill.getLevel());
          statement.setInt(2, oldSkill.getId());
          statement.setInt(3, getClanId());
          statement.execute();
          statement.close();
        }
        else
        {
          PreparedStatement statement = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name) VALUES (?,?,?,?)");
          statement.setInt(1, getClanId());
          statement.setInt(2, newSkill.getId());
          statement.setInt(3, newSkill.getLevel());
          statement.setString(4, newSkill.getName());
          statement.execute();
          statement.close();
        }
      }
      catch (Exception e)
      {
        _log.warning("Error could not store char skills: " + e);
      }
      finally {
        try {
          con.close();
        } catch (Exception e) {
        }
      }
      for (L2ClanMember temp : _members.values())
        try
        {
          if (temp.isOnline())
          {
            if (newSkill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
            {
              temp.getPlayerInstance().addSkill(newSkill, false);
              temp.getPlayerInstance().sendPacket(new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel()));
            }
          }
        }
        catch (NullPointerException e) {
        }
    }
    return oldSkill;
  }

  public void addSkillEffects()
  {
    for (Iterator i$ = _skills.values().iterator(); i$.hasNext(); ) { skill = (L2Skill)i$.next();

      for (L2ClanMember temp : _members.values())
        try
        {
          if (temp.isOnline())
          {
            if (skill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
              temp.getPlayerInstance().addSkill(skill, false);
          }
        } catch (NullPointerException e) {
        } }
    L2Skill skill;
  }

  public void addSkillEffects(L2PcInstance cm) {
    if (cm == null) {
      return;
    }
    for (L2Skill skill : _skills.values())
    {
      if (skill.getMinPledgeClass() <= cm.getPledgeClass())
        cm.addSkill(skill, false);
    }
  }

  public void broadcastToOnlineAllyMembers(L2GameServerPacket packet)
  {
    if (getAllyId() == 0)
    {
      return;
    }
    for (L2Clan clan : ClanTable.getInstance().getClans())
    {
      if (clan.getAllyId() != getAllyId())
        continue;
      clan.broadcastToOnlineMembers(packet);
    }
  }

  public void broadcastToOnlineMembers(L2GameServerPacket packet)
  {
    for (L2ClanMember member : _members.values())
      try
      {
        if (member.isOnline())
          member.getPlayerInstance().sendPacket(packet);
      }
      catch (NullPointerException e) {
      }
  }

  public void broadcastToOtherOnlineMembers(L2GameServerPacket packet, L2PcInstance player) {
    for (L2ClanMember member : _members.values())
      try
      {
        if ((member.isOnline()) && (member.getPlayerInstance() != player))
          member.getPlayerInstance().sendPacket(packet);
      }
      catch (NullPointerException e)
      {
      }
  }

  public String toString() {
    return getName();
  }

  public boolean hasCrest()
  {
    return _hasCrest;
  }

  public boolean hasCrestLarge()
  {
    return _hasCrestLarge;
  }

  public void setHasCrest(boolean flag)
  {
    _hasCrest = flag;
  }

  public void setHasCrestLarge(boolean flag)
  {
    _hasCrestLarge = flag;
  }

  public ItemContainer getWarehouse()
  {
    return _warehouse;
  }

  public boolean isAtWarWith(Integer id)
  {
    return (_atWarWith != null) && (_atWarWith.size() > 0) && 
      (_atWarWith.contains(id));
  }

  public boolean isAtWarAttacker(Integer id)
  {
    return (_atWarAttackers != null) && (_atWarAttackers.size() > 0) && 
      (_atWarAttackers.contains(id));
  }

  public void setEnemyClan(L2Clan clan)
  {
    Integer id = Integer.valueOf(clan.getClanId());
    _atWarWith.add(id);
  }

  public void setEnemyClan(Integer clan) {
    _atWarWith.add(clan);
  }

  public void setAttackerClan(L2Clan clan) {
    Integer id = Integer.valueOf(clan.getClanId());
    _atWarAttackers.add(id);
  }

  public void setAttackerClan(Integer clan) {
    _atWarAttackers.add(clan);
  }

  public void deleteEnemyClan(L2Clan clan) {
    Integer id = Integer.valueOf(clan.getClanId());
    _atWarWith.remove(id);
  }

  public void deleteAttackerClan(L2Clan clan) {
    Integer id = Integer.valueOf(clan.getClanId());
    _atWarAttackers.remove(id);
  }
  public int getHiredGuards() { return _hiredGuards; } 
  public void incrementHiredGuards() { _hiredGuards += 1; }

  public int isAtWar()
  {
    if ((_atWarWith != null) && (_atWarWith.size() > 0))
      return 1;
    return 0;
  }

  public List<Integer> getWarList()
  {
    return _atWarWith;
  }

  public List<Integer> getAttackerList()
  {
    return _atWarAttackers;
  }

  public void broadcastClanStatus()
  {
    for (L2PcInstance member : getOnlineMembers(""))
    {
      member.sendPacket(new PledgeShowMemberListDeleteAll());
      member.sendPacket(new PledgeShowMemberListAll(this, member));
    }
  }

  public void removeSkill(int id)
  {
    L2Skill deleteSkill = null;
    for (L2Skill sk : _skillList)
    {
      if (sk.getId() == id)
      {
        deleteSkill = sk;
        return;
      }
    }
    _skillList.remove(deleteSkill);
  }

  public void removeSkill(L2Skill deleteSkill)
  {
    _skills.remove(Integer.valueOf(deleteSkill.getId()));
    if (Config.DEBUG) _log.fine("Fort Skill removed");
  }

  public List<L2Skill> getSkills()
  {
    return _skillList;
  }

  private void restoreSubPledges()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT sub_pledge_id,name,leader_name FROM clan_subpledges WHERE clan_id=?");
      statement.setInt(1, getClanId());
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        int id = rset.getInt("sub_pledge_id");
        String name = rset.getString("name");
        String leaderName = rset.getString("leader_name");

        SubPledge pledge = new SubPledge(id, name, leaderName);
        _subPledges.put(Integer.valueOf(id), pledge);
      }

      rset.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Could not restore clan sub-units: " + e);
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  public final SubPledge getSubPledge(int pledgeType) {
    if (_subPledges == null) {
      return null;
    }
    return (SubPledge)_subPledges.get(Integer.valueOf(pledgeType));
  }

  public final SubPledge getSubPledge(String pledgeName)
  {
    if (_subPledges == null) {
      return null;
    }
    for (SubPledge sp : _subPledges.values())
    {
      if (sp.getName().equalsIgnoreCase(pledgeName))
      {
        return sp;
      }
    }
    return null;
  }

  public final SubPledge[] getAllSubPledges()
  {
    if (_subPledges == null) {
      return new SubPledge[0];
    }
    return (SubPledge[])_subPledges.values().toArray(new SubPledge[_subPledges.values().size()]);
  }

  public SubPledge createSubPledge(L2PcInstance player, int pledgeType, String leaderName, String subPledgeName)
  {
    SubPledge subPledge = null;
    pledgeType = getAvailablePledgeTypes(pledgeType);
    if (pledgeType == 0)
    {
      if (pledgeType == -1)
        player.sendPacket(new SystemMessage(SystemMessageId.CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY));
      else
        player.sendMessage("You can't create any more sub-units of this type");
      return null;
    }
    if (_leader.getName().equals(leaderName))
    {
      player.sendMessage("Leader is not correct");
      return null;
    }

    if ((pledgeType != -1) && (((getReputationScore() < 5000) && (pledgeType < 1001)) || ((getReputationScore() < 10000) && (pledgeType > 200))))
    {
      SystemMessage sp = new SystemMessage(SystemMessageId.CLAN_REPUTATION_SCORE_IS_TOO_LOW);
      player.sendPacket(sp);
      return null;
    }

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_name) values (?,?,?,?)");
      statement.setInt(1, getClanId());
      statement.setInt(2, pledgeType);
      statement.setString(3, subPledgeName);
      if (pledgeType != -1)
        statement.setString(4, leaderName);
      else
        statement.setString(4, "");
      statement.execute();
      statement.close();

      subPledge = new SubPledge(pledgeType, subPledgeName, leaderName);
      _subPledges.put(Integer.valueOf(pledgeType), subPledge);

      if (pledgeType != -1)
      {
        setReputationScore(getReputationScore() - 2500, true);
      }

      if (Config.DEBUG) _log.fine("New sub_clan saved in db: " + getClanId() + "; " + pledgeType);
    }
    catch (Exception e)
    {
      _log.warning("error while saving new sub_clan to db " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    broadcastToOnlineMembers(new PledgeShowInfoUpdate(_leader.getClan()));
    broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(subPledge));
    return subPledge;
  }

  public int getAvailablePledgeTypes(int pledgeType)
  {
    if (_subPledges.get(Integer.valueOf(pledgeType)) != null)
    {
      switch (pledgeType)
      {
      case -1:
        return 0;
      case 100:
        pledgeType = getAvailablePledgeTypes(200);
        break;
      case 200:
        return 0;
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
        return 0;
      }
    }
    return pledgeType;
  }

  public void updateSubPledgeInDB(int pledgeType)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE clan_subpledges SET leader_name=? WHERE clan_id=? AND sub_pledge_id=?");
      statement.setString(1, getSubPledge(pledgeType).getLeaderName());
      statement.setInt(2, getClanId());
      statement.setInt(3, pledgeType);
      statement.execute();
      statement.close();
      if (Config.DEBUG)
        _log.fine("New subpledge leader saved in db: " + getClanId());
    }
    catch (Exception e)
    {
      _log.warning("error while saving new clan leader to db " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void restoreRankPrivs() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT privs,rank,party FROM clan_privs WHERE clan_id=?");
      statement.setInt(1, getClanId());

      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        int rank = rset.getInt("rank");

        int privileges = rset.getInt("privs");

        ((RankPrivs)_privs.get(Integer.valueOf(rank))).setPrivs(privileges);
      }

      rset.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Could not restore clan privs by rank: " + e);
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  public void initializePrivs() {
    for (int i = 1; i < 10; i++)
    {
      RankPrivs privs = new RankPrivs(i, 0, 0);
      _privs.put(Integer.valueOf(i), privs);
    }
  }

  public int getRankPrivs(int rank)
  {
    if (_privs.get(Integer.valueOf(rank)) != null) {
      return ((RankPrivs)_privs.get(Integer.valueOf(rank))).getPrivs();
    }
    return 0;
  }

  public void setRankPrivs(int rank, int privs) {
    if (_privs.get(Integer.valueOf(rank)) != null)
    {
      ((RankPrivs)_privs.get(Integer.valueOf(rank))).setPrivs(privs);

      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,party,privs) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE privs = ?");
        statement.setInt(1, getClanId());
        statement.setInt(2, rank);
        statement.setInt(3, 0);
        statement.setInt(4, privs);
        statement.setInt(5, privs);

        statement.execute();
        statement.close();
      }
      catch (Exception e)
      {
        _log.warning("Could not store clan privs for rank: " + e);
      }
      finally {
        try {
          con.close(); } catch (Exception e) {
        }
      }
      for (L2ClanMember cm : getMembers())
      {
        if ((!cm.isOnline()) || 
          (cm.getPowerGrade() != rank) || 
          (cm.getPlayerInstance() == null))
          continue;
        cm.getPlayerInstance().setClanPrivileges(privs);
        cm.getPlayerInstance().sendPacket(new UserInfo(cm.getPlayerInstance()));
      }

      broadcastClanStatus();
    }
    else
    {
      _privs.put(Integer.valueOf(rank), new RankPrivs(rank, 0, privs));

      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,party,privs) VALUES (?,?,?,?)");
        statement.setInt(1, getClanId());
        statement.setInt(2, rank);
        statement.setInt(3, 0);
        statement.setInt(4, privs);
        statement.execute();
        statement.close();
      }
      catch (Exception e)
      {
        _log.warning("Could not create new rank and store clan privs for rank: " + e);
      }
      finally {
        try {
          con.close();
        } catch (Exception e) {
        }
      }
    }
  }

  public final RankPrivs[] getAllRankPrivs() {
    if (_privs == null) {
      return new RankPrivs[0];
    }
    return (RankPrivs[])_privs.values().toArray(new RankPrivs[_privs.values().size()]);
  }

  public int getLeaderSubPledge(String name)
  {
    int id = 0;
    for (SubPledge sp : _subPledges.values())
    {
      if (sp.getLeaderName() != null)
        if (sp.getLeaderName().equals(name))
          id = sp.getId();
    }
    return id;
  }

  public void setReputationScore(int value, boolean save)
  {
    L2Skill[] skills;
    L2Skill[] skills;
    if ((_reputationScore >= 0) && (value < 0))
    {
      broadcastToOnlineMembers(new SystemMessage(SystemMessageId.REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED));
      skills = getAllSkills();
      for (L2ClanMember member : _members.values())
      {
        if ((member.isOnline()) && (member.getPlayerInstance() != null))
        {
          for (L2Skill sk : skills)
            member.getPlayerInstance().removeSkill(sk, false);
        }
      }
    }
    else if ((_reputationScore < 0) && (value >= 0))
    {
      broadcastToOnlineMembers(new SystemMessage(SystemMessageId.CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER));
      skills = getAllSkills();
      for (L2ClanMember member : _members.values())
      {
        if ((member.isOnline()) && (member.getPlayerInstance() != null))
        {
          for (L2Skill sk : skills)
          {
            if (sk.getMinPledgeClass() <= member.getPlayerInstance().getPledgeClass()) {
              member.getPlayerInstance().addSkill(sk, false);
            }
          }
        }
      }
    }
    _reputationScore = value;
    if (_reputationScore > 100000000) _reputationScore = 100000000;
    if (_reputationScore < -100000000) _reputationScore = -100000000;
    if (save) updateClanInDB();
  }

  public int getReputationScore()
  {
    return _reputationScore;
  }

  public void setRank(int rank)
  {
    _rank = rank;
  }

  public int getRank()
  {
    return _rank;
  }

  public int getAuctionBiddedAt() {
    return _auctionBiddedAt;
  }

  public void setAuctionBiddedAt(int id, boolean storeInDb)
  {
    _auctionBiddedAt = id;

    if (storeInDb)
    {
      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?");
        statement.setInt(1, id);
        statement.setInt(2, getClanId());
        statement.execute();
        statement.close();
      }
      catch (Exception e)
      {
        _log.warning("Could not store auction for clan: " + e);
      }
      finally {
        try {
          con.close();
        }
        catch (Exception e)
        {
        }
      }
    }
  }

  public boolean checkClanJoinCondition(L2PcInstance activeChar, L2PcInstance target, int pledgeType)
  {
    if (activeChar == null)
    {
      return false;
    }
    if ((activeChar.getClanPrivileges() & 0x2) != 2)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
      return false;
    }
    if (target == null)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
      return false;
    }
    if (activeChar.getObjectId() == target.getObjectId())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_INVITE_YOURSELF));
      return false;
    }
    if (getCharPenaltyExpiryTime() > System.currentTimeMillis())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER);
      sm.addString(target.getName());
      activeChar.sendPacket(sm);
      sm = null;
      return false;
    }
    if (target.getClanId() != 0)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_WORKING_WITH_ANOTHER_CLAN);
      sm.addString(target.getName());
      activeChar.sendPacket(sm);
      sm = null;
      return false;
    }
    if (target.getClanId() != 0)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_WORKING_WITH_ANOTHER_CLAN);
      sm.addString(target.getName());
      activeChar.sendPacket(sm);
      sm = null;
      return false;
    }
    if (target.getClanJoinExpiryTime() > System.currentTimeMillis())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN);
      sm.addString(target.getName());
      activeChar.sendPacket(sm);
      sm = null;
      return false;
    }
    if (((target.getLevel() > 40) || (target.getClassId().level() >= 2)) && (pledgeType == -1))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY);
      sm.addString(target.getName());
      activeChar.sendPacket(sm);
      sm = null;
      activeChar.sendPacket(new SystemMessage(SystemMessageId.ACADEMY_REQUIREMENTS));
      return false;
    }
    if (getSubPledgeMembersCount(pledgeType) >= getMaxNrOfMembers(pledgeType))
    {
      if (pledgeType == 0)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_CLAN_IS_FULL);
        sm.addString(getName());
        activeChar.sendPacket(sm);
        sm = null;
      }
      else
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.SUBCLAN_IS_FULL));
      }
      return false;
    }
    return true;
  }

  public boolean checkAllyJoinCondition(L2PcInstance activeChar, L2PcInstance target)
  {
    if (activeChar == null)
    {
      return false;
    }
    if ((activeChar.getAllyId() == 0) || (!activeChar.isClanLeader()) || (activeChar.getClanId() != activeChar.getAllyId()))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
      return false;
    }
    L2Clan leaderClan = activeChar.getClan();
    if (leaderClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis())
    {
      if (leaderClan.getAllyPenaltyType() == 3)
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_INVITE_CLAN_WITHIN_1_DAY));
        return false;
      }
    }
    if (target == null)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
      return false;
    }
    if (activeChar.getObjectId() == target.getObjectId())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_INVITE_YOURSELF));
      return false;
    }
    if (target.getClan() == null)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_MUST_BE_IN_CLAN));
      return false;
    }
    if (!target.isClanLeader())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
      sm.addString(target.getName());
      activeChar.sendPacket(sm);
      sm = null;
      return false;
    }
    L2Clan targetClan = target.getClan();
    if (target.getAllyId() != 0)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE);
      sm.addString(targetClan.getName());
      sm.addString(targetClan.getAllyName());
      activeChar.sendPacket(sm);
      sm = null;
      return false;
    }
    if (targetClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis())
    {
      if (targetClan.getAllyPenaltyType() == 1)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY);
        sm.addString(target.getClan().getName());
        sm.addString(target.getClan().getAllyName());
        activeChar.sendPacket(sm);
        sm = null;
        return false;
      }
      if (targetClan.getAllyPenaltyType() == 2)
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_ENTER_ALLIANCE_WITHIN_1_DAY));
        return false;
      }
    }
    if ((activeChar.isInsideZone(4)) && (target.isInsideZone(4)))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE));
      return false;
    }
    if (leaderClan.isAtWarWith(Integer.valueOf(targetClan.getClanId())))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.MAY_NOT_ALLY_CLAN_BATTLE));
      return false;
    }

    int numOfClansInAlly = 0;
    for (L2Clan clan : ClanTable.getInstance().getClans())
    {
      if (clan.getAllyId() != activeChar.getAllyId())
        continue;
      numOfClansInAlly++;
    }

    if (numOfClansInAlly >= Config.ALT_MAX_NUM_OF_CLANS_IN_ALLY)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT));
      return false;
    }

    return true;
  }

  public long getAllyPenaltyExpiryTime()
  {
    return _allyPenaltyExpiryTime;
  }

  public int getAllyPenaltyType()
  {
    return _allyPenaltyType;
  }

  public void setAllyPenaltyExpiryTime(long expiryTime, int penaltyType)
  {
    _allyPenaltyExpiryTime = expiryTime;
    _allyPenaltyType = penaltyType;
  }

  public long getCharPenaltyExpiryTime()
  {
    return _charPenaltyExpiryTime;
  }

  public void setCharPenaltyExpiryTime(long time)
  {
    _charPenaltyExpiryTime = time;
  }

  public long getDissolvingExpiryTime()
  {
    return _dissolvingExpiryTime;
  }

  public void setDissolvingExpiryTime(long time)
  {
    _dissolvingExpiryTime = time;
  }

  public void createAlly(L2PcInstance player, String allyName) {
    if (null == player) {
      return;
    }
    if (Config.DEBUG) {
      _log.fine(player.getObjectId() + "(" + player.getName() + ") requested ally creation from ");
    }
    if (!player.isClanLeader())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE));
      return;
    }
    if (getAllyId() != 0)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_JOINED_ALLIANCE));
      return;
    }
    if (getLevel() < 5)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER));
      return;
    }
    if (getAllyPenaltyExpiryTime() > System.currentTimeMillis())
    {
      if (getAllyPenaltyType() == 4)
      {
        player.sendPacket(new SystemMessage(SystemMessageId.CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION));
        return;
      }
    }
    if (getDissolvingExpiryTime() > System.currentTimeMillis())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING));
      return;
    }
    if (!Util.isAlphaNumeric(allyName))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_ALLIANCE_NAME));
      return;
    }
    if ((allyName.length() > 16) || (allyName.length() < 2))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_ALLIANCE_NAME_LENGTH));
      return;
    }
    if (ClanTable.getInstance().isAllyExists(allyName))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.ALLIANCE_ALREADY_EXISTS));
      return;
    }

    setAllyId(getClanId());
    setAllyName(allyName.trim());
    setAllyPenaltyExpiryTime(0L, 0);
    updateClanInDB();

    player.sendPacket(new UserInfo(player));

    player.sendMessage("Alliance " + allyName + " has been created.");
  }

  public void dissolveAlly(L2PcInstance player)
  {
    if (getAllyId() == 0)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.NO_CURRENT_ALLIANCES));
      return;
    }
    if ((!player.isClanLeader()) || (getClanId() != getAllyId()))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
      return;
    }
    if (player.isInsideZone(4))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE));
      return;
    }

    broadcastToOnlineAllyMembers(new SystemMessage(SystemMessageId.ALLIANCE_DISOLVED));

    long currentTime = System.currentTimeMillis();
    for (L2Clan clan : ClanTable.getInstance().getClans())
    {
      if ((clan.getAllyId() != getAllyId()) || (clan.getClanId() == getClanId()))
        continue;
      clan.setAllyId(0);
      clan.setAllyName(null);
      clan.setAllyPenaltyExpiryTime(0L, 0);
      clan.updateClanInDB();
    }

    setAllyId(0);
    setAllyName(null);
    setAllyPenaltyExpiryTime(currentTime + Config.ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED * 86400000L, 4);

    updateClanInDB();

    player.deathPenalty(false);
  }

  public void levelUpClan(L2PcInstance player)
  {
    if (!player.isClanLeader())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
      return;
    }
    if (System.currentTimeMillis() < getDissolvingExpiryTime())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS));
      return;
    }

    boolean increaseClanLevel = false;

    switch (getLevel())
    {
    case 0:
      if ((player.getSp() < 30000) || (player.getAdena() < 650000))
        break;
      if (!player.reduceAdena("ClanLvl", 650000, player.getTarget(), true))
        break;
      player.setSp(player.getSp() - 30000);
      SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
      sp.addNumber(30000);
      player.sendPacket(sp);
      sp = null;
      increaseClanLevel = true;
      break;
    case 1:
      if ((player.getSp() < 150000) || (player.getAdena() < 2500000))
        break;
      if (!player.reduceAdena("ClanLvl", 2500000, player.getTarget(), true))
        break;
      player.setSp(player.getSp() - 150000);
      SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
      sp.addNumber(150000);
      player.sendPacket(sp);
      sp = null;
      increaseClanLevel = true;
      break;
    case 2:
      if ((player.getSp() < 500000) || (player.getInventory().getItemByItemId(1419) == null)) {
        break;
      }
      if (!player.destroyItemByItemId("ClanLvl", 1419, 1, player.getTarget(), false))
        break;
      player.setSp(player.getSp() - 500000);
      SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
      sp.addNumber(500000);
      player.sendPacket(sp);
      sp = null;
      SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
      sm.addItemName(1419);
      sm.addNumber(1);
      player.sendPacket(sm);
      sm = null;
      increaseClanLevel = true;
      break;
    case 3:
      if ((player.getSp() < 1400000) || (player.getInventory().getItemByItemId(3874) == null)) {
        break;
      }
      if (!player.destroyItemByItemId("ClanLvl", 3874, 1, player.getTarget(), false))
        break;
      player.setSp(player.getSp() - 1400000);
      SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
      sp.addNumber(1400000);
      player.sendPacket(sp);
      sp = null;
      SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
      sm.addItemName(3874);
      sm.addNumber(1);
      player.sendPacket(sm);
      sm = null;
      increaseClanLevel = true;
      break;
    case 4:
      if ((player.getSp() < 3500000) || (player.getInventory().getItemByItemId(3870) == null)) {
        break;
      }
      if (!player.destroyItemByItemId("ClanLvl", 3870, 1, player.getTarget(), false))
        break;
      player.setSp(player.getSp() - 3500000);
      SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
      sp.addNumber(3500000);
      player.sendPacket(sp);
      sp = null;
      SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
      sm.addItemName(3870);
      sm.addNumber(1);
      player.sendPacket(sm);
      sm = null;
      increaseClanLevel = true;
      break;
    case 5:
      if ((getReputationScore() < 10000) || (getMembersCount() < Config.CLAN_MEMBER_LVLUP6))
        break;
      setReputationScore(getReputationScore() - 10000, true);
      SystemMessage cr = new SystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
      cr.addNumber(10000);
      player.sendPacket(cr);
      cr = null;
      increaseClanLevel = true;
      break;
    case 6:
      if ((getReputationScore() < 20000) || (getMembersCount() < Config.CLAN_MEMBER_LVLUP7))
        break;
      setReputationScore(getReputationScore() - 20000, true);
      SystemMessage cr = new SystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
      cr.addNumber(20000);
      player.sendPacket(cr);
      cr = null;
      increaseClanLevel = true;
      break;
    case 7:
      if ((getReputationScore() < 40000) || (getMembersCount() < Config.CLAN_MEMBER_LVLUP8))
        break;
      setReputationScore(getReputationScore() - 40000, true);
      SystemMessage cr = new SystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
      cr.addNumber(40000);
      player.sendPacket(cr);
      cr = null;
      increaseClanLevel = true;
      break;
    default:
      return;
    }

    if (!increaseClanLevel)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.FAILED_TO_INCREASE_CLAN_LEVEL);
      player.sendPacket(sm);
      return;
    }

    StatusUpdate su = new StatusUpdate(player.getObjectId());
    su.addAttribute(13, player.getSp());
    player.sendPacket(su);

    ItemList il = new ItemList(player, false);
    player.sendPacket(il);

    MagicSkillUser MSU = new MagicSkillUser(player, player, SkillTable.getInstance().getInfo(5103, 1).getDisplayId(), 1, 1000, 1000);
    player.broadcastPacket(MSU);
    changeLevel(getLevel() + 1);
  }

  public void changeLevel(int level)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?");
      statement.setInt(1, level);
      statement.setInt(2, getClanId());
      statement.execute();
      statement.close();

      con.close();
    }
    catch (Exception e)
    {
      _log.warning("could not increase clan level:" + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    setLevel(level);

    if (getLeader().isOnline())
    {
      L2PcInstance leader = getLeader().getPlayerInstance();
      if (3 < level)
      {
        SiegeManager.getInstance().addSiegeSkills(leader);
      }
      else if (4 > level)
      {
        SiegeManager.getInstance().removeSiegeSkills(leader);
      }
      if (4 < level)
      {
        leader.sendPacket(new SystemMessage(SystemMessageId.CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS));
      }

    }

    broadcastToOnlineMembers(new SystemMessage(SystemMessageId.CLAN_LEVEL_INCREASED));
    broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
  }

  public void setHasFort(int hasFort)
  {
    _hasFort = hasFort;
  }

  public int getHasFort()
  {
    return _hasFort;
  }

  public class RankPrivs
  {
    private int _rankId;
    private int _party;
    private int _rankPrivs;

    public RankPrivs(int rank, int party, int privs)
    {
      _rankId = rank;
      _party = party;
      _rankPrivs = privs;
    }

    public int getRank()
    {
      return _rankId;
    }

    public int getParty() {
      return _party;
    }

    public int getPrivs() {
      return _rankPrivs;
    }

    public void setPrivs(int privs) {
      _rankPrivs = privs;
    }
  }

  public class SubPledge
  {
    private int _id;
    private String _subPledgeName;
    private String _leaderName;

    public SubPledge(int id, String name, String leaderName)
    {
      _id = id;
      _subPledgeName = name;
      _leaderName = leaderName;
    }

    public int getId()
    {
      return _id;
    }

    public String getName() {
      return _subPledgeName;
    }

    public String getLeaderName() {
      return _leaderName;
    }

    public void setLeaderName(String leaderName)
    {
      _leaderName = leaderName;
    }
  }
}