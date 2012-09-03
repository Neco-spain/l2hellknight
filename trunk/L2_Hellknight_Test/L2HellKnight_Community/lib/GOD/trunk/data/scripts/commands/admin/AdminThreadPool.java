package commands.admin;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.common.ThreadPoolManager.PriorityThreadFactory;
import l2rt.extensions.network.MMOConnection;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IAdminCommandHandler;
import l2rt.gameserver.model.L2Player;

public class AdminThreadPool implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_setcore,
		admin_setpool,
		admin_set_packet_pool
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().Menu)
			return false;

		if(wordList.length != 3)
		{
			activeChar.sendMessage("Incorrect arguments count");
			return false;
		}

		try
		{
			switch(command)
			{
				case admin_setcore:
					if(wordList[1].equals("f"))
					{
						ThreadPoolManager.getInstance().getPathfindScheduledThreadPool().setCorePoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New Pathfind theadpool core size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("g"))
					{
						ThreadPoolManager.getInstance().getGeneralScheduledThreadPool().setCorePoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New General theadpool core size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("n"))
					{
						ThreadPoolManager.getInstance().getNpcAiScheduledThreadPool().setCorePoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New NpcAI theadpool core size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("m"))
					{
						ThreadPoolManager.getInstance().getMoveScheduledThreadPool().setCorePoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New Move theadpool core size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("p"))
					{
						ThreadPoolManager.getInstance().getPlayerAiScheduledThreadPool().setCorePoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New PlayerAI theadpool core size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("p1"))
					{
						ThreadPoolManager.getInstance().getGeneralPacketsThreadPool().setCorePoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New GeneralPackets theadpool core size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("p2"))
					{
						ThreadPoolManager.getInstance().getIoPacketsThreadPool().setCorePoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New IoPackets theadpool core size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("p3"))
					{
						MMOConnection.getPool().setCorePoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New interest theadpool core size: " + Integer.parseInt(wordList[2]));
					}
					break;
				case admin_setpool:
					if(wordList[1].equals("f"))
					{
						ThreadPoolManager.getInstance().getPathfindScheduledThreadPool().setMaximumPoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New Pathfind theadpool query size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("g"))
					{
						ThreadPoolManager.getInstance().getGeneralScheduledThreadPool().setMaximumPoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New General theadpool query size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("n"))
					{
						ThreadPoolManager.getInstance().getNpcAiScheduledThreadPool().setMaximumPoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New NpcAI theadpool query size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("m"))
					{
						ThreadPoolManager.getInstance().getMoveScheduledThreadPool().setMaximumPoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New Move theadpool query size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("p"))
					{
						ThreadPoolManager.getInstance().getPlayerAiScheduledThreadPool().setMaximumPoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New PlayerAI theadpool query size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("p1"))
					{
						ThreadPoolManager.getInstance().getGeneralPacketsThreadPool().setMaximumPoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New GeneralPackets theadpool query size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("p2"))
					{
						ThreadPoolManager.getInstance().getIoPacketsThreadPool().setMaximumPoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New IoPackets theadpool query size: " + Integer.parseInt(wordList[2]));
					}
					else if(wordList[1].equals("p3"))
					{
						MMOConnection.getPool().setMaximumPoolSize(Integer.parseInt(wordList[2]));
						activeChar.sendMessage("New interest theadpool query size: " + wordList[2]);
					}
					break;
				case admin_set_packet_pool:
					ThreadPoolExecutor newPool;
					if("1".equals(wordList[1]))
					{
						newPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 15L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new PriorityThreadFactory("Normal Packet Pool", Thread.NORM_PRIORITY + 2));
						activeChar.sendMessage("Switching GeneralPackets ThreadPool to SynchronousQueue");
					}
					else if("2".equals(wordList[1]))
					{
						newPool = new ThreadPoolExecutor(Config.GENERAL_PACKET_THREAD_CORE_SIZE, Config.GENERAL_PACKET_THREAD_CORE_SIZE * 2, 120L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("Normal Packet Pool", Thread.NORM_PRIORITY + 2));
						activeChar.sendMessage("Switching GeneralPackets ThreadPool to LinkedBlockingQueue");
					}
					else
					{
						activeChar.sendMessage("USAGE: set_packet_pool 1|2 [forceshutdown]");
						return false;
					}
					ThreadPoolExecutor oldPool = ThreadPoolManager.getInstance()._generalPacketsThreadPool;
					ThreadPoolManager.getInstance()._generalPacketsThreadPool = newPool;

					oldPool.awaitTermination(1, TimeUnit.SECONDS);
					if("forceshutdown".equals(wordList[2]))
						oldPool.shutdownNow();
					else
						oldPool.shutdown();
					break;
			}
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Error!");
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}