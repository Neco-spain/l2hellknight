package zone_scripts.Freya.IceQueenCastle2;

import java.util.Calendar;

import javolution.util.FastMap;
import l2.brick.Config;
import l2.brick.gameserver.Text;
import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.datatables.NpcTable;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.datatables.SpawnTable;
import l2.brick.gameserver.instancemanager.InstanceManager;
import l2.brick.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.brick.gameserver.model.L2CharPosition;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.L2Party;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.L2Spawn;
import l2.brick.gameserver.model.L2World;
import l2.brick.gameserver.model.actor.L2Attackable;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.actor.instance.Okoli;
import l2.brick.gameserver.model.entity.Instance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.ExSendUIEvent;
import l2.brick.gameserver.network.serverpackets.OnEventTrigger;
import l2.brick.gameserver.network.serverpackets.Scenkos;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.templates.L2NpcTemplate;
import l2.brick.gameserver.templates.L2EffectType;
import l2.brick.gameserver.util.Util;
import l2.brick.util.Rnd;

public class IceQueenCastle2 extends Quest
{
	private static final String qn = "IceQueenCastle2";
	
	private static final int INSTANCE_ID = 139;
	
	private boolean debug = false;
	
	private class FreyaWorld extends InstanceWorld
	{
		public L2Attackable _freya_controller = null;
		public L2Attackable _freyaThrone = null;
		public L2Npc _freyaSpelling = null;
		public L2Attackable _freyaStand = null;
		public L2Attackable _glakias = null;
		public L2Attackable _jinia = null;
		public L2Attackable _kegor = null;
		public boolean isMovieNow = false;
		public FastMap<Integer, L2Npc> _archery_knights = new FastMap<Integer, L2Npc>();
		public FastMap<Integer, L2Npc> _simple_knights = new FastMap<Integer, L2Npc>();
		public FastMap<Integer, L2Npc> _glaciers = new FastMap<Integer, L2Npc>();
		//Hard
		public L2Attackable _freyaStand_hard = null;
		public L2Attackable _glakias_hard = null;
		public FastMap<Integer, L2Npc> _archery_knights_hard = new FastMap<Integer, L2Npc>();
		//Hard - end
		public FreyaWorld()
		{
			InstanceManager.getInstance();
		}
	}
	
	private class spawnWave implements Runnable
	{
		
		private int _waveId;
		private FreyaWorld _world;
		
		public spawnWave(int waveId, int instanceId)
		{
			_waveId = waveId;
			_world = getWorld(instanceId);
		}
		public void run()
		{
			//Hard
			if ( _isHard )
			{
				switch(_waveId)
				{
					case 1:
						// Freya controller
						_world._freya_controller = (L2Attackable) spawnNpc(freya_controller, 114707, -114793, -11199, 0, _world.instanceId);
						_world._freya_controller.setIsInvul(true);
						// Sirra
						spawnNpc(_sirra, 114766, -113141, -11200, 15956, _world.instanceId);
						handleWorldState(1 , _world.instanceId);
						break;
						
					case 3:
						if (_world == null)
							break;
						if (Util.contains(archery_blocked_status, _world.status))
							break;
						if (_world._archery_knights_hard.size() < 5 && _world.status < 44)
						{
							int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
							L2Npc mob = spawnNpc(archery_knight_hard, spawnXY[0], spawnXY[1], -11200, 20016, _world.instanceId);
							((L2Attackable) mob).setOnKillDelay(0);
							L2PcInstance victim = getRandomPlayer(_world);
							mob.setTarget(victim);
							mob.setRunning();
							((L2Attackable) mob).addDamageHate(victim, 0, 9999);
							mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
							_world._archery_knights_hard.put(mob.getObjectId(), mob);
							if (_world.status == 1 || _world.status == 11 || _world.status == 24 || _world.status == 30 || _world.status == 40)
							{
								mob.setIsImmobilized(true);
							}
						}
						break;
					case 4:
						break;
						
					case 5:
						if (_world != null && _world._glaciers.size() < 5
								&& _world.status < 44
								&& !Util.contains(glacier_blocked_status, _world.status))
						{
							int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
							L2Npc mob = spawnNpc(glacier, spawnXY[0], spawnXY[1], -11200, 20016, _world.instanceId);
							_world._glaciers.put(mob.getObjectId(), mob);
						}
						if (_world.status < 44)
							ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, _world.instanceId), (Rnd.get(10, 40) * 1000) + 20000);
						break;
						
					case 6:
						for (int[] iter : _archeryKnightsSpawn)
						{
							L2Npc mob = spawnNpc(archery_knight_hard, iter[0], iter[1], iter[2], iter[3], _world.instanceId);
							((L2Attackable) mob).setOnKillDelay(0);
							mob.setRunning();
							L2PcInstance victim = getRandomPlayer(_world);
							mob.setTarget(victim);
							((L2Attackable) mob).addDamageHate(victim, 0, 9999);
							mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
							_world._archery_knights_hard.put(mob.getObjectId(), mob);
						}
						handleWorldState(_world.status+1, _world);
						break;
						
					case 7:
						handleWorldState(2, _world.instanceId);
						break;
					case 8:
						handleWorldState(11, _world.instanceId);
						break;
					case 9:
						handleWorldState(19, _world.instanceId);
						break;
					case 10:
						handleWorldState(20, _world.instanceId);
						break;
					case 11:
						handleWorldState(25, _world.instanceId);
						break;
					case 12:
						handleWorldState(30, _world.instanceId);
						break;
					case 13:
						handleWorldState(31, _world.instanceId);
						break;
					case 14:
						handleWorldState(41, _world.instanceId);
						break;
					case 15: 
						handleWorldState(43, _world.instanceId);
						break;
					case 16:
						setInstanceRestriction(_world);
						InstanceManager.getInstance().getInstance(_world.instanceId).setDuration(300000);
						InstanceManager.getInstance().getInstance(_world.instanceId).setEmptyDestroyTime(0);
						break;
					case 19:
						stopAll(_world);
						break;
					case 20:
						_world.isMovieNow = false;
						startAll(_world);
						break;
				}
			}
			//Hard - end
			else
			{
				switch(_waveId)
				{
					case 1:
						// Freya controller
						_world._freya_controller = (L2Attackable) spawnNpc(freya_controller, 114707, -114793, -11199, 0, _world.instanceId);
						_world._freya_controller.setIsInvul(true);
						// Sirra
						spawnNpc(_sirra, 114766, -113141, -11200, 15956, _world.instanceId);
						handleWorldState(1, _world.instanceId);
						break;
						
					case 3:
						if (_world == null)
							break;
						if (Util.contains(archery_blocked_status, _world.status))
							break;
						if (_world._archery_knights.size() < 5 && _world.status < 44)
						{
							int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
							L2Npc mob = spawnNpc(archery_knight, spawnXY[0], spawnXY[1], -11200, 20016, _world.instanceId);
							((L2Attackable) mob).setOnKillDelay(0);
							L2PcInstance victim = getRandomPlayer(_world);
							mob.setTarget(victim);
							mob.setRunning();
							((L2Attackable) mob).addDamageHate(victim, 0, 9999);
							mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
							_world._archery_knights.put(mob.getObjectId(), mob);
							if (_world.status == 1 || _world.status == 11 || _world.status == 24 || _world.status == 30 || _world.status == 40)
							{
								mob.setIsImmobilized(true);
							}
						}
						break;
					case 4:
						break;
						
					case 5:
						if (_world != null && _world._glaciers.size() < 5
								&& _world.status < 44
								&& !Util.contains(glacier_blocked_status, _world.status))
						{
							int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
							L2Npc mob = spawnNpc(glacier, spawnXY[0], spawnXY[1], -11200, 20016, _world.instanceId);
							_world._glaciers.put(mob.getObjectId(), mob);
						}
						if (_world.status < 44)
							ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, _world.instanceId), (Rnd.get(10, 40) * 1000) + 20000);
						break;
						
