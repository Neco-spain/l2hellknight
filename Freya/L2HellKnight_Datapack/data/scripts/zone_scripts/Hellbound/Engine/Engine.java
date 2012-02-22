package zone_scripts.Hellbound.Engine;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import javolution.util.FastMap;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2.hellknight.Config;
import l2.hellknight.gameserver.Announcements;
import l2.hellknight.gameserver.datatables.DoorTable;
import l2.hellknight.gameserver.instancemanager.HellboundManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2DoorInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;

public class Engine extends Quest implements Runnable
{
	private static final int UPDATE_INTERVAL = 10000;

	private static final int[][] DOOR_LIST =
	{
		{ 19250001, 5 },
		{ 19250002, 5 },
		{ 20250001, 9 },
		{ 20250002, 7 }
	};

	private static final int[] MAX_TRUST =
	{
		0, 300000, 600000, 1000000, 1010000, 1400000, 1490000, 2000000, 2000001, 2500000, 4000000, 0
	};

	private static final String ANNOUNCE = "Hellbound now has reached level: %lvl%";
	
	private int _cachedLevel = -1;
	
	private static Map<Integer, PointsInfoHolder> pointsInfo = new FastMap<Integer, PointsInfoHolder>();

	//Holds info about points for mob killing
	private class PointsInfoHolder
	{
		protected int pointsAmount;
		protected int minHbLvl;
		protected int maxHbLvl;
		protected int lowestTrustLimit;
		
		protected PointsInfoHolder(int points, int min, int max, int trust)
		{
			pointsAmount = points;
			minHbLvl = min;
			maxHbLvl = max;
			lowestTrustLimit = trust;
		}
	}

	private final void onLevelChange(int newLevel)
	{
		try
		{
			HellboundManager.getInstance().setMaxTrust(MAX_TRUST[newLevel]);
			HellboundManager.getInstance().setMinTrust(MAX_TRUST[newLevel - 1]);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			HellboundManager.getInstance().setMaxTrust(0);
			HellboundManager.getInstance().setMinTrust(0);
		}
		HellboundManager.getInstance().updateTrust(0, false);
		HellboundManager.getInstance().doSpawn();
		
		for (int[] doorData : DOOR_LIST)
		{
			try
			{
				L2DoorInstance door = DoorTable.getInstance().getDoor(doorData[0]);
				if (door.getOpen())
				{
					if (newLevel < doorData[1])
						door.closeMe();
				}
				else
				{
					if (newLevel >= doorData[1])
						door.openMe();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if (_cachedLevel >= 0)
		{
			Announcements.getInstance().announceToAll(ANNOUNCE.replace("%lvl%", String.valueOf(newLevel)));
			_log.info("HellboundEngine: New Level: " + newLevel);
		}
		_cachedLevel = newLevel;
	}

	private void loadPointsInfoData()
	{
		String pointsInfoFile = "data/trust_points.xml";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT, pointsInfoFile);
		Document doc = null;
		
		if (file.exists())
		{
			try
			{
				doc = factory.newDocumentBuilder().parse(file);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not parse " + pointsInfoFile +" file: " + e.getMessage(), e);
			}

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("npc".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							Node att;

							att = attrs.getNamedItem("id");
							if (att == null)
							{
								_log.severe("[Hellbound Trust Points Info] Missing NPC ID, skipping record");
								continue;
							}

							int npcId = Integer.parseInt(att.getNodeValue());

							att = attrs.getNamedItem("points");
							if (att == null)
							{
								_log.severe("[Hellbound Trust Points Info] Missing reward point info for NPC ID " + npcId + ", skipping record");
								continue;
							}
							int points = Integer.parseInt(att.getNodeValue());

							att = attrs.getNamedItem("minHellboundLvl");
							if (att == null)
							{
								_log.severe("[Hellbound Trust Points Info] Missing minHellboundLvl info for NPC ID " + npcId + ", skipping record");
								continue;
							}
							int minHbLvl = Integer.parseInt(att.getNodeValue());

							att = attrs.getNamedItem("maxHellboundLvl");
							if (att == null)
							{
								_log.severe("[Hellbound Trust Points Info] Missing maxHellboundLvl info for NPC ID " + npcId + ", skipping record");
								continue;
							}
							int maxHbLvl = Integer.parseInt(att.getNodeValue());

							att = attrs.getNamedItem("lowestTrustLimit");
							int lowestTrustLimit = 0;
							if (att != null)
								lowestTrustLimit = Integer.parseInt(att.getNodeValue());
							
							pointsInfo.put(npcId, new PointsInfoHolder(points, minHbLvl, maxHbLvl, lowestTrustLimit));
						}
					}
				}
			}
		}
		else
			_log.warning("Can't locate points info file: " + pointsInfoFile);
		
		_log.info("HellboundEngine: Loaded: " + pointsInfo.size() + " trust point reward data");
	}

	public void run()
	{
		int level = HellboundManager.getInstance().getLevel();
		if (level == _cachedLevel)
		{
			if (HellboundManager.getInstance().getTrust() == HellboundManager.getInstance().getMaxTrust() && level != 4) //only exclusion is kill of Derek
			{
				level++;
				HellboundManager.getInstance().setLevel(level);
				onLevelChange(level);
			}
		}
		else
			onLevelChange(level);  // first run or changed by admin
	}
	
	//Let's try to manage all trust changes for killing here
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (pointsInfo.containsKey(npcId))
		{
			PointsInfoHolder npcInfo = pointsInfo.get(npcId);
			
			if (HellboundManager.getInstance().getLevel() >=  npcInfo.minHbLvl && HellboundManager.getInstance().getLevel() <=  npcInfo.maxHbLvl 
					&& (npcInfo.lowestTrustLimit == 0 || HellboundManager.getInstance().getTrust() > npcInfo.lowestTrustLimit))
			{
				HellboundManager.getInstance().updateTrust(npcInfo.pointsAmount, true);
			}
			
			if (npc.getNpcId() == 18465 && HellboundManager.getInstance().getLevel() == 4) //Derek
				HellboundManager.getInstance().setLevel(5); 
		} 

		return super.onKill(npc, killer, isPet);
	}


	public Engine(int questId, String name, String descr)
	{
		super(questId, name, descr);
		HellboundManager.getInstance().registerEngine(this, UPDATE_INTERVAL);
		loadPointsInfoData();
		
		//register onKill for all rewardable monsters
		for (int npcId : pointsInfo.keySet())
			addKillId(npcId);

		_log.info("HellboundEngine: Mode: levels 0-3");
		_log.info("HellboundEngine: Level: " + HellboundManager.getInstance().getLevel());
		_log.info("HellboundEngine: Trust: " + HellboundManager.getInstance().getTrust());
		if (HellboundManager.getInstance().isLocked())
			_log.info("HellboundEngine: State: locked");
		else
			_log.info("HellboundEngine: State: unlocked");
	}

	public static void main(String[] args)
	{
		new Engine(-1, Engine.class.getSimpleName(), "zone_scripts/Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Hellbound: Engine");
	}
}
