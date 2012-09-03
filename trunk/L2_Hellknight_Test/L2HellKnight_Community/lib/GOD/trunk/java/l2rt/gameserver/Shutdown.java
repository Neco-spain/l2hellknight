package l2rt.gameserver;

import l2rt.Config;
import l2rt.Server;
import l2rt.common.ThreadPoolManager;
import l2rt.database.L2DatabaseFactory;
import l2rt.debug.HeapDumper;
import l2rt.extensions.network.SelectorThread;
import l2rt.extensions.scripts.Scripts;
import l2rt.gameserver.cache.InfoCache;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.CoupleManager;
import l2rt.gameserver.instancemanager.CursedWeaponsManager;
import l2rt.gameserver.loginservercon.LSConnection;
import l2rt.gameserver.model.L2Multisell;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.serverpackets.ExShowScreenMessage;
import l2rt.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2rt.gameserver.tables.*;
import l2rt.util.Log;
import l2rt.util.Util;

import java.util.logging.Logger;

@SuppressWarnings( { "nls", "unqualified-field-access", "boxing" })
public class Shutdown extends Thread
{
	private static final Logger _log = Logger.getLogger(Shutdown.class.getName());

	private static Shutdown _instance;
	private static Shutdown _counterInstance = null;

	private int secondsShut;
	private int shutdownMode;

	public static final int SIGTERM = 0;
	public static final int GM_SHUTDOWN = 1;
	public static final int GM_RESTART = 2;
	public static final int ABORT = 3;
	private static String[] _modeText = { "shutdown", "shutdown", "restarting", "aborting" };

	public int getSeconds()
	{
		if(_counterInstance != null)
			return _counterInstance.secondsShut;
		return -1;
	}

	public int getMode()
	{
		if(_counterInstance != null)
			return _counterInstance.shutdownMode;
		return -1;
	}

	private void announce(String text, int time, ScreenMessageAlign align)
	{
		Announcements _an = Announcements.getInstance();
		ExShowScreenMessage sm = new ExShowScreenMessage(text, time, align, false);
		switch(Config.SHUTDOWN_MSG_TYPE)
		{
			case 1:
				_an.announceToAll(text);
				break;
			case 2:
				for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
					player.sendPacket(sm);
				break;
			case 3:
				_an.announceToAll(text);
				for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
					player.sendPacket(sm);
				break;
		}
	}

	/**
	 * This function starts a shutdown countdown from Telnet (Copied from Function startShutdown())
	 *
	 * @param IP		    IP Which Issued shutdown command
	 * @param seconds	   seconds untill shutdown
	 * @param restart	   true if the server will restart after shutdown
	 */
	public void startTelnetShutdown(String IP, int seconds, boolean restart)
	{
		_log.warning("IP: " + IP + " issued shutdown command. " + _modeText[shutdownMode] + " in " + seconds + " seconds!");
		announce("This server will be " + _modeText[shutdownMode] + " in " + seconds + " seconds!", 10000, ScreenMessageAlign.BOTTOM_RIGHT);

		if(_counterInstance != null)
			_counterInstance._abort();
		_counterInstance = new Shutdown(seconds, restart);
		_counterInstance.start();
	}

	public void setAutoRestart(int seconds)
	{
		_log.info("AutoRestart scheduled through " + Util.formatTime(seconds));
		if(_counterInstance != null)
			_counterInstance._abort();
		_counterInstance = new Shutdown(seconds, true);
		_counterInstance.start();
	}

	/**
	 * This function aborts a running countdown
	 *
	 * @param IP		    IP Which Issued shutdown command
	 */
	public void Telnetabort(String IP)
	{
		_log.warning("IP: " + IP + " issued shutdown ABORT. " + _modeText[shutdownMode] + " has been stopped!");

		if(_counterInstance != null)
		{
			_counterInstance._abort();
			announce("This server aborts " + _modeText[shutdownMode] + " and continues normal operation!", 10000, ScreenMessageAlign.TOP_CENTER);
		}
	}

	/**
	 * Default constucter is only used internal to create the shutdown-hook instance
	 *
	 */
	public Shutdown()
	{
		secondsShut = -1;
		shutdownMode = SIGTERM;
	}

	/**
	 * This creates a countdown instance of Shutdown.
	 *
	 * @param seconds	how many seconds until shutdown
	 * @param restart	true is the server shall restart after shutdown
	 *
	 */
	public Shutdown(int seconds, boolean restart)
	{
		if(seconds < 0)
			seconds = 0;
		secondsShut = seconds;
		if(restart)
			shutdownMode = GM_RESTART;
		else
			shutdownMode = GM_SHUTDOWN;
	}

	/**
	 * get the shutdown-hook instance
	 * the shutdown-hook instance is created by the first call of this function,
	 * but it has to be registrered externaly.
	 *
	 * @return instance of Shutdown, to be used as shutdown hook
	 */
	public static Shutdown getInstance()
	{
		if(_instance == null)
			_instance = new Shutdown();
		return _instance;
	}

