package l2rt.gameserver.model;

import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.gameserver.model.L2Clan.RankPrivs;
import l2rt.gameserver.model.L2Clan.SubPledge;

import java.lang.ref.WeakReference;

public class L2ClanMember
{
	private WeakReference<L2Clan> _clan;
	private String _name;
	private String _title;
	private int _level;
	private int _classId;
	private int _sex;
	private long playerStoreId;
	private int _pledgeType;
	private int _powerGrade;
	private int _apprentice;
	private Boolean _clanLeader;

	public L2ClanMember(L2Clan clan, String name, String title, int level, int classId, int objectId, int pledgeType, int powerGrade, int apprentice, Boolean clanLeader)
	{
		_clan = new WeakReference<L2Clan>(clan);
		_name = name;
		_title = title;
		_level = level;
		_classId = classId;
		_pledgeType = pledgeType;
		_powerGrade = powerGrade;
		_apprentice = apprentice;
		_clanLeader = clanLeader;
		playerStoreId = L2ObjectsStorage.objIdNoStore(objectId);
		if(powerGrade != 0)
		{
			RankPrivs r = clan.getRankPrivs(powerGrade);
			r.setParty(clan.countMembersByRank(powerGrade));
		}
	}

	public L2ClanMember(L2Player player)
	{
		playerStoreId = player.getStoredId();
	}

	public void setPlayerInstance(L2Player player)
	{
		if(player == null)
		{
			playerStoreId = L2ObjectsStorage.objIdNoStore(getObjectId());
			return;
		}
		// this is here to keep the data when the player logs off
		playerStoreId = player.getStoredId();
		_clan = new WeakReference<L2Clan>(player.getClan());
		_name = player.getName();
		_title = player.getTitle();
		_level = player.getLevel();
		_classId = player.getClassId().getId();
		_pledgeType = player.getPledgeType();
		_powerGrade = player.getPowerGrade();
		_apprentice = player.getApprentice();
		_clanLeader = player.isClanLeader();
	}

	public L2Player getPlayer()
	{
		return L2ObjectsStorage.getAsPlayer(playerStoreId);
	}

	public boolean isOnline()
	{
		L2Player player = getPlayer();
		return player != null && !player.isInOfflineMode();
	}

	public L2Clan getClan()
	{
		L2Player player = getPlayer();
		return player == null ? _clan.get() : player.getClan();
	}

	public int getClassId()
	{
		L2Player player = getPlayer();
		return player == null ? _classId : player.getClassId().getId();
	}

	public int getSex()
	{
		L2Player player = getPlayer();
		return player == null ? _sex : player.getSex();
	}

	public int getLevel()
	{
		L2Player player = getPlayer();
		return player == null ? _level : player.getLevel();
	}

	public String getName()
	{
		L2Player player = getPlayer();
		return player == null ? _name : player.getName();
	}

	public int getObjectId()
	{
		return L2ObjectsStorage.getStoredObjectId(playerStoreId);
	}

	public String getTitle()
	{
		L2Player player = getPlayer();
		return player == null ? _title : player.getTitle();
	}

	public void setTitle(String title)
	{
		L2Player player = getPlayer();
		_title = title;
		if(player != null)
		{
			player.setTitle(title);
			player.sendChanges();
		}
		else
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE characters SET title=? WHERE obj_Id=?");
				statement.setString(1, title);
				statement.setInt(2, getObjectId());
				statement.execute();
			}
			catch(Exception e)
			{}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}

	public int getPledgeType()
	{
		L2Player player = getPlayer();
		return player == null ? _pledgeType : player.getPledgeType();
	}

	public void setPledgeType(int pledgeType)
	{
		L2Player player = getPlayer();
		_pledgeType = pledgeType;
		if(player != null)
			player.setPledgeType(pledgeType);
		else
			updatePledgeType();
	}

	private void updatePledgeType()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET pledge_type=? WHERE obj_Id=?");
			statement.setInt(1, _pledgeType);
			statement.setInt(2, getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public int getPowerGrade()
	{
		L2Player player = getPlayer();
		return player == null ? _powerGrade : player.getPowerGrade();
	}

	public void setPowerGrade(int newPowerGrade)
	{
		L2Player player = getPlayer();
		int oldPowerGrade = getPowerGrade();
		_powerGrade = newPowerGrade;
		if(player != null)
			player.setPowerGrade(newPowerGrade);
		else
			updatePowerGrade();
		updatePowerGradeParty(oldPowerGrade, newPowerGrade);
	}

	private void updatePowerGradeParty(int oldGrade, int newGrade)
	{
		if(oldGrade != 0)
		{
			RankPrivs r1 = getClan().getRankPrivs(oldGrade);
			r1.setParty(getClan().countMembersByRank(oldGrade));
		}
		if(newGrade != 0)
		{
			RankPrivs r2 = getClan().getRankPrivs(newGrade);
			r2.setParty(getClan().countMembersByRank(newGrade));
		}
	}

	private void updatePowerGrade()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET pledge_rank=? WHERE obj_Id=?");
			statement.setInt(1, _powerGrade);
			statement.setInt(2, getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private int getApprentice()
	{
		L2Player player = getPlayer();
		return player == null ? _apprentice : player.getApprentice();
	}

	public void setApprentice(int apprentice)
	{
		L2Player player = getPlayer();
		_apprentice = apprentice;
		if(player != null)
			player.setApprentice(apprentice);
		else
			updateApprentice();
	}

	private void updateApprentice()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET apprentice=? WHERE obj_Id=?");
			statement.setInt(1, _apprentice);
			statement.setInt(2, getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public String getApprenticeName()
	{
		if(getApprentice() != 0)
			if(getClan().getClanMember(getApprentice()) != null)
				return getClan().getClanMember(getApprentice()).getName();
		return "";
	}

	public boolean hasApprentice()
	{
		return getApprentice() != 0;
	}

	public int getSponsor()
	{
		if(getPledgeType() != L2Clan.SUBUNIT_ACADEMY)
			return 0;
		int id = getObjectId();
		for(L2ClanMember element : getClan().getMembers())
			if(element.getApprentice() == id)
				return element.getObjectId();
		return 0;
	}

	private String getSponsorName()
	{
		int sponsorId = getSponsor();
		if(sponsorId == 0)
			return "";
		else if(getClan().getClanMember(sponsorId) != null)
			return getClan().getClanMember(sponsorId).getName();
		return "";
	}

	public boolean hasSponsor()
	{
		return getSponsor() != 0;
	}

	public String getRelatedName()
	{
		if(getPledgeType() == L2Clan.SUBUNIT_ACADEMY)
			return getSponsorName();
		return getApprenticeName();
	}

	public boolean isClanLeader()
	{
		L2Player player = getPlayer();
		return player == null ? _clanLeader : player.isClanLeader();
	}

	public int isSubLeader()
	{
		for(SubPledge pledge : getClan().getAllSubPledges())
			if(pledge.getLeaderId() == getObjectId())
				return pledge.getType();
		return 0;
	}
}