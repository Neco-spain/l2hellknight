package ai;


import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Location;
import l2rt.util.Rnd;

/**
 * @author Drizzy
 * @date 18.10.10  
 * @AI for location Sel Mahum Training Ground`s
 */
public class SelMahum extends Fighter
{
	//счётчики.
	// 1 state 18927
	private static final long CHANGE_PERIOD = 20 * 60 * 1000; //20 мин
	private long _changeperiod = System.currentTimeMillis();
	// respawn 18933 to 18927
	private static final long CHANGE_PERIOD2 = 15 * 60 * 1000; //15 мин
	private long _changeperiod2 = System.currentTimeMillis();
	// wait time for state 1
	private static final long CHANGE_STATE = 2 * 60 * 1000; //2 мин
	private long _changestate = System.currentTimeMillis();	
	// wait for state 1 end
	private static final long CHANGE_STATE_END = 30 * 1000; //30 сек
	private long _changestateend = System.currentTimeMillis();	
	
	//boolean	
	public boolean change = false;
	public boolean change1 = false;
	public boolean change2 = false;
	public boolean statemob = false;
	public boolean statemob1 = false;
	public boolean starttimer = false;
	public boolean timer = false;
	public boolean changecook = false;
	public boolean changecookstate = false;	
	public boolean state2 = false;

    //message
	private static final String[] say = {
			"Let's go eat.",
			"Looks delicious." };
	
	//other
	private L2NpcInstance mob;
	
