package l2r.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.dao.AccountReportDAO;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.AutoHuntingPunish;


public class AutoHuntingManager
{
	private static final Logger _log = LoggerFactory.getLogger(AutoHuntingManager.class);
	private static AutoHuntingManager _instance;
	
	private static FastMap<Integer, String[]> _unread;
	// Number of reportes made over each player
	private static FastMap<Integer, FastList<Player>> _reportedCount = new FastMap<Integer, FastList<Player>>();
	// Reporters blocked by time
	private static FastMap<Integer, Long> _lockedReporters = new FastMap<Integer, Long>();
	// Blocked ips
	private static Set<String> _lockedIps = new HashSet<String>();
	// Blocked accounts
	private static Set<String> _lockedAccounts = new HashSet<String>();
	
	private AutoHuntingManager()
	{
		loadUnread();
	}
	
	public static AutoHuntingManager getInstance()
	{
		if(_instance == null)
			_instance = new AutoHuntingManager();

		return _instance;
	}
	
	/**
	 * Check if the reported player is online
	 * @param reportedId
	 * @return true if L2World contains that player, else returns false
	 */
	private static boolean reportedIsOnline(Player player)
	{
		return World.getPlayer(player.getObjectId()) != null;
	}
	
	/**
	 * Will save the report in database
	 * @param reported (the L2PcInstance who was reported)
	 * @param reporter (the L2PcInstance who reported the bot) 
	 */
	public synchronized void reportBot(Player reported, Player reporter)
	{
		if (!reportedIsOnline(reported))
		{
			reporter.sendMessage("The player you are reporting is offline.");
			return;
		}
		
		_lockedReporters.put(reporter.getObjectId(), System.currentTimeMillis());
		_lockedIps.add(reporter.getIP());
		_lockedAccounts.add(reporter.getAccountName());
		
		long date = Calendar.getInstance().getTimeInMillis();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		
		try
		{
			if (!_reportedCount.containsKey(reported))
			{
				FastList<Player> p = new FastList<Player>();
				p.add(reported);
				_reportedCount.put(reporter.getObjectId(), p);
			}
			else
			{
				if(_reportedCount.get(reporter).contains(reported.getObjectId()))
				{
					reporter.sendMessage("You cannot report a player more than 1 time");
					return;
				}
				_reportedCount.get(reporter).add(reported);
			}
			
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `bot_report`(`reported_name`, `reported_objectId`, `reporter_name`, `reporter_objectId`, `date`) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, reported.getName());
			statement.setInt(2, reported.getObjectId());
			statement.setString(3, reporter.getName());
			statement.setInt(4, reporter.getObjectId());
			statement.setLong(5, date);
			statement.executeUpdate();
			
			rset = statement.getGeneratedKeys();
			rset.next();
			int maxId = rset.getInt(1);
			
			_unread.put(maxId, new String[]{reported.getName(), reporter.getName(), String.valueOf(date)});
		}
		catch (Exception e)
		{
			_log.warn("Could not save reported bot " + reported.getName() + " by " + reporter.getName() + " at " + date + ".");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		SystemMessage2 sm = new SystemMessage2(SystemMsg.C1_REPORTED_AS_BOT);
		sm.addName(reported);
		reporter.sendPacket(sm);
	}
	
