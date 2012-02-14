package bosses;

import javolution.util.FastMap;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.listeners.L2ZoneEnterLeaveListener;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.instancemanager.ServerVariables;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Rnd;

import java.util.Calendar;

/**
 * Manager for location Seed Of Annihilation
 * @author Drizzy
 * @date 15.12.10
 */
 
public class SeedOfAnnihilationManager extends Functions implements ScriptFile
{
	// Buffs
	private static final int[] ZONE_BUFFS = { 0, 6443, 6444, 6442 };
	private static final int[][] ZONE_BUFFS_LIST = { {1,2,3},{1,3,2},{2,1,3},{2,3,1},{3,2,1},{3,1,2} };
	// Region = 0: Bistakon, 1: Reptilikon, 2: Cokrakon
	private static SeedRegion[] _regionsData = new SeedRegion[3];
	// Timer
	private Long _seedsNextStatusChange;
	// Npc
	private static final int ANNIHILATION_FURNACE = 18928;	
	// Other
	private static final FastMap<Integer, int[]> _teleportZones = new FastMap<Integer, int[]>();
	private static L2Zone _zone;
	private ZoneListener _zoneListener = new ZoneListener();
	//Seed
	private static final int Seeds[] = { 18678, 18679, 18680, 18681, 18682, 18683 };
    private static GArray<L2Spawn> _SeedSpawnList = new GArray<L2Spawn>();

	private void init()
	{
		loadSeed(); //Load Region
		loadEffectZone(); //Load Zone Buffs
		for(int i : _teleportZones.keySet()) // add zone teleport
		{
			_zone = ZoneManager.getInstance().getZoneById(ZoneType.other, i, true);
			_zone.deleteSkill();	// Delete old skill for zone.			
			_zone.getListenerEngine().addMethodInvokedListener(_zoneListener);		
		}
		addSpawnsToList(); //Load Spawn Seed
		// Spawn Seeds.
		for(L2Spawn spawn : getSeedSpawnList())
		{
			L2NpcInstance mob = spawn.doSpawn(true);
			mob.getSpawn().startRespawn();
		}
		System.out.println("SeedOfAnnihilationManager: Load Manager.");
        System.out.println("SeedOfAnnihilationManager: Loaded " + _SeedSpawnList.size() + " Seed Spawn Locations.");
	}
	
	// class for Region.
	private static class SeedRegion
	{
		public int[] elite_mob_ids;
		public int buff_zone;
		public int[][] af_spawns;
		public L2NpcInstance[] af_npcs = new L2NpcInstance[2];
		public int activeBuff = 0;
		
		public SeedRegion(int[] emi, int bz, int[][] as)
		{
			elite_mob_ids = emi;
			buff_zone = bz;
			af_spawns = as;
		}
	}	

	static
	{
		_teleportZones.put(60002, new int[]{ -213175, 182648, -11020 });
		_teleportZones.put(60003, new int[]{ -181217, 186711, -10562 });
		_teleportZones.put(60004, new int[]{ -180211, 182984, -15186 });
		_teleportZones.put(60005, new int[]{ -179275, 186802, -10748 });
	}

