package l2rt.gameserver.model;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.database.*;
import l2rt.gameserver.cache.CrestCache;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.AuctionManager;
import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.instancemanager.FortressManager;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.model.entity.vehicle.L2AirShip;
import l2rt.gameserver.model.items.ClanWarehouse;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.L2ItemInstance.ItemClass;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.tables.ClanTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;
import l2rt.util.Log;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

public class L2Clan
{
	private static final Logger _log = Logger.getLogger(L2Clan.class.getName());

	private String _name;
	private int _clanId;
	private L2ClanMember _leader = null;
	FastMap<Integer, L2ClanMember> _members = new FastMap<Integer, L2ClanMember>().setShared(true);

	private int _allyId;
	private byte _level;
	private int _hasCastle = 0;
	private int _hasFortress = 0;
	private int _hiredGuards;
	private int _hasHideout = 0;
	private int _crestId;
	private int _crestLargeId;

	private long _expelledMemberTime;
	private long _leavedAllyTime;
	private long _dissolvedAllyTime;
	private L2AirShip _airship;
	private boolean _airshipLicense;
	private int _airshipFuel;

	// all these in milliseconds
	public static long EXPELLED_MEMBER_PENALTY = 24 * 60 * 60 * 1000L;
	public static long LEAVED_ALLY_PENALTY = 24 * 60 * 60 * 1000L;
	public static long DISSOLVED_ALLY_PENALTY = 24 * 60 * 60 * 1000L;

	private ClanWarehouse _warehouse = new ClanWarehouse(this);
	private int _whBonus = -1;

	private GArray<L2Clan> _atWarWith = new GArray<L2Clan>();
	private GArray<L2Clan> _underAttackFrom = new GArray<L2Clan>();

	protected FastMap<Integer, L2Skill> _skills = new FastMap<Integer, L2Skill>().setShared(true);
	protected FastMap<Integer, FastMap<Integer, L2Skill>> _squadSkills = new FastMap<Integer, FastMap<Integer, L2Skill>>().setShared(true);
	protected FastMap<Integer, RankPrivs> _Privs = new FastMap<Integer, RankPrivs>().setShared(true);
	protected FastMap<Integer, SubPledge> _SubPledges = new FastMap<Integer, SubPledge>().setShared(true);

	private int _reputation = 0;

	//	Clan Privileges: system
	public static final int CP_NOTHING = 0;
	public static final int CP_CL_INVITE_CLAN = 2; // Join clan
	public static final int CP_CL_MANAGE_TITLES = 4; // Give a title
	public static final int CP_CL_WAREHOUSE_SEARCH = 8; // View warehouse content
	public static final int CP_CL_MANAGE_RANKS = 16; // manage clan ranks
	public static final int CP_CL_CLAN_WAR = 32;
	public static final int CP_CL_DISMISS = 64;
	public static final int CP_CL_EDIT_CREST = 128; // Edit clan crest
	public static final int CP_CL_APPRENTICE = 256;
	public static final int CP_CL_TROOPS_FAME = 512;
	public static final int CP_CL_SUMMON_AIRSHIP = 1024;

	//	Clan Privileges: clan hall
	public static final int CP_CH_ENTRY_EXIT = 2048; // open a door
	public static final int CP_CH_USE_FUNCTIONS = 4096;
	public static final int CP_CH_AUCTION = 8192;
	public static final int CP_CH_DISMISS = 16384; // Выгнать чужаков из КХ
	public static final int CP_CH_SET_FUNCTIONS = 32768;

	//	Clan Privileges: castle/fotress
	public static final int CP_CS_ENTRY_EXIT = 65536;
	public static final int CP_CS_MANOR_ADMIN = 131072;
	public static final int CP_CS_MANAGE_SIEGE = 262144;
	public static final int CP_CS_USE_FUNCTIONS = 524288;
	public static final int CP_CS_DISMISS = 1048576; // Выгнать чужаков из замка/форта
	public static final int CP_CS_TAXES = 2097152;
	public static final int CP_CS_MERCENARIES = 4194304;
	public static final int CP_CS_SET_FUNCTIONS = 8388606;
	public static final int CP_ALL = 16777214;

	public static final int RANK_FIRST = 1;
	public static final int RANK_LAST = 9;

	// Sub-unit types
	public static final int SUBUNIT_ACADEMY = -1;
	public static final int SUBUNIT_NONE = 0;
	public static final int SUBUNIT_ROYAL1 = 100;
	public static final int SUBUNIT_ROYAL2 = 200;
	public static final int SUBUNIT_KNIGHT1 = 1001;
	public static final int SUBUNIT_KNIGHT2 = 1002;
	public static final int SUBUNIT_KNIGHT3 = 2001;
	public static final int SUBUNIT_KNIGHT4 = 2002;

	private final static ClanReputationComparator REPUTATION_COMPARATOR = new ClanReputationComparator();
	/** Количество мест в таблице рангов кланов */
	private final static int REPUTATION_PLACES = 100;

	private String _notice;
	public static final int NOTICE_MAX_LENGHT = 512;

	@SuppressWarnings("unused")
	private boolean _noticeEnabled = true;

	/**
	 * Конструктор используется только внутри для восстановления из базы
	 */
	private L2Clan(int clanId)
	{
		_clanId = clanId;
		InitializePrivs();
	}

	public L2Clan(int clanId, String clanName, L2ClanMember leader)
	{
		_clanId = clanId;
		_name = clanName;
		InitializePrivs();
		setLeader(leader);
		insertNotice();
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
		_members.put(leader.getObjectId(), leader);
	}

