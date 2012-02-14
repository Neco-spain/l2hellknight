package l2rt.gameserver.model.entity.olympiad;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.instancemanager.ServerVariables;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Map;

public class OlympiadDatabase
{
	public static synchronized void loadNobles()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Olympiad.OLYMPIAD_LOAD_NOBLES);
			rset = statement.executeQuery();

			while(rset.next())
			{
				int classId = rset.getInt(Olympiad.CLASS_ID);
				if(classId < 88) // Если это не 3-я профа, то исправляем со 2-й на 3-ю.
					for(ClassId id : ClassId.values())
						if(id.level() == 3 && id.getParent((byte) 0).getId() == classId)
						{
							classId = id.getId();
							break;
						}

				StatsSet statDat = new StatsSet();
				int charId = rset.getInt(Olympiad.CHAR_ID);
				statDat.set(Olympiad.CLASS_ID, classId);
				statDat.set(Olympiad.CHAR_NAME, rset.getString(Olympiad.CHAR_NAME));
				statDat.set(Olympiad.POINTS, rset.getInt(Olympiad.POINTS));
				statDat.set(Olympiad.POINTS_PAST, rset.getInt(Olympiad.POINTS_PAST));
				statDat.set(Olympiad.POINTS_PAST_STATIC, rset.getInt(Olympiad.POINTS_PAST_STATIC));
				statDat.set(Olympiad.COMP_DONE, rset.getInt(Olympiad.COMP_DONE));
				statDat.set(Olympiad.COMP_WIN, rset.getInt(Olympiad.COMP_WIN));
				statDat.set(Olympiad.COMP_LOOSE, rset.getInt(Olympiad.COMP_LOOSE));

				Olympiad._nobles.put(charId, statDat);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public static synchronized void loadNoblesRank()
	{
		Olympiad._noblesRank = new FastMap<Integer, Integer>().setShared(true);
		Map<Integer, Integer> tmpPlace = new FastMap<Integer, Integer>();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Olympiad.GET_ALL_CLASSIFIED_NOBLESS);
			rset = statement.executeQuery();

			int place = 1;
			while(rset.next())
				tmpPlace.put(rset.getInt(Olympiad.CHAR_ID), place++);

			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		int rank1 = (int) Math.round(tmpPlace.size() * 0.01);
		int rank2 = (int) Math.round(tmpPlace.size() * 0.10);
		int rank3 = (int) Math.round(tmpPlace.size() * 0.25);
		int rank4 = (int) Math.round(tmpPlace.size() * 0.50);

		if(rank1 == 0)
		{
			rank1 = 1;
			rank2++;
			rank3++;
			rank4++;
		}

		for(int charId : tmpPlace.keySet())
			if(tmpPlace.get(charId) <= rank1)
				Olympiad._noblesRank.put(charId, 1);
			else if(tmpPlace.get(charId) <= rank2)
				Olympiad._noblesRank.put(charId, 2);
			else if(tmpPlace.get(charId) <= rank3)
				Olympiad._noblesRank.put(charId, 3);
			else if(tmpPlace.get(charId) <= rank4)
				Olympiad._noblesRank.put(charId, 4);
			else
				Olympiad._noblesRank.put(charId, 5);
	}

	/**
	 * Сбрасывает информацию о ноблесах, сохраняя очки за предыдущий период
	 */
	public static synchronized void cleanupNobles()
	{
		Olympiad._log.info("Olympiad: Calculating last period...");
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Olympiad.OLYMPIAD_CALCULATE_LAST_PERIOD);
			statement.execute();
		}
		catch(Exception e)
		{
			Olympiad._log.warning("Olympiad System: Couldn't calculate last period!");
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		Olympiad._log.info("Olympiad: Clearing nobles table...");
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Olympiad.OLYMPIAD_CLEANUP_NOBLES);
			statement.execute();
		}
		catch(Exception e)
		{
			Olympiad._log.warning("Olympiad System: Couldn't cleanup nobles table!");
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		for(Integer nobleId : Olympiad._nobles.keySet())
		{
			StatsSet nobleInfo = Olympiad._nobles.get(nobleId);
			int points = nobleInfo.getInteger(Olympiad.POINTS);
			int compDone = nobleInfo.getInteger(Olympiad.COMP_DONE);
			nobleInfo.set(Olympiad.POINTS, Olympiad.DEFAULT_POINTS);
			if(compDone >= 9)
			{
				nobleInfo.set(Olympiad.POINTS_PAST, points);
				nobleInfo.set(Olympiad.POINTS_PAST_STATIC, points);
			}
			else
			{
				nobleInfo.set(Olympiad.POINTS_PAST, 0);
				nobleInfo.set(Olympiad.POINTS_PAST_STATIC, 0);
			}
			nobleInfo.set(Olympiad.COMP_DONE, 0);
			nobleInfo.set(Olympiad.COMP_WIN, 0);
			nobleInfo.set(Olympiad.COMP_LOOSE, 0);
		}
	}

	public static GArray<String> getClassLeaderBoard(int classId)
	{
		GArray<String> names = new GArray<String>();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(classId == 132 ? Olympiad.GET_EACH_CLASS_LEADER_SOULHOUND : Olympiad.GET_EACH_CLASS_LEADER);
			statement.setInt(1, classId);
			rset = statement.executeQuery();

			while(rset.next())
				names.add(rset.getString(Olympiad.CHAR_NAME));

			return names;
		}
		catch(Exception e)
		{
			Olympiad._log.warning("Olympiad System: Couldnt get heros from db: ");
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		return names;
	}

	public static synchronized void sortHerosToBe()
	{
		if(Olympiad._period != 1)
			return;

		Olympiad._heroesToBe = new GArray<StatsSet>();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			StatsSet hero;

			for(ClassId id : ClassId.values())
			{
				if(id.getId() == 133)
					continue;
				if(id.level() == 3)
				{
					statement = con.prepareStatement(id.getId() == 132 ? Olympiad.OLYMPIAD_GET_HEROS_SOULHOUND : Olympiad.OLYMPIAD_GET_HEROS);
					statement.setInt(1, id.getId());
					rset = statement.executeQuery();

					if(rset.next())
					{
						hero = new StatsSet();
						hero.set(Olympiad.CLASS_ID, id.getId());
						hero.set(Olympiad.CHAR_ID, rset.getInt(Olympiad.CHAR_ID));
						hero.set(Olympiad.CHAR_NAME, rset.getString(Olympiad.CHAR_NAME));

						Olympiad._heroesToBe.add(hero);
					}
					DatabaseUtils.closeDatabaseSR(statement, rset);
				}
			}
		}
		catch(Exception e)
		{
			Olympiad._log.warning("Olympiad System: Couldnt heros from db");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public static synchronized void saveNobleData(int nobleId)
	{
		L2Player player = L2ObjectsStorage.getPlayer(nobleId);

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			StatsSet nobleInfo = Olympiad._nobles.get(nobleId);

			int classId = nobleInfo.getInteger(Olympiad.CLASS_ID);
			String charName = player != null ? player.getName() : nobleInfo.getString(Olympiad.CHAR_NAME);
			int points = nobleInfo.getInteger(Olympiad.POINTS);
			int points_past = nobleInfo.getInteger(Olympiad.POINTS_PAST);
			int points_past_static = nobleInfo.getInteger(Olympiad.POINTS_PAST_STATIC);
			int compDone = nobleInfo.getInteger(Olympiad.COMP_DONE);
			int compWin = nobleInfo.getInteger(Olympiad.COMP_WIN);
			int compLoose = nobleInfo.getInteger(Olympiad.COMP_LOOSE);

			statement = con.prepareStatement(Olympiad.OLYMPIAD_SAVE_NOBLES);
			statement.setInt(1, nobleId);
			statement.setInt(2, classId);
			statement.setString(3, charName);
			statement.setInt(4, points);
			statement.setInt(5, points_past);
			statement.setInt(6, points_past_static);
			statement.setInt(7, compDone);
			statement.setInt(8, compWin);
			statement.setInt(9, compLoose);
			statement.execute();
		}
		catch(Exception e)
		{
			Olympiad._log.warning("Olympiad System: Couldnt save noble info in db for player " + (player != null ? player.getName() : "null"));
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public static synchronized void saveNobleData()
	{
		if(Olympiad._nobles == null)
			return;
		for(Integer nobleId : Olympiad._nobles.keySet())
			saveNobleData(nobleId);
	}

	public static synchronized void setNewOlympiadEnd()
	{
		Announcements.getInstance().announceToAll(new SystemMessage(SystemMessage.OLYMPIAD_PERIOD_S1_HAS_STARTED).addNumber(Olympiad._currentCycle));

		Calendar currentTime = Calendar.getInstance();
		currentTime.set(Calendar.DAY_OF_MONTH, 1);
		currentTime.add(Calendar.MONTH, 1);
		currentTime.set(Calendar.HOUR_OF_DAY, 00);
		currentTime.set(Calendar.MINUTE, 00);
		Olympiad._olympiadEnd = currentTime.getTimeInMillis();

		Calendar nextChange = Calendar.getInstance();
		Olympiad._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;

		Olympiad._isOlympiadEnd = false;
	}

	public static void save()
	{
		saveNobleData();
		ServerVariables.set("Olympiad_CurrentCycle", Olympiad._currentCycle);
		ServerVariables.set("Olympiad_Period", Olympiad._period);
		ServerVariables.set("Olympiad_End", Olympiad._olympiadEnd);
		ServerVariables.set("Olympiad_ValdationEnd", Olympiad._validationEnd);
		ServerVariables.set("Olympiad_NextWeeklyChange", Olympiad._nextWeeklyChange);
	}
}