					case 6:
						for (int[] iter : _archeryKnightsSpawn)
						{
							L2Npc mob = spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], _world.instanceId);
							((L2Attackable) mob).setOnKillDelay(0);
							mob.setRunning();
							L2PcInstance victim = getRandomPlayer(_world);
							mob.setTarget(victim);
							((L2Attackable) mob).addDamageHate(victim, 0, 9999);
							mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
							_world._archery_knights.put(mob.getObjectId(), mob);
						}
						handleWorldState(_world.status+1, _world);
						break;
						
					case 7:
						handleWorldState(2, _world.instanceId);
						break;
					case 8:
						handleWorldState(11, _world.instanceId);
						break;
					case 9:
						handleWorldState(19, _world.instanceId);
						break;
					case 10:
						handleWorldState(20, _world.instanceId);
						break;
					case 11:
						handleWorldState(25, _world.instanceId);
						break;
					case 12:
						handleWorldState(30, _world.instanceId);
						break;
					case 13:
						handleWorldState(31, _world.instanceId);
						break;
					case 14:
						handleWorldState(41, _world.instanceId);
						break;
					case 15: 
						handleWorldState(43, _world.instanceId);
						break;
					case 16: 
						handleWorldState(45, _world.instanceId);
						break;
					case 17:
						handleWorldState(46, _world.instanceId);
						break;
					case 18:
						setInstanceRestriction(_world);
						InstanceManager.getInstance().getInstance(_world.instanceId).setDuration(300000);
						InstanceManager.getInstance().getInstance(_world.instanceId).setEmptyDestroyTime(0);
						break;
					case 19:
						stopAll(_world);
						break;
					case 20:
						_world.isMovieNow = false;
						startAll(_world);
						break;
				}
			}
		}
	}
	
	//freyaStand = 29179;
	//archery_knight = 18855;
	//Glakias	= 25699;
	
	private boolean _isHard = false;
	private static int freyaOnThrone = 29177;
	private static int freyaSpelling = 29178;
	private static int freyaStand = 29179;
	private static int freya_controller = 36800;
	private static int glacier = 18853;
	private static int archery_knight = 18855;
	private static int Glakias = 25699;
	private static int _sirra = 32762;
	//private static int tmp					= 32777;
	private static int door = 23140101;
	
	//Hard
	private static int freyaStand_hard = 29180;
	private static int archery_knight_hard = 18856;
	private static int Glakias_hard = 25700;
	//Hard - end
	
	private static int[] emmiters = 
	{
		23140202, 23140204, 23140206, 23140208, 23140212, 23140214, 23140216
	};
	private static int decoration = 0;
	
	private static final int[] archery_blocked_status =
	{
		11, 19, 22, 29, 39
	};
	
	private static final int[] glacier_blocked_status =
	{
		11, 19, 29, 39
	};
	
	private static int[][] frozeKnightsSpawn =
	{
		{113845,-116091,-11168,8264},
		{113381,-115622,-11168,8264},
		{113380,-113978,-11168,-8224},
		{113845,-113518,-11168,-8224},
		{115591,-113516,-11168,-24504},
		{116053,-113981,-11168,-24504},
		{116061,-115611,-11168,24804},
		{115597,-116080,-11168,24804},
		{112942,-115480,-10960,52},
		{112940,-115146,-10960,52},
		{112945,-114453,-10960,52},
		{112945,-114123,-10960,52},
		{116497,-114117,-10960,32724},
		{116499,-114454,-10960,32724},
		{116501,-115145,-10960,32724},
		{116502,-115473,-10960,32724}
	};

	private static final int[][] _archeryKnightsSpawn = 
	{
		{114713, -115109, -11202, 16456},
		{114008, -115080, -11202, 3568},
		{114422, -115508, -11202, 12400},
		{115023, -115508, -11202, 20016},
		{115459, -115079, -11202, 27936}
	};
	
	private void broadcastMovie(int movieId, FreyaWorld world)
	{
		world.isMovieNow = true;
		
		stopAll(world);
		
		for (int objId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			player.showQuestMovie(movieId);
		}
		
		int pause = 0;
		//Hard
		if ( _isHard )
		{
			switch (movieId)
			{
				case 15:
					pause = 53500;
					break;
				case 16:
					pause = 21100;
					break;
				case 17:
					pause = 21500;
					break;
				case 18:
					pause = 27000;
					break;
				case 19:
					pause = 16000;
					break;
				case 23:
					pause = 7000;
					break;
				case 20:
					pause = 55500;
					break;
				default:
					pause = 0;
			}
		}
		//Hard - end
		else
		{
			switch (movieId)
			{
				case 15:
					pause = 53500;
					break;
				case 16:
					pause = 21100;
					break;
				case 17:
					pause = 21500;
					break;
				case 18:
					pause = 27000;
					break;
				case 19:
					pause = 16000;
					break;
				case 23:
					pause = 7000;
					break;
				case 20:
					pause = 55500;
					break;
				default:
					pause = 0;
			}
		}
		
		if (movieId != 15)
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.instanceId), pause);
		if (movieId == 19)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.instanceId), 100);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.instanceId), 200);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.instanceId), 500);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.instanceId), 1000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.instanceId), 2000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.instanceId), 3000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.instanceId), 4000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.instanceId), 5000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.instanceId), 6000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.instanceId), 7000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.instanceId), 8000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.instanceId), 9000);
		}
	}
	
	private void broadcastString(int strId, int instanceId)
	{
		Text sm = new Text(strId, 3000, Text.ScreenMessageAlign.TOP_CENTER, true, false, -1, true);
		Scenkos.toPlayersInInstance(sm, instanceId);
	}
	
	private void broadcastTimer(FreyaWorld world)
	{
		for (int objId : world.allowed)
		{
			L2PcInstance plr = L2World.getInstance().getPlayer(objId);
			ExSendUIEvent time_packet = new ExSendUIEvent(plr, false, false, 60, 0, "Time for prepare to next stage. Buffs please and wait for next stage!");
			plr.sendPacket(time_packet);
		}
	}
	
	private void handleWorldState(int statusId, int instanceId)
	{
		FreyaWorld world = getWorld(instanceId);
		if (world != null)
			handleWorldState(statusId, world);
		else
			System.out.println("Warning!!! Not Found world at handleWorldState(int, int).");
	}
	
	private void handleWorldState(int statusId, FreyaWorld world)
	{
		int instanceId = world.instanceId;
		//Hard
		if (_isHard)
		{
			switch (statusId)
			{
				case 0:
					break;
				case 1:
					if (!debug)
					{
						broadcastMovie(15, world);
						InstanceManager.getInstance().getInstance(world.instanceId).getDoor(door).openMe();
						ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(7, world.instanceId), 52500);
					}
					else
						ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(7, world.instanceId), 1000);
					break;
				case 2:
					world._freyaThrone = (L2Attackable) spawnNpc(freyaOnThrone, 114720, -117085, -11088, 15956, instanceId);
					world._freyaThrone.setIsNoRndWalk(true);
					world._freyaThrone.setisReturningToSpawnPoint(false);
					world._freyaThrone.setOnKillDelay(0);
					world._freyaThrone.setIsInvul(true);
					world._freyaThrone.setIsImmobilized(true);
					for (int objId : world.allowed)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(objId);
						if (player != null && player.isOnline())
						{
							player.getKnownList().addKnownObject(world._freyaThrone);
						}
					}
					
					for (int[] iter : frozeKnightsSpawn)
					{
						L2Npc mob = spawnNpc(archery_knight_hard, iter[0], iter[1], iter[2], iter[3], instanceId);
						archerySpawn(mob);
						world._simple_knights.put(mob.getObjectId(), mob);
					}
					
					for (int[] iter : _archeryKnightsSpawn)
					{
						L2Npc mob = spawnNpc(archery_knight_hard, iter[0], iter[1], iter[2], iter[3], instanceId);
						archerySpawn(mob);
						mob.setDisplayEffect(1);
						world._archery_knights_hard.put(mob.getObjectId(), mob);
					}
					
					for (int objId : world.allowed)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(objId);
						player.setIsImmobilized(false);
						player.setIsInvul(false);
					}
					
					world.isMovieNow = false;
					
					break;
				case 10:
					broadcastString(1801086, world.instanceId);
					InstanceManager.getInstance().getInstance(world.instanceId).getDoor(door).closeMe();
					world._freyaThrone.setIsInvul(false);
					world._freyaThrone.setIsImmobilized(false);
					world._freyaThrone.getAI();
					world._freyaThrone.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114722, -114798, -11205, 15956));
					
					for (int i = 0; i < 5; i++)
					{
						int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
						L2Npc mob = spawnNpc(glacier, spawnXY[0], spawnXY[1], -11200, 0, instanceId);
						world._glaciers.put(mob.getObjectId(), mob);
					}
					
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.instanceId), 7000);
					
					for (L2Npc mob : world._archery_knights_hard.values())
					{
						archeryAttack(mob, world);
					}
					break;
				case 11:
					broadcastMovie(16, world);
					for (L2Npc mob : world._archery_knights_hard.values())
						mob.deleteMe();
					world._archery_knights_hard.clear();
					world._freyaThrone.deleteMe();
					world._freyaThrone = null;
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(9, world.instanceId), 22000);
					break;
				case 12:
					break;
				case 19:
					world._freyaSpelling = spawnNpc(freyaSpelling, 114723, -117502, -10672, 15956, world.instanceId);
					world._freyaSpelling.setIsImmobilized(true);
					broadcastTimer(world);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(10, world.instanceId), 60000);
					break;
				case 20:
					for (int[] iter : _archeryKnightsSpawn)
					{
						L2Npc mob = spawnNpc(archery_knight_hard, iter[0], iter[1], iter[2], iter[3], instanceId);
						archerySpawn(mob);
						mob.setDisplayEffect(1);
						world._archery_knights_hard.put(mob.getObjectId(), mob);
					}
					break;
				case 21:
					broadcastString(1801087, instanceId);
					for (L2Npc mob : world._archery_knights_hard.values())
					{
						archeryAttack(mob, world);
					}
					
					for (int i = 0; i < 5; i++)
					{
						int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
						L2Npc mob = spawnNpc(glacier, spawnXY[0], spawnXY[1], -11200, 0, instanceId);
						world._glaciers.put(mob.getObjectId(), mob);
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.instanceId), 7000);
					break;
				case 22:
				case 23:
					break;
				case 24:
					broadcastMovie(23, world);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(11, world.instanceId), 7000);
					break;
				case 25:
					world._glakias_hard = (L2Attackable) spawnNpc(Glakias_hard, 114707, -114799, -11199, 15956, instanceId);
					world._glakias_hard.setOnKillDelay(0);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.instanceId), 7000);
					break;
				case 29:
					broadcastTimer(world);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(12, world.instanceId), 60000);
					break;
				case 30:
					for (int[] iter : _archeryKnightsSpawn)
					{
						L2Npc mob = spawnNpc(archery_knight_hard, iter[0], iter[1], iter[2], iter[3], instanceId);
						((L2Attackable) mob).setOnKillDelay(0);
						world._archery_knights_hard.put(mob.getObjectId(), mob);
					}
					world._freyaSpelling.deleteMe();
					world._freyaSpelling = null;
					broadcastMovie(17, world);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(13, world.instanceId), 21500);
					break;
				case 31:
					if (!debug)
					{
						Okoli as = new Okoli(decoration, 2);
						Scenkos.toPlayersInInstance(as, world.instanceId);
						for (int emitter : emmiters)
						{
							OnEventTrigger et = new OnEventTrigger(emitter, false);
							Scenkos.toPlayersInInstance(et, world.instanceId);
						}
					}
					
					broadcastString(1801088, instanceId);
					world._freyaStand_hard = (L2Attackable) spawnNpc(freyaStand_hard, 114720, -117085, -11088, 15956, world.instanceId);
					world._freyaStand_hard.setOnKillDelay(0);
					world._freyaStand_hard.getAI();
					world._freyaStand_hard.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114722, -114798, -11205, 15956));
					for (int objId : world.allowed)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(objId);
						if (player != null && player.isOnline())
						{
							player.getKnownList().addKnownObject(world._freyaStand_hard);
						}
					}
					break;
				case 40:
					broadcastMovie(18, world);
					stopAll(world);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(14, world.instanceId), 27000);
					break;
				case 41:
					for (L2Npc mob : world._archery_knights_hard.values())
					{
						archeryAttack(mob, world);
					}
					world._jinia = (L2Attackable) spawnNpc(18850, 114727, -114700, -11200, -16260, instanceId);
					world._jinia.setAutoAttackable(false);
					world._jinia.setIsMortal(false);
					world._kegor = (L2Attackable) spawnNpc(18851, 114690, -114700, -11200, -16260, instanceId);
					world._kegor.setAutoAttackable(false);
					world._kegor.setIsMortal(false);
					handleWorldState(42, instanceId);
					break;
				case 42:
					broadcastString(1801089, instanceId);
					if (world._freyaStand_hard != null && !world._freyaStand_hard.isDead())
					{
						world._jinia.setTarget(world._freyaStand_hard);
						world._jinia.addDamageHate(world._freyaStand_hard, 0, 9999);
						world._jinia.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world._freyaStand_hard);
						world._kegor.setTarget(world._freyaStand_hard);
						world._kegor.addDamageHate(world._freyaStand_hard, 0, 9999);
						world._kegor.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world._freyaStand_hard);
					}
					else
					{
						world._jinia.setIsImmobilized(true);
						world._kegor.setIsImmobilized(true);
					}
					L2Skill skill1 = SkillTable.getInstance().getInfo(6288, 1);
					L2Skill skill2 = SkillTable.getInstance().getInfo(6289, 1);
					for (int objId : world.allowed)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(objId);
						if (player != null)
						{
							skill1.getEffects(world._jinia, player);
							skill2.getEffects(world._kegor, player);
						}
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(15, instanceId), 6000);
					break;
				case 43:

					break;
				case 44:
					broadcastMovie(19, world);
					for (L2Npc mob : InstanceManager.getInstance().getInstance(instanceId).getNpcs())
					{
						if (mob.getNpcId() != freyaStand_hard)
						{
							mob.deleteMe();
							InstanceManager.getInstance().getInstance(instanceId).getNpcs().remove(mob);
						}
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(16, instanceId), 20000);
					break;
				default:
					System.out.println("Warning!!! Not handled world status - " + statusId);
					break;
			}
		}
		//Hard - end
		else
		{
			switch (statusId)
			{
				case 0:
					break;
				case 1:
					if (!debug)
					{
						broadcastMovie(15, world);
						InstanceManager.getInstance().getInstance(world.instanceId).getDoor(door).openMe();
						ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(7, world.instanceId), 52500);
					}
					else
						ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(7, world.instanceId), 1000);
					break;
				case 2:
					world._freyaThrone = (L2Attackable) spawnNpc(freyaOnThrone, 114720, -117085, -11088, 15956, instanceId);
					world._freyaThrone.setIsNoRndWalk(true);
					world._freyaThrone.setisReturningToSpawnPoint(false);
					world._freyaThrone.setOnKillDelay(0);
					world._freyaThrone.setIsInvul(true);
					world._freyaThrone.setIsImmobilized(true);
					for (int objId : world.allowed)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(objId);
						if (player != null && player.isOnline())
						{
							player.getKnownList().addKnownObject(world._freyaThrone);
						}
					}
					
					for (int[] iter : frozeKnightsSpawn)
					{
						L2Npc mob = spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], instanceId);
						archerySpawn(mob);
						world._simple_knights.put(mob.getObjectId(), mob);
					}
					
					for (int[] iter : _archeryKnightsSpawn)
					{
						L2Npc mob = spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], instanceId);
						archerySpawn(mob);
						mob.setDisplayEffect(1);
						world._archery_knights.put(mob.getObjectId(), mob);
					}
					
					for (int objId : world.allowed)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(objId);
						player.setIsImmobilized(false);
						player.setIsInvul(false);
					}
					
					world.isMovieNow = false;
					
					break;
				case 10:
					broadcastString(1801086, world.instanceId);
					InstanceManager.getInstance().getInstance(world.instanceId).getDoor(door).closeMe();
					world._freyaThrone.setIsInvul(false);
					world._freyaThrone.setIsImmobilized(false);
					world._freyaThrone.getAI();
					world._freyaThrone.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114722, -114798, -11205, 15956));
					
					for (int i = 0; i < 5; i++)
					{
						int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
						L2Npc mob = spawnNpc(glacier, spawnXY[0], spawnXY[1], -11200, 0, instanceId);
						world._glaciers.put(mob.getObjectId(), mob);
					}
					
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.instanceId), 7000);
					
					for (L2Npc mob : world._archery_knights.values())
					{
						archeryAttack(mob, world);
					}
					break;
				case 11:
					broadcastMovie(16, world);
					for (L2Npc mob : world._archery_knights.values())
						mob.deleteMe();
					world._archery_knights.clear();
					world._freyaThrone.deleteMe();
					world._freyaThrone = null;
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(9, world.instanceId), 22000);
					break;
				case 12:
					break;
				case 19:
					world._freyaSpelling = spawnNpc(freyaSpelling, 114723, -117502, -10672, 15956, world.instanceId);
					world._freyaSpelling.setIsImmobilized(true);
					broadcastTimer(world);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(10, world.instanceId), 60000);
					break;
				case 20:
					for (int[] iter : _archeryKnightsSpawn)
					{
						L2Npc mob = spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], instanceId);
						archerySpawn(mob);
						mob.setDisplayEffect(1);
						world._archery_knights.put(mob.getObjectId(), mob);
					}
					break;
				case 21:
					broadcastString(1801087, instanceId);
					for (L2Npc mob : world._archery_knights.values())
					{
						archeryAttack(mob, world);
					}
					
					for (int i = 0; i < 5; i++)
					{
						int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
						L2Npc mob = spawnNpc(glacier, spawnXY[0], spawnXY[1], -11200, 0, instanceId);
						world._glaciers.put(mob.getObjectId(), mob);
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.instanceId), 7000);
					break;
				case 22:
				case 23:
					break;
				case 24:
					broadcastMovie(23, world);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(11, world.instanceId), 7000);
					break;
				case 25:
					world._glakias = (L2Attackable) spawnNpc(Glakias, 114707, -114799, -11199, 15956, instanceId);
					world._glakias.setOnKillDelay(0);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.instanceId), 7000);
					break;
				case 29:
					broadcastTimer(world);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(12, world.instanceId), 60000);
					break;
				case 30:
					for (int[] iter : _archeryKnightsSpawn)
					{
						L2Npc mob = spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], instanceId);
						((L2Attackable) mob).setOnKillDelay(0);
						world._archery_knights.put(mob.getObjectId(), mob);
					}
					world._freyaSpelling.deleteMe();
					world._freyaSpelling = null;
					broadcastMovie(17, world);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(13, world.instanceId), 21500);
					break;
				case 31:
					if (!debug)
					{
						Okoli as = new Okoli(decoration, 2);
						Scenkos.toPlayersInInstance(as, world.instanceId);
						for (int emitter : emmiters)
						{
							OnEventTrigger et = new OnEventTrigger(emitter, false);
							Scenkos.toPlayersInInstance(et, world.instanceId);
						}
					}
					
					broadcastString(1801088, instanceId);
					world._freyaStand = (L2Attackable) spawnNpc(freyaStand, 114720, -117085, -11088, 15956, world.instanceId);
					world._freyaStand.setOnKillDelay(0);
					world._freyaStand.getAI();
					world._freyaStand.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114722, -114798, -11205, 15956));
					for (int objId : world.allowed)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(objId);
						if (player != null && player.isOnline())
						{
							player.getKnownList().addKnownObject(world._freyaStand);
						}
					}
					break;
				case 40:
					broadcastMovie(18, world);
					stopAll(world);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(14, world.instanceId), 27000);
					break;
				case 41:
					for (L2Npc mob : world._archery_knights.values())
					{
						archeryAttack(mob, world);
					}
					world._jinia = (L2Attackable) spawnNpc(18850, 114727, -114700, -11200, -16260, instanceId);
					world._jinia.setAutoAttackable(false);
					world._jinia.setIsMortal(false);
					world._kegor = (L2Attackable) spawnNpc(18851, 114690, -114700, -11200, -16260, instanceId);
					world._kegor.setAutoAttackable(false);
					world._kegor.setIsMortal(false);
					handleWorldState(42, instanceId);
					break;
				case 42:
					broadcastString(1801089, instanceId);
					if (world._freyaStand != null && !world._freyaStand.isDead())
					{
						world._jinia.setTarget(world._freyaStand);
						world._jinia.addDamageHate(world._freyaStand, 0, 9999);
						world._jinia.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world._freyaStand);
						world._kegor.setTarget(world._freyaStand);
						world._kegor.addDamageHate(world._freyaStand, 0, 9999);
						world._kegor.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world._freyaStand);
					}
					else
					{
						world._jinia.setIsImmobilized(true);
						world._kegor.setIsImmobilized(true);
					}
					L2Skill skill1 = SkillTable.getInstance().getInfo(6288, 1);
					L2Skill skill2 = SkillTable.getInstance().getInfo(6289, 1);
					for (int objId : world.allowed)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(objId);
						if (player != null)
						{
							skill1.getEffects(world._jinia, player);
							skill2.getEffects(world._kegor, player);
						}
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(15, instanceId), 6000);
					break;
				case 43:
	
					break;
				case 44:
					broadcastMovie(19, world);
					stopAll(world);
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(16, instanceId), 20000);
					handleWorldState(45, instanceId);
				case 45:
					broadcastMovie(20, world);
					handleWorldState(46, instanceId);
				case 46:
					for (L2Npc mob : InstanceManager.getInstance().getInstance(instanceId).getNpcs())
					{
						if (mob.getNpcId() != freyaStand)
						{
							mob.deleteMe();
							InstanceManager.getInstance().getInstance(instanceId).getNpcs().remove(mob);
						}
					}
					for (int objId : world.allowed)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(objId);
						QuestState st = player.getQuestState("Q10286_ReunionWithSirra");
						if (st != null && st.getState() == State.STARTED && st.getInt("progress") == 2)
						{
							st.set("cond", "7");
							st.playSound("ItemSound.quest_middle");
							st.set("progress", "3");
						}
					}
					break;
				default:
					System.out.println("Warning!!! Not handled world status - " + statusId);
					break;
			}
		}
		world.status = statusId;
	}
	
	private L2PcInstance getRandomPlayer(FreyaWorld world)
	{
		boolean exists = false;
		while (!exists)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(world.allowed.get(Rnd.get(0, world.allowed.size()-1)));
			if (player != null)
			{
				exists = true;
				return player;
			}
		}
		return null;
	}
	
	private FreyaWorld getWorld(int instanceId)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(instanceId);
		FreyaWorld world = null;
		if (tmpworld instanceof FreyaWorld)
			world = (FreyaWorld) tmpworld;
			
		if (world == null)
			System.out.println("Warning!!! World not found in getWorld(int instanceId)");
		return world;
	}
	
	private int getWorldStatus(L2PcInstance player)
	{
		return getWorld(player).status;
	}

	private FreyaWorld getWorld(L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		FreyaWorld world = null;
		if (tmpworld instanceof FreyaWorld)
			world = (FreyaWorld) tmpworld;
		
		if (world == null)
			System.out.println("Warning!!! World not found in getWorld(int instanceId)");		
		return world;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		//hard
		if(_isHard)
		{
			if (npcId == archery_knight_hard)
			{
				if (npc.getDisplayEffect() == 1)
					npc.setDisplayEffect(2);
				
				if (getWorldStatus(attacker) == 2)
				{
					handleWorldState(10, attacker.getInstanceId());
				}
				else if (getWorldStatus(attacker) == 20)
				{
					handleWorldState(21, attacker.getInstanceId());
				}
			}
			else if (npcId == freyaStand_hard)
			{
				double cur_hp = npc.getCurrentHp();
				double max_hp = npc.getMaxHp();
				int percent = (int) Math.round((cur_hp/max_hp) * 100);
				if (percent <= 20 && getWorldStatus(attacker) < 40)
				{
					handleWorldState(40, attacker.getInstanceId());
				}
			}
		}
		//hard - end
		else
		{
			if (npcId == archery_knight)
			{
				if (npc.getDisplayEffect() == 1)
					npc.setDisplayEffect(2);
				
				if (getWorldStatus(attacker) == 2)
				{
					handleWorldState(10, attacker.getInstanceId());
				}
				else if (getWorldStatus(attacker) == 20)
				{
					handleWorldState(21, attacker.getInstanceId());
				}
			}
			else if (npcId == freyaStand)
			{
				double cur_hp = npc.getCurrentHp();
				double max_hp = npc.getMaxHp();
				int percent = (int) Math.round((cur_hp/max_hp) * 100);
				if (percent <= 20 && getWorldStatus(attacker) < 40)
				{
					handleWorldState(40, attacker.getInstanceId());
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		//hard
		if(_isHard)
		{
			FreyaWorld world = getWorld(killer);
			if (npcId == glacier)
			{
				if (world != null)
				{
					world._glaciers.remove(npc.getObjectId());
				}
			}
			else if (npcId == archery_knight_hard && world != null)
			{
				if (world._archery_knights_hard.containsKey(npc.getObjectId()))
				{
					world._archery_knights_hard.remove(npc.getObjectId());
					
					if (world.status > 20 && world.status < 24)
					{
						if (world._archery_knights_hard.size() == 0)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(6, killer.getInstanceId()), 8000);
						}
					}
					else if (world.status < 44)
						ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(3, killer.getInstanceId()), (Rnd.get(10, 40) * 1000) + 20000);
				}
				else if (world._simple_knights.containsKey(npc.getObjectId()))
				{
					world._simple_knights.remove(npc.getObjectId());
					startQuestTimer("spawndeco_"+npc.getSpawn().getLocx()+"_"+npc.getSpawn().getLocy()+"_"+npc.getSpawn().getLocz()+"_"+npc.getSpawn().getHeading()+"_"+npc.getInstanceId(), 20000, null, null);
				}
			}
			else if (npcId == freyaOnThrone)
			{
				handleWorldState(11, killer.getInstanceId());
			}
			else if (npcId == Glakias_hard)
			{
				handleWorldState(29, killer.getInstanceId());
			}
			else if (npcId == freyaStand_hard)
			{
				handleWorldState(44, killer.getInstanceId());
			}
		}
		//Hard - end
		else
		{
			FreyaWorld world = getWorld(killer);
			if (npcId == glacier)
			{
				if (world != null)
				{
					world._glaciers.remove(npc.getObjectId());
				}
			}
			else if (npcId == archery_knight && world != null)
			{
				if (world._archery_knights.containsKey(npc.getObjectId()))
				{
					world._archery_knights.remove(npc.getObjectId());
					
					if (world.status > 20 && world.status < 24)
					{
						if (world._archery_knights.size() == 0)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(6, killer.getInstanceId()), 8000);
						}
					}
					else if (world.status < 44)
						ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(3, killer.getInstanceId()), (Rnd.get(10, 40) * 1000) + 20000);
				}
				else if (world._simple_knights.containsKey(npc.getObjectId()))
				{
					world._simple_knights.remove(npc.getObjectId());
					startQuestTimer("spawndeco_"+npc.getSpawn().getLocx()+"_"+npc.getSpawn().getLocy()+"_"+npc.getSpawn().getLocz()+"_"+npc.getSpawn().getHeading()+"_"+npc.getInstanceId(), 20000, null, null);
				}
			}
			else if (npcId == freyaOnThrone)
			{
				handleWorldState(11, killer.getInstanceId());
			}
			else if (npcId == Glakias)
			{
				handleWorldState(29, killer.getInstanceId());
			}
			else if (npcId == freyaStand)
			{
				handleWorldState(44, killer.getInstanceId());
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		FreyaWorld world = getWorld(npc.getInstanceId());
		if (world != null && world.status >= 44)
		{
			npc.deleteMe();
		}
		if (world != null && world.isMovieNow && npc instanceof L2Attackable)
		{
			npc.abortAttack();
			npc.abortCast();
			npc.setIsImmobilized(true);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		_log.info("id_talk=" + npc.getNpcId());
		if( npc.getNpcId() == 32781 || npc.getNpcId() == 32777 )
			return npc.getNpcId() + ".htm";
		else
			return null;
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		FreyaWorld world = getWorld(player);
		if (world != null)
		{
			world._freya_controller.deleteMe();
			world._freya_controller = null;
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(2, world.instanceId), 100);
			handleWorldState(31, world.instanceId);
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		_log.info("id=" + npc.getNpcId());
		if( npc.getNpcId() == 32781 || npc.getNpcId() == 32777 )
			return npc.getNpcId() + ".htm";
		else
			return null;
	}
	
	private void enterInstance(L2PcInstance player, String template)
	{
		_log.info("starter=" + player.getName());
		int instanceId = 0;
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if (world != null)
		{
			if (!(world instanceof FreyaWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			teleportPlayer(player,(FreyaWorld)world);
			return;
		}
		//New instance
		else
		{
			if (!checkConditions(player))
				return;
			L2Party party = player.getParty();
			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new FreyaWorld();

			world.instanceId = instanceId;
			world.templateId = INSTANCE_ID;
			world.status = 0;
				
			InstanceManager.getInstance().addWorld(world);
			_log.info("Freya started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
			
			if ((debug) || (player.isGM()))
			{
				QuestState qs = player.getQuestState("Q10286_ReunionWithSirra");
				if (qs != null)
					if (qs.getInt("cond") == 5)
					{
						qs.set("cond", "6");
						qs.playSound("ItemSound.quest_middle");
					}
				world.allowed.add(player.getObjectId());
				teleportPlayer(player,(FreyaWorld)world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(1, world.instanceId), 100);
				return;
			}
			
			if (party != null && party.isInCommandChannel())
			{
				for (L2PcInstance plr : party.getCommandChannel().getMembers())
				{
					QuestState qs = plr.getQuestState("Q10286_ReunionWithSirra");
					if (qs != null)
					{
						if (qs.getInt("cond") == 5)
						{
							qs.set("cond", "6");
							qs.playSound("ItemSound.quest_middle");
						}
					}
					world.allowed.add(plr.getObjectId());
					teleportPlayer(plr,(FreyaWorld)world);
				}
				
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(1, world.instanceId), 100);
				return;
			}
		}
	}
	
    private boolean checkConditions(L2PcInstance player)
    {
    	if ((debug) || (player.isGM()))
    		return true;
    	
    	if (player.getParty() == null)
    	{
    		player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
    		return false;
    	}
    	
    	if (player.getParty().getCommandChannel() == null)
    	{
    		player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
    		return false;
    	}
    	
    	if (player.getObjectId() != player.getParty().getCommandChannel().getChannelLeader().getObjectId())
    	{
    		player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
    		return false;
    	}
    	//hard
        if( _isHard )
        {
	    	if (player.getParty().getCommandChannel().getMemberCount() < Config.MIN_FREYA_HC_PLAYERS)
	    	{
	    		player.getParty().getCommandChannel().broadcastToChannelMembers(SystemMessage.getSystemMessage(2793).addNumber(10));
	    		return false;
	    	}
	    	
	    	if (player.getParty().getCommandChannel().getMemberCount() > Config.MAX_FREYA_HC_PLAYERS)
	    	{
	    		player.getParty().getCommandChannel().broadcastToChannelMembers(SystemMessage.getSystemMessage(2102));
	    		return false;
	    	}
        }
    	//hard - end
        else
        {
	    	if (player.getParty().getCommandChannel().getMemberCount() < Config.MIN_FREYA_PLAYERS)
	    	{
	    		player.getParty().getCommandChannel().broadcastToChannelMembers(SystemMessage.getSystemMessage(2793).addNumber(10));
	    		return false;
	    	}
	    	
	    	if (player.getParty().getCommandChannel().getMemberCount() > Config.MAX_FREYA_PLAYERS)
	    	{
	    		player.getParty().getCommandChannel().broadcastToChannelMembers(SystemMessage.getSystemMessage(2102));
	    		return false;
	    	}
        }
    	
    	for (L2PcInstance partyMember : player.getParty().getCommandChannel().getMembers())
    	{
    		//hard
            if( _isHard )
            {
	            if (partyMember.getLevel() < Config.MIN_LEVEL_HC_PLAYERS)
	            {
	                    SystemMessage sm = SystemMessage.getSystemMessage(2097);
	                    sm.addPcName(partyMember);
	                    player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
	                    return false;
	            }
            }
            //hard end
            else
            {
	            if (partyMember.getLevel() < Config.MIN_LEVEL_PLAYERS)
	            {
	                    SystemMessage sm = SystemMessage.getSystemMessage(2097);
	                    sm.addPcName(partyMember);
	                    player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
	                    return false;
	            }
            }
            
            if (!Util.checkIfInRange(1000, player, partyMember, true))
            {
                    SystemMessage sm = SystemMessage.getSystemMessage(2096);
                    sm.addPcName(partyMember);
                    player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
                    return false;
            }
            
            Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCE_ID);
            if (System.currentTimeMillis() < reentertime)
            {
                    SystemMessage sm = SystemMessage.getSystemMessage(2100);
                    sm.addPcName(partyMember);
                    player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
                    return false;
            }
            
            if( _isHard )
            {
	    		QuestState st = partyMember.getQuestState("Q10286_ReunionWithSirra");
	    		if( st == null || !st.isCompleted() )
	    		{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(partyMember);
					player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
	    			return false;
	    		}
            }
            
    	}
    	
        return true;
    }
    
	private void teleportPlayer(L2PcInstance player, FreyaWorld world)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(world.instanceId);
		player.teleToLocation(113991, -112297, -11200);
		if(player.getPet() != null)
		{
			player.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.getPet().setInstanceId(world.instanceId);
			player.getPet().teleToLocation(113991, -112297, -11200);
		}			
		return;
	}
	
	private void setInstanceRestriction(FreyaWorld world)
	{
        Calendar reenter = Calendar.getInstance();
        reenter.set(Calendar.MINUTE, 30);
        reenter.set(Calendar.HOUR_OF_DAY, 6);
        // if time is >= RESET_HOUR - roll to the next day
        if (reenter.getTimeInMillis() <= System.currentTimeMillis())
        	reenter.add(Calendar.DAY_OF_MONTH, 1);
        if (reenter.get(Calendar.DAY_OF_WEEK) <= Calendar.WEDNESDAY)
        	while(reenter.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY)
        		reenter.add(Calendar.DAY_OF_MONTH, 1);
        else
        	while(reenter.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
        		reenter.add(Calendar.DAY_OF_MONTH, 1);


        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
        sm.addString(InstanceManager.getInstance().getInstanceIdName(INSTANCE_ID));
        
        // set instance reenter time for all allowed players
        for (int objectId : world.allowed)
        {
                L2PcInstance player = L2World.getInstance().getPlayer(objectId);
                InstanceManager.getInstance().setInstanceTime(objectId, INSTANCE_ID, reenter.getTimeInMillis());
                if (player != null && player.isOnline())
                        player.sendPacket(sm);
        }
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		_log.info(event);
		if( event.equalsIgnoreCase("easy") && npc.getNpcId() == 32781 )
		{
			_log.info("Freya EASY started");
			enterInstance(player, "IceQueenCastle2.xml");
		}
		else if ( event.equalsIgnoreCase("hard") && npc.getNpcId() == 32777 )
		{
			_log.info("Freya HARD started");
			_isHard = true;
			enterInstance(player, "IceQueenCastle2.xml");
		}
		else if(event.startsWith("spawndeco"))
		{
			String[] params = event.split("_");
			FreyaWorld world = getWorld(Integer.parseInt(params[5]));
			//hard
			if (_isHard)
			{
				if (world != null && world.status < 44)
				{
					L2Npc mob = spawnNpc(archery_knight_hard, Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]), Integer.parseInt(params[4]), Integer.parseInt(params[5]));
					mob.setIsImmobilized(true);
					mob.setDisplayEffect(1);
					world._simple_knights.put(mob.getObjectId(), mob);
				}
			}
			//hard - end
			else
			{
				if (world != null && world.status < 44)
				{
					L2Npc mob = spawnNpc(archery_knight, Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]), Integer.parseInt(params[4]), Integer.parseInt(params[5]));
					mob.setIsImmobilized(true);
					mob.setDisplayEffect(1);
					world._simple_knights.put(mob.getObjectId(), mob);
				}
			}
		}
		/*else if (event.equalsIgnoreCase("enterinstance"))
		{
			enterInstance(player, "IceQueenCastle2.xml");
		}*/
		return null;
	}
	
	private int[] getRandomPoint(int min_x, int max_x, int min_y, int max_y)
	{
		int[] ret = {0,0};
		ret[0] = Rnd.get(min_x, max_x);
		ret[1] = Rnd.get(min_y, max_y);
		return ret;
	}
	
	private L2Npc spawnNpc(int npcId, int x, int y, int z, int heading, int instId)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		Instance inst = InstanceManager.getInstance().getInstance(instId);
		try
		{
			L2Spawn npcSpawn = new L2Spawn(npcTemplate);
			npcSpawn.setLocx(x);
			npcSpawn.setLocy(y);
			npcSpawn.setLocz(z);
			npcSpawn.setHeading(heading);
			npcSpawn.setAmount(1);
			npcSpawn.setInstanceId(instId);
			SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
			L2Npc npc = npcSpawn.spawnOne(false);
			inst.addNpc(npc);
			return npc;
		}
		catch(Exception ignored)
		{
		}
		return null;
	}
	
	private void archerySpawn(L2Npc mob)
	{
		((L2Attackable) mob).setOnKillDelay(0);
		mob.setDisplayEffect(1);
		mob.setIsImmobilized(true);
	}
	
	private void archeryAttack(L2Npc mob, FreyaWorld world)
	{
		mob.setDisplayEffect(2);
		mob.setIsImmobilized(false);
		mob.setRunning();
		L2PcInstance victim = getRandomPlayer(world);
		mob.setTarget(victim);
		((L2Attackable) mob).addDamageHate(victim, 0, 9999);
		mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
	}
	
	private void stopAll(FreyaWorld world)
	{
		if (world == null)
			return;
		//hard
		if(_isHard)
		{
			if (world._freyaStand_hard != null && !world._freyaStand_hard.isDead())
			{
				if (world._freyaStand_hard.getTarget() != null)
				{
					world._freyaStand_hard.abortAttack();
					world._freyaStand_hard.abortCast();
					world._freyaStand_hard.setTarget(null);
					world._freyaStand_hard.clearAggroList();
					world._freyaStand_hard.setIsImmobilized(true);
					world._freyaStand_hard.teleToLocation(world._freyaStand_hard.getX() - 100, world._freyaStand_hard.getY() + 100, world._freyaStand_hard.getZ(), world._freyaStand_hard.getHeading(), false);
				}
			}
		}
		//hard - end
		else
		{
			if (world._freyaStand != null && !world._freyaStand.isDead())
			{
				if (world._freyaStand.getTarget() != null)
				{
					world._freyaStand.abortAttack();
					world._freyaStand.abortCast();
					world._freyaStand.setTarget(null);
					world._freyaStand.clearAggroList();
					world._freyaStand.setIsImmobilized(true);
					world._freyaStand.teleToLocation(world._freyaStand.getX() - 100, world._freyaStand.getY() + 100, world._freyaStand.getZ(), world._freyaStand.getHeading(), false);
				}
			}
		}
		
		for (L2Npc mob : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
		{
			if (mob != null && !mob.isDead())
			{
				mob.abortAttack();
				mob.abortCast();
				if (mob instanceof L2Attackable)
					((L2Attackable) mob).clearAggroList();
				mob.setIsImmobilized(true);
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
		
		for (int objId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			player.abortAttack();
			player.abortCast();
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.setIsImmobilized(true);
			player.setIsInvul(true);
		}
	}
	
	private void startAll(FreyaWorld world)
	{
		if (world == null)
			return;
		
		for (L2Npc mob : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
		{
			L2Object target = null;
			
			if (mob.getTarget() != null)
				target = mob.getTarget();
			else
				target = getRandomPlayer(world);

			if (mob.getNpcId() != glacier
					&& !world._simple_knights.containsKey(mob.getObjectId())
					&& mob instanceof L2Attackable)
			{
				((L2Attackable) mob).addDamageHate((L2Character) target, 0, 9999);
				mob.setRunning();
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				mob.setIsImmobilized(false);
			}
		}
		
		for (int objId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			player.setIsImmobilized(false);
			if (player.getFirstEffect(L2EffectType.INVINCIBLE) == null)
			{
				player.setIsInvul(false);
			}
		}
	}
	
	public IceQueenCastle2(int questId, String name, String descr)
	{
		super(questId, name, descr);
		//addFirstTalkId(32781);
		addTalkId(32777);
		addTalkId(32781);
		
		addStartNpc(32781);
		addFirstTalkId(32781);
		addStartNpc(32777);
		addFirstTalkId(32777);
		
		addKillId(Glakias);
		addAggroRangeEnterId(freya_controller);
		addAttackId(archery_knight);
		addAttackId(freyaStand);
		addKillId(freyaOnThrone);
		addKillId(freyaStand);
		addKillId(freyaSpelling);
		addKillId(archery_knight);
		addKillId(glacier);
		addSpawnId(archery_knight);
		addSpawnId(18854);
		addSpawnId(glacier);
		//hard
		addKillId(Glakias_hard);
		addAttackId(archery_knight_hard);
		addAttackId(freyaStand_hard);
		addKillId(freyaStand_hard);
		addKillId(archery_knight_hard);
		addSpawnId(archery_knight_hard);
		//hard - end
	}
	
	public static void main(String[] args)
	{
		new IceQueenCastle2(-1,qn,"zone_scripts/Freya/IceQueenCastle2");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Freya: Ice Queen Castle 2");
	}
}