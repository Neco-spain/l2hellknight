package bosses;

import java.util.concurrent.ScheduledFuture;
import java.util.HashMap;
import javolution.util.FastMap;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.listeners.L2ZoneEnterLeaveListener;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.InstancedZoneManager;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.instancemanager.InstancedZoneManager.InstancedZone;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2CommandChannel;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.ReflectionTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.taskmanager.DecayTaskManager;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class FreyaManager extends Functions implements ScriptFile
{
	//Дебаг true\false
    private static boolean debug = true;
	
	//Параметры FreyaInstanceInfo
	public static HashMap<Long, FreyaInstanceInfo> instances = new HashMap<Long, FreyaInstanceInfo>();
	
	public static class FreyaInstanceInfo
	{
	    //Парметры NPC: id
		//Координаты расстановки мобов...
    	public static final int ICE_CASTLE_CONTROLLER = 18932;
    	public static final int FREYA_THRONE = 29177;
    	public static final int FREYA_SPELLING = 29178;
    	public static final int FREYA_STAND = 29179;
    	public static final int GLACIER = 18853;
    	public static final int GLAKIAS = 25699;
    	public static final int ARCHERY_KNIGHT = 18855;
    	public static final int ARCHERY_BREATH = 18854;
    	public static final int KEGOR = 18846;
    	public static final int JINIA = 18850;
    	public static final int FREYA_CASTLE_DOOR = 23140101;
    	public static final int TIMER_DESTROY_TASK = 115 * 60000; //Время очистки парметров ?? Нужно ли вообще ??
		
    	public static Location castle_controller_spawn = new Location(114707, -114797, -11199, 0, ICE_CASTLE_CONTROLLER); //Контроллер для эффекта замарозки экрана...
    	public static Location freyaSpawn = new Location(114720, -117068, -11078, 16384, FREYA_THRONE); //1 стадия
    	public static Location freyaSpelling = new Location(114723, -117502, -10672, 15956, FREYA_SPELLING); //2 стадия
    	public static Location freyaStand = new Location(114720, -117068, -11078, 16384, FREYA_STAND); //3 стадия
    	public static Location freyaDeadSpawn = new Location(114712,-114808,-11230, 0, FREYA_STAND); //4 стадия
    	public static Location ice_knight_leader = new Location(114707, -114799, -11199, 15956, GLAKIAS); //2 стадия
    	public static Location kegorSt3Spawn = new Location(114690, -114700, -11200, 0, KEGOR); //3 стадия
    	public static Location kegorSt4Spawn = new Location(114584,-114824,-11230,0, KEGOR); //4 стадия
    	public static Location jiniaSt3Spawn = new Location(114727, -114700, -11200, 0, JINIA); //3 стадия
    	public static final Location TRR_FREYA_1F = new Location(114712, -114808, -11229); //Координаты спауна гласиеров(Центр комнаты...) расчёт радиуса от центра в методе loadSpawn
	
    	private static int _intervalOfBlizzards = 300000;
		
    	public static Location[] first_spawn = { new Location(114713, -115109, -11202, 16456, ARCHERY_KNIGHT),
                                                  new Location(114008, -115080, -11202, 3568, ARCHERY_KNIGHT),
                                                  new Location(114422, -115508, -11202, 12400, ARCHERY_KNIGHT),
                                                  new Location(115023, -115508, -11202, 20016, ARCHERY_KNIGHT),
                                                  new Location(115459, -115079, -11202, 27936, ARCHERY_KNIGHT) };
											  
    	public static Location[] maker_ice_knight = {  new Location(113845, -116091, -11168, 8264, ARCHERY_KNIGHT), //[my_position]=6
                                                 	    new Location(113381, -115622, -11168, 8264, ARCHERY_KNIGHT), //[my_position]=7}	npc_ex_end	
                                                 	    new Location(113380, -113978, -11168, -8224, ARCHERY_KNIGHT), //[my_position]=8}	npc_ex_end	
                                                 	    new Location(113845, -113518, -11168, -8224, ARCHERY_KNIGHT), //[my_position]=9}	npc_ex_end	
                                                 	    new Location(115591, -113516, -11168, -24504, ARCHERY_KNIGHT), //[my_position]=10}	npc_ex_end	
                                                 	    new Location(116053, -113981, -11168, -24504, ARCHERY_KNIGHT), //[my_position]=11}	npc_ex_end	
                                                 	    new Location(116061, -115611, -11168, 24804, ARCHERY_KNIGHT), //[my_position]=12}	npc_ex_end	
                                                 	    new Location(115597, -116080, -11168, 24804, ARCHERY_KNIGHT), //[my_position]=13}	npc_ex_end	
                                                 	    new Location(112942, -115480, -10960, 52, ARCHERY_KNIGHT), //[my_position]=14, [SuperPointName]=[IceQueen_2F_Left], [PosX]=113078, [PosY]=-115480, [PosZ]=-10984
                                                 	    new Location(112940, -115146, -10960, 52, ARCHERY_KNIGHT), //[my_position]=15, [SuperPointName]=[IceQueen_2F_Left], [PosX]=113079, [PosY]=-115154, [PosZ]=-10984
                                                 	    new Location(112945, -114453, -10960, 52, ARCHERY_KNIGHT), //[my_position]=16, [SuperPointName]=[IceQueen_2F_Left], [PosX]=113081, [PosY]=-114459, [PosZ]=-10984
                                                 	    new Location(112945, -114123, -10960, 52, ARCHERY_KNIGHT), //[my_position]=17, [SuperPointName]=[IceQueen_2F_Left], [PosX]=113077, [PosY]=-114129, [PosZ]=-10984
                                                 	    new Location(116497, -114117, -10960, 32724, ARCHERY_KNIGHT), //[my_position]=18, [SuperPointName]=[IceQueen_2F_Right], [PosX]=116360, [PosY]=-114125, [PosZ]=-10984
                                                 	    new Location(116499, -114454, -10960, 32724, ARCHERY_KNIGHT), //[my_position]=19, [SuperPointName]=[IceQueen_2F_Right], [PosX]=116359, [PosY]=-114455, [PosZ]=-10984
                                                 	    new Location(116501, -115145, -10960, 32724, ARCHERY_KNIGHT), //[my_position]=20, [SuperPointName]=[IceQueen_2F_Right], [PosX]=116367, [PosY]=-115141, [PosZ]=-10984
                                                 	    new Location(116502, -115473, -10960, 32724, ARCHERY_KNIGHT) };//[my_position]=21, [SuperPointName]=[IceQueen_2F_Right], [PosX]=116351, [PosY]=-115457, [PosZ]=-10984
	
	    //Параметры
		public long instanceId;
		public int status;
		public int state;
		public long i1 = 0;
		public int i1c = 0;
		public long i2 = 0;
		public int knights_kill_count = 0;
		public boolean is_hard_mode;
		public L2NpcInstance castle_controller;
		public L2NpcInstance freya;
		public L2NpcInstance knight_leader;
		public L2NpcInstance kegor;
		public L2NpcInstance jinia;
		public L2NpcInstance glacier;
		public L2NpcInstance ice_knight;
		public L2NpcInstance ice_breath;
		GArray<L2NpcInstance> active_monsters = new GArray<L2NpcInstance>();
		GArray<L2NpcInstance> ice_knights = new GArray<L2NpcInstance>();
		GArray<L2NpcInstance> glaciers = new GArray<L2NpcInstance>();
		FastMap<Integer, Location> ice_knights_spawn = new FastMap<Integer, Location>();
		public ScheduledFuture<?> wavesTimer, TimeEndTask, BlizzardTimer;
		
		public L2NpcInstance spawn(Location loc, int npcId)
		{
	    	L2NpcTemplate template = NpcTable.getTemplate(npcId);
	    	L2NpcInstance npc = template.getNewInstance();
	    	npc.setSpawnedLoc(loc);
	    	npc.setReflection(instanceId);
	    	npc.onSpawn();
	    	npc.setHeading(loc.h);
	    	npc.setXYZInvisible(loc);
	    	npc.spawnMe();
	    	return npc;
		}
		
		public L2NpcInstance spawn(Location loc)
		{
	    	return spawn(new Location(loc.x, loc.y, loc.z, loc.h), loc.id);
		}
	    	
		//Подгрузка спауна по иду...
		public void loadSpawn(int loadId)
		{
    		switch(loadId)
			{
	    		case 1: //Мобы в центре комнаты
	    		    for(int i = 0; i < first_spawn.length; i++)
	    			{
	    				L2NpcInstance ice_k = spawn(first_spawn[i]);
	         			events(ice_k, 1, 5000);
	    				active_monsters.add(ice_k);
	         			loadSpawn(2);
	    			}
	    			break;
	    		case 2: //Мобы по окружности комнаты
				    for(int i = 0; i < maker_ice_knight.length; i++)
					{
						ice_knight = spawn(maker_ice_knight[i]);
						ice_knights.add(ice_knight);
						ice_knights_spawn.put(ice_knight.getObjectId(), ice_knight.getSpawnedLoc());
						
						//Время респа кнайтов в зависимости от стадии фреи
						if(is_hard_mode)
						{
				    		switch(status)
							{
	    						case 1:// STATE 1
	     							i1 = 25000;
	     							i1c = 4;
	            					break;
	    						case 2:// STATE 2
	     							i1 = 25000;
	     							i1c = 4;
	            					break;
	    						case 3:// STATE 3
	     							i1 = 20000;
	     							i1c = 4;
	            					break;
	    						case 4:// STATE 4
	     							i1 = 20000;
	     							i1c = 4;
	            					break;
							}
						}
						else
						{
				    		switch(status)
							{
	    						case 1:// STATE 1
	     							i1 = 30000;
	     							i1c = 2;
	     							break;
	    						case 2:// STATE 2
	     							i1 = 30000;
	     							i1c = 2;
	     							break;
	    						case 3:// STATE 3
	     							i1 = 45000;
	     							i1c = 2;
	     							break;
	    						case 4:// STATE 4
	     							i1 = 30000;
	     							i1c = 2;
	     							break;
							}
						}
	     				//events(ice_knight, 1, Rnd.get(20000, 120000));
	     				events(ice_knight, 1, Rnd.get(i1 * i1c, 120000));
					}
	    			break;
	    		case 3: //Гласиеры
	    			if(glaciers.size() < 7)
	    			{
		    		    //Нужна корректировка Проверить радиус отклонения точки от центра
		    	        Location spawnedLoc = Rnd.coordsRandomize(TRR_FREYA_1F, 200, 800);
		    			glacier = spawn(spawnedLoc, GLACIER);
		    	        glacier.setNpcState(1);
		    	        events(glacier, 4, 2000);
		    	        glacier.setOverloaded(true);
		    	        glaciers.add(glacier);
		    	        castle_controller.setNpcState(glaciers.size());
						
						//Время респа гласиеров в зависимости от стадии фреи
		    	        if(is_hard_mode)
		    	        {
			        		switch(status)
			        		{
	    	    				case 1:// STATE 1
	     			    			i2 = 25;
	     			    			break;
	    	    				case 2:// STATE 2
	     			    			i2 = 20;
	     			    			break;
	    	    				case 3:// STATE 3
	     			    			i2 = 20;
	     			    			break;
	    	    				case 4:// STATE 4
	     			    			i2 = 20;
	     			    			break;
			        		}
		    	        }
		    	        else
		    	        {
			        		switch(status)
			        		{
	    	    				case 1:// STATE 1
	     			    			i2 = 20;
	     			    			break;
	    	    				case 2:// STATE 2
	     			    			i2 = 30;
	     			    			break;
	    	    				case 3:// STATE 3
	     			    			i2 = 30;
	     			    			break;
	    	    				case 4:// STATE 4
	     			    			i2 = 0;
	     			    			break;
			        		}
		    	        }
		    			events(glacier, 3, i2 * 1000);
	    			}
	    			break;
			}
		}
	
		public void events(final L2NpcInstance npc, int eventId, long timer)
		{
    		switch(eventId)
			{
	    		case 1: //Смена NpcState кнайтов
	    			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() 
	    			{
		    			@Override
		     			public void run() 
		    			{
		    				if(npc.isImobilised())
		    				{
		    					npc.setNpcState(2);
		    					npc.setImobilised(false);
		    				}
	    	    			getRandomHate(npc);
			    			events(npc, 2, 1500);
		    			}
		    		}, timer);
    				break;
	    		case 2: //Смена NpcState кнайтов на 3 стейт для полной разморозки...
	    			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() 
	    			{
		    			@Override
		     			public void run() 
		    			{
	        				npc.setNpcState(3);
	        				events(ice_knight, 5, 2000);
		    			}
		    		}, timer);
    				break;
	    		case 3: //Респаун гласиеров
	    			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() 
	    			{
		    			@Override
		     			public void run() 
		    			{
		    				loadSpawn(3);
		    			}
		    		}, timer);
    				break;
	    		case 4: //Смена NpcState Кастл контроллера
	    			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() 
	    			{
		    			@Override
		     			public void run() 
		    			{
	    	    			npc.setNpcState(2);
	        				castle_controller.setNpcState(glaciers.size());
	        				for(L2Player player : getReflection().getPlayers())
		                    	SkillTable.getInstance().getInfo(6437, glaciers.size()).getEffects(npc, player, false, false);
		    			}
		    		}, timer);
    				break;
	    		case 5: //Респаун кнайтов по окружности...
	    			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() 
	    			{
		    			@Override
		     			public void run() 
		    			{
	                		Location loc = ice_knights_spawn.get(npc.getObjectId());						
	                		ice_knight = spawn(loc, ARCHERY_KNIGHT);
	                		getRandomHate(ice_knight);
	                		ice_knight.setNpcState(1);
	                		ice_knight.setImobilised(true);
	                		ice_knights.add(ice_knight);
    	     				events(ice_knight, 1, Rnd.get(i1, 120000));
		    			}
		    		}, timer);
    				break;
	    		case 6: //Меняем эффект замка фреи на разрушенный... (timer = 15000)
	    			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() 
	    			{
		    			@Override
		     			public void run() 
		    			{
	                		for(L2Player player : getReflection().getPlayers())
	                    		player.sendPacket(new ExChangeZoneInfo(0,2));
		    			}
		    		}, timer);
    				break;
	    		case 7: //Меняем эффект замка фреи на обычный... (timer = 500)
	    			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() 
	    			{
		    			@Override
		     			public void run() 
		    			{
	                		for(L2Player player : getReflection().getPlayers())
	                    		player.sendPacket(new ExChangeZoneInfo(0,1));
		    			}
		    		}, timer);
    				break;
			}
		}
		
		public Reflection getReflection()
		{
	    	return ReflectionTable.getInstance().get(instanceId);
		}

    	public void getRandomHate(L2NpcInstance npc)
    	{
	    	if(npc == null)
	    		return;
			
        	L2Character target = getRandomPlayer();
    	    if(target != null)
    	    	npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 300);
    	}

    	public L2Player getRandomPlayer()
    	{
    		GArray<L2Player> list = getReflection().getPlayers();
    		if(list.isEmpty())
    			return null;
    		return list.get(Rnd.get(list.size()));
    	}
		
	}
	
	//Инициализация зоны
    private static L2Zone _zone;
    private static ZoneListener _zoneListener = new ZoneListener();
	
	public static void init()
	{
		_zone = ZoneManager.getInstance().getZoneById(ZoneType.epic, 702122, false);
		_zone.getListenerEngine().addMethodInvokedListener(_zoneListener);
	}
	
    private static class ZoneListener extends L2ZoneEnterLeaveListener 
	{
		@Override
		public void objectEntered(L2Zone zone, final L2Object object) 
		{
			L2Player player = object.getPlayer();
			if(player == null)
				return;
			final FreyaInstanceInfo world = instances.get(player.getReflection().getId());
			if(world == null)
    			return;
				
			if(world.wavesTimer == null)
			{
		    	ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
	    			@Override
	    			public void run()
					{
		    			world.wavesTimer = ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(1, world), 1000);
		    			//world.TimeEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeEnd(), TIMER_DESTROY_TASK);
						world.getReflection().closeDoor(world.FREYA_CASTLE_DOOR);
					}
				}, 20000);
			}
        }

        @Override
        public void objectLeaved(L2Zone zone, L2Object object) 
		{}
    }
	
	//Инициализация таймеров
	public static class StartWave implements Runnable
	{
		private int _id;
		FreyaInstanceInfo world;
		
		public StartWave(int id, FreyaInstanceInfo _world)
		{
    		_id = id;
			world = _world;
			if(debug)
			{
		        System.out.println("FreyaManager: World status: " + world.status);
			}
		}
		
		@Override
		public void run()
		{
    		switch(_id)
			{
			    //1 Стадия
    			case 1:
                    for (L2Player pl : world.getReflection().getPlayers()) 
					{
                        pl.showQuestMovie(ExStartScenePlayer.SCENE_SC_BOSS_FREYA_OPENING);
                    }
					world.status = 1;
					ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(2, world), 54000L);
    				break;
    			case 2:
                    world.freya = world.spawn(world.freyaSpawn);
					world.castle_controller = world.spawn(world.castle_controller_spawn);
					world.loadSpawn(1);
					world.loadSpawn(2);
					world.loadSpawn(3);
					ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(3, world), 7000L);
			    	for (L2Player pl : world.getReflection().getPlayers())
			        	pl.broadcastPacket(new ExShowScreenMessage(1801097, 5000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, false));
					world.freya.setRunning();
					world.freya.moveToLocation(114730,-114805,-11200, 0, true);
        			//world.BlizzardTimer = ThreadPoolManager.getInstance().scheduleGeneral(new Blizzard(world), Rnd.get(world._intervalOfBlizzards));
    				break;
    			case 3:
                    for (L2Player pl : world.getReflection().getPlayers())
                        pl.broadcastPacket(new ExShowScreenMessage(1801086, 7000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, true));
    				break;
			    //2 Стадия
    			case 4:
                    for(L2Player pl : world.getReflection().getPlayers())
                        pl.showQuestMovie(ExStartScenePlayer.SCENE_SC_BOSS_FREYA_PHASECH_A);
					world.status = 2;
					ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(5, world), 21300L);
    				break;
    			case 5:
					for(L2Player player : world.getReflection().getPlayers())
	    				player.broadcastPacket(new ExSendUIEvent(player, false, false, 60, 0, "Time remaining until next battle"));
	        		world.freya = world.spawn(world.freyaSpelling);
	        		world.freya.setImobilised(true);
	        		world.freya.setIsInvul(true);
	        		ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(6, world), 60000L);
    				break;
    			case 6:
					for(L2Player player : world.getReflection().getPlayers()) 
					{
	    				player.broadcastPacket(new ExShowScreenMessage(1801087, 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, true));
						if(world.is_hard_mode)
						{
	    	    			player.broadcastPacket(new ExSendUIEvent(player, false, false, 360, 0, "Battle end limit time"));
        	        		//ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(7, world), 360000);
						}
					}
					world.loadSpawn(1);
					world.loadSpawn(2);
					world.loadSpawn(3);
    				break;
    			case 7:
                    for(L2Player player : world.getReflection().getPlayers())
	    				player.broadcastPacket(new ExStartScenePlayer(ExStartScenePlayer.SCENE_ICE_HEAVYKNIGHT_SPAWN));
					ChangeStates(0x04, world);
	        		ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(8, world), 6000L);
    				break;
    			case 8:
					ChangeStates(0x01, world);
	        		ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(81, world), 1000L);
					break;
    			case 81:
					world.loadSpawn(1);
					world.loadSpawn(2);
					world.loadSpawn(3);
					world.knight_leader = world.spawn(world.ice_knight_leader);
					world.getRandomHate(world.knight_leader);
					break;
			    //3 Стадия
    			case 9:
					for(L2Player player : world.getReflection().getPlayers())
	    				player.showQuestMovie(ExStartScenePlayer.SCENE_SC_BOSS_FREYA_PHASECH_B);
					world.events(world.freya, 6, 15000);
					world.status = 3;
					ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(10, world), 21600L);
					break;
    			case 10:
					world.freya.deleteMe();
					world.freya = world.spawn(world.freyaStand);
					world.loadSpawn(1);
					world.loadSpawn(2);
					world.loadSpawn(3);
					for(L2Player player : world.getReflection().getPlayers())
	    				player.broadcastPacket(new ExShowScreenMessage(1801088, 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, true));
					ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(101, world), 10000L);
					break;
    			case 101:
					world.freya.setRunning();
					world.freya.moveToLocation(114722, -114797, -11200, 0, true);
					break;
				//4 Стадия
    			case 102:
					ChangeStates(0x02, world);
					for(L2Player pl : world.getReflection().getPlayers())
	            		pl.showQuestMovie(ExStartScenePlayer.SCENE_SC_BOSS_KEGOR_INTRUSION);
					world.status = 4;
	            	ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(11, world), 29000L);
					break;
    			case 11:
					ChangeStates(0x01, world);
					world.kegor = world.spawn(world.kegorSt3Spawn);
					world.kegor.setRunning();
					world.kegor.moveToLocation(114602, -114664, -11200, 0, true);
					world.jinia = world.spawn(world.jiniaSt3Spawn);
					world.jinia.setRunning();
					world.jinia.moveToLocation(114846, -114664, -11200, 0, true);
					for(L2Player player : world.getReflection().getPlayers()) 
					{
		            	SkillTable.getInstance().getInfo(6288, 1).getEffects(world.kegor, player, false, false);
		            	SkillTable.getInstance().getInfo(6289, 1).getEffects(world.jinia, player, false, false);
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(12, world), 3000L);
					break;
    			case 12:
					for(L2Player player : world.getReflection().getPlayers()) 
	    				player.broadcastPacket(new ExShowScreenMessage(1801089, 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, true));
					world.loadSpawn(1);
					world.loadSpawn(2);
					world.loadSpawn(3);
					break;
    			case 13:
	    			world.kegor = world.spawn(world.kegorSt4Spawn);
					world.kegor.setImobilised(true);
	    			world.freya = world.spawn(world.freyaDeadSpawn);
            		world.freya.setCurrentHp(0, false);
            		world.freya.broadcastPacket(new Die(world.freya));
					DecayTaskManager.getInstance().cancelDecayTask(world.freya);
					break;
			}
		}
	}

	public static void OnDie(final L2Character self, L2Character killer)
	{
		if(killer == null || killer.getReflectionId() <= 0)
			return;
		FreyaInstanceInfo world = instances.get(killer.getReflection().getId());
		if(world == null)
    		return;
			
		switch(self.getNpcId())
		{
			case 29177: //FREYA_THRONE
				ChangeStates(0x04, world);
				ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(4, world), 3000);
				/*if(world.BlizzardTimer != null)
				{
    				world.BlizzardTimer.cancel(false);
					world.BlizzardTimer = null;
				}*/
				break;
			case 25699: //GLAKIAS
				ChangeStates(0x04, world);
				for (L2Player player : world.getReflection().getPlayers())
	    			player.broadcastPacket(new ExSendUIEvent(player, false, false, 60, 0, "Time remaining until next battle"));
				ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(9, world), 60100L);
				break;
			case 29179: //FREYA_STAND
				ChangeStates(0x04, world);
				for(L2Player pl : world.getReflection().getPlayers())
					pl.showQuestMovie(ExStartScenePlayer.SCENE_SC_BOSS_FREYA_ENDING_A);
				world.events(world.freya, 7, 500);
				ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(13, world), 17000L);
				break;
			case 18855: //ARCHERY_KNIGHT
				if(world.status == 2)
				{
        			if(world.knights_kill_count <= 11 && world.state != 1)
	    			{
		        		world.knights_kill_count++;
		    		}
		    		else
					{
					    if(world.state != 1)
						{
    	            		ThreadPoolManager.getInstance().scheduleGeneral(new StartWave(7, world), 1000);
			    			world.state = 1;
						}
					}
				}
				break;
			case 18853: //GLACIER
				((L2NpcInstance)self).setNpcState(3);
				world.glaciers.remove(self);
				if(world.glaciers.size() > 0)
				{
	    			L2Skill skill = SkillTable.getInstance().getInfo(6437, world.glaciers.size());
					for (L2Player player : world.getReflection().getPlayers())
	        			skill.getEffects(((L2NpcInstance)self), player, false, false);
					world.castle_controller.setNpcState(world.glaciers.size());
				}
				else 
				{
	    			for (L2Player player : world.getReflection().getPlayers()) 
	    			{
	        			GArray<L2Effect> effects = player.getEffectList().getEffectsBySkillId(6437);
	        			for (L2Effect ef : effects) 
	        			{
	            			if (ef != null) 
	            			{
	                			ef.exit();
	            			}
	        			}
	    			}
					world.castle_controller.setNpcState(0);
				}
				if(Rnd.get(100) < 75)
				{
	    			world.ice_breath = world.spawn(new Location(((L2NpcInstance)self).getX(), ((L2NpcInstance)self).getY(), -11235, 0, world.ARCHERY_BREATH));
	    			world.active_monsters.add(world.ice_breath);
	    			world.getRandomHate(world.ice_breath);
				}
				break;
		}
	}
	
	private static class Blizzard implements Runnable
	{
		FreyaInstanceInfo world;
		
		public Blizzard(FreyaInstanceInfo _world)
		{
			world = _world;
		}
		
		public void run()
		{
			if(world.freya == null)
				return;
				
			if(!world.freya.isBlocked())
			{
				world.freya.broadcastPacket(new ExShowScreenMessage(1801111, 3000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, true, 1, false));
				world.freya.broadcastPacket(new MagicSkillUse(world.freya, world.freya, 5007, 1, 10000, 0));
				ThreadPoolManager.getInstance().scheduleGeneral(new BlizzardEffect(getBlizzardTargets(), world), 10000);
			}
			world.BlizzardTimer = ThreadPoolManager.getInstance().scheduleGeneral(new Blizzard(world), world._intervalOfBlizzards + Rnd.get(10000));
		}

		private GArray<L2Character> getBlizzardTargets()
		{
			GArray<L2Character> targets = new GArray<L2Character>();
			// Target is the players
			for(L2Player pc : world.getReflection().getPlayers())
				if(!pc.isDead())
					targets.add(pc);
			return targets;
		}
	}

	private static class BlizzardEffect implements Runnable
	{
		private final GArray<L2Character> _targets;
		FreyaInstanceInfo world;
		
		public BlizzardEffect(GArray<L2Character> targets, FreyaInstanceInfo _world)
		{
			_targets = targets;
			world = _world;
		}

		public void run()
		{
			world.freya.callSkill(SkillTable.getInstance().getInfo(6274, 1), _targets, false);
		}
	}
	
	public static void ChangeStates(int state, FreyaInstanceInfo world)
	{
	    /* =================== */
	    /**    states         **/
		/* =================== */
		/**    startAll       **/
		/**    stopAll        **/
		/**    destroyInst    **/
		/**    endWave        **/
		/* =================== */
		switch(state)
		{
			//startAll
			case 0x01:
				for(L2Player player : world.getReflection().getPlayers())
				{
    				player.setImobilised(false);
    				player.setIsInvul(false);
    				player.leaveMovieMode();
				}
				for(L2NpcInstance npc : world.ice_knights)
				{
	    			npc.setIsInvul(false);
	    			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	    			npc.setImobilised(false);
	    			world.getRandomHate(npc);
				}
				for(L2NpcInstance npc : world.active_monsters)
				{
	    			npc.setIsInvul(false);
	    			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	    			npc.setImobilised(false);
	    			world.getRandomHate(npc);
				}
				for(L2NpcInstance npc : world.glaciers)
				{
	    			npc.setIsInvul(false);
	    			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	    			npc.setImobilised(false);
				}
				if(world.freya != null)
				{
	    			world.freya.setIsInvul(false);
	    			world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	    			world.freya.setImobilised(false);
	    			world.getRandomHate(world.freya);
				}
				if(world.glaciers.size() > 0)
				{
    				for(L2Player player : world.getReflection().getPlayers())
					{
	    				SkillTable.getInstance().getInfo(6437, world.glaciers.size()).getEffects(world.glaciers.get(Rnd.get(world.glaciers.size())), player, false, false);		
	    				world.castle_controller.setNpcState(world.glaciers.size());
					}
				}
				break;
			//stopAll
			case 0x02:
				for(L2Player player : world.getReflection().getPlayers())
				{
    				player.abortCast(true);
    				player.abortAttack(true, true);
    				player.setIsInvul(true);
				}
				if(world.ice_knights != null)
				{
    				for(L2NpcInstance npc : world.ice_knights)
					{
	    				npc.setIsInvul(true);
	    				npc.abortCast(true);
	    				npc.abortAttack(true, true);
	    				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
	    				npc.setImobilised(true);
	    				npc.setTarget(null);
	    				if(npc.isMoving)
	    	        		npc.stopMove();
					}
				}
				if(world.active_monsters != null)
				{
    				for(L2NpcInstance npc : world.active_monsters)
					{
	    				npc.setIsInvul(true);
	    				npc.abortCast(true);
	    				npc.abortAttack(true, true);
	    				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
	    				npc.setImobilised(true);
	    				npc.setTarget(null);
	    				if(npc.isMoving)
	    	        		npc.stopMove();
					}
				}
   			    if (world.glaciers.size() > 0) 
   			    {
   		    	    for (L2Player player : world.getReflection().getPlayers()) 
   	    		    {
   	        		    GArray<L2Effect> effects = player.getEffectList().getEffectsBySkillId(6437);
   	        		    for (L2Effect ef : effects) 
   	        		    {
   	            		    if (ef != null) 
   	            		    {
   	                		    ef.exit();
   	            		    }
   	        		    }
   	    		    }
					world.castle_controller.setNpcState(0);
   			    }
				if(world.glaciers != null)
				{
    				for(L2NpcInstance npc : world.glaciers)
					{
	    				npc.setIsInvul(true);
	    				npc.abortCast(true);
	    				npc.abortAttack(true, true);
	    				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
	    				npc.setImobilised(true);
	    				npc.setTarget(null);
	    				if(npc.isMoving)
	    	        		npc.stopMove();
					}
				}
				if(world.freya != null)
				{
    				world.freya.setIsInvul(true);
    				world.freya.abortCast(true);
    				world.freya.abortAttack(true, true);
    				world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    				world.freya.setImobilised(true);
    				world.freya.setTarget(null);
    				if(world.freya.isMoving)
	    	        	world.freya.stopMove();
				}
        		/*if(world.BlizzardTimer != null)
         		{
        			world.BlizzardTimer.cancel(false);
        			world.BlizzardTimer = null;
        		}*/
				break;
			//destroyInst
			/*case 0x03:
    			for(L2Player player : world.getReflection().getPlayers())
	    			if(!player.isGM())
	    				player.teleToClosestTown();
						
	    		ChangeStates(0x04);
				if(world.wavesTimer != null)
				{
					world.wavesTimer.cancel(true);
					world.wavesTimer = null;
				}
				if(world.TimeEndTask != null)
				{
					world.TimeEndTask.cancel(true);
					world.TimeEndTask = null;
				}
				break;*/
			//endWave
			case 0x04:
   			    for (L2NpcInstance active_monster : world.active_monsters) 
   			    {
	        		active_monster.deleteMe();
   			    }
   			    if (world.glaciers.size() > 0) 
   			    {
   		    	    for (L2Player player : world.getReflection().getPlayers()) 
   	    		    {
   	        		    GArray<L2Effect> effects = player.getEffectList().getEffectsBySkillId(6437);
   	        		    for (L2Effect ef : effects) 
   	        		    {
   	            		    if (ef != null) 
   	            		    {
   	                		    ef.exit();
   	            		    }
   	        		    }
   	    		    }
					world.castle_controller.setNpcState(0);
   			    }
   			    for (L2NpcInstance glacier : world.glaciers) 
   			    {
	        		glacier.deleteMe();
   			    }
   			    for (L2NpcInstance ice_knight : world.ice_knights) 
   			    {
	        		ice_knight.deleteMe();
   			    }
   			    if (world.freya != null) 
   			    {
	        		world.freya.deleteMe();
   			    }
   			    if (world.kegor != null) 
   			    {
	        		world.kegor.deleteMe();
   			    }
   			    if (world.jinia != null) 
   			    {
	        		world.jinia.deleteMe();
   			    }
				break;
		}
	}

    public static L2Zone getZone() 
	{
        return _zone;
    }
	
    @Override
    public void onLoad() 
	{
        init();
    }

    @Override
    public void onReload() 
	{
        getZone().getListenerEngine().removeMethodInvokedListener(_zoneListener);
    }

    @Override
    public void onShutdown() 
	{}
	
	public static boolean enterInstance(L2Player player, int instancedZoneId, String htmMsg)
	{
		if(player == null)
			return false;
			
		if(debug && player.isGM())
		{
    		enterDebugInstance(player, instancedZoneId);
			return false;
		}
			
		if(player.getParty() == null || !player.getParty().isInCommandChannel())
		{
			player.sendPacket(Msg.YOU_CANNOT_ENTER_BECAUSE_YOU_ARE_NOT_IN_A_CURRENT_COMMAND_CHANNEL);
			return false;
		}
		
		L2CommandChannel cc = player.getParty().getCommandChannel();
		if(cc.getChannelLeader() != player)
		{
		    player.sendMessage("You must be leader of the Command Channel.");
			return false;
		}
		InstancedZoneManager izm = InstancedZoneManager.getInstance();
		FastMap<Integer, InstancedZone> izs = InstancedZoneManager.getInstance().getById(instancedZoneId);
		if(izs == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return false;
		}
		InstancedZone iz = izs.get(0);
		assert iz != null;
		String name = iz.getName();
		int timelimit = iz.getTimelimit();
		int minMembers = iz.getMinParty();
		int maxMembers = iz.getMaxParty();
		if(cc.getMemberCount() < minMembers)
		{
			player.sendMessage("The command channel must contains at least " + minMembers + " members.");
			return false;
		}
		if(cc.getMemberCount() > maxMembers)
		{
			player.sendMessage("The command channel must contains not more than " + maxMembers + " members.");
			return false;
		}
		for(L2Player member : cc.getMembers())
		{
			if(member.getLevel() < iz.getMinLevel() || member.getLevel() > iz.getMaxLevel())
			{
				cc.broadcastToChannelMembers(new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member));
				return false;
			}
			if(member.isCursedWeaponEquipped() || member.isInFlyingTransform() || member.isDead())
			{
				player.sendPacket(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member));
				return false;
			}
			if(!player.isInRange(member, 500))
			{
				member.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
				player.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
				return false;
			}
			if(izm.getTimeToNextEnterInstance(name, member) > 0)
			{
				cc.broadcastToChannelMembers(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(member));
				return false;
			}
		}
		Reflection r = new Reflection(name);
		r.setInstancedZoneId(instancedZoneId);
		for(InstancedZone i : izs.values())
		{
			if(r.getReturnLoc() == null)
				r.setReturnLoc(i.getReturnCoords());
			if(r.getTeleportLoc() == null)
				r.setTeleportLoc(i.getTeleportCoords());
			r.FillSpawns(i.getSpawnsInfo());
			r.FillDoors(i.getDoors());
		}
		
		r.setCoreLoc(r.getReturnLoc());
		r.setReturnLoc(player.getLoc());
		for(L2Player member : cc.getMembers())
		{
			member.setVar(name, String.valueOf(System.currentTimeMillis()));
			member.setVar("backCoords", r.getReturnLoc().toXYZString());
			member.teleToLocation(r.getTeleportLoc(), r.getId());
		}
		cc.setReflection(r);
		r.setCommandChannel(cc);
		
		// init
		FreyaInstanceInfo world = new FreyaInstanceInfo();
    	world.instanceId = r.getId();
    	world.status = 0;
		world.wavesTimer = null;
    	instances.put(r.getId(), world);
		
		if(timelimit > 0)
			r.startCollapseTimer(timelimit * 60 * 1000L);
			
		return true;
	}

	/*private static void enterDebugInstance(L2Player player, int instancedZoneId)
	{
		FastMap<Integer, InstancedZone> izs = InstancedZoneManager.getInstance().getById(instancedZoneId);
		if(izs == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}

		InstancedZone iz = izs.get(0);
		assert iz != null;

		Reflection r = new Reflection(iz);
		r.setInstancedZoneId(instancedZoneId);

		if(!player.isInParty())
		{
			player.sendPacket(Msg.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
			return;
		}

		if(!player.getParty().isLeader(player))
		{
			player.sendPacket(Msg.ONLY_A_PARTY_LEADER_CAN_TRY_TO_ENTER);
			return;
		}

		for(InstancedZone i : izs.values())
		{
			if(r.getReturnLoc() == null)
				r.setReturnLoc(i.getReturnCoords());
			if(r.getTeleportLoc() == null)
				r.setTeleportLoc(i.getTeleportCoords());
			r.FillSpawns(i.getSpawnsInfo());
			r.FillDoors(i.getDoors());
		}

		long timelimit = 0;

		r.setCoreLoc(r.getReturnLoc());
		r.setReturnLoc(player.getLoc());
		
		for(L2Player member : player.getParty().getPartyMembers())
		{
			member.setVar("backCoords", r.getReturnLoc().toXYZString());
			member.teleToLocation(iz.getTeleportCoords(), r.getId());
		}

		player.getParty().setReflection(r);
		r.setParty(player.getParty());
		
		// init
    	FreyaInstanceInfo world = new FreyaInstanceInfo();
    	world.instanceId = r.getId();
    	world.status = 0;
		world.wavesTimer = null;
    	instances.put(r.getId(), world);
		
		if(timelimit > 0)
			r.startCollapseTimer(timelimit);
	}*/

	private static void enterDebugInstance(L2Player player, int instancedZoneId)
	{
		FastMap<Integer, InstancedZone> izs = InstancedZoneManager.getInstance().getById(instancedZoneId);
		if(izs == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}

		InstancedZone iz = izs.get(0);
		assert iz != null;

		Reflection r = new Reflection(iz);
		r.setInstancedZoneId(instancedZoneId);
		
		for(InstancedZone i : izs.values())
		{
			if(r.getReturnLoc() == null)
				r.setReturnLoc(i.getReturnCoords());
			if(r.getTeleportLoc() == null)
				r.setTeleportLoc(i.getTeleportCoords());
			r.FillSpawns(i.getSpawnsInfo());
			r.FillDoors(i.getDoors());
		}

		long timelimit = 0;

		r.setCoreLoc(r.getReturnLoc());
		r.setReturnLoc(player.getLoc());
		
		player.setVar("backCoords", r.getReturnLoc().toXYZString());
		player.teleToLocation(iz.getTeleportCoords(), r.getId());

		player.setReflection(r);
		
		// init
    	FreyaInstanceInfo world = new FreyaInstanceInfo();
    	world.instanceId = r.getId();
    	world.status = 0;
		world.wavesTimer = null;
    	instances.put(r.getId(), world);
		
		if(timelimit > 0)
			r.startCollapseTimer(timelimit);
	}
}