	/**
	 * Will load the data from all unreaded reports (used to load reports
	 * in a window for admins/GMs)
	 * @return a FastMap<Integer, String[]> (Integer - report id, String[] - reported name, report name, date)
	 */
	private void loadUnread()
	{
		_unread = new FastMap<Integer, String[]>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `report_id`, `reported_name`, `reporter_name`, `date` FROM `bot_report` WHERE `read` = ?");
			statement.setString(1, "false");
			
			rset = statement.executeQuery();
			while (rset.next())
			{
				//Not loading objectIds to increase performance
				//L2World.getInstance().getPlayer(name).getObjectId();
				String[] data = new String[3];
				data[0] = rset.getString("reported_name");
				data[1] = rset.getString("reporter_name");
				data[2] = rset.getString("date");
				
				_unread.put(rset.getInt("report_id"), data);
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not load data from bot_report:\n" + e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	/**
	 * Return a FastMap holding all the reports data
	 * to be viewed by any GM
	 * @return _unread
	 */
	public FastMap<Integer, String[]> getUnread()
	{
		return _unread;
	}
	
	/**
	 * Marks a reported bot as readed (from admin menu)
	 * @param id (the report id)
	 */
	public void markAsRead(int id)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE `bot_report` SET `read` = ? WHERE `report_id` = ?");
			statement.setString(1, "true");
			statement.setInt(2, id);
			statement.execute();
			
			_unread.remove(id);
			_log.info("Reported bot marked as read, id was: " + id);
		}
		catch (Exception e)
		{
			_log.warn("Could not mark as read the reported bot: " + id + ":\n" + e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	/**
	 * Returns the number of times the player has been reported
	 * @param reported
	 * @return int
	 */
	public int getPlayerReportsCount(Player reported)
	{
		int count = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT COUNT(*) FROM `bot_report` WHERE `reported_objectId` = ?");
			statement.setInt(1, reported.getObjectId());
			
			rset = statement.executeQuery();
			if (rset.next())
				count = rset.getInt(1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return count;
	}
	
	/**
	 * Will save the punish being suffered to player in database
	 * (at player logs out), to be restored next time players enter
	 * in server
	 * @param punished 
	 */
	public void savePlayerPunish(Player punished)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE `bot_reported_punish` SET `time_left` = ? WHERE `charId` = ?");
			statement.setLong(1, punished.getPlayerPunish().getPunishTimeLeft());
			statement.setInt(2, punished.getObjectId());
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	/**
	 * Retail report restrictions (Validates the player - reporter relationship)
	 * @param reported (the reported bot)
	 * @return 
	 */
	public boolean validateBot(Player reported, Player reporter)
	{
		if (reported == null || reporter == null)
			return false;
		
		// Cannot report while reported is inside peace zone, war zone or olympiad
		if (reported.isInPeaceZone() || reported.isInCombatZone() || reported.isInOlympiadMode())
		{
			reporter.sendPacket(new SystemMessage2(SystemMsg.THIS_CHARACTER_CANNOT_MAKE_A_REPORT));
			return false;
		}
		// Cannot report if reported and reporter are in war
		if (reported.getClan() != null && reporter.getClan() != null)
		{
			if (reported.getClan().isAtWarWith(reporter.getClanId()))
			{
				reporter.sendPacket(new SystemMessage2(SystemMsg.CANNOT_REPORT_TARGET_IN_CLAN_WAR));
				return false;
			}
		}
		// Cannot report if the reported didnt earn exp since he logged in
		if (!reported.hasEarnedExp())
		{
			reporter.sendPacket(new SystemMessage2(SystemMsg.CANNOT_REPORT_CHARACTER_WITHOUT_GAINEXP));
			return false;
		}
		// Cannot report twice or more a player
		if (_reportedCount.containsKey(reporter))
		{
			for (Player p : _reportedCount.get(reporter))
			{
				if (reported == p)
				{
					reporter.sendPacket(new SystemMessage2(SystemMsg.C1_REPORTED_AS_BOT));
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Retail report restrictions (Validates the reporter state)
	 * @param reporter
	 * @return
	 */
	public synchronized boolean validateReport(Player reporter)
	{
		if (reporter == null)
			return false;
		
		if(reporter._account == null)
			reporter._account = new AccountReportDAO(reporter.getAccountName());
		
		// The player has a 30 mins lock before be able to report anyone again
		if(reporter._account.getReportsPoints() == 0)
		{
			SystemMessage2 sm = new SystemMessage2(SystemMsg.YOU_CAN_REPORT_IN_S1_MINUTES_S2_REPORT_POINTS_REMAIN_IN_ACCOUNT);
			sm.addNumber(0);
			sm.addNumber(0);
			reporter.sendPacket(sm);
			return false;
		}
			
		// 30 mins must pass before report again 
		else if (_lockedReporters.containsKey(reporter.getObjectId()))
		{
			long delay = (System.currentTimeMillis() - _lockedReporters.get(reporter.getObjectId()));
			if (delay <= 1800000)
			{
				int left = (int) (1800000 - delay) / 60000;
				SystemMessage2 sm = new SystemMessage2(SystemMsg.YOU_CAN_REPORT_IN_S1_MINUTES_S2_REPORT_POINTS_REMAIN_IN_ACCOUNT);
				sm.addNumber(left);
				sm.addNumber(reporter._account.getReportsPoints());
				reporter.sendPacket(sm);
				return false;
			}
			else
				ThreadPoolManager.getInstance().execute(new ReportClear(reporter));
		}
		// In those 30 mins, the ip which made the first report cannot report again
		else if (_lockedIps.contains(reporter.getIP()))
		{
			reporter.sendPacket(new SystemMessage2(SystemMsg.THIS_CHARACTER_CANNOT_MAKE_A_REPORT_));
			return false;
		}
		// In those 30 mins, the account which made report cannot report again
		else if (_lockedAccounts.contains(reporter.getAccountName()))
		{
			reporter.sendPacket(new SystemMessage2(SystemMsg.THIS_CHARACTER_CANNOT_MAKE_A_REPORT_BECAUSE_ANOTHER_CHARACTER_FROM_THIS_ACCOUNT_HAS_ALREADY_DONE_SO));
			return false;
		}
		// If any clan/ally mate has reported any bot, you cannot report till he releases his lock
		else if (reporter.getClan() != null)
		{
			for (int i : _lockedReporters.keySet())
			{
				// Same clan
				Player p = World.getPlayer(i);
				if (p == null)
					continue;
				
				if (p.getClanId() == reporter.getClanId())
				{
					reporter.sendPacket(new SystemMessage2(SystemMsg.THIS_CHARACTER_CANNOT_MAKE_A_REPORT_));
					return false;
				}
				// Same ally
				else if (reporter.getClan().getAllyId() != 0)
				{
					if (p.getClan().getAllyId() == reporter.getClan().getAllyId())
					{
						reporter.sendPacket(new SystemMessage2(SystemMsg.THIS_CHARACTER_CANNOT_MAKE_A_REPORT_));
						return false;
					}
				}
			}
		}
		reporter._account.reducePoints();
		return true;
	}
	
	/**
	 * Will manage needed actions on enter
	 * @param activeChar
	 */
	public void onEnter(Player activeChar)
	{
		activeChar.setFirstExp(activeChar.getExp());
		restorePlayerBotPunishment(activeChar);
		activeChar._account = new AccountReportDAO(activeChar.getAccountName());
	}
	
	/**
	 * Will retore the player punish on enter
	 * @param activeChar
	 */
	private void restorePlayerBotPunishment(Player activeChar)
	{
		String punish = "";
		long delay = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `punish_type`, `time_left` FROM `bot_reported_punish` WHERE `charId` = ?");
			statement.setInt(1, activeChar.getObjectId());
			
			rset = statement.executeQuery();
			while (rset.next())
			{
				punish = rset.getString("punish_type");
				delay = rset.getLong("time_left");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		if (!punish.isEmpty() && AutoHuntingPunish.Punish.valueOf(punish) != null)
		{
			if (delay < 0)
			{
				AutoHuntingPunish.Punish p = AutoHuntingPunish.Punish.valueOf(punish);
				long left = (-delay / 1000) / 60;
				activeChar.setPunishDueBotting(p, (int) left);
			}
			else
				activeChar.endPunishment();
		}
	}
	
	/**
	 * Manages the reporter restriction data clean up
	 * to be able to report again
	 */
	private class ReportClear implements Runnable
	{
		private Player _reporter;
		
		private ReportClear(Player reporter)
		{
			_reporter = reporter;
		}
		
		@Override
		public void run()
		{
			_lockedReporters.remove(_reporter.getObjectId());
			_lockedIps.remove(_reporter.getNetConnection());
			_lockedAccounts.remove(_reporter.getAccountName());
		}
	}
}