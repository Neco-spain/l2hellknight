package l2r.gameserver.instancemanager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.utils.Log;

public class MMOTopManager
{
	private static Logger _log = Logger.getLogger(MMOTopManager.class.getName());

	private static final String SELECT_PLAYER_OBJID = "SELECT obj_Id FROM characters WHERE char_name=?";
	private static final String SELECT_CHARACTER_MMOTOP_DATA = "SELECT * FROM character_mmotop_votes WHERE id=? AND date=? AND multipler=?";
	private static final String INSERT_MMOTOP_DATA = "INSERT INTO character_mmotop_votes (date, id, nick, multipler) values (?,?,?,?)";
	private static final String DELETE_MMOTOP_DATA = "DELETE FROM character_mmotop_votes WHERE date<?";
	private static final String SELECT_MULTIPLER_MMOTOP_DATA = "SELECT multipler FROM character_mmotop_votes WHERE id=? AND has_reward=0";
	private static final String UPDATE_MMOTOP_DATA = "UPDATE character_mmotop_votes SET has_reward=1 WHERE id=?";

	BufferedReader reader;

	private static MMOTopManager _instance;

	public static MMOTopManager getInstance()
	{
		if(_instance == null && Config.MMO_TOP_MANAGER_ENABLED)
			_instance = new MMOTopManager();
		return _instance;
	}