	public String getLeaderName()
	{
		return _leader.getName();
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
		_members.put(member.getObjectId(), member);
	}

	public void addClanMember(L2Player player)
	{
		addClanMember(new L2ClanMember(this, player.getName(), player.getTitle(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getApprentice(), false));
	}

	public L2ClanMember getClanMember(int id)
	{
		return _members.get(id);
	}

	public L2ClanMember getClanMember(String name)
	{
		for(L2ClanMember member : _members.values())
			if(member.getName().equals(name))
				return member;
		return null;
	}

	public int getMembersCount()
	{
		return _members.size();
	}

	public void flush()
	{
		for(L2ClanMember member : getMembers())
			removeClanMember(member.getObjectId());
		for(L2ItemInstance item : _warehouse.listItems(ItemClass.ALL))
			_warehouse.destroyItem(item.getItemId(), item.getCount());
		if(_hasCastle != 0)
			CastleManager.getInstance().getCastleByIndex(_hasCastle).changeOwner(null);
		if(_hasFortress != 0)
			FortressManager.getInstance().getFortressByIndex(_hasFortress).changeOwner(null);
	}

	public void removeClanMember(int id)
	{
		if(id == getLeaderId())
			return;
		L2ClanMember exMember = _members.remove(id);
		if(exMember == null)
			return;
		SubPledge sp = _SubPledges.get(exMember.getPledgeType());
		if(sp != null && sp.getLeaderId() == exMember.getObjectId()) // subpledge leader
			sp.setLeaderId(0); // clan leader has to assign another one, via villagemaster
		if(exMember.hasSponsor())
			getClanMember(exMember.getSponsor()).setApprentice(0);
		removeMemberInDatabase(exMember);
	}

	public L2ClanMember[] getMembers()
	{
		return _members.values().toArray(new L2ClanMember[_members.size()]);
	}

	public L2Player[] getOnlineMembers(int exclude)
	{
		GArray<L2Player> result = new GArray<L2Player>();
		for(L2ClanMember temp : _members.values())
			if(temp.isOnline() && temp.getObjectId() != exclude)
				result.add(temp.getPlayer());
		return result.toArray(new L2Player[result.size()]);
	}

	public int getAllyId()
	{
		return _allyId;
	}

	public byte getLevel()
	{
		return _level;
	}

	/**
	 * Возвращает замок, которым владеет клан
	 * @return ID замка
	 */
	public int getHasCastle()
	{
		return _hasCastle;
	}

	/**
	 * Возвращает крепость, которой владеет клан
	 * @return ID крепости
	 */
	public int getHasFortress()
	{
		return _hasFortress;
	}

	/**
	 * Возвращает кланхолл, которым владеет клан
	 * @return ID кланхолла
	 */
	public int getHasHideout()
	{
		return _hasHideout;
	}

	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}

	/**
	 * Устанавливает замок, которым владеет клан.<BR>
	 * Одновременно владеть и замком и крепостью нельзя
	 * @param castle ID замка
	 */
	public void setHasCastle(int castle)
	{
		if(_hasFortress == 0)
			_hasCastle = castle;
	}

	/**
	 * Устанавливает крепость, которой владеет клан.<BR>
	 * Одновременно владеть и крепостью и замком нельзя
	 * @param fortress ID крепости
	 */
	public void setHasFortress(int fortress)
	{
		if(_hasCastle == 0)
			_hasFortress = fortress;
	}

	public void setHasHideout(int hasHideout)
	{
		_hasHideout = hasHideout;
	}

	public void setLevel(byte level)
	{
		_level = level;
	}

	public boolean isMember(Integer id)
	{
		return _members.containsKey(id);
	}