	/**
	 * this function is called, when a new thread starts
	 *
	 * if this thread is the thread of getInstance, then this is the shutdown hook
	 * and we save all data and disconnect all clients.
	 *
	 * after this thread ends, the server will completely exit
	 *
	 * if this is not the thread of getInstance, then this is a countdown thread.
	 * we start the countdown, and when we finished it, and it was not aborted,
	 * we tell the shutdown-hook why we call exit, and then call exit
	 *
	 * when the exit status of the server is 1, startServer.sh / startServer.bat
	 * will restart the server.
	 *
	 * Логгинг в этом методе не работает!!!
	 */
	@Override
	public void run()
	{
		if(this == _instance)
		{
			LSConnection.getInstance().shutdown();
			System.out.println("Shutting down scripts.");
			// Вызвать выключение у скриптов
			Scripts.getInstance().shutdown();

			// ensure all services are stopped
			// stop all scheduled tasks
			saveData();
			try
			{
				ThreadPoolManager.getInstance().shutdown();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
			// last byebye, save all data and quit this server
			// logging doesn't works here :(
			LSConnection.getInstance().shutdown();
			// saveData sends messages to exit players, so shutdown selector after it
			System.out.println("Shutting down selector.");
			if(GameServer.gameServer != null)
				for(SelectorThread<L2GameClient> st : GameServer.gameServer.getSelectorThreads())
					try
					{
						st.shutdown();
					}
					catch(Throwable t)
					{
						t.printStackTrace();
					}
			// commit data, last chance
			try
			{
				System.out.println("Shutting down database communication.");
				L2DatabaseFactory.getInstance().shutdown();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}

			if(Config.DUMP_MEMORY_ON_SHUTDOWN)
				try
				{
					System.out.println("Prepearing to make memory snapshot - unloading static data...");
					GeoEngine.unload();
					IdFactory.unload();
					L2Multisell.unload();
					InfoCache.unload();

					//TODO ClanTable ??
					NpcTable.unload();
					PetDataTable.unload();
					SkillSpellbookTable.unload();
					SkillTable.unload();
					SkillTreeTable.unload();
					SpawnTable.unload();
					TerritoryTable.unload();

					Util.gc(10, 1000);
					System.out.println("Memory snapshot saved: " + HeapDumper.dumpHeap(Config.SNAPSHOTS_DIRECTORY, true));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

			// server will quit, when this function ends.
			System.out.println("Shutdown finished.");
			Server.halt(_instance.shutdownMode == GM_RESTART ? 2 : 0, "GS Shutdown");
		}
		else
		{
			// gm shutdown: send warnings and then call exit to start shutdown sequence
			countdown();
			// last point where logging is operational :(
			System.out.println("GM shutdown countdown is over. " + _modeText[shutdownMode] + " NOW!");
			switch(shutdownMode)
			{
				case GM_SHUTDOWN:
					_instance.setMode(GM_SHUTDOWN);
					Server.exit(0, "GM_SHUTDOWN");
					break;
				case GM_RESTART:
					_instance.setMode(GM_RESTART);
					Server.exit(2, "GM_RESTART");
					break;
			}
		}
	}

	/**
	 * This functions starts a shutdown countdown
	 *
	 * @param activeChar	GM who issued the shutdown command
	 * @param seconds		seconds until shutdown
	 * @param restart		true if the server will restart after shutdown
	 */
	public void startShutdown(L2Player activeChar, int seconds, boolean restart)
	{
		_log.warning("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown command. " + _modeText[shutdownMode] + " in " + seconds + " seconds!");
		if(shutdownMode > 0)
			announce("This server will be " + _modeText[shutdownMode] + " in " + seconds + " seconds", 10000, ScreenMessageAlign.BOTTOM_RIGHT);

		if(_counterInstance != null)
			_counterInstance._abort();

		//the main instance should only run for shutdown hook, so we start a new instance
		_counterInstance = new Shutdown(seconds, restart);
		_counterInstance.start();
	}

	/**
	 * This function aborts a running countdown
	 *
	 * @param activeChar	GM who issued the abort command
	 */
	public void abort(L2Player activeChar)
	{
		_log.warning("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown ABORT. " + _modeText[shutdownMode] + " has been stopped!");
		announce("This server aborts " + _modeText[shutdownMode] + " and continues normal operation!", 10000, ScreenMessageAlign.TOP_CENTER);

		if(_counterInstance != null)
			_counterInstance._abort();
	}

	/**
	 * set the shutdown mode
	 * @param mode	what mode shall be set
	 */
	private void setMode(int mode)
	{
		shutdownMode = mode;
	}

	/**
	 * set shutdown mode to ABORT
	 */
	private void _abort()
	{
		shutdownMode = ABORT;
	}

	/**
	 * this counts the countdown and reports it to all players
	 * countdown is aborted if mode changes to ABORT
	 */
	private void countdown()
	{
		while(secondsShut > 0)
			try
			{
				switch(secondsShut)
				{
					case 1800:
						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 30 minutes.", 10000, ScreenMessageAlign.BOTTOM_RIGHT);
						break;
					case 600:
						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 10 minutes.", 10000, ScreenMessageAlign.BOTTOM_RIGHT);
						break;
					case 300:
						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 5 minutes.", 10000, ScreenMessageAlign.BOTTOM_RIGHT);
						break;
					case 240:
						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 4 minutes.", 10000, ScreenMessageAlign.BOTTOM_RIGHT);
						break;
					case 180:
						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 3 minutes.", 10000, ScreenMessageAlign.BOTTOM_RIGHT);
						break;
					case 120:
						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 2 minutes.", 10000, ScreenMessageAlign.BOTTOM_RIGHT);
						break;
					case 60:
						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 1 minutes.", 10000, ScreenMessageAlign.BOTTOM_RIGHT);
						break;
					case 30:
						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 30 sec.", 10000, ScreenMessageAlign.BOTTOM_RIGHT);
						break;
					case 15:
						System.out.println(l2rt.gameserver.model.L2ObjectsStorage.getStats());
						System.out.println();
						System.out.println(l2rt.gameserver.geodata.PathFindBuffers.getStats());
						System.out.println();
						if(Config.PROTECT_ENABLE && Config.PROTECT_COMPRESSION > 0)
						{
							System.out.println(SelectorThread.getStats());
							System.out.println();
						}

						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 15 sec.", 10000, ScreenMessageAlign.TOP_CENTER);
						if(!Config.DONTLOADSPAWN)
							try
							{
								L2World.deleteVisibleNpcSpawns();
							}
							catch(Throwable t)
							{
								System.out.println("Error while unspawn Npcs!");
								t.printStackTrace();
							}
						break;
					case 10:
						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 10 sec.", 10000, ScreenMessageAlign.BOTTOM_RIGHT);
						break;
					case 5:
						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 5 sec.", 10000, ScreenMessageAlign.BOTTOM_RIGHT);
						break;
					case 4:
						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 4 sec.", 10000, ScreenMessageAlign.BOTTOM_RIGHT);
						break;
					case 3:
						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 3 sec.", 10000, ScreenMessageAlign.BOTTOM_RIGHT);
						break;
					case 2:
						announce("Attention Players! This server will be " + _modeText[shutdownMode] + " in 2 sec.", 10000, ScreenMessageAlign.BOTTOM_RIGHT);
						break;
					case 1:
						announce("This server will be " + _modeText[shutdownMode] + " momentally!", 10000, ScreenMessageAlign.TOP_CENTER);
						break;
					case 0:
						disconnectAllCharacters();
						break;
				}

				secondsShut--;

				int delay = 1000; //milliseconds
				Thread.sleep(delay);

				if(shutdownMode == ABORT)
					break;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	/**
	 * this sends a last byebye, disconnects all players and saves data
	 */
	private void saveData()
	{
		switch(shutdownMode)
		{
			case SIGTERM:
				System.err.println("SIGTERM received. Shutting down NOW!");
				Log.LogServ(Log.GS_SIGTERM, 0, 0, 0, 0);
				break;
			case GM_SHUTDOWN:
				System.err.println("GM shutdown received. Shutting down NOW!");
				Log.LogServ(Log.GS_shutdown, 0, 0, 0, 0);
				break;
			case GM_RESTART:
				System.err.println("GM restart received. Restarting NOW!");
				Log.LogServ(Log.GS_restart, 0, 0, 0, 0);
				break;
		}

		disconnectAllCharacters();
		
		if(Config.ENABLE_OLYMPIAD)
			try
			{
				OlympiadDatabase.save();
				System.out.println("Olympiad System: Data saved!");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		if(Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance().store();
			System.out.println("Couples: Data saved!");
		}

		if(Config.ALLOW_CURSED_WEAPONS)
		{
			CursedWeaponsManager.getInstance().saveData();
			System.out.println("CursedWeaponsManager: Data saved!");
		}

		NpcTable.storeKillsCount();

		System.out.println("All Data saved. All players disconnected, shutting down.");
		try
		{
			int delay = 5000;
			Thread.sleep(delay);
		}
		catch(InterruptedException e)
		{
			//never happens :p
		}
	}

	/**
	 * this disconnects all clients from the server
	 *
	 */
	private void disconnectAllCharacters()
	{
		for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
			try
			{
				player.logout(true, false, false, true);
			}
			catch(Exception e)
			{
				System.out.println("Error while disconnect char: " + player.getName());
				e.printStackTrace();
			}
		try
		{
			Thread.sleep(1000);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
}