	public MMOTopManager()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new ConnectAndUpdate(), Config.MMO_TOP_MANAGER_INTERVAL, Config.MMO_TOP_MANAGER_INTERVAL);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Clean(), Config.MMO_TOP_MANAGER_INTERVAL, Config.MMO_TOP_MANAGER_INTERVAL);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new GiveReward(), Config.MMO_TOP_MANAGER_INTERVAL, Config.MMO_TOP_MANAGER_INTERVAL);
		_log.info("MMOTopManager: loaded sucesfully");
	}

	public void getPage(String address)
	{
		try
		{
			URL url = new URL(address);
			reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF8"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void parse()
	{
		try
		{
			String line;
			while((line = reader.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\t. :");
				while(st.hasMoreTokens())
					try
					{
						st.nextToken();
						int day = Integer.parseInt(st.nextToken());
						int month = Integer.parseInt(st.nextToken()) - 1;
						int year = Integer.parseInt(st.nextToken());
						int hour = Integer.parseInt(st.nextToken());
						int minute = Integer.parseInt(st.nextToken());
						int second = Integer.parseInt(st.nextToken());
						st.nextToken();
						st.nextToken();
						st.nextToken();
						st.nextToken();
						String charName = st.nextToken();
						int voteType = Integer.parseInt(st.nextToken());

						Calendar calendar = Calendar.getInstance();
						calendar.set(1, year);
						calendar.set(2, month);
						calendar.set(5, day);
						calendar.set(11, hour);
						calendar.set(12, minute);
						calendar.set(13, second);
						calendar.set(14, 0);

						long voteTime = calendar.getTimeInMillis() / 1000;

						if(voteTime + Config.MMO_TOP_SAVE_DAYS * 86400 > System.currentTimeMillis() / 1000)
							checkAndSave(voteTime, charName, voteType);
					}
					catch(Exception e)
					{}
			}
		}
		catch(Exception e)
		{
			_log.warning("MMOTopManager: Cant store MMOTop data.");
			e.printStackTrace();
		}
	}

	public void checkAndSave(long voteTime, String charName, int voteType)
	{
		Connection con = null;
		PreparedStatement selectObjectStatement = null, selectMmotopStatement = null, insertStatement = null;
		ResultSet rsetObject = null, rsetMmotop = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			selectObjectStatement = con.prepareStatement(SELECT_PLAYER_OBJID);
			selectObjectStatement.setString(1, charName);
			rsetObject = selectObjectStatement.executeQuery();

			int objId = 0;
			if(rsetObject.next())
				objId = rsetObject.getInt("obj_Id");
			if(objId > 0)
			{
				selectMmotopStatement = con.prepareStatement(SELECT_CHARACTER_MMOTOP_DATA);
				selectMmotopStatement.setInt(1, objId);
				selectMmotopStatement.setLong(2, voteTime);
				selectMmotopStatement.setInt(3, voteType);
				rsetMmotop = selectMmotopStatement.executeQuery();
				if(!rsetMmotop.next())
				{
					insertStatement = con.prepareStatement(INSERT_MMOTOP_DATA);
					insertStatement.setLong(1, voteTime);
					insertStatement.setInt(2, objId);
					insertStatement.setString(3, charName);
					insertStatement.setInt(4, voteType);
					insertStatement.execute();
					insertStatement.close();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, selectObjectStatement, rsetObject);
			DbUtils.closeQuietly(con, selectMmotopStatement, rsetMmotop);
			DbUtils.closeQuietly(con, insertStatement);
		}
	}

	private synchronized void clean()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -Config.MMO_TOP_SAVE_DAYS);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_MMOTOP_DATA);
			statement.setLong(1, calendar.getTimeInMillis() / 1000);
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private synchronized void giveReward()
	{
		Connection con = null;
		PreparedStatement selectMultStatement = null, updateStatement = null;
		ResultSet rsetMult = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			for(Player player : GameObjectsStorage.getAllPlayers())
			{
				int objId = player.getObjectId();
				int mult = 0;
				selectMultStatement = con.prepareStatement(SELECT_MULTIPLER_MMOTOP_DATA);
				selectMultStatement.setInt(1, objId);
				rsetMult = selectMultStatement.executeQuery();

				while(rsetMult.next())
					mult += rsetMult.getInt("multipler");

				if(mult > 0)
				{
					updateStatement = con.prepareStatement(UPDATE_MMOTOP_DATA);
					updateStatement.setInt(1, objId);
					updateStatement.executeUpdate();

					if(player.getVar("lang@").equalsIgnoreCase("ru"))
						player.sendMessage("Спасибо за Ваш голос в рейтинге MMOTop. C наилучшими пожеланиями " + Config.MMO_TOP_SERVER_ADDRESS);
					else
						player.sendMessage("Thank you for your vote in MMOTop raiting. Best regards " + Config.L2_TOP_SERVER_ADDRESS);
					for(int i = 0; i < Config.MMO_TOP_REWARD.length; i += 2)
					{
						if(Config.MMO_TOP_REWARD[i] == -100) // PC Bang
						{
							player.addPcBangPoints((Config.MMO_TOP_REWARD[i + 1] * mult), false);
							Log.add(player.getName() + " | " + player.getObjectId() + " | MMOTop reward item ID | " + Config.MMO_TOP_REWARD[i] + " | MMOTop reward count | " + (Config.MMO_TOP_REWARD[i + 1] * mult) + " |", "mmotop");
						}
						else if(Config.MMO_TOP_REWARD[i] == -200) // Clan reputation
						{
							player.getClan().incReputation((Config.MMO_TOP_REWARD[i + 1] * mult));
							Log.add(player.getName() + " | " + player.getObjectId() + " | MMOTop reward item ID | " + Config.MMO_TOP_REWARD[i] + " | MMOTop reward count | " + (Config.MMO_TOP_REWARD[i + 1] * mult) + " |", "mmotop");
						}
						else if(Config.MMO_TOP_REWARD[i] == -300) // Fame
						{
							player.setFame(player.getFame() + (Config.MMO_TOP_REWARD[i + 1] * mult));
							Log.add(player.getName() + " | " + player.getObjectId() + " | MMOTop reward item ID | " + Config.MMO_TOP_REWARD[i] + " | MMOTop reward count | " + (Config.MMO_TOP_REWARD[i + 1] * mult) + " |", "mmotop");
						}
						else
						{
							player.getInventory().addItem(Config.MMO_TOP_REWARD[i], (Config.MMO_TOP_REWARD[i + 1] * mult));
							Log.add(player.getName() + " | " + player.getObjectId() + " | MMOTop reward item ID | " + Config.MMO_TOP_REWARD[i] + " | MMOTop reward count | " + (Config.MMO_TOP_REWARD[i + 1] * mult) + " |", "mmotop");
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, selectMultStatement, rsetMult);
			DbUtils.closeQuietly(con, updateStatement);
		}
	}

	private class ConnectAndUpdate implements Runnable
	{
		@Override
		public void run()
		{
			getPage(Config.MMO_TOP_WEB_ADDRESS);
			parse();
		}
	}

	private class Clean implements Runnable
	{
		@Override
		public void run()
		{
			clean();
		}
	}

	private class GiveReward implements Runnable
	{
		@Override
		public void run()
		{
			giveReward();
		}
	}
}