	public void loadSeed() // Load Region
	{
		// Bistakon data
		_regionsData[0] = new SeedRegion(new int[]{ 22750, 22751, 22752, 22753 },
				60006, new int[][]{ {-180450,185507,-10544,11632},{-180005,185489,-10544,11632} });
		
		// Reptilikon data
		_regionsData[1] = new SeedRegion(new int[]{ 22757, 22758, 22759 },
				60007, new int[][]{ {-179600,186998,-10704,11632},{-179295,186444,-10704,11632} });
		
		// Cokrakon data
		_regionsData[2] = new SeedRegion(new int[]{ 22763, 22764, 22765 },
				60008, new int[][]{ {-180971,186361,-10528,11632},{-180758,186739,-10528,11632} });
		int buffsNow = 0;
		//Get change timer
		String var = ServerVariables.getString("SeedNextStatusChange", "");
		if (var.equalsIgnoreCase("") || Long.parseLong(var) < System.currentTimeMillis())
		{
			buffsNow = Rnd.get(ZONE_BUFFS_LIST.length);
			ServerVariables.set("SeedBuffsList", String.valueOf(buffsNow));
			_seedsNextStatusChange = getNextSeedsStatusChangeTime();
			ServerVariables.set("SeedNextStatusChange", String.valueOf(_seedsNextStatusChange));
		}
		else
		{
			_seedsNextStatusChange = Long.parseLong(var);
			buffsNow = Integer.parseInt(ServerVariables.getString("SeedBuffsList"));
		}
		for(int i = 0; i < _regionsData.length; i++)
			_regionsData[i].activeBuff = ZONE_BUFFS_LIST[buffsNow][i];
	}

