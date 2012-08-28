package net.sf.l2j.gameserver;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.CharSchemesTable;
import net.sf.l2j.gameserver.gameserverpackets.ServerStatus;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.events.Heroes;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ServerClose;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class Shutdown extends Thread
{
	private static Logger _log = Logger.getLogger(Shutdown.class.getName());
	private static Shutdown _instance;
	private static Shutdown _counterInstance = null;

	private int _secondsShut;

	private int _shutdownMode;
	public static final int SIGTERM = 0;
	public static final int GM_SHUTDOWN = 1;
	public static final int GM_RESTART = 2;
	public static final int ABORT = 3;
	private static final String[] MODE_TEXT = {"SIGTERM", "shutting down", "restarting", "aborting"};

    public void startTelnetShutdown(String IP, int seconds, boolean restart)
    {
        Announcements _an = Announcements.getInstance();
        _log.warning("IP: " + IP + " issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in "+seconds+ " seconds!");

        if (restart) 
        {
            _shutdownMode = GM_RESTART;
        } 
        else 
        {
            _shutdownMode = GM_SHUTDOWN;
        }

        if(_shutdownMode > 0)
        {
            _an.announceToAll("Внимание все!");
            _an.announceToAll("Сервер " + MODE_TEXT[_shutdownMode] + " через "+seconds+ " секунд!");
            if(_shutdownMode == 1 || _shutdownMode == 2)
            {
                _an.announceToAll("Пожалуйста не пользуйтесь Gatekeepers и SoE");
                _an.announceToAll("during server " + MODE_TEXT[_shutdownMode] + " procedure.");
            }
        }

        if (_counterInstance != null) 
        {
            _counterInstance._abort();
        }
        _counterInstance = new Shutdown(seconds, restart);
        _counterInstance.start();
    }

    public void telnetAbort(String IP) 
    {
        Announcements _an = Announcements.getInstance();
        _log.warning("IP: " + IP + " issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
        _an.announceToAll("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");

        if (_counterInstance != null) 
        {
            _counterInstance._abort();
        }
    }
    public static Shutdown getCounterInstance()
    {
      return _counterInstance;
    }
	public Shutdown() 
	{
		_secondsShut = -1;
		_shutdownMode = SIGTERM;
	}

	public Shutdown(int seconds, boolean restart)
	{
		if (seconds < 0) 
		{
			seconds = 0;
		}
		_secondsShut = seconds;
		
		if (restart) 
		{
			_shutdownMode = GM_RESTART;
		}
		else 
		{
			_shutdownMode = GM_SHUTDOWN;
		}
	}

	public static Shutdown getInstance()
	{
		if (_instance == null)
		{
			_instance = new Shutdown();
		}
		return _instance;
	}

	@Override
	public void run()
	{
		try
		{
		}
		catch (Throwable t)
		{
		}

		if (this == _instance)
		{
			try
			{
				GameTimeController.getInstance().stopTimer();
			}
			catch (Throwable t)
			{
			}
			try
			{
				ThreadPoolManager.getInstance().shutdown();
			}
			catch (Throwable t)
			{
			}
			saveData();

			try
			{
				LoginServerThread.getInstance().interrupt();
			}
			catch (Throwable t)
			{
			}
			try
			{
				GameServer.gameServer.getSelectorThread().shutdown();
				GameServer.gameServer.getSelectorThread().setDaemon(true);
			}
			catch (Throwable t)
			{
			}
			try
			{
				L2DatabaseFactory.getInstance().shutdown();
			}
			catch (Throwable t)
			{

			}
			if (_instance._shutdownMode == GM_RESTART)
			{
				Runtime.getRuntime().halt(2);
			}
			else
			{
				Runtime.getRuntime().halt(0);
			}
		}
		else
		{
			countdown();
			_log.warning("GM shutdown countdown is over. " + MODE_TEXT[_shutdownMode] + " NOW!");
			switch (_shutdownMode) 
			{
				case GM_SHUTDOWN:
					_instance.setMode(GM_SHUTDOWN);
					System.exit(0);
					break;
				case GM_RESTART:
					_instance.setMode(GM_RESTART);
					System.exit(2);
					break;
			}
		}
	}

	public void startShutdown(L2PcInstance activeChar, int seconds, boolean restart) 
	{
		Announcements _an = Announcements.getInstance();
		_log.warning("GM: "+activeChar.getName()+"("+activeChar.getObjectId()+") issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in "+seconds+ " seconds!");

		if (restart) 
		{
            _shutdownMode = GM_RESTART;
        } 
		else 
		{
            _shutdownMode = GM_SHUTDOWN;
        }

        if(_shutdownMode > 0)
        {
            _an.announceToAll("Attention players!");
            _an.announceToAll("Server is " + MODE_TEXT[_shutdownMode] + " in "+seconds+ " seconds!");
            if(_shutdownMode == 1 || _shutdownMode == 2)
            {
                _an.announceToAll("Please, avoid to use Gatekeepers/SoE");
                _an.announceToAll("during server " + MODE_TEXT[_shutdownMode] + " procedure.");
            }
        }

		if (_counterInstance != null) 
		{
			_counterInstance._abort();
		}

		_counterInstance = new Shutdown(seconds, restart);
		_counterInstance.start();
	}

	public void abort(L2PcInstance activeChar) 
	{
		Announcements _an = Announcements.getInstance();
		_log.warning("GM: "+activeChar.getName()+"("+activeChar.getObjectId()+") issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
		_an.announceToAll("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");

		if (_counterInstance != null) 
		{
			_counterInstance._abort();
		}
	}

	private void setMode(int mode) 
	{
		_shutdownMode = mode;
	}

	private void _abort() 
	{
		_shutdownMode = ABORT;
	}

	private void countdown() 
	{
		Announcements _an = Announcements.getInstance();

		try {
			while (_secondsShut > 0) 
			{

				switch (_secondsShut)
				{
					case 540:
						{
						_an.announceToAll("The server is " + MODE_TEXT[_shutdownMode] + " in 9 minutes.");
						SystemMessage sm = new SystemMessage(SystemMessageId.EXIT_GAME_SECOND);
						sm.addString("540");
						MessageToAll(sm);
						break;
					}
				    case 480:
						{
						_an.announceToAll("The server is " + MODE_TEXT[_shutdownMode] + " in 8 minutes.");
						SystemMessage sm = new SystemMessage(SystemMessageId.EXIT_GAME_SECOND);
						sm.addString("480");
						MessageToAll(sm);
						break;
					}
				    case 420:
						{
						_an.announceToAll("The server is " + MODE_TEXT[_shutdownMode] + " in 7 minutes.");
						SystemMessage sm = new SystemMessage(SystemMessageId.EXIT_GAME_SECOND);
						sm.addString("420");
						MessageToAll(sm);
						break;
					}
				    case 360:
						{
						_an.announceToAll("The server is " + MODE_TEXT[_shutdownMode] + " in 6 minutes.");
						SystemMessage sm = new SystemMessage(SystemMessageId.EXIT_GAME_SECOND);
						sm.addString("360");
						MessageToAll(sm);
						break;
					}
					case 300:
						{
						_an.announceToAll("The server is " + MODE_TEXT[_shutdownMode] + " in 5 minutes.");
						SystemMessage sm = new SystemMessage(SystemMessageId.EXIT_GAME_SECOND);
						sm.addString("300");
						MessageToAll(sm);
						break;
					}
					case 240:
						{
						_an.announceToAll("The server is " + MODE_TEXT[_shutdownMode] + " in 4 minutes.");
						SystemMessage sm = new SystemMessage(SystemMessageId.EXIT_GAME_SECOND);
						sm.addString("240");
						MessageToAll(sm);
						break;
					}
					case 180:
						{
						_an.announceToAll("The server is " + MODE_TEXT[_shutdownMode] + " in 3 minutes.");
						SystemMessage sm = new SystemMessage(SystemMessageId.EXIT_GAME_SECOND);
						sm.addString("180");
						MessageToAll(sm);
						break;
					}
					case 120:
						{
						_an.announceToAll("The server is " + MODE_TEXT[_shutdownMode] + " in 2 minutes.");
						SystemMessage sm = new SystemMessage(SystemMessageId.EXIT_GAME_SECOND);
						sm.addString("120");
						MessageToAll(sm);
						break;
					}
					case 60:
						{
						LoginServerThread.getInstance().setServerStatus(ServerStatus.STATUS_DOWN);
						_an.announceToAll("The server is " + MODE_TEXT[_shutdownMode] + " in 1 minute.");
						SystemMessage sm = new SystemMessage(SystemMessageId.EXIT_GAME_SECOND);
						sm.addString("60");
						MessageToAll(sm);
						break;
					}
					case 30:
						{
						_an.announceToAll("The server is " + MODE_TEXT[_shutdownMode] + " in 30 seconds.");
						SystemMessage sm = new SystemMessage(SystemMessageId.EXIT_GAME_SECOND);
						sm.addString("30");
						MessageToAll(sm);
						break;
					}
					case 5:
						_an.announceToAll("The server is " + MODE_TEXT[_shutdownMode] + " in 5 seconds, please delog NOW !");
						SystemMessage sm = new SystemMessage(SystemMessageId.EXIT_GAME_SECOND);
						sm.addString("5");
						MessageToAll(sm);
						break;
				}

				_secondsShut--;

				int delay = 1000;
				Thread.sleep(delay);

				if(_shutdownMode == ABORT) break;
			}
		} catch (InterruptedException e) {
		}
	}

	private void saveData() 
	{
		Announcements _an = Announcements.getInstance();
		switch (_shutdownMode)
		{
			case SIGTERM:
				System.err.println("SIGTERM received. Shutting down NOW!");
				break;
			case GM_SHUTDOWN:
				System.err.println("GM shutdown received. Shutting down NOW!");
				break;
			case GM_RESTART:
				System.err.println("GM restart received. Restarting NOW!");
				break;

		}
		if (Config.ACTIVATE_POSITION_RECORDER)
			Universe.getInstance().implode(true);
		try
		{
		    _an.announceToAll("Server is " + MODE_TEXT[_shutdownMode] + " NOW!");
		} catch (Throwable t) {
			_log.log(Level.INFO, "", t);
		}

		disconnectAllCharacters();

        if (!SevenSigns.getInstance().isSealValidationPeriod())
            SevenSignsFestival.getInstance().saveFestivalData(false);

        SevenSigns.getInstance().saveSevenSignsData(null, true);
		Heroes.getInstance().saveHeroes();

        System.out.println("Constant heroes: All heroes has been saved");
        RaidBossSpawnManager.getInstance().cleanUp();
        System.err.println("RaidBossSpawnManager: All raid boss info saved");
		GrandBossManager.getInstance().cleanUp();
        System.err.println("GrandBossManager: All grand boss info saved");
        TradeController.getInstance().dataCountStore();
        System.err.println("TradeController: All count Item Saved");
        try
        {
            Olympiad.getInstance().save();
        }
        catch(Exception e){e.printStackTrace();}
        System.err.println("Olympiad System: Data saved");
        
        
        CursedWeaponsManager.getInstance().saveData();
        
        CastleManorManager.getInstance().save();
        
        QuestManager.getInstance().save();
        
        if(Config.SAVE_DROPPED_ITEM){
        ItemsOnGroundManager.getInstance().saveInDb();        
        ItemsOnGroundManager.getInstance().cleanUp();
        System.err.println("ItemsOnGroundManager: All items on ground saved");
        }
       
        if (Config.SAVE_BUFF_PROFILES)
        	CharSchemesTable.getInstance().onServerShutdown();
        
		System.err.println("Data saved. All players disconnected, shutting down");
		
		try {
			int delay = 5000;
			Thread.sleep(delay);
		} 
		catch (InterruptedException e) {
		}
	}

	@SuppressWarnings("deprecation")
	private void disconnectAllCharacters()
	{
		SystemMessage sysm = new SystemMessage(0);
		ServerClose ql = new ServerClose();
		Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();
		{
			for (L2PcInstance player : pls)
			{
				player.sendPacket(sysm);
				try
				{
					L2GameClient.saveCharToDisk(player);
					player.sendPacket(ql);
				}
				catch (Throwable t)
				{
				}
			}
		}
		try
		{
			Thread.sleep(1000);
		}
		catch (Throwable t)
		{
			_log.log(Level.INFO, "", t);
		}
		
		pls = L2World.getInstance().getAllPlayers();
		{
			for (L2PcInstance player : pls)
			{
				try
				{
					player.closeNetConnection(false);
				}
				catch (Throwable t)
				{
				}
			}
		}
	}

	public void MessageToAll(SystemMessage sm)
    {
        L2PcInstance player;
        for(Iterator<?> i$ = L2World.getInstance().getAllPlayers().iterator(); i$.hasNext(); player.sendPacket(sm))
            player = (L2PcInstance)i$.next();

    }

	public int get_seconds()
	{
		if(_counterInstance != null)
			return _counterInstance._secondsShut;
		return -1;
	}

}
