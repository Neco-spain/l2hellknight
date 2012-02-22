package l2.hellknight.gameserver.instancemanager;

import java.util.Map;
import java.util.concurrent.Future;

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

public class LeaderboardFisherman
{
	private static LeaderboardFisherman _instance;
	public Map<Integer, FishRank> _ranks = new FastMap<Integer, FishRank>();
	protected Future<?> _actionTask = null;
	protected int TASK_DELAY = Config.RANK_FISHERMAN_INTERVAL;
	protected Long nextTimeUpdateReward = 0L;
	
	public static LeaderboardFisherman getInstance()
	{
		if (_instance == null)
			_instance = new LeaderboardFisherman();
		
		return _instance;
	}
	
	public LeaderboardFisherman()
	{
		engineInit();
	}	
	
	public void onCatch(int owner, String name)
	{
		FishRank ar = null;
		if (_ranks.get(owner) == null)
			ar = new FishRank();
		else
			ar = _ranks.get(owner);
		
		ar.cought();
		ar.name = name;
		_ranks.put(owner, ar);
	}
	
	public void onEscape(int owner, String name)
	{
		FishRank ar = null;
		if (_ranks.get(owner) == null)
			ar = new FishRank();
		else
			ar = _ranks.get(owner);
		
		ar.escaped();
		ar.name = name;
		_ranks.put(owner, ar);
	}
	
	public void stopTask()
	{
		if (_actionTask != null)
			_actionTask.cancel(true);
		
		_actionTask = null;
	}
	
	public class FishermanTask implements Runnable
	{
		public void run()
		{
			formRank();
			nextTimeUpdateReward = System.currentTimeMillis() + TASK_DELAY * 60000;
		}
	}
	
	public void startTask()
	{
		if (_actionTask == null)
			_actionTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FishermanTask(), 1000, TASK_DELAY * 60000);
	}
	
	public void formRank()
	{
		Map<Integer, Integer> scores = new FastMap<Integer, Integer>();
		for (int obj : _ranks.keySet())
		{
			FishRank ar = _ranks.get(obj);
			scores.put(obj, ar.cought - ar.escaped);
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
		
		FishRank arTop = _ranks.get(idTop);
		
		if (arTop == null)
		{
			Announcements.getInstance().announceToAll("Fisherman: No winners at this time!");
			_ranks.clear();
			return;
		}
		
		L2PcInstance winner = (L2PcInstance) L2World.getInstance().findObject(idTop);
		
		Announcements.getInstance().announceToAll("Attention Fishermans: " + arTop.name + " is the winner for this time with " + arTop.cought + "/" + arTop.escaped + ". Next calculation in " + Config.RANK_FISHERMAN_INTERVAL + " min(s).");
		if (winner != null && Config.RANK_FISHERMAN_REWARD_ID > 0 && Config.RANK_FISHERMAN_REWARD_COUNT > 0)
		{
			winner.getInventory().addItem("FishManager", Config.RANK_FISHERMAN_REWARD_ID, Config.RANK_FISHERMAN_REWARD_COUNT, winner, null);
			if (Config.RANK_FISHERMAN_REWARD_COUNT > 1) //You have earned $s1.
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(Config.RANK_FISHERMAN_REWARD_ID).addNumber(
						Config.RANK_FISHERMAN_REWARD_COUNT));
			else
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM).addItemName(Config.RANK_FISHERMAN_REWARD_ID));
			winner.sendPacket(new ItemList(winner, false));
		}
		_ranks.clear();
	}
	
	public String showHtm(int owner)
	{
		Map<Integer, Integer> scores = new FastMap<Integer, Integer>();
		for (int obj : _ranks.keySet())
		{
			FishRank ar = _ranks.get(obj);
			scores.put(obj, ar.cought - ar.escaped);
		}
		
		scores = Util.sortMap(scores, false);
		
		int counter = 0, max = 20;
		String pt = "<html><body><center>" + "<font color=\"cc00ad\">TOP " + max + " Fisherman</font><br>";
		
		pt += "<table width=260 border=0 cellspacing=0 cellpadding=0 bgcolor=333333>";
		pt += "<tr> <td align=center>No.</td> <td align=center>Name</td> <td align=center>Cought</td> <td align=center>Escaped</td> </tr>";
		pt += "<tr> <td align=center>&nbsp;</td> <td align=center>&nbsp;</td> <td align=center></td> <td align=center></td> </tr>";
		boolean inTop = false;
		for (int id : scores.keySet())
		{
			if (counter < max)
			{
				FishRank ar = _ranks.get(id);
				pt += tx(counter, ar.name, ar.cought, ar.escaped, id == owner);
				if (id == owner)
					inTop = true;
				
				counter++;
			}
			else
				break;
		}
		
		if (!inTop)
		{
			FishRank arMe = _ranks.get(owner);
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
				pt += tx(placeMe, arMe.name, arMe.cought, arMe.escaped, true);
			}
		}
		
		pt += "</table>";
		pt += "<br><br>";
		if (Config.RANK_FISHERMAN_REWARD_ID > 0 && Config.RANK_FISHERMAN_REWARD_COUNT > 0)
		{
			pt += "Next Reward Time in <font color=\"LEVEL\">" + calcMinTo() + " min(s)</font><br1>";
			pt += "<font color=\"aadd77\">" + Config.RANK_FISHERMAN_REWARD_COUNT + " &#" + Config.RANK_FISHERMAN_REWARD_ID + ";</font>";
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
		_ranks = new FastMap<Integer, FishRank>();
		startTask();
	}
	
	public class FishRank
	{
		public int		cought, escaped;
		public String	name;
		
		public FishRank()
		{
			cought = 0;
			escaped = 0;
		}
		
		public void cought()
		{
			cought++;
		}
		
		public void escaped()
		{
			escaped++;
		}
	}
}