	//Get time for change region buffs.
	private Long getNextSeedsStatusChangeTime()
	{
		Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.SECOND, 0);
		reenter.set(Calendar.MINUTE, 0);
		reenter.set(Calendar.HOUR_OF_DAY, 13);
		reenter.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		if (reenter.getTimeInMillis() <= System.currentTimeMillis())
			reenter.add(Calendar.DAY_OF_MONTH, 7);
		return reenter.getTimeInMillis();
	}	
	
	//Apply effect to zone
	private void loadEffectZone()
	{
		for(int i = 0; i < _regionsData.length; i++)
		{
			for(int j = 0; j < _regionsData[i].af_spawns.length; j++)
			{
                _regionsData[i].af_npcs[j] = spawn(new Location(_regionsData[i].af_spawns[j][0], _regionsData[i].af_spawns[j][1], _regionsData[i].af_spawns[j][2], _regionsData[i].af_spawns[j][3]),  ANNIHILATION_FURNACE);
                _regionsData[i].af_npcs[j].setNpcState(_regionsData[i].activeBuff);
			}
			L2Skill skill = SkillTable.getInstance().getInfo(ZONE_BUFFS[_regionsData[i].activeBuff], 1);
			_zone = ZoneManager.getInstance().getZoneById(ZoneType.other, _regionsData[i].buff_zone, true);
			_zone.deleteSkill();
			_zone.setSkill(skill);
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new ChangeSeedsStatus(), _seedsNextStatusChange - System.currentTimeMillis());
	}

	public class ZoneListener extends L2ZoneEnterLeaveListener
	{
		public ZoneListener()
		{}

		public void objectEntered(L2Zone zone, L2Object object)
		{
			teleportTo((L2Player) object, zone);		
		}

		public void objectLeaved(L2Zone zone, L2Object object)
		{}
	}

	public static void teleportTo(L2Player cha, L2Zone zone)
	{
		if (cha != null)
		{
			if (_teleportZones.containsKey(zone.getId()))
			{
				int[] teleLoc = _teleportZones.get(zone.getId());		
				cha.teleToLocation(teleLoc[0],teleLoc[1],teleLoc[2]);
			}
		}
	}
	
	private class ChangeSeedsStatus implements Runnable
	{
		public void run()
		{
			int buffsNow = Rnd.get(ZONE_BUFFS_LIST.length);
			ServerVariables.set("SeedBuffsList", String.valueOf(buffsNow));
			_seedsNextStatusChange = getNextSeedsStatusChangeTime();
			ServerVariables.set("SeedNextStatusChange", String.valueOf(_seedsNextStatusChange));
			for(int i = 0; i < _regionsData.length; i++)
			{
				_regionsData[i].activeBuff = ZONE_BUFFS_LIST[buffsNow][i];
				
				for(L2NpcInstance af : _regionsData[i].af_npcs)
					af.setNpcState(_regionsData[i].activeBuff);
				
				L2Zone zone = ZoneManager.getInstance().getZoneById(ZoneType.other, _regionsData[i].buff_zone, true);	
				L2Skill skill = SkillTable.getInstance().getInfo(ZONE_BUFFS[_regionsData[i].activeBuff], 1);
				zone.deleteSkill();
				zone.setSkill(skill);
			}
			ThreadPoolManager.getInstance().scheduleGeneral(new ChangeSeedsStatus(), _seedsNextStatusChange - System.currentTimeMillis());
		}
	}
		
	public void transform(L2NpcInstance npc)
	{
		L2Player player = (L2Player) getSelf(); 
		if(player == null)
			return;
			
		if (player.getEffectList().getEffectsBySkillId(6408) != null)
			npc.showChatWindow(player, 2);
		else
		{
			npc.setTarget(player);
			SkillTable.getInstance().getInfo(6408, 1).getEffects(player, player, false, false);
			SkillTable.getInstance().getInfo(6649, 1).getEffects(player, player, false, false);
			npc.showChatWindow(player, 1);
		}
	}

    public void SpawnSeed(Location loc, int npcId, int resp, int resprnd)
    {
        try
        {
            int npcTemplateId;
            L2Spawn spawnDat;
            L2NpcTemplate npcTemplate;

            npcTemplateId = npcId;
            npcTemplate = NpcTable.getTemplate(npcTemplateId);
            if(npcTemplate != null)
            {
                spawnDat = new L2Spawn(npcTemplate);
                spawnDat.setAmount(1);
                spawnDat.setLoc(loc);
                spawnDat.setRespawnDelay(resp, resprnd);
                _SeedSpawnList.add(spawnDat);
            }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }

	public static GArray<L2Spawn> getSeedSpawnList()
	{
		return _SeedSpawnList;
	}

	//spawn Energy Seed
	public void addSpawnsToList()
	{
		// Seed of Annihilation
		SpawnSeed(new Location(-184519,183007,-10456), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184873,181445,-10488), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184009,180962,-10488), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185321,181641,-10448), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184035,182775,-10512), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185433,181935,-10424), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-183309,183007,-10560), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184929,181886,-10488), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184009,180392,-10424), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-183793,183239,-10488), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184245,180848,-10464), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-182704,183761,-10528), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184705,181886,-10504), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184304,181076,-10488), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-183596,180430,-10424), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184422,181038,-10480), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184929,181543,-10496), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184398,182891,-10472), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-177606,182848,-10584), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-178104,183224,-10560), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-177274,182284,-10600), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-177772,183224,-10560), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-181532,180364,-10504), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-181802,180276,-10496), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-178429,180444,-10512), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-177606,182190,-10600), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-177357,181908,-10576), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-178747,179534,-10408), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-178429,179534,-10392), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-178853,180094,-10472), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-181937,179660,-10416), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-180992,179572,-10416), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185552,179252,-10368), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184572,178913,-10400), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184768,178348,-10312), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184572,178574,-10352), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185062,178913,-10384), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-181397,179484,-10416), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-181667,179044,-10408), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185258,177896,-10240), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-183506,176570,-10280), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-183719,176804,-10240), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-183648,177116,-10240), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-183932,176492,-10240), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-183861,176570,-10240), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-183790,175946,-10240), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-178641,179604,-10416), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-178959,179814,-10432), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-176367,178456,-10376), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-175845,177172,-10264), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-175323,177600,-10248), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-174975,177172,-10216), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-176019,178242,-10352), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-174801,178456,-10264), Seeds[Rnd.get(Seeds.length)], 480, 180);
		
		SpawnSeed(new Location(-185648,183384,-15680), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-186740,180908,-15528), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185297,184658,-15680), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185697,181601,-15488), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-186684,182744,-15536), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184908,183384,-15616), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184994,185572,-15784), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185796,182616,-15608), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184970,184385,-15648), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185995,180809,-15512), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185352,182872,-15632), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185624,184294,-15680), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184486,185774,-15816), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-186496,184112,-15680), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184232,185976,-15816), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184994,185673,-15792), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185733,184203,-15680), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185079,184294,-15680), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184803,180710,-15528), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-186293,180413,-15528), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185352,182936,-15632), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184356,180611,-15496), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185375,186784,-15816), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184867,186784,-15816), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-180553,180454,-15152), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-180422,180454,-15152), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-181863,181138,-15120), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-181732,180454,-15152), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-180684,180397,-15152), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-182256,180682,-15112), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185492,179492,-15392), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185894,178538,-15336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-186028,178856,-15336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185224,179068,-15336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185492,178538,-15336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185894,178538,-15336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-180619,178855,-15152), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-180255,177892,-15152), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-185804,176472,-15336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184580,176370,-15320), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184308,176166,-15320), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-183764,177186,-15304), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-180801,177571,-15144), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184716,176064,-15320), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-184444,175452,-15296), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-180164,177464,-15152), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-180164,178213,-15152), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-179982,178320,-15152), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-176925,177757,-15824), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-176164,179282,-15720), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-175692,177613,-15800), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-175418,178117,-15824), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-176103,177829,-15824), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-175966,177325,-15792), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-174778,179732,-15664), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-175692,178261,-15824), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-176038,179192,-15736), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-175660,179462,-15680), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-175912,179732,-15664), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-175156,180182,-15680), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-174240,182059,-15664), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-175590,181478,-15640), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-174510,181561,-15616), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-174240,182391,-15688), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-174105,182806,-15672), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-174645,182806,-15712), Seeds[Rnd.get(Seeds.length)], 480, 180);
		
		SpawnSeed(new Location(-214962,182403,-10992), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-215019,182493,-11000), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-211374,180793,-11672), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-211198,180661,-11680), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-213097,178936,-12720), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-213517,178936,-12712), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-214105,179191,-12720), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-213769,179446,-12720), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-214021,179344,-12720), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-210582,180595,-11672), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-210934,180661,-11696), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-207058,178460,-12656), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-207454,179151,-11368), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-207422,181365,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-207358,180627,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-207230,180996,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-208515,184160,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-207613,184000,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-208597,183760,-11352), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206710,176142,-12656), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206361,178136,-11336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206178,178630,-12672), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-205738,178715,-12656), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206442,178205,-12648), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206585,178874,-11336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206073,179366,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206009,178628,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206155,181301,-12656), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206595,181641,-12656), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206507,181641,-12656), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206507,181471,-12640), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206974,175972,-12672), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206304,175130,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206886,175802,-12672), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-207238,175972,-12672), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206386,174857,-11328), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-206386,175039,-11336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-205976,174584,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-207367,184320,-11336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219002,180419,-12608), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-218853,182790,-12592), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-218853,183343,-12600), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-218358,186247,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-218358,186083,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-217574,185796,-11352), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219178,181051,-12592), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-220171,180313,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219293,183738,-12592), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219381,182553,-12584), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219600,183024,-11336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219940,182680,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219260,183884,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219855,183540,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-218946,186575,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219882,180103,-12592), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219266,179787,-12584), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219201,178337,-12592), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219716,179875,-11336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219716,180021,-11328), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219989,179437,-11336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219078,178298,-12592), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-218684,178954,-11328), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219089,178456,-11328), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-220266,177623,-12608), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219201,178025,-12592), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219142,177044,-12592), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219690,177895,-12592), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219754,177623,-12584), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-218791,177830,-12584), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-218904,176219,-12592), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-218768,176384,-12584), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-218774,177626,-11320), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-218774,177792,-11328), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219880,175901,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219210,176054,-12592), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219850,175991,-12608), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-219079,175021,-11336), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-218812,174229,-11344), Seeds[Rnd.get(Seeds.length)], 480, 180);
		SpawnSeed(new Location(-218723,174669,-11336), Seeds[Rnd.get(Seeds.length)], 480, 180);
	}

	public void onLoad()
	{
		init();
	}

	public void onReload()
	{}

	public void onShutdown()
	{}		
}
