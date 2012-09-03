package l2rt.gameserver.tables;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.util.GArray;
import l2rt.util.GCArray;
import l2rt.util.Rnd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.logging.Logger;

public class FakePlayersTable
{
	public class Task implements Runnable
	{
		public void run()
		{
			try
			{
				if(_activeFakePlayers.size() < Math.max(0, L2ObjectsStorage.getAllPlayersCount() - L2ObjectsStorage.getAllOfflineCount()) * Config.FAKE_PLAYERS_PERCENT / 100 && _activeFakePlayers.size() < _fakePlayers.length)
				{
					if(Rnd.chance(10))
					{
						String player = _fakePlayers[Rnd.get(_fakePlayers.length)];
						if(player != null && !_activeFakePlayers.contains(player))
							_activeFakePlayers.add(player);
					}
				}
				else if(_activeFakePlayers.size() > 0)
					_activeFakePlayers.remove(Rnd.get(_activeFakePlayers.size()));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private static final Logger _log = Logger.getLogger(FakePlayersTable.class.getName());

	private static String[] _fakePlayers;
	private static GCArray<String> _activeFakePlayers = new GCArray<String>();

	private static FakePlayersTable _instance;

	public static FakePlayersTable getInstance()
	{
		if(_instance == null)
			new FakePlayersTable();
		return _instance;
	}

	public FakePlayersTable()
	{
		_instance = this;
		if(Config.ALLOW_FAKE_PLAYERS)
		{
			parseData();
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Task(), 180000, 1000);
		}
	}

	private void parseData()
	{
		LineNumberReader lnr = null;
		try
		{
			File doorData = new File(Config.FAKE_PLAYERS_LIST);
			lnr = new LineNumberReader(new BufferedReader(new FileReader(doorData)));
			String line;
			GArray<String> players_list = new GArray<String>();
			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() == 0 || line.startsWith("#"))
					continue;
				players_list.add(line);
			}
			_fakePlayers = players_list.toArray(new String[players_list.size()]);
			_log.config("FakePlayersTable: Loaded " + _fakePlayers.length + " Fake Players.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception e1)
			{}
		}
	}

	public static int getFakePlayersCount()
	{
		return _activeFakePlayers.size();
	}

	public static GCArray<String> getActiveFakePlayers()
	{
		return _activeFakePlayers;
	}
}