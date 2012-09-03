package l2rt.gameserver.instancemanager;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.net.Socket;

import java.sql.ResultSet;
import java.sql.SQLException;

import l2rt.common.ThreadPoolManager;
import l2rt.config.ConfigSystem;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.ThreadConnection;
import l2rt.database.L2DatabaseFactory;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;

public class L2TopManager 
{
	private static Logger _log = Logger.getLogger(L2TopManager.class.getName());
	
	private final static String voteWeb = ConfigSystem.get("DatapackRoot") + "/data/vote-web.txt";
	private final static String voteSms = ConfigSystem.get("DatapackRoot") + "/data/vote-sms.txt";
	
	private static L2TopManager _instance;
	
	public static L2TopManager getInstance()
	{
		if(_instance == null && ConfigSystem.getBoolean("L2TopManagerEnabled"))
			_instance = new L2TopManager();
		return _instance;
	}
	
	public L2TopManager()
	{
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ConnectAndUpdate(), ConfigSystem.getLong("L2TopManagerInterval"), ConfigSystem.getLong("L2TopManagerInterval"));
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Clean(), ConfigSystem.getLong("L2TopManagerInterval"), ConfigSystem.getLong("L2TopManagerInterval"));
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new GiveReward(), ConfigSystem.getLong("L2TopManagerInterval"), ConfigSystem.getLong("L2TopManagerInterval"));
		_log.info("L2TopManager: loaded sucesfully");
	}
	
	private void update()
	{
		String out_sms = getPage(ConfigSystem.get("L2TopSmsAddress"));
		String out_web = getPage(ConfigSystem.get("L2TopWebAddress"));
		
		File sms = new File(voteSms);
		File web = new File(voteWeb);
		FileWriter SaveWeb = null;
		FileWriter SaveSms = null;

		try
		{
			SaveSms = new FileWriter(sms);
			SaveSms.write(out_sms);
			SaveWeb = new FileWriter(web);
			SaveWeb.write(out_web);
		}

		catch(IOException e)
		{
			e.printStackTrace();
		}
	
		finally
		{
			try
			{
				if(SaveSms != null)
					SaveSms.close();
				if(SaveWeb != null)
					SaveWeb.close();
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
			}
		}
	}
	
	private static String getPage(String address)
	{
		StringBuffer buf = new StringBuffer();
		Socket s;
		try
		{
			s = new Socket("l2top.ru", 80);

			s.setSoTimeout(30000); //Таймут 30 секунд
			String request = "GET " + address + " HTTP/1.1\r\n" +
                    "User-Agent: http:\\" + ConfigSystem.get("L2TopServerAddress") + " server\r\n" +
                    "Host: http:\\" + ConfigSystem.get("L2TopServerAddress") + " \r\n" +
                    "Accept: */*\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
			s.getOutputStream().write(request.getBytes());
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "Cp1251"));

			for(String line = in.readLine(); line != null; line = in.readLine())
			{
				buf.append(line);
				buf.append("\r\n");
			}
			s.close();
		}
		catch(Exception e)
		{
			buf.append("Connection error");
		}
		return buf.toString();
	}
	
	private void parse(boolean sms) 
	{
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(sms? voteSms : voteWeb));
			String line = in.readLine();
			while(line != null)
			{
				Calendar cal = Calendar.getInstance();
				int year = cal.get(Calendar.YEAR);
				if(line.startsWith(""+year))
				{
					try
					{
						StringTokenizer st = new StringTokenizer(line, "\t -:");
						cal.set(Calendar.YEAR, Integer.parseInt(st.nextToken()));
						cal.set(Calendar.MONTH, Integer.parseInt(st.nextToken()));
						cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(st.nextToken()));
						cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(st.nextToken()));
						cal.set(Calendar.MINUTE, Integer.parseInt(st.nextToken()));
						cal.set(Calendar.SECOND, Integer.parseInt(st.nextToken()));
						cal.set(Calendar.MILLISECOND, 0);
						String nick = st.nextToken();
						int mult = 1;
						if(sms)
							mult = Integer.parseInt(new StringBuffer(st.nextToken()).delete(0, 1).toString());
						if(cal.getTimeInMillis() + ConfigSystem.getInt("L2TopSaveDays") * 86400000 > System.currentTimeMillis())
							checkAndSaveFromDb(cal.getTimeInMillis(), nick, mult);
					}
					catch(NoSuchElementException nsee)
					{
						continue;
					}
				}
				line = in.readLine();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private synchronized void clean()
	{
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, - ConfigSystem.getInt("L2TopSaveDays"));
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_vote WHERE date<?");
			statement.setLong(1, cal.getTimeInMillis());
			statement.execute();
		}	
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}
	
	private synchronized void checkAndSaveFromDb(long date, String nick, int mult)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
			statement.setString(1, nick);
			rset = statement.executeQuery();
			int objId = 0;
			if(rset.next()) // чар существует и проверка успешна
			{
				objId = rset.getInt("obj_Id");
			}
			if(objId > 0)
			{
				DatabaseUtils.closeDatabaseSR(statement, rset);
				statement = con.prepareStatement("SELECT * FROM character_vote WHERE id=? AND date=? AND multipler=?");
				statement.setInt(1, objId);
				statement.setLong(2, date);
				statement.setInt(3, mult);
				rset = statement.executeQuery();
				if(!rset.next())
				{
					DatabaseUtils.closeDatabaseSR(statement, rset);
					statement = con.prepareStatement("INSERT INTO character_vote (date, id, nick, multipler) values (?,?,?,?)");
					statement.setLong(1, date);
					statement.setInt(2, objId);
					statement.setString(3, nick);
					statement.setInt(4, mult);
					statement.execute();
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}
	
	private synchronized void giveReward()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			for(L2Player player : L2ObjectsStorage.getAllPlayers())
			{
				int objId = player.getObjectId();
				int mult = 0;
				statement = con.prepareStatement("SELECT multipler FROM character_vote WHERE id=? AND has_reward=0");
				statement.setInt(1, objId);
				rset = statement.executeQuery();
				while(rset.next())
				{
					mult += rset.getInt("multipler");
				}
				
				DatabaseUtils.closeDatabaseSR(statement, rset);
				statement = con.prepareStatement("UPDATE character_vote SET has_reward=1 WHERE id=?");
				statement.setInt(1, objId);
				statement.executeUpdate();
				if(mult > 0)
				{
					if(player.getVar("lang@").equalsIgnoreCase("ru"))
						player.sendMessage("Администрация сервера " + ConfigSystem.get("L2TopServerAddress") + " благодарит вас за голосование");
					else
						player.sendMessage("The administration server " + ConfigSystem.get("L2TopServerAddress") + " thank you for your vote");
                    for(int i=0; i < ConfigSystem.getIntArray("L2TopReward").length; i+=2)
                        player.getInventory().addItem(ConfigSystem.getIntArray("L2TopReward")[i], ConfigSystem.getIntArray("L2TopReward")[i+1]*mult);
				}
				DatabaseUtils.closeStatement(statement);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}
	
	private class ConnectAndUpdate implements Runnable
	{
		@Override
		public void run()
		{
			update();
			parse(true);
			parse(false);
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