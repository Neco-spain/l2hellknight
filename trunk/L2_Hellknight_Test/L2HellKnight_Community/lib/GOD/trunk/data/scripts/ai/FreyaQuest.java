package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.listeners.L2ZoneEnterLeaveListener;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.ExStartScenePlayer;
import l2rt.gameserver.network.serverpackets.PlaySound;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.Location;
import l2rt.util.Rnd;

/**
 * AI для мобов в квесте на фрею
 * @author DarkShadow74 ^_^
 */
public class FreyaQuest extends Fighter
{
	private L2NpcInstance target;
	private long _lastTarget;
	private int Eternal_Blizzard = 6276;
	private boolean say = false;
	private boolean first = true;
    private L2Zone _zone = ZoneManager.getInstance().getZoneById(ZoneType.epic, 702122, false);
    private ZoneListener _zoneListener = new ZoneListener();
	private static final int targets[] = { 18848, 18849, 18926 };
	//private static final int targets2[] = { 22767, 18847 };
	
	public FreyaQuest(L2Character actor)
	{
		super(actor);
		AI_TASK_DELAY = 1000;
		AI_TASK_ACTIVE_DELAY = 1000;
	}

    @Override
    protected void onEvtSpawn() 
	{
    	if(first)
		{
    		getActor().setImobilised(true);
			first = false;
		}
    }
	
    private class ZoneListener extends L2ZoneEnterLeaveListener 
	{

        @Override
        public void objectEntered(L2Zone zone, L2Object object) 
		{
			L2Player player = object.getPlayer();
			if(player == null)
				return;
				
			ThreadPoolManager.getInstance().scheduleGeneral(new Events(1), 3000L); //Create_Timer
			ThreadPoolManager.getInstance().scheduleGeneral(new Events(5), 3000); //VoiceNPCEffect
    		getActor().setImobilised(false);
        }

        @Override
        public void objectLeaved(L2Zone zone, L2Object object) 
		{}
    }

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return true;
			
		if(System.currentTimeMillis() - _lastTarget > 1000)
		{
        	_lastTarget = System.currentTimeMillis();
    		switch (getActor().getNpcId())
    		{
	    		case 22767:
	    		case 18847:
	    			if(target == null)
					{
    					for(L2NpcInstance npc : L2ObjectsStorage.getAllByNpcId(targets[Rnd.get(targets.length)], true))
    	    		    	if(npc.getReflection().getId() == actor.getReflection().getId())
    	    		    	{
	        					npc.addDamageHate(actor, 0, 1);
	        					target = npc;
    	    		    	}
					}
					if(target != null)
					{
        		    	setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
        		    	target = null;//Обнуляем таргет чтобы бил и игроков тоже
					}
	    			break;
	    		case 18848:
	    		case 18849:
	    		case 18926:
	    			if(target == null)
					{
    					for(L2NpcInstance npc : L2ObjectsStorage.getAllByNpcId(22767, true)) //Чёто тупо...
    	    		    	if(npc.getReflection().getId() == actor.getReflection().getId())
    	    		    	{
	        					npc.addDamageHate(actor, 0, 1);
	        					target = npc;
    	    		    	}
					}
					if(target != null)
					{
        		    	setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
        		    	target = null;//Обнуляем таргет чтобы бил и игроков тоже
					}
	    			break;
    		}
		}
		actor.setRunning(); // всегда бегают
		return false;
    }
	
	private class Events implements Runnable
	{
	    private int _id;
		//Todo TIMER_moving in progress....
		
        Reflection r = getActor().getReflection();
		
		public Events(int id)
		{
    		_id = id;
		}
		
		public void run()
		{
    		switch(_id)
			{
			    //Start Quest Instance
    			case 1:
        			L2NpcInstance guard = L2ObjectsStorage.getByNpcId(18848);
        			Location sloc = guard.getSpawnedLoc();
        			for(L2Player player : r.getPlayers())
            		    if(guard != null && sloc.x == 114861 && !say)
            		    {
            				say = true;
            				Functions.npcSay(guard, player.getName() + ". May the protection of the gods be upon you!");
            		    }
        			r.closeDoor(23140101);					
        			ThreadPoolManager.getInstance().scheduleGeneral(new Events(2), 3 * 60 * 1000L); //TIMER_Blizzard
    				break;
			    //Freya Blizzard Timer
    			case 2:
				    L2NpcInstance freya = L2ObjectsStorage.getByNpcId(18847);
				    Functions.npcShout(freya, "I can no longer stand by.");
				    L2Skill skill = SkillTable.getInstance().getInfo(Eternal_Blizzard, 1);
        			for(L2Player player : r.getPlayers())
            		    if(player != null)
            				AddUseSkillDesire(player, skill, 1000000000);
        			ThreadPoolManager.getInstance().scheduleGeneral(new Events(3), 1000L); //On UseSkillFinished - Blizzard Start Timer TIMER_SCENE_21
    				break;
			    //Movie And End Instance
    			case 3:
    				r.clearReflection(2,false);
        			for(L2Player player : r.getPlayers())
        				if(player != null)
             				player.showQuestMovie(ExStartScenePlayer.SCENE_SC_BOSS_FREYA_FORCED_DEFEAT);
					ThreadPoolManager.getInstance().scheduleGeneral(new Events(4), 24 * 1000L); //Timer To Pc Leave Instance
    				break;
    			case 4:
        			for(L2Player pl : r.getPlayers())
        				if(pl != null)
						{
             				QuestState qs = pl.getQuestState("_10285_MeetingSirra");
							if(qs != null && qs.isStarted() && qs.getCond() == 9)
							{
								qs.setCond(10);
								qs.playSound(Quest.SOUND_MIDDLE);
							}
							if(r.getReturnLoc() != null)
	                     	    pl.teleToLocation(r.getReturnLoc(), 0);
							pl.setVar(r.getName(), String.valueOf(System.currentTimeMillis() - 1380000));
							r.collapse();
						}
    				break;
				//Freya Voice Timers
    			case 5:
        			for(L2Player player : r.getPlayers())
        				if(player != null)
         					player.sendPacket(new PlaySound("Freya.freya_voice_01"));
					ThreadPoolManager.getInstance().scheduleGeneral(new Events(6), 20 * 1000L); //Timer To next Voice
    				break;
    			case 6:
        			for(L2Player player : r.getPlayers())
        				if(player != null)
         					player.sendPacket(new PlaySound("Freya.freya_voice_02"));
    				break;
			}

		}
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		
		if(actor.getNpcId() != 22767 || actor.getNpcId() != 18847 || attacker.isPlayer())
	    	return;
			
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtAggression(L2Character attacker, int aggro)
	{
		L2NpcInstance actor = getActor();
		
		if(actor.getNpcId() != 22767 || actor.getNpcId() != 18847 || attacker.isPlayer())
	    	return;
			
		super.onEvtAggression(attacker, aggro);
	}

	/**
	 * Запуск АИ
	 */
	@Override
	public void startAITask()
	{
		if(_aiTask == null)
		{
			_zone.getListenerEngine().addMethodInvokedListener(_zoneListener);
		}
		super.startAITask();
	}

	/**
	 * Остановка АИ.
	 */
	@Override
	public void stopAITask()
	{
		if(_aiTask != null)
		{
			_zone.getListenerEngine().removeMethodInvokedListener(_zoneListener);
		}
		super.stopAITask();
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}