package l2rt.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import l2rt.Config;
import l2rt.Server;
import l2rt.common.StatsUtil;
import l2rt.config.ConfigSystem;
import l2rt.database.L2DatabaseFactory;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.taskmanager.MemoryWatchDog;
import l2rt.status.Status;
import l2rt.util.Log;
import l2rt.util.Util;

@SuppressWarnings( { "nls", "unqualified-field-access", "boxing" })
public class GameStart extends GameServer
{
	public GameStart() throws Exception
	{}
	private static final Logger _log = Logger.getLogger(GameStart.class.getName());
	public static GameServer gameServer;
	public static Status statusServer;
	public static void main(String[] args) throws Exception
	{
		Server.SERVER_MODE = Server.MODE_GAMESERVER;
		// Local Constants
		
		

		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();

		// Create input stream for log file -- or store file data into memory
		InputStream is = new FileInputStream(new File(LOG_NAME));
		LogManager.getLogManager().readConfiguration(is);
		is.close();

		// Initialize config
		Config.load();
        ConfigSystem.load();
		ConfigSystem.loadSkillDurationList();
		ConfigSystem.loadSkillReuseList();
		Util.waitForFreePorts(Config.GAMESERVER_HOSTNAME, Config.PORTS_GAME);
		L2DatabaseFactory.getInstance();
		Log.InitGSLoggers();

		gameServer = new GameServer();

		if(Config.IS_TELNET_ENABLED)
		{
			statusServer = new Status(Server.MODE_GAMESERVER);
			statusServer.start();
		}
		else
			_log.info(LOG_TEXT);

		Util.gc(5, 1000);
		//_log.info("Free memory " + MemoryWatchDog.getMemFreeMb() + " of " + MemoryWatchDog.getMemMaxMb());
		Log.LogServ(Log.GS_started, (int) MemoryWatchDog.getMemFree(), (int) MemoryWatchDog.getMemMax(), IdFactory.getInstance().size(), 0);
		serverLoaded = true;
		_log.info(Lines);
		String memUsage = StatsUtil.getMemUsage().toString();
		for (String line : memUsage.split("\n"))
		    _log.info(line);
		_log.info(Lines);
		//Shutdown.getInstance().setAutoRestart(Config.RESTART_TIME * 60 * 60);
	}
}