	public void updateClanInDB()
	{
		if(getLeaderId() == 0)
		{
			_log.warning("updateClanInDB with empty LeaderId");
			Thread.dumpStack();
			return;
		}

		if(getClanId() == 0)
		{
			_log.warning("updateClanInDB with empty ClanId");
			Thread.dumpStack();
			return;
		}
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET leader_id=?,ally_id=?,reputation_score=?,expelled_member=?,leaved_ally=?,dissolved_ally=?,clan_level=?,warehouse=?,clan_name=?,airship=? WHERE clan_id=?");
			statement.setInt(1, getLeaderId());
			statement.setInt(2, getAllyId());
			statement.setInt(3, getReputationScore());
			statement.setLong(4, getExpelledMemberTime() / 1000);
			statement.setLong(5, getLeavedAllyTime() / 1000);
			statement.setLong(6, getDissolvedAllyTime() / 1000);
			statement.setInt(7, _level);
			statement.setInt(8, getWhBonus());
			statement.setString(9, getName());
			statement.setInt(10, isHaveAirshipLicense() ? getAirshipFuel() : -1);
			statement.setInt(11, getClanId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warning("error while updating clan '" + getClanId() + "' data in db");
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void store()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,hasFortress,hasHideout,ally_id,leader_id,expelled_member,leaved_ally,dissolved_ally,airship) values (?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, _clanId);
			statement.setString(2, _name);
			statement.setInt(3, _level);
			statement.setInt(4, _hasCastle);
			statement.setInt(5, _hasFortress);
			statement.setInt(6, _hasHideout);
			statement.setInt(7, _allyId);
			statement.setInt(8, getLeaderId());
			statement.setLong(9, getExpelledMemberTime() / 1000);
			statement.setLong(10, getLeavedAllyTime() / 1000);
			statement.setLong(11, getDissolvedAllyTime() / 1000);
			statement.setInt(12, isHaveAirshipLicense() ? getAirshipFuel() : -1);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("UPDATE characters SET clanid=?,pledge_type=0 WHERE obj_Id=?");
			statement.setInt(1, getClanId());
			statement.setInt(2, getLeaderId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warning("error while saving new clan to db");
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void removeMemberInDatabase(L2ClanMember member)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET clanid=0, pledge_type=0, pledge_rank=0, lvl_joined_academy=0, apprentice=0, title='', leaveclan=? WHERE obj_Id=?");
			statement.setLong(1, System.currentTimeMillis() / 1000);
			statement.setInt(2, member.getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warning("error while removing clan member in db " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public static L2Clan restore(int clanId)
	{
		if(clanId == 0) // no clan
			return null;

		L2Clan clan = null;
		int leaderId = 0;

		ThreadConnection con1 = null;
		FiltredPreparedStatement statement1 = null;
		ResultSet clanData = null;

		try
		{
			con1 = L2DatabaseFactory.getInstance().getConnection();
			statement1 = con1.prepareStatement("SELECT clan_name,clan_level,hasCastle,hasFortress,hasHideout,ally_id,leader_id,reputation_score,expelled_member,leaved_ally,dissolved_ally,auction_bid_at,warehouse,airship FROM clan_data where clan_id=?");
			statement1.setInt(1, clanId);
			clanData = statement1.executeQuery();

			if(clanData.next())
			{
				clan = new L2Clan(clanId);
				clan.setName(clanData.getString("clan_name"));
				clan.setLevel(clanData.getByte("clan_level"));
				clan.setHasCastle(clanData.getByte("hasCastle"));
				clan.setHasFortress(clanData.getByte("hasFortress"));
				clan.setHasHideout(clanData.getInt("hasHideout"));
				clan.setAllyId(clanData.getInt("ally_id"));
				clan._reputation = clanData.getInt("reputation_score");
				clan.setAuctionBiddedAt(clanData.getInt("auction_bid_at"));
				clan.setExpelledMemberTime(clanData.getLong("expelled_member") * 1000L);
				clan.setLeavedAllyTime(clanData.getLong("leaved_ally") * 1000L);
				clan.setDissolvedAllyTime(clanData.getLong("dissolved_ally") * 1000L);
				clan.setWhBonus(clanData.getInt("warehouse"));
				clan.setAirshipLicense(clanData.getInt("airship") == -1 ? false : true);
				if(clan.isHaveAirshipLicense())
					clan.setAirshipFuel(clanData.getInt("airship"));

				leaderId = clanData.getInt("leader_id");
			}
			else
			{
				_log.warning("L2Clan.java clan " + clanId + " does't exist");
				return null;
			}

			if(clan.getName() == null)
				_log.config("null name for clan?? " + clanId);

			if(clan.getAuctionBiddedAt() > 0 && AuctionManager.getInstance().getAuction(clan.getAuctionBiddedAt()) == null)
				clan.setAuctionBiddedAt(0);
		}
		catch(Exception e)
		{
			_log.warning("error while restoring clan " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con1, statement1, clanData);
		}

		if(clan == null)
		{
			_log.fine("Clan " + clanId + " does't exist");
			return null;
		}

		if(leaderId == 0)
		{
			_log.fine("Not found leader for clan: " + clanId);
			return null;
		}

		ThreadConnection con2 = null;
		FiltredPreparedStatement statement2 = null;
		ResultSet clanMembers = null;

		try
		{
			con2 = L2DatabaseFactory.getInstance().getConnection();
			statement2 = con2.prepareStatement(//
			"SELECT `c`.`char_name` AS `char_name`," + //
			"`s`.`level` AS `level`," + //
			"`s`.`class_id` AS `classid`," + //
			"`c`.`obj_Id` AS `obj_id`," + //
			"`c`.`title` AS `title`," + //
			"`c`.`pledge_type` AS `pledge_type`," + //
			"`c`.`pledge_rank` AS `pledge_rank`," + //
			"`c`.`apprentice` AS `apprentice` " + //
			"FROM `characters` `c` " + //
			"LEFT JOIN `character_subclasses` `s` ON (`s`.`char_obj_id` = `c`.`obj_Id` AND `s`.`isBase` = '1') " + //
			"WHERE `c`.`clanid`=? ORDER BY `c`.`lastaccess` DESC");

			statement2.setInt(1, clanId);
			clanData = statement2.executeQuery();

			statement2.setInt(1, clan.getClanId());
			clanMembers = statement2.executeQuery();

			while(clanMembers.next())
			{
				L2ClanMember member = new L2ClanMember(clan, clanMembers.getString("char_name"), clanMembers.getString("title"), clanMembers.getInt("level"), clanMembers.getInt("classid"), clanMembers.getInt("obj_id"), clanMembers.getInt("pledge_type"), clanMembers.getInt("pledge_rank"), clanMembers.getInt("apprentice"), clanMembers.getInt("obj_id") == leaderId);
				if(member.getObjectId() == leaderId)
					clan.setLeader(member);
				else
					clan.addClanMember(member);
			}

			if(clan.getLeader() == null)
				_log.severe("Clan " + clan.getName() + " have no leader!");
		}
		catch(Exception e)
		{
			_log.warning("Error while restoring clan members for clan: " + clanId + " " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con2, statement2, clanMembers);
		}
		clan.restoreSkills();
		clan.restoreSubPledges();
		clan.restoreRankPrivs();
		clan.setCrestId(CrestCache.getPledgeCrestId(clanId));
		clan.setCrestLargeId(CrestCache.getPledgeCrestLargeId(clanId));
		return clan;
	}

	public void broadcastToOnlineMembers(L2GameServerPacket... packets)
	{
		for(L2ClanMember member : _members.values())
			if(member.isOnline())
				member.getPlayer().sendPacket(packets);
	}

	public void broadcastToOtherOnlineMembers(L2GameServerPacket packet, L2Player player)
	{
		for(L2ClanMember member : _members.values())
			if(member.isOnline() && member.getPlayer() != player)
				member.getPlayer().sendPacket(packet);
	}

	@Override
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
		return _warehouse.getAdenaCount();
	}

	public ClanWarehouse getWarehouse()
	{
		return _warehouse;
	}

	public int getHiredGuards()
	{
		return _hiredGuards;
	}

	public void incrementHiredGuards()
	{
		_hiredGuards++;
	}

	public int isAtWar()
	{
		if(_atWarWith != null && !_atWarWith.isEmpty())
			return 1;
		return 0;
	}

	public int isAtWarOrUnderAttack()
	{
		if(_atWarWith != null && !_atWarWith.isEmpty() || _underAttackFrom != null && !_underAttackFrom.isEmpty())
			return 1;
		return 0;
	}

	public boolean isAtWarWith(Integer id)
	{
		L2Clan clan = ClanTable.getInstance().getClan(id);
		if(_atWarWith != null && !_atWarWith.isEmpty())
			if(_atWarWith.contains(clan))
				return true;
		return false;
	}

	public boolean isUnderAttackFrom(Integer id)
	{
		L2Clan clan = ClanTable.getInstance().getClan(id);
		if(_underAttackFrom != null && !_underAttackFrom.isEmpty())
			if(_underAttackFrom.contains(clan))
				return true;
		return false;
	}

	public void setEnemyClan(L2Clan clan)
	{
		_atWarWith.add(clan);
	}

	public void deleteEnemyClan(L2Clan clan)
	{
		_atWarWith.remove(clan);
	}

	// clans that are attacking this clan
	public void setAttackerClan(L2Clan clan)
	{
		_underAttackFrom.add(clan);
	}

	public void deleteAttackerClan(L2Clan clan)
	{
		_underAttackFrom.remove(clan);
	}

	public GArray<L2Clan> getEnemyClans()
	{
		return _atWarWith;
	}

	public int getWarsCount()
	{
		return _atWarWith.size();
	}

	public GArray<L2Clan> getAttackerClans()
	{
		return _underAttackFrom;
	}

	public void broadcastClanStatus(boolean updateList, boolean needUserInfo, boolean relation)
	{
		for(L2ClanMember member : _members.values())
			if(member.isOnline())
			{
				if(updateList)
					member.getPlayer().sendPacket(Msg.PledgeShowMemberListDeleteAll, new PledgeShowMemberListAll(this, member.getPlayer()));
				member.getPlayer().sendPacket(new PledgeShowInfoUpdate(this));
				if(needUserInfo)
					member.getPlayer().broadcastUserInfo(true);
				if(relation)
					member.getPlayer().broadcastRelationChanged();
			}
	}

	public L2Alliance getAlliance()
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
		L2Clan[] clans = ClanTable.getInstance().getClans();
		Arrays.sort(clans, REPUTATION_COMPARATOR);

		int place = 1;
		for(int i = 0; i < clans.length; i++)
		{
			if(i == REPUTATION_PLACES)
				return 0;

			L2Clan clan = clans[i];
			if(clan == this)
				return place + i;
		}

		return 0;
	}

	public int getReputationScore()
	{
		return _reputation;
	}

	private void setReputationScore(int rep)
	{
		if(_reputation >= 0 && rep < 0)
		{
			broadcastToOnlineMembers(Msg.SINCE_THE_CLAN_REPUTATION_SCORE_HAS_DROPPED_TO_0_OR_LOWER_YOUR_CLAN_SKILLS_WILL_BE_DE_ACTIVATED);
			L2Skill[] skills = getAllSkills();
			for(L2ClanMember member : _members.values())
				if(member.isOnline())
					for(L2Skill sk : skills)
						member.getPlayer().removeSkill(sk, false);
		}
		else if(_reputation < 0 && rep >= 0)
		{
			broadcastToOnlineMembers(Msg.THE_CLAN_SKILL_WILL_BE_ACTIVATED_BECAUSE_THE_CLANS_REPUTATION_SCORE_HAS_REACHED_TO_0_OR_HIGHER);
			L2Skill[] skills = getAllSkills();
			for(L2ClanMember member : _members.values())
				if(member.isOnline())
					for(L2Skill sk : skills)
					{
						member.getPlayer().sendPacket(new PledgeSkillListAdd(sk.getId(), sk.getLevel()));
						if(sk.getMinPledgeClass() <= member.getPlayer().getPledgeClass())
							member.getPlayer().addSkill(sk, false);
					}
		}

		if(_reputation != rep)
		{
			_reputation = rep;
			broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
		}

		updateClanInDB();
	}

	public int incReputation(int inc, boolean rate, String source)
	{
		if(_level < 5)
			return 0;

		if(rate && Math.abs(inc) <= Config.RATE_CLAN_REP_SCORE_MAX_AFFECTED)
			inc = Math.round(inc * Config.RATE_CLAN_REP_SCORE);

		setReputationScore(_reputation + inc);
		Log.add(_name + "|" + inc + "|" + _reputation + "|" + source, "clan_reputation");

		return inc;
	}

	/* ============================ clan skills stuff ============================ */
	private void restoreSkills()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id, skill_level, squad_index FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");
				int pId = rset.getInt("squad_index");
				L2Skill skill = SkillTable.getInstance().getInfo(id, level);
				if(pId > -1)
				{
					FastMap<Integer, L2Skill> oldPSkill = _squadSkills.get(pId);
					if(oldPSkill == null)
						oldPSkill = new FastMap<Integer, L2Skill>();
					oldPSkill.put(id, skill);
					_squadSkills.put(pId, oldPSkill);
				}
				else
				_skills.put(skill.getId(), skill);
			}
		}
		catch(Exception e)
		{
			_log.warning("Could not restore clan skills: " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/** used to retrieve all skills */
	public final L2Skill[] getAllSkills()
	{
		if(_reputation < 0)
			return new L2Skill[0];

		return _skills.values().toArray(new L2Skill[_skills.values().size()]);
	}

	public L2Skill addNewSkill(L2Skill newSkill, boolean store)
	{
		return addNewSkill(newSkill, store, -1);
	}

	/** used to add a new skill to the list, send a packet to all online clan members, update their stats and store it in db*/
	public L2Skill addNewSkill(L2Skill newSkill, boolean store, int plId)
	{
		L2Skill oldSkill = null;
		if(newSkill != null)
		{
			if(plId == -1)
			oldSkill = _skills.put(newSkill.getId(), newSkill);
			else if(plId >= 0)
			{
				FastMap<Integer, L2Skill> oldPSkill = _squadSkills.get(plId);
				if(oldPSkill == null)
					oldPSkill = new FastMap<Integer, L2Skill>().setShared(true);
				oldSkill = oldPSkill.put(newSkill.getId(), newSkill);
				_squadSkills.put(plId, oldPSkill);
			}
			else
			{
				_log.warning("Player " + getLeaderName() + " tried to add a Squad Skill to a squad that doesn't exist, ban him!");
				return null;
			}
			if(store)
			{
				ThreadConnection con = null;
				FiltredPreparedStatement statement = null;

				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();

					if(oldSkill != null)
					{
						statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?");
						statement.setInt(1, newSkill.getLevel());
						statement.setInt(2, oldSkill.getId());
						statement.setInt(3, getClanId());
						statement.execute();
					}
					else
					{
						statement = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name,squad_index) VALUES (?,?,?,?,?)");
						statement.setInt(1, getClanId());
						statement.setInt(2, newSkill.getId());
						statement.setInt(3, newSkill.getLevel());
						statement.setString(4, newSkill.getName());
						statement.setInt(5, plId);
						statement.execute();
					}
				}
				catch(Exception e)
				{
					_log.warning("Error could not store char skills: " + e);
					e.printStackTrace();
				}
				finally
				{
					DatabaseUtils.closeDatabaseCS(con, statement);
				}
			}

			for(L2ClanMember temp : _members.values())
			{
				if(temp.isOnline() && temp.getPlayer() != null)
				{
					if(plId == -1)
					{
						temp.getPlayer().sendPacket(new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel()));
						if(newSkill.getMinRank() <= temp.getPlayer().getPledgeClass())
							temp.getPlayer().addSkill(newSkill, false);
					}
					else
					{
						temp.getPlayer().sendPacket(new ExSubPledgetSkillAdd(newSkill.getId(), newSkill.getLevel(), plId));
						if(temp.getPledgeType() == plId)
							temp.getPlayer().addSkill(newSkill, false);
					}
				}
			}
		}
		return oldSkill;
	}

	/**
	 * Удаляет скилл у клана, без удаления из базы. Используется для удаления скилов резиденций.
	 * После удаления скила(ов) необходимо разослать boarcastSkillListToOnlineMembers()
	 * @param skill
	 */
	public void removeSkill(L2Skill skill)
	{
		_skills.remove(skill.getId());
		for(L2ClanMember temp : _members.values())
			if(temp.isOnline() && temp.getPlayer() != null)
				temp.getPlayer().removeSkill(skill);
	}

	public void boarcastSkillListToOnlineMembers()
	{
		for(L2ClanMember temp : _members.values())
			if(temp.isOnline() && temp.getPlayer() != null)
				addAndShowSkillsToPlayer(temp.getPlayer());
	}

	public void addAndShowSkillsToPlayer(L2Player activeChar)
	{
		if(_reputation < 0)
			return;

		activeChar.sendPacket(new PledgeSkillList(this));

		for(L2Skill s : _skills.values())
		{
			if(s == null)
				continue;
			activeChar.sendPacket(new PledgeSkillListAdd(s.getId(), s.getLevel()));
			if(s.getMinRank() <= activeChar.getPledgeClass())
				activeChar.addSkill(s, false);
		}
		if(_squadSkills != null && !_squadSkills.isEmpty())
		{
			for(int pledgeId : _squadSkills.keySet())
			{
				FastMap<Integer, L2Skill> skills = _squadSkills.get(pledgeId);
				for(L2Skill s : skills.values())
				{
					activeChar.sendPacket(new ExSubPledgetSkillAdd(s.getId(), s.getLevel(), pledgeId));
					if(pledgeId == activeChar.getPledgeType())
						activeChar.addSkill(s, false);
				}
			}
		}
		activeChar.sendPacket(new SkillList(activeChar));
	}

	public void showSquadSkillsToPlayer(L2Player player)
	{
		if(_squadSkills != null && !_squadSkills.isEmpty())
		{
			for(int pledgeId : _squadSkills.keySet())
			{
				FastMap<Integer, L2Skill> skills = _squadSkills.get(pledgeId);
				for(L2Skill s : skills.values())
				{
					player.sendPacket(new ExSubPledgetSkillAdd(s.getId(), s.getLevel(), pledgeId));
					if(pledgeId == player.getPledgeType())
						player.addSkill(s, false);
				}
			}
		}
	}

	/* ============================ clan subpledges stuff ============================ */

	public class SubPledge
	{
		private int _type;
		private int _leaderId;
		private String _name;

		public SubPledge(int type, int leaderId, String name)
		{
			_type = type;
			_leaderId = leaderId;
			_name = name;
		}

		public int getType()
		{
			return _type;
		}

		public String getName()
		{
			return _name;
		}

		public int getLeaderId()
		{
			return _leaderId;
		}

		public void setLeaderId(int leaderId)
		{
			_leaderId = leaderId;
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE clan_subpledges SET leader_id=? WHERE clan_id=? and type=?");
				statement.setInt(1, _leaderId);
				statement.setInt(2, getClanId());
				statement.setInt(3, _type);
				statement.execute();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}

		public String getLeaderName()
		{
			for(L2ClanMember member : _members.values())
				if(member.getObjectId() == _leaderId)
					return member.getName();
			return "";
		}
	}

	public final boolean isAcademy(int pledgeType)
	{
		return pledgeType == SUBUNIT_ACADEMY;
	}

	public final boolean isRoyalGuard(int pledgeType)
	{
		return pledgeType == SUBUNIT_ROYAL1 || pledgeType == SUBUNIT_ROYAL2;
	}

	public final boolean isOrderOfKnights(int pledgeType)
	{
		return pledgeType == SUBUNIT_KNIGHT1 || pledgeType == SUBUNIT_KNIGHT2 || pledgeType == SUBUNIT_KNIGHT3 || pledgeType == SUBUNIT_KNIGHT4;
	}

	public int getAffiliationRank(int pledgeType)
	{
		if(isAcademy(pledgeType))
			return 9;
		else if(isOrderOfKnights(pledgeType))
			return 8;
		else if(isRoyalGuard(pledgeType))
			return 7;
		else
			return 6;
	}

	public final SubPledge getSubPledge(int pledgeType)
	{
		if(_SubPledges == null)
			return null;

		return _SubPledges.get(pledgeType);
	}

	public final void addSubPledge(SubPledge sp, boolean updateDb)
	{
		_SubPledges.put(sp.getType(), sp);

		if(updateDb)
		{
			broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(sp));
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("INSERT INTO `clan_subpledges` (clan_id,type,leader_id,name) VALUES (?,?,?,?)");
				statement.setInt(1, getClanId());
				statement.setInt(2, sp.getType());
				statement.setInt(3, sp.getLeaderId());
				statement.setString(4, sp.getName());
				statement.execute();
			}
			catch(Exception e)
			{
				_log.warning("Could not store clan Sub pledges: " + e);
				e.printStackTrace();
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}

	public int createSubPledge(L2Player player, int pledgeType, int leaderId, String name)
	{
		int temp = pledgeType;
		pledgeType = getAvailablePledgeTypes(pledgeType);

		if(pledgeType == SUBUNIT_NONE)
		{
			if(temp == SUBUNIT_ACADEMY)
				player.sendPacket(Msg.YOUR_CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY);
			else
				player.sendMessage("You can't create any more sub-units of this type");
			return SUBUNIT_NONE;
		}

		switch(pledgeType)
		{
			case SUBUNIT_ACADEMY:
				break;
			case SUBUNIT_ROYAL1:
			case SUBUNIT_ROYAL2:
				if(getReputationScore() < 5000)
				{
					player.sendPacket(Msg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
					return SUBUNIT_NONE;
				}
				incReputation(-5000, false, "SubunitCreate");
				break;
			case SUBUNIT_KNIGHT1:
			case SUBUNIT_KNIGHT2:
			case SUBUNIT_KNIGHT3:
			case SUBUNIT_KNIGHT4:
				if(getReputationScore() < 10000)
				{
					player.sendPacket(Msg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
					return SUBUNIT_NONE;
				}
				incReputation(-10000, false, "SubunitCreate");
				break;
		}

		addSubPledge(new SubPledge(pledgeType, leaderId, name), true);
		return pledgeType;
	}

	public int getAvailablePledgeTypes(int pledgeType)
	{
		if(pledgeType == SUBUNIT_NONE)
			return SUBUNIT_NONE;

		if(_SubPledges.get(pledgeType) != null)
			switch(pledgeType)
			{
				case SUBUNIT_ACADEMY:
					return 0;
				case SUBUNIT_ROYAL1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_ROYAL2);
					break;
				case SUBUNIT_ROYAL2:
					return 0;
				case SUBUNIT_KNIGHT1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT2);
					break;
				case SUBUNIT_KNIGHT2:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT3);
					break;
				case SUBUNIT_KNIGHT3:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT4);
					break;
				case SUBUNIT_KNIGHT4:
					return 0;
			}
		return pledgeType;
	}

	private void restoreSubPledges()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while(rset.next())
			{
				int type = rset.getInt("type");
				int leaderId = rset.getInt("leader_id");
				String name = rset.getString("name");
				SubPledge pledge = new SubPledge(type, leaderId, name);
				addSubPledge(pledge, false);
			}
		}
		catch(Exception e)
		{
			_log.warning("Could not restore clan SubPledges: " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/** used to retrieve all subPledges */
	public final SubPledge[] getAllSubPledges()
	{
		return _SubPledges.values().toArray(new SubPledge[_SubPledges.values().size()]);
	}

	public int getSubPledgeLimit(int pledgeType)
	{
		int limit;
		switch(_level)
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
				break;
		}
		switch(pledgeType)
		{
			case SUBUNIT_ACADEMY:
			case SUBUNIT_ROYAL1:
			case SUBUNIT_ROYAL2:
				if(getLevel() >= 11)
					limit = 30;
				else
					limit = 20;
				break;
			case SUBUNIT_KNIGHT1:
			case SUBUNIT_KNIGHT2:
				if(getLevel() >= 9)
					limit = 25;
				else
					limit = 10;
				break;
			case SUBUNIT_KNIGHT3:
			case SUBUNIT_KNIGHT4:
				if(getLevel() >= 10)
					limit = 25;
				else
					limit = 10;
				break;
		}
		return limit;
	}

	public int getSubPledgeMembersCount(int pledgeType)
	{
		int result = 0;
		for(L2ClanMember temp : _members.values())
			if(temp.getPledgeType() == pledgeType)
				result++;
		return result;
	}

	public int getSubPledgeLeaderId(int pledgeType)
	{
		return _SubPledges.get(pledgeType).getLeaderId();
	}

	/* ============================ clan privilege ranks stuff ============================ */

	public class RankPrivs
	{
		private int _rank;
		private int _party;
		private int _privs;

		public RankPrivs(int rank, int party, int privs)
		{
			_rank = rank;
			_party = party;
			_privs = privs;
		}

		public int getRank()
		{
			return _rank;
		}

		public int getParty()
		{
			return _party;
		}

		public void setParty(int party)
		{
			_party = party;
		}

		public int getPrivs()
		{
			return _privs;
		}

		public void setPrivs(int privs)
		{
			_privs = privs;
		}
	}

	private void restoreRankPrivs()
	{
		if(_Privs == null)
			InitializePrivs();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			// Retrieve all skills of this L2Player from the database
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT privilleges,rank FROM clan_privs WHERE clan_id=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while(rset.next())
			{
				int rank = rset.getInt("rank");
				//int party = rset.getInt("party"); - unused?
				int privileges = rset.getInt("privilleges");
				//noinspection ConstantConditions
				RankPrivs p = _Privs.get(rank);
				if(p != null)
					p.setPrivs(privileges);
				else
					_log.warning("Invalid rank value (" + rank + "), please check clan_privs table");
			}
		}
		catch(Exception e)
		{
			_log.warning("Could not restore clan privs by rank: " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void InitializePrivs()
	{
		for(int i = RANK_FIRST; i <= RANK_LAST; i++)
			_Privs.put(i, new RankPrivs(i, 0, CP_NOTHING));
	}

	public void updatePrivsForRank(int rank)
	{
		for(L2ClanMember member : _members.values())
			if(member.isOnline() && member.getPlayer() != null && member.getPlayer().getPowerGrade() == rank)
			{
				if(member.getPlayer().isClanLeader())
					continue;
				member.getPlayer().sendUserInfo(false);
			}
	}

	public RankPrivs getRankPrivs(int rank)
	{
		if(rank < RANK_FIRST || rank > RANK_LAST)
		{
			_log.warning("Requested invalid rank value: " + rank);
			Thread.dumpStack();
			return null;
		}
		if(_Privs.get(rank) == null)
		{
			_log.warning("Request of rank before init: " + rank);
			Thread.dumpStack();
			setRankPrivs(rank, CP_NOTHING);
		}
		return _Privs.get(rank);
	}

	public int countMembersByRank(int rank)
	{
		int ret = 0;
		for(L2ClanMember m : getMembers())
			if(m.getPowerGrade() == rank)
				ret++;
		return ret;
	}

	public void setRankPrivs(int rank, int privs)
	{
		if(rank < RANK_FIRST || rank > RANK_LAST)
		{
			_log.warning("Requested set of invalid rank value: " + rank);
			Thread.dumpStack();
			return;
		}

		if(_Privs.get(rank) != null)
			_Privs.get(rank).setPrivs(privs);
		else
			_Privs.put(rank, new RankPrivs(rank, countMembersByRank(rank), privs));

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			//_log.warning("requested store clan privs in db for rank: " + rank + ", privs: " + privs);
			// Retrieve all skills of this L2Player from the database
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO clan_privs (clan_id,rank,privilleges) VALUES (?,?,?)");
			statement.setInt(1, getClanId());
			statement.setInt(2, rank);
			statement.setInt(3, privs);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warning("Could not store clan privs for rank: " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/** used to retrieve all privilege ranks */
	public final RankPrivs[] getAllRankPrivs()
	{
		if(_Privs == null)
			return new RankPrivs[0];
		return _Privs.values().toArray(new RankPrivs[_Privs.values().size()]);
	}

	private int _auctionBiddedAt = 0;

	public int getAuctionBiddedAt()
	{
		return _auctionBiddedAt;
	}

	public void setAuctionBiddedAt(int id)
	{
		_auctionBiddedAt = id;
	}

	public void sendMessageToAll(String message)
	{
		for(L2ClanMember member : _members.values())
			if(member.isOnline() && member.getPlayer() != null)
				member.getPlayer().sendMessage(message);
	}

	public void sendMessageToAll(String message, String message_ru)
	{
		L2Player player;
		for(L2ClanMember member : _members.values())
			if(member.isOnline() && (player = member.getPlayer()) != null)
				player.sendMessage(player.isLangRus() && !message_ru.isEmpty() ? message_ru : message);
	}

	private Siege _siege;
	private boolean _isDefender;
	private boolean _isAttacker;

	public void setSiege(Siege siege)
	{
		_siege = siege;
	}

	public Siege getSiege()
	{
		return _siege;
	}

	public void setDefender(boolean b)
	{
		_isDefender = b;
	}

	public void setAttacker(boolean b)
	{
		_isAttacker = b;
	}

	public boolean isDefender()
	{
		return _isDefender;
	}

	public boolean isAttacker()
	{
		return _isAttacker;
	}

	private static class ClanReputationComparator implements Comparator<L2Clan>
	{
		public int compare(L2Clan o1, L2Clan o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			return o2.getReputationScore() - o1.getReputationScore();
		}
	}

	public void insertNotice()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO clan_notices (clanID, notice, enabled) values (?,?,?)");
			statement.setInt(1, getClanId());
			statement.setString(2, "Change me");
			statement.setString(3, "false");
			statement.execute();
		}
		catch(Exception e)
		{
			System.out.println("BBS: Error while creating clan notice for clan " + getClanId() + "");
			if(e.getMessage() != null)
				System.out.println("BBS: Exception = " + e.getMessage() + "");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public String getNotice()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT notice FROM clan_notices WHERE clanID=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();
			while(rset.next())
				_notice = rset.getString("notice");
		}
		catch(Exception e)
		{
			System.out.println("BBS: Error while getting notice from DB for clan " + getClanId() + "");
			if(e.getMessage() != null)
				System.out.println("BBS: Exception = " + e.getMessage() + "");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return _notice;
	}

	public String getNoticeForBBS()
	{
		String notice = "";
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT notice FROM clan_notices WHERE clanID=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();
			while(rset.next())
				notice = rset.getString("notice");
		}
		catch(Exception e)
		{
			System.out.println("BBS: Error while getting notice from DB for clan " + getClanId() + "");
			if(e.getMessage() != null)
				System.out.println("BBS: Exception = " + e.getMessage() + "");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return notice.replaceAll("<br>", "\n");
	}

	/**
	 * Назначить новое сообщение
	 */
	public void setNotice(String notice)
	{
		notice = notice.replaceAll("\n", "<br>");

		if(notice.length() > NOTICE_MAX_LENGHT)
			notice = notice.substring(0, NOTICE_MAX_LENGHT - 1);
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_notices SET notice=? WHERE clanID=?");
			statement.setString(1, notice);
			statement.setInt(2, getClanId());
			statement.execute();
			_notice = notice;
		}
		catch(Exception e)
		{
			System.out.println("BBS: Error while saving notice for clan " + getClanId() + "");
			if(e.getMessage() != null)
				System.out.println("BBS: Exception = " + e.getMessage() + "");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Включено или нет?
	 */
	public boolean isNoticeEnabled()
	{
		String result = "";
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT enabled FROM clan_notices WHERE clanID=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();

			while(rset.next())
				result = rset.getString("enabled");
		}
		catch(Exception e)
		{
			System.out.println("BBS: Error while reading _noticeEnabled for clan " + getClanId() + "");
			if(e.getMessage() != null)
				System.out.println("BBS: Exception = " + e.getMessage() + "");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		if(result.isEmpty())
			insertNotice();
		else if(result.compareToIgnoreCase("true") == 0)
			return true;
		return false;
	}

	/**
	 * Включить/выключить
	 */
	public void setNoticeEnabled(boolean noticeEnabled)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_notices SET enabled=? WHERE clanID=?");
			if(noticeEnabled)
				statement.setString(1, "true");
			else
				statement.setString(1, "false");
			statement.setInt(2, getClanId());
			statement.execute();
		}
		catch(Exception e)
		{
			System.out.println("BBS: Error while updating notice status for clan " + getClanId() + "");
			if(e.getMessage() != null)
				System.out.println("BBS: Exception = " + e.getMessage() + "");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		_noticeEnabled = noticeEnabled;
	}

	public int getWhBonus()
	{
		return _whBonus;
	}

	public void setWhBonus(int i)
	{
		if(_whBonus != -1)
			mysql.set("UPDATE `clan_data` SET `warehouse`=? WHERE `clan_id`=?", i, getClanId());
		_whBonus = i;
	}

	private int _territorySide = -1;

	public void setTerritorySiege(int side)
	{
		_territorySide = side;
	}

	public int getTerritorySiege()
	{
		return _territorySide;
	}

	public void setAirshipLicense(boolean val)
	{
		_airshipLicense = val;
	}

	public boolean isHaveAirshipLicense()
	{
		return _airshipLicense;
	}

	public L2AirShip getAirship()
	{
		return _airship;
	}

	public void setAirship(L2AirShip airship)
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
	
	public final FastMap<Integer, FastMap<Integer, L2Skill>> getSquadSkills()
	{
		if(_squadSkills == null)
			return new FastMap<Integer, FastMap<Integer, L2Skill>>();
		return _squadSkills;
	}

	public FastMap<Integer, SubPledge> getSubPledges()
	{
		return _SubPledges;
	}
}