	public SelMahum(L2Character actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean randomAnimation()
	{
		return false;
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}	
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected boolean thinkActive()
	{	
		L2NpcInstance actor = getActor();
		int id = getActor().getNpcId();
		int state = actor.getNpcState();
		
		if (id == 18927)
		{
			// Если рядом есть нпс с ид 18933. Останавливаем скрипт для 18927.
			if (state == 1)
			{
				for(L2NpcInstance koster : L2World.getAroundNpc(actor, 50, 200))
				{
					if (koster.getNpcId() == 18933)
					{
						return true;
					}
				}
			}
			// Если state меньше 1 или = 2, запускаем таймер.						
			if (state == 2 || state < 1)
			{
				if (!timer)
				{
					_changeperiod = System.currentTimeMillis();
					timer = true;
					change1 = false;					
				}					
				// Если кончилось время таймера, меняем state.
				if (!change && _changeperiod + CHANGE_PERIOD < System.currentTimeMillis())
				{
					changestate(actor, 1);
					statemob = false;
					change = true;
					timer = false;
				}
				// Если кончился таймер, то меняет стейт на 3.
				if (_changestateend + CHANGE_STATE_END < System.currentTimeMillis())
				{
					for(L2NpcInstance npc : L2World.getAroundNpc(actor, 500, 200))
					{
						if (npc.getNpcId() == 22786 || npc.getNpcId() == 22787 || npc.getNpcId() == 22788)
						{
							if (!statemob1)
							{
								changestate(npc, 3);
								starttimer = false;
								statemob1 = true;
							}
						}					
					}
					_changestateend = 0;
				}
			}
			// Если state = 1 запускаем таймер.
			if (state == 1)
			{
				if (!change1)
				{
					change = false;
					change1 = true;	
					change2 = false;
				}
				for(L2NpcInstance npc : L2World.getAroundNpc(actor, 500, 200))
				{
					if (npc.getNpcId() == 18908)
					{
						changecookstate = true;					
					}			
				}
				//Если рядом с актором есть нпс и их ид 22786-22788. То меняет стейт + запускаем таймер на окончание стейта.
				for(L2NpcInstance npc : L2World.getAroundNpc(actor, 500, 200))
				{
					if (npc.getNpcId() == 22786 || npc.getNpcId() == 22787 || npc.getNpcId() == 22788)
					{
						if (!statemob || _changestate + CHANGE_STATE < System.currentTimeMillis())
						{	
							changestate(npc, 2);
							mob = npc;
							//если болеан фалс, запускаем таймер на окончание state 1 у мобов.
							if (!starttimer)
							{								
								_changestateend = System.currentTimeMillis();
								starttimer = true;
								statemob1 = false;
								statemob = true;
								_changestate = 0;
							}	
						}
						// Если кончился таймер, то меняет стейт на 3.
						if (_changestateend + CHANGE_STATE_END < System.currentTimeMillis())
						{
							changestate(npc, 3);
							if (!statemob1)
							{
								_changestate = System.currentTimeMillis();
								starttimer = false;
								statemob1 = true;
							}
						}
					}
				}
				// если переменная верна то спауним 18933.
				if (changecookstate)
				{
					try
					{
						L2Spawn sp = new L2Spawn(NpcTable.getTemplate(18933));
						Location pos = GeoEngine.findPointToStay(actor.getX(), actor.getY(), actor.getZ(), 0, 0, actor.getReflection().getGeoIndex());
						sp.setLoc(pos);
						sp.doSpawn(true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					changecookstate = false;	
				}
			}
		}
		if (id == 18933)
		{	
			//если болеан фалсе. ставим текущее время
			if (!change2)
			{
				_changeperiod2 = System.currentTimeMillis();
				change2 = true;
			}
			// если кончился счётчик, удаляем спаун 18933 и меняет стейт 18927.
			if (_changeperiod2 + CHANGE_PERIOD2 < System.currentTimeMillis())
			{
				for(L2NpcInstance fire : L2World.getAroundNpc(actor, 100, 200))
				{
					if (fire.getNpcId() == 18927)
					{
						changestate(fire, 2);
					}
				}				
				actor.decayMe();
			}
			
			//Если рядом нпс 18908 меняем болеан.
			for(L2NpcInstance cook : L2World.getAroundNpc(actor, 100, 200))
			{
				if (cook.getNpcId() == 18908)
				{
					changecook = true;							
				}
			}	
			
			//Если рядом нпс с ид 22786-22788, запускаем стейт и таймер на окончание.
			for(L2NpcInstance npc : L2World.getAroundNpc(actor, 500, 200))
			{
				if (npc.getNpcId() == 22786 || npc.getNpcId() == 22787 || npc.getNpcId() == 22788)
				{
					//если болеан тру, меняем стейт на 1 + таймер.
					if (changecook)
					{
						state2 = true;
						_changestateend = System.currentTimeMillis();
						statemob1 = false;
						changecook = false;
                        Functions.npcSay(npc, say[Rnd.get(say.length)]);

                        Location sloc = actor.getSpawnedLoc();

                        int x = sloc.x + Rnd.get(30,60) - 5;
                        int y = sloc.y + Rnd.get(30,60) - 5;
                        int z = GeoEngine.getHeight(x, y, sloc.z, actor.getReflection().getGeoIndex());

                        npc.setRunning();
                        npc.moveToLocation(x, y, z, 0, true);
                        //TODO: сидение мобов=)
						changestate(npc, 1);
					}
					if (!statemob && !state2 || _changestate + CHANGE_STATE < System.currentTimeMillis() && !state2)
					{	
						changestate(npc, 2);
						mob = npc;
						//если болеан фалс, запускаем таймер на окончание state 2 у мобов.
						if (!starttimer)
						{								
							_changestateend = System.currentTimeMillis();
							starttimer = true;
							statemob1 = false;
							statemob = true;
							_changestate = 0;
						}	
					}
					// Если кончился таймер, то меняет стейт на 3.
					if (_changestateend + CHANGE_STATE_END < System.currentTimeMillis())
					{
						changestate(npc, 3);
						if (!statemob1)
						{
							_changestate = System.currentTimeMillis();
							starttimer = false;
							statemob1 = true;
							state2 = false;
						}
					}	
				}
			}		
		}
		return super.thinkActive();				
	}

	private void changestate(L2NpcInstance actor, int state)
	{
		actor.setNpcState(state);	
	}
}