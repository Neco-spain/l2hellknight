package l2.hellknight.gameserver.instancemanager;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2.hellknight.Config;
import l2.hellknight.gameserver.Announcements;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.ItemList;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.util.Util;

public class LeaderboardTvT
{
	private static LeaderboardTvT _instance;
	public Logger _log = Logger.getLogger(LeaderboardArena.class.getName());
	public Map<Integer, TvTRank> _ranks = new FastMap<Integer, TvTRank>();
	protected Future<?> _actionTask = null;
	protected int SAVETASK_DELAY = Config.RANK_TVT_INTERVAL;
	protected Long nextTimeUpdateReward = 0L;
	
	public static LeaderboardTvT getInstance()
	{
		if (_instance == null)
			_instance = new LeaderboardTvT();
		
		return _instance;
	}
	
	public LeaderboardTvT()
	{
		engineInit();
	}
	
	public void onKill(int owner, String name)
	{
		TvTRank ar = null;
		if (_ranks.get(owner) == null)
			ar = new TvTRank();
		else
			ar = _ranks.get(owner);
		
		ar.pvp();
		ar.name = name;
		_ranks.put(owner, ar);
	}
	
	public void onDeath(int owner, String name)
	{
		TvTRank ar = null;
		if (_ranks.get(owner) == null)
			ar = new TvTRank();
		else
			ar = _ranks.get(owner);
		
		ar.death();
		ar.name = name;
		_ranks.put(owner, ar);
	}
	
	public void startTask()
	{
		if (_actionTask == null)
			_actionTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TvTTask(), 1000, SAVETASK_DELAY * 60000);
	}
	
	public void stopTask()
	{
		if (_actionTask != null)
			_actionTask.cancel(true);
		
		_actionTask = null;
	}
	
	public class TvTTask implements Runnable
	{
		public void run()
		{
			formRank();
			nextTimeUpdateReward = System.currentTimeMillis() + SAVETASK_DELAY * 60000;
		}
	}
	
	public void formRank()
	{
		Map<Integer, Integer> scores = new FastMap<Integer, Integer>();
		for (int obj : _ranks.keySet())
		{
			TvTRank ar = _ranks.get(obj);
			scores.put(obj, ar.kills - ar.death);
		}
		
		int Top = -1;
		int idTop = 0;
		for (int id : scores.keySet())
		{
			if (scores.get(id) > Top)
			{
				idTop = id;
				Top = scores.get(id);
			}
		}
		
		TvTRank arTop = _ranks.get(idTop);
		
		if (arTop == null)
		{
			Announcements.getInstance().announceToAll("TvTMaster: No winners at this time!");
			_ranks.clear();
			return;
		}
		
		L2PcInstance winner = (L2PcInstance) L2World.getInstance().findObject(idTop);
		
		Announcements.getInstance().announceToAll("TvTMaster: " + arTop.name + " is the winner for this time with " + arTop.kills + "/" + arTop.death + ". Next calculation in " + Config.RANK_TVT_INTERVAL + " min(s).");
		if (winner != null && Config.RANK_TVT_REWARD_ID > 0 && Config.RANK_TVT_REWARD_COUNT > 0)
		{
			winner.getInventory().addItem("TvTManager", Config.RANK_TVT_REWARD_ID, Config.RANK_TVT_REWARD_COUNT, winner, null);
			if (Config.RANK_TVT_REWARD_COUNT > 1) //You have earned $s1.
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(Config.RANK_TVT_REWARD_ID).addNumber(Config.RANK_TVT_REWARD_COUNT));
			else
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM).addItemName(Config.RANK_TVT_REWARD_ID));
			winner.sendPacket(new ItemList(winner, false));
		}
		_ranks.clear();
	}
	
	public String showHtm(int owner)
	{
		Map<Integer, Integer> scores = new FastMap<Integer, Integer>();
		for (int obj : _ranks.keySet())
		{
			TvTRank ar = _ranks.get(obj);
			scores.put(obj, ar.kills - ar.death);
		}
		
		scores = Util.sortMap(scores, false);
		
		int counter = 0, max = 20;
		String pt = "<html><body><center>" + "<font color=\"cc00ad\">TvT TOP " + max + " Players</font><br>";
		
		pt += "<table width=260 border=0 cellspacing=0 cellpadding=0 bgcolor=333333>";
		pt += "<tr> <td align=center>No.</td> <td align=center>Name</td> <td align=center>Kills</td> <td align=center>Deaths</td> </tr>";
		pt += "<tr> <td align=center>&nbsp;</td> <td align=center>&nbsp;</td> <td align=center></td> <td align=center></td> </tr>";
		boolean inTop = false;
		for (int id : scores.keySet())
		{
			if (counter < max)
			{
				TvTRank ar = _ranks.get(id);
				pt += tx(counter, ar.name, ar.kills, ar.death, id == owner);
				if (id == owner)
					inTop = true;
				
				counter++;
			}
			else
				break;
		}
		
		if (!inTop)
		{
			TvTRank arMe = _ranks.get(owner);
			if (arMe != null)
			{
				pt += "<tr> <td align=center>...</td> <td align=center>...</td> <td align=center>...</td> <td align=center>...</td> </tr>";
				int placeMe = 0;
				for (int idMe : scores.keySet())
				{
					placeMe++;
					if (idMe == owner)
						break;
				}
				pt += tx(placeMe, arMe.name, arMe.kills, arMe.death, true);
			}
		}
		
		pt += "</table>";
		pt += "<br><br>";
		if (Config.RANK_TVT_REWARD_ID > 0 && Config.RANK_TVT_REWARD_COUNT > 0)
		{
			pt += "Next Reward Time in <font color=\"LEVEL\">" + calcMinTo() + " min(s)</font><br1>";
			pt += "<font color=\"aadd77\">" + Config.RANK_TVT_REWARD_COUNT + " &#" + Config.RANK_TVT_REWARD_ID + ";</font>";
		}
		
		pt += "</center></body></html>";
		
		return pt;
	}
	
	private int calcMinTo()
	{
		return ((int) (nextTimeUpdateReward - System.currentTimeMillis())) / 60000;
	}
	
	private String tx(int counter, String name, int kills, int deaths, boolean mi)
	{
		String t = "";
		
		t += "	<tr>" + "<td align=center>" + (mi ? "<font color=\"LEVEL\">" : "") + (counter + 1) + ".</td>" + "<td align=center>" + name + "</td>"
				+ "<td align=center>" + kills + "</td>" + "<td align=center>" + deaths + "" + (mi ? "</font>" : "") + " </td>" + "</tr>";
		
		return t;
	}
	
	public void engineInit()
	{
		_ranks = new FastMap<Integer, TvTRank>();
		startTask();
	}
	
	public class TvTRank
	{
		public int		kills, death, classId;
		public String	name;
		
		public TvTRank()
		{
			kills = 0;
			death = 0;
		}
		
		public void pvp()
		{
			kills++;
		}
		
		public void death()
		{
			death++;
		}
	}
}